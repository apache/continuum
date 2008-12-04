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

import org.apache.continuum.taskqueue.OverallBuildQueue;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Schedule;
import org.codehaus.plexus.spring.PlexusInSpringTestCase;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit3.JUnit3Mockery;

/**
 * 
 * @author <a href="mailto:oching@apache.org">Maria Odea Ching</a>
 */
public class DefaultBuildManagerTest
    extends PlexusInSpringTestCase
{
    private DefaultBuildManager buildManager;
    
    private Mockery context;
    
    private ConfigurationService configurationService;
    
    public void setUp() 
        throws Exception
    {
        super.setUp();
        
        context = new JUnit3Mockery();
        
        configurationService = context.mock( ConfigurationService.class );
        
        buildManager = ( DefaultBuildManager ) lookup( BuildManager.class );
        
        buildManager.setContainer( getContainer() ); 
        
        buildManager.setConfigurationService( configurationService );
    }
    
    public void testParallelBuilds()
        throws Exception
    {   
        // set expectations
        context.checking(new Expectations() {{
            exactly( 2 ).of(configurationService).getNumberOfBuildsInParallel(); will( returnValue( 2 ) );
        }});
        
        Schedule schedule = new Schedule();
        schedule.setMaxJobExecutionTime( 100 );
        
        BuildDefinition buildDef = new BuildDefinition();
        buildDef.setId( 1 );
        buildDef.setSchedule( schedule );
        
        buildManager.addProjectToBuildQueue( 1, buildDef, 1, "continuum-test-project-1", "build-def-label" );
        
        buildManager.addProjectToBuildQueue( 2, buildDef, 1, "continuum-test-project-2", "build-def-label" );
        
        // verify
        context.assertIsSatisfied();
        
        // assert if the project is in the correct build queue!
        OverallBuildQueue overallBuildQueue = buildManager.getOverallBuildQueueWhereProjectIsQueued( 1 );
        assertEquals( 1, overallBuildQueue.getId() );
        
        overallBuildQueue = buildManager.getOverallBuildQueueWhereProjectIsQueued( 2 );
        assertEquals( 2, overallBuildQueue.getId() );
        
        assertEquals( 2, buildManager.getOverallBuildQueuesInUse().size() );
    }
    
    public void testParallelBuildsLimitIsMaximized()
        throws Exception
    {
        // assert size of the overallBuildQueuesInUse!
    }
    
    public void testParallelBuildsBuildIsCancelled()
        throws Exception
    {
    
    }
    
    public void testParallelBuildsProjectAlreadyInBuildQueue()
        throws Exception
    {
    
    }
    
    public void testParallelBuildsRemoveFromBuildQueue()
        throws Exception
    {
    
    }
    
    public void testParallelBuildsAddToCheckoutQueue()
        throws Exception
    {
    
    }
        
    public void testParallelBuildsAddToPrepareBuildQueue()
        throws Exception
    {
    
    }
    
    public void testParallelBuildsProjectAlreadyInCheckoutQueue()
        throws Exception
    {
    
    }
    
    public void testParallelBuildsProjectAlreadyInPrepareBuildQueue()
        throws Exception
    {
    
    }
    
    public void testParallelBuildsRemoveFromCheckoutQueue()
        throws Exception
    {
    
    }
        
    public void testParallelBuildsRemoveFromPrepareBuildQueue()
        throws Exception
    {
    
    }
        
    public void testSingleBuild()
        throws Exception
    {
    
    }
}
