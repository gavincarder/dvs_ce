/*******************************************************************************
 * Copyright (c) 2015 CA.  All rights reserved.
 *
 * This source file is licensed under the terms of the Eclipse Public License 1.0
 * For the full text of the EPL please see https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ca.dvs.app.dvs_servlet.misc;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.glassfish.jersey.internal.util.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * CA LISA/DevTest virtual service controller
 * <p>
 * @author CA Technologies
 * @version 1
 */
public class VSEController {

	private URL vseUrl;
	private static Logger logger = Logger.getLogger(VSEController.class);
	
	private String authorization = null;
	
	public VSEController(String vseUrl, String authorization) throws Exception {
		this.vseUrl = new URL(vseUrl);
		if (authorization!=null && !authorization.isEmpty()) {
			setAuthorization(authorization);
		}
	}
	
	public static String getLisaErrorResponse(InputStream errorStream) throws Exception {
		
    	StringBuffer response = new StringBuffer();
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		dbFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true); 
		DocumentBuilder        dBuilder  = dbFactory.newDocumentBuilder();
		
		Document	doc	= dBuilder.parse(errorStream);
		NodeList errorNodes = doc.getElementsByTagName("Error");
		if (errorNodes.getLength()>0) {
			for (int i=0; i < errorNodes.getLength(); i++) {
				Node		errorNode = errorNodes.item(i);
				NodeList 	childNodes = errorNode.getChildNodes();
				
				String id=null;
				String message=null;
			
				for (int cIdx = 0; cIdx < childNodes.getLength(); cIdx++) {
					Node childNode = childNodes.item(cIdx);

					if (childNode.getNodeType() == Node.ELEMENT_NODE) {
						Element el = (Element)childNode;
						String tag = el.getTagName();
						switch (tag) {
							case "Id":
								id = el.getTextContent();
								break;
							case "Message":
								message = el.getTextContent();
								break;
							default:
						}
					}								
				}
				response.append(String.format("Error %s - %s\n", id, message));
			}
		} else {
			throw new Exception("Error stream does not contain LISA Error message");
		}
		return response.toString();
	}
	
	public HashMap<String, VSE> getVseList() throws Exception{
		HashMap<String, VSE> vseList = new HashMap<String, VSE>();
		BufferedReader in = null;
		try {
						
		    HttpURLConnection conn = (HttpURLConnection) this.vseUrl.openConnection();

		    if (getAuthorization()!=null) {
				String encodedPswd = Base64.encodeAsString(getAuthorization());
			    conn.setRequestProperty("Authorization", "Basic " + encodedPswd);
		    }

		    conn.setRequestMethod("GET");
		    conn.setRequestProperty("Content-Type", "application/vnd.ca.lisaInvoke.vseList+xml");
			    
		    // Get the response for the GET VSE list
		    int responseCode = conn.getResponseCode();
		    
		    if (responseCode >= 200 && responseCode <= 300) {
		    	in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

		    	String inputLine;
				StringBuffer response = new StringBuffer();
		 
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				//in.close();
				
				//convert the response to xml doc
				Document doc = readXmlString(response.toString());
				
				//construct the vse list
				NodeList vseNList = doc.getElementsByTagName("Vse");
		    	for (int i=0; i < vseNList.getLength(); i++) {
		    		
		    		Element vseEle = (Element) vseNList.item(i);
		    		VSE vse = new VSE();
		    		
		    		vse.setName(vseEle.getAttribute("name"));
		    		
		    		//get the virtual services belonging to this vse 
			        NodeList virtualSvcNList = vseEle.getElementsByTagName("VirtualService");
			        
			        ArrayList<VirtualService> virtualSvcList = new ArrayList<VirtualService>();
			        
			        for (int j=0; j < virtualSvcNList.getLength(); j++) {
			        	
			        	Element virtualSvcEle = (Element) virtualSvcNList.item(j);
			        	VirtualService  virtualSvc = createVirtualServiceFromDom(virtualSvcEle);

		                virtualSvcList.add(virtualSvc);
			        }
			        vse.setVirtualSvcs(virtualSvcList);
			        
			        vseList.put(vse.getName(),vse);

		    	}
				
		    } else {
				
		    	StringBuffer sbError = new StringBuffer();
		    	
		    	sbError.append(String.format("Error getting list of virtual services from configured LISA VSE server: %s - ", vseUrl));

		    	try {
		    		sbError.append(getLisaErrorResponse(conn.getErrorStream()));
		    	} catch(Exception ex) {
		    		sbError.append(String.format("response code: %s", Integer.toString(responseCode)));
				}

				logger.error(sbError.toString());
				throw new Exception(sbError.toString(), null);
			}
		 
		    return vseList;

		} catch(Exception e) {
			throw e;
		}
		finally{
			if (in != null)
				in.close();
		}
	}

	public VirtualService getVirtualService(String vsName) throws Exception{
		BufferedReader in = null;
		try {
						
			URL url = new URL(vseUrl + "/" + vsName);
		    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		    if (getAuthorization()!=null) {
				String encodedPswd = Base64.encodeAsString(getAuthorization());
			    conn.setRequestProperty("Authorization", "Basic " + encodedPswd);
		    }
		    
		    conn.setRequestMethod("GET");

		    conn.setRequestProperty("Content-Type", "application/vnd.ca.lisaInvoke.vseList+xml");
			    
		    // Get the response for the GET VSE list
		    int responseCode = conn.getResponseCode();
		    
		    if (responseCode >= 200 && responseCode <= 300) {
		    	in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

		    	String inputLine;
				StringBuffer response = new StringBuffer();
		 
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
				
				//convert the response to xml doc
				Document doc = readXmlString(response.toString());

		    	//get the virtual service
			    NodeList virtualSvcNList = doc.getElementsByTagName("VirtualService");
			    //Should be only one.
			     			        	
			    Element virtualSvcEle = (Element) virtualSvcNList.item(0);
			    return createVirtualServiceFromDom(virtualSvcEle);

		    }else {
		    	
		    	String errMessage = null;

		    	try {
		    		errMessage = getLisaErrorResponse(conn.getErrorStream());
		    	} catch(Exception ex) {
					errMessage = String.format("response code: %s", Integer.toString(responseCode));
				}

				String errMsg = String.format("Error getting virtual service %s - %s", vsName, errMessage);
				throw new Exception(errMsg, null);
			}	

		} catch(Exception e) {
			throw e;
		} finally{
			if (in != null)
				in.close();
		}
	}
	
	private VirtualService createVirtualServiceFromDom(Node virtualSvcEle) throws URISyntaxException {
	    VirtualService virtualSvc = new VirtualService();
	        	
	    NodeList virtualSvcPropList = virtualSvcEle.getChildNodes();
    	virtualSvc.setName(((Element) virtualSvcEle).getAttribute("name"));
    	String SvcCtrlUri = ((Element) virtualSvcEle).getAttribute("href");
    	virtualSvc.setDomainName(getDomainNameFromUri(SvcCtrlUri));
    	virtualSvc.setServiceCtrlUri(SvcCtrlUri);
        for (int k=0; k < virtualSvcPropList.getLength(); k++) {
        	Node node = virtualSvcPropList.item(k);        	
            /*if (node.getNodeName().equals("ModelName")) {
            	virtualSvc.setName(node.getTextContent());
            }else */if (node.getNodeName().equals("ResourceName")) {
            	String[] toks = node.getTextContent().split(":");
            	if (toks.length >= 4) {
            		virtualSvc.setPort(Integer.parseInt(toks[0].trim()));
            		virtualSvc.setProtocol(toks[1].trim());
            		virtualSvc.setBasePath(toks[3].trim());
            	}
            }else if (node.getNodeName().equals("Status")) {
            	virtualSvc.setStatus(Integer.parseInt(node.getTextContent()));
            }
        }
		return virtualSvc;
	}
	
	private String getDomainNameFromUri(String strUri) throws URISyntaxException{
		strUri = strUri.substring(0, strUri.lastIndexOf("/"));
		URI uri = new URI(strUri);
	    return uri.getHost();
	}
	
	public String getNewServiceName(String svcNameSeed, HashMap<String, VSE> vseList) throws Exception {
		
		//Expects the caller of this method to provide the initial seed for service name, otherwise
		//throw an exception
		if(svcNameSeed == null || svcNameSeed.isEmpty())
			throw new Exception("No default service name provided");
		
		// Replace space to underscore
		svcNameSeed = svcNameSeed.replaceAll(" ", "_");
		
		ArrayList<String> usedSvcNameList = new ArrayList<String>();
		
		for(String vseKey : vseList.keySet()) {
     		VSE vse = vseList.get(vseKey);
     		for (VirtualService virtualSvc : vse.getVirtualSvcs()) {
     			usedSvcNameList.add(virtualSvc.getName());
     		}

     	}
		
		if (!usedSvcNameList.contains(svcNameSeed))
			return svcNameSeed;
		else {
			boolean bUsed = true;
			int counter = 2;
			String tNewSvcName = "";
			while (bUsed) {
				tNewSvcName = svcNameSeed + "_" + counter++;
				bUsed = usedSvcNameList.contains(tNewSvcName);
			}
			return tNewSvcName;
		}
		
	}
	
	/**
	 * @param host the host on which to check for a listening port
	 * @param port the port to check for a listening service
	 * @return true if the specified host:port refers to an active listening service, otherwise false
	 */
	private boolean isPortPresent(String host, int port) {
		boolean isPresent = false;
		
		try {
			Socket s = new Socket(host, port);
			isPresent = true;
			s.close();
		} catch (Exception e) { // Exceptions - i.e. host is unknown or unreachable or if specified port is not listening
		}
		
		return isPresent;
	}

	public int getAvailablePort(int portSeed, String portRange, HashMap<String, VSE> vseList) throws Exception {
		
		//Expects the caller of this method to provide a valid initial seed for port name
		//If initial port is not valid, then we will assign one from the port range provided 
		//and if range is provided, then this method throws an exception.
		
		ArrayList<Integer> usedPortsList = new ArrayList<Integer>();
		
		for(String vseKey : vseList.keySet()) {
     		VSE vse = vseList.get(vseKey);
     		for (VirtualService virtualSvc : vse.getVirtualSvcs()) {
     			usedPortsList.add(virtualSvc.getPort());
     		}

     	}
				
		if (isValidPort(portSeed) && !usedPortsList.contains(portSeed)  && !isPortPresent(this.vseUrl.getHost(), portSeed))
			return portSeed;
		else {
			String[] portRangeToks= portRange.split(",");
			for (int i = 0; i < portRangeToks.length ; i++) {
				if (portRangeToks[i].matches("^\\w+-\\w+$")) {
					//range
					
					int minPortInRange = Integer.parseInt(portRangeToks[i].split("-")[0].trim());
					int maxPortInRange = Integer.parseInt(portRangeToks[i].split("-")[1].trim());
					
					if (minPortInRange <= maxPortInRange) {
						if (isValidPort(minPortInRange) && isValidPort(maxPortInRange)) {
							for (int portToCheck = minPortInRange ; portToCheck <= maxPortInRange; portToCheck++ ) {
								if (!usedPortsList.contains(portToCheck) && !isPortPresent(this.vseUrl.getHost(), portToCheck))
									return portToCheck;
							}
						}
					}
				}else if (portRangeToks[i].matches("^\\w+$")) {
					int port = Integer.parseInt(portRangeToks[i].trim());
					if(isValidPort(port) &&  !usedPortsList.contains(port) && !isPortPresent(this.vseUrl.getHost(), port))
						return port;
				}

			}

		}
		
		//if you are here, then the passed in port is in use or not valid and none of ports in the passed in
		//port ranges was valid or is in use by LISA VSE
		//throw exception
		throw new Exception("No free port found");
		
	}
	
	public boolean isValidPort(int port)
    {

        if (port >=1024 && port <= 65535)
        	return true;

        return false;
    }
	
	public void deployMar(File marFile) throws Exception {
		
		OutputStream output = null;
		try {
			String LF = "\r\n"; // Line separator required by multipart/form-data.
			String charset = "UTF-8";
			URL url = new URL(vseUrl + "/actions/deployMar");
		    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		    
		    conn.setRequestMethod("POST");
		    conn.setDoOutput(true);
		    conn.setDoInput(true);
		    
		    String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.
		    conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

		    if (getAuthorization()!=null) {
				String encodedPswd = Base64.encodeAsString(getAuthorization());
			    conn.setRequestProperty("Authorization", "Basic " + encodedPswd);
		    }
		    			
		    output = conn.getOutputStream();
		    PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true);

		    // upload mar file (this is a zip file)
		    writer.append("--" + boundary).append(LF);
			writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + marFile.getName() + "\"").append(LF);
			writer.append("Content-Type: " + "application/zip").append(LF);
			writer.append("Content-Transfer-Encoding: binary").append(LF);
			writer.append(LF).flush();
			Files.copy(marFile.toPath(), output);
		    output.flush(); 
		    writer.append(LF).flush(); // end of boundary.

		    // End of multipart/form-data.
		    writer.append("--" + boundary + "--").append(LF).flush();
		    writer.close();

		    // Get the response for deployMar POST request
		    int responseCode = conn.getResponseCode();
		   	
		    BufferedReader in;
		    if (responseCode == 200 || responseCode == 201) {	
		    	in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

		    	String inputLine;
				StringBuffer response = new StringBuffer();
			 
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
		    } else {
		    	
		    	StringBuffer sbError = new StringBuffer();
		    	
		    	sbError.append(String.format("Failed to deploy virtual service MAR to configured LISA VSE server: %s - ", vseUrl));

		    	try {
		    		sbError.append(getLisaErrorResponse(conn.getErrorStream()));
		    	} catch(Exception ex) {
		    		sbError.append(String.format("response code: %s", Integer.toString(responseCode)));
				}

				logger.error(sbError.toString());
				throw new Exception(sbError.toString(), null);
			}

		} catch(Exception e) {
			throw e;
		} finally {
			if (output != null)
				output.close();
		}
	}
	
	/**
	 * Delete a virtual service
	 * @param vsName
	 * @throws Exception
	 */
	public void deleteVirtualService(String vsName) throws Exception{
		try {
						
			URL url = new URL(vseUrl + "/" + vsName);
		    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		    if (getAuthorization()!=null) {
				String encodedPswd = Base64.encodeAsString(getAuthorization());
			    conn.setRequestProperty("Authorization", "Basic " + encodedPswd);
		    }
		    
		    conn.setRequestMethod("DELETE");
			    
		    // Get the response for the GET VSE list
		    int responseCode = conn.getResponseCode();
		    
		    // response code 204 or 404 is considered to pass the check because 404 is invalid non-existed service name.
		    if (responseCode != 204 && responseCode != 404) {

		    	StringBuffer sbError = new StringBuffer();
		    	
		    	sbError.append(String.format("Failed to delete virtual service: %s - ", vsName));

		    	try {
		    		sbError.append(getLisaErrorResponse(conn.getErrorStream()));
		    	} catch(Exception ex) {
		    		sbError.append(String.format("response code: %s", Integer.toString(responseCode)));
				}

				logger.error(sbError.toString());
				throw new Exception(sbError.toString(), null);
			}	

		} catch(Exception e) {
			throw e;
		}		
	}

	/*
     *  Reads an xml string into XML Document.
     *  xmlStr: String containing xml
     *  Return xml Document
     *
     */
    public Document readXmlString(String xmlStr) throws Exception {

        Document doc = null;

		try {
			
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
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
	 * @return the authorization
	 */
	public String getAuthorization() {
		return authorization;
	}

	/**
	 * @param authorization the authorization to set
	 */
	public void setAuthorization(String authorization) {
		this.authorization = authorization;
	}

    public static void main(String [] args) {

		
		try {
		
			VSEController vseCtrl = new VSEController("http://devjo01w7a:1505/api/Dcm/VSEs/VSE", "admin:admin");
			HashMap<String, VSE> vseList = vseCtrl.getVseList();
			System.out.println(vseCtrl.getNewServiceName("", vseList));
			//System.out.println(vseCtrl.getNewServiceName("JuneDemo_2", vseList));
			
			
//			VirtualService vs = vseCtrl.getVirtualService("test_set_odata_v3");
//			System.out.println(vs.getName());
//			System.out.println(vs.getBasePath());
//			System.out.println(vs.getStatus());
//			
//
//			for (String key : vseCtrl.getVseList().keySet()) {
//				VSE vse = vseCtrl.getVseList().get(key);
//				
//				for (VirtualService vs1 : vse.getVirtualSvcs()) {
//					System.out.println(vs1.getName());
//					System.out.println(vs1.getPort());
//					System.out.println(vs1.getBasePath());
//					System.out.println(vs1.getStatus());
//					
//				}
//			}

			
			File marFile = new File("c:/mars/dvs_Test_Service.mar");
			vseCtrl.deployMar(marFile);
//			
//			HashSet<Integer> portsInUseList = getVsePortsInUse("devjo01w7a","1505");
//
//			Integer[] intarray = portsInUseList.toArray(new Integer[0]);
//			for (int i = 0; i < intarray.length ; i++)
//				System.out.println(intarray[i]);
////			
////			ServerSocket serverSocket = new ServerSocket(0);
////		    System.out.println("listening on port " + serverSocket.getLocalPort());
////		    serverSocket.close();
//		    
//		    System.out.println(getNewServiceNameFromVSE("JuneDemo", "http://devjo01w7a:1505/api/Dcm/VSEs/VSE"));
//    
//		    System.out.println(getAvailablePortFromVSE(8094, "9986-9989,8094-9094,1024-7890,4599","http://devjo01w7a:1505/api/Dcm/VSEs/VSE"));
//		    File marFile = new File("c:/mars/dvs_Test_Service.mar");
//		    deployMar(marFile,"http://devjo01w7a:1505/api/Dcm/VSEs/VSE");
		} catch (Exception e) {
				// TODO Auto-generated catch block
			e.printStackTrace();
		}
		                  
	 }
}
