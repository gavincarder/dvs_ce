/*******************************************************************************
 * Copyright (c) 2015 CA.  All rights reserved.
 *
 * This source file is licensed under the terms of the Eclipse Public License 1.0
 * For the full text of the EPL please see https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ca.dvs.utilities.lisamar;

import java.io.IOException;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.ca.casd.utilities.commonUtils.CommonDefs;
import com.ca.casd.utilities.commonUtils.metadata.Association;
import com.ca.casd.utilities.commonUtils.metadata.ComplexType;
import com.ca.casd.utilities.commonUtils.metadata.Edm;
import com.ca.casd.utilities.commonUtils.metadata.EntitySet;
import com.ca.casd.utilities.commonUtils.metadata.EntityType;
import com.ca.casd.utilities.commonUtils.metadata.EnumType;
import com.ca.casd.utilities.commonUtils.metadata.Metadata;
import com.ca.casd.utilities.commonUtils.metadata.NavigationProperty;
import com.ca.casd.utilities.commonUtils.metadata.Property;
import com.ca.casd.utilities.commonUtils.util.Util;
import com.ca.casd.utilities.commonUtils.log.Log;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * JDBCUtil Class
 * <p>
 * Based on the data model of AEDM or Raml,  
 * Generate the database schema and the seed data if necessary
 * 
 * @author gonbo01
 *
 */

public class JDBCUtil {

	// identifiers that would be unlikely to be used
	private final String internal_column_id = "___id___";
	private final String internal_column_class = "___class___";
	private final String internal_column_etag = "___etag___";

	private Metadata metadata;
	private Map<String, Map<String, Object>> sampleData = null;
	List<Map<String, Object>> insertDataErrors = new ArrayList<Map<String, Object>>();
	Map<String, String> referenceMap = null;
	
	private VSMObject vsmobj;
	private boolean isOdataVersion4 = false;
	private String jdbcDriver = "org.h2.Driver";
	private String dbURL;	
	
	/**
	 * @param vsmobj	- the information of VS object
	 * @param metadata	- data model 
	 */
	public JDBCUtil(final VSMObject vsmobj, Metadata metadata) {
		// TODO Auto-generated constructor stub	
		this.metadata = metadata;	
		this.sampleData = null;
		this.vsmobj = vsmobj;
		this.dbURL = "jdbc:h2:mem:DB" + vsmobj.getVSMName();
		this.isOdataVersion4 = CommonDefs.isOdataVersion4(vsmobj.getOdataVersion());
	}
	
	/**
	 * 
	 * @param vsmobj	 - the information of VS object
	 * @param metadata	 - data model 
	 * @param sampleData - seed data
	 */
	public JDBCUtil(final VSMObject vsmobj, Metadata metadata, Map<String, Map<String, Object>> sampleData) {
		// TODO Auto-generated constructor stub		
		this.metadata = metadata;	
		this.sampleData = sampleData;	
		this.vsmobj = vsmobj;
		this.dbURL = "jdbc:h2:mem:DB" + vsmobj.getVSMName();
		this.isOdataVersion4 = CommonDefs.isOdataVersion4(vsmobj.getOdataVersion());
	}
	
	/**
	 * generates SQL database schema ans save it as SQL file
	 * 
	 * @param targetFile	   - the file with the full path to store the schema  	
	 * @param generateSeedData - true if append the seed data into the schema
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public boolean createDBSchema(String targetFile, boolean generateSeedData) throws SQLException, ClassNotFoundException {
		
		Connection conn = createJDBConnection(jdbcDriver, dbURL, vsmobj.getDatabaseUser(), vsmobj.getDatabasePassword());
		if (conn == null) {
			String errMessage = "Failed to Connect to database"; 
			Log.write().error(errMessage);
			LisaMarUtil.mapErrors.put("createJDBConnection", errMessage);			
			return false;
		}
		
		boolean bOK = true;		
		// get cmds based on AEDM model and execute DB commands
		List<String> cmdList = createDBSchemaCommands(generateSeedData);
		for (String sqlcmd : cmdList) {
			bOK &= execSQLStatement(conn, sqlcmd);		  
		 }

		// create a sequence
		String cmdLine = getCreateSequenceCommand();
		execSQLStatement(conn, cmdLine);
		
		bOK &= exportDatabaseSchema(conn, targetFile);
		
		conn.close();
		
		if (insertDataErrors.size() > 0)
			LisaMarUtil.mapWarnings.put("insertSampleData", insertDataErrors);			
		
		return bOK;
		
	}

	/**
	 *  Create a JDBC connection 
	 * 
	 * @param jdbcDriver - the class name of driver
	 * @param dbURL		 - a database url of the form jdbc:subprotocol:subname
	 * @param user		 - the database user on whose behalf the connection is being made
	 * @param password	 - user's password
	 * @return	a connection to the URL 
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
	 */
	private Connection createJDBConnection(String jdbcDriver, String dbURL, String user, String password) throws ClassNotFoundException, SQLException{

		Connection conn = null;
		String message = "Connecting to database...";
	    Log.write().info(message);
	    
		Class.forName(jdbcDriver);
		conn = DriverManager.getConnection(dbURL, user, password);
		return conn;
	}
	
