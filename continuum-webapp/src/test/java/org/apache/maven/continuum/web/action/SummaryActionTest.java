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
import org.jmock.Mock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SummaryActionTest
    extends AbstractActionTest
{
    private SummaryActionStub action;

    private Mock continuum;

    private Mock configurationService;

    private Mock buildsManager;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        action = new SummaryActionStub();

        continuum = mock( Continuum.class );

        configurationService = mock( ConfigurationService.class );

        buildsManager = mock( BuildsManager.class );

        action.setContinuum( (Continuum) continuum.proxy() );
        action.setParallelBuildsManager( (BuildsManager) buildsManager.proxy() );
    }

    public void testLatestBuildIdWhenCurrentlyBuildingInDistributedBuild()
        throws Exception
    {
        Collection<Project> projectsInGroup = createProjectsInGroup( 1, ContinuumProjectState.BUILDING );
        Map<Integer, BuildResult> buildResults = createBuildResults( 0, ContinuumProjectState.OK );
        Map<Integer, BuildResult> buildResultsInSuccess = new HashMap<Integer, BuildResult>();

        continuum.expects( once() ).method( "getProjectsInGroup" ).will( returnValue( projectsInGroup ) );
        continuum.expects( once() ).method( "getLatestBuildResults" ).will( returnValue( buildResults ) );
        continuum.expects( once() ).method( "getBuildResultsInSuccess" ).will( returnValue( buildResultsInSuccess ) );

        buildsManager.expects( once() ).method( "isInAnyBuildQueue" ).will( returnValue( false ) );
        buildsManager.expects( once() ).method( "isInPrepareBuildQueue" ).will( returnValue( false ) );
        buildsManager.expects( once() ).method( "isInAnyCheckoutQueue" ).will( returnValue( false ) );

        continuum.expects( once() ).method( "getConfiguration" ).will( returnValue(
            (ConfigurationService) configurationService.proxy() ) );
        configurationService.expects( once() ).method( "isDistributedBuildEnabled" ).will( returnValue( true ) );

        action.execute();
        continuum.verify();

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

        continuum.expects( once() ).method( "getProjectsInGroup" ).will( returnValue( projectsInGroup ) );
        continuum.expects( once() ).method( "getLatestBuildResults" ).will( returnValue( buildResults ) );
        continuum.expects( once() ).method( "getBuildResultsInSuccess" ).will( returnValue( buildResultsInSuccess ) );

        buildsManager.expects( once() ).method( "isInAnyBuildQueue" ).will( returnValue( false ) );
        buildsManager.expects( once() ).method( "isInPrepareBuildQueue" ).will( returnValue( false ) );
        buildsManager.expects( once() ).method( "isInAnyCheckoutQueue" ).will( returnValue( false ) );

        continuum.expects( once() ).method( "getConfiguration" ).will( returnValue(
            (ConfigurationService) configurationService.proxy() ) );
        configurationService.expects( once() ).method( "isDistributedBuildEnabled" ).will( returnValue( true ) );

        action.execute();
        continuum.verify();

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

        continuum.expects( once() ).method( "getProjectsInGroup" ).will( returnValue( projectsInGroup ) );
        continuum.expects( once() ).method( "getLatestBuildResults" ).will( returnValue( buildResults ) );
        continuum.expects( once() ).method( "getBuildResultsInSuccess" ).will( returnValue( buildResultsInSuccess ) );

        buildsManager.expects( once() ).method( "isInAnyBuildQueue" ).will( returnValue( false ) );
        buildsManager.expects( once() ).method( "isInPrepareBuildQueue" ).will( returnValue( false ) );
        buildsManager.expects( once() ).method( "isInAnyCheckoutQueue" ).will( returnValue( false ) );

        continuum.expects( once() ).method( "getConfiguration" ).will( returnValue(
            (ConfigurationService) configurationService.proxy() ) );
        configurationService.expects( once() ).method( "isDistributedBuildEnabled" ).will( returnValue( false ) );

        action.execute();
        continuum.verify();

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
