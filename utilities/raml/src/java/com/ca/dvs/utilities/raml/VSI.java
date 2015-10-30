/*******************************************************************************
 * Copyright (c) 2015 CA.  All rights reserved.
 *
 * This source file is licensed under the terms of the Eclipse Public License 1.0
 * For the full text of the EPL please see https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ca.dvs.utilities.raml;

import java.io.File;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.MimeType;
import org.raml.model.ParamType;
import org.raml.model.Raml;
import org.raml.model.Resource;
import org.raml.model.parameter.Header;
import org.raml.model.parameter.UriParameter;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;

/**
 * VSI - Transform a RAML document into a DevTest (LISA) Virtual Service Image (VSI) 
 * <p>
 * 
 * @author CA Technologies
 * @version 1
 */
public class VSI {

	private static final String LISA_VSI_VERSION = "7.5";
	
	private static int seq = 0;
	
	/**
	 * Generate a sequence number unique for this execution.  This is used to support VSI transformation.
	 * <p>
	 * @return the new seq
	 */
	public static int getSeq() {
		return ++seq;
	}
	
	/**
	 * @param seq the seq to set
	 */
	public static void setSeq(int seq) {
		VSI.seq = seq;
	}

	private class TableHeading {
		String	label;
		int		colWidth;
		
		TableHeading(String label, int colWidth) {
			setLabel(label);
			setColWidth(colWidth);
		}
		
		/**
		 * @return the label
		 */
		public String getLabel() {
			return label;
		}
		/**
		 * @param label the label to set
		 */
		public void setLabel(String label) {
			this.label = label;
		}
		/**
		 * @return the colWidth
		 */
		public int getColWidth() {
			return colWidth;
		}
		/**
		 * @param colWidth the colWidth to set
		 */
		public void setColWidth(int colWidth) {
			this.colWidth = colWidth;
		}
	};
	
	private class Argument {
		String					name;
		UriParameter			uriParameter;
		
		public Argument() {
			setName(null);
			setUriParameter(null);
		}

		public Argument(String name, UriParameter uriParameter) {
			this();
			setName(name);
			setUriParameter(uriParameter);
		}

		public Element getElement(Document doc) {
			
			// <ag>
			//		<p n="paramName" t="arg:EQUALS;false; ??? ; ??? ;CASE_INSENSITIVE;IS_NUMERIC">example_value</p>
			// </ag>
			
			Element agElement	= doc.createElement("ag");
			Element pElement	= doc.createElement("p");
			
			pElement.setAttribute("n", getName());
			
			if (ParamType.NUMBER == uriParameter.getType()) {

				pElement.setAttribute("t", "arg:EQUALS;false;;;;IS_NUMERIC");
				
			} else {
				
				pElement.setAttribute("t", "arg:EQUALS;false;;;;");
				
			}
			
			String example = uriParameter.getExample();
			
			if (null==example || example.isEmpty()) { // When RAML does not specify an example for the parameter, DevTest 8.0 assigns a value of {<paramName>}
				example = String.format("{%s}", getName());
			}
			
			pElement.setTextContent(example);
			
			agElement.appendChild(pElement);
			
			return agElement;
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
		 * @return the uriParameter
		 */
		@SuppressWarnings("unused")
		public UriParameter getUriParameter() {
			return uriParameter;
		}

		/**
		 * @param uriParameter the uriParameter to set
		 */
		public void setUriParameter(UriParameter uriParameter) {
			this.uriParameter = uriParameter;
		}
		
	};
	 	
	/**
	 * Request - Maintains content for a DevTest (LISA) request
	 * <p>
	 * 
	 * @author CA Technologies
	 * @version 1
	 */
	private class Request {
		String					op;
		String					basePath;
		ActionType				actionType;
		Action					action;
		Map<String, String>		metadata;
		List<Argument>			arguments;
		List<String>			matchScript;
		Map<String, String>		attributes;
		
		/**
		 * Default constructor
		 */
		public Request() {
			setOp(null);
			setBasePath(null);
			setMetadata(new HashMap<String, String>());
			setArguments(new LinkedList<Argument>());
			setMatchScript(new ArrayList<String>());
			setAttributes(new HashMap<String, String>());
		}

		/**
		 * Construct a Request object given a custom base URI, RAML ActionType and Action objects.
		 * <p>
		 * @param baseUri
		 * @param actionType
		 * @param action
		 */
		public Request(String baseUri, ActionType actionType, Action action) {
			this();
			try {
				URI uri = new URI(baseUri);
				setBasePath(uri.getPath());
			} catch (URISyntaxException e) {
				setBasePath(baseUri);
			}
			setActionType(actionType);
			setAction(action);
			for (Map.Entry<String, UriParameter> paramEntry : action.getResource().getResolvedUriParameters().entrySet()) {
				arguments.add(new Argument(paramEntry.getKey(), paramEntry.getValue()));
			}
			setOp(String.format("%s %s", actionType.name(), action.getResource().getUri()));
		}
		
