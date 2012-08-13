package org.apache.continuum.web.test.parent;

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

/**
 * @author José Morales Martínez
 * @version $Id$
 */
public abstract class AbstractScheduleTest
    extends AbstractAdminTest
{
    void goToSchedulePage()
    {
        clickLinkWithText( "Schedules" );

        assertSchedulePage();
    }

    protected void goToAddSchedule()
    {
        goToSchedulePage();
        clickButtonWithValue( "Add" );
        assertAddSchedulePage();
    }

    void assertSchedulePage()
    {
        assertPage( "Continuum - Schedules" );
        assertTextPresent( "Schedules" );
        assertTextPresent( "Name" );
        assertTextPresent( "Description" );
        assertTextPresent( "Quiet Period" );
        assertTextPresent( "Cron Expression" );
        assertTextPresent( "Max Job Time" );
        assertTextPresent( "Active" );
        assertTextPresent( "DEFAULT_SCHEDULE" );
        assertImgWithAlt( "Edit" );
        assertImgWithAlt( "Delete" );
        assertButtonWithValuePresent( "Add" );
    }

    void assertAddSchedulePage()
    {
        assertPage( "Continuum - Edit Schedule" );
        assertTextPresent( "Edit Schedule" );
        assertTextPresent( "Name" );
        assertElementPresent( "name" );
        assertTextPresent( "Description" );
        assertElementPresent( "description" );
        assertTextPresent( "Cron Expression" );
        assertTextPresent( "Second" );
        assertElementPresent( "second" );
        assertTextPresent( "Minute" );
        assertElementPresent( "minute" );
        assertTextPresent( "Hour" );
        assertElementPresent( "hour" );
        assertTextPresent( "Day of Month" );
        assertElementPresent( "dayOfMonth" );
        assertTextPresent( "Month" );
        assertElementPresent( "month" );
        assertTextPresent( "Day of Week" );
        assertElementPresent( "dayOfWeek" );
        assertTextPresent( "Year [optional]" );
        assertElementPresent( "year" );
        assertTextPresent( "Maximum job execution time" );
        assertElementPresent( "maxJobExecutionTime" );
        assertTextPresent( "Quiet Period (seconds):" );
        assertElementPresent( "delay" );
        assertTextPresent( "Add Build Queue" );
        assertElementPresent( "availableBuildQueuesIds" );
        assertElementPresent( "selectedBuildQueuesIds" );
        assertElementPresent( "active" );
        assertTextPresent( "Enable/Disable the schedule" );
        assertButtonWithValuePresent( "Save" );
        assertButtonWithValuePresent( "Cancel" );
    }

    protected void addEditSchedule( String name, String description, String second, String minute, String hour,
                                    String dayMonth, String month, String dayWeek, String year, String maxTime,
                                    String period, boolean buildQueue, boolean success )
    {
        if ( buildQueue )
        {
            setFieldValue( "name", name );
            setFieldValue( "description", description );
            setFieldValue( "second", second );
            setFieldValue( "minute", minute );
            setFieldValue( "hour", hour );
            setFieldValue( "dayOfMonth", dayMonth );
            setFieldValue( "month", month );
            setFieldValue( "dayOfWeek", dayWeek );
            setFieldValue( "year", year );
            setFieldValue( "maxJobExecutionTime", maxTime );
            setFieldValue( "delay", period );
            getSelenium().addSelection( "saveSchedule_availableBuildQueuesIds", "label=DEFAULT_BUILD_QUEUE" );
            getSelenium().click( "//input[@value='->']" );
            submit();
        }
        else
        {
            setFieldValue( "name", name );
            setFieldValue( "description", description );
            setFieldValue( "second", second );
            setFieldValue( "minute", minute );
            setFieldValue( "hour", hour );
            setFieldValue( "dayOfMonth", dayMonth );
            setFieldValue( "month", month );
            setFieldValue( "dayOfWeek", dayWeek );
            setFieldValue( "year", year );
            setFieldValue( "maxJobExecutionTime", maxTime );
            setFieldValue( "delay", period );
            submit();
        }

        if ( success )
        {
            assertSchedulePage();
        }
        else
        {
            assertAddSchedulePage();
        }
    }

    public void goToEditSchedule( String name, String description, String second, String minute, String hour,
                                  String dayMonth, String month, String dayWeek, String year, String maxTime,
                                  String period )
    {
        goToSchedulePage();
        String xPath = "//preceding::td[text()='" + name + "']//following::img[@alt='Edit']";
        clickLinkWithXPath( xPath );
        assertAddSchedulePage();
        assertFieldValue( name, "name" );
        assertFieldValue( description, "description" );
        assertFieldValue( second, "second" );
        assertFieldValue( minute, "minute" );
        assertFieldValue( hour, "hour" );
        assertFieldValue( dayMonth, "dayOfMonth" );
        assertFieldValue( month, "month" );
        assertFieldValue( dayWeek, "dayOfWeek" );
        assertFieldValue( year, "year" );
        assertFieldValue( maxTime, "maxJobExecutionTime" );
        assertFieldValue( period, "delay" );
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
