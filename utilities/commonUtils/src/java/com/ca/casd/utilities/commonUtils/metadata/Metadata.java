/*******************************************************************************
 * Copyright (c) 2015 CA.  All rights reserved.
 *
 * This source file is licensed under the terms of the Eclipse Public License 1.0
 * For the full text of the EPL please see https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ca.casd.utilities.commonUtils.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ca.casd.utilities.commonUtils.log.Log;
import com.ca.casd.utilities.commonUtils.util.*;

/**
 * The class to parse the AEDM document.
 * <p>For validation, it is necessary to parse in the order as
 * <p>- The name space of AEDM schema 
 * <p>- Enumerate Types
 * <p>- Complex types
 * <p>- Associations 
 * <p>- Entity types 
 * <p>- Entity Sets 
 * 
 * @author gonbo01
 *
 */
public class Metadata {
	
	// Keyword for custom property and enum type
	public static final String customTypeSuffix = "Property";
	public static final String customSetSuffix = "Properties";	
	public static final String enumTypeSuffix = "EnumType";
	public static final String complexTypeSuffix = "ComplexType";
	public static final String collectionPrefix  = "Collection(";
	public static final String collectionSuffix  = ")";
		
	// Keyword for property
	public static final String Generated_Always = "Always";
	public static final String Generated_Once = "Once";
	public static final String GenMethod_Init = "Init";
	public static final String GenMethod_Sequence = "Sequence";
	public static final String GenMethod_ModificationAction = "ModificationAction";	
	public static final String Keyword_MaxLength_Max = "Max";
	
	private HashMap<String, EntityType> entityTypes = new HashMap<String, EntityType>();
	private HashMap<String, EntitySet> entitySets = new HashMap<String, EntitySet>();
	private HashMap<String, Association> associations = new HashMap<String, Association>();
	private HashMap<String, EnumType> enumTypes = new HashMap<String, EnumType>();
	private HashMap<String, ComplexType> complexTypes = new HashMap<String, ComplexType>();
	
	private Document metadataDoc;
	private String nameSpace = "AEDM";
	private String nameSpaceAlias = null;
	private String nameSpaceVersion = "0.9";
	
	
	public Metadata(Document metadataDoc)  {
		this.metadataDoc = metadataDoc;
	}
	
	/**
	 * For validation, it is necessary to parse in the order as
	 * Enumerate Types, Complex type, Associations, Entity types and Entity Sets 
	 * 
	 * @throws Exception if parsing the metadata failed
	 */
    public boolean parse() throws Exception {

    	boolean isValid = true;
    	
    	// parse the schema
    	isValid &= parseSchemaNameSpace();

    	// parse the enumerate types 
    	isValid &= parseEnumTypes();
    	
    	// parse the complex types
    	isValid &= parseComplexTypes();

    	// parse the Associations/Relationships
    	isValid &= parseAssociations();
    	
    	// parse the entityTypes
    	isValid &= parseEntityTypes();
    	
    	// parse the entitySets
    	isValid &= parseEntitySets();
    	
    	// check if Associations at last once Entity types are parsed
    	isValid &= associationsValidor();
    	
    	return isValid;
    	
    }
    
