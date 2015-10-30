/*******************************************************************************
 * Copyright (c) 2015 CA.  All rights reserved.
 *
 * This source file is licensed under the terms of the Eclipse Public License 1.0
 * For the full text of the EPL please see https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ca.casd.lisa.plugins.odataassistant.utils;

import com.ca.casd.utilities.commonUtils.util.EnumResourceObjectType;

public class ResourceObject {
	
	String name = "";					// resource name
	String entitySet = "";				// name of entity set
	String entityType = "";				// name of entity type
	EnumResourceObjectType objectType;	// resource type
	
	public ResourceObject(String name) {
		this.name = name;
	}
	
	public static ResourceObject create(String name) {
		return new ResourceObject(name);
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setEntitySet(String name) {
		this.entitySet = name;
	}

	public String getEntitySet() {
		return entitySet;
	}

	public void setEntityType(String type) {
		this.entityType = type;
	}

	public String getEntityType() {
		return entityType;
	}
	public void setObjectType(EnumResourceObjectType type) {
		this.objectType = type;
	}

	public EnumResourceObjectType getObjectType() {
		return objectType;
	}

}