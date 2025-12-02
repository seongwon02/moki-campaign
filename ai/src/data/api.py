import numpy as np
import pandas as pd
from sklearn.ensemble import RandomForestRegressor
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List, Optional
import uvicorn
from fastapi.middleware.cors import CORSMiddleware

# ---------------- 요청(Request) 모델 ----------------
class CustomerDataInput(BaseModel):
    customer_id: str
    amount: float
    total_visits: int
    days_since_last_visit: int
    visits_8_week_ago: int
    visits_7_week_ago: int
    visits_6_week_ago: int
    visits_5_week_ago: int
    visits_4_week_ago: int
    visits_3_week_ago: int
    visits_2_week_ago: int
    visits_1_week_ago: int

class CustomerRequest(BaseModel):
    data: List[CustomerDataInput]

# ---------------- 응답(Response) 모델 ----------------
class CustomerDataOutput(BaseModel):
    customer_id: str
    customer_segment: str
    predicted_loyalty_score: float
    churn_risk_score: float  # 디버깅 및 분석용

class CustomerResponse(BaseModel):
    result: List[CustomerDataOutput]

# ---------- 2. FastAPI 앱 초기화 ----------
app = FastAPI(
    title="MOKI 고객 분석 AI API",
    description="고객 데이터를 받아 충성도와 이탈 위험도를 분석합니다."
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ---------- 3. 분석용 유틸 함수 ----------
def minmax_series(s):
    """기본 Min-Max 정규화"""
    mn, mx = s.min(), s.max()
    if mx == mn:
        return s.apply(lambda _: 0.5)
    return (s - mn) / (mx - mn)

def log_minmax_series(s):
    """
    [핵심] 로그 변환 후 정규화
    - 데이터 쏠림 현상 방지
    """
    s_log = np.log1p(s.astype(float))
    return minmax_series(s_log)

def inv_minmax_series(s):
    """역수 변환 (작을수록 점수 높음)"""
    arr = 1.0 / (1.0 + s.astype(float))
    return minmax_series(pd.Series(arr))

def compute_initial_loyalty_score(df, weights=None):
    """
    [1단계: AI 학습용 정답지 생성 - Log Scale 적용]
    - Recency(40), Visits(30), Spend(30)
    """
    if weights is None:
        weights = {"total_visits": 0.3, "avg_spend": 0.3, "recency_inv": 0.4}
    
    df = df.copy()
    
    # 평균 지출 금액 계산
    df["avg_spend"] = df.apply(lambda x: x["amount"] / x["total_visits"] if x["total_visits"] > 0 else 0, axis=1)

    # 로그 변환 + 정규화
    total_visits_n = log_minmax_series(df["total_visits"])
    avg_spend_n = log_minmax_series(df["avg_spend"])
    
    # Recency: 작을수록 좋음 -> 로그 변환 후 뒤집기 (1 - 값)
    recency_log = np.log1p(df["days_since_last_visit"].astype(float))
    recency_norm = minmax_series(recency_log)
    recency_inv_n = 1.0 - recency_norm 
    
    # 가중 합산
    weighted = (
        total_visits_n * weights["total_visits"]
        + avg_spend_n * weights["avg_spend"]
        + recency_inv_n * weights["recency_inv"]
    )
    
    return np.clip(weighted, 0.0, 1.0)

def try_train_model_and_predict(df, feature_cols, label_col="initial_loyalty_score"):
    """
    [AI 모델 학습]
    - 규칙 기반 점수(initial_loyalty_score)를 정답으로 학습
    - Random Forest로 최종 점수 예측
    """
    if label_col not in df.columns or df[label_col].isnull().all():
        return None
    
    # 학습 데이터 준비
    X = df[feature_cols].fillna(0).astype(float)
    y = df[label_col].astype(float)
    
    # 데이터가 너무 적으면(10개 미만) 규칙 점수 그대로 사용
    if len(X) < 10:
        return None

    # 모델 학습
    model = RandomForestRegressor(n_estimators=200, random_state=42, max_depth=5)
    model.fit(X, y)
    
    # 예측
    preds = model.predict(X)
    return np.clip(preds, 0.0, 1.0)

def compute_churn_risk_score(df, weights=None):
    """
    [이탈 위험도 점수 - Decline 중심 + Log Scale]
    - 가중치: Recency(0.2), Trend(0.3), Decline(0.5)
    """
    if weights is None:
        weights = {"recency_n": 0.2, "weighted_trend_inv_n": 0.3, "visit_decline_n": 0.5}
        
    df = df.copy()
    # 결측치 채우기
    for w in range(1, 9):
        col = f"visits_{w}_week_ago"
        if col not in df.columns:
            df[col] = 0.0
        df[col] = df[col].fillna(0.0)
    df["days_since_last_visit"] = df["days_since_last_visit"].fillna(0.0)

    # 1. Recency (60일 기준 Clipping + MinMax)
    clipped_recency = df["days_since_last_visit"].clip(upper=60)
    recency_n = minmax_series(clipped_recency.astype(float))

    # 2. Activity Trend
    weights_recent = {
        "visits_1_week_ago": 8.0, "visits_2_week_ago": 7.0,
        "visits_3_week_ago": 6.0, "visits_4_week_ago": 5.0,
        "visits_5_week_ago": 4.0, "visits_6_week_ago": 3.0,
        "visits_7_week_ago": 2.0, "visits_8_week_ago": 1.0,
    }
    weighted_score = pd.Series(np.zeros(len(df)), index=df.index, dtype=float)
    for col, weight in weights_recent.items():
        weighted_score += df[col] * weight
    
    # Trend는 활동 적을수록 위험 -> 역수 변환
    weighted_inv = 1.0 / (1.0 + weighted_score)
    weighted_trend_inv_n = minmax_series(weighted_inv)

    # 3. Visit Decline (로그 변환 적용)
    recent_4w_sum = (df["visits_1_week_ago"] + df["visits_2_week_ago"] + 
                     df["visits_3_week_ago"] + df["visits_4_week_ago"])
    past_4w_sum = (df["visits_5_week_ago"] + df["visits_6_week_ago"] + 
                   df["visits_7_week_ago"] + df["visits_8_week_ago"])
    
    decline_score = (past_4w_sum - recent_4w_sum).clip(lower=0)
    visit_decline_n = log_minmax_series(decline_score) # [핵심] 로그 적용

    churn_scores = (
        recency_n * weights["recency_n"]
        + weighted_trend_inv_n * weights["weighted_trend_inv_n"]
        + visit_decline_n * weights["visit_decline_n"]
    )
    return minmax_series(churn_scores)

# ---------- 4. 고객 데이터 분석 (메인 로직) ----------
def analyze_customer_data(df: pd.DataFrame) -> pd.DataFrame:
    # -----------------------------------------------------------------
    # [1] 활성/비활성 고객 분리 (Active vs Inactive)
    # -----------------------------------------------------------------
    # 60일 이상 미방문 고객은 분석 분포를 왜곡하므로 별도 처리
    ACTIVE_THRESHOLD = 60
    
    if "days_since_last_visit" not in df.columns:
        df["days_since_last_visit"] = 0
    
    mask_active = df["days_since_last_visit"] <= ACTIVE_THRESHOLD
    
    df_active = df[mask_active].copy()
    df_inactive = df[~mask_active].copy()
    
    # -----------------------------------------------------------------
    # [2] 활성 고객(Active) 분석 수행
    # -----------------------------------------------------------------
    if not df_active.empty:
        # 1. 초기 충성도 점수 (로그 변환 + 규칙 기반)
        df_active["initial_loyalty_score"] = compute_initial_loyalty_score(df_active)
        
        # 2. AI 모델 학습 및 예측 (Random Forest)
        feature_cols = ["total_visits", "days_since_last_visit", "amount", 
                        "visits_1_week_ago", "visits_2_week_ago", "visits_3_week_ago", "visits_4_week_ago"]
        
        learned_preds = try_train_model_and_predict(df_active, feature_cols, label_col="initial_loyalty_score")
        
        if learned_preds is not None:
            df_active["predicted_loyalty_score"] = learned_preds
        else:
            # 데이터 부족 시 규칙 기반 점수 사용
            df_active["predicted_loyalty_score"] = df_active["initial_loyalty_score"]
            
        df_active["predicted_loyalty_score"] = df_active["predicted_loyalty_score"].round(4)
        
        # 3. 이탈 위험도 점수
        df_active["churn_risk_score"] = compute_churn_risk_score(df_active).round(4)
        
        # 4. 세그먼트 할당 (요청하신 임계값 적용)
        LOYALTY_THRESHOLD = 0.60      # 충성도 기준 0.6
        CHURN_RISK_THRESHOLD = 0.40   # 이탈 기준 0.4

        df_active["is_loyal"] = df_active["predicted_loyalty_score"] >= LOYALTY_THRESHOLD
        df_active["is_high_risk"] = df_active["churn_risk_score"] >= CHURN_RISK_THRESHOLD

        conditions = [
            (df_active["is_loyal"] & df_active["is_high_risk"]),      # AT_RISK_LOYAL
            (df_active["is_loyal"] & ~df_active["is_high_risk"]),     # LOYAL
            (~df_active["is_loyal"] & df_active["is_high_risk"]),     # CHURN_RISK
            (~df_active["is_loyal"] & ~df_active["is_high_risk"])     # GENERAL
        ]
        choices = ["AT_RISK_LOYAL", "LOYAL", "CHURN_RISK", "GENERAL"]
        df_active["customer_segment"] = np.select(conditions, choices, default="GENERAL")

    # -----------------------------------------------------------------
    # [3] 비활성 고객(Inactive) 처리
    # -----------------------------------------------------------------
    if not df_inactive.empty:
        # 장기 미방문자는 무조건 이탈 위험군으로 분류
        df_inactive["predicted_loyalty_score"] = 0.0
        df_inactive["churn_risk_score"] = 1.0
        df_inactive["customer_segment"] = "CHURN_RISK"

    # -----------------------------------------------------------------
    # [4] 결과 병합
    # -----------------------------------------------------------------
    result_df = pd.concat([df_active, df_inactive])
    
    # 안전한 반환을 위해 NaN 처리
    result_df["predicted_loyalty_score"] = result_df["predicted_loyalty_score"].fillna(0.0)
    result_df["churn_risk_score"] = result_df["churn_risk_score"].fillna(0.0)
    result_df["customer_segment"] = result_df["customer_segment"].fillna("GENERAL")
    
    return result_df

# ---------- 5. 엔드포인트 ----------
@app.get("/")
async def root():
    return {"status": "AI Server is running"}

@app.post("/api/ai/customers", 
          response_model=CustomerResponse,
          response_model_exclude_unset=True)
async def analyze_customers_endpoint(request: CustomerRequest): 
    """
    고객 데이터 리스트(JSON)를 받아 분석 후,
    결과(점수, 세그먼트)가 추가된 리스트(JSON)를 반환합니다.
    """
    if not request.data:
        return CustomerResponse(result=[])
    
    try:
        data_list = [customer.model_dump() for customer in request.data]
        df = pd.DataFrame(data_list)
        
        result_df = analyze_customer_data(df)
        
        # DataFrame -> Dict List 변환
        result_records = result_df.to_dict(orient="records")
        
        return CustomerResponse(result=result_records)
    
    except Exception as e:
        print(f"Error during analysis: {e}")
        # 로컬 테스트 시 상세 에러 확인용
        import traceback
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=f"Analysis failed: {str(e)}")

# ---------- 6. 서버 실행 ----------
if __name__ == "__main__":
    print("AI 서버를 http://127.0.0.1:8000 에서 시작합니다.")
    uvicorn.run(app, host="127.0.0.1", port=8000)