    /**
     * Parse Entity types
     * 
     * @return true if successful
     * @throws Exception
     */
    private boolean parseEntityTypes() throws Exception {

    	boolean isValid = true;
       	String prefixString = "Edm.EntityType - ";
   	
    	NodeList eTypeList = metadataDoc.getElementsByTagName("EntityType");
    	for (int i=0; i < eTypeList.getLength(); i++) {
    		EntityType eType = new EntityType();
    		
    		prefixString = "Edm.EntityType - ";

    		Element eTypeEle = (Element) eTypeList.item(i);
            
            //set the name
            String etypeName = eTypeEle.getAttribute("Name");
            if (etypeName.isEmpty()) {
            	isValid = false;
			    Log.write().error(prefixString + "Name is missed");
			    continue;
            }            
            else if (null != this.getEntityType(etypeName)) {
            	isValid = false;
			    Log.write().error(prefixString  + "'" + etypeName + "' is duplicated");
			    continue;
            }
            eType.setName(etypeName);

        	prefixString = "Edm.EntityType - " + etypeName;
            
            //set the corresponding DB Table name
		    String dbTable = eTypeEle.getAttribute("DBTable");
		    if (dbTable.isEmpty())
		    	dbTable = etypeName;
		    eType.setDBTableName(dbTable.toUpperCase());
		    
		    //set supports custom properties
		    // Story 197658:
		    // "CustomProp" attribute used in story 177034 is replaced by two attributes, 
		    // "CustomPropertiesEnabled" which behaves exactly as "CustomProp" did in the previous story, 
		    // and "CustomPropertiesName" which specifies the name of the Property to contain 
		    // the collection of custom properties as Name-Value pairs.     
		    // "CustomPropertiesEnabled" MUST be present and set to true in order to enable custom properties at all. 
		    String customPropertiesName = "";
		    if (eTypeEle.getAttribute("CustomProp").equalsIgnoreCase("true") ||
		    		eTypeEle.getAttribute("CustomPropertiesEnabled").equalsIgnoreCase("true")) {
        		eType.setCustomPropsSupported(true);
        		
    		    customPropertiesName = eTypeEle.getAttribute("CustomPropertiesName");
    		    if (!customPropertiesName.isEmpty())
    		    	eType.setCustomPropertyName(customPropertiesName);
        	}
		    
            //set the properties
	        NodeList propertyNList = eTypeEle.getElementsByTagName("Property");
	        
	        for (int j=0; j < propertyNList.getLength(); j++) {

	        	String propName = ((Element)propertyNList.item(j)).getAttribute("Name");
	        	if (propName.isEmpty()) {	        		
	            	isValid = false;
				    Log.write().error(prefixString + ": Property Name is missed");
	        		continue;
	        	}
	        	else if (!customPropertiesName.isEmpty() && propName.equals(customPropertiesName)) {
	            	isValid = false;
				    Log.write().error(prefixString + ": '" + propName + "' has been used as CustomPropertiesName already");
	        		continue;	        		
	        	}
	        	else if (null != eType.getProperty(propName)) {	        		
	            	isValid = false;
				    Log.write().error(prefixString + ": '" + propName + "' is duplicated");
	        		continue;
	        	}

	        	String prefixStringProperty = "Edm.EntityType.Property - " + etypeName + " - " + propName;
	        	
	        	String propDbCol = ((Element)propertyNList.item(j)).getAttribute("DBColumn");
	        	if (propDbCol.isEmpty())
	        		propDbCol = propName;
	        	Property prop = new Property(propName, propDbCol);
            	
            	String propType = ((Element)propertyNList.item(j)).getAttribute("Type");
            	if (!propType.isEmpty()) {
            		if (false == dataTypeValidor(propType)) {
                    	isValid = false;
        			    Log.write().error(prefixStringProperty + ": invalid Type '" + propType + "'");            			            			
            		}
            		
             		boolean isCollection = isCollection(propType);
             		if (isCollection) {
             			prop.setCollection(isCollection);
            			propType = propType.substring(collectionPrefix.length(), propType.length()-1);
             		}
             		
             		// Set datatype for Enumtype as <nameSpace>.<EnumtypeName>
             		EnumType enumtype = this.getEnumType(propType);
             		if (enumtype != null ) {
             			String prefixNameSpace = this.getSchemaNameSpace() + ".";
             	    	if (false == propType.startsWith(prefixNameSpace)) {
             	    		propType = prefixNameSpace + propType;
             	    	}            			
             		}
             		
            		//set property type name as normalized name (i.e, remove namespace prefix if it is there)
             		EnumType tEnum = getEnumType(propType);
             		if (tEnum != null)
             			prop.setType(tEnum.getName());
             		else
             			prop.setType(getNormalizedName(propType));
            		
             		prop.setComplexType(isQualifiedComplexTypeName(propType));
            	}
            	
                //set the key Names
            	if (((Element)propertyNList.item(j)).getAttribute("Key").equalsIgnoreCase("true")) {
            		eType.addKey(((Element)propertyNList.item(j)).getAttribute("Name"));
            		prop.setIsKey(true);
            	}
            		
            	String propLength = ((Element)propertyNList.item(j)).getAttribute("MaxLength");
            	if (propLength.isEmpty()) {
                	propLength = ((Element)propertyNList.item(j)).getAttribute("Length");
            	}
            	if (propLength.isEmpty())
            		propLength = Keyword_MaxLength_Max;
            	else if ( !propLength.equalsIgnoreCase(Keyword_MaxLength_Max) && !isValidInteger (propLength)) {
                    isValid = false;
        			Log.write().error(prefixStringProperty + ": MaxLength '" + propLength + "' is invalid Integer.") ;            			            			           				            			
            	}
            	prop.setLength(propLength);

                //set Nullable
            	if (((Element)propertyNList.item(j)).getAttribute("Nullable").equalsIgnoreCase("false")) {
            		prop.setNullable(false);
            	}
            	
                //set DefaultValue
              	String defaultValue = ((Element)propertyNList.item(j)).getAttribute("DefaultValue");
            	if (!defaultValue.isEmpty()) {
            		EnumType enumtype = this.getEnumType(propType);
            		if (enumtype != null) {
            			if (null == enumtype.getMemberByName(Util.dequoteString(defaultValue))) {
                        	isValid = false;
            			    Log.write().error(prefixStringProperty + ": DefaultValue '" + defaultValue + "' is not defined in '" + enumtype.getName() + "'") ;            			            			           				
            			}
            		}
            		prop.setDefaultValue(defaultValue);
            	}

            	// set Generated 
            	// it has an optional value which can be either 'Always', or 'Once'.
              	String generated = ((Element)propertyNList.item(j)).getAttribute("Generated");
            	if (!generated.isEmpty()) {            		
            		if (!generated.equalsIgnoreCase(Generated_Always) && !generated.equalsIgnoreCase(Generated_Once)) {
                    	isValid = false;
        			    Log.write().error(prefixStringProperty + ": invalid Generated '" + generated + "'");            			
            		}
            		else if (prop.isKey() && !generated.equalsIgnoreCase(Generated_Once)) {
                    	isValid = false;
        			    Log.write().error(prefixStringProperty + ": invalid Generated for Key '" + generated + "'");           			
            		}
            		prop.setGenerated(generated);
            		
            		// set GenMethod 
            		// it has an optional value which can be either 'Init', or 'Sequence',  default is 'Init'
                  	String generateMethod = ((Element)propertyNList.item(j)).getAttribute("GenMethod");
                	if (generateMethod.isEmpty())
                		generateMethod = GenMethod_Init;
                	else if ( !generateMethod.equalsIgnoreCase(GenMethod_Init) && 
                			  !generateMethod.equalsIgnoreCase(GenMethod_Sequence) &&
                			  !generateMethod.equalsIgnoreCase(GenMethod_ModificationAction) ) {
                    	isValid = false;
        			    Log.write().error(prefixStringProperty + ": invalid GenMethod '" + generateMethod + "'");            			                		
                	}
            		else if (prop.isKey() && !generateMethod.equalsIgnoreCase(GenMethod_Sequence)) {
                    	isValid = false;
        			    Log.write().error(prefixStringProperty + ": invalid GenMethod for Key '" + generateMethod + "'");           			
            		}
                	prop.setGenerateMethod(generateMethod);
                		
            		// set GenPattern
                	// It has a mandatory value which is a format string supporting the syntax of the Java String.format method.
                	// GenPattern is only used if Property is of type String and GenMethod is 'Sequence' 
                  	String generatePattern = ((Element)propertyNList.item(j)).getAttribute("GenPattern");
                	if (generatePattern.isEmpty() && generateMethod.equalsIgnoreCase(GenMethod_Sequence))
                 		generatePattern = "%d";			  
                   	prop.setGeneratePattern(generatePattern);
                }
            	
                eType.addProperty(prop);
                
            }
            
            //set the Navigation Properties
            NodeList navPropertyNList = eTypeEle.getElementsByTagName("NavigationProperty");
            for (int j=0; j < navPropertyNList.getLength(); j++) {
           		
            	String navPropName = ((Element)navPropertyNList.item(j)).getAttribute("Name");
            	if (navPropName.isEmpty()) {
	            	isValid = false;
				    Log.write().error(prefixString + ": Navigation property Name is missed");
	        		continue;
	        	}
            	else if ( null != eType.getNavigationProperty(navPropName)) {
	            	isValid = false;
				    Log.write().error(prefixString + ": '" + navPropName + "' is duplicated");
	        		continue;
	        	}
            	
	        	String prefixStringNavProperty = "Edm.EntityType.NavigateProperty - " + etypeName + "." + navPropName;
            	
            	String navPropType = ((Element)navPropertyNList.item(j)).getAttribute("Type");
            	if (navPropType.isEmpty()) {
	            	isValid = false;
				    Log.write().error(prefixStringNavProperty + ": Type is missed");
	        		continue;
	        	}
            	
            	String navPropRel = ((Element)navPropertyNList.item(j)).getAttribute("Relationship");
            	if (navPropRel.isEmpty()) {
	            	isValid = false;
				    Log.write().error(prefixStringNavProperty + ": Relationship is missed");
	        		continue;
	        	}
            	else {
                    // check if the specified association is defined in AEDM
            		Association association = this.associations.get(navPropRel);
            		if (association == null) {
                    	isValid = false;
        			    Log.write().error(prefixStringNavProperty + ": Cannot find Association '" + navPropRel + "'");            			
    	        		continue;
            		}
            	}
            	
            	String navPropToEntityMult = ((Element)navPropertyNList.item(j)).getAttribute("Multiplicity");
            	
            	NavigationProperty navProp = new NavigationProperty(navPropName, navPropType, navPropRel, navPropToEntityMult);
            	
            	eType.addNavigationProperty(navProp);
            }
            
            // add the entity to entity-types list
            this.entityTypes.put(etypeName,eType);
                    	
    	}
    	
    	return isValid;
    	
    }
    
