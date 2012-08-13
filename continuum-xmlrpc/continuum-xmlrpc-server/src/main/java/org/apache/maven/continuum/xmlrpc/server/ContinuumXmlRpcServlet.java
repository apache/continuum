package org.apache.maven.continuum.xmlrpc.server;

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

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.server.AbstractReflectiveHandlerMapping;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.RequestProcessorFactoryFactory;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.redback.authentication.AuthenticationException;
import org.codehaus.plexus.redback.authentication.PasswordBasedAuthenticationDataSource;
import org.codehaus.plexus.redback.policy.PolicyViolationException;
import org.codehaus.plexus.redback.system.DefaultSecuritySession;
import org.codehaus.plexus.redback.system.SecuritySystem;
import org.codehaus.plexus.redback.users.UserNotFoundException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class ContinuumXmlRpcServlet
    extends HttpServlet
{
    private ContinuumXmlRpcServletServer server;

    private SecuritySystem securitySystem;

    public String getServletInfo()
    {
        return "Continuum XMLRPC Servlet";
    }

    public void destroy()
    {
        if ( server != null )
        {
            try
            {
                getPlexusContainer().release( server );
            }
            catch ( ServletException e )
            {
                log( "Unable to release XmlRpcServletServer.", e );
            }
            catch ( ComponentLifecycleException e )
            {
                log( "Unable to release XmlRpcServletServer.", e );
            }
        }
    }

    public void init( ServletConfig servletConfig )
        throws ServletException
    {
        super.init( servletConfig );

        ensureContainerSet( servletConfig );

        initServer();
    }

    public void initServer()
        throws ServletException
    {
        server = new ContinuumXmlRpcServletServer();

        try
        {
            securitySystem = (SecuritySystem) getPlexusContainer().lookup( SecuritySystem.ROLE );
        }
        catch ( ComponentLookupException e )
        {
            throw new ServletException( "Can't init the xml rpc server, unable to obtain security system", e );
        }

        try
        {
            XmlRpcServerConfigImpl cfg = (XmlRpcServerConfigImpl) server.getConfig();
            cfg.setEnabledForExtensions( true );
            PropertiesHandlerMapping mapping = (PropertiesHandlerMapping) lookup(
                PropertyHandlerMapping.class.getName() );
            mapping.setRequestProcessorFactoryFactory( (RequestProcessorFactoryFactory) lookup(
                RequestProcessorFactoryFactory.class.getName() ) );
            mapping.load();
            mapping.setAuthenticationHandler( getAuthenticationHandler() );
            server.setHandlerMapping( mapping );
        }
        catch ( XmlRpcException e )
        {
            throw new ServletException( "Can't init the xml rpc server", e );
        }
    }

    private AbstractReflectiveHandlerMapping.AuthenticationHandler getAuthenticationHandler()
    {
        return new AbstractReflectiveHandlerMapping.AuthenticationHandler()
        {
            public boolean isAuthorized( XmlRpcRequest pRequest )
            {
                if ( pRequest.getConfig() instanceof ContinuumXmlRpcConfig )
                {
                    ContinuumXmlRpcConfig config = (ContinuumXmlRpcConfig) pRequest.getConfig();

                    try
                    {
                        // if username is null, then treat this as a guest user with an empty security session
                        if ( config.getBasicUserName() == null )
                        {
                            config.setSecuritySession( new DefaultSecuritySession() );

                            return true;
                        }
                        else
                        {
                            // otherwise treat this as an authn required session, and if the credentials are invalid
                            // do not default to guest privileges
                            PasswordBasedAuthenticationDataSource authdatasource =
                                new PasswordBasedAuthenticationDataSource();
                            authdatasource.setPrincipal( config.getBasicUserName() );
                            authdatasource.setPassword( config.getBasicPassword() );

                            config.setSecuritySession( securitySystem.authenticate( authdatasource ) );

                            return config.getSecuritySession().isAuthenticated();
                        }
                    }
                    catch ( AuthenticationException e )
                    {
                        e.printStackTrace();
                        return false;
                    }
                    catch ( PolicyViolationException e )
                    {
                        e.printStackTrace();
                        return false;
                    }
                    catch ( UserNotFoundException e )
                    {
                        e.printStackTrace();
                        return false;
                    }
                }
                else
                {
                    System.out.println( "unknown xml rpc configiration object found..." );
                    return false;
                }
            }
        };
    }

    public void doPost( HttpServletRequest pRequest, HttpServletResponse pResponse )
        throws IOException, ServletException
    {
        server.execute( pRequest, pResponse );
    }

    private void ensureContainerSet( ServletConfig sc )
        throws ServletException
    {
        // TODO: unify this code with the lifecycle listener and application server

        ServletContext context = sc.getServletContext();

        // Container not found.

        if ( context.getAttribute( PlexusConstants.PLEXUS_KEY ) != null )
        {
            context.log( "Plexus container already in context." );

            return;
        }

        // Create container.

        Map keys = new HashMap();

        PlexusContainer pc;
        try
        {
            pc = new DefaultPlexusContainer( "default", keys, "META-INF/plexus/application.xml", new ClassWorld(
                "plexus.core", getClass().getClassLoader() ) );

            context.setAttribute( PlexusConstants.PLEXUS_KEY, pc );
        }
        catch ( PlexusContainerException e )
        {
            throw new ServletException( "Unable to initialize Plexus Container.", e );
        }
    }

    private PlexusContainer getPlexusContainer()
        throws ServletException
    {
        PlexusContainer container = (PlexusContainer) getServletContext().getAttribute( PlexusConstants.PLEXUS_KEY );
        if ( container == null )
        {
            throw new ServletException( "Unable to find plexus container." );
        }
        return container;
    }

    public Object lookup( String role )
        throws ServletException
    {
        try
        {
            return getPlexusContainer().lookup( role );
        }
        catch ( ComponentLookupException e )
        {
            throw new ServletException( "Unable to lookup role [" + role + "]", e );
        }
    }

    public Object lookup( String role, String hint )
        throws ServletException
    {
        try
        {
            return getPlexusContainer().lookup( role, hint );
        }
        catch ( ComponentLookupException e )
        {
            throw new ServletException( "Unable to lookup role [" + role + "] hint [" + hint + "]", e );
        }
    }
}
