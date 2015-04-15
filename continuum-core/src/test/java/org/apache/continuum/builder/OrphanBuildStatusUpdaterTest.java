package org.apache.continuum.builder;

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

import org.apache.continuum.builder.distributed.work.BuildStatusUpdater;
import org.apache.continuum.dao.BuildDefinitionDao;
import org.apache.continuum.dao.BuildResultDao;
import org.apache.maven.continuum.AbstractContinuumTest;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.apache.maven.continuum.project.ContinuumProjectState.*;
import static org.junit.Assert.assertEquals;

public class OrphanBuildStatusUpdaterTest
    extends AbstractContinuumTest
{

    private BuildResultDao resultDao;

    private BuildDefinitionDao buildDefDao;

    private List<BuildResult> canceled = new ArrayList<BuildResult>();

    private List<BuildResult> ok = new ArrayList<BuildResult>();

    private List<BuildResult> building = new ArrayList<BuildResult>();

    private BuildDefinition defOne;

    private BuildDefinition defTwo;

    @Before
    public void populateTestData()
        throws Exception
    {
        resultDao = lookup( BuildResultDao.class );
        buildDefDao = lookup( BuildDefinitionDao.class );

        defOne = addBuildDef();
        defTwo = addBuildDef();

        // NOTE: Build results added in build order - last added is most recent build

        Project noCleanup = addProject( "One In-Progress (No Cleanup)" );
        addResult( noCleanup, defOne, BUILDING, building );

        Project oneCleanup = addProject( "Two In-Progress With Success (One Cleanup)" );
        addResult( oneCleanup, defTwo, BUILDING, canceled );
        addResult( oneCleanup, defTwo, OK, ok );
        addResult( oneCleanup, defTwo, BUILDING, building );

        Project twoCleanup = addProject( "Three In-Progress With Success (Two Cleanup)" );
        addResult( twoCleanup, defTwo, BUILDING, canceled );
        addResult( twoCleanup, defTwo, OK, ok );
        addResult( twoCleanup, defTwo, BUILDING, canceled );
        addResult( twoCleanup, defTwo, BUILDING, building );

        Project bdNoCleanup = addProject( "Two In-Progress (No Cleanup)" );
        addResult( bdNoCleanup, defOne, BUILDING, building );
        addResult( bdNoCleanup, defTwo, BUILDING, building );
    }

    @Test
    public void testOrphansResolvedSafely()
        throws ContinuumStoreException
    {
        lookup( BuildStatusUpdater.class, "orphans" ).performScan();
        verifyResults();
    }

    private BuildDefinition addBuildDef()
        throws ContinuumStoreException
    {
        BuildDefinition def = new BuildDefinition();
        def.setAlwaysBuild( false );
        def.setBuildFresh( false );
        def.setDefaultForProject( false );
        def.setTemplate( false );
        def = buildDefDao.addBuildDefinition( def );
        return def;
    }

    private void addResult( Project project, BuildDefinition buildDef, int state, List<BuildResult> expected )
        throws ContinuumStoreException
    {
        BuildResult br = new BuildResult();

        // nullability constraints
        br.setBuildNumber( 0 );
        br.setEndTime( 0 );
        br.setExitCode( 0 );
        br.setStartTime( 0 );
        br.setTrigger( 0 );

        // associate relationship
        br.setBuildDefinition( buildDef );

        // set the build result
        br.setState( state );

        // persist the result
        resultDao.addBuildResult( project, br );
        br = resultDao.getBuildResult( br.getId() );
        expected.add( br );
    }

    private void verifyResults()
        throws ContinuumStoreException
    {
        for ( BuildResult br : ok )
        {
            assertEquals( "Successful results should be untouched", OK,
                          resultDao.getBuildResult( br.getId() ).getState() );
        }

        for ( BuildResult br : building )
        {
            assertEquals( "Latest building result for build def should be untouched", BUILDING,
                          resultDao.getBuildResult( br.getId() ).getState() );
        }

        for ( BuildResult br : canceled )
        {
            assertEquals( "Prior building results for build def should be canceled", CANCELLED,
                          resultDao.getBuildResult( br.getId() ).getState() );
        }
    }
}