		/**
		 * Create a Document Element for this Request
		 * @param doc the DOM Document
		 * @param matchStyle the desired matchStyle (EXACT or SIGNATURE)
		 * @return the Request Document Element
		 */
		public Element getElement(Document doc, String matchStyle) {
			Element element = doc.createElement("rq");
					element.setAttribute("id", Integer.toString(VSI.getSeq()));
					
			String[]	parts	= getOp().trim().split(" ");
			String		fqOp	= String.format("%s %s%s", parts[0], getBasePath(), parts.length>1 ? parts[1] : "");
			
			//
			// Unless the resource part of the path is empty, ensure that the resource path terminates with a slash.
			// LISA will fail to match the resource path otherwise.
			//
			if (parts.length>1 && !parts[1].equals(getBasePath()) && !fqOp.endsWith("/"))
				fqOp += "/";
			
			element.setAttribute("op", fqOp);
			element.setAttribute("tl", matchStyle);
			
			for (Argument argument : arguments) {
				element.appendChild(argument.getElement(doc));
			}
			Element msElement = doc.createElement("ms");	// match script element
			
			if (getMatchScript().size()>0) {				// create a match script element from all script lines
				StringBuffer sb = new StringBuffer();
				for (String line : getMatchScript()) {
					sb.append(String.format("%s;&#13", line));
				}
				msElement.setTextContent(sb.toString());
			}
			
			element.appendChild(msElement);
			
			Element atElement = doc.createElement("at");
			
			for (Map.Entry<String, String> attributeEntry : getAttributes().entrySet()) {
				Element pElement = doc.createElement("p");
				pElement.setAttribute("n", attributeEntry.getKey());
				pElement.setTextContent(attributeEntry.getValue());
				atElement.appendChild(pElement);
			}
			
			element.appendChild(atElement);
			
			if (action.hasBody()) {
				for (Map.Entry<String, MimeType> bodyEntry : action.getBody().entrySet()) {
					MimeType	mimeType	= bodyEntry.getValue();
					//String		contentType	= mimeType.getType().toString();
					Element		bodyElement	= doc.createElement("bd");
					
					bodyElement.setTextContent(mimeType.getExample());
					element.appendChild(bodyElement);
				}
			}

			return element;
		}
		
		/**
		 * @return the op
		 */
		public String getOp() {
			return op;
		}

		/**
		 * @param op the op to set
		 */
		public void setOp(String op) {
			this.op = op;
		}

		/**
		 * @return the basePath
		 */
		public String getBasePath() {
			return basePath;
		}

		/**
		 * @param basePath the basePath to set
		 */
		public void setBasePath(String basePath) {
			this.basePath = basePath;
		}

		/**
		 * @return the actionType
		 */
		@SuppressWarnings("unused")
		public ActionType getActionType() {
			return actionType;
		}

		/**
		 * @param actionType the actionType to set
		 */
		public void setActionType(ActionType actionType) {
			this.actionType = actionType;
		}

		/**
		 * @return the action
		 */
		@SuppressWarnings("unused")
		public Action getAction() {
			return action;
		}

		/**
		 * @param action the action to set
		 */
		public void setAction(Action action) {
			this.action = action;
		}

		/**
		 * @return the metadata
		 */
		@SuppressWarnings("unused")
		public Map<String, String> getMetadata() {
			return metadata;
		}

		/**
		 * @param metadata the metadata to set
		 */
		public void setMetadata(Map<String, String> metadata) {
			this.metadata = metadata;
		}

		/**
		 * @return the arguments
		 */
		@SuppressWarnings("unused")
		public List<Argument> getArguments() {
			return arguments;
		}

		/**
		 * @param arguments the arguments to set
		 */
		public void setArguments(List<Argument> arguments) {
			this.arguments = arguments;
		}

		/**
		 * @return the matchScript
		 */
		public List<String> getMatchScript() {
			return matchScript;
		}

		/**
		 * @param matchScript the matchScript to set
		 */
		public void setMatchScript(List<String> script) {
			this.matchScript = script;
		}

		/**
		 * @return the attributes
		 */
		public Map<String, String> getAttributes() {
			return attributes;
		}

		/**
		 * @param attributes the attributes to set
		 */
		public void setAttributes(Map<String, String> attributes) {
			this.attributes = attributes;
		}

	};

	/**
	 * Response - Maintains content for a DevTest (LISA) response
	 * <p>
	 * 
	 * @author CA Technologies
	 * @version 1
	 */
	private class Response {
		org.raml.model.Response	response = null;
		String					status = null;
		int						t = 0;
		Map<String, String>		metadata;
		Map<String, Header>		headers;
		
