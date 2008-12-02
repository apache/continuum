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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.continuum.AbstractContinuumTest;
import org.apache.maven.continuum.buildqueue.BuildProjectTask;
import org.apache.maven.continuum.scm.queue.CheckOutTask;
import org.apache.maven.continuum.scm.queue.PrepareBuildProjectsTask;

public class DefaultOverallQueueTest
    extends AbstractContinuumTest
{    
    private OverallQueue overallQueue;
    
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        
        overallQueue = ( OverallQueue ) lookup( OverallQueue.class );
    }
    
    public void testAddToCheckoutQueue()
        throws Exception
    {
        File workingDir = new File( getBasedir(), "target/working-dir" );
        
        CheckOutTask task = new CheckOutTask( 1, workingDir, "continuum-test-project", "username", "password" );
        overallQueue.addToCheckoutQueue( task );
        
        CheckOutTask queuedTask = ( CheckOutTask ) overallQueue.getTaskQueueManager().getCheckoutQueue().take();
        assertNotNull( queuedTask );
        assertEquals( 1, queuedTask.getProjectId() );
        assertEquals( "continuum-test-project", queuedTask.getProjectName() );
    }
    
    public void testAddToBuildQueue()
        throws Exception
    {   
        BuildProjectTask buildTask = new BuildProjectTask( 1, 1, 1, "continuum-test-project", "build-def-label" );
        overallQueue.addToBuildQueue( buildTask );
        
        BuildProjectTask queuedTask = ( BuildProjectTask ) overallQueue.getTaskQueueManager().getBuildQueue().take();
        assertNotNull( queuedTask );
        assertEquals( 1, queuedTask.getProjectId() );
        assertEquals( "continuum-test-project", queuedTask.getProjectName() );
    }
    
    public void testAddToPrepareBuildQueue()
        throws Exception
    {
        Map<Integer, Integer> projectsBuildDefMap = new HashMap<Integer, Integer>();
        projectsBuildDefMap.put( new Integer( 1 ), new Integer( 1 ) ); 
        
        PrepareBuildProjectsTask prepareBuildTask = new PrepareBuildProjectsTask( projectsBuildDefMap, 1 );
        overallQueue.addToPrepareBuildProjectsQueue( prepareBuildTask );
        
        PrepareBuildProjectsTask queuedTask = ( PrepareBuildProjectsTask ) overallQueue.getTaskQueueManager().getPrepareBuildQueue().take();
        assertNotNull( queuedTask );
        assertEquals( 1, ( ( Integer )queuedTask.getProjectsBuildDefinitionsMap().get( new Integer( 1 ) ) ).intValue() );        
    }
}
