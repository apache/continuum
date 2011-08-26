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
import java.util.Properties;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.xmlrpc.ApacheBinder;
import com.atlassian.xmlrpc.Binder;
import com.atlassian.xmlrpc.BindingException;
import com.atlassian.xmlrpc.ConnectionInfo;

/**
 * SlaveBuildAgentTransportClient
 */
public class SlaveBuildAgentTransportClient
    implements SlaveBuildAgentTransportService
{
    private static final Logger log = LoggerFactory.getLogger( SlaveBuildAgentTransportClient.class );

    private SlaveBuildAgentTransportService slave;

    private String buildAgentUrl;

    public SlaveBuildAgentTransportClient( URL serviceUrl )
        throws Exception
    {
        this( serviceUrl, null, null );
    }

    public SlaveBuildAgentTransportClient( URL serviceUrl, String login, String password )
        throws Exception
    {
        Binder binder = new ApacheBinder();

        ConnectionInfo connectionInfo = new ConnectionInfo();
        connectionInfo.setUsername( login );
        connectionInfo.setPassword( password );
        connectionInfo.setTimeZone( TimeZone.getDefault() );

        buildAgentUrl = serviceUrl.toString();

        try
        {
            slave = binder.bind( SlaveBuildAgentTransportService.class, serviceUrl, connectionInfo );
        }
        catch ( BindingException e )
        {
            log.error( "Can't bind service interface " + SlaveBuildAgentTransportService.class.getName() + " to " +
                serviceUrl.toExternalForm() + " using " + connectionInfo.getUsername() + ", " + connectionInfo.getPassword(), e );
            throw new Exception(
                "Can't bind service interface " + SlaveBuildAgentTransportService.class.getName() + " to " +
                    serviceUrl.toExternalForm() + " using " + connectionInfo.getUsername() + ", " + connectionInfo.getPassword(),
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
            log.debug( "Building projects in build agent {}", buildAgentUrl );
        }
        catch ( Exception e )
        {
            log.error( "Failed to build projects in build agent " + buildAgentUrl, e );
            throw new Exception( "Failed to build projects in build agent " + buildAgentUrl, e );
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
            log.debug( "Available installations in build agent {} : {}", buildAgentUrl, installations.size() );
        }
        catch ( Exception e )
        {
            log.error( "Failed to get available installations in build agent " + buildAgentUrl, e );
            throw new Exception( "Failed to get available installations in build agent " + buildAgentUrl, e );
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
            log.debug( "Build result for project '{}' acquired from build agent {}", projectId, buildAgentUrl );
        }
        catch ( Exception e )
        {
            log.error( "Failed to get build result for project '" + projectId + "' in build agent " + buildAgentUrl, e );
            throw new Exception( "Failed to get build result for project '" + projectId + "' in build agent " + buildAgentUrl, e );
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
            log.debug( "Retrieving currently building project in build agent {}", buildAgentUrl );
        }
        catch ( Exception e )
        {
            log.error( "Failed to get the currently building project in build agent " + buildAgentUrl, e );
            throw new Exception( "Failed to get the currently building project in build agent " + buildAgentUrl, e );
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
            log.debug( "Ping build agent {} : {}", buildAgentUrl, ( result ? "ok" : "failed" ) );
        }
        catch ( Exception e )
        {
            log.error( "Ping build agent " + buildAgentUrl + " error", e );
            throw new Exception( "Ping build agent " + buildAgentUrl + " error", e );
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
            log.debug( "Cancelled current build in build agent {}", buildAgentUrl );
        }
        catch ( Exception e )
        {
            log.error( "Error cancelling current build in build agent " + buildAgentUrl, e  );
            throw new Exception( "Error cancelling current build in build agent " + buildAgentUrl, e );
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
            log.debug( "Generated working copy content for project '{}' in build agent ", projectId, buildAgentUrl );
        }
        catch ( Exception e )
        {
            log.error( "Error generating working copy content for project '" + projectId + "' in build agent " + buildAgentUrl, e );
            throw new Exception( "Error generating working copy content for project '" + projectId + "' in build agent " + buildAgentUrl, e );
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
            log.debug( "Retrieved project '{}' file content from build agent {}", projectId, buildAgentUrl );
        }
        catch ( Exception e )
        {
            log.error( "Error retrieving project '" + projectId + "' file content from build agent " + buildAgentUrl, e );
            throw new Exception( "Error retrieving project '" + projectId + "' file content from build agent " + buildAgentUrl, e );
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
            log.debug( "Retrieving release plugin parameters for project '{}' from build agent {}", projectId, buildAgentUrl );
        }
        catch ( Exception e )
        {
            log.error( "Error retrieving release plugin parameters for project '" + projectId + "' from build agent " + buildAgentUrl, e );
            throw new Exception( "Error retrieving release plugin parameters for project '" + projectId + "' from build agent " + buildAgentUrl, e );
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
            log.debug( "Processing project '{}' in build agent ", projectId, buildAgentUrl );
        }
        catch ( Exception e )
        {
            log.error( "Error processing project '" + projectId + "' in build agent " + buildAgentUrl, e );
            throw new Exception( "Error processing project '" + projectId + "' in build agent " + buildAgentUrl, e );
        }

        return result;
    }

    public String releasePrepare( Map project, Properties properties, Map releaseVersion, Map developmentVersion,
                                  Map environments, String username )
        throws Exception
    {
        String releaseId;

        try
        {
            releaseId = slave.releasePrepare( project, properties, releaseVersion, developmentVersion, environments, username );
            log.debug( "Preparing release '{}' in build agent {}", releaseId, buildAgentUrl );
        }
        catch ( Exception e )
        {
            log.error( "Error while preparing release in build agent " + buildAgentUrl, e );
            throw new Exception( "Error while preparing release in build agent " + buildAgentUrl, e );
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
            log.debug( "Retrieving release result, releaseId={} from build agent {}", releaseId, buildAgentUrl );
        }
        catch ( Exception e )
        {
            log.error( "Error retrieving release result, releaseId=" + releaseId + " from build agent " + buildAgentUrl, e );
            throw new Exception( "Error retrieving release result, releaseId=" + releaseId + " from build agent " + buildAgentUrl, e );
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
            log.debug( "Retrieving listener for releaseId={} from build agent {}", releaseId, buildAgentUrl );
        }
        catch ( Exception e )
        {
            log.error( "Error retrieving listener for releaseId=" + releaseId + " from build agent " + buildAgentUrl, e );
            throw new Exception( "Error retrieving listener for releaseId=" + releaseId + " from build agent " + buildAgentUrl, e );
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
            log.debug( "Removing listener for releaseId={} from build agent {}", releaseId, buildAgentUrl );
        }
        catch ( Exception e )
        {
            log.error( "Error removing listener for releaseId=" + releaseId + " from build agent " + buildAgentUrl, e );
            throw new Exception( "Error removing listener for releaseId=" + releaseId + " from build agent " + buildAgentUrl, e );
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
            log.debug( "Retrieving prepared release name, releaseId={} from build agent {}", releaseId, buildAgentUrl );
        }
        catch ( Exception e )
        {
            log.error( "Error while retrieving prepared release name, releaseId=" + releaseId + " from build agent " + buildAgentUrl, e );
            throw new Exception( "Error while retrieving prepared release name, releaseId=" + releaseId + " from build agent " + buildAgentUrl, e );
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
            log.debug( "Performing release of releaseId={} from build agent {}", releaseId, buildAgentUrl );
        }
        catch ( Exception e )
        {
            log.error( "Error performing release of releaseId=" + releaseId + " from build agent " + buildAgentUrl, e );
            throw new Exception( "Error performing release of releaseId=" + releaseId + " from build agent " + buildAgentUrl, e );
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
            log.debug( "Performing release of scmUrl={} from build agent {}", scmUrl, buildAgentUrl );
        }
        catch ( Exception e )
        {
            log.error( "Error performing release from scm '" + scmUrl + "' from build agent " + buildAgentUrl, e );
            throw new Exception( "Error performing release from scm '" + scmUrl + "' from build agent " + buildAgentUrl, e );
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
            log.debug( "Cleanup release, releaseId={} from build agent {}", releaseId, buildAgentUrl );
        }
        catch ( Exception e )
        {
            log.error( "Error cleaning up release, releaseId=" + releaseId + " from build agent " + buildAgentUrl, e );
            throw new Exception( "Error cleaning up release, releaseId=" + releaseId + " from build agent " + buildAgentUrl, e );
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
            log.debug( "Rollback release. releaseId={}, projectId={} from build agent {}", new Object[] { releaseId, projectId, buildAgentUrl } );
        }
        catch ( Exception e )
        {
            log.error( "Failed to rollback release. releaseId=" + releaseId + ", projectId=" + projectId + " from build agent " + buildAgentUrl, e );
            throw new Exception( "Failed to rollback release. releaseId=" + releaseId + ", projectId=" + projectId + " from build agent " + buildAgentUrl, e );
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
            log.debug( "Retrieving build size of build agent {}", buildAgentUrl );
        }
        catch ( Exception e )
        {
            log.error( "Failed to retrieve build size of build agent " + buildAgentUrl, e );
            throw new Exception( "Failed to retrieve build size of build agent " + buildAgentUrl, e );
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
            log.debug( "Retrieving projects currently preparing build in build agent {}", buildAgentUrl );
        }
        catch ( Exception e )
        {
            log.error( "Failed to retrieve projects currently preparing build in build agent " + buildAgentUrl, e );
            throw new Exception( "Failed to retrieve projects currently preparing build in build agent " + buildAgentUrl, e );
        }

        return projects;
    }

    public List<Map<String, Object>> getProjectsAndBuildDefinitionsCurrentlyPreparingBuild()
        throws Exception
    {
        List<Map<String, Object>> projects;

        try
        {
            projects = slave.getProjectsAndBuildDefinitionsCurrentlyPreparingBuild();
            log.debug( "Retrieving projects currently preparing build in build agent {}", buildAgentUrl );
        }
        catch ( Exception e )
        {
            log.error( "Failed to retrieve projects currently preparing build in build agent " + buildAgentUrl, e );
            throw new Exception( "Failed to retrieve projects currently preparing build in build agent " + buildAgentUrl, e );
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
            log.debug( "Retrieving projects in build queue of build agent {}", buildAgentUrl );
        }
        catch ( Exception e )
        {
            log.error( "Failed to retrieve projects in build queue of build agent " + buildAgentUrl, e );
            throw new Exception( "Failed to retrieve projects in build queue of build agent " + buildAgentUrl, e );
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
            log.debug( "Retrieving projects in prepare build queue of build agent {}", buildAgentUrl );
        }
        catch ( Exception e )
        {
            log.error( "Failed to retrieve projects in prepare build queue of build agent " + buildAgentUrl, e );
            throw new Exception( "Failed to retrieve projects in prepare build queue of build agent " + buildAgentUrl, e );
        }

        return projects;
    }

    public List<Map<String, Object>> getProjectsAndBuildDefinitionsInPrepareBuildQueue()
        throws Exception
    {
        List<Map<String, Object>> projects;

        try
        {
            projects = slave.getProjectsAndBuildDefinitionsInPrepareBuildQueue();
            log.debug( "Retrieving projects in prepare build queue of build agent {}", buildAgentUrl );
        }
        catch ( Exception e )
        {
            log.error( "Failed to retrieve projects in prepare build queue of build agent " + buildAgentUrl, e );
            throw new Exception( "Failed to retrieve projects in prepare build queue of build agent " + buildAgentUrl, e );
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
            log.debug( "Checking if project group '{}' is in queue in build agent {}", projectGroupId, buildAgentUrl );
        }
        catch ( Exception e )
        {
            log.error( "Failed to check if project group '" + projectGroupId + "' is in queue in build agent " + buildAgentUrl, e );
            throw new Exception( "Failed to check if project group '" + projectGroupId + "' is in queue in build agent " + buildAgentUrl, e );
        }

        return result;
    }

    public Boolean isProjectScmRootInQueue( int projectScmRootId, List<Integer> projectIds )
        throws Exception
    {
        Boolean result;

        try
        {
            result = slave.isProjectScmRootInQueue( projectScmRootId, projectIds );
            log.debug( "Checking if project scm root '{}' is in queue in build agent {}", projectScmRootId, buildAgentUrl );
        }
        catch ( Exception e )
        {
            log.error( "Failed to check if project scm root '" + projectScmRootId + "' is in queue in build agent " + buildAgentUrl, e );
            throw new Exception( "Failed to check if project scm root '" + projectScmRootId + "' is in queue in build agent " + buildAgentUrl, e );
        }

        return result;
    }

    public Boolean isProjectCurrentlyBuilding( int projectId, int buildDefinitionId )
        throws Exception
    {
        Boolean result;

        try
        {
            result = slave.isProjectCurrentlyBuilding( projectId, buildDefinitionId );
            log.debug( "Checking if projectId={}, buildDefinitionId={} is currently building in build agent {}", 
                       new Object[] { projectId, buildDefinitionId, buildAgentUrl } );
        }
        catch ( Exception e )
        {
            log.error( "Failed to check if projectId=" + projectId + ", buildDefinitionId=" + buildDefinitionId + 
                       " is currently building in build agent " + buildAgentUrl, e );
            throw new Exception( "Failed to check if projectId=" + projectId + ", buildDefinitionId=" + buildDefinitionId + 
                                 " is currently building in build agent " + buildAgentUrl, e );
        }

        return result;
    }

    public Boolean isProjectInBuildQueue( int projectId, int buildDefinitionId )
        throws Exception
    {
        Boolean result;

        try
        {
            result = slave.isProjectInBuildQueue( projectId, buildDefinitionId );
            log.debug( "Checking if projectId={}, buildDefinitionId={} is in build queue of build agent {}",
                       new Object[] { projectId, buildDefinitionId, buildAgentUrl } );
        }
        catch ( Exception e )
        {
            log.error( "Failed to check if projectId=" + projectId + ", buildDefinitionId=" + buildDefinitionId + 
                       " is in build queue of build agent " + buildAgentUrl, e );
            throw new Exception( "Failed to check if projectId=" + projectId + ", buildDefinitionId=" + buildDefinitionId + 
                                 " is in build queue of build agent " + buildAgentUrl, e );
        }

        return result;
    }

    public Boolean isProjectCurrentlyPreparingBuild( int projectId, int buildDefinitionId )
        throws Exception
    {
        Boolean result;

        try
        {
            result = slave.isProjectCurrentlyPreparingBuild( projectId, buildDefinitionId );
            log.debug( "Checking if projectId={}, buildDefinitionId={} is currently preparing build in build agent {}",
                       new Object[] { projectId, buildDefinitionId, buildAgentUrl } );
        }
        catch ( Exception e )
        {
            log.error( "Failed to check if projectId=" + projectId + ", buildDefinitionId=" + buildDefinitionId + 
                       " is currently preparing build in build agent " + buildAgentUrl, e );
            throw new Exception( "Failed to check if projectId=" + projectId + ", buildDefinitionId=" + buildDefinitionId + 
                                 " is currently preparing build in build agent " + buildAgentUrl, e );
        }

        return result;
    }

    public Boolean isProjectInPrepareBuildQueue( int projectId, int buildDefinitionId )
        throws Exception
    {
        Boolean result;

        try
        {
            result = slave.isProjectInPrepareBuildQueue( projectId, buildDefinitionId );
            log.debug( "Checking if projectId={}, buildDefinitionId={} is in prepare build queue of build agent {}",
                       new Object[] { projectId, buildDefinitionId, buildAgentUrl } );
        }
        catch ( Exception e )
        {
            log.error( "Failed to check if projectId=" + projectId + ", buildDefinitionId=" + buildDefinitionId + 
                       " is in prepare build queue of build agent " + buildAgentUrl, e );
            throw new Exception( "Failed to check if projectId=" + projectId + ", buildDefinitionId=" + buildDefinitionId + 
                                 " is in prepare build queue of build agent " + buildAgentUrl, e );
        }

        return result;
    }

    public Boolean isProjectGroupInPrepareBuildQueue( int projectGroupId )
        throws Exception
    {
        Boolean result;

        try
        {
            result = slave.isProjectGroupInPrepareBuildQueue( projectGroupId );
            log.debug( "Checking if projectGroup {} is in prepare build queue of build agent {}", projectGroupId, buildAgentUrl );
        }
        catch ( Exception e )
        {
            log.error( "Failed to check if projectGroup " + projectGroupId + " is in prepare build queue of build agent " + buildAgentUrl, e );
            throw new Exception( "Failed to check if projectGroup " + projectGroupId + " is in prepare build queue of build agent " + buildAgentUrl, e );
        }

        return result;
    }

    public Boolean isProjectGroupCurrentlyPreparingBuild( int projectGroupId )
        throws Exception
    {
        Boolean result;

        try
        {
            result = slave.isProjectGroupCurrentlyPreparingBuild( projectGroupId );
            log.debug( "Checking if projectGroup {} is currently preparing build in build agent {}", projectGroupId, buildAgentUrl );
        }
        catch ( Exception e )
        {
            log.error( "Failed to check if projectGroup " + projectGroupId + " is currently preparing build in build agent " + buildAgentUrl, e );
            throw new Exception( "Failed to check if projectGroup " + projectGroupId + " is currently preparing build in build agent " + buildAgentUrl, e );
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
            log.debug( "Remove projects from prepare build queue of build agent {}. projectGroupId={}, scmRootId={}", 
                       new Object[] { buildAgentUrl, projectGroupId, scmRootId } );
        }
        catch ( Exception e )
        {
            log.error( "Failed to remove projects from prepare build queue of build agent " + buildAgentUrl + ". projectGroupId=" + projectGroupId + 
                       ", scmRootId=" + scmRootId, e );
            throw new Exception( "Failed to remove from prepare build queue of build agent " + buildAgentUrl + ". projectGroupId=" + projectGroupId +
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
            log.debug( "Removing projects from prepare build queue of build agent {}", buildAgentUrl );
        }
        catch ( Exception e )
        {
            log.error( "Failed to remove projects from prepare build queue of build agent " + buildAgentUrl, e );
            throw new Exception( "Failed to remove projects from prepare build queue of build agent " + buildAgentUrl, e );
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
            log.debug( "Removing project '{}' from build queue of build agent {}", projectId, buildAgentUrl );
        }
        catch ( Exception e )
        {
            log.error( "Failed to remove project '" + projectId + "' from build queue of build agent " + buildAgentUrl, e );
            throw new Exception( "Failed to remove project '" + projectId + "' from build queue of build agent " + buildAgentUrl, e );
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
            log.debug( "Removing projects from build queue of build agent {}", buildAgentUrl );
        }
        catch ( Exception e )
        {
            log.error( "Failed to remove projects from build queue of build agent " + buildAgentUrl, e );
            throw new Exception( "Failed to remove projects from build queue of build agent " + buildAgentUrl, e );
        }

        return result;
    }

    public String getBuildAgentPlatform()
        throws Exception
    {
        String result;

        try
        {
            result = slave.getBuildAgentPlatform();
            log.debug( "Retrieved build agent {} platform", buildAgentUrl );
        }
        catch ( Exception e )
        {
            log.error( "Failed to return build agent " + buildAgentUrl + " platform", e );
            throw new Exception( "Failed to return build agent " + buildAgentUrl + " platform", e );
        }

        return result;
    }

    @Override
    public void executeDirectoryPurge( String directoryType, int daysOlder, int retentionCount, boolean deleteAll ) throws Exception
    {
        slave.executeDirectoryPurge( directoryType, daysOlder, retentionCount, deleteAll );
    }
}