    /**
     * Paese Entity sets
     * @return true if successful
     * @throws Exception
     */
    private boolean parseEntitySets() throws Exception {

    	boolean isValid = true;
    	
    	String prefixString = "Edm.EntitySet - ";
    	NodeList eSetList = metadataDoc.getElementsByTagName("EntitySet");
    	
    	for (int i=0; i < eSetList.getLength(); i++) {
    		EntitySet eSet = new EntitySet();
    		
            Element eSetEle = (Element) eSetList.item(i);
            
            //set the name
            String name = eSetEle.getAttribute("Name");
            if (name.isEmpty()) {
            	isValid = false;
			    Log.write().error(prefixString + "Name is missed");
			    continue;
            }            
            else if (null != this.getEntitySet(name)) {
            	isValid = false;
			    Log.write().error(prefixString + "'" + name + "' is duplicated '");
			    continue;
            }
            eSet.setName(name);

            //set the entityType
            String eTypeName = eSetEle.getAttribute("EntityType");
            if (eTypeName.isEmpty()) {
            	isValid = false;
			    Log.write().error(prefixString + name + ": No EntityType is specified");
        		continue;
            } 
            else {
            // check if the specified entity type is defined in AEDM
            	EntityType etype = this.entityTypes.get(eTypeName);
            	if (etype == null){
                	isValid = false;
    			    Log.write().error(prefixString + name + ": Cannot find EntityType '" + eTypeName + "'");
	        		continue;
                } 
            }
            
            eSet.setEntityTypeName(eTypeName);
                       
            // add the entity to entitysetslist
            this.entitySets.put(name,eSet);
            
    	}
    	
    	return isValid;
             
    }
    
    /**
     * parse Associations
     * @return true if successful
     * @throws Exception
     */
    private boolean parseAssociations() throws Exception {

    	boolean isValid = true;
    	
    	String prefixString = "Edm.Association - ";
    	
    	NodeList assocList = metadataDoc.getElementsByTagName("Association");
    	for (int i=0; i < assocList.getLength(); i++) {
    		
            //get the name
            String name = ((Element)assocList.item(i)).getAttribute("Name");
            if (name.isEmpty()) {
            	isValid = false;
			    Log.write().error(prefixString + "Name is missed");
			    continue;
            }
            else if (null != this.getAssociation(name)) {
            	isValid = false;
			    Log.write().error(prefixString + ": '" + name + "' is duplicated");
			    continue;
            }
            
            String dbLinkTable = ((Element)assocList.item(i)).getAttribute("DBLinkTable");
            if (dbLinkTable.isEmpty()) {
            	isValid = false;
			    Log.write().error(prefixString + name + ": DBLinkTable is missed");
            }

            String dbJoinOn = ((Element)assocList.item(i)).getAttribute("DBJoinOn");
            if (dbJoinOn.isEmpty()) {
            	isValid = false;
			    Log.write().error(prefixString + name + ": DBJoinOn is missed");
            	continue;
            }
            
            dbJoinOn = dbJoinOn.replace(" ", "");
    		Association assoc = new Association(name, dbLinkTable, dbJoinOn);        	
    		this.associations.put(name, assoc);

    	}
    	
    	return isValid;
             
    }

    /**
     * get the name space of Schema
     * @return true if successful
     * @throws Exception
     */
    private boolean parseSchemaNameSpace() throws Exception {
    	
    	boolean isValid = true;
    	
		NodeList nlRoot = this.metadataDoc.getChildNodes();
		for ( int x = 0; x < nlRoot.getLength(); x++ ) {
	        Node nRoot = nlRoot.item(x);
	        if (nRoot.getNodeName().equals("Schema")){
	        	
	        	String nameSpace = ((Element)nRoot).getAttribute("Namespace");
	        	if (nameSpace != null && !nameSpace.isEmpty())
	        		this.nameSpace = nameSpace;
	        	
	        	String nameSpaceAlias = ((Element)nRoot).getAttribute("Alias");
	        	if (nameSpaceAlias != null && !nameSpaceAlias.isEmpty())
	        		this.nameSpaceAlias = nameSpaceAlias;
	        	
	        	String version = ((Element)nRoot).getAttribute("Version");
	        	if (version != null && !version.isEmpty())
	        		this.nameSpaceVersion = version;
	        }
	        		
		}
		
		return isValid;
		
}
    
	/**
	 * Get all the Entity Sets from the metadata.
	 * @return HashMap of EntitySet
	 */
    public HashMap<String, EntitySet> getEntitySets() {
    	
    	return this.entitySets;
    }

    
	/**
	 * Get all the Entity Types from the metadata.
	 * @return HashMap of EntityType
	 */
    public HashMap<String, EntityType> getEntityTypes() {
    	
    	return this.entityTypes;
    }
    
