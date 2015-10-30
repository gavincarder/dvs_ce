/*******************************************************************************
 * Copyright (c) 2015 CA.  All rights reserved.
 *
 * This source file is licensed under the terms of the Eclipse Public License 1.0
 * For the full text of the EPL please see https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ca.dvs.utilities.lisamar;

import java.util.LinkedHashMap;
import java.util.Map;

public class LisaMarObject {
	
	String lisafile = "";			// export file name with the full path
	Map<String, Object> errors = new LinkedHashMap<String, Object>();		// errors
	Map<String, Object> warnings = new LinkedHashMap<String, Object>(); 	// warnings
	
	public LisaMarObject() {
		lisafile = "";
	}
	
	public void setLisafile(String lisafile) {
		this.lisafile = lisafile;
	}

	public String getLisafile() {
		return lisafile;
	}

	public Map<String, Object> getWarnings() {
		return warnings;
	}
	
	public void setWarnings(Map<String, Object> warnings) {
		this.warnings = warnings;
	}

	public void appendWarnings(String key, Object value) {
		this.warnings.put(key, value);
	}

	public Map<String, Object> getErrors() {
		return errors;
	}
	
	public void setErrors(Map<String, Object> errors) {
		this.errors = errors;
	}

	public void appendErrors(String key, Object value) {
		this.errors.put(key, value);
	}

}