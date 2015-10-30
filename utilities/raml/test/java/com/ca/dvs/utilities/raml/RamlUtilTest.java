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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.raml.model.ParamType;
import org.raml.model.Raml;
import org.raml.parser.loader.FileResourceLoader;
import org.raml.parser.rule.ValidationResult;
import org.raml.parser.visitor.RamlDocumentBuilder;
import org.raml.parser.visitor.RamlValidationService;

/**
 * @author artal03
 *
 */
public class RamlUtilTest {

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

	private final void compareFiles(File testFile, File refFile) throws Exception {

		FileReader testFileReader = new FileReader(testFile);
		try {
			BufferedReader testReader = new BufferedReader(testFileReader);
			try {
				FileReader refFileReader = new FileReader(refFile);
				try {
					BufferedReader refReader = new BufferedReader(refFileReader);
					try {
						String testLine = null;
						String refLine  = null;
			
						int lineCounter = 1;
						
						while (null != (refLine = refReader.readLine())) {
							testLine = testReader.readLine();
							String msg = String.format("File contents do not match at line %d", lineCounter);
							assertEquals(msg, refLine, testLine);
						}
						
					} finally {
							refReader.close();
					}
				} finally {
					refFileReader.close();
				}
			} finally {
				testReader.close();
			}
		} finally {
			testFileReader.close();
		}
		
	}
	
	/**
	 * Test method for {@link com.ca.dvs.utilities.raml.RamlUtil#RamlUtil()}.
	 */
	@Test
	public final void testRamlUtil() {
		String[] edmSysoutArgs         = { "test/resources/jug/jug_odata.raml", "EDM" };
		String[] edmFileArgs           = { "test/resources/jug/jug_odata.raml", "EDM", "junit.edm" };
		String[] sourceTargetSameArgs  = { "test/resources/jug/jug_odata.raml", "EDM", "test/resources/jug/jug_odata.raml" };
		String[] wadlArgs              = { "test/resources/jug/jug_odata.raml", "WADL" };
		String[] invalidOutputTypeArgs = { "test/resources/jug/jug_odata.raml", "BAD" };
		String[] invalidRamlArgs       = { "test/resources/jug/jug_odata.edm", "EDM" };
		String[] fileNotFoundArgs      = { "xxx.yyy.zzz", "EDM" };
		String[] noArgs                = {};
		try {
			System.out.println("Generate EDM on System.out...");
			RamlUtil.main(edmSysoutArgs);
			
			System.out.println("Generate EDM file (junit.edm)...");
			RamlUtil.main(edmFileArgs);
			File refFile = new File("test/resources/jug/jug_odata.edm");
			File outFile = new File(edmFileArgs[2]);
			compareFiles(refFile, outFile);
			outFile.delete();
			
			System.out.println("Generate WADL file on System.out...");
			RamlUtil.main(wadlArgs);
			
			try {
				RamlUtil.main(invalidOutputTypeArgs);
				fail("Expected exception - invalid output file type");
			} catch (Exception e) {
				System.out.println("Good - caught exception when specifying invalid output file type");
			}
			try {
				RamlUtil.main(sourceTargetSameArgs);
				fail("Expected exception - Source and target file arguments are the same");
			} catch (Exception e) {
				System.out.println("Good - caught exception when specifying same path for source and target file arguments");
			}
			try {
				RamlUtil.main(invalidRamlArgs);
				fail("Expected exception - source file is not a RAML file");
			} catch (Exception e) {
				System.out.println("Good - caught exception when specifying invalid source (RAML) file");
			}
			try {
				RamlUtil.main(noArgs);
				fail("Expected exception - no arguments specified");
			} catch (Exception e) {
				System.out.println("Good - caught exception when arguments were not specified");
			}
			try {
				RamlUtil.main(fileNotFoundArgs);
				fail("Expected exception - specified source file is not found");
			} catch (Exception e) {
				System.out.println("Good - caught exception when specified source file was not found");
			}
			
			System.out.println("Test WADL.xmlType...");
			ParamType[] paramTypes = { ParamType.BOOLEAN, ParamType.DATE, ParamType.FILE, ParamType.INTEGER, ParamType.NUMBER, ParamType.STRING };
			for (ParamType paramType : paramTypes) {
				WADL.xmlType(paramType);
			}
			
			System.out.println("Test JsonUtil...");
			
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("A", 1);
			map.put("B", 2);
			map.put("C", 3);
			
			int[] array = { 1, 2, 3 };
			
			String arrayJson = JsonUtil.serialize(array);
			String mapJson  = JsonUtil.serialize(map);
			String fileJson = JsonUtil.serialize(refFile);
			
			assertTrue("Identical JSON (map) did not match!", JsonUtil.equals(mapJson, mapJson));
			assertFalse("Differing JSON matched!", JsonUtil.equals(mapJson,  fileJson));
			assertTrue("Identical JSON (array) did not match!", JsonUtil.equals(arrayJson,  arrayJson));
			
		} catch(Exception e) {
			fail(e.getMessage());
		} finally {
		}
	}