	/**
	 * Get all the Associations from the metadata.
	 * @return HashMap of Association
	 */
     public HashMap<String, Association> getAssociations() {
    	
    	return this.associations;
    }

  	/**
	 * Get all EntityTypes which supports the Custome property from the metadata.
	 * @return HashMap of EntityType
  	 */
     public ArrayList<EntityType> getCustomSupportedEntityTypes() {
     	
     	ArrayList<EntityType> customTypes = new ArrayList<EntityType>();
     	
     	for(String eKey : entityTypes.keySet()) {
     		EntityType eType = entityTypes.get(eKey);
     		if (eType.isCustomPropsSupported())
     			customTypes.add(eType);
     			
     	}
     	return customTypes;
     }
     
     /**
 	 * Get all the EnumTypes from the metadata.
 	 * @return HashMap of EnumType
 	 */
     public HashMap<String, EnumType> getEnumTypes() {
    	
    		return this.enumTypes;
     }
    
     /**
 	 * Get all the ComplexTypes from the metadata.
 	 * @return HashMap of EnumType
 	 */
     public HashMap<String, ComplexType> getComplexTypes() {
    	
    		return this.complexTypes;
     }
    
     
 	/**
 	 * Get the specified Association
 	 * @param associationName String containing the name the Association
 	 * @return Association
 	 */
    public Association getAssociation(String associationName)  {

    	return associations.get(associationName);
    }


	/**
	 * Get the specified EnumType
	 * @param enumTypeName String containing the name the EnumType
	 * @return EnumType
	 */   
    public EnumType getEnumType(String enumTypeName)  {

    	String realTypeName = enumTypeName;
    	String prefixString = this.getSchemaNameSpace() + ".";
    	if (enumTypeName.startsWith(prefixString)) {
    		realTypeName = enumTypeName.substring(prefixString.length());
    	}
    	return enumTypes.get(realTypeName);
    }
    
	/**
	 * Get the specified Entity Set
	 * @param entitySetName String containing the name the EntitySet
	 * @return EntitySet
	 */
    public EntitySet getEntitySet(String entitySetName)  {

    	return entitySets.get(entitySetName);

    }
    
	/**
	 * Get the specified Entity Type
	 * @param entityTypeName String containing the name the EntityType
	 * @return EntityType
	 */
    public EntityType getEntityType(String entityTypeName)  {

    	return entityTypes.get(entityTypeName);

    }
    
	/**
	 * Get the specified Entity Type's Key Name
	 * @param entityTypeName
	 * @return Key Name String
	 */
    public List<String> getEntityTypeKey(String entityTypeName)  {

    	EntityType eType = getEntityType(entityTypeName);
    	
    	return eType.getKeys();
		
    }

	/**
	 * Get the specified Entity Type's Key DB Columns
	 * @param entityTypeName
	 * @return Key Column List
	 */    
    public List<String> getEntityTypeKeyColumns(EntityType eType)  {

    	ArrayList<String> columns = new ArrayList<String>();
    	
    	for (String key : eType.getKeys()) {
    		columns.add(eType.getProperty(key).getDBColumnName());
    	}

    	return columns;
    	
		
    }

    /**
     * Get the specified Entity Type's Properties
     * 
     * @param entityTypeName - name of the EntityType
     * get specified EntityType's BaseType Properties as well
     * 
     * @throws LogException
     * 
     * @returns List of Property
     *
     **/
    public List<Property> getEntityTypeProperties(String entityTypeName)  {

    	List<Property> props = new ArrayList<Property>();
    	
    	props =  entityTypes.get(entityTypeName).getProperties();
    	
    	return props;
    	
    }
    
    /**
     * get the current metadata document
     * @return
     */
    public Document getMetadataDoc() {
    	return metadataDoc;
    }
    
    /**
     * return effective name space by alias. If no alias, go with name space.
     */
    public String getSchemaNameSpace()  {
    	if (this.nameSpaceAlias != null)
    		return this.nameSpaceAlias;
    	else 
    		return this.nameSpace;
    }
    
    /**
     * return exact schema name space
     */
    public String getSchemaNameSpaceExactOnFile()  {
    	return this.nameSpace;
    }
    
    /**
     * return exact schema alias
     */
    public String getSchemaAliasExactOnFile(){
    	return this.nameSpaceAlias;
    }
    
    public String getSchemaNameSpaceVersion()  {

    	return this.nameSpaceVersion;
    }
    
    /**
     * Get Table Keys form Join
     * @param tableJoin
     * @param tableName
     * @return
     */
	public HashMap<String, String> getTableForignKeysFromAssociation(Association association){
		
		HashMap<String, String> tableKeyValueMap = new HashMap<String, String>();

		String tableName = association.getDBLinkTable();
		String tableJoin = association.getDBJoinOn();
		
		if (tableJoin != null) {
			String[] allTableJoins = tableJoin.split(",");
			for (int i = 0; i < allTableJoins.length; i++ ) {
				
				// Format should be TABLE1.col=TABLE2.col
				if (  allTableJoins[i].matches("^(\\w+)\\.(\\w+)=(\\w+)\\.(\\w+)$")) {
					String[] joinOnTokens = allTableJoins[i].split("=");
					
					String lhTableCol = joinOnTokens[0];
					String rhTableCol = joinOnTokens[1];
					
					String[] tableAndCol = lhTableCol.split("\\.");
					if (tableAndCol[0].equalsIgnoreCase(tableName) == true) {
						tableKeyValueMap.put(tableAndCol[1], rhTableCol);
					} else {
						tableAndCol = rhTableCol.split("\\.");
						if (tableAndCol[0].equalsIgnoreCase(tableName) == true) {
							tableKeyValueMap.put(tableAndCol[1], lhTableCol);
						}
					}
				}
				else {
					String errMsg = association.getName() + ": " + "DBJoin" + " is not in the right format TABLE1.colName=Table2.colName: " + allTableJoins[i];
				    //System.out.println(errMsg);	
				    Log.write().error(errMsg);
				}					
			}
			
		}
		return tableKeyValueMap;
		
	}

