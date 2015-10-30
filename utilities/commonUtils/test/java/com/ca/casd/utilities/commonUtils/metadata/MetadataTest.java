/*******************************************************************************
 * Copyright (c) 2015 CA.  All rights reserved.
 *
 * This source file is licensed under the terms of the Eclipse Public License 1.0
 * For the full text of the EPL please see https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
/**
 * 
 */
package com.ca.casd.utilities.commonUtils.metadata;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * @author artal03
 *
 */
public class MetadataTest {

	private Document dom      = null;
	private Metadata metadata = null;
	static String edmFile = "test/resources/data/vPubEDMDB.xml"; //"test/resources/data/m2m.xml"; 
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		//get an instance of builder
		DocumentBuilder db = dbf.newDocumentBuilder();

		File testXML = new File(edmFile);
		
		assertTrue("Test XML file "+testXML.getPath()+" does not exist", testXML.exists());
		
		// Load the test XML file
		dom = db.parse(testXML);
		
		metadata = new Metadata(dom);
		
		metadata.parse();

	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		metadata = null;
		dom     = null;
	}

	/**
	 * Test method for {@link com.ca.casd.lisa.extensions.odata.metadata.Metadata#Metadata(org.w3c.dom.Document)}.
	 */
	@Test
	public final void testMetadata() {
		//fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.ca.casd.lisa.extensions.odata.metadata.Metadata#parse()}.
	 */
	@Test
	public final void testParse() {
		//fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.ca.casd.lisa.extensions.odata.metadata.Metadata#getEntitySets()}.
	 */
	@Test
	public final void testGetEntitySets() {
		//fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.ca.casd.lisa.extensions.odata.metadata.Metadata#getEntityTypes()}.
	 */
	@Test
	public final void testGetEntityTypes() {
		//fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.ca.casd.lisa.extensions.odata.metadata.Metadata#getAssociations()}.
	 */
	@Test
	public final void testGetAssociations() {
		//fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.ca.casd.lisa.extensions.odata.metadata.Metadata#getEntityType(java.lang.String)}.
	 */
	@Test
	public final void testGetEntityType() {
		//fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.ca.casd.lisa.extensions.odata.metadata.Metadata#getEntityTypeKey(java.lang.String)}.
	 */
	@Test
	public final void testGetEntityTypeKey() {
		List<String> typeKeyList = metadata.getEntityTypeKey("Comment");
		Iterator<String> itKeyList = typeKeyList.iterator();
		while(itKeyList.hasNext()) {
			System.out.println("EntityTypeKey(Comment): "+itKeyList.next());
		}
	}

	/**
	 * Test method for {@link com.ca.casd.lisa.extensions.odata.metadata.Metadata#getEntityTypeProperties(java.lang.String)}.
	 */
	@Test
	public final void testGetEntityTypeProperties() {
		List<Property> entityTypePropertyList = metadata.getEntityTypeProperties("Comment");
		Iterator<Property> itEntityTypePropertyList = entityTypePropertyList.iterator();
		while(itEntityTypePropertyList.hasNext()) {
			System.out.println("EntityTypeProperty(id): "+itEntityTypePropertyList.next());
		}
	}

}
