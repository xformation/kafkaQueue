/**
 * 
 */
package com.synectiks.kafka.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

/**
 * @author Rajesh
 */
@Configuration
public class KafkaTopicConfig {

	@Value("${kafka.bootstrap.server}")
	private String bootstrap;
	@Value("${kafka.topic}")
	private String topic;


	@Bean
	public KafkaAdmin kafkaAdmin() {
		Map<String, Object> configs = new HashMap<>();
		configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
		return new KafkaAdmin(configs);
	}

	@Bean
	public NewTopic backupTopic() {
		return new NewTopic(topic, 1, (short) 1);
	}
}
