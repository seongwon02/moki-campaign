import numpy as np
import pandas as pd
from sklearn.ensemble import RandomForestRegressor
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field
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

# (참고) CustomerRequest 모델은 이 방식에서 사용되지 않습니다.
class CustomerRequest(BaseModel):
    data: List[CustomerDataInput]

# ---------------- 응답(Response) 모델 ----------------
class CustomerDataOutput(BaseModel):
    customer_id: str
    customer_segment: str
    predicted_loyalty_score: float

class CustomerResponse(BaseModel):
    result: List[CustomerDataOutput]

# ---------- 2. FastAPI 앱 초기화 ----------
app = FastAPI(
    title="MOKI 고객 분석 AI API",
    description="고객 데이터를 받아 충성도와 이탈 위험도를 분석합니다."
)

# CORS 설정
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ---------- 3. 분석용 유틸 함수 ----------
def minmax_series(s):
    mn, mx = s.min(), s.max()
    if mx == mn:
        return s.apply(lambda _: 0.5)
    return (s - mn) / (mx - mn)

def inv_minmax_series(s):
    # 0이 들어올 경우를 대비해 안전하게 역수 계산
    arr = 1.0 / (1.0 + s.astype(float))
    return minmax_series(pd.Series(arr))

def compute_rule_based_scores(df, weights=None):
    # [변경 2] 충성도 점수: Recency 제거 (누적 가치 중심)
    default_w = {"total_visits": 0.5, "amount": 0.5, "recency_inv": 0.0}
    
    if weights is None:
        weights = default_w
    
    for col in ["total_visits", "days_since_last_visit", "amount"]:
        if col not in df.columns:
            df[col] = 0.0
            
    df["total_visits"] = df["total_visits"].fillna(0.0)
    df["days_since_last_visit"] = df["days_since_last_visit"].fillna(0.0)
    df["amount"] = df["amount"].fillna(0.0)

    total_visits_n = minmax_series(df["total_visits"].astype(float))
    recency_inv_n = inv_minmax_series(df["days_since_last_visit"].astype(float))
    amount_n = minmax_series(df["amount"].astype(float))
    
    weighted = (
        total_visits_n * weights["total_visits"]
        + recency_inv_n * weights["recency_inv"]
        + amount_n * weights["amount"]
    )
    
    w_min = weighted.min()
    shifted = weighted - w_min
    
    if shifted.max() == 0:
        scores = np.zeros(len(shifted))
    else:
        scores = shifted / shifted.max()
        
    scores = np.clip(scores, 0.0, 1.0)
    return scores

def try_train_model_and_predict(df, feature_cols, label_col="loyalty_score"):
    if label_col not in df.columns or df[label_col].isnull().all():
        return None
    
    labeled = df[df[label_col].notnull() & (df[label_col] > 0)]
    if len(labeled) < 10:
        return None
    
    X = df[feature_cols].fillna(0).astype(float)
    y = df[label_col].astype(float)
    
    X_train, y_train = X.loc[y > 0], y.loc[y > 0]
    X_all = X
    
    model = RandomForestRegressor(n_estimators=200, random_state=42)
    model.fit(X_train, y_train)
    
    preds = model.predict(X_all)
    preds = np.clip(preds, 0.0, 1.0)
    return preds

def compute_churn_risk_score(df, weights=None):
    # [수정 1] 가중치 조정: 감소폭(decline) 변수 추가
    default_w = {"recency_n": 0.4, "weighted_trend_inv_n": 0.3, "visit_decline_n": 0.3}
    if weights is None:
        weights = default_w
        
    # 결측치 채우기
    for w in range(1, 9):
        col = f"visits_{w}_week_ago"
        if col not in df.columns:
            df[col] = 0.0
        df[col] = df[col].fillna(0.0)
        
    df["days_since_last_visit"] = df["days_since_last_visit"].fillna(0.0)
    df["total_visits"] = df["total_visits"].fillna(0.0)

    # ---------------------------------------------------------
    # 1. Recency (최근 방문 경과일) - Outlier 처리 (Clipping)
    # ---------------------------------------------------------
    # 999일 같은 이상치 때문에 30일 미방문이 0점 처리되는 것을 방지하기 위해
    # 60일(약 2달) 이상은 모두 동일하게 '완전 이탈'로 간주 (Max 60으로 자름)
    clipped_recency = df["days_since_last_visit"].clip(upper=60)
    recency_n = minmax_series(clipped_recency.astype(float))

    # ---------------------------------------------------------
    # 2. Activity Trend (단순 활동성)
    # ---------------------------------------------------------
    # 최근 8주간 방문이 아예 없으면 위험
    weights_recent = {
        "visits_1_week_ago": 8.0, "visits_2_week_ago": 7.0,
        "visits_3_week_ago": 6.0, "visits_4_week_ago": 5.0,
        "visits_5_week_ago": 4.0, "visits_6_week_ago": 3.0,
        "visits_7_week_ago": 2.0, "visits_8_week_ago": 1.0,
    }
    
    weighted_score = pd.Series(np.zeros(len(df)), index=df.index, dtype=float)
    for col, weight in weights_recent.items():
        weighted_score += df[col] * weight
        
    weighted_trend_inv_n = inv_minmax_series(weighted_score) # 방문 적을수록 1.0

    # ---------------------------------------------------------
    # 3. Visit Decline (방문 감소폭) - [신규 로직]
    # ---------------------------------------------------------
    # 과거 4주(5~8주전) 대비 최근 4주(1~4주전) 방문이 얼마나 줄었는가?
    # (과거 - 최근) / (과거 + 1) -> 양수면 감소(위험), 음수면 증가(양호)
    recent_4w_sum = (df["visits_1_week_ago"] + df["visits_2_week_ago"] + 
                     df["visits_3_week_ago"] + df["visits_4_week_ago"])
    past_4w_sum = (df["visits_5_week_ago"] + df["visits_6_week_ago"] + 
                   df["visits_7_week_ago"] + df["visits_8_week_ago"])
    
    # 감소량 계산 (값이 클수록 많이 줄어든 것)
    decline_score = (past_4w_sum - recent_4w_sum)
    # 감소하지 않았거나(음수), 방문이 늘어난 경우는 0으로 처리 (위험도 없음)
    decline_score = decline_score.clip(lower=0)
    
    visit_decline_n = minmax_series(decline_score)

    # 최종 점수 합산
    churn_scores = (
        recency_n * weights["recency_n"]
        + weighted_trend_inv_n * weights["weighted_trend_inv_n"]
        + visit_decline_n * weights["visit_decline_n"]
    )
    return minmax_series(churn_scores)

