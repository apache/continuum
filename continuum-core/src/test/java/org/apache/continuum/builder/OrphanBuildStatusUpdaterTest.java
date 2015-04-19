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

    private final int ageCutoff = 2;

    private BuildResultDao resultDao;

    private BuildDefinitionDao buildDefDao;

    private List<BuildResult> aged = new ArrayList<BuildResult>();

    private List<BuildResult> recent = new ArrayList<BuildResult>();

    private BuildDefinition defOne;

    private BuildDefinition defTwo;

    private OrphanBuildStatusUpdater updater;

    @Override
    protected String[] getConfigLocations()
    {
        return super.getConfigLocations();
    }

    @Before
    public void populateTestData()
        throws Exception
    {
        updater = (OrphanBuildStatusUpdater) lookup( BuildStatusUpdater.class, "orphans" );
        resultDao = lookup( BuildResultDao.class );
        buildDefDao = lookup( BuildDefinitionDao.class );

        defOne = addBuildDef();
        defTwo = addBuildDef();

        // NOTE: Build results added in build order - last added is most recent build

        Project p1 = addProject( "One Too Young" );
        addRecent( p1, defOne, BUILDING );

        Project p2 = addProject( "One Orphan" );
        addAged( p2, defOne, BUILDING );

        Project p3 = addProject( "Two Orphans, Interleaved Success" );
        addAged( p3, defTwo, BUILDING );
        addRecent( p3, defTwo, OK );
        addAged( p3, defTwo, BUILDING );

        Project p4 = addProject( "Two Orphans, Interleaved Success, One Too Young" );
        addAged( p4, defTwo, BUILDING );
        addRecent( p4, defTwo, OK );
        addAged( p4, defTwo, BUILDING );
        addRecent( p4, defTwo, BUILDING );

        Project p5 = addProject( "Two In-Progress, No Orphans" );
        addAged( p5, defOne, BUILDING );
        addAged( p5, defTwo, BUILDING );

        Project p6 = addProject( "Two In-Progress, No Orphans (Diff Build Defs)" );
        addRecent( p6, defOne, BUILDING );
        addRecent( p6, defTwo, BUILDING );

        updater.setOrphanAfterHours( ageCutoff );
    }

    @Test
    public void testOrphansCanceled()
        throws ContinuumStoreException
    {
        updater.performScan();
        verifyUntouched( recent );
        verifyCanceled( aged );
    }

    @Test
    public void testDisabledByZeroCutoff()
        throws ContinuumStoreException
    {
        updater.setOrphanAfterHours( 0 );
        updater.performScan();
        verifyUntouched( recent, aged );
    }

    @Test
    public void testDisabledByNegativeCutoff()
        throws ContinuumStoreException
    {
        updater.setOrphanAfterHours( Integer.MIN_VALUE );
        updater.performScan();
        verifyUntouched( recent, aged );
    }

    private void verifyUntouched( List<BuildResult>... resultLists )
        throws ContinuumStoreException
    {
        for ( List<BuildResult> list : resultLists )
        {
            for ( BuildResult br : list )
            {
                assertEquals( "Status should not have been touched: " + br, br.getState(),
                              resultDao.getBuildResult( br.getId() ).getState() );
            }
        }
    }

    private void verifyCanceled( List<BuildResult>... resultLists )
        throws ContinuumStoreException
    {
        for ( List<BuildResult> list : resultLists )
        {
            for ( BuildResult br : list )
            {
                assertEquals( "Status should be canceled: " + br, CANCELLED,
                              resultDao.getBuildResult( br.getId() ).getState() );
            }
        }
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

    private void addRecent( Project project, BuildDefinition buildDef, int state )
        throws ContinuumStoreException
    {
        addResult( project, buildDef, state, recent, ageCutoff - 1 );
    }

    private void addAged( Project project, BuildDefinition buildDef, int state )
        throws ContinuumStoreException
    {
        addResult( project, buildDef, state, aged, ageCutoff + 1 );
    }

    private void addResult( Project project, BuildDefinition buildDef, int state, List<BuildResult> expected,
                            long ageInHours )
        throws ContinuumStoreException
    {
        BuildResult br = new BuildResult();

        // nullability constraints
        br.setBuildNumber( 0 );
        br.setEndTime( 0 );
        br.setExitCode( 0 );
        br.setStartTime( System.currentTimeMillis() - ( 1000 * 60 * 60 * ageInHours ) );
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
}