		/**
		 * default constructor
		 */
		public Response() {
			setT(0);
			setResponse(null);
			setMetadata(new HashMap<String, String>());
		}

		/**
		 * Construct a Response with a specified status and RAML Response object
		 * @param status the status to be set in the Response
		 * @param response the RAML Response object used as a model
		 */
		public Response(String status, org.raml.model.Response response) {
			this();
			setResponse(response);
			setStatus(status);
			setHeaders(getStandardHeaders());
		}

		private Header createHeader(String value) {
			Header header = new Header();

			header.setDefaultValue(value);
			return header;
		}
				
		private Map<String, Header> getStandardHeaders() {
			Map<String, Header> standardHeaders = new HashMap<String, Header>();

			standardHeaders.put("HTTP-Response-Code", createHeader(getStatus()));
			standardHeaders.put("Server", createHeader("LISA/Virtual-Environment-Server"));
			standardHeaders.put("Date", createHeader("{{=httpNow()}}"));
			standardHeaders.put("X-Powered-By", createHeader("LISA/{{=lisaVersionString()}}"));
			
			return standardHeaders;
		}
		
		/**
		 * Create a Document Element for this Response object
		 * @param doc the Document in which to create the Element
		 * @return the Document Element generated from this Response
		 */
		public Element getElement(Document doc) {
			
			Element element = doc.createElement("rp");
					element.setAttribute("id", Integer.toString(VSI.getSeq()));
					element.setAttribute("t", Integer.toString(getT()));
			
			Element mElement = doc.createElement("m");

			if (getResponse().hasBody()) {
				for (Map.Entry<String, MimeType> bodyEntry : getResponse().getBody().entrySet()) {
	
					getHeaders().put("Content-Type", createHeader(bodyEntry.getKey()));
	
					Element bdElement = doc.createElement("bd");
							bdElement.setTextContent(bodyEntry.getValue().getExample());
							
					element.appendChild(bdElement);
				}
			}
			
			for (Map.Entry<String, Header> headerEntry : getHeaders().entrySet()) {
				Element	pElement = doc.createElement("p");
						pElement.setAttribute("n", headerEntry.getKey());
						pElement.setTextContent(headerEntry.getValue().getDefaultValue());
						
				mElement.appendChild(pElement);
			}

			element.appendChild(mElement);

			return element;
		}
		
		/**
		 * @return the response
		 */
		public org.raml.model.Response getResponse() {
			return response;
		}

		/**
		 * @param response the response to set
		 */
		public void setResponse(org.raml.model.Response response) {
			this.response = response;
		}

		/**
		 * @return the status
		 */
		public String getStatus() {
			return status;
		}

		/**
		 * @param status the status to set
		 */
		public void setStatus(String status) {
			this.status = status;
		}

		/**
		 * @return the t
		 */
		public int getT() {
			return t;
		}

		/**
		 * @param t the t to set
		 */
		public void setT(int t) {
			this.t = t;
		}

		/**
		 * @return the metadata
		 */
		@SuppressWarnings("unused")
		public Map<String, String> getMetadata() {
			return metadata;
		}

		/**
		 * @param metadata the metadata to set
		 */
		public void setMetadata(Map<String, String> metadata) {
			this.metadata = metadata;
		}

		/**
		 * @return the headers
		 */
		public Map<String, Header> getHeaders() {
			return headers;
		}

		/**
		 * @param headers the headers to set
		 */
		public void setHeaders(Map<String, Header> headers) {
			this.headers = headers;
		}
		
	};
	
	/**
	 * Transaction - Maintains all content for a DevTest (LISA) transaction
	 * <p>
	 * @author CA Technologies
	 * @version 1
	 */
	private class Transaction {
		String						basePath;
		ActionType					actionType;
		Action						action;
		Request						request;
		Map<String, UriParameter>	uriParameters;
		List<Response>				responses;
		
		/**
		 * default constructor
		 */
		public Transaction() {
			setActionType(null);
			setAction(null);
			setUriParameters(new LinkedHashMap<String, UriParameter>());
			setRequest(null);
			setResponses(new LinkedList<Response>());
		}

		/**
		 * Construct a Transaction object given a service base path, RAML ActionType and Action and any URI parameters
		 * <p> 
		 * @param basePath service base path
		 * @param actionType RAML Action type associated with this Transaction
		 * @param action RAML Action associated with this Transaction
		 * @param uriParameters URI parameters used in this transaction
		 */
		public Transaction(String basePath, ActionType actionType, Action action, Map<String, UriParameter> uriParameters) {
			this();
			setBasePath			(basePath);
			setActionType		(actionType);
			setAction			(action);
			setUriParameters	(uriParameters);
			setRequest(new Request(getBasePath(), actionType, action));
			for (Entry<String, org.raml.model.Response> responseEntry : action.getResponses().entrySet()) {
				responses.add(new Response(responseEntry.getKey(), responseEntry.getValue()));
			}
		}
		
