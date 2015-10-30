/*******************************************************************************
 * Copyright (c) 2015 CA.  All rights reserved.
 *
 * This source file is licensed under the terms of the Eclipse Public License 1.0
 * For the full text of the EPL please see https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
/**
 * 
 */
package com.ca.dvs.app.dvs_servlet;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

/**
 * DVS servlet class
 * <p>
 * @author CA Technologies
 * @version 1
 */
@ApplicationPath("rest") // base URI
public class RestServlet extends ResourceConfig {

	/**
	 * Default constructor for DVS servlet.  Registers dependent facilities used by the servlet.
	 * <p>
	 */
	public RestServlet() {
		register(MultiPartFeature.class);
		register(LoggingFilter.class);
		register(JacksonJsonProvider.class);
		packages("com.ca.dvs.app.dvs_servlet.resources");
	}

}
