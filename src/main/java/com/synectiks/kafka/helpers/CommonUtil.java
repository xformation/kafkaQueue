package com.synectiks.kafka.helpers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

public class CommonUtil {

	private static final Logger logger = LoggerFactory.getLogger(CommonUtil.class);
	/**
     * Parse the JSON file using the specified character encoding
     *
     * @param file
     * @return
     */
    public static List<String> parseJsonFile(File file, String encoding) throws Exception{
        if(StringUtils.isBlank(encoding)) {
        	encoding = "UTF-8";
        }
    	List<String> flatJson = null;
        String json = "";

        try {
            json = FileUtils.readFileToString(file, encoding);
            flatJson = parseJson(json);
        } catch (Exception e) {
        	logger.error("Exception. parseJson(file, encoding) : ", e);
            throw e;
        } 
        
        return flatJson;
    }
    
    /**
     * Parse the JSON String
     *
     * @param json
     * @return
     * @throws Exception
     */
    public static List<String> parseJson(String json) {
        List<String> flatJson = null;

        try {
            logger.info("Handle the JSON String as JSON Array");
            flatJson = handleAsArray(json);
        } catch (Exception e) {
        	logger.error("Exception. Json array exception : ",e);
        }

        return flatJson;
    }
    
    /**
     * Handle the JSON String as Array
     *
     * @param json
     * @return
     * @throws Exception
     */
    private static List<String> handleAsArray(String json) {
        List<String> flatJson = null;
        try {
            JSONArray jsonArray = new JSONArray(json);
            flatJson = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
            	JSONObject jsonObject = jsonArray.getJSONObject(i);
                flatJson.add(jsonObject.toString());
            }
        } catch (Exception e) {
            logger.error("JSON might be malformed, Please verify that your JSON is valid: ",e);
        }
        return flatJson;
    }
    
	
	
	public static String getValueByKey(Environment env, String key, String defVal) {
		if (env == null || StringUtils.isBlank(key)) {
			logger.error(env + " or " + key + " should not be null.");
			return null;
		}
		String val = env.getProperty(key);
		if (StringUtils.isBlank(val)) {
			val = defVal;
		}
		return val;
	}
	
}
