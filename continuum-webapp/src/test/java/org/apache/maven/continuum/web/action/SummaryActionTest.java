package org.apache.maven.continuum.web.action;

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

import org.apache.continuum.buildmanager.BuildsManager;
import org.apache.continuum.web.action.AbstractActionTest;
import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.web.action.stub.SummaryActionStub;
import org.apache.maven.continuum.web.model.ProjectSummary;
import org.apache.maven.continuum.xmlrpc.project.ContinuumProjectState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

public class SummaryActionTest
    extends AbstractActionTest
{
    private SummaryActionStub action;

    private Continuum continuum;

    private ConfigurationService configurationService;

    private BuildsManager buildsManager;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        continuum = mock( Continuum.class );
        configurationService = mock( ConfigurationService.class );
        buildsManager = mock( BuildsManager.class );

        action = new SummaryActionStub();
        action.setContinuum( continuum );
        action.setParallelBuildsManager( buildsManager );
    }

    public void testLatestBuildIdWhenCurrentlyBuildingInDistributedBuild()
        throws Exception
    {
        Collection<Project> projectsInGroup = createProjectsInGroup( 1, ContinuumProjectState.BUILDING );
        Map<Integer, BuildResult> buildResults = createBuildResults( 0, ContinuumProjectState.OK );
        Map<Integer, BuildResult> buildResultsInSuccess = new HashMap<Integer, BuildResult>();

        when( continuum.getProjectsInGroup( anyInt() ) ).thenReturn( projectsInGroup );
        when( continuum.getLatestBuildResults( anyInt() ) ).thenReturn( buildResults );
        when( continuum.getBuildResultsInSuccess( anyInt() ) ).thenReturn( buildResultsInSuccess );
        when( buildsManager.isInAnyBuildQueue( anyInt() ) ).thenReturn( false );
        when( buildsManager.isInPrepareBuildQueue( anyInt() ) ).thenReturn( false );
        when( buildsManager.isInAnyCheckoutQueue( anyInt() ) ).thenReturn( false );
        when( continuum.getConfiguration() ).thenReturn( configurationService );
        when( configurationService.isDistributedBuildEnabled() ).thenReturn( true );

        action.execute(); // expected result?

        List<ProjectSummary> projects = action.getProjects();

        assertNotNull( projects );
        assertEquals( 1, projects.size() );

        ProjectSummary summary = projects.get( 0 );
        assertEquals( 0, summary.getLatestBuildId() );
    }

    public void testLatestBuildIdInDistributedBuild()
        throws Exception
    {
        Collection<Project> projectsInGroup = createProjectsInGroup( 1, ContinuumProjectState.OK );
        Map<Integer, BuildResult> buildResults = createBuildResults( 1, ContinuumProjectState.OK );
        Map<Integer, BuildResult> buildResultsInSuccess = new HashMap<Integer, BuildResult>();

        when( continuum.getProjectsInGroup( anyInt() ) ).thenReturn( projectsInGroup );
        when( continuum.getLatestBuildResults( anyInt() ) ).thenReturn( buildResults );
        when( continuum.getBuildResultsInSuccess( anyInt() ) ).thenReturn( buildResultsInSuccess );
        when( buildsManager.isInAnyBuildQueue( anyInt() ) ).thenReturn( false );
        when( buildsManager.isInPrepareBuildQueue( anyInt() ) ).thenReturn( false );
        when( buildsManager.isInAnyCheckoutQueue( anyInt() ) ).thenReturn( false );
        when( continuum.getConfiguration() ).thenReturn( configurationService );
        when( configurationService.isDistributedBuildEnabled() ).thenReturn( true );

        action.execute();  // expected result?

        List<ProjectSummary> projects = action.getProjects();

        assertNotNull( projects );
        assertEquals( 1, projects.size() );

        ProjectSummary summary = projects.get( 0 );
        assertEquals( 1, summary.getLatestBuildId() );
    }

    public void testLatestBuildIdWhenCurrentlyBuilding()
        throws Exception
    {
        Collection<Project> projectsInGroup = createProjectsInGroup( 1, ContinuumProjectState.BUILDING );
        Map<Integer, BuildResult> buildResults = createBuildResults( 1, ContinuumProjectState.BUILDING );
        Map<Integer, BuildResult> buildResultsInSuccess = new HashMap<Integer, BuildResult>();

        when( continuum.getProjectsInGroup( anyInt() ) ).thenReturn( projectsInGroup );
        when( continuum.getLatestBuildResults( anyInt() ) ).thenReturn( buildResults );
        when( continuum.getBuildResultsInSuccess( anyInt() ) ).thenReturn( buildResultsInSuccess );
        when( buildsManager.isInAnyBuildQueue( anyInt() ) ).thenReturn( false );
        when( buildsManager.isInPrepareBuildQueue( anyInt() ) ).thenReturn( false );
        when( buildsManager.isInAnyCheckoutQueue( anyInt() ) ).thenReturn( false );
        when( continuum.getConfiguration() ).thenReturn( configurationService );
        when( configurationService.isDistributedBuildEnabled() ).thenReturn( false );

        action.execute();  // expected result?

        List<ProjectSummary> projects = action.getProjects();

        assertNotNull( projects );
        assertEquals( 1, projects.size() );

        ProjectSummary summary = projects.get( 0 );
        assertEquals( 1, summary.getLatestBuildId() );
    }

    private Collection<Project> createProjectsInGroup( int projectId, int state )
    {
        Collection<Project> projectsInGroup = new ArrayList<Project>();

        ProjectGroup group = new ProjectGroup();
        group.setId( 1 );
        group.setName( "test-group" );

        Project project = new Project();
        project.setId( projectId );
        project.setName( "test-project" );
        project.setVersion( "1.0" );
        project.setBuildNumber( 1 );
        project.setState( state );
        project.setExecutorId( "maven2" );
        project.setProjectGroup( group );

        projectsInGroup.add( project );

        return projectsInGroup;
    }

    private Map<Integer, BuildResult> createBuildResults( int projectId, int state )
    {
        Map<Integer, BuildResult> buildResults = new HashMap<Integer, BuildResult>();

        BuildResult br = new BuildResult();
        br.setId( 1 );
        br.setStartTime( System.currentTimeMillis() );
        br.setEndTime( System.currentTimeMillis() );
        br.setState( state );

        buildResults.put( projectId, br );

        return buildResults;
    }
}
