package com.synectiks.kafka;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;

import com.synectiks.commons.utils.IUtils;
import com.synectiks.json.datagenerator.JsonDataGenerator;
import com.synectiks.json.datagenerator.JsonDataGeneratorException;
import com.synectiks.json.datagenerator.impl.JsonDataGeneratorImpl;
import com.synectiks.json.datagenerator.impl.NonCloseableBufferedOutputStream;
import com.synectiks.kafka.helpers.CommonUtil;

@SpringBootApplication
@ComponentScan("com.synectiks")
public class KafkaApplication {

	private static final Logger logger = LoggerFactory.getLogger(KafkaApplication.class);
	public static String KAFKA_URL = "http://localhost:8190/kafka/send";
	private static final String PRM_MSG = "msg";
	private static final String PRM_TOPIC = "topic";
	
	private static ConfigurableApplicationContext ctx;

	private static Options buildOptions() {
        Options options = new Options();

        Option o = new Option("s", "sourceFile", true,
            "the source file.");
        o.setRequired(false);
        options.addOption(o);
        
        Option kafkaTopic = new Option("kafkaTopic", "kafkaTopic", true,
                "kafka topic.");
            o.setRequired(false);
            options.addOption(kafkaTopic);

        return options;
    }
	
	public static void main(String[] args) {
		System.out.println(args[0]);
		if(args.length == 1 && args[0].contains("--SERVER_PORT=8190")) {
			ctx = SpringApplication.run(KafkaApplication.class, args);
			for (String bean : ctx.getBeanDefinitionNames()) {
				logger.info("Beans: " + bean);
			}
		}else {
			generateData(args);
		}
		
	}

	@SuppressWarnings("checkstyle:linelength")
    public static void generateData(final String[] args) {
            try {
            	Options options = buildOptions();
                CommandLineParser parser = new DefaultParser();
                
            	CommandLine cmd = parser.parse(options, args);

                String source = cmd.getOptionValue("s");
                if (source == null) {
                    throw new ParseException("Missing required option: -s");
                }
                File sourceFile = new File(source);
                if (!sourceFile.exists()) {
                    throw new FileNotFoundException(source + " cannot be found");
                }
                
                String kafkaTopic = cmd.getOptionValue("kafkaTopic");
                if(StringUtils.isBlank(kafkaTopic)) {
                	throw new ParseException("Missing required option: -kafkaTopic");
                }
                
                File tempDestinationFile = new File(System.getProperty("java.io.tmpdir")+"/"+sourceFile.getName()+".json");
        		String jsonString = generateRandomData(sourceFile, tempDestinationFile);
//        		ResponseEntity<String> response = uploadDataToKafka(jsonString, tempDestinationFile, sourceFile.getName(), kafkaTopic);
        		List<String> jsonList = CommonUtil.parseJson(jsonString);
        		
        		for(String jsonStr: jsonList) {
        			fireEvent(jsonStr, kafkaTopic);
        		}
                if(tempDestinationFile.exists()) {
                	tempDestinationFile.delete();
                }

            } catch (Exception e) {
                System.err.println(e.getMessage());
            }

    }
	
	private static String generateRandomData(File sourceFile, File tempDestinationFile)
			throws JsonDataGeneratorException, IOException, FileNotFoundException {
		tempDestinationFile.deleteOnExit();
		String jsonString = "";
		JsonDataGenerator jsonDataGenerator = new JsonDataGeneratorImpl();
		try (InputStream inputStream = new FileInputStream(sourceFile);
		     OutputStream outputStream = tempDestinationFile != null
		             ? new FileOutputStream(tempDestinationFile)
		         : new NonCloseableBufferedOutputStream(System.out)) {
		    jsonString = jsonDataGenerator.generateTestDataJson(inputStream, outputStream);
		    System.out.println(jsonString);
		}
		return jsonString;
	}
	
	private static void fireEvent(String jsonStr, String kafkaTopic) {
		RestTemplate restTemplate = new RestTemplate();
//    	ApplicationProperties applicationProperties = JsontokafkaApp.getBean(ApplicationProperties.class);
		String res = null;
		try {
			res = IUtils.sendGetRestRequest(
					restTemplate, 
					KAFKA_URL,
					IUtils.getRestParamMap(PRM_TOPIC, kafkaTopic, PRM_MSG, jsonStr), 
					String.class);
			logger.debug("Response : "+res);
		} catch(Exception ex) {
			logger.error(ex.getMessage(), ex);
			res = null;
		}
	}
	
	/**
	 * Utility method to get bean from spring context.
	 * @param cls
	 * @return
	 */
	public static <T> T getBean(Class<T> cls) {
		return ctx.getBean(cls);
	}

}

