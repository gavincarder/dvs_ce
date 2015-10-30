/*******************************************************************************
 * Copyright (c) 2015 CA.  All rights reserved.
 *
 * This source file is licensed under the terms of the Eclipse Public License 1.0
 * For the full text of the EPL please see https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ca.dvs.utilities.raml;

import java.io.Writer;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.raml.model.Raml;
import org.raml.model.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.gson.Gson;

/**
 * EDM transformation class
 * <p>
 * 
 * @author CA Technologies
 * @version 1
 */
public class EDM {

	private class Association {
		
		private String   name;
		private String   sourceType;
		private String   targetType;
		
		/**
		 * Create an Association object from source and target types
		 * <p>
		 * @param sourceType 
		 * @param targetType
		 */
		public Association(String sourceType, String targetType) {
			setName(String.format("%s_%s", sourceType, targetType));
			setSourceType(sourceType);
			setTargetType(targetType);
		}
		
		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}

		/**
		 * @param name the name to set
		 */
		public void setName(String name) {
			this.name = name;
		}

		/**
		 * @return the sourceEntity
		 */
		public String getSourceType() {
			return sourceType;
		}

		/**
		 * @param sourceEntity the sourceEntity to set
		 */
		public void setSourceType(String sourceType) {
			this.sourceType = sourceType;
		}

		/**
		 * @return the targetType
		 */
		public String getTargetType() {
			return targetType;
		}

		/**
		 * @param targetType the targetType to set
		 */
		public void setTargetType(String targetType) {
			this.targetType = targetType;
		}

