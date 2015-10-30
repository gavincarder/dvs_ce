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

import com.ca.casd.utilities.commonUtils.CommonDefs;
import com.ca.casd.utilities.commonUtils.util.DefaultTransactionUtil;
import com.ca.casd.utilities.commonUtils.util.EnumResourceObjectType;

public class DefaultTransactionUtilTest {

	static String edmFile = "test/resources/data/vPubEDMDB.xml";  
	private final String strHttp = "http://";
	private final String strLisaHost = "localhost";
	private final String strPortNumber = "80";	
	private final String strBasePath = "/Odata/vPub/v1";	
	
	private DefaultTransactionUtil transationUtil = null;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		MetadataParser cmsmdParser = new MetadataParser();
		Metadata metadata = cmsmdParser.parseXmlFile(edmFile);
		if (metadata != null)
			transationUtil = new DefaultTransactionUtil(metadata);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void testDefaultTransactionUtil() {
		if (transationUtil == null)
			fail("cannot to get DefaultTransactionUtil");		
	}

	@Test
	public final void testBuildResponseTemplate_Collection() {
		if (transationUtil == null)
			return;

		String strMethod = "GET";
		EnumResourceObjectType objectType = EnumResourceObjectType.COLLECTION;
		String strResourcePath = "/Beers";
		String edmTypeName = "Beer";
		String odataVersion = CommonDefs.VALUE_ODATA_VERSION_3;
		
		String template = transationUtil.buildResponseTemplate(strHttp, strLisaHost, 
					strPortNumber, strBasePath, strMethod, strResourcePath, objectType, edmTypeName, odataVersion);
		
		if (template.isEmpty()) {
			fail("no template for Collection");			
		}
		else if (!template.contains("odata.metadata")) {
			fail("'odata.metadata' is missed in the template for Element");		
		}
		else if (!template.contains("@@LIST") || !template.contains("@@END")){
			fail("missed '@@LIST' or '@@END' in the template for Collection");		
		}
		
		System.out.println(template);
		
	}

	@Test
	public final void testBuildResponseTemplate_Collection_v4() {
		if (transationUtil == null)
			return;

		String strMethod = "GET";
		EnumResourceObjectType objectType = EnumResourceObjectType.COLLECTION;
		String strResourcePath = "/Beers";
		String edmTypeName = "Beer";
		String odataVersion = CommonDefs.VALUE_ODATA_VERSION_4;
		
		String template = transationUtil.buildResponseTemplate(strHttp, strLisaHost, 
					strPortNumber, strBasePath, strMethod, strResourcePath, objectType, edmTypeName, odataVersion);
		
		if (template.isEmpty()) {
			fail("no template for Collection");			
		}
		else if (!template.contains("@odata.context")) {
			fail("'@odata.context' is missed in the template for Element");		
		}
		else if (!template.contains("@@LIST") || !template.contains("@@END")){
			fail("missed '@@LIST' or '@@END' in the template for Collection");		
		}
		
		System.out.println(template);
		
	}
	
	@Test
	public final void testBuildOdataMetadata_Element() {
		if (transationUtil == null)
			return;

		String strMethod = "GET";
		EnumResourceObjectType objectType = EnumResourceObjectType.ELEMENT;
		String strResourcePath = "/Beers('1')";
		String edmTypeName = "Beer";
		String odataVersion = CommonDefs.VALUE_ODATA_VERSION_3;
		
		String template = transationUtil.buildResponseTemplate(strHttp, strLisaHost, 
					strPortNumber, strBasePath, strMethod, strResourcePath, objectType, edmTypeName, odataVersion);
		
		if (template.isEmpty()) {
			fail("no template for Element");			
		}
		else if (template.contains("@@LIST") || template.contains("@@END")){
			fail("'@@LIST' or '@@END' is in the template for Element");		
		} 
		else if (!template.contains("odata.metadata")) {
			fail("'odata.metadata' is missed in the template for Element");		
		}
		else if (!template.contains("@Element")) {
			fail("'@Element' is missed in the template for Element");		
		}
		
		System.out.println(template);		
	}

