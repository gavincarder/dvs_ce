/*******************************************************************************
 * Copyright (c) 2015 CA.  All rights reserved.
 *
 * This source file is licensed under the terms of the Eclipse Public License 1.0
 * For the full text of the EPL please see https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
/**
 * 
 */
package com.ca.dvs.utilities.raml;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.raml.model.Raml;
import org.raml.parser.loader.FileResourceLoader;
import org.raml.parser.rule.ValidationResult;
import org.raml.parser.visitor.RamlDocumentBuilder;
import org.raml.parser.visitor.RamlValidationService;

/**
 * @author artal03
 *
 */
public class VSITest {

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
	 * Test method for {@link com.ca.dvs.utilities.raml.VSI#getDocument()}.
	 */
	@Test
	public final void testGetDocument() {
		
		File				ramlFile	= new File("test/resources/jug/jug_rest.raml");
		SimpleDateFormat	df 			= new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
		
		System.out.println(String.format("%s: BEGIN - VSITest.testGetDocument...", df.format(new Date())));

		System.out.println(String.format("Generate VSI from %s...", ramlFile.getAbsolutePath()));

		assertTrue(String.format("Cannot read test data source - %s", ramlFile.getAbsolutePath()), ramlFile.canRead());

		// Validate the RAML file
		System.out.println(String.format("Validate RAML - %s...", ramlFile.getAbsolutePath()));
		List<ValidationResult> results = RamlValidationService.createDefault().validate(ramlFile.getPath());

		// If the RAML file is valid, get to work...
		assertTrue("Test data source failed RAML validation", ValidationResult.areValid(results));

		try {
			FileInputStream ramlFileStream = new FileInputStream(ramlFile.getAbsolutePath());
			
				try {
					FileResourceLoader 	resourceLoader	= new FileResourceLoader(ramlFile.getParentFile());
					RamlDocumentBuilder rdb         	= new RamlDocumentBuilder(resourceLoader);
					Raml				raml			= rdb.build(ramlFileStream, ramlFile.getAbsolutePath());
			
					assertNotNull(String.format("Failed to build RAML file from test data source - %s", ramlFile.getAbsolutePath()), raml);
			
					VSI vsi = new VSI(raml, ramlFile.getParentFile());
			
					File vsiFile = File.createTempFile("dvs.utilities.raml.VSI.junit", ".vsi");
			
					PrintWriter printWriter = new PrintWriter(vsiFile);
					try {
						VSI.prettyPrint(vsi.getDocument(), printWriter);
					} finally {
						printWriter.close();
					}
	
			} finally {
				ramlFileStream.close();
			}
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
		System.out.println(String.format("%s: END - VSITest.testGetDocument...", df.format(new Date())));
	}

	/**
	 * Test method for {@link com.ca.dvs.utilities.raml.VSI#getDocument()}.
	 */
	@Test
	public final void testGetOperations() {
		
		File				ramlFile	= new File("test/resources/jug/jug_rest.raml");
		SimpleDateFormat	df 			= new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
		
		System.out.println(String.format("%s: BEGIN - VSITest.testGetOperations...", df.format(new Date())));

		System.out.println(String.format("Generate Operations List from %s...", ramlFile.getAbsolutePath()));

		assertTrue(String.format("Cannot read test data source - %s", ramlFile.getAbsolutePath()), ramlFile.canRead());

		// Validate the RAML file
		System.out.println(String.format("Validate RAML - %s...", ramlFile.getAbsolutePath()));
		List<ValidationResult> results = RamlValidationService.createDefault().validate(ramlFile.getPath());

		// If the RAML file is valid, get to work...
		assertTrue("Test data source failed RAML validation", ValidationResult.areValid(results));

		FileInputStream ramlFileStream = null;
		try {
			
			ramlFileStream = new FileInputStream(ramlFile.getAbsolutePath());
		
			FileResourceLoader 	resourceLoader	= new FileResourceLoader(ramlFile.getParentFile());
			RamlDocumentBuilder rdb         	= new RamlDocumentBuilder(resourceLoader);
			Raml				raml			= rdb.build(ramlFileStream, ramlFile.getAbsolutePath());
	
			assertNotNull(String.format("Failed to build RAML file from test data source - %s", ramlFile.getAbsolutePath()), raml);
	
			VSI vsi = new VSI(raml, ramlFile.getParentFile());
	
			StringBuffer sb = new StringBuffer();
			for (String operation : vsi.getOperationsList(raml.getResources())) {
				sb.append(String.format("%s\n", operation));
			}
			//TODO - add code to validate extraction of Operations (compare to reference file ?)

		} catch (Exception e) {
			
			fail(String.format(e.getMessage()));
			
		} finally {
			if (null != ramlFileStream) {
				try {
					ramlFileStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			System.out.println(String.format("%s: END - VSITest.testGetOperations...", df.format(new Date())));
		}
	}

}
