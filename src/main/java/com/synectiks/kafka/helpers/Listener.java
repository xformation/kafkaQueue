/**
 * 
 */
package com.synectiks.kafka.helpers;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.support.ProducerListener;
import org.springframework.util.ObjectUtils;

/**
 * @author Rajesh Upadhyay
 */
public class Listener implements ProducerListener<Object, Object> {

	private static final Logger logger = LoggerFactory.getLogger(Listener.class);
	public static final int DEFAULT_MAX_CONTENT_LOGGED = 100;

	@Override
	public void onSuccess(String topic, Integer partition, Object key, Object value,
			RecordMetadata recordMetadata) {
		logger.info("[" + recordMetadata.offset() + "]" + topic
				+ " - " + partition + " -> " + key + " = " + value);
	}

	@Override
	public void onError(String topic, Integer partition, Object key, Object value,
			Exception exception) {
		StringBuffer logOutput = new StringBuffer();
		logOutput.append("Exception thrown when sending a message");
		logOutput.append(" with key='")
			.append(toDisplayString(ObjectUtils.nullSafeToString(key), DEFAULT_MAX_CONTENT_LOGGED))
			.append("'")
			.append(" and payload='")
			.append(toDisplayString(ObjectUtils.nullSafeToString(value), DEFAULT_MAX_CONTENT_LOGGED))
			.append("'");
		logOutput.append(" to topic ").append(topic);
		if (partition != null) {
			logOutput.append(" and partition ").append(partition);
		}
		logOutput.append(":");
		logger.error(logOutput.toString(), exception);
	}

	private String toDisplayString(String original, int maxCharacters) {
		if (original.length() <= maxCharacters) {
			return original;
		}
		return original.substring(0, maxCharacters) + "...";
	}

}
