/*******************************************************************************
 * Copyright (c) 2015 CA.  All rights reserved.
 *
 * This source file is licensed under the terms of the Eclipse Public License 1.0
 * For the full text of the EPL please see https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ca.dvs.app.dvs_servlet.misc;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.internal.util.Base64;
import org.raml.model.Raml;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ca.casd.utilities.commonUtils.CommonDefs;
import com.ca.casd.utilities.commonUtils.metadata.Metadata;
import com.ca.dvs.utilities.lisamar.LisaMarObject;
import com.ca.dvs.utilities.lisamar.LisaMarUtil;
import com.ca.dvs.utilities.raml.EDM;
import com.ca.dvs.utilities.raml.RamlUtil;
import com.ca.dvs.utilities.raml.VSI;
import com.ca.dvs.utilities.raml.WADL;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 * Build a CA LISA/DevTest virtual service from a RAML file
 * <p>
 * @author CA Technologies
 * @version 1
 */
public class VirtualServiceBuilder {
	
	VSEController vseCtrlr;
	LisaMarObject marObject;
	
	String strVsName = null;
	File fileMar = null;
	Response response = null;
	
	String vseServerUrl					= "http://localhost:1505/api/Dcm/VSEs/VSE";
	String vseServicePortRange			= "46001-46999";
	int    vseServiceReadyWaitSeconds	= 15;
	boolean generateServiceDocument		= false;
	String	authorization				= null;
	
	public VirtualServiceBuilder(String vseServerUrl, String vseServicePortRange, int vseServiceReadyWaitSeconds, boolean generateServiceDocument, String authorization) throws Exception{
		
		if (null != vseServerUrl && !vseServerUrl.isEmpty()) {
			setVseServerUrl(vseServerUrl);
		}

		setAuthorization(authorization);

		this.vseCtrlr = new VSEController(getVseServerUrl(), getAuthorization());
		
		if (vseServiceReadyWaitSeconds>0) {
			setVseServiceReadyWaitSeconds(vseServiceReadyWaitSeconds);
		}
		
		if (null != vseServicePortRange && !vseServicePortRange.isEmpty()) {
			setVseServicePortRange(vseServicePortRange);
		}
		
		setGenerateServiceDocument(generateServiceDocument);
		
	}
	
	/**
	 * Get VSE controller, this object can retrieve VS name, port, base path, uri...etc
	 * @return
	 */
	public VSEController getVseController(){
		return this.vseCtrlr;
	}
	
	/**
	 * Get File object of MAR file
	 * @return
	 */
	public File getMarFile(){
		return this.fileMar;
	}
	
	/**
	 * Get response of the instance
	 * @return
	 */
	public Response getResponse(){
		return this.response;
	}

	/**
	 * @return the vseServerUrl
	 */
	public String getVseServerUrl() {
		return vseServerUrl;
	}

	/**
	 * @param vseServerUrl the vseServerUrl to set
	 */
	public void setVseServerUrl(String vseServerUrl) {
		this.vseServerUrl = vseServerUrl;
	}

	/**
	 * @return the vseServicePortRange
	 */
	public String getVseServicePortRange() {
		return vseServicePortRange;
	}

	/**
	 * @param vseServicePortRange the vseServicePortRange to set
	 */
	public void setVseServicePortRange(String vseServicePortRange) {
		this.vseServicePortRange = vseServicePortRange;
	}

	/**
	 * @return the vseServiceReadyWaitSeconds
	 */
	public int getVseServiceReadyWaitSeconds() {
		return vseServiceReadyWaitSeconds;
	}

	/**
	 * @param vseServiceReadyWaitSeconds the vseServiceReadyWaitSeconds to set
	 */
	public void setVseServiceReadyWaitSeconds(int vseServiceReadyWaitSeconds) {
		this.vseServiceReadyWaitSeconds = vseServiceReadyWaitSeconds;
	}
	
	/**
	 * @return the generateServiceDocument
	 */
	public boolean isGenerateServiceDocument() {
		return generateServiceDocument;
	}

