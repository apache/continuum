package org.apache.continuum.distributed.transport.slave;

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

import java.net.URL;
import java.util.Map;

import org.apache.continuum.distributed.transport.MasterBuildAgentTransportService;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.util.ClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ProxyMasterAgentTransportService
 */
public class ProxyMasterAgentTransportService
    implements MasterBuildAgentTransportService
{
    private Logger log = LoggerFactory.getLogger( this.getClass() );
    
    MasterBuildAgentTransportService master;
    
    public ProxyMasterAgentTransportService( URL serviceUrl )
    {
        this( serviceUrl, null, null );
    }

    public ProxyMasterAgentTransportService( URL serviceUrl, String login, String password )
    {
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl()
        {
            public boolean isEnabledForExtensions()
            {
                return true;
            }
        };

        if ( login != null && !"".equals( login ) )
        {
            config.setBasicUserName( login );
            config.setBasicPassword( password );
        }
        config.setServerURL( serviceUrl );

        XmlRpcClient client = new XmlRpcClient();
        client.setConfig( config );
        ClientFactory factory = new ClientFactory( client );
        master = (MasterBuildAgentTransportService) factory.newInstance( MasterBuildAgentTransportService.class );
    }

    public void returnBuildResult( Map buildResult )
        throws Exception
    {
        master.returnBuildResult( buildResult );
        log.info( "Returning the build result." );
    }

    public void returnScmResult( Map scmResult )
        throws Exception
    {
        master.returnScmResult( scmResult );
        log.info( "Returning the scm result." );
    }
    
    public boolean ping()
        throws Exception
    {
        boolean result = master.ping();
        
        log.info( "Ping " + (result ? "ok" : "failed") );
        
        return result;
    }
}
