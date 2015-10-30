/*******************************************************************************
 * Copyright (c) 2015 CA.  All rights reserved.
 *
 * This source file is licensed under the terms of the Eclipse Public License 1.0
 * For the full text of the EPL please see https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ca.casd.utilities.commonUtils.util;

import java.util.ArrayList;
import java.util.List;

import com.ca.casd.utilities.commonUtils.CommonDefs;
import com.ca.casd.utilities.commonUtils.log.Log;
import com.ca.casd.utilities.commonUtils.metadata.Edm;
import com.ca.casd.utilities.commonUtils.metadata.EntitySet;
import com.ca.casd.utilities.commonUtils.metadata.EntityType;
import com.ca.casd.utilities.commonUtils.metadata.Metadata;
import com.ca.casd.utilities.commonUtils.metadata.Property;
import com.ca.casd.utilities.commonUtils.metadata.StructuredType;
import com.ca.casd.utilities.commonUtils.util.EnumResourceObjectType;

/**
 * DefaultTransactionUtil
 * <p>Generates the template of Response body for OData Service transaction
 * <p>Generates the template of metadata for OData Service transaction
 * 
 * @author gonbo01
 *
 */
public class DefaultTransactionUtil {
	
	private Metadata metadata;

	final String  URL_PATH_LINKS = "$links";		
	final String  URL_PATH_REF = "$ref";  			// OData 4.0 tag for $link
	final String  URL_COLLECTION = "Collection";
	
	/**
	 * DefaultTransactionUtil
	 * @param metadata - AEDM metadata
	 */
	public DefaultTransactionUtil(Metadata metadata) {
		this.metadata = metadata;
	}

	/**
	 * Generates the template of Response body for OData Service transaction
	 * @param strHttp		- http/https
	 * @param strLisaHost	- host name
	 * @param strPortNumber - port number
	 * @param strBasePath	- the base path of service
	 * @param strMethod		- method 
	 * @param strResourcePath - resource path
	 * @param objectType	- Object type of resource
	 * @param edmTypeName	- Entity type name	
	 * @param odataVersion	- odata version 3 or 4
	 * @return template
	 */
    public String buildResponseTemplate(
    			final String strHttp,						// http://
    			final String strLisaHost,					// Lisa server name
    			final String strPortNumber,					// port number
    			final String strBasePath,
    			final String strMethod, 					
    			final String strResourcePath, 
    			final EnumResourceObjectType objectType,
    			final String edmTypeName,
    			final String odataVersion)  {
    	
     	  String 	templateString = "";

     	  final String	RESPONSE_BODY_BEGIN = "{\n";
    	  final String	RESPONSE_BODY_END = "\n}";
		       	  
    	  // DELETE has no response
		  if (strMethod.equals("DELETE")) {
			  return templateString;
		  }
		  
    	  // $links has no response except GET
		  if ((strResourcePath.contains(URL_PATH_LINKS) || strResourcePath.contains(URL_PATH_REF)) && false == strMethod.equals("GET")){
			  return templateString;
		  }
		  
		  // $Value no template
		  if (objectType.equals(EnumResourceObjectType.VALUE)) {
			  return templateString;
		  }		  
		  
		  // build odata.metadata  
		  String metadataString = buildOdataMetadata(strHttp, strLisaHost, strPortNumber, strBasePath, strResourcePath, objectType, edmTypeName, odataVersion);
		  if (CommonDefs.isOdataVersion4(odataVersion))
			  metadataString = "\"@odata.context\": " + "\"" + metadataString + "\"";
		  else
			  metadataString = "\"odata.metadata\": " + "\"" + metadataString + "\"";

		  // build response content 
		  String contentString = "";
		  if (objectType.equals(EnumResourceObjectType.EDMTYPE) || objectType.equals(EnumResourceObjectType.COMPLEXEDMTYPE)) {
			  int index = strResourcePath.lastIndexOf("/");
			  if (index >= 0){
				  String pname = strResourcePath.substring(index+1);
				  contentString = getSingleProperty(edmTypeName, pname);
			  }
		  }
		  else { 
			  String beginTab = "\t";
			  if (objectType.equals(EnumResourceObjectType.COLLECTION) || objectType.equals(EnumResourceObjectType.COMPLEXEDMTYPE_COLLECTION)) {
				  contentString += "\t" + "\"value\": [\n";
				  contentString += "\t\t" + "@@LIST(query_result)\n";
				  contentString += "\t\t" + "{\n";			  
				  beginTab = "\t\t\t";
			  }
			  
			  if ((strResourcePath.contains(URL_PATH_LINKS) || strResourcePath.contains(URL_PATH_REF)) && strMethod.equals("GET"))
				  contentString += getLinkURL(edmTypeName, beginTab, strHttp, strBasePath, strLisaHost, strPortNumber, odataVersion);
			  else 
				  contentString += getDBColumnNames(edmTypeName, beginTab, strHttp, strLisaHost, strPortNumber, strBasePath, strResourcePath, odataVersion, objectType);
			  
			  if (objectType.equals(EnumResourceObjectType.COLLECTION) || objectType.equals(EnumResourceObjectType.COMPLEXEDMTYPE_COLLECTION)) {
				  contentString += "\n\t\t" + "}\n";
				  contentString += "\t\t" + "@@END\n";
				  contentString += "\t" + "]\n";				  
			  }
		  }

		  templateString = RESPONSE_BODY_BEGIN;		  
		  templateString = templateString + "\t" + metadataString;		  
		  if (!contentString.isEmpty()) {
			  templateString += ",\n"; 
			  templateString += contentString; 
		  }
		  
		  templateString += RESPONSE_BODY_END;
		  
		  return templateString;
    	
    }

