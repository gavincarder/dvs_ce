/*******************************************************************************
 * Copyright (c) 2015 CA.  All rights reserved.
 *
 * This source file is licensed under the terms of the Eclipse Public License 1.0
 * For the full text of the EPL please see https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ca.casd.utilities.commonUtils.metadata;


public class Association {
	private String name;
	private String dbFKeyTable;
	private String dbJoinOn;
	
	public Association(String name, String dbFKeyTable, String dbJoinOn) {
		this.name = name;
		this.dbFKeyTable = dbFKeyTable;
		this.dbJoinOn = dbJoinOn;

	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}

	public String getDBFKeyTable() {
		return this.dbFKeyTable;
	}
	
	public void setDBFKeyTable(String dbFKeyTable) {
		this.dbFKeyTable = dbFKeyTable;
	}
	
	public String getDBLinkTable() {
		return this.dbFKeyTable;
	}
	
	
	public String getDBJoinOn() {
		return this.dbJoinOn;
	}
	
	public void setDBJoinOn(String dbJoinOn) {
		this.dbJoinOn = dbJoinOn;
	}
	
}
