FROM openjdk:8-jdk-alpine
COPY build/libs/aws-swim-ingest-poc-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar","--ssm-config-root=${SSM_CONFIG_ROOT}"]