		/**
		 * Generate a Document Element for this Transaction object
		 * <p>
		 * @param doc the Document in which to create the Element
		 * @return the Transaction Document Element
		 */
		public Element getElement(Document doc) {
			
			Element		element = doc.createElement("t");
						element.setAttribute("id", Integer.toString(VSI.getSeq()));
						element.setAttribute("nd", "true");

			Element		spElement = doc.createElement("sp");		// specific transaction set?
			
			Element		spitElement = doc.createElement("t");		// specific transaction
						spitElement.setAttribute("id", Integer.toString(VSI.getSeq()));

			Element		spitrsElement	= doc.createElement("rs");	// specific transaction response set
			Element		rsElement		= doc.createElement("rs");	// non-specific transaction response set

			for (Response response : getResponses()) {
				
				rsElement.appendChild(response.getElement(doc));
				spitrsElement.appendChild(response.getElement(doc));
				
			}
			
			spitElement.appendChild(getRequest().getElement(doc, "EXACT"));
			spitElement.appendChild(spitrsElement);
			spElement.appendChild(spitElement);
			
			element.appendChild(spElement);
			element.appendChild(getRequest().getElement(doc, "SIGNATURE"));
			element.appendChild(rsElement);
			
			return element;
		}
		
		/**
		 * @return the basePath
		 */
		public String getBasePath() {
			return basePath;
		}

		/**
		 * @param basePath the basePath to set
		 */
		public void setBasePath(String basePath) {
			this.basePath = basePath;
		}

		/**
		 * @return the actionType
		 */
		@SuppressWarnings("unused")
		public ActionType getActionType() {
			return actionType;
		}

		/**
		 * @param actionType the actionType to set
		 */
		public void setActionType(ActionType actionType) {
			this.actionType = actionType;
		}

		/**
		 * @return the action
		 */
		@SuppressWarnings("unused")
		public Action getAction() {
			return action;
		}

		/**
		 * @param action the action to set
		 */
		public void setAction(Action action) {
			this.action = action;
		}

		/**
		 * @return the request
		 */
		public Request getRequest() {
			return request;
		}

		/**
		 * @param request the request to set
		 */
		public void setRequest(Request request) {
			this.request = request;
		}

		/**
		 * @return the uriParameters
		 */
		@SuppressWarnings("unused")
		public Map<String, UriParameter> getUriParameters() {
			return uriParameters;
		}

		/**
		 * @param uriParameters the uriParameters to set
		 */
		public void setUriParameters(Map<String, UriParameter> uriParameters) {
			this.uriParameters = uriParameters;
		}

		/**
		 * @return the responses
		 */
		public List<Response> getResponses() {
			return responses;
		}

		/**
		 * @param response the response to set
		 */
		public void setResponses(List<Response> responses) {
			this.responses = responses;
		}
		
	};
		
	private Raml				raml				= null;
	private File				resDir				= null;
	private String				name				= null;
	private String				version				= null;
	private Date				creationTime		= null;
	private Date				modificationTime	= null;
	private List<Transaction>	transactions		= null;
	private boolean				genServiceDocument	= false;
	
	
	/**
	 * default constructor for a VSI object
	 */
	public VSI() {
		setRaml(null);
		setResDir(null);
		setName(null);
		setVersion(null);
		setCreationTime(new Date());
		setModificationTime(new Date());
		setTransactions(new LinkedList<Transaction>());
	}
	
	/**
	 * Transform a RAML service model into a DevTest (LISA) Virtual Service Image (VSI) -- caller specifies whether to generate a Service Document transaction
	 * <p>
	 * @param raml the source Raml object
	 * @param resDir the folder searched for imported RAML resources
	 * @param genServiceDocument - when true, an additional Service Document transaction (root document) will be added to those transactions found in RAML file
	 */
	public VSI(Raml raml, File resDir, boolean genServiceDocument) {
		
		this();
		setRaml(raml);
		setResDir(resDir);
		setName(raml.getTitle().replaceAll("\\s", ""));
		setVersion(raml.getVersion());
		setGenServiceDocument(genServiceDocument);
		
		if (raml.getResources().get("/")==null  && isGenServiceDocument()) { // if a resource has not been defined for the service document, create one now
			// Need to add two Service Document resources to work around a LISA request matching bug
			addServiceDocumentResource("___SERVICE_DOCUMENT_1___", "");		// Service Document resource (without a trailing slash)
			addServiceDocumentResource("___SERVICE_DOCUMENT_2___", "/");	// Service Document resource (with a trailing slash)
		}
		
		buildTransactions(raml.getResources());

	}
	
