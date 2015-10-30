/*******************************************************************************
 * Copyright (c) 2015 CA.  All rights reserved.
 *
 * This source file is licensed under the terms of the Eclipse Public License 1.0
 * For the full text of the EPL please see https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
/**
 * 
 */
package com.ca.dvs.app.dvs_servlet.resources.test;

import static org.junit.Assert.assertEquals;

import java.io.File;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.ServletDeploymentContext;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.Test;

import com.ca.dvs.app.dvs_servlet.RestServlet;

/**
 * @author artal03
 *
 */
public class RAMLResourceTest extends JerseyTest {
		 
	/* (non-Javadoc)
	 * @see org.glassfish.jersey.test.JerseyTest#getTestContainerFactory()
	 */
	@Override
	protected TestContainerFactory getTestContainerFactory()
			throws TestContainerException {
		return new GrizzlyWebTestContainerFactory();
	}

	@Override
	protected DeploymentContext configureDeployment() {
		return ServletDeploymentContext.forServlet(new ServletContainer(
                new RestServlet())).build();
	}
	
	@Override
	protected void configureClient(ClientConfig config) {
	    config.register(MultiPartFeature.class);
	}
	
    @Test
    public void testGetConfig() {
        final String configJson = target("raml/config").request().get(String.class);
        assertEquals("{}", configJson);
    }
    
    @Test
    public void testGetSampleData() {
        FormDataMultiPart multiPart = new FormDataMultiPart();

        FileDataBodyPart fileDataBodyPart = new FileDataBodyPart("file",
                new File("test/resources/jug/jug_odata.raml"), MediaType.APPLICATION_OCTET_STREAM_TYPE);

        
        multiPart.bodyPart(fileDataBodyPart);

        Response response = target("raml/sampleData").request().post(Entity.entity(multiPart, multiPart.getMediaType()));
        
        assertEquals(200, response.getStatus());

        /* String s = */ response.readEntity(String.class); // The sampleData

        fileDataBodyPart = new FileDataBodyPart("file",
                new File("test/resources/jukebox-api.zip"), MediaType.APPLICATION_OCTET_STREAM_TYPE);

        multiPart = new FormDataMultiPart();
        multiPart.bodyPart(fileDataBodyPart);

        response = target("raml/sampleData").request().post(Entity.entity(multiPart, multiPart.getMediaType()));
        
        assertEquals(200, response.getStatus());

        /* s = */ response.readEntity(String.class); // The sampleData
        
        fileDataBodyPart = new FileDataBodyPart("file",
                new File("test/resources/OData_test/OData_test.raml"), MediaType.APPLICATION_OCTET_STREAM_TYPE);

        
        multiPart = new FormDataMultiPart();
        multiPart.bodyPart(fileDataBodyPart);

        response = target("raml/sampleData").request().post(Entity.entity(multiPart, multiPart.getMediaType()));
        
        assertEquals(200, response.getStatus());

        /* s = */ response.readEntity(String.class); // The sampleData
        
    }
    
    @Test
    public void testGenEdm() {
        
        FormDataMultiPart multiPart = new FormDataMultiPart();

        FileDataBodyPart fileDataBodyPart = new FileDataBodyPart("file",
                new File("test/resources/jug/jug_odata.raml"), MediaType.APPLICATION_OCTET_STREAM_TYPE);

        
        multiPart.bodyPart(fileDataBodyPart);

        Response response = target("raml/edm").request().post(Entity.entity(multiPart, multiPart.getMediaType()));
        
        assertEquals(200, response.getStatus());

        /* String s = */ response.readEntity(String.class); // The EDM
        
    }
    
    @Test
    public void testGenWadl() {
        
        FormDataMultiPart multiPart = new FormDataMultiPart();

        FileDataBodyPart fileDataBodyPart = new FileDataBodyPart("file",
                new File("test/resources/jug/jug_odata.raml"), MediaType.APPLICATION_OCTET_STREAM_TYPE);

        
        multiPart.bodyPart(fileDataBodyPart);

        Response response = target("raml/wadl").request().post(Entity.entity(multiPart, multiPart.getMediaType()));
        
        assertEquals(200, response.getStatus());

        /* String s = */ response.readEntity(String.class); // The WADL
        
    }

}