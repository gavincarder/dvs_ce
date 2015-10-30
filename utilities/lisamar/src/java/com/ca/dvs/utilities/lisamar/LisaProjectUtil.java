/*******************************************************************************
 * Copyright (c) 2015 CA.  All rights reserved.
 *
 * This source file is licensed under the terms of the Eclipse Public License 1.0
 * For the full text of the EPL please see https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ca.dvs.utilities.lisamar;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.NoSuchFileException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.ca.casd.utilities.commonUtils.CommonDefs;
import com.ca.casd.utilities.commonUtils.log.Log;
import com.ca.casd.utilities.commonUtils.metadata.Metadata;

/**
 * LisaProjectUtil Class
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

public class LisaProjectUtil {

	private static String 		ODATA_ODME_VSM_TEMPLATE = "ODME_VSMtemplate.xml";
	private static String 		ODATA_ODME_VSI_TEMPLATE = "ODME_VSItemplate.xml";
	private static String 		REST_ODME_VSM_TEMPLATE  = "REST_VSMtemplate.xml";
	private static String 		LISA_PROJECT_TEMPLATE = "LisaProject.xml";
	
	private static VSModelUtil  vsmInstance = VSModelUtil.getInstance(); 	
	private static VSImageUtil  vsiInstance = VSImageUtil.getInstance(); 
	private Metadata 			metadata = null;
	private Map<String, Map<String, Object>> sampleData = null;	// this sample data should be formated as Map<String, Map<String, Object>>
	
	/**
	 * Generates a DevTest project based on AEDM and zip the project into a zip file for ODATA Service
	 * 
	 * @param vsmobj object of VS information 
	 * @param metadata AEDM model 
	 * @see VSMObject
	 * @return the zip file with the full path
	 */
	public String makeLisaProject (final VSMObject vsmobj, Metadata metadata) {

		String zippedFile = "";
		setMetadata(metadata);
		
		String vsmFolder = vsmobj.getVSMPath() + File.separator + vsmobj.getVSMName();
		String projRoot = vsmFolder + File.separator + vsmobj.getVSMName();
		
		try {
			
			// Clean up, we need to delete the existing vcmFolder first
			if (DirectoryFileUtil.exists(vsmFolder))
				DirectoryFileUtil.deleteDirectory(vsmFolder);
			
			if (false == buildLisaProject(vsmobj, projRoot, false))
				return zippedFile;
			
			zippedFile = DirectoryFileUtil.zipDirectory(vsmFolder, vsmobj.getVSMPath(), "zip");
		
			DirectoryFileUtil.deleteDirectory(vsmFolder);
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
			Log.write().error(e.getMessage());
		}
		
		return zippedFile;
		
	}
	
	/**
	 * Generates a DevTest MAR file based on the AEDM for ODATA Service
	 * 
	 * @param vsmobj the information of VS Object 
	 * @param metadata AEDM model 
	 * @see VSMObject
	 * @return the mar file with the full path
	 */
	public String makeLisaMar (final VSMObject vsmobj, Metadata metadata) {

		String zippedFile = "";
		
		setMetadata(metadata);
		
		String vsmFolder = vsmobj.getVSMPath() + File.separator + vsmobj.getVSMName();
		String projRoot = vsmFolder + File.separator + vsmobj.getVSMName();
		
		try {
			
			// Clean up, we need to delete the existing vcmFolder first
			if (DirectoryFileUtil.exists(vsmFolder))
				DirectoryFileUtil.deleteDirectory(vsmFolder);
			
			// Lisa project first
			boolean bOK = buildLisaProject(vsmobj, projRoot, true);
			
			// create a mar audit file in <ProjectRoot> for Lisa Mar
			bOK &= createMaraudit(vsmFolder);
			
			// create a mar information file
			bOK &= createMarInfor(vsmobj, vsmFolder, true, true);

			if ( !bOK )
				return zippedFile;
			
			// zip the vsmFolder folder as mar
			zippedFile = DirectoryFileUtil.zipDirectory(vsmFolder, vsmobj.getVSMPath(), "mar");
		
			DirectoryFileUtil.deleteDirectory(vsmFolder);
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
			Log.write().error(e.getMessage());
		}
		
		return zippedFile;
		
	}
	
	/**
	 * Set seed data for schema 
	 * 
	 * @param sampleData - a set of sample data
	 */
	public void setSampledata (Map<String, Map<String, Object>> sampleData) {
		this.sampleData = sampleData;
	}
	
	/**
	 * Set AEDM metadata
	 * @param metadata - AEDM metadata
	 */
	private void setMetadata(Metadata metadata) {
		this.metadata = metadata;		
	}

	/**
	 * Generates a DevTest project or MAR for OData Service
	 * 
	 * @param vsmobj the information of VS Object 
	 * @param projRoot the location to store the project
	 * @param isMar true to create a MAR file
	 * @see VSMObject
	 * @return true if the project or MAR was generated
	 * @throws Exception
	 */
	public boolean buildLisaProject(final VSMObject vsmobj, final String projRoot, final boolean isMar) throws Exception {
		
		Document vsmDoc = loadLisaTemplate( ODATA_ODME_VSM_TEMPLATE );
		if (vsmDoc == null)
			return false;
		
		// create a project root folder 
		// create a project.config file in the folder <ProjectRoot>/configs
		String configFile = "project.config";
		String configsFolder = projRoot + File.separator + "Configs"; 
		
		boolean bOK = DirectoryFileUtil.createBlankFile(configsFolder, configFile);
		if (bOK == false) {
			String errMessage = "Failed to create " + configsFolder + File.separator + configFile;
			Log.write().error(errMessage);
			LisaMarUtil.mapErrors.put("buildLisaProject", errMessage);						
			return bOK;
		}
		
		//Set LISA project properties
		FileWriter fw = null;
		try {
			fw = new FileWriter(configsFolder + File.separator + configFile );
			String newLine = System.getProperty("line.separator");
			fw.write(CommonDefs.DDME_DATA_DIR + "=dvs.data/{{LISA_PROJ_NAME}}" + newLine);
			fw.write(CommonDefs.DDME_INIT_SCRIPT + "=current.sql" + newLine);
			fw.write(CommonDefs.DDME_SAVE_SCRIPT + "=new.sql" + newLine);
			fw.write(CommonDefs.DDME_SCHEMA_DIR + "={{LISA_PROJ_ROOT}}/data/sql" + newLine);
			fw.write(CommonDefs.RSP_ENABLE_JSONVERBOSE_META_PROPERTIES + "=false" + newLine);
			fw.write(CommonDefs.ODATA_VERSION + "=" + vsmobj.getOdataVersion().trim() + newLine);
		}
		finally {
			if (fw != null) {
				fw.close();
				Log.write().info("Created the configuration file: " + configFile);
			}
		}
		
		// create a lisa.project file in <ProjectRoot>
		bOK = createLisaProject(vsmobj, projRoot);
		if (bOK == false)
			return bOK;

		// create a mar information file in <ProjectRoot>/MARInfos for Lisa project
		String mariFolder = projRoot;
		if (!isMar) {
			mariFolder = mariFolder + File.separator + "MARInfos";
			bOK &= createMarInfor(vsmobj, mariFolder, isMar, true);
		}
		
		// copy edm file to <ProjectRoot>/data
		String dataFolder = projRoot + File.separator + "data";
		boolean bDataFolde = createEntityDataModel(vsmobj, dataFolder);
		if ( bDataFolde == false) {
			bOK = false;
		}
		else {
			String sqlFolder = dataFolder + File.separator + "sql";
			bOK &= createDBSchema(vsmobj, sqlFolder);
		}
		
		// create VSM/VSI file in <ProjectRoot>/VServices
		bOK &= createVSMDocument(vsmDoc, vsmobj, projRoot);
		
		return bOK;
		
	}
	
	/**
	 * Generates a DevTest MAR for REST Service
	 * 
	 * @param vsmobj the information of VS Object
	 * @see VSMObject
	 * @return MAR file with the full path
	 */
	public String makeRestLisaMar (final VSMObject vsmobj) {

		String zippedFile = "";
		
		setMetadata(metadata);
		
		String vsmFolder = vsmobj.getVSMPath() + File.separator + vsmobj.getVSMName();
		String projRoot = vsmFolder + File.separator + vsmobj.getVSMName();
		
		try {
			
			// Clean up, we need to delete the existing vcmFolder first
			if (DirectoryFileUtil.exists(vsmFolder))
				DirectoryFileUtil.deleteDirectory(vsmFolder);
			
			// Lisa project first
			boolean bOK = buildRestLisaProject(vsmobj, projRoot, true);
			
			// create a mar audit file in <ProjectRoot> for Lisa Mar
			bOK &= createMaraudit(vsmFolder);
			
			// create a mar information file
			bOK &= createMarInfor(vsmobj, vsmFolder, true, false);

			if ( !bOK )
				return zippedFile;
			
			// zip the vsmFolder folder as mar
			zippedFile = DirectoryFileUtil.zipDirectory(vsmFolder, vsmobj.getVSMPath(), "mar");
		
			DirectoryFileUtil.deleteDirectory(vsmFolder);
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
			Log.write().error(e.getMessage());
		}
		
		return zippedFile;
		
	}
	
	/**
	 * Generates a DevTest project for REST Service
	 * 
	 * @param vsmobj the information of VS Object
	 * @see VSMObject
	 * @return zip file with the full path
	 */
	public String makeRestLisaProject (final VSMObject vsmobj) {

		String zippedFile = "";
		setMetadata(metadata);
		
		String vsmFolder = vsmobj.getVSMPath() + File.separator + vsmobj.getVSMName();
		String projRoot = vsmFolder + File.separator + vsmobj.getVSMName();
		
		try {
			
			// Clean up, we need to delete the existing vcmFolder first
			if (DirectoryFileUtil.exists(vsmFolder))
				DirectoryFileUtil.deleteDirectory(vsmFolder);
			
			if (false == buildRestLisaProject(vsmobj, projRoot, false))
				return zippedFile;
			
			zippedFile = DirectoryFileUtil.zipDirectory(vsmFolder, vsmobj.getVSMPath(), "zip");
		
			DirectoryFileUtil.deleteDirectory(vsmFolder);
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
			Log.write().error(e.getMessage());
		}
		
		return zippedFile;
		
	}
	/**
	 * Generates DevTest project or MAR for REST Service
	 * 
	 * @param vsmobj the information of VS Object
	 * @param projRoot the directory to hold the project 
	 * @param isMar true to generate a MAR
	 * @return true if the project or MAR was generated
	 * @see VSMObject
	 * @throws Exception
	 */
	public boolean buildRestLisaProject (final VSMObject vsmobj, final String projRoot, final boolean isMar) throws Exception {
		
		Document vsmDoc = loadLisaTemplate( REST_ODME_VSM_TEMPLATE );
		if (vsmDoc == null)
			return false;
		
		// create a project root folder 
		// create a project.config file in the folder <ProjectRoot>/configs
		String configFile = "project.config";
		String configsFolder = projRoot + File.separator + "Configs"; 
		
		boolean bOK = DirectoryFileUtil.createBlankFile(configsFolder, configFile);
		if (bOK == false) {
			String errMessage = "Failed to create " + configsFolder + File.separator + configFile;
			Log.write().error(errMessage);
			LisaMarUtil.mapErrors.put("buildLisaRestProject", errMessage);						
			return bOK;
		}
		
		//Set LISA project properties
		FileWriter fw = null;
		try {
			fw = new FileWriter(configsFolder + File.separator + configFile );
			String newLine = System.getProperty("line.separator");
			fw.write("lisa.vse.execution.mode" + "=EFFICIENT" + newLine);
		}
		finally {
			if (fw != null) {
				fw.close();		
				Log.write().info("Created the configuration file: " + configFile);
			}
		}
		// create a lisa.project file in <ProjectRoot>
		bOK = createLisaProject(vsmobj, projRoot);
		if (bOK == false)
			return bOK;

		// create a mar information file in <ProjectRoot>/MARInfos for Lisa project
		String mariFolder = projRoot;
		if (!isMar) {
			mariFolder = mariFolder + File.separator + "MARInfos";
			bOK &= createMarInfor(vsmobj, mariFolder, isMar, false);
		}
		
		// create VSM/VSI file in <ProjectRoot>/VServices
		bOK &= createRestVSMDocument(vsmDoc, vsmobj, projRoot);
		
		return bOK;
		
	}

	/**
	 * Generates DevTest VS Model for REST Service
	 * 
	 * @param vsmDoc - VSM Document
	 * @param vsmobj - the information of VS Object
	 * @param projRoot - the directory to hold the project 
	 * @return true if VSM was generated
	 * @throws Exception
	 */
	private boolean createRestVSMDocument(final Document vsmDoc, final VSMObject vsmobj, final String projRoot) throws Exception{
		
		// create directories <ProjectRoot>/VServices/Images
		Log.write().info("Create VSM Document ... ");
		
		String errMessage = "";
		String vsmFolder = projRoot + File.separator + "VServices";
		String vsiFolder = vsmFolder + File.separator + "Images";
		DirectoryFileUtil.createDirectories(vsiFolder);

		boolean bOK = vsmInstance.updateRestVSMDocument(vsmDoc, vsmobj);
	    if (!bOK) {
			errMessage = "Failed to create VSM Document";
			Log.write().error(errMessage);
			LisaMarUtil.mapErrors.put("createVSM", errMessage);						
			return bOK;
	    }
		
		bOK = createRestVSIDocument(vsmobj, vsiFolder);	 			
	    if (!bOK) {
			errMessage = "Failed to generate Virtaul Service Image";
			Log.write().error(errMessage);
			LisaMarUtil.mapErrors.put("createVSI", errMessage);						
			return bOK;
	    }
	    
		// save VSM to file 
		String vsmFile = vsmFolder + File.separator + vsmobj.getVSMName() + ".vsm"; 
		bOK = XMLDomUtil.writeXmlFile(vsmFile, vsmDoc);
		Log.write().info("Generated the VSM: " + vsmFile);
		
		return bOK;
	}
	
	/**
	 * Generates VS Image for REST Service
	 * 
	 * @param vsmobj - the information of VS Object
	 * @param vsiFolder - the directory of VSI
	 * @return true if VSI was generated
	 * @throws Exception
	 */
	private boolean createRestVSIDocument(final VSMObject vsmobj, final String vsiFolder) throws Exception{

		Log.write().info("Create VSI Document ... ");		
    	Document vsiDocument = vsmobj.getVsiDocment();		
		if (vsiDocument == null)
			return false;
		
		// save VSI 
		String vsiFile = vsiFolder + File.separator + vsmobj.getVSMName() + ".vsi";  
		boolean bOK = XMLDomUtil.writeXmlFile(vsiFile, vsiDocument);		
		Log.write().info("Generated the VSI: " + vsiFile);
    
		return bOK;
	}
	
	/**
	 * Generates DevTest VS Model for Odata service
	 * 
	 * @param vsmDoc - VSM Document
	 * @param vsmobj - the information of VS Object
	 * @param projRoot - the directory to hold the project 
	 * @return true if VSM was generated
	 * @throws Exception
	 */
	private boolean createVSMDocument(final Document vsmDoc, final VSMObject vsmobj, final String projRoot) throws Exception{
		
		// create directories <ProjectRoot>/VServices/Images
		Log.write().info("Create VSM Document ... ");
		
		String errMessage = "";
		String vsmFolder = projRoot + File.separator + "VServices";
		String vsiFolder = vsmFolder + File.separator + "Images";
		DirectoryFileUtil.createDirectories(vsiFolder);
		
		String currentSQL = "{{DDME_DATA_DIR}}/{{DDME_INIT_SCRIPT}}"; //"{{LISA_PROJ_ROOT}}/data/sql/current.sql";
		String innerDBURL = String.format("jdbc:h2:mem:DB%s;INIT=RUNSCRIPT FROM '%s'", vsmobj.getVSMName(), currentSQL);
		vsmobj.setDataStoreURL(innerDBURL);
		boolean bOK = vsmInstance.updateVSMDocument(vsmDoc, vsmobj);
	    if (!bOK) {
			errMessage = "Failed to create VSM Document";
			Log.write().error(errMessage);
			LisaMarUtil.mapErrors.put("createVSM", errMessage);						
			return bOK;
	    }
	    
		bOK = createVSIDocument(vsmobj, vsiFolder);	 			
	    if (!bOK) {
			errMessage = "Failed to generate Virtaul Service Image";
			Log.write().error(errMessage);
			LisaMarUtil.mapErrors.put("createVSI", errMessage);						
			return bOK;
	    }
	    
		// save VSM to file 
		String vsmFile = vsmFolder + File.separator + vsmobj.getVSMName() + ".vsm"; 
		bOK = XMLDomUtil.writeXmlFile(vsmFile, vsmDoc);
		Log.write().info("Generated the VSM: " + vsmFile);
		
		return bOK;
	}

	/**
	 * Generates VS Image for Odata service
	 * 
	 * @param vsmobj - the information of VS Object
	 * @param vsiFolder - the directory of VSI
	 * @return true if VSI was generated
	 * @throws Exception
	 */
	private boolean createVSIDocument(final VSMObject vsmobj, final String vsiFolder) throws Exception{

		//System.out.println(this.getClass().getSimpleName() + " @createVSIDocument() method called");

		Log.write().info("Create VSI Document ... ");
		
    	Document tempDoc = loadLisaTemplate(ODATA_ODME_VSI_TEMPLATE);		
		if (tempDoc == null)
			return false;
		
		// save the VSI  
		String vsiFile = vsiFolder + File.separator + vsmobj.getVSMName() + ".vsi";  
    	File file = new File(vsiFile);
    	if (file.exists()){
			System.out.println("@createVSIDocument() method will overwrite the existing: " + vsiFile);    		
    	}
    	
		boolean bUpdated = vsiInstance.updateVSImageDocument(tempDoc, vsmobj);
    	if (bUpdated) {
			// save VSM to file 
			XMLDomUtil.writeXmlFile(vsiFile, tempDoc);		
			Log.write().info("Generated the VSI: " + vsiFile);
    	}
    	
		return bUpdated;
	}
	
	/**
	 * Generates DevTest project for Odata service
	 * 
	 * @param vsmobj - the information of VS Object
	 * @param projFolder - the directory of DevTest project
	 * @return true if the project was generated
	 * @throws Exception
	 */
	private boolean createLisaProject(final VSMObject vsmobj, final String projFolder) throws Exception{

		Log.write().info("Creating the project file ...");
		
		/**************************************************************/
    	Document tempDoc  = loadLisaTemplate(LISA_PROJECT_TEMPLATE);		
		if (tempDoc == null) 
			return false;
		
		// Set vsmName to <Name></Name>
		NodeList nl = tempDoc.getElementsByTagName("Name");
		Text a = tempDoc.createTextNode(vsmobj.getVSMName()); 
		Node node = nl.item(0);
		node.appendChild(a);		
		
		// save Lisa project file 
		String projfile = projFolder + File.separator + "lisa.project";      	
    	boolean bOK = XMLDomUtil.writeXmlFile(projfile, tempDoc);		
     	Log.write().info("Created the project file " + projfile);
     	
		return bOK;
	}
	
	/**
	 * Generate a DevTest project or MAR
	 *  
	 * @param vsmobj - the information of VS Object
	 * @param mariFolder - the directory of MAR infor 
	 * @param isMar	- true to generate a MAR
	 * @param isOdata - true if VS is Odata service
	 * @return true if the project or MAR was generated
	 * @throws Exception
	 */
	private boolean createMarInfor(final VSMObject vsmobj, final String mariFolder, final boolean isMar, final boolean isOdata) throws Exception{

		Log.write().info("Creating MAR information file ...");

		/* -----------------------------------------------------------------
		<MarInfo>
			<name>Service_Name</name>
			<type>VIRTUAL_SERVICE</type>
			<projectRoot></projectRoot>
			<optimized>true</optimized>
			<deployInfo>
			    <PrimaryAsset></PrimaryAsset>
			    <ConcurrentCapacity>1</ConcurrentCapacity>
			    <ThinkTimePercent>1</ThinkTimePercent>
			    <AutoRestart>true</AutoRestart>
			    <StartOnDeploy>true</StartOnDeploy>
			</deployInfo>
			<extraFile>data/EDMToDB.xml</extraFile>			--- Odata only
			<extraFile>data/sql/current.sql</extraFile>		--- Odata only
			<extraFile>data/sql/schema.sql</extraFile>		--- Odata only
		</MarInfo>
		--------------------------------------------------------------------*/
	
		Document doc = XMLDomUtil.createDocument();
		
		Element nameElement = doc.createElement("name");		
		nameElement.appendChild(doc.createTextNode(vsmobj.getVSMName()));
		
		Element typeElement = doc.createElement("type");		
		typeElement.appendChild(doc.createTextNode("VIRTUAL_SERVICE"));
		
		Element projRootElement = doc.createElement("projectRoot");		
		projRootElement.appendChild(doc.createTextNode(vsmobj.getVSMName()));
		
		Element optimizedElement = doc.createElement("optimized");
		optimizedElement.appendChild(doc.createTextNode("true"));
	
		Element primaryAssetElement = doc.createElement("PrimaryAsset");
		primaryAssetElement.appendChild(doc.createTextNode("VServices/" + vsmobj.getVSMName() + ".vsm"));
		
		Element concurrentCapacityElement = doc.createElement("ConcurrentCapacity");
		concurrentCapacityElement.appendChild(doc.createTextNode("1"));
		
		Element thinkTimePercentElement = doc.createElement("ThinkTimePercent");
		thinkTimePercentElement.appendChild(doc.createTextNode("1"));
		
		Element autoRestartElement = doc.createElement("AutoRestart");
		autoRestartElement.appendChild(doc.createTextNode("true"));
		
		Element startOnDeployElement = doc.createElement("StartOnDeploy");
		startOnDeployElement.appendChild(doc.createTextNode("true"));
		
		Element deployInfoElement = doc.createElement("deployInfo");
		deployInfoElement.appendChild(primaryAssetElement);
		deployInfoElement.appendChild(concurrentCapacityElement);
		deployInfoElement.appendChild(thinkTimePercentElement);
		deployInfoElement.appendChild(autoRestartElement);
		deployInfoElement.appendChild(startOnDeployElement);
		
		Element marInfoElement = doc.createElement("MarInfo");
		marInfoElement.appendChild(nameElement);
		marInfoElement.appendChild(typeElement);
		marInfoElement.appendChild(projRootElement);
		marInfoElement.appendChild(optimizedElement);
		marInfoElement.appendChild(deployInfoElement);
		
		if (isOdata) {
			Element edmElement = doc.createElement("extraFile");
			edmElement.appendChild(doc.createTextNode("data/EDMToDB.xml"));
			Element currentSqlElement = doc.createElement("extraFile");
			currentSqlElement.appendChild(doc.createTextNode("data/sql/current.sql"));
			Element schedmaSqlElement = doc.createElement("extraFile");
			schedmaSqlElement.appendChild(doc.createTextNode("data/sql/schema.sql"));			
			marInfoElement.appendChild(edmElement);
			marInfoElement.appendChild(currentSqlElement);
			marInfoElement.appendChild(schedmaSqlElement);
		}
		doc.appendChild(marInfoElement);	
		
		// save MARInfors to file 
		DirectoryFileUtil.createDirectory(mariFolder);
		String mariFile = mariFolder + File.separator;
		if (isMar)
			mariFile = mariFile + ".marinfo";
		else
			mariFile = mariFile + vsmobj.getVSMName() + ".mari";
			
		boolean bOK = XMLDomUtil.writeXmlFile(mariFile, doc);		
    	Log.write().info("Created MAR information file: " + mariFile );
		
		return bOK;
		
	}

	/**
	 * Generates the MAR audit information file 
	 * 
	 * @param projfolder - directory of DevTest project
	 * @return true if the file was generated
	 * @throws Exception
	 */
	private boolean createMaraudit(final String projfolder) throws Exception{

		Log.write().info("Creating MAR audit information file ...");
		
		/*
		<MAR_AUDIT_INFO>
			<LISA_MAR_CREATE_DATE></LISA_MAR_CREATE_DATE>
			<LISA_HOST_NAME>lisaHost</LISA_HOST_NAME>
			<LISA_SOURCE_PROJ_ROOT>lisaProject</LISA_SOURCE_PROJ_ROOT>
		</MAR_AUDIT_INFO>
		*/
		Document doc = XMLDomUtil.createDocument();
		
		//get current date time with Date()
		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		Date date = new Date();
		System.out.println(dateFormat.format(date));
		
		Element createDateElement = doc.createElement("LISA_MAR_CREATE_DATE");		
		createDateElement.appendChild(doc.createTextNode(dateFormat.format(date)));

		Element hostElement = doc.createElement("LISA_HOST_NAME");		
		hostElement.appendChild(doc.createTextNode("lisaHost"));

		Element projRootElement = doc.createElement("LISA_SOURCE_PROJ_ROOT");		
		projRootElement.appendChild(doc.createTextNode("lisaProject"));

		Element auditElement = doc.createElement("MAR_AUDIT_INFO");	
		auditElement.appendChild(createDateElement);
		auditElement.appendChild(hostElement);
		auditElement.appendChild(projRootElement);
		doc.appendChild(auditElement);		
		
		// save MARInfors to file 
		String marFile = projfolder + File.separator + ".maraudit";			
		boolean bOK = XMLDomUtil.writeXmlFile(marFile, doc);		
    	Log.write().info("Created MAR audit file: " + marFile );
    	
		return bOK;
		
	}
	
	/**
	 * Generates Data Model file
	 * 
	 * @param vsmobj - the information of VS Object
	 * @param dataFolder - the location to hold Data Model file
	 * @return true if the file was generated
	 * @throws Exception
	 */
	private boolean createEntityDataModel(final VSMObject vsmobj, final String dataFolder) throws Exception{

		System.out.println(this.getClass().getSimpleName() + " @createEntityDataModel() method called");

		boolean bOK = false;
		
		Log.write().info("Creating AEDM ...");
		
		DirectoryFileUtil.createDirectory(dataFolder);
		
		// copy edm file to LisaProject\Data folder 
		File sourcefile = new File( vsmobj.getEDMFile() );
		if (sourcefile.exists()) {
			String edmFile = dataFolder + File.separator + "EDMToDB.xml";     	
			File destfile = new File(edmFile);
			DirectoryFileUtil.copyFileUsingFileChannels(sourcefile, destfile);			
			Log.write().info("Saved AEDM to " + edmFile);
			bOK = true;
		}
		else {
			String errMessage = vsmobj.getEDMFile() + " doesn't exist";
			Log.write().error("Failed to create AEDM: " + errMessage);
			LisaMarUtil.mapErrors.put("createEntityDataModel", errMessage);
		}
		
		return bOK;
		
	}
	
	/**
	 * Generates Database Schema file
	 * 
	 * @param vsmobj - the information of VS Object
	 * @param sqlFolder - the location to hold Database Schema file
	 * @return true if the file was generated
	 */
	private boolean createDBSchema(final VSMObject vsmobj, final String sqlFolder)  {

		System.out.println(this.getClass().getSimpleName() + " @createDBSchema() method called");
		
		boolean bOK = false;
		
		Log.write().info("Creating Database Schema ...");		

		if (metadata == null) {
			Log.write().error("There is No AEDM available");
			return bOK;
		}
	
		try {
			
			DirectoryFileUtil.createDirectories(sqlFolder);
			
			JDBCUtil jdbcUtil = new JDBCUtil(vsmobj, metadata, sampleData);
			
			// save the VSI as the same location as VSM 
			String schemaFile = sqlFolder + File.separator + "schema.sql";  
			String currentFile = sqlFolder + File.separator + "current.sql";  
			
			bOK = jdbcUtil.createDBSchema(schemaFile, false);				
			if ( bOK ) {
				bOK = jdbcUtil.createDBSchema(currentFile, true);				
				if ( !bOK ) {
					File sourcefile = new File( schemaFile );
					File destfile = new File(currentFile);
					DirectoryFileUtil.copyFileUsingFileChannels(sourcefile, destfile);					
					Log.write().info("Created DB schema file: " + currentFile);
				}		
			}
		} 
		catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.write().error(e.getMessage());
			LisaMarUtil.mapErrors.put("createDBSchema", e.getMessage());			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.write().error(e.getMessage());
			LisaMarUtil.mapErrors.put("createDBSchema", e.getMessage());			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.write().error(e.getMessage());
			LisaMarUtil.mapErrors.put("createDBSchema", e.getMessage());			
		}			
		
		return bOK;
	}
	
	/**
	 * Load template file
	 * 
	 * @param template - template file
	 * @return Document
	 * @throws Exception
	 */
	private Document loadLisaTemplate(String template) throws Exception {
		
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		InputStream is = classloader.getResourceAsStream(template);
        if(is == null) {
            throw new NoSuchFileException("Resource file not found. Note that the current directory is the source folder!");
        }
		
    	Document doc = XMLDomUtil.readXmlFile(is);
		if (doc == null){
			String errMessage = "Failed to read " + template;
			Log.write().error("Failed to read " + template);
			LisaMarUtil.mapErrors.put("loadLisaTemplate", errMessage);			
		}			
		return doc;	    
				
	}
	
}
