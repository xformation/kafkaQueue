/**
 * 
 */
package com.synectiks.kafka.controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.synectiks.json.datagenerator.impl.NonCloseableBufferedOutputStream;
import com.synectiks.kafka.KafkaApplication;
import com.synectiks.kafka.helpers.CommonUtil;

@RestController
@RequestMapping(path = "/randomDataGenerator", method = RequestMethod.POST)
public class DataGeneratorController {

	private static final Logger logger = LoggerFactory.getLogger(DataGeneratorController.class);

	@Value("${search.fire.event.url}")
	private String searchUrl;
	
	@Value("${kafka.topic}")
	private String defTopic;
	
	@Value("${kafka.group}")
	private String defGroup;

	/**
	 * API to send message in kafka queue.
	 * 
	 * @param msg
	 * @return
	 * @throws IOException 
	 */
	@RequestMapping(value = "/generate", consumes = "multipart/form-data")
	public ResponseEntity<Object> generateData(@RequestParam String kafkaTopic, 
			@RequestParam String sourceJson,
			@RequestParam String entity,
			@RequestParam(required = false) Integer iteration,
			@RequestParam(required = false) Integer delayFrequency,
			@RequestParam(required = false) String frequencyType,
			@RequestParam(required = false) String startDate,
			@RequestParam(required = false) String endDate) throws IOException {
		
		logger.info("Random data generation initiated");
		File tempFile = null;
		OutputStream outputStream = null;
		try {
			tempFile = new File(System.getProperty("java.io.tmpdir")+"/temp.json");
			tempFile.deleteOnExit();
			outputStream = tempFile != null ? new FileOutputStream(tempFile) : new NonCloseableBufferedOutputStream(System.out);
			
			String jsonString = KafkaApplication.generateRandomData(sourceJson, outputStream);
			logger.debug("Random data generated");
			
			List<String> jsonList = CommonUtil.parseJson(jsonString);
			logger.debug("Json array created from random data");
			logger.debug("Pushing random data to kafka");
	 		for(String jsonStr: jsonList) {
	 			KafkaApplication.fireEvent(jsonStr, kafkaTopic);
	 			logger.debug("Data : "+jsonStr);
	 		}
	 		
	        logger.debug("Data transfert to kafka completed");
		}catch(Exception e) {
			e.printStackTrace();
			logger.error("Random data generation failed. Exception : ",e);
		}finally {
			if(outputStream != null) {
				outputStream.close();
			}
			if(tempFile != null && tempFile.exists()) {
				tempFile.delete();
			}
		}
		logger.info("Random data generation completed");
		return new ResponseEntity<>("Ok", HttpStatus.OK);
	}
	
//	@RequestMapping(value = "/usinginputs",consumes  = "multipart/form-data")
//	public ResponseEntity<Object> generateData2(@RequestParam String kafkaTopic, 
//			@RequestParam String fileContent,
//			@RequestParam String entity,
//			@RequestParam int iteration,
//			@RequestParam int delayFrequency,
//			@RequestParam String frequencyType,
//			@RequestParam String startDate,
//			@RequestParam String endDate) {
//		logger.info("Saving file to local server");
////		ResponseEntity<Object> obj = IUtils.saveUploadedFile(file, upPath);
////		logger.info("File saved in local server");
////		Documents doc = saveDocument(upPath, module, file);
////		sendMessageToKafka(doc);
//		try {
//			if(StringUtils.isBlank(kafkaTopic)) {
//	        	throw new ParseException("Missing required option: -kafkaTopic");
//	        }
//	//		JSONParser parser = new JSONParser(fileContent); 
//	//		System.out.println(parser.toString());
//	//		JSONObject jsonObject = new JSONObject(fileContent);
//			
//			File tempSource = new File(System.getProperty("java.io.tmpdir")+"/"+"source"+".json");
//			FileWriter fw=new FileWriter(tempSource.getAbsolutePath());    
//	        fw.write(fileContent);    	
//	        fw.close(); 
//			File tempDestinationFile = new File(System.getProperty("java.io.tmpdir")+"/"+"temp"+".json");
//			String jsonString = KafkaApplication.generateRandomData(tempSource, tempDestinationFile);
//	// 		ResponseEntity<String> response = uploadDataToKafka(jsonString, tempDestinationFile, sourceFile.getName(), kafkaTopic);
//	 		List<String> jsonList = CommonUtil.parseJson(jsonString);
//	 		
//	 		for(String jsonStr: jsonList) {
//	 			KafkaApplication.fireEvent(jsonStr, kafkaTopic);
//	 		}
//	        if(tempDestinationFile.exists()) {
//	         	tempDestinationFile.delete();
//	        }
//		}catch(Exception e) {
//			System.err.println(e.getMessage());
//		}
//		return new ResponseEntity<>("Ok", HttpStatus.OK);
//	}

}