# ---------- 4. 고객 데이터 분석 ----------
def analyze_customer_data(df: pd.DataFrame) -> pd.DataFrame:
    rule_scores = compute_rule_based_scores(df)
    
    # 모델 학습용 컬럼 (현재 API 호출에서는 label이 없으므로 rule_scores가 주로 사용됨)
    feature_cols = ["total_visits", "days_since_last_visit", "amount"]
    learned_preds = try_train_model_and_predict(df, feature_cols, label_col="loyalty_score")
    
    if learned_preds is not None:
        final_scores = 0.4 * rule_scores + 0.6 * learned_preds
    else:
        final_scores = rule_scores
        
    df["predicted_loyalty_score"] = final_scores.round(4)
    df["churn_risk_score"] = compute_churn_risk_score(df).round(4)
    
    # [변경 4] 기준값(Threshold) 및 세그먼트 정의 수정
    # LOYALTY_THRESHOLD = 0.7 (기존 유지)
    # CHURN_RISK_THRESHOLD = 0.45 (민감도 향상을 위해 0.5 -> 0.45로 하향)
    LOYALTY_THRESHOLD = 0.7
    CHURN_RISK_THRESHOLD = 0.45

    df["is_loyal"] = df["predicted_loyalty_score"] >= LOYALTY_THRESHOLD
    df["is_high_risk"] = df["churn_risk_score"] >= CHURN_RISK_THRESHOLD
    
    # 세그먼트 할당 로직 (np.select 활용)
    conditions = [
        (df["is_loyal"] & df["is_high_risk"]),      # 1. 충성도 높음 & 위험 높음 -> AT_RISK_LOYAL
        (df["is_loyal"] & ~df["is_high_risk"]),     # 2. 충성도 높음 & 위험 낮음 -> LOYAL
        (~df["is_loyal"] & df["is_high_risk"]),     # 3. 충성도 낮음 & 위험 높음 -> CHURN_RISK
        (~df["is_loyal"] & ~df["is_high_risk"])     # 4. 충성도 낮음 & 위험 낮음 -> GENERAL
    ]
    choices = ["AT_RISK_LOYAL", "LOYAL", "CHURN_RISK", "GENERAL"]
    
    df["customer_segment"] = np.select(conditions, choices, default="GENERAL")
    
    return df

# ---------- 5. 엔드포인트 ----------
@app.get("/")
async def root():
    return {"status": "AI Server is running"}

@app.post("/api/ai/customers", 
          response_model=CustomerResponse,
          response_model_exclude_unset=True)
async def analyze_customers_endpoint(customer_data: List[CustomerDataInput]): 
    """
    고객 데이터 리스트(JSON)를 받아 분석 후,
    결과(점수, 세그먼트)가 추가된 리스트(JSON)를 반환합니다.
    """
    if not customer_data:
        return CustomerResponse(result=[])
    
    try:
        data_list = [customer.model_dump() for customer in customer_data]
        df = pd.DataFrame(data_list)
        
        result_df = analyze_customer_data(df)
        
        result_records = result_df.to_dict(orient="records")
        
        return CustomerResponse(result=result_records)
    
    except Exception as e:
        print(f"Error during analysis: {e}")
        raise HTTPException(status_code=500, detail=f"Analysis failed: {str(e)}")

# ---------- 6. 서버 실행 ----------
if __name__ == "__main__":
    print("AI 서버를 http://127.0.0.1:8000 에서 시작합니다.")
    uvicorn.run(app, host="127.0.0.1", port=8000)