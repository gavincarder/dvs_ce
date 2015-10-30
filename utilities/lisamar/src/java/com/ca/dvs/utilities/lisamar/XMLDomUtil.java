/*******************************************************************************
 * Copyright (c) 2015 CA.  All rights reserved.
 *
 * This source file is licensed under the terms of the Eclipse Public License 1.0
 * For the full text of the EPL please see https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ca.dvs.utilities.lisamar;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

public class XMLDomUtil {

	/**
	 * Returns the string of InputStream
	 * 
	 * convert InputStream to String
	 * @param is	InputStream
	 * @return
	 */
	private static String getStringFromInputStream(InputStream is) {
 
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();
 
		String line;
		try {
 
			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
 
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
 
		return sb.toString();
 
	}

	/**
	 * Creates a new document
	 * 
	 * @return the document
	 * @throws Exception 
	 */
	public static Document createDocument() throws Exception {

		DocumentBuilderFactory dbf      = DocumentBuilderFactory.newInstance();
        DocumentBuilder        builder  = dbf.newDocumentBuilder();	        
        return builder.newDocument();

	}
	
	/**
	 * Parse the content of the given file as an XML document and return a new DOM
	 * 
	 * @param fileName
	 * @return xml Document
	 * @throws Exception
	 */
    public static Document readXmlFile(String fileName) throws Exception{

        Document doc = null;

		try {
            File xmlFile = new File(fileName);
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance(); 
            domFactory.setNamespaceAware(false); 

            DocumentBuilder builder = domFactory.newDocumentBuilder();
            doc = builder.parse(xmlFile);
            
    	} 
		catch (ParserConfigurationException e) {
            throw new Exception(e.getMessage(), e);
		}
        catch (Exception e) {
            throw new Exception(e.getMessage(), e);
        }
        return doc;

    }

	/**
	 * Reads an xml file into XML Document.
	 * @param fileName
	 * @return xml Document
	 * @throws Exception
	 */
    public static Document readXmlFile(InputStream is) throws Exception{
        Document doc = null;
    	String xmlStr = getStringFromInputStream(is);
    	if (false == xmlStr.isEmpty())
    		doc = readXmlString(xmlStr);
    	
    	return doc;

    }
    
    /**
     * Writes  XML Document into an xml file.
     * 
     * @param fileName  the target file with the full path
     * @param document	the source document
     * @return	boolean true if the file saved
     * @throws Exception
     */
    public static boolean writeXmlFile(String fileName, Document document) throws Exception{

    	// creating and writing to xml file  
    	
    	File file = new File(fileName);

    	TransformerFactory transformerFactory = TransformerFactory.newInstance();  
    	transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);  // to prevent XML External Entities attack
    	
    	Transformer transformer = transformerFactory.newTransformer(); 
    	transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    	transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    	transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
    	transformer.transform(new DOMSource(document), new StreamResult(file));
    	
        return true;

    }

    /**
     * Reads an xml string into XML Document.
     * 
     * @param xmlStr String containing xml
     * @return xml Document
     * @throws Exception
     */
    public static Document readXmlString(String xmlStr) throws Exception {

        Document doc = null;

		try {
			
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance(); 
            domFactory.setNamespaceAware(false); 

            DocumentBuilder builder = domFactory.newDocumentBuilder(); 
            InputSource inStream = new InputSource();
            inStream.setCharacterStream(new StringReader(xmlStr)); 
            doc = builder.parse(inStream);
    	} 
        catch (ParserConfigurationException e) {
            throw new Exception(e.getMessage(), e);
        }
        catch (SAXException e) {
            throw new Exception(e.getMessage(), e);
        }
        catch (IOException e) {
            throw new Exception(e.getMessage(), e);
        }
        catch (Exception e) {
            throw new Exception(e.getMessage(), e);
        }
		
        return doc;

    }
    
    /**
     * Converts the xml into a document
     * 
     * @param xml	the xml formated string
     * @return		a new document
     * @throws SAXException
     * @throws IOException
     * @throws ParserConfigurationException
     */
    public static Document xmlToDoc(String xml) throws SAXException, IOException, ParserConfigurationException{
	    Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
	            .parse(new InputSource(new StringReader(xml)));
	    
	    return doc;    	
    }
        
    /**
     * Appends two documents
     * 
     * @param curDoc		source document
     * @param appendDoc		document to append to source
     * @param eleNameToAdd	element tag name in source to which the appendDoc has to be appended as a child
     * @throws Exception
     */
    public static void appendDocs (Document curDoc, Document appendDoc, String eleNameToAdd) throws Exception {
    
         Node importNode = curDoc.importNode(appendDoc.getDocumentElement(), true);
         curDoc.getElementsByTagName(eleNameToAdd).item(0).appendChild(importNode);
        
    }

    /**
     * Search a first node by name in the document
     * 
     * @param doc		source document
     * @param nodeName	the node name for searching
     * @return			Node with the specified name
     * @see				Node
     * @throws Exception
     */
    public static Node findChildNodesByName(Document doc, String nodeName) throws Exception {
    	
    	Node node = null;
    	
    	NodeList nl = doc.getChildNodes();
    	for (int i=0; i<nl.getLength(); i++){
    		node = nl.item(i);
    		if (node.getNodeName().equals(nodeName)) {
    			break;
    		}
    	}
    	
        return node;
 
    }
    
    /**
     * Search a child node by name 
     * 
     * @param parent	the parent node
     * @param nodeName	the node name for searching
     * @return			Node with the specified name
     * @see				Node
     * @throws Exception
     */
    public static Node findChildNodeByName(Node parent, String nodeName) throws Exception {
    	
    	Node child = null;
        Node node = parent.getFirstChild();
        while (node != null) {
    		if (node.getNodeName().equals(nodeName)) {
    			child = node;
    			break;
    		}
    		node = node.getNextSibling();
    	}
        
        return child;
    }
    
    /**
     * Search a child node by type
     * 
     * @param parent	the parent node
     * @param nodeType  the node type for searching
     * @return			Node with the specified name
     * @see				Node
     * @throws Exception
     */
    public static Node findChildNodeByType(Node parent, String nodeType) throws Exception {
        NodeList nl = parent.getChildNodes();
    	for (int i=0; i<nl.getLength(); i++){
    		Node node = nl.item(i);
    		if (node.getNodeName().equals("Node")){
    			String strType = node.getAttributes().getNamedItem("type").getNodeValue();
    			if (strType.equals(nodeType))
    				return node;
    		}
    	}
        return null;
    }
    
    /**
     * Search a child node by type
     * 
     * @param parent	the parent node
     * @param nodeName	the node name
     * @param nodeType  the node type for searching
     * @return			Node with the specified name
     * @see				Node
     * @throws Exception
     */
    public static Node findChildNodeByNameAndType(Node parent, String nodeName, String nodeType) throws Exception {
        NodeList nl = parent.getChildNodes();
    	for (int i=0; i<nl.getLength(); i++){
    		Node node = nl.item(i);
    		if (node.getNodeName().equals(nodeName)){
    			String strType = node.getAttributes().getNamedItem("type").getNodeValue();
    			if (strType.equals(nodeType))
    				return node;
    		}
    	}
        return null;
    }
    
    /**
     * Gets a node value by name
     * 
     * @param parent	the parent node
     * @param nodeName	the node name
     * @return String	the value of the node 
     * @throws Exception
     */
    public static String getChildNodeValueByName(Node parent, String nodeName) throws Exception {
        Node node = findChildNodeByName(parent, nodeName);
        if (node != null && node.getFirstChild() != null) {   
     		return node.getFirstChild().getNodeValue().trim();    		
    	}
    	
        return "";
    }
    
    /**
     * Updates the specified the child node
     *  
     * @param parent	the parent node
     * @param nodeName	the name of child node to update
     * @param nodeValue	the new value
     * @return boolean  true 
     * @throws Exception
     */
    public static boolean updateChildNodeValueByName(Document doc, Node parent, String nodeName, String nodeValue) throws Exception {
    	Node node = findChildNodeByName(parent, nodeName);
        if (node == null)
        	return false;
        
        Node firstNode = node.getFirstChild();
        if (firstNode == null){
         	node.appendChild( doc.createTextNode(nodeValue));
        }
        else {
        	firstNode.setNodeValue(nodeValue);
        }
        return true;    	
    	
    }  
    
    /*
     * Converts the string to XML string
     * 
     * @param instr
     * @return	String
     * 
    */
    public static String convertToXMLString(String instr){
    	//XML has a special set of characters that cannot be used in normal XML strings
    	String ourstr = instr;
    	ourstr = ourstr.replace("&", "&amp;");
    	ourstr = ourstr.replace("<", "&lt;");
    	ourstr = ourstr.replace(">", "&gt;");
    	ourstr = ourstr.replace("\"", "&quot;");
    	ourstr = ourstr.replace("'", "&#39;");
    	
    	return ourstr;
    }

}
