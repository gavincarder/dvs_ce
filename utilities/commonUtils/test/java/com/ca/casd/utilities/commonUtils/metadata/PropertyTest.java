/*******************************************************************************
 * Copyright (c) 2015 CA.  All rights reserved.
 *
 * This source file is licensed under the terms of the Eclipse Public License 1.0
 * For the full text of the EPL please see https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ca.casd.utilities.commonUtils.metadata;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class PropertyTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
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
	public final void testPropertyStringString() {
		Property property = new Property("testName", "testColumn");
		assertEquals("constructor failed to set name", "testName", property.getName());
		assertEquals("constructor failed to set column", "TESTCOLUMN", property.getDBColumnName());
	}

	@Test
	public final void testPropertyStringStringBoolean() {
		Property property = new Property("testName", "testColumn", true);
		
		// Test isKey = true
		assertEquals("constructor failed to set name",          "testName",    property.getName());
		assertEquals("constructor failed to set dbcolumn",      "TESTCOLUMN",  property.getDBColumnName());
		assertEquals("constructor failed to set isKey to true", true,          property.isKey());
		
		property = null;

		// Test isKey = false
		property = new Property("testName", "testColumn", false);
		assertEquals("constructor failed to set name",          "testName",    property.getName());
		assertEquals("constructor failed to set dbcolumn",      "TESTCOLUMN",  property.getDBColumnName());
		assertEquals("constructor failed to set isKey to false", false,        property.isKey());
	}

	@Test
	public final void testPropertyStringStringString() {
		Property property = new Property("testName", "testColumn", "testType");
		
		assertEquals("constructor failed to set name",      "testName",    property.getName());
		assertEquals("constructor failed to set dbcolumn",  "TESTCOLUMN",  property.getDBColumnName());
		assertEquals("constructor failed to set type",      "testType",    property.getType());
	}

	@Test
	public final void testSetType() {
		final String INITIAL_TYPE = "initialType";
		final String NEW_TYPE     = "newType";
		
		Property property = new Property("testName", "testColumn", INITIAL_TYPE);
		
		assertEquals("constructor failed to set initial type", property.getType(), INITIAL_TYPE);
		
		property.setType(NEW_TYPE);
		
		assertEquals("setType failed to update type", property.getType(), NEW_TYPE );
		
	}

}
