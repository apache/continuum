package org.apache.maven.continuum.xmlrpc.server;

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

import net.sf.dozer.util.mapping.DozerBeanMapperSingletonWrapper;
import net.sf.dozer.util.mapping.MapperIF;
import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.execution.ContinuumBuildExecutorConstants;
import org.apache.maven.continuum.installation.InstallationException;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.apache.maven.continuum.security.ContinuumRoleConstants;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.apache.maven.continuum.xmlrpc.project.AddingResult;
import org.apache.maven.continuum.xmlrpc.project.BuildDefinition;
import org.apache.maven.continuum.xmlrpc.project.BuildResult;
import org.apache.maven.continuum.xmlrpc.project.BuildResultSummary;
import org.apache.maven.continuum.xmlrpc.project.Project;
import org.apache.maven.continuum.xmlrpc.project.ProjectGroup;
import org.apache.maven.continuum.xmlrpc.project.ProjectGroupSummary;
import org.apache.maven.continuum.xmlrpc.project.ProjectSummary;
import org.apache.maven.continuum.xmlrpc.project.Schedule;
import org.apache.maven.continuum.xmlrpc.system.Installation;
import org.apache.maven.continuum.xmlrpc.system.Profile;
import org.apache.maven.continuum.xmlrpc.system.SystemConfiguration;
import org.codehaus.plexus.redback.authorization.AuthorizationException;
import org.codehaus.plexus.redback.role.RoleManager;
import org.codehaus.plexus.redback.role.RoleManagerException;
import org.codehaus.plexus.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 * @plexus.component role="org.apache.maven.continuum.xmlrpc.server.ContinuumXmlRpcComponent" role-hint="org.apache.maven.continuum.xmlrpc.ContinuumService"
 */
