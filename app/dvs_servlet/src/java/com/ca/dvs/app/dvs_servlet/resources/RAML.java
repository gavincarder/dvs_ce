/*******************************************************************************
 * Copyright (c) 2015 CA.  All rights reserved.
 *
 * This source file is licensed under the terms of the Eclipse Public License 1.0
 * For the full text of the EPL please see https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
/**
 * 
 */
package com.ca.dvs.app.dvs_servlet.resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidParameterException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.raml.model.Raml;
import org.raml.parser.loader.FileResourceLoader;
import org.raml.parser.rule.ValidationResult;
import org.raml.parser.visitor.RamlDocumentBuilder;
import org.w3c.dom.Document;

import com.ca.dvs.app.dvs_servlet.misc.FileUtil;
import com.ca.dvs.utilities.raml.RamlUtil;
import com.ca.dvs.app.dvs_servlet.misc.VirtualServiceBuilder;
import com.ca.dvs.utilities.raml.EDM;
import com.ca.dvs.utilities.raml.VSI;
import com.ca.dvs.utilities.raml.WADL;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * RAML related resource requests
 * <p>
 * @author CA Technologies
 * @version 1
 */
@Path("raml")
public class RAML {
	
	private static Logger log = Logger.getLogger(RAML.class);

	/**
	 * Get the servlet configuration
	 * <p>
	 * @return HTTP response containing the Servlet configuration in JSON format
	 */
	@GET
	@Path("config")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getConfig() {
		
		log.info("GET raml/config");
		
		Map<String, Object> configMap = new LinkedHashMap<String, Object>();
		
		try {
			Context initialContext = new InitialContext();
			Context envContext     = (Context) initialContext.lookup("java:comp/env");
			
			configMap.put("vseServerUrl",				envContext.lookup("vseServerUrl"));
			configMap.put("vseServicePortRange",		envContext.lookup("vseServicePortRange"));
			configMap.put("vseServiceReadyWaitSeconds",	envContext.lookup("vseServiceReadyWaitSeconds"));
			
		} catch (NamingException e) {
			e.printStackTrace();
			log.error("Failed to obtain servlet configuration", e.getCause());
		}

		GsonBuilder gsonBuilder = new GsonBuilder();
		
		gsonBuilder.setPrettyPrinting();
		gsonBuilder.serializeNulls();
		
		Gson gson = gsonBuilder.create();

		Response response = Response.status(200).entity(gson.toJson(configMap)).build();

		return response;
	}

