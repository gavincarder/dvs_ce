/*******************************************************************************
 * Copyright (c) 2015 CA.  All rights reserved.
 *
 * This source file is licensed under the terms of the Eclipse Public License 1.0
 * For the full text of the EPL please see https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ca.casd.utilities.commonUtils.util;

import com.ca.casd.utilities.commonUtils.metadata.Edm;

public class Util {

	public final class H2 { // EDM Primitive types
		
		public static final String BOOLEAN			= "BOOLEAN";
		public static final String TIME				= "TIME";					
		public static final String VARCHAR			= "VARCHAR";					
		public static final String TIMESTAMP		= "TIMESTAMP";					
		public static final String REAL				= "REAL";				
		public static final String DOUBLE			= "DOUBLE";	
		public static final String SMALLINT			= "SMALLINT";
		public static final String INT				= "INT";	
		public static final String INTEGER			= "INTEGER";	
		public static final String BIGINT			= "BIGINT";	
		public static final String UUID				= "UUID";
		public static final String DATE				= "DATE";					
	}

	/**
	 * dequoteString
	 * @param s
	 * @return
	 */
	public static String dequoteString(String s) {
		
		String dequoteStr = s.trim();
		
		if(dequoteStr.startsWith("'") && dequoteStr.endsWith("'")) {
			dequoteStr = dequoteStr.substring(1);
			dequoteStr = dequoteStr.substring(0,dequoteStr.length()-1);
			
			return dequoteStr;
		}
		
		return s;
	}
	
	/**
	 * normalizeQuotes
	 * @param s
	 * @return
	 */
	public static String normalizeQuotes(String s) {
		
		return dequoteString(s).replace("''", "'");
		
	}
	
	/**
	 * quoteString
	 * @param s
	 * @return
	 */
	public static String quoteString(String s) {
		
		String quoteStr = s.trim();
		
		if(!quoteStr.startsWith("'") && !quoteStr.endsWith("'")) {
			quoteStr = "'" + quoteStr + "'";
			
			return quoteStr;
		}
		
		return s;
	}
	
	/**
	 * isValidDBString
	 * @param s
	 * @return
	 */
	public static boolean isValidDBString(String s) {
		
		if (dequoteString(s).replace("''", "").indexOf("'") != -1)
			return false;
		
		s=s.trim();
		if(!s.startsWith("'") && !s.endsWith("'")) {
			if (s.indexOf(";") != -1)
				return false;
		}
		return true;
	}
	
	/**
	 * isStringOfEdmType
	 * @param s
	 * @param edmType
	 * @return true  if it is a EDM type
	 * @throws NumberFormatException
	 */
	public static boolean isStringOfEdmType(String s, String edmType) throws NumberFormatException{
 		boolean retVal = true;
 		
 		try {
 			switch (edmType) { 			
				case Edm.Int16:
 					Short.parseShort(Util.dequoteString(s));
 					break;
 				case Edm.Int32:
 					Integer.parseInt(Util.dequoteString(s));
 					break;
 				case Edm.Int64:
 					Long.parseLong(Util.dequoteString(s));
 					break;
 				case Edm.Float:
 				case Edm.Single:
 					Float.parseFloat(Util.dequoteString(s));
 					break;
 				case Edm.Double:
 					Double.parseDouble(Util.dequoteString(s));
 					break;
 				case Edm.Boolean:
 					Boolean.parseBoolean(Util.dequoteString(s));
 					break;
 				case Edm.String:
 					retVal = true;
 					break;
				case Edm.Date:
 				case Edm.DateTime:
 				case Edm.DateTimeOffset:
 				case Edm.Time:
 				case Edm.TimeOfDay:
 				case Edm.Guid:
 					return false;
 			}
 		}catch (NumberFormatException e) {
 			retVal = false;
 			throw e;
 		}
 		return retVal;
 	
 	}
	
	/**
	 * isQualifiedEdmDataType
	 * @param datatype
	 * @return true if it is a qualified Edm DataType
	 */
	public static boolean isQualifiedEdmDataType(final String datatype) {
		boolean isValid = false;
	    	 
		switch (datatype) {

			case Edm.Boolean:
		   	case Edm.Date:
		   	case Edm.DateTime:
		   	case Edm.DateTimeOffset:
		   	case Edm.Double:
		   	case Edm.Float:
		   	case Edm.Guid:
		   	case Edm.Int16:
		   	case Edm.Int32:
		   	case Edm.Int64:
		   	case Edm.String:
			case Edm.Single:
			case Edm.Time:
			case Edm.TimeOfDay:
			//case Edm.Decimal:
			//case "Edm.Binary":
		   	//case "Edm.Byte":
			//case "Edm.Int16":
			//case "Edm.SByte":
		   		isValid = true;
				break;
					
		   	default:
			  	break;	
	   	}
		
		return isValid;
	}
	
	/**
	 * convert edm type into H2 data type
	 * @param type - emd data type
	 * @param length - lenght for String 
	 * @return - H2 data type
	 */
	public static String edmToH2DataType(String type, String length) {
		// Reference: http://h2database.com/html/datatypes.html
		String sqlType = type;
		
		switch (type) {
		
			case Edm.String:
				//Mapped to java.lang.String
				sqlType = H2.VARCHAR;
				if (!length.isEmpty())
					sqlType = sqlType + "(" + length + ")";
				break;
			case Edm.Date:
				// The date data type. The format is yyyy-MM-dd.
				//Mapped to java.sql.Date	
				sqlType = H2.DATE;	
				break;
			case Edm.DateTime:
			case Edm.DateTimeOffset:
				//The format is yyyy-MM-dd hh:mm:ss[.nnnnnnnnn].
				//Mapped to java.sql.Timestamp (java.util.Date is also supported).
				sqlType = H2.TIMESTAMP;	
				break;
			case Edm.Time:
			case Edm.TimeOfDay:
				//The format is hh:mm:ss
				//Mapped to java.sql.Time
				sqlType = H2.TIME;
				break;
			case Edm.Int16:
				//Mapped to java.lang.Short
				sqlType = H2.SMALLINT;
				break;
			case Edm.Int32:
				//Mapped to java.lang.Integer
				sqlType = H2.INT;
				break;
			case Edm.Int64:
				//Mapped to java.lang.Long
				sqlType = H2.BIGINT;
				break;
		   	case Edm.Float:
			case Edm.Single:
				//Mapped to java.lang.Float
				sqlType = H2.REAL;
				break;			
			case Edm.Double:
				//Mapped to java.lang.Double
				sqlType = H2.DOUBLE;
				break;
			case Edm.Boolean:
				//Mapped to java.lang.Boolean
				sqlType = H2.BOOLEAN;
				break;
			case Edm.Guid:
				//Mapped to java.util.UUID
				sqlType = H2.UUID;
				break;			
		}
		
		return sqlType;
		
	}
	
	/**
	 * Converts H2 data type into EDM data type
	 * @param dbType - H2 data type
	 * @return - EDM data type
	 */
	public static String H2DataTypeToEdm(String dbType) {
    	
    	String edmType = dbType;
    	
		switch (dbType) {		
			case H2.VARCHAR:
				edmType = Edm.String;
				break;
			case H2.DATE:
				edmType = Edm.Date;	
				break;
			case H2.TIME:
				edmType = Edm.Time;	
				break;
			case H2.TIMESTAMP:
				edmType = Edm.DateTime;	
				break;				
			case H2.SMALLINT:
				edmType = Edm.Int16;	
				break;
			case H2.INT:
			case H2.INTEGER:
				edmType = Edm.Int32;
				break;				
			case H2.BIGINT:
				edmType = Edm.Int64;
				break;				
			case H2.REAL:	
				edmType = Edm.Single;
				break;				
			case H2.DOUBLE:
				edmType = Edm.Double;
				break;				
			case H2.BOOLEAN:
				edmType = Edm.Boolean;
				break;				
			case H2.UUID:
				edmType = Edm.Guid;
				break;
		}
    	
    	return edmType;
    			
    }

	/**
	 * Get the value object based on the data type
	 * @param dbType - H2 data type
	 * @param value - value
	 * @return object of value
	 */
	public static Object getValueObject(String dbType, String value) {
    	
		switch (dbType) {		
			case H2.INT:
			case H2.INTEGER:				
				return new Integer(value);
				
			case H2.SMALLINT:
				return new Short(value);
				
			case H2.BIGINT:
				return new Long(value);
				
			case H2.REAL:	
				return new Float(value);
				
			case H2.DOUBLE:
				return new Double(value);
				
			case H2.BOOLEAN:
				return new Boolean(value);
				
		}
    	
    	return value;
    			
    }
	
}
