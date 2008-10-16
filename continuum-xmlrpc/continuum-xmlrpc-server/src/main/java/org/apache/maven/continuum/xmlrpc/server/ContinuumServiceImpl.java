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
import org.apache.continuum.dao.SystemConfigurationDao;
import org.apache.continuum.purge.ContinuumPurgeManagerException;
import org.apache.continuum.purge.PurgeConfigurationServiceException;
import org.apache.continuum.repository.RepositoryServiceException;
import org.apache.continuum.xmlrpc.release.ContinuumReleaseResult;
import org.apache.continuum.xmlrpc.repository.DirectoryPurgeConfiguration;
import org.apache.continuum.xmlrpc.repository.LocalRepository;
import org.apache.continuum.xmlrpc.repository.RepositoryPurgeConfiguration;
import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.execution.ContinuumBuildExecutorConstants;
import org.apache.maven.continuum.installation.InstallationException;
import org.apache.maven.continuum.profile.ProfileException;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.apache.maven.continuum.security.ContinuumRoleConstants;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.apache.maven.continuum.xmlrpc.project.AddingResult;
import org.apache.maven.continuum.xmlrpc.project.BuildDefinition;
import org.apache.maven.continuum.xmlrpc.project.BuildDefinitionTemplate;
import org.apache.maven.continuum.xmlrpc.project.BuildProjectTask;
import org.apache.maven.continuum.xmlrpc.project.BuildResult;
import org.apache.maven.continuum.xmlrpc.project.BuildResultSummary;
import org.apache.maven.continuum.xmlrpc.project.Project;
import org.apache.maven.continuum.xmlrpc.project.ProjectGroup;
import org.apache.maven.continuum.xmlrpc.project.ProjectGroupSummary;
import org.apache.maven.continuum.xmlrpc.project.ProjectNotifier;
import org.apache.maven.continuum.xmlrpc.project.ProjectSummary;
import org.apache.maven.continuum.xmlrpc.project.Schedule;
import org.apache.maven.continuum.xmlrpc.system.Installation;
import org.apache.maven.continuum.xmlrpc.system.Profile;
import org.apache.maven.continuum.xmlrpc.system.SystemConfiguration;
import org.codehaus.plexus.redback.authorization.AuthorizationException;
import org.codehaus.plexus.redback.role.RoleManager;
import org.codehaus.plexus.redback.role.RoleManagerException;
import org.codehaus.plexus.util.StringUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
     * @plexus.requirement
     */
    private SystemConfigurationDao systemConfigurationDao;

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

    public List<ProjectSummary> getProjects( int projectGroupId )
        throws ContinuumException
    {
        checkViewProjectGroupAuthorization( getProjectGroupName( projectGroupId ) );

        List<ProjectSummary> projectsList = new ArrayList<ProjectSummary>();

        Collection<org.apache.maven.continuum.model.project.Project> projects =
            continuum.getProjectsInGroup( projectGroupId );
        if ( projects != null )
        {
            for ( org.apache.maven.continuum.model.project.Project project : projects )
            {
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

    public ProjectGroup getProjectGroup( int projectGroupId )
        throws ContinuumException
    {
        ProjectGroup result = null;
        org.apache.maven.continuum.model.project.ProjectGroup projectGroup = continuum.getProjectGroup( projectGroupId );
        try
        {
            if ( isAuthorized( ContinuumRoleConstants.CONTINUUM_VIEW_GROUP_OPERATION, projectGroup.getName() ) )
            {
                result = populateProjectGroupWithAllDetails( projectGroup );
            }
        }
        catch ( AuthorizationException e )
        {
            throw new ContinuumException( "error authorizing request." );
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
        org.apache.maven.continuum.model.project.ProjectGroup projectGroup = continuum.getProjectGroup( projectGroupId );

        checkViewProjectGroupAuthorization( projectGroup.getName() );
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

        org.apache.continuum.model.repository.LocalRepository repo = 
            new org.apache.continuum.model.repository.LocalRepository();
        pg.setLocalRepository( populateLocalRepository( projectGroup.getLocalRepository(), repo ) );
        
        continuum.updateProjectGroup( pg );
        return getProjectGroupSummary( projectGroup.getId() );
    }

    public ProjectGroupSummary addProjectGroup( String groupName, String groupId, String description )
        throws Exception
    {
        org.apache.maven.continuum.model.project.ProjectGroup pg =
            new org.apache.maven.continuum.model.project.ProjectGroup();
        pg.setName( groupName );
        pg.setGroupId( groupId );
        pg.setDescription( description );
        continuum.addProjectGroup( pg );
        return populateProjectGroupSummary( continuum.getProjectGroupByGroupId( groupId ) );
    }

    public ProjectNotifier getNotifier( int projectid, int notifierId )
        throws ContinuumException
    {
        return populateProjectNotifier( continuum.getNotifier( projectid, notifierId ) );
    }

    public ProjectNotifier updateNotifier( int projectid, ProjectNotifier newNotifier )
        throws ContinuumException
    {

        org.apache.maven.continuum.model.project.ProjectNotifier notifier =
                        continuum.getNotifier( projectid, newNotifier.getId() );
        notifier.setConfiguration( newNotifier.getConfiguration() );
        notifier.setFrom( newNotifier.getFrom() );
        notifier.setRecipientType( newNotifier.getRecipientType() );
        notifier.setType( newNotifier.getType() );
        notifier.setEnabled( newNotifier.isEnabled() );
        notifier.setSendOnError( newNotifier.isSendOnError() );
        notifier.setSendOnFailure( newNotifier.isSendOnFailure() );
        notifier.setSendOnSuccess( newNotifier.isSendOnSuccess() );
        notifier.setSendOnWarning( newNotifier.isSendOnWarning() );
        return populateProjectNotifier( continuum.updateNotifier( projectid, notifier ) );
    }

    public ProjectNotifier addNotifier( int projectid, ProjectNotifier newNotifier )
        throws ContinuumException
    {

        org.apache.maven.continuum.model.project.ProjectNotifier notifier =
                        new org.apache.maven.continuum.model.project.ProjectNotifier();
        notifier.setConfiguration( newNotifier.getConfiguration() );
        notifier.setFrom( newNotifier.getFrom() );
        notifier.setRecipientType( newNotifier.getRecipientType() );
        notifier.setType( newNotifier.getType() );
        notifier.setEnabled( newNotifier.isEnabled() );
        notifier.setSendOnError( newNotifier.isSendOnError() );
        notifier.setSendOnFailure( newNotifier.isSendOnFailure() );
        notifier.setSendOnSuccess( newNotifier.isSendOnSuccess() );
        notifier.setSendOnWarning( newNotifier.isSendOnWarning() );
        return populateProjectNotifier( continuum.addNotifier( projectid, notifier ) );
    }

    public int removeNotifier( int projectid, int notifierId )
        throws ContinuumException
    {
        continuum.removeNotifier( projectid, notifierId );
        return 0;
    }

    public ProjectNotifier getGroupNotifier( int projectgroupid, int notifierId )
        throws ContinuumException
    {
        return populateProjectNotifier( continuum.getGroupNotifier( projectgroupid, notifierId ) );
    }

    public ProjectNotifier updateGroupNotifier( int projectgroupid, ProjectNotifier newNotifier )
        throws ContinuumException
    {

        org.apache.maven.continuum.model.project.ProjectNotifier notifier =
                        continuum.getGroupNotifier( projectgroupid, newNotifier.getId() );
        notifier.setConfiguration( newNotifier.getConfiguration() );
        notifier.setFrom( newNotifier.getFrom() );
        notifier.setRecipientType( newNotifier.getRecipientType() );
        notifier.setType( newNotifier.getType() );
        notifier.setEnabled( newNotifier.isEnabled() );
        notifier.setSendOnError( newNotifier.isSendOnError() );
        notifier.setSendOnFailure( newNotifier.isSendOnFailure() );
        notifier.setSendOnSuccess( newNotifier.isSendOnSuccess() );
        notifier.setSendOnWarning( newNotifier.isSendOnWarning() );
        return populateProjectNotifier( continuum.updateGroupNotifier( projectgroupid, notifier ) );
    }

    public ProjectNotifier addGroupNotifier( int projectgroupid, ProjectNotifier newNotifier )
        throws ContinuumException
    {
        org.apache.maven.continuum.model.project.ProjectNotifier notifier =
                        new org.apache.maven.continuum.model.project.ProjectNotifier();
        notifier.setConfiguration( newNotifier.getConfiguration() );
        notifier.setFrom( newNotifier.getFrom() );
        notifier.setRecipientType( newNotifier.getRecipientType() );
        notifier.setType( newNotifier.getType() );
        notifier.setEnabled( newNotifier.isEnabled() );
        notifier.setSendOnError( newNotifier.isSendOnError() );
        notifier.setSendOnFailure( newNotifier.isSendOnFailure() );
        notifier.setSendOnSuccess( newNotifier.isSendOnSuccess() );
        notifier.setSendOnWarning( newNotifier.isSendOnWarning() );
        return populateProjectNotifier( continuum.addGroupNotifier( projectgroupid, notifier ) );
    }

    public int removeGroupNotifier( int projectgroupid, int notifierId )
        throws ContinuumException
    {
        continuum.removeGroupNotifier( projectgroupid, notifierId );
        return 0;
    }

    // ----------------------------------------------------------------------
    // Build Definitions
    // ----------------------------------------------------------------------

    public List<BuildDefinition> getBuildDefinitionsForProject( int projectId )
        throws ContinuumException
    {
        ProjectSummary ps = getProjectSummary( projectId );

        checkViewProjectGroupAuthorization( ps.getProjectGroup().getName() );

        List<org.apache.maven.continuum.model.project.BuildDefinition> bds =
            continuum.getBuildDefinitionsForProject( projectId );

        List<BuildDefinition> result = new ArrayList<BuildDefinition>();
        for ( org.apache.maven.continuum.model.project.BuildDefinition bd : bds )
        {
            result.add( populateBuildDefinition( bd ) );
        }
        return result;
    }

    public List<BuildDefinition> getBuildDefinitionsForProjectGroup( int projectGroupId )
        throws ContinuumException
    {
        checkViewProjectGroupAuthorization( getProjectGroupName( projectGroupId ) );

        List<org.apache.maven.continuum.model.project.BuildDefinition> bds =
            continuum.getBuildDefinitionsForProjectGroup( projectGroupId );

        List<BuildDefinition> result = new ArrayList<BuildDefinition>();
        for ( org.apache.maven.continuum.model.project.BuildDefinition bd : bds )
        {
            result.add( populateBuildDefinition( bd ) );
        }
        return result;
    }

    public int removeBuildDefinitionFromProjectGroup( int projectGroupId, int buildDefinitionId )
        throws ContinuumException
    {
        checkRemoveGroupBuildDefinitionAuthorization( getProjectGroupName( projectGroupId ) );

        continuum.removeBuildDefinitionFromProjectGroup( projectGroupId, buildDefinitionId );
        return 0;
    }

    public BuildDefinition updateBuildDefinitionForProject( int projectId, BuildDefinition buildDef )
        throws ContinuumException
    {
        ProjectSummary ps = getProjectSummary( projectId );

        checkModifyProjectBuildDefinitionAuthorization( ps.getProjectGroup().getName() );
        org.apache.maven.continuum.model.project.BuildDefinition newbd =
            continuum.getBuildDefinition( buildDef.getId() );
        org.apache.maven.continuum.model.project.BuildDefinition bd = populateBuildDefinition( buildDef, newbd );
        bd = continuum.updateBuildDefinitionForProject( projectId, bd );
        return populateBuildDefinition( bd );
    }

    public BuildDefinition updateBuildDefinitionForProjectGroup( int projectGroupId, BuildDefinition buildDef )
        throws ContinuumException
    {
        checkModifyGroupBuildDefinitionAuthorization( getProjectGroupName( projectGroupId ) );
        org.apache.maven.continuum.model.project.BuildDefinition newbd =
            continuum.getBuildDefinition( buildDef.getId() );
        org.apache.maven.continuum.model.project.BuildDefinition bd = populateBuildDefinition( buildDef, newbd );
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
        org.apache.maven.continuum.model.project.BuildDefinition newbd =
            new org.apache.maven.continuum.model.project.BuildDefinition();
        org.apache.maven.continuum.model.project.BuildDefinition bd = populateBuildDefinition( buildDef, newbd );
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
        org.apache.maven.continuum.model.project.BuildDefinition newbd =
            new org.apache.maven.continuum.model.project.BuildDefinition();
        org.apache.maven.continuum.model.project.BuildDefinition bd = populateBuildDefinition( buildDef, newbd );
        bd = continuum.addBuildDefinitionToProjectGroup( projectGroupId, bd );
        return populateBuildDefinition( bd );
    }

    public List<BuildDefinitionTemplate> getBuildDefinitionTemplates()
        throws Exception
    {
        checkManageBuildDefinitionTemplatesAuthorization();
        List<org.apache.maven.continuum.model.project.BuildDefinitionTemplate> bdts =
            continuum.getBuildDefinitionService().getAllBuildDefinitionTemplate();

        List<BuildDefinitionTemplate> result = new ArrayList<BuildDefinitionTemplate>();
        for ( org.apache.maven.continuum.model.project.BuildDefinitionTemplate bdt : bdts )
        {
            result.add( populateBuildDefinitionTemplate( bdt ) );
        }
        return result;
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
        org.apache.maven.continuum.model.project.Project newProject =
                        new org.apache.maven.continuum.model.project.Project();
        int projectId =
            continuum.addProject( populateProject( project, newProject ), ContinuumBuildExecutorConstants.ANT_BUILD_EXECUTOR );
        return getProjectSummary( projectId );
    }

    public ProjectSummary addAntProject( ProjectSummary project, int projectGroupId )
        throws ContinuumException
    {
        checkAddProjectGroupAuthorization();
        org.apache.maven.continuum.model.project.Project newProject =
                        new org.apache.maven.continuum.model.project.Project();
        int projectId =
                        continuum.addProject( populateProject( project, newProject ),
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
        org.apache.maven.continuum.model.project.Project newProject =
                        new org.apache.maven.continuum.model.project.Project();
        int projectId =
                        continuum.addProject( populateProject( project, newProject ),
                            ContinuumBuildExecutorConstants.SHELL_BUILD_EXECUTOR );
        return getProjectSummary( projectId );
    }

    public ProjectSummary addShellProject( ProjectSummary project, int projectGroupId )
        throws ContinuumException
    {
        checkAddProjectGroupAuthorization();
        org.apache.maven.continuum.model.project.Project newProject =
                        new org.apache.maven.continuum.model.project.Project();
        int projectId =
                        continuum.addProject( populateProject( project, newProject ),
                            ContinuumBuildExecutorConstants.SHELL_BUILD_EXECUTOR, projectGroupId );
        return getProjectSummary( projectId );
    }

    // ----------------------------------------------------------------------
    // Schedules
    // ----------------------------------------------------------------------

    public List<Schedule> getSchedules()
        throws ContinuumException
    {
        checkManageSchedulesAuthorization();
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
        checkManageSchedulesAuthorization();
        return populateSchedule( continuum.getSchedule( scheduleId ) );
    }

    public Schedule updateSchedule( Schedule schedule )
        throws ContinuumException
    {
        checkManageSchedulesAuthorization();
        org.apache.maven.continuum.model.project.Schedule s = continuum.getSchedule( schedule.getId() );
        org.apache.maven.continuum.model.project.Schedule newSchedule = populateSchedule( schedule, s );
        org.apache.maven.continuum.model.project.Schedule storedSchedule = continuum.getSchedule( schedule.getId() );
        storedSchedule.setActive( newSchedule.isActive() );
        storedSchedule.setName( newSchedule.getName() );
        storedSchedule.setDescription( newSchedule.getDescription() );
        storedSchedule.setDelay( newSchedule.getDelay() );
        storedSchedule.setCronExpression( newSchedule.getCronExpression() );
        storedSchedule.setMaxJobExecutionTime( newSchedule.getMaxJobExecutionTime() );
        continuum.updateSchedule( storedSchedule );

        return populateSchedule( continuum.getScheduleByName( schedule.getName() ) );
    }

    public Schedule addSchedule( Schedule schedule )
        throws ContinuumException
    {
        checkManageSchedulesAuthorization();
        org.apache.maven.continuum.model.project.Schedule s = new org.apache.maven.continuum.model.project.Schedule();
        continuum.addSchedule( populateSchedule( schedule, s ) );

        return populateSchedule( continuum.getScheduleByName( schedule.getName() ) );
    }

    public int removeSchedule( int scheduleId )
        throws ContinuumException
    {
        checkManageSchedulesAuthorization();

        continuum.removeSchedule( scheduleId );

        return 0;
    }

    // ----------------------------------------------------------------------
    // Profiles
    // ----------------------------------------------------------------------

    public List<Profile> getProfiles()
        throws ContinuumException
    {
        checkManageProfilesAuthorization();
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
        checkManageProfilesAuthorization();
        return populateProfile( continuum.getProfileService().getProfile( profileId ) );
    }

    public Profile addProfile( Profile profile )
        throws ContinuumException
    {
        org.apache.maven.continuum.model.system.Profile newProfile =
                        new org.apache.maven.continuum.model.system.Profile();

        return populateProfile( continuum.getProfileService().addProfile( populateProfile( profile, newProfile ) ) );
    }

    public int updateProfile( Profile profile )
        throws ContinuumException
    {
        org.apache.maven.continuum.model.system.Profile newProfile =
                        continuum.getProfileService().getProfile( profile.getId() );

        continuum.getProfileService().updateProfile( populateProfile( profile, newProfile ) );
        return 0;
    }

    public int deleteProfile( int profileId )
        throws ContinuumException
    {

        continuum.getProfileService().deleteProfile( profileId );
        return 0;
    }

    // ----------------------------------------------------------------------
    // Installations
    // ----------------------------------------------------------------------

    public List<Installation> getInstallations()
        throws ContinuumException
    {
        checkManageInstallationsAuthorization();
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
        checkManageInstallationsAuthorization();
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

    public Installation addInstallation( Installation installation )
        throws ContinuumException
    {
        try
        {
            org.apache.maven.continuum.model.system.Installation newInstallation =
                            new org.apache.maven.continuum.model.system.Installation();
            return populateInstallation( continuum.getInstallationService().add(
                populateInstallation( installation, newInstallation ) ) );
        }
        catch ( InstallationException e )
        {
            throw new ContinuumException( "Can't delete installations", e );
        }
    }

    public int updateInstallation( Installation installation )
        throws ContinuumException
    {
        try
        {
            final org.apache.maven.continuum.model.system.Installation newInst =
                            continuum.getInstallationService().getInstallation( installation.getInstallationId() );
            continuum.getInstallationService().update( populateInstallation( installation, newInst ) );
            return 0;
        }
        catch ( InstallationException e )
        {
            throw new ContinuumException( "Can't delete installations", e );
        }
    }

    public int deleteInstallation( int installationId )
        throws ContinuumException
    {
        try
        {
            org.apache.maven.continuum.model.system.Installation installationTODelete =
                            continuum.getInstallationService().getInstallation( installationId );
            continuum.getInstallationService().delete( installationTODelete );
            return 0;
        }
        catch ( InstallationException e )
        {
            throw new ContinuumException( "Can't delete installations", e );
        }
    }

    // ----------------------------------------------------------------------
    // SystemConfigurationDao
    // ----------------------------------------------------------------------

    public SystemConfiguration getSystemConfiguration()
        throws ContinuumException
    {
        checkManageConfigurationAuthorization();
        try
        {
            org.apache.maven.continuum.model.system.SystemConfiguration sysConf =
                systemConfigurationDao.getSystemConfiguration();
            return populateSystemConfiguration( sysConf );
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumException( "Can't get SystemConfigurationDao.", e );
        }
    }

    // ----------------------------------------------------------------------
    // Queue
    // ----------------------------------------------------------------------


    public boolean isProjectInBuildingQueue( int projectId )
        throws ContinuumException
    {
        return continuum.isInBuildingQueue( projectId );
    }

    public List<BuildProjectTask> getProjectsInBuildQueue()
        throws ContinuumException
    {
        return populateBuildProjectTaskList( continuum.getProjectsInBuildQueue() );
    }

    public int removeProjectsFromBuildingQueue( int[] projectsId )
        throws ContinuumException
    {
        checkManageQueuesAuthorization();
        continuum.removeProjectsFromBuildingQueue( projectsId );
        return 0;
    }

    public boolean cancelCurrentBuild()
        throws ContinuumException
    {
        checkManageQueuesAuthorization();
        return continuum.cancelCurrentBuild();
    }

    // ----------------------------------------------------------------------
    // Release Results
    // ----------------------------------------------------------------------

    public ContinuumReleaseResult getReleaseResult( int releaseId )
        throws ContinuumException
    {
        org.apache.continuum.model.release.ContinuumReleaseResult releaseResult = continuum.getContinuumReleaseResult( releaseId );
        checkViewProjectGroupAuthorization( getProjectGroupName( releaseResult.getProjectGroup().getId() ) );
        return populateReleaseResult( releaseResult );
    }

    public List<ContinuumReleaseResult> getReleaseResultsForProjectGroup( int projectGroupId )
        throws ContinuumException
    {
        checkViewProjectGroupAuthorization( getProjectGroupName( projectGroupId ) );
        Collection releaseResults = continuum.getContinuumReleaseResultsByProjectGroup( projectGroupId );
        
        List<ContinuumReleaseResult> r = new ArrayList<ContinuumReleaseResult>();
        for ( Object releaseResult : releaseResults )
        {
            r.add( populateReleaseResult( (org.apache.continuum.model.release.ContinuumReleaseResult) releaseResult ) );
        }
        return r;
    }

    public int removeReleaseResult( ContinuumReleaseResult releaseResult )
        throws ContinuumException
    {
        checkModifyProjectGroupAuthorization( getProjectGroupName( releaseResult.getProjectGroup().getId() ) );
        continuum.removeContinuumReleaseResult( releaseResult.getId() );
        return 0;
    }

    public String getReleaseOutput( int releaseId )
        throws ContinuumException
    {
        org.apache.continuum.model.release.ContinuumReleaseResult releaseResult = continuum.getContinuumReleaseResult( releaseId );
        checkViewProjectGroupAuthorization( getProjectGroupName( releaseResult.getProjectGroup().getId() ) );

        return continuum.getReleaseOutput( releaseId );
    }

    // ----------------------------------------------------------------------
    // Purge Configuration
    // ----------------------------------------------------------------------

    public RepositoryPurgeConfiguration addRepositoryPurgeConfiguration( RepositoryPurgeConfiguration repoPurge )
        throws ContinuumException
    {
        checkManagePurgingAuthorization();
        
        try
        {
            org.apache.continuum.model.repository.RepositoryPurgeConfiguration newPurge = 
                new org.apache.continuum.model.repository.RepositoryPurgeConfiguration();
            return populateRepositoryPurgeConfiguration( continuum.getPurgeConfigurationService().
                                                         addRepositoryPurgeConfiguration( populateRepositoryPurgeConfiguration( repoPurge, newPurge ) ) );
        }
        catch ( RepositoryServiceException e )
        {
            throw new ContinuumException( "Error while converting repository purge configuration", e );
        }
        catch ( PurgeConfigurationServiceException e )
        {
            throw new ContinuumException( "Can't add repositoryPurgeConfiguration", e );
        }
    }

    public int updateRepositoryPurgeConfiguration( RepositoryPurgeConfiguration repoPurge )
        throws ContinuumException
    {
        checkManagePurgingAuthorization();
        
        try
        {
            org.apache.continuum.model.repository.RepositoryPurgeConfiguration purge = 
                new org.apache.continuum.model.repository.RepositoryPurgeConfiguration();
            continuum.getPurgeConfigurationService().updateRepositoryPurgeConfiguration( populateRepositoryPurgeConfiguration( repoPurge, purge ) );
            return 0;
        }
        catch ( RepositoryServiceException e )
        {
            throw new ContinuumException( "Error while converting repository purge configuration", e );
        }
        catch ( PurgeConfigurationServiceException e )
        {
            throw new ContinuumException( "Cant' update repository PurgeException", e );
        }
    }

    public int removeRepositoryPurgeConfiguration( int repoPurgeId )
        throws ContinuumException
    {
        checkManagePurgingAuthorization();

        try
        {
            org.apache.continuum.model.repository.RepositoryPurgeConfiguration repoPurge = 
                continuum.getPurgeConfigurationService().getRepositoryPurgeConfiguration( repoPurgeId );
            continuum.getPurgeConfigurationService().removeRepositoryPurgeConfiguration( repoPurge );
            return 0;
        }
        catch ( PurgeConfigurationServiceException e )
        {
            throw new ContinuumException( "Can't delete repository purge configuration", e );
        }
    }

    public RepositoryPurgeConfiguration getRepositoryPurgeConfiguration( int repoPurgeId )
        throws ContinuumException
    {
        checkManagePurgingAuthorization();

        try
        {
            org.apache.continuum.model.repository.RepositoryPurgeConfiguration repoPurgeConfig = 
                continuum.getPurgeConfigurationService().getRepositoryPurgeConfiguration( repoPurgeId );
            return populateRepositoryPurgeConfiguration( repoPurgeConfig );
        }
        catch ( PurgeConfigurationServiceException e )
        {
            throw new ContinuumException( "Error while retrieving repository purge configuration", e );
        }
    }

    public List<RepositoryPurgeConfiguration> getAllRepositoryPurgeConfigurations()
        throws ContinuumException
    {
        checkManagePurgingAuthorization();
        Collection repoPurgeConfigs = continuum.getPurgeConfigurationService().getAllRepositoryPurgeConfigurations();
        
        List<RepositoryPurgeConfiguration> r = new ArrayList<RepositoryPurgeConfiguration>();
        for ( Object repoPurgeConfig : repoPurgeConfigs )
        {
            r.add( populateRepositoryPurgeConfiguration( ( org.apache.continuum.model.repository.RepositoryPurgeConfiguration ) repoPurgeConfig ) );
        }
        return r;
    }

    public DirectoryPurgeConfiguration addDirectoryPurgeConfiguration( DirectoryPurgeConfiguration dirPurge )
        throws ContinuumException
    {
        checkManagePurgingAuthorization();

        try
        {
            org.apache.continuum.model.repository.DirectoryPurgeConfiguration newPurge =
                new org.apache.continuum.model.repository.DirectoryPurgeConfiguration();
            return populateDirectoryPurgeConfiguration( continuum.getPurgeConfigurationService().
                                                        addDirectoryPurgeConfiguration( populateDirectoryPurgeConfiguration( dirPurge, newPurge ) ) );
        }
        catch ( RepositoryServiceException e )
        {
            throw new ContinuumException( "Error while converting directory purge configuration", e );
        }
        catch ( PurgeConfigurationServiceException e )
        {
            throw new ContinuumException( "Can't add directory purge configuration", e );
        }
    }

    public int updateDirectoryPurgeConfiguration( DirectoryPurgeConfiguration dirPurge )
        throws ContinuumException
    {
        checkManagePurgingAuthorization();

        try
        {
            org.apache.continuum.model.repository.DirectoryPurgeConfiguration purge =
                new org.apache.continuum.model.repository.DirectoryPurgeConfiguration();
            continuum.getPurgeConfigurationService().updateDirectoryPurgeConfiguration( populateDirectoryPurgeConfiguration( dirPurge, purge ) );
            return 0;
        }
        catch ( RepositoryServiceException e )
        {
            throw new ContinuumException( "Error while converting directory purge configuration", e );
        }
        catch ( PurgeConfigurationServiceException e )
        {
            throw new ContinuumException( "Can't add directory purge configuration", e );
        }
    }

    public int removeDirectoryPurgeConfiguration( int dirPurgeId )
        throws ContinuumException
    {
        checkManagePurgingAuthorization();
        
        try
        {
            org.apache.continuum.model.repository.DirectoryPurgeConfiguration dirPurge =
                continuum.getPurgeConfigurationService().getDirectoryPurgeConfiguration( dirPurgeId );
            continuum.getPurgeConfigurationService().removeDirectoryPurgeConfiguration( dirPurge );
            return 0;
        }
        catch ( PurgeConfigurationServiceException e )
        {
            throw new ContinuumException( "Can't delete directory purge configuration", e );
        }
    }

    public DirectoryPurgeConfiguration getDirectoryPurgeConfiguration( int dirPurgeId )
        throws ContinuumException
    {
        checkManagePurgingAuthorization();

        try
        {
            org.apache.continuum.model.repository.DirectoryPurgeConfiguration dirPurgeConfig = continuum.getPurgeConfigurationService().getDirectoryPurgeConfiguration( dirPurgeId );
            return populateDirectoryPurgeConfiguration( dirPurgeConfig );
        }
        catch ( PurgeConfigurationServiceException e )
        {
            throw new ContinuumException( "Error while retrieving directory purge configuration", e );
        }
    }

    public List<DirectoryPurgeConfiguration> getAllDirectoryPurgeConfigurations()
        throws ContinuumException
    {
        checkManagePurgingAuthorization();
        Collection dirPurgeConfigs = continuum.getPurgeConfigurationService().getAllDirectoryPurgeConfigurations();

        List<DirectoryPurgeConfiguration> d = new ArrayList<DirectoryPurgeConfiguration>();
        for ( Object dirPurgeConfig : dirPurgeConfigs )
        {
            d.add( populateDirectoryPurgeConfiguration( ( org.apache.continuum.model.repository.DirectoryPurgeConfiguration ) dirPurgeConfig ) );
        }
        return d;
    }
    
    public void purgeLocalRepository( int repoPurgeId )
        throws ContinuumException
    {
        checkManagePurgingAuthorization();

        try
        {
            org.apache.continuum.model.repository.RepositoryPurgeConfiguration repoPurgeConfig = continuum.getPurgeConfigurationService().getRepositoryPurgeConfiguration( repoPurgeId );
            continuum.getPurgeManager().purgeRepository( repoPurgeConfig );
        }
        catch ( PurgeConfigurationServiceException e )
        {
            throw new ContinuumException( "Error while retrieving repository purge configuration", e );
        }
        catch ( ContinuumPurgeManagerException e )
        {
            throw new ContinuumException( "Error while purging local repository", e );
        }
    }

    public void purgeDirectory( int dirPurgeId )
        throws ContinuumException
    {
        checkManagePurgingAuthorization();

        try
        {
            org.apache.continuum.model.repository.DirectoryPurgeConfiguration dirPurgeConfig = continuum.getPurgeConfigurationService().getDirectoryPurgeConfiguration( dirPurgeId );
            continuum.getPurgeManager().purgeDirectory( dirPurgeConfig );
        }
        catch ( PurgeConfigurationServiceException e )
        {
            throw new ContinuumException( "Error while retrieving directory purge configuration", e );
        }
        catch ( ContinuumPurgeManagerException e )
        {
            throw new ContinuumException( "Error while purging directory", e );
        }
    }

    // ----------------------------------------------------------------------
    // Local Repository
    // ----------------------------------------------------------------------

    public LocalRepository addLocalRepository( LocalRepository repository )
        throws ContinuumException
    {
        checkManageRepositoriesAuthorization();

        try
        {
            org.apache.continuum.model.repository.LocalRepository newRepository =
                        new org.apache.continuum.model.repository.LocalRepository();
            return populateLocalRepository( continuum.getRepositoryService().addLocalRepository(
                                           populateLocalRepository( repository, newRepository ) ) );
        }
        catch ( RepositoryServiceException e )
        {
            throw new ContinuumException( "Unable to add repository", e );
        }
    }

    public int updateLocalRepository( LocalRepository repository )
        throws ContinuumException
    {
        checkManageRepositoriesAuthorization();

        try
        {
            final org.apache.continuum.model.repository.LocalRepository newRepo =
                            continuum.getRepositoryService().getLocalRepository( repository.getId() );
            continuum.getRepositoryService().updateLocalRepository( populateLocalRepository( repository, newRepo ) );
            return 0;
        }
        catch ( RepositoryServiceException e )
        {
            throw new ContinuumException( "Can't update repository", e );
        }
    }

    public int removeLocalRepository( int repositoryId )
        throws ContinuumException
    {
        checkManageRepositoriesAuthorization();

        try
        {
            continuum.getRepositoryService().removeLocalRepository( repositoryId );
            return 0;
        }
        catch ( RepositoryServiceException e)
        {
            throw new ContinuumException( "Can't delete repository", e );
        }
    }

    public LocalRepository getLocalRepository( int repositoryId )
        throws ContinuumException
    {
        checkManageRepositoriesAuthorization();
        
        try
        {
            return populateLocalRepository( continuum.getRepositoryService().getLocalRepository( repositoryId ) );
        }
        catch ( RepositoryServiceException e )
        {
            throw new ContinuumException( "Error while retrieving repository.", e);
        }
    }

    public List<LocalRepository> getAllLocalRepositories()
        throws ContinuumException
    {
        checkManageRepositoriesAuthorization();
        Collection repos = continuum.getRepositoryService().getAllLocalRepositories();

        List<LocalRepository> r = new ArrayList<LocalRepository>();
        for ( Object repo : repos )
        {
            r.add( populateLocalRepository( (org.apache.continuum.model.repository.LocalRepository) repo ) );
        }
        return r;
    }

    // ----------------------------------------------------------------------
    // Converters
    // ----------------------------------------------------------------------

    private List<BuildProjectTask> populateBuildProjectTaskList(
        List<org.apache.maven.continuum.buildqueue.BuildProjectTask> buildProjectTasks )
    {
        List<BuildProjectTask> responses = new ArrayList<BuildProjectTask>();
        for ( org.apache.maven.continuum.buildqueue.BuildProjectTask buildProjectTask : buildProjectTasks )
        {

            responses.add( (BuildProjectTask) mapper.map( buildProjectTask, BuildProjectTask.class ) );
        }
        return responses;
    }

    private ProjectSummary populateProjectSummary( org.apache.maven.continuum.model.project.Project project )
    {
        return (ProjectSummary) mapper.map( project, ProjectSummary.class );
    }

    private Project populateProject( org.apache.maven.continuum.model.project.Project project )
    {
        return (Project) mapper.map( project, Project.class );
    }

    private org.apache.maven.continuum.model.project.Project populateProject( ProjectSummary projectSummary,
                                                                              org.apache.maven.continuum.model.project.Project project )
        throws ContinuumException
    {
        if ( projectSummary == null )
        {
            return null;
        }
        project.setArtifactId( projectSummary.getArtifactId() );
        project.setBuildNumber( projectSummary.getBuildNumber() );
        project.setDescription( projectSummary.getDescription() );
        project.setExecutorId( projectSummary.getExecutorId() );
        project.setGroupId( projectSummary.getGroupId() );
        project.setId( projectSummary.getId() );
        project.setLatestBuildId( projectSummary.getLatestBuildId() );
        project.setName( projectSummary.getName() );
        if ( projectSummary.getProjectGroup() != null )
        {
            org.apache.maven.continuum.model.project.ProjectGroup g =
                            continuum.getProjectGroup( projectSummary.getProjectGroup().getId() );
            project.setProjectGroup( populateProjectGroupSummary( projectSummary.getProjectGroup(), g ) );
        }
        else
        {
            project.setProjectGroup( null );
        }
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

    private ProjectNotifier populateProjectNotifier( org.apache.maven.continuum.model.project.ProjectNotifier notifier )
    {
        return (ProjectNotifier) mapper.map( notifier, ProjectNotifier.class );
    }

    private ProjectGroupSummary populateProjectGroupSummary( org.apache.maven.continuum.model.project.ProjectGroup group )
    {
        return (ProjectGroupSummary) mapper.map( group, ProjectGroupSummary.class );
    }

    private org.apache.maven.continuum.model.project.ProjectGroup populateProjectGroupSummary( ProjectGroupSummary group,
                                                                                               org.apache.maven.continuum.model.project.ProjectGroup g )
    {
        if ( group == null )
        {
            return null;
        }

        g.setDescription( group.getDescription() );
        g.setGroupId( group.getGroupId() );
        g.setId( group.getId() );
        g.setName( group.getName() );
        org.apache.continuum.model.repository.LocalRepository repo =
            new org.apache.continuum.model.repository.LocalRepository();
        g.setLocalRepository( populateLocalRepository( group.getLocalRepository(), repo ) );
        return g;
    }

    private ProjectGroup populateProjectGroupWithAllDetails( org.apache.maven.continuum.model.project.ProjectGroup group )
    {
        return (ProjectGroup) mapper.map( group, ProjectGroup.class );
    }

    private BuildResultSummary populateBuildResultSummary( org.apache.maven.continuum.model.project.BuildResult buildResult )
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

    private org.apache.maven.continuum.model.project.BuildDefinition populateBuildDefinition( BuildDefinition buildDef,
                                                                                              org.apache.maven.continuum.model.project.BuildDefinition bd )
        throws ProfileException, ContinuumException
    {
        if ( buildDef == null )
        {
            return null;
        }

        bd.setArguments( buildDef.getArguments() );
        bd.setBuildFile( buildDef.getBuildFile() );
        bd.setType( buildDef.getType() );
        bd.setBuildFresh( buildDef.isBuildFresh() );
        bd.setAlwaysBuild( buildDef.isAlwaysBuild() );
        bd.setDefaultForProject( buildDef.isDefaultForProject() );
        bd.setGoals( buildDef.getGoals() );
        bd.setId( buildDef.getId() );
        if ( buildDef.getProfile() != null )
        {
            bd.setProfile( populateProfile( buildDef.getProfile(), continuum.getProfileService().getProfile(
                buildDef.getProfile().getId() ) ) );
        }
        else
        {
            bd.setProfile( null );
        }
        if ( buildDef.getSchedule() != null )
        {
            bd.setSchedule( populateSchedule( buildDef.getSchedule(), continuum.getSchedule( buildDef.getSchedule()
                            .getId() ) ) );
        }
        else
        {
            bd.setSchedule( null );
        }

        return bd;
    }

    private BuildDefinitionTemplate populateBuildDefinitionTemplate( org.apache.maven.continuum.model.project.BuildDefinitionTemplate bdt )
    {
        return (BuildDefinitionTemplate) mapper.map( bdt, BuildDefinitionTemplate.class );
    }

    private org.apache.maven.continuum.model.project.Schedule populateSchedule( Schedule schedule,
                                                                                org.apache.maven.continuum.model.project.Schedule s )
    {
        if ( schedule == null )
        {
            return null;
        }

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

    private org.apache.maven.continuum.model.system.Profile populateProfile( Profile profile,
                                                                             org.apache.maven.continuum.model.system.Profile newProfile )
        throws ContinuumException
    {
        if ( profile == null )
        {
            return null;
        }

        try
        {
            newProfile.setActive( profile.isActive() );
            newProfile.setBuildWithoutChanges( profile.isBuildWithoutChanges() );
            newProfile.setDescription( profile.getDescription() );
            newProfile.setName( profile.getName() );
            newProfile.setScmMode( profile.getScmMode() );
            if ( profile.getBuilder() != null )
            {
                final org.apache.maven.continuum.model.system.Installation newBuilder =
                                continuum.getInstallationService().getInstallation(
                                    profile.getBuilder().getInstallationId() );
                newProfile.setBuilder( populateInstallation( profile.getBuilder(), newBuilder ) );

            }
            else
            {
                newProfile.setBuilder( null );
            }
            if ( profile.getJdk() != null )
            {
                final org.apache.maven.continuum.model.system.Installation newJdk =
                                continuum.getInstallationService().getInstallation(
                                    profile.getJdk().getInstallationId() );
                newProfile.setJdk( populateInstallation( profile.getJdk(), newJdk ) );

            }
            else
            {
                newProfile.setJdk( null );
            }
            newProfile.getEnvironmentVariables().clear();
            if ( profile.getEnvironmentVariables() != null )
            {
                for ( Iterator it = profile.getEnvironmentVariables().iterator(); it.hasNext(); )
                {
                    final Installation varEnv = (Installation) it.next();

                    final org.apache.maven.continuum.model.system.Installation newInst =
                                    continuum.getInstallationService().getInstallation( varEnv.getInstallationId() );
                    newProfile.getEnvironmentVariables().add( populateInstallation( varEnv, newInst ) );

                }
            }
            return newProfile;
        }
        catch ( InstallationException e )
        {
            throw new ContinuumException( "Can't load installations", e );
        }
    }

    private Profile populateProfile( org.apache.maven.continuum.model.system.Profile profile )
    {
        return (Profile) mapper.map( profile, Profile.class );
    }

    private org.apache.maven.continuum.model.system.Installation populateInstallation( Installation install,
                                                                                       org.apache.maven.continuum.model.system.Installation inst )
    {
        if ( install == null )
        {
            return null;
        }

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

    private SystemConfiguration populateSystemConfiguration( org.apache.maven.continuum.model.system.SystemConfiguration sysConf )
    {
        return (SystemConfiguration) mapper.map( sysConf, SystemConfiguration.class );
    }

    private ContinuumReleaseResult populateReleaseResult( org.apache.continuum.model.release.ContinuumReleaseResult releaseResult )
    {
        return (ContinuumReleaseResult) mapper.map( releaseResult, ContinuumReleaseResult.class );
    }

    private RepositoryPurgeConfiguration populateRepositoryPurgeConfiguration( org.apache.continuum.model.repository.RepositoryPurgeConfiguration repoPurgeConfig )
    {
        return (RepositoryPurgeConfiguration) mapper.map( repoPurgeConfig, RepositoryPurgeConfiguration.class );
    }

    private org.apache.continuum.model.repository.RepositoryPurgeConfiguration populateRepositoryPurgeConfiguration( RepositoryPurgeConfiguration repoPurgeConfig,
                                                                                                                     org.apache.continuum.model.repository.RepositoryPurgeConfiguration repoPurge )
        throws RepositoryServiceException, ContinuumException
    {
        if ( repoPurgeConfig == null )
        {
            return null;
        }

        repoPurge.setDaysOlder( repoPurgeConfig.getDaysOlder() );
        repoPurge.setDefaultPurge( repoPurgeConfig.isDefaultPurge() );
        repoPurge.setDeleteAll( repoPurgeConfig.isDeleteAll() );
        repoPurge.setDeleteReleasedSnapshots( repoPurgeConfig.isDeleteReleasedSnapshots() );
        repoPurge.setDescription( repoPurgeConfig.getDescription() );
        repoPurge.setEnabled( repoPurgeConfig.isEnabled() );
        repoPurge.setRetentionCount( repoPurgeConfig.getRetentionCount() );
        if ( repoPurgeConfig.getRepository() != null )
        {
            repoPurge.setRepository( populateLocalRepository( repoPurgeConfig.getRepository(), continuum.getRepositoryService().
                                                              getLocalRepository( repoPurgeConfig.getRepository().getId() ) ) );
        }
        else
        {
            repoPurge.setRepository( null );
        }
        if ( repoPurgeConfig.getSchedule() != null )
        {
            repoPurge.setSchedule( populateSchedule( repoPurgeConfig.getSchedule(), continuum.getSchedule( repoPurgeConfig.getSchedule()
                            .getId() ) ) );
        }
        else
        {
            repoPurge.setSchedule( null );
        }

        return repoPurge;
    }

    private DirectoryPurgeConfiguration populateDirectoryPurgeConfiguration( org.apache.continuum.model.repository.DirectoryPurgeConfiguration dirPurgeConfig )
    {
        return (DirectoryPurgeConfiguration) mapper.map( dirPurgeConfig, DirectoryPurgeConfiguration.class );
    }

    private org.apache.continuum.model.repository.DirectoryPurgeConfiguration populateDirectoryPurgeConfiguration( DirectoryPurgeConfiguration dirPurgeConfig, 
                                                                                                                   org.apache.continuum.model.repository.DirectoryPurgeConfiguration dirPurge )
        throws RepositoryServiceException, ContinuumException
    {
        if ( dirPurgeConfig == null )
        {
            return null;
        }

        dirPurge.setDaysOlder( dirPurgeConfig.getDaysOlder() );
        dirPurge.setDefaultPurge( dirPurgeConfig.isDefaultPurge() );
        dirPurge.setDeleteAll( dirPurgeConfig.isDeleteAll() );
        dirPurge.setDescription( dirPurgeConfig.getDescription() );
        dirPurge.setDirectoryType( dirPurgeConfig.getDirectoryType() );
        dirPurge.setEnabled( dirPurgeConfig.isEnabled() );

        String path = "";

        if ( dirPurge.getDirectoryType().equals( "releases" ) )
        {
            path = continuum.getConfiguration().getWorkingDirectory().getAbsolutePath();
        }
        else if ( dirPurge.getDirectoryType().equals( "buildOutput" ) )
        {
            path = continuum.getConfiguration().getBuildOutputDirectory().getAbsolutePath();
        }
        
        dirPurge.setLocation( path );
        dirPurge.setRetentionCount( dirPurgeConfig.getRetentionCount() );
        if ( dirPurgeConfig.getSchedule() != null )
        {
            dirPurge.setSchedule( populateSchedule( dirPurgeConfig.getSchedule(), continuum.getSchedule( dirPurgeConfig.getSchedule()
                            .getId() ) ) );
        }
        else
        {
            dirPurge.setSchedule( null );
        }

        return dirPurge;
    }

    private LocalRepository populateLocalRepository( org.apache.continuum.model.repository.LocalRepository localRepository )
    {
        return (LocalRepository) mapper.map( localRepository, LocalRepository.class );
    }

    private org.apache.continuum.model.repository.LocalRepository populateLocalRepository( LocalRepository repository,
                                                                                           org.apache.continuum.model.repository.LocalRepository repo )
    {
        if ( repository == null )
        {
            return null;
        }

        repo.setLayout( repository.getLayout() );
        repo.setLocation( repository.getLocation() );
        repo.setName( repository.getName() );
        return repo;
    }

    private Map<String, Object> serializeObject( Object o, final String ... ignore )
    {
        if ( o != null )
        {
            return serializeObject( o, o.getClass(), ignore );
        }
        else
        {
            return null;
        }
    }

    private Map<String, Object> serializeObject( Object o, Class clasz, final String ... ignore )
    {

        final List<String> ignoreList = ignore == null ? new ArrayList<String>() : Arrays.asList( ignore );
        if ( o != null )
        {
            final Map<String, Object> retValue = new HashMap<String, Object>();
            if ( !Object.class.equals( clasz.getSuperclass() ) )
            {
                retValue.putAll( serializeObject( o, clasz.getSuperclass(), ignore ) );
            }

            final Field[] fields = clasz.getDeclaredFields();

            retValue.put( "__class", clasz.getName() );
            for ( final Field field : fields )
            {

                if ( !ignoreList.contains( field.getName() ) )
                {
                    field.setAccessible( true );
                    try
                    {
                        final Object tmpFO = field.get( o );
                        final Object tmpNO = mapObject( tmpFO );

                        retValue.put( field.getName(), tmpNO );
                    }
                    catch ( IllegalAccessException e )
                    {
                        e.printStackTrace();
                    }
                }
            }
            return retValue;
        }
        else
        {
            return null;
        }
    }

    private Object mapObject( Object tmpFO )
    {
        final Object retValue;
        if ( tmpFO instanceof String )
        {
            Object tmpNO = serializeObject( (String) tmpFO );
            if ( tmpNO == null )
            {
                tmpNO = "";
            }
            retValue = tmpNO;
        }
        else if ( tmpFO instanceof Float )
        {
            Object tmpNO = serializeObject( (Float) tmpFO );
            if ( tmpNO == null )
            {
                tmpNO = "";
            }
            retValue = tmpNO;
        }
        else if ( tmpFO instanceof Boolean )
        {
            Object tmpNO = serializeObject( (Boolean) tmpFO );
            if ( tmpNO == null )
            {
                tmpNO = "";
            }
            retValue = tmpNO;
        }
        else if ( tmpFO instanceof Integer )
        {
            Object tmpNO = serializeObject( (Integer) tmpFO );
            if ( tmpNO == null )
            {
                tmpNO = "";
            }
            retValue = tmpNO;
        }
        else if ( tmpFO instanceof Long )
        {
            Object tmpNO = serializeObject( (Long) tmpFO );
            if ( tmpNO == null )
            {
                tmpNO = "";
            }
            retValue = tmpNO;
        }
        else if ( tmpFO instanceof Character )
        {
            Object tmpNO = serializeObject( (Character) tmpFO );
            if ( tmpNO == null )
            {
                tmpNO = "";
            }
            retValue = tmpNO;
        }
        else if ( tmpFO instanceof Byte )
        {
            Object tmpNO = serializeObject( (Byte) tmpFO );
            if ( tmpNO == null )
            {
                tmpNO = "";
            }
            retValue = tmpNO;
        }
        else if ( tmpFO instanceof Double )
        {
            Object tmpNO = serializeObject( (Double) tmpFO );
            if ( tmpNO == null )
            {
                tmpNO = "";
            }
            retValue = tmpNO;
        }
        else if ( tmpFO instanceof List )
        {
            Object tmpNO = serializeObject( (List) tmpFO );
            if ( tmpNO == null )
            {
                tmpNO = "";
            }
            retValue = tmpNO;
        }
        else if ( tmpFO instanceof Map )
        {
            Object tmpNO = serializeObject( (Map) tmpFO );
            if ( tmpNO == null )
            {
                tmpNO = "";
            }
            retValue = tmpNO;
        }
        else
        {
            Object tmpNO = serializeObject( tmpFO );
            if ( tmpNO == null )
            {
                tmpNO = "";
            }
            retValue = tmpNO;
        }
        return retValue;
    }

    private Map<String, Object> serializeObject( Map<Object, Object> map )
    {
        final Map<String, Object> retValue = new HashMap<String, Object>();

        for ( Object key : map.keySet() )
        {
            final Object tmpKey = mapObject( key );

            if ( tmpKey != null )
            {
                retValue.put( tmpKey.toString(), mapObject( map.get( key ) ) );
            }
        }
        return retValue;
    }

    private List<Object> serializeObject( List list )
    {
        final List<Object> retValue = new ArrayList<Object>();

        for ( Object o : list )
        {
            final Object tmpO = mapObject( o );
            if ( tmpO == null )
            {
                retValue.add( "" );
            }
            else
            {
                retValue.add( tmpO );
            }
        }
        return retValue;
    }

    private String serializeObject( String o )
    {
        return o;
    }

    private String serializeObject( Byte o )
    {
        return (o == null ? null : o.toString());
    }

    private String serializeObject( Character o )
    {
        return (o == null ? null : o.toString());
    }

    private Double serializeObject( Long o )
    {
        return (o == null ? null : o.doubleValue());
    }

    private Double serializeObject( Float o )
    {
        return (o == null ? null : o.doubleValue());
    }

    private Double serializeObject( Double o )
    {
        return o;
    }

    private Integer serializeObject( Integer o )
    {
        return o;
    }

    private Boolean serializeObject( Boolean o )
    {
        return o;
    }

    private Object unserializeObject( Map<String, Object> o )
    {
        Object retValue = null;
        if ( o != null )
        {
            final String className = (String) o.remove( "__class" );

            if ( className != null )
            {
                try
                {
                    final Class clasz = Class.forName( className );
                    final Object tmpO = clasz.newInstance();
                    for ( final String key : o.keySet() )
                    {
                        final Field field = clasz.getDeclaredField( key );
                        field.setAccessible( true );
                        final Object tmpFO = o.get( key );

                        field.set( tmpO, unMapObject( tmpFO ) );
                    }
                    retValue = tmpO;
                }
                catch ( Exception e )
                {
                    e.printStackTrace();
                    retValue = null;
                }
            }
            else
            {
                // Not an object, it's a normal Map
                Map<String, Object> tmpValue = new HashMap<String, Object>();

                for ( String key : o.keySet() )
                {
                    tmpValue.put( key, unMapObject( o.get( key ) ) );
                }
                retValue = tmpValue;
            }
        }
        return retValue;
    }

    private Object unMapObject( Object tmpFO )
    {
        final Object retValue;
        if ( tmpFO instanceof String )
        {
            retValue = unserializeObject( (String) tmpFO );
        }
        else if ( tmpFO instanceof Float )
        {
            retValue = unserializeObject( (Float) tmpFO );
        }
        else if ( tmpFO instanceof Boolean )
        {
            retValue = unserializeObject( (Boolean) tmpFO );
        }
        else if ( tmpFO instanceof Integer )
        {
            retValue = unserializeObject( (Integer) tmpFO );
        }
        else if ( tmpFO instanceof Long )
        {
            retValue = unserializeObject( (Long) tmpFO );
        }
        else if ( tmpFO instanceof Character )
        {
            retValue = unserializeObject( (Character) tmpFO );
        }
        else if ( tmpFO instanceof Byte )
        {
            retValue = unserializeObject( (Byte) tmpFO );
        }
        else if ( tmpFO instanceof Double )
        {
            retValue = unserializeObject( (Double) tmpFO );
        }
        else if ( tmpFO instanceof List )
        {
            retValue = unserializeObject( (List) tmpFO );
        }
        else if ( tmpFO instanceof Map )
        {
            retValue = unserializeObject( (Map) tmpFO );
        }
        else if ( tmpFO instanceof Object[] )
        {
            retValue = unserializeObject( (Object[]) tmpFO );
        }
        else
        {
            retValue = unserializeObject( tmpFO );
        }
        return retValue;
    }

    private List<Object> unserializeObject( List list )
    {
        final List<Object> retValue = new ArrayList<Object>();

        for ( Object o : list )
        {
            retValue.add( unMapObject( o ) );
        }
        return retValue;
    }

    private Object unserializeObject( Object o )
    {
        return o;
    }

    private Object unserializeObject( Object[] list )
    {
        final List<Object> retValue = new ArrayList<Object>();

        for ( Object o : list )
        {
            retValue.add( unMapObject( o ) );
        }
        return retValue;
    }

    private String unserializeObject( String o )
    {
        return o;
    }

    private Byte unserializeObject( Byte o )
    {
        return o;
    }

    private Character unserializeObject( Character o )
    {
        return o;
    }

    private Long unserializeObject( Long o )
    {
        return o;
    }

    private Float unserializeObject( Float o )
    {
        return o;
    }

    private Double unserializeObject( Double o )
    {
        return o;
    }

    private Integer unserializeObject( Integer o )
    {
        return o;
    }

    private Boolean unserializeObject( Boolean o )
    {
        return o;
    }

    public Map<String, Object> addAntProjectRPC( Map<String, Object> project )
        throws Exception
    {
        return serializeObject( this.addAntProject( (ProjectSummary) unserializeObject( project ) ) );
    }

    public Map<String, Object> addAntProjectRPC( Map<String, Object> project, int projectGroupId )
        throws Exception
    {
        return serializeObject( this.addAntProject( (ProjectSummary) unserializeObject( project ), projectGroupId ) );
    }

    public Map<String, Object> addBuildDefinitionToProjectGroupRPC( int projectGroupId, Map<String, Object> buildDef )
        throws Exception
    {
        return serializeObject( this.addBuildDefinitionToProjectGroup( projectGroupId,
            (BuildDefinition) unserializeObject( buildDef ) ) );
    }

    public Map<String, Object> addBuildDefinitionToProjectRPC( int projectId, Map<String, Object> buildDef )
        throws Exception
    {
        return serializeObject( this.addBuildDefinitionToProject( projectId,
            (BuildDefinition) unserializeObject( buildDef ) ) );
    }

    public Map<String, Object> addMavenOneProjectRPC( String url )
        throws Exception
    {
        return serializeObject( this.addMavenOneProject( url ) );
    }

    public Map<String, Object> addMavenOneProjectRPC( String url, int projectGroupId )
        throws Exception
    {
        return serializeObject( this.addMavenOneProject( url, projectGroupId ) );
    }

    public Map<String, Object> addMavenTwoProjectRPC( String url )
        throws Exception
    {
        return serializeObject( this.addMavenTwoProject( url ) );
    }

    public Map<String, Object> addMavenTwoProjectRPC( String url, int projectGroupId )
        throws Exception
    {
        return serializeObject( this.addMavenTwoProject( url, projectGroupId ) );
    }

    public Map<String, Object> addProjectGroupRPC( String groupName, String groupId, String description )
        throws Exception
    {
        return serializeObject( this.addProjectGroup( groupName, groupId, description ) );
    }

    public Map<String, Object> addScheduleRPC( Map<String, Object> schedule )
        throws Exception
    {
        return serializeObject( this.addSchedule( (Schedule) unserializeObject( schedule ) ) );
    }

    public Map<String, Object> addShellProjectRPC( Map<String, Object> project, int projectGroupId )
        throws Exception
    {
        return serializeObject( this.addShellProject( (ProjectSummary) unserializeObject( project ), projectGroupId ) );
    }

    public Map<String, Object> addShellProjectRPC( Map<String, Object> project )
        throws Exception
    {
        return serializeObject( this.addShellProject( (ProjectSummary) unserializeObject( project ) ) );
    }

    public List<Object> getAllProjectGroupsRPC()
        throws Exception
    {
        return serializeObject( this.getAllProjectGroups() );
    }

    public List<Object> getAllProjectGroupsWithAllDetailsRPC()
        throws Exception
    {
        return serializeObject( this.getAllProjectGroupsWithAllDetails() );
    }

    public List<Object> getBuildDefinitionTemplatesRPC()
        throws Exception
    {
        return serializeObject( this.getBuildDefinitionTemplates() );
    }

    public List<Object> getBuildDefinitionsForProjectGroupRPC( int projectGroupId )
        throws Exception
    {
        return serializeObject( this.getBuildDefinitionsForProjectGroup( projectGroupId ) );
    }

    public List<Object> getBuildDefinitionsForProjectRPC( int projectId )
        throws Exception
    {
        return serializeObject( this.getBuildDefinitionsForProject( projectId ) );
    }

    public Map<String, Object> getBuildResultRPC( int projectId, int buildId )
        throws Exception
    {
        return serializeObject( this.getBuildResult( projectId, buildId ) );
    }

    public List<Object> getBuildResultsForProjectRPC( int projectId )
        throws Exception
    {
        return serializeObject( this.getBuildResultsForProject( projectId ) );
    }

    public Map<String, Object> getInstallationRPC( int installationId )
        throws Exception
    {
        return serializeObject( this.getInstallation( installationId ) );
    }

    public List<Object> getInstallationsRPC()
        throws Exception
    {
        return serializeObject( this.getInstallations() );
    }

    public Map<String, Object> getLatestBuildResultRPC( int projectId )
        throws Exception
    {
        return serializeObject( this.getLatestBuildResult( projectId ) );
    }

    public Map<String, Object> getProfileRPC( int profileId )
        throws Exception
    {
        return serializeObject( this.getProfile( profileId ) );
    }

    public List<Object> getProfilesRPC()
        throws Exception
    {
        return serializeObject( this.getProfiles() );
    }

    public Map<String, Object> getProjectGroupSummaryRPC( int projectGroupId )
        throws Exception
    {
        return serializeObject( this.getProjectGroupSummary( projectGroupId ) );
    }

    public Map<String, Object> getProjectGroupWithProjectsRPC( int projectGroupId )
        throws Exception
    {
        return serializeObject( this.getProjectGroupWithProjects( projectGroupId ) );
    }

    public Map<String, Object> updateProjectGroupRPC( Map<String, Object> projectGroup )
        throws Exception
    {
        return serializeObject( this.updateProjectGroup( (ProjectGroupSummary) unserializeObject( projectGroup ) ) );
    }

    public Map<String, Object> getProjectSummaryRPC( int projectId )
        throws Exception
    {
        return serializeObject( this.getProjectSummary( projectId ) );
    }

    public Map<String, Object> getProjectWithAllDetailsRPC( int projectId )
        throws Exception
    {
        return serializeObject( this.getProjectWithAllDetails( projectId ) );
    }

    public List<Object> getProjectsRPC( int projectGroupId )
        throws Exception
    {
        return serializeObject( this.getProjects( projectGroupId ) );
    }

    public Map<String, Object> getScheduleRPC( int scheduleId )
        throws Exception
    {
        return serializeObject( this.getSchedule( scheduleId ) );
    }

    public List<Object> getSchedulesRPC()
        throws Exception
    {
        return serializeObject( this.getSchedules() );
    }

    public Map<String, Object> getSystemConfigurationRPC()
        throws Exception
    {
        return serializeObject( this.getSystemConfiguration() );
    }

    public int removeBuildResultRPC( Map<String, Object> br )
        throws Exception
    {
        return serializeObject( this.removeBuildResult( (BuildResult) unserializeObject( br ) ) );
    }

    public Map<String, Object> updateBuildDefinitionForProjectGroupRPC( int projectGroupId, Map<String, Object> buildDef )
        throws Exception
    {
        return serializeObject( this.updateBuildDefinitionForProjectGroup( projectGroupId,
            (BuildDefinition) unserializeObject( buildDef ) ) );
    }

    public Map<String, Object> updateBuildDefinitionForProjectRPC( int projectId, Map<String, Object> buildDef )
        throws Exception
    {
        return serializeObject( this.updateBuildDefinitionForProject( projectId,
            (BuildDefinition) unserializeObject( buildDef ) ) );
    }

    public Map<String, Object> updateProjectRPC( Map<String, Object> project )
        throws Exception
    {
        return serializeObject( this.updateProject( (ProjectSummary) unserializeObject( project ) ) );
    }

    public Map<String, Object> updateScheduleRPC( Map<String, Object> schedule )
        throws Exception
    {
        return serializeObject( this.updateSchedule( (Schedule) unserializeObject( schedule ) ) );
    }

    public Map<String, Object> getProjectGroupRPC( int projectGroupId )
        throws Exception
    {
        return serializeObject( this.getProjectGroup( projectGroupId ), "projects" );
    }

    public Map<String, Object> getGroupNotifierRPC( int projectgroupid, int notifierId )
        throws Exception
    {
        return serializeObject( this.getGroupNotifier( projectgroupid, notifierId ) );
    }

    public Map<String, Object> getNotifierRPC( int projectid, int notifierId )
        throws Exception
    {
        return serializeObject( this.getNotifier( projectid, notifierId ) );
    }

    public Map<String, Object> updateGroupNotifierRPC( int projectgroupid, Map<String, Object> newNotifier )
        throws Exception
    {
        return serializeObject( this.updateGroupNotifier( projectgroupid,
            (ProjectNotifier) unserializeObject( newNotifier ) ) );
    }

    public Map<String, Object> updateNotifierRPC( int projectid, Map<String, Object> newNotifier )
        throws Exception
    {
        return serializeObject( this.updateNotifier( projectid, (ProjectNotifier) unserializeObject( newNotifier ) ) );
    }

    public Map<String, Object> addGroupNotifierRPC( int projectgroupid, Map<String, Object> newNotifier )
        throws Exception
    {
        return serializeObject( this.addGroupNotifier( projectgroupid,
            (ProjectNotifier) unserializeObject( newNotifier ) ) );
    }

    public Map<String, Object> addNotifierRPC( int projectid, Map<String, Object> newNotifier )
        throws Exception
    {
        return serializeObject( this.addNotifier( projectid, (ProjectNotifier) unserializeObject( newNotifier ) ) );
    }

    public Map<String, Object> addInstallationRPC( Map<String, Object> installation )
        throws Exception
    {
        return serializeObject( this.addInstallation( (Installation) unserializeObject( installation ) ) );
    }

    public Map<String, Object> addProfileRPC( Map<String, Object> profile )
        throws Exception
    {
        return serializeObject( this.addProfile( (Profile) unserializeObject( profile ) ) );
    }

    public int updateInstallationRPC( Map<String, Object> installation )
        throws Exception
    {
        return this.updateInstallation( (Installation) unserializeObject( installation ) );
    }

    public int updateProfileRPC( Map<String, Object> profile )
        throws Exception
    {
        return this.updateProfile( (Profile) unserializeObject( profile ) );
    }

    public Map<String,Object> getReleaseResultRPC( int releaseId )
        throws Exception
    {
        return serializeObject( this.getReleaseResult( releaseId ) );
    }

    public List<Object> getReleaseResultsForProjectGroupRPC( int projectGroupId )
        throws Exception
    {
        return serializeObject( this.getReleaseResultsForProjectGroup( projectGroupId ) );
    }

    public int removeReleaseResultRPC( Map<String, Object> rr )
        throws Exception
    {
        return serializeObject( this.removeReleaseResult( (ContinuumReleaseResult) unserializeObject( rr ) ) );
    }

    public Map<String, Object> addRepositoryPurgeConfigurationRPC( Map<String, Object> repoPurge )
        throws Exception
    {
        return serializeObject( this.addRepositoryPurgeConfiguration( (RepositoryPurgeConfiguration) unserializeObject( repoPurge ) ) );
    }

    public int updateRepositoryPurgeConfigurationRPC( Map<String, Object> repoPurge )
        throws Exception
    {
        return serializeObject( this.updateRepositoryPurgeConfiguration( (RepositoryPurgeConfiguration) unserializeObject( repoPurge ) ) );
    }

    public Map<String, Object> getRepositoryPurgeConfigurationRPC( int repoPurgeId )
        throws Exception
    {
        return serializeObject( this.getRepositoryPurgeConfiguration( repoPurgeId ) );
    }

    public List<Object> getAllRepositoryPurgeConfigurationsRPC()
        throws Exception
    {
        return serializeObject( this.getAllDirectoryPurgeConfigurations() );
    }

    public Map<String, Object> addDirectoryPurgeConfigurationRPC( Map<String, Object> dirPurge )
        throws Exception
    {
        return serializeObject( this.addDirectoryPurgeConfiguration( (DirectoryPurgeConfiguration) unserializeObject( dirPurge ) ) );
    }

    public int updateDirectoryPurgeConfigurationRPC( Map<String, Object> dirPurge )
        throws Exception
    {
        return serializeObject( this.updateDirectoryPurgeConfiguration( (DirectoryPurgeConfiguration) unserializeObject( dirPurge ) ) );
    }

    public Map<String, Object> getDirectoryPurgeConfigurationRPC( int dirPurgeId )
        throws Exception
    {
        return serializeObject( this.getDirectoryPurgeConfiguration( dirPurgeId ) );
    }

    public List<Object> getAllDirectoryPurgeConfigurationsRPC()
        throws Exception
    {
        return serializeObject( this.getAllRepositoryPurgeConfigurations() );
    }

    public Map<String, Object> addLocalRepositoryRPC( Map<String, Object> repository )
        throws Exception
    {
        return serializeObject( this.addLocalRepository( (LocalRepository) unserializeObject( repository ) ) );
    }

    public int updateLocalRepositoryRPC( Map<String, Object> repository )
        throws Exception
    {
        return serializeObject( this.updateLocalRepository( (LocalRepository) unserializeObject( repository ) ) );
    }

    public Map<String, Object> getLocalRepositoryRPC( int repositoryId )
        throws Exception
    {
        return serializeObject( this.getLocalRepository( repositoryId ) );
    }

    public List<Object> getAllLocalRepositoriesRPC()
        throws Exception
    {
        return serializeObject( this.getAllLocalRepositories() );
    }
}