	@Test
	public final void testBuildOdataMetadata_Element_v4() {
		if (transationUtil == null)
			return;

		String strMethod = "GET";
		EnumResourceObjectType objectType = EnumResourceObjectType.ELEMENT;
		String strResourcePath = "/Beers('1')";
		String edmTypeName = "Beer";
		String odataVersion = CommonDefs.VALUE_ODATA_VERSION_4;
		
		String template = transationUtil.buildResponseTemplate(strHttp, strLisaHost, 
					strPortNumber, strBasePath, strMethod, strResourcePath, objectType, edmTypeName, odataVersion);
		
		if (template.isEmpty()) {
			fail("no template for Element");			
		}
		else if (template.contains("@@LIST") || template.contains("@@END")){
			fail("'@@LIST' or '@@END' is in the template for Element");		
		} 
		else if (!template.contains("@odata.context")) {
			fail("'@odata.context' is missed in the template for Element");		
		}
		else if (!template.contains("$entity")) {
			fail("'$entity' is missed in the template for Element");		
		}
		
		System.out.println(template);
		
	}
	
	@Test
	public final void testBuildOdataMetadata_Property() {
		if (transationUtil == null)
			return;

		String strMethod = "GET";
		EnumResourceObjectType objectType = EnumResourceObjectType.EDMTYPE;
		String strResourcePath = "/Beers('1')/name";
		String edmTypeName = "Beer";
		String odataVersion = CommonDefs.VALUE_ODATA_VERSION_3;
		
		String template = transationUtil.buildResponseTemplate(strHttp, strLisaHost, 
					strPortNumber, strBasePath, strMethod, strResourcePath, objectType, edmTypeName, odataVersion);
		
		if (template.isEmpty()) {
			fail("no template for Element");			
		}
		else if (!template.contains("odata.metadata")) {
			fail("'odata.metadata' is missed in the template for Element");		
		}
		else if (template.contains("@@LIST") || template.contains("@@END")){
			fail("'@@LIST' or '@@END' is in the template for Element");		
		} 
		else if (!template.contains("EDM.DataType")) {
			fail("'EDM.DataType' is missed in the template for Element");		
		}
		
		System.out.println(template);
		
	}
	
	@Test
	public final void testBuildOdataMetadata_Property_v4() {
		if (transationUtil == null)
			return;

		String strMethod = "GET";
		EnumResourceObjectType objectType = EnumResourceObjectType.EDMTYPE;
		String strResourcePath = "/Beers('1')/name";
		String edmTypeName = "Beer";
		String odataVersion = CommonDefs.VALUE_ODATA_VERSION_4;
		
		String template = transationUtil.buildResponseTemplate(strHttp, strLisaHost, 
					strPortNumber, strBasePath, strMethod, strResourcePath, objectType, edmTypeName, odataVersion);
		
		if (template.isEmpty()) {
			fail("no template for Element");			
		}
		else if (!template.contains("@odata.context")) {
			fail("'@odata.context' is missed in the template for Element");		
		}
		else if (template.contains("@@LIST") || template.contains("@@END")){
			fail("'@@LIST' or '@@END' is in the template for Element");		
		} 
		else if (!template.contains("EDM.DataType")) {
			fail("'EDM.DataType' is missed in the template for Element");		
		}
		
		System.out.println(template);
		
	}

	@Test
	public final void testBuildOdataMetadata_Links_Collection() {
		if (transationUtil == null)
			return;

		String strMethod = "GET";
		EnumResourceObjectType objectType = EnumResourceObjectType.COLLECTION;
		String strResourcePath = "/Brewers('1')/$links/Beers";
		String edmTypeName = "Beer";
		String odataVersion = CommonDefs.VALUE_ODATA_VERSION_3;
		
		String template = transationUtil.buildResponseTemplate(strHttp, strLisaHost, 
					strPortNumber, strBasePath, strMethod, strResourcePath, objectType, edmTypeName, odataVersion);
		
		if (template.isEmpty()) {
			fail("no template for Element");			
		}
		else if (!template.contains("odata.metadata")) {
			fail("'odata.metadata' is missed in the template for Element");		
		}
		else if (!template.contains("@@LIST") || !template.contains("@@END")){
			fail("missed '@@LIST' or '@@END' in the template for Collection");		
		}
		else if (!template.contains("$links")) {
			fail("$links' is missed in the template for link");		
		}
		
		System.out.println(template);
		
	}
	
