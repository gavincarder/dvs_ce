/*******************************************************************************
 * Copyright (c) 2015 CA.  All rights reserved.
 *
 * This source file is licensed under the terms of the Eclipse Public License 1.0
 * For the full text of the EPL please see https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ca.dvs.utilities.lisamar;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ca.casd.utilities.commonUtils.CommonDefs;
import com.ca.casd.utilities.commonUtils.log.Log;

/**
 * VSImageUtil Class
 * 
 * Generate DevTest VS Image 
 * 
 * @author gonbo01
*/

public class VSImageUtil {
	
	private static String LISA_VSI_VERSION = "7.5";

	private static int sequencenum = 10;
	
	private static final VSImageUtil instance = new VSImageUtil();
	
    public static VSImageUtil getInstance() {
		return instance;
    }
    
    /**
     * Create transactions into the document of VSI
     * 
     * @param doc			- VSI document
     * @param vsmObject		- the information of VS model
     * @return				- true if it is successful
     * @throws Exception
     */
	public boolean updateVSImageDocument(Document doc, VSMObject vsmObject) throws Exception {
		// TODO Auto-generated method stub
		// get VSModel Name
		
		if (vsmObject == null) {
			Log.write().error("There is no VSM Object available");
			return false;
		}
		
		ArrayList<VSITransactionObject> transcations = vsmObject.getTranscations();
		if ( transcations.size() == 0) {
			Log.write().info("There is no transaction available");
			//return false;
		}
				
		Node vsiNode = XMLDomUtil.findChildNodesByName(doc, "serviceImage");	
		if (vsiNode != null) {
			if (false == vsmObject.getVSMName().isEmpty())
				vsiNode.getAttributes().getNamedItem("name").setNodeValue(vsmObject.getVSMName());	
			
			SimpleDateFormat dateFormat	= new SimpleDateFormat("yyyy-MM-dd.hh:mm:ss.SSS");
			vsiNode.getAttributes().getNamedItem("version").setNodeValue(LISA_VSI_VERSION);			
			vsiNode.getAttributes().getNamedItem("created").setNodeValue(dateFormat.format(new Date()));			
			vsiNode.getAttributes().getNamedItem("lastModified").setNodeValue(dateFormat.format(new Date()));						

			// there are transactions in VSI
			// remove all nodes in <st>...</st>
			Node firstChildNode = vsiNode.getFirstChild();
			if (firstChildNode.getTextContent().equals("st")) {
				Node stNode = firstChildNode;				
				NodeList oldChilds = stNode.getChildNodes();
				for (int i = oldChilds.getLength()-1; i > 0; i--) {
					Node oldChild =  oldChilds.item(i);
					stNode.removeChild(oldChild);
				}
			}
			
			// create the input error transaction required by ODME
			createInputErrorTransactionNode(doc, vsmObject);

			// create Admin transactions
			//createAdminRequestNodes(doc, vsmObject);		 
			
			// create GET $metadata transaction
			//createRequestMetadataNode(doc, vsmObject);
			
			// create the generic transaction for GET/POST/PUT/DELETE
			createRequestGenericNode(doc, vsmObject, "", "/<GENERIC>"); 	
			
			// create the service root transaction
			createRequestGenericNode(doc, vsmObject, "GET", "");
			
			// create the transactions populated by user 
			createTransactionNodes(doc, vsmObject);
			
			return true;
		}
		
		return false;
	}

	/**
	 * Generate the transactions predefined
	 * 
     * @param doc			- VSI document which is including a list of transactions predefined
     * @param vsmObject		- the information of VS model
	 * @return				- document of VSI 
	 * @throws Exception
	 */
	private Document createTransactionNodes(Document doc, VSMObject vsmObject) throws Exception {
		ArrayList<VSITransactionObject> transcations = vsmObject.getTranscations();
		if ( transcations.size() < 1)
			return doc;
		
		for (int i=0; i<transcations.size(); i++){
			//Document transDoc = createTransactionNode(doc, transcations.get(i));
			VSITransactionObject object = transcations.get(i);
			if (CommonDefs.isOdataVersion4(vsmObject.getOdataVersion())) {
				// verbose json has been removed from Odata version 4 
				object.setEnableVerbosejson(false);
			}
			Document transDoc = createSingleTransactionNode(object);
			XMLDomUtil.appendDocs(doc, transDoc, "st");
		}
		
		return doc;
	}
		
