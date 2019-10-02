/**
 * 
 */
package com.synectiks.kafka.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties.AckMode;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.SeekToCurrentErrorHandler;

/**
 * @author Rajesh
 */
@EnableKafka
@Configuration
public class KafkaConsumerConfig {

	@Value("${kafka.bootstrap.server}")
	private String bootstrap;
	@Value("${kafka.server}")
	private String server;
	@Value("${kafka.group}")
	private String group;
	@Value("${kafka.autocommit}")
	private String autoCommit;

	@Bean
	public ConsumerFactory<Object, Object> consumerFactory() {
		Map<String, Object> props = new HashMap<>();
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, autoCommit);
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, group);
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
				StringDeserializer.class);
		return new DefaultKafkaConsumerFactory<>(props);
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
			KafkaTemplate<Object, Object> template) {

		ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory());
		// Required to get Acknowledgement object in listners.
		factory.getContainerProperties().setAckMode(AckMode.MANUAL_IMMEDIATE);
		// Set default error handler for message consume failure
		factory.setErrorHandler(new SeekToCurrentErrorHandler(
				new DeadLetterPublishingRecoverer(template), 3));
		return factory;
	}
}
