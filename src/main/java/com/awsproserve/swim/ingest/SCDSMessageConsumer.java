/**
 * 
 */
package com.awsproserve.swim.ingest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.zip.GZIPOutputStream;

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
import software.amazon.awssdk.services.kinesis.model.PutRecordsRequest;
import software.amazon.awssdk.services.kinesis.model.PutRecordsRequestEntry;
import software.amazon.awssdk.services.kinesis.model.PutRecordsResponse;

/**
 * @author lembrent
 *
 */
public class SCDSMessageConsumer implements MessageListener {

	@Autowired
	KinesisAsyncClient kinesisClient;

	@Value("${aws.kinesis.stream}")
	private String stream;

	@Value("#{new Boolean('${aws.kinesis.stream.json}')}")
	private Boolean streamJson;

	@Value("#{new Boolean('${aws.kinesis.stream.compress}')}")
	private Boolean streamCompress;

	private static final Logger logger = LoggerFactory.getLogger(SCDSMessageConsumer.class);
    
    public SCDSMessageConsumer() {
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
				List<String> flightRecords = new ArrayList<String>();

				logger.debug("raw message: " + msgTextObj);
				
				flightRecords.add(msgTextObj);
				
				if (flightRecords.size() > 0) {
					List<PutRecordsRequestEntry> kinesisRecords = new ArrayList<PutRecordsRequestEntry>();
					
					for (String flightRecord : flightRecords) {
						SdkBytes kinesisBytes = SdkBytes.fromUtf8String(flightRecord);
//						if (streamCompress) {
//						logger.info("compressing");
							try {
								kinesisBytes = SdkBytes.fromByteArray(compress(flightRecord));
							} catch(IOException ioex) {
								logger.error(ioex.toString());
							}	
//						}
						kinesisRecords.add(PutRecordsRequestEntry.builder()
								.partitionKey(routingKey)
								.data(kinesisBytes)
								.build());
					}

					try {
						PutRecordsResponse putRecordsResponseFuture = kinesisClient.putRecords(
						        PutRecordsRequest.builder()
						                        .streamName(this.stream)
						                        .records(kinesisRecords)
						                        .build()).get();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						logger.error(e.toString());
					} catch (ExecutionException e) {
						// TODO Auto-generated catch block
						logger.error(e.toString());
					}
				}
			}

		} catch (JMSException ex) {
			logger.error("Exception: " + ex);
			throw new RuntimeException(ex);
		}

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
