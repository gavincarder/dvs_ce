/*******************************************************************************
 * Copyright (c) 2015 CA.  All rights reserved.
 *
 * This source file is licensed under the terms of the Eclipse Public License 1.0
 * For the full text of the EPL please see https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ca.dvs.utilities.lisamar;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ca.casd.utilities.commonUtils.CommonDefs;
import com.ca.casd.utilities.commonUtils.metadata.MetadataParser;

public class JDBCUtilTest {

	static String edmFile = "test/resources/data/EDMToDB.xml";
	MetadataParser cmsmdParser = null;
	
	//JDBCUtil jdbcUtil = null;

	public JDBCUtilTest() {
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		cmsmdParser = new MetadataParser();
		cmsmdParser.parseXmlFile(edmFile);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testJDBCUtil() {		
		JDBCUtil jdbcUtil = getjdbcUtil(CommonDefs.VALUE_ODATA_VERSION_4);
		if (jdbcUtil == null)
			fail("new jdbcUtil(...) failed"); 
	}

	@Test
	public void testCreateDBSchema_odata3() {
		JDBCUtil jdbcUtil = getjdbcUtil(CommonDefs.VALUE_ODATA_VERSION_3);
		if (jdbcUtil == null) {
			fail("testCreateDBSchema - jdbcUtil is NULL"); // TODO
			return;
		}
		
		File tempFile = null;
		try {
			tempFile = File.createTempFile("casd_test","sql");
			tempFile.deleteOnExit(); // assure that we clean up after ourselves
			
			String dbFile = tempFile.getPath(); 
			System.out.println("DB schema: " + dbFile);
			if ( false == jdbcUtil.createDBSchema(dbFile, false))
				fail("JDBCUtil::createDBSchema(...) - failed!");
			else {
				deleteTestFile(dbFile);
			}				
		} catch (IOException e) {
			fail("createTempFile failed - " + e.getMessage());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			fail("createTempFile failed - " + e.getMessage());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			fail("createTempFile failed - " + e.getMessage());
		} 
	}
	
	@Test
	public void testCreateDBSchema_odata4() {
		JDBCUtil jdbcUtil = getjdbcUtil(CommonDefs.VALUE_ODATA_VERSION_4);
		if (jdbcUtil == null) {
			fail("testCreateDBSchema - jdbcUtil is NULL"); // TODO
			return;
		}
		
		File tempFile = null;
		try {
			tempFile = File.createTempFile("casd_test","sql");
			tempFile.deleteOnExit(); // assure that we clean up after ourselves
			String dbFile = tempFile.getPath(); 
			System.out.println("DB schema: " + dbFile);
			if ( false == jdbcUtil.createDBSchema(dbFile, false))
				fail("JDBCUtil::createDBSchema(...) - failed!");
			else {
				deleteTestFile(dbFile);
			}
		} catch (IOException e) {
			fail("createTempFile failed - " + e.getMessage());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			fail("createTempFile failed - " + e.getMessage());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			fail("createTempFile failed - " + e.getMessage());
		} 
	}

	@Test
	public void testCreateDBSchemaNoFile_1() {
		JDBCUtil jdbcUtil = getjdbcUtil(CommonDefs.VALUE_ODATA_VERSION_4);
		if (jdbcUtil == null) {
			fail("testCreateDBSchemaNoFile - jdbcUtil is NULL"); // TODO
			return;
		}
		try {
			try {
				if ( true == jdbcUtil.createDBSchema(null, false)) {
					fail("JDBCUtil::createDBSchema(...) - failed!");
				}
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

	@Test
	public void testCreateDBSchemaNoFile_2() {
		JDBCUtil jdbcUtil = getjdbcUtil(CommonDefs.VALUE_ODATA_VERSION_4);
		if (jdbcUtil == null) {
			fail("testCreateDBSchemaNoFile - jdbcUtil is NULL"); // TODO
			return;
		}
		try {
			if ( true == jdbcUtil.createDBSchema("", false)) {
				fail("JDBCUtil::createDBSchema(...) - failed!");
			}
				
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	private JDBCUtil getjdbcUtil(final String odataversion) {		
		VSMObject vsmobj = new VSMObject("dvsTest");
		vsmobj.setDataStoreURL("");
		vsmobj.setDataStoreType("H2");
		vsmobj.setDatabaseUser("test");
		vsmobj.setDatabasePassword("test");
		vsmobj.setOdataVersion(odataversion);		
		return new JDBCUtil(vsmobj, cmsmdParser.getMetadata() );	
	}
	
	private void deleteTestFile (final String dbFile) {
		File testFile = new File(dbFile);
		if (testFile.isFile() && testFile.exists())
			testFile.delete();
		System.out.println("Deleted DB schema: " + dbFile);
	}
}
