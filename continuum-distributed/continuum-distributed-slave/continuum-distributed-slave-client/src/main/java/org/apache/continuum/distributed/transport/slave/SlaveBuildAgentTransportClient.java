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

    public Map<String, Object> getBuildResult( int projectId )
        throws Exception
    {
        Map<String, Object> buildResult;

        try
        {
            buildResult = slave.getBuildResult( projectId );
            log.info( "Build result for project '" + projectId + "' acquired." );
        }
        catch ( Exception e )
        {
            log.error( "Failed to get build result for project '" + projectId + "'", e );
            throw new Exception( "Failed to get build result for project '" + projectId + "'", e );
        }

        return buildResult;
    }

    public Map<String, Object> getProjectCurrentlyBuilding()
        throws Exception
    {
        Map map;

        try
        {
            map = slave.getProjectCurrentlyBuilding();
            log.info( "Retrieving currently building project" );
        }
        catch ( Exception e )
        {
            log.error( "Failed to get the currently building project", e );
            throw new Exception( "Failed to get the currently building project", e );
        }

        return map;
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
            log.info( "Generated working copy content for project '" + projectId + "'" );
        }
        catch ( Exception e )
        {
            log.error( "Error generating working copy content for project '" + projectId + "'", e );
            throw new Exception( "Error generating working copy content for project '" + projectId + "'", e );
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
            log.info( "Retrieved project '" + projectId + "' file content" );
        }
        catch ( Exception e )
        {
            log.error( "Error retrieving project '" + projectId + "' file content", e );
            throw new Exception( "Error retrieving project '" + projectId + "' file content", e );
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
            log.info( "Retrieving release plugin parameters for project '" + projectId + "'" );
        }
        catch ( Exception e )
        {
            log.error( "Error retrieving release plugin parameters for project '" + projectId + "'", e );
            throw new Exception( "Error retrieving release plugin parameters for project '" + projectId + "'", e );
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
            log.info( "Processing project '" + projectId + "'" );
        }
        catch ( Exception e )
        {
            log.error( "Error processing project '" + projectId + "'", e );
            throw new Exception( "Error processing project '" + projectId + "'", e );
        }

        return result;
    }

    public String releasePrepare( Map project, Map properties, Map releaseVersion, Map developmentVersion,
                                  Map environments, String username )
        throws Exception
    {
        String releaseId;

        try
        {
            releaseId = slave.releasePrepare( project, properties, releaseVersion, developmentVersion, environments, username );
            log.info( "Preparing release '" + releaseId + "'" );
        }
        catch ( Exception e )
        {
            log.error( "Error while preparing release", e );
            throw new Exception( "Error while preparing release", e );
        }

        return releaseId;
    }

    public Map<String, Object> getReleaseResult( String releaseId )
        throws Exception
    {
        Map result;

        try
        {
            result = slave.getReleaseResult( releaseId );
            log.info( "Retrieving release result, releaseId=" + releaseId );
        }
        catch ( Exception e )
        {
            log.error( "Error retrieving release result, releaseId=" + releaseId, e );
            throw new Exception( "Error retrieving release result, releaseId=" + releaseId, e );
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
            log.info( "Retrieving listener for releaseId=" + releaseId );
        }
        catch ( Exception e )
        {
            log.error( "Error retrieving listener for releaseId=" + releaseId, e );
            throw new Exception( "Error retrieving listener for releaseId=" + releaseId, e );
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
            log.info( "Removing listener for releaseId=" + releaseId );
        }
        catch ( Exception e )
        {
            log.error( "Error removing listener for releaseId=" + releaseId, e );
            throw new Exception( "Error removing listener for releaseId=" + releaseId, e );
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
            log.info( "Retrieving prepared release name, releaseId=" + releaseId );
        }
        catch ( Exception e )
        {
            log.error( "Error while retrieving prepared release name, releaseId=" + releaseId );
            throw new Exception( "Error while retrieving prepared release name, releaseId=" + releaseId );
        }

        return result;
    }

    public Boolean releasePerform( String releaseId, String goals, String arguments, boolean useReleaseProfile,
                                   Map repository, String username )
        throws Exception
    {
        Boolean result;

        try
        {
            slave.releasePerform( releaseId, goals, arguments, useReleaseProfile, repository, username );
            result = Boolean.FALSE;
            log.info( "Performing release of releaseId=" + releaseId );
        }
        catch ( Exception e )
        {
            log.error( "Error performing release of releaseId=" + releaseId, e );
            throw new Exception( "Error performing release of releaseId=" + releaseId, e );
        }

        return result;
    }

    public String releasePerformFromScm( String goals, String arguments, boolean useReleaseProfile, Map repository,
                                         String scmUrl, String scmUsername, String scmPassword, String scmTag,
                                         String scmTagBase, Map environments, String username )
        throws Exception
    {
        String result;

        try
        {
            result = slave.releasePerformFromScm( goals, arguments, useReleaseProfile, repository, scmUrl, scmUsername,
                                                  scmPassword, scmTag, scmTagBase, environments, username );
            log.info( "Performing release of scmUrl=" + scmUrl );
        }
        catch ( Exception e )
        {
            log.error( "Error performing release from scm '" + scmUrl + "'", e );
            throw new Exception( "Error performing release from scm '" + scmUrl + "'", e );
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
            log.info( "Cleanup release, releaseId=" + releaseId );
        }
        catch ( Exception e )
        {
            log.error( "Error cleaning up release, releaseId=" + releaseId, e );
            throw new Exception( "Error cleaning up release, releaseId=" + releaseId, e );
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
            log.info( "Rollback release. releaseId=" + releaseId + ", projectId=" + projectId );
        }
        catch ( Exception e )
        {
            log.error( "Failed to rollback release. releaseId=" + releaseId + ", projectId=" + projectId );
            throw new Exception( "Failed to rollback release. releaseId=" + releaseId + ", projectId=" + projectId );
        }

        return result;
    }

    public Integer getBuildSizeOfAgent()
        throws Exception
    {
        Integer size;

        try
        {
            size = slave.getBuildSizeOfAgent();
            log.info( "Retrieving build size of agent" );
        }
        catch ( Exception e )
        {
            log.error( "Failed to retrieve build size of agent", e );
            throw new Exception( "Failed to retrieve build size of agent", e );
        }

        return size;
    }

    public Map<String, Object> getProjectCurrentlyPreparingBuild()
        throws Exception
    {
        Map<String, Object> projects;

        try
        {
            projects = slave.getProjectCurrentlyPreparingBuild();
            log.info( "Retrieving projects currently preparing build" );
        }
        catch ( Exception e )
        {
            log.error( "Failed to retrieve projects currently preparing build", e );
            throw new Exception( "Failed to retrieve projects currently preparing build", e );
        }

        return projects;
    }

    public List<Map<String, Object>> getProjectsInBuildQueue()
        throws Exception
    {
        List<Map<String, Object>> projects;

        try
        {
            projects = slave.getProjectsInBuildQueue();
            log.info( "Retrieving projects in build queue" );
        }
        catch ( Exception e )
        {
            log.error( "Failed to retrieve projects in build queue", e );
            throw new Exception( "Failed to retrieve projects in build queue", e );
        }

        return projects;
    }

    public List<Map<String, Object>> getProjectsInPrepareBuildQueue()
        throws Exception
    {
        List<Map<String, Object>> projects;

        try
        {
            projects = slave.getProjectsInPrepareBuildQueue();
            log.info( "Retrieving projects in prepare build queue" );
        }
        catch ( Exception e )
        {
            log.error( "Failed to retrieve projects in prepare build queue", e );
            throw new Exception( "Failed to retrieve projects in prepare build queue", e );
        }

        return projects;
    }

    public Boolean isProjectGroupInQueue( int projectGroupId )
        throws Exception
    {
        Boolean result;

        try
        {
            result = slave.isProjectGroupInQueue( projectGroupId );
            log.info( "Checking if project group '" + projectGroupId + "' is in queue" );
        }
        catch ( Exception e )
        {
            log.error( "Failed to check if project group '" + projectGroupId + "' is in queue", e );
            throw new Exception( "Failed to check if project group '" + projectGroupId + "' is in queue", e );
        }

        return result;
    }

    public Boolean isProjectCurrentlyBuilding( int projectId )
        throws Exception
    {
        Boolean result;

        try
        {
            result = slave.isProjectCurrentlyBuilding( projectId );
            log.info( "Checking if project " + projectId + " is currently building in agent" );
        }
        catch ( Exception e )
        {
            log.error( "Failed to check if project " + projectId + " is currently building in agent", e );
            throw new Exception( "Failed to check if project " + projectId + " is currently building in agent", e );
        }

        return result;
    }

    public Boolean isProjectInBuildQueue( int projectId )
        throws Exception
    {
        Boolean result;

        try
        {
            result = slave.isProjectInBuildQueue( projectId );
            log.info( "Checking if project " + projectId + "is in build queue of agent" );
        }
        catch ( Exception e )
        {
            log.error( "Failed to check if project " + projectId + " is in build queue of agent", e );
            throw new Exception( "Failed to check if project " + projectId + " is in build queue of agent", e );
        }

        return result;
    }

    public Boolean removeFromPrepareBuildQueue( int projectGroupId, int scmRootId )
        throws Exception
    {
        Boolean result;

        try
        {
            result = slave.removeFromPrepareBuildQueue( projectGroupId, scmRootId );
            log.debug( "Remove projects from prepare build queue. projectGroupId=" + projectGroupId +
            		   ", scmRootId=" + scmRootId );
        }
        catch ( Exception e )
        {
            log.error( "Failed to remove projects from prepare build queue. projectGroupId=" + projectGroupId +
                       ", scmRootId=" + scmRootId );
            throw new Exception( "Failed to remove from prepare build queue. projectGroupId=" + projectGroupId +
                                 " scmRootId=" + scmRootId, e );
        }

        return result;
    }

    public Boolean removeFromPrepareBuildQueue( List<String> hashCodes )
        throws Exception
    {
        Boolean result;

        try
        {
            result = slave.removeFromPrepareBuildQueue( hashCodes );
            log.info( "Removing projects from prepare build queue of agent" );
        }
        catch ( Exception e )
        {
            log.error( "Failed to remove projects from prepare build queue of agent", e );
            throw new Exception( "Failed to remove projects from prepare build queue of agent", e );
        }

        return result;
    }

    public Boolean removeFromBuildQueue( int projectId, int buildDefinitionId )
        throws Exception
    {
        Boolean result;

        try
        {
            result = slave.removeFromBuildQueue( projectId, buildDefinitionId );
            log.info( "Removing project '" + projectId + "' from build queue of agent" );
        }
        catch ( Exception e )
        {
            log.error( "Failed to remove project '" + projectId + "' from build queue of agent", e );
            throw new Exception( "Failed to remove project '" + projectId + "' from build queue of agent", e );
        }

        return result;
    }

    public Boolean removeFromBuildQueue( List<String> hashCodes )
        throws Exception
    {
        Boolean result;

        try
        {
            result = slave.removeFromBuildQueue( hashCodes );
            log.info( "Removing projects from build queue of agent" );
        }
        catch ( Exception e )
        {
            log.error( "Failed to remove projects from build queue of agent", e );
            throw new Exception( "Failed to remove projects from build queue of agent", e );
        }

        return result;
    }
}
