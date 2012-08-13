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
import org.apache.xmlrpc.XmlRpcHandler;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.RequestProcessorFactoryFactory;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 * @plexus.component role="org.apache.xmlrpc.server.PropertyHandlerMapping"
 */
public class PropertiesHandlerMapping
    extends PropertyHandlerMapping
    implements Contextualizable
{
    private static final Logger log = LoggerFactory.getLogger( PropertiesHandlerMapping.class );

    /**
     * @plexus.requirement role="org.apache.maven.continuum.xmlrpc.server.ContinuumXmlRpcComponent"
     */
    private Map<String, Object> xmlrpcComponents;

    private PlexusContainer container;

    public void load()
        throws XmlRpcException
    {
        for ( String key : xmlrpcComponents.keySet() )
        {
            Class cl = xmlrpcComponents.get( key ).getClass();
            if ( log.isDebugEnabled() )
            {
                log.debug( key + " => " + cl.getName() );
            }

            registerPublicMethods( key, cl );
        }

        if ( log.isDebugEnabled() )
        {
            String[] methods = getListMethods();
            for ( String method : methods )
            {
                log.debug( method );
            }
        }
    }

    protected XmlRpcHandler newXmlRpcHandler( final Class pClass, final Method[] pMethods )
        throws XmlRpcException
    {
        String[][] sig = getSignature( pMethods );
        String help = getMethodHelp( pClass, pMethods );
        RequestProcessorFactoryFactory.RequestProcessorFactory factory =
            getRequestProcessorFactoryFactory().getRequestProcessorFactory( pClass );
        return new ContinuumXmlRpcMetaDataHandler( this, getTypeConverterFactory(), pClass, factory, pMethods, sig,
                                                   help, container );
    }

    public void contextualize( Context context )
        throws ContextException
    {
        container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }
}