	/**
	 * @param generateServiceDocument the generateServiceDocument to set
	 */
	public void setGenerateServiceDocument(boolean generateServiceDocument) {
		this.generateServiceDocument = generateServiceDocument;
	}

	/**
	 * @return the authorization
	 */
	public String getAuthorization() {
		return authorization;
	}

	/**
	 * @param authorization the authorization to set
	 */
	public void setAuthorization(String authorization) {
		this.authorization = authorization;
	}

	/**
	 * Set to process RAML file
	 * @param raml
	 * @param ramlParentFile
	 * @return
	 * @throws Exception 
	 */
	public Response setInputFile(Raml raml, File ramlParentFile, boolean isRest) throws Exception{
		strVsName = null;
		fileMar = null;
		response = null;
		if (isRest) 
			createRestVsMar(raml, ramlParentFile);
		else
			createVsMar(raml, ramlParentFile);
		deployVsMar(this.fileMar, this.strVsName);
		cleanUp();
		return this.response;
	}

	/**
	 * Set to process WADL file
	 * @param wadl
	 * @return
	 */
	public Response setInputFile(WADL wadl){
		strVsName = null;
		fileMar = null;
		response = null;
				
		//createVsMar(wadl);
		//deployVsMar(this.fileMar, this.strVsName);
		//cleanUp();
		return this.response;
	}
	
	/**
	 * Set to process metadata
	 * @param metadata
	 * @return
	 */
	public Response setInputFile(Metadata metadata){
		strVsName = null;
		fileMar = null;
		response = null;
				
		//createVsMar(metadata);
		//deployVsMar(this.fileMar, this.strVsName);
		//cleanUp();
		return this.response;
	}
	
	/**
	 * Generate mar file from RAML for Odata service
	 * @param raml
	 * @param ramlParentFile
	 * @throws Exception 
	 */
	private void createVsMar(Raml raml, File ramlParentFile) throws Exception {
		
	try{	

		// Generate the AEDM from the RAML
		Document docAEDM = null;		
		EDM edm = new EDM(raml);
		docAEDM = edm.getDocument();
		String odataVersion = CommonDefs.VALUE_ODATA_VERSION_4; 
		
		Map<String, Map<String, Object>> sampleData = RamlUtil.getSampleData(raml, ramlParentFile);
		
		// Get base URL and port from RAML
		String strVsBaseURL = raml.getBasePath();
		
		// Get seed port from RAML
		URI uri = new URI(raml.getBaseUri());
		int intRamlPort = uri.getPort();
		
        //Get current Virtual Service list from LISA
        HashMap<String, VSE> vseList = this.vseCtrlr.getVseList();
		
		// get Alias name from AEDM
		String strAlias = getDocumentAliasNameParser(docAEDM);
	    
		// set a valid service name
		this.strVsName = vseCtrlr.getNewServiceName(strAlias, vseList);
		
		// Get a valid free port
		int intVsPort = vseCtrlr.getAvailablePort(intRamlPort,getVseServicePortRange(),vseList);
		
		// Generate MAR			 
		//LisaMarObject marObject = LisaMarUtil.generateLiasMar(docAEDM, strVsName, strVsBaseURL, String.valueOf(intVsPort));		
		this.marObject = LisaMarUtil.generateLisaMar(docAEDM, strVsName, strVsBaseURL, String.valueOf(intVsPort), odataVersion, sampleData);		
		String strPathMar = marObject.getLisafile();
		
		// set fileMar
		this.fileMar = new File(strPathMar);
						
		}catch (Exception e) {
			String msg = String.format("Failed to create MAR file - %s", e.getMessage());			
			throw new Exception(msg, e.getCause());
		}		
	}

