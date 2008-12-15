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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.continuum.dao.BuildDefinitionDao;
import org.apache.continuum.taskqueue.OverallBuildQueue;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildQueue;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.Schedule;
import org.codehaus.plexus.spring.PlexusInSpringTestCase;
import org.codehaus.plexus.taskqueue.Task;
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

    public void setUp()
        throws Exception
    {
        super.setUp();

        buildsManager = (ParallelBuildsManager) lookup( BuildsManager.class, "parallel" );

        context = new JUnit3Mockery();

        buildDefinitionDao = context.mock( BuildDefinitionDao.class );

        buildsManager.setBuildDefinitionDao( buildDefinitionDao );
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

    private void setupOverallBuildQueues()
    {
        for ( int i = 2; i <= 5; i++ )
        {
            OverallBuildQueue overallBuildQueue = (OverallBuildQueue) lookup( OverallBuildQueue.class );
            overallBuildQueue.setId( i );
            overallBuildQueue.setName( "BUILD_QUEUE_" + String.valueOf( i ) );

            buildsManager.addOverallBuildQueue( overallBuildQueue );
        }

        assertEquals( 5, buildsManager.getOverallBuildQueues().size() );
    }

    public void testContainer()
        throws Exception
    {
        buildsManager.setContainer( getContainer() );

        buildsManager.isProjectInAnyCurrentBuild( 1 );

        assertTrue( true );
    }

    // start of tests...

    public void testBuildProjectNoProjectQueuedInAnyOverallBuildQueues()
        throws Exception
    {
        setupOverallBuildQueues();

        BuildDefinition buildDef = new BuildDefinition();
        buildDef.setId( 1 );
        buildDef.setSchedule( getSchedule( 1, 1, 2 ) );

        buildsManager.buildProject( 1, buildDef, "continuum-project-test-1", 1 );

        Map<Integer, OverallBuildQueue> overallBuildQueues = buildsManager.getOverallBuildQueues();
        OverallBuildQueue whereBuildIsQueued = overallBuildQueues.get( 1 );

        assertNotNull( whereBuildIsQueued );
        assertEquals( 1, whereBuildIsQueued.getId() );
        assertEquals( ConfigurationService.DEFAULT_BUILD_QUEUE_NAME, whereBuildIsQueued.getName() );

        // verify that other build queues are not used
        assertFalse( overallBuildQueues.get( 2 ).isInBuildQueue( 1 ) );
        assertFalse( overallBuildQueues.get( 3 ).isInBuildQueue( 1 ) );
        assertFalse( overallBuildQueues.get( 4 ).isInBuildQueue( 1 ) );
        assertFalse( overallBuildQueues.get( 5 ).isInBuildQueue( 1 ) );
    }

    public void testBuildProjectProjectsAreAlreadyQueuedInOverallBuildQueues()
        throws Exception
    {
        setupOverallBuildQueues();

        BuildDefinition buildDef = new BuildDefinition();
        buildDef.setId( 1 );
        buildDef.setSchedule( getSchedule( 1, 1, 2 ) );

        buildsManager.buildProject( 1, buildDef, "continuum-project-test-1", 1 );
        buildsManager.buildProject( 2, buildDef, "continuum-project-test-2", 1 );
        buildsManager.buildProject( 3, buildDef, "continuum-project-test-3", 1 );

        Map<Integer, OverallBuildQueue> overallBuildQueues = buildsManager.getOverallBuildQueues();

        assertNotNull( overallBuildQueues.get( 1 ) );
        assertNotNull( overallBuildQueues.get( 2 ) );

        assertTrue( overallBuildQueues.get( new Integer( 1 ) ).isInBuildQueue( 1, buildDef.getId() ) );
        assertTrue( overallBuildQueues.get( new Integer( 1 ) ).isInBuildQueue( 3, buildDef.getId() ) );
        assertTrue( overallBuildQueues.get( new Integer( 2 ) ).isInBuildQueue( 2, buildDef.getId() ) );
    }

    public void testBuildProjects()
        throws Exception
    {
        setupOverallBuildQueues();

        BuildDefinition buildDef = new BuildDefinition();
        buildDef.setId( 1 );
        buildDef.setSchedule( getSchedule( 1, 1, 2 ) );

        List<Project> projects = new ArrayList<Project>();
        Project project = new Project();
        project.setId( 4 );
        project.setGroupId( "org.apache.continuum" );
        project.setArtifactId( "continuum-test-1" );
        project.addBuildDefinition( buildDef );
        projects.add( project );

        project = new Project();
        project.setId( 5 );
        project.setGroupId( "org.apache.continuum" );
        project.setArtifactId( "continuum-test-2" );
        project.addBuildDefinition( buildDef );
        projects.add( project );

        project = new Project();
        project.setId( 6 );
        project.setGroupId( "org.apache.continuum" );
        project.setArtifactId( "continuum-test-3" );
        project.addBuildDefinition( buildDef );
        projects.add( project );

        Map<Integer, BuildDefinition> projectsBuildDefinitionsMap = new HashMap<Integer, BuildDefinition>();
        projectsBuildDefinitionsMap.put( 4, buildDef );
        projectsBuildDefinitionsMap.put( 5, buildDef );
        projectsBuildDefinitionsMap.put( 6, buildDef );

        // populate build queue
        buildsManager.buildProject( 1, buildDef, "continuum-project-test-1", 1 );
        buildsManager.buildProject( 2, buildDef, "continuum-project-test-2", 1 );
        buildsManager.buildProject( 3, buildDef, "continuum-project-test-3", 1 );

        Map<Integer, OverallBuildQueue> overallBuildQueues = buildsManager.getOverallBuildQueues();

        assertTrue( overallBuildQueues.get( 1 ).isInBuildQueue( 1, buildDef.getId() ) );
        assertTrue( overallBuildQueues.get( 1 ).isInBuildQueue( 3, buildDef.getId() ) );
        assertTrue( overallBuildQueues.get( 2 ).isInBuildQueue( 2, buildDef.getId() ) );

        // build a set of projects
        buildsManager.buildProjects( projects, projectsBuildDefinitionsMap, 1 );

        overallBuildQueues = buildsManager.getOverallBuildQueues();

        assertFalse( overallBuildQueues.get( 1 ).isInBuildQueue( 4 ) );
        assertFalse( overallBuildQueues.get( 1 ).isInBuildQueue( 5 ) );
        assertFalse( overallBuildQueues.get( 1 ).isInBuildQueue( 6 ) );

        assertTrue( overallBuildQueues.get( 2 ).isInBuildQueue( 4 ) );
        assertTrue( overallBuildQueues.get( 2 ).isInBuildQueue( 5 ) );
        assertTrue( overallBuildQueues.get( 2 ).isInBuildQueue( 6 ) );
    }

    public void testRemoveProjectFromBuildQueue()
        throws Exception
    {
        // - if project is built from a group, should the whole group be cancelled?
        setupOverallBuildQueues();

        BuildDefinition buildDef = new BuildDefinition();
        buildDef.setId( 1 );
        buildDef.setSchedule( getSchedule( 1, 1, 2 ) );

        // populate build queue
        buildsManager.buildProject( 1, buildDef, "continuum-project-test-1", 1 );
        buildsManager.buildProject( 2, buildDef, "continuum-project-test-2", 1 );
        buildsManager.buildProject( 3, buildDef, "continuum-project-test-3", 1 );

        Map<Integer, OverallBuildQueue> overallBuildQueues = buildsManager.getOverallBuildQueues();

        assertTrue( overallBuildQueues.get( 1 ).isInBuildQueue( 1, buildDef.getId() ) );
        assertTrue( overallBuildQueues.get( 1 ).isInBuildQueue( 3, buildDef.getId() ) );
        assertTrue( overallBuildQueues.get( 2 ).isInBuildQueue( 2, buildDef.getId() ) );

        // remove project 1
        buildsManager.removeProjectFromBuildQueue( 1 );

        overallBuildQueues = buildsManager.getOverallBuildQueues();
        assertFalse( overallBuildQueues.get( 1 ).isInBuildQueue( 1, buildDef.getId() ) );

        // remove project 2
        buildsManager.removeProjectFromBuildQueue( 2 );

        overallBuildQueues = buildsManager.getOverallBuildQueues();
        assertFalse( overallBuildQueues.get( 2 ).isInBuildQueue( 2, buildDef.getId() ) );

        // remove project 3
        buildsManager.removeProjectFromBuildQueue( 3 );

        overallBuildQueues = buildsManager.getOverallBuildQueues();
        assertFalse( overallBuildQueues.get( 1 ).isInBuildQueue( 3, buildDef.getId() ) );
    }

    /*public void testRemoveProjectFromBuildQueueProjectNotInAnyBuildQueue()
        throws Exception
    {
        setupOverallBuildQueues();
        
        try
        {
            buildsManager.removeProjectFromBuildQueue( 1 );
            fail( "An exception should have been thrown." );
        }
        catch( BuildManagerException e )
        {
            assertEquals( "Project not found in any of the build queues.", e.getMessage() );
        }
    }*/

    public void testRemoveProjectsFromBuildQueue()
        throws Exception
    {
        setupOverallBuildQueues();

        BuildDefinition buildDef = new BuildDefinition();
        buildDef.setId( 1 );
        buildDef.setSchedule( getSchedule( 1, 1, 2 ) );

        int[] projectIds = new int[] { 1, 2, 3 };

        // populate build queue
        buildsManager.buildProject( 1, buildDef, "continuum-project-test-1", 1 );
        buildsManager.buildProject( 2, buildDef, "continuum-project-test-2", 1 );
        buildsManager.buildProject( 3, buildDef, "continuum-project-test-3", 1 );

        Map<Integer, OverallBuildQueue> overallBuildQueues = buildsManager.getOverallBuildQueues();

        assertTrue( overallBuildQueues.get( 1 ).isInBuildQueue( 1, buildDef.getId() ) );
        assertTrue( overallBuildQueues.get( 1 ).isInBuildQueue( 3, buildDef.getId() ) );
        assertTrue( overallBuildQueues.get( 2 ).isInBuildQueue( 2, buildDef.getId() ) );

        // remove all projects
        buildsManager.removeProjectsFromBuildQueue( projectIds );

        overallBuildQueues = buildsManager.getOverallBuildQueues();
        assertFalse( overallBuildQueues.get( 1 ).isInBuildQueue( 1, buildDef.getId() ) );
        assertFalse( overallBuildQueues.get( 2 ).isInBuildQueue( 2, buildDef.getId() ) );
        assertFalse( overallBuildQueues.get( 1 ).isInBuildQueue( 3, buildDef.getId() ) );
    }

    public void testCheckoutProjectSingle()
        throws Exception
    {
        setupOverallBuildQueues();

        BuildDefinition buildDef = new BuildDefinition();
        buildDef.setId( 1 );
        buildDef.setSchedule( getSchedule( 1, 1, 2 ) );

        buildsManager.checkoutProject( 1, "continuum-test-1", new File( getBasedir(), "/target/test-working-dir/1" ),
                                       "dummy", "dummypass", buildDef );

        Map<Integer, OverallBuildQueue> overallBuildQueues = buildsManager.getOverallBuildQueues();
        assertTrue( overallBuildQueues.get( 1 ).isInCheckoutQueue( 1 ) );

        // verify that other build queues are not used
        assertFalse( overallBuildQueues.get( 2 ).isInCheckoutQueue( 1 ) );
        assertFalse( overallBuildQueues.get( 3 ).isInCheckoutQueue( 1 ) );
        assertFalse( overallBuildQueues.get( 4 ).isInCheckoutQueue( 1 ) );
        assertFalse( overallBuildQueues.get( 5 ).isInCheckoutQueue( 1 ) );
    }

    public void testCheckoutProjectMultiple()
        throws Exception
    {
        setupOverallBuildQueues();

        BuildDefinition buildDef = new BuildDefinition();
        buildDef.setId( 1 );
        buildDef.setSchedule( getSchedule( 1, 1, 2 ) );

        buildsManager.checkoutProject( 1, "continuum-test-1", new File( getBasedir(), "/target/test-working-dir/1" ),
                                       "dummy", "dummypass", buildDef );
        buildsManager.checkoutProject( 2, "continuum-test-2", new File( getBasedir(), "/target/test-working-dir/1" ),
                                       "dummy", "dummypass", buildDef );
        buildsManager.checkoutProject( 3, "continuum-test-3", new File( getBasedir(), "/target/test-working-dir/1" ),
                                       "dummy", "dummypass", buildDef );
        buildsManager.checkoutProject( 4, "continuum-test-4", new File( getBasedir(), "/target/test-working-dir/1" ),
                                       "dummy", "dummypass", buildDef );
        buildsManager.checkoutProject( 5, "continuum-test-5", new File( getBasedir(), "/target/test-working-dir/1" ),
                                       "dummy", "dummypass", buildDef );

        Map<Integer, OverallBuildQueue> overallBuildQueues = buildsManager.getOverallBuildQueues();

        assertTrue( overallBuildQueues.get( 1 ).isInCheckoutQueue( 1 ) );
        assertTrue( overallBuildQueues.get( 2 ).isInCheckoutQueue( 2 ) );
        assertTrue( overallBuildQueues.get( 1 ).isInCheckoutQueue( 3 ) );
        assertTrue( overallBuildQueues.get( 2 ).isInCheckoutQueue( 4 ) );
        assertTrue( overallBuildQueues.get( 1 ).isInCheckoutQueue( 5 ) );
    }

    public void testRemoveProjectFromCheckoutQueue()
        throws Exception
    {
        setupOverallBuildQueues();

        BuildDefinition buildDef = new BuildDefinition();
        buildDef.setId( 1 );
        buildDef.setSchedule( getSchedule( 1, 1, 2 ) );

        buildsManager.checkoutProject( 1, "continuum-test-1", new File( getBasedir(), "/target/test-working-dir/1" ),
                                       "dummy", "dummypass", buildDef );
        buildsManager.checkoutProject( 2, "continuum-test-2", new File( getBasedir(), "/target/test-working-dir/1" ),
                                       "dummy", "dummypass", buildDef );
        buildsManager.checkoutProject( 3, "continuum-test-3", new File( getBasedir(), "/target/test-working-dir/1" ),
                                       "dummy", "dummypass", buildDef );

        Map<Integer, OverallBuildQueue> overallBuildQueues = buildsManager.getOverallBuildQueues();
        assertTrue( overallBuildQueues.get( 1 ).isInCheckoutQueue( 1 ) );
        assertTrue( overallBuildQueues.get( 2 ).isInCheckoutQueue( 2 ) );
        assertTrue( overallBuildQueues.get( 1 ).isInCheckoutQueue( 3 ) );

        buildsManager.removeProjectFromCheckoutQueue( 1 );

        overallBuildQueues = buildsManager.getOverallBuildQueues();
        assertFalse( overallBuildQueues.get( 1 ).isInCheckoutQueue( 1 ) );

        buildsManager.removeProjectFromCheckoutQueue( 2 );

        overallBuildQueues = buildsManager.getOverallBuildQueues();
        assertFalse( overallBuildQueues.get( 2 ).isInCheckoutQueue( 2 ) );

        buildsManager.removeProjectFromCheckoutQueue( 3 );

        overallBuildQueues = buildsManager.getOverallBuildQueues();
        assertFalse( overallBuildQueues.get( 1 ).isInCheckoutQueue( 3 ) );
    }

    /*public void testRemoveProjectFromCheckoutQueueProjectNotInAnyCheckoutQueue()
        throws Exception
    {
        setupOverallBuildQueues();
        
        try
        {
            buildsManager.removeProjectFromCheckoutQueue( 1 );
            fail( "An exception should have been thrown." );
        }
        catch ( BuildManagerException e )
        {
            assertEquals( "Project not found in any of the checkout queues.", e.getMessage() );
        }         
    }*/

    public void testRemoveProjectsFromCheckoutQueue()
        throws Exception
    {
        setupOverallBuildQueues();

        BuildDefinition buildDef = new BuildDefinition();
        buildDef.setId( 1 );
        buildDef.setSchedule( getSchedule( 1, 1, 2 ) );

        buildsManager.checkoutProject( 1, "continuum-test-1", new File( getBasedir(), "/target/test-working-dir/1" ),
                                       "dummy", "dummypass", buildDef );
        buildsManager.checkoutProject( 2, "continuum-test-2", new File( getBasedir(), "/target/test-working-dir/1" ),
                                       "dummy", "dummypass", buildDef );
        buildsManager.checkoutProject( 3, "continuum-test-3", new File( getBasedir(), "/target/test-working-dir/1" ),
                                       "dummy", "dummypass", buildDef );

        Map<Integer, OverallBuildQueue> overallBuildQueues = buildsManager.getOverallBuildQueues();
        assertTrue( overallBuildQueues.get( 1 ).isInCheckoutQueue( 1 ) );
        assertTrue( overallBuildQueues.get( 2 ).isInCheckoutQueue( 2 ) );
        assertTrue( overallBuildQueues.get( 1 ).isInCheckoutQueue( 3 ) );

        int[] projectIds = new int[] { 1, 2, 3 };
        buildsManager.removeProjectsFromCheckoutQueue( projectIds );

        overallBuildQueues = buildsManager.getOverallBuildQueues();
        assertFalse( overallBuildQueues.get( 1 ).isInCheckoutQueue( 1 ) );
        assertFalse( overallBuildQueues.get( 2 ).isInCheckoutQueue( 2 ) );
        assertFalse( overallBuildQueues.get( 1 ).isInCheckoutQueue( 3 ) );
    }

    /*public void testRemoveProjectFromPrepareBuildQueue()
        throws Exception
    {

    }*/

    public void testRemoveDefaultOverallBuildQueue()
        throws Exception
    {
        try
        {
            buildsManager.removeOverallBuildQueue( 1 );
            fail( "An exception should have been thrown." );
        }
        catch ( BuildManagerException e )
        {
            assertEquals( "Cannot remove default build queue.", e.getMessage() );
        }
    }

    public void testRemoveOverallBuildQueue()
        throws Exception
    {
        // queued tasks (both checkout & build tasks) must be transferred to the other queues!
        setupOverallBuildQueues();
        assertEquals( 5, buildsManager.getOverallBuildQueues().size() );

        BuildDefinition buildDef = new BuildDefinition();
        buildDef.setId( 1 );
        buildDef.setSchedule( getSchedule( 1, 1, 3 ) );

        // populate build queue
        buildsManager.buildProject( 1, buildDef, "continuum-build-test-1", 1 );
        buildsManager.buildProject( 2, buildDef, "continuum-build-test-2", 1 );
        buildsManager.buildProject( 3, buildDef, "continuum-build-test-3", 1 );
        buildsManager.buildProject( 4, buildDef, "continuum-build-test-4", 1 );
        buildsManager.buildProject( 5, buildDef, "continuum-build-test-5", 1 );

        Map<Integer, OverallBuildQueue> overallBuildQueues = buildsManager.getOverallBuildQueues();
        assertTrue( overallBuildQueues.get( 1 ).isInBuildQueue( 1 ) );
        assertTrue( overallBuildQueues.get( 2 ).isInBuildQueue( 2 ) );
        assertTrue( overallBuildQueues.get( 3 ).isInBuildQueue( 3 ) );
        assertTrue( overallBuildQueues.get( 1 ).isInBuildQueue( 4 ) );
        assertTrue( overallBuildQueues.get( 2 ).isInBuildQueue( 5 ) );

        // populate checkout queue
        buildsManager.checkoutProject( 6, "continuum-checkout-test-6", new File( getBasedir(),
                                                                                 "/target/test-working-dir/1" ),
                                       "dummy", "dummypass", buildDef );
        buildsManager.checkoutProject( 7, "continuum-checkout-test-7", new File( getBasedir(),
                                                                                 "/target/test-working-dir/1" ),
                                       "dummy", "dummypass", buildDef );
        buildsManager.checkoutProject( 8, "continuum-checkout-test-8", new File( getBasedir(),
                                                                                 "/target/test-working-dir/1" ),
                                       "dummy", "dummypass", buildDef );
        buildsManager.checkoutProject( 9, "continuum-checkout-test-9", new File( getBasedir(),
                                                                                 "/target/test-working-dir/1" ),
                                       "dummy", "dummypass", buildDef );
        buildsManager.checkoutProject( 10, "continuum-checkout-test-10", new File( getBasedir(),
                                                                                   "/target/test-working-dir/1" ),
                                       "dummy", "dummypass", buildDef );

        assertTrue( overallBuildQueues.get( 1 ).isInCheckoutQueue( 6 ) );
        assertTrue( overallBuildQueues.get( 2 ).isInCheckoutQueue( 7 ) );
        assertTrue( overallBuildQueues.get( 3 ).isInCheckoutQueue( 8 ) );
        assertTrue( overallBuildQueues.get( 1 ).isInCheckoutQueue( 9 ) );
        assertTrue( overallBuildQueues.get( 2 ).isInCheckoutQueue( 10 ) );

        final BuildDefinition buildDefinition = new BuildDefinition();
        buildDefinition.setId( 1 );
        buildDefinition.setSchedule( getSchedule( 1, 2, 3 ) );

        // set expectations
        context.checking( new Expectations()
        {
            {
                exactly( 2 ).of( buildDefinitionDao ).getBuildDefinition( 1 );
                will( returnValue( buildDefinition ) );
            }
        } );

        context.checking( new Expectations()
        {
            {
                one( buildDefinitionDao ).getDefaultBuildDefinition( 7 );
                will( returnValue( buildDefinition ) );

                one( buildDefinitionDao ).getDefaultBuildDefinition( 10 );
                will( returnValue( buildDefinition ) );
            }
        } );

        buildsManager.removeOverallBuildQueue( 2 );

        // verify
        context.assertIsSatisfied();

        overallBuildQueues = buildsManager.getOverallBuildQueues();
        assertEquals( 4, overallBuildQueues.size() );

        // checkout queues
        assertNull( overallBuildQueues.get( 2 ) );
        assertTrue( overallBuildQueues.get( 1 ).isInCheckoutQueue( 6 ) );
        assertTrue( overallBuildQueues.get( 3 ).isInCheckoutQueue( 7 ) );
        assertTrue( overallBuildQueues.get( 3 ).isInCheckoutQueue( 8 ) );
        assertTrue( overallBuildQueues.get( 1 ).isInCheckoutQueue( 9 ) );
        // shouldn't this be queued in build queue #1?
        assertTrue( overallBuildQueues.get( 3 ).isInCheckoutQueue( 10 ) );

        // build queues                   
        assertTrue( overallBuildQueues.get( 1 ).isInBuildQueue( 1 ) );
        assertTrue( overallBuildQueues.get( 3 ).isInBuildQueue( 2 ) );
        assertTrue( overallBuildQueues.get( 3 ).isInBuildQueue( 3 ) );
        assertTrue( overallBuildQueues.get( 1 ).isInBuildQueue( 4 ) );
        // shouldn't this be queued in build queue #1?
        assertTrue( overallBuildQueues.get( 3 ).isInBuildQueue( 5 ) );
    }

    // TODO use the default build queue instead!
    public void testNoBuildQueuesConfigured()
        throws Exception
    {
        OverallBuildQueue overallBuildQueue = (OverallBuildQueue) lookup( OverallBuildQueue.class );
        overallBuildQueue.setId( 1 );
        overallBuildQueue.setName( "BUILD_QUEUE_1" );

        buildsManager.addOverallBuildQueue( overallBuildQueue );

        BuildDefinition buildDef = new BuildDefinition();
        buildDef.setId( 1 );
        buildDef.setSchedule( getSchedule( 1, 2, 3 ) );

        // test if buildProject(...) is invoked
        try
        {
            buildsManager.buildProject( 1, buildDef, "continuum-project-test-1", 1 );
            fail( "An exception should have been thrown." );
        }
        catch ( BuildManagerException e )
        {
            assertEquals( "No build queue found.", e.getMessage() );
        }

        // test if buildProjects(...) is invoked
        List<Project> projects = new ArrayList<Project>();
        Project project = new Project();
        project.setId( 4 );
        project.setGroupId( "org.apache.continuum" );
        project.setArtifactId( "continuum-test-4" );
        project.addBuildDefinition( buildDef );
        projects.add( project );

        project = new Project();
        project.setId( 5 );
        project.setGroupId( "org.apache.continuum" );
        project.setArtifactId( "continuum-test-5" );
        project.addBuildDefinition( buildDef );
        projects.add( project );

        Map<Integer, BuildDefinition> projectsBuildDefinitionsMap = new HashMap<Integer, BuildDefinition>();
        projectsBuildDefinitionsMap.put( 4, buildDef );
        projectsBuildDefinitionsMap.put( 5, buildDef );

        try
        {
            buildsManager.buildProjects( projects, projectsBuildDefinitionsMap, 1 );
            fail( "An exception should have been thrown." );
        }
        catch ( BuildManagerException e )
        {
            assertEquals( "No build queue found.", e.getMessage() );
        }

        // test if checkoutProject(..) is invoked        
        try
        {
            buildsManager.checkoutProject( 6, "continuum-checkout-test-1", new File( getBasedir(),
                                                                                     "/target/test-working-dir/1" ),
                                           "dummy", "dummypass", buildDef );
            fail( "An exception should have been thrown." );
        }
        catch ( BuildManagerException e )
        {
            assertEquals( "No build queue found.", e.getMessage() );
        }
    }

    public void testGetProjectsInBuildQueue()
        throws Exception
    {
        setupOverallBuildQueues();

        BuildDefinition buildDef = new BuildDefinition();
        buildDef.setId( 1 );
        buildDef.setSchedule( getSchedule( 1, 1, 2 ) );

        // populate build queue
        buildsManager.buildProject( 1, buildDef, "continuum-project-test-1", 1 );
        buildsManager.buildProject( 2, buildDef, "continuum-project-test-2", 1 );
        buildsManager.buildProject( 3, buildDef, "continuum-project-test-3", 1 );

        Map<String, List<Task>> buildsInQueue = buildsManager.getProjectsInBuildQueues();

        assertEquals( 5, buildsInQueue.size() );
        assertTrue( buildsInQueue.containsKey( ConfigurationService.DEFAULT_BUILD_QUEUE_NAME ) );
        assertTrue( buildsInQueue.containsKey( "BUILD_QUEUE_2" ) );

        assertEquals( 2, buildsInQueue.get( ConfigurationService.DEFAULT_BUILD_QUEUE_NAME ).size() );
        assertEquals( 1, buildsInQueue.get( "BUILD_QUEUE_2" ).size() );
    }

    public void testGetProjectsInCheckoutQueue()
        throws Exception
    {
        setupOverallBuildQueues();

        BuildDefinition buildDef = new BuildDefinition();
        buildDef.setId( 1 );
        buildDef.setSchedule( getSchedule( 1, 1, 2 ) );

        buildsManager.checkoutProject( 1, "continuum-test-1", new File( getBasedir(), "/target/test-working-dir/1" ),
                                       "dummy", "dummypass", buildDef );
        buildsManager.checkoutProject( 2, "continuum-test-2", new File( getBasedir(), "/target/test-working-dir/1" ),
                                       "dummy", "dummypass", buildDef );
        buildsManager.checkoutProject( 3, "continuum-test-3", new File( getBasedir(), "/target/test-working-dir/1" ),
                                       "dummy", "dummypass", buildDef );
        buildsManager.checkoutProject( 4, "continuum-test-4", new File( getBasedir(), "/target/test-working-dir/1" ),
                                       "dummy", "dummypass", buildDef );
        buildsManager.checkoutProject( 5, "continuum-test-5", new File( getBasedir(), "/target/test-working-dir/1" ),
                                       "dummy", "dummypass", buildDef );

        Map<String, List<Task>> checkoutsInQueue = buildsManager.getProjectsInCheckoutQueues();

        assertEquals( 5, checkoutsInQueue.size() );
        assertTrue( checkoutsInQueue.containsKey( ConfigurationService.DEFAULT_BUILD_QUEUE_NAME ) );
        assertTrue( checkoutsInQueue.containsKey( "BUILD_QUEUE_2" ) );

        assertEquals( 3, checkoutsInQueue.get( ConfigurationService.DEFAULT_BUILD_QUEUE_NAME ).size() );
        assertEquals( 2, checkoutsInQueue.get( "BUILD_QUEUE_2" ).size() );
    }

    /*public void testRemoveProjectsFromCheckoutQueueWithHashcodes() 
        throws Exception
    {
    
    }
    
    public void testRemoveProjectsFromBuildQueueWithHashcodes() 
        throws Exception
    {
    
    }*/
}
