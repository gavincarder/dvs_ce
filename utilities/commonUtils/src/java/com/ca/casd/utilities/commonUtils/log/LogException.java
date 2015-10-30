/*******************************************************************************
 * Copyright (c) 2015 CA.  All rights reserved.
 *
 * This source file is licensed under the terms of the Eclipse Public License 1.0
 * For the full text of the EPL please see https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ca.casd.utilities.commonUtils.log;

public class LogException extends Exception {
	
/**	Public string constant required by CA									*/
	public static final String CACOPYRIGHT = "(c) 2015 CA.  All rights reserved.";

	private static final long serialVersionUID = 1L;
	
	String strMessage = "";
	
	public LogException (String strMessage) {
		
		super();
		this.strMessage = strMessage;
	}
	
	public LogException (String strMessage, Throwable throwable) {
		
		super(throwable);
		this.strMessage = strMessage;
		
		if ( this.getCause() != null ) {
			
			this.strMessage += " " + this.getCause().getMessage();
		}
	}
	
	public String getMessage () {
		
		return this.strMessage;
	}
}
