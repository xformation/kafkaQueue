/**
 * 
 */
package com.synectiks.kafka.controllers;

import java.io.File;
import java.io.FileWriter;
import java.util.Date;
import java.util.List;

import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.StringUtils;
import org.apache.tomcat.util.json.JSONParser;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.synectiks.kafka.KafkaApplication;
import com.synectiks.kafka.helpers.CommonUtil;

@RestController
@RequestMapping(path = "/dataGenerator", method = RequestMethod.POST)
public class DataGeneratorController {

	private static final Logger logger = LoggerFactory.getLogger(DataGeneratorController.class);

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
	 * 
	 * @param msg
	 * @return
	 */
//	@RequestMapping(path = "/generateRandomData")
//	public ResponseEntity<Object> sendMsg(@RequestParam String source, @RequestParam String kafkaTopic, @RequestPart MultipartFile myFile) {
//		
////        String src = dataMap.get("source");
////        String kafkaTopic = dataMap.get("kafkaTopic");	
//		System.out.println("source : "+source+", topic : "+kafkaTopic);
////		System.out.println(myFile.getName());
//		return new ResponseEntity<>("Ok", HttpStatus.OK);
//		
//	}

	@RequestMapping(value = "/upload",consumes  = "multipart/form-data")
	public ResponseEntity<Object> generateData(@RequestParam String kafkaTopic, @RequestParam String fileContent,@RequestParam String entity,@RequestParam int iteration,@RequestParam int delayFrequency,@RequestParam String frequencyType,@RequestParam String startDate,@RequestParam String endDate) {
		logger.info("Saving file to local server");
//		ResponseEntity<Object> obj = IUtils.saveUploadedFile(file, upPath);
//		logger.info("File saved in local server");
//		Documents doc = saveDocument(upPath, module, file);
//		sendMessageToKafka(doc);
		try {
		if(StringUtils.isBlank(kafkaTopic)) {
        	throw new ParseException("Missing required option: -kafkaTopic");
        	
        }
		File tempSource = new File(System.getProperty("java.io.tmpdir")+"/"+"source"+".json");
		FileWriter fw=new FileWriter(tempSource.getAbsolutePath());    
        fw.write(fileContent);    	
        fw.close(); 
		 File tempDestinationFile = new File(System.getProperty("java.io.tmpdir")+"/"+"temp"+".json");
		 String jsonString = KafkaApplication.generateRandomData(tempSource, tempDestinationFile);
// 		ResponseEntity<String> response = uploadDataToKafka(jsonString, tempDestinationFile, sourceFile.getName(), kafkaTopic);
 		List<String> jsonList = CommonUtil.parseJson(jsonString);
 		
 		for(String jsonStr: jsonList) {
 			KafkaApplication.fireEvent(jsonStr, kafkaTopic);
 		}
         if(tempDestinationFile.exists()) {
         	tempDestinationFile.delete();
         }
		}catch(Exception e) {
			System.err.println(e.getMessage());
		}
		return new ResponseEntity<>("Ok", HttpStatus.OK);
	}
	@RequestMapping(value = "/usinginputs",consumes  = "multipart/form-data")
	public ResponseEntity<Object> generateData2(@RequestParam String kafkaTopic, @RequestParam String fileContent,@RequestParam String entity,@RequestParam int iteration,@RequestParam int delayFrequency,@RequestParam String frequencyType,@RequestParam String startDate,@RequestParam String endDate) {
		logger.info("Saving file to local server");
//		ResponseEntity<Object> obj = IUtils.saveUploadedFile(file, upPath);
//		logger.info("File saved in local server");
//		Documents doc = saveDocument(upPath, module, file);
//		sendMessageToKafka(doc);
		try {
		if(StringUtils.isBlank(kafkaTopic)) {
        	throw new ParseException("Missing required option: -kafkaTopic");
        	
        }
//		JSONParser parser = new JSONParser(fileContent); 
//		System.out.println(parser.toString());
//		JSONObject jsonObject = new JSONObject(fileContent);
		
		File tempSource = new File(System.getProperty("java.io.tmpdir")+"/"+"source"+".json");
		FileWriter fw=new FileWriter(tempSource.getAbsolutePath());    
        fw.write(fileContent);    	
        fw.close(); 
		 File tempDestinationFile = new File(System.getProperty("java.io.tmpdir")+"/"+"temp"+".json");
		 String jsonString = KafkaApplication.generateRandomData(tempSource, tempDestinationFile);
// 		ResponseEntity<String> response = uploadDataToKafka(jsonString, tempDestinationFile, sourceFile.getName(), kafkaTopic);
 		List<String> jsonList = CommonUtil.parseJson(jsonString);
 		
 		for(String jsonStr: jsonList) {
 			KafkaApplication.fireEvent(jsonStr, kafkaTopic);
 		}
         if(tempDestinationFile.exists()) {
         	tempDestinationFile.delete();
         }
		}catch(Exception e) {
			System.err.println(e.getMessage());
		}
		return new ResponseEntity<>("Ok", HttpStatus.OK);
	}

}
