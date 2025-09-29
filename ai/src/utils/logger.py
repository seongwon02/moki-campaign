import logging
import logging.handlers
import os

# 1. 설정 상수 정의
LOG_DIR = 'logs'
LOG_FILE = os.path.join(LOG_DIR, 'app_activity.log')
LOG_LEVEL = logging.INFO  # 운영 환경에서는 INFO 이상만 기록

# 2. 로그 포맷 정의 (시간, 레벨, 모듈/파일, 메시지 포함)
LOG_FORMAT = (
    '%(asctime)s - %(levelname)s - %(name)s - '
    '[%(filename)s:%(lineno)d] - %(message)s'
)

def setup_logger():
    """
    프로젝트 전반에서 사용할 메인 로거를 설정하고 반환합니다.
    """
    # 로그 디렉토리 생성
    if not os.path.exists(LOG_DIR):
        os.makedirs(LOG_DIR)

    # 로거 객체 생성
    logger = logging.getLogger('AI_Project_Logger')
    logger.setLevel(LOG_LEVEL)

    # 기존 핸들러가 있다면 제거 (재설정 방지)
    if logger.hasHandlers():
        logger.handlers.clear()

    # 파일 핸들러 (로그 파일에 저장)
    file_handler = logging.handlers.RotatingFileHandler(
        LOG_FILE,
        maxBytes=1024 * 1024 * 10,  # 10MB 크기 제한
        backupCount=5,              # 최대 5개 백업 파일 유지
        encoding='utf-8'
    )
    file_handler.setFormatter(logging.Formatter(LOG_FORMAT))
    file_handler.setLevel(logging.INFO) # 파일에는 INFO 이상만 저장

    # 콘솔 핸들러 (터미널에 출력)
    console_handler = logging.StreamHandler()
    console_handler.setFormatter(logging.Formatter(LOG_FORMAT))
    console_handler.setLevel(logging.DEBUG) # 콘솔에는 디버깅을 위해 DEBUG 이상 출력

    # 로거에 핸들러 추가
    logger.addHandler(file_handler)
    logger.addHandler(console_handler)

    return logger

# 프로젝트 어디서든 이 로거를 가져와 사용할 수 있도록 초기화
project_logger = setup_logger()