public class ContinuumServiceImpl
    extends AbstractContinuumSecureService
{
    private static MapperIF mapper = DozerBeanMapperSingletonWrapper.getInstance();

    /**
     * @plexus.requirement
     */
    private Continuum continuum;

    /**
     * @plexus.requirement role-hint="jdo"
     */
    private ContinuumStore store;

    /**
     * @plexus.requirement role-hint="default"
     */
    private RoleManager roleManager;

    public boolean ping()
        throws ContinuumException
    {
        return true;
    }

    // ----------------------------------------------------------------------
    // Projects
    // ----------------------------------------------------------------------

    public List getProjects( int projectGroupId )
        throws ContinuumException
    {
        checkViewProjectGroupAuthorization( getProjectGroupName( projectGroupId ) );

        List projectsList = new ArrayList();

        Collection projects = continuum.getProjectsInGroup( projectGroupId );
        if ( projects != null )
        {
            for ( Iterator i = projects.iterator(); i.hasNext(); )
            {
                org.apache.maven.continuum.model.project.Project project =
                    (org.apache.maven.continuum.model.project.Project) i.next();
                ProjectSummary ps = populateProjectSummary( project );
                projectsList.add( ps );
            }
        }
        return projectsList;
    }

    public ProjectSummary getProjectSummary( int projectId )
        throws ContinuumException
    {
        org.apache.maven.continuum.model.project.Project project = continuum.getProject( projectId );

        checkViewProjectGroupAuthorization( project.getProjectGroup().getName() );

        return populateProjectSummary( project );
    }

    public Project getProjectWithAllDetails( int projectId )
        throws ContinuumException
    {
        org.apache.maven.continuum.model.project.Project project = continuum.getProjectWithAllDetails( projectId );

        checkViewProjectGroupAuthorization( project.getProjectGroup().getName() );

        return populateProject( project );
    }


    public int removeProject( int projectId )
        throws ContinuumException
    {
        ProjectSummary ps = getProjectSummary( projectId );

        checkRemoveProjectFromGroupAuthorization( ps.getProjectGroup().getName() );

        continuum.removeProject( projectId );

        return 0;
    }

    public ProjectSummary updateProject( ProjectSummary project )
        throws ContinuumException
    {
        ProjectSummary ps = getProjectSummary( project.getId() );

        checkRemoveProjectFromGroupAuthorization( ps.getProjectGroup().getName() );

        org.apache.maven.continuum.model.project.Project p = continuum.getProject( project.getId() );

        p.setName( project.getName() );
        p.setVersion( project.getVersion() );
        p.setScmUrl( project.getScmUrl() );
        p.setScmUseCache( project.isScmUseCache() );
        p.setScmUsername( project.getScmUsername() );
        p.setScmPassword( project.getScmPassword() );
        p.setScmTag( project.getScmTag() );

        continuum.updateProject( p );

        return getProjectSummary( project.getId() );
    }

    // ----------------------------------------------------------------------
    // Projects Groups
    // ----------------------------------------------------------------------

    public List<ProjectGroupSummary> getAllProjectGroups()
        throws ContinuumException
    {
        Collection<org.apache.maven.continuum.model.project.ProjectGroup> pgList = continuum.getAllProjectGroups();
        List<ProjectGroupSummary> result = new ArrayList<ProjectGroupSummary>();
        for ( org.apache.maven.continuum.model.project.ProjectGroup projectGroup : pgList )
        {
            try
            {
                if ( isAuthorized( ContinuumRoleConstants.CONTINUUM_VIEW_GROUP_OPERATION, projectGroup.getName() ) )
                {
                    result.add( populateProjectGroupSummary( projectGroup ) );
                }
            }
            catch ( AuthorizationException e )
            {
                throw new ContinuumException( "error authorizing request." );
            }
        }
        return result;
    }

    public List<ProjectGroup> getAllProjectGroupsWithAllDetails()
        throws ContinuumException
    {
        Collection<org.apache.maven.continuum.model.project.ProjectGroup> pgList =
            continuum.getAllProjectGroupsWithBuildDetails();
        List<ProjectGroup> result = new ArrayList<ProjectGroup>();
        for ( org.apache.maven.continuum.model.project.ProjectGroup projectGroup : pgList )
        {
            try
            {
                if ( isAuthorized( ContinuumRoleConstants.CONTINUUM_VIEW_GROUP_OPERATION, projectGroup.getName() ) )
                {
                    result.add( populateProjectGroupWithAllDetails( projectGroup ) );
                }
            }
            catch ( AuthorizationException e )
            {
                throw new ContinuumException( "error authorizing request." );
            }
        }
        return result;
    }

    public List<ProjectGroup> getAllProjectGroupsWithProjects()
        throws ContinuumException
    {
        return getAllProjectGroupsWithAllDetails();
    }

    protected String getProjectGroupName( int projectGroupId )
        throws ContinuumException
    {
        ProjectGroupSummary pgs = getPGSummary( projectGroupId );
        return pgs.getName();
    }

    private ProjectGroupSummary getPGSummary( int projectGroupId )
        throws ContinuumException
    {
        org.apache.maven.continuum.model.project.ProjectGroup projectGroup =
            continuum.getProjectGroup( projectGroupId );
        return populateProjectGroupSummary( projectGroup );
    }

    public ProjectGroupSummary getProjectGroupSummary( int projectGroupId )
        throws ContinuumException
    {
        checkViewProjectGroupAuthorization( getProjectGroupName( projectGroupId ) );

        org.apache.maven.continuum.model.project.ProjectGroup projectGroup =
            continuum.getProjectGroup( projectGroupId );
        return populateProjectGroupSummary( projectGroup );
    }

    public ProjectGroup getProjectGroupWithProjects( int projectGroupId )
        throws ContinuumException
    {
        checkViewProjectGroupAuthorization( getProjectGroupName( projectGroupId ) );

        org.apache.maven.continuum.model.project.ProjectGroup projectGroup =
            continuum.getProjectGroupWithProjects( projectGroupId );
        return populateProjectGroupWithAllDetails( projectGroup );
    }

    public int removeProjectGroup( int projectGroupId )
        throws ContinuumException
    {
        checkRemoveProjectGroupAuthorization( getProjectGroupName( projectGroupId ) );

        continuum.removeProjectGroup( projectGroupId );
        return 0;
    }

    public ProjectGroupSummary updateProjectGroup( ProjectGroupSummary projectGroup )
        throws ContinuumException
    {
        if ( projectGroup == null )
        {
            return null;
        }

        checkModifyProjectGroupAuthorization( getProjectGroupName( projectGroup.getId() ) );

        if ( StringUtils.isEmpty( projectGroup.getName() ) )
        {
            throw new ContinuumException( "project group name is required" );
        }
        else if ( StringUtils.isEmpty( projectGroup.getName().trim() ) )
        {
            throw new ContinuumException( "project group name can't be spaces" );
        }

        org.apache.maven.continuum.model.project.ProjectGroup pg =
            continuum.getProjectGroupWithProjects( projectGroup.getId() );

        // need to administer roles since they are based off of this
        // todo convert everything like to work off of string keys
        if ( !projectGroup.getName().equals( pg.getName() ) )
        {
            try
            {
                roleManager.updateRole( "project-administrator", pg.getName(), projectGroup.getName() );
                roleManager.updateRole( "project-developer", pg.getName(), projectGroup.getName() );
                roleManager.updateRole( "project-user", pg.getName(), projectGroup.getName() );

                pg.setName( projectGroup.getName() );
            }
            catch ( RoleManagerException e )
            {
                throw new ContinuumException( "unable to rename the project group", e );
            }
        }

        pg.setDescription( projectGroup.getDescription() );

        continuum.updateProjectGroup( pg );
        return getProjectGroupSummary( projectGroup.getId() );
    }

    // ----------------------------------------------------------------------
    // Build Definitions
    // ----------------------------------------------------------------------

    public List getBuildDefinitionsForProject( int projectId )
        throws ContinuumException
    {
        ProjectSummary ps = getProjectSummary( projectId );

        checkViewProjectGroupAuthorization( ps.getProjectGroup().getName() );

        List bds = continuum.getBuildDefinitionsForProject( projectId );

        List result = new ArrayList();
        for ( Iterator i = bds.iterator(); i.hasNext(); )
        {
            result.add(
                populateBuildDefinition( (org.apache.maven.continuum.model.project.BuildDefinition) i.next() ) );
        }
        return result;
    }

    public List getBuildDefinitionsForProjectGroup( int projectGroupId )
        throws ContinuumException
    {
        checkViewProjectGroupAuthorization( getProjectGroupName( projectGroupId ) );

        List bds = continuum.getBuildDefinitionsForProjectGroup( projectGroupId );

        List result = new ArrayList();
        for ( Iterator i = bds.iterator(); i.hasNext(); )
        {
            result.add(
                populateBuildDefinition( (org.apache.maven.continuum.model.project.BuildDefinition) i.next() ) );
        }
        return result;
    }

    public BuildDefinition updateBuildDefinitionForProject( int projectId, BuildDefinition buildDef )
        throws ContinuumException
    {
        ProjectSummary ps = getProjectSummary( projectId );

        checkModifyProjectBuildDefinitionAuthorization( ps.getProjectGroup().getName() );

        org.apache.maven.continuum.model.project.BuildDefinition bd = populateBuildDefinition( buildDef );
        bd = continuum.updateBuildDefinitionForProject( projectId, bd );
        return populateBuildDefinition( bd );
    }

    public BuildDefinition updateBuildDefinitionForProjectGroup( int projectGroupId, BuildDefinition buildDef )
        throws ContinuumException
    {
        checkModifyGroupBuildDefinitionAuthorization( getProjectGroupName( projectGroupId ) );

        org.apache.maven.continuum.model.project.BuildDefinition bd = populateBuildDefinition( buildDef );
        bd = continuum.updateBuildDefinitionForProjectGroup( projectGroupId, bd );
        return populateBuildDefinition( bd );
    }

    public BuildDefinition addBuildDefinitionToProject( int projectId, BuildDefinition buildDef )
        throws ContinuumException
    {
        checkAddProjectBuildDefinitionAuthorization( getProjectSummary( projectId ).getProjectGroup().getName() );

        if ( buildDef.getSchedule() == null )
        {
            throw new ContinuumException( "The schedule can't be null." );
        }

        org.apache.maven.continuum.model.project.BuildDefinition bd = populateBuildDefinition( buildDef );
        bd = continuum.addBuildDefinitionToProject( projectId, bd );
        return populateBuildDefinition( bd );
    }

    public BuildDefinition addBuildDefinitionToProjectGroup( int projectGroupId, BuildDefinition buildDef )
        throws ContinuumException
    {
        checkAddGroupBuildDefinitionAuthorization( getPGSummary( projectGroupId ).getName() );

        if ( buildDef.getSchedule() == null )
        {
            throw new ContinuumException( "The schedule can't be null." );
        }

        org.apache.maven.continuum.model.project.BuildDefinition bd = populateBuildDefinition( buildDef );
        bd = continuum.addBuildDefinitionToProjectGroup( projectGroupId, bd );
        return populateBuildDefinition( bd );
    }

    // ----------------------------------------------------------------------
    // Building
    // ----------------------------------------------------------------------

    public int addProjectToBuildQueue( int projectId )
        throws ContinuumException
    {
        ProjectSummary ps = getProjectSummary( projectId );
        checkBuildProjectInGroupAuthorization( ps.getProjectGroup().getName() );

        continuum.buildProject( projectId, ContinuumProjectState.TRIGGER_SCHEDULED );
        return 0;
    }

    public int addProjectToBuildQueue( int projectId, int buildDefinitionId )
        throws ContinuumException
    {
        ProjectSummary ps = getProjectSummary( projectId );
        checkBuildProjectInGroupAuthorization( ps.getProjectGroup().getName() );

        continuum.buildProject( projectId, buildDefinitionId, ContinuumProjectState.TRIGGER_SCHEDULED );
        return 0;
    }

    public int buildProject( int projectId )
        throws ContinuumException
    {
        ProjectSummary ps = getProjectSummary( projectId );
        checkBuildProjectInGroupAuthorization( ps.getProjectGroup().getName() );

        continuum.buildProject( projectId );
        return 0;
    }

    public int buildProject( int projectId, int buildDefintionId )
        throws ContinuumException
    {
        ProjectSummary ps = getProjectSummary( projectId );
        checkBuildProjectInGroupAuthorization( ps.getProjectGroup().getName() );

        continuum.buildProjectWithBuildDefinition( projectId, buildDefintionId );
        return 0;
    }

    public int buildGroup( int projectGroupId )
        throws ContinuumException
    {
        ProjectGroupSummary pg = getProjectGroupSummary( projectGroupId );
        checkBuildProjectInGroupAuthorization( pg.getName() );

        continuum.buildProjectGroup( projectGroupId );

        return 0;
    }

    public int buildGroup( int projectGroupId, int buildDefintionId )
        throws ContinuumException
    {
        ProjectGroupSummary pg = getProjectGroupSummary( projectGroupId );
        checkBuildProjectInGroupAuthorization( pg.getName() );

        continuum.buildProjectGroupWithBuildDefinition( projectGroupId, buildDefintionId );

        return 0;
    }

    // ----------------------------------------------------------------------
    // Build Results
    // ----------------------------------------------------------------------

    public BuildResult getLatestBuildResult( int projectId )
        throws ContinuumException
    {
        ProjectSummary ps = getProjectSummary( projectId );
        checkViewProjectGroupAuthorization( ps.getProjectGroup().getName() );

        org.apache.maven.continuum.model.project.BuildResult buildResult =
            continuum.getLatestBuildResultForProject( projectId );

        return getBuildResult( projectId, buildResult.getId() );
    }

    public BuildResult getBuildResult( int projectId, int buildId )
        throws ContinuumException
    {
        ProjectSummary ps = getProjectSummary( projectId );
        checkViewProjectGroupAuthorization( ps.getProjectGroup().getName() );

        return populateBuildResult( continuum.getBuildResult( buildId ) );
    }

    public List<BuildResultSummary> getBuildResultsForProject( int projectId )
        throws ContinuumException
    {
        ProjectSummary ps = getProjectSummary( projectId );
        checkViewProjectGroupAuthorization( ps.getProjectGroup().getName() );

        List<BuildResultSummary> result = new ArrayList<BuildResultSummary>();
        Collection buildResults = continuum.getBuildResultsForProject( projectId );
        if ( buildResults != null )
        {
            for ( org.apache.maven.continuum.model.project.BuildResult buildResult : (List<org.apache.maven.continuum.model.project.BuildResult>) buildResults )
            {
                BuildResultSummary br = populateBuildResultSummary( buildResult );
                result.add( br );
            }
        }

        return result;
    }

    public int removeBuildResult( BuildResult br )
        throws ContinuumException
    {
        checkModifyProjectGroupAuthorization(
            getProjectSummary( br.getProject().getId() ).getProjectGroup().getName() );
        continuum.removeBuildResult( br.getId() );
        return 0;
    }

    public String getBuildOutput( int projectId, int buildId )
        throws ContinuumException
    {
        ProjectSummary ps = getProjectSummary( projectId );
        checkViewProjectGroupAuthorization( ps.getProjectGroup().getName() );

        return continuum.getBuildOutput( projectId, buildId );
    }

    // ----------------------------------------------------------------------
    // Maven 2.x projects
    // ----------------------------------------------------------------------

    public AddingResult addMavenTwoProject( String url )
        throws ContinuumException
    {
        checkAddProjectGroupAuthorization();

        ContinuumProjectBuildingResult result = continuum.addMavenTwoProject( url );
        return populateAddingResult( result );
    }

    public AddingResult addMavenTwoProject( String url, int projectGroupId )
        throws ContinuumException
    {
        checkAddProjectToGroupAuthorization( getProjectGroupName( projectGroupId ) );

        ContinuumProjectBuildingResult result = continuum.addMavenTwoProject( url, projectGroupId );
        return populateAddingResult( result );
    }

    // ----------------------------------------------------------------------
    // Maven 1.x projects
    // ----------------------------------------------------------------------

    public AddingResult addMavenOneProject( String url )
        throws ContinuumException
    {
        checkAddProjectGroupAuthorization();

        ContinuumProjectBuildingResult result = continuum.addMavenOneProject( url );
        return populateAddingResult( result );
    }

    public AddingResult addMavenOneProject( String url, int projectGroupId )
        throws ContinuumException
    {
        checkAddProjectToGroupAuthorization( getProjectGroupName( projectGroupId ) );

        ContinuumProjectBuildingResult result = continuum.addMavenOneProject( url, projectGroupId );
        return populateAddingResult( result );
    }

    // ----------------------------------------------------------------------
    // Maven ANT projects
    // ----------------------------------------------------------------------

    public ProjectSummary addAntProject( ProjectSummary project )
        throws ContinuumException
    {
        checkAddProjectGroupAuthorization();

        int projectId =
            continuum.addProject( populateProject( project ), ContinuumBuildExecutorConstants.ANT_BUILD_EXECUTOR );
        return getProjectSummary( projectId );
    }

    public ProjectSummary addAntProject( ProjectSummary project, int projectGroupId )
        throws ContinuumException
    {
        checkAddProjectGroupAuthorization();

        int projectId = continuum.addProject( populateProject( project ),
                                              ContinuumBuildExecutorConstants.ANT_BUILD_EXECUTOR, projectGroupId );
        return getProjectSummary( projectId );
    }

    // ----------------------------------------------------------------------
    // Maven Shell projects
    // ----------------------------------------------------------------------

    public ProjectSummary addShellProject( ProjectSummary project )
        throws ContinuumException
    {
        checkAddProjectGroupAuthorization();

        int projectId =
            continuum.addProject( populateProject( project ), ContinuumBuildExecutorConstants.SHELL_BUILD_EXECUTOR );
        return getProjectSummary( projectId );
    }

    public ProjectSummary addShellProject( ProjectSummary project, int projectGroupId )
        throws ContinuumException
    {
        checkAddProjectGroupAuthorization();

        int projectId = continuum.addProject( populateProject( project ),
                                              ContinuumBuildExecutorConstants.SHELL_BUILD_EXECUTOR, projectGroupId );
        return getProjectSummary( projectId );
    }

    // ----------------------------------------------------------------------
    // Schedules
    // ----------------------------------------------------------------------

    public List<Schedule> getSchedules()
        throws ContinuumException
    {
        Collection schedules = continuum.getSchedules();

        List<Schedule> s = new ArrayList<Schedule>();
        for ( Object schedule : schedules )
        {
            s.add( populateSchedule( (org.apache.maven.continuum.model.project.Schedule) schedule ) );
        }

        return s;
    }

    public Schedule getSchedule( int scheduleId )
        throws ContinuumException
    {
        return populateSchedule( continuum.getSchedule( scheduleId ) );
    }

    public Schedule updateSchedule( Schedule schedule )
        throws ContinuumException
    {
        checkManageSchedulesAuthorization();

        continuum.updateSchedule( populateSchedule( schedule ) );

        return populateSchedule( continuum.getScheduleByName( schedule.getName() ) );
    }

    public Schedule addSchedule( Schedule schedule )
        throws ContinuumException
    {
        checkManageSchedulesAuthorization();

        continuum.addSchedule( populateSchedule( schedule ) );

        return populateSchedule( continuum.getScheduleByName( schedule.getName() ) );
    }

    // ----------------------------------------------------------------------
    // Profiles
    // ----------------------------------------------------------------------

    public List<Profile> getProfiles()
        throws ContinuumException
    {
        Collection profiles = continuum.getProfileService().getAllProfiles();

        List<Profile> p = new ArrayList<Profile>();
        for ( Object profile : profiles )
        {
            p.add( populateProfile( (org.apache.maven.continuum.model.system.Profile) profile ) );
        }

        return p;
    }

    public Profile getProfile( int profileId )
        throws ContinuumException
    {
        return populateProfile( continuum.getProfileService().getProfile( profileId ) );
    }

    // ----------------------------------------------------------------------
    // Installations
    // ----------------------------------------------------------------------

    public List<Installation> getInstallations()
        throws ContinuumException
    {
        try
        {
            List<org.apache.maven.continuum.model.system.Installation> installs =
                continuum.getInstallationService().getAllInstallations();

            List<Installation> i = new ArrayList<Installation>();
            for ( Object install : installs )
            {
                i.add( populateInstallation( (org.apache.maven.continuum.model.system.Installation) install ) );
            }
            return i;
        }
        catch ( InstallationException e )
        {
            throw new ContinuumException( "Can't load installations", e );
        }
    }

    public Installation getInstallation( int installationId )
        throws ContinuumException
    {
        try
        {
            org.apache.maven.continuum.model.system.Installation install =
                continuum.getInstallationService().getInstallation( installationId );
            return populateInstallation( install );
        }
        catch ( InstallationException e )
        {
            throw new ContinuumException( "Can't load installations", e );
        }
    }

    // ----------------------------------------------------------------------
    // SystemConfiguration
    // ----------------------------------------------------------------------

    public SystemConfiguration getSystemConfiguration()
        throws ContinuumException
    {
        try
        {
            org.apache.maven.continuum.model.system.SystemConfiguration sysConf = store.getSystemConfiguration();
            return populateSystemConfiguration( sysConf );
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumException( "Can't get SystemConfiguration.", e );
        }
    }

    // ----------------------------------------------------------------------
    // Converters
    // ----------------------------------------------------------------------

    private ProjectSummary populateProjectSummary( org.apache.maven.continuum.model.project.Project project )
    {
        return (ProjectSummary) mapper.map( project, ProjectSummary.class );
    }

    private Project populateProject( org.apache.maven.continuum.model.project.Project project )
    {
        return (Project) mapper.map( project, Project.class );
    }

    private org.apache.maven.continuum.model.project.Project populateProject( ProjectSummary projectSummary )
    {
        if ( projectSummary == null )
        {
            return null;
        }

        org.apache.maven.continuum.model.project.Project project =
            new org.apache.maven.continuum.model.project.Project();
        project.setArtifactId( projectSummary.getArtifactId() );
        project.setBuildNumber( projectSummary.getBuildNumber() );
        project.setDescription( projectSummary.getDescription() );
        project.setExecutorId( projectSummary.getExecutorId() );
        project.setGroupId( projectSummary.getGroupId() );
        project.setId( projectSummary.getId() );
        project.setLatestBuildId( projectSummary.getLatestBuildId() );
        project.setName( projectSummary.getName() );
        project.setProjectGroup( populateProjectGroupSummary( projectSummary.getProjectGroup() ) );
        project.setScmTag( projectSummary.getScmTag() );
        project.setScmUrl( projectSummary.getScmUrl() );
        project.setScmUseCache( projectSummary.isScmUseCache() );
        project.setScmUsername( projectSummary.getScmUsername() );
        project.setState( projectSummary.getState() );
        project.setUrl( projectSummary.getUrl() );
        project.setVersion( projectSummary.getVersion() );
        project.setWorkingDirectory( projectSummary.getWorkingDirectory() );
        return project;
    }

    private ProjectGroupSummary populateProjectGroupSummary(
        org.apache.maven.continuum.model.project.ProjectGroup group )
    {
        return (ProjectGroupSummary) mapper.map( group, ProjectGroupSummary.class );
    }

    private org.apache.maven.continuum.model.project.ProjectGroup populateProjectGroupSummary(
        ProjectGroupSummary group )
    {
        if ( group == null )
        {
            return null;
        }

        org.apache.maven.continuum.model.project.ProjectGroup g =
            new org.apache.maven.continuum.model.project.ProjectGroup();
        g.setDescription( group.getDescription() );
        g.setGroupId( group.getGroupId() );
        g.setId( group.getId() );
        g.setName( group.getName() );
        return g;
    }

    private ProjectGroup populateProjectGroupWithAllDetails(
        org.apache.maven.continuum.model.project.ProjectGroup group )
    {
        return (ProjectGroup) mapper.map( group, ProjectGroup.class );
    }

    private BuildResultSummary populateBuildResultSummary(
        org.apache.maven.continuum.model.project.BuildResult buildResult )
    {
        return (BuildResultSummary) mapper.map( buildResult, BuildResultSummary.class );
    }

    private BuildResult populateBuildResult( org.apache.maven.continuum.model.project.BuildResult buildResult )
        throws ContinuumException
    {
        return (BuildResult) mapper.map( buildResult, BuildResult.class );
    }

    private AddingResult populateAddingResult( ContinuumProjectBuildingResult result )
    {
        if ( result == null )
        {
            return null;
        }
        AddingResult res = new AddingResult();

        if ( result.hasErrors() )
        {
            for ( Iterator i = result.getErrors().iterator(); i.hasNext(); )
            {
                String error = (String) i.next();
                res.addError( error );
            }
        }

        if ( result.getProjects() != null )
        {
            for ( Iterator i = result.getProjects().iterator(); i.hasNext(); )
            {
                org.apache.maven.continuum.model.project.Project project =
                    (org.apache.maven.continuum.model.project.Project) i.next();
                res.addProject( populateProjectSummary( project ) );
            }
        }

        if ( result.getProjectGroups() != null )
        {
            for ( Iterator i = result.getProjectGroups().iterator(); i.hasNext(); )
            {
                org.apache.maven.continuum.model.project.ProjectGroup projectGroup =
                    (org.apache.maven.continuum.model.project.ProjectGroup) i.next();
                res.addProjectGroup( populateProjectGroupSummary( projectGroup ) );
            }
        }

        return res;
    }

    private BuildDefinition populateBuildDefinition( org.apache.maven.continuum.model.project.BuildDefinition buildDef )
    {
        return (BuildDefinition) mapper.map( buildDef, BuildDefinition.class );
    }

    private org.apache.maven.continuum.model.project.BuildDefinition populateBuildDefinition( BuildDefinition buildDef )
    {
        if ( buildDef == null )
        {
            return null;
        }

        org.apache.maven.continuum.model.project.BuildDefinition bd =
            new org.apache.maven.continuum.model.project.BuildDefinition();
        bd.setArguments( buildDef.getArguments() );
        bd.setBuildFile( buildDef.getBuildFile() );
        bd.setBuildFresh( buildDef.isBuildFresh() );
        bd.setDefaultForProject( buildDef.isDefaultForProject() );
        bd.setGoals( buildDef.getGoals() );
        bd.setId( buildDef.getId() );
        bd.setProfile( populateProfile( buildDef.getProfile() ) );
        bd.setSchedule( populateSchedule( buildDef.getSchedule() ) );
        return bd;
    }

    private org.apache.maven.continuum.model.project.Schedule populateSchedule( Schedule schedule )
    {
        if ( schedule == null )
        {
            return null;
        }

        org.apache.maven.continuum.model.project.Schedule s = new org.apache.maven.continuum.model.project.Schedule();
        s.setActive( schedule.isActive() );
        s.setCronExpression( schedule.getCronExpression() );
        s.setDelay( schedule.getDelay() );
        s.setDescription( schedule.getDescription() );
        s.setId( schedule.getId() );
        s.setMaxJobExecutionTime( schedule.getMaxJobExecutionTime() );
        s.setName( schedule.getName() );
        return s;
    }

    private Schedule populateSchedule( org.apache.maven.continuum.model.project.Schedule schedule )
    {
        return (Schedule) mapper.map( schedule, Schedule.class );
    }

    private org.apache.maven.continuum.model.system.Profile populateProfile( Profile profile )
    {
        if ( profile == null )
        {
            return null;
        }

        org.apache.maven.continuum.model.system.Profile p = new org.apache.maven.continuum.model.system.Profile();
        p.setActive( profile.isActive() );
        p.setBuilder( populateInstallation( profile.getBuilder() ) );
        p.setBuildWithoutChanges( profile.isBuildWithoutChanges() );
        p.setDescription( profile.getDescription() );
        if ( profile.getEnvironmentVariables() != null )
        {
            List envs = new ArrayList();
            for ( Iterator i = profile.getEnvironmentVariables().iterator(); i.hasNext(); )
            {
                envs.add( populateInstallation( (Installation) i.next() ) );
            }
            p.setEnvironmentVariables( envs );
        }
        p.setId( profile.getId() );
        p.setJdk( populateInstallation( profile.getJdk() ) );
        p.setName( profile.getName() );
        p.setScmMode( profile.getScmMode() );
        return p;
    }

    private Profile populateProfile( org.apache.maven.continuum.model.system.Profile profile )
    {
        return (Profile) mapper.map( profile, Profile.class );
    }

    private org.apache.maven.continuum.model.system.Installation populateInstallation( Installation install )
    {
        if ( install == null )
        {
            return null;
        }

        org.apache.maven.continuum.model.system.Installation inst =
            new org.apache.maven.continuum.model.system.Installation();
        inst.setName( install.getName() );
        inst.setType( install.getType() );
        inst.setVarName( install.getVarName() );
        inst.setVarValue( install.getVarValue() );
        return inst;
    }

    private Installation populateInstallation( org.apache.maven.continuum.model.system.Installation install )
    {
        return (Installation) mapper.map( install, Installation.class );
    }

    private SystemConfiguration populateSystemConfiguration(
        org.apache.maven.continuum.model.system.SystemConfiguration sysConf )
    {
        return (SystemConfiguration) mapper.map( sysConf, SystemConfiguration.class );
    }
}
