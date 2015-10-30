/*******************************************************************************
 * Copyright (c) 2015 CA.  All rights reserved.
 *
 * This source file is licensed under the terms of the Eclipse Public License 1.0
 * For the full text of the EPL please see https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ca.dvs.utilities.lisamar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.ca.casd.utilities.commonUtils.log.Log;
import com.ca.casd.utilities.commonUtils.log.LogException;
import com.ca.casd.utilities.commonUtils.metadata.EntityType;
import com.ca.casd.utilities.commonUtils.metadata.Metadata;

/**
 * LisaMarUtil Class
 * <p>
 * - Generates DevTest Mar or project for OData Service v3/v4 based on AEDM
 * <p>
 * - Generates DevTest Mar or project for OData Service v3/v4 based on Raml(Odata)
 * <p>
 * - Generates DevTest Mar or project for REST Service based on Raml(Rest)
 * 
 * @author gonbo01
 *
 */

public class LisaMarUtil {

	private static String DEFAULT_AEDM_NAME = "EDMToDB.xml";
	private static String DEFAULT_DVS_FOLDER = "dvs";
	
	//Used to store the errors/warnings when generating VS model 
	public static Map<String, Object> mapErrors = new LinkedHashMap<String, Object>();
	public static Map<String, Object> mapWarnings = new LinkedHashMap<String, Object>();
	
	private static final String LOG_PATH_FILE_NAME = System.getProperty("user.home")
			  + System.getProperty("file.separator")
			  + "ca"
			  + System.getProperty("file.separator")
			  + "com.ca.dvs.utilities.lisamar"
			  + System.getProperty("file.separator")
			  + "logs"
			  + System.getProperty("file.separator")
			  + "default.log";

	private static final String LOG_LAYOUT_PATTERN = "%d{ISO8601}, %p, %m, %C(%L) %n";

	/**
	 * Generates the lisa MAR file of OData Service based on the specified information, 
	 * also put the sample data into the schema generated if the sample data are provided, 
	 * also returns the errors or warnings during the generation
	 * The aedmFile argument must specify an absolute path
	 * 
	 * @param aedmFile 		AEDM file including full path
	 * @param serviceName 	Service Name for DVS
	 * @param baseURL 		Base url for DVS
	 * @param port 			Port# for DVS 
	 * @param odataVersion 	odata version (3 or 4)
	 * @param sampleData	sample data
	 * @return 				LisaMarObject including the mar file name with full path, errors and warnings during the generation
	 * @see					LisaMarObject
	 * @throws Exception
	 */
	public static LisaMarObject generateLisaMar(Document metadataDoc, String serviceName, String baseURL, String port, String odataVersion, Map<String, Map<String, Object>> sampleData) throws Exception {		
		String destPath = getSystemTempDirectory() + DEFAULT_DVS_FOLDER;
		String aedmFile = writeDomToFile(metadataDoc);    		
    	Metadata metadata = parseXmlDocument(metadataDoc);   	
    	return generateLisaProject(aedmFile, metadata, serviceName, baseURL, port, destPath, odataVersion, sampleData, true);
	}
	
	/**
	 * generate lisa MAR or Project ZIP file of OData Service based on the specified information,
	 * 
	 * @param aedmFile 		AEDM file including full path
	 * @param serviceName 	Service Name for DVS
	 * @param baseURL 		Base url for DVS
	 * @param port 			Port# for DVS	
	 * @param destPath 		the location to store Lisa mar file or project zip file 	
	 * @param odataVersion 	odata version (3 or 4)
	 * @param isMar			true: generate a lisa mar 
	 * @return 				the file of mar or Lis_project_zip with full path 
	 * @throws Exception
	 */
	public static LisaMarObject generateLisaProject(String aedmFile, String serviceName, String baseURL, String port, String destPath, String odataVersion, boolean isMar) throws Exception {
		Metadata metadata = parseXmlFile(aedmFile);
    	return generateLisaProject(aedmFile, metadata, serviceName, baseURL, port, destPath, odataVersion, null, isMar);		
	}

