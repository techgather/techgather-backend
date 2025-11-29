FROM eclipse-temurin:17-jdk AS build
WORKDIR batch
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} batch.jar
RUN java -Djarmode=layertools -jar batch.jar extract

FROM eclipse-temurin:17-jre AS runtime
EXPOSE 7500
WORKDIR batch
COPY --from=build batch/dependencies/ ./
COPY --from=build batch/spring-boot-loader/ ./
COPY --from=build batch/snapshot-dependencies/ ./
RUN true
COPY --from=build batch/application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]