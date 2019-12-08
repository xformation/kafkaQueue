/**
 * 
 */
package com.synectiks.kafka.helpers;

import java.util.Map;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import com.synectiks.commons.constants.IConsts;
import com.synectiks.commons.utils.IUtils;
import com.synectiks.kafka.KafkaApplication;

/**
 * @author Rajesh
 */
public class MessageProcessor {

	private static final Logger logger = LoggerFactory.getLogger(MessageProcessor.class);

	private String msg;
	private String topic;

	public MessageProcessor(String message, String topic) {
		this.msg = message;
		this.topic = topic;
	}

	/**
	 * Process message to run backup and collect response
	 */
	public boolean failed() {
		/*
		 * if (msg != null && "fail".equals(msg)) { return true; }
		 */
		// mark consumed for all failed messages
		return true;
	}

	/**
	 * Process message to run backup and collect response
	 */
	public boolean process() {
		return this.process(null);
	}

	/**
	 * Process message to run backup and collect response
	 */
	public boolean process(String url) {
		if (msg != null && "fail".equals(msg)) {
			return false;
		} else if (topic != null && topic.equals("cms")) {
			return sendSearchIndexRequest(url);
		}
		return true;
	}

	/**
	 * Sample msg:
	 * {
	 * 	"eventType": "CREATE",
	 * 	"entity": {
	 * 			"id":1451,
	 * 			"shortName":"MC",
	 *  		"logoPath":"abcd",
	 *   		"backgroundImagePath":"abcde",
	 *    		"instructionInformation":"My College",
	 *     		"logoFileName":"abc.jpg",
	 *      	"backgroundImageFileName":"def.jpg"
	 * 		},
	 * 	"cls": "com.synectiks.cms.entities.College"}
	 * @param url
	 * @return
	 */
	private boolean sendSearchIndexRequest(String url) {
		logger.info(this.topic + " - " + url + " - Msg:\n" + msg);
		try {
			JSONObject jsonMsg = new JSONObject(msg);
			String cls = jsonMsg.optString("cls");
			String type = jsonMsg.optString("eventType");
			String entity = jsonMsg.optString("entity");
			RestTemplate rest = KafkaApplication.getBean(RestTemplate.class);

			logger.info("Entity class: " + cls);
			Map<String, Object> params = IUtils.getRestParamMap(IConsts.PRM_EV_TYPE,
					type, IConsts.PRM_CLASS, cls, IConsts.PRM_ENTITY, entity);
			logger.info("Request: " + params);
			String res = IUtils.sendPostRestRequest(rest, url, null, String.class, params,
					MediaType.APPLICATION_FORM_URLENCODED);
			logger.info("Indexing response: " + res);
			if (IUtils.isNullOrEmpty(res)) {
				return false;
			}
		} catch (JSONException e) {
			logger.error(e.getMessage(), e);
			return false;
		}
		return true;
	}
	
}
