/*******************************************************************************
 * Copyright (c) 2015 CA.  All rights reserved.
 *
 * This source file is licensed under the terms of the Eclipse Public License 1.0
 * For the full text of the EPL please see https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ca.casd.lisa.plugins.odataassistant;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ca.casd.lisa.plugins.odataassistant.utils.EnumDefaultTransactions;
import com.ca.dvs.utilities.lisamar.VSITransactionObject;

import com.ca.casd.utilities.commonUtils.CommonDefs;
import com.ca.casd.utilities.commonUtils.metadata.MetadataParser;

public class EnumDefaultTransactionsTest {
	
	static String edmFile = "test/data/vPubEDMDB.xml";
	static String basePath= "/Odata/v1/vNIM";
	EnumDefaultTransactions defaultTransactions = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		MetadataParser cmsmdParser = new MetadataParser();
		cmsmdParser.parseXmlFile(edmFile);
		defaultTransactions = new EnumDefaultTransactions(basePath, 4, cmsmdParser.getMetadata(), CommonDefs.VALUE_ODATA_VERSION_3);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testEnumDefaultTransactions() {
		if (defaultTransactions == null)
			fail("EnumDefaultTransactions::EnumDefaultTransactions()");
	}

	@Test
	public void testPopulateAvailableTransaction() {
		ArrayList<VSITransactionObject> transObjects = defaultTransactions.populateAvailableTransaction();
   		if (transObjects.size() == 0) { 
   			fail("EnumDefaultTransactions::populateAvailableTransaction()");
   		}
	}

	@Test
	public void testBuildAvailableResourcePath() {
		String method = "GET";
		String[] resPathes = defaultTransactions.buildAvailableResourcePath(method);
       	if (resPathes == null) 
       		fail("EnumDefaultTransactions::buildAvailableResourcePath() - " + method);
	}

	@Test
	public void testBuildDefaultDescription() {		
		String method = "GET";
		String description = defaultTransactions.buildDefaultDescription(method, basePath);
		if (description==null || description.isEmpty())
       		fail("EnumDefaultTransactions::buildAvailableResourcePath()");
	}

	/*@Test
	public void testBuildDefaultResponseBody() {
		String method = "GET";
		String resourcePath = basePath + "/Comments";
		String strResponse = defaultTransactions.buildDefaultResponseBody(method, resourcePath);
		if (strResponse==null || strResponse.isEmpty())
       		fail("EnumDefaultTransactions::buildDefaultResponseBody()");
	}*/

}
