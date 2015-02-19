package org.apache.maven.continuum.xmlrpc.client;

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

import org.apache.continuum.xmlrpc.release.ContinuumReleaseResult;
import org.apache.continuum.xmlrpc.repository.DirectoryPurgeConfiguration;
import org.apache.continuum.xmlrpc.repository.LocalRepository;
import org.apache.continuum.xmlrpc.repository.RepositoryPurgeConfiguration;
import org.apache.continuum.xmlrpc.utils.BuildTrigger;
import org.apache.maven.continuum.xmlrpc.ContinuumService;
import org.apache.maven.continuum.xmlrpc.project.AddingResult;
import org.apache.maven.continuum.xmlrpc.project.BuildAgentConfiguration;
import org.apache.maven.continuum.xmlrpc.project.BuildAgentGroupConfiguration;
import org.apache.maven.continuum.xmlrpc.project.BuildDefinition;
import org.apache.maven.continuum.xmlrpc.project.BuildDefinitionTemplate;
import org.apache.maven.continuum.xmlrpc.project.BuildProjectTask;
import org.apache.maven.continuum.xmlrpc.project.BuildResult;
import org.apache.maven.continuum.xmlrpc.project.BuildResultSummary;
import org.apache.maven.continuum.xmlrpc.project.ContinuumProjectState;
import org.apache.maven.continuum.xmlrpc.project.Project;
import org.apache.maven.continuum.xmlrpc.project.ProjectGroup;
import org.apache.maven.continuum.xmlrpc.project.ProjectGroupSummary;
import org.apache.maven.continuum.xmlrpc.project.ProjectNotifier;
import org.apache.maven.continuum.xmlrpc.project.ProjectScmRoot;
import org.apache.maven.continuum.xmlrpc.project.ProjectSummary;
import org.apache.maven.continuum.xmlrpc.project.ReleaseListenerSummary;
import org.apache.maven.continuum.xmlrpc.project.Schedule;
import org.apache.maven.continuum.xmlrpc.system.Installation;
import org.apache.maven.continuum.xmlrpc.system.Profile;
import org.apache.maven.continuum.xmlrpc.system.SystemConfiguration;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcCommonsTransportFactory;
import org.apache.xmlrpc.client.util.ClientFactory;

