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

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.continuum.buildagent.ContinuumBuildAgentException;
import org.apache.continuum.buildagent.ContinuumBuildAgentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ProxyMasterBuildAgentTransportService
 */
public class SlaveBuildAgentTransportServer
    implements SlaveBuildAgentTransportService
{
    private Logger log = LoggerFactory.getLogger( this.getClass() );

    private ContinuumBuildAgentService continuumBuildAgentService;

    public SlaveBuildAgentTransportServer( ContinuumBuildAgentService continuumBuildAgentService )
    {
        this.continuumBuildAgentService = continuumBuildAgentService;
    }

    public Boolean buildProjects( List<Map<String, Object>> projectsBuildContext )
        throws Exception
    {
        Boolean result;

        try
        {
            continuumBuildAgentService.buildProjects( projectsBuildContext );
            result = Boolean.TRUE;

            log.info( "Building projects." );
        }
        catch ( ContinuumBuildAgentException e )
        {
            log.error( "Failed to build projects.", e );
            throw e;
        }

        return result;
    }

    public List<Map<String, String>> getAvailableInstallations()
        throws Exception
    {
        List<Map<String, String>> installations;

        try
        {
            installations = continuumBuildAgentService.getAvailableInstallations();
            log.debug( "Available installations: {}", installations.size() );
        }
        catch ( ContinuumBuildAgentException e )
        {
            log.error( "Failed to get available installations.", e );
            throw e;
        }

        return installations;
    }

    public Map<String, Object> getBuildResult( int projectId )
        throws Exception
    {
        Map<String, Object> buildResult;

        try
        {
            buildResult = continuumBuildAgentService.getBuildResult( projectId );
            log.debug( "Build result for project '{}' acquired.", projectId );
        }
        catch ( ContinuumBuildAgentException e )
        {
            log.error( "Failed to get build result for project '" + projectId + "'", e );
            throw e;
        }

        return buildResult;
    }

    public Map<String, Object> getProjectCurrentlyBuilding()
        throws Exception
    {
        Map<String, Object> project = continuumBuildAgentService.getProjectCurrentlyBuilding();

        log.debug( "Retrieving currently building project" );

        return project;
    }

    public Boolean ping()
        throws Exception
    {
        return continuumBuildAgentService.ping(); 
    }

    public Boolean cancelBuild()
        throws Exception
    {
        Boolean result;

        try
        {
            continuumBuildAgentService.cancelBuild();
            result = Boolean.TRUE;
            log.debug( "Cancelled build" );
        }
        catch ( ContinuumBuildAgentException e )
        {
            log.error( "Failed to cancel build", e );
            throw e;
        }

        return result;
    }

    public String generateWorkingCopyContent( int projectId, String directory, String baseUrl, String imagesBaseUrl )
        throws Exception
    {
        try
        {
            log.debug( "Generate working copy content for project '{}'", projectId );
            return continuumBuildAgentService.generateWorkingCopyContent( projectId, directory, baseUrl, imagesBaseUrl );
        }
        catch ( ContinuumBuildAgentException e )
        {
            log.error( "Failed to generate working copy content for projectId=" + projectId, e );
            throw e;
        }
    }

    public String getProjectFileContent( int projectId, String directory, String filename )
        throws Exception
    {
        try
        {
            log.debug( "Retrieve project '{}' file content", projectId );
            return continuumBuildAgentService.getProjectFileContent( projectId, directory, filename );
        }
        catch ( ContinuumBuildAgentException e )
        {
            log.error( "Failed to retrieve project '" + projectId + "' file content", e );
            throw e;
        }
    }

    public Map getReleasePluginParameters( int projectId, String pomFilename )
        throws Exception
    {
        try
        {
            log.debug( "Retrieving release plugin parameters for project '{}'", projectId );
            return continuumBuildAgentService.getReleasePluginParameters( projectId, pomFilename );
        }
        catch ( ContinuumBuildAgentException e )
        {
            log.error( "Failed to retrieve release plugin parameters for project '" + projectId + "'", e );
            throw e;
        }
    }

    public List<Map<String, String>> processProject( int projectId, String pomFilename, boolean autoVersionSubmodules )
        throws Exception
    {
        try
        {
            log.debug( "Processing project '{}'", projectId );
            return continuumBuildAgentService.processProject( projectId, pomFilename, autoVersionSubmodules );
        }
        catch ( ContinuumBuildAgentException e )
        {
            log.error( "Failed to process project '" + projectId + "'", e );
            throw e;
        }
    }

    public String releasePrepare( Map project, Properties properties, Map releaseVersion, Map developmentVersion,
                                  Map environments, String username )
        throws Exception
    {
        try
        {
            log.debug( "Preparing release" );
            return continuumBuildAgentService.releasePrepare( project, properties, releaseVersion, developmentVersion,
                                                              environments, username );
        }
        catch ( ContinuumBuildAgentException e )
        {
            log.error( "Failed to prepare release", e );
            throw e;
        }
    }

    public Map<String, Object> getListener( String releaseId )
        throws Exception
    {
        try
        {
            log.debug( "Retrieving listener for releaseId={}", releaseId );
            return continuumBuildAgentService.getListener( releaseId );
        }
        catch ( ContinuumBuildAgentException e )
        {
            log.error( "Failed to retrieve listener state of releaseId=" + releaseId, e );
            throw e;
        }
    }

    public Map<String, Object> getReleaseResult( String releaseId )
        throws Exception
    {
        try
        {
            log.debug( "Retrieving release result, releaseId={}", releaseId );
            return continuumBuildAgentService.getReleaseResult( releaseId );
        }
        catch ( ContinuumBuildAgentException e )
        {
            log.error( "Failed to retrieve release result of releaseId=" + releaseId, e );
            throw e;
        }
    }

    public Boolean removeListener( String releaseId )
        throws Exception
    {
        Boolean result;

        try
        {
            continuumBuildAgentService.removeListener( releaseId );
            result = Boolean.TRUE;
            log.debug( "Removing listener for releaseId={}", releaseId );
        }
        catch ( ContinuumBuildAgentException e )
        {
            log.error( "Failed to remove listener of releaseId=" + releaseId, e );
            throw e;
        }

        return result;
    }

    public String getPreparedReleaseName( String releaseId )
        throws Exception
    {
        try
        {
            log.debug( "Retrieving prepared release name, releaseId={}", releaseId );
            return continuumBuildAgentService.getPreparedReleaseName( releaseId );
        }
        catch ( ContinuumBuildAgentException e )
        {
            log.error( "Failed to retrieve prepared release name of releaseId=" + releaseId );
            throw e;
        }
    }

    public Boolean releasePerform( String releaseId, String goals, String arguments, boolean useReleaseProfile,
                                   Map repository, String username )
        throws Exception
    {
        Boolean result;

        try
        {
            continuumBuildAgentService.releasePerform( releaseId, goals, arguments, useReleaseProfile, repository, username );
            result = Boolean.TRUE;
            log.debug( "Perform release of releaseId={}", releaseId );
        }
        catch ( ContinuumBuildAgentException e )
        {
            log.error( "Unable to perform release of releaseId=" + releaseId, e );
            throw e;
        }

        return result;
    }

    public String releasePerformFromScm( String goals, String arguments, boolean useReleaseProfile, Map repository,
                                         String scmUrl, String scmUsername, String scmPassword, String scmTag,
                                         String scmTagBase, Map environments, String username )
        throws Exception
    {
        try
        {
            log.debug( "Perform release of scmUrl={}", scmUrl );
            return continuumBuildAgentService.releasePerformFromScm( goals, arguments, useReleaseProfile, repository,
                                                                     scmUrl, scmUsername, scmPassword, scmTag,
                                                                     scmTagBase, environments, username );
        }
        catch ( ContinuumBuildAgentException e )
        {
            log.error( "Unable to perform release of scmUrl=" + scmUrl, e );
            throw e;
        }
    }

    public String releaseCleanup( String releaseId )
        throws Exception
    {
        try
        {
            log.debug( "Cleanup release, releaseId={}", releaseId );
            return continuumBuildAgentService.releaseCleanup( releaseId );
        }
        catch ( ContinuumBuildAgentException e )
        {
            log.error( "Unable to cleanup release, releaseId=" + releaseId, e );
            throw e;
        }
    }

    public Boolean releaseRollback( String releaseId, int projectId )
        throws Exception
    {
        Boolean result;

        try
        {
            continuumBuildAgentService.releaseRollback( releaseId, projectId );
            result = Boolean.TRUE;
            log.debug( "Rollback release. releaseId={}, projectId={}", releaseId, projectId );
        }
        catch ( ContinuumBuildAgentException e )
        {
            log.error( "Failed to rollback release. releaseId=" + releaseId + ", projectId=" + projectId, e );
            throw e;
        }

        return result;
    }

    public Integer getBuildSizeOfAgent()
        throws Exception
    {
        try
        {
            return continuumBuildAgentService.getBuildSizeOfAgent();
        }
        catch ( ContinuumBuildAgentException e )
        {
            log.error( "Failed to retrieve build size of agent", e );
            throw e;
        }
    }

    public Map<String, Object> getProjectCurrentlyPreparingBuild()
        throws Exception
    {
        try
        {
            return continuumBuildAgentService.getProjectCurrentlyPreparingBuild();
        }
        catch ( ContinuumBuildAgentException e )
        {
            log.error( "Failed to retrieve projects currently preparing build", e );
            throw e;
        }
    }

    public List<Map<String, Object>> getProjectsAndBuildDefinitionsCurrentlyPreparingBuild()
        throws Exception
    {
        try
        {
            return continuumBuildAgentService.getProjectsAndBuildDefinitionsCurrentlyPreparingBuild();
        }
        catch ( ContinuumBuildAgentException e )
        {
            log.error( "Failed to retrieve projects currently preparing build", e );
            throw e;
        }
    }

    public List<Map<String, Object>> getProjectsInBuildQueue()
        throws Exception
    {
        try
        {
            log.debug( "Retrieving projects in build queue" );
            return continuumBuildAgentService.getProjectsInBuildQueue();
        }
        catch ( ContinuumBuildAgentException e )
        {
            log.error( "Failed to retrieve projects in build queue", e );
            throw e;
        }
    }

    public List<Map<String, Object>> getProjectsInPrepareBuildQueue()
        throws Exception
    {
        try
        {
            log.debug( "Retrieving projects in prepare build queue" );
            return continuumBuildAgentService.getProjectsInPrepareBuildQueue();
        }
        catch ( ContinuumBuildAgentException e )
        {
            log.error( "Failed to retrieve projects in prepare build queue", e );
            throw e;
        }
    }

    public List<Map<String, Object>> getProjectsAndBuildDefinitionsInPrepareBuildQueue()
        throws Exception
    {
        try
        {
            log.debug( "Retrieving projects in prepare build queue" );
            return continuumBuildAgentService.getProjectsAndBuildDefinitionsInPrepareBuildQueue();
        }
        catch ( ContinuumBuildAgentException e )
        {
            log.error( "Failed to retrieve projects in prepare build queue", e );
            throw e;
        }
    }

    public Boolean isProjectGroupInQueue( int projectGroupId )
        throws Exception
    {
        log.debug( "Checking if project group '{}' is in queue", projectGroupId );
        return continuumBuildAgentService.isProjectGroupInQueue( projectGroupId );
    }

    public Boolean isProjectScmRootInQueue( int projectScmRootId, List<Integer> projectIds )
        throws Exception
    {
        log.debug( "Checking if project scm root '{}' is in queue", projectScmRootId );
        return continuumBuildAgentService.isProjectScmRootInQueue( projectScmRootId, projectIds );
    }

    public Boolean isProjectCurrentlyBuilding( int projectId, int buildDefinitionId )
        throws Exception
    {
        log.info( "Checking if projectId={}, buildDefinitionId={} is currently building in agent", projectId, buildDefinitionId );
        return continuumBuildAgentService.isProjectCurrentlyBuilding( projectId, buildDefinitionId );
    }

    public Boolean isProjectInBuildQueue( int projectId, int buildDefinitionId )
        throws Exception
    {
        log.info( "Checking if projectId={}, buildDefinitionId={} is in build queue of agent", projectId, buildDefinitionId );
        return continuumBuildAgentService.isProjectInBuildQueue( projectId, buildDefinitionId );
    }

    public Boolean isProjectCurrentlyPreparingBuild( int projectId, int buildDefinitionId )
        throws Exception
    {
        log.info( "Checking if projectId={}, buildDefinitionId={} is currently preparing build", projectId, buildDefinitionId );
        return continuumBuildAgentService.isProjectCurrentlyPreparingBuild( projectId, buildDefinitionId );
    }

    public Boolean isProjectInPrepareBuildQueue( int projectId, int buildDefinitionId )
        throws Exception
    {
        log.info( "Checking if projectId={}, buildDefinitionId={} is in prepare build queue", projectId, buildDefinitionId );
        return continuumBuildAgentService.isProjectInPrepareBuildQueue( projectId, buildDefinitionId );
    }

    public Boolean isProjectGroupInPrepareBuildQueue( int projectGroupId )
        throws Exception
    {
        log.info( "Checking if project group '" + projectGroupId + "' is in prepare build queue" );
        return continuumBuildAgentService.isProjectGroupInPrepareBuildQueue( projectGroupId );
    }

    public Boolean isProjectGroupCurrentlyPreparingBuild( int projectGroupId )
        throws Exception
    {
        log.info( "Checking if project group '" + projectGroupId + "' is currently preparing build" );
        return continuumBuildAgentService.isProjectGroupCurrentlyPreparingBuild( projectGroupId );
    }

    public Boolean removeFromPrepareBuildQueue( int projectGroupId, int scmRootId )
        throws Exception
    {
        try
        {
            log.info( "Remove projects from prepare build queue. projectGroupId=" + projectGroupId +
                      ", scmRootId=" + scmRootId );
            return continuumBuildAgentService.removeFromPrepareBuildQueue( projectGroupId, scmRootId );
        }
        catch ( ContinuumBuildAgentException e )
        {
            log.error( "Failed to remove projects from prepare build queue. projectGroupId=" + projectGroupId +
                       ", scmRootId=" + scmRootId );
            throw e;
        }
    }

    public Boolean removeFromPrepareBuildQueue( List<String> hashCodes )
        throws Exception
    {
        Boolean result;

        try
        {
            continuumBuildAgentService.removeFromPrepareBuildQueue( hashCodes );
            result = Boolean.TRUE;
            log.info( "Remove projects from prepare build queue" );
        }
        catch ( ContinuumBuildAgentException e )
        {
            log.error( "Failed to remove projects from prepare build queue" );
            throw e;
        }

        return result;
    }

    public Boolean removeFromBuildQueue( int projectId, int buildDefinitionId )
        throws Exception
    {
        try
        {
            log.debug( "Remove project '" + projectId + "' from build queue" );
            return continuumBuildAgentService.removeFromBuildQueue( projectId, buildDefinitionId );
        }
        catch ( ContinuumBuildAgentException e )
        {
            log.error( "Failed to remove project '" + projectId + "' from build queue" );
            throw e;
        }
    }

    public Boolean removeFromBuildQueue( List<String> hashCodes )
        throws Exception
    {
        Boolean result;

        try
        {
            continuumBuildAgentService.removeFromBuildQueue( hashCodes );
            result = Boolean.TRUE;
            log.info( "Remove projects from build queue" );
        }
        catch ( ContinuumBuildAgentException e )
        {
            log.error( "Failed to remove projects from build queue" );
            throw e;
        }

        return result;
    }

    public String getBuildAgentPlatform()
        throws Exception
    {
        return continuumBuildAgentService.getBuildAgentPlatform();
    }
    
    @Override
    public void executeDirectoryPurge( String directoryType, int daysOlder, int retentionCount, boolean deleteAll ) throws Exception
    {
        continuumBuildAgentService.executeDirectoryPurge( directoryType, daysOlder, retentionCount, deleteAll );
    }
}
