FROM amazoncorretto:21-al2023 AS build
WORKDIR api
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} api.jar
RUN java -Djarmode=layertools -jar api.jar extract

FROM amazoncorretto:21-al2023 AS runtime
EXPOSE 8888
WORKDIR api
COPY --from=build api/dependencies/ ./
COPY --from=build api/spring-boot-loader/ ./
COPY --from=build api/snapshot-dependencies/ ./
RUN true
COPY --from=build api/application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]