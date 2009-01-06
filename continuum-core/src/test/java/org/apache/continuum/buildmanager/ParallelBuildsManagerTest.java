package org.apache.continuum.buildmanager;

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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.continuum.buildqueue.BuildQueueService;
import org.apache.continuum.dao.BuildDefinitionDao;
import org.apache.continuum.taskqueue.OverallBuildQueue;
import org.apache.maven.continuum.buildqueue.BuildProjectTask;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildQueue;
import org.apache.maven.continuum.model.project.Schedule;
import org.codehaus.plexus.spring.PlexusInSpringTestCase;
import org.codehaus.plexus.taskqueue.Task;
import org.codehaus.plexus.taskqueue.TaskQueue;
import org.codehaus.plexus.taskqueue.TaskQueueException;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit3.JUnit3Mockery;

/**
 * 
 * @author <a href="mailto:oching@apache.org">Maria Odea Ching</a>
 */
public class ParallelBuildsManagerTest
    extends PlexusInSpringTestCase
{
    private ParallelBuildsManager buildsManager;

    Mockery context;

    private BuildDefinitionDao buildDefinitionDao;
    
    private TaskQueue prepareBuildQueue;
    
    private ConfigurationService configurationService;
    
    private BuildQueueService buildQueueService;
    
    private OverallBuildQueue overallBuildQueue;
    
    private TaskQueue buildQueue;
    
    private TaskQueue checkoutQueue;

    public void setUp()
        throws Exception
    {
        super.setUp();

        buildsManager = (ParallelBuildsManager) lookup( BuildsManager.class, "parallel" );

        context = new JUnit3Mockery();

        buildDefinitionDao = context.mock( BuildDefinitionDao.class );

        buildsManager.setBuildDefinitionDao( buildDefinitionDao );
        
        prepareBuildQueue = context.mock( TaskQueue.class, "prepare-build-queue" );
        
        buildsManager.setPrepareBuildQueue( prepareBuildQueue );
        
        configurationService = context.mock( ConfigurationService.class );
        
        buildsManager.setConfigurationService( configurationService );
        
        buildQueueService = context.mock( BuildQueueService.class );
        
        buildsManager.setBuildQueueService( buildQueueService );        

        buildQueue = context.mock( TaskQueue.class, "build-queue" );
        
        checkoutQueue = context.mock( TaskQueue.class, "checkout-queue" );
    }

    public void tearDown()
        throws Exception
    {
        super.tearDown();

        buildsManager = null;
    }

    private List<BuildQueue> getBuildQueues( int start, int end )
    {
        List<BuildQueue> buildQueues = new ArrayList<BuildQueue>();
        for ( int i = start; i <= end; i++ )
        {
            BuildQueue buildQueue = new BuildQueue();
            buildQueue.setId( i );
            if ( i == 1 )
            {
                buildQueue.setName( ConfigurationService.DEFAULT_BUILD_QUEUE_NAME );
            }
            else
            {
                buildQueue.setName( "BUILD_QUEUE_" + String.valueOf( i ) );
            }
            buildQueues.add( buildQueue );
        }

        return buildQueues;
    }

    private Schedule getSchedule( int id, int start, int end )
    {
        Schedule schedule = new Schedule();
        schedule.setId( id );
        schedule.setName( "DEFAULT_SCHEDULE" );
        schedule.setCronExpression( "0 0 * * * ?" );
        schedule.setDelay( 100 );
        schedule.setMaxJobExecutionTime( 10000 );
        schedule.setBuildQueues( getBuildQueues( start, end ) );

        return schedule;
    }

    /*private void setupOverallBuildQueues()
        throws Exception
    {   
        for ( int i = 2; i <= 5; i++ )
        {
            BuildQueue buildQueue = new BuildQueue();
            buildQueue.setId( i );
            buildQueue.setName( "BUILD_QUEUE_" + String.valueOf( i ) );
                        
            buildsManager.addOverallBuildQueue( buildQueue );
        }

        assertEquals( 5, buildsManager.getOverallBuildQueues().size() );
    }*/
    
    public void setupMockOverallBuildQueues()
        throws Exception
    {   
        Map<Integer, OverallBuildQueue> overallBuildQueues =
            Collections.synchronizedMap( new HashMap<Integer, OverallBuildQueue>() );
        overallBuildQueue = context.mock( OverallBuildQueue.class );        
        for ( int i = 1; i <=5; i++ )
        {   
            overallBuildQueues.put( new Integer( i ), overallBuildQueue );
        }
        
        buildsManager.setOverallBuildQueues( overallBuildQueues );
    }
    
    private void recordStartOfProcess( )
        throws TaskQueueException
    {
        context.checking( new Expectations()
        {
            {
                exactly(5).of( overallBuildQueue ).isInBuildQueue( with( any(int.class) ) );
                will( returnValue( false ) );
            }
        } );
        
        context.checking( new Expectations()
        {
            {
                one( configurationService ).getNumberOfBuildsInParallel();
                will( returnValue( 2 ) );
            }
        } );
        
        context.checking( new Expectations()
        {
            {
                exactly(2).of( overallBuildQueue ).getBuildQueue();
                will( returnValue( buildQueue ) );
            }
        } );
    }
    
    private void recordBuildProjectBuildQueuesAreEmpty()
        throws TaskQueueException
    {
        // shouldn't only the build queues attached to the schedule be checked?
        recordStartOfProcess();
        
        final List<Task> tasks = new ArrayList<Task>();        
        context.checking( new Expectations()
        {
            {
                exactly(3).of( buildQueue ).getQueueSnapshot();
                will( returnValue( tasks ) );
            }
        } );
        
        context.checking( new Expectations()
        {
            {
                one( overallBuildQueue).getName();
                will( returnValue( "BUILD_QUEUE_2" ) );
            }
        } );
        
        context.checking( new Expectations()
        {
            {
                one( overallBuildQueue).addToBuildQueue( with( any( Task.class ) ) );
            }
        } );
    }

    // start of tests...
    
    public void testContainer()
        throws Exception
    {
        buildsManager.setContainer( getContainer() );
    
        buildsManager.isProjectInAnyCurrentBuild( 1 );
    
        assertTrue( true );
    }
    
    public void testBuildProjectNoProjectQueuedInAnyOverallBuildQueues()
        throws Exception
    {
        setupMockOverallBuildQueues();

        BuildDefinition buildDef = new BuildDefinition();
        buildDef.setId( 1 );
        buildDef.setSchedule( getSchedule( 1, 1, 2 ) );

        recordBuildProjectBuildQueuesAreEmpty();
        
        buildsManager.buildProject( 1, buildDef, "continuum-project-test-1", 1 );
        
        context.assertIsSatisfied();
    }
    
    public void testBuildProjectProjectsAreAlreadyQueuedInOverallBuildQueues()
        throws Exception
    {
        setupMockOverallBuildQueues();

        BuildDefinition buildDef = new BuildDefinition();
        buildDef.setId( 1 );
        buildDef.setSchedule( getSchedule( 1, 1, 2 ) );

        recordBuildProjectBuildQueuesAreEmpty();
        
        buildsManager.buildProject( 1, buildDef, "continuum-project-test-1", 1 );
        context.assertIsSatisfied();
        
        //queue second project - 1st queue is not empty, 2nd queue is empty 
        recordStartOfProcess();
        
        // the first build queue already has a task queued
        final List<Task> tasksOfFirstBuildQueue = new ArrayList<Task>();
        tasksOfFirstBuildQueue.add( new BuildProjectTask( 2, 1, 1, "continuum-project-test-2", buildDef.getDescription() ) );        
        context.checking( new Expectations()
        {
            {
                exactly(2).of( buildQueue ).getQueueSnapshot();
                will( returnValue( tasksOfFirstBuildQueue ) );
            }
        } );
        
        final List<Task> tasks = new ArrayList<Task>();
        
        // the second build queue has no tasks queued, so it should return 0
        context.checking( new Expectations()
        {
            {
                exactly(2).of(buildQueue).getQueueSnapshot();
                will( returnValue( tasks ) );
            }
        } );
        
        context.checking( new Expectations()
        {
            {
                one( overallBuildQueue).getName();
                will( returnValue( "BUILD_QUEUE_3" ) );
            }
        } );
        
        context.checking( new Expectations()
        {
            {
                one( overallBuildQueue).addToBuildQueue( with( any( Task.class ) ) );
            }
        } );
        
        buildsManager.buildProject( 2, buildDef, "continuum-project-test-2", 1 );
        context.assertIsSatisfied();
        
        // queue third project - both queues have 1 task queued each
        recordStartOfProcess();
        
        // both queues have 1 task each        
        context.checking( new Expectations()
        {
            {
                exactly(3).of( buildQueue ).getQueueSnapshot();
                will( returnValue( tasksOfFirstBuildQueue ) );
            }
        } );
                
        context.checking( new Expectations()
        {
            {
                one( overallBuildQueue).getName();
                will( returnValue( "BUILD_QUEUE_2" ) );
            }
        } );
        
        context.checking( new Expectations()
        {
            {
                one( overallBuildQueue).addToBuildQueue( with( any( Task.class ) ) );
            }
        } );
        
        buildsManager.buildProject( 3, buildDef, "continuum-project-test-3", 1 );
        context.assertIsSatisfied();
    }
    
    public void testRemoveProjectFromBuildQueue()
        throws Exception
    {
        setupMockOverallBuildQueues();
        
        context.checking( new Expectations()
        {
            {
                one(overallBuildQueue).isInBuildQueue( 1 );
                will( returnValue( true ) );
            }
        } );
        
        context.checking( new Expectations()
        {
            {
                one(overallBuildQueue).removeProjectFromBuildQueue( 1 );
            }
        } );
        
        buildsManager.removeProjectFromBuildQueue( 1 );
        context.assertIsSatisfied();
    }
    
    public void testRemoveProjectsFromBuildQueue()
        throws Exception
    {
        setupMockOverallBuildQueues();
        int[] projectIds = new int[] { 1, 2, 3 };
    
        context.checking( new Expectations()
        {
            {
                exactly(3).of(overallBuildQueue).isInBuildQueue( with( any( int.class ) ) );
                will( returnValue( true ) );
            }
        } );
        
        context.checking( new Expectations()
        {
            {
                exactly(3).of(overallBuildQueue).removeProjectFromBuildQueue( with( any( int.class ) ) );
            }
        } );
        
        buildsManager.removeProjectsFromBuildQueue( projectIds );
        context.assertIsSatisfied();
    }
    
    public void testCheckoutProjectSingle()
        throws Exception
    {
        setupMockOverallBuildQueues();
    
        BuildDefinition buildDef = new BuildDefinition();
        buildDef.setId( 1 );
        buildDef.setSchedule( getSchedule( 1, 1, 2 ) );
    
        context.checking( new Expectations()
        {
            {
                exactly(5).of(overallBuildQueue).isInCheckoutQueue( 1 );
                will( returnValue( false ) );
            }
        } );
        
        context.checking( new Expectations()
        {
            {
                one( configurationService ).getNumberOfBuildsInParallel();
                will( returnValue( 2 ) );
            }
        } );
        
        context.checking( new Expectations()
        {
            {
                exactly(2).of( overallBuildQueue ).getCheckoutQueue();
                will( returnValue( checkoutQueue ) );
            }
        } );
        
        final List<Task> tasks = new ArrayList<Task>();        
        context.checking( new Expectations()
        {
            {
                exactly(3).of( checkoutQueue ).getQueueSnapshot();
                will( returnValue( tasks ) );
            }
        } );
        
        context.checking( new Expectations()
        {
            {
                one( overallBuildQueue).getName();
                will( returnValue( "BUILD_QUEUE_2" ) );
            }
        } );
        
        context.checking( new Expectations()
        {
            {
                one( overallBuildQueue).addToCheckoutQueue( with( any( Task.class ) ) );
            }
        } );
        
        buildsManager.checkoutProject( 1, "continuum-project-test-1", new File( getBasedir(), "/target/test-working-dir/1" ),
                                       "dummy", "dummypass", buildDef );
        context.assertIsSatisfied();
    }
}
