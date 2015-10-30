/*******************************************************************************
 * Copyright (c) 2015 CA.  All rights reserved.
 *
 * This source file is licensed under the terms of the Eclipse Public License 1.0
 * For the full text of the EPL please see https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ca.casd.utilities.commonUtils.metadata;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/*******************************************************************************/

public class MetadataParserTest {

	static MetadataParser cmsmdParser = null;
	static String edmFile = "test/resources/data/vPubEDMDB.xml"; //"test/resources/data/m2m.xml"; 
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		String odataAEDM = System.getProperty("odataaddistant.AEDM");
		if (odataAEDM != null && false ==odataAEDM.isEmpty())
			edmFile = odataAEDM;
		cmsmdParser = new MetadataParser();
		cmsmdParser.parseXmlFile(edmFile);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void testParseXmlFile() {
		//fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.ca.casd.lisa.plugins.odataassistant.metadata.MetadataParser#getMetadata()}.
	 */
	@Test
	public void testGetMetadata() {
		//fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.ca.casd.lisa.plugins.odataassistant.metadata.MetadataParser#dump()}.
	 * @throws Exception 
	 */
	@Test
	public void testMetadataParser() throws Exception {
	    cmsmdParser.dump();	    
	}

	/**
	 * Test method for {@link com.ca.casd.lisa.plugins.odataassistant.metadata.getEntityTypeProperties()}.
	 * @throws Exception 
	 */
	@Test
	public void testMetadatagetEntityTypeProperties() throws Exception {
		
	    Metadata metadata = cmsmdParser.getMetadata();
	    
	    if ( metadata == null ) {
			fail("MetadataParser::getMetadata()");
			return;
	    } 
	    
    	for (String eSetName : metadata.getEntitySets().keySet()) {
    		EntityType eType = metadata.getEntityType(metadata.getEntitySets().get(eSetName).getEntityTypeName());
    		List<Property> props = metadata.getEntityTypeProperties(eType.getName());
    		System.out.println(eType.getName() + "'s Properties#:" + props.size());
    	}

	}
	
	/**
	 * Test method for {@link com.ca.casd.lisa.plugins.odataassistant.metadata.EntityType}.
	 * @throws Exception 
	 */
	@Test
	public void testMetadataEntityType() {
		
	    Metadata metadata = cmsmdParser.getMetadata();
	    
	    if ( metadata == null ) {
			fail("MetadataParser::getMetadata()");
			return;
	    } 
	    
	    EntityType eType = new EntityType();
	    String strName = "entityName";
	    eType.setName(strName);
	    if (false == eType.getName().equals(strName))
			fail("metadata.EntityType()::getName/setName");
	    
	    
	    //EntityType eType = null;
    	for (String eSetName : metadata.getEntitySets().keySet()) {
    		eType = metadata.getEntityType(metadata.getEntitySets().get(eSetName).getEntityTypeName());
    		if (eType != null)
    			break;
    	}
    	
		if (eType != null) {
			List<String> keys = eType.getKeys();
			if (keys != null && keys.size()>0) {
	    		System.out.println(eType.getName() + " key:" + keys.get(0));
	    		eType.getProperty(keys.get(0));
	    		eType.getProperty("casd.test.dummy");
			}
    		for (NavigationProperty nProp : eType.getNavigationProperties()) {    			
    			eType.getNavigationProperty(nProp.getName());
    		}
    		
    		eType.getNavigationProperty("casd.test.dummy");
    		
    		metadata.getEntityType(eType.getName());
    		
    		metadata.getEntityTypeKey(eType.getName());    	
    		
    		metadata.getAssociations();
    		
		}

	}

	/**
	 * Test method for {@link com.ca.casd.lisa.plugins.odataassistant.metadata.Association}.
	 * @throws Exception 
	 */
	@Test
	public void testMetadataAssociation() {
		
	    Association association = new Association("test", "testDB", "");
	    
	    String strName = "newAssociation";
	    association.setName(strName);
	    if (false == association.getName().equals(strName))
			fail("metadata.Association()::getName(/setName");
	    
	    String strDBJoinOn = "firstDB.ID=secondeDB.ID";
	    association.setDBJoinOn(strDBJoinOn);
	    if (false == association.getDBJoinOn().equals(strDBJoinOn))
			fail("metadata.Association()::getDBJoinOn/setDBJoinOn");
	    
	    String strDbKeyTable = "DBKeyTable";
	    association.setDBFKeyTable(strDbKeyTable);
	    if (false == association.getDBFKeyTable().equals(strDbKeyTable))
			fail("metadata.Association()::getDBFKeyTable/setDBFKeyTable");
	    
	    association.getDBLinkTable();	    
	    
	}

	/**
	 * Test method for {@link com.ca.casd.lisa.plugins.odataassistant.metadata.Property}.
	 * @throws Exception 
	 */
	@Test
	public void testMetadataProperty() {
		
		Property property = new Property("property", "dbColumn", true);
	    
	    String type = "EMD.String";
	    property.setType(type);
	    property.setLength("32");
	    if (false == property.getType().equals(type))
			fail("metadata.Property()::getType/setType");
	    
	    Property property2 = new Property("property2", "dbColumn2");
	    if (property2.isKey())
			fail("metadata.Property(String name, String dbColumnName)");
	    
	    Property property3 = new Property("property3", "dbColumn3", "Edm.String", "256", false);
	    if (property3.isKey())
			fail("metadata.(String name, String dbColumnName, String type, String length, boolean isKey)");
	}

	/**
	 * Test method for {@link com.ca.casd.lisa.plugins.odataassistant.metadata.NavigationProperty}.
	 * @throws Exception 
	 */
	@Test
	public void testMetadataNavigationProperty() {
		
// 	<NavigationProperty Name="Comments" Type="Comment" Relationship="Beer_Comment" Multiplicity="*" />  	

		String name = "Comments";
		String toEntityType = "Comment";
		String relationship = "Beer_Comment";
		String multiplicity = "*";
		NavigationProperty navProperty = new NavigationProperty(name, toEntityType, relationship, multiplicity);
		
		navProperty.setMultiplicity(multiplicity);
		if (false == navProperty.getMultiplicity().equals(multiplicity))
			fail("metadata.NavigationProperty()::setMultiplicity/getMultiplicity");
	    
		if (false == navProperty.getName().equals(name))
			fail("metadata.NavigationProperty()::getName");
			
		if (false == navProperty.getEntityTypeName().equals(toEntityType))
			fail("metadata.NavigationProperty()::getEntityTypeName");		
		
	}
	
	/**
	 * Test method for {@link com.ca.casd.lisa.plugins.odataassistant.metadata.ComplexType}.
	 * @throws Exception 
	 */
	@Test
	public void testMetadataComplexType() {
		
	    Metadata metadata = cmsmdParser.getMetadata();
	    
	    if ( metadata == null ) {
			fail("MetadataParser::getMetadata()");
			return;
	    } 
	    
	    ComplexType complexType = new ComplexType();
	    String strName = "complexTypeName";
	    complexType.setName(strName);
	    if (false == complexType.getName().equals(strName))
			fail("metadata.ComplexType()::getName/setName");
	    	    
	    //EntityType eType = null;
    	for (String key : metadata.getComplexTypes().keySet()) {
    		complexType = metadata.getComplexTypes().get(key);
    		if (complexType != null) {
    			String printStr = "ComplexType: " + complexType.getName();
    			if (complexType.getBaseType() != null && !complexType.getBaseType().isEmpty())
    				printStr += ", BaseType: " + complexType.getBaseType();
	    		System.out.println(printStr);
	    		for (Property p : complexType.getProperties()) {
	    			printStr = "    Property Name: " + p.getName() + ", Type: " + p.getType();
	    			if (p.isCollection())
	    				printStr += ", isCollection=true";
	    			if (p.isComplexType())
	    				printStr += ", isComplexType=true";
	    			
	    			System.out.println(printStr);
	    		}
    		}
    	}
    	
	}
	
	/**
	 * Test method for {@link com.ca.casd.lisa.plugins.odataassistant.metadata.Entity}.
	 * @throws Exception 
	 */
	@Test
	public void testMetadataEntity() {
		
	    Metadata metadata = cmsmdParser.getMetadata();
	    
	    if ( metadata == null ) {
			fail("MetadataParser::getMetadata()");
			return;
	    } 
	    
	    EntityType eType = new EntityType();
	    String strName = "EntityTypeName";
	    eType.setName(strName);
	    if (false == eType.getName().equals(strName))
			fail("metadata.EntityType()::getName/setName");
	    	    
	    //EntityType eType = null;
    	for (String key : metadata.getEntityTypes().keySet()) {
    		eType = metadata.getEntityTypes().get(key);
    		if (eType != null) {
    			String printStr = "ComplexType: " + eType.getName();
    			if (eType.getBaseType() != null && !eType.getBaseType().isEmpty())
    				printStr += ", BaseType: " + eType.getBaseType();
	    		System.out.println(printStr);
	    		for (Property p : eType.getProperties()) {
	    			printStr = "    Property Name: " + p.getName() + ", Type: " + p.getType();
	    			if (p.isCollection())
	    				printStr += ", isCollection=true";
	    			if (p.isComplexType())
	    				printStr += ", isComplexType=true";
	    			
	    			System.out.println(printStr);
	    		}
    		}
    	}
    	
	}
	

}
