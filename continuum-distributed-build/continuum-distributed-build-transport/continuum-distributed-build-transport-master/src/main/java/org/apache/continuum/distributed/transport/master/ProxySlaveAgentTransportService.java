package org.apache.continuum.distributed.transport.master;

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
import java.util.List;
import java.util.Map;

import org.apache.continuum.buildagent.model.Installation;
import org.apache.continuum.distributed.transport.SlaveBuildAgentTransportService;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.util.ClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ProxySlaveAgentTransportService
 */
public class ProxySlaveAgentTransportService
    implements SlaveBuildAgentTransportService
{
    private Logger log = LoggerFactory.getLogger( this.getClass() );
    
    private SlaveBuildAgentTransportService slave;
    
    private XmlRpcClient client;
    
    public ProxySlaveAgentTransportService( URL serviceUrl )
    {
        this( serviceUrl, null, null );
    }

    public ProxySlaveAgentTransportService( URL serviceUrl, String login, String password )
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

        client = new XmlRpcClient();
        client.setConfig( config );
        ClientFactory factory = new ClientFactory( client );
        slave = (SlaveBuildAgentTransportService) factory.newInstance( SlaveBuildAgentTransportService.class );
    }

    public void buildProjects( List<Map> projectsBuildContext )
        throws Exception
    {
        try
        {
            slave.buildProjects( projectsBuildContext );
            log.info( "Building projects." );
        }
        catch ( Exception e )
        {
            log.error( "Failed to build projects.", e );
            throw new Exception( "Failed to build projects.", e );
        }
    }

    public List<Installation> getAvailableInstallations()
        throws Exception
    {
        List<Installation> installations = null;
        
        try
        {
            installations = slave.getAvailableInstallations();
            log.info( "Available installations: " + installations.size() );
        }
        catch ( Exception e )
        {
            log.error( "Failed to get available installations.", e );
            throw new Exception( "Failed to get available installations." , e );
        }
        
        return installations;
    }

    public Map getBuildResult( int projectId )
        throws Exception
    {
        Map buildResult = null;
        
        try
        {
            slave.getBuildResult( projectId );
            log.info( "Build result for project " + projectId + " acquired." );
        }
        catch ( Exception e )
        {
            log.error( "Failed to get build result for project " + projectId, e );
            throw new Exception( "Failed to get build result for project " + projectId, e );
        }
        
        return buildResult;
    }

    public int getProjectCurrentlyBuilding()
        throws Exception
    {
        int projectId = slave.getProjectCurrentlyBuilding();
        
        log.info( "Currently building project " + projectId );
        
        return projectId;
    }

    public boolean isBusy()
        throws Exception
    {
        boolean busy = slave.isBusy();
        
        log.info( "Build agent is " + ( busy ? "" : "not" ) + " busy." );
        
        return busy;
    }

    public boolean ping()
        throws Exception
    {
        boolean result = slave.ping();
        
        log.info( "Ping " + (result ? "ok" : "failed") );
        
        return result;
    }
}