	/**
	 * Create the default transaction <BASE URL>/<INPUT_ERROR>
     * @param doc			- VSI document which is including a list of transactions predefined
     * @param vsmObject		- the information of VS model
	 * @return				- document of VSI 
	 * @throws Exception
	 */
	private Document createInputErrorTransactionNode( Document doc, VSMObject vsmObject) throws Exception {

		String resBody = "{&quot;odata.error&quot;:{&quot;code&quot;:&quot;&quot;,&quot;message&quot;:{&quot;lang&quot;:&quot;en-US&quot;,&quot;value&quot;:&quot;{{=incomingRequest.getArguments().get(&quot;$ERROR_MESSAGE&quot;)}}&quot;}}}";

		VSITransactionObject object = new VSITransactionObject(0);
		object.setBaseURL(vsmObject.getHttpBaseURL());
		object.setPath("/<INPUT_ERROR>");
		object.setMatchScript("");
		object.setResponseBody(resBody);
		object.setResponseCode("404");
		object.setResponseCodeText("Not Found");
		object.setEnableVerbosejson(false);
		object.setAsDefault(true);
		
		Document rootDoc = createSingleTransactionNode(object);
		XMLDomUtil.appendDocs(doc, rootDoc, "st");	    

		return doc;
		
	}
	
	/**
	 * Create the default transaction <BASE URL>/$metadata
     * @param doc			- VSI document which is including a list of transactions predefined
     * @param vsmObject		- the information of VS model
	 * @return				- document of VSI 
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	private Document createRequestMetadataNode( Document doc, VSMObject vsmObject) throws Exception {

		String resBody = ""; 

		VSITransactionObject object = new VSITransactionObject(0);
		object.setOperation("GET");
		object.setBaseURL(vsmObject.getHttpBaseURL());
		object.setPath("/$metadata");
		object.setDescription("Get Metadata");
		object.setMatchScript("");
		object.setResponseBody(resBody);
		object.setResponseCode("200");
		object.setResponseCodeText("OK");
		object.setAsDefault(true);
		object.setResponseFormat("xml");
		object.setEnableXML(true);
		object.setEnableJson(false);
		object.setEnableVerbosejson(false);
		
		Document xmlDoc = createSingleTransactionNode(object);
		XMLDomUtil.appendDocs(doc, xmlDoc, "st");	    

		return doc;
		
	}
	
	/**
	 * Create the default transaction <BASE URL>/$Generic/<method>
     * @param doc			- VSI document which is including a list of transactions predefined
     * @param vsmObject		- the information of VS model
     * @param method		- Method(GET/POST/PUT/DELETE)
	 * @return				- document of VSI 
	 * @throws Exception
	 */
	private Document createRequestGenericNode( Document doc, VSMObject vsmObject, String method, String resourcePath) throws Exception {

		String matchScript = "return true;";
		String resBody = "";
		String description = "Generic transaction";
		if (!method.isEmpty())
			description = method + " " + description;

		VSITransactionObject object = new VSITransactionObject(0);
		object.setOperation(method);
		object.setBaseURL(vsmObject.getHttpBaseURL());
		object.setPath(resourcePath);						
		object.setDescription(description);
		object.setMatchScript(matchScript);
		object.setResponseBody(resBody);
		object.setResponseCode("200");
		object.setResponseCodeText("OK");
		object.setAsDefault(true);
		object.setResponseFormat("");
		object.setEnableXML(false);
		object.setEnableJson(false);
		object.setEnableVerbosejson(false);
		
		Document xmlDoc = createSingleTransactionNode(object);
		XMLDomUtil.appendDocs(doc, xmlDoc, "st");	    

		return doc;
		
	}
	
