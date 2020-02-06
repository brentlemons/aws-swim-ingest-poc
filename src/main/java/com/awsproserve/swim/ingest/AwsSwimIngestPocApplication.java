package com.awsproserve.swim.ingest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class AwsSwimIngestPocApplication implements CommandLineRunner {

    @Autowired
    ConfigurableApplicationContext context;

    public static void main(String[] args) {
		SpringApplication.run(AwsSwimIngestPocApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
