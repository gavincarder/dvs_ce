/*******************************************************************************
 * Copyright (c) 2015 CA.  All rights reserved.
 *
 * This source file is licensed under the terms of the Eclipse Public License 1.0
 * For the full text of the EPL please see https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ca.dvs.app.dvs_servlet.misc;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

import javax.ws.rs.core.UriBuilder;

/**
 * Model a LISA/DevTest Virtual Service
 * <p>
 * @author CA Technologies
 * @version 1
 */
public class VirtualService {

	private String name;
	private String basePath;
	private String protocol;
	private int port;
	private int status;
	private String domainName;
	private URI svcCtrlUri;
	
	public VirtualService () {
		
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getBasePath() {
		return basePath;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getDomainName(){
		return this.domainName;
	}
	
	public void setDomainName(String domain){
		
		// convert localhost to hostname, otherwise response might include incorrect link on client UI
		domain = convertLocalHostToHostname(domain);
		
		// set domain name
		this.domainName = domain;
	}
	
	public URI getServiceUri() throws URISyntaxException{
		return  new URI(this.protocol + "://" + this.domainName + ":" + this.port + this.basePath);
	}
	
	public void setServiceCtrlUri(String ctrlUri) throws URISyntaxException{
		
		// do nothing if ctrl Uri string contains whitespace which is an invalid URI.
		if (ctrlUri.contains(" "))
			return;
		
		URI uri = new URI(ctrlUri);
	    String hostname = uri.getHost();
	 
	    // convert localhost to hostname, otherwise response might include incorrect link on client UI
	    hostname = convertLocalHostToHostname(hostname);		
	    uri = UriBuilder.fromUri(uri).host(hostname).build();
	    
	    // set service control URI
	 	this.svcCtrlUri = uri;
	}
	
	public URI getServiceCtrlUri(){
		return this.svcCtrlUri;
	}
	
	private String convertLocalHostToHostname(String hostname){
		String _hostname = null;
		
		// return right away if it is empty or null
		if (hostname == null || hostname.isEmpty())
			return hostname;  
		
		// proceed to convert hostname if it is localhost
		if (hostname.equalsIgnoreCase("localhost"))  
			try {
				_hostname = InetAddress.getLocalHost().getHostName();
			} catch (UnknownHostException e) {
				// failed;  try alternate means.
				_hostname = System.getenv("COMPUTERNAME");  	// Windows
				if (_hostname == null || _hostname.isEmpty())
					_hostname = System.getenv("HOSTNAME");		// Unix
			}

		// if all three methods above failed, use localhost
		if (_hostname != null && !_hostname.isEmpty())
			hostname = _hostname;
		
		return hostname;
	}
}
