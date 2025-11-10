# 베이스 이미지 선택
FROM eclipse-temurin:17-jdk-jammy

# 작업 디렉토리 설정
WORKDIR /app

# 빌드된 Jar 파일을 컨테이너 내부로 복사
COPY build/libs/*.jar app.jar

# 애플리케이션 포트 노출
EXPOSE 8080

# Production 프로파일로 실행 (JVM 옵션 추가)
ENTRYPOINT ["java", "-XX:-UseContainerSupport", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
