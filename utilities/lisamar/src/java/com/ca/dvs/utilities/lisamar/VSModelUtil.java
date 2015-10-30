/*******************************************************************************
 * Copyright (c) 2015 CA.  All rights reserved.
 *
 * This source file is licensed under the terms of the Eclipse Public License 1.0
 * For the full text of the EPL please see https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ca.dvs.utilities.lisamar;


import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * VSModelUtil Class
 * <p>
 * Generates VS model for OData or Rest service 
 *  
 * @author gonbo01
 *
 */
public class VSModelUtil {

	private static String LOAD_DVS_DATASTORE_STEP = "com.ca.casd.lisa.extensions.odata.devteststeps.LoadDVSDataStoreStep";
	private static String HTTP_LISTEN_STEP = "com.itko.lisa.vse.stateful.protocol.http.HttpListenStep";
	private static String CONVERSATIONAL_STEP = "com.itko.lisa.vse.stateful.ConversationalStep";
	private static String DATA_PROTOCOL_FILTER = "com.itko.lisa.vse.stateful.common.DataProtocolFilter";
	
	private static final VSModelUtil instance = new VSModelUtil();
	
    public static VSModelUtil getInstance() {
		return instance;
    }
    
    /**
     * Gets the information of VS object
     * @param doc	- the document of VS model
     * @return VSMObject - the information of VS object
     * @see VSMObject
     * @throws Exception
     */
	public VSMObject loadVSMInformation(Document doc) throws Exception{
    	
		// get VSModel Name
		Node vsmNode = XMLDomUtil.findChildNodesByName(doc, "VSModel");	
		if (vsmNode != null) {
			String vsmName = vsmNode.getAttributes().getNamedItem("name").getNodeValue();
			VSMObject vsmOject = new VSMObject(vsmName);
			
			// get DataStore information
			Node dsStepNode = XMLDomUtil.findChildNodeByType(vsmNode, LOAD_DVS_DATASTORE_STEP);
			if (dsStepNode != null) {
				String dsFile = XMLDomUtil.getChildNodeValueByName(dsStepNode, "filename");
				String dsType = XMLDomUtil.getChildNodeValueByName(dsStepNode, "dataStoreType");
				String dbUser = XMLDomUtil.getChildNodeValueByName(dsStepNode, "dbUserName");
				String dbPassword = XMLDomUtil.getChildNodeValueByName(dsStepNode, "dbPassword");
				vsmOject.setDataStoreURL(dsFile);
				vsmOject.setDataStoreType(dsType);
				vsmOject.setDatabaseUser(dbUser);
				vsmOject.setDatabasePassword(dbPassword);				
			}
			
			// get Http information
			Node httpStepNode = XMLDomUtil.findChildNodeByType(vsmNode, HTTP_LISTEN_STEP);	
			if (httpStepNode != null) {
				String httpBaseURL = XMLDomUtil.getChildNodeValueByName(httpStepNode, "basePath");
				String httpPort = XMLDomUtil.getChildNodeValueByName(httpStepNode, "listenPort");
				vsmOject.setHttpBaseURL(httpBaseURL);
				vsmOject.setHttpPort(httpPort);
			}
			//Get VSImage information
			Node vsiStepNode = XMLDomUtil.findChildNodeByType(vsmNode, CONVERSATIONAL_STEP);	
			if (vsiStepNode != null) {
				String vsiFile = XMLDomUtil.getChildNodeValueByName(vsiStepNode, "vsiSource");
				vsmOject.setVSIFile(vsiFile);
			}
			
			return vsmOject;
		}    	
    	
    	return null;
    	
    }

