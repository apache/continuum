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
import org.testng.annotations.Test;

/**
 * @author José Morales Martínez
 * @version $Id$
 */
@Test( groups = { "schedule" } )
public class ScheduleTest
    extends AbstractAdminTest
{
    public void testAddScheduleNoBuildQueueToBeUsed()
    {
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
        goToAddSchedule();
        addEditSchedule( SCHEDULE_NAME, SCHEDULE_DESCRIPTION, SCHEDULE_EXPR_SECOND, SCHEDULE_EXPR_MINUTE,
                         SCHEDULE_EXPR_HOUR, SCHEDULE_EXPR_DAY_MONTH, SCHEDULE_EXPR_MONTH, SCHEDULE_EXPR_DAY_WEEK,
                         SCHEDULE_EXPR_YEAR, SCHEDULE_MAX_TIME, SCHEDULE_PERIOD, false, false );
        assertTextPresent( "Used Build Queues cannot be empty" );
    }

    @Test( dependsOnMethods = { "testAddScheduleNoBuildQueueToBeUsed" } )
    public void testAddSchedule()
    {
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
        goToAddSchedule();
        addEditSchedule( SCHEDULE_NAME, SCHEDULE_DESCRIPTION, SCHEDULE_EXPR_SECOND, SCHEDULE_EXPR_MINUTE,
                         SCHEDULE_EXPR_HOUR, SCHEDULE_EXPR_DAY_MONTH, SCHEDULE_EXPR_MONTH, SCHEDULE_EXPR_DAY_WEEK,
                         SCHEDULE_EXPR_YEAR, SCHEDULE_MAX_TIME, SCHEDULE_PERIOD, true, true );
    }

    @Test( dependsOnMethods = { "testAddScheduleNoBuildQueueToBeUsed" } )
    public void testAddScheduleWithInvalidValues()
    {
        String SCHEDULE_NAME = "!@#$<>?etc";
        String SCHEDULE_DESCRIPTION = "![]<>'^&etc";
        String SCHEDULE_EXPR_SECOND = getProperty( "SCHEDULE_EXPR_SECOND" );
        String SCHEDULE_EXPR_MINUTE = getProperty( "SCHEDULE_EXPR_MINUTE" );
        String SCHEDULE_EXPR_HOUR = getProperty( "SCHEDULE_EXPR_HOUR" );
        String SCHEDULE_EXPR_DAY_MONTH = getProperty( "SCHEDULE_EXPR_DAY_MONTH" );
        String SCHEDULE_EXPR_MONTH = getProperty( "SCHEDULE_EXPR_MONTH" );
        String SCHEDULE_EXPR_DAY_WEEK = getProperty( "SCHEDULE_EXPR_DAY_WEEK" );
        String SCHEDULE_EXPR_YEAR = getProperty( "SCHEDULE_EXPR_YEAR" );
        String SCHEDULE_MAX_TIME = getProperty( "SCHEDULE_MAX_TIME" );
        String SCHEDULE_PERIOD = getProperty( "SCHEDULE_PERIOD" );
        goToAddSchedule();
        addEditSchedule( SCHEDULE_NAME, SCHEDULE_DESCRIPTION, SCHEDULE_EXPR_SECOND, SCHEDULE_EXPR_MINUTE,
                         SCHEDULE_EXPR_HOUR, SCHEDULE_EXPR_DAY_MONTH, SCHEDULE_EXPR_MONTH, SCHEDULE_EXPR_DAY_WEEK,
                         SCHEDULE_EXPR_YEAR, SCHEDULE_MAX_TIME, SCHEDULE_PERIOD, true, false );
        assertTextPresent( "Name contains invalid characters." );
    }

    public void testAddInvalidSchedule()
    {
        goToAddSchedule();
        addEditSchedule( "", "", "", "", "", "", "", "", "", "", "", true, false );
        assertTextPresent( "Invalid cron expression value(s)" );
        assertTextPresent( "Name is required and cannot contain spaces only" );
        assertTextPresent( "Description is required and cannot contain spaces only" );
    }

    @Test( dependsOnMethods = { "testAddSchedule" } )
    public void testAddDuplicatedSchedule()
    {
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
        goToAddSchedule();
        addEditSchedule( SCHEDULE_NAME, SCHEDULE_DESCRIPTION, SCHEDULE_EXPR_SECOND, SCHEDULE_EXPR_MINUTE,
                         SCHEDULE_EXPR_HOUR, SCHEDULE_EXPR_DAY_MONTH, SCHEDULE_EXPR_MONTH, SCHEDULE_EXPR_DAY_WEEK,
                         SCHEDULE_EXPR_YEAR, SCHEDULE_MAX_TIME, SCHEDULE_PERIOD, true, false );
        assertTextPresent( "A Schedule with the same name already exists" );
    }

    @Test( dependsOnMethods = { "testAddDuplicatedSchedule" } )
    public void testEditSchedule()
    {
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
        String name = "new_name";
        String description = "new_description";
        String second = "1";
        String minute = "20";
        String hour = "15";
        String dayMonth = "20";
        String month = "9";
        String dayWeek = "?";
        String year = "";
        String maxTime = "9000";
        String period = "0";
        goToEditSchedule( SCHEDULE_NAME, SCHEDULE_DESCRIPTION, SCHEDULE_EXPR_SECOND, SCHEDULE_EXPR_MINUTE,
                          SCHEDULE_EXPR_HOUR, SCHEDULE_EXPR_DAY_MONTH, SCHEDULE_EXPR_MONTH, SCHEDULE_EXPR_DAY_WEEK,
                          SCHEDULE_EXPR_YEAR, SCHEDULE_MAX_TIME, SCHEDULE_PERIOD );
        addEditSchedule( name, description, second, minute, hour, dayMonth, month, dayWeek, year, maxTime, period,
                         false, true );
        goToEditSchedule( name, description, second, minute, hour, dayMonth, month, dayWeek, year, maxTime, period );
        addEditSchedule( SCHEDULE_NAME, SCHEDULE_DESCRIPTION, SCHEDULE_EXPR_SECOND, SCHEDULE_EXPR_MINUTE,
                         SCHEDULE_EXPR_HOUR, SCHEDULE_EXPR_DAY_MONTH, SCHEDULE_EXPR_MONTH, SCHEDULE_EXPR_DAY_WEEK,
                         SCHEDULE_EXPR_YEAR, SCHEDULE_MAX_TIME, SCHEDULE_PERIOD, false, true );
    }

    @Test( dependsOnMethods = { "testEditSchedule" } )
    public void testDeleteSchedule()
    {
        String SCHEDULE_NAME = getProperty( "SCHEDULE_NAME" );
        removeSchedule( SCHEDULE_NAME );
    }

    protected void removeSchedule( String name )
    {
        goToSchedulePage();
        clickLinkWithXPath( "(//a[contains(@href,'removeSchedule.action') and contains(@href, '" + name + "')])//img" );
        assertPage( "Continuum - Delete Schedule" );
        assertTextPresent( "Delete Schedule" );
        assertTextPresent( "Are you sure you want to delete the schedule \"" + name + "\"?" );
        assertButtonWithValuePresent( "Delete" );
        assertButtonWithValuePresent( "Cancel" );
        clickButtonWithValue( "Delete" );
        assertSchedulePage();
    }
}