	/**
	 * Transform a RAML service model into a DevTest (LISA) Virtual Service Image (VSI) -- w/o generating an additional transaction for the Service Document
	 * <p>
	 * @param raml the source Raml object
	 * @param resDir the folder searched for imported RAML resources
	 */
	public VSI(Raml raml, File resDir) {
		this(raml, resDir, false);
	}
	
	/**
	 * From the RAML resources (raml.getResources()), recursively build a map of all Transactions
	 * <p>
	 * @param resources - The RAML resources (i.e. from raml.getResources())
	 */
	private void buildTransactions(Map<String, Resource> resources) {
		
		if (null != resources) {
			for (Map.Entry<String,Resource> resourceEntry : resources.entrySet()) {

				Resource	resource	= resourceEntry.getValue();
				
				for (Map.Entry<ActionType, Action> actionEntry : resource.getActions().entrySet()) {
					ActionType	actionType	= actionEntry.getKey();
					Action		action		= actionEntry.getValue();
					
					transactions.add(new Transaction(raml.getBaseUri(), actionType, action, resource.getResolvedUriParameters()));
				}
				buildTransactions(resource.getResources()); // get the nested resources too
			}
		}
	}

	private Element createStyleElement(Document doc) {
		Element element = doc.createElement("style");
		element.setTextContent("table, th, td { border: 1px solid black; padding: 5px; }");
		return element;
	}
	
	/**
	 * @param doc - The DOM document from which to create new elements
	 * @param headings - String list of heading labels
	 * @return an HTML table heading row element
	 */
	private Element createTableHeadingRowElement(Document doc, List<TableHeading> headings) {
		Element elHeadingRow = doc.createElement("tr");
		
		for (TableHeading heading : headings) {
			Element elHeading	 = doc.createElement("th");
			elHeading.setAttribute("style", String.format("width:%spx", heading.getColWidth()));
			elHeading.setTextContent(heading.getLabel());
			elHeadingRow.appendChild(elHeading);
		}
		
		return elHeadingRow;
	}
	
	/**
	 * @param doc - The DOM document from which to create new elements
	 * @param values - String list of table row column data
	 * @return
	 */
	private Element createTableDataRowElement(Document doc, List<String> values) {
		Element elDataRow = doc.createElement("tr");
		
		for (String value : values) {
			Element elValue	= doc.createElement("td");
			elValue.setTextContent(value);
			elDataRow.appendChild(elValue);
		}
		
		return elDataRow;
	}
	
	/**
	 * @return body content for Service Document request
	 */
	private MimeType createServiceDocumentHtmlBody() {
		
		MimeType body = new MimeType("text/html");
		
		Document doc = null;
		
		try {
			doc = createDocument();

	        DOMImplementation domImpl = doc.getImplementation();
			DocumentType doctype = domImpl.createDocumentType("html", null, null);
			doc.appendChild(doctype);			
			
			Element elHtml = doc.createElement("html");
			elHtml.setAttribute("lang", "en-US");
			
			doc.appendChild(elHtml);
			
			Element elHead	= doc.createElement("head");
			Element elTitle = doc.createElement("title");
			elTitle.setTextContent(String.format("%s", raml.getTitle()));
			elHead.appendChild(elTitle);
			elHead.appendChild(createStyleElement(doc));
			elHtml.appendChild(elHead);
			
			Element elBody = doc.createElement("body");
			elHtml.appendChild(elBody);

			Element h1 = doc.createElement("h1");
			h1.setTextContent(String.format("%s", raml.getTitle()));
			elBody.appendChild(h1);
			
			elBody.appendChild(doc.createElement("p"));
			elBody.appendChild(doc.createElement("hr"));
			elBody.appendChild(doc.createElement("p"));

			Element elMessage = doc.createElement("i");
			elMessage.setTextContent(String.format("The following requests are supported by the service model:"));
			elBody.appendChild(elMessage);

			Element elTableP = doc.createElement("p");
			elBody.appendChild(elTableP);
	
			Element elTable	= doc.createElement("table");
			elTableP.appendChild(elTable);
			
			List<TableHeading> headings = new ArrayList<TableHeading>();
			headings.add(new TableHeading("Method", 100));
			headings.add(new TableHeading("Resource Path", -1));
			elTable.appendChild(createTableHeadingRowElement(doc, headings));
			
			for (String operation : getOperationsList(raml.getResources())) {
				String parts[] = operation.split(" ");
				elTable.appendChild(createTableDataRowElement(doc, Arrays.asList(parts)));
			}

			elBody.appendChild(doc.createElement("p"));
			elBody.appendChild(doc.createElement("hr"));
			elBody.appendChild(doc.createElement("br"));
			
			Element elSignature = doc.createElement("font");
			elSignature.setAttribute("size", "-2");
			elSignature.setTextContent("Produced by a DevTest virtualized web server.");
			
			elBody.appendChild(elSignature);
			
			//body.setExample(((DOMImplementationLS) domImpl).createLSSerializer().writeToString(doc));
			StringWriter bodyWriter = new StringWriter();
			VSI.prettyPrint(doc,  bodyWriter);
			body.setExample(bodyWriter.toString());
			
		} catch(Exception e) {
			
			body.setExample(String.format("<HTML><HEAD><TITLE>DOM Error</TITLE></HEAD><BODY><H1>Exception</H1><P>%s</P></BODY></HTML>", e.getMessage()));
			
		}
		return body;
	}
	
