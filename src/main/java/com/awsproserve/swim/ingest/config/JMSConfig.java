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

    @Value("${swim.jndi.context_factory}") String jndiContextFactory;
    @Value("${swim.jndi.host}") String jndiHost;
    @Value("${swim.jndi.principal}") String jndiPrincipal;
    @Value("${swim.jndi.credentials}") String jndiCredentials;
    @Value("${swim.jndi.vpn}") String jndiVpn;

    @Value("${swim.jms.connection_factory_name}") String jmsCfName;
    @Value("${swim.jms.queue_name}") String jmsQueueName;

    @Bean
    public JndiTemplate solaceJndiTemplate() {
        JndiTemplate jndiTemplate = new JndiTemplate();
        Properties jndiProps = new Properties();

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
    
} 