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

import org.apache.continuum.buildqueue.BuildQueueService;
import org.apache.continuum.dao.BuildDefinitionDao;
import org.apache.continuum.dao.ProjectDao;
import org.apache.continuum.taskqueue.BuildProjectTask;
import org.apache.continuum.taskqueue.CheckOutTask;
import org.apache.continuum.taskqueue.OverallBuildQueue;
import org.apache.continuum.taskqueue.PrepareBuildProjectsTask;
import org.apache.continuum.taskqueueexecutor.ParallelBuildsThreadedTaskQueueExecutor;
import org.apache.continuum.utils.build.BuildTrigger;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildQueue;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.Schedule;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.spring.PlexusInSpringTestCase;
import org.codehaus.plexus.taskqueue.Task;
import org.codehaus.plexus.taskqueue.TaskQueue;
import org.codehaus.plexus.taskqueue.TaskQueueException;
import org.codehaus.plexus.taskqueue.execution.TaskQueueExecutor;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit3.JUnit3Mockery;
import org.jmock.lib.legacy.ClassImposteriser;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private ProjectDao projectDao;

    private ConfigurationService configurationService;

    private OverallBuildQueue overallBuildQueue;

    private TaskQueue buildQueue;

    private TaskQueue checkoutQueue;

    private TaskQueue prepareBuildQueue;

    private List<Project> projects;

    private TaskQueueExecutor buildTaskQueueExecutor;

    private TaskQueueExecutor checkoutTaskQueueExecutor;

    private TaskQueueExecutor prepareBuildTaskQueueExecutor;

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

        configurationService = context.mock( ConfigurationService.class );

        buildsManager.setConfigurationService( configurationService );

        BuildQueueService buildQueueService = context.mock( BuildQueueService.class );

        buildsManager.setBuildQueueService( buildQueueService );

        buildQueue = context.mock( TaskQueue.class, "build-queue" );

        checkoutQueue = context.mock( TaskQueue.class, "checkout-queue" );

        prepareBuildQueue = context.mock( TaskQueue.class, "prepare-build-queue" );

        projectDao = context.mock( ProjectDao.class );

        buildsManager.setProjectDao( projectDao );

        buildTaskQueueExecutor = context.mock( TaskQueueExecutor.class, "build-task-queue" );

        checkoutTaskQueueExecutor = context.mock( TaskQueueExecutor.class, "checkout-task-queue" );

        prepareBuildTaskQueueExecutor = context.mock( TaskQueueExecutor.class, "prepare-build-task-queue" );
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
        Map<Integer, OverallBuildQueue> overallBuildQueues = Collections.synchronizedMap(
            new HashMap<Integer, OverallBuildQueue>() );
        overallBuildQueue = context.mock( OverallBuildQueue.class );
        for ( int i = 1; i <= 5; i++ )
        {
            overallBuildQueues.put( i, overallBuildQueue );
        }

        buildsManager.setOverallBuildQueues( overallBuildQueues );
    }

    // build project recordings
    private void recordStartOfBuildProjectSequence()
        throws TaskQueueException, ContinuumStoreException
    {
        context.checking( new Expectations()
        {
            {
                exactly( 5 ).of( overallBuildQueue ).isInBuildQueue( with( any( int.class ) ) );
                will( returnValue( false ) );

                exactly( 5 ).of( buildTaskQueueExecutor ).getCurrentTask();
                will( returnValue( null ) );

                one( projectDao ).getProjectsInGroup( with( any( int.class ) ) );
                will( returnValue( projects ) );

                one( configurationService ).getNumberOfBuildsInParallel();
                will( returnValue( 2 ) );

                exactly( 2 ).of( overallBuildQueue ).getBuildQueue();
                will( returnValue( buildQueue ) );

                exactly( 7 ).of( overallBuildQueue ).getBuildTaskQueueExecutor();
                will( returnValue( buildTaskQueueExecutor ) );
            }
        } );
    }

    private void recordBuildProjectBuildQueuesAreEmpty()
        throws TaskQueueException, ContinuumStoreException
    {
        // shouldn't only the build queues attached to the schedule be checked?
        recordStartOfBuildProjectSequence();

        final List<Task> tasks = new ArrayList<Task>();
        context.checking( new Expectations()
        {
            {
                exactly( 2 ).of( buildQueue ).getQueueSnapshot();
                will( returnValue( tasks ) );

                exactly( 2 ).of( buildTaskQueueExecutor ).getCurrentTask();
                will( returnValue( null ) );

                one( overallBuildQueue ).getName();
                will( returnValue( "BUILD_QUEUE_2" ) );
            }
        } );

        recordAddToBuildQueue();
    }

    private void recordAddToBuildQueue()
        throws TaskQueueException
    {
        context.checking( new Expectations()
        {
            {
                one( overallBuildQueue ).addToBuildQueue( with( any( BuildProjectTask.class ) ) );
            }
        } );
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

                exactly( 2 ).of( overallBuildQueue ).getCheckoutTaskQueueExecutor();
                will( returnValue( checkoutTaskQueueExecutor ) );
            }
        } );

    }

    private void recordCheckoutProjectBuildQueuesAreEmpty()
        throws TaskQueueException
    {
        recordStartOfCheckoutProjectSequence();

        final List<Task> tasks = new ArrayList<Task>();
        context.checking( new Expectations()
        {
            {
                exactly( 2 ).of( checkoutQueue ).getQueueSnapshot();
                will( returnValue( tasks ) );

                exactly( 2 ).of( checkoutTaskQueueExecutor ).getCurrentTask();
                will( returnValue( null ) );

                one( overallBuildQueue ).getName();
                will( returnValue( "BUILD_QUEUE_2" ) );
            }
        } );

        recordAddToCheckoutQueue();
    }

    private void recordAddToCheckoutQueue()
        throws TaskQueueException
    {
        context.checking( new Expectations()
        {
            {
                one( overallBuildQueue ).addToCheckoutQueue( with( any( CheckOutTask.class ) ) );
            }
        } );
    }

    // prepare build project recordings
    private void recordStartOfPrepareBuildProjectSequence()
        throws TaskQueueException, ContinuumStoreException
    {
        final BuildDefinition buildDef = new BuildDefinition();
        buildDef.setId( 1 );
        buildDef.setSchedule( getSchedule( 1, 1, 2 ) );

        context.checking( new Expectations()
        {
            {
                exactly( 5 ).of( overallBuildQueue ).isInPrepareBuildQueue( with( any( int.class ) ), with( any(
                    int.class ) ) );
                will( returnValue( false ) );

                one( buildDefinitionDao ).getBuildDefinition( 1 );
                will( returnValue( buildDef ) );

                one( configurationService ).getNumberOfBuildsInParallel();
                will( returnValue( 2 ) );

                exactly( 2 ).of( overallBuildQueue ).getPrepareBuildQueue();
                will( returnValue( prepareBuildQueue ) );

                exactly( 2 ).of( overallBuildQueue ).getPrepareBuildTaskQueueExecutor();
                will( returnValue( prepareBuildTaskQueueExecutor ) );
            }
        } );
    }

    private void recordPrepareBuildProjectPrepareBuildQueuesAreEmpty()
        throws TaskQueueException, ContinuumStoreException
    {
        recordStartOfPrepareBuildProjectSequence();

        final List<Task> tasks = new ArrayList<Task>();
        context.checking( new Expectations()
        {
            {
                exactly( 2 ).of( prepareBuildQueue ).getQueueSnapshot();
                will( returnValue( tasks ) );

                exactly( 2 ).of( prepareBuildTaskQueueExecutor ).getCurrentTask();
                will( returnValue( null ) );

                one( overallBuildQueue ).getName();
                will( returnValue( "BUILD_QUEUE_2" ) );
            }
        } );

        recordAddToPrepareBuildQueue();
    }

    private void recordAddToPrepareBuildQueue()
        throws TaskQueueException
    {
        context.checking( new Expectations()
        {
            {
                one( overallBuildQueue ).addToPrepareBuildQueue( with( any( PrepareBuildProjectsTask.class ) ) );
            }
        } );
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

        buildsManager.buildProject( 1, buildDef, "continuum-project-test-1", new BuildTrigger( 1, "test-user" ), null,
                                    1 );

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

        buildsManager.buildProject( 1, buildDef, "continuum-project-test-1", new BuildTrigger( 1, "test-user" ), null,
                                    1 );
        context.assertIsSatisfied();

        //queue second project - 1st queue is not empty, 2nd queue is empty 
        recordStartOfBuildProjectSequence();

        // the first build queue already has a task queued
        final List<Task> tasks = new ArrayList<Task>();
        final List<Task> tasksOfFirstBuildQueue = new ArrayList<Task>();
        tasksOfFirstBuildQueue.add( new BuildProjectTask( 2, 1, new BuildTrigger( 1, "test-user" ),
                                                          "continuum-project-test-2", buildDef.getDescription(), null,
                                                          2 ) );
        context.checking( new Expectations()
        {
            {
                one( buildQueue ).getQueueSnapshot();
                will( returnValue( tasksOfFirstBuildQueue ) );

                // the second build queue has no tasks queued, so it should return 0
                one( buildQueue ).getQueueSnapshot();
                will( returnValue( tasks ) );

                exactly( 2 ).of( buildTaskQueueExecutor ).getCurrentTask();
                will( returnValue( null ) );

                one( overallBuildQueue ).getName();
                will( returnValue( "BUILD_QUEUE_3" ) );
            }
        } );

        recordAddToBuildQueue();

        buildsManager.buildProject( 2, buildDef, "continuum-project-test-2", new BuildTrigger( 1, "test-user" ), null,
                                    2 );
        context.assertIsSatisfied();

        // queue third project - both queues have 1 task queued each
        recordStartOfBuildProjectSequence();

        // both queues have 1 task each        
        context.checking( new Expectations()
        {
            {
                exactly( 2 ).of( buildQueue ).getQueueSnapshot();
                will( returnValue( tasksOfFirstBuildQueue ) );

                exactly( 2 ).of( buildTaskQueueExecutor ).getCurrentTask();
                will( returnValue( null ) );

                one( overallBuildQueue ).getName();
                will( returnValue( "BUILD_QUEUE_2" ) );
            }
        } );

        recordAddToBuildQueue();

        buildsManager.buildProject( 3, buildDef, "continuum-project-test-3", new BuildTrigger( 1, "test-user" ), null,
                                    3 );
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
            }
        } );

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

        recordCheckoutProjectBuildQueuesAreEmpty();

        buildsManager.checkoutProject( 1, "continuum-project-test-1", new File( getBasedir(),
                                                                                "/target/test-working-dir/1" ), null,
                                       "dummy", "dummypass", buildDef, null );
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

        buildsManager.checkoutProject( 1, "continuum-project-test-1", new File( getBasedir(),
                                                                                "/target/test-working-dir/1" ), null,
                                       "dummy", "dummypass", buildDef, null );
        context.assertIsSatisfied();

        // queue second project - 1st queue has 1 task while 2nd queue is empty; project should be queued in
        //      2nd queue
        recordStartOfCheckoutProjectSequence();

        final List<Task> tasks = new ArrayList<Task>();

        final List<Task> tasksInFirstCheckoutQueue = new ArrayList<Task>();
        tasksInFirstCheckoutQueue.add( new CheckOutTask( 1, new File( getBasedir(), "/target/test-working-dir/1" ),
                                                         "continuum-project-test-1", "dummy", "dummypass", null,
                                                         null ) );

        context.checking( new Expectations()
        {
            {
                one( checkoutQueue ).getQueueSnapshot();
                will( returnValue( tasksInFirstCheckoutQueue ) );

                one( checkoutQueue ).getQueueSnapshot();
                will( returnValue( tasks ) );

                exactly( 2 ).of( checkoutTaskQueueExecutor ).getCurrentTask();
                will( returnValue( null ) );

                one( overallBuildQueue ).getName();
                will( returnValue( "BUILD_QUEUE_3" ) );
            }
        } );

        recordAddToCheckoutQueue();

        buildsManager.checkoutProject( 2, "continuum-project-test-2", new File( getBasedir(),
                                                                                "/target/test-working-dir/1" ), null,
                                       "dummy", "dummypass", buildDef, null );
        context.assertIsSatisfied();

        // queue third project - both queues have 1 task queued each; third project should be queued in 1st queue
        recordStartOfCheckoutProjectSequence();

        context.checking( new Expectations()
        {
            {
                exactly( 2 ).of( checkoutQueue ).getQueueSnapshot();
                will( returnValue( tasksInFirstCheckoutQueue ) );

                exactly( 2 ).of( checkoutTaskQueueExecutor ).getCurrentTask();
                will( returnValue( null ) );

                one( overallBuildQueue ).getName();
                will( returnValue( "BUILD_QUEUE_2" ) );
            }
        } );

        recordAddToCheckoutQueue();

        buildsManager.checkoutProject( 3, "continuum-project-test-3", new File( getBasedir(),
                                                                                "/target/test-working-dir/1" ), null,
                                       "dummy", "dummypass", buildDef, null );
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
            }
        } );

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
            }
        } );

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
            }
        } );

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
                }
            } );

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
        final TaskQueueExecutor checkoutQueueExecutor = context.mock( TaskQueueExecutor.class,
                                                                      "checkout-queue-executor" );
        final TaskQueueExecutor prepareBuildQueueExecutor = context.mock( TaskQueueExecutor.class,
                                                                          "prepare-build-queue-executor" );

        final List<Task> buildTasks = new ArrayList<Task>();
        buildTasks.add( new BuildProjectTask( 2, 1, new BuildTrigger( 1, "test-user" ), "continuum-project-test-2",
                                              "BUILD_DEF", null, 2 ) );

        final List<CheckOutTask> checkoutTasks = new ArrayList<CheckOutTask>();
        checkoutTasks.add( new CheckOutTask( 2, new File( getBasedir(), "/target/test-working-dir/1" ),
                                             "continuum-project-test-2", "dummy", "dummypass", null, null ) );

        final List<Task> prepareBuildTasks = new ArrayList<Task>();
        final Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        map.put( 1, 1 );
        prepareBuildTasks.add( new PrepareBuildProjectsTask( map, new BuildTrigger( 1, "test-user" ), 1,
                                                             "Project Group A", "http://scm.root.address", 2 ) );

        final ParallelBuildsThreadedTaskQueueExecutor buildTaskQueueExecutor = context.mock(
            ParallelBuildsThreadedTaskQueueExecutor.class, "parallel-build-task-executor" );
        final ParallelBuildsThreadedTaskQueueExecutor checkoutTaskQueueExecutor = context.mock(
            ParallelBuildsThreadedTaskQueueExecutor.class, "parallel-checkout-task-executor" );
        final ParallelBuildsThreadedTaskQueueExecutor prepareBuildTaskQueueExecutor = context.mock(
            ParallelBuildsThreadedTaskQueueExecutor.class, "parallel-prepare-build-task-executor" );

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

                // check if there is any prepare build task currently being executed
                one( overallBuildQueue ).getPrepareBuildTaskQueueExecutor();
                will( returnValue( prepareBuildQueueExecutor ) );
                one( prepareBuildQueueExecutor ).getCurrentTask();
                will( returnValue( null ) );

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

                // get all queued prepare build tasks & remove them
                one( overallBuildQueue ).getProjectsInPrepareBuildQueue();
                will( returnValue( prepareBuildTasks ) );
                one( overallBuildQueue ).getPrepareBuildQueue();
                will( returnValue( prepareBuildQueue ) );
                one( prepareBuildQueue ).removeAll( prepareBuildTasks );

                // stop the build & checkout task queue executors
                one( overallBuildQueue ).getBuildTaskQueueExecutor();
                will( returnValue( buildTaskQueueExecutor ) );
                one( overallBuildQueue ).getCheckoutTaskQueueExecutor();
                will( returnValue( checkoutTaskQueueExecutor ) );
                one( overallBuildQueue ).getPrepareBuildTaskQueueExecutor();
                will( returnValue( prepareBuildTaskQueueExecutor ) );

                one( buildTaskQueueExecutor ).stop();
                one( checkoutTaskQueueExecutor ).stop();
                one( prepareBuildTaskQueueExecutor ).stop();

                // TODO: test scenario when there are no longer build queues configured aside from the one removed?
                //      - the behaviour should be that the default build queue will be used!

                // re-queue projects in the build queue of the deleted overall build queue
                one( buildDefinitionDao ).getBuildDefinition( 1 );
                will( returnValue( buildDef ) );

                // queue to other build queue
                exactly( 4 ).of( overallBuildQueue ).isInBuildQueue( with( any( int.class ) ) );
                will( returnValue( false ) );

                exactly( 4 ).of( buildQueueExecutor ).getCurrentTask();
                will( returnValue( null ) );

                one( projectDao ).getProjectsInGroup( with( any( int.class ) ) );
                will( returnValue( projects ) );

                one( configurationService ).getNumberOfBuildsInParallel();
                will( returnValue( 2 ) );

                exactly( 2 ).of( overallBuildQueue ).getBuildQueue();
                will( returnValue( buildQueue ) );

                exactly( 6 ).of( overallBuildQueue ).getBuildTaskQueueExecutor();
                will( returnValue( buildQueueExecutor ) );

                exactly( 2 ).of( buildQueue ).getQueueSnapshot();
                will( returnValue( tasks ) );

                exactly( 2 ).of( buildQueueExecutor ).getCurrentTask();
                will( returnValue( null ) );

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

                exactly( 2 ).of( overallBuildQueue ).getCheckoutTaskQueueExecutor();
                will( returnValue( checkoutQueueExecutor ) );

                exactly( 2 ).of( checkoutQueue ).getQueueSnapshot();
                will( returnValue( tasks ) );

                exactly( 2 ).of( checkoutQueueExecutor ).getCurrentTask();
                will( returnValue( null ) );

                one( overallBuildQueue ).getName();
                will( returnValue( "BUILD_QUEUE_2" ) );

                recordAddToCheckoutQueue();

                // re-queue projects in the prepare build queue of the deleted overall build queue
                exactly( 4 ).of( overallBuildQueue ).isInPrepareBuildQueue( with( any( int.class ) ), with( any(
                    int.class ) ) );
                will( returnValue( false ) );

                one( buildDefinitionDao ).getBuildDefinition( 1 );
                will( returnValue( buildDef ) );

                one( configurationService ).getNumberOfBuildsInParallel();
                will( returnValue( 2 ) );

                exactly( 2 ).of( overallBuildQueue ).getPrepareBuildQueue();
                will( returnValue( prepareBuildQueue ) );

                exactly( 2 ).of( overallBuildQueue ).getPrepareBuildTaskQueueExecutor();
                will( returnValue( prepareBuildQueueExecutor ) );

                exactly( 2 ).of( prepareBuildQueue ).getQueueSnapshot();
                will( returnValue( tasks ) );

                exactly( 2 ).of( prepareBuildQueueExecutor ).getCurrentTask();
                will( returnValue( null ) );

                one( overallBuildQueue ).getName();
                will( returnValue( "BUILD_QUEUE_2" ) );

                recordAddToPrepareBuildQueue();
            }
        } );

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
        final Task buildTask = new BuildProjectTask( 1, 1, new BuildTrigger( 1, "test-user" ),
                                                     "continuum-project-test-1", "BUILD_DEF", null, 1 );

        final List<BuildProjectTask> buildTasks = new ArrayList<BuildProjectTask>();
        buildTasks.add( new BuildProjectTask( 2, 1, new BuildTrigger( 1, "test-user" ), "continuum-project-test-2",
                                              "BUILD_DEF", null, 2 ) );

        final List<CheckOutTask> checkoutTasks = new ArrayList<CheckOutTask>();
        checkoutTasks.add( new CheckOutTask( 2, new File( getBasedir(), "/target/test-working-dir/1" ),
                                             "continuum-project-test-2", "dummy", "dummypass", null, null ) );

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
                }
            } );

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

        Map<Integer, OverallBuildQueue> overallBuildQueues = Collections.synchronizedMap(
            new HashMap<Integer, OverallBuildQueue>() );
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

                one( overallBuildQueue ).getBuildTaskQueueExecutor();
                will( returnValue( buildTaskQueueExecutor ) );

                one( buildTaskQueueExecutor ).getCurrentTask();
                will( returnValue( null ) );

                one( projectDao ).getProjectsInGroup( with( any( int.class ) ) );
                will( returnValue( projects ) );

                one( configurationService ).getNumberOfBuildsInParallel();
                will( returnValue( 2 ) );

                exactly( 2 ).of( overallBuildQueue ).getName();
                will( returnValue( ConfigurationService.DEFAULT_BUILD_QUEUE_NAME ) );

                one( overallBuildQueue ).addToBuildQueue( with( any( BuildProjectTask.class ) ) );
            }
        } );

        buildsManager.buildProject( 1, buildDef, "continuum-project-test-1", new BuildTrigger( 1, "test-user" ), null,
                                    1 );
        context.assertIsSatisfied();
    }

    public void testGetProjectsInBuildQueue()
        throws Exception
    {
        setupMockOverallBuildQueues();

        final List<Task> tasks = new ArrayList<Task>();
        tasks.add( new BuildProjectTask( 2, 1, new BuildTrigger( 1, "test-user" ), "continuum-project-test-2",
                                         "BUILD_DEF", null, 2 ) );

        context.checking( new Expectations()
        {
            {
                exactly( 5 ).of( overallBuildQueue ).getName();
                will( returnValue( "BUILD_QUEUE" ) );

                exactly( 5 ).of( overallBuildQueue ).getProjectsInBuildQueue();
                will( returnValue( tasks ) );
            }
        } );

        buildsManager.getProjectsInBuildQueues();
        context.assertIsSatisfied();
    }

    public void testGetProjectsInCheckoutQueue()
        throws Exception
    {
        setupMockOverallBuildQueues();

        final List<Task> tasks = new ArrayList<Task>();
        tasks.add( new CheckOutTask( 2, new File( getBasedir(), "/target/test-working-dir/1" ),
                                     "continuum-project-test-2", "dummy", "dummypass", null, null ) );

        context.checking( new Expectations()
        {
            {
                exactly( 5 ).of( overallBuildQueue ).getName();
                will( returnValue( "BUILD_QUEUE" ) );

                exactly( 5 ).of( overallBuildQueue ).getProjectsInCheckoutQueue();
                will( returnValue( tasks ) );
            }
        } );

        buildsManager.getProjectsInCheckoutQueues();
        context.assertIsSatisfied();
    }

    // prepare build queue
    public void testPrepareBuildProjectNoProjectQueuedInAnyOverallBuildQueues()
        throws Exception
    {
        setupMockOverallBuildQueues();

        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        map.put( 1, 1 );

        recordPrepareBuildProjectPrepareBuildQueuesAreEmpty();

        buildsManager.prepareBuildProjects( map, new BuildTrigger( 1, "test-user" ), 1, "Project Group A",
                                            "http://scm.root.address", 1 );
        context.assertIsSatisfied();
    }

    public void testPrepareBuildProjectsAlreadyQueued()
        throws Exception
    {
        setupMockOverallBuildQueues();

        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        map.put( 1, 1 );

        //queue second project - 1st queue is not empty, 2nd queue is empty 
        recordStartOfPrepareBuildProjectSequence();

        // the first prepare build queue already has a task queued
        final List<Task> tasks = new ArrayList<Task>();
        final List<Task> tasksOfFirstPrepareBuildQueue = new ArrayList<Task>();
        tasksOfFirstPrepareBuildQueue.add( new PrepareBuildProjectsTask( new HashMap<Integer, Integer>(),
                                                                         new BuildTrigger( 1, "test-user" ), 1,
                                                                         "Project Group B", "http://scm.root.address2",
                                                                         2 ) );
        context.checking( new Expectations()
        {
            {
                one( prepareBuildQueue ).getQueueSnapshot();
                will( returnValue( tasksOfFirstPrepareBuildQueue ) );

                // the second prepare build queue has no tasks queued, so it should return 0
                one( prepareBuildQueue ).getQueueSnapshot();
                will( returnValue( tasks ) );

                exactly( 2 ).of( prepareBuildTaskQueueExecutor ).getCurrentTask();
                will( returnValue( null ) );

                one( overallBuildQueue ).getName();
                will( returnValue( "BUILD_QUEUE_3" ) );
            }
        } );

        recordAddToPrepareBuildQueue();

        buildsManager.prepareBuildProjects( map, new BuildTrigger( 1, "test-user" ), 1, "Project Group A",
                                            "http://scm.root.address", 1 );
        context.assertIsSatisfied();

        // queue third project - both queues have 1 task queued each
        recordStartOfPrepareBuildProjectSequence();

        // both queues have 1 task each        
        context.checking( new Expectations()
        {
            {
                exactly( 2 ).of( prepareBuildQueue ).getQueueSnapshot();
                will( returnValue( tasksOfFirstPrepareBuildQueue ) );

                exactly( 2 ).of( prepareBuildTaskQueueExecutor ).getCurrentTask();
                will( returnValue( null ) );

                one( overallBuildQueue ).getName();
                will( returnValue( "BUILD_QUEUE_2" ) );
            }
        } );

        recordAddToPrepareBuildQueue();

        buildsManager.prepareBuildProjects( map, new BuildTrigger( 1, "test-user" ), 1, "Project Group A",
                                            "http://scm.root.address", 1 );
        context.assertIsSatisfied();
    }

    public void testGetProjectsInPrepareBuildQueue()
        throws Exception
    {
        setupMockOverallBuildQueues();

        final List<Task> tasks = new ArrayList<Task>();
        tasks.add( new PrepareBuildProjectsTask( new HashMap<Integer, Integer>(), new BuildTrigger( 1, "test-user" ), 1,
                                                 "Project Group A", "http://scm.root.address", 2 ) );

        context.checking( new Expectations()
        {
            {
                exactly( 5 ).of( overallBuildQueue ).getName();
                will( returnValue( "PREPARE_BUILD_QUEUE" ) );

                exactly( 5 ).of( overallBuildQueue ).getProjectsInPrepareBuildQueue();
                will( returnValue( tasks ) );
            }
        } );

        buildsManager.getProjectsInPrepareBuildQueue();
        context.assertIsSatisfied();
    }

    public void testRemoveProjectFromPrepareBuildQueue()
        throws Exception
    {
        setupMockOverallBuildQueues();
        context.checking( new Expectations()
        {
            {
                exactly( 5 ).of( overallBuildQueue ).isInPrepareBuildQueue( 1, 2 );
                will( returnValue( true ) );

                one( overallBuildQueue ).removeProjectFromPrepareBuildQueue( 1, 2 );
            }
        } );

        buildsManager.removeProjectFromPrepareBuildQueue( 1, 2 );
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