		/**
		 * @return the dbLinkTable
		 */
		public String getDbLinkTable() {
			return String.format("%s_%s", sourceType.toUpperCase(), targetType.toUpperCase());
		}

	};
	
	private enum PropertyClass {
		EntityProperty, NavigationProperty;
	}
	
	private enum PropertyFlag {
		KEY, NOT_NULLABLE; 
	}
	
	private class EntityProperty {
		private String  name;
		private PropertyClass propertyClass;
		private String  type;
		private String  dbColumn;
		private int     maxLength;
		private EnumSet<PropertyFlag> flags;
		private String  relationship;
		
		public EntityProperty() {
			setName(null);
			setPropertyClass(PropertyClass.EntityProperty);
			setType(null);
			setDbColumn(null);
			setMaxLength(0);
			flags = EnumSet.noneOf(PropertyFlag.class);
			setRelationship(relationship);
		}
				
		public EntityProperty(String propName, Map<String, Object> propDef) {
			this();
			setName(propName);
			String type = propDef.get("type").toString();

			setType(edmTypeFromRamlType(type)); // update later if/when we have more information

			if (type.equalsIgnoreCase("array")) {
				Object itemObj = propDef.get("items");
				if (itemObj instanceof Map) {
					try {
						@SuppressWarnings("unchecked")
						Map<String, Object> items = (Map<String, Object>)itemObj;
						Object itRefObj = items.get("$ref");
						if (itRefObj instanceof String) {
							setPropertyClass(PropertyClass.NavigationProperty);
							setType((String)itRefObj);
						}
					} catch (ClassCastException cce) {
						System.out.println(String.format("Error parsing property %s (type=%s) - %s", propName, type, cce));
					}
				}
			} else {
				setType(edmTypeFromRamlType(type));
			}
		}
		
		public EntityProperty(String name, String type) {
			this();
			setName(name);
			setType(type);
		}

		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}

		/**
		 * @param name the name to set
		 */
		public void setName(String name) {
			this.name = name;
		}

		/**
		 * @return the propertyClass
		 */
		public PropertyClass getPropertyClass() {
			return propertyClass;
		}

		/**
		 * @param propertyClass the propertyClass to set
		 */
		public void setPropertyClass(PropertyClass propertyClass) {
			this.propertyClass = propertyClass;
		}

		/**
		 * @return the type
		 */
		public String getType() {
			return type;
		}

		/**
		 * @param type the type to set
		 */
		public void setType(String type) {
			this.type = type;
		}

		/**
		 * @return the dbColumn
		 */
		@SuppressWarnings("unused")
		public String getDbColumn() {
			return dbColumn;
		}

		/**
		 * @param dbColumn the dbColumn to set
		 */
		public void setDbColumn(String dbColumn) {
			this.dbColumn = dbColumn;
		}

		/**
		 * @return the maxLength
		 */
		@SuppressWarnings("unused")
		public int getMaxLength() {
			return maxLength;
		}

		/**
		 * @param maxLength the maxLength to set
		 */
		public void setMaxLength(int maxLength) {
			this.maxLength = maxLength;
		}

		/**
		 * @return the flags
		 */
		public EnumSet<PropertyFlag> getFlags() {
			return flags;
		}

		/**
		 * @param flags the flags to set
		 */
		public void setFlags(EnumSet<PropertyFlag> flags) {
			this.flags = flags;
		}
				
		/**
		 * @param flag the PropertyFlag to set
		 */
		@SuppressWarnings("unused")
		public void clearFlags() {
			this.flags.clear();
		}
				
		/**
		 * @param flag the PropertyFlag to set
		 */
		public void setFlag(PropertyFlag flag) {
			this.flags.add(flag);
		}
								
		/**
		 * @param flag the PropertyFlag to check
		 * @return boolean - if specified PropertyFlag is set
		 */
		public boolean isSet(PropertyFlag flag) {
			return getFlags().contains(flag);
		}

		/**
		 * @return the relationship
		 */
		public String getRelationship() {
			return relationship;
		}

		/**
		 * @param relationship the relationship to set
		 */
		public void setRelationship(String relationship) {
			this.relationship = relationship;
		}

	};
			
	private class EntityType {
		private String               name;
		private Schema               schema;
		private String               dbTable;
		private String               entitySetRefType;
		private boolean              isEntitySetSchema;
		private List<EntityProperty> properties;
		private List<Association>    associations;
		private List<String>		 primaryKeys;

		public EntityType() {
			setName(null);
			setSchema(null);
			setDbTable(null);
			setEntitySetRefType(null);
			setIsEntitySetSchema(false);
			properties   = new ArrayList<EntityProperty>();
			associations = new ArrayList<Association>();
			primaryKeys	 = new ArrayList<String>();

		}
		
		public EntityType(String name, Schema schema) {
			this();
			setName(name);
			setSchema(schema);
			if (null != schema.primaryKeys) {
				setPrimaryKeys(schema.primaryKeys);
			}
			for (Entry<String, Object> propEntry : schema.properties.entrySet()) {
				String propName = propEntry.getKey();
				Object propVal  = propEntry.getValue();
				if (propVal instanceof Map) {
					try {
						@SuppressWarnings("unchecked")
						Map<String, Object> propDef = (Map<String, Object>)propVal;
						EntityProperty entityProperty = new EntityProperty(propName, propDef);
						if (entityProperty.getPropertyClass().equals(PropertyClass.NavigationProperty)) {
							setEntitySetRefType(entityProperty.getType());
							entityProperty.setRelationship(String.format("%s_%s", this.getName(), entityProperty.getType()));
							addAssociation(new Association(getName(), entityProperty.getType()));
						} else {
							if (getPrimaryKeys().contains(entityProperty.getName())) {
								entityProperty.setFlag(PropertyFlag.KEY);
								entityProperty.setFlag(PropertyFlag.NOT_NULLABLE);
							}
						}
						addProperty(entityProperty);
					} catch (ClassCastException cce) {
						System.out.println(String.format("Error parsing schema property %s - %s", propName, cce));
					}
				}
			}
		}
		
		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}

		/**
		 * @param name the name to set
		 */
		public void setName(String name) {
			this.name = name;
		}

		/**
		 * @return the schema
		 */
		@SuppressWarnings("unused")
		public Schema getSchema() {
			return schema;
		}

		/**
		 * @param schema the schema to set
		 */
		public void setSchema(Schema schema) {
			this.schema = schema;
		}

		/**
		 * @return the dbTable
		 */
		public String getDbTable() {
			return dbTable;
		}

		/**
		 * @param dbTable the dbTable to set
		 */
		public void setDbTable(String dbTable) {
			this.dbTable = dbTable;
		}

		public void setIsEntitySetSchema(boolean isEntitySetSchema) {
			this.isEntitySetSchema = isEntitySetSchema;
		}
		/**
		 * @return the entitySetRefType
		 */
		public String getEntitySetRefType() {
			return entitySetRefType;
		}

		/**
		 * @param entitySetRefType the entitySetRefType to set
		 */
		public void setEntitySetRefType(String entitySetRefType) {
			this.entitySetRefType = entitySetRefType;
		}

		/**
		 * @return the properties
		 */
		public List<EntityProperty> getProperties() {
			return properties;
		}

		public void addProperty(EntityProperty property) {
			if (property.getPropertyClass().equals(PropertyClass.NavigationProperty)) {
				setEntitySetRefType(property.getType());
				property.setRelationship(String.format("%s_%s", getName(), property.getType()));
			}
			properties.add(property);
		}
		
		/**
		 * @return the associations
		 */
		public List<Association> getAssociations() {
			return associations;
		}

		/**
		 * @param the Association to add
		 */
		public void addAssociation(Association association) {
			associations.add(association);
		}

		/**
		 * @return the primaryKeys
		 */
		public List<String> getPrimaryKeys() {
			return primaryKeys;
		}

		/**
		 * @param primaryKeys the primaryKeys to set
		 */
		public void setPrimaryKeys(List<String> primaryKeys) {
			this.primaryKeys = primaryKeys;
		}

	}
	
	private String                      namespace       = null;
	private String                      version         = null;
	private Map<String, EntityType>     entityTypes		= null;
	
	/**
	 * @param raml document base for EDM file
	 */
	public EDM(Raml raml) {
		this.namespace       = raml.getTitle().replaceAll("\\s", "");
		this.version         = raml.getVersion();
		this.entityTypes     = getEntityTypes(raml);
	}
	
	/**
	 * @param the EDM Document
	 * @return the EDM root element
	 */
	private Element createSchemaElement(Document doc) {
        Element element = doc.createElement("Schema");
        
        element.setAttribute("xmlns",              "http://schemas.microsoft.com/ado/2006/04/edm");
        element.setAttribute("xmlns:m",            "http://schemas.microsoft.com/ado/2007/08/dataservices/metadata");
        element.setAttribute("xmlns:d",            "http://schemas.microsoft.com/ado/2007/08/dataservices");

        element.setAttribute("Namespace",          namespace);
        element.setAttribute("Version",            version);

        return element;
	}

	/**
	 * @param  the EDM Document
	 * @param  the EntityProperty
	 * @return the EDM property element
	 */
	private Element createEntityTypePropertyElement(Document doc, EntityProperty property) {
		
		Element element = null;
		
		boolean isNavigationProperty = (property.getPropertyClass() == PropertyClass.NavigationProperty);
		
		element = doc.createElement(isNavigationProperty ? "NavigationProperty" : "Property");

		element.setAttribute("Name",            property.getName());
		element.setAttribute("Type",            property.getType());
		
		if (isNavigationProperty) {
			element.setAttribute("Multiplicity",          "*");
			element.setAttribute("Relationship", property.getRelationship());
		} else { // !isNavigationProperty
			//element.setAttribute("DBColumn", property.getName());                  // DBcolumn=<name> unless otherwise specified
			if (property.isSet(PropertyFlag.NOT_NULLABLE)){                             // Nullable=true unless otherwise specified
				element.setAttribute("Nullable", Boolean.valueOf(false).toString());
			}
			if (property.isSet(PropertyFlag.KEY)){                                   // Key=false unless otherwise specified
				element.setAttribute("Key", Boolean.valueOf(true).toString());
			}
		}
        
        return element;
	}

	/**
	 * @param  the EDM Document
	 * @param  the RAML resource to create EntityType from
	 * @return the EDM EntityType element
	 */
	private Element createEntityTypeElement(Document doc, EntityType entityType) {
        Element element = doc.createElement("EntityType");
               
        element.setAttribute("Name",       entityType.getName());
        
        String dbTable = entityType.getDbTable();
        if (null!=dbTable) {
        	element.setAttribute("DBTable",    dbTable);
        }

        for (EntityProperty entityProperty : entityType.getProperties()) {
        	element.appendChild(createEntityTypePropertyElement(doc, entityProperty));
        }

        return element;
	}
		
	/**
	 * @return the document
	 * @throws Exception 
	 */
	private Document createDocument() throws Exception {

		DocumentBuilderFactory dbf      = DocumentBuilderFactory.newInstance();
        DocumentBuilder        builder  = dbf.newDocumentBuilder();
	        
        return builder.newDocument();

	}
	
	/**
	 * @param  RAML resources anchor
	 * @return list of EntitySetNames
	 */
	private Set<String> getEntitySetNames(Map<String, Resource> resources) {
		
		Set<String> entitySetNames = new HashSet<String>();
		if (null!=resources) { // there are resources

			for (Entry<String, Resource> resEntry : resources.entrySet()) {
				String   key           = resEntry.getKey();
				Pattern  pattern       = Pattern.compile("/(?<entitySet>.*?)\\((?<keyValue>.*?)\\)");
				Matcher  matcher       = pattern.matcher(key);
				String   entitySetName = null;
				if (matcher.find()) {
					entitySetName = matcher.group("entitySet").trim();

					if (!entitySetNames.contains(entitySetName)) {
						entitySetNames.add(entitySetName);
					}
				}
			}
		}
		
		return entitySetNames;
	}

	private Map<String, EntityType> getEntityTypes(Raml raml) {
		Map<String, EntityType> schemas = null;
		if (null!=raml) {
			// The schemas are a mapping from a schema name to a JSON object
			//{   "$schema": "http://json-schema.org/draft-04/schema",
			//    "type": "object",
			//    "description": "A single author",
			//    "properties": {
			//        "id": { "type": "integer" },
			//        "name":  { "type": "string" },
			//        "books": {
			//            "type": "array",
			//            "items": {
			//                "$ref":"book"
			//            }
			//        }
			//    },
			//    "required": [ "id", "name" ],
			//    "primaryKeys": [ "id" ]
			//}
			List<Map<String, String>> ramlSchemas = raml.getSchemas();
			schemas = new LinkedHashMap<String, EntityType>();
			Set<String> entitySetNames = getEntitySetNames(raml.getResources());
			for (Map<String, String> ramlSchemaMap : ramlSchemas) {
				for (Entry<String, String> schemaEntry : ramlSchemaMap.entrySet()) {
					String key    = schemaEntry.getKey();
					//System.out.println("RAML Schema: "+key);
					Schema schema = createSchema(schemaEntry.getValue());  
					EntityType entityType = new EntityType(key, schema);
					if (entitySetNames.contains(key)) {
						entityType.setIsEntitySetSchema(true);
					}
					schemas.put(key, entityType);
				}
			}
		}
		return schemas;
	}
	
	private Schema createSchema(String json) {
		Schema schema = null;
		if (null!=json  && !json.isEmpty()) {
			Gson gson = new Gson();
			schema = gson.fromJson(json, Schema.class);
		}
		return schema;
	}
	
	/**
	 * Construct the EntitySet Document Element for the specified EntityType
	 * <p>
	 * @param doc the Document in which to create the Element
	 * @param entityType the EntityType 
	 * @return the EntitySet Document Element
	 */
	public Element createEntitySetElement(Document doc, EntityType entityType) {
		Element element = doc.createElement("EntitySet");
		
		element.setAttribute("Name",       entityType.getName());
		element.setAttribute("EntityType", entityType.getEntitySetRefType());

		return element;
	}

	private String getDbJoinOn(Association association) {

		StringBuffer dbJoinOn = new StringBuffer();
		
		EntityType sourceType = entityTypes.get(association.getSourceType());
		EntityType targetType = entityTypes.get(association.getTargetType());

		String dbLinkTable = association.getDbLinkTable().toUpperCase();

		String sourceName  = sourceType.getName().toUpperCase();
		String targetName  = targetType.getName().toUpperCase();
		
		// Generate the source key terms
		for (String sourceKey : sourceType.getPrimaryKeys()) {
			if (dbJoinOn.length()>0) {
				dbJoinOn.append(", ");
			}
			dbJoinOn.append(String.format("%s.%s=%s.%s", sourceName, sourceKey.toUpperCase(), dbLinkTable, sourceName));
		}

		// Generate the target key terms
		for (String targetKey : targetType.getPrimaryKeys()) {
			if (dbJoinOn.length()>0) {
				dbJoinOn.append(", ");
			}
			dbJoinOn.append(String.format("%s.%s=%s.%s", targetName, targetKey.toUpperCase(), dbLinkTable, targetName));
		}
		
		return dbJoinOn.toString();
	
	}
	
	/**
	 * Create the Association Document Element for the specified Association
	 * <p>
	 * @param doc the Document in which to create the Association Element
	 * @param association the Association from which to create the Association Element
	 * @return the Association Document Element
	 */
	public Element createAssociationElement(Document doc, Association association) {
		Element element = doc.createElement("Association");
		
		element.setAttribute("Name",        association.getName());
		element.setAttribute("DBLinkTable", association.getDbLinkTable());
		element.setAttribute("DBJoinOn",    getDbJoinOn(association));

		return element;
	}
	
	private EntityType createAssociationEntityType(Association association) {
		EntityType assocEntity = null; 
		EntityType sourceEntity = entityTypes.get(association.getSourceType());
		EntityType targetEntity = entityTypes.get(association.getTargetType());

		if (null!=sourceEntity && null!=targetEntity) {
			
			assocEntity = new EntityType();
			assocEntity.setName(String.format("%s_%s",  sourceEntity.getName(), targetEntity.getName()));

			// source key: name, type, Key=true, Nullable=false
			EntityProperty sourceKey = sourceEntity.getProperties().get(0);
			
			// target key: name, type, Key=true, Nullable=false
			EntityProperty targetKey = targetEntity.getProperties().get(0);

			EntityProperty sourceProp = new EntityProperty(sourceEntity.getName(), sourceKey.getType());
			sourceProp.setFlags(EnumSet.of(PropertyFlag.KEY, PropertyFlag.NOT_NULLABLE));
			assocEntity.addProperty(sourceProp);
			
			EntityProperty targetProp = new EntityProperty(targetEntity.getName(), targetKey.getType());
			targetProp.setFlags(EnumSet.of(PropertyFlag.KEY, PropertyFlag.NOT_NULLABLE));
			assocEntity.addProperty(targetProp);

		}
		return assocEntity;
	}

	/**
	 * @return the EDM Document
	 * @throws Exception
	 */
	public Document getDocument() throws Exception {

		Document           doc = createDocument();
		Element  schemaElement = createSchemaElement(doc);
		
		if (entityTypes.size()>0) {
			for (Entry<String, EntityType> entitySchemaEntry : entityTypes.entrySet()) {
				EntityType entityType = entitySchemaEntry.getValue();
				if (entityType.isEntitySetSchema) {
					schemaElement.appendChild(createEntitySetElement(doc, entityType));
				} else {
					schemaElement.appendChild(createEntityTypeElement(doc, entityType));
					for (Association association : entityType.getAssociations()) {
						schemaElement.appendChild(createAssociationElement(doc, association));

						EntityType assocEntity = createAssociationEntityType(association);
						
						schemaElement.appendChild(createEntityTypeElement(doc, assocEntity));
					}
				}
			}
		}
		
		doc.appendChild(schemaElement);
		return doc;
    }

	/**
	 * Transform DOM Document into a format that is more visually appealing
	 * <p>
	 * @param xml the source DOM Document
	 * @param writer used to produce the output transformation
	 * @throws Exception
	 */
	public static final void prettyPrint(Document xml, Writer writer) throws Exception {
        Transformer tf = TransformerFactory.newInstance().newTransformer();
        
        tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        tf.setOutputProperty(OutputKeys.INDENT, "yes");
        tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        
        tf.transform(new DOMSource(xml), new StreamResult(writer));
        
    }
	
