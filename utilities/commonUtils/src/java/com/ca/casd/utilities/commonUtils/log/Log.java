/*******************************************************************************
 * Copyright (c) 2015 CA.  All rights reserved.
 *
 * This source file is licensed under the terms of the Eclipse Public License 1.0
 * For the full text of the EPL please see https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
/* ------------------------------------------------------------------------ //

	Revision History:
	
	2009-06-10	George Curran, CA
				
				Added to com.ca.dso.common.utils.log package.
	
 
// ------------------------------------------------------------------------ */

package com.ca.casd.utilities.commonUtils.log;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;

import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;


/**
 * Static wrapper class to simplify use of the standard "log4j" package by
 * setting default values for all properties and initializing any required
 * objects when the either the "write" is invoked.  Defaults may be 
 * overwritten using the appropriate "set" method.
 * <p>
 * Defaults:
 * <p>
 * <b>Log File Name:</b> om.ca.casd.utilities.commonUtils.log
 * <p>
 * <b>Log File Path:</b> [User's Home Directory]/ca/com.ca.casd.lisa/logs (if the directory does not
 * exist it will be created)
 * <p>
 * <b>Log Layout Pattern:</b> Comma separated
 * <p>
 * <p>- Date/Time Stamp
 * <p>- Severity (DEBUG, INFO, WARN, ERROR, FATAL)
 * <p>-	Log Message
 * <p>- Class (class where "write" method was invoked)
 * <p>- Method (method where "write" method was invoked)
 * <p>- Line (line number where "write" method was invoked)
 * <p>
 * <b>Log Level:</b> WARN (i.e., only "write.warn" messages and higher will
 * be logged)
 * <p>
 * <b>Log Maximum Backup Index:</b> 9 (i.e., the current plus 9 previous
 * logs will be retained
 * <p>
 * <b>Log Maximum File Size:</b> 2mb (i.e., logs will roll over when the file
 * size exceeds 2mb)
 * <p>
 * <b>Log Roll Over On Initialization:</b> true (i.e., log will roll over each
 * time it is initialized)
 * 																			*/

public class Log {
	
	public static final String COPYRIGHT = "(c) 2015 CA.  All rights reserved.";

