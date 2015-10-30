/*******************************************************************************
 * Copyright (c) 2015 CA.  All rights reserved.
 *
 * This source file is licensed under the terms of the Eclipse Public License 1.0
 * For the full text of the EPL please see https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
/**
 * 
 */
package com.ca.dvs.app.dvs_servlet.misc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import net.lingala.zip4j.core.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import com.ca.dvs.utilities.raml.RamlUtil;
import com.google.common.io.Files;

/**
 * File related utility methods supporting DVS servlet
 * <p>
 * @author CA Technologies
 * @version 1
 */
public class FileUtil {
	
	private final static byte[] ZIP_SIGNATURE = { 'P', 'K', 0x03, 0x04 };

	private static Logger log = Logger.getLogger(FileUtil.class);

	/**
	 * Create A File object from the content uploaded to the servlet
	 * <p>
	 * @param uploadedInputStream the stream associated with a POSTed form containing a File argument
	 * @param fileDetail the File specification for the uploadedInputStream
	 * @return the File locally stored from the uploadedInputStream 
	 * @throws Exception (IOException net.lingala.zip4j.exception.ZipException)
	 */
	public static File getUploadedFile(InputStream uploadedInputStream, FormDataContentDisposition fileDetail) throws Exception {
		
		File uploadedFile	= null;
		File tmpDir			= null;
		
		try {
			
			uploadedFile = File.createTempFile(fileDetail.getFileName(), null);
			
			FileUtils.copyInputStreamToFile(uploadedInputStream, uploadedFile);

			if (FileUtil.isZipFile(uploadedFile)) {
				
				tmpDir = Files.createTempDir();
				ZipFile zipFile = new ZipFile(uploadedFile);
				zipFile.extractAll(tmpDir.getAbsolutePath());
				
				uploadedFile.delete();
				
				uploadedFile = tmpDir;
				
			}
			
		} catch (IOException e) {
			
			String msg = String.format("Failed to store uploaded file - %s", e.getMessage());
			
			throw new Exception(msg, e.getCause());
			
		} catch (net.lingala.zip4j.exception.ZipException e) {

			String msg = String.format("Failed to store uploaded file - %s", e.getMessage());
			
			throw new Exception(msg, e.getCause());
			
		}

		return uploadedFile;
	}

	/**
	 * A quick test to determine if uploaded file is a ZIP file
	 * <p> 
	 * @param file the file to interrogate for being a zip file 
	 * @return true is the specified file is a zip file
	 * @throws IOException
	 */
	public static boolean isZipFile(File file) throws IOException {
		
		boolean			isZipFile		= false;
		FileInputStream	fileInputStream	= null;
		
		try {

			fileInputStream = new FileInputStream(file);
			
			if (fileInputStream.available()>ZIP_SIGNATURE.length) {
				
				byte[] magic = new byte[ZIP_SIGNATURE.length];
				
				if (ZIP_SIGNATURE.length == fileInputStream.read(magic, 0, ZIP_SIGNATURE.length)) {
					
					isZipFile = Arrays.equals(magic, ZIP_SIGNATURE);
					
				}

			}
			
		} finally {
			
			if (null != fileInputStream) {
				fileInputStream.close();
			}
			
		}
		
		return isZipFile;
	}

	/**
	 * Given a folder, return the main RAML file
	 * <p>
	 * @param folder the folder containing a multi-part RAML
	 * @param targetName the desired RAML file to find in the folder
	 * @return the targetName file if found, or the first RAML file found
	 * @throws FileNotFoundException if no RAML files are found in the folder
	 */
	public static File selectRamlFile(File folder, String targetName) throws FileNotFoundException {

		File selectedFile = null;
		
		File[] ramlFiles = RamlUtil.getRamlFiles(folder);
		
		if (null != ramlFiles && ramlFiles.length > 0) {
						
			for (File f : ramlFiles) {
				if (f.getName().equalsIgnoreCase(targetName) && f.canRead()) {
					selectedFile = f;
					break;
				}
			}
			
			// If none of the raml files match the base name of the uploaded file, use the first one found
			if (null == selectedFile) { 
				selectedFile = ramlFiles[0];
			}
		} else {
	
			String msg = String.format("No RAML files found in folder - %s", folder.getAbsolutePath());
			log.error(msg);
			throw new FileNotFoundException(msg);
		}
		
		return selectedFile;
	}
}
