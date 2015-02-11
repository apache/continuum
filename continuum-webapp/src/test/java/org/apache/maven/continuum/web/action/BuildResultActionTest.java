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
import org.apache.continuum.taskqueue.BuildProjectTask;
import org.apache.continuum.web.action.AbstractActionTest;
import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.web.action.stub.BuildResultActionStub;
import org.apache.maven.continuum.xmlrpc.project.ContinuumProjectState;

import java.io.File;
import java.util.HashMap;

import static org.mockito.Mockito.*;

public class BuildResultActionTest
    extends AbstractActionTest
{
    private BuildResultActionStub action;

    private Continuum continuum;

    private ConfigurationService configurationService;

    private DistributedBuildManager distributedBuildManager;

    private BuildsManager buildsManager;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        continuum = mock( Continuum.class );
        configurationService = mock( ConfigurationService.class );
        distributedBuildManager = mock( DistributedBuildManager.class );
        buildsManager = mock( BuildsManager.class );

        action = new BuildResultActionStub();
        action.setContinuum( continuum );
        action.setDistributedBuildManager( distributedBuildManager );
    }

    public void testViewPreviousBuild()
        throws Exception
    {
        Project project = createProject( "stub-project" );
        BuildResult buildResult = createBuildResult( project );

        when( continuum.getProject( anyInt() ) ).thenReturn( project );
        when( continuum.getBuildResult( anyInt() ) ).thenReturn( buildResult );
        when( continuum.getConfiguration() ).thenReturn( configurationService );
        when( configurationService.isDistributedBuildEnabled() ).thenReturn( false );
        when( configurationService.getTestReportsDirectory( anyInt(), anyInt() ) ).thenReturn(
            new File( "testReportsDir" ) );
        when( continuum.getChangesSinceLastSuccess( anyInt(), anyInt() ) ).thenReturn( null );
        when( configurationService.getBuildOutputFile( anyInt(), anyInt() ) ).thenReturn(
            new File( "buildOutputFile" ) );
        when( continuum.getBuildsManager() ).thenReturn( buildsManager );
        when( buildsManager.getCurrentBuilds() ).thenReturn( new HashMap<String, BuildProjectTask>() );

        assertEquals( Action.SUCCESS, action.execute() );
    }

    public void testViewCurrentBuildInDistributedBuildAgent()
        throws Exception
    {
        Project project = createProject( "stub-project" );

        when( continuum.getProject( anyInt() ) ).thenReturn( project );
        when( continuum.getConfiguration() ).thenReturn( configurationService );
        when( configurationService.isDistributedBuildEnabled() ).thenReturn( true );
        when( distributedBuildManager.getBuildResult( anyInt() ) ).thenReturn( new HashMap<String, Object>() );

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

        return buildResult;
    }
}
