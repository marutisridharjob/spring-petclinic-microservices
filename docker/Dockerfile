FROM amazoncorretto:17-alpine-jdk
ARG ARTIFACT_PATH 
COPY ${ARTIFACT_PATH} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]