	/**
	 * Export the database schema to file
	 * 
	 * @param conn		- Database connection 
	 * @param targetFile - the file name with full path to store the schema
	 * @return true if successful
	 */
	private boolean exportDatabaseSchema(Connection conn, String targetFile){

		if (targetFile == null || targetFile.isEmpty()) {
			String errMessage = "There is no target file";
		    Log.write().error(errMessage);
			LisaMarUtil.mapErrors.put("exportDatabaseSchema", errMessage);			
		    return false;
		}
		
		String sql = String.format("SCRIPT TO '%s';", targetFile);
		
		return execSQLStatement(conn, sql);
	}
	
	/**
	 *  Execute a SQL statement 
	 *   
	 * @param conn	- connection to database
	 * @param sql	- sql command
	 * 
	 * @return boolean - true the command executed successful, false if the command failed  	
	 */
	private boolean execSQLStatement(Connection conn, String sql){
		boolean bOK = false;
		String message = "";
		
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(sql);
			//message = "Completed:" + sql;
			//Log.write().info(message);
			bOK = true;
		    
		} 
		catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			message = e.getMessage();
			Log.write().error(message);
			
			if (sql.startsWith("INSERT INTO")) {
			// If failed to insert seeddata, report the error then continue the process
				Map<String, Object> err = new LinkedHashMap<String, Object>();
				err.put("insertTo", message);	
				insertDataErrors.add(err);
				bOK = true;
			}
			else {
				LisaMarUtil.mapErrors.put("execSQLStatement", message);
			}
			
		}
		
		return bOK;
		
	}

	/**
	 * return the command to create sequence start with 100
	 */
	private String getCreateSequenceCommand() {
		
		String cmdLine = "CREATE SEQUENCE ODME_SEQNUM START WITH 100";
		
		return cmdLine;
	}

	/**
	 * Build the commands of inserting seed data into the table of the specified entity type and append the commands into the list
	 * 
	 * @param eType 	- entity type
	 * @param cmdList 	- list of commands  
	 */
	private void insertSampleDataIntoTable(final EntityType eType, List<String> cmdList) {

		 if (eType.isCustomType())
			 return;
		  
		 if (sampleData == null || sampleData.size() == 0)
			 return;

		 if ( isLinkTable(eType) ) {
			 insertSampleDataIntoLinkTable(eType, cmdList);
			 return;
		 }
		  List<Map<String, Object>> samples = getSampleObjects(getEntitySetName(eType.getName()));
		  if (samples == null)
			  return;
		  
		  for (Map<String, Object> map : samples) {
			
			  String sql = "INSERT INTO " + eType.getDBTableName();
			  String strColumns = "";	
			  String strValues = "";
			  
			  boolean hasError = false;
			  boolean hasMissedKey = false;
			  Map<String, Object> errRecord = new LinkedHashMap<String, Object>();
			  String errProperties = "";
			 
			  boolean isMultiKey = eType.getKeys().size() > 1 ? true: false;
			  
			  String canonicalPath = "";
			  for (Property p : eType.getProperties()) {
				  
				  String pName = p.getName();
				  if (pName.isEmpty())
					  continue;			  
  
				  Object dataObj = null;
				  Object sampleObj = map.get(pName);
				  if (sampleObj == null) {
					  if (p.isKey() || !p.isNullable()) {
						  errRecord.put(pName, "<<this property cann't have a null value>>");
						  errProperties += pName + ", ";
						  hasMissedKey = true;
						  continue;
					  }
					  dataObj = p.getDefaultValue();
				  }
				  else {
					  dataObj = isMatchedDataType(p.getType(), sampleObj);
					  if (dataObj == null) {
						  errRecord.put(pName, sampleObj);
						  errProperties += pName + ", ";
						  hasError = true;
						  if (!p.isNullable())
							  hasMissedKey = true;
					  }
					  else 
						  errRecord.put(pName, sampleObj);
				  }
				  
				  if (dataObj == null) 
					  continue;
				  
				  String strObject = convertKeyValueToString(dataObj, p.getType());
				  if (p.isKey()) {
					  if (!canonicalPath.isEmpty())
						  canonicalPath += ",";
					  if (isMultiKey) 
						  canonicalPath += p.getName() + "=" + strObject;
					  else 
						  canonicalPath = strObject;				  
				  }
				  
				  if (strColumns.isEmpty())
					  strColumns += "( ";
				  else
					  strColumns += ",";
				  
				  if (strValues.isEmpty())
					  strValues += " VALUES( ";
				  else
					  strValues += ",";
				  
				  strColumns += p.getDBColumnName();
				  if (dataObj instanceof String)
					  strValues += (String)dataObj;
				  else
					  strValues += dataObj.toString();
				  
			  }
			  
			  if (hasError || hasMissedKey) {
				  Map<String, Object> err = new LinkedHashMap<String, Object>();
				  err.put("entitySet", getEntitySetName(eType.getName()));	
				  err.put("record", errRecord);	
				  String errMessage = "Property is not added due to validation failed";
				  if (hasMissedKey)
					  errMessage = "Record is not added due to the required property is missed or invalid";					  
				  err.put("warning", errMessage);
				  errProperties = errProperties.substring(0, errProperties.length()-2);
				  err.put("problemProperties", errProperties);
				  insertDataErrors.add(err);
			  }
			  
			  if (strColumns.isEmpty() || strValues.isEmpty() || hasMissedKey)
				  break;
			  
			  if (isOdataVersion4) {
				  canonicalPath = "'" + eType.getName() + "(" + canonicalPath + ")" + "'";
				  strColumns += "," + internal_column_id;
				  strValues += "," + canonicalPath;
			  }
			  
			  strColumns += ")";
			  strValues += ");";
			  sql += strColumns + " " + strValues;
			  
			  cmdList.add(sql);
			  System.out.println(sql);
			  
		  } 
		  
	}

	/**
	 * Build the commands of inserting seed data into the linked table and append the commands into the list
	 * 
	 * @param eType 	- entity type
	 * @param cmdList 	- list of commands  
	 */
	private void insertSampleDataIntoLinkTable(final EntityType eType, List<String> cmdList) {

		  if (eType.isCustomType())
			  return;
		  
		  if (referenceMap.size() == 0)
			  return;
		  
		  String strReference = referenceMap.get(eType.getDBTableName().toUpperCase());
		  String[] referencetables = strReference.split(",");
		  
		  boolean bFound = false;
		  EntityType refEtype = null;
		  HashMap<String, String> tableKeyValueMap = new HashMap<String, String>();
		  
		  // search the related Entity
		  for (String tblName: referencetables) {
			  
			  refEtype = getEntityTypeByDBTable(tblName);			  
	    	  for (NavigationProperty nProp : refEtype.getNavigationProperties()) {
	    		  String associationName = nProp.getRelationship();
	    		  if (associationName == null || associationName.isEmpty())
	    			  continue;
	  			  Association association = metadata.getAssociation(associationName);
	  			  if (association == null)
	  				  continue;
				  String tname = association.getDBLinkTable();
				  if (tname.equalsIgnoreCase(eType.getDBTableName().toUpperCase())){
		  			  tableKeyValueMap = metadata.getTableForignKeysFromAssociation(association);
					  bFound = true;
					  break;
				  }
	    	  }
	    	  
	    	  if(bFound)
	    		  break;	    			 			  
		  }
		  
		  if (bFound == false)
			  return;
		  
		  HashMap<String, EntityType> refEntityMap = new HashMap<String, EntityType> ();  	// store refEntityType
		  HashMap<String, String> refEntitySetMap = new HashMap<String, String> (); 		// store refEntitySet
		  HashMap<String, String> refKeyMap = new HashMap<String, String> ();	 			// store refDbcolumn
		  for (Property p : eType.getProperties()) {
			  String dbColumn = p.getDBColumnName().toUpperCase();
			  if (dbColumn.isEmpty())
				  continue;
			  
				String refString = tableKeyValueMap.get(dbColumn);
				if (refString == null || refString.isEmpty())
					continue;
				
				int idxDot = refString.indexOf(".");
				if (idxDot < 0 || idxDot == refString.length()-1) 
					continue;	
				
				String refTable = refString.substring(0, idxDot);
				String refDBcolumn = refString.substring(idxDot+1);
				EntityType e = getEntityTypeByDBTable(refTable);
				refEntityMap.put(p.getName(), e);
				refEntitySetMap.put(p.getName(), getEntitySetName(e.getName()));
				refKeyMap.put(p.getName(), getPropertyNameByDBColumn(e, refDBcolumn));
		  }

		  // link table should have two refs
		  if (refEntityMap.size() < 2)
			  return;
		  
		  insertDataIntoLinkTable(eType, refEntityMap, refEntitySetMap, refKeyMap, cmdList);		  
	}
	
	@SuppressWarnings("unchecked")
	/**
	 * Get the seed data of the specified entity set from Raml mode of OData Service
	 * @param tagName - the name of entity set 
	 * @return - the list of Map<String, Object>
	 */
	private List<Map<String, Object>> getSampleObjects(final String tagName) {		
		
		/* Sample data:
		  "/books": {						"/<EntitySetName>"
		    "schema": "books",				"<EntitySetName>"
		    "example": {
		      "size": 2,
		      "books": [					"<EntitySetName>"
		        {
		          "id": 1,
		          "name": "The Hobbit; or, There and Back Again",
		          "isbn": "054792822X",
		          "author_id": 1
		        },
		        {
		          "id": 2,
		          "name": "The Fellowship of the Ring",
		          "isbn": "B007978NPG",
		          "author_id": 1
		        }
		      ]
		    }
		  }		   
		*/
		
		  Object dataObject = null;		  
		  Object curObject = null;
		  
		  if (sampleData.size() == 0)
			  return null;
			  
		  if (tagName == null || tagName.isEmpty()) 
			  return null;
		  
		  curObject = sampleData.get("/" + tagName); //looking for /<EmtotySetName>
		  if (curObject != null) {			  
			  Map<String, Object> mapObjectList = null;
			  mapObjectList = (Map<String, Object>)curObject;			  
			  Object schemaObject = mapObjectList.get("schema");
			  String schemaValue = schemaObject.toString();
			  if (schemaObject instanceof JsonPrimitive) {
				  JsonPrimitive sObject = (JsonPrimitive)schemaObject;
				  schemaValue = sObject.getAsString();
			  }
			  if (schemaValue.equals(tagName)) {
				  curObject = mapObjectList.get("example");
				  if (curObject != null) {
					  HashMap<String, Object> exampleObject = null;
					  if (curObject instanceof JsonObject)  {
						  JsonObject jsonObj = (JsonObject)curObject;
						  try {
							  exampleObject = convertJsonToMap(jsonObj.toString());
						  } catch (Exception e) {
							  // TODO Auto-generated catch block
							  Map<String, Object> err = new LinkedHashMap<String, Object>();
							  err.put("entitySet", tagName);	
							  err.put("record", jsonObj.toString());	
							  err.put("warning", e.getMessage());						  
							  insertDataErrors.add(err);
							  return null;
						  }
					  }
					  else if (curObject instanceof LinkedHashMap) {						  
						  exampleObject = (HashMap<String, Object>)curObject;
					  }
					  else if (curObject instanceof HashMap) {						  
						  exampleObject = (HashMap<String, Object>)curObject;
					  }
					  
					  if (exampleObject == null)
						  return null;
					  
					  dataObject = exampleObject.get(tagName);					  
				  }
			  }				  
		  }
		  
		  // no sample data for the specified entity
		  if (dataObject == null)
			  return null;
		  
		  List<Map<String, Object>> samples = (List<Map<String, Object>>)dataObject;
		  return samples;
		
	}

	@SuppressWarnings("unchecked")
	/**
	 * Get the seed data of the specified entity set from Map<String, Object>
	 * 
	 * @param tagName	- entity set
	 * @param map		- Map<String, Object>
	 * @return			- the list of Map<String, Object>
	 */
	private List<Map<String, Object>> getSampleObjects(final String tagName, final Map<String, Object> map) {		
		
		Map<String, Object> myMap = new HashMap<String, Object>();
		myMap = map;
		
		if (myMap.size() == 0)
			return null;
		  
		if (tagName == null || tagName.isEmpty()) 
			return null;
	  
		Object dataObject = myMap.get(tagName); 		  
		if (dataObject == null)
			return null;
	  
		List<Map<String, Object>> samples = new ArrayList<Map<String, Object>>();
		try {
			if (dataObject instanceof ArrayList){
				samples = (List<Map<String, Object>>)dataObject;
			}
			else if (dataObject instanceof HashMap) {
				samples.add((Map<String, Object>)dataObject);
			}
			else if (dataObject instanceof LinkedHashMap) {
				samples.add((Map<String, Object>)dataObject);
			}
			else if (dataObject instanceof JsonObject) {
				samples.add( (Map<String, Object>)convertJsonToMap(dataObject.toString()));
			}
			else if (dataObject instanceof JsonPrimitive) {
				JsonPrimitive jsonObject = (JsonPrimitive)dataObject;
	 			samples.add( (Map<String, Object>)convertJsonToMap(jsonObject.getAsString()));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return samples;
		
	}
	
	/**
	 * Insert the seed data into the linked table
	 * 
	 * @param eType 			- entity type
	 * @param refEntityMap		- map of the referenced entity type
	 * @param refEntitySetMap	- map of the referenced entity set  
	 * @param refKeyMap			- map of the referenced key
	 * @param cmdList			- command list
	 */
	private void insertDataIntoLinkTable(final EntityType eType,
			HashMap<String, EntityType> refEntityMap,
			HashMap<String, String> refEntitySetMap,
			HashMap<String, String> refKeyMap,
			List<String> cmdList) {
		
		if (sampleData.size() == 0)
			return;	  

		if (eType.isCustomType())
			return;
		 
		if (refEntitySetMap.size() < 2)
			return;	  
		
		String refEntitySetName1 = "";
		String refEntitySetName2 = "";
		for (Property p : eType.getProperties()) {	
			String refEntitySetName = refEntitySetMap.get(p.getName());
			if (refEntitySetName1.isEmpty())
				refEntitySetName1 = refEntitySetName;
			else if (false == refEntitySetName.equalsIgnoreCase(refEntitySetName1))
				refEntitySetName2 = refEntitySetName;
		}		
		if (refEntitySetName1.isEmpty() || refEntitySetName2.isEmpty())
			return;
		
		// find sample data set of refEntitySet
		List<Map<String, Object>> samples = null;
		samples = getSampleObjects(refEntitySetName1);
		if (samples == null || samples.size() == 0) {
			samples = getSampleObjects(refEntitySetName2);
			String refEntitySetName = refEntitySetName1;
			refEntitySetName1 = refEntitySetName2;
			refEntitySetName2 = refEntitySetName; 
		}
		if (samples == null || samples.size() == 0)
			return;
		
		String sqlInsert = "INSERT INTO " + eType.getDBTableName();
		
		for (Map<String, Object> map1 : samples) {
			
			  List<Map<String, Object>> subSamples = getSampleObjects(refEntitySetName2, map1);
			  if (subSamples == null)
					continue;
			  
			  String strColumns = "";	
			  String strValues = "";
			  
			  for (Property p1 : eType.getProperties()) {
				  
				  if (p1.getName().isEmpty())
					  continue;
				  
				  
				  Object dataObj = null;				  
				  if (refEntitySetMap.get(p1.getName()).equalsIgnoreCase(refEntitySetName1)) {

					  dataObj = map1.get(refKeyMap.get(p1.getName()));
					  if (dataObj == null)
						  continue;
				  
					  dataObj = isMatchedDataType(p1.getType(), dataObj);
					  if (dataObj == null)
						  continue;
					  
					  if (strColumns.isEmpty())
						  strColumns += "( ";
					  else
						  strColumns += ",";
					  
					  if (strValues.isEmpty())
						  strValues += " VALUES( ";
					  else
						  strValues += ",";
					  
					  strColumns += p1.getDBColumnName();
					  if (dataObj instanceof String)
						  strValues += (String)dataObj;
					  else
						  strValues += dataObj.toString();
				  }
				  
			  }
			  if (strColumns.isEmpty() || strValues.isEmpty())
				  break;				  
			  
			  for (Map<String, Object> map2 : subSamples) {
				  
				  String strColumns2 = "";
				  String strValues2 = "";					  
				  for (Property p2 : eType.getProperties()){
					  
					  if (p2.getName().isEmpty())
						  continue;

					  if (refEntitySetMap.get(p2.getName()).equalsIgnoreCase(refEntitySetName1))
						  continue;
					  
					  Object dataObj2 = map2.get(refKeyMap.get(p2.getName()));
					  if (dataObj2 == null)
						  continue;
				  
					  dataObj2 = isMatchedDataType(p2.getType(), dataObj2);
					  if (dataObj2 == null)
						  continue;
					  
					  if (false == strColumns2.isEmpty())
						  strColumns += ",";
					  
					  if (false == strValues2.isEmpty())
						  strValues2 += ",";
					  
					  strColumns2 += p2.getDBColumnName();
					  if (dataObj2 instanceof String)
						  strValues2 += (String)dataObj2;
					  else
						  strValues2 += dataObj2.toString();						  
				  }
				  
				  if (strColumns2.isEmpty() || strValues2.isEmpty())
					  break;
				  
				  String totalColumns = strColumns + "," + strColumns2 + ")";
				  String totalValuse = strValues + "," + strValues2 + ");";
				  String sql = sqlInsert + totalColumns + " " + totalValuse;
				  cmdList.add(sql);
				  System.out.println(sql);					  
			  }
		  } 		  
	}

	/**
	 * Get the map of the referenced tables
	 * 
	 * @return Map<String, String>
	 */
	private Map<String, String> getReferenceTables() {
		
		Map<String, String> referenaceMap = new LinkedHashMap<String, String>();
		
		for (String assName : metadata.getAssociations().keySet()) {
			
			Association association = metadata.getAssociation(assName);
			HashMap<String, String> tableKeyValueMap = metadata.getTableForignKeysFromAssociation(association);

			String tableName = association.getDBLinkTable();
			if (tableName.isEmpty())
				continue;
							
			String refs = "";
			for (String fKey : tableKeyValueMap.keySet()) {
				
				String refString = tableKeyValueMap.get(fKey);
				int idxDot = refString.indexOf(".");
				if (idxDot < 0 || idxDot == refString.length()-1)
					continue;					
				
				String refTable = refString.substring(0, idxDot);
				if (refTable.equalsIgnoreCase(tableName))
					continue;
				
				if (refs.isEmpty())
					refs = refTable;
				else if (refs.startsWith(refTable) || refs.endsWith("," + refTable) || refs.contains("," + refTable + ","))
					continue;
				else
					refs = refs + "," + refTable;
			}				
			referenaceMap.put(tableName, refs);						  			  
		}		
		
		return referenaceMap;
		
	}
	
	/**
	 * generates the commands to create schema
	 * @param generateSeedData
	 * @return the sql command list
	 */
	private List<String> createDBSchemaCommands(boolean generateSeedData) {
		
		List<String> cmdList = new ArrayList<String>();
		
		boolean bGenerateSeedData = false;
		if (generateSeedData && sampleData != null)
			bGenerateSeedData = true;
		
		//create a table based on AEDM
		String message = "Create database table for EntityTypes and ComplexTypes(OData version 4 only)";
		Log.write().info(message);
	
		// For odata4 only: generate DB schema for each complex type 
		if (isOdataVersion4) {
			for (String cTypeName : metadata.getComplexTypes().keySet()) {			
				ComplexType cType = metadata.getComplexTypes().get(cTypeName);			  
				String sql = buildCreateTableCommandForComplexType(cType);
				cmdList.add(sql);			
			}
		}
		
		// create DB schema for each entity type
		Map<String, String> tablesMap = new LinkedHashMap<String, String>();
		
		if (bGenerateSeedData)
			referenceMap = getReferenceTables();
		
		while (tablesMap.size() < metadata.getEntityTypes().size()) {
		
			for (String entityTypeName : metadata.getEntityTypes().keySet()) {			
				  
				EntityType eType = metadata.getEntityType(entityTypeName);			  
				String dbTable = eType.getDBTableName().toUpperCase();
				
				//create table cmd is in list already
				if (null != tablesMap.get(dbTable))
					 continue;
				
				if(bGenerateSeedData && referenceMap != null && referenceMap.size() > 0) {
					boolean okCreate = true;					
					String strReference = referenceMap.get(dbTable);
					if (strReference != null && !strReference.isEmpty()) {
						String[] referencetables = strReference.split(",");
						for (String tblName: referencetables) {
							if (null == tablesMap.get(tblName)) {
								okCreate = false;
								break;
							}				
						}				
					}
					
					if (false == okCreate)
						continue;
				}  
				// One Entity type
				String sql = buildCreateTableCommandForEntityType(eType);
				cmdList.add(sql);
		
				tablesMap.put(dbTable, entityTypeName);
			  
				if (bGenerateSeedData) {
					insertSampleDataIntoTable(eType, cmdList);
				}
				
			 }
		}
		
		appendAssociations(cmdList);
		
		return cmdList;
		
	}
	
	/**
	 * Generates a table of entity type 
	 * 
	 * @param eType - Entity type
	 * @return String - a SQL statement to create a table
	 */
	private String buildCreateTableCommandForEntityType(final EntityType eType) {		
		return buildCreateTableCommandForStructuredType(eType);
	}

	/**
	 * Generates a table of Complex type 
	 * 
	 * @param eType - Complex type
	 * @return String - a SQL statement to create a table
	 */
	private String buildCreateTableCommandForComplexType(final ComplexType eType) {		
		return buildCreateTableCommandForStructuredType(eType);
	}
	
	/**
	 * Generates a table of Object type 
	 * 
	 * @param typeObject - Entity or Complex type
	 * @return String - a SQL statement to create a table
	 */
	private String buildCreateTableCommandForStructuredType(final Object typeObject) {
		
		String sql = "";
		List<Property> properties = null;
		boolean isComplexType = false;
		boolean isLinkTable = false;

		if (typeObject instanceof ComplexType ) {
			ComplexType cType = (ComplexType)typeObject;
			String dbTableName = "";
			dbTableName = cType.getDBTableName();
			if (dbTableName == null || dbTableName.isEmpty())
				dbTableName = cType.getName();
			sql = "CREATE TABLE " + dbTableName;
			properties = cType.getProperties();
			isComplexType = true;
		}
		else {
			EntityType eType = (EntityType)typeObject;
			sql = "CREATE TABLE " + eType.getDBTableName();			
			properties = eType.getProperties();
			isLinkTable = isLinkTable(eType);
		}
			
		  // One Entity/Complex type
		  sql += "(";
		  
		  String column = "";
		  String keyColumn = ", PRIMARY KEY(";
		  int keyCount = 0;
		  
		  // Add the some internal columns for non-link table 
		  // add $id, $json for Complex type
		  // add $id, $class, $etag for entity type	
		  if (!isLinkTable && isOdataVersion4) {
			  column = internal_column_id + " " + Util.edmToH2DataType("Edm.String", "") + " " + "NOT NULL";
			  if (isComplexType) {
				  keyCount = 1;
				  keyColumn += internal_column_id;
			  } 
			  else {
				  column += ", ";
				  column += internal_column_class + " " + Util.edmToH2DataType("Edm.String", "");			  
				  column += ", ";
				  column += internal_column_etag + " " + Util.edmToH2DataType("Edm.String", ""); 
			  }
		  }
		  
		  for (Property p : properties) {
			  
		    /* The dataType is a enum-type
			1. A Property with an enumerated type will create TWO columns in the database. 
			2. First column will be created from the Property element as usual, except:
			3. Type of the database column will be derived from the UnderlyingType instead of the Property Type attribute.
			4. Second column will be created with name colname and with data type INT
			5. If the DefaultValue is defined, and the value of that  DefaultValue matches that of one of the Names of the Members of the EnumType, 
				then a DEFAULT value clause will be added to the $column definition, where value is the Value for the matching Member.
			6. If the Property does not have a DefaultValue defined, or the value of DefaultValue does not match one of the Names of the Members of the EnumType, 
				then a DEFAULT -1 clause will be added to the $column definition.
			*/
			  
			  column += ", ";
			  column += p.getDBColumnName();
			  
			  // A Column for each embedded Collection, 
			  // which will have the Name of the Property (unless overridden by DBCOLUMN) and be of type VARCHAR NOT NULL
			  // An empty collection will still be stored as '[]'
			  if (p.isCollection()) {				  
				  column += " " + Util.edmToH2DataType("Edm.String", "") + " NOT NULL" + " DEFAULT '[]'"; 
				  continue;
			  }
			  
			  // complex type property should be omitted
			  if (p.isComplexType()) {
				  column += " " + Util.edmToH2DataType("Edm.String", "");
				  
				  if (!isOdataVersion4) {
						String errMessage = "'" + p.getType() + "' is a Complex Type which is not supported by DVS implementation for OData Version 3" ; 
						Log.write().error(errMessage);
						LisaMarUtil.mapWarnings.put("Unsupported feature", errMessage);								  
				  }
				  continue;
			  }
			  
			  // simple property (including enumtype)
			  String dataType = p.getType();
			  EnumType enumtype = metadata.getEnumType(dataType);
			  if (enumtype == null) {
				  column += " " + Util.edmToH2DataType(p.getType(), p.getLength());
			  }
			  else {
				  String strLenght = p.getLength();
				  if (strLenght.isEmpty())
					  strLenght = "256";
				  column += " " + Util.edmToH2DataType(enumtype.getUnderlyingType(), strLenght);					  
			  }
			  
			  if (p.isKey()) {
				  if (keyCount > 0)
					  keyColumn += ",";
				  keyColumn += p.getDBColumnName();
				  keyCount++;
			  }
			  
			  if (!p.isNullable()) {
				 column += " " + "NOT NULL"; 
			  }

			  String defultValue = p.getDefaultValue();
			  if (defultValue != null && !defultValue.isEmpty()) {
				  column += " " + "DEFAULT ";
				  column += defultValue;
			  }				  
			  
		  }
		  
		  if ( column.startsWith(",") )
		  		column = column.substring(1);
		  if ( column.endsWith(",") )
		  		column = column.substring(0, column.length()-1);
		  sql += column;
		  
		  if(keyCount > 0) {
			  keyColumn += ")";
			  sql += keyColumn;				  
		  }
		  
		  sql += ");";
		  
		  return sql;
		  
	}
	
	/**
	 * generates the statement for Reference and foreign key and append into the list
	 * 
	 * @param cmdList - the list of statement
	 */
	private void appendAssociations(List<String> cmdList) {
		String message = "Create FOREIGN KEY based on Associations";		
		Log.write().info(message);
		for (String assName : metadata.getAssociations().keySet()) {
			
			Association association = metadata.getAssociation(assName);
			HashMap<String, String> tableKeyValueMap = metadata.getTableForignKeysFromAssociation(association);
				
			for (String fKey : tableKeyValueMap.keySet()) {
				
				String tableName = association.getDBLinkTable();
				if (tableName.isEmpty()) {
					message = "There is no database table specified for " + assName + "-" +fKey;
					Log.write().error(message);
					LisaMarUtil.mapWarnings.put("createDBSchema_" + assName, message);			
					continue;
				}
				
				String refString = tableKeyValueMap.get(fKey);
				int idxDot = refString.indexOf(".");
				if (idxDot < 0 || idxDot == refString.length()-1) {
					message = "Wrong Data Format (TableName.ColumnName): " + refString;
					Log.write().error(message);
					continue;					
				}
				
				String refTable = refString.substring(0, idxDot);
				String refKey = refString.substring(idxDot+1);				
				String assCmd = "ALTER TABLE " + tableName + " ADD FOREIGN KEY (" + fKey + ") "; 
				assCmd += "REFERENCES " + refTable + "(" + refKey + ");";
				cmdList.add(assCmd);			  			  
			}					
		}		
	}

	/**
	 * Check if the specified column is FOREIGN key
	 * 
	 * @param eType		- entity type
	 * @param dbColumn	- column name to check
	 * @return true if the given column is FOREIGN key
	 */
	private boolean isForeignKey(final EntityType eType, final String dbColumn) {
		
		for (String assName : metadata.getAssociations().keySet()) {
	  	
			Association association = metadata.getAssociation(assName);
			HashMap<String, String> tableKeyValueMap = metadata.getTableForignKeysFromAssociation(association);
				
			for (String fKey : tableKeyValueMap.keySet()) {
				
				String tableName = association.getDBLinkTable();
				if (tableName.isEmpty())
					continue;
				
				if (tableName.equalsIgnoreCase(eType.getDBTableName()) && fKey.equalsIgnoreCase(dbColumn) ) 
					return true;
				
			}
	  	}
	  	
		return false;
	}
	
	/**
	 * Get the name of entity set for the given entity type 
	 * @param edmTypeName - the name of Entity type
	 * @return the name of entity set
	 */
	private String getEntitySetName(final String edmTypeName) {
		
		String entitySetName = "";

		for (String eSetName : metadata.getEntitySets().keySet()) {
			EntitySet entitySet = metadata.getEntitySets().get(eSetName);
			if (entitySet == null)
				continue;
			  
			String entityTypeName = entitySet.getEntityTypeName();	
			if ( entityTypeName == null || entityTypeName.isEmpty())
				continue;
			
			if (entityTypeName.equalsIgnoreCase(edmTypeName)) {
				entitySetName = eSetName;
				break;
			}
		}
		
		return entitySetName;
		
	}

	/**
	 * Get the entity type for the given DB table
	 * 
	 * @param dbName - DB table name
	 * @return EntityType
	 */
	private EntityType getEntityTypeByDBTable(final String dbName) {
		
		for (String key : metadata.getEntityTypes().keySet()) {
			EntityType entityType = metadata.getEntityTypes().get(key);
			if (entityType == null)
				continue;
			  
			String tablename = entityType.getDBTableName();	
			if (tablename == null)
				continue;
			
			if (tablename.equalsIgnoreCase(dbName))
				return entityType;
		}
		
		return null;
		
	}
	
	/**
	 * Get the property name of the given the column name
	 * 
	 * @param entityType - Entity type
	 * @param column	 - the column name
	 * @return	the property name 
	 */
	private String getPropertyNameByDBColumn(EntityType entityType, final String column) {
		
		for (Property p: entityType.getProperties()){	
			if (p.getDBColumnName().equalsIgnoreCase(column))
				return p.getName();
		}
		return null;
		
	}
	
	private Object isMatchedDataType(String edmType, Object dataObject) {
		
		 String s = "";
		 if (dataObject instanceof String) 
			 s = (String)dataObject;
		 else 
			 s = dataObject.toString();
		  
		Object retObject = null; 		
 		try {
 			switch (edmType) { 			
				case Edm.Int16:
					retObject = Short.parseShort(s);
 					break;
 				case Edm.Int32:
					retObject = Integer.parseInt(s);
 					break;
 				case Edm.Int64:
					retObject = Long.parseLong(s);
 					break;
 				case Edm.Float:
 				case Edm.Single:
 					retObject = Float.parseFloat(s);
 					break;
 				case Edm.Double:
 					retObject = Double.parseDouble(s);
 					break;
 				case Edm.Boolean:
 					retObject = Boolean.parseBoolean(s);
 					break;
 				case Edm.Guid:
				case Edm.String:
					retObject = "'" + s + "'";
 					break;
				case Edm.Date:
 				case Edm.DateTime:
 				case Edm.DateTimeOffset:
 					retObject = Timestamp.valueOf(s);
 					break;
 				case Edm.Time:
 				case Edm.TimeOfDay:
					SimpleDateFormat df = new SimpleDateFormat("hh:mm:ss a");
					retObject = df.parse(s); 	
					break;
 			}
 		}catch (ParseException e) {
 			return null;
 		}catch (NumberFormatException e) {
 			return null;
 		}
 		
 		return retObject;
 	
 	}
	
	private boolean isLinkTable (final EntityType eType) {
		  // all properties in Link table are Key & ForeignKey
		for (Property p : eType.getProperties()) {			  
			if (p.getName().isEmpty())
				return false;
			if (!p.isKey())
				return false;			  
			if (!isForeignKey(eType, p.getDBColumnName()))
				return false;  			  
		}  
		return true;
	}
	
	private HashMap<String, Object> convertJsonToMap(String sampleJson) throws JsonParseException, JsonMappingException, IOException {

		HashMap<String, Object> map = new HashMap<>();	
		ObjectMapper mapper = new ObjectMapper();
		map = mapper.readValue(sampleJson, new TypeReference<HashMap<String, Object>>() {});
		return map;
	}
	
	private String convertKeyValueToString(Object dataObj, String dataType) {
		String strObject = "";
		
		if (dataObj instanceof String)
			strObject += (String)dataObj;
		else
			strObject = dataObj.toString();
		
		//if (dataType.equals("Edm.String"))
		//	strObject = "'" + strObject + "'";
		
		return strObject;
	}
	
}
