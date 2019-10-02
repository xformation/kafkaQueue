/**
 * 
 */
package com.synectiks.kafka.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import com.synectiks.kafka.helpers.Listener;

/**
 * @author Rajesh
 */
@Configuration
public class KafkaProducerConfig {

	@Value("${kafka.bootstrap.server}")
	private String bootstrap;

	@Bean
	public ProducerFactory<Object, Object> producerFactory() {
		Map<String, Object> configProps = new HashMap<>();
		configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
		configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
				StringSerializer.class);
		configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
				StringSerializer.class);
		return new DefaultKafkaProducerFactory<>(configProps);
	}

	@Bean
	public KafkaTemplate<Object, Object> kafkaTemplate() {
		KafkaTemplate<Object, Object> template = new KafkaTemplate<>(producerFactory());
		template.setProducerListener(new Listener());
		return template;
	}
}