	/**
	 * Generates lisa MAR or Project ZIP file of OData Service based on the specified information
	 * 
	 * @param aedmFile 		AEDM file including full path
	 * @param Metadata 		AEDM metadata
	 * @param serviceName 	Service Name for DVS
	 * @param baseURL 		Base url for DVS
	 * @param port 			Port# for DVS	
	 * @param destPath 		the location to store Lisa mar file	
	 * @param odataVersion 	odata version (3 or 4)
	 * @param sampleData 	sample data
	 * @param isMar			true: generate a lisa mar 
	 * @return 				the file of mar or Lis_project_zip with full path 
	 * @throws Exception
	 */
	 public static LisaMarObject generateLisaProject(String aedmFile, Metadata metadata, String serviceName, String baseURL, String port, String destPath, String odataVersion, Map<String, Map<String, Object>> sampleData, boolean isMar) throws Exception {
		
		init();
		
		String lisafile = "";		
		String msgString = "";
		VSMObject vsmobj = new VSMObject(serviceName.trim());
		vsmobj.setDataStoreURL("");
		vsmobj.setDataStoreType("H2");
		vsmobj.setDatabaseUser("test");
		vsmobj.setDatabasePassword("test");
		vsmobj.setHttpBaseURL(baseURL.trim());
		vsmobj.setHttpPort(port.trim());
		vsmobj.setVSMPath(destPath.trim());
		vsmobj.setEDMFile(aedmFile.trim());
		vsmobj.setOdataVersion(odataVersion);		
		
		//VSI should be always in the folder {{LISA_PROJ_ROOT}}/VServices/Images/
		String vsiFile = "{{LISA_PROJ_ROOT}}/VServices/Images/" + vsmobj.getVSMName() + ".vsi"; 
    	vsmobj.setVSIFile(vsiFile);

		LisaProjectUtil projUtil = new LisaProjectUtil(); 
		if (sampleData != null)
			projUtil.setSampledata(sampleData);
		
		if (isMar) 
			lisafile = projUtil.makeLisaMar(vsmobj, metadata);
		else
			lisafile = projUtil.makeLisaProject(vsmobj, metadata);
		
		String logFile = Log.getLogPathFileName();
   	    if (lisafile.isEmpty()) {
    		msgString = "Failed to save the project of VS Model, please check the log file for detail\n" + logFile;
    		throw new Exception(msgString);
   	    }
    	else {   	
    		msgString = "The project of VS Model has been saved as " + lisafile + ". Please check the log file for detail\n" + logFile;
    		System.out.println(msgString);
    	}
   	    
		return setResultObject(lisafile);
		
	}

	/**
	 * Generates lisa MAR or Project ZIP file of OData Service based on the specified information
	 * 
	 * @param vsmobj		The information of VS Object
	 * @param Metadata 		AEDM metadata
	 * @param sampleData	sample data
	 * @param isMar			true if generating a DevTest mar
	 * @return				the file of mar or Lis_project_zip with full path
	 * @throws Exception
	 */
	 public static LisaMarObject generateLisaProject(VSMObject vsmobj, Metadata metadata, Map<String, Map<String, Object>> sampleData, boolean isMar) throws Exception {

		 init();
		 
		 String lisafile = "";		
		 LisaProjectUtil projUtil = new LisaProjectUtil(); 
		 if (sampleData != null)
			 projUtil.setSampledata(sampleData);
		
		 if (isMar) 
			 lisafile = projUtil.makeLisaMar(vsmobj, metadata);
		 else
			 lisafile = projUtil.makeLisaProject(vsmobj, metadata);

		 return setResultObject(lisafile);

	}
	 
	
	/**
	 * Writes AEDM Document into file
	 * 
	 * @param  - the document of AEDM
	 * @return - output file name with the full path
	 * @throws Exception 
	 */
	public static String writeDomToFile(Document document) throws Exception {
		String xmlOutputFilePath = getSystemTempDirectory() + "/" + DEFAULT_AEDM_NAME;
		if (DirectoryFileUtil.exists(xmlOutputFilePath))
			DirectoryFileUtil.deleteDirectory(xmlOutputFilePath);
		
		XMLDomUtil.writeXmlFile(xmlOutputFilePath, document);
		return xmlOutputFilePath;
	}
	
	/**
	 * Parse the specified AEDM file 
	 * @param fileName - source file formatted as AEDM
	 * @return Metadata
	 * @throws Exception
	 */
	private static Metadata parseXmlFile(String fileName) throws Exception{

    	if (null==fileName || fileName.isEmpty()) {
    		throw new IllegalArgumentException("no fileName specified");
    	}
    	
        File xmlFile = new File(fileName);
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance(); 
        domFactory.setNamespaceAware(false);
        
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document metadataDoc = builder.parse(xmlFile);
        
        return parseXmlDocument(metadataDoc);
            
    }
	
	/**
	 * Parse the specified document 
	 * @param metadataDoc - - source document formatted as AEDM
	 * @return Metadata
	 * @throws Exception
	 */
	private static Metadata parseXmlDocument(Document metadataDoc) throws Exception{

		String errMsg;		
		Metadata metadata;
		metadata = new Metadata(metadataDoc);
		
 		try {
 	        boolean bValided = metadata.parse();
 	        if (!bValided) {
 	       		throw new IllegalArgumentException("Found error(s) in metadataDoc");            	
 	        }
 
            // create custom entity types(and their custom properties), and  entity sets for any entitytype
          	// that supports custom properties
            ArrayList<EntityType> customETypes = metadata.getCustomSupportedEntityTypes();
            if (customETypes.size() > 0) {
              	for (EntityType cEType : customETypes)  { 	
              		//Create the corresponding Custom EntitySet and Custom EntityType for this entity type
              		metadata.createCustomPropEntitySet(cEType.getName());
                    metadata.createCustomPropEntityType(cEType.getName());       	
              	}
            }            
            return metadata;   
    	} 
		catch (Exception e) {
			// ParserConfigurationException can be thrown by newDocumentBuilder
			if (e instanceof ParserConfigurationException) {
				errMsg = "DocumentBuilderFactory.newDocumentBuilder threw ParserConfigurationException - " + e.getMessage();
			    Log.write().error(errMsg);
			}
			// IOException thrown from builder.parse - non-existent file?
			if (e instanceof IOException) {
				errMsg = "DocumentBuilder.parse(xmlFile) threw IOException - does xmlFile exist? - " + e.getMessage();
			    Log.write().error(errMsg);
			}
			// SAXException builder.parse - invalid XML file
			if (e instanceof SAXException) {
				errMsg = "DocumentBuilder.parse(xmlFile) threw SAXException - does xmlFile contain valid XML? -  " + e.getMessage();
			    Log.write().error(errMsg);
			}

            throw new Exception(e.getMessage(), e);
		}

    }
	
