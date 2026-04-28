# 🏪 Moki-Campaign - 키오스크 고객 데이터 기반 이탈 위험 고객 관리 플랫폼

**Moki-Campaign**은 소상공인의 키오스크 고객 데이터를 활용하여, 머신러닝 기반 충성도 분석과 이탈 위험 고객 세분화를 통해 데이터 기반 마케팅 의사결정을 지원하는 플랫폼입니다.

기존 RFM 분석의 한계인 **정적 상태만 반영**하는 문제를 해결하여, 시간 흐름에 따른 고객의 이탈 징후를 조기에 포착하고 효율적인 고객 유지 전략을 제공합니다.

> 🏆 본 프로젝트는 **2025 한국정보기술학회 추계 종합학술대회 대학생 논문경진대회**에서 **우수논문상(금상)** 을 수상했습니다.

## 🎬 데모 영상

[![Moki-Campaign 데모 영상](https://img.youtube.com/vi/GAUHzghGeRI/hqdefault.jpg)](https://youtu.be/GAUHzghGeRI)

## 💡 주요 기능 (Core Features)

---

### 1. 매출 데이터 리포트 제공

- **주간 매출 요약:** 이번 주 매출, 방문자 수, 재방문율을 직관적으로 제공합니다.
- **실시간 매출 리포트:** 일별 / 주별 / 월별 매출 추이를 그래프로 시각화합니다.
- **메뉴별 판매 리포트:** 판매량/매출 순으로 메뉴별 판매 현황을 제공합니다.
- **매출 예측 리포트:** AI 기반 내일/이번 주 매출 예측값을 제공합니다.

---

### 2. AI 기반 고객 분류

고객을 4가지 세그먼트로 분류하여 타겟 마케팅을 지원합니다.

| 세그먼트 | 정의 |
| --- | --- |
| **Loyal** | 평균 방문 횟수와 지출 금액이 높은 VIP 고객 |
| **At Risk Loyal** | VIP 고객 중 최근 방문 횟수가 감소한 이탈 위험 단골 |
| **Churn Risk** | 방문 횟수와 지출 금액이 낮은 이탈 위험군 고객 |
| **General** | 그 외의 일반 고객 |

- **충성도 점수 (Loyalty Score):** 규칙 기반 점수(40%) + Random Forest ML 예측(60%)의 하이브리드 모델
- **이탈 위험 점수 (Churn Risk Score):** 최근성(Recency), 가중 방문 추세(Activity Trend), 활동 감소량(Decline)의 가중합

---

### 3. 고객 정보 시각화

- **이탈 위험 대시보드:** 이탈 위험 고객 수 및 비율을 게이지 차트로 제공합니다.
- **고객 분류 스티커:** 고객 상세 페이지에서 `단골`, `이탈 위험` 등 분류 태그를 표시합니다.
- **방문 빈도 그래프:** 최근 8주간 주별/월별 방문 빈도를 바 차트로 시각화합니다.

---

## ⚙️ 기술 스택 (Tech Stack)

| **구분** | **기술** | **버전** |
| --- | --- | --- |
| **Backend** | `Java`, `Spring Boot` | 3.4.4 |
| **Security** | `Spring Security`, `JWT` | - |
| **Database** | `MySQL` | 9.2.0 |
| **Data Access** | `Spring Data JPA` | 3.4.4 |
| **API Docs** | `Springdoc OpenAPI (Swagger)` | 2.3.0 |
| **Frontend** | `React`, `Vite`, `Tailwind CSS` | 18.3.1 / 6.2.0 / 3.4.17 |
| **Data Viz** | `Nivo` | 0.88.0 |
| **AI / ML** | `FastAPI`, `Prophet`, `XGBoost`, `Optuna` | - |
| **CI/CD** | `Docker`, `Docker Compose`, `GitHub Actions` | 27.3.1 |
| **Infra** | `AWS EC2`, `AWS RDS`, `Nginx`, `Vercel` | - |

## 🐳 시스템 아키텍처
<img width="1067" height="590" alt="image" src="https://github.com/user-attachments/assets/396a831d-142f-402c-b913-7e61df3b4c20" />


- GitHub Actions를 통한 백엔드 Docker 이미지 빌드 및 EC2 자동 배포
- 프론트엔드는 Vercel을 통한 자동 배포

## 🤖 AI 모델 상세

### 충성도 점수 (Loyalty Score)

```
Loyalty Score = 0.4 × Rule Score + 0.6 × ML Prediction
```

- **Rule Score:** 방문 횟수, 최근 방문일, 지출 금액을 가중 합산
- **ML Prediction:** Random Forest Regressor 모델 학습 결과

### 이탈 위험 점수 (Churn Risk Score)

```
Churn Risk Score = 0.3 × Recency + 0.3 × Activity Trend + 0.4 × Decline
```

- **Recency:** 마지막 방문 후 경과일
- **Activity Trend:** 이전 8주간 방문 빈도에 최근 가중치 부여
- **Decline:** 4 ~ 8주 전 대비 최근 1 ~ 4주 방문 횟수 감소량

> 데이터 분포 왜곡 문제 해결을 위해 로그 스케일 적용 및 활성 고객(60일 이내 방문)으로 분석 대상을 제한하였습니다.

## 🚀 시작하기 (Getting Started)

```bash
# 1. Git Repository Clone
git clone https://github.com/seongwon02/moki-campaign.git
cd moki-campaign

# 2. Frontend 실행
cd frontend
npm install
npm run dev

# 3. Backend 실행 (로컬 H2 환경)
# 로컬 개발 환경에서는 application-dev.properties가 활성화되며, H2 인메모리 DB를 사용합니다.
cd backend
./gradlew bootRun

# 4. AI 서버 실행
cd ai
pip install -r requirements.txt
uvicorn main:app --reload --port 8000  # (필요시 main.py 경로에 맞게 수정)
```

## 📚 API 문서
API 명세는 Swagger UI를 통해 제공됩니다.
서버 실행 후 아래 주소로 접속하여 API 엔드포인트와 스펙을 확인할 수 있습니다.

- **Swagger UI** : http://localhost:8080/swagger-ui.html

## 🏆 성과 (Achievement)

- **논문 게재:** 키오스크 고객 데이터를 활용한 머신러닝 기반 충성도 분석 및 세분화
- **게재처:** 2025 한국정보기술학회 추계 종합학술대회
- **수상:** 우수논문상 (금상) 🥇
