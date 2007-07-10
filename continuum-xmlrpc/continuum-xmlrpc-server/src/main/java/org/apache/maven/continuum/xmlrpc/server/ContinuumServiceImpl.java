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

import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.apache.maven.continuum.security.ContinuumRoleConstants;
import org.apache.maven.continuum.xmlrpc.project.AddingResult;
import org.apache.maven.continuum.xmlrpc.project.BuildDefinition;
import org.apache.maven.continuum.xmlrpc.project.BuildResult;
import org.apache.maven.continuum.xmlrpc.project.BuildResultSummary;
import org.apache.maven.continuum.xmlrpc.project.Project;
import org.apache.maven.continuum.xmlrpc.project.ProjectDependency;
import org.apache.maven.continuum.xmlrpc.project.ProjectDeveloper;
import org.apache.maven.continuum.xmlrpc.project.ProjectGroup;
import org.apache.maven.continuum.xmlrpc.project.ProjectGroupSummary;
import org.apache.maven.continuum.xmlrpc.project.ProjectNotifier;
import org.apache.maven.continuum.xmlrpc.project.ProjectSummary;
import org.apache.maven.continuum.xmlrpc.scm.ChangeFile;
import org.apache.maven.continuum.xmlrpc.scm.ChangeSet;
import org.apache.maven.continuum.xmlrpc.scm.ScmResult;
import org.apache.maven.continuum.xmlrpc.test.SuiteResult;
import org.apache.maven.continuum.xmlrpc.test.TestCaseFailure;
import org.apache.maven.continuum.xmlrpc.test.TestResult;
import org.codehaus.plexus.redback.authorization.AuthorizationException;
import org.codehaus.plexus.redback.role.RoleManager;
import org.codehaus.plexus.redback.role.RoleManagerException;
import org.codehaus.plexus.util.StringUtils;

