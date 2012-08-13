package org.apache.continuum.web.test;

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

import org.apache.continuum.web.test.parent.AbstractBuildQueueTest;
import org.testng.annotations.Test;


/**
 * @author José Morales Martínez
 * @version $Id$
 */


@Test( groups = {"queue"} )
public class QueueTest
    extends AbstractBuildQueueTest
{

    public void testAddBuildQueue()
    {
        setMaxBuildQueue( 2 );
        String BUILD_QUEUE_NAME = getProperty( "BUILD_QUEUE_NAME" );
        addBuildQueue( BUILD_QUEUE_NAME, true );
    }

    @Test( dependsOnMethods = {"testAddBuildQueue"} ) //"testDeleteBuildQueue" } )
    public void testQueuePageWithoutBuild()
    {
        clickAndWait( "link=Queues" );
        assertPage( "Continuum - Build Queue" );
        assertTextPresent( "Nothing is building" );
        assertTextNotPresent( "Project Name* Build Definition" );
        assertTextPresent( "Current Build" );
        assertTextPresent( "Build Queue" );
        assertTextPresent( "Current Checkout" );
        assertTextPresent( "Checkout Queue " );
        assertTextPresent( "Current Prepare Build" );
        assertTextPresent( "Prepare Build Queue" );
    }

    @Test( dependsOnMethods = {"testAddBuildQueue", "testAddSchedule"} )
    public void testAddBuildQueueToSchedule()
    {
        ScheduleTest sched = new ScheduleTest();

        String SCHEDULE_NAME = getProperty( "SCHEDULE_NAME" );
        String SCHEDULE_DESCRIPTION = getProperty( "SCHEDULE_DESCRIPTION" );
        String SCHEDULE_EXPR_SECOND = getProperty( "SCHEDULE_EXPR_SECOND" );
        String SCHEDULE_EXPR_MINUTE = getProperty( "SCHEDULE_EXPR_MINUTE" );
        String SCHEDULE_EXPR_HOUR = getProperty( "SCHEDULE_EXPR_HOUR" );
        String SCHEDULE_EXPR_DAY_MONTH = getProperty( "SCHEDULE_EXPR_DAY_MONTH" );
        String SCHEDULE_EXPR_MONTH = getProperty( "SCHEDULE_EXPR_MONTH" );
        String SCHEDULE_EXPR_DAY_WEEK = getProperty( "SCHEDULE_EXPR_DAY_WEEK" );
        String SCHEDULE_EXPR_YEAR = getProperty( "SCHEDULE_EXPR_YEAR" );
        String SCHEDULE_MAX_TIME = getProperty( "SCHEDULE_MAX_TIME" );
        String SCHEDULE_PERIOD = getProperty( "SCHEDULE_PERIOD" );

        String BUILD_QUEUE_NAME = getProperty( "BUILD_QUEUE_NAME" );

        sched.goToEditSchedule( SCHEDULE_NAME, SCHEDULE_DESCRIPTION, SCHEDULE_EXPR_SECOND, SCHEDULE_EXPR_MINUTE,
                                SCHEDULE_EXPR_HOUR, SCHEDULE_EXPR_DAY_MONTH, SCHEDULE_EXPR_MONTH,
                                SCHEDULE_EXPR_DAY_WEEK, SCHEDULE_EXPR_YEAR, SCHEDULE_MAX_TIME, SCHEDULE_PERIOD );
        getSelenium().addSelection( "saveSchedule_availableBuildQueuesIds", "label=" + BUILD_QUEUE_NAME );
        getSelenium().click( "//input[@value='->']" );
        submit();

    }

    @Test( dependsOnMethods = {"testAddBuildQueue"} )
    public void testAddNotAllowedBuildQueue()
    {
        setMaxBuildQueue( 1 );
        String secodQueue = "second_queue_name";
        addBuildQueue( secodQueue, false );
        assertTextPresent( "You are only allowed 1 number of builds in parallel." );
    }

    @Test( dependsOnMethods = {"testAddBuildQueue"} )
    public void testAddAlreadyExistBuildQueue()
    {
        setMaxBuildQueue( 3 );
        String BUILD_QUEUE_NAME = getProperty( "BUILD_QUEUE_NAME" );
        addBuildQueue( BUILD_QUEUE_NAME, false );
        assertTextPresent( "Build queue name already exists." );
    }

    @Test( dependsOnMethods = {"testAddAlreadyExistBuildQueue"} )
    public void testAddEmptyBuildQueue()
    {
        setMaxBuildQueue( 3 );
        addBuildQueue( "", false );
        assertTextPresent( "You must define a name" );
    }

    @Test( dependsOnMethods = {"testAddBuildQueueToSchedule"} )
    public void testDeleteBuildQueue()
    {
        goToBuildQueuePage();
        String BUILD_QUEUE_NAME = getProperty( "BUILD_QUEUE_NAME" );
        removeBuildQueue( BUILD_QUEUE_NAME );
        assertTextNotPresent( BUILD_QUEUE_NAME );
    }

    @Test( dependsOnMethods = {"testAddMavenTwoProject"} )
    public void testQueuePageWithProjectCurrentlyBuilding()
        throws Exception
    {
        //build a project
        String M2_PROJ_GRP_NAME = getProperty( "M2_PROJ_GRP_NAME" );
        String M2_PROJ_GRP_ID = getProperty( "M2_PROJ_GRP_ID" );
        String M2_PROJ_GRP_DESCRIPTION = getProperty( "M2_PROJ_GRP_DESCRIPTION" );
        buildProjectForQueuePageTest( M2_PROJ_GRP_NAME, M2_PROJ_GRP_ID, M2_PROJ_GRP_DESCRIPTION );
        String location = getSelenium().getLocation();

        //check queue page while building
        getSelenium().open( "/continuum/admin/displayQueues!display.action" );
        assertPage( "Continuum - Build Queue" );
        assertTextPresent( "Current Build" );
        assertTextPresent( "Build Queue" );
        assertTextPresent( "Current Checkout" );
        assertTextPresent( "Checkout Queue " );
        assertTextPresent( "Current Prepare Build" );
        assertTextPresent( "Prepare Build Queue" );
        assertElementPresent( "//table[@id='ec_table']/tbody/tr/td[4]" );
        assertTextPresent( M2_PROJ_GRP_NAME );
        getSelenium().open( location );
        waitPage();
        waitForElementPresent( "//img[@alt='Success']" );
    }

    @Test( dependsOnMethods = {"testQueuePageWithProjectCurrentlyBuilding", "testAddBuildAgent"} )
    public void testQueuePageWithProjectCurrentlyBuildingInDistributedBuilds()
        throws Exception
    {
        String M2_PROJ_GRP_NAME = getProperty( "M2_PROJ_GRP_NAME" );
        String M2_PROJ_GRP_ID = getProperty( "M2_PROJ_GRP_ID" );
        String M2_PROJ_GRP_DESCRIPTION = getProperty( "M2_PROJ_GRP_DESCRIPTION" );

        try
        {
            enableDistributedBuilds();
            buildProjectForQueuePageTest( M2_PROJ_GRP_NAME, M2_PROJ_GRP_ID, M2_PROJ_GRP_DESCRIPTION );

            //check queue page while building
            getSelenium().open( "/continuum/admin/displayQueues!display.action" );
            assertPage( "Continuum - View Distributed Builds" );
            assertTextPresent( "Current Build" );
            assertTextPresent( "Build Queue" );
            assertTextPresent( "Current Prepare Build" );
            assertTextPresent( "Prepare Build Queue" );
            assertTextPresent( M2_PROJ_GRP_NAME );
            assertTextPresent( "Build Agent URL" );
        }
        finally
        {
            disableDistributedBuilds();
        }
    }

}
