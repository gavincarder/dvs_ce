/*******************************************************************************
 * Copyright (c) 2015 CA.  All rights reserved.
 *
 * This source file is licensed under the terms of the Eclipse Public License 1.0
 * For the full text of the EPL please see https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ca.casd.utilities.commonUtils.metadata;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.ca.casd.utilities.commonUtils.log.Log;

/**
 * Parse the AEDM file
 * 
 * @author gonbo01
 *
 */
public class MetadataParser {

	Metadata metadata ;

	     
	/**
	 * Parse AEDM file 
	 * 
	 * @param fileName - AEDM file name String 
     * @return Metadata
     * @see Metadata
	 * @throws Exception if parsing AEDM failed
	 */
    public Metadata parseXmlFile(String fileName) throws Exception{

		String errMsg;
		
    	if (null==fileName || fileName.isEmpty()) {
    		throw new IllegalArgumentException("no fileName specified");
    	}
		try {
            File xmlFile = new File(fileName);
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance(); 
            domFactory.setNamespaceAware(false); 

            DocumentBuilder builder = domFactory.newDocumentBuilder();
            Document metadataDoc = builder.parse(xmlFile);
            this.metadata = new Metadata(metadataDoc);
            
            boolean bValided = metadata.parse();
            if (!bValided) {
           		throw new IllegalArgumentException("Found error(s) in " + fileName);            	
            }
 
            // create custom entity types(and their custom properties), and  entity sets for any entitytype
          	// that supports custom properties
            ArrayList<EntityType> customETypes = metadata.getCustomSupportedEntityTypes();
            if (customETypes.size() > 0) {
              	for (EntityType cEType : customETypes)  { 	
              		//Create the corresponding Custom EntitySet and Custom EntityType for this entity type
              		metadata.createCustomPropEntitySet(cEType.getName());
                    metadata.createCustomPropEntityType(cEType.getName());       	
              	}
            }
            
            return metadata;
            
    	} 
		catch (Exception e) {
			// ParserConfigurationException can be thrown by newDocumentBuilder
			if (e instanceof ParserConfigurationException) {
				errMsg = "DocumentBuilderFactory.newDocumentBuilder threw ParserConfigurationException - " + e.getMessage();
				System.out.println(errMsg);
			    Log.write().error(errMsg);
			}
			// IOException thrown from builder.parse - non-existent file?
			if (e instanceof IOException) {
				errMsg = "DocumentBuilder.parse(xmlFile) threw IOException - does xmlFile exist? - " + e.getMessage();
				System.out.println(errMsg);
			    Log.write().error(errMsg);
			}
			// SAXException builder.parse - invalid XML file
			if (e instanceof SAXException) {
				errMsg = "DocumentBuilder.parse(xmlFile) threw SAXException - does xmlFile contain valid XML? -  " + e.getMessage();
				System.out.println(errMsg);
			    Log.write().error(errMsg);
			}

            throw new Exception(e.getMessage(), e);
		}

    }
    
    /**
     * Get the current metadata
     * 
     * @return Metadata
     * @see Metadata
     */
    public Metadata getMetadata() {
    	return metadata;
    }

    /**
     * dump Metadata information
     */
    public void dump() {
    	System.out.println("EntitySet:EntityType:DBTable");
    	System.out.println("=================================");
    	for (String eSetName : metadata.getEntitySets().keySet()) {
    		System.out.print(eSetName + ":");
    		EntityType eType = metadata.getEntityType(metadata.getEntitySets().get(eSetName).getEntityTypeName());

    		System.out.println(eType.getName() + ":" + eType.getDBTableName());

    	}
    	
    	System.out.println("\n");
    	System.out.println("=================================");
       	for (String eTypeName : metadata.getEntityTypes().keySet()) {
    		EntityType eType = metadata.getEntityType(eTypeName);
       		String eTypeString = eTypeName;
       		if (eType.isCustomPropsSupported()) {
       			eTypeString += ":" + "CustomPropsSupported";
       			if (!eType.getCustomPropertyName().isEmpty())
       				eTypeString += ":" + eType.getCustomPropertyName();
       		}
        	System.out.println("EntityType:" + eTypeString);
       		
        	System.out.println("Properties:");	       	
    		for (Property prop : eType.getProperties()) {
    			String propString = "\t" +prop.getName() + ":" + prop.getDBColumnName() + ((prop.isKey()==true)? "(key)": ""); 
    			
    			if (!prop.isNullable())
    				propString += ":" + "NOT NULL";
    			
    			String dataType = prop.getType();
    			if (dataType!=null && !dataType.isEmpty())
    				propString += ":" + "Datatype(" + dataType + ")";
    			
    			String defValue = prop.getDefaultValue();
    			if (defValue!=null && !defValue.isEmpty())
    				propString += ":" + "Default(" + defValue + ")";
    				
    			String generated = prop.getGenerated();
    			if (generated!=null && !generated.isEmpty())
    				propString += ":" + "Generated(" + generated + ")";
    			
    			String generatMethod = prop.getGenerateMethod();
    			if (generatMethod!=null && !generatMethod.isEmpty())
    				propString += ":" + "GenMethod(" + generatMethod + ")";
    			
    			String generatePattern = prop.getGeneratePattern();
    			if (generatePattern!=null && !generatePattern.isEmpty())
    				propString += ":" + "GenPattern(" + generatePattern + ")";	
    			
    			System.out.println(propString);
    		}
    		
    		System.out.println("Navigation Properties:");
    		for (NavigationProperty nProp : eType.getNavigationProperties()) {
    			System.out.println("\t" +nProp.getName());
    			System.out.println("\t" + "Relationship:");
    			System.out.println("\t\t" + nProp.getRelationship());
    		}
        	System.out.println("--------------------------");
    	}
       	
    	System.out.println("\n");
    	System.out.println("=================================");
       	for (String eTypeName : metadata.getEnumTypes().keySet()) {
        	System.out.println("EnumType:" + eTypeName);
    		EnumType eType = metadata.getEnumType(eTypeName);
    		String underlyingType = eType.getUnderlyingType();
    		if (underlyingType != null && !underlyingType.isEmpty())
    			System.out.println("underlyingType:" + underlyingType);	
        	System.out.println("Members:");	
    		for (EnumMember member : eType.getMembers())
    			System.out.println("\t" +member.getName() + ":" + member.getValue());    		
        	System.out.println("--------------------------");
    	}      	

    }
    
    public static void main(String[] args) throws XPathExpressionException, SAXException, IOException, ParserConfigurationException, Exception {

    	MetadataParser cmsmdParser = new MetadataParser();

    	if (args.length > 0) {
	    	cmsmdParser.parseXmlFile(args[0]); // i.e. test/resources/data/vPubEDMDB1.xml
	    	cmsmdParser.dump();
    	} else {
    		System.out.println("usage: must pass path to AEDM xml file");
    	}
    		
    }
}