	/**
	 * Insert extra resource to those extracted from RAML document.  This will define the Service Document (root) transaction
	 */
	private void addServiceDocumentResource(String resourceKey, String relativeUri) {
		
		Action action = new Action();
		
		org.raml.model.Resource ramlResource = new org.raml.model.Resource();
		ramlResource.setDescription("Service Document");
		
		ramlResource.setParentUri("");
		ramlResource.setRelativeUri(relativeUri);

		action.setType(ActionType.GET);
		action.setDescription("Return Service Document");
		action.setResource(ramlResource);
		
		Map<ActionType, Action> actions = ramlResource.getActions();
		actions.put(ActionType.GET, action);
		ramlResource.setActions(actions);
		
		Map<String, org.raml.model.Response>	responses		= new HashMap<String, org.raml.model.Response>();
		org.raml.model.Response					response		= null;
		Map<String, MimeType>					responseBody	= null;
		
		response = new org.raml.model.Response();

		// HTML Response (begin)
		response.setDescription("Service Document - Response (text/html)");
		
		responseBody = new HashMap<String, MimeType>();
		responseBody.put("text/html", createServiceDocumentHtmlBody());
		
		response.setBody(responseBody);
		responses.put("200", response);
		// HTML Response (end)
		
		action.setResponses(responses);
		
		Map<String, Resource> resources = raml.getResources();
		resources.put(resourceKey, ramlResource);
		raml.setResources(resources);
			
	}
		
	/**
	 * @return body content for Unknown Stateless Request Response
	 * 
	 * 	<html>
	 * 		<head>
	 * 			<title>404 Not Found</title>
	 * 		</head>
	 * 		<body>
	 * 			<h1>Not Found</h1>
	 * 				The requested URL was not found on this server.
	 * 			<p>
	 * 			<hr>
	 * 			<p>
	 * 			<i>The DevTest VSE service could not match your request to a recorded request.&amp;nbsp; Consider expanding your service image.</i>
	 * 			<br>
	 * 			<font size="-2">Produced by a DevTest virtualized web server.</font>
	 * 		</body>
	 * </html>
	 */
	private MimeType createUnknownStatelessRequestResponseHtmlBody() {
		
		MimeType body = new MimeType("text/html");
		
		Document doc = null;
		
		try {
			doc = createDocument();

	        DOMImplementation domImpl = doc.getImplementation();
			DocumentType doctype = domImpl.createDocumentType("html", null, null);
			doc.appendChild(doctype);			
			
			Element elHtml = doc.createElement("html");
			elHtml.setAttribute("lang", "en-US");
			
			doc.appendChild(elHtml);
			
			Element elHead	= doc.createElement("head");
			Element elTitle = doc.createElement("title");
			elTitle.setTextContent("404 Not Found");
			elHead.appendChild(elTitle);
			elHtml.appendChild(elHead);
			
			Element elBody = doc.createElement("body");
			elHtml.appendChild(elBody);
	
			Element h1 = doc.createElement("h1");
			h1.setTextContent("404 - Resource Not Found");
			
			elBody.appendChild(h1);
			elBody.appendChild(doc.createElement("p"));
			elBody.appendChild(doc.createElement("hr"));
			elBody.appendChild(doc.createElement("p"));
			
			Element elMessage = doc.createElement("i");
			elMessage.setTextContent("The DevTest VSE service could not match your request to one defined in the service model.");
			elBody.appendChild(elMessage);
			elBody.appendChild(doc.createElement("p"));
			elBody.appendChild(doc.createElement("hr"));
			
			elBody.appendChild(doc.createElement("br"));
			
			Element elSignature = doc.createElement("font");
			elSignature.setAttribute("size", "-2");
			elSignature.setTextContent("Produced by a DevTest virtualized web server.");
			
			elBody.appendChild(elSignature);
			
			//body.setExample(((DOMImplementationLS) domImpl).createLSSerializer().writeToString(doc));
			StringWriter bodyWriter = new StringWriter();
			VSI.prettyPrint(doc,  bodyWriter);
			body.setExample(bodyWriter.toString());
			
		} catch(Exception e) {
			
			body.setExample(String.format("<HTML><HEAD><TITLE>DOM Error</TITLE></HEAD><BODY><H1>Exception</H1><P>%s</P></BODY></HTML>", e.getMessage()));
			
		}
		return body;
	}

