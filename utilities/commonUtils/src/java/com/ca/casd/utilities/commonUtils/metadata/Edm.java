/*******************************************************************************
 * Copyright (c) 2015 CA.  All rights reserved.
 *
 * This source file is licensed under the terms of the Eclipse Public License 1.0
 * For the full text of the EPL please see https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ca.casd.utilities.commonUtils.metadata;

/**
 * 
 * Edm class - convenience definitions and utility functions for testing Edm data types
 * @author artal03
 *
 */
public final class Edm { // EDM Primitive types
	
	public static final String Binary					= "Edm.Binary";
	public static final String Boolean					= "Edm.Boolean";					// SQL: BOOLEAN
	public static final String Byte				        = "Edm.Byte";						// SQL: TINYINT
	public static final String DateTime					= "Edm.DateTime";					// SQL: TIMESTAMP
	public static final String DateTimeOffset			= "Edm.DateTimeOffset";
	public static final String Decimal					= "Edm.Decimal";					// SQL: DECIMAL
	public static final String Double					= "Edm.Double";						// SQL: DOUBLE
	public static final String Float					= "Edm.Float";						// SQL: FLOAT
	public static final String Geography				= "Edm.Geography";
	public static final String GeographyPoint			= "Edm.GeographyPoint";
	public static final String GeographyLineString		= "Edm.GeographyLineString";
	public static final String GeographyPolygon			= "Edm.GeographyPolygon";
	public static final String GeographyMultiPoint		= "Edm.GeographyMultiPoint";
	public static final String GeographyMultiLineString	= "Edm.GeographyMultiLineString";
	public static final String GeographyMultiPolygon	= "Edm.GeographyMultiPolygon";
	public static final String GeographyCollection		= "Edm.GeographyCollection";
	public static final String Geometry					= "Edm.Geometry";
	public static final String GeometryPoint			= "Edm.GeometryPoint";
	public static final String GeometryLineString		= "Edm.GeometryLineString";
	public static final String GeometryPolygon			= "Edm.GeometryPolygon";
	public static final String GeometryMultiPoint		= "Edm.GeometryMultiPoint";
	public static final String GeometryMultiLineString	= "Edm.GeometryMultiLineString";
	public static final String GeometryMultiPolygon		= "Edm.GeometryMultiPolygon";
	public static final String GeometryCollection		= "Edm.GeometryCollection";
	public static final String Guid						= "Edm.Guid";						// SQL: GUID
	public static final String Int16					= "Edm.Int16";						// SQL: SMALLINT
	public static final String Int32					= "Edm.Int32";						// SQL: INT
	public static final String Int64					= "Edm.Int64";						// SQL: BIGINT
	public static final String SByte					= "Edm.SByte";						// SQL: TINYINT
	public static final String Single					= "Edm.Single";						// SQL: FLOAT
	public static final String Stream					= "Edm.Stream";
	public static final String String					= "Edm.String";						// SQL: VARCHAR
	public static final String Time						= "Edm.Time";						// SQL: TIME
	public static final String TimeOfDay				= "Edm.TimeOfDay";					// SQL: TIME
	public static final String Date						= "Edm.Date";						// SQL: Date
	
	static boolean isPrimitiveTypeString(String typeString) {
		
		boolean isPrimitiveType = false;
		
		switch(typeString) {
		case Edm.Binary:
		case Edm.Boolean:
		case Edm.Byte:
		case Edm.Date:
		case Edm.DateTime:
		case Edm.DateTimeOffset:
		case Edm.Decimal:
		case Edm.Double:
		case Edm.Float:
		case Edm.Geography:
		case Edm.GeographyPoint:
		case Edm.GeographyLineString:
		case Edm.GeographyPolygon:
		case Edm.GeographyMultiPoint:
		case Edm.GeographyMultiLineString:
		case Edm.GeographyMultiPolygon:
		case Edm.GeographyCollection:
		case Edm.Geometry:
		case Edm.GeometryPoint:
		case Edm.GeometryLineString:
		case Edm.GeometryPolygon:
		case Edm.GeometryMultiPoint:
		case Edm.GeometryMultiLineString:
		case Edm.GeometryMultiPolygon:
		case Edm.GeometryCollection:
		case Edm.Guid:
		case Edm.Int16:
		case Edm.Int32:
		case Edm.Int64:
		case Edm.SByte:
		case Edm.Single:
		case Edm.String:
		case Edm.Time:
		case Edm.TimeOfDay:
		case Edm.Stream:
		isPrimitiveType = true;
			break;
		default:
			isPrimitiveType = false;
		}
		
		return isPrimitiveType;
	}
	