	/**
	 * Set the information of VS object to the VS model for OData Service
	 * 
	 * @param vsmDoc	- the document of VS model
	 * @param VSMObject	- the information of VS object
	 * @return true if successful
	 * @see VSMObject
	 * @throws Exception
	 */
	public boolean updateVSMDocument(Document vsmDoc, VSMObject vsmObject) throws Exception{

		boolean bUpdated = false;
		Node vsmNode = XMLDomUtil.findChildNodesByName(vsmDoc, "VSModel");	
		if (vsmNode != null) {			
			
			// set VSM name
			vsmNode.getAttributes().getNamedItem("name").setNodeValue(vsmObject.getServiceName());
			
			// set DataStore information
			Node dsStepNode = XMLDomUtil.findChildNodeByType(vsmNode, LOAD_DVS_DATASTORE_STEP);
			if (dsStepNode != null) {
				XMLDomUtil.updateChildNodeValueByName(vsmDoc, dsStepNode, "filename", vsmObject.getDataStoreURL());
				XMLDomUtil.updateChildNodeValueByName(vsmDoc, dsStepNode, "dataStoreType", vsmObject.getDataStoreType());
				XMLDomUtil.updateChildNodeValueByName(vsmDoc, dsStepNode, "dbUserName", vsmObject.getDatabaseUser());
				XMLDomUtil.updateChildNodeValueByName(vsmDoc, dsStepNode, "dbPassword", vsmObject.getDatabasePassword());
			}
			
			// set Http information
			Node httpStepNode = XMLDomUtil.findChildNodeByType(vsmNode, HTTP_LISTEN_STEP);	
			if (httpStepNode != null) {
				XMLDomUtil.updateChildNodeValueByName(vsmDoc, httpStepNode, "basePath", vsmObject.getHttpBaseURL());
				XMLDomUtil.updateChildNodeValueByName(vsmDoc, httpStepNode, "listenPort", vsmObject.getHttpPort());
			}
			// set VSImage information
			Node vsiStepNode = XMLDomUtil.findChildNodeByType(vsmNode, CONVERSATIONAL_STEP);	
			if (vsiStepNode != null) {				
				XMLDomUtil.updateChildNodeValueByName(vsmDoc, vsiStepNode, "vsiSource", vsmObject.getVSIFile());
			}
			
			bUpdated = true;
		}

		return bUpdated;
	}
	
	/**
	 * Set the information of VS object to the VS model for REST Service
	 * 
	 * @param vsmDoc	- the document of VS model
	 * @param VSMObject	- the information of VS object
	 * @return true if successful
	 * @see VSMObject
	 * @throws Exception
	 */
	public boolean updateRestVSMDocument(Document vsmDoc, VSMObject vsmObject) throws Exception{

		boolean bUpdated = false;
		Node vsmNode = XMLDomUtil.findChildNodesByName(vsmDoc, "VSModel");	
		if (vsmNode != null) {			
			
			// set VSM name
			vsmNode.getAttributes().getNamedItem("name").setNodeValue(vsmObject.getServiceName());
						
			// set Http information
			Node httpStepNode = XMLDomUtil.findChildNodeByType(vsmNode, HTTP_LISTEN_STEP);	
			if (httpStepNode != null) {
				XMLDomUtil.updateChildNodeValueByName(vsmDoc, httpStepNode, "basePath", vsmObject.getHttpBaseURL());
				XMLDomUtil.updateChildNodeValueByName(vsmDoc, httpStepNode, "listenPort", vsmObject.getHttpPort());
				
				List<String> operations = vsmObject.getOperations();
				if (operations != null && operations.size() > 0) {
					Node dataProtocolFilter = XMLDomUtil.findChildNodeByNameAndType(httpStepNode, "Filter", DATA_PROTOCOL_FILTER);	
					if (dataProtocolFilter != null) {
					// add rules in <CustomFilterData>\<rules>...<rules>
						Node customFilterDataNode = XMLDomUtil.findChildNodeByName(dataProtocolFilter, "CustomFilterData");
						if (customFilterDataNode != null) {
							Node rulesNode = XMLDomUtil.findChildNodeByName(customFilterDataNode, "rules");
							if (rulesNode != null) {
								for (String optStr: operations) {
									Element newOpt = vsmDoc.createElement("operation");
									if (!optStr.endsWith("/"))
										optStr += "/";			// Lisa requires operation end with /
									newOpt.appendChild(vsmDoc.createTextNode(optStr));
									Element newRule = vsmDoc.createElement("rule");
									newRule.appendChild(newOpt);
									rulesNode.appendChild(newRule);
								}
							}
						}
					}
				}
			}
			
			// set VSImage information
			Node vsiStepNode = XMLDomUtil.findChildNodeByType(vsmNode, CONVERSATIONAL_STEP);	
			if (vsiStepNode != null) {				
				XMLDomUtil.updateChildNodeValueByName(vsmDoc, vsiStepNode, "vsiSource", vsmObject.getVSIFile());
			}
			
			bUpdated = true;
		}

		return bUpdated;
	}	
	

}
