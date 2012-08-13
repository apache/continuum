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

import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;
import org.apache.continuum.buildagent.configuration.BuildAgentConfigurationService;
import org.apache.jackrabbit.webdav.DavSessionProvider;
import org.apache.jackrabbit.webdav.WebdavRequest;
import org.apache.jackrabbit.webdav.WebdavRequestImpl;
import org.codehaus.plexus.util.Base64;
import org.easymock.MockControl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class ContinuumBuildAgentDavSessionProviderTest
    extends TestCase
{
    private DavSessionProvider sessionProvider;

    private WebdavRequest request;

    private MockControl buildAgentConfigurationServiceControl;

    private BuildAgentConfigurationService buildAgentConfigurationService;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        buildAgentConfigurationServiceControl = MockControl.
            createControl( BuildAgentConfigurationService.class );
        buildAgentConfigurationService =
            (BuildAgentConfigurationService) buildAgentConfigurationServiceControl.getMock();

        sessionProvider = new ContinuumBuildAgentDavSessionProvider( buildAgentConfigurationService );
        request = new WebdavRequestImpl( new HttpServletRequestMock(), null );

        buildAgentConfigurationServiceControl.expectAndReturn( buildAgentConfigurationService.getSharedSecretPassword(),
                                                               "secret", 2 );

        buildAgentConfigurationServiceControl.replay();

    }

    public void testAttachSession()
        throws Exception
    {
        assertNull( request.getDavSession() );

        sessionProvider.attachSession( request );

        buildAgentConfigurationServiceControl.verify();

        assertNotNull( request.getDavSession() );
    }

    public void testReleaseSession()
        throws Exception
    {
        assertNull( request.getDavSession() );

        sessionProvider.attachSession( request );

        buildAgentConfigurationServiceControl.verify();

        assertNotNull( request.getDavSession() );

        sessionProvider.releaseSession( request );
        assertNull( request.getDavSession() );
    }

    @SuppressWarnings( "unchecked" )
    private class HttpServletRequestMock
        implements HttpServletRequest
    {
        public Object getAttribute( String arg0 )
        {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public Enumeration getAttributeNames()
        {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public String getCharacterEncoding()
        {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public int getContentLength()
        {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public String getContentType()
        {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public ServletInputStream getInputStream()
            throws IOException
        {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public String getLocalAddr()
        {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public String getLocalName()
        {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public int getLocalPort()
        {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public Locale getLocale()
        {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public Enumeration getLocales()
        {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public String getParameter( String arg0 )
        {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public Map getParameterMap()
        {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public Enumeration getParameterNames()
        {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public String[] getParameterValues( String arg0 )
        {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public String getProtocol()
        {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public BufferedReader getReader()
            throws IOException
        {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public String getRealPath( String arg0 )
        {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public String getRemoteAddr()
        {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public String getRemoteHost()
        {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public int getRemotePort()
        {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public RequestDispatcher getRequestDispatcher( String arg0 )
        {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public String getScheme()
        {
            return "";
        }

        public String getServerName()
        {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public int getServerPort()
        {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public boolean isSecure()
        {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public void removeAttribute( String arg0 )
        {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public void setAttribute( String arg0, Object arg1 )
        {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public void setCharacterEncoding( String arg0 )
            throws UnsupportedEncodingException
        {
            throw new UnsupportedOperationException( "Not supported yet." );
        }


        public String getAuthType()
        {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public String getContextPath()
        {
            return "/";
        }

        public Cookie[] getCookies()
        {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public long getDateHeader( String arg0 )
        {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public String getHeader( String arg0 )
        {
            if ( arg0 != null && arg0.equalsIgnoreCase( "authorization" ) )
            {
                return getAuthorizationHeader();
            }

            return "";
        }

        public Enumeration getHeaderNames()
        {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public Enumeration getHeaders( String arg0 )
        {
            Hashtable<String, String> hashTable = new Hashtable<String, String>();
            hashTable.put( "Authorization", getAuthorizationHeader() );

            return hashTable.elements();
        }

        public int getIntHeader( String arg0 )
        {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public String getMethod()
        {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public String getPathInfo()
        {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public String getPathTranslated()
        {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public String getQueryString()
        {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public String getRemoteUser()
        {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public String getRequestURI()
        {
            return "/";
        }

        public StringBuffer getRequestURL()
        {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public String getRequestedSessionId()
        {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public String getServletPath()
        {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public HttpSession getSession( boolean arg0 )
        {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public HttpSession getSession()
        {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public Principal getUserPrincipal()
        {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public boolean isRequestedSessionIdFromCookie()
        {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public boolean isRequestedSessionIdFromURL()
        {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public boolean isRequestedSessionIdFromUrl()
        {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public boolean isRequestedSessionIdValid()
        {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public boolean isUserInRole( String arg0 )
        {
            throw new UnsupportedOperationException( "Not supported yet." );
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
}
