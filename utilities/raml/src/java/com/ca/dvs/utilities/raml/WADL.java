/*******************************************************************************
 * Copyright (c) 2015 CA.  All rights reserved.
 *
 * This source file is licensed under the terms of the Eclipse Public License 1.0
 * For the full text of the EPL please see https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
/**
 * 
 */
package com.ca.dvs.utilities.raml;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.DocumentationItem;
import org.raml.model.MimeType;
import org.raml.model.ParamType;
import org.raml.model.Raml;
import org.raml.model.Resource;
import org.raml.model.Response;
import org.raml.model.parameter.Header;
import org.raml.model.parameter.QueryParameter;
import org.raml.model.parameter.UriParameter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * WADL - Transform a RAML document into a WADL Document 
 * <p>
 * @author CA Technologies
 * @version 1
 */
public class WADL {

	static Logger logger = Logger.getLogger(RamlUtil.class);

	private Raml		raml;
	private File		resDir			= null;
	
	private Document	document       = null;
	private boolean 	methodsWithIds = true;

	private String		remarks       = null;

	public final static String IDENTIFIER_PATTERN = "\\{([_a-zA-Z][_a-zA-Z0-9]*)}"; // Identifiers must start with an alpha or underscore and may be followed by zero or more of the same or numerics
	public final static String RESOURCE_PATTERN = "\\/([_a-zA-Z][_a-zA-Z0-9]*)"; // Identifiers must start with an alpha or underscore and may be followed by zero or more of the same or numerics

	public final static String WADL_GEN_VERSION = "1.0";
	
	/**
	 * Construct a WADL object from a provided Raml object
	 * <p>
	 * @param raml the source Raml object
	 */
	public WADL(Raml raml) {
		this.raml = raml;
	}

	/**
	 * Construct a WADL object from a provided Raml object and an associated resource directory
	 * <p>
	 * @param raml the source Raml object
	 * @param resDir folder in which to search for resources imported in RAMl document
	 */
	public WADL(Raml raml, File resDir) {
		this(raml);
		this.resDir	= resDir;
	}

	/**
	 * Map a RAML ParamType to a corresponding XML type
	 * <p>
	 * @param org.raml.model.ParamType
	 * @return XML type string
	 */
	public static String xmlType(ParamType paramType) { // "xsd:string", "xsd:decimal", "xsd:integer", "xsd:date", "xsd:boolean"
		String xmlType = null;
		
		switch(paramType) {
		
		case STRING:
			xmlType = "xsd:string";
			break;
			
		case NUMBER:
			xmlType = "xsd:decimal";
			break;
			
		case INTEGER:
			xmlType = "xsd:integer";
			break;
			
		case DATE:
			xmlType = "xsd:date";
			break;
			
		case FILE:
			xmlType = "xsd:clob";
			break;
			
		case BOOLEAN:
			xmlType = "xsd:boolean";
			break;
			
		default:
			xmlType = String.format("UNHANDLED-TYPE (%s)", paramType);
			break;
		}
		return xmlType;
	}

	/**
	 * @param name
	 * @return
	 */
	private Element createRootElement(String name) {
        Element rootElement = document.createElement(name);
        
        rootElement.setAttribute("xmlns",              "http://wadl.dev.java.net/2009/02");
        rootElement.setAttribute("xmlns:xsi",          "http://www.w3.org/2001/XMLSchema-instance");
        rootElement.setAttribute("xmlns:xsd",          "http://www.w3.org/2001/XMLSchema");
        rootElement.setAttribute("xsi:schemaLocation", "http://wadl.dev.java.net/2009/02 wadl.xsd");
        rootElement.setAttribute("xmlns:docbook",      "http://docbook.org/ns/docbook");
        rootElement.setAttribute("xmlns:m",            "urn:message");
        //rootElement.setAttribute("xmlns:xsdxt",        "http://docs.rackspacecloud.com/xsd-ext/v1.0");
        //rootElement.setAttribute("xmlns:wadl",         "http://wadl.dev.java.net/2009/02");
        //rootElement.setAttribute("xmlns:json",         "http://wadl.dev.java.net/2009/02/json-schema");
        
        rootElement.appendChild(document.createElement("grammars")); // Empty - no grammars
        
        document.appendChild(rootElement);
        
        return rootElement;
	}
	
	/**
	 * @param queryParameter
	 * @return
	 */
	private Element createQueryParamElement(QueryParameter queryParameter) {
		Element element = document.createElement("param");
		
		element.setAttribute("name", queryParameter.getDisplayName());
		element.setAttribute("style", "query");
		element.setAttribute("required", Boolean.toString(queryParameter.isRequired()));
		
		ParamType paramType = queryParameter.getType();
		
		element.setAttribute("type", xmlType(paramType));
		
		return element;
	}