	/**
	 * Extract sample data as defined in an uploaded RAML file
	 * <p>
	 * @param uploadedInputStream the file content associated with the RAML file upload
	 * @param fileDetail the file details associated with the RAML file upload
	 * @return HTTP response containing sample data extracted from the uploaded RAML file, in JSON format
	 */
	@POST
	@Path("sampleData")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSampleData(
			@DefaultValue("")		@FormDataParam("file")			InputStream					uploadedInputStream,
			@DefaultValue("")		@FormDataParam("file")			FormDataContentDisposition	fileDetail) {

		log.info("POST raml/sampleData");

		Response			response		= null;
		File    			uploadedFile	= null;
		File    			ramlFile		= null;
		FileInputStream     ramlFileStream	= null;

		try {
			
			if (fileDetail == null || fileDetail.getFileName() == null || fileDetail.getName() == null) {
				throw new InvalidParameterException("file");
			}
			
			uploadedFile = FileUtil.getUploadedFile(uploadedInputStream, fileDetail);
					
			if (uploadedFile.isDirectory()) { // find RAML file in directory
				
				// First, look for a raml file that has the same base name as the uploaded file
				String targetName = Files.getNameWithoutExtension(fileDetail.getFileName())+".raml";

				ramlFile = FileUtil.selectRamlFile(uploadedFile, targetName);
									
			} else {
				
				ramlFile = uploadedFile;
				
			}
			
			List<ValidationResult> results = null;
			
			try {
				
				results = RamlUtil.validateRaml(ramlFile);
				
			} catch (IOException e) {
				
				String msg = String.format("RAML validation failed catastrophically for %s", ramlFile.getName());
				log.error(msg, e.getCause());
				throw new Exception(msg, e.getCause());
			}
			
			// If the RAML file is valid, get to work...
			if (ValidationResult.areValid(results)) {

				try {
					
					ramlFileStream = new FileInputStream(ramlFile.getAbsolutePath());
					
				} catch (FileNotFoundException e) {
					
					String msg = String.format("Failed to open input stream from %s", ramlFile.getAbsolutePath());
					
					throw new Exception(msg, e.getCause());
					
				}

				FileResourceLoader 	resourceLoader	= new FileResourceLoader(ramlFile.getParentFile());
				RamlDocumentBuilder rdb         	= new RamlDocumentBuilder(resourceLoader);
				Raml				raml			= rdb.build(ramlFileStream, ramlFile.getAbsolutePath());
				
				ramlFileStream.close();
				ramlFileStream = null;

				//String schemaJson = RamlUtil.getSchemaJson(raml, ramlFile.getParentFile());
				String sampleData = RamlUtil.getSampleJson(raml, ramlFile.getParentFile());
				
				response = Response.status(Status.OK).entity(sampleData).build();
				
			} else { // RAML file failed validation
	
				response = Response.status(Status.BAD_REQUEST).entity(RamlUtil.validationMessages(results)).build();
	
			}
			
		} catch (Exception ex) {

			ex.printStackTrace();
			
			String msg = ex.getMessage();
			
			log.error(msg, ex);

			if (ex instanceof JsonSyntaxException) {
				
				response = Response.status(Status.BAD_REQUEST).entity(msg).build();
				
			} else if (ex instanceof InvalidParameterException) {
				
				response = Response.status(Status.BAD_REQUEST).entity(String.format("Invalid form parameter - %s",  ex.getMessage())).build();
			
			}  else {
				
				response = Response.status(Status.INTERNAL_SERVER_ERROR).entity(msg).build();
				
			}
			
			return response;
			
		} finally {
			
			if (null != ramlFileStream) {
				
				try {
					
					ramlFileStream.close();
					
				} catch (IOException e) {
					
					e.printStackTrace();
					
				}
				
			}
			
			if (null != uploadedFile) {
				
				if (uploadedFile.isDirectory()) {
					
					try {
						
						System.gc(); // To help release files that snakeyaml abandoned open streams on -- otherwise, some files may not delete
						
						// Wait a bit for the system to close abandoned streams
						try {

							Thread.sleep(1000);
							
						} catch (InterruptedException e) {
							
							e.printStackTrace();
							
						}
						
						FileUtils.deleteDirectory(uploadedFile);
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					uploadedFile.delete();
				}
			
			}
		}
		
		return response;
    }

	/**
	 * Produce an EDM from an uploaded RAML file
	 * <p>
	 * @param uploadedInputStream the file content associated with the RAML file upload
	 * @param fileDetail the file details associated with the RAML file upload
	 * @return an HTTP response containing the EDM transformation for the uploaded RAML file
	 */
	@POST
	@Path("edm")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_XML)
    public Response genEdm(
			@DefaultValue("")		@FormDataParam("file")			InputStream					uploadedInputStream,
			@DefaultValue("")		@FormDataParam("file")			FormDataContentDisposition	fileDetail) {

		log.info("POST raml/edm");

		Response			response		= null;
		File    			uploadedFile	= null;
		File    			ramlFile		= null;
		FileInputStream     ramlFileStream	= null;

		try {
			
			if (fileDetail == null || fileDetail.getFileName() == null || fileDetail.getName() == null) {
				throw new InvalidParameterException("file");
			}
			
			uploadedFile = FileUtil.getUploadedFile(uploadedInputStream, fileDetail);
					
			if (uploadedFile.isDirectory()) { // find RAML file in directory
				
				// First, look for a raml file that has the same base name as the uploaded file
				String targetName = Files.getNameWithoutExtension(fileDetail.getFileName())+".raml";

				ramlFile = FileUtil.selectRamlFile(uploadedFile, targetName);
									
			} else {
				
				ramlFile = uploadedFile;
				
			}
			
			List<ValidationResult> results = null;
			
			try {
				
				results = RamlUtil.validateRaml(ramlFile);
				
			} catch (IOException e) {
				
				String msg = String.format("RAML validation failed catastrophically for %s", ramlFile.getName());
				throw new Exception(msg, e.getCause());
			}
			
			// If the RAML file is valid, get to work...
			if (ValidationResult.areValid(results)) {

				try {
					
					ramlFileStream = new FileInputStream(ramlFile.getAbsolutePath());
					
				} catch (FileNotFoundException e) {
					
					String msg = String.format("Failed to open input stream from %s", ramlFile.getAbsolutePath());
					
					throw new Exception(msg, e.getCause());
					
				}

				FileResourceLoader 	resourceLoader	= new FileResourceLoader(ramlFile.getParentFile());
				RamlDocumentBuilder rdb         	= new RamlDocumentBuilder(resourceLoader);
				Raml				raml			= rdb.build(ramlFileStream, ramlFile.getAbsolutePath());
				
				ramlFileStream.close();
				ramlFileStream = null;
								
				EDM					edm				= new EDM(raml);
				Document			doc				= null;
				
				try {
	
					doc = edm.getDocument();

					StringWriter stringWriter = new StringWriter();

					EDM.prettyPrint(doc, stringWriter);
					
					response = Response.status(Status.OK).entity(stringWriter.toString()).build();
	
	
				} catch (Exception e) {
	
					String msg = String.format("Failed to build EDM document - %s", e.getMessage());

					throw new Exception(msg, e.getCause());

				}
				
			} else { // RAML file failed validation
	
				response = Response.status(Status.BAD_REQUEST).entity(RamlUtil.validationMessages(results)).build();
	
			}
			
		} catch (Exception ex) {

			ex.printStackTrace();
			
			String msg = ex.getMessage();
			
			log.error(msg, ex);

			if (ex instanceof JsonSyntaxException) {
				
				response = Response.status(Status.BAD_REQUEST).entity(msg).build();
				
			} else if (ex instanceof InvalidParameterException) {
				
				response = Response.status(Status.BAD_REQUEST).entity(String.format("Invalid form parameter - %s",  ex.getMessage())).build();
			
			}  else {
				
				response = Response.status(Status.INTERNAL_SERVER_ERROR).entity(msg).build();
				
			}

			return response;
			
		} finally {
			
			if (null != ramlFileStream) {
				
				try {
					
					ramlFileStream.close();
					
				} catch (IOException e) {
					
					e.printStackTrace();
					
				}
				
			}
			
			if (null != uploadedFile) {
				
				if (uploadedFile.isDirectory()) {
					
					try {
						
						System.gc(); // To help release files that snakeyaml abandoned open streams on -- otherwise, some files may not delete
						
						// Wait a bit for the system to close abandoned streams
						try {

							Thread.sleep(1000);
							
						} catch (InterruptedException e) {
							
							e.printStackTrace();
							
						}
						
						FileUtils.deleteDirectory(uploadedFile);
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					uploadedFile.delete();
				}
			
			}
		}
		
		return response;
    }

	/**
	 * Produce a WADL file from an uploaded RAML file
	 * <p>
	 * @param uploadedInputStream the file content associated with the RAML file upload
	 * @param fileDetail the file details associated with the RAML file upload
	 * @param baseUri the baseUri to use in the returned WADL file.  Optionally provided, this will override that which is defined in the uploaded RAML.
	 * @return HTTP response containing the WADL transformation from the uploaded RAML file
	 */
	@POST
	@Path("wadl")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_XML)
    public Response genWadl(
			@DefaultValue("")	@FormDataParam("file")		InputStream					uploadedInputStream,
			@DefaultValue("")	@FormDataParam("file")		FormDataContentDisposition	fileDetail,
    		@DefaultValue("")	@FormDataParam("baseUri")	String						baseUri	) {

		log.info("POST raml/wadl");

		Response			response		= null;
		File    			uploadedFile	= null;
		File    			ramlFile		= null;
		FileInputStream     ramlFileStream	= null;

		try {
			
			if (fileDetail == null || fileDetail.getFileName() == null || fileDetail.getName() == null) {
				throw new InvalidParameterException("file");
			}

			if (!baseUri.isEmpty()) { // validate URI syntax
				try {
					
					new URI(baseUri);
					
				} catch(URISyntaxException uriEx) {
					
					throw new InvalidParameterException(String.format("baseUri - %s", uriEx.getMessage()));
					
				}
			}
			
			uploadedFile = FileUtil.getUploadedFile(uploadedInputStream, fileDetail);
					
			if (uploadedFile.isDirectory()) { // find RAML file in directory
				
				// First, look for a raml file that has the same base name as the uploaded file
				String targetName = Files.getNameWithoutExtension(fileDetail.getFileName())+".raml";

				ramlFile = FileUtil.selectRamlFile(uploadedFile, targetName);
									
			} else {
				
				ramlFile = uploadedFile;
				
			}
			
			List<ValidationResult> results = null;
			
			try {
				
				results = RamlUtil.validateRaml(ramlFile);
				
			} catch (IOException e) {
				
				String msg = String.format("RAML validation failed catastrophically for %s", ramlFile.getName());
				throw new Exception(msg, e.getCause());
			}
			
			// If the RAML file is valid, get to work...
			if (ValidationResult.areValid(results)) {

				try {
					
					ramlFileStream = new FileInputStream(ramlFile.getAbsolutePath());
					
				} catch (FileNotFoundException e) {
					
					String msg = String.format("Failed to open input stream from %s", ramlFile.getAbsolutePath());
					
					throw new Exception(msg, e.getCause());
					
				}

				FileResourceLoader 	resourceLoader	= new FileResourceLoader(ramlFile.getParentFile());
				RamlDocumentBuilder rdb         	= new RamlDocumentBuilder(resourceLoader);
				Raml				raml			= rdb.build(ramlFileStream, ramlFile.getAbsolutePath());
				
				ramlFileStream.close();
				ramlFileStream = null;
				
				if (!baseUri.isEmpty()) {
					raml.setBaseUri(baseUri);
				}
				
				WADL				wadl			= new WADL(raml, ramlFile.getParentFile());
				Document			doc				= null;
				
				doc = wadl.getDocument();

				StringWriter stringWriter = new StringWriter();

				WADL.prettyPrint(doc, stringWriter);
				
				response = Response.status(Status.OK).entity(stringWriter.toString()).build();
					
			} else { // RAML file failed validation
	
				StringBuilder sb = new StringBuilder();

				for (ValidationResult result : results) {
					
					sb.append(result.getLevel());
					
					if (result.getLine()>0) {
						
						sb.append(String.format(" (line %d)", result.getLine()));
						
					}
					
					sb.append(String.format(" - %s\n", result.getMessage()));
				}

				response = Response.status(Status.BAD_REQUEST).entity(sb.toString()).build();
	
			}
			
		} catch (Exception ex) {

			ex.printStackTrace();
			
			String msg = ex.getMessage();
			
			log.error(msg, ex.getCause());

			if (ex instanceof JsonSyntaxException) {
				
				response = Response.status(Status.BAD_REQUEST).entity(msg).build();
				
			} else if (ex instanceof InvalidParameterException) {
				
				response = Response.status(Status.BAD_REQUEST).entity(String.format("Invalid form parameter - %s",  ex.getMessage())).build();
			
			}  else {
				
				response = Response.status(Status.INTERNAL_SERVER_ERROR).entity(msg).build();
				
			}

			return response;
			
		} finally {
			
			if (null != ramlFileStream) {
				
				try {
					
					ramlFileStream.close();
					
				} catch (IOException e) {
					
					e.printStackTrace();
					
				}
				
			}
			
			if (null != uploadedFile) {
				
				if (uploadedFile.isDirectory()) {
					
					try {
						
						System.gc(); // To help release files that snakeyaml abandoned open streams on -- otherwise, some files may not delete
						
						// Wait a bit for the system to close abandoned streams
						try {

							Thread.sleep(1000);
							
						} catch (InterruptedException e) {
							
							e.printStackTrace();
							
						}
						
						FileUtils.deleteDirectory(uploadedFile);
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					uploadedFile.delete();
				}
			
			}
		}
		
		return response;
    }

	/**
	 * Produce a CA LISA/DevTest Virtual Service Image (VSI) from an uploaded RAML file
	 * <p>
	 * @param uploadedInputStream the file content associated with the RAML file upload
	 * @param fileDetail the file details associated with the RAML file upload
	 * @param baseUri the baseUri to use in the returned WADL file.  Optionally provided, this will override that which is defined in the uploaded RAML.
	 * @param generateServiceDocument when true, the VSI transformation will include a service document transaction defined. (default: false)
	 * @return HTTP response containing the VSI transformation from the uploaded RAML file
	 */
	@POST
	@Path("vsi")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_XML)
    public Response genVsi(
			@DefaultValue("")		@FormDataParam("file")						InputStream					uploadedInputStream,
			@DefaultValue("")		@FormDataParam("file")						FormDataContentDisposition	fileDetail,
    		@DefaultValue("")		@FormDataParam("baseUri")					String						baseUri,
    		@DefaultValue("false")	@FormDataParam("generateServiceDocument")	Boolean						generateServiceDocument	) {

		log.info("POST raml/vsi");

		Response			response		= null;
		File    			uploadedFile	= null;
		File    			ramlFile		= null;
		FileInputStream     ramlFileStream	= null;

		try {
			
			if (fileDetail == null || fileDetail.getFileName() == null || fileDetail.getName() == null) {
				throw new InvalidParameterException("file");
			}
			
			if (!baseUri.isEmpty()) { // validate URI syntax
				try {
					
					new URI(baseUri);
					
				} catch(URISyntaxException uriEx) {
					
					throw new InvalidParameterException(String.format("baseUri - %s", uriEx.getMessage()));
					
				}
			}
			
			uploadedFile = FileUtil.getUploadedFile(uploadedInputStream, fileDetail);
					
			if (uploadedFile.isDirectory()) { // find RAML file in directory
				
				// First, look for a raml file that has the same base name as the uploaded file
				String targetName = Files.getNameWithoutExtension(fileDetail.getFileName())+".raml";

				ramlFile = FileUtil.selectRamlFile(uploadedFile, targetName);
									
			} else {
				
				ramlFile = uploadedFile;
				
			}
			
			List<ValidationResult> results = null;
			
			try {
				
				results = RamlUtil.validateRaml(ramlFile);
				
			} catch (IOException e) {
				
				String msg = String.format("RAML validation failed catastrophically for %s", ramlFile.getName());
				throw new Exception(msg, e.getCause());
			}
			
			// If the RAML file is valid, get to work...
			if (ValidationResult.areValid(results)) {

				try {
					
					ramlFileStream = new FileInputStream(ramlFile.getAbsolutePath());
					
				} catch (FileNotFoundException e) {
					
					String msg = String.format("Failed to open input stream from %s", ramlFile.getAbsolutePath());
					
					throw new Exception(msg, e.getCause());
					
				}

				FileResourceLoader 	resourceLoader	= new FileResourceLoader(ramlFile.getParentFile());
				RamlDocumentBuilder rdb         	= new RamlDocumentBuilder(resourceLoader);
				Raml				raml			= rdb.build(ramlFileStream, ramlFile.getAbsolutePath());
				
				ramlFileStream.close();
				ramlFileStream = null;
				
				if (!baseUri.isEmpty()) {
					raml.setBaseUri(baseUri);
				}
				
				VSI					vsi				= new VSI(raml, ramlFile.getParentFile(), generateServiceDocument);
				Document			doc				= null;
				
				doc = vsi.getDocument();

				StringWriter stringWriter = new StringWriter();

				VSI.prettyPrint(doc, stringWriter);
				
				response = Response.status(Status.OK).entity(stringWriter.toString()).build();
					
			} else { // RAML file failed validation
	
				StringBuilder sb = new StringBuilder();

				for (ValidationResult result : results) {
					
					sb.append(result.getLevel());
					
					if (result.getLine()>0) {
						
						sb.append(String.format(" (line %d)", result.getLine()));
						
					}
					
					sb.append(String.format(" - %s\n", result.getMessage()));
				}

				response = Response.status(Status.BAD_REQUEST).entity(sb.toString()).build();
	
			}
			
		} catch (Exception ex) {

			ex.printStackTrace();
			
			String msg = ex.getMessage();
			
			log.error(msg, ex.getCause());

			if (ex instanceof JsonSyntaxException) {
				
				response = Response.status(Status.BAD_REQUEST).entity(msg).build();
				
			} else if (ex instanceof InvalidParameterException) {
				
				response = Response.status(Status.BAD_REQUEST).entity(String.format("Invalid form parameter - %s",  ex.getMessage())).build();
			
			}  else {
				
				response = Response.status(Status.INTERNAL_SERVER_ERROR).entity(msg).build();
				
			}

			return response;
			
		} finally {
			
			if (null != ramlFileStream) {
				
				try {
					
					ramlFileStream.close();
					
				} catch (IOException e) {
					
					e.printStackTrace();
					
				}
				
			}
			
			if (null != uploadedFile) {
				
				if (uploadedFile.isDirectory()) {
					
					try {
						
						System.gc(); // To help release files that snakeyaml abandoned open streams on -- otherwise, some files may not delete
						
						// Wait a bit for the system to close abandoned streams
						try {

							Thread.sleep(1000);
							
						} catch (InterruptedException e) {
							
							e.printStackTrace();
							
						}
						
						FileUtils.deleteDirectory(uploadedFile);
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					uploadedFile.delete();
				}
			
			}
		}
		
		return response;
    }
	
	/**
	 * Produce a list of request operations from an uploaded RAML file
	 * <p>
	 * @param uploadedInputStream the file content associated with the RAML file upload
	 * @param fileDetail the file details associated with the RAML file upload
	 * @param baseUri the baseUri to use in the returned WADL file.  Optionally provided, this will override that which is defined in the uploaded RAML.
	 * @param generateServiceDocument when true, the VSI transformation will include a service document transaction defined. (default: false)
	 * @return HTTP response containing a list of REST operations defined in the uploaded RAML file
	 */
	@POST
	@Path("vsiOperations")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    public Response genVsiOperations(
			@DefaultValue("")		@FormDataParam("file")						InputStream					uploadedInputStream,
			@DefaultValue("")		@FormDataParam("file")						FormDataContentDisposition	fileDetail,
    		@DefaultValue("")		@FormDataParam("baseUri")					String						baseUri,
    		@DefaultValue("false")	@FormDataParam("generateServiceDocument")	Boolean						generateServiceDocument	) {
    		

		log.info("POST raml/vsiOperations");

		Response			response		= null;
		File    			uploadedFile	= null;
		File    			ramlFile		= null;
		FileInputStream     ramlFileStream	= null;

		try {
			
			if (fileDetail == null || fileDetail.getFileName() == null || fileDetail.getName() == null) {
				throw new InvalidParameterException("file");
			}
			
			if (!baseUri.isEmpty()) { // validate URI syntax
				try {
					
					new URI(baseUri);
					
				} catch(URISyntaxException uriEx) {
					
					throw new InvalidParameterException(String.format("baseUri - %s", uriEx.getMessage()));
					
				}
			}
			
			uploadedFile = FileUtil.getUploadedFile(uploadedInputStream, fileDetail);
					
			if (uploadedFile.isDirectory()) { // find RAML file in directory
				
				// First, look for a raml file that has the same base name as the uploaded file
				String targetName = Files.getNameWithoutExtension(fileDetail.getFileName())+".raml";

				ramlFile = FileUtil.selectRamlFile(uploadedFile, targetName);
									
			} else {
				
				ramlFile = uploadedFile;
				
			}
			
			List<ValidationResult> results = null;
			
			try {
				
				results = RamlUtil.validateRaml(ramlFile);
				
			} catch (IOException e) {
				
				String msg = String.format("RAML validation failed catastrophically for %s", ramlFile.getName());
				throw new Exception(msg, e.getCause());
			}
			
			// If the RAML file is valid, get to work...
			if (ValidationResult.areValid(results)) {

				try {
					
					ramlFileStream = new FileInputStream(ramlFile.getAbsolutePath());
					
				} catch (FileNotFoundException e) {
					
					String msg = String.format("Failed to open input stream from %s", ramlFile.getAbsolutePath());
					
					throw new Exception(msg, e.getCause());
					
				}

				FileResourceLoader 	resourceLoader	= new FileResourceLoader(ramlFile.getParentFile());
				RamlDocumentBuilder rdb         	= new RamlDocumentBuilder(resourceLoader);
				Raml				raml			= rdb.build(ramlFileStream, ramlFile.getAbsolutePath());
				
				ramlFileStream.close();
				ramlFileStream = null;
				
				if (!baseUri.isEmpty()) {
					raml.setBaseUri(baseUri);
				}
				
				VSI					vsi				= new VSI(raml, ramlFile.getParentFile(), generateServiceDocument);

				StringBuffer sb = new StringBuffer();
				for (String operation : vsi.getOperationsList(raml.getResources())) {
					sb.append(String.format("%s\n", operation));
				}
				
				response = Response.status(Status.OK).entity(sb.toString()).build();
					
			} else { // RAML file failed validation
	
				StringBuilder sb = new StringBuilder();

				for (ValidationResult result : results) {
					
					sb.append(result.getLevel());
					
					if (result.getLine()>0) {
						
						sb.append(String.format(" (line %d)", result.getLine()));
						
					}
					
					sb.append(String.format(" - %s\n", result.getMessage()));
				}

				response = Response.status(Status.BAD_REQUEST).entity(sb.toString()).build();
	
			}
			
		} catch (Exception ex) {

			ex.printStackTrace();
			
			String msg = ex.getMessage();
			
			log.error(msg, ex.getCause());

			if (ex instanceof JsonSyntaxException) {
				
				response = Response.status(Status.BAD_REQUEST).entity(msg).build();
				
			} else if (ex instanceof InvalidParameterException) {
				
				response = Response.status(Status.BAD_REQUEST).entity(String.format("Invalid form parameter - %s",  ex.getMessage())).build();
			
			}  else {
				
				response = Response.status(Status.INTERNAL_SERVER_ERROR).entity(msg).build();
				
			}

			return response;
			
		} finally {
			
			if (null != ramlFileStream) {
				
				try {
					
					ramlFileStream.close();
					
				} catch (IOException e) {
					
					e.printStackTrace();
					
				}
				
			}
			
			if (null != uploadedFile) {
				
				if (uploadedFile.isDirectory()) {
					
					try {
						
						System.gc(); // To help release files that snakeyaml abandoned open streams on -- otherwise, some files may not delete
						
						// Wait a bit for the system to close abandoned streams
						try {

							Thread.sleep(1000);
							
						} catch (InterruptedException e) {
							
							e.printStackTrace();
							
						}
						
						FileUtils.deleteDirectory(uploadedFile);
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					uploadedFile.delete();
				}
			
			}
		}
		
		return response;
    }
	
	/**
	 * Deploys an OData virtual service from an uploaded RAML file
	 * <p>
	 * @param uploadedInputStream the file content associated with the RAML file upload
	 * @param fileDetail the file details associated with the RAML file upload
	 * @param baseUri the baseUri to use in the returned WADL file.  Optionally provided, this will override that which is defined in the uploaded RAML.
	 * @param authorization basic authorization string (user:password) used to grant access to LISA/DevTest REST APIs (when required)
	 * @return HTTP response containing a status of OData virtual service deployed from uploaded RAML file
	 */
	@POST
	@Path("odataVs")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deployOdataVS(
    							@DefaultValue("")	@FormDataParam("file")			InputStream					uploadedInputStream,
    							@DefaultValue("")	@FormDataParam("file")			FormDataContentDisposition	fileDetail,
    				    		@DefaultValue("")	@FormDataParam("baseUri")		String						baseUri,
    				    		@DefaultValue("")	@FormDataParam("authorization")	String						authorization	) {

		log.info("POST raml/odataVs");
		
		Response			response		= null;
		File    			uploadedFile	= null;
		File    			ramlFile		= null;
		FileInputStream     ramlFileStream	= null;

		try {
			
			if (fileDetail == null || fileDetail.getFileName() == null || fileDetail.getName() == null) {
				throw new InvalidParameterException("file");
			}
			
			if (!baseUri.isEmpty()) { // validate URI syntax
				try {
					
					new URI(baseUri);
					
				} catch(URISyntaxException uriEx) {
					
					throw new InvalidParameterException(String.format("baseUri - %s", uriEx.getMessage()));
					
				}
			}
			
			uploadedFile = FileUtil.getUploadedFile(uploadedInputStream, fileDetail);
					
			if (uploadedFile.isDirectory()) { // find RAML file in directory
				
				// First, look for a raml file that has the same base name as the uploaded file
				String targetName = Files.getNameWithoutExtension(fileDetail.getFileName())+".raml";

				ramlFile = FileUtil.selectRamlFile(uploadedFile, targetName);
									
			} else {
				
				ramlFile = uploadedFile;
				
			}
			
			List<ValidationResult> results = null;
			
			try {
				
				results = RamlUtil.validateRaml(ramlFile);
				
			} catch (IOException e) {
				
				String msg = String.format("RAML validation failed catastrophically for %s", ramlFile.getName());
				throw new Exception(msg, e.getCause());
			}
			
			// If the RAML file is valid, get to work...
			if (ValidationResult.areValid(results)) {

				try {
					
					ramlFileStream = new FileInputStream(ramlFile.getAbsolutePath());
					
				} catch (FileNotFoundException e) {
					
					String msg = String.format("Failed to open input stream from %s", ramlFile.getAbsolutePath());
					
					throw new Exception(msg, e.getCause());
					
				}

				FileResourceLoader 	resourceLoader	= new FileResourceLoader(ramlFile.getParentFile());
				RamlDocumentBuilder rdb         	= new RamlDocumentBuilder(resourceLoader);
				Raml				raml			= rdb.build(ramlFileStream, ramlFile.getAbsolutePath());
				
				ramlFileStream.close();
				ramlFileStream = null;
				
				if (!baseUri.isEmpty()) {
					raml.setBaseUri(baseUri);
				}
							
				try {
	
					Context initialContext = new InitialContext();
					Context envContext     = (Context) initialContext.lookup("java:comp/env");
					
					String vseServerUrl					= (String) envContext.lookup("vseServerUrl");
					String vseServicePortRange			= (String) envContext.lookup("vseServicePortRange");
					int    vseServiceReadyWaitSeconds	= (Integer)envContext.lookup("vseServiceReadyWaitSeconds");

					// Generate mar and deploy VS
					VirtualServiceBuilder vs = new VirtualServiceBuilder(vseServerUrl, vseServicePortRange, vseServiceReadyWaitSeconds, false, authorization);
					response = vs.setInputFile(raml, ramlFile.getParentFile(), false);
							
				} catch (Exception e) {
	
					String msg = String.format("Failed to deploy service - %s", e.getMessage());

					throw new Exception(msg, e.getCause());

				}
				
			} else { // RAML file failed validation
	
				StringBuilder sb = new StringBuilder();

				for (ValidationResult result : results) {
					
					sb.append(result.getLevel());
					
					if (result.getLine()>0) {
						
						sb.append(String.format(" (line %d)", result.getLine()));
						
					}
					
					sb.append(String.format(" - %s\n", result.getMessage()));
				}

				response = Response.status(Status.BAD_REQUEST).entity(sb.toString()).build();
	
			}
			
		} catch (Exception ex) {

			ex.printStackTrace();
			
			String msg = ex.getMessage();
			
			log.error(msg, ex);

			if (ex instanceof JsonSyntaxException) {
			
				response = Response.status(Status.BAD_REQUEST).entity(msg).build();

			} else if (ex instanceof InvalidParameterException) {
				
				response = Response.status(Status.BAD_REQUEST).entity(String.format("Invalid form parameter - %s",  ex.getMessage())).build();
			
			} else {

				response = Response.status(Status.INTERNAL_SERVER_ERROR).entity(msg).build();

			}

			return response;
			
		} finally {
			
			if (null != ramlFileStream) {
				
				try {
					
					ramlFileStream.close();
					
				} catch (IOException e) {
					
					e.printStackTrace();
					
				}
				
			}
			
			if (null != uploadedFile) {
				
				if (uploadedFile.isDirectory()) {
					
					try {
						
						System.gc(); // To help release files that snakeyaml abandoned open streams on -- otherwise, some files may not delete
						
						// Wait a bit for the system to close abandoned streams
						try {

							Thread.sleep(1000);
							
						} catch (InterruptedException e) {
							
							e.printStackTrace();
							
						}
						
						FileUtils.deleteDirectory(uploadedFile);
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					uploadedFile.delete();
				}
			
			}
		}
		
		return response;
    }

	/**
	 * Deploys an REST virtual service from an uploaded RAML file
	 * <p>
	 * @param uploadedInputStream the file content associated with the RAML file upload
	 * @param fileDetail the file details associated with the RAML file upload
	 * @param baseUri the baseUri to use in the returned WADL file.  Optionally provided, this will override that which is defined in the uploaded RAML.
	 * @param authorization basic authorization string (user:password) used to grant access to LISA/DevTest REST APIs (when required)
	 * @return HTTP response containing a status of REST virtual service deployed from uploaded RAML file
	 */
	@POST
	@Path("restVs")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deployRestVS(
    							@DefaultValue("")		@FormDataParam("file")						InputStream					uploadedInputStream,
    							@DefaultValue("")		@FormDataParam("file")						FormDataContentDisposition	fileDetail,
    							@DefaultValue("")		@FormDataParam("baseUri")					String						baseUri,
    				    		@DefaultValue("false")	@FormDataParam("generateServiceDocument")	Boolean						generateServiceDocument,
    				    		@DefaultValue("")		@FormDataParam("authorization")				String						authorization	) {

		log.info("POST raml/restVs");
		
		Response			response		= null;
		File    			uploadedFile	= null;
		File    			ramlFile		= null;
		FileInputStream     ramlFileStream	= null;

		try {
			
			if (fileDetail == null || fileDetail.getFileName() == null || fileDetail.getName() == null) {
				throw new InvalidParameterException("file");
			}
		
			if (!baseUri.isEmpty()) { // validate URI syntax
				try {
					
					new URI(baseUri);
					
				} catch(URISyntaxException uriEx) {
					
					throw new InvalidParameterException(String.format("baseUri - %s", uriEx.getMessage()));
					
				}
			}
			
			uploadedFile = FileUtil.getUploadedFile(uploadedInputStream, fileDetail);
					
			if (uploadedFile.isDirectory()) { // find RAML file in directory
				
				// First, look for a raml file that has the same base name as the uploaded file
				String targetName = Files.getNameWithoutExtension(fileDetail.getFileName())+".raml";

				ramlFile = FileUtil.selectRamlFile(uploadedFile, targetName);
									
			} else {
				
				ramlFile = uploadedFile;
				
			}
			
			List<ValidationResult> results = null;
			
			try {
				
				results = RamlUtil.validateRaml(ramlFile);
				
			} catch (IOException e) {
				
				String msg = String.format("RAML validation failed catastrophically for %s", ramlFile.getName());
				throw new Exception(msg, e.getCause());
			}
			
			// If the RAML file is valid, get to work...
			if (ValidationResult.areValid(results)) {

				try {
					
					ramlFileStream = new FileInputStream(ramlFile.getAbsolutePath());
					
				} catch (FileNotFoundException e) {
					
					String msg = String.format("Failed to open input stream from %s", ramlFile.getAbsolutePath());
					
					throw new Exception(msg, e.getCause());
					
				}

				FileResourceLoader 	resourceLoader	= new FileResourceLoader(ramlFile.getParentFile());
				RamlDocumentBuilder rdb         	= new RamlDocumentBuilder(resourceLoader);
				Raml				raml			= rdb.build(ramlFileStream, ramlFile.getAbsolutePath());
				
				ramlFileStream.close();
				ramlFileStream = null;
				
				if (!baseUri.isEmpty()) {
					raml.setBaseUri(baseUri);
				}
							
				try {
	
					Context initialContext = new InitialContext();
					Context envContext     = (Context) initialContext.lookup("java:comp/env");
					
					String vseServerUrl					= (String) envContext.lookup("vseServerUrl");
					String vseServicePortRange			= (String) envContext.lookup("vseServicePortRange");
					int    vseServiceReadyWaitSeconds	= (Integer)envContext.lookup("vseServiceReadyWaitSeconds");

					// Generate mar and deploy VS
					VirtualServiceBuilder vs = new VirtualServiceBuilder(vseServerUrl, vseServicePortRange, vseServiceReadyWaitSeconds, generateServiceDocument, authorization);
					response = vs.setInputFile(raml, ramlFile.getParentFile(), true);
							
				} catch (Exception e) {
	
					String msg = String.format("Failed to deploy service - %s", e.getMessage());

					throw new Exception(msg, e.getCause());

				}
				
			} else { // RAML file failed validation
	
				StringBuilder sb = new StringBuilder();

				for (ValidationResult result : results) {
					
					sb.append(result.getLevel());
					
					if (result.getLine()>0) {
						
						sb.append(String.format(" (line %d)", result.getLine()));
						
					}
					
					sb.append(String.format(" - %s\n", result.getMessage()));
				}

				response = Response.status(Status.BAD_REQUEST).entity(sb.toString()).build();
	
			}
			
		} catch (Exception ex) {

			ex.printStackTrace();
			
			String msg = ex.getMessage();
			
			log.error(msg, ex);

			if (ex instanceof JsonSyntaxException) {
				
				response = Response.status(Status.BAD_REQUEST).entity(msg).build();
				
			} else if (ex instanceof InvalidParameterException) {
				
				response = Response.status(Status.BAD_REQUEST).entity(String.format("Invalid form parameter - %s",  ex.getMessage())).build();
			
			}  else {
				
				response = Response.status(Status.INTERNAL_SERVER_ERROR).entity(msg).build();
				
			}

			return response;
			
		} finally {
			
			if (null != ramlFileStream) {
				
				try {
					
					ramlFileStream.close();
					
				} catch (IOException e) {
					
					e.printStackTrace();
					
				}
				
			}
			
			if (null != uploadedFile) {
				
				if (uploadedFile.isDirectory()) {
					
					try {
						
						System.gc(); // To help release files that snakeyaml abandoned open streams on -- otherwise, some files may not delete
						
						// Wait a bit for the system to close abandoned streams
						try {

							Thread.sleep(1000);
							
						} catch (InterruptedException e) {
							
							e.printStackTrace();
							
						}
						
						FileUtils.deleteDirectory(uploadedFile);
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					uploadedFile.delete();
				}
			
			}
		} 
		
		return response;
    }

}
