FROM eclipse-temurin:17-jdk-alpine
ARG ARTIFACT_PATH 
COPY ${ARTIFACT_PATH} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]