	/**
	 * @param doc - the VSI DOM document in which to create the usrr element
	 * @return the unknown conversational request response (ucrr) element
	 */
	private Element createUnknownConversationalRequestResponseElement(Document doc) {

		Element ucrr = doc.createElement("ucrr");

		//	<ucrr>
		//		<rp id="18" t="0">
		//			<m>
		//				<p n="HTTP-Response-Code">200</p>
		//				<p n="HTTP-Response-Code-Text">OK</p>
		//				<p n="Server">LISA/Virtual-Environment-Server</p>
		//				<p n="Date">{{=httpNow()}}</p>
		//				<p n="X-Powered-By">LISA/{{=lisaVersionString()}}</p>
		//			</m>
		//		</rp>
		//	</ucrr>
		
		Element rp = doc.createElement("rp");
		rp.setAttribute("id", Integer.toString(VSI.getSeq()));
		rp.setAttribute("t", "0");
		
		Element m = doc.createElement("m");
		Element p = doc.createElement("p");
		p.setAttribute("n", "HTTP-Response-Code"); p.setTextContent("200");
		m.appendChild(p);

		p = doc.createElement("p");
		p.setAttribute("n", "HTTP-Response-Code-Text"); p.setTextContent("OK");
		m.appendChild(p);

		p = doc.createElement("p");
		p.setAttribute("n", "Server"); p.setTextContent("LISA/Virtual-Environment-Server");
		m.appendChild(p);

		p = doc.createElement("p");
		p.setAttribute("n", "Date"); p.setTextContent("{{=httpNow()}}");
		m.appendChild(p);

		p = doc.createElement("p");
		p.setAttribute("n", "X-Powered-By"); p.setTextContent("LISA/{{=lisaVersionString()}}");
		m.appendChild(p);
		
		rp.appendChild(m);

		ucrr.appendChild(rp);
		
		return ucrr;
	}
	
	/**
	 * @param doc - the VSI DOM document in which to create the usrr element
	 * @return the unknown stateless request response (usrr) element
	 */
	private Element createUnknownStatelessRequestResponseElement(Document doc) {

		Element usrr = doc.createElement("usrr");
		
		//	<usrr>
		//  	<rp id="17" t="0">
		//	 		 <m>
		//	  			<p n="HTTP-Response-Code">404</p>
		//	  			<p n="HTTP-Response-Code-Text">Not Found</p>
		//	  			<p n="Server">LISA/Virtual-Environment-Server</p>
		//	  			<p n="Date">{{=httpNow()}}</p>
		//	  			<p n="X-Powered-By">LISA/{{=lisaVersionString()}}</p>
		//	  			<p n="Content-Type">text/html</p>
		//	  		</m>
		//	  		<bd>&lt;html&gt;&lt;head&gt;&lt;title&gt;404 Not Found&lt;/title&gt;&lt;/head&gt;&lt;body&gt;&lt;h1&gt;Not Found&lt;/h1&gt;The requested URL was not found on this server.&lt;p&gt;&lt;hr&gt;&lt;p&gt;&lt;i&gt;The DevTest VSE service could not match your request to a recorded request.&amp;nbsp; Consider expanding your service image.&lt;/i&gt;&lt;br&gt;&lt;font size=&quot;-2&quot;&gt;Produced by a DevTest virtualized web server.&lt;/font&gt;&lt;/body&gt;&lt;/html&gt;</bd>
		//  	</rp>
		//</usrr>
		
		Element rp = doc.createElement("rp");
		rp.setAttribute("id", Integer.toString(VSI.getSeq()));
		rp.setAttribute("t", "0");
		
		Element m = doc.createElement("m");
		Element p = doc.createElement("p");
		p.setAttribute("n", "HTTP-Response-Code"); p.setTextContent("404");
		m.appendChild(p);

		p = doc.createElement("p");
		p.setAttribute("n", "HTTP-Response-Code-Text"); p.setTextContent("Not Found");
		m.appendChild(p);

		p = doc.createElement("p");
		p.setAttribute("n", "Server"); p.setTextContent("LISA/Virtual-Environment-Server");
		m.appendChild(p);

		p = doc.createElement("p");
		p.setAttribute("n", "Date"); p.setTextContent("{{=httpNow()}}");
		m.appendChild(p);

		p = doc.createElement("p");
		p.setAttribute("n", "X-Powered-By"); p.setTextContent("LISA/{{=lisaVersionString()}}");
		m.appendChild(p);

		p = doc.createElement("p");
		p.setAttribute("n", "Content-Type"); p.setTextContent("text/html");
		m.appendChild(p);

		rp.appendChild(m);

		Element bd = doc.createElement("bd");
		bd.setTextContent(createUnknownStatelessRequestResponseHtmlBody().getExample());
		rp.appendChild(bd);
		
		usrr.appendChild(rp);
		
		return usrr;
	}

