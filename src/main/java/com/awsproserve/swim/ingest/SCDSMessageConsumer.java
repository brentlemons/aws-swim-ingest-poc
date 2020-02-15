/**
 * 
 */
package com.awsproserve.swim.ingest;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lembrent
 *
 */
public class SCDSMessageConsumer implements MessageListener {

	private static final Logger logger = LoggerFactory.getLogger(SCDSMessageConsumer.class);

	@Override
	public void onMessage(Message message) {
		logger.debug("Received message");
		
		try {
			logger.debug("Process message");
			logger.debug("Received message on destination: " + message.getJMSDestination().toString());
			
			if (message instanceof TextMessage) {
				TextMessage txtMsg = (TextMessage) message;
				String msgTextObj = txtMsg.getText();
				logger.debug("message: " + msgTextObj);
			}

		} catch (JMSException ex) {
			logger.error("Exception: " + ex);
			throw new RuntimeException(ex);
		}

	}

}
