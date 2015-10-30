/*******************************************************************************
 * Copyright (c) 2015 CA.  All rights reserved.
 *
 * This source file is licensed under the terms of the Eclipse Public License 1.0
 * For the full text of the EPL please see https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ca.dvs.utilities.lisamar;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.raml.model.Raml;
import org.raml.parser.loader.FileResourceLoader;
import org.raml.parser.visitor.RamlDocumentBuilder;
import org.w3c.dom.Document;

import com.ca.casd.utilities.commonUtils.CommonDefs;
import com.ca.casd.utilities.commonUtils.log.Log;
import com.ca.dvs.utilities.raml.EDM;
import com.ca.dvs.utilities.raml.RamlUtil;
import com.ca.dvs.utilities.raml.VSI;

public class LisaMarUtilTest {
	
	static String targetPath = "c:/temp"; 
	static String aedmFile = "test/resources/data/EDMToDB.xml"; 
	static String aedmRaml = "test/resources/data/ramledm.xml";
	static String xmlRaml = "test/resources/data/jug_odata.raml";
	static String odata3ServiceName = "dvsTestServiceV3"; 
	static String baseURL = "/odata/api/v3"; 
	static String port = "8853"; 
	static String odataVersion = CommonDefs.VALUE_ODATA_VERSION_3; 

	static String odata4ServiceName = "dvsTestServiceV4"; 
	static String odata4BaseURL = "/odata/api/v4"; 
	static String odata4Port = "8854"; 
	static String odataVersion4 = CommonDefs.VALUE_ODATA_VERSION_4;
	
	static String restServiceName = "dvsRestService"; 
	static String restPort = "8855"; 
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		targetPath = System.getProperty("java.io.tmpdir") + "dvs";
	}

	@After
	public void tearDown() throws Exception {
		DirectoryFileUtil.deleteDirectory(targetPath);
	}

	@Test
	public final void testGenerateLiasProject() {
		//fail("Not yet implemented");
		try {
			String serviceName = odata3ServiceName + "_aedm" + odataVersion; 
			LisaMarObject marObject = LisaMarUtil.generateLisaProject(aedmFile, serviceName, baseURL, port, targetPath, odataVersion, false);
			System.out.println(marObject.getLisafile());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("generateLiasProject failed");
		}
	}
	
	@Test
	public final void testgenerateLisaMarFromDocumentWithSampleData() {
		
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance(); 
        domFactory.setNamespaceAware(false); 

		try {
	        DocumentBuilder builder = domFactory.newDocumentBuilder();
	        Document metadataDoc = builder.parse(aedmFile);

			String serviceName = odata3ServiceName + "_aedm" + odataVersion + "_seeddata"; 
	        Map<String, Map<String, Object>> sampleData = buildSampleData();
	        LisaMarObject marObject = LisaMarUtil.generateLisaMar(metadataDoc, serviceName, baseURL, port, odataVersion, sampleData);
			System.out.println(marObject.getLisafile());
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("generateLisaMar failed");
		}
	}
	
	@Test
	public final void testgenerateLisaMarFromDocumentWithSampleData_odataV4() {
		
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance(); 
        domFactory.setNamespaceAware(false); 

		try {
	        DocumentBuilder builder = domFactory.newDocumentBuilder();
	        Document metadataDoc = builder.parse(aedmFile);

			String serviceName = odata4ServiceName + "_aedm" + odataVersion4 + "_seeddata"; 
	        Map<String, Map<String, Object>> sampleData = buildSampleData();
	        LisaMarObject marObject = LisaMarUtil.generateLisaMar(metadataDoc, serviceName, odata4BaseURL, odata4Port, odataVersion4, sampleData);
			System.out.println(marObject.getLisafile());
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("generateLisaMar failed");
		}
	}
	
	private Map<String, Map<String, Object>> buildSampleData () {
		
		/****Brewer************/
		Map<String,Object> brewer1 = new LinkedHashMap<String, Object>();
		brewer1.put("Id", "31442b00-47c4-11e3-8f96-0800200c9a66");
		brewer1.put("Name", "21st Amendment Brewery");
		brewer1.put("Description", "Description - 21st Amendment Brewery");
		
		Map<String, Object> brewer2 = new LinkedHashMap<String, Object>();
		brewer2.put("Id", "31442b01-47c4-11e3-8f96-0800200c9a66");
		brewer2.put("Name", "Avery Brewing Company");
		brewer2.put("Description", "Description - Avery Brewing Company");
		
		List<Map<String, Object>> brewers = new ArrayList<Map<String, Object>>();
		brewers.add(brewer1);
		brewers.add(brewer2);
		
		Map<String, Object> exampleBrewer = new LinkedHashMap<String, Object>();
		exampleBrewer.put("size", brewers.size());
		exampleBrewer.put("Brewers", brewers);
		
		Map<String, Object> brewerData = new LinkedHashMap<String, Object>();
		brewerData.put("schema", "Brewers");
		brewerData.put("example", exampleBrewer);
		
		/****Beer************/		
		Map<String, Object> beer1 = new LinkedHashMap<String, Object>();
		beer1.put("Id", "f598195e-b6ee-4790-a96a-f12a3ca5c3cf");
		beer1.put("Brewer_Id", "31442b00-47c4-11e3-8f96-0800200c9a66");
		beer1.put("Name", "21st Ammendment Hop Crisis!");
		beer1.put("Type", "IPA");
		beer1.put("ABV", 9.7);
		beer1.put("Serving", "12 oz. Draft");
		beer1.put("Price", 8);
		beer1.put("Description", "Hop Crisis. Crisis? What Crisis? A few years ago, when hop prices shot through the roof and the worldwide hop market went into a tailspin, at our pub in San Francisco we decided there was only one thing for us to do. We made the biggest, hoppiest IPA we could imagine and aged it on oak for good measure. This Imperial IPA breaks all the rules with more malt, more hops and more aroma.");
		
		Map<String, Object> beer2 = new LinkedHashMap<String, Object>();
		beer2.put("Id", "6f11fe00-d22a-46f0-887a-e4c13a90cfcd");
		beer2.put("Brewer_Id", "31442b01-47c4-11e3-8f96-0800200c9a66");
		beer2.put("Name", "Avery Brewing Company");
		beer2.put("Type", "IPA");
		beer2.put("ABV", 9.7);
		beer2.put("Serving", "12 oz. Draft");
		beer2.put("Price", 8);
		beer2.put("Description", "Hop Crisis. Crisis? What Crisis? A few years ago, when hop prices shot through the roof and the worldwide hop market went into a tailspin, at our pub in San Francisco we decided there was only one thing for us to do. We made the biggest, hoppiest IPA we could imagine and aged it on oak for good measure. This Imperial IPA breaks all the rules with more malt, more hops and more aroma.");
		
		List<Map<String, Object>> beers = new ArrayList<Map<String, Object>>();
		beers.add(beer1);
		beers.add(beer2);
		
		Map<String, Object> exampleBeer = new LinkedHashMap<String, Object>();
		exampleBeer.put("size", beers.size());
		exampleBeer.put("Beers", beers);
		
		Map<String, Object> beerData = new LinkedHashMap<String, Object>();
		beerData.put("schema", "Beers");
		beerData.put("example", exampleBeer);
		
		Map<String, Map<String, Object>> sampleData = new LinkedHashMap<String, Map<String, Object>>();
		sampleData.put("/Brewers", brewerData);
		sampleData.put("/Beers", beerData);
		
		return sampleData;
	}

	@Test
	public final void testgenerateLisaMarFromDocumentWithRaml() {
		
        boolean bOK = true;
        FileInputStream  ramlFileStream = null;
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance(); 
        domFactory.setNamespaceAware(false); 

		try {
			File  ramlFile	= new File(xmlRaml);			
			ramlFileStream	= new FileInputStream(ramlFile.getAbsolutePath());			
			FileResourceLoader 	resourceLoader	= new FileResourceLoader(ramlFile.getParentFile());
			RamlDocumentBuilder rdb         	= new RamlDocumentBuilder(resourceLoader);
			Raml				raml			= rdb.build(ramlFileStream, ramlFile.getAbsolutePath());
			
			Document docAEDM = null;		
			EDM edm = new EDM(raml);
			docAEDM = edm.getDocument();			
			
			Map<String, Map<String, Object>> sampleData = RamlUtil.getSampleData(raml, ramlFile.getParentFile());
			
			String serviceName = odata3ServiceName + "_raml" + odataVersion; 
	        LisaMarObject marObject = LisaMarUtil.generateLisaMar(docAEDM, serviceName, baseURL, port, odataVersion, sampleData);
			System.out.println(marObject.getLisafile());
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			bOK = false;
		}
		finally {
			safeClose(ramlFileStream);
		}
		
		if (!bOK)
			fail("generateLisaMar failed");
	}
	
	@Test
	public final void testgenerateLisaMarFromDocumentWithRaml_v4() {
		
        boolean bOK = true;
        FileInputStream  ramlFileStream = null;
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance(); 
        domFactory.setNamespaceAware(false); 
		try {
			File  ramlFile	= new File(xmlRaml);			
			ramlFileStream	= new FileInputStream(ramlFile.getAbsolutePath());			
			FileResourceLoader 	resourceLoader	= new FileResourceLoader(ramlFile.getParentFile());
			RamlDocumentBuilder rdb         	= new RamlDocumentBuilder(resourceLoader);
			Raml				raml			= rdb.build(ramlFileStream, ramlFile.getAbsolutePath());
			
			Document docAEDM = null;		
			EDM edm = new EDM(raml);
			docAEDM = edm.getDocument();			
			
			Map<String, Map<String, Object>> sampleData = RamlUtil.getSampleData(raml, ramlFile.getParentFile());
			
			String serviceName = odata4ServiceName + "_raml" + odataVersion4;
	        LisaMarObject marObject = LisaMarUtil.generateLisaMar(docAEDM, serviceName, odata4BaseURL, odata4Port, odataVersion4, sampleData);
			System.out.println(marObject.getLisafile());
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			bOK = false;
		}
		finally {
			safeClose(ramlFileStream);
		}
		
		if (!bOK)
			fail("generateLisaMar failed");
	}

	@Test
	public final void testGenerateRestLiasMarFromDocumentWithRaml() {
		
        boolean bOK = true;
        FileInputStream  ramlFileStream = null;
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance(); 
        domFactory.setNamespaceAware(false); 

		try {
			File ramlFile						= new File("test/resources/data/jug_rest.raml");
			ramlFileStream						= new FileInputStream(ramlFile.getAbsolutePath());			
			FileResourceLoader 	resourceLoader	= new FileResourceLoader(ramlFile.getParentFile());
			RamlDocumentBuilder rdb         	= new RamlDocumentBuilder(resourceLoader);
			Raml raml							= rdb.build(ramlFileStream, ramlFile.getAbsolutePath());
			
			VSI vsi = new VSI(raml, ramlFile.getParentFile());
			Document vsidoc = vsi.getDocument();
			
	        LisaMarObject marObject = LisaMarUtil.generateRestLisaMar(restServiceName, raml.getBasePath(), restPort, vsidoc, vsi.getOperationsList(raml.getResources()));
			System.out.println(marObject.getLisafile());
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			bOK = false;
		}
		finally {
			safeClose(ramlFileStream);
		}
		
		if (!bOK)
			fail("generateLisaMar failed");
	}

	@Test
	public final void testGenerateRestLiasProjectFromDocumentWithRaml() {
		
        boolean bOK = true;
        FileInputStream  ramlFileStream = null;
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance(); 
        domFactory.setNamespaceAware(false); 

		try {
			File ramlFile						= new File("test/resources/data/jug_rest.raml");
			ramlFileStream						= new FileInputStream(ramlFile.getAbsolutePath());			
			FileResourceLoader 	resourceLoader	= new FileResourceLoader(ramlFile.getParentFile());
			RamlDocumentBuilder rdb         	= new RamlDocumentBuilder(resourceLoader);
			Raml raml							= rdb.build(ramlFileStream, ramlFile.getAbsolutePath());
			
			VSI vsi = new VSI(raml, ramlFile.getParentFile());
			Document vsidoc = vsi.getDocument();
			
	        LisaMarObject marObject = LisaMarUtil.generateRestLisaProject(restServiceName, raml.getBasePath(), restPort, targetPath, vsidoc, vsi.getOperationsList(raml.getResources()), false);
			System.out.println(marObject.getLisafile());
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			bOK = false;
		}		
		finally {
			safeClose(ramlFileStream);
		}
		
		if (!bOK)
			fail("generateLisaProject failed");
	}

	public static void safeClose(FileInputStream fis) {
		if (fis != null) {
		    try {
		      fis.close();
		    } 
		    catch (IOException e) {
				Log.write().error(e.getMessage());
		    }
	  	}
	}
	
}
