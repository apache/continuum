package org.apache.continuum.taskqueue;

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

import org.apache.continuum.dao.BuildDefinitionDao;
import org.apache.continuum.taskqueueexecutor.ParallelBuildsThreadedTaskQueueExecutor;
import org.apache.continuum.utils.build.BuildTrigger;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.codehaus.plexus.spring.PlexusInSpringTestCase;
import org.codehaus.plexus.taskqueue.Task;
import org.codehaus.plexus.taskqueue.TaskQueue;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit3.JUnit3Mockery;
import org.jmock.lib.legacy.ClassImposteriser;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DefaultOverallBuildQueueTest
 *
 * @author <a href="mailto:oching@apache.org">Maria Odea Ching</a>
 */
public class DefaultOverallBuildQueueTest
    extends PlexusInSpringTestCase
{
    private DefaultOverallBuildQueue overallQueue;

    private Mockery context;

    private BuildDefinitionDao buildDefinitionDao;

    private ParallelBuildsThreadedTaskQueueExecutor buildTaskQueueExecutor;

    private ParallelBuildsThreadedTaskQueueExecutor checkoutTaskQueueExecutor;

    private ParallelBuildsThreadedTaskQueueExecutor prepareBuildTaskQueueExecutor;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        overallQueue = new DefaultOverallBuildQueue();

        context = new JUnit3Mockery();

        buildDefinitionDao = context.mock( BuildDefinitionDao.class );
        context.setImposteriser( ClassImposteriser.INSTANCE );

        buildTaskQueueExecutor = context.mock( ParallelBuildsThreadedTaskQueueExecutor.class, "build-queue-executor" );

        checkoutTaskQueueExecutor = context.mock( ParallelBuildsThreadedTaskQueueExecutor.class,
                                                  "checkout-queue-executor" );

        prepareBuildTaskQueueExecutor = context.mock( ParallelBuildsThreadedTaskQueueExecutor.class,
                                                      "prepare-build-queue-executor" );

        overallQueue.setBuildDefinitionDao( buildDefinitionDao );

        overallQueue.setBuildTaskQueueExecutor( buildTaskQueueExecutor );

        overallQueue.setCheckoutTaskQueueExecutor( checkoutTaskQueueExecutor );

        overallQueue.setPrepareBuildTaskQueueExecutor( prepareBuildTaskQueueExecutor );
    }

    // checkout queue

    public void testAddToCheckoutQueue()
        throws Exception
    {
        final CheckOutTask checkoutTask = new CheckOutTask( 1, new File( getBasedir(), "/target/test-working-dir/1" ),
                                                            "continuum-project-test-1", "dummy", "dummypass", null,
                                                            null );
        final TaskQueue checkoutQueue = context.mock( TaskQueue.class, "checkout-queue" );

        context.checking( new Expectations()
        {
            {
                one( checkoutTaskQueueExecutor ).getQueue();
                will( returnValue( checkoutQueue ) );

                one( checkoutQueue ).put( checkoutTask );
            }
        } );

        overallQueue.addToCheckoutQueue( checkoutTask );
        context.assertIsSatisfied();
    }

    public void testGetProjectsInCheckoutQueue()
        throws Exception
    {
        final TaskQueue checkoutQueue = context.mock( TaskQueue.class, "checkout-queue" );
        final List<Task> tasks = new ArrayList<Task>();
        tasks.add( new CheckOutTask( 1, new File( getBasedir(), "/target/test-working-dir/1" ),
                                     "continuum-project-test-1", "dummy", "dummypass", null, null ) );

        context.checking( new Expectations()
        {
            {
                one( checkoutTaskQueueExecutor ).getQueue();
                will( returnValue( checkoutQueue ) );

                one( checkoutQueue ).getQueueSnapshot();
                will( returnValue( tasks ) );
            }
        } );

        List<CheckOutTask> returnedTasks = overallQueue.getProjectsInCheckoutQueue();
        context.assertIsSatisfied();

        assertNotNull( returnedTasks );
        assertEquals( 1, returnedTasks.size() );
    }

    public void testIsInCheckoutQueue()
        throws Exception
    {
        final TaskQueue checkoutQueue = context.mock( TaskQueue.class, "checkout-queue" );
        final List<Task> tasks = new ArrayList<Task>();
        tasks.add( new CheckOutTask( 1, new File( getBasedir(), "/target/test-working-dir/1" ),
                                     "continuum-project-test-1", "dummy", "dummypass", null, null ) );

        context.checking( new Expectations()
        {
            {
                one( checkoutTaskQueueExecutor ).getQueue();
                will( returnValue( checkoutQueue ) );

                one( checkoutQueue ).getQueueSnapshot();
                will( returnValue( tasks ) );
            }
        } );

        assertTrue( overallQueue.isInCheckoutQueue( 1 ) );
        context.assertIsSatisfied();
    }

    public void testRemoveProjectFromCheckoutQueue()
        throws Exception
    {
        final Task checkoutTask = new CheckOutTask( 1, new File( getBasedir(), "/target/test-working-dir/1" ),
                                                    "continuum-project-test-1", "dummy", "dummypass", null, null );
        final TaskQueue checkoutQueue = context.mock( TaskQueue.class, "checkout-queue" );
        final List<Task> tasks = new ArrayList<Task>();
        tasks.add( checkoutTask );

        context.checking( new Expectations()
        {
            {
                one( checkoutTaskQueueExecutor ).getQueue();
                will( returnValue( checkoutQueue ) );

                one( checkoutQueue ).getQueueSnapshot();
                will( returnValue( tasks ) );

                one( checkoutTaskQueueExecutor ).getQueue();
                will( returnValue( checkoutQueue ) );

                one( checkoutQueue ).remove( checkoutTask );
            }
        } );

        overallQueue.removeProjectFromCheckoutQueue( 1 );
        context.assertIsSatisfied();
    }

    // build queue

    public void testAddToBuildQueue()
        throws Exception
    {
        final BuildProjectTask buildTask = new BuildProjectTask( 2, 1, new BuildTrigger( 1, "test-user" ),
                                                                 "continuum-project-test-2", "BUILD_DEF", null, 2 );
        final TaskQueue buildQueue = context.mock( TaskQueue.class, "build-queue" );

        context.checking( new Expectations()
        {
            {
                one( buildTaskQueueExecutor ).getQueue();
                will( returnValue( buildQueue ) );

                one( buildQueue ).put( buildTask );
            }
        } );

        overallQueue.addToBuildQueue( buildTask );
        context.assertIsSatisfied();
    }

    public void testGetProjectsFromBuildQueue()
        throws Exception
    {
        final TaskQueue buildQueue = context.mock( TaskQueue.class, "build-queue" );
        final List<Task> tasks = new ArrayList<Task>();
        tasks.add( new BuildProjectTask( 2, 1, new BuildTrigger( 1, "test-user" ), "continuum-project-test-2",
                                         "BUILD_DEF", null, 2 ) );

        context.checking( new Expectations()
        {
            {
                one( buildTaskQueueExecutor ).getQueue();
                will( returnValue( buildQueue ) );

                one( buildQueue ).getQueueSnapshot();
                will( returnValue( tasks ) );
            }
        } );

        List<BuildProjectTask> returnedTasks = overallQueue.getProjectsInBuildQueue();
        context.assertIsSatisfied();

        assertNotNull( returnedTasks );
        assertEquals( 1, returnedTasks.size() );
    }

    public void testIsInBuildQueue()
        throws Exception
    {
        final TaskQueue buildQueue = context.mock( TaskQueue.class, "build-queue" );
        final List<Task> tasks = new ArrayList<Task>();
        tasks.add( new BuildProjectTask( 2, 1, new BuildTrigger( 1, "test-user" ), "continuum-project-test-2",
                                         "BUILD_DEF", null, 2 ) );

        context.checking( new Expectations()
        {
            {
                one( buildTaskQueueExecutor ).getQueue();
                will( returnValue( buildQueue ) );

                one( buildQueue ).getQueueSnapshot();
                will( returnValue( tasks ) );
            }
        } );

        assertTrue( overallQueue.isInBuildQueue( 2 ) );
        context.assertIsSatisfied();
    }

    public void testCancelBuildTask()
        throws Exception
    {
        final Task buildTask = new BuildProjectTask( 2, 1, new BuildTrigger( 1, "test-user" ),
                                                     "continuum-project-test-2", "BUILD_DEF", null, 2 );

        context.checking( new Expectations()
        {
            {
                one( buildTaskQueueExecutor ).getCurrentTask();
                will( returnValue( buildTask ) );

                one( buildTaskQueueExecutor ).cancelTask( buildTask );
            }
        } );

        overallQueue.cancelBuildTask( 2 );
        context.assertIsSatisfied();
    }

    public void testCancelCurrentBuild()
        throws Exception
    {
        final Task buildTask = new BuildProjectTask( 2, 1, new BuildTrigger( 1, "test-user" ),
                                                     "continuum-project-test-2", "BUILD_DEF", null, 2 );

        context.checking( new Expectations()
        {
            {
                one( buildTaskQueueExecutor ).getCurrentTask();
                will( returnValue( buildTask ) );

                one( buildTaskQueueExecutor ).cancelTask( buildTask );
            }
        } );

        overallQueue.cancelCurrentBuild();
        context.assertIsSatisfied();
    }

    public void testRemoveProjectFromBuildQueueWithGivenBuildDefinition()
        throws Exception
    {
        final BuildDefinition buildDef = new BuildDefinition();
        buildDef.setId( 1 );
        buildDef.setDescription( "Test build definition" );

        final TaskQueue buildQueue = context.mock( TaskQueue.class, "build-queue" );

        context.checking( new Expectations()
        {
            {
                one( buildDefinitionDao ).getBuildDefinition( 1 );
                will( returnValue( buildDef ) );

                one( buildTaskQueueExecutor ).getQueue();
                will( returnValue( buildQueue ) );

                one( buildQueue ).remove( with( any( Task.class ) ) );
            }
        } );

        overallQueue.removeProjectFromBuildQueue( 1, 1, new BuildTrigger( 1, "test-user" ), "continuum-project-test-1",
                                                  1 );
        context.assertIsSatisfied();
    }

    public void testRemoveProjectFromBuildQueue()
        throws Exception
    {
        final Task buildTask = new BuildProjectTask( 1, 1, new BuildTrigger( 1, "test-user" ),
                                                     "continuum-project-test-2", "BUILD_DEF", null, 1 );

        final TaskQueue buildQueue = context.mock( TaskQueue.class, "build-queue" );
        final List<Task> tasks = new ArrayList<Task>();
        tasks.add( buildTask );

        context.checking( new Expectations()
        {
            {
                one( buildTaskQueueExecutor ).getQueue();
                will( returnValue( buildQueue ) );

                one( buildQueue ).getQueueSnapshot();
                will( returnValue( tasks ) );

                one( buildTaskQueueExecutor ).getQueue();
                will( returnValue( buildQueue ) );

                one( buildQueue ).remove( buildTask );
            }
        } );

        overallQueue.removeProjectFromBuildQueue( 1 );
        context.assertIsSatisfied();
    }

    // prepare build queue

    public void testAddToPrepareBuildQueue()
        throws Exception
    {
        final PrepareBuildProjectsTask prepareBuildTask = new PrepareBuildProjectsTask( new HashMap<Integer, Integer>(),
                                                                                        new BuildTrigger( 1,
                                                                                                          "test-user" ),
                                                                                        1, "Project Group A",
                                                                                        "http://scmRootAddress", 1 );
        final TaskQueue prepareBuildQueue = context.mock( TaskQueue.class, "prepare-build-queue" );

        context.checking( new Expectations()
        {
            {
                one( prepareBuildTaskQueueExecutor ).getQueue();
                will( returnValue( prepareBuildQueue ) );

                one( prepareBuildQueue ).put( prepareBuildTask );
            }
        } );

        overallQueue.addToPrepareBuildQueue( prepareBuildTask );
        context.assertIsSatisfied();
    }

    public void testCancelCurrentPrepareBuild()
        throws Exception
    {
        final Task prepareBuildTask = new PrepareBuildProjectsTask( new HashMap<Integer, Integer>(), new BuildTrigger(
            1, "test-user" ), 1, "Project Group A", "http://scm.root.address", 1 );

        context.checking( new Expectations()
        {
            {
                one( prepareBuildTaskQueueExecutor ).getCurrentTask();
                will( returnValue( prepareBuildTask ) );

                one( prepareBuildTaskQueueExecutor ).cancelTask( prepareBuildTask );
            }
        } );

        overallQueue.cancelCurrentPrepareBuild();
        context.assertIsSatisfied();
    }

    public void testCancelPrepareBuildTaskByProject()
        throws Exception
    {
        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        map.put( 1, 1 );

        final Task prepareBuildTask = new PrepareBuildProjectsTask( map, new BuildTrigger( 1, "test-user" ), 1,
                                                                    "Project Group A", "http://scm.root.address", 1 );

        context.checking( new Expectations()
        {
            {
                one( prepareBuildTaskQueueExecutor ).getCurrentTask();
                will( returnValue( prepareBuildTask ) );

                one( prepareBuildTaskQueueExecutor ).cancelTask( prepareBuildTask );
            }
        } );

        overallQueue.cancelPrepareBuildTask( 1 );
        context.assertIsSatisfied();
    }

    public void testCancelPrepareBuildTaskByProjectGroup()
        throws Exception
    {
        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        map.put( 1, 1 );

        final Task prepareBuildTask = new PrepareBuildProjectsTask( map, new BuildTrigger( 1, "test-user" ), 1,
                                                                    "Project Group A", "http://scm.root.address", 2 );

        context.checking( new Expectations()
        {
            {
                one( prepareBuildTaskQueueExecutor ).getCurrentTask();
                will( returnValue( prepareBuildTask ) );

                one( prepareBuildTaskQueueExecutor ).cancelTask( prepareBuildTask );
            }
        } );

        overallQueue.cancelPrepareBuildTask( 1, 2 );
        context.assertIsSatisfied();
    }

    public void testGetProjectsFromPrepareBuildQueue()
        throws Exception
    {
        final TaskQueue prepareBuildQueue = context.mock( TaskQueue.class, "prepare-build-queue" );
        final List<Task> tasks = new ArrayList<Task>();
        tasks.add( new PrepareBuildProjectsTask( new HashMap<Integer, Integer>(), new BuildTrigger( 1, "test-user" ), 2,
                                                 "Project Group A", "http://scm.root.address", 2 ) );

        context.checking( new Expectations()
        {
            {
                one( prepareBuildTaskQueueExecutor ).getQueue();
                will( returnValue( prepareBuildQueue ) );

                one( prepareBuildQueue ).getQueueSnapshot();
                will( returnValue( tasks ) );
            }
        } );

        List<PrepareBuildProjectsTask> returnedTasks = overallQueue.getProjectsInPrepareBuildQueue();
        context.assertIsSatisfied();

        assertNotNull( returnedTasks );
        assertEquals( 1, returnedTasks.size() );
    }

    public void testIsInPrepareBuildQueueByProject()
        throws Exception
    {
        final TaskQueue prepareBuildQueue = context.mock( TaskQueue.class, "prepare-build-queue" );

        final Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        map.put( 2, 1 );

        final List<Task> tasks = new ArrayList<Task>();
        tasks.add( new PrepareBuildProjectsTask( map, new BuildTrigger( 1, "test-user" ), 1, "Project Group A",
                                                 "http://scm.root.address", 2 ) );

        context.checking( new Expectations()
        {
            {
                one( prepareBuildTaskQueueExecutor ).getQueue();
                will( returnValue( prepareBuildQueue ) );

                one( prepareBuildQueue ).getQueueSnapshot();
                will( returnValue( tasks ) );
            }
        } );

        assertTrue( overallQueue.isInPrepareBuildQueue( 2 ) );
        context.assertIsSatisfied();
    }

    public void testIsInPrepareBuildQueueByProjectGroupAndScmRootId()
        throws Exception
    {
        final TaskQueue prepareBuildQueue = context.mock( TaskQueue.class, "prepare-build-queue" );

        final Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        map.put( 2, 1 );

        final List<Task> tasks = new ArrayList<Task>();
        tasks.add( new PrepareBuildProjectsTask( map, new BuildTrigger( 1, "test-user" ), 1, "Project Group A",
                                                 "http://scm.root.address", 2 ) );

        context.checking( new Expectations()
        {
            {
                one( prepareBuildTaskQueueExecutor ).getQueue();
                will( returnValue( prepareBuildQueue ) );

                one( prepareBuildQueue ).getQueueSnapshot();
                will( returnValue( tasks ) );
            }
        } );

        assertTrue( overallQueue.isInPrepareBuildQueue( 1, 2 ) );
        context.assertIsSatisfied();
    }

    public void testIsInPrepareBuildQueueByProjectGroupAndScmRootAddress()
        throws Exception
    {
        final TaskQueue prepareBuildQueue = context.mock( TaskQueue.class, "prepare-build-queue" );

        final Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        map.put( 2, 1 );

        final List<Task> tasks = new ArrayList<Task>();
        tasks.add( new PrepareBuildProjectsTask( map, new BuildTrigger( 1, "test-user" ), 1, "Project Group A",
                                                 "http://scm.root.address", 2 ) );

        context.checking( new Expectations()
        {
            {
                one( prepareBuildTaskQueueExecutor ).getQueue();
                will( returnValue( prepareBuildQueue ) );

                one( prepareBuildQueue ).getQueueSnapshot();
                will( returnValue( tasks ) );
            }
        } );

        assertTrue( overallQueue.isInPrepareBuildQueue( 1, "http://scm.root.address" ) );
        context.assertIsSatisfied();
    }

    public void testRemoveProjectsFromPrepareBuildQueueByProjectGroupAndScmRootId()
        throws Exception
    {
        final Task prepareBuildTask = new PrepareBuildProjectsTask( new HashMap<Integer, Integer>(), new BuildTrigger(
            1, "test-user" ), 1, "Project Group A", "http://scm.root.address", 1 );

        final TaskQueue prepareBuildQueue = context.mock( TaskQueue.class, "prepare-build-queue" );
        final List<Task> tasks = new ArrayList<Task>();
        tasks.add( prepareBuildTask );

        context.checking( new Expectations()
        {
            {
                one( prepareBuildTaskQueueExecutor ).getQueue();
                will( returnValue( prepareBuildQueue ) );

                one( prepareBuildQueue ).getQueueSnapshot();
                will( returnValue( tasks ) );

                one( prepareBuildTaskQueueExecutor ).getQueue();
                will( returnValue( prepareBuildQueue ) );

                one( prepareBuildQueue ).remove( prepareBuildTask );
            }
        } );

        overallQueue.removeProjectFromPrepareBuildQueue( 1, 1 );
        context.assertIsSatisfied();
    }

    public void testRemoveProjectsFromPrepareBuildQueueByProjectGroupAndScmRootAddress()
        throws Exception
    {
        final Task prepareBuildTask = new PrepareBuildProjectsTask( new HashMap<Integer, Integer>(), new BuildTrigger(
            1, "test-user" ), 1, "Project Group A", "http://scm.root.address", 1 );

        final TaskQueue prepareBuildQueue = context.mock( TaskQueue.class, "prepare-build-queue" );
        final List<Task> tasks = new ArrayList<Task>();
        tasks.add( prepareBuildTask );

        context.checking( new Expectations()
        {
            {
                one( prepareBuildTaskQueueExecutor ).getQueue();
                will( returnValue( prepareBuildQueue ) );

                one( prepareBuildQueue ).getQueueSnapshot();
                will( returnValue( tasks ) );

                one( prepareBuildTaskQueueExecutor ).getQueue();
                will( returnValue( prepareBuildQueue ) );

                one( prepareBuildQueue ).remove( prepareBuildTask );
            }
        } );

        overallQueue.removeProjectFromPrepareBuildQueue( 1, "http://scm.root.address" );
        context.assertIsSatisfied();
    }
}
