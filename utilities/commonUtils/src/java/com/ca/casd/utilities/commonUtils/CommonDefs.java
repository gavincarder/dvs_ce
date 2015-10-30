/*******************************************************************************
 * Copyright (c) 2015 CA.  All rights reserved.
 *
 * This source file is licensed under the terms of the Eclipse Public License 1.0
 * For the full text of the EPL please see https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
/**
 * Common static definitions used in various projects
 */
package com.ca.casd.utilities.commonUtils;

/**
 * @author artal03
 *
 */
public final class CommonDefs {
	public final static String ADMIN_SERVICE                               = "dynvs_admin";
	public final static String EDM_DB_METADATA                             = "EDMDBMetadata";                                                      // State Object handle for the EDM DB MetaData object
	public final static String EDM_MODEL_PATH                              = "{{LISA_PROJ_ROOT}}/Data/EDMToDB.xml";                                // Path to the project's EDM data model file 
	public final static String DDME_DATA_STORE                             = "DDME_DATA_STORE";                                                    // State Object handle for the DDME data store object
	public final static String DDME_DATA_DIR                               = "DDME_DATA_DIR";                                                      // State object handle for the path to the dynamic virtualization persistent data folder (dvs.data/{{LISA_PROJ_NAME}})
	public final static String DDME_INIT_SCRIPT                            = "DDME_INIT_SCRIPT";                                                   // State object handle for the filename used to initialize the virtualization persistent data (current.sql)
	public final static String DDME_SAVE_SCRIPT                            = "DDME_SAVE_SCRIPT";                                                   // State object handle for the filename used to save state of virtualization persistent data at shutdown (new.sql)
	public final static String DDME_SCHEMA_DIR                             = "DDME_SCHEMA_DIR";                                                    // State object handle for the filename used to hold the schema (no data) for the current project ({{LISA_PROJ_ROOT}}/data/sql)
	public final static String RSP_ENABLE_JSONVERBOSE_META_PROPERTIES	   = "ODATA_RESPONSE_ENABLE_JSONVERBOSE_META_PROPERTIES";				   // State Object handle for enable the jsonverbose properties in response 
	public final static String ODATA_VERSION                               = "ODATA_VERSION";                                                      // State object handle for the version of odata service
	public final static String VALUE_ODATA_VERSION_3                       = "3";                                                      			   // the value of odata service vervion 3
	public final static String VALUE_ODATA_VERSION_4                       = "4";                                                      			   // the value of odata service version 4

	/**
	 * check if the current version is Odata version 4
	 * 
	 * @param String odataVersion
	 * @return boolean: true if it is version 4
	 */
	public static boolean isOdataVersion4(final String odataVersion) {
		if (odataVersion == null)
			return false;
		
		return odataVersion.equals(CommonDefs.VALUE_ODATA_VERSION_4);		
	}
	 

}