	public static final String DEFAULT_LOG_PATH_FILE_NAME = System.getProperty("user.home")
														  + System.getProperty("file.separator")
														  + "ca"
														  + System.getProperty("file.separator")
														  + "com.ca.casd.lisa"
														  + System.getProperty("file.separator")
														  + "logs"
														  + System.getProperty("file.separator")
														  + "default.log";
	
/**	String constant set to default log file pattern layout					*/
	public static final String DEFAULT_LAYOUT_PATTERN = "%d{ISO8601}, %p, %m, %C, %M, %L %n";
	
/**	Integer constant set to default log level								*/
	public static final Level DEFAULT_LOG_LEVEL = Level.ALL;
	
/** Integer constant set to default maximum number of backups				*/
	public static final int DEFAULT_MAX_BACKUP_INDEX = 9;
	
/** String constant set to default maximum log size							*/
	public static final String DEFAULT_MAX_FILE_SIZE = "2MB";
	
/**	Boolean constant set to default roll over on init behavior				*/
	public static final boolean DEFAULT_ROLLOVER_ON_INIT = true;
	
/** String constant set to system debug property name						*/
	public static final String SYSTEM_DEBUG = "debug";
	
/** Print boolean to store echo to console status flag						*/
	private static boolean bolEchoToConsole = false;
	
/**	Private console appender used by logger									*/	
	private static ConsoleAppender appenderConsole = null;
	
/**	Private rolling file appender used by logger							*/
	private static RollingFileAppender appenderRollingFile = null;
	
/**	Private logger for debug and higher messages							*/
	private static Logger logger = Logger.getLogger("default");
	
/**	String to store log layout pattern										*/
	private static String strLayoutPattern = Log.DEFAULT_LAYOUT_PATTERN;
	
/** String to store log path and file name									*/
	private static String strLogPathFileName = Log.DEFAULT_LOG_PATH_FILE_NAME;
	
/**	Level object to store specified minimum priority log level				*/
	private static Level level = Log.DEFAULT_LOG_LEVEL;
	
/** Integer to store specified maximum number of backups					*/
	private static int intMaxBackupIndex = Log.DEFAULT_MAX_BACKUP_INDEX;
	
/** String to store specified maximum log size								*/
	private static String strMaxFileSize = Log.DEFAULT_MAX_FILE_SIZE;
	
/**	Boolean to store specified roll-over on initialization flag				*/
	private static boolean bolRolloverOnInit = DEFAULT_ROLLOVER_ON_INIT;
	
	
public synchronized static void close() {
	
	if ( Log.appenderConsole != null ) Log.appenderConsole.close();
	if ( Log.appenderRollingFile != null ) Log.appenderRollingFile.close();
	Log.logger.removeAllAppenders();
	Log.appenderConsole = null;
	Log.appenderRollingFile = null;
}


/**
 * Create directories that do not exist in the log path.
 * 
 * @return boolean set to true if successful otherwise false
 * 																			*/
	private static boolean createLogDirectory(){
		
	//	Create directory 
		return new File(Log.strLogPathFileName).getParentFile().mkdirs();
	}
	
	
/**
 * Get log layout pattern.
 * 
 * @return String set to log layout pattern.
 * 																			*/
	public synchronized static String getLogLayoutPattern(){
		
		return Log.strLayoutPattern;
	}
	
	
/**
 * Get log level.
 * 
 * @return String corresponding to log level.
 * 
 * @throws LogException 
 * 																			*/
	public synchronized static Level getLogLevel() throws LogException{
		
		if ( Log.appenderRollingFile == null ) Log.initLog();
		return Log.logger.getLevel();
	}
	
	
/**
 * Get log maximum backups value.
 * 
 * @return int set to log maximum backups value.
 * 
 * @throws LogException 
 * 																			*/
	public synchronized static int getLogMaxBackupIndex() throws LogException{
		
		if ( Log.appenderRollingFile == null ) Log.initLog();
		return Log.appenderRollingFile.getMaxBackupIndex();
	}
	
	
/**
 * Get log maximum file size.
 * 
 * @return String set to log maximum file size
 * @throws LogException 
 * 																			*/
	public synchronized static String getLogMaxFileSize() throws LogException{
		
		if ( Log.appenderRollingFile == null ) Log.initLog();
		return Log.strMaxFileSize;
	}

	
/**
 * Get log path and file name.
 * 
 * @return String set to log path and file name.
 * 
 * @throws LogException 
 * 																			*/
	public synchronized static String getLogPathFileName() throws LogException{
		
		if ( Log.strLogPathFileName == null ) Log.initLog();
		return Log.strLogPathFileName;
	}

	
	private synchronized static void initAppenderConsole() {
		
		if (Log.appenderConsole != null) Log.logger.removeAppender(Log.appenderConsole);
		
		if (Log.bolEchoToConsole) {
						
			appenderConsole = new ConsoleAppender(new PatternLayout(strLayoutPattern), "System.out");
			appenderConsole.setName("Console");
			appenderConsole.setLayout(new PatternLayout(Log.strLayoutPattern));
			Log.logger.addAppender(appenderConsole);
		}
	}

	
/**
 * Initializes the appender used to add entries to the log file.
 * 																				*/
	private synchronized static void initAppenderRollingFile(){
		
	//	Create parent directory structure
		Log.createLogDirectory();
		
	//	Remove existing appenders
		if (Log.appenderRollingFile != null) Log.logger.removeAllAppenders();
		
	//	Initialize new appender
		Log.appenderRollingFile = new RollingFileAppender();
		
	//	Set appender properties
		Log.appenderRollingFile.setFile(Log.strLogPathFileName);
		Log.appenderRollingFile.setLayout(new PatternLayout(Log.strLayoutPattern));
		Log.appenderRollingFile.setMaxBackupIndex(Log.intMaxBackupIndex);
		Log.appenderRollingFile.setMaxFileSize(Log.strMaxFileSize);
		
	//	Activate appender options
		Log.appenderRollingFile.activateOptions();
		
	//	Add new appender to logger appenders
		Log.logger.addAppender(appenderRollingFile);

	//	Check roll over property
		if (bolRolloverOnInit){
			
		//	Determine if log file exists has already been written to
			File fileLog = new File(strLogPathFileName);			
			if (fileLog.length() > 0){
				
			//	Roll over existing log
				appenderRollingFile.rollOver();
			}
		}
		
		if ( Log.bolEchoToConsole ) {
			
		}
	}


/**
 * Set properties and initialize required objects.
 * 
 * @throws LogException 
 *																			*/
	private synchronized static void initLog() throws LogException{
		
	//	Initialize logger
		Log.logger.setLevel(Log.level);
		
	//	Initialize console appender
		Log.initAppenderConsole();
		
	//	Initialize rolling file appender
		Log.initAppenderRollingFile();
		
	//	Update default properties if Java system debug enabled
		if (System.getProperty(Log.SYSTEM_DEBUG) != null){
			
			Log.setLogLevel(Level.ALL);
			Log.setEchoToConsoleEnabled(true);
		}
	}
	
/**
 * Determines if a 'Console' appender is included in the logger's appender
 * collection.
 * 
 * @return boolean set to true if found otherwise false
 */
	public synchronized static boolean isEchoToConsoleEnabled() {
		
		boolean bolEchoToConsoleEnabled = false;
		
		for (@SuppressWarnings("unchecked")
		Enumeration<Appender> enumAppenders = Log.logger.getAllAppenders(); enumAppenders.hasMoreElements();) {
			
			Appender appender = enumAppenders.nextElement();
			if ( appender.getName().equalsIgnoreCase("Console") ) {
				
				bolEchoToConsoleEnabled = true;
				break;
			}
		}
		
		return bolEchoToConsoleEnabled;
	}
	
	
/**
 * Enable console appender to echo log messages to console.
 * 
 * @param bolEnabled - boolean set to true to enable echo to console or
 * false to disable
 * 																			*/
	public synchronized static void setEchoToConsoleEnabled(boolean bolEnabled){
		
		if ( Log.bolEchoToConsole != bolEnabled ) {
			
			Log.bolEchoToConsole = bolEnabled;
			Log.initAppenderConsole();
		}
	}
	
	
/** 
 * Set/reset log layout pattern. 
 * 
 * @param strLayoutPattern - String set to valid pattern (Default,
 * "%d{ISO8601}, %p, %m, %C, %M, %L %n", creates a comma delimited log file
 * that can be easily parsed and filtered using Microsoft Excel)
 * <p>
 * <p>
 * See <a href="http://www.allapplabs.com/log4j/log4j_layouts.htm" target="_blank">Log4J Layouts</a> for more information.
 * 
 * @throws LogException 
 * 																			*/
	public synchronized static void setLogLayoutPattern(String strLayoutPattern) throws LogException{
		
		if ( strLayoutPattern != null && !strLayoutPattern.isEmpty() ) {
			
			Log.strLayoutPattern = strLayoutPattern;
			
			if (Log.appenderRollingFile != null){
				
				Log.initAppenderRollingFile();
			}
			else {
				
				Log.initLog();
			}
			
		}
		else {
			
			String strMsg = "Required layout pattern argument is null or empty";
			throw new LogException(strMsg);
		}
	}
	
	
/**
 * Set/reset log level.
 * 
 * @param level - Member of org.appached.log4j.Level enumeration.
 * <p>
 * <p>Level.ALL
 * <p>Level.DEBUG
 * <p>Level.ERROR
 * <p>Level.INFO
 * <p>Level.OFF
 * <p>Level.WARN
 * <p>
 * <p>Default is Level.WARN.
 * 
 * @throws LogException 
 * 																			*/
	public synchronized static void setLogLevel(Level level) throws LogException {
	
		if ( level != null ) {
			
			if ( !level.equals(Log.getLogLevel() ) ) {
				
				Log.level = level;
				Log.logger.setLevel(level);			
			}
		}
		else {
			
			String strMsg = "Required log level argument is null or empty";
			throw new LogException(strMsg);
		}
	}
	
	
/**
 * Set/reset maximum number of backups to be retained.
 * 
 * @param intMaxBackupIndex - int set to number of backup ups to be
 * retained (Default is 9, valid entries 0 through 99).
 * 
 * @throws LogException 
 * 
 */
	public synchronized static void setLogMaxBackupIndex(int intMaxBackupIndex) throws LogException{
		
		if ( intMaxBackupIndex >= 0 && intMaxBackupIndex <= 99 ) {

			Log.intMaxBackupIndex = intMaxBackupIndex;
			
			if (Log.appenderRollingFile != null){
				
				Log.initAppenderRollingFile();
			}
			else {
				
				Log.initLog();
			}
		}
		else {
			
			String strMsg = "Required maximum backup indes argument must be > 0 and < 100";
			throw new LogException(strMsg);
		}
	}
	

/**
 * Set/reset maximum file size for log file. 
 * 
 * @param strMaxFileSize - String set to valid file size value (i.e.,
 * "150KB", "5MB" - note no spaces are allow, units must be specified in
 * upper case, valid unit are "KB" and "MB", default is "2MB")
 * 
 * @throws LogException
 * 
 *  Important: Using the default pattern is highly recommended since it
 *  can be easily displayed, filtered and search in Excel.
 * 																			*/
	public synchronized static void setLogMaxFileSize(String strMaxFileSize) throws LogException {
	
		if ( strMaxFileSize != null &&  !strMaxFileSize.isEmpty() ) {
			
			strMaxFileSize = strMaxFileSize.toUpperCase();
			strMaxFileSize = strMaxFileSize.replaceAll(" ",  "");

			if ( strMaxFileSize.matches("[1-9][0-9]*[K|M][B]") ) {
				
				Log.strMaxFileSize = strMaxFileSize;
				
				if (Log.appenderRollingFile != null){
					
					Log.initAppenderRollingFile();
				}
				else {
					
					Log.initLog();
				}
			}
			else {
				
				String strMsg = "Required maximum file size argument must specify an integer value greater than 0 followed by units 'KB' or 'MB' without spaces (ex. 200KB)";
				throw new LogException(strMsg);
			}
		}
		else {
			
			String strMsg = "Required maximum file size argument is null or empty";
			throw new LogException(strMsg);
		}
	}
	
	
/**
 * Set/reset log path and file name.
 * 
 * @param strLogPathFileName - String set to log file path and file name
 * (Default is [User's Home Directory]/.eclipse/log/com.ca.casd.lisa.plugins.log, directories in the path
 * that do not exist will be created)
 * 
 * @throws LogException 
 */
	public synchronized static void setLogPathFileName(String strLogPathFileName) throws LogException{
		
		try {
			
			File fLog = new File(strLogPathFileName);
			fLog.getCanonicalPath();
			
			Log.strLogPathFileName = strLogPathFileName;
			
			Log.initLog();
		}
		catch (IOException e) {

			String strMsg = "Log path/file name contains invalid characters";
			throw new LogException(strMsg, e);
		}
	}


/** 
 * Exposes logger object and methods used to write events to log file.
 * <p>
 * <p>
 * Note: Actual output of events is controlled by the current log level
 * setting (i.e., invoking "Log.write().debug("Log message") will not
 * insert an entry into the log file if the log level is set to "WARN").
 * <p>
 * - Log.write().debug("Log message") inserts a DEBUG level entry
 * <p>
 * - log.write().warn("Log message") inserts a WARN level entry
 * <p>
 * - Log.write().error("Log message") inserts a ERROR level entry
 * <p>
 * - Log.write().fatal("Log message") inserts a FATAL level entry
 * <p>
 * Note logger will be initialized automatically if necessary.
 * 
 * @return Logger object capable of writing events to log file
 * 
 * @throws LogException 
 */
	public synchronized static Logger write() {
		
	//	Initialize logger object if necessary
		if ( ! Log.logger.getAllAppenders().hasMoreElements() ) {
			
			try {
				initLog();
			}
			catch (LogException e) {
			//	Ignore
			}
		}
		
	//	Return required object
		return Log.logger;
	}
}
