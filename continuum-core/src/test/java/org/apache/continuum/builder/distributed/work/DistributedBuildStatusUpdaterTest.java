package org.apache.continuum.builder.distributed.work;

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

import org.apache.continuum.builder.distributed.manager.DistributedBuildManager;
import org.apache.continuum.dao.BuildDefinitionDao;
import org.apache.continuum.dao.BuildResultDao;
import org.apache.continuum.dao.ProjectDao;
import org.apache.continuum.dao.ProjectScmRootDao;
import org.apache.continuum.model.project.ProjectRunSummary;
import org.apache.continuum.model.project.ProjectScmRoot;
import org.apache.maven.continuum.PlexusSpringTestCase;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class DistributedBuildStatusUpdaterTest
    extends PlexusSpringTestCase
{
    private ProjectDao projectDao;

    private ProjectScmRootDao projectScmRootDao;

    private BuildDefinitionDao buildDefinitionDao;

    private BuildResultDao buildResultDao;

    private DistributedBuildManager distributedBuildManager;

    private ConfigurationService configurationService;

    private DistributedBuildStatusUpdater worker;

    @Before
    public void setUp()
        throws Exception
    {
        projectDao = mock( ProjectDao.class );
        projectScmRootDao = mock( ProjectScmRootDao.class );
        buildDefinitionDao = mock( BuildDefinitionDao.class );
        buildResultDao = mock( BuildResultDao.class );
        configurationService = mock( ConfigurationService.class );
        distributedBuildManager = mock( DistributedBuildManager.class );

        worker = (DistributedBuildStatusUpdater) lookup( BuildStatusUpdater.class, "distributed" );
        worker.setBuildDefinitionDao( buildDefinitionDao );
        worker.setBuildResultDao( buildResultDao );
        worker.setProjectDao( projectDao );
        worker.setProjectScmRootDao( projectScmRootDao );
        worker.setConfigurationService( configurationService );
        worker.setDistributedBuildManager( distributedBuildManager );

        when( configurationService.isDistributedBuildEnabled() ).thenReturn( true );
        when( distributedBuildManager.getCurrentRuns() ).thenReturn( getCurrentRuns() );
    }

    @Test
    public void testWorkerWithStuckBuild()
        throws Exception
    {
        Project project1 = getProject( 1, ContinuumProjectState.BUILDING );
        when( projectScmRootDao.getProjectScmRoot( 1 ) ).thenReturn( getScmRoot( ContinuumProjectState.OK ) );
        when( projectDao.getProject( 1 ) ).thenReturn( project1 );
        when( distributedBuildManager.isProjectCurrentlyBuilding( 1, 1 ) ).thenReturn( false );
        when( buildDefinitionDao.getBuildDefinition( 1 ) ).thenReturn( new BuildDefinition() );
        when( projectDao.getProject( 2 ) ).thenReturn( getProject( 2, ContinuumProjectState.OK ) );

        worker.performScan();

        verify( buildResultDao ).addBuildResult( any( Project.class ), any( BuildResult.class ) );
        verify( projectDao ).updateProject( project1 );
    }

    @Test
    public void testWorkerWithStuckScm()
        throws Exception
    {
        ProjectScmRoot scmRootUpdating = getScmRoot( ContinuumProjectState.UPDATING );
        when( projectScmRootDao.getProjectScmRoot( 1 ) ).thenReturn( scmRootUpdating,
                                                                     getScmRoot( ContinuumProjectState.ERROR ) );
        when( distributedBuildManager.isProjectCurrentlyPreparingBuild( 1, 1 ) ).thenReturn( false );

        worker.performScan();

        verify( projectScmRootDao ).updateProjectScmRoot( scmRootUpdating );
    }

    private List<ProjectRunSummary> getCurrentRuns()
    {
        List<ProjectRunSummary> runs = new ArrayList<ProjectRunSummary>();

        ProjectRunSummary run1 = new ProjectRunSummary();
        run1.setProjectId( 1 );
        run1.setBuildDefinitionId( 1 );
        run1.setProjectGroupId( 1 );
        run1.setProjectScmRootId( 1 );
        run1.setTrigger( 1 );
        run1.setTriggeredBy( "user" );
        run1.setBuildAgentUrl( "http://localhost:8181/continuum-buildagent/xmlrpc" );
        runs.add( run1 );

        ProjectRunSummary run2 = new ProjectRunSummary();
        run2.setProjectId( 2 );
        run2.setBuildDefinitionId( 2 );
        run2.setProjectGroupId( 1 );
        run2.setProjectScmRootId( 1 );
        run2.setTrigger( 1 );
        run2.setTriggeredBy( "user" );
        run2.setBuildAgentUrl( "http://localhost:8181/continuum-buildagent/xmlrpc" );
        runs.add( run2 );

        return runs;
    }

    private ProjectScmRoot getScmRoot( int state )
    {
        ProjectScmRoot scmRoot = new ProjectScmRoot();
        scmRoot.setState( state );
        return scmRoot;
    }

    private Project getProject( int projectId, int state )
    {
        Project project = new Project();
        project.setId( projectId );
        project.setState( state );
        return project;
    }
}
