/*******************************************************************************
 * Copyright (c) 2015 CA.  All rights reserved.
 *
 * This source file is licensed under the terms of the Eclipse Public License 1.0
 * For the full text of the EPL please see https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ca.casd.utilities.commonUtils.metadata;


public class EntityType extends StructuredType{

	private boolean customPropsSupported = false;
	private boolean customType = false;
	private String 	customPropertyName = ""; // Specifies the name of the Property to contain the collection of custom properties as Name-Value pairs. 
	
	public EntityType () {
		
	}
	
	public boolean isCustomPropsSupported() {
		return customPropsSupported;
	}

	public void setCustomPropsSupported(boolean customPropsSupported) {
		this.customPropsSupported = customPropsSupported;
	}

	public boolean isCustomType() {
		return customType;
	}

	public void setCustomType(boolean customType) {
		this.customType = customType;
	}

	public void setCustomPropertyName(String customPropertyName) {
		this.customPropertyName = customPropertyName;
	}
	
	public String getCustomPropertyName() {
		return this.customPropertyName;
	}

}
