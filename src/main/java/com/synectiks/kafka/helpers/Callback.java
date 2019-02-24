/**
 * 
 */
package com.synectiks.kafka.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFutureCallback;

/**
 * @author Rajesh
 */
public class Callback implements ListenableFutureCallback<SendResult<String, String>> {

	private static Logger logger = LoggerFactory.getLogger(Callback.class);

	private String message;

	public Callback(String msg) {
		this.message = msg;
	}

	@Override
	public void onSuccess(SendResult<String, String> result) {
		logger.info("Sent message=[" + message + "] with offset=["
				+ result.getRecordMetadata().offset() + "]");
	}

	@Override
	public void onFailure(Throwable ex) {
		logger.error("Unable to send message=[" + message + "] due to : " +
				ex.getMessage(), ex);
	}

}