	/**
	 * Parse Enum types
     * @return true if successful
	 * @throws Exception
	 */
    private boolean parseEnumTypes() throws Exception {
    	
    	boolean isValid = true;
    	String 	prefixString = "Edm.EnumType - ";
    	
    	NodeList enumTypeList = metadataDoc.getElementsByTagName(enumTypeSuffix);
    	
    	for (int i=0; i < enumTypeList.getLength(); i++) {
    		
    		EnumType eType = new EnumType();
    		
            Element typeElement = (Element) enumTypeList.item(i);
            
            //set the name
            String eTypeName = typeElement.getAttribute("Name");
            if (eTypeName.isEmpty()) {
	        	isValid = false;
			    Log.write().error( prefixString + "Name is missed");
            	continue;
            }
            else if (null != this.getEnumType(eTypeName)) {
	        	isValid = false;
			    Log.write().error( prefixString  + ": '"+ eTypeName + "' is duplicated");
            	continue;
            }            
            eType.setName(eTypeName);
            
            //set the UnderlyingType
		    String underlyingType = typeElement.getAttribute("UnderlyingType");
		    if (underlyingType.isEmpty())
		    	underlyingType = Edm.String;
		    else if ( !isQualifiedEdmDataType(underlyingType, true) ) {
	        	isValid = false;
			    Log.write().error( prefixString + eTypeName + ": UnderlyingType is not supported '" + underlyingType + "'");
             }
		    eType.setUnderlyingType(underlyingType);
		    
            //set the enmu-members
	    	int 	intValue = -1; 	    	
	        NodeList memberNList = typeElement.getElementsByTagName("Member");
	        for (int j=0; j < memberNList.getLength(); j++) {
	        	
	        	String memberName = ((Element)memberNList.item(j)).getAttribute("Name");
               	if (memberName.isEmpty()) {
    	        	isValid = false;
				    Log.write().error(prefixString + eTypeName + ": Member - Name is missed");
				    continue;
               	}
               	else if (null != eType.getMemberByName(memberName)) { //	
    	        	isValid = false;
				    Log.write().error(prefixString + eTypeName + ": Member - '" + memberName + "' is duplicated" );
				    continue;
               	}
               	else if (!underlyingType.equals(Edm.String) && !isNumeric(memberName)) {
    	        	isValid = false;
				    Log.write().error(prefixString + eTypeName + ": Member - Name '" + memberName + "' is not a number" );
				    continue;
               	}
               	
	        	EnumMember member = new EnumMember(memberName);           	
            	String value = ((Element)memberNList.item(j)).getAttribute("Value");
            	if (value.isEmpty()) {
           			intValue += 1;
            		value = Integer.toString(intValue);
            	} 
            	else {             		
            		intValue = Integer.parseInt(value);
            	}
            	member.setValue(value);
            	
            	String isflags = ((Element)memberNList.item(j)).getAttribute("IsFlags"); 
               	if (!isflags.isEmpty())
				    Log.write().info(prefixString + eTypeName + ": Member - IsFlags is not supported currently");
          	    
                eType.addMember(member);  
            }
	        
	        //The EnumType element should include one or more Member elements
	        if (eType.getMembers().isEmpty()) {
	        	isValid = false;
			    Log.write().error(prefixString + eTypeName + ": no member");
			    continue;
	        }
	        
	        // add the enumtype to enumTypes list
	        this.enumTypes.put(eTypeName, eType);
    	}
    	
        return isValid;
        
     }
    