	/**
	 * Test method for {@link com.ca.dvs.utilities.raml.RamlUtil#RamlUtil()}.
	 */
	@Test
	public final void testRamlMultikey2EDM() {
		
		String[] edmFileArgs           = { "test/resources/novo/RegistrationManagerAPI.raml", "EDM", "junitMultiKey.edm" };
	
		try {

			System.out.println("Generate multi-key EDM file (junitMultiKey.edm)...");
			RamlUtil.main(edmFileArgs);
			File refFile = new File("test/resources/novo/RegistrationManagerAPI.edm");
			File outFile = new File(edmFileArgs[2]);
			compareFiles(refFile, outFile);
			outFile.delete();
			
		} catch(Exception e) {
		
			fail(e.getMessage());
			
		} finally {
		}
	}

	/**
	 * Test method for {@link com.ca.dvs.utilities.raml.RamlUtil#RamlUtil()}.
	 */
	@Test
	public final void testRaml2VSI() {
		
		String[] odataArgs	= { "test/resources/jug/jug_odata.raml", "VSI", "junit_jug_odata.vsi" };
		String[] restArgs	= { "test/resources/jug/jug_rest.raml", "VSI", "junit_jug_rest.vsi" };
	
		try {

			System.out.println("Generate VSI file (jug_odata.vsi) from OData RAML...");
			RamlUtil.main(odataArgs);
			File outFile = new File(odataArgs[2]);
			// Can't compare results.  VSI file has time stamp attributes that will never match
			assertTrue(String.format("Failed to generate VSI file from OData RAML - %s", outFile.getAbsolutePath()), outFile.canRead());
			outFile.delete();
			
			System.out.println("Generate VSI file (jug_rest.vsi) from REST RAML...");
			RamlUtil.main(restArgs);
			outFile = new File(restArgs[2]);
			// Can't compare results.  VSI file has time stamp attributes that will never match
			assertTrue(String.format("Failed to generate VSI file from REST RAML - %s", outFile.getAbsolutePath()), outFile.canRead());
			outFile.delete();
			
		} catch(Exception e) {
		
			fail(e.getMessage());
			
		} finally {
		}
	}
	
	/**
	 * Test method for {@link com.ca.dvs.utilities.raml.RamlUtil#RamlUtil()}.
	 */
	@Test
	public final void testGetSampleData() {
		
		try {

			System.out.println("Extract sample data Map from OData RAML...");
			
			File ramlFile = new File("test/resources/jug/jug_odata.raml");
			
			if (ramlFile.canRead()) {
				
				//
				// Validate the RAML file
				//
				List<ValidationResult> results = RamlValidationService.createDefault().validate(ramlFile.getPath());

				// If the RAML file is valid, get to work...
				if (ValidationResult.areValid(results)) {

					FileInputStream		ramlFileStream	= new FileInputStream(ramlFile.getAbsolutePath());
					try {
						FileResourceLoader 	resourceLoader	= new FileResourceLoader(ramlFile.getParentFile());
						RamlDocumentBuilder rdb         	= new RamlDocumentBuilder(resourceLoader);
						Raml				raml			= rdb.build(ramlFileStream, ramlFile.getAbsolutePath());
	
						if (null!=raml) {
	
							Map<String, Map<String, Object>> sampleData = RamlUtil.getSampleData(raml, ramlFile.getParentFile());
							
							assertNotNull(String.format("Failed to extract sample data from OData RAML - %s", ramlFile.getAbsolutePath()), sampleData);
							assertFalse(String.format("Failed to extract sample data from OData RAML - %s", ramlFile.getAbsolutePath()), sampleData.isEmpty());
	
							sampleData = RamlUtil.getSampleData(raml);
							
							assertNotNull(String.format("Failed to extract sample data from OData RAML - %s", ramlFile.getAbsolutePath()), sampleData);
							assertFalse(String.format("Failed to extract sample data from OData RAML - %s", ramlFile.getAbsolutePath()), sampleData.isEmpty());
	
						}
					} finally {
						ramlFileStream.close();
					}
				} else { // invalid RAML file

					StringBuffer sb = new StringBuffer();

					sb.append("Error(s) parsing RAML file: "+ramlFile.getName()+"\n");

					for (ValidationResult result : results) {

						sb.append(String.format("line %d: (%d-%d): %s\n", result.getLine()+1, result.getStartColumn(), result.getEndColumn(), result.getMessage()));

					}
					throw new Exception(sb.toString());
				}
			} else {

				String msg = "Cannot read file: "+ramlFile.getPath();
				throw new Exception(msg);

			}
			
		} catch(Exception e) {
		
			fail(e.getMessage());
			
		} finally {
		}
	}

