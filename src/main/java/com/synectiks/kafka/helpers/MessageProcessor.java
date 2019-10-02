/**
 * 
 */
package com.synectiks.kafka.helpers;

/**
 * @author Rajesh
 */
public class MessageProcessor {

	private String msg;

	public MessageProcessor(String message) {
		this.msg = message;
	}

	/**
	 * Process message to run backup and collect response
	 */
	public boolean process() {
		if (msg != null && "fail".equals(msg)) {
			return false;
		}
		return true;
	}

	/**
	 * Process message to run backup and collect response
	 */
	public boolean failed() {
		if (msg != null && "fail".equals(msg)) {
			return true;
		}
		return false;
	}
	
}
