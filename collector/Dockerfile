FROM eclipse-temurin:17-jdk AS build
WORKDIR collector
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} collector.jar
RUN java -Djarmode=layertools -jar collector.jar extract

FROM eclipse-temurin:17-jre AS runtime
WORKDIR collector
COPY --from=build collector/dependencies/ ./
COPY --from=build collector/spring-boot-loader/ ./
COPY --from=build collector/snapshot-dependencies/ ./
RUN true
COPY --from=build collector/application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