	/**
	 * Generate mar file from RAML for Rest service
	 * @param raml
	 * @param ramlParentFile
	 * @throws Exception 
	 */
	private void createRestVsMar(Raml raml, File ramlParentFile) throws Exception {
		
	try{	

		// Get base URL and port from RAML
		String strVsBaseURL = raml.getBasePath();
		
		// Get seed port from RAML
		URI uri = new URI(raml.getBaseUri());
		int intRamlPort = uri.getPort();
		
        //Get current Virtual Service list from LISA
        HashMap<String, VSE> vseList = this.vseCtrlr.getVseList();
		
		// get service name from raml
		String strAlias = raml.getTitle().replaceAll("\\s", ""); 
	    
		// set a valid service name
		this.strVsName = vseCtrlr.getNewServiceName(strAlias, vseList);
		
		// Get a valid free port
		int intVsPort = vseCtrlr.getAvailablePort(intRamlPort,getVseServicePortRange(),vseList);
				
		// Generate a VSI from raml
		VSI vsi = new VSI(raml, ramlParentFile, isGenerateServiceDocument());
		Document vsidoc = vsi.getDocument();

		// Generate MAR			 
		this.marObject = LisaMarUtil.generateRestLisaMar(strVsName, strVsBaseURL, String.valueOf(intVsPort), vsidoc, vsi.getOperationsList(raml.getResources()));		
		String strPathMar = marObject.getLisafile();
		
		// set fileMar
		this.fileMar = new File(strPathMar);
						
		}catch (Exception e) {
			String msg = String.format("Failed to create MAR file - %s", e.getMessage());			
			throw new Exception(msg, e.getCause());
		}		
	}	
	/**
	 * Deploy MAR to VSE and generate response to the newly VS URI
	 * @param fileMar
	 * @param strVsName
	 * @throws Exception 
	 */
	private void deployVsMar(File fileMar, String strVsName) throws Exception{
		
	try{	

		// Deploy MAR
		this.vseCtrlr.deployMar(fileMar);
	}catch (Exception e) {
		String msg = String.format("Failed to deploy a virtual service from MAR file - %s", e.getMessage());			
		throw new Exception(msg, e.getCause());
	}
	
	try{
		// Validate newly deployed service	
		// success - form response body containing URL to new service (LISA Host, base URL, port)
		// fail - form response body containing error message
		Thread.sleep(getVseServiceReadyWaitSeconds()* 1000);  // wait to check status of the VS
		VirtualService vs = vseCtrlr.getVirtualService(strVsName);
		if (vs.getStatus() == 2){
			
			// form actions URLs hash map
			Map<String, Object> mapActions = formActionURIs(vs.getServiceCtrlUri());
			
			// form messages hash map
			Map<String, Object> mapMsg = new LinkedHashMap<String, Object>();
			Map<String, Object> mapMsgWarn = new LinkedHashMap<String, Object>();
			Map<String, Object> mapMsgErr = new LinkedHashMap<String, Object>();
			Map<String, Object> mapSvcWarnCreateMar = this.marObject.getWarnings();
			Map<String, Object> mapSvcErrCreateMar = this.marObject.getErrors();

			if (mapSvcWarnCreateMar.size() > 0)
				mapMsgWarn.put("createMarFile", mapSvcWarnCreateMar); 
			if (mapSvcErrCreateMar.size() > 0)
				mapMsgErr.put("createMarFile", mapSvcErrCreateMar);
			
			mapMsg.put("serviceWarnings", mapMsgWarn);
			mapMsg.put("serviceErrors", mapMsgErr);
			
			// put into map for response
			Map<String, Object> map = new LinkedHashMap<String, Object>();
			map.put("serviceName", vs.getName());
			map.put("serviceUrl",  vs.getServiceUri().toString());
			map.put("actions", mapActions);
			map.put("messages", mapMsg);
			
			GsonBuilder gsonBuilder = new GsonBuilder();
			gsonBuilder.setPrettyPrinting();
			gsonBuilder.serializeNulls();
			Gson gson = gsonBuilder.create();

			this.response = Response.status(Status.OK).entity(gson.toJson(map)).build();

		}else {
			this.response = Response.status(Status.INTERNAL_SERVER_ERROR).entity(String.format("Service name '%s' is deployed but failed to start", strVsName)).build();
		}					
						
		}catch (Exception e) {
			String msg = String.format("Service name '%s' is deployed but failed to start - %s", strVsName, e.getMessage());			
			throw new Exception(msg, e.getCause());
		}		
	}