	@Test
	public final void testBuildOdataMetadata_Ref_Collection() {
		if (transationUtil == null)
			return;

		String strMethod = "GET";
		EnumResourceObjectType objectType = EnumResourceObjectType.COLLECTION;
		String strResourcePath = "/Brewers('1')/Beers/$ref";
		String edmTypeName = "Beer";
		String odataVersion = CommonDefs.VALUE_ODATA_VERSION_4;
		
		String template = transationUtil.buildResponseTemplate(strHttp, strLisaHost, 
					strPortNumber, strBasePath, strMethod, strResourcePath, objectType, edmTypeName, odataVersion);
		
		if (template.isEmpty()) {
			fail("no template for Element");			
		}
		else if (!template.contains("@odata.context")) {
			fail("'@odata.context' is missed in the template for Element");		
		}
		else if (!template.contains("@odata.id")) {
			fail("'@odata.id' is missed in the template for Element");		
		}
		else if (!template.contains("@@LIST") || !template.contains("@@END")){
			fail("missed '@@LIST' or '@@END' in the template for Collection");		
		}
		else if (!template.contains("$ref")) {
			fail("$ref' is missed in the template for link");		
		}
		
		System.out.println(template);
		
	}

	@Test
	public final void testBuildOdataMetadata_Link_Element() {
		if (transationUtil == null)
			return;

		String strMethod = "GET";
		EnumResourceObjectType objectType = EnumResourceObjectType.ELEMENT;
		String strResourcePath = "/Beers('1')/$links/Brewer";
		String edmTypeName = "Brewer";
		String odataVersion = CommonDefs.VALUE_ODATA_VERSION_3;
		
		String template = transationUtil.buildResponseTemplate(strHttp, strLisaHost, 
					strPortNumber, strBasePath, strMethod, strResourcePath, objectType, edmTypeName, odataVersion);
		
		if (template.isEmpty()) {
			fail("no template for link");			
		}
		else if (!template.contains("odata.metadata")) {
			fail("'odata.metadata' is missed in the template for Element");		
		}
		else if (template.contains("@@LIST") || template.contains("@@END")){
			fail("'@@LIST' or '@@END' is in the template for Element");		
		} 
		else if (!template.contains("$links")) {
			fail("$links' is missed in the template for link");		
		}
		
		System.out.println(template);
		
	}

	
	@Test
	public final void testBuildOdataMetadata_ref_Element() {
		if (transationUtil == null)
			return;

		String strMethod = "GET";
		EnumResourceObjectType objectType = EnumResourceObjectType.ELEMENT;
		String strResourcePath = "/Beers('1')/Brewer/$ref";
		String edmTypeName = "Brewer";
		String odataVersion = CommonDefs.VALUE_ODATA_VERSION_4;
		
		String template = transationUtil.buildResponseTemplate(strHttp, strLisaHost, 
					strPortNumber, strBasePath, strMethod, strResourcePath, objectType, edmTypeName, odataVersion);
		
		if (template.isEmpty()) {
			fail("no template for link");			
		}
		else if (!template.contains("@odata.context")) {
			fail("'@odata.context' is missed in the template for Element");		
		}
		else if (!template.contains("@odata.id")) {
			fail("'@odata.id' is missed in the template for Element");		
		}
		else if (template.contains("@@LIST") || template.contains("@@END")){
			fail("'@@LIST' or '@@END' is in the template for Element");		
		} 
		else if (!template.contains("$ref")) {
			fail("$ref' is missed in the template for link");		
		}
		
		System.out.println(template);
		
	}
	
}
