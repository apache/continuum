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

import org.apache.continuum.web.test.parent.AbstractAdminTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


/**
 * @author José Morales Martínez
 * @version $Id$
 */
@Test( groups = { "queue" } )
public class QueueTest
    extends AbstractAdminTest
{
    private String buildQueueName;

    @BeforeMethod
    protected void setUp()
        throws Exception
    {
        buildQueueName = getProperty( "BUILD_QUEUE_NAME" );
    }

    public void testAddBuildQueue()
    {
        setMaxBuildQueue( 2 );
        addBuildQueue( buildQueueName );
    }

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

    @Test( dependsOnMethods = { "testAddBuildQueue" } )
    public void testAddBuildQueueToSchedule()
    {
        String scheduleName = getProperty( "QUEUE_SCHEDULE_NAME" );
        String scheduleDescription = getProperty( "SCHEDULE_DESCRIPTION" );
        String second = getProperty( "SCHEDULE_EXPR_SECOND" );
        String minute = getProperty( "SCHEDULE_EXPR_MINUTE" );
        String hour = getProperty( "SCHEDULE_EXPR_HOUR" );
        String dayOfMonth = getProperty( "SCHEDULE_EXPR_DAY_MONTH" );
        String month = getProperty( "SCHEDULE_EXPR_MONTH" );
        String dayOfWeek = getProperty( "SCHEDULE_EXPR_DAY_WEEK" );
        String year = getProperty( "SCHEDULE_EXPR_YEAR" );
        String maxTime = getProperty( "SCHEDULE_MAX_TIME" );
        String period = getProperty( "SCHEDULE_PERIOD" );

        goToAddSchedule();
        addEditSchedule( scheduleName, scheduleDescription, second, minute, hour, dayOfMonth, month, dayOfWeek, year,
                         maxTime, period, true, true );
        goToEditSchedule( scheduleName, scheduleDescription, second, minute, hour, dayOfMonth, month, dayOfWeek, year,
                          maxTime, period );

        getSelenium().addSelection( "saveSchedule_availableBuildQueuesIds", "label=" + buildQueueName );
        getSelenium().click( "//input[@value='->']" );
        submit();
    }

    @Test( dependsOnMethods = { "testAddBuildQueue" } )
    public void testAddNotAllowedBuildQueue()
    {
        setMaxBuildQueue( 1 );
        String secondQueue = "second_queue_name";
        addBuildQueue( secondQueue, false );
        assertTextPresent( "You are only allowed 1 number of builds in parallel." );
    }

    @Test( dependsOnMethods = { "testAddBuildQueue" } )
    public void testAddAlreadyExistBuildQueue()
    {
        setMaxBuildQueue( 3 );
        addBuildQueue( buildQueueName, false );
        assertTextPresent( "Build queue name already exists." );
    }

    public void testAddEmptyBuildQueue()
    {
        setMaxBuildQueue( 3 );
        addBuildQueue( "", false, false );
        assertTextPresent( "You must define a name" );
    }

    public void testDeleteBuildQueue()
    {
        setMaxBuildQueue( 3 );
        goToBuildQueuePage();
        String testBuildQueue = "test_build_queue";
        addBuildQueue( testBuildQueue );

        removeBuildQueue( testBuildQueue );
        assertTextNotPresent( testBuildQueue );
    }

    @Test( dependsOnMethods = { "testQueuePageWithoutBuild" } )
    public void testQueuePageWithProjectCurrentlyBuilding()
        throws Exception
    {
        String pomUrl = getProperty( "MAVEN2_QUEUE_TEST_POM_URL" );
        String pomUsername = getProperty( "MAVEN2_QUEUE_TEST_POM_USERNAME" );
        String pomPassword = getProperty( "MAVEN2_QUEUE_TEST_POM_PASSWORD" );

        String projectGroupName = getProperty( "MAVEN2_QUEUE_TEST_POM_PROJECT_GROUP_NAME" );
        String projectGroupId = getProperty( "MAVEN2_QUEUE_TEST_POM_PROJECT_GROUP_ID" );
        String projectGroupDescription = getProperty( "MAVEN2_QUEUE_TEST_POM_PROJECT_GROUP_DESCRIPTION" );

        //build a project
        goToAddMavenTwoProjectPage();
        addMavenTwoProject( pomUrl, pomUsername, pomPassword, null, true );

        buildProjectForQueuePageTest( projectGroupName, projectGroupId, projectGroupDescription );
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
        assertTextPresent( projectGroupName );
        getSelenium().open( location );
        waitPage();
        waitForElementPresent( "//img[@alt='Success']" );
    }

    protected void goToBuildQueuePage()
    {
        clickLinkWithText( "Build Queue" );

        assertBuildQueuePage();
    }

    void assertBuildQueuePage()
    {
        assertPage( "Continumm - Parallel Build Queue" );
        assertTextPresent( "Continuum - Parallel Build Queue" );
        assertTextPresent( "Name" );
        assertTextPresent( "DEFAULT_BUILD_QUEUE" );
        assertButtonWithValuePresent( "Add" );
    }

    protected void removeBuildQueue( String queueName )
    {
        clickLinkWithXPath(
            "(//a[contains(@href,'deleteBuildQueue.action') and contains(@href, '" + queueName + "')])//img" );
        assertTextPresent( "Delete Parallel Build Queue" );
        assertTextPresent( "Are you sure you want to delete the build queue \"" + queueName + "\"?" );
        assertButtonWithValuePresent( "Delete" );
        assertButtonWithValuePresent( "Cancel" );
        clickButtonWithValue( "Delete" );
        assertBuildQueuePage();
    }

    void assertAddBuildQueuePage()
    {
        assertPage( "Continuum - Add/Edit Parallel Build Queue" );
        assertTextPresent( "Continuum - Add/Edit Parallel Build Queue" );
        assertTextPresent( "Name*" );
        assertElementPresent( "name" );
        assertButtonWithValuePresent( "Save" );
        assertButtonWithValuePresent( "Cancel" );
    }

    protected void addBuildQueue( String name )
    {
        addBuildQueue( name, true );
    }

    protected void addBuildQueue( String name, boolean success )
    {
        addBuildQueue( name, success, true );
    }

    protected void addBuildQueue( String name, boolean success, boolean waitForError )
    {
        goToBuildQueuePage();
        assertBuildQueuePage();
        submit();
        assertAddBuildQueuePage();
        setFieldValue( "name", name );
        if ( success )
        {
            submit();
            assertBuildQueuePage();
            assertTextPresent( name );
        }
        else
        {
            submit( waitForError );
            assertAddBuildQueuePage();
        }
    }
}