	// Odata Metadata Format:
	// "odata.metadata": "http://{{LISA_HOST}}:{{lisa.vse.port.number}}/{{base_path}}/$metadata#{{entity_set}}"
	// "odata.metadata": "http://{{LISA_HOST}}:{{lisa.vse.port.number}}/{{base_path}}/$metadata#{{entity_set}}/@Element"
	// "odata.metadata": "http://{{LISA_HOST}}:{{lisa.vse.port.number}}/{{base_path}}/$metadata#EDM.DataType"
	// "odata.metadata": "http://{{LISA_HOST}}:{{lisa.vse.port.number}}/{{base_path}}/$metadata#{{entity_set1(id)}}/$links/{{entity_set2}}"
    /**
     * Builds a Metadata in the response body 
	 * @param strHttp		- http/https
	 * @param strLisaHost	- host name
	 * @param strPortNumber - port number
	 * @param strBasePath	- the base path of service
	 * @param strMethod		- method 
	 * @param strResourcePath - resource path
	 * @param objectType	- Object type of resource
	 * @param edmTypeName	- Entity type name	
	 * @param odataVersion	- odata version 3 or 4
     * @return the string of metadata
     */
    public String buildOdataMetadata(
    		final String strHttp,				
    		final String strLisaHost,
			final String strPortNumber,
			final String strBasePath,
			final String strResourcePath,
			final EnumResourceObjectType objectType,
			final String strEdmTypeName,
			final String odataVersion) {
    	
    	//URL: "http://{{LISA_HOST}}:{{lisa.vse.port.number}}/strBasePath"
		String urlString = getServiceURL(strHttp, strLisaHost, strPortNumber, strBasePath);
		
		String metadataBodyString = urlString + "/$metadata";		
		if (strResourcePath == null || objectType == null || strEdmTypeName == null)
			return metadataBodyString;
		
		metadataBodyString += "#";
		
		if (strResourcePath.contains(URL_PATH_LINKS)) { // $links is supported only in Odata3
			String linkTag = URL_PATH_LINKS;			
			String tempStr = strResourcePath.substring(0, strResourcePath.indexOf(linkTag)-1);
			if (tempStr.startsWith("/"))
				tempStr = tempStr.substring(1);
			int beginIndex = tempStr.lastIndexOf("/");
			if (beginIndex != -1)
				tempStr = tempStr.substring(beginIndex+1);
			int endIndex = tempStr.lastIndexOf("(");
			if (endIndex != -1)
				tempStr = tempStr.substring(0, endIndex);
			metadataBodyString += tempStr;
			metadataBodyString += "/" + linkTag;
			metadataBodyString += "/" + getEntitySetName(strEdmTypeName);  			
		}
		else if (strResourcePath.contains(URL_PATH_REF)) { // $ref is supported only in Odata4
			if (objectType.equals(EnumResourceObjectType.COLLECTION))
				metadataBodyString += URL_COLLECTION + "(" + URL_PATH_REF + ")";
			else	
				metadataBodyString += URL_PATH_REF;
		}
		else if (objectType.equals(EnumResourceObjectType.COMPLEXEDMTYPE)) {
			metadataBodyString += "EDM.ComplexDataType";
		}
		else if (objectType.equals(EnumResourceObjectType.COMPLEXEDMTYPE_COLLECTION)) {
			metadataBodyString += "EDM.ComplexDataTypeCollection";
		}
		else if (objectType.equals(EnumResourceObjectType.EDMTYPE)) {
			metadataBodyString += "EDM.DataType";
		}
		else {
			metadataBodyString += getEntitySetName(strEdmTypeName);  
		  	if (objectType.equals(EnumResourceObjectType.ELEMENT)) {
				if (CommonDefs.isOdataVersion4(odataVersion))
					metadataBodyString += "/$entity";
				else
					metadataBodyString += "/@Element";
		  	}
		}
				
		return metadataBodyString;
		 
    }

