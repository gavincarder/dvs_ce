/*******************************************************************************
 * Copyright (c) 2015 CA.  All rights reserved.
 *
 * This source file is licensed under the terms of the Eclipse Public License 1.0
 * For the full text of the EPL please see https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
/*****************************************************************/

package com.ca.dvs.utilities.lisamar;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DirectoryFileUtil {

	/**
	 * Creates a zip file to the specified location 
	 * 
	 * @param sourceFolder - source location
	 * @param targetFolder - target location
	 * @param zipExtension - the extension of ZIP file (such as ZIP, MAR, ..,)
	 * @return			   - the name of zip file including the full path
	 * @throws IOException
	 */
	public static String zipDirectory (final String sourceFolder, final String targetFolder, final String zipExtension) throws IOException {

		File directoryToZip = new File( sourceFolder );

		List<File> fileList = new ArrayList<File>();
		System.out.println("---Getting references to all files in: " + directoryToZip.getCanonicalPath());
		
		getAllFiles(directoryToZip, fileList);
		System.out.println("---Creating zip file");
		
		String zippedfile = writeZipFile(directoryToZip, fileList, targetFolder, zipExtension);
		if (zippedfile.isEmpty())
			System.out.println( "----Failed to zip the direstory: " + directoryToZip);
		else
			System.out.println( "---Done: " + zippedfile);
		
		return zippedfile;
	}
	
	/**
	 * Creates the directory
	 * 
	 * @param path - the path name 
	 * @return true if the directory was created or exists
	 * @throws IOException
	 */
	public static boolean createDirectory (final String path) throws IOException {

		File file = new File( path );
		if (file.exists()) {
			System.out.println( path + " exists already!" );
		}
		else {
			if (file.mkdir()) {
				System.out.println( path + " is created! " );
			} else {
				System.out.println("Failed to create directory: " + path );
			}
		}
		
		return true;	
	}

	/**
	 * Creates the directory, including any necessary but nonexistent parent directories.
	 * 
	 * @param path - the path name
	 * @return true if the directory was created or exists, along with all necessary parent directories; 
	 * @throws IOException
	 */
	public static boolean createDirectories (final String path) throws IOException {

		File files = new File( path );
		if (files.exists()) {
			System.out.println( path + " exists already!" );
		}
		else {
			if (files.mkdirs()) {
				System.out.println( path + " are created!" );
			} else {
				System.out.println("Failed to create multiple directories: " + path);
				return false;
			}
		}
		
		return true;
		
	}

	/**
	 * Creates a new, empty file 
	 * 
	 * @param path - the location to create file
	 * @param filename - file name
	 * @return true if and only if a file with this name does not yet exist
	 * @throws IOException
	 */
	public static boolean createBlankFile (final String path, final String filename) throws IOException {

		boolean bCreate = false;
		
		String newfile = path + File.separator + filename;
		File file = new File(newfile);
		File fileLocation = new File( path );
		
		if (file.exists()) {
			System.out.println( newfile + " exists already!"  );
		}
		else {
			bCreate = fileLocation.exists();
			if (!bCreate)
				bCreate = fileLocation.mkdirs();
			
			if (bCreate)
				bCreate = file.createNewFile();
			
			if (bCreate) 
				System.out.println( newfile + " is created!" );
			else 
				System.out.println("Failed to create: " + newfile );			
		}
		
		return bCreate;
		
	}
	
	/**
	 * Deletes a file or directory
	 *   
	 * @param path - the full path 
	 * @throws IOException
	 */
    public static void deleteDirectory(final String path) throws IOException{
    	File file = new File(path);
    	deleteDirectory(file);
    }
    
	/**
	 * Deletes a directory. 
	 * 
	 * @param file - the file or directory
	 * @throws IOException
	 */
    public static void deleteDirectory(File file) throws IOException{
     
    	if(file.isDirectory()) {
 
			//list all the directory contents
			String[] files = file.list();

    		//directory is empty, then delete it
    		if(files == null || files.length == 0) {
        		deleteFileOrDirectory(file);
    		} 
    		else {
 
        	   	for (String temp : files) {
        		   //construct the file structure
        		   File fileDelete = new File(file, temp);
 
        	      	//recursive delete
        	      	deleteDirectory(fileDelete);
        	   	}
 
        	   	//check the directory again, if empty then delete it
        	   	files = file.list();
        		if(files == null || files.length == 0) {
            		deleteFileOrDirectory(file);
        	   	}
    		}
 
    	}else{
    		//if file, then delete it
    		deleteFileOrDirectory(file);
    	}
    	
    }
    
	/**
	 * Delete a directory or file 
	 * 
	 * @param file - the file or directory
	 * @throws IOException
	 */
    public static void deleteFileOrDirectory(File file) throws IOException {
		if (file.delete())
			System.out.println("Deleted : " + file.getAbsolutePath());
		else
			System.out.println("Failed to delete : " + file.getAbsolutePath());
    }
    
    /**
     * Copy a file
     * 
     * @param source - source file
     * @param dest - target file
     * @throws IOException
     */
    public static void copyFileUsingFileChannels(File source, File dest) throws IOException {
    	FileInputStream inputStream = null;
    	FileOutputStream outputStream = null;    	
        FileChannel inputChannel = null;
        FileChannel outputChannel = null;
        try {
        	inputStream = new FileInputStream(source);
        	outputStream = new FileOutputStream(dest);
        	if (inputStream != null)
        		inputChannel = inputStream.getChannel();
        	
        	if (outputStream != null)
        		outputChannel = outputStream.getChannel();
        	
            if (outputChannel != null && inputChannel != null)
            	outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
        } 
        finally {
        	safeFileChannelClose(inputChannel);
        	safeFileChannelClose(outputChannel);
        	safeFileInputStreamClose(inputStream);
        	safeFileOutputStreamClose(outputStream);
        }
    }
    
	/**
	 * Gets the files in the specified path
	 * 
	 * @param dir - the source path
	 * @param fileList - an array of abstract pathnames denoting the files in the specified path 
	 */
	public static void getAllFiles(File dir, List<File> fileList) {
		try {
			File[] files = dir.listFiles();
			if (files != null) {
				for (File file : files) {
					fileList.add(file);
					if (file.isDirectory()) {
						System.out.println("directory:" + file.getCanonicalPath());
						getAllFiles(file, fileList);
					} else {
						System.out.println("     file:" + file.getCanonicalPath());
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Creates a zip file in the specified location
	 * 
	 * @param directoryToZip - zip file
	 * @param fileList		 - file list to zip
	 * @param targetFolder	 - target location
	 * @param zipExtension 	 - the extension of ZIP file (such as ZIP, MAR, ..,)
	 * @return			     - the name of zip file including the full path
	 */
	public static String writeZipFile(File directoryToZip, List<File> fileList, final String targetFolder, final String zipExtension) {

		String zipFile = "";
		FileOutputStream fos = null;
		ZipOutputStream zos = null;
		try {
			String targetFile = targetFolder + File.separator + directoryToZip.getName() + "." + zipExtension;
			
			if ( exists(targetFile) )
				deleteDirectory(targetFile);
			
			fos = new FileOutputStream(targetFile);
			zos = new ZipOutputStream(fos);

			for (File file : fileList) {
				if (!file.isDirectory()) { // only zip files, not directories
					addToZip(directoryToZip, file, zos);
				}
			}
			
			zipFile = targetFile;
			System.out.println("zipped to " + targetFile);
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			safeZipOutputStreamClose(zos);
			safeFileOutputStreamClose(fos);
		}
 
		return zipFile;
		
	}

	/**
	 * Add a file to the zip file
	 * 
	 * @param directoryToZip - zip file
	 * @param file			 - a file to add
	 * @param zos 			 - ZipOutputStream
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void addToZip(File directoryToZip, File file, ZipOutputStream zos) throws FileNotFoundException,
			IOException {

		FileInputStream fis = null;
		
		try {
			fis = new FileInputStream(file);

			// we want the zipEntry's path to be a relative path that is relative
			// to the directory being zipped, so chop off the rest of the path
			String zipFilePath = file.getCanonicalPath().substring(directoryToZip.getCanonicalPath().length() + 1,
					file.getCanonicalPath().length());
			System.out.println("Writing '" + zipFilePath + "' to zip file");
			ZipEntry zipEntry = new ZipEntry(zipFilePath);
			zos.putNextEntry(zipEntry);
	
			byte[] bytes = new byte[1024];
			int length;
			while ((length = fis.read(bytes)) >= 0) {
				zos.write(bytes, 0, length);
			}
		}
		finally {
			safeZipOutputStreamcloseEntry(zos);
			safeFileInputStreamClose(fis);
		}
	}
	
	/**
	 * Tests whether the file or directory
	 * 
	 * @param path - path name
	 * @return true if and only if the file or directory exists
	 * @throws IOException
	 */
	public static boolean exists (final String path) throws IOException {
		File file = new File( path );
		return file.exists();
	}

	/**
	 * Closes the file input stream and releases any system resources associated with the stream.
	 * 
	 * @param fis - reading streams of raw bytes 
	 */
	public static void safeFileInputStreamClose(FileInputStream fis) {
		if (fis != null) {
		    try {
		    	fis.close();
		    } 
		    catch (IOException e) {
				e.printStackTrace();
		    }
		}
	}

	/**
	 * Closes the file output stream and releases any system resources associated with this stream
	 * 
	 * @param fos - an output stream for writing data to a File 
	 */
	public static void safeFileOutputStreamClose(FileOutputStream fos) {
		if (fos != null) {
		    try {
		    	fos.close();
		    } 
		    catch (IOException e) {
				e.printStackTrace();
		    }
		}
	}
	
	/**
	 * Closes the ZIP output stream as well as the stream being filtered
	 * 
	 * @param zos - an output stream filter for writing files in the ZIP file format
	 */
	public static void safeZipOutputStreamClose(ZipOutputStream zos) {
		if (zos != null) {
		    try {
		    	zos.close();
		    } 
		    catch (IOException e) {
				e.printStackTrace();
		    }
		}
	}

	/**
	 * Closes the current ZIP entry and positions the stream for writing the next entry
	 * 
	 * @param zos - an output stream filter for writing files in the ZIP file format
	 */
	public static void safeZipOutputStreamcloseEntry(ZipOutputStream zos) {
		if (zos != null) {
		    try {
		    	zos.closeEntry();
		    } 
		    catch (IOException e) {
				e.printStackTrace();
		    }
		}
	}	

	/**
	 * Closes the file channel and releases any system resources associated with the stream.
	 * 
	 * @param fc - reading streams of raw bytes 
	 */
	public static void safeFileChannelClose(FileChannel fc) {
		if (fc != null) {
		    try {
		    	fc.close();
		    } 
		    catch (IOException e) {
				e.printStackTrace();
		    }
		}
	}
	
	
}