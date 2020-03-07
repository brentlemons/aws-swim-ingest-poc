/**
 * 
 */
package com.awsproserve.swim.ingest;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import aero.faa.nas._3.AbstractMessageType;
import aero.faa.nas._3.FlightMessageType;
import aero.faa.nas._3.MessageCollectionType;
import aero.faa.nas._3.NasFlightType;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.services.kinesis.model.KinesisResponseMetadata;
import software.amazon.awssdk.services.kinesis.model.PutRecordRequest;
import software.amazon.awssdk.services.kinesis.model.PutRecordResponse;

/**
 * @author lembrent
 *
 */
public class SCDSMessageConsumer implements MessageListener {

	@Autowired
	KinesisAsyncClient kinesisClient;

	@Autowired
	private Jaxb2Marshaller  marshaller;

	@Value("${aws.kinesis.stream}")
	private String stream;

	private static final Logger logger = LoggerFactory.getLogger(SCDSMessageConsumer.class);
    private ObjectMapper mapper;
    
    public SCDSMessageConsumer() {
		this.mapper = new ObjectMapper();
		this.mapper.registerModule(new JavaTimeModule());
		this.mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);	
		this.mapper.setSerializationInclusion(Include.NON_NULL);
		this.mapper.setSerializationInclusion(Include.NON_EMPTY);    	
    }

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

//				logger.info("raw message: " + msgTextObj);
				try {
					JAXBElement<MessageCollectionType> element = (JAXBElement<MessageCollectionType>) xmlToObject(msgTextObj);
					List<AbstractMessageType> messages = ((MessageCollectionType)element.getValue()).getMessage();

					for (AbstractMessageType msg : messages) {
						if (msg.getClass() == FlightMessageType.class) {
							NasFlightType nasFlight = (NasFlightType) ((FlightMessageType)msg).getFlight();
							if (nasFlight.getSource() != null) {
								
								logger.debug("json message: " + this.mapper.writeValueAsString(nasFlight));

								CompletableFuture<PutRecordResponse> putRecordResponseFuture = kinesisClient.putRecord(
						                PutRecordRequest.builder()
						                                .streamName(this.stream)
						                                .partitionKey(routingKey)
				//		                                .data(SdkBytes.fromByteArray(compress(this.mapper.writeValueAsBytes(nasFlight))))
				//		                                .data(SdkBytes.fromUtf8String(msgTextObj.toString()))
						                                .data(SdkBytes.fromUtf8String(this.mapper.writeValueAsString(nasFlight)))
						                                .build());

							}
						}
					}
				} catch (JAXBException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (JsonProcessingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		} catch (JMSException ex) {
			logger.error("Exception: " + ex);
			throw new RuntimeException(ex);
		}

	}

	@SuppressWarnings("unchecked")
	public <T> T unmarshallXml(final StreamSource streamSource) throws JAXBException {
		return (T) marshaller.unmarshal(streamSource);
	}
	
    //Converts XML to Java Object
    public Object xmlToObject(String xml) throws JAXBException {
    	return unmarshallXml(new StreamSource(new java.io.StringReader(xml)));
    }

}
