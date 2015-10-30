/*******************************************************************************
 * Copyright (c) 2015 CA.  All rights reserved.
 *
 * This source file is licensed under the terms of the Eclipse Public License 1.0
 * For the full text of the EPL please see https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ca.casd.lisa.plugins.odataassistant.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import com.ca.casd.utilities.commonUtils.CommonDefs;
import com.ca.casd.utilities.commonUtils.log.Log;
import com.ca.casd.utilities.commonUtils.metadata.EntitySet;
import com.ca.casd.utilities.commonUtils.metadata.EntityType;
import com.ca.casd.utilities.commonUtils.metadata.Metadata;
import com.ca.casd.utilities.commonUtils.metadata.NavigationProperty;
import com.ca.casd.utilities.commonUtils.util.DefaultTransactionUtil;
import com.ca.casd.utilities.commonUtils.util.EnumResourceObjectType;

import com.ca.dvs.utilities.lisamar.VSITransactionObject;


public class EnumDefaultTransactions {
	
	public static String 	URL_PATH_SEPERATOR = "/";
	public static String 	URL_PATH_LINKS = "$links";
	public static String 	URL_PATH_REF = "$ref";
	public static String 	URL_PATH_ID = "(<id>)";
	public static String 	URL_PATH_PROPERTY = "<property>";
	public static String 	URL_PATH_PROPERTY_VALUE = "$value";
	public static String 	URL_PATH_COUNT = "$count";
	
	private String   baseURL;
	private Metadata metadata;
	private String 	 odataVersion;
	private int		 maxResourcePathDepth = 0;		// no limit depth of the path
	private DefaultTransactionUtil transactionUtil = null; 
	
	private HashMap<String, ResourceObject> resourceObjects = new HashMap<String, ResourceObject>();
	private boolean isError = false;
	
	public EnumDefaultTransactions(String baseURL, int maxResourcePathDepth, Metadata metadata, String odataVersion) {
		if (false == baseURL.startsWith(URL_PATH_SEPERATOR))
			baseURL = URL_PATH_SEPERATOR + baseURL;
		
		this.baseURL = baseURL;
		this.metadata = metadata;
		this.maxResourcePathDepth = maxResourcePathDepth;
		this.odataVersion = odataVersion;
		
		this.transactionUtil = new DefaultTransactionUtil(this.metadata);
		
		setPopulateAvailableTransactionError(false);
		
	}
	
	public ArrayList<VSITransactionObject> populateAvailableTransaction() {
		
		setPopulateAvailableTransactionError(false);
		
		ArrayList<VSITransactionObject> transactionObjects = new ArrayList<VSITransactionObject>();
		
        for (int i = 0; i < VSITransactionObject.DEFAULT_OPTIONS.length; i++) {
        	
        	String method = VSITransactionObject.DEFAULT_OPTIONS[i];
        	if (method.equals("PATCH"))	// LISA doesn't support PATCH so far
        		continue;
        	
        	String[] resPathes = buildAvailableResourcePath(method);
        	if (resPathes == null)
        		continue;
      		
    		int newTransactionId = transactionObjects.size();
    		for (String path : resPathes) {
        		
        		VSITransactionObject tObject = new VSITransactionObject(newTransactionId);
        		String description = buildDefaultDescription(method, path);
        		String response = buildDefaultResponseBody(method, path, odataVersion);
        		
        		tObject.setBaseURL(baseURL);
        		tObject.setOperation(method);
           		tObject.setPath(path);
        		tObject.setDescription(description);
         		tObject.setResponseBody(response);
         		
     			tObject.setEnableXML(false);
     			
        		if (response.isEmpty()) {
         		// no necessary to set $format if there is no response required 
         			tObject.setEnableJson(false);
         			tObject.setEnableVerbosejson(false);
         		}
         		else if (path.endsWith(URL_PATH_PROPERTY) || isContainsLink(path)) {
         			tObject.setEnableJson(true);
         			tObject.setEnableVerbosejson(false);
          		}
         		else {
        			tObject.setEnableJson(true);
         			tObject.setEnableVerbosejson(true);
         		}
         		
         		transactionObjects.add(tObject);
         		
        		newTransactionId ++;
        	}
        }
  
        return transactionObjects;
        
	}
	
	public String[] buildAvailableResourcePath(String method) {
		
		resourceObjects.clear();
		  
		for (String eSetName : metadata.getEntitySets().keySet()) {
			// Resource path always start at a entity set 
			  String basePath = URL_PATH_SEPERATOR + eSetName; 
			  
			  EntitySet entitySet = metadata.getEntitySets().get(eSetName);
			  if (entitySet == null) {
				  Log.write().error("cannot find the entity set '" + eSetName + "'");
				  setPopulateAvailableTransactionError(true);
				  continue;
			  }
			  
			  String entityTypeName = entitySet.getEntityTypeName();			  
			  buildEntityResourcePath(basePath, entityTypeName, method);
		}
		  
		if (resourceObjects.size() == 0) 
			return null;
		
		// post and sort the available links 
		String[] items = new String[resourceObjects.size()];
		int index = 0;
		for (String name : resourceObjects.keySet()) {
			items[index] = name;
			index ++;
		}			
		Arrays.sort(items);				
		
		return items;
			
	}
	
	public String buildDefaultDescription(String strMethod, String resourcePath) {		
		return strMethod + " " + resourcePath;		
	}
	
	public String buildDefaultResponseBody(String strMethod, String resourcePath, String odataVersion){

		 String strResponse = "";
		 
		 if (resourceObjects.size() == 0){
			  return strResponse;
		 }
		  
		 String strHttp = "http://"; 
	   	 String strLisaHost = "{{LISA_HOST}}";
	   	 String strPortNumber = "{{lisa.vse.port.number}}";
			   
		 String key = resourcePath; 
		 ResourceObject resObject = resourceObjects.get(key);
		 strResponse = transactionUtil.buildResponseTemplate(
				strHttp, strLisaHost, strPortNumber, baseURL, strMethod, resourcePath, resObject.getObjectType(), resObject.getEntityType(), odataVersion);

		  return strResponse;
	}
	
	public void setPopulateAvailableTransactionError(boolean error) {
		this.isError = error;
	}
	
	public boolean populateAvailableTransactionError() {
		return this.isError;
	}
	
	private void buildEntityResourcePath (String basePath, String entityTypeName, String method) {	  
		  
		  EntityType eType = metadata.getEntityType(entityTypeName);

		  // CustomType: don't support DELETE
		  if (method.equals("DELETE") && eType.isCustomType())
			  return;		  

		  // only GET/POST support COLLECTION: EntitySet
		  if ( method.equals("GET")) { 
			  addResourceObject(method, basePath, entityTypeName, EnumResourceObjectType.COLLECTION);
			  buildCountResourcePath(basePath, entityTypeName, method);
		  }
		  else if ( method.equals("POST") ) { 
			  addResourceObject(method, basePath, entityTypeName, EnumResourceObjectType.COLLECTION);
		  }
		  
		  // only allow one layer link
		  if (isContainsLink(basePath))
			  return;
		  
		  // a element of entity set: EntitySet By ID
		  basePath = basePath + URL_PATH_ID;		  
		  if ( false == method.equals("POST") ) {			  
			  addResourceObject(method, basePath, eType.getName(), EnumResourceObjectType.ELEMENT );			  
			  buildPropertyResourcePath(basePath, eType.getName(), method);
		  }
		  
		  if (eType.getNavigationProperties().size() == 0)
			  return;
		  		  
	  	  //Navigation Properties
	  	  for (NavigationProperty nProp : eType.getNavigationProperties()) {
	  		  
	  		  String navPropertyName = nProp.getName();
	  		  if (navPropertyName == null || navPropertyName.isEmpty()) {
	  			  Log.write().error("The name of navigation property missed in the entity type '" + eType.getName() + "'");
				  setPopulateAvailableTransactionError(true);
	  			  continue;
	  		  }
	  		  
	  		  String navEntityType = nProp.getEntityTypeName();
	  		  if (navEntityType == null || navEntityType.isEmpty()) {
	  			  Log.write().error("the entity type is missed in the navigation property '" + navPropertyName + "'");
				  setPopulateAvailableTransactionError(true);
	  			  continue;
	  		  }
	  		  
	  		  String newPath = URL_PATH_SEPERATOR + navPropertyName;
	  		  if (basePath.contains(newPath))
	  			  continue;
	  		  
	  		  EntityType entityType = metadata.getEntityType(navEntityType);
	  		  if (entityType == null) {
	  			  Log.write().error("Cannot find the entity type '" + navEntityType + "' specified in the navigation property '" + navPropertyName + "'"  );
				  setPopulateAvailableTransactionError(true);
	  			  continue;    			  
	  		  }
	  		  
	  		  String nEntityType = entityType.getName();
	  		  
	  		  String resPath = "";
	  		  if (method.equals("GET") || method.equals("POST")) {
	  			  resPath = basePath + URL_PATH_SEPERATOR + navPropertyName;
	  			  
		  		  if (nProp.getMultiplicity().equals("*")) {
		  			  extentEntityResourcePath(resPath, nEntityType, method);
		  		  }
		  		  else {
		  			  addResourceObject(method, resPath, nEntityType, EnumResourceObjectType.ELEMENT );	
		  			  buildPropertyResourcePath(resPath, nEntityType, method);
		  		  }
	  		  }
	  		  
	  		  /* Add $links/$ref for each method:
	  		   * PUT doesn't support for Collection
	  		   * DELETE is only allowed for element
	  		   * GET/POST are allowed for NavProperty
	  		  */	  		  
  			  if (CommonDefs.isOdataVersion4(odataVersion))
	  			  resPath = basePath + URL_PATH_SEPERATOR + navPropertyName + URL_PATH_SEPERATOR + URL_PATH_REF;
	  		  else
	  			  resPath = basePath + URL_PATH_SEPERATOR + URL_PATH_LINKS + URL_PATH_SEPERATOR + navPropertyName;
  			  
      		  if (nProp.getMultiplicity().equals("*")) {
      			  if (method.equals("GET") || method.equals("POST")) {
    				  addResourceObject(method, resPath, nEntityType, EnumResourceObjectType.COLLECTION);
      			  }
      			  
      			  if (method.equals("DELETE") || method.equals("GET")) {
    	  			  if (CommonDefs.isOdataVersion4(odataVersion))
    		  			  resPath = basePath + URL_PATH_SEPERATOR + navPropertyName + URL_PATH_ID + URL_PATH_SEPERATOR + URL_PATH_REF;
    		  		  else
    		  			  resPath = basePath + URL_PATH_SEPERATOR + URL_PATH_LINKS + URL_PATH_SEPERATOR + navPropertyName + URL_PATH_ID;
	      			  addResourceObject(method, resPath, nEntityType, EnumResourceObjectType.ELEMENT );	  		  
      			  }
      		  }
      		  else {
      			  addResourceObject(method, resPath, nEntityType, EnumResourceObjectType.ELEMENT );	  		  
      		  }
	  		  
	  	  }
    				  
	}
	
	private boolean extentEntityResourcePath(String resPath, String entityTypeName, String method){
		String[] elements = resPath.split(URL_PATH_SEPERATOR);
		if (maxResourcePathDepth > 0 && elements.length > maxResourcePathDepth )
			return false;
		
		buildEntityResourcePath(resPath, entityTypeName, method);	
		return true;
	}

	private void buildCountResourcePath(String basePath, String entityTypeName, String method) {	  
		  
		  if (false == method.equals("GET"))
			  return;
		  
		  if (isContainsLink(basePath))
			  return;
		  
		  String resPath = basePath + URL_PATH_SEPERATOR + URL_PATH_COUNT;
		  addResourceObject(method, resPath, entityTypeName, EnumResourceObjectType.VALUE );		  
	}

	private void buildPropertyResourcePath(String basePath, String entityTypeName, String method) {	  
		  
		  if (false == method.equals("GET"))
			  return;
		  
		  if (isContainsLink(basePath))
			  return;
		  
		  String resPath = basePath + URL_PATH_SEPERATOR + URL_PATH_PROPERTY;
		  addResourceObject(method, resPath, entityTypeName, EnumResourceObjectType.EDMTYPE );
		  
		  buildPropertyValueResourcePath(resPath, entityTypeName, method);
		  
	}

	private void buildPropertyValueResourcePath(String basePath, String entityTypeName, String method) {	  
		  
		  if (false == method.equals("GET"))
			  return;
		  
		  if (isContainsLink(basePath))
			  return;
		  
		  String resPath = basePath + URL_PATH_SEPERATOR + URL_PATH_PROPERTY_VALUE;
		  addResourceObject(method, resPath, entityTypeName, EnumResourceObjectType.VALUE);
	}
	
	private void addResourceObject(String method, String resPath, String entityType, EnumResourceObjectType dataType){
		  ResourceObject resObject = new ResourceObject(resPath);
		  resObject.setEntityType(entityType);
		  resObject.setEntitySet( getEntitySetName(entityType) );
		  resObject.setObjectType(dataType);
		  String key = resPath; //method + "-" + resPath;
		  resourceObjects.put(key, resObject);		  
	}
	  
	private String getEntitySetName(String entitytype) {
		
		String nameStr = "";

		for (String eSetName : metadata.getEntitySets().keySet()) {
			EntitySet entitySet = metadata.getEntitySets().get(eSetName);
			if (entitySet == null) {
				Log.write().error("Cannot find the entity set '" + eSetName + "'");
				  setPopulateAvailableTransactionError(true);
				continue;
			}
			  
			String entityTypeName = entitySet.getEntityTypeName();	
			if ( entityTypeName == null || entityTypeName.isEmpty()){
				Log.write().error("The entity type is not specified for '" + eSetName + "'");
				setPopulateAvailableTransactionError(true);
				continue;
			}
				
			if (entityTypeName.equalsIgnoreCase(entitytype)) {
				nameStr = eSetName;
				break;
			}
		}
		
		return nameStr;
		
	}
	
	private boolean isContainsLink(final String resourcePath) {
		return resourcePath.contains(URL_PATH_LINKS) || resourcePath.contains(URL_PATH_REF);
	}

}