    /**
     * Parse Complex types
     * @return true if successful
     * @throws Exception
     */
    private boolean parseComplexTypes() throws Exception {

     	boolean isValid = true;
        String prefixString = "Edm.ComplexType - ";
    	
     	NodeList complexTypeList = metadataDoc.getElementsByTagName(complexTypeSuffix);
     	for (int i=0; i < complexTypeList.getLength(); i++) {
     		prefixString = "Edm.ComplexType - ";
     		ComplexType complexType = new ComplexType();     		
     		Element cTypeEle = (Element) complexTypeList.item(i);
     		
             //set the name
             String typeName = cTypeEle.getAttribute("Name");
             if (typeName.isEmpty()) {
             	isValid = false;
 			    Log.write().error(prefixString + "Name is missed");
 			    continue;
             }            
             else if (null != this.getEntityType(typeName)) {
             	isValid = false;
 			    Log.write().error(prefixString  + "'" + typeName + "' is duplicated");
 			    continue;
             }
             complexType.setName(typeName);
             complexType.setDBTableName(typeName.toUpperCase());
             prefixString = "Edm.ComplexType - " + typeName;
             
             //set Abstract
             String attrAbstract = cTypeEle.getAttribute("Abstract");
             if (attrAbstract.equalsIgnoreCase("true")) 
            	 complexType.setAbstract(true);
             
             //set BaseType
             String attrBaseType = cTypeEle.getAttribute("BaseType");
             if (!attrBaseType.isEmpty()) {
            	 if (isQualifiedComplexTypeName(attrBaseType))
                   	 complexType.setBaseType(attrBaseType);
            	 else
  				    Log.write().error(prefixString + ": Wrong BaseType - " + attrBaseType);
            }
             
             //set OpenType
             String attrOpenType = cTypeEle.getAttribute("OpenType");
             if (attrOpenType.equalsIgnoreCase("true")) 
            	 complexType.setOpenType(true);
             
             //set the properties
 	        NodeList propertyNList = cTypeEle.getElementsByTagName("Property");
 	        
 	        for (int j=0; j < propertyNList.getLength(); j++) {

 	        	Element el = (Element)propertyNList.item(j);
 	        	String propName = el.getAttribute("Name");
 	        	if (propName.isEmpty()) {	        		
 	            	isValid = false;
 				    Log.write().error(prefixString + ": Property Name is missed");
 	        		continue;
 	        	}
 	        	else if (null != complexType.getProperty(propName)) {	        		
 	            	isValid = false;
 				    Log.write().error(prefixString + ": '" + propName + "' is duplicated");
 	        		continue;
 	        	}

 	        	String prefixStringProperty = "Edm.ComplexType.Property - " + typeName + " - " + propName;
 	        	
 	        	String propDbCol = el.getAttribute("DBColumn");
 	        	if (propDbCol.isEmpty())
 	        		propDbCol = propName;
 	        	Property prop = new Property(propName, propDbCol);
             	
             	String propType = el.getAttribute("Type");
             	if (!propType.isEmpty()) {
             		if (false == dataTypeValidor(propType)) {
                     	isValid = false;
         			    Log.write().error(prefixStringProperty + ": invalid Type '" + propType + "'");            			            			
             		}

             		boolean isCollection = isCollection(propType);
             		if (isCollection) {
             			prop.setCollection(isCollection);
            			propType = propType.substring(collectionPrefix.length(), propType.length()-1);
             		}
             		
             		// Set datatype for Enumtype as <nameSpace>.<EnumtypeName>
             		EnumType enumtype = this.getEnumType(propType);
             		if (enumtype != null ) {
             			String prefixNameSpace = this.getSchemaNameSpace() + ".";
             	    	if (false == propType.startsWith(prefixNameSpace)) {
             	    		propType = prefixNameSpace + propType;
             	    	}            			
             		}
             		
            		//set property type name as normalized name (i.e, remove namespace prefix if it is there)
             		EnumType tEnum = getEnumType(propType);
             		if (tEnum != null)
             			prop.setType(tEnum.getName());
             		else
             			prop.setType(getNormalizedName(propType));
             		
             		prop.setComplexType(isQualifiedComplexTypeName(propType));

             	}
             	
             	String propLength = el.getAttribute("MaxLength");
             	if (propLength.isEmpty()) {
                 	propLength = el.getAttribute("Length");
             	}
             	if (propLength.isEmpty())
             		propLength = Keyword_MaxLength_Max;
             	else if ( !propLength.equalsIgnoreCase(Keyword_MaxLength_Max) && !isValidInteger (propLength)) {
                     isValid = false;
         			Log.write().error(prefixStringProperty + ": MaxLength '" + propLength + "' is invalid Integer.") ;            			            			           				            			
             	}
             	prop.setLength(propLength);

                 //set Nullable
             	if (el.getAttribute("Nullable").equalsIgnoreCase("false")) {
             		prop.setNullable(false);
             	}
             	
                 //set DefaultValue
               	String defaultValue = el.getAttribute("DefaultValue");
             	if (!defaultValue.isEmpty()) {
             		EnumType enumtype = this.getEnumType(propType);
             		if (enumtype != null) {
             			if (null == enumtype.getMemberByName(Util.dequoteString(defaultValue))) {
                         	isValid = false;
             			    Log.write().error(prefixStringProperty + ": DefaultValue '" + defaultValue + "' is not defined in '" + enumtype.getName() + "'") ;            			            			           				
             			}
             		}
             		prop.setDefaultValue(defaultValue);
             	}

             	// set Generated 
             	// it has an optional value which can be either 'Always', or 'Once'.
               	String generated = el.getAttribute("Generated");
             	if (!generated.isEmpty()) {            		
             		if (!generated.equalsIgnoreCase(Generated_Always) && !generated.equalsIgnoreCase(Generated_Once)) {
                     	isValid = false;
         			    Log.write().error(prefixStringProperty + ": invalid Generated '" + generated + "'");            			
             		}
             		else if (prop.isKey() && !generated.equalsIgnoreCase(Generated_Once)) {
                     	isValid = false;
         			    Log.write().error(prefixStringProperty + ": invalid Generated for Key '" + generated + "'");           			
             		}
             		prop.setGenerated(generated);
             		
             		// set GenMethod 
             		// it has an optional value which can be either 'Init', or 'Sequence',  default is 'Init'
                   	String generateMethod = el.getAttribute("GenMethod");
                 	if (generateMethod.isEmpty())
                 		generateMethod = GenMethod_Init;
                 	else if ( !generateMethod.equalsIgnoreCase(GenMethod_Init) && 
                 			  !generateMethod.equalsIgnoreCase(GenMethod_Sequence) &&
                 			  !generateMethod.equalsIgnoreCase(GenMethod_ModificationAction) ) {
                     	isValid = false;
         			    Log.write().error(prefixStringProperty + ": invalid GenMethod '" + generateMethod + "'");            			                		
                 	}
             		else if (prop.isKey() && !generateMethod.equalsIgnoreCase(GenMethod_Sequence)) {
                     	isValid = false;
         			    Log.write().error(prefixStringProperty + ": invalid GenMethod for Key '" + generateMethod + "'");           			
             		}
                 	prop.setGenerateMethod(generateMethod);
                 		
             		// set GenPattern
                 	// It has a mandatory value which is a format string supporting the syntax of the Java String.format method.
                 	// GenPattern is only used if Property is of type String and GenMethod is 'Sequence'
                   	String generatePattern = el.getAttribute("GenPattern");
                 	if (generatePattern.isEmpty())
                 		generatePattern = "%d";
                 	prop.setGeneratePattern(generatePattern);               		
                 }
             	
             	complexType.addProperty(prop);
                 
             }
             
             // add the entity to entity-types list
             this.complexTypes.put(typeName, complexType);
                     	
     	}
     	
     	return isValid;
     	
     }
     
    /**
     * Valid the data type
     * @param datatype
     * @return - true if it is valid type
     */
     private boolean dataTypeValidor(final String datatype) {
    	 
    	 boolean bCollection = isCollection(datatype);
    	 
    	 String strDatatype = datatype;
    	 if (bCollection) 
    		 strDatatype = datatype.substring(collectionPrefix.length(), datatype.length()-1);
    	 
    	 boolean isValid = isQualifiedEdmDataType(strDatatype, false);
    	 
   		 // check if it is a defined enum-type
    	 if (!isValid) {
 		  	if (null != getEnumType(strDatatype))
				isValid = true;
   	  	 }	 
    	 
   		 // check if it is a qualified complex type
    	 if (!isValid) {
			isValid = isQualifiedComplexTypeName(strDatatype);
   	  	 }
    	 
    	 return isValid;
     }
    
     /**
      * Check if the data type is collection
      * @param datatype - data type string
      * @return - true if it is a collection
      */
     private boolean isCollection(final String datatype) {
    	 boolean bCollection = false;
    	 if (datatype.startsWith(collectionPrefix) && datatype.endsWith(collectionSuffix))
    		 bCollection = true;
    	 
    	 return bCollection;
     }
     
