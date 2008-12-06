package org.apache.continuum.xmlrpc.distributed.client;

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
import org.apache.continuum.xmlrpc.distributed.ContinuumDistributedBuildService;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.util.ClientFactory;

/**
 * ContinuumDistributedBuildClient
 */
public class ContinuumDistributedBuildClient
    implements ContinuumDistributedBuildService
{
    ContinuumDistributedBuildService continuumDistributedBuildService;
    
    public ContinuumDistributedBuildClient( URL serviceUrl )
    {
        this( serviceUrl, null, null );
    }

    public ContinuumDistributedBuildClient( URL serviceUrl, String login, String password )
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
        continuumDistributedBuildService = (ContinuumDistributedBuildService) factory.newInstance( ContinuumDistributedBuildService.class );
    }

    public void buildProjects( List<Map> projectsBuildContext )
        throws Exception
    {
        try
        {
            continuumDistributedBuildService.buildProjects( projectsBuildContext );
            //getLogger().info( "Building projects." );
        }
        catch ( Exception e )
        {
            //getLogger().error( "Failed to build projects.", e );
            throw new Exception( "Failed to build projects.", e );
        }
    }

    public List<Installation> getAvailableInstallations()
        throws Exception
    {
        List installations = null;
        
        try
        {
            continuumDistributedBuildService.getAvailableInstallations();
            //getLogger().info( "Available installations: " + installations.size() );
        }
        catch ( Exception e )
        {
            //getLogger().error( "Failed to get available installations.", e );
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
            continuumDistributedBuildService.getBuildResult( projectId );
            //getLogger().info( "Build result for project " + projectId + " acquired." );
        }
        catch ( Exception e )
        {
            //getLogger().error( "Failed to get build result for project " + projectId, e );
            throw new Exception( "Failed to get build result for project " + projectId, e );
        }
        
        return buildResult;
    }

    public int getProjectCurrentlyBuilding()
        throws Exception
    {
        int projectId = continuumDistributedBuildService.getProjectCurrentlyBuilding();
        
        //getLogger().info( "Currently building project " + projectId );
        
        return projectId;
    }

    public int isBusy()
        throws Exception
    {
        int busy = continuumDistributedBuildService.isBusy();
        
        //getLogger().info( "Build agent is " + ( busy == 0 ? "" : "not" ) + " busy." );
        
        return busy;
    }

    public int ping()
        throws Exception
    {
        //getLogger().info( "Ping" );
        
        return continuumDistributedBuildService.ping();
    }
}
