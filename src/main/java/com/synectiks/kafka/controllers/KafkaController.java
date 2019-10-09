/**
 * 
 */
package com.synectiks.kafka.controllers;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
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
	private KafkaTemplate<Object, Object> kafkaTemplate;

	@Value("${search.fire.event.url}")
	private String searchUrl;
	@Value("${kafka.topic}")
	private String defTopic;
	@Value("${kafka.group}")
	private String defGroup;

	/**
	 * API to send message in kafka queue default topic.
	 * @param msg
	 * @return
	 */
	@RequestMapping(path = "/send", method = RequestMethod.GET)
	public ResponseEntity<Object> sendMsg(String msg,
			@RequestParam(required = false) String topic,
			@RequestParam(required = false) String key,
			@RequestParam(required = false) Integer partition) {
		Object res = null;
		String message = null;
		try {
			message = URLDecoder.decode(msg, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// ignore it
		}
		try {
			ListenableFuture<SendResult<Object, Object>> future = null;
			String tp = (topic != null ? topic : defTopic);
			if (partition != null && key != null) {
				future = kafkaTemplate.send(tp, partition, key, message);
			} else {
				future = kafkaTemplate.send(tp, message);
			}
			future.addCallback(new Callback(message));
			res = future.get(3000, TimeUnit.MILLISECONDS);
			//res = new JSONObject("{\"result\": \"Success\"}");
		} catch (Throwable ex) {
			logger.error(ex.getMessage(), ex);
			return new ResponseEntity<>(IUtils.getFailedResponse(ex),
					HttpStatus.PRECONDITION_FAILED);
		}
		return new ResponseEntity<>(res, HttpStatus.OK);
	}

	@KafkaListener(topics = "${kafka.topic}", groupId = "${kafka.group}")
	public void listen(String message, Acknowledgment ack) {
		logger.info("Received Messasge in group - " + defGroup + ": " + message);
		boolean res = new MessageProcessor(message, defTopic).process();
		if (res) {
			ack.acknowledge();
			logger.info("Message consumed and acknowledged in topic");
		} else {
			throw new RuntimeException("failed");
		}
	}

	@KafkaListener(topics = "${kafka.topic}.DLT")
	public void listenDel(String message, Acknowledgment ack) {
		logger.info("Received Messasge in Delete topic - " + defTopic + ".DLT: " + message);
		boolean res = new MessageProcessor(message, defTopic).failed();
		if (res) {
			ack.acknowledge();
			logger.info("Message consumed and acknowledged in Delete topic");
		} else {
			throw new RuntimeException("failed");
		}
	}

	@KafkaListener(topics = "${kafka.topic}")
	public void listenWithHeaders(@Payload String message,
			@Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
			Acknowledgment ack) {
		logger.info("Received Message: " + message + " in partition: " + partition);
		boolean res = new MessageProcessor(message, defTopic).process();
		if (res) {
			ack.acknowledge();
			logger.info("Message consumed and acknowledged in topic with headers");
		} else {
			throw new RuntimeException("failed");
		}
	}

	@KafkaListener(topics = "cms")
	public void cmslistenWithHeaders(@Payload String message,
			@Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
			Acknowledgment ack) {
		String msg = null;
		try {
			msg = URLDecoder.decode(message, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// ignore it
		}
		logger.info("Received Message: " + msg + " in partition: " + partition);
		boolean res = new MessageProcessor(msg, "cms").process(searchUrl);
		if (res) {
			ack.acknowledge();
			logger.info("Message consumed and acknowledged in cms topic");
		} else {
			throw new RuntimeException("failed");
		}
	}

	@KafkaListener(topics = "cms.DLT")
	public void cmsDelListener(String message, Acknowledgment ack) {
		String msg = null;
		try {
			msg = URLDecoder.decode(message, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// ignore it
		}
		logger.info("Received Messasge in Delete topic - cms.DLT: " + msg);
		boolean res = new MessageProcessor(msg, "cms").failed();
		if (res) {
			ack.acknowledge();
			logger.info("Message consumed and acknowledged in cms.DLT topic");
		} else {
			throw new RuntimeException("failed");
		}
	}
}
