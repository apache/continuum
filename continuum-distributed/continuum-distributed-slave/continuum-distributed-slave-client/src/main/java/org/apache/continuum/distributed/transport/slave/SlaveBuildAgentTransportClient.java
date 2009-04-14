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

import com.atlassian.xmlrpc.AuthenticationInfo;
import com.atlassian.xmlrpc.Binder;
import com.atlassian.xmlrpc.BindingException;
import com.atlassian.xmlrpc.DefaultBinder;

import java.net.URL;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SlaveBuildAgentTransportClient
 */
public class SlaveBuildAgentTransportClient
    implements SlaveBuildAgentTransportService
{
    private static final Logger log = LoggerFactory.getLogger( SlaveBuildAgentTransportClient.class );

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
            log.error( "Can't bind service interface " + SlaveBuildAgentTransportService.class.getName() + " to " +
                serviceUrl.toExternalForm() + " using " + authnInfo.getUsername() + ", " + authnInfo.getPassword(), e );
            throw new Exception(
                "Can't bind service interface " + SlaveBuildAgentTransportService.class.getName() + " to " +
                    serviceUrl.toExternalForm() + " using " + authnInfo.getUsername() + ", " + authnInfo.getPassword(),
                e );
        }
    }

    public Boolean buildProjects( List<Map<String, Object>> projectsBuildContext )
        throws Exception
    {
        Boolean result;

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

    public List<Map<String, String>> getAvailableInstallations()
        throws Exception
    {
        List<Map<String, String>> installations;

        try
        {
            installations = slave.getAvailableInstallations();
            log.info( "Available installations: " + installations.size() );
        }
        catch ( Exception e )
        {
            log.error( "Failed to get available installations.", e );
            throw new Exception( "Failed to get available installations.", e );
        }

        return installations;
    }

    public Map getBuildResult( int projectId )
        throws Exception
    {
        Map buildResult;

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
        Integer projectId;

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
        Boolean result;

        try
        {
            result = slave.ping();
            log.info( "Ping " + ( result ? "ok" : "failed" ) );
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
        Boolean result;

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
        String result;

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
        String result;

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

    public Map getReleasePluginParameters( int projectId, String pomFilename )
        throws Exception
    {
        Map result;

        try
        {
            result = slave.getReleasePluginParameters( projectId, pomFilename );
            log.info( "Retrieving release plugin parameters" );
        }
        catch ( Exception e )
        {
            log.error( "Error retrieving release plugin parameters", e );
            throw new Exception( "Error retrieving release plugin parameters", e );
        }

        return result;
    }

    public List<Map<String, String>> processProject( int projectId, String pomFilename, boolean autoVersionSubmodules )
        throws Exception
    {
        List<Map<String, String>> result;

        try
        {
            result = slave.processProject( projectId, pomFilename, autoVersionSubmodules );
            log.info( "Processing project" );
        }
        catch ( Exception e )
        {
            log.error( "Error processing project", e );
            throw new Exception( "Error processing project", e );
        }

        return result;
    }

    public String releasePrepare( Map project, Map properties, Map releaseVersion, Map developmentVersion,
                                  Map environments )
        throws Exception
    {
        String releaseId;

        try
        {
            releaseId = slave.releasePrepare( project, properties, releaseVersion, developmentVersion, environments );
            log.info( "Preparing release" );
        }
        catch ( Exception e )
        {
            log.error( "Error while preparing release", e );
            throw new Exception( "Error while preparing release", e );
        }

        return releaseId;
    }

    public Map getReleaseResult( String releaseId )
        throws Exception
    {
        Map result;

        try
        {
            result = slave.getReleaseResult( releaseId );
            log.info( "Retrieving release result for " + releaseId );
        }
        catch ( Exception e )
        {
            log.error( "Error retrieving release result for " + releaseId, e );
            throw new Exception( "Error retrieving release result for " + releaseId, e );
        }

        return result;
    }

    public Map getListener( String releaseId )
        throws Exception
    {
        Map result;

        try
        {
            result = slave.getListener( releaseId );
            log.info( "Retrieving listener for " + releaseId );
        }
        catch ( Exception e )
        {
            log.error( "Error retrieving listener for " + releaseId, e );
            throw new Exception( "Error retrieving listener for " + releaseId, e );
        }

        return result;
    }

    public Boolean removeListener( String releaseId )
        throws Exception
    {
        Boolean result;

        try
        {
            slave.removeListener( releaseId );
            result = Boolean.FALSE;
            log.info( "Removing listener for " + releaseId );
        }
        catch ( Exception e )
        {
            log.error( "Error removing listener for " + releaseId, e );
            throw new Exception( "Error removing listener for " + releaseId, e );
        }

        return result;
    }

    public String getPreparedReleaseName( String releaseId )
        throws Exception
    {
        String result;

        try
        {
            result = slave.getPreparedReleaseName( releaseId );
            log.info( "Retrieving prepared release name for " + releaseId );
        }
        catch ( Exception e )
        {
            log.error( "Error while retrieving prepared release name for " + releaseId );
            throw new Exception( "Error while retrieving prepared release name for " + releaseId );
        }

        return result;
    }

    public Boolean releasePerform( String releaseId, String goals, String arguments, boolean useReleaseProfile,
                                   Map repository )
        throws Exception
    {
        Boolean result;

        try
        {
            slave.releasePerform( releaseId, goals, arguments, useReleaseProfile, repository );
            result = Boolean.FALSE;
            log.info( "Performing release" );
        }
        catch ( Exception e )
        {
            log.error( "Error performing release", e );
            throw new Exception( "Error performing release", e );
        }

        return result;
    }

    public String releasePerformFromScm( String goals, String arguments, boolean useReleaseProfile, Map repository,
                                         String scmUrl, String scmUsername, String scmPassword, String scmTag,
                                         String scmTagBase, Map environments )
        throws Exception
    {
        String result;

        try
        {
            result = slave.releasePerformFromScm( goals, arguments, useReleaseProfile, repository, scmUrl, scmUsername,
                                                  scmPassword, scmTag, scmTagBase, environments );
            log.info( "Performing release" );
        }
        catch ( Exception e )
        {
            log.error( "Error performing release from scm", e );
            throw new Exception( "Error performing release from scm", e );
        }

        return result;
    }

    public String releaseCleanup( String releaseId )
        throws Exception
    {
        String result;

        try
        {
            result = slave.releaseCleanup( releaseId );
            log.info( "Cleanup release of " + releaseId );
        }
        catch ( Exception e )
        {
            log.error( "Error cleaning up release of " + releaseId, e );
            throw new Exception( "Error cleaning up release of " + releaseId, e );
        }

        return result;
    }

    public Boolean releaseRollback( String releaseId, int projectId )
        throws Exception
    {
        Boolean result;

        try
        {
            slave.releaseRollback( releaseId, projectId );
            result = Boolean.TRUE;
            log.info( "Rollback release " + releaseId );
        }
        catch ( Exception e )
        {
            log.error( "Failed to rollback release " + releaseId );
            throw new Exception( "Failed to rollback release " + releaseId );
        }

        return result;
    }
}