	/**
	 * Test method for {@link com.ca.dvs.utilities.raml.RamlUtil#RamlUtil()}.
	 */
	@Test
	public final void testGetSampleJson() {
		
		try {

			System.out.println("Extract JSON sample data from OData RAML...");
			
			File ramlFile = new File("test/resources/jug/jug_odata.raml");
			
			if (ramlFile.canRead()) {
				
				//
				// Validate the RAML file
				//
				List<ValidationResult> results = RamlValidationService.createDefault().validate(ramlFile.getPath());

				// If the RAML file is valid, get to work...
				if (ValidationResult.areValid(results)) {

					FileInputStream		ramlFileStream	= new FileInputStream(ramlFile.getAbsolutePath());
					try {
						FileResourceLoader 	resourceLoader	= new FileResourceLoader(ramlFile.getParentFile());
						RamlDocumentBuilder rdb         	= new RamlDocumentBuilder(resourceLoader);
						Raml				raml			= rdb.build(ramlFileStream, ramlFile.getAbsolutePath());
	
						if (null!=raml) {
	
							String json = RamlUtil.getSampleJson(raml, ramlFile.getParentFile());
							
							assertNotNull(String.format("Failed to extract sample JSON from OData RAML - %s", ramlFile.getAbsolutePath()), json);
							assertFalse(String.format("Failed to extract sample JSON from OData RAML - %s", ramlFile.getAbsolutePath()), json.isEmpty());
	
						}
					} finally {
						ramlFileStream.close();
					}

				} else { // invalid RAML file

					StringBuffer sb = new StringBuffer();

					sb.append("Error(s) parsing RAML file: "+ramlFile.getName()+"\n");

					for (ValidationResult result : results) {

						sb.append(String.format("line %d: (%d-%d): %s\n", result.getLine()+1, result.getStartColumn(), result.getEndColumn(), result.getMessage()));

					}
					throw new Exception(sb.toString());
				}
			} else {

				String msg = "Cannot read file: "+ramlFile.getPath();
				throw new Exception(msg);

			}
			
		} catch(Exception e) {
		
			fail(e.getMessage());
			
		} finally {
		}
	}

	/**
	 * Test method for {@link com.ca.dvs.utilities.raml.RamlUtil#RamlUtil()}.
	 */
	@Test
	public final void testGetSchemaJson() {
		
		try {

			System.out.println("Extract JSON sample data from OData RAML...");
			
			File ramlFile = new File("test/resources/jug/jug_odata.raml");
			
			if (ramlFile.canRead()) {
				
				//
				// Validate the RAML file
				//
				List<ValidationResult> results = RamlValidationService.createDefault().validate(ramlFile.getPath());

				// If the RAML file is valid, get to work...
				if (ValidationResult.areValid(results)) {

					FileInputStream		ramlFileStream	= new FileInputStream(ramlFile.getAbsolutePath());
					try {
						FileResourceLoader 	resourceLoader	= new FileResourceLoader(ramlFile.getParentFile());
						RamlDocumentBuilder rdb         	= new RamlDocumentBuilder(resourceLoader);
						Raml				raml			= rdb.build(ramlFileStream, ramlFile.getAbsolutePath());
	
						if (null!=raml) {
	
							String json = RamlUtil.getSchemaJson(raml, ramlFile.getParentFile());
							
							assertNotNull(String.format("Failed to extract sample JSON from OData RAML - %s", ramlFile.getAbsolutePath()), json);
							assertFalse(String.format("Failed to extract sample JSON from OData RAML - %s", ramlFile.getAbsolutePath()), json.isEmpty());
	
						}
					} finally {
						ramlFileStream.close();
					}
				} else { // invalid RAML file

					StringBuffer sb = new StringBuffer();

					sb.append("Error(s) parsing RAML file: "+ramlFile.getName()+"\n");

					for (ValidationResult result : results) {

						sb.append(String.format("line %d: (%d-%d): %s\n", result.getLine()+1, result.getStartColumn(), result.getEndColumn(), result.getMessage()));

					}
					throw new Exception(sb.toString());
				}
			} else {

				String msg = "Cannot read file: "+ramlFile.getPath();
				throw new Exception(msg);

			}
			
		} catch(Exception e) {
		
			fail(e.getMessage());
			
		} finally {
		}
	}
}
