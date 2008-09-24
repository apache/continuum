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

import org.apache.maven.continuum.xmlrpc.ContinuumService;
import org.apache.maven.continuum.xmlrpc.project.AddingResult;
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
import org.apache.maven.continuum.xmlrpc.project.ProjectSummary;
import org.apache.maven.continuum.xmlrpc.project.Schedule;
import org.apache.maven.continuum.xmlrpc.system.Installation;
import org.apache.maven.continuum.xmlrpc.system.Profile;
import org.apache.maven.continuum.xmlrpc.system.SystemConfiguration;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.util.ClientFactory;

import java.net.URL;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class ContinuumXmlRpcClient
    implements ContinuumService
{
    private ContinuumService continuum;

    private static Hashtable statusMap;

    static
    {
        statusMap = new Hashtable();
        statusMap.put( new Integer( ContinuumProjectState.NEW ), "New" );
        statusMap.put( new Integer( ContinuumProjectState.CHECKEDOUT ), "New" );
        statusMap.put( new Integer( ContinuumProjectState.OK ), "OK" );
        statusMap.put( new Integer( ContinuumProjectState.FAILED ), "Failed" );
        statusMap.put( new Integer( ContinuumProjectState.ERROR ), "Error" );
        statusMap.put( new Integer( ContinuumProjectState.BUILDING ), "Building" );
        statusMap.put( new Integer( ContinuumProjectState.CHECKING_OUT ), "Checking out" );
        statusMap.put( new Integer( ContinuumProjectState.UPDATING ), "Updating" );
        statusMap.put( new Integer( ContinuumProjectState.WARNING ), "Warning" );
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

    public int buildGroup( int projectGroupId )
        throws Exception, XmlRpcException
    {
        return continuum.buildGroup( projectGroupId );
    }

    public int buildGroup( int projectGroupId, int buildDefinitionId )
        throws Exception, XmlRpcException
    {
        return continuum.buildGroup( projectGroupId, buildDefinitionId );
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
        throws Exception, XmlRpcException
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

    // ----------------------------------------------------------------------
    // Maven 1.x projects
    // ----------------------------------------------------------------------

    public AddingResult addMavenOneProject( String url )
        throws Exception
    {
        return continuum.addMavenOneProject( url );
    }

    public AddingResult addMavenOneProject( String url, int projectGroupId )
        throws Exception
    {
        return continuum.addMavenOneProject( url, projectGroupId );
    }

    // ----------------------------------------------------------------------
    // Maven ANT projects
    // ----------------------------------------------------------------------

    public ProjectSummary addAntProject( ProjectSummary project )
        throws Exception
    {
        return continuum.addAntProject( project );
    }

    public ProjectSummary addAntProject( ProjectSummary project, int projectGroupId )
        throws Exception
    {
        return continuum.addAntProject( project, projectGroupId );
    }

    // ----------------------------------------------------------------------
    // Maven Shell projects
    // ----------------------------------------------------------------------

    public ProjectSummary addShellProject( ProjectSummary project )
        throws Exception
    {
        return continuum.addShellProject( project );
    }

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
        return (String) statusMap.get( new Integer( status ) );
    }

    // ----------------------------------------------------------------------
    // Queue
    // ----------------------------------------------------------------------

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

    public Map<String, Object> addAntProjectRPC( Map<String, Object> project )
        throws Exception
    {
        return continuum.addAntProjectRPC( project );
    }

    public Map<String, Object> addAntProjectRPC( Map<String, Object> project, int projectGroupId )
        throws Exception
    {
        return continuum.addAntProjectRPC( project, projectGroupId );
    }

    public Map<String, Object> addBuildDefinitionToProjectGroupRPC( int projectGroupId,
                                                                    Map<String, Object> buildDef )
        throws Exception
    {
        return continuum.addBuildDefinitionToProjectGroupRPC( projectGroupId, buildDef );
    }

    public Map<String, Object> addBuildDefinitionToProjectRPC( int projectId,
                                                               Map<String, Object> buildDef )
        throws Exception
    {
        return continuum.addBuildDefinitionToProjectRPC( projectId, buildDef );
    }

    public Map<String, Object> addMavenOneProjectRPC( String url )
        throws Exception
    {
        return continuum.addMavenOneProjectRPC( url );
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

    public Map<String, Object> addProjectGroupRPC( String groupName,
                                                   String groupId,
                                                   String description )
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

    public Map<String, Object> addShellProjectRPC( Map<String, Object> project )
        throws Exception
    {
        return continuum.addShellProjectRPC( project );
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

    public List<Object> getInstallationsRPC()
        throws Exception
    {
        return continuum.getInstallationsRPC();
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

    public Map<String, Object> updateBuildDefinitionForProjectRPC( int projectId,
                                                                   Map<String, Object> buildDef )
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

    public Map<String, Object> updateGroupNotifierRPC( int projectgroupid,
                                                       Map<String, Object> newNotifier )
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

    public Map<String, Object> addGroupNotifierRPC( int projectgroupid,
                                                    Map<String, Object> newNotifier )
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
}
