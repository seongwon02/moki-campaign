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
    customer_id: str  # 1. int -> str 로 수정
    amount: float
    total_visits: int
    days_since_last_visit: int
    visits_6_month_ago: int
    visits_5_month_ago: int
    visits_4_month_ago: int
    visits_3_month_ago: int
    visits_2_month_ago: int
    visits_1_month_ago: int

# (참고) CustomerRequest 모델은 이 방식에서 사용되지 않습니다.
class CustomerRequest(BaseModel):
    data: List[CustomerDataInput]

# ---------------- 응답(Response) 모델 ----------------
class CustomerDataOutput(BaseModel):
    customer_id: str  # 2. int -> str 로 수정
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
# (이전과 동일 - 생략)
def minmax_series(s):
    mn, mx = s.min(), s.max()
    if mx == mn:
        return s.apply(lambda _: 0.5)
    return (s - mn) / (mx - mn)

def inv_minmax_series(s):
    arr = 1.0 / (1.0 + s.astype(float))
    return minmax_series(pd.Series(arr))

def compute_rule_based_scores(df, weights=None):
    default_w = {"total_visits": 0.25, "recency_inv": 0.25, "amount": 0.25}
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
    default_w = {"recency_n": 0.4, "activity_concentration_inv_n": 0.3, "weighted_trend_inv_n": 0.3}
    if weights is None:
        weights = default_w
        
    for m in range(1, 7):
        col = f"visits_{m}_month_ago"
        if col not in df.columns:
            df[col] = 0.0
        df[col] = df[col].fillna(0.0)
        
    df["days_since_last_visit"] = df["days_since_last_visit"].fillna(0.0)
    df["total_visits"] = df["total_visits"].fillna(0.0)

    recency_n = minmax_series(df["days_since_last_visit"].astype(float))

    sum_6_months = (
        df["visits_1_month_ago"] + df["visits_2_month_ago"] +
        df["visits_3_month_ago"] + df["visits_4_month_ago"] +
        df["visits_5_month_ago"] + df["visits_6_month_ago"]
    )
    total_visits = df["total_visits"] + 1.0
    activity_ratio = (sum_6_months + 1.0) / total_visits
    activity_concentration_inv_n = inv_minmax_series(activity_ratio)
    
    weights_recent = {
        "visits_1_month_ago": 6.0,
        "visits_2_month_ago": 5.0,
        "visits_3_month_ago": 4.0,
        "visits_4_month_ago": 3.0,
        "visits_5_month_ago": 2.0,
        "visits_6_month_ago": 1.0,
    }
    
    weighted_score = pd.Series(np.zeros(len(df)), index=df.index, dtype=float)
    for col, weight in weights_recent.items():
        weighted_score += df[col] * weight
        
    weighted_trend_inv_n = inv_minmax_series(weighted_score)
    df['weighted_recent_score'] = weighted_score

    churn_scores = (
        recency_n * weights["recency_n"]
        + activity_concentration_inv_n * weights["activity_concentration_inv_n"]
        + weighted_trend_inv_n * weights["weighted_trend_inv_n"]
    )
    return minmax_series(churn_scores)

# ---------- 4. 고객 데이터 분석 ----------
def analyze_customer_data(df: pd.DataFrame) -> pd.DataFrame:
    rule_scores = compute_rule_based_scores(df)
    
    feature_cols = ["total_visits", "days_since_last_visit", "amount"]
    learned_preds = try_train_model_and_predict(df, feature_cols, label_col="loyalty_score")
    
    if learned_preds is not None:
        final_scores = 0.4 * rule_scores + 0.6 * learned_preds
    else:
        final_scores = rule_scores
        
    df["predicted_loyalty_score"] = final_scores.round(4)
    df["churn_risk_score"] = compute_churn_risk_score(df).round(4)
    
    df["is_loyal_customer"] = df["predicted_loyalty_score"] >= 0.6
    df["is_at_risk"] = df["churn_risk_score"] >= 0.6
    
    df["customer_segment"] = "GENERAL"
    df.loc[df["is_loyal_customer"], "customer_segment"] = "LOYAL"
    df.loc[df["is_at_risk"], "customer_segment"] = "CHURN-RISK"
    df.loc[df["is_loyal_customer"] & df["is_at_risk"], "customer_segment"] = "AT-RISK LOYAL"
    
    return df

# ---------- 5. 엔드포인트 (수정됨) ----------
@app.get("/")
async def root():
    return {"status": "AI Server is running"}

@app.post("/api/ai/customers", 
          response_model=CustomerResponse,  # 응답은 {"result": [...]} 형식을 유지
          response_model_exclude_unset=True)
# 3. 입력을 CustomerRequest(객체) -> List[CustomerDataInput](리스트)로 수정
async def analyze_customers_endpoint(customer_data: List[CustomerDataInput]): 
    """
    (백엔드 API와 동일한 경로)
    고객 데이터 리스트(JSON)를 받아 분석 후,
    결과(점수, 세그먼트)가 추가된 리스트(JSON)를 반환합니다.
    """
    if not customer_data:
        return CustomerResponse(result=[])
    
    try:
        # 4. customer_data 자체가 리스트이므로 .data 제거
        data_list = [customer.model_dump() for customer in customer_data]
        df = pd.DataFrame(data_list)
        
        result_df = analyze_customer_data(df)
        
        result_records = result_df.to_dict(orient="records")
        
        # 5. 응답은 CustomerResponse 모델로 감싸서 반환
        return CustomerResponse(result=result_records)
    
    except Exception as e:
        print(f"Error during analysis: {e}")
        raise HTTPException(status_code=500, detail=f"Analysis failed: {str(e)}")

# ---------- 6. 서버 실행 ----------
if __name__ == "__main__":
    print("AI 서버를 http://127.0.0.1:8000 에서 시작합니다.")
    uvicorn.run(app, host="127.0.0.1", port=8000)