import java.util.ArrayList;
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
    /**
     * @plexus.requirement
     */
    private Continuum continuum;

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

    public List getAllProjectGroups()
        throws ContinuumException
    {
        Collection pgList = continuum.getAllProjectGroups();
        List result = new ArrayList();
        for ( Iterator i = pgList.iterator(); i.hasNext(); )
        {
            org.apache.maven.continuum.model.project.ProjectGroup projectGroup =
                (org.apache.maven.continuum.model.project.ProjectGroup) i.next();
            try
            {
                if ( isAuthorized( ContinuumRoleConstants.CONTINUUM_VIEW_GROUP_OPERATION, projectGroup.getName() ) )
                {
                    result.add( populateProjectGroupWithProjects( projectGroup ) );
                }
            }
            catch ( AuthorizationException e )
            {
                throw new ContinuumException( "error authorizing request." );
            }
        }
        return result;
    }

    public List getAllProjectGroupsWithProjects()
        throws ContinuumException
    {
        Collection pgList = continuum.getAllProjectGroupsWithProjects();
        List result = new ArrayList();
        for ( Iterator i = pgList.iterator(); i.hasNext(); )
        {
            org.apache.maven.continuum.model.project.ProjectGroup projectGroup =
                (org.apache.maven.continuum.model.project.ProjectGroup) i.next();
            try
            {
                if ( isAuthorized( ContinuumRoleConstants.CONTINUUM_VIEW_GROUP_OPERATION, projectGroup.getName() ) )
                {
                    result.add( populateProjectGroupWithProjects( projectGroup ) );
                }
            }
            catch ( AuthorizationException e )
            {
                throw new ContinuumException( "error authorizing request." );
            }
        }
        return result;
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
        return populateProjectGroupWithProjects( projectGroup );
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
        checkAddProjectBuildDefinitionAuthorization( getProjectSummary( projectId ).getName() );

        org.apache.maven.continuum.model.project.BuildDefinition bd = populateBuildDefinition( buildDef );
        bd = continuum.addBuildDefinitionToProject( projectId, bd );
        return populateBuildDefinition( bd );
    }

    public BuildDefinition addBuildDefinitionToProjectGroup( int projectGroupId, BuildDefinition buildDef )
        throws ContinuumException
    {
        checkAddGroupBuildDefinitionAuthorization( getPGSummary( projectGroupId ).getName() );

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

        continuum.buildProject( projectId, buildDefintionId );
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

    public List getBuildResultsForProject( int projectId )
        throws ContinuumException
    {
        ProjectSummary ps = getProjectSummary( projectId );
        checkViewProjectGroupAuthorization( ps.getProjectGroup().getName() );

        List result = new ArrayList();
        Collection buildResults = continuum.getBuildResultsForProject( projectId );
        if ( buildResults != null )
        {
            for ( Iterator i = buildResults.iterator(); i.hasNext(); )
            {
                org.apache.maven.continuum.model.project.BuildResult buildResult =
                    (org.apache.maven.continuum.model.project.BuildResult) i.next();
                BuildResultSummary br = populateBuildResultSummary( buildResult );
                result.add( br );
            }
        }

        return result;
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

        int projectId = continuum.addProject( populateProject( project ), "ant" );
        return getProjectSummary( projectId );
    }

    public ProjectSummary addAntProject( ProjectSummary project, int projectGroupId )
        throws ContinuumException
    {
        checkAddProjectGroupAuthorization();

        int projectId = continuum.addProject( populateProject( project ), "ant", projectGroupId );
        return getProjectSummary( projectId );
    }

    // ----------------------------------------------------------------------
    // Maven Shell projects
    // ----------------------------------------------------------------------

    public ProjectSummary addShellProject( ProjectSummary project )
        throws ContinuumException
    {
        checkAddProjectGroupAuthorization();

        int projectId = continuum.addProject( populateProject( project ), "shell" );
        return getProjectSummary( projectId );
    }

    public ProjectSummary addShellProject( ProjectSummary project, int projectGroupId )
        throws ContinuumException
    {
        checkAddProjectGroupAuthorization();

        int projectId = continuum.addProject( populateProject( project ), "shell", projectGroupId );
        return getProjectSummary( projectId );
    }

    // ----------------------------------------------------------------------
    // Converters
    // ----------------------------------------------------------------------

    private ProjectSummary populateProjectSummary( org.apache.maven.continuum.model.project.Project project )
    {
        if ( project == null )
        {
            return null;
        }

        ProjectSummary ps = new Project();
        ps.setArtifactId( project.getArtifactId() );
        ps.setBuildNumber( project.getBuildNumber() );
        ps.setDescription( project.getDescription() );
        ps.setExecutorId( project.getExecutorId() );
        ps.setGroupId( project.getGroupId() );
        ps.setId( project.getId() );
        ps.setLatestBuildId( project.getLatestBuildId() );
        ps.setName( project.getName() );
        ps.setProjectGroup( populateProjectGroupSummary( project.getProjectGroup() ) );
        ps.setScmTag( project.getScmTag() );
        ps.setScmUrl( project.getScmUrl() );
        ps.setScmUseCache( project.isScmUseCache() );
        ps.setScmUsername( project.getScmUsername() );
        ps.setState( project.getState() );
        ps.setUrl( project.getUrl() );
        ps.setVersion( project.getVersion() );
        ps.setWorkingDirectory( project.getWorkingDirectory() );
        return ps;
    }

    private Project populateProject( org.apache.maven.continuum.model.project.Project project )
    {
        if ( project == null )
        {
            return null;
        }

        Project p = (Project) populateProjectSummary( project );

        p.setParent( populateProjectDependency( project.getParent() ) );

        if ( project.getDependencies() != null )
        {
            List deps = new ArrayList();
            for ( Iterator i = project.getDependencies().iterator(); i.hasNext(); )
            {
                org.apache.maven.continuum.model.project.ProjectDependency d =
                    (org.apache.maven.continuum.model.project.ProjectDependency) i.next();
                deps.add( populateProjectDependency( d ) );
            }
            p.setDependencies( deps );
        }

        //TODO: p.setBuildDefinitions( );

        if ( project.getDevelopers() != null )
        {
            for ( Iterator i = project.getDevelopers().iterator(); i.hasNext(); )
            {
                org.apache.maven.continuum.model.project.ProjectDeveloper developer =
                    (org.apache.maven.continuum.model.project.ProjectDeveloper) i.next();
                p.addDeveloper( populateProjectDeveloper( developer ) );
            }
        }

        if ( project.getNotifiers() != null )
        {
            for ( Iterator i = project.getNotifiers().iterator(); i.hasNext(); )
            {
                org.apache.maven.continuum.model.project.ProjectNotifier notifier =
                    (org.apache.maven.continuum.model.project.ProjectNotifier) i.next();
                p.addNotifier( populateProjectNotifier( notifier ) );
            }
        }

        return p;
    }

    private ProjectDeveloper populateProjectDeveloper(
        org.apache.maven.continuum.model.project.ProjectDeveloper developer )
    {
        if ( developer == null )
        {
            return null;
        }

        ProjectDeveloper res = new ProjectDeveloper();
        res.setContinuumId( developer.getContinuumId() );
        res.setEmail( developer.getEmail() );
        res.setName( developer.getName() );
        res.setScmId( developer.getScmId() );
        return res;
    }

    private ProjectNotifier populateProjectNotifier( org.apache.maven.continuum.model.project.ProjectNotifier notifier )
    {
        if ( notifier == null )
        {
            return null;
        }

        ProjectNotifier res = new ProjectNotifier();
        res.setEnabled( notifier.isEnabled() );
        res.setFrom( notifier.getFrom() );
        res.setId( notifier.getId() );
        res.setRecipientType( notifier.getRecipientType() );
        res.setSendOnError( notifier.isSendOnError() );
        res.setSendOnFailure( notifier.isSendOnFailure() );
        res.setSendOnSuccess( notifier.isSendOnSuccess() );
        res.setSendOnWarning( notifier.isSendOnWarning() );
        res.setType( notifier.getType() );

        if ( notifier.getConfiguration() != null )
        {
            Map conf = new HashMap();
            for ( Iterator i = notifier.getConfiguration().keySet().iterator(); i.hasNext(); )
            {
                String key = (String) i.next();
                conf.put( key, notifier.getConfiguration().get( key ) );
            }
            res.setConfiguration( conf );
        }
        return res;
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
        //TODO: project.setProjectGroup( populateProjectGroupSummary( projectSummary.getProjectGroup() ) );
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

    private ProjectDependency populateProjectDependency(
        org.apache.maven.continuum.model.project.ProjectDependency dependency )
    {
        if ( dependency == null )
        {
            return null;
        }

        ProjectDependency dep = new ProjectDependency();
        dep.setArtifactId( dependency.getArtifactId() );
        dep.setGroupId( dependency.getGroupId() );
        dep.setVersion( dependency.getVersion() );
        return dep;
    }

    private ProjectGroupSummary populateProjectGroupSummary(
        org.apache.maven.continuum.model.project.ProjectGroup group )
    {
        if ( group == null )
        {
            return null;
        }

        ProjectGroupSummary g = new ProjectGroup();
        g.setDescription( group.getDescription() );
        g.setGroupId( group.getGroupId() );
        g.setId( group.getId() );
        g.setName( group.getName() );
        return g;
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

    private ProjectGroup populateProjectGroupWithProjects( org.apache.maven.continuum.model.project.ProjectGroup group )
    {
        if ( group == null )
        {
            return null;
        }
        ProjectGroup g = (ProjectGroup) populateProjectGroupSummary( group );

        if ( group.getProjects() != null )
        {
            List projects = new ArrayList();
            for ( Iterator i = group.getProjects().iterator(); i.hasNext(); )
            {
                org.apache.maven.continuum.model.project.Project p =
                    (org.apache.maven.continuum.model.project.Project) i.next();
                ProjectSummary ps = populateProjectSummary( p );
                projects.add( ps );
            }
            g.setProjects( projects );
        }
        return g;
    }

    private BuildResultSummary populateBuildResultSummary(
        org.apache.maven.continuum.model.project.BuildResult buildResult )
    {
        if ( buildResult == null )
        {
            return null;
        }
        BuildResultSummary br = new BuildResult();
        br.setBuildNumber( buildResult.getBuildNumber() );
        br.setEndTime( buildResult.getEndTime() );
        br.setError( buildResult.getError() );
        br.setExitCode( buildResult.getExitCode() );
        br.setId( buildResult.getId() );
        br.setStartTime( buildResult.getStartTime() );
        br.setState( buildResult.getState() );
        br.setSuccess( buildResult.isSuccess() );
        br.setTrigger( buildResult.getTrigger() );
        br.setProject( populateProjectSummary( buildResult.getProject() ) );
        return br;
    }

    private BuildResult populateBuildResult( org.apache.maven.continuum.model.project.BuildResult buildResult )
        throws ContinuumException
    {
        if ( buildResult == null )
        {
            return null;
        }
        BuildResult br = (BuildResult) populateBuildResultSummary( buildResult );

        List changeSet = continuum.getChangesSinceLastSuccess( br.getProject().getId(), br.getId() );
        if ( changeSet != null )
        {
            for ( Iterator i = changeSet.iterator(); i.hasNext(); )
            {
                br.addChangesSinceLastSucces(
                    populateChangeSet( (org.apache.maven.continuum.model.scm.ChangeSet) i.next() ) );
            }
        }

        br.setScmResult( populateScmResult( buildResult.getScmResult() ) );

        if ( buildResult.getModifiedDependencies() != null )
        {
            for ( Iterator i = buildResult.getModifiedDependencies().iterator(); i.hasNext(); )
            {
                org.apache.maven.continuum.model.project.ProjectDependency dependency =
                    (org.apache.maven.continuum.model.project.ProjectDependency) i.next();
                ProjectDependency dep = populateProjectDependency( dependency );
                br.addModifiedDependency( dep );
            }
        }
        br.setTestResult( populateTestResult( buildResult.getTestResult() ) );
        return br;
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

    private ScmResult populateScmResult( org.apache.maven.continuum.model.scm.ScmResult scmResult )
    {
        if ( scmResult == null )
        {
            return null;
        }

        ScmResult res = new ScmResult();

        if ( scmResult.getChanges() != null )
        {
            for ( Iterator i = scmResult.getChanges().iterator(); i.hasNext(); )
            {
                org.apache.maven.continuum.model.scm.ChangeSet changeSet =
                    (org.apache.maven.continuum.model.scm.ChangeSet) i.next();
                res.addChange( populateChangeSet( changeSet ) );
            }
        }

        res.setCommandLine( scmResult.getCommandLine() );
        res.setCommandOutput( scmResult.getCommandOutput() );
        res.setException( scmResult.getException() );
        res.setProviderMessage( scmResult.getProviderMessage() );
        res.setSuccess( scmResult.isSuccess() );
        return res;
    }

    private ChangeSet populateChangeSet( org.apache.maven.continuum.model.scm.ChangeSet changeSet )
    {
        if ( changeSet == null )
        {
            return null;
        }

        ChangeSet res = new ChangeSet();
        res.setAuthor( changeSet.getAuthor() );
        res.setComment( changeSet.getComment() );
        res.setDate( changeSet.getDate() );

        if ( changeSet.getFiles() != null )
        {
            for ( Iterator i = changeSet.getFiles().iterator(); i.hasNext(); )
            {
                org.apache.maven.continuum.model.scm.ChangeFile changeFile =
                    (org.apache.maven.continuum.model.scm.ChangeFile) i.next();
                res.addFile( populateChangeFile( changeFile ) );
            }
        }

        res.setId( changeSet.getId() );
        return res;
    }

    private ChangeFile populateChangeFile( org.apache.maven.continuum.model.scm.ChangeFile changeFile )
    {
        if ( changeFile == null )
        {
            return null;
        }

        ChangeFile res = new ChangeFile();
        res.setName( changeFile.getName() );
        res.setRevision( changeFile.getRevision() );
        res.setStatus( changeFile.getStatus() );
        return res;
    }

    private TestResult populateTestResult( org.apache.maven.continuum.model.scm.TestResult testResult )
    {
        if ( testResult == null )
        {
            return null;
        }

        TestResult res = new TestResult();
        res.setFailureCount( testResult.getFailureCount() );

        if ( testResult.getSuiteResults() != null )
        {
            for ( Iterator i = testResult.getSuiteResults().iterator(); i.hasNext(); )
            {
                org.apache.maven.continuum.model.scm.SuiteResult suiteResult =
                    (org.apache.maven.continuum.model.scm.SuiteResult) i.next();
                res.addSuiteResult( populateSuiteResult( suiteResult ) );
            }
        }

        res.setTestCount( testResult.getTestCount() );
        res.setTotalTime( testResult.getTotalTime() );
        return res;
    }

    private SuiteResult populateSuiteResult( org.apache.maven.continuum.model.scm.SuiteResult suiteresult )
    {
        if ( suiteresult == null )
        {
            return null;
        }

        SuiteResult res = new SuiteResult();
        res.setFailureCount( suiteresult.getFailureCount() );

        if ( suiteresult.getFailures() != null )
        {
            for ( Iterator i = suiteresult.getFailures().iterator(); i.hasNext(); )
            {
                org.apache.maven.continuum.model.scm.TestCaseFailure failure =
                    (org.apache.maven.continuum.model.scm.TestCaseFailure) i.next();
                res.addFailure( populateTestCaseFailure( failure ) );
            }
        }

        res.setName( suiteresult.getName() );
        res.setTestCount( suiteresult.getTestCount() );
        res.setTotalTime( suiteresult.getTotalTime() );
        return res;
    }

    private TestCaseFailure populateTestCaseFailure( org.apache.maven.continuum.model.scm.TestCaseFailure failure )
    {
        if ( failure == null )
        {
            return null;
        }

        TestCaseFailure res = new TestCaseFailure();
        res.setException( failure.getException() );
        res.setName( failure.getName() );
        return res;
    }

    private BuildDefinition populateBuildDefinition( org.apache.maven.continuum.model.project.BuildDefinition buildDef )
    {
        if ( buildDef == null )
        {
            return null;
        }

        BuildDefinition bd = new BuildDefinition();
        bd.setArguments( buildDef.getArguments() );
        bd.setBuildFile( buildDef.getBuildFile() );
        bd.setBuildFresh( buildDef.isBuildFresh() );
        bd.setDefaultForProject( buildDef.isDefaultForProject() );
        bd.setGoals( buildDef.getGoals() );
        //TODO: bd.setProfile( populateProfile( buildDef.getProfile() ) );
        //TODO: bd.setSchedule( populateSchedule( buildDef.getSchedule() ) );
        return bd;
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
        //TODO: bd.setProfile( populateProfile( buildDef.getProfile() ) );
        //TODO: bd.setSchedule( populateSchedule( buildDef.getSchedule() ) );
        return bd;
    }
}
