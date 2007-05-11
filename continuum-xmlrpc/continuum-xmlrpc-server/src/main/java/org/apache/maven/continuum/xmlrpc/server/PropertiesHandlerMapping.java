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
import org.apache.xmlrpc.server.PropertyHandlerMapping;

import java.util.Iterator;
import java.util.Map;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 * @plexus.component role="org.apache.xmlrpc.server.PropertyHandlerMapping"
 */
public class PropertiesHandlerMapping
    extends PropertyHandlerMapping
{
    /**
     * @plexus.requirement role="org.apache.maven.continuum.xmlrpc.ContinuumXmlRpcComponent"
     */
    private Map xmlrpcComponents;

    /**
     * @plexus.requirement
     */
    private Listener listener;

    public void load()
        throws XmlRpcException
    {
        for ( Iterator i = xmlrpcComponents.keySet().iterator(); i.hasNext(); )
        {
            String key = (String) i.next();
            Class cl = xmlrpcComponents.get( key ).getClass();
            listener.getLogger().debug( key + " => " + cl.getName() );

            registerPublicMethods( key, cl );
        }

        if ( listener.getLogger().isDebugEnabled() )
        {
            String[] methods = getListMethods();
            for ( int i = 0; i < methods.length; i++ )
            {
                listener.getLogger().debug( methods[i] );
            }
        }
    }

}