	/**
	 * Create the default transaction for the Admin requests
	 *		GET /<CommonDefs.ADMIN_SERVICE>/Checkpoints
	 *		GET /<CommonDefs.ADMIN_SERVICE>/Checkpoints(<id>)
	 *  	PUT /<CommonDefs.ADMIN_SERVICE>/Checkpoints(<id>)
	 *		DELETE /<CommonDefs.ADMIN_SERVICE>/Checkpoints(<id>)
	 *	
     * @param doc			- VSI document which is including a list of transactions predefined
     * @param vsmObject		- the information of VS model
	 * @return				- document of VSI 
	 * @throws Exception
	 */		
	@SuppressWarnings("unused")
	private Document createAdminRequestNodes(Document doc, VSMObject vsmObject) throws Exception {
		
		String admin_resource_root = "/" + CommonDefs.ADMIN_SERVICE;
		
		final String[] operations = { "GET", "GET", "PUT", "DELETE" };
		
		final String[] resPaths   = { "/Checkpoints", 
									  "/Checkpoints(<id>)", 
									  "/Checkpoints(<id>)", 
									  "/Checkpoints(<id>)" };
		
		for (int i=0; i<operations.length; i++) {
			
			String fullResourcePath = admin_resource_root + resPaths[i];
			String description = operations[i] + " " + fullResourcePath;
			
			VSITransactionObject object = new VSITransactionObject(0);
			object.setOperation(operations[i]);
			object.setBaseURL(vsmObject.getHttpBaseURL());			
			object.setPath(fullResourcePath);
			object.setDescription(description);
			object.setMatchScript("");
			object.setResponseBody("");
			object.setResponseCode("200");
			object.setResponseCodeText("OK");
			object.setAsDefault(true);
			object.setResponseFormat("");
			object.setEnableXML(false);
			object.setEnableJson(false);
			object.setEnableVerbosejson(false);
			
			Document xmlDoc = createSingleTransactionNode(object);
			XMLDomUtil.appendDocs(doc, xmlDoc, "st");			
		}
		
		return doc;
		
	}

	/**
	 * @return the sequence number
	 */
	public static int getSequenceNum() {
		return ++sequencenum;
	}
	
