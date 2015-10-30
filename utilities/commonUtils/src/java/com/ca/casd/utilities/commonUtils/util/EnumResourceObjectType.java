/*******************************************************************************
 * Copyright (c) 2015 CA.  All rights reserved.
 *
 * This source file is licensed under the terms of the Eclipse Public License 1.0
 * For the full text of the EPL please see https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ca.casd.utilities.commonUtils.util;

public enum EnumResourceObjectType {

	COLLECTION("Collection"), ELEMENT("Element"), KEYWORD("KeyWord"), EDMTYPE("DataType"), EDMTYPE_COLLECTION("DataType_Collection"), COMPLEXEDMTYPE("ComplexDataType"), COMPLEXEDMTYPE_COLLECTION("ComplexDataType_Collection"), VALUE("value"), COUNT("count");
    private String objectType;
 
    private EnumResourceObjectType(String objectType) {
    	this.objectType = objectType;
    }

     public String getResourceObjectType() {
    	 return objectType;
    }
}