     /**
      * Check if the data type is qualified EDM type
      * @param datatype - data type 
      * @param isUnderlyingType - true if the data type is a underlying type for Enum type 
      * @return - true if it is qualified
      */
     private boolean isQualifiedEdmDataType(final String datatype, final boolean isUnderlyingType) {
    	 boolean isValid = false;
  	 
   	  	switch (datatype) {

	   	  	case Edm.Boolean:
	   	  	case Edm.Date:
	   	  	case Edm.DateTime:
	   	  	case Edm.DateTimeOffset:
	   	  	case Edm.Guid:
	   	  	case Edm.Time:
	   	  	case Edm.TimeOfDay:
  	  			isValid = isUnderlyingType ? false : true;
	   	  		break;
	   	  		
	   	  	case Edm.Double:
	   	  	case Edm.Float:
	   	  	case Edm.Single:
	   	  	case Edm.Int16:
	   	  	case Edm.Int32:
	   	  	case Edm.Int64:
	   	  	case Edm.String:
		  		isValid = true;
				break;
				
		  	default:
		  		break;	
   	  	 }	 
    	 
    	 return isValid;
     }

     /**
      * Check if the data type is qualified Complex type
      * @param name - data type 
      * @return - true if it is qualified
      */
     private boolean isQualifiedComplexTypeName(final String name) {
    	 boolean isQualified = false;    	 
    	 String typeName = name;
    	 int dotIndex = typeName.lastIndexOf(".");
    	 if (dotIndex > 0) {
    		 String nameSpace = typeName.substring(0, dotIndex);
    		 if (!nameSpace.equals(getSchemaNameSpace()))
    			 return isQualified;
    		 
    		 typeName = typeName.substring(dotIndex+1);
    	 }
    	 
    	 for (String key: complexTypes.keySet()) {
    		 if (key.equals(typeName)) {
    			 isQualified = true;
    			 break;    			
    		 }    		
    	 }
    	 
    	 return isQualified;
     }
     
     @SuppressWarnings("unused")
	 private boolean isNumeric(String str) {  
	     try {  
	         double d = Double.parseDouble(str);  
	     }  
	     catch(NumberFormatException nfe) {  
	         return false;  
	     } 	     
	     return true;  
     }
     
     private boolean isValidInteger(String str) {
	     try {  
	  		Integer intLength = new Integer(str);
	    	 if ( intLength.intValue() > Integer.MAX_VALUE)
	    		 return false;
	     }  
	     catch(NumberFormatException nfe) {  
	         return false;  
	     } 	     
	     return true;  
   	 
     }
     
     private boolean associationsValidor() {
    	 
    	 boolean isValid = true;
         String prefixString = "Edm.Association - ";
         EntityType entityType = null;

    	 for (String assName : this.associations.keySet()) {       		 
    		 
       		Association association = this.associations.get(assName);
       		
       		//Check if dbTableName is defined in AEDM
       		String dbTableName = association.getDBLinkTable();
       		if (dbTableName.isEmpty()) {
	           	isValid = false;
				Log.write().error(prefixString + assName + ": DBLinkTable is missed");
				continue;
       		}
       		else {
	       		entityType = getEntityTypeByDatabaseTable(dbTableName);
	           	if (entityType == null) {
	           		isValid = false;
				    Log.write().error(prefixString + assName + ": invalid DBLinkTable '" + dbTableName + "'");           		
	           	}
       		}
       		
           	// check DBJoinOn
			String tableJoin = association.getDBJoinOn();
			String[] allTableJoins = tableJoin.split(",");
			for (int i = 0; i < allTableJoins.length; i++ ) {
				
				// Format should be TABLE1.col1=TABLE2.col2
				if (  allTableJoins[i].matches("^(\\w+)\\.(\\w+)=(\\w+)\\.(\\w+)$")) {
					String[] joinOnTokens = allTableJoins[i].split("=");
					
					String lhTableCol = joinOnTokens[0];
					String rhTableCol = joinOnTokens[1];
					
					String keyColumn = "";
					String tableAndColumn = "";
					String[] tableAndCol = lhTableCol.split("\\.");
					if (tableAndCol[0].equalsIgnoreCase(dbTableName) == true) {
						keyColumn = tableAndCol[1];
						tableAndColumn = rhTableCol;
					} else {
						tableAndCol = rhTableCol.split("\\.");
						if (tableAndCol[0].equalsIgnoreCase(dbTableName) == true) {
							keyColumn = tableAndCol[1];
							tableAndColumn = lhTableCol;
						}
						else {
			           		isValid = false;
						    Log.write().error(prefixString + assName + " - DBJoinOn: is not in the right format TABLE1.colName1=Table2.colName2: '" + allTableJoins[i] + "'");           		
						}				
					}
					
					// check if the column is defined in the table dbTableName
					if (!keyColumn.isEmpty() && entityType != null) {
				  		if (false == isDatabaseColumnExist(keyColumn, entityType)) {
			           		isValid = false;
						    Log.write().error(prefixString + assName + " - DBJoinOn: '" + keyColumn + "' is not in the table '" + dbTableName + "'");           						  			
				  		}
					}
					
					if (tableAndColumn.isEmpty())
						continue;
						
					int idxDot = tableAndColumn.indexOf(".");
					if (idxDot < 0 || idxDot == tableAndColumn.length()-1) {
		           		isValid = false;
					    Log.write().error(prefixString + assName + " - DBJoinOn: Worng Format (TableName.ColumnName): '" + tableAndColumn + "'");           		
					}
					else {
						String dbtable2Name = tableAndColumn.substring(0, idxDot); 
			       		EntityType entityType2 = getEntityTypeByDatabaseTable(dbtable2Name);
			           	if (entityType2 == null) {
			           		isValid = false;
						    Log.write().error(prefixString + assName + ": invalid table name'" + dbtable2Name + "'");           		
			           	}
			           	else {
			           		String keyColumn2 = tableAndColumn.substring(idxDot+1); 
					  		if (false == isDatabaseColumnExist(keyColumn2, entityType2)) {
				           		isValid = false;
							    Log.write().error(prefixString + assName + " - DBJoinOn: '" + keyColumn2 + "' is not in the table '" + dbtable2Name + "'");           						  			
					  		}
			           	}
					}						
				}
				else {
	           		isValid = false;
				    Log.write().error(prefixString + assName + " - DBJoinOn: is not in the right format TABLE1.colName1=Table2.colName2: '" + allTableJoins[i] + "'");           		
				}					
			}			
			
       	 }
       	 
    	 return isValid;
     }
     