import java.net.URL;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 */
public class ContinuumXmlRpcClient
    implements ContinuumService
{
    private final ContinuumService continuum;

    private static Hashtable<Integer, String> statusMap;

    static
    {
        statusMap = new Hashtable<Integer, String>();
        statusMap.put( ContinuumProjectState.NEW, "New" );
        statusMap.put( ContinuumProjectState.CHECKEDOUT, "New" );
        statusMap.put( ContinuumProjectState.OK, "OK" );
        statusMap.put( ContinuumProjectState.FAILED, "Failed" );
        statusMap.put( ContinuumProjectState.ERROR, "Error" );
        statusMap.put( ContinuumProjectState.BUILDING, "Building" );
        statusMap.put( ContinuumProjectState.CHECKING_OUT, "Checking out" );
        statusMap.put( ContinuumProjectState.UPDATING, "Updating" );
        statusMap.put( ContinuumProjectState.WARNING, "Warning" );
    }

    public ContinuumXmlRpcClient( URL serviceUrl )
    {
        this( serviceUrl, null, null );
    }

    public ContinuumXmlRpcClient( URL serviceUrl, String login, String password )
    {
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl()
        {
            public boolean isEnabledForExtensions()
            {
                return true;
            }
        };

        if ( login != null )
        {
            config.setBasicUserName( login );
            config.setBasicPassword( password );
        }
        config.setServerURL( serviceUrl );

        XmlRpcClient client = new XmlRpcClient();
        client.setTransportFactory( new XmlRpcCommonsTransportFactory( client ) );
        client.setConfig( config );
        ClientFactory factory = new ClientFactory( client );
        continuum = (ContinuumService) factory.newInstance( ContinuumService.class );
    }

    public boolean ping()
        throws Exception
    {
        return continuum.ping();
    }

    // ----------------------------------------------------------------------
    // Projects
    // ----------------------------------------------------------------------

    public List<ProjectSummary> getProjects( int projectGroupId )
        throws Exception
    {
        return continuum.getProjects( projectGroupId );
    }

    public ProjectSummary getProjectSummary( int projectId )
        throws Exception
    {
        return continuum.getProjectSummary( projectId );
    }

    public Project getProjectWithAllDetails( int projectId )
        throws Exception
    {
        return continuum.getProjectWithAllDetails( projectId );
    }

    public int removeProject( int projectId )
        throws Exception
    {
        return continuum.removeProject( projectId );
    }

    public ProjectSummary updateProject( ProjectSummary project )
        throws Exception
    {
        return continuum.updateProject( project );
    }

    public ProjectSummary refreshProjectSummary( ProjectSummary project )
        throws Exception
    {
        if ( project == null )
        {
            return null;
        }
        return getProjectSummary( project.getId() );
    }

    public Project refreshProjectWithAllDetails( ProjectSummary project )
        throws Exception
    {
        if ( project == null )
        {
            return null;
        }
        return getProjectWithAllDetails( project.getId() );
    }

    // ----------------------------------------------------------------------
    // Projects Groups
    // ----------------------------------------------------------------------

    public List<ProjectGroupSummary> getAllProjectGroups()
        throws Exception
    {
        return continuum.getAllProjectGroups();
    }

    public List<ProjectGroup> getAllProjectGroupsWithAllDetails()
        throws Exception
    {
        return continuum.getAllProjectGroupsWithAllDetails();
    }

    /**
     * @deprecated
     */
    public List<ProjectGroup> getAllProjectGroupsWithProjects()
        throws Exception
    {
        return getAllProjectGroupsWithAllDetails();
    }

    public ProjectGroupSummary getProjectGroupSummary( int projectGroupId )
        throws Exception
    {
        return continuum.getProjectGroupSummary( projectGroupId );
    }

    public ProjectGroup getProjectGroupWithProjects( int projectGroupId )
        throws Exception
    {
        return continuum.getProjectGroupWithProjects( projectGroupId );
    }

    public int removeProjectGroup( int projectGroupId )
        throws Exception
    {
        return continuum.removeProjectGroup( projectGroupId );
    }

    public ProjectGroupSummary refreshProjectGroupSummary( ProjectGroupSummary projectGroup )
        throws Exception
    {
        if ( projectGroup == null )
        {
            return null;
        }
        return getProjectGroupSummary( projectGroup.getId() );
    }

    public ProjectGroup refreshProjectGroupSummaryWithProjects( ProjectGroupSummary projectGroup )
        throws Exception
    {
        if ( projectGroup == null )
        {
            return null;
        }
        return getProjectGroupWithProjects( projectGroup.getId() );
    }

    public ProjectGroupSummary updateProjectGroup( ProjectGroupSummary projectGroup )
        throws Exception
    {
        return continuum.updateProjectGroup( projectGroup );
    }

    public ProjectGroupSummary addProjectGroup( ProjectGroupSummary pg )
        throws Exception
    {
        return addProjectGroup( pg.getName(), pg.getGroupId(), pg.getDescription() );
    }

    public ProjectGroupSummary addProjectGroup( String groupName, String groupId, String description )
        throws Exception
    {
        return continuum.addProjectGroup( groupName, groupId, description );
    }

    // ----------------------------------------------------------------------
    // Build Definitions
    // ----------------------------------------------------------------------

    public List<BuildDefinition> getBuildDefinitionsForProject( int projectId )
        throws Exception
    {
        return continuum.getBuildDefinitionsForProject( projectId );
    }

    public List<BuildDefinition> getBuildDefinitionsForProjectGroup( int projectGroupId )
        throws Exception
    {
        return continuum.getBuildDefinitionsForProjectGroup( projectGroupId );
    }

    public BuildDefinition updateBuildDefinitionForProject( int projectId, BuildDefinition buildDef )
        throws Exception
    {
        return continuum.updateBuildDefinitionForProject( projectId, buildDef );
    }

    public BuildDefinition updateBuildDefinitionForProjectGroup( int projectGroupId, BuildDefinition buildDef )
        throws Exception
    {
        return continuum.updateBuildDefinitionForProjectGroup( projectGroupId, buildDef );
    }

    public int removeBuildDefinitionFromProjectGroup( int projectGroupId, int buildDefinitionId )
        throws Exception
    {
        return continuum.removeBuildDefinitionFromProjectGroup( projectGroupId, buildDefinitionId );
    }

    public BuildDefinition addBuildDefinitionToProject( int projectId, BuildDefinition buildDef )
        throws Exception
    {
        return continuum.addBuildDefinitionToProject( projectId, buildDef );
    }

    public BuildDefinition addBuildDefinitionToProjectGroup( int projectGroupId, BuildDefinition buildDef )
        throws Exception
    {
        return continuum.addBuildDefinitionToProjectGroup( projectGroupId, buildDef );
    }

    public List<BuildDefinitionTemplate> getBuildDefinitionTemplates()
        throws Exception
    {
        return continuum.getBuildDefinitionTemplates();
    }

    // ----------------------------------------------------------------------
    // Building
    // ----------------------------------------------------------------------

    public int addProjectToBuildQueue( int projectId )
        throws Exception
    {
        return continuum.addProjectToBuildQueue( projectId );
    }

    public int addProjectToBuildQueue( int projectId, int buildDefinitionId )
        throws Exception
    {
        return continuum.addProjectToBuildQueue( projectId, buildDefinitionId );
    }

    public int buildProject( int projectId )
        throws Exception
    {
        return continuum.buildProject( projectId );
    }

    public int buildProject( int projectId, int buildDefinitionId )
        throws Exception
    {
        return continuum.buildProject( projectId, buildDefinitionId );
    }

    public int buildProject( int projectId, BuildTrigger buildTrigger )
        throws Exception
    {
        return continuum.buildProject( projectId, buildTrigger );
    }

    public int buildProject( int projectId, int buildDefinitionId, BuildTrigger buildTrigger )
        throws Exception
    {
        return continuum.buildProject( projectId, buildDefinitionId, buildTrigger );
    }

    public int buildGroup( int projectGroupId )
        throws Exception
    {
        return continuum.buildGroup( projectGroupId );
    }

    public int buildGroup( int projectGroupId, int buildDefinitionId )
        throws Exception
    {
        return continuum.buildGroup( projectGroupId, buildDefinitionId );
    }

    // ----------------------------------------------------------------------
    // SCM roots
    // ----------------------------------------------------------------------

    public List<ProjectScmRoot> getProjectScmRootByProjectGroup( int projectGroupId )
        throws Exception
    {
        return continuum.getProjectScmRootByProjectGroup( projectGroupId );
    }

    public ProjectScmRoot getProjectScmRootByProject( int projectId )
        throws Exception
    {
        return continuum.getProjectScmRootByProject( projectId );
    }

    // ----------------------------------------------------------------------
    // Build Results
    // ----------------------------------------------------------------------

    public BuildResult getLatestBuildResult( int projectId )
        throws Exception
    {
        return continuum.getLatestBuildResult( projectId );
    }

    public BuildResult getBuildResult( int projectId, int buildId )
        throws Exception
    {
        return continuum.getBuildResult( projectId, buildId );
    }

    public List<BuildResultSummary> getBuildResultsForProject( int projectId )
        throws Exception
    {
        return continuum.getBuildResultsForProject( projectId );
    }

    public int removeBuildResult( BuildResult br )
        throws Exception
    {
        return continuum.removeBuildResult( br );
    }

    public String getBuildOutput( int projectId, int buildId )
        throws Exception
    {
        return continuum.getBuildOutput( projectId, buildId );
    }

    // ----------------------------------------------------------------------
    // Maven 2.x projects
    // ----------------------------------------------------------------------

    public AddingResult addMavenTwoProject( String url )
        throws Exception
    {
        return continuum.addMavenTwoProject( url );
    }

    public AddingResult addMavenTwoProject( String url, int projectGroupId )
        throws Exception
    {
        return continuum.addMavenTwoProject( url, projectGroupId );
    }

    public AddingResult addMavenTwoProject( String url, int projectGroupId, boolean checkoutInSingleDirectory )
        throws Exception
    {
        return continuum.addMavenTwoProject( url, projectGroupId, checkoutInSingleDirectory );
    }

    public AddingResult addMavenTwoProjectAsSingleProject( String url, int projectGroupId )
        throws Exception
    {
        return continuum.addMavenTwoProjectAsSingleProject( url, projectGroupId );
    }

    public AddingResult addMavenTwoProject( String url, int projectGroupId, boolean checkProtocol,
                                            boolean useCredentialsCache, boolean recursiveProjects,
                                            boolean checkoutInSingleDirectory )
        throws Exception
    {
        return continuum.addMavenTwoProject( url, projectGroupId, checkProtocol, useCredentialsCache, recursiveProjects,
                                             checkoutInSingleDirectory );
    }

    // ----------------------------------------------------------------------
    // Maven 1.x projects
    // ----------------------------------------------------------------------

    public AddingResult addMavenOneProject( String url, int projectGroupId )
        throws Exception
    {
        return continuum.addMavenOneProject( url, projectGroupId );
    }

    // ----------------------------------------------------------------------
    // Maven ANT projects
    // ----------------------------------------------------------------------

    public ProjectSummary addAntProject( ProjectSummary project, int projectGroupId )
        throws Exception
    {
        return continuum.addAntProject( project, projectGroupId );
    }

    // ----------------------------------------------------------------------
    // Maven Shell projects
    // ----------------------------------------------------------------------

    public ProjectSummary addShellProject( ProjectSummary project, int projectGroupId )
        throws Exception
    {
        return continuum.addShellProject( project, projectGroupId );
    }

    // ----------------------------------------------------------------------
    // Schedules
    // ----------------------------------------------------------------------

    public List<Schedule> getSchedules()
        throws Exception
    {
        return continuum.getSchedules();
    }

    public Schedule getSchedule( int scheduleId )
        throws Exception
    {
        return continuum.getSchedule( scheduleId );
    }

    public Schedule addSchedule( Schedule schedule )
        throws Exception
    {
        return continuum.addSchedule( schedule );
    }

    public Schedule updateSchedule( Schedule schedule )
        throws Exception
    {
        return continuum.updateSchedule( schedule );
    }

    // ----------------------------------------------------------------------
    // Profiles
    // ----------------------------------------------------------------------

    public List<Profile> getProfiles()
        throws Exception
    {
        return continuum.getProfiles();
    }

    public Profile getProfile( int profileId )
        throws Exception
    {
        return continuum.getProfile( profileId );
    }

    public Profile getProfileWithName( String profileName )
        throws Exception
    {
        return continuum.getProfileWithName( profileName );
    }

    // ----------------------------------------------------------------------
    // Installations
    // ----------------------------------------------------------------------

    public List<Installation> getInstallations()
        throws Exception
    {
        return continuum.getInstallations();
    }

    public Installation getInstallation( int installationId )
        throws Exception
    {
        return continuum.getInstallation( installationId );
    }

    public Installation getInstallation( String installationName )
        throws Exception
    {
        return continuum.getInstallation( installationName );
    }

    public List<Installation> getBuildAgentInstallations( String url )
        throws Exception
    {
        return continuum.getBuildAgentInstallations( url );
    }

    // ----------------------------------------------------------------------
    // SystemConfiguration
    // ----------------------------------------------------------------------

    public SystemConfiguration getSystemConfiguration()
        throws Exception
    {
        return continuum.getSystemConfiguration();
    }

    // ----------------------------------------------------------------------
    // Utils
    // ----------------------------------------------------------------------

    public String getProjectStatusAsString( int status )
    {
        return statusMap.get( new Integer( status ) );
    }

    // ----------------------------------------------------------------------
    // Queue
    // ----------------------------------------------------------------------
    public boolean isProjectInPrepareBuildQueue( int projectId )
        throws Exception
    {
        return continuum.isProjectInPrepareBuildQueue( projectId );
    }

    public boolean isProjectInPrepareBuildQueue( int projectId, int buildDefinitionId )
        throws Exception
    {
        return continuum.isProjectInPrepareBuildQueue( projectId, buildDefinitionId );
    }

    public List<BuildProjectTask> getProjectsInBuildQueue()
        throws Exception
    {
        return continuum.getProjectsInBuildQueue();
    }

    public boolean isProjectInBuildingQueue( int projectId )
        throws Exception
    {
        return continuum.isProjectInBuildingQueue( projectId );
    }

    public boolean isProjectInBuildingQueue( int projectId, int buildDefinitionId )
        throws Exception
    {
        return continuum.isProjectInBuildingQueue( projectId, buildDefinitionId );
    }

    public boolean isProjectCurrentlyPreparingBuild( int projectId )
        throws Exception
    {
        return continuum.isProjectCurrentlyPreparingBuild( projectId );
    }

    public boolean isProjectCurrentlyPreparingBuild( int projectId, int buildDefinitionId )
        throws Exception
    {
        return continuum.isProjectCurrentlyPreparingBuild( projectId, buildDefinitionId );
    }

    public boolean isProjectCurrentlyBuilding( int projectId )
        throws Exception
    {
        return continuum.isProjectCurrentlyBuilding( projectId );
    }

    public boolean isProjectCurrentlyBuilding( int projectId, int buildDefinitionId )
        throws Exception
    {
        return continuum.isProjectCurrentlyBuilding( projectId, buildDefinitionId );
    }

    public int removeProjectsFromBuildingQueue( int[] projectsId )
        throws Exception
    {
        return continuum.removeProjectsFromBuildingQueue( projectsId );
    }

    public boolean cancelCurrentBuild()
        throws Exception
    {
        return continuum.cancelCurrentBuild();
    }

    public boolean cancelBuild( int projectId, int buildDefinitionId )
        throws Exception
    {
        return continuum.cancelBuild( projectId, buildDefinitionId );
    }

    // ----------------------------------------------------------------------
    // Release Result
    // ----------------------------------------------------------------------

    public ContinuumReleaseResult getReleaseResult( int releaseId )
        throws Exception
    {
        return continuum.getReleaseResult( releaseId );
    }

    public List<ContinuumReleaseResult> getReleaseResultsForProjectGroup( int projectGroupId )
        throws Exception
    {
        return continuum.getReleaseResultsForProjectGroup( projectGroupId );
    }

    public int removeReleaseResult( ContinuumReleaseResult releaseResult )
        throws Exception
    {
        return continuum.removeReleaseResult( releaseResult );
    }

    public String getReleaseOutput( int releaseId )
        throws Exception
    {
        return continuum.getReleaseOutput( releaseId );
    }

    // ----------------------------------------------------------------------
    // Purge Configuration
    // ----------------------------------------------------------------------

    public RepositoryPurgeConfiguration addRepositoryPurgeConfiguration( RepositoryPurgeConfiguration repoPurge )
        throws Exception
    {
        return continuum.addRepositoryPurgeConfiguration( repoPurge );
    }

    public int updateRepositoryPurgeConfiguration( RepositoryPurgeConfiguration repoPurge )
        throws Exception
    {
        return continuum.updateRepositoryPurgeConfiguration( repoPurge );
    }

    public int removeRepositoryPurgeConfiguration( int repoPurgeId )
        throws Exception
    {
        return continuum.removeRepositoryPurgeConfiguration( repoPurgeId );
    }

    public RepositoryPurgeConfiguration getRepositoryPurgeConfiguration( int repoPurgeId )
        throws Exception
    {
        return continuum.getRepositoryPurgeConfiguration( repoPurgeId );
    }

    public List<RepositoryPurgeConfiguration> getAllRepositoryPurgeConfigurations()
        throws Exception
    {
        return continuum.getAllRepositoryPurgeConfigurations();
    }

    public DirectoryPurgeConfiguration addDirectoryPurgeConfiguration( DirectoryPurgeConfiguration dirPurge )
        throws Exception
    {
        return continuum.addDirectoryPurgeConfiguration( dirPurge );
    }

    public int updateDirectoryPurgeConfiguration( DirectoryPurgeConfiguration dirPurge )
        throws Exception
    {
        return continuum.updateDirectoryPurgeConfiguration( dirPurge );
    }

    public int removeDirectoryPurgeConfiguration( int dirPurgeId )
        throws Exception
    {
        return continuum.removeDirectoryPurgeConfiguration( dirPurgeId );
    }

    public DirectoryPurgeConfiguration getDirectoryPurgeConfiguration( int dirPurgeId )
        throws Exception
    {
        return continuum.getDirectoryPurgeConfiguration( dirPurgeId );
    }

    public List<DirectoryPurgeConfiguration> getAllDirectoryPurgeConfigurations()
        throws Exception
    {
        return continuum.getAllDirectoryPurgeConfigurations();
    }

    public int purgeLocalRepository( int repoPurgeId )
        throws Exception
    {
        return continuum.purgeLocalRepository( repoPurgeId );
    }

    public int purgeDirectory( int dirPurgeId )
        throws Exception
    {
        return continuum.purgeDirectory( dirPurgeId );
    }

    // ----------------------------------------------------------------------
    // Local Repository
    // ----------------------------------------------------------------------

    public LocalRepository addLocalRepository( LocalRepository repository )
        throws Exception
    {
        return continuum.addLocalRepository( repository );
    }

    public int updateLocalRepository( LocalRepository repository )
        throws Exception
    {
        return continuum.updateLocalRepository( repository );
    }

    public int removeLocalRepository( int repositoryId )
        throws Exception
    {
        return continuum.removeLocalRepository( repositoryId );
    }

    public LocalRepository getLocalRepository( int repositoryId )
        throws Exception
    {
        return continuum.getLocalRepository( repositoryId );
    }

    public List<LocalRepository> getAllLocalRepositories()
        throws Exception
    {
        return continuum.getAllLocalRepositories();
    }

    // ----------------------------------------------------------------------
    // ConfigurationService
    // ----------------------------------------------------------------------

    public BuildAgentConfiguration addBuildAgent( BuildAgentConfiguration buildAgentConfiguration )
        throws Exception
    {
        return continuum.addBuildAgent( buildAgentConfiguration );
    }

    public BuildAgentConfiguration getBuildAgent( String url )

    {
        return continuum.getBuildAgent( url );
    }

    public BuildAgentConfiguration updateBuildAgent( BuildAgentConfiguration buildAgentConfiguration )
        throws Exception

    {
        return continuum.updateBuildAgent( buildAgentConfiguration );
    }

    public boolean removeBuildAgent( String url )
        throws Exception

    {
        return continuum.removeBuildAgent( url );
    }

    public List<BuildAgentConfiguration> getAllBuildAgents()
    {
        return continuum.getAllBuildAgents();
    }

    public Map<String, Object> addAntProjectRPC( Map<String, Object> project, int projectGroupId )
        throws Exception
    {
        return continuum.addAntProjectRPC( project, projectGroupId );
    }

    public Map<String, Object> addBuildDefinitionToProjectGroupRPC( int projectGroupId, Map<String, Object> buildDef )
        throws Exception
    {
        return continuum.addBuildDefinitionToProjectGroupRPC( projectGroupId, buildDef );
    }

    public Map<String, Object> addBuildDefinitionToProjectRPC( int projectId, Map<String, Object> buildDef )
        throws Exception
    {
        return continuum.addBuildDefinitionToProjectRPC( projectId, buildDef );
    }

    public Map<String, Object> addMavenOneProjectRPC( String url, int projectGroupId )
        throws Exception
    {
        return continuum.addMavenOneProjectRPC( url, projectGroupId );
    }

    public Map<String, Object> addMavenTwoProjectRPC( String url )
        throws Exception
    {
        return continuum.addMavenTwoProjectRPC( url );
    }

    public Map<String, Object> addMavenTwoProjectRPC( String url, int projectGroupId )
        throws Exception
    {
        return continuum.addMavenTwoProjectRPC( url, projectGroupId );
    }

    public Map<String, Object> addMavenTwoProjectRPC( String url, int projectGroupId,
                                                      boolean checkoutInSingleDirectory )
        throws Exception
    {
        return continuum.addMavenTwoProjectRPC( url, projectGroupId, checkoutInSingleDirectory );
    }

    public Map<String, Object> addMavenTwoProjectAsSingleProjectRPC( String url, int projectGroupId )
        throws Exception
    {
        return continuum.addMavenTwoProjectAsSingleProjectRPC( url, projectGroupId );
    }

    public Map<String, Object> addMavenTwoProjectRPC( String url, int projectGroupId, boolean checkProtocol,
                                                      boolean useCredentialsCache, boolean recursiveProjects,
                                                      boolean checkoutInSingleDirectory )
        throws Exception
    {
        return continuum.addMavenTwoProjectRPC( url, projectGroupId, checkProtocol, useCredentialsCache,
                                                recursiveProjects, checkoutInSingleDirectory );
    }

    public Map<String, Object> addProjectGroupRPC( String groupName, String groupId, String description )
        throws Exception
    {
        return continuum.addProjectGroupRPC( groupName, groupId, description );
    }

    public Map<String, Object> addScheduleRPC( Map<String, Object> schedule )
        throws Exception
    {
        return continuum.addScheduleRPC( schedule );
    }

    public Map<String, Object> addShellProjectRPC( Map<String, Object> project, int projectGroupId )
        throws Exception
    {
        return continuum.addShellProjectRPC( project, projectGroupId );
    }

    public List<Object> getAllProjectGroupsRPC()
        throws Exception
    {
        return continuum.getAllProjectGroupsRPC();
    }

    public List<Object> getAllProjectGroupsWithAllDetailsRPC()
        throws Exception
    {
        return continuum.getAllProjectGroupsWithAllDetailsRPC();
    }

    public List<Object> getBuildDefinitionTemplatesRPC()
        throws Exception
    {
        return continuum.getBuildDefinitionTemplatesRPC();
    }

    public List<Object> getBuildDefinitionsForProjectGroupRPC( int projectGroupId )
        throws Exception
    {
        return continuum.getBuildDefinitionsForProjectGroupRPC( projectGroupId );
    }

    public List<Object> getBuildDefinitionsForProjectRPC( int projectId )
        throws Exception
    {
        return continuum.getBuildDefinitionsForProjectRPC( projectId );
    }

    public Map<String, Object> getBuildResultRPC( int projectId, int buildId )
        throws Exception
    {
        return continuum.getBuildResultRPC( projectId, buildId );
    }

    public List<Object> getBuildResultsForProjectRPC( int projectId )
        throws Exception
    {
        return continuum.getBuildResultsForProjectRPC( projectId );
    }

    public Map<String, Object> getInstallationRPC( int installationId )
        throws Exception
    {
        return continuum.getInstallationRPC( installationId );
    }

    public Map<String, Object> getInstallationRPC( String installationName )
        throws Exception
    {
        return continuum.getInstallationRPC( installationName );
    }

    public List<Object> getInstallationsRPC()
        throws Exception
    {
        return continuum.getInstallationsRPC();
    }

    public List<Object> getBuildAgentInstallationsRPC( String url )
        throws Exception
    {
        return continuum.getBuildAgentInstallationsRPC( url );
    }

    public Map<String, Object> getLatestBuildResultRPC( int projectId )
        throws Exception
    {
        return continuum.getLatestBuildResultRPC( projectId );
    }

    public Map<String, Object> getProfileRPC( int profileId )
        throws Exception
    {
        return continuum.getProfileRPC( profileId );
    }

    public Map<String, Object> getProfileWithNameRPC( String profileName )
        throws Exception
    {
        return continuum.getProfileWithNameRPC( profileName );
    }

    public List<Object> getProfilesRPC()
        throws Exception
    {
        return continuum.getProfilesRPC();
    }

    public Map<String, Object> getProjectGroupSummaryRPC( int projectGroupId )
        throws Exception
    {
        return continuum.getProjectGroupSummaryRPC( projectGroupId );
    }

    public Map<String, Object> getProjectGroupWithProjectsRPC( int projectGroupId )
        throws Exception
    {
        return continuum.getProjectGroupWithProjectsRPC( projectGroupId );
    }

    public Map<String, Object> updateProjectGroupRPC( Map<String, Object> projectGroup )
        throws Exception
    {
        return continuum.updateProjectGroupRPC( projectGroup );
    }

    public Map<String, Object> getProjectSummaryRPC( int projectId )
        throws Exception
    {
        return continuum.getProjectSummaryRPC( projectId );
    }

    public Map<String, Object> getProjectWithAllDetailsRPC( int projectId )
        throws Exception
    {
        return continuum.getProjectWithAllDetailsRPC( projectId );
    }

    public List<Object> getProjectsRPC( int projectGroupId )
        throws Exception
    {
        return continuum.getProjectsRPC( projectGroupId );
    }

    public Map<String, Object> getScheduleRPC( int scheduleId )
        throws Exception
    {
        return continuum.getScheduleRPC( scheduleId );
    }

    public List<Object> getSchedulesRPC()
        throws Exception
    {
        return continuum.getSchedulesRPC();
    }

    public Map<String, Object> getSystemConfigurationRPC()
        throws Exception
    {
        return continuum.getSystemConfigurationRPC();
    }

    public int removeBuildResultRPC( Map<String, Object> br )
        throws Exception
    {
        return continuum.removeBuildResultRPC( br );
    }

    public Map<String, Object> updateBuildDefinitionForProjectGroupRPC( int projectGroupId,
                                                                        Map<String, Object> buildDef )
        throws Exception
    {
        return continuum.updateBuildDefinitionForProjectGroupRPC( projectGroupId, buildDef );
    }

    public Map<String, Object> updateBuildDefinitionForProjectRPC( int projectId, Map<String, Object> buildDef )
        throws Exception
    {
        return continuum.updateBuildDefinitionForProjectRPC( projectId, buildDef );
    }

    public Map<String, Object> updateProjectRPC( Map<String, Object> project )
        throws Exception
    {
        return continuum.updateProjectRPC( project );
    }

    public Map<String, Object> updateScheduleRPC( Map<String, Object> schedule )
        throws Exception
    {
        return continuum.updateScheduleRPC( schedule );
    }

    public ProjectGroup getProjectGroup( int projectGroupId )
        throws Exception
    {
        return continuum.getProjectGroup( projectGroupId );
    }

    public Map<String, Object> getProjectGroupRPC( int projectGroupId )
        throws Exception
    {
        return continuum.getProjectGroupRPC( projectGroupId );
    }

    public ProjectNotifier getGroupNotifier( int projectgroupid, int notifierId )
        throws Exception
    {
        return continuum.getGroupNotifier( projectgroupid, notifierId );
    }

    public Map<String, Object> getGroupNotifierRPC( int projectgroupid, int notifierId )
        throws Exception
    {
        return continuum.getGroupNotifierRPC( projectgroupid, notifierId );
    }

    public ProjectNotifier getNotifier( int projectid, int notifierId )
        throws Exception
    {
        return continuum.getNotifier( projectid, notifierId );
    }

    public Map<String, Object> getNotifierRPC( int projectid, int notifierId )
        throws Exception
    {
        return continuum.getNotifierRPC( projectid, notifierId );
    }

    public ProjectNotifier updateGroupNotifier( int projectgroupid, ProjectNotifier newNotifier )
        throws Exception
    {
        return continuum.updateGroupNotifier( projectgroupid, newNotifier );
    }

    public Map<String, Object> updateGroupNotifierRPC( int projectgroupid, Map<String, Object> newNotifier )
        throws Exception
    {
        return continuum.updateGroupNotifierRPC( projectgroupid, newNotifier );
    }

    public ProjectNotifier updateNotifier( int projectid, ProjectNotifier newNotifier )
        throws Exception
    {
        return continuum.updateNotifier( projectid, newNotifier );
    }

    public Map<String, Object> updateNotifierRPC( int projectid, Map<String, Object> newNotifier )
        throws Exception
    {
        return continuum.updateNotifierRPC( projectid, newNotifier );
    }

    public int removeGroupNotifier( int projectgroupid, int notifierId )
        throws Exception
    {
        return continuum.removeGroupNotifier( projectgroupid, notifierId );
    }

    public int removeNotifier( int projectid, int notifierId )
        throws Exception
    {
        return continuum.removeNotifier( projectid, notifierId );
    }

    public ProjectNotifier addGroupNotifier( int projectgroupid, ProjectNotifier newNotifier )
        throws Exception
    {
        return continuum.addGroupNotifier( projectgroupid, newNotifier );
    }

    public Map<String, Object> addGroupNotifierRPC( int projectgroupid, Map<String, Object> newNotifier )
        throws Exception
    {
        return continuum.addGroupNotifierRPC( projectgroupid, newNotifier );
    }

    public ProjectNotifier addNotifier( int projectid, ProjectNotifier newNotifier )
        throws Exception
    {
        return continuum.addNotifier( projectid, newNotifier );
    }

    public Map<String, Object> addNotifierRPC( int projectid, Map<String, Object> newNotifier )
        throws Exception
    {
        return continuum.addNotifierRPC( projectid, newNotifier );
    }

    public Installation addInstallation( Installation installation )
        throws Exception
    {
        return continuum.addInstallation( installation );
    }

    public Map<String, Object> addInstallationRPC( Map<String, Object> installation )
        throws Exception
    {
        return continuum.addInstallationRPC( installation );
    }

    public Profile addProfile( Profile profile )
        throws Exception
    {
        return continuum.addProfile( profile );
    }

    public Map<String, Object> addProfileRPC( Map<String, Object> profile )
        throws Exception
    {
        return continuum.addProfileRPC( profile );
    }

    public int deleteInstallation( int installationId )
        throws Exception
    {
        return continuum.deleteInstallation( installationId );
    }

    public int deleteProfile( int profileId )
        throws Exception
    {
        return continuum.deleteProfile( profileId );
    }

    public int updateInstallation( Installation installation )
        throws Exception
    {
        return continuum.updateInstallation( installation );
    }

    public int updateInstallationRPC( Map<String, Object> installation )
        throws Exception
    {
        return continuum.updateInstallationRPC( installation );
    }

    public int updateProfile( Profile profile )
        throws Exception
    {
        return continuum.updateProfile( profile );
    }

    public int updateProfileRPC( Map<String, Object> profile )
        throws Exception
    {
        return continuum.updateProfileRPC( profile );
    }

    public Map<String, Object> getReleaseResultRPC( int releaseId )
        throws Exception
    {
        return continuum.getReleaseResultRPC( releaseId );
    }

    public List<Object> getReleaseResultsForProjectGroupRPC( int projectGroupId )
        throws Exception
    {
        return continuum.getReleaseResultsForProjectGroupRPC( projectGroupId );
    }

    public int removeReleaseResultRPC( Map<String, Object> rr )
        throws Exception
    {
        return continuum.removeReleaseResultRPC( rr );
    }

    public Map<String, Object> addRepositoryPurgeConfigurationRPC( Map<String, Object> repoPurge )
        throws Exception
    {
        return continuum.addRepositoryPurgeConfigurationRPC( repoPurge );
    }

    public int updateRepositoryPurgeConfigurationRPC( Map<String, Object> repoPurge )
        throws Exception
    {
        return continuum.updateRepositoryPurgeConfigurationRPC( repoPurge );
    }

    public Map<String, Object> getRepositoryPurgeConfigurationRPC( int repoPurgeId )
        throws Exception
    {
        return continuum.getRepositoryPurgeConfigurationRPC( repoPurgeId );
    }

    public List<Object> getAllRepositoryPurgeConfigurationsRPC()
        throws Exception
    {
        return continuum.getAllRepositoryPurgeConfigurationsRPC();
    }

    public Map<String, Object> addDirectoryPurgeConfigurationRPC( Map<String, Object> dirPurge )
        throws Exception
    {
        return continuum.addDirectoryPurgeConfigurationRPC( dirPurge );
    }

    public int updateDirectoryPurgeConfigurationRPC( Map<String, Object> dirPurge )
        throws Exception
    {
        return continuum.updateDirectoryPurgeConfigurationRPC( dirPurge );
    }

    public Map<String, Object> getDirectoryPurgeConfigurationRPC( int dirPurgeId )
        throws Exception
    {
        return continuum.getDirectoryPurgeConfigurationRPC( dirPurgeId );
    }

    public List<Object> getAllDirectoryPurgeConfigurationsRPC()
        throws Exception
    {
        return continuum.getAllDirectoryPurgeConfigurationsRPC();
    }

    public Map<String, Object> addLocalRepositoryRPC( Map<String, Object> repository )
        throws Exception
    {
        return continuum.addLocalRepositoryRPC( repository );
    }

    public int updateLocalRepositoryRPC( Map<String, Object> repository )
        throws Exception
    {
        return continuum.updateLocalRepositoryRPC( repository );
    }

    public Map<String, Object> getLocalRepositoryRPC( int repositoryId )
        throws Exception
    {
        return continuum.getLocalRepositoryRPC( repositoryId );
    }

    public List<Object> getAllLocalRepositoriesRPC()
        throws Exception
    {
        return continuum.getAllLocalRepositoriesRPC();
    }

    public Map<String, Object> addBuildAgentRPC( Map<String, Object> buildAgentConfiguration )
        throws Exception
    {
        return continuum.addBuildAgentRPC( buildAgentConfiguration );
    }

    public Map<String, Object> getBuildAgentRPC( String url )

    {
        return continuum.getBuildAgentRPC( url );
    }

    public Map<String, Object> updateBuildAgentRPC( Map<String, Object> buildAgentConfiguration )
        throws Exception

    {
        return continuum.updateBuildAgentRPC( buildAgentConfiguration );
    }

    public List<Object> getAllBuildAgentsRPC()
    {
        return continuum.getAllBuildAgentsRPC();
    }

    public int releasePerform( int projectId, String releaseId, String goals, String arguments,
                               boolean useReleaseProfile, String repositoryName, String username )
        throws Exception
    {
        return continuum.releasePerform( projectId, releaseId, goals, arguments, useReleaseProfile, repositoryName,
                                         username );
    }

    public String releasePrepare( int projectId, Properties releaseProperties, Map<String, String> releaseVersions,
                                  Map<String, String> developmentVersions, Map<String, String> environments,
                                  String username )
        throws Exception
    {
        return continuum.releasePrepare( projectId, releaseProperties, releaseVersions, developmentVersions,
                                         environments, username );
    }

    public ReleaseListenerSummary getListener( int projectId, String releaseId )
        throws Exception
    {
        return continuum.getListener( projectId, releaseId );
    }

    public int releaseCleanup( int projectId, String releaseId )
        throws Exception
    {
        return continuum.releaseCleanup( projectId, releaseId );
    }

    public int releaseCleanup( int projectId, String releaseId, String releaseType )
        throws Exception
    {
        return continuum.releaseCleanup( projectId, releaseId, releaseType );
    }

    public int releaseRollback( int projectId, String releaseId )
        throws Exception
    {
        return continuum.releaseRollback( projectId, releaseId );
    }

    public Map<String, Object> getReleasePluginParameters( int projectId )
        throws Exception
    {
        return continuum.getReleasePluginParameters( projectId );
    }

    public List<Map<String, String>> getProjectReleaseAndDevelopmentVersions( int projectId, String pomFilename,
                                                                              boolean autoVersionSubmodules )
        throws Exception
    {
        return continuum.getProjectReleaseAndDevelopmentVersions( projectId, pomFilename, autoVersionSubmodules );
    }

    public boolean pingBuildAgent( String buildAgentUrl )
        throws Exception
    {
        return continuum.pingBuildAgent( buildAgentUrl );
    }

    public String getBuildAgentUrl( int projectId, int buildDefinitionId )
        throws Exception
    {
        return continuum.getBuildAgentUrl( projectId, buildDefinitionId );
    }

    public BuildDefinition getBuildDefinition( int buildDefinitionId )
        throws Exception
    {
        return continuum.getBuildDefinition( buildDefinitionId );
    }

    public Map<String, Object> getBuildDefinitionRPC( int buildDefinitionId )
        throws Exception
    {
        return continuum.getBuildDefinitionRPC( buildDefinitionId );
    }

    public BuildAgentGroupConfiguration addBuildAgentGroup( BuildAgentGroupConfiguration buildAgentGroup )
        throws Exception
    {
        return continuum.addBuildAgentGroup( buildAgentGroup );
    }

    public Map<String, Object> addBuildAgentGroupRPC( Map<String, Object> buildAgentGroup )
        throws Exception
    {
        return continuum.addBuildAgentGroupRPC( buildAgentGroup );
    }

    public BuildAgentGroupConfiguration getBuildAgentGroup( String name )
    {
        return continuum.getBuildAgentGroup( name );
    }

    public Map<String, Object> getBuildAgentGroupRPC( String name )
    {
        return continuum.getBuildAgentGroupRPC( name );
    }

    public BuildAgentGroupConfiguration updateBuildAgentGroup( BuildAgentGroupConfiguration buildAgentGroup )
        throws Exception
    {
        return continuum.updateBuildAgentGroup( buildAgentGroup );
    }

    public Map<String, Object> updateBuildAgentGroupRPC( Map<String, Object> buildAgentGroup )
        throws Exception
    {
        return continuum.updateBuildAgentGroupRPC( buildAgentGroup );
    }

    public int removeBuildAgentGroup( String name )
        throws Exception
    {
        return continuum.removeBuildAgentGroup( name );
    }

    public List<BuildAgentConfiguration> getBuildAgentsWithInstallations()
        throws Exception
    {
        return continuum.getBuildAgentsWithInstallations();
    }

    public List<Object> getBuildAgentsWithInstallationsRPC()
        throws Exception
    {
        return continuum.getBuildAgentsWithInstallationsRPC();
    }
}
