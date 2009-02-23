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
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.xmlrpc.AuthenticationInfo;
import com.atlassian.xmlrpc.Binder;
import com.atlassian.xmlrpc.BindingException;
import com.atlassian.xmlrpc.DefaultBinder;

/**
 * SlaveBuildAgentTransportClient
 */
public class SlaveBuildAgentTransportClient
    implements SlaveBuildAgentTransportService
{
    private Logger log = LoggerFactory.getLogger( this.getClass() );
    
    private SlaveBuildAgentTransportService slave;
    
    public SlaveBuildAgentTransportClient( URL serviceUrl )
        throws Exception
    {
        this( serviceUrl, null, null );
    }

    public SlaveBuildAgentTransportClient( URL serviceUrl, String login, String password )
        throws Exception
    {
        Binder binder = new DefaultBinder();
        AuthenticationInfo authnInfo = new AuthenticationInfo( login, password );
        
        try
        {
            slave = binder.bind( SlaveBuildAgentTransportService.class, serviceUrl, authnInfo );
        }
        catch ( BindingException e )
        {
            log.error( "Can't bind service interface " + SlaveBuildAgentTransportService.class.getName() + " to " + serviceUrl.toExternalForm() + " using " + authnInfo.getUsername() + ", " + authnInfo.getPassword(), e );
            throw new Exception( "Can't bind service interface " + SlaveBuildAgentTransportService.class.getName() + " to " + serviceUrl.toExternalForm() + " using " + authnInfo.getUsername() + ", " + authnInfo.getPassword(), e);
        }
    }

    public Boolean buildProjects( List<Map> projectsBuildContext )
        throws Exception
    {
        Boolean result = null;
        
        try
        {
            result = slave.buildProjects( projectsBuildContext );
            log.info( "Building projects." );
        }
        catch ( Exception e )
        {
            log.error( "Failed to build projects.", e );
            throw new Exception( "Failed to build projects.", e );
        }
        
        return result;
    }

    public List<Map> getAvailableInstallations()
        throws Exception
    {
        List<Map> installations = null;
        
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
            buildResult = slave.getBuildResult( projectId );
            log.info( "Build result for project " + projectId + " acquired." );
        }
        catch ( Exception e )
        {
            log.error( "Failed to get build result for project " + projectId, e );
            throw new Exception( "Failed to get build result for project " + projectId, e );
        }
        
        return buildResult;
    }

    public Integer getProjectCurrentlyBuilding()
        throws Exception
    {
        Integer projectId = null;
        
        try
        {
            projectId = slave.getProjectCurrentlyBuilding();
            log.info( "Currently building project " + projectId );
        }
        catch ( Exception e )
        {
            log.error( "Failed to get the currently building project", e );
            throw new Exception( "Failed to get the currently building project", e );
        }
        
        return projectId;
    }

    public Boolean ping()
        throws Exception
    {
        Boolean result = null;
        
        try
        {
            result = slave.ping();
            log.info( "Ping " + ( result.booleanValue() ? "ok" : "failed" ) );
        }
        catch ( Exception e )
        {
            log.info( "Ping error" );
            throw new Exception( "Ping error", e );
        }
        
        return result;
    }

    public Boolean cancelBuild()
        throws Exception
    {
        Boolean result = null;

        try
        {
            result = slave.cancelBuild();
            log.info( "Cancelled build" );
        }
        catch ( Exception e )
        {
            log.error( "Error cancelling build" );
            throw new Exception( "Error cancelling build", e );
        }

        return result;
    }

    public String generateWorkingCopyContent( int projectId, String directory, String baseUrl, String imagesBaseUrl )
        throws Exception
    {
        String result = null;

        try
        {
            result = slave.generateWorkingCopyContent( projectId, directory, baseUrl, imagesBaseUrl );
            log.info( "Generated working copy content" );
        }
        catch ( Exception e )
        {
            log.error( "Error generating working copy content", e );
            throw new Exception( "Error generating working copy content", e );
        }

        return result;
    }

    public String getProjectFileContent( int projectId, String directory, String filename )
        throws Exception
    {
        String result = null;

        try
        {
            result = slave.getProjectFileContent( projectId, directory, filename );
            log.info( "Retrived project file content" );
        }
        catch ( Exception e )
        {
            log.error( "Error retrieving project file content", e );
            throw new Exception( "Error retrieving project file content", e );
        }

        return result;
    }
}