     private EntityType getEntityTypeByDatabaseTable(final String dbTableName) {

    	 EntityType eType = null;
    	 
         for (String eTypeName : this.getEntityTypes().keySet()) {
        	eType = this.getEntityType(eTypeName);
     		if (dbTableName.equalsIgnoreCase(eType.getDBTableName()))
     			return eType;
         }           	
        
         return null;
     }
    
     private boolean isDatabaseColumnExist(final String dbColumnName, final EntityType entityType) {
    	 boolean bFound = false;
  		for (Property prop : entityType.getProperties()) {
  			if (prop.getDBColumnName().equalsIgnoreCase(dbColumnName)) {
  				bFound = true;
  				break;
  			}
  		}
    	 return bFound;
     }
     
     public EntitySet createCustomPropEntitySet(String name) {
     	EntitySet eSet = new EntitySet();
     	String customSetName = name+customSetSuffix;
     	
     	eSet.setName(customSetName);
     	eSet.setEntityTypeName(name+customTypeSuffix);
     	eSet.setCustomSet(true);
     	
     	// add the entity to entitytypes list
         this.entitySets.put(customSetName,eSet);
         
         return eSet;
     }
     
     public EntityType createCustomPropEntityType(String name) {
     	EntityType eType = new EntityType();
     	
     	String customTypeName = name+customTypeSuffix;
     	eType.setName(customTypeName);
     	eType.setDBTableName(customTypeName.toUpperCase());
     	eType.setCustomType(true); 
     	
     	Property nameProp = new Property(Constants.CPDEF_NAME, Constants.CPDEFCOL_NAME, "Edm.String", "256", true);
     	Property dataTypeProp = new Property(Constants.CPDEF_DATATYPE, Constants.CPDEFCOL_DATATYPE,"Edm.String", "256");
     	Property descProp = new Property(Constants.CPDEF_DESCRIPTION, Constants.CPDEFCOL_DESCRIPTION,"Edm.String", "4096");
     	Property defValProp = new Property(Constants.CPDEF_DEFAULTVALUE, Constants.CPDEFCOL_DEFAULTVALUE, "Edm.String", "256");
     	Property possValsProp = new Property(Constants.CPDEF_POSSIBLEVALUES, Constants.CPDEFCOL_POSSIBLEVALUES, "Edm.String", "4096");
     	Property prodMapProp = new Property(Constants.CPDEF_PRODUCERMAPPINGPROPERTY, Constants.CPDEFCOL_PRODUCERMAPPINGPROPERTY,"Edm.String", "256");
     	Property entityNameProp = new Property(Constants.CPDEF_ENTITYNAME, Constants.CPDEFCOL_ENTITYNAME,"Edm.String", "256");
     	
     	eType.addProperty(nameProp);
     	eType.addProperty(dataTypeProp);
     	eType.addProperty(descProp);
     	eType.addProperty(defValProp);
     	eType.addProperty(possValsProp);
     	eType.addProperty(prodMapProp);
     	eType.addProperty(entityNameProp); 
     	
     	// add the entity to entitytypes list
         this.entityTypes.put(customTypeName,eType);
         
         return eType;
         
     }  
     
     public void createEnumeratedType(String name, String dataType, String possibleValues) throws RuntimeException{
     	
     	if (possibleValues != null) {
 			//create a new enum type
     		EnumType enumType = new EnumType();
 			enumType.setName(name);
 			enumType.setUnderlyingType(dataType);
 			String[] enumMembersTok = possibleValues.split(",");
 			for (int i = 0; i < enumMembersTok.length; i++ ) {
 				if (!Util.isValidDBString(enumMembersTok[i]) ) {
 					String errMsg = "Custom Property's possible values has unmatched quoted string";
 					Log.write().error(errMsg);
 					throw new RuntimeException(errMsg, null);							

 				}
 				
 				String enumMemberStr = Util.normalizeQuotes(enumMembersTok[i]);
 				
 				if (!Util.isStringOfEdmType(dataType, enumMemberStr)) {
 					String errMsg = "Custom Property's possible values do not match the specified dataType";
 					Log.write().error(errMsg);
 					throw new RuntimeException(errMsg, null);

 				}else {
 					EnumMember enumMem = new EnumMember(enumMemberStr);
 					enumMem.setValue(Integer.toString(i));
 					enumType.addMember(enumMem);						
 				}
 				
 			}
 			
     	}

     }
     
     public void addEnumeratedType(EnumType enumType) {
     	this.enumTypes.put(enumType.getName(), enumType);
     }
     
     public void removeEnumeratedType(String name) {
     	this.enumTypes.remove(name);
     }
     
     public void addEntityTypeCustomProperty(EntityType eType, String name, String dbColumnName, String dataType, String length, String defaultValue) {
     	
     	if (dbColumnName == null || dbColumnName.isEmpty())
     		dbColumnName = name;
     	
     	Property customProp = new Property(name, dbColumnName, dataType);
     	
    	customProp.setDefaultValue(defaultValue);
    	
    	if (!length.isEmpty())
     		customProp.setLength(length);
     	
     	customProp.setCustomProp(true);
     	
     	eType.addProperty(customProp);
     }
     
     public void updateEntityTypeCustomProperty(EntityType eType, String name, String dbColumnName, String dataType, String length,String defaultValue) {
     	
     	if (dbColumnName == null || dbColumnName.isEmpty())
     		dbColumnName = name;
     	
     	Property customProp = eType.getProperty(name);
     	if (customProp != null) {
     		if(!customProp.getDBColumnName().toUpperCase().equals(dbColumnName.toUpperCase()))
     			customProp.setDBColumnName(dbColumnName);
     		customProp.setType(dataType);
     		customProp.setLength(length);
     		customProp.setDefaultValue(defaultValue);
     	}

     }
     
     public void removeEntityTypeCustomProperty(EntityType eType, String name) {
     	eType.removeProperty(name);
     }
 
 	/**
 	 * Get the normalized name 
 	 * @param name String containing SchemaNameSpace
 	 * @return normalizedName
 	 */   
     public String getNormalizedName(String name)  {

     	String normalizedName = name;
     	String prefixString = this.getSchemaNameSpace() + ".";
     	if (name.startsWith(prefixString)) {
     		normalizedName = name.substring(prefixString.length());
     	}
     	return normalizedName;
     }
     
}
