# 런타임 전용 이미지.
# Gradle 빌드는 로컬에서 수행하고 결과 jar(app.jar)만 복사한다.
# t3.micro(1GiB, CPU 크레딧 버스터블)에서 Gradle을 돌리면 메모리와 CPU 크레딧을 모두 태운다.
FROM eclipse-temurin:21-jre

WORKDIR /app

# `./gradlew clean bootJar` 하면 build/libs/app.jar 가 나온다
# (build.gradle 의 bootJar.archiveFileName 으로 이름을 고정해 둠).
# 이 파일을 deploy/app.jar 로 복사한 뒤 이미지를 빌드한다.
COPY app.jar app.jar

# t3.micro에 MySQL이 함께 뜨므로 힙을 컨테이너 메모리의 40%(약 380MB)로 제한한다.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=40.0 -Duser.timezone=Asia/Seoul -Dfile.encoding=UTF-8"

# nginx만 이 포트로 접근한다. 호스트로는 publish하지 않는다(docker-compose.yml 참고).
EXPOSE 8080

RUN useradd --system --uid 1001 appuser
USER appuser

# exec로 java를 PID 1로 만들어 SIGTERM이 전달되게 한다(graceful shutdown).
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]