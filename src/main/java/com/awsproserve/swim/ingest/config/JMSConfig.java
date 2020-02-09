/**
 * 
 */
package com.awsproserve.swim.ingest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.config.SimpleJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;

import org.springframework.jndi.JndiTemplate;

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterRequest;
import com.awsproserve.swim.ingest.SCDSMessageConsumer;

import org.springframework.jndi.JndiObjectFactoryBean;
import java.util.Properties;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import javax.jms.Destination;

import org.springframework.beans.factory.annotation.Value;

/**
 * @author lembrent
 *
 */
@Configuration
public class JMSConfig {

//    @Value("${ssm.config-root}") String configRoot;

//	AWSSimpleSystemsManagement ssm = null;	
    
    @Value("${swim.jndi.context_factory}") String jndiContextFactory;
    @Value("${swim.jndi.host}") String jndiHost;
    @Value("${swim.jndi.principal}") String jndiPrincipal;
    @Value("${swim.jndi.credentials}") String jndiCredentials;
    @Value("${swim.jndi.vpn}") String jndiVpn;

    @Value("${swim.jms.connection_factory_name}") String jmsCfName;
    @Value("${swim.jms.queue_name}") String jmsQueueName;

//	public JMSConfig() {
//		
//		/**
//		* Initialize AWS System Manager Client with default credentials
//		*/
////		ssm = AWSSimpleSystemsManagementClientBuilder.defaultClient();
//		
//	}

    @Bean
    public JndiTemplate solaceJndiTemplate() {
        JndiTemplate jndiTemplate = new JndiTemplate();
        Properties jndiProps = new Properties();

//        jndiProps.setProperty("java.naming.factory.initial", getParameter(configRoot + "/jndi/context_factory"));
//        jndiProps.setProperty("java.naming.provider.url", getParameter(configRoot + "/jndi/host")); // t3://serverAddress:port  ---smf://___IP:PORT___
//        jndiProps.setProperty("java.naming.security.principal", getParameter(configRoot + "/jndi/principal")); // injected from properties file username   ---spring_user@Solace_Spring_VPN
//        jndiProps.setProperty("java.naming.security.credentials", getParameter(configRoot + "/jndi/credentials")); //injected from properties file password  ---spring_password
//        jndiProps.setProperty("Solace_JMS_VPN", getParameter(configRoot + "/jndi/vpn"));
        jndiProps.setProperty("java.naming.factory.initial", jndiContextFactory);
        jndiProps.setProperty("java.naming.provider.url", jndiHost);
        jndiProps.setProperty("java.naming.security.principal", jndiPrincipal);
        jndiProps.setProperty("java.naming.security.credentials", jndiCredentials);
        jndiProps.setProperty("Solace_JMS_VPN", jndiVpn);

        jndiTemplate.setEnvironment(jndiProps);

        return jndiTemplate;
    }

    @Bean
    public JndiObjectFactoryBean jmsConnectionFactory() { //solaceConnectionFactory
        JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();


        jndiObjectFactoryBean.setJndiTemplate(solaceJndiTemplate());
//        jndiObjectFactoryBean.setJndiName(getParameter(configRoot + "/jms/connection_factory_name")); // connectionFactory name.  --JNDI/CF/spring
        jndiObjectFactoryBean.setJndiName(jmsCfName);

        return jndiObjectFactoryBean;
    }

    @Bean // Strictly speaking this bean is not necessary as boot creates a default
    JmsListenerContainerFactory<?> brentJmsContainerFactory() {
        SimpleJmsListenerContainerFactory factory = new SimpleJmsListenerContainerFactory();
        factory.setConnectionFactory((javax.jms.ConnectionFactory) jmsConnectionFactory().getObject());
        return factory;
    }

    @Bean
    public JndiObjectFactoryBean destination() {
        JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();

        jndiObjectFactoryBean.setJndiTemplate(solaceJndiTemplate());
//        jndiObjectFactoryBean.setJndiName(getParameter(configRoot + "/jms/queue_name")); //queue name  --JNDI/Q/requests
        jndiObjectFactoryBean.setJndiName(jmsQueueName);

        return jndiObjectFactoryBean;
    }

    @Bean
    public JmsTemplate jmsTemplate() {
        JmsTemplate jmsTemplate = new JmsTemplate();

        jmsTemplate.setConnectionFactory((javax.jms.ConnectionFactory) jmsConnectionFactory().getObject());
        jmsTemplate.setDefaultDestination((Destination) destination().getObject());
        jmsTemplate.setDeliveryPersistent(true);
        jmsTemplate.setExplicitQosEnabled(true);

        return jmsTemplate;
    }

    @Bean
    public SCDSMessageConsumer solaceMessageConsumer() {
    	SCDSMessageConsumer solaceMessageConsumer = new SCDSMessageConsumer();

        return solaceMessageConsumer;
    }

    @Bean
    public DefaultMessageListenerContainer jmsContainer() {
        DefaultMessageListenerContainer defaultMessageListenerContainer = new DefaultMessageListenerContainer();


        defaultMessageListenerContainer.setConnectionFactory((javax.jms.ConnectionFactory) jmsConnectionFactory().getObject());
        defaultMessageListenerContainer.setDestination((Destination) destination().getObject());
        defaultMessageListenerContainer.setMessageListener(solaceMessageConsumer()); // The actual bean which implements the MessageListener interface

        return defaultMessageListenerContainer;
    }
    
//    /**
//    * Helper method to retrieve SSM Parameter's value
//    * @param parameterName identifier of the SSM Parameter
//    * @return decrypted parameter value
//    */
//    public String getParameter(String parameterName) {
//    	System.out.println("==> " + parameterName);
//        GetParameterRequest request = new GetParameterRequest();
//        request.setName(parameterName);
//        request.setWithDecryption(true);
//        return ssm.getParameter(request).getParameter().getValue();
//    }
} 