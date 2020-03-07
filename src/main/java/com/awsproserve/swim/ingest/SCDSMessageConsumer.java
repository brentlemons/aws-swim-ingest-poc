/**
 * 
 */
package com.awsproserve.swim.ingest;

import java.util.concurrent.CompletableFuture;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.services.kinesis.model.PutRecordRequest;
import software.amazon.awssdk.services.kinesis.model.PutRecordResponse;

/**
 * @author lembrent
 *
 */
public class SCDSMessageConsumer implements MessageListener {

	private static final Logger logger = LoggerFactory.getLogger(SCDSMessageConsumer.class);

	@Autowired
	KinesisAsyncClient kinesisClient;

	@Value("${aws.kinesis.stream}")
	private String stream;

	@Override
	public void onMessage(Message message) {
		logger.debug("Received message");
		
		try {
			logger.debug("Process message");
			logger.debug("Received message on destination: " + message.getJMSDestination().toString());
			
			if (message instanceof TextMessage) {
				String routingKey = message.getJMSDestination().toString().replace('/', '.');
				
				TextMessage txtMsg = (TextMessage) message;
				String msgTextObj = txtMsg.getText();
				logger.debug("message: " + msgTextObj);
				CompletableFuture<PutRecordResponse> myResult = kinesisClient.putRecord(
		                PutRecordRequest.builder()
		                                .streamName(this.stream)
		                                .partitionKey(routingKey)
//		                                .data(SdkBytes.fromByteArray(compress(this.mapper.writeValueAsBytes(nasFlight))))
		                                .data(SdkBytes.fromUtf8String(msgTextObj.toString()))
		                                .build());
			}

		} catch (JMSException ex) {
			logger.error("Exception: " + ex);
			throw new RuntimeException(ex);
		}

	}

}