	/**
	 * @param name
	 * @param header
	 * @return
	 */
	private Element createHeaderParamElement(String name, Header header) {
		Element element = document.createElement("param");
		element.setAttribute("name", name);
		element.setAttribute("required", Boolean.toString(header.isRequired()));
		element.setAttribute("style", "header");
		element.setAttribute("type", xmlType(header.getType()));
		element.appendChild(createParamDocElement(header.getDescription()));
		return element;
	}
	
	/**
	 * @param action
	 * @return
	 */
	private Element createRequestElement(Action action) {
		Element element = document.createElement("request");
		
		element.appendChild(createRequestDocElement(action.getDescription()));
		
		Map<String, Header> headersMap = action.getHeaders();
		for(Entry<String, Header> entry : headersMap.entrySet()) {
			element.appendChild(createHeaderParamElement(entry.getKey(), entry.getValue()));
		}
		
		Map<String, QueryParameter> params = action.getQueryParameters();
		for(Entry<String, QueryParameter> paramEntry : params.entrySet()) {
			element.appendChild(createQueryParamElement(paramEntry.getValue()));
		}
		
		Map<String, MimeType> bodyMap = action.getBody();
		if (null!=bodyMap && bodyMap.size()>0) {
			for (Entry<String,MimeType> entry : bodyMap.entrySet()) {
				MimeType mimeType = entry.getValue();
				if (mimeType.getType().equals("application/json")) {
					Element representation = document.createElement("representation");
					
					representation.setAttribute("element", "m:requestMessage");
					representation.setAttribute("mediaType", mimeType.getType());
					
					String example = mimeType.getExample();
					
					if (null!=example) {
						
						String jsonExample = JsonUtil.getJsonFromExample(example, resDir);
						
						representation.appendChild(document.createCDATASection(String.format("\n%s", Util.indentJson(jsonExample, 6))));
						
					}
					
					element.appendChild(representation);
					
				}
			}
		}

		return element;
	}
	
	/**
	 * @param status
	 * @param response
	 * @return
	 */
	private Element createResponseElement(String status, Response response) {
		Element element = document.createElement("response");
		element.setAttribute("status", status);
		element.appendChild(createResponseDocElement(response.getDescription()));
		
		Map<String, Header> headersMap = response.getHeaders();
		for(Entry<String, Header> entry : headersMap.entrySet()) {
			element.appendChild(createHeaderParamElement(entry.getKey(), entry.getValue()));
		}
		
		Map<String, MimeType> bodyMap = response.getBody();
		if (null!=bodyMap && bodyMap.size()>0) {
			for (Entry<String,MimeType> entry : bodyMap.entrySet()) {
				MimeType mimeType = entry.getValue();
				if (mimeType.getType().equals("application/json")) {
					Element representation = document.createElement("representation");
					representation.setAttribute("element", "m:responseMessage");
					representation.setAttribute("mediaType", mimeType.getType());
					String example = mimeType.getExample();
					if (null!=example) {
					
						String jsonExample = JsonUtil.getJsonFromExample(example, resDir);
						
						representation.appendChild(document.createCDATASection(String.format("\n%s", Util.indentJson(jsonExample, 6))));

					}
					element.appendChild(representation);
				}
			}
		}
		
		return element;
	}
	
	//
	// According to the WADL spec, method IDs are not permitted in method blocks defined within a resource.
	// Since I'm defining methods within a resource, I normally shouldn't need this method.
	// However, Layer7 (explorer view) uses the method id for its list of methods, so I currently add
	// the id attribute to fill this need.
	//
	/**
	 * @param action
	 * @return
	 */
	private String methodId(Action action) {
		String methodId = null;

		List<String> segments = new ArrayList<String>();
		
		segments.add(action.getType().toString().toLowerCase());

		Pattern pattern = Pattern.compile(RESOURCE_PATTERN);
		Matcher matcher = pattern.matcher(action.getResource().getRelativeUri());
		
		String resource = "unknown";
		if (matcher.find()) {
			resource = matcher.group(1);
		}
		
		segments.add(resource);
		
		Map<String, UriParameter> paramMap = action.getResource().getUriParameters();
		if (null!=paramMap) {
			boolean first = true;
			for(String paramName : paramMap.keySet()) {
				segments.add(first ? "by" : "and"); first = false;
				segments.add(paramName);
			}
		}

		methodId = Util.camelCase(segments);

		return methodId;
	}

