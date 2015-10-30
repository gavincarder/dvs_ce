/*******************************************************************************
 * Copyright (c) 2015 CA.  All rights reserved.
 *
 * This source file is licensed under the terms of the Eclipse Public License 1.0
 * For the full text of the EPL please see https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ca.dvs.utilities.raml;

import java.util.LinkedHashMap;
import java.util.ArrayList;

import com.google.gson.annotations.SerializedName;

/**
 * Map RAML based JSON schema into a Java object
 * <p>
 * @author CA Technologies
 * @version 1
 */
public final class Schema {
	
	@SerializedName("$schema")
	public String _schema;
	
	@SerializedName("type")
	public String type;
	
	@SerializedName("description")
	public String description;
	
	@SerializedName("properties")
	public LinkedHashMap<String, Object> properties;

	@SerializedName("primaryKeys")
	public ArrayList<String> primaryKeys;
}
