/*******************************************************************************
 * Copyright (c) 2015 CA.  All rights reserved.
 *
 * This source file is licensed under the terms of the Eclipse Public License 1.0
 * For the full text of the EPL please see https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ca.casd.utilities.commonUtils.metadata;

import java.util.ArrayList;
import java.util.List;

public class StructuredType {

	private String 	name;
	private String 	baseType;
	private boolean openType = false;
	private boolean attrAbstract = false;
	private String  dbTableName;
	private List<String> keys = new ArrayList<String>();
	private List<Property>properties = new ArrayList<Property>();
	private List<NavigationProperty>navigationProperties = new ArrayList<NavigationProperty>();
	
	//public StructuredType () {
	//}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}

	public void setBaseType(String baseType) {
		this.baseType = baseType;
	}
	
	public String getBaseType() {
		return this.baseType;
	}
	
	public void setOpenType(boolean openType) {
		this.openType = openType;
	}

	public boolean getOpenType() {
		return openType;
	}

	public void setAbstract(boolean attrAbstract) {
		this.attrAbstract = attrAbstract;
	}

	public boolean getAbstract() {
		return attrAbstract;
	}

	public void addKey(String key) {		
		if (!keys.contains(key))
			this.keys.add(key);
	}
	
	public List<String> getKeys() {
		return this.keys;
	}
	
	public void setDBTableName(String name) {
		this.dbTableName = name;
	}
	
	public String getDBTableName() {
		return this.dbTableName;
	}

	public void addProperty(Property property) {
		
		if(getProperty(property.getName()) != null)
			return;
		
		properties.add(property);
		if (property.isKey())
			addKey(property.getName());
	}

	public void removeProperty(String name) {		
		Property property = getProperty(name);
		if(property != null)
			properties.remove(property);
	}
	
	public Property getProperty(String name) {	
		for (Property prop : properties)
			if (prop.getName().equals(name))
				return prop;
		
		return null;
	}
	
	public List<Property> getProperties() {
		return properties;
	}	

	public void addNavigationProperty(NavigationProperty property) {
		if (getNavigationProperty(property.getName()) != null)
			return;
		
		navigationProperties.add(property);
	}
	
	public List<NavigationProperty> getNavigationProperties() {
		return navigationProperties;
	}
	
	public NavigationProperty getNavigationProperty(String name) {
		
		for (NavigationProperty nProp : navigationProperties)
			if (nProp.getName().equals(name))
				return nProp;
		
		return null;
	}
	
	
}
