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

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author artal03
 *
 */
public class EntityTypeTest {

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
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link com.ca.casd.lisa.extensions.odata.metadata.EntityType#getNavigationProperties()}.
	 */
	@Test
	public final void testGetNavigationProperties() {
		EntityType et = new EntityType();
		
		NavigationProperty initial_np = new NavigationProperty("testNavigationProperty", "myEntityType", "myRelationship", "myMultiplicity");

		et.addNavigationProperty(initial_np);
		
		NavigationProperty got_np = et.getNavigationProperty("testNavigationProperty");
		
		assertEquals("unexpected navigation property name", "testNavigationProperty", got_np.getName());
		
		List<NavigationProperty> listNavigationProperties = et.getNavigationProperties();

		assertEquals("Unexpected NavigationPropertyList size", 1, listNavigationProperties.size());
		
	}

}
