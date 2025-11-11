# 1. 기본 이미지 (environment.yml과 버전 일치)
FROM python:3.12.11-slim

# 2. 작업 디렉토리 설정
WORKDIR /app

# 3. ai/requirements.txt 를 컨테이너 /app/ 로 복사
COPY requirements.txt .

# 4. 의존성 설치
RUN pip install --no-cache-dir -r requirements.txt

# 5. ai/src 폴더 전체를 컨테이너 /app/src 로 복사
COPY src /app/src

# 6. AI 서버 포트 노출 (api.py에서 8000번 포트 사용)
EXPOSE 8000

# 7. 컨테이너 실행 시 uvicorn으로 FastAPI 앱 실행
#
CMD ["uvicorn", "src.data.api:app", "--host", "0.0.0.0", "--port", "8000"]