    /**
     * Get a template of the property
     * @param edmTypeName	- entity type name
     * @param edmProperty	- property name
     * @return the template string of the property
     */
    // return: "value": {{BeerProperty_PropertyName}}
	private String getSingleProperty(String edmTypeName, String edmProperty) {

		String returnStr = "";
  		EntityType eType = metadata.getEntityType(edmTypeName);
		if (eType == null) {
			Log.write().error("Cannot find the entity type '" + edmTypeName + "'");
	   		return returnStr; 
		}
		
  		String dbTableName = eType.getDBTableName();
  		if (dbTableName == null || dbTableName.isEmpty()) {
  			Log.write().error("Missed database table name for <<" + edmTypeName + ">>");
	   		return returnStr;  			
  		}
  		
  		returnStr = "\t\"value\": {{" + dbTableName + "_PropertyName" + "}}";

   		return returnStr; 
		
	}

	/**
	 * Get the template of columns
	 * @param edmTypeName 	- Entity type name
	 * @param beginTab 		- Begin tab
	 * @param strHttp		- http/https
	 * @param strLisaHost	- Hose name
	 * @param strPortNumber	- Port number
	 * @param strBasePath	- Service base path
	 * @param strResourcePath - Resource path
	 * @param odataVersion	- Odata version 3/4
	 * @param objectType	- EnumResourceObjectType
	 * @return template string
	 */
	private String getDBColumnNames( final String edmTypeName, 
							final String beginTab, 
				    		final String strHttp,				
				    		final String strLisaHost,
							final String strPortNumber,
							final String strBasePath, 
							final String strResourcePath, 
							final String odataVersion, 
							final EnumResourceObjectType objectType) {
		
		//Format: "PropertyName": "{{TableName_ColumnName}}"
		String dbcolumns = "";
  		StructuredType sType = metadata.getEntityType(edmTypeName);
  		if (sType == null)
  			sType = metadata.getComplexTypes().get(edmTypeName); 
  		
  		String dbTableName = sType.getDBTableName().toUpperCase();
  		
		// For Odata version 4 
		// @odata.id: "<BasePath>/<RsourcePath>(<keyID>)"	
		// @odata.editLink: "<BasePath>/<RsourcePath>(<keyID>)"
		if (CommonDefs.isOdataVersion4(odataVersion)){
			
			String strUrl = getServiceURL(strHttp, strLisaHost, strPortNumber, strBasePath);
	  		strUrl += "/" + getEntitySetName(edmTypeName);	  
	  		if (sType instanceof EntityType)
	  			strUrl += "(" + getEntityTypeKeys((EntityType)sType) + ")";

			dbcolumns += beginTab + "\"@odata.id\": \"" + strUrl + "\"";
   			dbcolumns += ",\n";
			dbcolumns += beginTab + "\"@odata.editLink\": \"" + strUrl + "\"";				
  		}
  		
  		for (Property prop : sType.getProperties()) {
   			String propName = prop.getName();
   			if (propName == null || propName.isEmpty()) {
   				Log.write().error("Missed property name for <<" + edmTypeName + ">>");  				
   			}
   			else {
   	   			if (false == dbcolumns.isEmpty())
   	   				dbcolumns += ",\n";
   	   			
  				dbcolumns += beginTab + "\"" + prop.getName() + "\": \"{{" + dbTableName + "_" + prop.getDBColumnName().toUpperCase() + "}}\"";
   			}
   		}
		
		return dbcolumns;
		
	}

