/**
 * 
 */
package com.synectiks.kafka.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.synectiks.commons.utils.IUtils;
import com.synectiks.kafka.helpers.Callback;
import com.synectiks.kafka.helpers.MessageProcessor;

/**
 * @author Rajesh
 */
@RestController
@RequestMapping(path = "/kafka", method = RequestMethod.POST)
public class KafkaController {

	private static final Logger logger = LoggerFactory.getLogger(KafkaController.class);

	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;

	@Value("${kafka.topic}")
	private String topic;
	@Value("${kafka.group}")
	private String group;

	@RequestMapping(path = "/send", method = RequestMethod.GET)
	public ResponseEntity<Object> sendMsg(String msg) {
		String res = null;
		try {
			ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, msg);
			future.addCallback(new Callback(msg));
			res = "{\"result\": \"Success\"}";
		} catch (Throwable ex) {
			logger.error(ex.getMessage(), ex);
			return new ResponseEntity<>(IUtils.getFailedResponse(ex),
					HttpStatus.PRECONDITION_FAILED);
		}
		return new ResponseEntity<>(res, HttpStatus.OK);
	}

	@KafkaListener(topics = "${kafka.topic}", groupId = "${kafka.group}")
	public void listen(String message) {
		logger.info("Received Messasge in group - " + group + ": " + message);
		new MessageProcessor(message).process();
	}

	@KafkaListener(topics = "${kafka.topic}")
	public void listenWithHeaders(@Payload String message,
			@Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition) {
		logger.info("Received Message: " + message + "from partition: " + partition);
		new MessageProcessor(message).process();
	}
}
