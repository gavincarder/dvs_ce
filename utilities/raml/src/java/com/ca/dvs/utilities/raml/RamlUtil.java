/*******************************************************************************
 * Copyright (c) 2015 CA.  All rights reserved.
 *
 * This source file is licensed under the terms of the Eclipse Public License 1.0
 * For the full text of the EPL please see https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
/**
 * 
 */
package com.ca.dvs.utilities.raml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.MimeType;
import org.raml.model.Raml;
import org.raml.model.Resource;
import org.raml.model.Response;
import org.raml.parser.loader.FileResourceLoader;
import org.raml.parser.rule.ValidationResult;
import org.raml.parser.visitor.RamlDocumentBuilder;
import org.raml.parser.visitor.RamlValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * Primary module for RAML transformations
 * <p>
 * @author CA Technologies
 * @version 1
 */
public class RamlUtil {

	static Logger logger = LoggerFactory.getLogger(RamlUtil.class);

	/**
	 * @param dir the directory in which to search for RAML files
	 * @return a File array of RAML files
	 */
	public static File[] getRamlFiles(File dir) {
		
		File[] ramlFiles = dir.listFiles(
				new FilenameFilter() {
									    public boolean accept(File dir, String name) {
									        return name.toLowerCase().endsWith(".raml");
									    }
				}
		);
		
		return ramlFiles;
	}
		
	/**
	 * @param ramlFile the RAML file to validate
	 * @return a list of ValidationResults (diagnostics)
	 * @throws IOException
	 */
	public static List<ValidationResult> validateRaml(File ramlFile) throws IOException {
		
		List<ValidationResult> results = null;
		
		FileResourceLoader      resourceLoader	= new FileResourceLoader(ramlFile.getParentFile());
		RamlValidationService   rvs       		= RamlValidationService.createDefault(resourceLoader);
		FileInputStream			ramlFileStream 	= null;
		
		try {
			
			ramlFileStream	= new FileInputStream(ramlFile);
			
			results			= rvs.validate(ramlFileStream, ramlFile.getAbsolutePath());
			
		} finally {
			
			if (null != ramlFileStream) {
				
				ramlFileStream.close();
				
				ramlFileStream = null;
			}
		}
		
		return results;
	}
	
	/**
	 * @param results the results from the RAML validation
	 * @return a message string with the concatenated diagnostics from the RAML validation
	 */
	public static String validationMessages(List<ValidationResult> results) {
		
		StringBuilder sb = new StringBuilder();

		for (ValidationResult result : results) {
			
			sb.append(result.getLevel());
			
			if (result.getLine()>0) {
				
				sb.append(String.format(" (line %d)", result.getLine()));
				
			}
			
			sb.append(String.format(" - %s\n", result.getMessage()));
		}

		return sb.toString();

	}
	
	/**
	 * @param raml the Raml object from which to extract schema information
	 * @param resourceDir the directory anchor from which any possible included resources must be loaded from
	 * @return the JSON schema object for all of the schemas in the Raml
	 */
	public static String getSchemaJson(Raml raml, File resourceDir) {
		
		GsonBuilder gsonBuilder = new GsonBuilder();
		
		gsonBuilder.setPrettyPrinting();
		gsonBuilder.serializeNulls();
		
		Gson 		gson 		= gsonBuilder.create();

		Map<String, Object> schemaMap = new LinkedHashMap<String, Object>();
		
		List<Map<String, String>> schemas = raml.getSchemas();
		
		for(Map<String, String> map : schemas) {
			
			for (Entry<String, String> entry : map.entrySet()) {
				schemaMap.put(entry.getKey(), entry.getValue());
			}
			
		}
				
		return gson.toJson(schemaMap);
		
	}
	
	/**
	 * @param raml the Raml object from which to extract Json sample data
	 * @param resourceDir the directory anchor from which any possible included resources must be loaded from
	 * @return the sample data for all resources in the raml, formatted as Json.
	 */
	public static String getSampleJson(Raml raml, File resourceDir) {
		
		Map<String, Map<String, Object>>	sampleData	= getSampleData(raml, resourceDir);
		GsonBuilder							gsonBuilder	= new GsonBuilder();
		
		gsonBuilder.setPrettyPrinting();
		gsonBuilder.serializeNulls();
		
		Gson gson = gsonBuilder.create();
		
		return gson.toJson(sampleData);

	}
	
