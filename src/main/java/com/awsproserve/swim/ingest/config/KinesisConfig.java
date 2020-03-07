/**
 * 
 */
package com.awsproserve.swim.ingest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClientBuilder;

/**
 * @author lembrent
 *
 */
@Configuration
public class KinesisConfig {

	@Bean
	public KinesisAsyncClient kinesisClient() {
		
		KinesisAsyncClientBuilder clientBuilder = KinesisAsyncClient.builder();
	    KinesisAsyncClient kinesisClient = clientBuilder.build();

		return kinesisClient;
	}

}
