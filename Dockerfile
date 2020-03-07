FROM openjdk:8-jdk-alpine
COPY build/libs/aws-swim-ingest-poc-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT [ "java", \
	"-jar","/app.jar", \
	"--aws.kinesis.stream=${AWS_KINESIS_STREAM}", \
	"--swim.jndi.context_factory=${SWIM_JNDI_CONTEXT_FACTORY}", \
    "--swim.jndi.host=${SWIM_JNDI_HOST}", \
    "--swim.jndi.principal=${SWIM_JNDI_PRINCIPAL}", \
    "--swim.jndi.credentials=${SWIM_JNDI_CREDENTIALS}", \
    "--swim.jndi.vpn=${SWIM_JNDI_VPN}", \
    "--swim.jms.connection_factory_name=${SWIM_JMS_CONNECTION_FACTORY_NAME}", \
    "--swim.jms.queue_name=${SWIM_JMS_QUEUE_NAME}" ]