/*******************************************************************************
 * Copyright (c) 2015 CA.  All rights reserved.
 *
 * This source file is licensed under the terms of the Eclipse Public License 1.0
 * For the full text of the EPL please see https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ca.casd.utilities.commonUtils.metadata;

public class EntitySet {

	private String name;
	private String entityType;
	private boolean customSet = false;

	public EntitySet () {
		
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setEntityTypeName( String entityTypeName) {
		this.entityType = entityTypeName;
	}
	
	public String getEntityTypeName() {
		return this.entityType;
	}

	public boolean isCustomSet() {
		return customSet;
	}

	public void setCustomSet(boolean customSet) {
		this.customSet = customSet;
	}		
	
}
