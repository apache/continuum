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
import org.apache.continuum.taskqueue.BuildProjectTask;
import org.apache.continuum.taskqueue.CheckOutTask;
import org.apache.continuum.taskqueue.OverallBuildQueue;
import org.apache.continuum.taskqueueexecutor.ParallelBuildsThreadedTaskQueueExecutor;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildQueue;
import org.apache.maven.continuum.model.project.Schedule;
import org.codehaus.plexus.spring.PlexusInSpringTestCase;
import org.codehaus.plexus.taskqueue.Task;
import org.codehaus.plexus.taskqueue.TaskQueue;
import org.codehaus.plexus.taskqueue.TaskQueueException;
import org.codehaus.plexus.taskqueue.execution.TaskQueueExecutor;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit3.JUnit3Mockery;
import org.jmock.lib.legacy.ClassImposteriser;

/**
 * ParallelBuildsManagerTest
 *
 * @author <a href="mailto:oching@apache.org">Maria Odea Ching</a>
 */
public class ParallelBuildsManagerTest
    extends PlexusInSpringTestCase
{
    private ParallelBuildsManager buildsManager;

    private Mockery context;

    private BuildDefinitionDao buildDefinitionDao;

    private ConfigurationService configurationService;

    private OverallBuildQueue overallBuildQueue;

    private TaskQueue buildQueue;

    private TaskQueue checkoutQueue;

    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();

        buildsManager = (ParallelBuildsManager) lookup( BuildsManager.class, "parallel" );

        context = new JUnit3Mockery();
        context.setImposteriser( ClassImposteriser.INSTANCE );

        buildDefinitionDao = context.mock( BuildDefinitionDao.class );

        buildsManager.setBuildDefinitionDao( buildDefinitionDao );

        TaskQueue prepareBuildQueue = context.mock( TaskQueue.class, "prepare-build-queue" );

        buildsManager.setPrepareBuildQueue( prepareBuildQueue );

        configurationService = context.mock( ConfigurationService.class );

        buildsManager.setConfigurationService( configurationService );

        BuildQueueService buildQueueService = context.mock( BuildQueueService.class );

        buildsManager.setBuildQueueService( buildQueueService );

        buildQueue = context.mock( TaskQueue.class, "build-queue" );

        checkoutQueue = context.mock( TaskQueue.class, "checkout-queue" );
    }

    @Override
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

    public void setupMockOverallBuildQueues()
        throws Exception
    {
        Map<Integer, OverallBuildQueue> overallBuildQueues =
            Collections.synchronizedMap( new HashMap<Integer, OverallBuildQueue>() );
        overallBuildQueue = context.mock( OverallBuildQueue.class );
        for ( int i = 1; i <= 5; i++ )
        {
            overallBuildQueues.put( i, overallBuildQueue );
        }

        buildsManager.setOverallBuildQueues( overallBuildQueues );
    }

    // build project recordings
    private void recordStartOfBuildProjectSequence()
        throws TaskQueueException
    {
        context.checking( new Expectations()
        {
            {
                exactly( 5 ).of( overallBuildQueue ).isInBuildQueue( with( any( int.class ) ) );
                will( returnValue( false ) );

                one( configurationService ).getNumberOfBuildsInParallel();
                will( returnValue( 2 ) );

                exactly( 2 ).of( overallBuildQueue ).getBuildQueue();
                will( returnValue( buildQueue ) );
            }} );
    }

    private void recordBuildProjectBuildQueuesAreEmpty()
        throws TaskQueueException
    {
        // shouldn't only the build queues attached to the schedule be checked?
        recordStartOfBuildProjectSequence();

        final List<Task> tasks = new ArrayList<Task>();
        context.checking( new Expectations()
        {
            {
                exactly( 3 ).of( buildQueue ).getQueueSnapshot();
                will( returnValue( tasks ) );

                one( overallBuildQueue ).getName();
                will( returnValue( "BUILD_QUEUE_2" ) );
            }} );

        recordAddToBuildQueue();
    }

    private void recordAddToBuildQueue()
        throws TaskQueueException
    {
        context.checking( new Expectations()
        {
            {
                one( overallBuildQueue ).addToBuildQueue( with( any( BuildProjectTask.class ) ) );
            }} );
    }

    // checkout project recordings
    private void recordStartOfCheckoutProjectSequence()
        throws TaskQueueException
    {
        context.checking( new Expectations()
        {
            {
                exactly( 5 ).of( overallBuildQueue ).isInCheckoutQueue( with( any( int.class ) ) );
                will( returnValue( false ) );

                one( configurationService ).getNumberOfBuildsInParallel();
                will( returnValue( 2 ) );

                exactly( 2 ).of( overallBuildQueue ).getCheckoutQueue();
                will( returnValue( checkoutQueue ) );
            }} );

    }

    private void recordCheckoutProjectBuildQueuesAreEmpty()
        throws TaskQueueException
    {
        recordStartOfCheckoutProjectSequence();

        final List<Task> tasks = new ArrayList<Task>();
        context.checking( new Expectations()
        {
            {
                exactly( 3 ).of( checkoutQueue ).getQueueSnapshot();
                will( returnValue( tasks ) );

                one( overallBuildQueue ).getName();
                will( returnValue( "BUILD_QUEUE_2" ) );
            }} );

        recordAddToCheckoutQueue();
    }

    private void recordAddToCheckoutQueue()
        throws TaskQueueException
    {
        context.checking( new Expectations()
        {
            {
                one( overallBuildQueue ).addToCheckoutQueue( with( any( CheckOutTask.class ) ) );
            }} );
    }

    // start of test cases..

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

        buildsManager.buildProject( 1, buildDef, "continuum-project-test-1", 1, null );

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

        buildsManager.buildProject( 1, buildDef, "continuum-project-test-1", 1, null );
        context.assertIsSatisfied();

        //queue second project - 1st queue is not empty, 2nd queue is empty 
        recordStartOfBuildProjectSequence();

        // the first build queue already has a task queued
        final List<Task> tasks = new ArrayList<Task>();
        final List<Task> tasksOfFirstBuildQueue = new ArrayList<Task>();
        tasksOfFirstBuildQueue.add(
            new BuildProjectTask( 2, 1, 1, "continuum-project-test-2", buildDef.getDescription(), null ) );
        context.checking( new Expectations()
        {
            {
                exactly( 2 ).of( buildQueue ).getQueueSnapshot();
                will( returnValue( tasksOfFirstBuildQueue ) );

                // the second build queue has no tasks queued, so it should return 0
                exactly( 2 ).of( buildQueue ).getQueueSnapshot();
                will( returnValue( tasks ) );

                one( overallBuildQueue ).getName();
                will( returnValue( "BUILD_QUEUE_3" ) );
            }} );

        recordAddToBuildQueue();

        buildsManager.buildProject( 2, buildDef, "continuum-project-test-2", 1, null );
        context.assertIsSatisfied();

        // queue third project - both queues have 1 task queued each
        recordStartOfBuildProjectSequence();

        // both queues have 1 task each        
        context.checking( new Expectations()
        {
            {
                exactly( 3 ).of( buildQueue ).getQueueSnapshot();
                will( returnValue( tasksOfFirstBuildQueue ) );

                one( overallBuildQueue ).getName();
                will( returnValue( "BUILD_QUEUE_2" ) );
            }} );

        recordAddToBuildQueue();

        buildsManager.buildProject( 3, buildDef, "continuum-project-test-3", 1, null );
        context.assertIsSatisfied();
    }

    public void testRemoveProjectFromBuildQueue()
        throws Exception
    {
        setupMockOverallBuildQueues();

        context.checking( new Expectations()
        {
            {
                one( overallBuildQueue ).isInBuildQueue( 1 );
                will( returnValue( true ) );

                one( overallBuildQueue ).removeProjectFromBuildQueue( 1 );
            }} );

        buildsManager.removeProjectFromBuildQueue( 1 );
        context.assertIsSatisfied();
    }

    public void testRemoveProjectsFromBuildQueue()
        throws Exception
    {
        setupMockOverallBuildQueues();
        int[] projectIds = new int[]{1, 2, 3};

        context.checking( new Expectations()
        {
            {
                exactly( 3 ).of( overallBuildQueue ).isInBuildQueue( with( any( int.class ) ) );
                will( returnValue( true ) );

                exactly( 3 ).of( overallBuildQueue ).removeProjectFromBuildQueue( with( any( int.class ) ) );
            }} );

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

        recordCheckoutProjectBuildQueuesAreEmpty();

        buildsManager.checkoutProject( 1, "continuum-project-test-1",
                                       new File( getBasedir(), "/target/test-working-dir/1" ), "dummy", "dummypass",
                                       buildDef );
        context.assertIsSatisfied();
    }

    public void testCheckoutProjectMultiple()
        throws Exception
    {
        setupMockOverallBuildQueues();

        BuildDefinition buildDef = new BuildDefinition();
        buildDef.setId( 1 );
        buildDef.setSchedule( getSchedule( 1, 1, 2 ) );

        recordCheckoutProjectBuildQueuesAreEmpty();

        buildsManager.checkoutProject( 1, "continuum-project-test-1",
                                       new File( getBasedir(), "/target/test-working-dir/1" ), "dummy", "dummypass",
                                       buildDef );
        context.assertIsSatisfied();

        // queue second project - 1st queue has 1 task while 2nd queue is empty; project should be queued in
        //      2nd queue
        recordStartOfCheckoutProjectSequence();

        final List<Task> tasks = new ArrayList<Task>();

        final List<Task> tasksInFirstCheckoutQueue = new ArrayList<Task>();
        tasksInFirstCheckoutQueue.add(
            new CheckOutTask( 1, new File( getBasedir(), "/target/test-working-dir/1" ), "continuum-project-test-1",
                              "dummy", "dummypass" ) );

        context.checking( new Expectations()
        {
            {
                exactly( 2 ).of( checkoutQueue ).getQueueSnapshot();
                will( returnValue( tasksInFirstCheckoutQueue ) );

                exactly( 2 ).of( checkoutQueue ).getQueueSnapshot();
                will( returnValue( tasks ) );

                one( overallBuildQueue ).getName();
                will( returnValue( "BUILD_QUEUE_3" ) );
            }} );

        recordAddToCheckoutQueue();

        buildsManager.checkoutProject( 2, "continuum-project-test-2",
                                       new File( getBasedir(), "/target/test-working-dir/1" ), "dummy", "dummypass",
                                       buildDef );
        context.assertIsSatisfied();

        // queue third project - both queues have 1 task queued each; third project should be queued in 1st queue
        recordStartOfCheckoutProjectSequence();

        context.checking( new Expectations()
        {
            {
                exactly( 3 ).of( checkoutQueue ).getQueueSnapshot();
                will( returnValue( tasksInFirstCheckoutQueue ) );

                one( overallBuildQueue ).getName();
                will( returnValue( "BUILD_QUEUE_2" ) );
            }} );

        recordAddToCheckoutQueue();

        buildsManager.checkoutProject( 3, "continuum-project-test-3",
                                       new File( getBasedir(), "/target/test-working-dir/1" ), "dummy", "dummypass",
                                       buildDef );
        context.assertIsSatisfied();
    }

    public void testRemoveProjectFromCheckoutQueue()
        throws Exception
    {
        setupMockOverallBuildQueues();

        context.checking( new Expectations()
        {
            {
                one( overallBuildQueue ).isInCheckoutQueue( 1 );
                will( returnValue( true ) );

                one( overallBuildQueue ).removeProjectFromCheckoutQueue( 1 );
            }} );

        buildsManager.removeProjectFromCheckoutQueue( 1 );
        context.assertIsSatisfied();
    }

    public void testRemoveProjectsFromCheckoutQueue()
        throws Exception
    {
        setupMockOverallBuildQueues();

        context.checking( new Expectations()
        {
            {
                exactly( 3 ).of( overallBuildQueue ).isInCheckoutQueue( with( any( int.class ) ) );
                will( returnValue( true ) );

                exactly( 3 ).of( overallBuildQueue ).removeProjectFromCheckoutQueue( with( any( int.class ) ) );
            }} );

        int[] projectIds = new int[]{1, 2, 3};

        buildsManager.removeProjectsFromCheckoutQueue( projectIds );
        context.assertIsSatisfied();
    }

    public void testRemoveProjectFromCheckoutQueueProjectNotFound()
        throws Exception
    {
        setupMockOverallBuildQueues();

        // shouldn't only the project's build queues be checked instead of all the overall build queues?
        context.checking( new Expectations()
        {
            {
                exactly( 5 ).of( overallBuildQueue ).isInCheckoutQueue( 1 );
                will( returnValue( false ) );
            }} );

        buildsManager.removeProjectFromCheckoutQueue( 1 );
        context.assertIsSatisfied();
    }

    public void testRemoveDefaultOverallBuildQueue()
        throws Exception
    {
        setupMockOverallBuildQueues();

        try
        {
            context.checking( new Expectations()
            {
                {
                    one( overallBuildQueue ).getName();
                    will( returnValue( ConfigurationService.DEFAULT_BUILD_QUEUE_NAME ) );
                }} );

            buildsManager.removeOverallBuildQueue( 1 );
            context.assertIsSatisfied();
            fail( "An exception should have been thrown." );
        }
        catch ( BuildManagerException e )
        {
            assertEquals( "Cannot remove default build queue.", e.getMessage() );
        }
    }

    public void testRemoveOverallBuildQueueNoTasksCurrentlyExecuting()
        throws Exception
    {
        // queued tasks (both checkout & build tasks) must be transferred to the other queues!
        setupMockOverallBuildQueues();

        final BuildDefinition buildDef = new BuildDefinition();
        buildDef.setId( 1 );
        buildDef.setSchedule( getSchedule( 1, 1, 2 ) );

        final TaskQueueExecutor buildQueueExecutor = context.mock( TaskQueueExecutor.class, "build-queue-executor" );
        final TaskQueueExecutor checkoutQueueExecutor =
            context.mock( TaskQueueExecutor.class, "checkout-queue-executor" );

        final List<Task> buildTasks = new ArrayList<Task>();
        buildTasks.add( new BuildProjectTask( 2, 1, 1, "continuum-project-test-2", "BUILD_DEF", null ) );

        final List<CheckOutTask> checkoutTasks = new ArrayList<CheckOutTask>();
        checkoutTasks.add(
            new CheckOutTask( 2, new File( getBasedir(), "/target/test-working-dir/1" ), "continuum-project-test-2",
                              "dummy", "dummypass" ) );

        final ParallelBuildsThreadedTaskQueueExecutor buildTaskQueueExecutor =
            context.mock( ParallelBuildsThreadedTaskQueueExecutor.class, "parallel-build-task-executor" );
        final ParallelBuildsThreadedTaskQueueExecutor checkoutTaskQueueExecutor =
            context.mock( ParallelBuildsThreadedTaskQueueExecutor.class, "parallel-checkout-task-executor" );

        final List<Task> tasks = new ArrayList<Task>();

        context.checking( new Expectations()
        {
            {
                one( overallBuildQueue ).getName();
                will( returnValue( "BUILD_QUEUE_5" ) );

                // check if there is any build task currently being executed
                one( overallBuildQueue ).getBuildTaskQueueExecutor();
                will( returnValue( buildQueueExecutor ) );
                one( buildQueueExecutor ).getCurrentTask();
                will( returnValue( null ) );
                //will( returnValue( buildTask ) );

                // check if there is any checkout task currently being executed
                one( overallBuildQueue ).getCheckoutTaskQueueExecutor();
                will( returnValue( checkoutQueueExecutor ) );
                one( checkoutQueueExecutor ).getCurrentTask();
                will( returnValue( null ) );
                //will( returnValue( checkoutTask ) );

                // get all queued build tasks & remove them
                one( overallBuildQueue ).getProjectsInBuildQueue();
                will( returnValue( buildTasks ) );
                one( overallBuildQueue ).getBuildQueue();
                will( returnValue( buildQueue ) );
                one( buildQueue ).removeAll( buildTasks );

                // get all queued checkout tasks & remove them
                one( overallBuildQueue ).getProjectsInCheckoutQueue();
                will( returnValue( checkoutTasks ) );
                one( overallBuildQueue ).getCheckoutQueue();
                will( returnValue( checkoutQueue ) );
                one( checkoutQueue ).removeAll( checkoutTasks );

                // stop the build & checkout task queue executors
                one( overallBuildQueue ).getBuildTaskQueueExecutor();
                will( returnValue( buildTaskQueueExecutor ) );
                one( overallBuildQueue ).getCheckoutTaskQueueExecutor();
                will( returnValue( checkoutTaskQueueExecutor ) );

                one( buildTaskQueueExecutor ).stop();
                one( checkoutTaskQueueExecutor ).stop();

                // TODO: test scenario when there are no longer build queues configured aside from the one removed?
                //      - the behaviour should be that the default build queue will be used!

                // re-queue projects in the build queue of the deleted overall build queue
                one( buildDefinitionDao ).getBuildDefinition( 1 );
                will( returnValue( buildDef ) );

                // queue to other build queue
                exactly( 4 ).of( overallBuildQueue ).isInBuildQueue( with( any( int.class ) ) );
                will( returnValue( false ) );

                one( configurationService ).getNumberOfBuildsInParallel();
                will( returnValue( 2 ) );

                exactly( 2 ).of( overallBuildQueue ).getBuildQueue();
                will( returnValue( buildQueue ) );

                exactly( 3 ).of( buildQueue ).getQueueSnapshot();
                will( returnValue( tasks ) );

                one( overallBuildQueue ).getName();
                will( returnValue( "BUILD_QUEUE_2" ) );

                recordAddToBuildQueue();

                // re-queue projects in the checkout queue of the deleted overall build queue
                one( buildDefinitionDao ).getDefaultBuildDefinition( 2 );
                will( returnValue( buildDef ) );

                // queue to other checkout queues
                exactly( 4 ).of( overallBuildQueue ).isInCheckoutQueue( with( any( int.class ) ) );
                will( returnValue( false ) );

                one( configurationService ).getNumberOfBuildsInParallel();
                will( returnValue( 2 ) );

                exactly( 2 ).of( overallBuildQueue ).getCheckoutQueue();
                will( returnValue( checkoutQueue ) );

                exactly( 3 ).of( checkoutQueue ).getQueueSnapshot();
                will( returnValue( tasks ) );

                one( overallBuildQueue ).getName();
                will( returnValue( "BUILD_QUEUE_2" ) );

                recordAddToCheckoutQueue();
            }} );

        buildsManager.removeOverallBuildQueue( 5 );
        context.assertIsSatisfied();

        Map<Integer, OverallBuildQueue> overallBuildQueues = buildsManager.getOverallBuildQueues();
        assertNull( overallBuildQueues.get( 5 ) );
    }

    public void testRemoveOverallBuildQueueTasksCurrentlyExecuting()
        throws Exception
    {
        setupMockOverallBuildQueues();

        final BuildDefinition buildDef = new BuildDefinition();
        buildDef.setId( 1 );
        buildDef.setSchedule( getSchedule( 1, 1, 2 ) );

        final TaskQueueExecutor buildQueueExecutor = context.mock( TaskQueueExecutor.class, "build-queue-executor" );
        final Task buildTask = new BuildProjectTask( 1, 1, 1, "continuum-project-test-1", "BUILD_DEF", null );

        final List<BuildProjectTask> buildTasks = new ArrayList<BuildProjectTask>();
        buildTasks.add( new BuildProjectTask( 2, 1, 1, "continuum-project-test-2", "BUILD_DEF", null ) );

        final List<CheckOutTask> checkoutTasks = new ArrayList<CheckOutTask>();
        checkoutTasks.add(
            new CheckOutTask( 2, new File( getBasedir(), "/target/test-working-dir/1" ), "continuum-project-test-2",
                              "dummy", "dummypass" ) );

        try
        {
            context.checking( new Expectations()
            {
                {
                    one( overallBuildQueue ).getName();
                    will( returnValue( "BUILD_QUEUE_5" ) );

                    // check if there is any build task currently being executed
                    one( overallBuildQueue ).getBuildTaskQueueExecutor();
                    will( returnValue( buildQueueExecutor ) );
                    one( buildQueueExecutor ).getCurrentTask();
                    will( returnValue( buildTask ) );
                }} );

            buildsManager.removeOverallBuildQueue( 5 );
            context.assertIsSatisfied();
            fail( "An exception should have been thrown." );
        }
        catch ( BuildManagerException e )
        {
            assertEquals( "Cannot remove build queue. A task is currently executing.", e.getMessage() );
        }
    }

    public void testNoBuildQueuesConfigured()
        throws Exception
    {
        overallBuildQueue = context.mock( OverallBuildQueue.class );

        Map<Integer, OverallBuildQueue> overallBuildQueues =
            Collections.synchronizedMap( new HashMap<Integer, OverallBuildQueue>() );
        overallBuildQueues.put( 1, overallBuildQueue );

        buildsManager.setOverallBuildQueues( overallBuildQueues );

        Schedule schedule = new Schedule();
        schedule.setId( 1 );
        schedule.setName( "DEFAULT_SCHEDULE" );
        schedule.setCronExpression( "0 0 * * * ?" );
        schedule.setDelay( 100 );
        schedule.setMaxJobExecutionTime( 10000 );

        BuildDefinition buildDef = new BuildDefinition();
        buildDef.setId( 1 );
        buildDef.setSchedule( schedule );

        context.checking( new Expectations()
        {
            {
                one( overallBuildQueue ).isInBuildQueue( with( any( int.class ) ) );
                will( returnValue( false ) );

                one( configurationService ).getNumberOfBuildsInParallel();
                will( returnValue( 2 ) );

                exactly( 2 ).of( overallBuildQueue ).getName();
                will( returnValue( ConfigurationService.DEFAULT_BUILD_QUEUE_NAME ) );

                one( overallBuildQueue ).addToBuildQueue( with( any( BuildProjectTask.class ) ) );
            }} );

        buildsManager.buildProject( 1, buildDef, "continuum-project-test-1", 1, null );
        context.assertIsSatisfied();
    }

    public void testGetProjectsInBuildQueue()
        throws Exception
    {
        setupMockOverallBuildQueues();

        final List<Task> tasks = new ArrayList<Task>();
        tasks.add( new BuildProjectTask( 2, 1, 1, "continuum-project-test-2", "BUILD_DEF", null ) );

        context.checking( new Expectations()
        {
            {
                exactly( 5 ).of( overallBuildQueue ).getName();
                will( returnValue( "BUILD_QUEUE" ) );

                exactly( 5 ).of( overallBuildQueue ).getProjectsInBuildQueue();
                will( returnValue( tasks ) );
            }} );

        buildsManager.getProjectsInBuildQueues();
        context.assertIsSatisfied();
    }

    public void testGetProjectsInCheckoutQueue()
        throws Exception
    {
        setupMockOverallBuildQueues();

        final List<Task> tasks = new ArrayList<Task>();
        tasks.add(
            new CheckOutTask( 2, new File( getBasedir(), "/target/test-working-dir/1" ), "continuum-project-test-2",
                              "dummy", "dummypass" ) );

        context.checking( new Expectations()
        {
            {
                exactly( 5 ).of( overallBuildQueue ).getName();
                will( returnValue( "BUILD_QUEUE" ) );

                exactly( 5 ).of( overallBuildQueue ).getProjectsInCheckoutQueue();
                will( returnValue( tasks ) );
            }} );

        buildsManager.getProjectsInCheckoutQueues();
        context.assertIsSatisfied();
    }

    /*
    public void testNumOfAllowedParallelBuildsIsLessThanConfiguredBuildQueues()
        throws Exception
    {
    
    }
    
    public void testPrepareBuildProjects()
        throws Exception
    {
    
    }
    
    public void testRemoveProjectFromPrepareBuildQueue()
        throws Exception
    {

    }
    */
}
