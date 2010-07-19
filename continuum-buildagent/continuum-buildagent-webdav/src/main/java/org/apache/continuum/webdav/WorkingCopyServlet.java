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

import org.apache.jackrabbit.webdav.DavLocatorFactory;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceFactory;
import org.apache.jackrabbit.webdav.DavSessionProvider;
import org.apache.jackrabbit.webdav.WebdavRequest;
import org.apache.jackrabbit.webdav.server.AbstractWebdavServlet;
import org.codehaus.plexus.spring.PlexusToSpringUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class WorkingCopyServlet
    extends AbstractWebdavServlet
{
    private DavLocatorFactory locatorFactory;

    private DavResourceFactory resourceFactory;

    private DavSessionProvider sessionProvider;

    @Override
    public void init( ServletConfig servletConfig )
        throws ServletException
    {
        super.init( servletConfig );
        initServers( servletConfig );
    }

    public synchronized void initServers( ServletConfig servletConfig )
    {
        WebApplicationContext wac =
            WebApplicationContextUtils.getRequiredWebApplicationContext( servletConfig.getServletContext() );

        resourceFactory =
            (DavResourceFactory) wac.getBean( PlexusToSpringUtils.
                                              buildSpringId( ContinuumBuildAgentDavResourceFactory.class ) );
        locatorFactory = new ContinuumBuildAgentDavLocatorFactory();
        sessionProvider = new ContinuumBuildAgentDavSessionProvider();
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