	// TODO Remove the following method when Bo syncs-up code with the new API (with the additional parameter)
	/**
	 * @param raml the Raml object from which to extract sample data for all Resources
	 * @return the sample data as a Map object, suitable to be converted into a Json object
	 */
	public static Map<String, Map<String, Object>> getSampleData(Raml raml) {
		return getSampleData(raml, null);
	}
	
	/**
	 * @param raml the Raml object from which to extract sample data for all Resources
	 * @param resourceDir the directory anchor from which any possible included resources must be loaded from
	 * @return the sample data as a Map object, suitable to be converted into a Json object
	 */
	public static Map<String, Map<String, Object>> getSampleData(Raml raml, File resourceDir) {

		Map<String, Map<String, Object>> 	allSamples	= new LinkedHashMap<String, Map<String, Object>>();
		Map<String, Resource>				resMap 		= raml.getResources();
		
		for (Entry<String, Resource> resEntry : resMap.entrySet()) {
			
			String				key				= resEntry.getKey();
			Resource			resource		= resEntry.getValue();
			Map<String, Object>	resourceSamples	= getSampleData(resource, resourceDir);
			
			allSamples.put(key, resourceSamples);
		}

		return allSamples;

	}
	
	/**
	 * @param resource the Raml Resource object from which to extract example data
	 * @param resourceDir the directory anchor from which any possible included resources must be loaded from
	 * @return the object containing the example data
	 */
	public static Map<String, Object> getSampleData(Resource resource, File resourceDir) {
		
		Map<String, Object> sampleData = new LinkedHashMap<String, Object>();
		
		Action getAction = resource.getAction(ActionType.GET);
		if (null != getAction) {
			
			Map<String, Response> getResponses = getAction.getResponses();
			
			if (null != getResponses) {
				
				Response response200 = getResponses.get("200");
				
				if (null != response200) {
		
					// We're only interested in data associated with a successful GET operation
					Map<String, MimeType> bodyMap = response200.getBody();
					
					if (null != bodyMap) {
					
						// We can only deal with JSON style data for the moment
						MimeType mimeBody = bodyMap.get("application/json");
						
						if (null != mimeBody) {
				
							String		schema	= mimeBody.getSchema();
							
							if (null != schema) {
								
								try {
									
									sampleData.put("schema", getJsonElementValue(schema));
			
								} catch( JsonSyntaxException e ) {
									String msg = String.format("Parsing schema for resource %s - %s", resource.getUri(), e.getMessage());
									logger.error(msg);
									throw new JsonSyntaxException(msg, e.getCause());
								}
								
							} else {
								sampleData.put("schema", null);
							}
			
							String		example	= mimeBody.getExample();
							
							if (null != example) {

								try {
									
									String		jsonString	= JsonUtil.getJsonFromExample(example, resourceDir);
									
									sampleData.put("example", getJsonElementValue(jsonString));
																
								} catch( JsonSyntaxException e ) {
									String msg = String.format("Parsing example for resource %s - %s", resource.getUri(), e.getMessage());
									logger.error(msg);
									throw new JsonSyntaxException(msg, e.getCause());
								}
							} else {
								sampleData.put("example", null);
							}
						}
					}
				}
			}
		}
		
		return sampleData;
	}

	/**
	 * @param jsonString
	 * @return the appropriate JsonElement object from the parsed jsonString
	 */
	private static Object getJsonElementValue(String jsonString) {

		JsonParser	jsonParser	= new JsonParser();
		JsonElement	jsonElement	= jsonParser.parse(jsonString);
		
		if (jsonElement.isJsonObject()) {
			
			return jsonElement.getAsJsonObject();
			
		} else if (jsonElement.isJsonArray()){
			
			return jsonElement.getAsJsonArray();
			
		} else if (jsonElement.isJsonNull()) {
			
			return jsonElement.getAsJsonNull();
			
		} else if (jsonElement.isJsonPrimitive()) {

			return jsonElement.getAsJsonPrimitive();

		} else {
			
			return null;
			
		}
	}
	