	/**
	 * Method to clean up afterward
	 * @throws Exception 
	 */
	public void cleanUp() throws Exception{
		// Delete Mar file
		try {
			Files.deleteIfExists(this.fileMar.toPath());
		} catch (IOException e) {
			String msg = String.format("Failed to delete MAR file: %s - %s", this.fileMar.getPath(), e.getMessage());			
			throw new Exception(msg, e.getCause());
		}
	}
	
	/**
	 * Get schema alias name. However if alias name is not presented, use last segment of namespace 
	 * @param doc
	 * @return
	 */
	private String getDocumentAliasNameParser(Document doc){
		String strAlias = null;
		
		doc.getDocumentElement().normalize();
		NodeList nList = doc.getElementsByTagName("Schema");
		Node nNode = nList.item(0);
		if (nNode.getNodeType() == Node.ELEMENT_NODE){
			strAlias = ((Element)nNode).getAttribute("Alias");
		}
		
		if (strAlias == null || strAlias.isEmpty()){
			if (nNode.getNodeType() == Node.ELEMENT_NODE){
				String strNameSpace = ((Element)nNode).getAttribute("Namespace");
				if (strNameSpace != null && !strNameSpace.isEmpty())
					strAlias = strNameSpace.substring(strNameSpace.lastIndexOf("\\.") + 1);
			}
		}		
		return strAlias;
	}
	
	public Map<String, Object> formActionURIs(URI svcCtrlUri) throws Exception{
		BufferedReader in = null;
		try {
			String strActions = "actions";
			URL url = new URL( svcCtrlUri + "/" + strActions);
			
		    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		    conn.setRequestMethod("GET");
		    conn.setRequestProperty("Content-Type", "application/vnd.ca.lisaInvoke.vseList+xml");
			    
		    if (getAuthorization()!=null) {
				String encodedPswd = Base64.encodeAsString(getAuthorization());
			    conn.setRequestProperty("Authorization", "Basic " + encodedPswd);
		    }

		    // Get the response from VSE of specific VM actions
		    int responseCode = conn.getResponseCode();
		    
		    if (responseCode >= 200 && responseCode <= 300) {
		    	in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

		    	String inputLine;
				StringBuffer response = new StringBuffer();
		 
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				//in.close();
				
				//convert the response to xml doc
				Document doc = vseCtrlr.readXmlString(response.toString());

		    	//get the list of actions nodelist
			    NodeList virtualSvcNList = doc.getElementsByTagName("Link");
			    Map<String, Object> mapActionsUri = new LinkedHashMap<String, Object>();
			    for (int i=0; i < virtualSvcNList.getLength(); i++) { 			        	
			    	Node node = virtualSvcNList.item(i);
			    	String strRel = ((Element) node).getAttribute("rel");
			    	String strHref = ((Element) node).getAttribute("href");
			    	
			    	if (strRel != null && !strRel.isEmpty()
			    			&& strHref != null && !strHref.isEmpty())
			    		mapActionsUri.put(strRel, strHref);    	
			    }
			    
			    // last, add control URI to the map
			    mapActionsUri.put("control", svcCtrlUri.toString());
			    
			    return mapActionsUri;

		    }else {

		    	StringBuffer sbError = new StringBuffer();
		    	
		    	sbError.append(String.format("Error getting list action from virtual service at uri: %s - ", svcCtrlUri));

		    	try {
		    		sbError.append(VSEController.getLisaErrorResponse(conn.getErrorStream()));
		    	} catch(Exception ex) {
		    		sbError.append(String.format("response code: %s", Integer.toString(responseCode)));
				}

				throw new Exception(sbError.toString(), null);
		    	
			}	

		} catch(Exception e) {
			throw e;
		}
		finally{
			if (in != null)
				in.close();
		}
	}

}
