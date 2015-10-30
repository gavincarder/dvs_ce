/*******************************************************************************
 * Copyright (c) 2015 CA.  All rights reserved.
 *
 * This source file is licensed under the terms of the Eclipse Public License 1.0
 * For the full text of the EPL please see https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ca.dvs.utilities.raml;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Scanner;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * Simple JSON utility methods
 * <p>
 * 
 * @author CA Technologies
 * @version 1
 */
public class JsonUtil {
	
	static Logger logger = Logger.getLogger(RamlUtil.class);
	
	/**
	 * Serialize a Java Map into a JSON string
	 * <p>
	 * @param objMap the source Java Map
	 * @return the serialized Java Map in JSON format
	 */
	public static String serialize(Map<String,Object> objMap) {
		String json = null;
		if (null!=objMap && objMap.size()>0) {
			GsonBuilder gsonBuilder = new GsonBuilder();
			gsonBuilder.setPrettyPrinting();
			gsonBuilder.serializeNulls();
			Gson gson = gsonBuilder.create();
			json = gson.toJson(objMap);
		}
		return json;
	}
	
	/**
	 * Serialize a Java object into a JSON string
	 * <p>
	 * @param obj the source Java object
	 * @return the serialized Java object in JSON format
	 */
	public static String serialize(Object obj) {
		String json = null;
		if (null!=obj) {
			GsonBuilder gsonBuilder = new GsonBuilder();
			gsonBuilder.setPrettyPrinting();
			gsonBuilder.serializeNulls();
			Gson gson = gsonBuilder.create();
			json = gson.toJson(obj);
		}
		return json;
	}
	
	/**
	 * Compare two JSON strings
	 * @param json1 the first of two JSON objects to compare
	 * @param json2 the second of two JSON objects to compare
	 * @return true when the JSON strings compare favorably, otherwise false
	 */
	public static boolean equals(String json1, String json2) {

		boolean match = false;
		
		if (null!=json1 && null!=json2) {
			JsonParser parser = new JsonParser();
			
			Object obj1 = parser.parse(json1);
			Object obj2 = parser.parse(json2);
			
			if (obj1 instanceof JsonObject  && obj2 instanceof JsonObject) {
				JsonObject jObj1 = (JsonObject)obj1;
				JsonObject jObj2 = (JsonObject)obj2;
				match = jObj1.equals(jObj2);
			} else if (obj1 instanceof JsonArray  && obj2 instanceof JsonArray) {
				JsonArray jAry1 = (JsonArray)obj1;
				JsonArray jAry2 = (JsonArray)obj2;
				match = jAry1.equals(jAry2);
			}
		}
		return match;
	}
	
	/**
	 * Get a JSON representation of the Raml example, whether it is inline or included.
	 * <p>
	 * @param example the Raml example text
	 * @param resourceDir a folder to search for imported Raml elements
	 * @return Raml example data in JSON format
	 */
	public static String getJsonFromExample(String example, File resourceDir) {
		
		String json = example;

		// Example is not JSON.  Is it a string containing the path to a file?
		File includeFile = null;
		
		includeFile = resourceDir == null ? new File(example.trim()) : new File(resourceDir, example.trim());
		
		logger.debug(String.format("Potential include file: %s", includeFile.getAbsolutePath()));
		
		String includedContent = null;
		
		if (includeFile.canRead()) { // First check to see if example is actually an existing file path (RAML parser include bug workaround)
		
			Scanner scanner = null;
			
			try {
				
				scanner	= new Scanner(includeFile);
				
				includedContent	= scanner.useDelimiter("\\Z").next();
				
				JsonParser	parser			= new JsonParser();
				
				try {
					
					parser.parse(includedContent);

					json = includedContent;

				} catch (JsonSyntaxException jse) {
					
					String msg = String.format("Processing included example %s - %s\nContent follows...\n%s", includeFile.getPath(), jse.getMessage(), includedContent);
					throw new JsonSyntaxException(msg, jse.getCause());
					
				}
				
			} catch (FileNotFoundException e1) {
				
				// If resolving the example content as a file lands us here, it's apparently not a file
				// validate the content by parsing as JSON
				
				try {
					
					JsonParser	parser	= new JsonParser();
					parser.parse( example );

					// If we get this far without an exception, the example is truly JSON
					json = example;
						
				} catch( JsonSyntaxException e ) {
							
						String msg = String.format("Processing example - %s\nContent follows...\n%s", e.getMessage(), example);
						throw new JsonSyntaxException(msg, e.getCause());
							
				}

			} finally {
				
				if (null != scanner) {
					scanner.close();
				}
				
			}

		} else {
			
			try {
				JsonParser	parser	= new JsonParser();
				parser.parse( example );
	
				// If we get this far without an exception, the example is truly JSON
				json = example;

			} catch( JsonSyntaxException e ) {
				
				String msg = String.format("Processing example - %s\nContent follows...\n%s", e.getMessage(), example);
				throw new JsonSyntaxException(msg, e.getCause());
					
			}
		}
		
		return json;
	}

}
