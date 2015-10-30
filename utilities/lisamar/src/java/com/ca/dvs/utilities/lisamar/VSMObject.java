/*******************************************************************************
 * Copyright (c) 2015 CA.  All rights reserved.
 *
 * This source file is licensed under the terms of the Eclipse Public License 1.0
 * For the full text of the EPL please see https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ca.dvs.utilities.lisamar;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;

public class VSMObject {
	
	String vsmName = "";		// Virtual Service Model Name ( use as file name too)
	String httpPort = "";		// Http port#
	String httpBaseURL = "";	// Http Base URL
	String serviceName = "";	// Virtual Service Name	
	String vsmPath = "";		// VSM path to save
	String vsiFile = "";		// VS Image
	
	String dsURL = "";			// dvs Odata: Data Store URL with full path
	String dsType = "";			// dvs Odata: Data store type (XLS, Accress or H2)
	String dbUser = "";			// dvs Odata: name of DB user
	String dbPassword = null;	// dvs Odata: password of DB User
	String edmFile = "";		// dvs Odata: Entity Data Model file 
	String odataVersion = "";	// dvs Odata: Odata version 
	private ArrayList<VSITransactionObject> transcations = new ArrayList<VSITransactionObject>();	//dvs Odata: transcation list
	
	Document vsiDocument = null;							// Rest service: VSI document 
	List<String> operationsList = new ArrayList<String>();	// Rest service: operation list 
	
	public VSMObject(String vsmName) {
		this.serviceName = vsmName;
		this.vsmName = 	vsmName.replace(" ", "_");
	}
	
	public static VSMObject create(String vsmName) {
		return new VSMObject(vsmName);
	}
	
	public String getServiceName() {
		return serviceName;
	}
	
	public void setServiceName(String serviceName ) {
		this.serviceName = serviceName;
	}
	
	public String getVSMName() {
		return vsmName;
	}
	
	public void setVSMName(String vsmName) {
		this.vsmName = vsmName;
	}

	public String getOdataVersion() {
		return odataVersion;
	}
	
	public void setOdataVersion(String odataVersion) {
		this.odataVersion = odataVersion.trim();
	}

	public String getHttpPort() {
		return httpPort;
	}
	
	public void setHttpPort(String httpPort) {
		this.httpPort = httpPort;
	}

	public String getHttpBaseURL() {
		return httpBaseURL;
	}
	
	public void setHttpBaseURL(String httpBaseURL) {
		this.httpBaseURL = httpBaseURL;
	}

	public String getDataStoreURL() {
		return dsURL;
	}
	
	public void setDataStoreURL(String url) {
		this.dsURL = url;
	}

	public String getDataStoreType() {
		return dsType;
	}
	
	public void setDataStoreType(String dsType) {
		this.dsType = dsType;
	}

	public String getDatabaseUser() {
		return dbUser;
	}
	
	public void setDatabaseUser(String user) {
		this.dbUser = user;
	}

	public String getDatabasePassword() {
		return dbPassword;
	}
	
	public void setDatabasePassword(String pwd) {
		this.dbPassword = pwd;
	}

	public String getVSMPath() {
		return vsmPath;
	}
	
	public void setVSMPath(String vsmPath) {
		this.vsmPath = vsmPath;
	}

	public String getVSIFile() {
		return vsiFile;
	}
	
	public void setVSIFile(String vsiFile) {
		this.vsiFile = vsiFile;
	}
	
	public String getEDMFile() {
		return edmFile;
	}
	
	public void setEDMFile(String edmFile) {
		this.edmFile = edmFile;
	}
	
	public void addTranscation(VSITransactionObject object){
		transcations.add(object);
	}
	
//	public void addTranscations(VSITransactionObject[] objects){
//		transcations.addAll(Arrays.asList(objects));
//	}

	public ArrayList<VSITransactionObject> getTranscations() {
		return transcations;
	}

	public void addTranscations(ArrayList<VSITransactionObject> transactions) {
		this.transcations = transactions;
	}

	public List<String> getOperations() {
		return operationsList;
	}

	public void setOperations(List<String> operationsList) {
		this.operationsList = operationsList;
	}
	
	public Document getVsiDocment() {
		return vsiDocument;
	}
	
	public void setVsiDocment(Document vsiDocument) {
		this.vsiDocument = vsiDocument;
	}
}