	/**
	 * @param seqnum the sequence number to set
	 */
	public static void setSequenceNum(int sequencenum) {
		VSImageUtil.sequencenum = sequencenum;
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
	 * Create a document of a single transaction
	 * 
	 * @param object - the information of transaction
	 * @return		 - the document of transaction
	 * @throws Exception
	 */
	private Document createSingleTransactionNode( VSITransactionObject object ) throws Exception {
		
		// Create the root node of a transaction 
		Document doc = createDocument();
		Element	element = doc.createElement("t");
		element.setAttribute("id", Integer.toString(getSequenceNum()));
		element.setAttribute("nd", "true");
		
		element.appendChild(createRequestElement(doc, object, "OPERATION"));
		element.appendChild(createResponseElement(doc, object));

		Element	spElement = doc.createElement("sp");
		element.appendChild(spElement);
		
		VSITransactionObject newObject = VSITransactionObject.clone(object);
		
		if (object.isEnableXML()){
			// Create the xml node of a transaction 
			newObject.setResponseFormat("xml");
			Element	t = doc.createElement("t");
			t.appendChild(createRequestElement(doc, newObject, "EXACT"));
			t.appendChild(createResponseElement(doc, newObject));
			spElement.appendChild(t);
		}
		
		if (object.isEnableJson()) {
			// Create the json node of a transaction 
			newObject.setResponseFormat("json");
			Element	t = doc.createElement("t");
			t.appendChild(createRequestElement(doc, newObject, "EXACT"));
			t.appendChild(createResponseElement(doc, newObject));
			spElement.appendChild(t);
		}
		
		if (object.isEnableVerbosejson()) {
			// Create the verbosejson node of a transaction 
			newObject.setResponseFormat("verbosejson");
			Element	t = doc.createElement("t");
			t.appendChild(createRequestElement(doc, newObject, "EXACT"));
			t.appendChild(createResponseElement(doc, newObject));
			spElement.appendChild(t);
		}
		
		doc.appendChild(element);
		
		return doc;
		
	}

	/**
	 * create the request element of transaction
	 * 
	 * @param doc		 - VSI document
	 * @param object	 - the information of transaction
	 * @param matchStyle - match style 
	 * @return			 - the element of request
	 */
	public Element createRequestElement(Document doc, VSITransactionObject object, String matchStyle) {
		
		// <rq id="sequence_number" op="operation-url" tl="matchStyle">
		//		Argument Element
		//		Match Script Element
		//		Attribute Element
		// </rq>

		String resourcePath = object.getPath();
		String operation = "";
		if (object.getOperation().isEmpty())
			operation = object.getBaseURL() + resourcePath;
		else 
			operation = object.getOperation() + " " + object.getBaseURL() + resourcePath;
		
		Element rq = doc.createElement("rq");
		rq.setAttribute("id", Integer.toString(getSequenceNum()));
		rq.setAttribute("op", operation);
		rq.setAttribute("tl", matchStyle);
		
		if (!object.getResponseFormat().isEmpty())
			rq.appendChild(createArgumentElement(doc, "$format", object.getResponseFormat()));
		
		rq.appendChild(createMatchScriptElement(doc, object.getMatchScript()));
		
		String strDescription = object.getDescription();
		rq.appendChild(createAttributeElement(doc, "$Transaction_Description", strDescription));
		
		return rq;
	}

	/**
	 * create the response element of transaction
	 * 
	 * @param doc	 - VSI document
	 * @param object - the information of transaction
	 * @return 		 - the element of response
	 */
	public Element createResponseElement(Document doc, VSITransactionObject object) {
		// <rs>
		// 		<rp id="sequence_number" t="0">
		//			<m>
		//				<p n="HTTP-Response-Code">200</p>
		//				<p n="HTTP-Response-Code-Text">OK</p>
		//				<p n="Content-Type">application/xml; charset=utf-8</p>
		//				<p n="Server">LISA/Virtual-Environment-Server</p>
		//				<p n="Date">{{=httpNow()}}</p>
		//				<p n="X-Powered-By">LISA/{{=lisaVersionString()}}</p>
		//			</m>
		//			<bd>
		//			</bd>
		// 		</rp>
		// </rs>

		Element rp = doc.createElement("rp");
		rp.setAttribute("id", Integer.toString(getSequenceNum()));
		rp.setAttribute("t", "0");
		
		Element m = doc.createElement("m");
		Element p = doc.createElement("p");
		p.setAttribute("n", "HTTP-Response-Code"); p.setTextContent(object.getResponseCode());
		m.appendChild(p);

		p = doc.createElement("p");
		p.setAttribute("n", "HTTP-Response-Code-Text"); p.setTextContent(object.getResponseCodeText());
		m.appendChild(p);

		String contentType = "application/json";
		if (object.getResponseFormat().equalsIgnoreCase("xml"))
			contentType = "application/xml";
		p = doc.createElement("p");
		p.setAttribute("n", "Content-Type"); p.setTextContent(contentType);
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
		
		Element bd = doc.createElement("bd");
		bd.setTextContent(object.getResponseBody());

		rp.appendChild(m);
		rp.appendChild(bd);
		
		Element rs = doc.createElement("rs");
		rs.appendChild(rp);
		
		return rs;
	}
	
	/**
	 * create Argument Element
	 * @param doc	 	- VSI document
	 * @param argName	- the value of argument
	 * @param argValue	- the value of argument
	 * @return			- the element of argument
	 */
	public Element createArgumentElement(Document doc, String argName, String argValue) {
		
		// <ag>
		//		<p n="argName" t="arg:EQUALS;false;;;CASE_INSENSITIVE;">argValue</p>
		// </ag>
		
		Element pElement	= doc.createElement("p");		
		pElement.setAttribute("n", argName);		
		pElement.setAttribute("t", "arg:EQUALS;false;;;CASE_INSENSITIVE;");		
		pElement.setTextContent(argValue);
		
		Element agElement	= doc.createElement("ag");
		agElement.appendChild(pElement);
		
		return agElement;
	}
	
	/**
	 * create Attribute Element
	 * @param doc 		- VSI document
	 * @param attrName	- attribute name
	 * @param attrValue	- the value of attrName
	 * @return 			- an element of the specified attribute
	 */
	public Element createAttributeElement(Document doc, String attrName, String attrValue) {
		
		// <at>
		//		<p n="attrName">argValue</p>
		//		<p n="$Transaction_Description">GET /Assets</p>
		// </at>
		
		Element pElement	= doc.createElement("p");		
		pElement.setAttribute("n", attrName);		
		pElement.setTextContent(attrValue);
		
		Element agElement	= doc.createElement("at");		
		agElement.appendChild(pElement);
		
		return agElement;
	}

	/**
	 * Generates the element of Match Script
	 * 
	 * @param doc 			- VSI document
	 * @param matchScript	- the content of match script
	 * @return 				- an element of Match Script
	 */
	public Element createMatchScriptElement(Document doc, String matchScript) {
		
		// <ms> 
		//		matchScript
		// </ms>
		
		Element msElement	= doc.createElement("ms");
		if (matchScript != null && !matchScript.isEmpty())
			msElement.setTextContent(matchScript);
		
		return msElement;
	}
	
}