	/**
	 * generate lisa MAR file of REST Service based on the specified information below
	 * 
	 * @param serviceName 	- Service Name for DVS
	 * @param baseURL 		- Base url for DVS
	 * @param port 			- Port# for DVS 
	 * @param vsiDoc 		- document of VSI
	 * @param operations	- list of operations
	 * @return 				- mar file with full path 
	 * @throws Exception
	 */
	public static LisaMarObject generateRestLisaMar(String serviceName, String baseURL, String port, Document vsiDoc, List<String> operations ) throws Exception {		
		String destPath = getSystemTempDirectory() + DEFAULT_DVS_FOLDER;
		return generateRestLisaProject(serviceName, baseURL, port, destPath, vsiDoc, operations, true);
	}

	/**
	 * generate lisa MAR or Project ZIP file of of REST Service based on the specified information below
	 * 
	 * @param serviceName 	- Service Name for DVS
	 * @param baseURL 		- Base url for DVS
	 * @param port 			- Port# for DVS	
	 * @param destPath 		- the location to store Lisa mar file	
	 * @param odataVersion 	- odata version
	 * @param vsiDoc 		- document of VSI
	 * @param operations	- list of operations
	 * @param isMar			- true: generate a lisa mar 
	 * @return 				- the file of mar or Lis_project_zip with full path 
	 * @throws Exception
	 */
	public static LisaMarObject generateRestLisaProject(String serviceName, String baseURL, String port, String destPath, Document vsiDoc, List<String> operations, boolean isMar) throws Exception {
		init();
		
		String lisafile = "";		
		String msgString = "";
		
		if (vsiDoc == null) {
    		msgString = "There is no service image file\n";
    		throw new Exception(msgString);			
		}
		
		VSMObject vsmobj = new VSMObject(serviceName.trim());
		vsmobj.setHttpBaseURL(baseURL.trim());
		vsmobj.setHttpPort(port.trim());
		vsmobj.setVSMPath(destPath.trim());
		
		//VSI should be always in the folder {{LISA_PROJ_ROOT}}/VServices/Images/
		String vsiFile = "{{LISA_PROJ_ROOT}}/VServices/Images/" + vsmobj.getVSMName() + ".vsi"; 
    	vsmobj.setVSIFile(vsiFile);
    	vsmobj.setOperations(operations);
    	vsmobj.setVsiDocment(vsiDoc);

		LisaProjectUtil projUtil = new LisaProjectUtil(); 
		
		if (isMar) 
			lisafile = projUtil.makeRestLisaMar(vsmobj);
		else
			lisafile = projUtil.makeRestLisaProject(vsmobj);
		
		String logFile = Log.getLogPathFileName();
   	    if (lisafile.isEmpty()) {
    		msgString = "Failed to save the project of VS Model, please check the log file for detail\n" + logFile;
    		throw new Exception(msgString);
   	    }
    	else {   	
    		msgString = "The project of VS Model has been saved as " + lisafile + ". Please check the log file for detail\n" + logFile;
    		System.out.println(msgString);
    	}
   	    
		return setResultObject(lisafile);
	}	
	
	/**
	 * set the LisaMarObject
	 * 
	 * @param  lisafile 	the MAR file or the zipped file of the project  	
	 * @return LisaMarObject
	 */
	private static LisaMarObject setResultObject(final String lisafile) {
		
		LisaMarObject resultObject = new LisaMarObject();
   	    resultObject.setLisafile(lisafile);  	    
   	    
   	    if (mapErrors.size() > 0)
   	    	resultObject.setErrors(mapErrors);
   	    
   	    if (mapWarnings.size() > 0)
  	    	resultObject.setWarnings(mapWarnings);
   	    
   	    return resultObject;

	}

	/**
	 * initialize
	 * 
	 */
	private static void init() {
		
		mapErrors.clear();
		mapWarnings.clear();
		
		try {
			Log.setLogPathFileName(LOG_PATH_FILE_NAME);
			Log.setLogLayoutPattern(LOG_LAYOUT_PATTERN);
		} catch (LogException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	 
	}
	
	/**
	 * Returns system temp directory
	 * 
	 */
	private static String getSystemTempDirectory() {		
		return System.getProperty("java.io.tmpdir");	
	}
	
	
}