	/**
	 * Gets the template of LINK
	 * @param edmTypeName 	- Entity type name
	 * @param beginTab		- Begin tab
	 * @param strHttp		- http/https
	 * @param basePath		- Service base path
	 * @param strLisaHost	- Host name
	 * @param strPortNumber	- Port number
	 * @param odataVersion	- Odata version 3/4
	 * @return template string
	 */
	private String getLinkURL(	final String edmTypeName, 
								final String beginTab, 
								final String strHttp,
								final String basePath, 
								final String strLisaHost, 
								final String strPortNumber, 
								final String odataVersion) {
		
		// Format in Odata 3: 
		// "url":"http://{{LISA_HOST}}:{{lisa.vse.port.number}}/{{BasePath}}/{{EntitySetName}}('{{DBTableName_Key_columnname}}')"
		// Format in Odata 4: 
		// "@odata.id":"http://{{LISA_HOST}}:{{lisa.vse.port.number}}/{{BasePath}}/{{EntitySetName}}('{{DBTableName_Key_columnname}}')"
		  		
  		EntityType eType = metadata.getEntityType(edmTypeName);
  		//String dbTableName = eType.getDBTableName().toUpperCase();

  		String urlTagName = "\"url\":";
		if (CommonDefs.isOdataVersion4(odataVersion))
			urlTagName = "\"@odata.id\":";
		
		List<String> listEntTypeKey = new ArrayList<String>();
		listEntTypeKey.addAll(eType.getKeys());
		for ( int i=0; i<listEntTypeKey.size(); i++){
			listEntTypeKey.set(i, eType.getProperty(listEntTypeKey.get(i)).getDBColumnName().toUpperCase());
		}		
  		
   		String strUrl = beginTab + urlTagName;
   		strUrl += " \"";
  		strUrl += getServiceURL(strHttp, strLisaHost, strPortNumber, basePath); 
  		strUrl += "/" + getEntitySetName(edmTypeName);
 		strUrl += getEntityTypeKeys(eType);
 		strUrl += "\"";
 		
		return strUrl;
		
	}

	/**
	 * Get Entity Set Name of the specified entity type 
	 * @param edmTypeName - entity type
	 * @return - Entity Set Name
	 */
	private String getEntitySetName(final String edmTypeName) {
		
		String entitySetName = "";

		for (String eSetName : metadata.getEntitySets().keySet()) {
			EntitySet entitySet = metadata.getEntitySets().get(eSetName);
			if (entitySet == null) {
				Log.write().error("Cannot find the entity set <<" + eSetName + ">>");
				continue;
			}
			  
			String entityTypeName = entitySet.getEntityTypeName();	
			if ( entityTypeName == null || entityTypeName.isEmpty()){
				Log.write().error("Missed the entity type name for <<" + eSetName + ">>");
				continue;
			}
				
			if (entityTypeName.equalsIgnoreCase(edmTypeName)) {
				entitySetName = eSetName;
				break;
			}
		}
		
		return entitySetName;
		
	}
	
	/**
	 * Gets a service URL
	 * @param strHttp		- http/https
	 * @param strLisaHost	- Host name
	 * @param strPortNumber	- Port number
	 * @param basePath		- Service base path
	 * @return url string
	 */
	private String getServiceURL(final String strHttp,	final String strLisaHost, final String strPortNumber, final String basePath) {
		String strUrl = strHttp + strLisaHost;
		if (!strPortNumber.isEmpty()) 
			strUrl += ":" + strPortNumber;
		strUrl += basePath;
		
		return strUrl;
	}
	
	private String getPropertyDBNameByType(final String dbTableName, final Property prop) {
		String keyString = "{{" + dbTableName + "_" +  prop.getDBColumnName().toUpperCase() + "}}";
		String dataType = prop.getType();
		if (!dataType.equals(Edm.Int16) && !dataType.equals(Edm.Int32) && !dataType.equals(Edm.Int64))
			keyString = "'" + keyString + "'";
		
		return keyString;
	}
	
	private String getEntityTypeKeys(final EntityType eType) {
		
  		String strKeys = "";
  		String dbTableName = eType.getDBTableName().toUpperCase();

  		for (Property prop : eType.getProperties()) {			
   			if (prop.isKey()) { 
   				if (eType.getKeys().size() == 1) {
	   		  		//{{dbTableName_dbColumnName}}; 
   					strKeys = getPropertyDBNameByType(dbTableName, prop);
   	   					break;
   				}
   				else {
   					// For multi-keys,
   					// keyname1={{dbTableName_dbColumnName1}},keyname2={{dbTableName_dbColumnName2}}, ...
   					if (strKeys.isEmpty()==false) 
   						strKeys += ",";
   					
					strKeys += prop.getName() + "=" + getPropertyDBNameByType(dbTableName, prop);
   				}   					
  			}   			
   		}
   		strKeys = "(" + strKeys + ")";
   		return strKeys;

	}
    
}