	static boolean isValidStringValue(Edm edm, String strValue) {
		
		boolean isValid = true;
		
		String edmType = edm.toString();
		
		switch(edmType) {
    	
	    	case Edm.Boolean:  // validate before sending to db (SQL:BOOLEAN)
	    	{ // ensure that boolean values are only "true", "false" or null
	    		if (null!=strValue) {
	        		switch(strValue.toLowerCase()) {
	        		case "true":
	        		case "false":
	        			isValid = true;
	        			break;
	    			default:
	        			isValid = false;
    					System.out.println(java.lang.String.format("Value %s is not valid for type %s (must be true or false)", strValue, edmType));
	        		}
	    		}
	    	}
	    	break;
	
			case Edm.Int16:		// pass to DB for validation (SQL:SMALLINT)
			case Edm.Int32:		// pass to DB for validation (SQL:INT)
			case Edm.Int64:		// pass to DB for validation (SQL:BIGINT)
			{
				if (null!=strValue) {
	    			try {
	    				long l = Long.parseLong(strValue);
	    				if ((edmType.equals(Edm.Int16) && ((l < Short.MIN_VALUE)   || (l > Short.MAX_VALUE)))    ||
	    					(edmType.equals(Edm.Int32) && ((l < Integer.MIN_VALUE) || (l > Integer.MAX_VALUE)))  ||
	    					(edmType.equals(Edm.Int64) && ((l < Long.MIN_VALUE)    || (l > Long.MAX_VALUE)))) {
	    					System.out.println(java.lang.String.format("Value %s is out of range for type %s", strValue, edmType));
	        				isValid = false;
	    				}
	    			} catch(NumberFormatException e) {
    					System.out.println(java.lang.String.format("Value %s is out of range for type %s - %s", strValue, edmType, e.getMessage()));
	    				isValid = false;
	    			}
				}
			}
			break;
				
			case Edm.Date:		// pass to DB for validation (SQL:DATE)
			case Edm.DateTime:	// pass to DB for validation (SQL:TIMESTAMP)
			case Edm.DateTimeOffset: // pass to DB for validation (SQL:TIMESTAMP)
			case Edm.Float:		// pass to DB for validation (SQL:FLOAT)
			case Edm.Single:	// pass to DB for validation (SQL:FLOAT)
			case Edm.Double:	// pass to DB for validation (SQL:DOUBLE)
			case Edm.Guid:		// pass to DB for validation (SQL:GUID)
			case Edm.String:	// pass to DB for validation (SQL:VARCHAR)
			case Edm.Time:		// pass to DB for validation (SQL:TIME)
			case Edm.TimeOfDay:	// pass to DB for validation (SQL:TIME)
				break;
				
			case Edm.Binary:					// unsupported type
			case Edm.Byte: 						// unsupported type
			case Edm.Decimal:					// pass to DB for validation (SQL:DECIMAL)
			case Edm.Geography:					// unsupported type
			case Edm.GeographyPoint:			// unsupported type
			case Edm.GeographyLineString:		// unsupported type
			case Edm.GeographyPolygon:			// unsupported type
			case Edm.GeographyMultiPoint:		// unsupported type
			case Edm.GeographyMultiLineString:	// unsupported type
			case Edm.GeographyMultiPolygon:		// unsupported type
			case Edm.GeographyCollection:		// unsupported type
			case Edm.Geometry:					// unsupported type
			case Edm.GeometryPoint:				// unsupported type
			case Edm.GeometryLineString:		// unsupported type
			case Edm.GeometryPolygon:			// unsupported type
			case Edm.GeometryMultiPoint:		// unsupported type
			case Edm.GeometryMultiLineString:	// unsupported type
			case Edm.GeometryMultiPolygon:		// unsupported type
			case Edm.GeometryCollection:		// unsupported type
			case Edm.SByte:						// unsupported type
			case Edm.Stream:					// unsupported type
			default:
				System.out.println(java.lang.String.format("Type %s is unsupported", edmType));
				isValid = false;
		}
		return isValid;
	}
}