	/**
	 * @param action
	 * @return
	 */
	private Element createMethodElement(Action action) {
		Element element = document.createElement("method");
		element.setAttribute("name", action.getType().name());
		element.setAttribute("id", methodId(action));
		
		String title = String.format("%s %s", action.getType().name(), action.getResource().getDisplayName());
		element.appendChild(createMethodDocElement(title, action.getDescription()));
	
		element.appendChild(createRequestElement(action));
		
		Map<String, Response> responses = action.getResponses();
		for(Entry<String, Response> entry : responses.entrySet()) {
			element.appendChild(createResponseElement(entry.getKey(), entry.getValue()));
		}
		
		return element;
	}
	
	/**
	 * @param name
	 * @param param
	 * @return
	 */
	private Element createTemplateParam(String name, UriParameter param) {
		Element element = document.createElement("param");

		element.setAttribute("name", name);
		element.setAttribute("style", "template");
		element.setAttribute("type", xmlType(param.getType()));
		element.setAttribute("required", Boolean.toString(true)); // Though I have not found a definitive answer, I believe that template parameters should always be required
		
		String description = param.getDescription();
		
		if (null!=description) {
			element.appendChild(createParamDocElement(description));
		}

		String defaultValue = param.getDefaultValue();

		if (null!=defaultValue) {
			element.setAttribute("default", defaultValue);
		}
		
		List<String> valueSet = param.getEnumeration();
		
		if (null!=valueSet && valueSet.size()>0) {
			for (String value : valueSet){
				Element optionEl = document.createElement("option");
				optionEl.setAttribute("value", value);
			}
		}
		
		return element;
	}
	
	/**
	 * @param resource
	 * @return
	 */
	private Element createResourceElement(Resource resource) {
		Element element = document.createElement("resource");
		
		String path = resource.getParentUri() != null ? resource.getRelativeUri() : resource.getUri();
		element.setAttribute("path", path);
		
		String description = resource.getDescription();
		if (null!=description) {
			element.appendChild(createResourceDocElement(description));
		}
		
		Map<String, UriParameter> mapParams = resource.getUriParameters();
		for (Entry<String, UriParameter> paramEntry : mapParams.entrySet()) {
			UriParameter param = paramEntry.getValue();
			element.appendChild(createTemplateParam(paramEntry.getKey(), param));
		}
		
		Map<ActionType, Action> actions = resource.getActions();
		for (Entry<ActionType, Action> actionEntry : actions.entrySet()) {
			element.appendChild(createMethodElement(actionEntry.getValue()));
		}
		
		Map<String, Resource> resources = resource.getResources();
		if (null!=resources) {
			for(Entry<String, Resource> resourceEntry : resources.entrySet()) {
				element.appendChild(createResourceElement(resourceEntry.getValue()));
			}
		}
		
		return element;
	}
	
	/**
	 * @return
	 */
	private Element createResourcesElement() {
        Element element = document.createElement("resources");
        
        element.setAttribute("base", raml.getBaseUri());
        
        Map<String, Resource> resources = raml.getResources();
        if (null!=resources) {
        	for (Entry<String, Resource> resourceEntry : resources.entrySet()) {
        		element.appendChild(createResourceElement(resourceEntry.getValue()));
        	}
        }
        
        return element;
	}
	
	/**
	 * @return
	 */
	private Element createApplicationDocElement() {
		Element element = document.createElement("docbook:doc");
		element.setAttribute("xml:lang", "EN");
		element.setAttribute("title", raml.getTitle());
		
		List<DocumentationItem> docItems = raml.getDocumentation();
		if (null != docItems) {
			for(DocumentationItem docItem : docItems) {
				Element p = document.createElement("docbook:para");
				p.setAttribute("title", docItem.getTitle());
				p.setTextContent(docItem.getContent());
				element.appendChild(p);
			}
		}
		
		if (null!=remarks) {
			Element remarksEl = document.createElement("docbook:remarks");

			remarksEl.setAttribute("role", "remarks");
			remarksEl.setTextContent(remarks);
			
			element.appendChild(remarksEl);
		}
		
		Element para = document.createElement("docbook:para");
		para.setAttribute("role", "author");
		para.setTextContent("This WADL was created by the CA WADL generator (ver. "+WADL_GEN_VERSION+") for "+raml.getTitle()+" (ver. "+raml.getVersion()+")");

		element.appendChild(para);
		
		return element;
	}
	
	/**
	 * @param text
	 * @return
	 */
	private Element createResponseDocElement(String text) {
		Element element = document.createElement("docbook:doc");
		
		element.setAttribute("xml:lang", "EN");
		element.setTextContent(text);

		return element;
	}
	
