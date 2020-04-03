/**
 * 
 */
package com.awsproserve.swim.ingest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.zip.GZIPOutputStream;

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

	@Value("#{new Boolean('${aws.kinesis.stream.json}')}")
	private Boolean streamJson;

	@Value("#{new Boolean('${aws.kinesis.stream.compress}')}")
	private Boolean streamCompress;

	private static final Logger logger = LoggerFactory.getLogger(SCDSMessageConsumer.class);
    private ObjectMapper mapper;
    
    public SCDSMessageConsumer() {
		this.mapper = new ObjectMapper();
		this.mapper.registerModule(new JavaTimeModule());
		this.mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);	
		this.mapper.setSerializationInclusion(Include.NON_NULL);
		this.mapper.setSerializationInclusion(Include.NON_EMPTY); 
		logger.debug("streamJson: " + streamJson);
		logger.debug("streamCompress: " + streamCompress);
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
				String kinesisRecord = "";

				logger.debug("raw message: " + msgTextObj);
				
				if (!streamJson) {
					kinesisRecord = msgTextObj;
				} else {
					try {
						JAXBElement<MessageCollectionType> element = (JAXBElement<MessageCollectionType>) xmlToObject(msgTextObj);
						List<AbstractMessageType> messages = ((MessageCollectionType)element.getValue()).getMessage();
	
						for (AbstractMessageType msg : messages) {
							if (msg.getClass() == FlightMessageType.class) {
								NasFlightType nasFlight = (NasFlightType) ((FlightMessageType)msg).getFlight();
								if (nasFlight.getSource() != null) {
									logger.debug("json message: " + this.mapper.writeValueAsString(nasFlight));
									kinesisRecord = this.mapper.writeValueAsString(nasFlight);
								}
							} else {
								logger.error("unknown message type: " + msg.getClass().toString());
							}
						}
					} catch (JAXBException e1) {
						logger.error(e1.toString());
					} catch (JsonProcessingException e) {
						logger.error(e.toString());
					}
				}
				
				if (kinesisRecord.length() > 0) {
					SdkBytes kinesisBytes = SdkBytes.fromUtf8String(kinesisRecord);
					if (streamCompress) {
						logger.info("compressing");
						try {
							kinesisBytes = SdkBytes.fromByteArray(compress(kinesisRecord));
						} catch(IOException ioex) {
							logger.error(ioex.toString());
						}	
					}
					CompletableFuture<PutRecordResponse> putRecordResponseFuture = kinesisClient.putRecord(
			                PutRecordRequest.builder()
			                                .streamName(this.stream)
			                                .partitionKey(routingKey)
			                                .data(kinesisBytes)
			                                .build());
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
    
	public static byte[] compress(String data) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length());
		GZIPOutputStream gzip = new GZIPOutputStream(bos);
		gzip.write(data.getBytes("UTF-8"));
		gzip.close();
		byte[] compressed = bos.toByteArray();
		bos.close();
		return compressed;
	}


}
