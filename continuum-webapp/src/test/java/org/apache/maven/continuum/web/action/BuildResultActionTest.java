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

import com.opensymphony.xwork2.Action;
import org.apache.continuum.builder.distributed.manager.DistributedBuildManager;
import org.apache.continuum.buildmanager.BuildsManager;
import org.apache.continuum.model.project.ProjectRunSummary;
import org.apache.continuum.taskqueue.BuildProjectTask;
import org.apache.continuum.web.action.AbstractActionTest;
import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.configuration.ConfigurationException;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.web.action.stub.BuildResultActionStub;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class BuildResultActionTest
    extends AbstractActionTest
{
    private BuildResultActionStub action;

    private Continuum continuum;

    private ConfigurationService configurationService;

    private DistributedBuildManager distributedBuildManager;

    private BuildsManager buildsManager;

    private Project project;

    private BuildResult buildResult;

    @Before
    public void setUp()
        throws Exception
    {
        continuum = mock( Continuum.class );
        configurationService = mock( ConfigurationService.class );
        distributedBuildManager = mock( DistributedBuildManager.class );
        buildsManager = mock( BuildsManager.class );

        action = new BuildResultActionStub();
        action.setContinuum( continuum );
        action.setDistributedBuildManager( distributedBuildManager );

        project = createProject( "stub-project" );

        when( continuum.getProject( anyInt() ) ).thenReturn( project );
        when( continuum.getConfiguration() ).thenReturn( configurationService );
        when( continuum.getBuildsManager() ).thenReturn( buildsManager );

        buildResult = createBuildResult( project );
        when( continuum.getBuildResult( anyInt() ) ).thenReturn( buildResult );
    }

    @Test
    public void testViewPreviousBuild()
        throws Exception
    {
        when( configurationService.isDistributedBuildEnabled() ).thenReturn( false );
        when( configurationService.getTestReportsDirectory( anyInt(), anyInt() ) ).thenReturn(
            new File( "testReportsDir" ) );
        when( continuum.getChangesSinceLastSuccess( anyInt(), anyInt() ) ).thenReturn( null );
        when( configurationService.getBuildOutputFile( anyInt(), anyInt() ) ).thenReturn(
            new File( "buildOutputFile" ) );
        when( buildsManager.getCurrentBuilds() ).thenReturn( new HashMap<String, BuildProjectTask>() );

        assertEquals( Action.SUCCESS, action.execute() );
    }

    @Test
    public void testViewCurrentBuildInDistributedBuildAgent()
        throws Exception
    {
        int expectedResultId = 777;
        action.setBuildId( expectedResultId );
        buildResult.setState( org.apache.maven.continuum.project.ContinuumProjectState.BUILDING );
        ProjectRunSummary runSummary = new ProjectRunSummary();
        runSummary.setBuildResultId( expectedResultId );
        when( configurationService.isDistributedBuildEnabled() ).thenReturn( true );
        when( distributedBuildManager.getBuildResult( anyInt() ) ).thenReturn( new HashMap<String, Object>() );
        when( distributedBuildManager.getCurrentRun( anyInt(), anyInt() ) ).thenReturn( runSummary );

        assertEquals( Action.SUCCESS, action.execute() );

        verify( distributedBuildManager ).getBuildResult( anyInt() );
    }

    @Test
    public void testViewBuildSentToDistributedBuildAgent()
        throws Exception
    {
        int expectedResultId = 777;
        action.setBuildId( expectedResultId );
        buildResult.setState( org.apache.maven.continuum.project.ContinuumProjectState.SENT_TO_AGENT );
        ProjectRunSummary runSummary = new ProjectRunSummary();
        runSummary.setBuildResultId( expectedResultId );
        when( configurationService.isDistributedBuildEnabled() ).thenReturn( true );
        when( configurationService.getTestReportsDirectory( anyInt(), anyInt() ) ).thenReturn(
            new File( "non-existent" ) );
        when( configurationService.getBuildOutputFile( anyInt(), anyInt() ) ).thenReturn(
            new File( "nonExistentBuildOutputFile" ) );
        when( distributedBuildManager.getBuildResult( anyInt() ) ).thenReturn( new HashMap<String, Object>() );
        when( distributedBuildManager.getCurrentRun( anyInt(), anyInt() ) ).thenReturn( runSummary );

        assertEquals( Action.SUCCESS, action.execute() );

        verify( distributedBuildManager, never() ).getBuildResult( anyInt() );
    }

    @Test
    public void testSuccessfulWhenTestDirThrows()
        throws Exception
    {
        when( configurationService.getTestReportsDirectory( anyInt(), anyInt() ) ).thenThrow(
            new ConfigurationException( "failed creating dir" ) );
        when( configurationService.getBuildOutputFile( anyInt(), anyInt() ) ).thenReturn( new File( "non-existent" ) );

        assertEquals( Action.SUCCESS, action.execute() );
    }

    @Test
    public void testSuccessfulWhenBuildOutputDirThrows()
        throws Exception
    {
        when( configurationService.getTestReportsDirectory( anyInt(), anyInt() ) ).thenReturn(
            new File( "non-existent" ) );
        when( configurationService.getBuildOutputFile( anyInt(), anyInt() ) ).thenThrow(
            new ConfigurationException( "failed creating dir" ) );

        assertEquals( Action.SUCCESS, action.execute() );
    }

    private Project createProject( String name )
    {
        Project project = new Project();
        project.setId( 1 );
        project.setName( name );
        project.setArtifactId( "foo:bar" );
        project.setVersion( "1.0" );
        project.setState( ContinuumProjectState.BUILDING );

        return project;
    }

    private BuildResult createBuildResult( Project project )
    {
        BuildResult buildResult = new BuildResult();
        buildResult.setId( 1 );
        buildResult.setProject( project );
        buildResult.setBuildDefinition( new BuildDefinition() );

        return buildResult;
    }
}