	/**
	 * @param resources the resources from which to extract a list of operations (i.e. "GET /my/resource", "POST /my/resource", etc.)
	 * @return the list of operations
	 */
	public List<String>	getOperationsList(Map<String, Resource> resources) {
		
		List<String> operationsList = new ArrayList<String>();

		if (null != resources) {
			for (Map.Entry<String,Resource> resourceEntry : resources.entrySet()) {

				Resource	resource	= resourceEntry.getValue();
				
				for (Map.Entry<ActionType, Action> actionEntry : resource.getActions().entrySet()) {
					ActionType	actionType	= actionEntry.getKey();
					Action		action		= actionEntry.getValue();
					
					operationsList.add(String.format("%s %s%s", actionType.name(), getRaml().getBasePath(), action.getResource().getUri()));
				}
				operationsList.addAll(getOperationsList(resource.getResources())); // get the nested resources too
			}
		}
		
		return operationsList;
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
	 * @return the VSI Document
	 * @throws Exception
	 */
	public Document getDocument() throws Exception {

		Document			doc			= createDocument();
		
		SimpleDateFormat	dateFormat	= new SimpleDateFormat("yyyy-MM-dd.hh:mm:ss.SSS");
		
		Element 			siElement	= doc.createElement("serviceImage");
							siElement.setAttribute("name", getName());
							siElement.setAttribute("version", LISA_VSI_VERSION);
							siElement.setAttribute("created", dateFormat.format(getCreationTime()));
							siElement.setAttribute("lastModified", dateFormat.format(getModificationTime()));
		
		Element 			stElement = doc.createElement("st");
		
		for(Transaction transaction : getTransactions()) {
			stElement.appendChild(transaction.getElement(doc));
		}
		
		/*
		 * <serviceImage>
		 * 		<st>		- Stateless Transactions
		 * 			:
		 * 		</st>
		 * 		<ucrr>		- Unknown Conversational Request Response
		 * 			:
		 * 		</ucrr>
		 * 		<usrr>		- Unknown Stateless Request Response
		 * 			:
		 * 		</usrr>
		 * </serviceImage>
		 */
		
		// Append Stateless Transactions to ServiceImage
		siElement.appendChild(stElement);
		
		// Append Unknown Conversational Request Response to ServiceImage
		siElement.appendChild(createUnknownConversationalRequestResponseElement(doc));

		// Append Unknown Stateless Request Response to ServiceImage
		siElement.appendChild(createUnknownStatelessRequestResponseElement(doc));
		
		doc.appendChild(siElement);
		return doc;
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
	 * @return the raml
	 */
	public Raml getRaml() {
		return raml;
	}

	/**
	 * @param raml the raml to set
	 */
	public void setRaml(Raml raml) {
		this.raml = raml;
	}

	/**
	 * @return the resDir
	 */
	public File getResDir() {
		return resDir;
	}

	/**
	 * @param resDir the resDir to set
	 */
	public void setResDir(File resDir) {
		this.resDir = resDir;
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
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * @return the creationTime
	 */
	public Date getCreationTime() {
		return creationTime;
	}

	/**
	 * @param creationTime the creationTime to set
	 */
	public void setCreationTime(Date creationTime) {
		this.creationTime = creationTime;
	}

	/**
	 * @return the modificationTime
	 */
	public Date getModificationTime() {
		return modificationTime;
	}

	/**
	 * @param modificationTime the modificationTime to set
	 */
	public void setModificationTime(Date modificationTime) {
		this.modificationTime = modificationTime;
	}

	/**
	 * @return the transactions
	 */
	public List<Transaction> getTransactions() {
		return transactions;
	}

	/**
	 * @param transactions the transactions to set
	 */
	public void setTransactions(List<Transaction> transactions) {
		this.transactions = transactions;
	}
		
	/**
	 * @return the generateServiceDocument
	 */
	public boolean isGenServiceDocument() {
		return genServiceDocument;
	}

	/**
	 * @param generateServiceDocument the generateServiceDocument to set
	 */
	public void setGenServiceDocument(boolean genServiceDocument) {
		this.genServiceDocument = genServiceDocument;
	}

}
