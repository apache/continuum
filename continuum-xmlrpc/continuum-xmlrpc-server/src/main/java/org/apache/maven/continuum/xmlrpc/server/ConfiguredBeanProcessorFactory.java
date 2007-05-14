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
import org.apache.xmlrpc.server.RequestProcessorFactoryFactory;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 * @plexus.component role="org.apache.xmlrpc.server.RequestProcessorFactoryFactory"
 */
public class ConfiguredBeanProcessorFactory
    implements RequestProcessorFactoryFactory, Initializable, Contextualizable
{
    /**
     * @plexus.requirement role="org.apache.maven.continuum.xmlrpc.server.ContinuumXmlRpcComponent"
     */
    private Map xmlrpcComponents;

    /**
     * @plexus.requirement
     */
    private Listener listener;

    private Map componentsMapping = new HashMap();

    PlexusContainer container;

    public void initialize()
        throws InitializationException
    {
        for ( Iterator i = xmlrpcComponents.keySet().iterator(); i.hasNext(); )
        {
            String key = (String) i.next();
            String className = xmlrpcComponents.get( key ).getClass().getName();
            componentsMapping.put( className, key );
        }
    }

    public RequestProcessorFactory getRequestProcessorFactory( final Class cls )
        throws XmlRpcException
    {
        return new RequestProcessorFactory()
        {
            public Object getRequestProcessor( XmlRpcRequest request )
                throws XmlRpcException
            {
                Object obj = ConfiguredBeanProcessorFactory.this.getRequestProcessor( cls );

                if ( obj instanceof ContinuumXmlRpcComponent )
                {
                    ContinuumXmlRpcConfig config = (ContinuumXmlRpcConfig) request.getConfig();
                    ( (ContinuumXmlRpcComponent) obj ).setConfig( config );
                }
                return obj;
            }
        };
    }

    protected Object getRequestProcessor( Class cls )
        throws XmlRpcException
    {
        listener.getLogger().debug( "Load '" + cls.getName() + "' handler." );

        Object obj = null;
        try
        {
            obj = getComponent( cls );
        }
        catch ( ComponentLookupException e )
        {
            listener.getLogger().error( "Can't load component.", e );
        }

        if ( obj == null )
        {
            throw new XmlRpcException( "Handler bean not found for: " + cls );
        }

        return obj;
    }

    private String getComponentKey( Class cls )
    {
        return (String) componentsMapping.get( cls.getName() );
    }

    private Object getComponent( Class cls )
        throws ComponentLookupException
    {
        String key = getComponentKey( cls );
        listener.getLogger().debug( "load component:" );
        return container.lookup( ContinuumXmlRpcComponent.class.getName(), key );
    }

    public void contextualize( Context context )
        throws ContextException
    {
        container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }
}