	/**
	 * Used for simple RAML transformation testing
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
	
		try {
			if (args.length>1) {
	
				File ramlFile = new File(args[0]);
	
				if (ramlFile.canRead()) {
	
					//
					// Validate the RAML file
					//
					List<ValidationResult> results = RamlValidationService.createDefault().validate(ramlFile.getPath());
	
					// If the RAML file is valid, get to work...
					if (ValidationResult.areValid(results)) {
	
						FileInputStream		ramlFileStream	= new FileInputStream(ramlFile.getAbsolutePath());
						try {
							FileResourceLoader 	resourceLoader	= new FileResourceLoader(ramlFile.getParentFile());
							RamlDocumentBuilder rdb         	= new RamlDocumentBuilder(resourceLoader);
							Raml				raml			= rdb.build(ramlFileStream, ramlFile.getAbsolutePath());
		
							if (null!=raml) {
		
								switch(args[1].toUpperCase()) {
								case "EDM":
									EDM edm = new EDM(raml);
									Document edmDoc = edm.getDocument();
									if (args.length>2) {
										File edmFile = new File(args[2]);
										if (ramlFile.equals(edmFile)) {
											String msg = String.format("input file (%s) and output file (%s) cannot be the same", ramlFile, edmFile);
											logger.error(msg);
											throw new Exception(msg);
										} else {
											PrintWriter printWriter = new PrintWriter(edmFile);
											try {
												EDM.prettyPrint(edmDoc, printWriter);
											} finally {
												printWriter.close();
											}
										}
									} else {
										PrintWriter printWriter = new PrintWriter(System.out);
										try {
											EDM.prettyPrint(edmDoc, printWriter);
										} finally {
											printWriter.close();
										}
										
									}
									break;
								case "VSI":
									VSI vsi = new VSI(raml, ramlFile.getParentFile(), true);
									if (args.length>2) {
										File vsiFile = new File(args[2]);
										if (ramlFile.equals(vsiFile)) {
											String msg = String.format("input file (%s) and output file (%s) cannot be the same", ramlFile, vsiFile);
											logger.error(msg);
											throw new Exception(msg);
										} else {
											PrintWriter printWriter = new PrintWriter(vsiFile);
											try {
												VSI.prettyPrint(vsi.getDocument(), printWriter);
											} finally {
												printWriter.close();
											}
										}
									} else {
										PrintWriter printWriter = new PrintWriter(System.out);
										try {
											VSI.prettyPrint(vsi.getDocument(), printWriter);
										} finally {
											printWriter.close();
										}
									}
									break;
								case "WADL":
									WADL wadl = new WADL(raml);
									if (args.length>2) {
										File wadlFile = new File(args[2]);
										if (ramlFile.equals(wadlFile)) {
											String msg = String.format("input file (%s) and output file (%s) cannot be the same", ramlFile, wadlFile);
											logger.error(msg);
											throw new Exception(msg);
										} else {
											PrintWriter printWriter = new PrintWriter(wadlFile);
											try {
												WADL.prettyPrint(wadl.getDocument(), printWriter);
											} finally {
												printWriter.close();
											}
										}
									} else {
										PrintWriter printWriter = new PrintWriter(System.out);
										try {
											WADL.prettyPrint(wadl.getDocument(), printWriter);
										} finally {
											printWriter.close();
										}
									}
									break;
								default:
									throw new Exception("Unhandled output type - "+args[1]);
								}
		
							}
						} finally {
							if (ramlFileStream!=null) {
								try {
									ramlFileStream.close();
								} catch(IOException ioe) {
									ramlFileStream = null;
								}
							}
						}
					} else { // invalid RAML file
	
						StringBuffer sb = new StringBuffer();
	
						sb.append("Error(s) parsing RAML file: "+ramlFile.getName()+"\n");
	
						for (ValidationResult result : results) {
	
							sb.append(String.format("line %d: (%d-%d): %s\n", result.getLine()+1, result.getStartColumn(), result.getEndColumn(), result.getMessage()));
	
						}
						logger.error(sb.toString());
						throw new Exception(sb.toString());
					}
				} else {
	
					String msg = "Cannot read file: "+ramlFile.getPath();
					logger.error(msg);
					throw new Exception(msg);
	
				}
	
			} else {
	
				String msg = "usage: <RAML filespec> <Output Type> <Output File -- optional>\n\twhere <Output Type> is one of EDM, VSI or WADL";
				logger.error(msg);
				throw new Exception(msg);
	
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			throw e;
		}
	}

}
