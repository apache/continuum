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
import org.apache.maven.continuum.xmlrpc.project.AddingResult;
import org.apache.maven.continuum.xmlrpc.project.BuildResult;
import org.apache.maven.continuum.xmlrpc.project.BuildResultSummary;
import org.apache.maven.continuum.xmlrpc.project.Project;
import org.apache.maven.continuum.xmlrpc.project.ProjectDependency;
import org.apache.maven.continuum.xmlrpc.project.ProjectGroup;
import org.apache.maven.continuum.xmlrpc.project.ProjectGroupSummary;
import org.apache.maven.continuum.xmlrpc.project.ProjectSummary;

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
    /**
     * @plexus.requirement
     */
    private Continuum continuum;

    // ----------------------------------------------------------------------
    // Projects
    // ----------------------------------------------------------------------

    public List getProjects( int projectGroupId )
        throws ContinuumException
    {
        checkViewProjectGroupAuthorization( getProjectGroupName( projectGroupId ) );

        List projectsList = new ArrayList();

        Collection projects = continuum.getProjects();
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

    // ----------------------------------------------------------------------
    // Projects Groups
    // ----------------------------------------------------------------------

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

    // ----------------------------------------------------------------------
    // Converters
    // ----------------------------------------------------------------------

    private ProjectSummary populateProjectSummary( org.apache.maven.continuum.model.project.Project project )
    {
        if ( project == null )
        {
            return null;
        }

        Project ps = new Project();
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

        //TODO
        //p.setBuildDefinitions( );
        //p.setDevelopers( );
        //p.setNotifiers( );
        return p;
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
        //project.setProjectGroup( populateProjectGroupSummary( projectSummary.getProjectGroup() ) );
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

        ProjectGroup g = new ProjectGroup();
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
        BuildResult br = new BuildResult();
        br.setBuildNumber( buildResult.getBuildNumber() );
        br.setEndTime( buildResult.getEndTime() );
        br.setError( buildResult.getError() );
        br.setExitCode( buildResult.getExitCode() );
        br.setId( buildResult.getId() );
        br.setStartTime( buildResult.getStartTime() );
        br.setState( buildResult.getState() );
        br.setSuccess( buildResult.isSuccess() );
        br.setTrigger( buildResult.getTrigger() );
        return br;
    }

    private BuildResult populateBuildResult( org.apache.maven.continuum.model.project.BuildResult buildResult )
    {
        if ( buildResult == null )
        {
            return null;
        }
        BuildResult br = (BuildResult) populateBuildResultSummary( buildResult );
        //TODO:
        //br.setScmResult( populateScmResult(br.getScmResult()));
        if ( buildResult.getModifiedDependencies() != null )
        {
            List deps = new ArrayList();
            for ( Iterator i = buildResult.getModifiedDependencies().iterator(); i.hasNext(); )
            {
                org.apache.maven.continuum.model.project.ProjectDependency dependency =
                    (org.apache.maven.continuum.model.project.ProjectDependency) i.next();
                ProjectDependency dep = populateProjectDependency( dependency );
                deps.add( dep );
            }
            //br.setModifiedDependencies( deps );
        }
        //br.setProject( populateProjectSummary(buildResult.getProject() ) );
        //br.setTestResult( populateTestResult( buildResult.getTestResult() ) );
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
}
