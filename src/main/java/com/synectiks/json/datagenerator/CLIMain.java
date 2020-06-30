package com.synectiks.json.datagenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.synectiks.json.datagenerator.impl.JsonDataGeneratorImpl;
import com.synectiks.json.datagenerator.impl.NonCloseableBufferedOutputStream;
import com.synectiks.kafka.helpers.CommonUtil;

/**
 * Main class for command line interface
 */
public final class CLIMain {

    private static TimeZone DEFAULT_TIMEZONE = TimeZone.getDefault();

    public static final String ENTER_JSON_TEXT = "Enter input json:\n\n ";
    public static String JSON_TO_KAFKA_URL = "http://localhost:9450/api/send-data-to-kafka";
    /**
     * default constructor
     */
    private CLIMain() {

    }

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

    /**
     * main method for command line interface
     * @param args arguments for command line interface
     * @throws IOException if there is an error reading from the input or writing to the output
     * @throws JsonDataGeneratorException if there is an error running the json data converter
     * @throws ParseException if the cli arguments cannot be parsed
     * @throws ClassNotFoundException if the addition classes passed in with -f cannot be found
     */
    @SuppressWarnings("checkstyle:linelength")
    public static void main(final String[] args) throws IOException, JsonDataGeneratorException, ParseException, ClassNotFoundException {
            Options options = buildOptions();
            CommandLineParser parser = new DefaultParser();
            try {
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
//        			fireEvent(jsonStr, kafkaTopic);
        		}
                if(tempDestinationFile.exists()) {
                	tempDestinationFile.delete();
                }

            } catch (ParseException e) {
                System.err.println(e.getMessage());
                throw e;
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


    private static ResponseEntity<String> uploadDataToKafka(String jsonString, File tempDestinationFile, String entity, String kafkaTopic) throws IOException {
    	HttpHeaders headers = new HttpHeaders();
    	headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    	MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    	body.add("file", getTempOutputFile(jsonString, entity));
    	
    	HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
    	
    	RestTemplate restTemplate = new RestTemplate();
    	
    	ResponseEntity<String> response = restTemplate.postForEntity(JSON_TO_KAFKA_URL+"?kafkaTopic="+kafkaTopic, requestEntity, String.class);
    	System.out.println(response);
    	return response;
    }
    
    public static FileSystemResource getTempOutputFile(String jsonString, String entity) throws IOException {
        Path tempFile = Files.createTempFile(entity, ".json");
        Files.write(tempFile, jsonString.getBytes());
        System.out.println("uploading: " + tempFile);
        File file = tempFile.toFile();
        file.deleteOnExit();
        return new FileSystemResource(file);
    }
    
    /**
     * helper {@link Comparator} to wort arguments in help
     */
    private static class OptionComparator implements Comparator<Option> {
        private final List<String> orderList;

        OptionComparator(final List<String> orderList) {
            this.orderList = orderList;
        }


        @Override
        public int compare(final Option o1, final Option o2) {
            int index1 = orderList.indexOf(o1.getOpt());
            int index2 = orderList.indexOf(o2.getOpt());
            return index1 - index2;
        }
    }

    
}
