# 빌드 스테이지
FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

# 루트에 있는 Gradle 관련 파일들 복사
COPY gradlew .
COPY gradle gradle
#있으면 복사, 없으면 무시
COPY settings.gradle* build.gradle* .

# 소스 복사
COPY collector/ collector/

#gradlew build
RUN ./gradlew :collector:build

# 런타임 스테이지
FROM eclipse-temurin:17-jre AS runtime

WORKDIR /app

# 빌드된 JAR 복사
COPY --from=build /app/collector/build/libs/*.jar app.jar

# 포트 EXPOSE 없음
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
