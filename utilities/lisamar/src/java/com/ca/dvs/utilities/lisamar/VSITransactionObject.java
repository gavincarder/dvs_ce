/*******************************************************************************
 * Copyright (c) 2015 CA.  All rights reserved.
 *
 * This source file is licensed under the terms of the Eclipse Public License 1.0
 * For the full text of the EPL please see https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ca.dvs.utilities.lisamar;

public class VSITransactionObject {
	
	int id;					    	// transaction ID (should be unique)
	String operation = "";			// Operation such as GET, POST, PUT, PORTCH, DELETE
	String baseURL = "";			// Base URL
	String path = "";				// resource path
	String description = "";		// the transaction's description 
	String dataObjects = "";		// data objects(entities) for query
	String primaryKeys = "";		// primary keys for query
	String relationShips = "";		// Relationships between objects;
	String selectProperties = "";	// select properties in query;
	String responseBody = "";		// Response body;
	String matchScript = "";		// Match Script;
	String responseCode = "200";	// Response code, the default is 200 
	String responseCodeText = "OK";	// Response code text, the default is OK
	String responseFormat = "json";	// the format of Response, the default is application/json
	boolean enableJson = true;		// enable Json response
	boolean enableVerbosejson = false;	// enable verbosejson response
	boolean enableXML = false;		// enable XML response
	
	boolean isDefault = false;		// true --- default transaction, false - user defined
	
	public static final String[] DEFAULT_OPTIONS = { "GET", "POST", "PUT", "PATCH", "DELETE"};

	public VSITransactionObject(int id) {
		this.id = id;
	}
	
	public static VSITransactionObject create(int id) {
		return new VSITransactionObject(id);
	}
	
	public static VSITransactionObject clone(VSITransactionObject obj) {		
		VSITransactionObject newobj = new VSITransactionObject(obj.getID());
		newobj.setOperation(obj.getOperation());
		newobj.setBaseURL(obj.getBaseURL());
		newobj.setPath(obj.getPath());
		newobj.setDescription(obj.getDescription());
		newobj.setSelectProperties(obj.getSelectProperties());
		newobj.setResponseBody(obj.getResponseBody());
		newobj.setResponseFormat(obj.getResponseFormat());
		newobj.setEnableJson(obj.isEnableJson());
		newobj.setEnableVerbosejson(obj.isEnableVerbosejson());
		newobj.setEnableXML(obj.isEnableXML());
		
		return newobj;
	}
	
	public int getID() {
		return id;
	}

	public void setID( int id) {
		this.id = id;
	}
	
	public void setOperation(String operation) {
		this.operation = operation;
	}

	public String getOperation() {
		return operation;
	}

	public void setDescription(String str) {
		this.description = str;
	}

	public String getDescription() {
		return description;
	}
	
	public void setPath(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	public String getSelectProperties() {
		return selectProperties;
	}

	public void setSelectProperties(String properties) {
		this.selectProperties = properties;
	}

	public String getResponseBody() {
		return responseBody;
	}

	public void setResponseBody(String responseBody) {
		this.responseBody = responseBody;
	}

	public void setBaseURL(String baseURL) {
		this.baseURL = baseURL;
	}

	public String getBaseURL() {
		return baseURL;
	}
	
	public void setAsDefault(boolean isDefault){
		this.isDefault = isDefault;
	}

	public boolean getAsDefault(){
		return this.isDefault;
	}
	
	
	public void setMatchScript(String matchScript) {
		this.matchScript = matchScript;
	}
	
	public String getMatchScript(){
		return this.matchScript;
	}

	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}
	
	public String getResponseCode(){
		return this.responseCode;
	}

	public void setResponseCodeText(String responseCodeText) {
		this.responseCodeText = responseCodeText;
	}
	
	public String getResponseCodeText(){
		return this.responseCodeText;
	}	

	public void setResponseFormat(String responseFormat) {
		this.responseFormat = responseFormat;
	}
	
	public String getResponseFormat(){
		return this.responseFormat;
	}	
	
	public void setEnableJson(boolean enableJson) {
		this.enableJson = enableJson;
	}
	
	public boolean isEnableJson(){
		return this.enableJson;
	}	

	public void setEnableVerbosejson(boolean enableVerbosejson) {
		this.enableVerbosejson = enableVerbosejson;
	}
	
	public boolean isEnableVerbosejson(){
		return this.enableVerbosejson;
	}	

	public void setEnableXML(boolean enableXML) {
		this.enableXML = enableXML;
	}
	
	public boolean isEnableXML(){
		return this.enableXML;
	}	

	
}