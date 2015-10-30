/*******************************************************************************
 * Copyright (c) 2015 CA.  All rights reserved.
 *
 * This source file is licensed under the terms of the Eclipse Public License 1.0
 * For the full text of the EPL please see https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ca.dvs.utilities.raml;

import java.util.List;

/**
 * General utility methods
 * <p>
 * @author CA Technologies
 * @version 1
 */
public class Util {
	
	/**
	 * Return a string containing an equivalent number of spaces given a tab stop number (1 tab stop = 2 spaces)
	 * <p>
	 * @param tabstop how many tab stops to synthesize a string of spaces for
	 * @return the equivalent string of spaces for the specified tab stop
	 */
	public static String getTabStopSpaces(int tabstop) {
		final int tabwidth = 2;
		return String.format("%"+(tabstop*tabwidth)+"s", " ");
	}
		
	/**
	 * Given a list of string segments, form a single string in camel case form
	 * <p>
	 * @param segmentList string segments provided
	 * @return camel case string transformed from provided string segments
	 */
	public static String camelCase(List<String> segmentList) {
		String camelCase = "";
		
		for (int i=0; i<segmentList.size(); i++) {
			String segment = (String)segmentList.get(i);
			camelCase+= i==0 ? segment.toLowerCase() : segment.substring(0,1).toUpperCase()+segment.substring(1);
		}
		return camelCase;
	}

	/**
	 * Given a JSON string, pad each line so that it appears to have been indented by the specified number of tab stops
	 * @param json the source JSON element to indent
	 * @param tabstop the number of tab stops to indent JSON block
	 * @return json block, each line indented with spaces
	 */
	public static String indentJson(String json, int tabstop) {
		StringBuilder sb = new StringBuilder();
		String[] lines = json.split("\\n");
		for (String line : lines) {
			sb.append(String.format("%s%s", Util.getTabStopSpaces(tabstop), line));
			if (0!="}".compareTo(line)) { // Skip newline on closing brace of JSON so that end of CDATA is always indented
				sb.append("\n");
			}
		}
		return sb.toString();
	}
	
	
}
