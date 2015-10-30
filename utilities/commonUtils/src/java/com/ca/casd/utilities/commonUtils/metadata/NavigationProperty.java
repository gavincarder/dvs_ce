/*******************************************************************************
 * Copyright (c) 2015 CA.  All rights reserved.
 *
 * This source file is licensed under the terms of the Eclipse Public License 1.0
 * For the full text of the EPL please see https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ca.casd.utilities.commonUtils.metadata;

public class NavigationProperty {
	
	private String name;
	private String relationship; 
	private String entityType;
	private String multiplicity;
	//private String dbLinkTable;  /* use this to setup many-to-many relationship */	
	
	/*
	public NavigationProperty(String name, String entityType, String dbLinkTable, String dbJoinOn) {
		this.name = name;
		this.entityType = entityType;
		this.dbLinkTable = dbLinkTable;
		this.dbJoinOn = dbJoinOn;

	}*/
	
	
	public NavigationProperty(String name, String toEntityType, String relationship, String multiplicity) {
		this.name = name;
		this.entityType = toEntityType;
		this.relationship = relationship;
		this.multiplicity = multiplicity;
	}
	
	public NavigationProperty() {
		// TODO Auto-generated constructor stub
	}

	public String getName() {
		return this.name;
	}
	
	public String getRelationship() {
		return this.relationship;
	}
	
	/*
	public String getDBJoinOn() {
		return this.dbJoinOn;
	}
	
	public void setDBJoinOn(String dbJoinOn) {
		this.dbJoinOn = dbJoinOn;
	}
	
	*/
	public String getEntityTypeName() {
		return this.entityType;
	}
	
	
	public String getMultiplicity() {
		return this.multiplicity;
	}

	public void setMultiplicity(String multiplicity) {
		// TODO Auto-generated method stub
		this.multiplicity = multiplicity;
		
	}
	
}
