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

import com.atlassian.xmlrpc.AuthenticationInfo;
import com.atlassian.xmlrpc.Binder;
import com.atlassian.xmlrpc.BindingException;
import com.atlassian.xmlrpc.DefaultBinder;

import java.net.URL;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.continuum.distributed.commons.utils.ContinuumDistributedUtil;

/**
 * MasterBuildAgentTransportClient
 */
public class MasterBuildAgentTransportClient
    implements MasterBuildAgentTransportService
{
    private static final Logger log = LoggerFactory.getLogger( MasterBuildAgentTransportClient.class );

    MasterBuildAgentTransportService master;

    public MasterBuildAgentTransportClient( URL serviceUrl )
        throws Exception
    {
        this( serviceUrl, null, null );
    }

    public MasterBuildAgentTransportClient( URL serviceUrl, String login, String password )
        throws Exception
    {
        Binder binder = new DefaultBinder();
        AuthenticationInfo authnInfo = new AuthenticationInfo( login, password );

        try
        {
            master = binder.bind( MasterBuildAgentTransportService.class, serviceUrl, authnInfo );
        }
        catch ( BindingException e )
        {
            log.error( "Can't bind service interface " + MasterBuildAgentTransportService.class.getName() + " to " +
                serviceUrl.toExternalForm() + " using " + authnInfo.getUsername() + ", " + authnInfo.getPassword(), e );
            throw new Exception(
                "Can't bind service interface " + MasterBuildAgentTransportService.class.getName() + " to " +
                    serviceUrl.toExternalForm() + " using " + authnInfo.getUsername() + ", " + authnInfo.getPassword(),
                e );
        }
    }

    public Boolean returnBuildResult( Map<String, Object> buildResult )
        throws Exception
    {
        Boolean result;
        String projectInfo = ContinuumDistributedUtil.getProjectNameAndId( buildResult );

        try
        {
            result = master.returnBuildResult( buildResult );
            log.info( "Returning the build result for project " + projectInfo + "." );
        }
        catch ( Exception e )
        {
            log.error( "Failed to return the build result for project " + projectInfo + ".", e );
            throw new Exception( "Failed to return the build result for project " + projectInfo + ".", e );
        }

        return result;
    }

    public Boolean ping()
        throws Exception
    {
        Boolean result;

        try
        {
            result = master.ping();
            log.info( "Ping " + ( result ? "ok" : "failed" ) );
        }
        catch ( Exception e )
        {
            log.info( "Ping error" );
            throw new Exception( "Ping error", e );
        }

        return result;
    }

    public Boolean prepareBuildFinished( Map<String, Object> prepareBuildResult )
        throws Exception
    {
        Boolean result;
        String projectInfo = ContinuumDistributedUtil.getProjectNameAndId( prepareBuildResult );

        try
        {
            result = master.prepareBuildFinished( prepareBuildResult );
            log.info( "Prepare build finished for project " + projectInfo + "." );
        }
        catch ( Exception e )
        {
            log.error( "Failed to finish prepare build for project " + projectInfo + "." );
            throw new Exception( "Failed to finish prepare build for project " + projectInfo + ".", e );
        }

        return result;
    }

    public Boolean startProjectBuild( Integer projectId )
        throws Exception
    {
        Boolean result;

        try
        {
            result = master.startProjectBuild( projectId );
            log.info( "Return project currently building, projectId=" + projectId );
        }
        catch ( Exception e )
        {
            log.error( "Failed to return project currently building, projectId=" + projectId, e );
            throw new Exception( "Failed to return project currently building, projectId=" + projectId, e );
        }

        return result;
    }

    public Boolean startPrepareBuild( Map<String, Object> prepareBuildResult )
        throws Exception
    {
        Boolean result;
        String projectInfo = ContinuumDistributedUtil.getProjectNameAndId( prepareBuildResult );

        try
        {
            result = master.startPrepareBuild( prepareBuildResult );
            log.info( "Start prepare build for project " + projectInfo );
        }
        catch ( Exception e )
        {
            log.error( "Failed to start prepare build for project " + projectInfo, e );
            throw new Exception( "Failed to start prepare build for project " + projectInfo, e );
        }

        return result;
    }

    public Map<String, String> getEnvironments( Integer buildDefinitionId, String installationType )
        throws Exception
    {
        Map<String, String> result;
        try
        {
            result = master.getEnvironments( buildDefinitionId, installationType );
            log.info( "Retrieved environments. buildDefinitionId=" + buildDefinitionId + ", installationType=" + installationType );
        }
        catch ( Exception e )
        {
            log.error( "Failed to retrieve environments. buildDefinitionId=" + buildDefinitionId +
                       ", installationType=" + installationType, e );
            throw new Exception( "Failed to retrieve environments. buildDefinitionId=" +
                                  buildDefinitionId + ", installationType=" + installationType, e );
        }

        return result;
    }

    public Boolean updateProject( Map<String, Object> project )
        throws Exception
    {
        Boolean result;
        String projectInfo = ContinuumDistributedUtil.getProjectNameAndId( project );

        try
        {
            result = master.updateProject( project );
            log.info( "Updating project " + projectInfo );
        }
        catch ( Exception e )
        {
            log.error( "Failed to update project " + projectInfo, e );
            throw new Exception( "Failed to update project " + projectInfo, e );
        }

        return result;
    }

    public Boolean shouldBuild( Map<String, Object> context )
        throws Exception
    {
        Boolean result;
        String projectInfo = ContinuumDistributedUtil.getProjectNameAndId( context );

        try
        {
            result = master.shouldBuild( context );
            log.info( "Checking if project " + projectInfo + " should build" );
        }
        catch ( Exception e )
        {
            log.error( "Failed to determine if project " + projectInfo + " should build", e );
            throw new Exception( "Failed to determine if project " + projectInfo + " should build", e );
        }

        return result;
    }
}
