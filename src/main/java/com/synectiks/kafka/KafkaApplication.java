package com.synectiks.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class KafkaApplication {

	private static final Logger logger = LoggerFactory.getLogger(KafkaApplication.class);

	private static ConfigurableApplicationContext ctx;

	public static void main(String[] args) {
		ctx = SpringApplication.run(KafkaApplication.class, args);
		for (String bean : ctx.getBeanDefinitionNames()) {
			logger.info("Beans: " + bean);
		}
	}

}

