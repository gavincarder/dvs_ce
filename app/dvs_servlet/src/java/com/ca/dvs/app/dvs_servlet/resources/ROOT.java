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

import java.io.StringWriter;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * DVS Servlet root REST resource
 * <p>
 * @author CA Technologies
 * @version 1
 */
@Path("/")
public class ROOT {
	
	private static Logger log = Logger.getLogger(ROOT.class);

	/**
	 * Produce a service document for the DVS servlet
	 * <p>
	 * @return a service document
	 */
	@GET
	@Path("")
	@Produces(MediaType.TEXT_HTML)
	public Response getRoot() {
		
		Response response = null;
		
		log.info("GET /");
		
        DocumentBuilderFactory dbf      = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
		try {
			builder = dbf.newDocumentBuilder();
	    
			Document document = builder.newDocument();
	        Element elHtml = document.createElement("html");
	        
	        Element elHead = document.createElement("head");
	        Element elH1   = document.createElement("h1");

	        elH1.setTextContent("DVS Servlet");
	        elHead.appendChild(elH1);
	        
	        Element elTitle = document.createElement("title");
	        
	        elTitle.setTextContent("Dynamic Virtual Services");
	        
	        Element elBody  = document.createElement("body");
	        Element elP1    = document.createElement("p");
	        elP1.setTextContent("Documentation text goes here");
	        elBody.appendChild(elP1);

	        elHtml.appendChild(elHead);
	        elHtml.appendChild(elTitle);
	        elHtml.appendChild(elBody);
	        
	        
	        document.appendChild(elHtml);
	        
	        StringWriter sw = new StringWriter();
			prettyPrint(document, sw);

	        response = Response.status(200).entity(sw.toString()).build();

		} catch (Exception e) {

			e.printStackTrace();
			response = Response.status(500).entity(e.getMessage()).build();

		}
        
		return response;
	}

	/**
	 * @return HTTP response containing DVS servlet configuration parameters in JSON format
	 */
	@GET
	@Path("config")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getConfig() {
		
		log.info("GET /config");
		
		Map<String, Object> configMap = new LinkedHashMap<String, Object>();
		
		try {
			Context initialContext = new InitialContext();
			Context envContext     = (Context) initialContext.lookup("java:comp/env");
			
			configMap.put("vseServerUrl",				envContext.lookup("vseServerUrl"));
			configMap.put("vseServicePortRange",		envContext.lookup("vseServicePortRange"));
			configMap.put("vseServiceReadyWaitSeconds",	envContext.lookup("vseServiceReadyWaitSeconds"));
			
		} catch (NamingException e) {
			e.printStackTrace();
		}

		GsonBuilder gsonBuilder = new GsonBuilder();
		
		gsonBuilder.setPrettyPrinting();
		gsonBuilder.serializeNulls();
		
		Gson gson = gsonBuilder.create();

		Response response = Response.status(200).entity(gson.toJson(configMap)).build();

		return response;
	}

	/**
	 * Format a DOM Document so that it is visually appealing
	 *  
	 * @param xml Document to transform
	 * @param writer used for transformation output
	 * @throws Exception when transformation fails
	 */
	public static final void prettyPrint(Document xml, Writer writer) throws Exception {
		
        TransformerFactory tf = TransformerFactory.newInstance();
        
        //tf.setFeature(XMLConstants.ACCESS_EXTERNAL_DTD, false); <-- Fortify recommended.  Eclipse compiled ok.  ANT failed.
        tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true); // Per web article, this is a suitable replacement for setting ACCESS_EXTERNAL_DTD, false (above)
        
        Transformer trans = tf.newTransformer();
        
        trans.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        trans.setOutputProperty(OutputKeys.INDENT, "yes");
        trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        
        trans.transform(new DOMSource(xml), new StreamResult(writer));
        
    }

}

