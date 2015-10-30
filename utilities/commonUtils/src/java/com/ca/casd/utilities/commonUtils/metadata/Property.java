/*******************************************************************************
 * Copyright (c) 2015 CA.  All rights reserved.
 *
 * This source file is licensed under the terms of the Eclipse Public License 1.0
 * For the full text of the EPL please see https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ca.casd.utilities.commonUtils.metadata;

public class Property {

	private String 	name;
	private String 	type = "";
	private String 	length = "";
	private String 	dbColumnName;
	private boolean isKey = false;
	private boolean isCustomProp = false;	// default is false
	private boolean isCollection = false; 	// default is false
	private boolean isComplexType = false; 	// default is false
	private boolean nullable = true; 		// default is true
	private String 	defaultValue = null;	
	private String 	generated = null; 		// value could be 'once' or 'always'
	private String 	generateMethod = null;	// could be 'Init', 'Sequence', ...
											// If no value is specified, or GenMethod is absent, default is 'Init'
	private String 	generatePattern = null;	// GenPattern has a mandatory value which is a format string 
											// supporting the syntax of the Java String.format method.
											// GenPattern is only used if Property is of type String and GenMethod is 'Sequence'
	
	public Property(String name, String dbColumnName, boolean isKey) {
		this.name = name;
		this.dbColumnName = dbColumnName.toUpperCase();
		this.isKey = isKey;
	}
	
	public Property(String name, String dbColumnName) {
		this.name = name;
		this.dbColumnName = dbColumnName.toUpperCase();
		this.isKey = false;
	}
	
    public Property(String name, String dbColumnName, String type) {
        this.name = name;
        this.dbColumnName = dbColumnName.toUpperCase();
        this.type = type;
        this.isKey = false;
    }
	
	public Property(String name, String dbColumnName, String type, String length ) {
		this.name = name;
		this.dbColumnName = dbColumnName.toUpperCase();
		this.type = type;
		this.length = length;
		this.isKey = false;
	}

	public Property(String name, String dbColumnName, String type, String length, boolean isKey) {
		this.name = name;
		this.dbColumnName = dbColumnName.toUpperCase();
		this.type = type;
		this.length = length;
		this.isKey = isKey;
	}
		
	public String getName() {
		return this.name;
	}
	
	public void setDBColumnName(String dbColumnName) {
		this.dbColumnName = dbColumnName.toUpperCase();
	}
	
	public String getDBColumnName() {
		return this.dbColumnName;
	}
	
	public void setIsKey(boolean isKey) {
		this.isKey = isKey;
	}
	
	public boolean isKey() {
		return this.isKey;
	}
	
    public void setType(String type) {
        this.type = type;
    }
  
    public String getType() {
        return this.type;
  	}

	public void setLength(String length) {
		this.length = length;
	}
	
	public String getLength() {
		return this.length;
	}

	public boolean isCustomProp() {
		return isCustomProp;
	}

	public void setCustomProp(boolean isCustomProp) {
		this.isCustomProp = isCustomProp;
	}
	    
	public void setNullable(boolean nullable) {
		this.nullable = nullable;
	}
	
	public boolean isNullable() {
		return this.nullable;
	}

	public void setGenerated(String generated) {
		this.generated = generated;
	}
	
	public String getGenerated() {
		return this.generated;
	}

	public void setGenerateMethod(String generateMethod) {
		this.generateMethod = generateMethod;
	}
	
	public String getGenerateMethod() {
		return this.generateMethod;
	}
	
	//generatePattern
	public void setGeneratePattern(String generatePattern) {
		this.generatePattern = generatePattern;
	}
	
	public String getGeneratePattern() {
		return this.generatePattern;
	}
	
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	public String getDefaultValue() {
		return this.defaultValue;
	}

	public void setCollection(boolean isCollection) {
		this.isCollection = isCollection;
	}	    
	
	public boolean isCollection() {
		return isCollection;
	}

	public void setComplexType(boolean isComplexType) {
		this.isComplexType = isComplexType;
	}
	
	public boolean isComplexType() {
		return isComplexType;
	}

}