	/**
	 * @param text
	 * @return
	 */
	private Element createRequestDocElement(String text) {
		Element element = document.createElement("docbook:doc");
		
		element.setAttribute("xml:lang", "EN");
		element.setTextContent(text);

		return element;
	}
	
	/**
	 * @param text
	 * @return
	 */
	private Element createResourceDocElement(String text) {
		Element element = document.createElement("docbook:doc");
		
		element.setAttribute("xml:lang", "EN");
		element.setTextContent(text);

		return element;
	}
	
	/**
	 * @param title
	 * @param shortDesc
	 * @return
	 */
	private Element createMethodDocElement(String title, String shortDesc) {
		Element doc = document.createElement("docbook:doc");
		
		doc.setAttribute("xml:lang", "EN");
		doc.setAttribute("title", title);
		
		Element para = document.createElement("docbook:para");
		para.setAttribute("role", "shortDesc");
		para.setTextContent(shortDesc);
		
		doc.appendChild(para);
		
		return doc;
	}
	
	/**
	 * @param text
	 * @return
	 */
	private Element createParamDocElement(String text) {
		Element element = document.createElement("docbook:doc");
		
		element.setAttribute("xml:lang", "EN");
		element.setTextContent(text);

		return element;
	}
	
	/**
	 * Generate the WADL Document
	 * <p>
	 * @return the WADL Document
	 * @throws Exception
	 */
	public Document getDocument() throws Exception {

		Document wadl = createDocument();
        
        // create the root element node
        Element application = createRootElement("application");
        Element doc         = createApplicationDocElement();
        
        application.appendChild(doc);
        
        Element resources   = createResourcesElement();
        
        application.appendChild(resources);
        
        return wadl;
    }
	
	/**
	 * prettyPrint - formats DOM document in a visually pleasing manner
	 * <p>
	 * @param xml the Document
	 * @param writer used to produce the transformation
	 * @throws Exception
	 */
	public static final void prettyPrint(Document xml, Writer writer) throws Exception {
		
        TransformerFactory tf = TransformerFactory.newInstance();
        
        //tf.setFeature(XMLConstants.ACCESS_EXTERNAL_DTD, false); <-- Fortify recommended.  Eclipse compiled ok.  ANT failed.
        tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true); // Per web article, this is a suitable replacement for setting ACCESS_EXTERNAL_DTD, false (above)
        
        Transformer trans = tf.newTransformer();
        
        trans.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        trans.setOutputProperty(OutputKeys.INDENT, "yes");
        trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        
        trans.transform(new DOMSource(xml), new StreamResult(writer));
        
    }
	
	/**
	 * Produce WADL Document on System.out
	 * <p>
	 * @throws Exception
	 */
	public void print() throws Exception {
		
		Document doc = getDocument();
		
		Writer writer = new PrintWriter(System.out);
		try {
			
			prettyPrint(doc, writer);
			
		} finally {
			writer.close();
		}

	}

	/**
	 * Generate a WADL Document and store in a specified File
	 * <p>
	 * @param wadlFile the file in which to persist the WADL Document
	 * @throws Exception
	 */
	public void save(File wadlFile) throws Exception {
		
		Document doc = getDocument();
		
		FileWriter writer = new FileWriter(wadlFile);
		try {
		
			prettyPrint(doc, writer);
			
		} finally {
			writer.close();
		}
		
	}
	
	/**
	 * Produce a WADL file employing a user specified Writer
	 * <p>
	 * @param writer the writer from which to produce the WADL Document
	 * @throws Exception
	 */
	public void write(Writer writer) throws Exception {
		
		Document doc = getDocument();
		
		prettyPrint(doc, writer);
		
	}

	/**
	 * @return the methodsWithIds
	 */
	public boolean isMethodsWithIds() {
		return methodsWithIds;
	}

	/**
	 * @param methodsWithIds the methodsWithIds to set
	 */
	public void setMethodsWithIds(boolean methodsWithIds) {
		this.methodsWithIds = methodsWithIds;
	}

	/**
	 * @return the remarks
	 */
	public String getRemarks() {
		return remarks;
	}

	/**
	 * @param remarks the remarks to set
	 */
	public void setRemarks(String remarks) {
		this.remarks = (null!=remarks? new String(remarks) : null);
	}

	/**
	 * @return the document
	 * @throws Exception 
	 */
	private Document createDocument() throws Exception {
		if (null==document) {
			
	        DocumentBuilderFactory dbf      = DocumentBuilderFactory.newInstance();
	        DocumentBuilder        builder  = dbf.newDocumentBuilder();
	        
	        document = builder.newDocument();
	        
		}
		return document;
	}
	
}
