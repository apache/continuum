package org.apache.continuum.webdav;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;
import net.sf.ehcache.CacheManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.spring.PlexusInSpringTestCase;
import org.codehaus.plexus.util.Base64;

import java.io.File;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;

public class WorkingCopyServletTest
    extends PlexusInSpringTestCase
{
    private static final String REQUEST_PATH = "http://machine.com/workingcopy/1/";

    private WebRequest request;

    private WebResponse response;

    private ServletRunner sr;

    private ServletUnitClient sc;

    private File workingDirectory;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        String appserverBase = getTestFile( "target/appserver-base" ).getAbsolutePath();
        System.setProperty( "appserver.base", appserverBase );

        workingDirectory = new File( appserverBase, "data/working-directory" );

        CacheManager.getInstance().removeCache( "url-failures-cache" );

        HttpUnitOptions.setExceptionsThrownOnErrorStatus( false );

        sr = new ServletRunner( getTestFile( "src/test/resources/WEB-INF/web.xml" ) );
        sr.registerServlet( "/workingcopy/*", MockWorkingCopyServlet.class.getName() );
        sc = sr.newClient();

        new File( workingDirectory, "1/src/main/java/org/apache/continuum" ).mkdirs();
        new File( workingDirectory, "1/src/main/java/org/apache/continuum/App.java" ).createNewFile();
        new File( workingDirectory, "1/src/test" ).mkdirs();
        new File( workingDirectory, "1/pom.xml" ).createNewFile();
        new File( workingDirectory, "1/target" ).mkdir();
        new File( workingDirectory, "1/target/continuum-artifact-1.0.jar" ).createNewFile();
    }

    @Override
    protected void tearDown()
        throws Exception
    {
        if ( sc != null )
        {
            sc.clearContents();
        }

        if ( sr != null )
        {
            sr.shutDown();
        }

        if ( workingDirectory.exists() )
        {
            FileUtils.deleteDirectory( workingDirectory );
        }

        super.tearDown();
    }

    public void testGetWorkingCopy()
        throws Exception
    {
        MockWorkingCopyServlet servlet = (MockWorkingCopyServlet) sc.newInvocation( REQUEST_PATH ).getServlet();
        assertNotNull( servlet );
    }

    public void testBrowse()
        throws Exception
    {
        request = new GetMethodWebRequest( REQUEST_PATH );
        request.setHeaderField( "Authorization", getAuthorizationHeader() );

        response = sc.getResponse( request );
        assertEquals( "Response", HttpServletResponse.SC_OK, response.getResponseCode() );

        String expectedLinks[] = new String[]{"pom.xml", "src/", "target/"};
        assertLinks( expectedLinks, response.getLinks() );
    }

    public void testBrowseSubDirectory()
        throws Exception
    {
        request = new GetMethodWebRequest( REQUEST_PATH + "src/" );
        request.setHeaderField( "Authorization", getAuthorizationHeader() );

        response = sc.getResponse( request );
        assertEquals( "Response", HttpServletResponse.SC_OK, response.getResponseCode() );

        String expectedLinks[] = new String[]{"../", "main/", "test/"};
        assertLinks( expectedLinks, response.getLinks() );
    }

    public void testGetFile()
        throws Exception
    {
        request = new GetMethodWebRequest( REQUEST_PATH + "src/main/java/org/apache/continuum" );
        request.setHeaderField( "Authorization", getAuthorizationHeader() );
        response = sc.getResponse( request );
        assertEquals( "Response", HttpServletResponse.SC_OK, response.getResponseCode() );

        request = new GetMethodWebRequest( REQUEST_PATH + "src/main/java/org/apache/continuum/" );
        request.setHeaderField( "Authorization", getAuthorizationHeader() );
        response = sc.getResponse( request );
        assertEquals( "Response", HttpServletResponse.SC_OK, response.getResponseCode() );

        request = new GetMethodWebRequest( REQUEST_PATH + "src/main/java/org/apache/continuum/App.java" );
        request.setHeaderField( "Authorization", getAuthorizationHeader() );
        response = sc.getResponse( request );
        assertEquals( "Response", HttpServletResponse.SC_OK, response.getResponseCode() );

        request = new GetMethodWebRequest( REQUEST_PATH + "src/main/java/org/apache/continuum/App.java/" );
        request.setHeaderField( "Authorization", getAuthorizationHeader() );
        response = sc.getResponse( request );
        assertEquals( "Response", HttpServletResponse.SC_NOT_FOUND, response.getResponseCode() );

        request = new GetMethodWebRequest( REQUEST_PATH + "pom.xml" );
        request.setHeaderField( "Authorization", getAuthorizationHeader() );
        response = sc.getResponse( request );
        assertEquals( "Response", HttpServletResponse.SC_OK, response.getResponseCode() );

        request = new GetMethodWebRequest( REQUEST_PATH + "pom.xml/" );
        request.setHeaderField( "Authorization", getAuthorizationHeader() );
        response = sc.getResponse( request );
        assertEquals( "Response", HttpServletResponse.SC_NOT_FOUND, response.getResponseCode() );

        request = new GetMethodWebRequest( REQUEST_PATH + "target/continuum-artifact-1.0.jar" );
        request.setHeaderField( "Authorization", getAuthorizationHeader() );
        response = sc.getResponse( request );
        assertEquals( "Response", HttpServletResponse.SC_OK, response.getResponseCode() );

        request = new GetMethodWebRequest( REQUEST_PATH + "target/continuum-artifact-1.0.jar/" );
        request.setHeaderField( "Authorization", getAuthorizationHeader() );
        response = sc.getResponse( request );
        assertEquals( "Response", HttpServletResponse.SC_NOT_FOUND, response.getResponseCode() );
    }

    private void assertLinks( String expectedLinks[], WebLink actualLinks[] )
    {
        assertEquals( "Links.length", expectedLinks.length, actualLinks.length );
        for ( int i = 0; i < actualLinks.length; i++ )
        {
            assertEquals( "Link[" + i + "]", expectedLinks[i], actualLinks[i].getURLString() );
        }
    }

    private String getAuthorizationHeader()
    {
        try
        {
            String encodedPassword = IOUtils.toString( Base64.encodeBase64( ":secret".getBytes() ) );
            return "Basic " + encodedPassword;
        }
        catch ( IOException e )
        {
            return "";
        }
    }
}
