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
import org.apache.maven.continuum.xmlrpc.project.BuildResult;
import org.apache.maven.continuum.xmlrpc.project.BuildResultSummary;
import org.apache.maven.continuum.xmlrpc.project.ContinuumProjectState;
import org.apache.maven.continuum.xmlrpc.project.Project;
import org.apache.maven.continuum.xmlrpc.project.ProjectGroup;
import org.apache.maven.continuum.xmlrpc.project.ProjectGroupSummary;
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
}