//	/**
//	 * @param args
//	 */
//	public static void main(String[] args) {
//		
//		//
//		// Create an EDM file from a source RAML file describing an OData serv)ice
//		//
//		if (args.length>0) {
//			File ramlFile = new File(args[0]);
//			
//			if (ramlFile.canRead()) {
//			
//				//
//				// Validate the RAML file
//				//
//				List<ValidationResult> results = RamlValidationService.createDefault().validate(ramlFile.getPath());
//				
//				// If the RAML file is valid, get to work...
//				if (ValidationResult.areValid(results)) {
//					Raml raml = new RamlDocumentBuilder().build(ramlFile.getPath());
//					if (null!=raml) {
//						try {
//							EDM edm = new EDM(raml);
//							Document edmDoc = edm.getDocument();
//							if (args.length>1) {
//								File edmFile = new File(args[1]);
//								if (ramlFile.equals(edmFile)) {
//									System.out.println(String.format("input file (%s) and output file (%s) cannot be the same", ramlFile, edmFile));
//								} else {
//									EDM.prettyPrint(edmDoc, new PrintWriter(edmFile));
//								}
//							}
//							EDM.prettyPrint(edmDoc, new PrintWriter(System.out));
//						} catch (Exception e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//					}
//				} else { // invalid RAML file
//					System.out.println("Error(s) parsing RAML file: "+ramlFile.getName());
//					for (ValidationResult result : results) {
//						System.out.println(String.format("line offset %d: (%d-%d): %s", result.getLine(), result.getStartColumn(), result.getEndColumn(), result.getMessage()));
//					}
//				}
//			} else {
//				System.out.println("Cannot read file: "+ramlFile.getPath());
//			}
//		} else {
//			System.out.println("usage: requires one argument specifying the RAML path for reading");
//		}
//
//	}
	
	/**
	 * Map a RAML defined type string to an EDM type string 
	 * @param ramlType RAML type string
	 * @return EDM type string
	 */
	public static String edmTypeFromRamlType(String ramlType) {
		String edmType = null;
		
		switch(ramlType) {
		
		case "number":  // Value MUST be a number. Indicate floating point numbers as defined by YAML.
			edmType = "Edm.Decimal";
			break;
		
		case "integer": // Value MUST be an integer. Floating point numbers are not allowed. The integer type is a subset of the number type.
			edmType = "Edm.Int32";
			break;
		
		case "date":    // Value MUST be a string representation of a date as defined in RFC2616 Section 3.3 [RFC2616]. See Date Representations.
			edmType = "Edm.DateTime";
			break;
		
		case "boolean": // Value MUST be either the string "true" or "false" (without the quotes).
			edmType = "Edm.Boolean";
			break;
		
		case "file":    // (Applicable only to Form properties) Value is a file. Client generators SHOULD use this type to handle file uploads correctly.
			edmType = "Edm.Binary";
			break;
		
		case "string": // Value MUST be a string.
			edmType = "Edm.String";
			break;
			
		default:
			edmType = ramlType;
		}
		
		return edmType;
		
	}
	
}
