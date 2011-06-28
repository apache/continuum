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

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.continuum.buildagent.configuration.BuildAgentConfigurationService;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavLocatorFactory;
import org.apache.jackrabbit.webdav.DavMethods;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceFactory;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.DavSessionProvider;
import org.apache.jackrabbit.webdav.WebdavRequest;
import org.apache.jackrabbit.webdav.WebdavRequestImpl;
import org.apache.jackrabbit.webdav.WebdavResponse;
import org.apache.jackrabbit.webdav.WebdavResponseImpl;
import org.apache.jackrabbit.webdav.server.AbstractWebdavServlet;
import org.codehaus.plexus.spring.PlexusToSpringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.io.IOException;

public class WorkingCopyServlet
    extends AbstractWebdavServlet
{
    private Logger log = LoggerFactory.getLogger( this.getClass() );

    private DavLocatorFactory locatorFactory;

    private DavResourceFactory resourceFactory;

    protected DavSessionProvider sessionProvider;

    @Override
    public void init( ServletConfig servletConfig )
        throws ServletException
    {
        super.init( servletConfig );
        initServers( servletConfig );
    }

    /**
     * Service the given request. This method has been overridden and copy/pasted to allow better exception handling and
     * to support different realms
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws java.io.IOException
     */
    @Override
    protected void service( HttpServletRequest request, HttpServletResponse response )
        throws ServletException, IOException
    {
        WebdavRequest webdavRequest = new WebdavRequestImpl( request, getLocatorFactory() );

        // DeltaV requires 'Cache-Control' header for all methods except 'VERSION-CONTROL' and 'REPORT'.
        int methodCode = DavMethods.getMethodCode( request.getMethod() );
        boolean noCache =
            DavMethods.isDeltaVMethod( webdavRequest )
                && !( DavMethods.DAV_VERSION_CONTROL == methodCode || DavMethods.DAV_REPORT == methodCode );
        WebdavResponse webdavResponse = new WebdavResponseImpl( response, noCache );
        DavResource resource = null;

        try
        {
            // make sure there is a authenticated user
            if ( !( getDavSessionProvider() ).attachSession( webdavRequest ) )
            {
                return;
            }

            // check matching if=header for lock-token relevant operations
            resource =
                getResourceFactory().createResource( webdavRequest.getRequestLocator(), webdavRequest, webdavResponse );

            if ( !isPreconditionValid( webdavRequest, resource ) )
            {
                webdavResponse.sendError( DavServletResponse.SC_PRECONDITION_FAILED );
                return;
            }
            if ( !execute( webdavRequest, webdavResponse, methodCode, resource ) )
            {
                super.service( request, response );
            }
        }
        catch ( DavException e )
        {
            if ( e.getErrorCode() == HttpServletResponse.SC_UNAUTHORIZED )
            {
                final String msg = "Unauthorized error";
                log.error( msg );
                webdavResponse.sendError( e.getErrorCode(), msg );
            }
            else if ( e.getCause() != null )
            {
                webdavResponse.sendError( e.getErrorCode(), e.getCause().getMessage() );
            }
            else
            {
                webdavResponse.sendError( e.getErrorCode(), e.getMessage() );
            }
        }
        finally
        {
            getDavSessionProvider().releaseSession( webdavRequest );
        }
    }

    public synchronized void initServers( ServletConfig servletConfig )
    {
        WebApplicationContext wac =
            WebApplicationContextUtils.getRequiredWebApplicationContext( servletConfig.getServletContext() );

        resourceFactory =
            (DavResourceFactory) wac.getBean( PlexusToSpringUtils.
                                              buildSpringId( ContinuumBuildAgentDavResourceFactory.class ) );

        BuildAgentConfigurationService buildAgentConfigurationService = (BuildAgentConfigurationService)
            wac.getBean( PlexusToSpringUtils.buildSpringId( BuildAgentConfigurationService.class ) );

        locatorFactory = new ContinuumBuildAgentDavLocatorFactory();
        sessionProvider = new ContinuumBuildAgentDavSessionProvider( buildAgentConfigurationService);
    }

    @Override
    public String getAuthenticateHeaderValue()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public DavSessionProvider getDavSessionProvider()
    {
        return sessionProvider;
    }

    @Override
    public DavLocatorFactory getLocatorFactory()
    {
        return locatorFactory;
    }

    @Override
    public DavResourceFactory getResourceFactory()
    {
        return resourceFactory;
    }

    @Override
    protected boolean isPreconditionValid( WebdavRequest request, DavResource resource )
    {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public void setDavSessionProvider( final DavSessionProvider sessionProvider )
    {
        this.sessionProvider = sessionProvider;
    }

    @Override
    public void setLocatorFactory( final DavLocatorFactory locatorFactory )
    {
        this.locatorFactory = locatorFactory;
    }

    @Override
    public void setResourceFactory( DavResourceFactory resourceFactory )
    {
        this.resourceFactory = resourceFactory;
    }

    @Override
    public void destroy()
    {
        resourceFactory = null;
        locatorFactory = null;
        sessionProvider = null;

        WebApplicationContext wac = WebApplicationContextUtils.getRequiredWebApplicationContext( getServletContext() );

        if ( wac instanceof ConfigurableApplicationContext )
        {
            ( (ConfigurableApplicationContext) wac ).close();
        }
        super.destroy();
    }
}
