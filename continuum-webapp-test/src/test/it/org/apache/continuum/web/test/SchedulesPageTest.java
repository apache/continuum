package org.apache.continuum.web.test;

/*
 * Copyright 2005-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



import java.util.HashMap;

/**
 *
 *
 *
 */
public class SchedulesPageTest
    extends AbstractAuthenticatedAccessTestCase
{
    // Add Edit Page fields
    final public static String FIELD_NAME = "name";

    final public static String FIELD_DESCRIPTION = "description";

    final public static String FIELD_SECOND = "second";

    final public static String FIELD_MINUTE = "minute";

    final public static String FIELD_HOUR = "hour";

    final public static String FIELD_DAYOFMONTH = "dayOfMonth";

    final public static String FIELD_MONTH = "month";

    final public static String FIELD_DAYOFWEEK = "dayOfWeek";

    final public static String FIELD_YEAR = "year";

    final public static String FIELD_MAXJOBEXECUTIONTIME = "maxJobExecutionTime";

    final public static String FIELD_DELAY = "delay";


    // field values
    final public static String SCHEDULES_PAGE_TITLE = "Continuum - Schedules";

    final public static String DEFAULT_SCHEDULE = "DEFAULT_SCHEDULE";

    final public static String DEFAULT_SCHEDULE_DESCRIPTION = "Run hourly";

    final public static String DEFAULT_CRONVALUE = "0 0 * * * ?";

    final public static String DEFAULT_DELAY = "0";

    final public static String DEFAULT_MAXJOBEXECUTIONTIME = "0";

    final public static String EDIT_SCHEDULE_PAGE_TITLE = "Continuum - Edit Schedule";

    final public static String SCHEDULE_NAME = "Test Schedule";

    final public static String SCHEDULE_NAME_EDIT = "Test Schedule Edit";

    final public static String SCHEDULE_DESCRIPTION = "Test Description";

    final public static String SECOND = "1";

    final public static String MINUTE = "2";

    final public static String HOUR = "3";

    final public static String DAYOFMONTH = "?";

    final public static String MONTH = "4";

    final public static String DAYOFWEEK = "5";

    final public static String YEAR = "2020";

    final public static String MAXJOBEXECUTIONTIME = "6";

    final public static String DELAY = "7";

    public void setUp()
        throws Exception
    {
        super.setUp();

        clickLinkWithText( "Schedules" );

        assertSchedulesPage();
    }

    public String getUsername()
    {
        return this.adminUsername;
    }

    public String getPassword()
    {
        return this.adminPassword;
    }

/*    public void testBasicScheduleAddAndDelete()
        throws Exception
    {
        // add schedule
        clickButtonWithValue( "Add" );

        assertEditSchedulePage();

        inputSchedule( SCHEDULE_NAME, SCHEDULE_DESCRIPTION, SECOND, MINUTE, HOUR, DAYOFMONTH, MONTH, DAYOFWEEK, YEAR,
                       MAXJOBEXECUTIONTIME, DELAY, true );

        String cronSchedule = SECOND;
        cronSchedule += " " + MINUTE;
        cronSchedule += " " + HOUR;
        cronSchedule += " " + DAYOFMONTH;
        cronSchedule += " " + MONTH;
        cronSchedule += " " + DAYOFWEEK;
        cronSchedule += " " + YEAR;

        String[] columnValues = {SCHEDULE_NAME, SCHEDULE_DESCRIPTION, DELAY, cronSchedule, MAXJOBEXECUTIONTIME};

        assertTrue( "Can not add schedule",
                    getSelenium().isElementPresent( XPathExpressionUtil.getTableRow( columnValues ) ) );

        // delete schedule after adding
        deleteSchedule( SCHEDULE_NAME );

        assertFalse( "Can not delete schedule",
                     getSelenium().isElementPresent( XPathExpressionUtil.getTableRow( columnValues ) ) );
    }

    public void testEditSchedule()
        throws Exception
    {
        clickButtonWithValue( "Add" );

        assertEditSchedulePage();

        inputSchedule( SCHEDULE_NAME_EDIT, SCHEDULE_DESCRIPTION, SECOND, MINUTE, HOUR, DAYOFMONTH, MONTH, DAYOFWEEK,
                       YEAR, MAXJOBEXECUTIONTIME, DELAY, true );

        String cronSchedule = SECOND;
        cronSchedule += " " + MINUTE;
        cronSchedule += " " + HOUR;
        cronSchedule += " " + DAYOFMONTH;
        cronSchedule += " " + MONTH;
        cronSchedule += " " + DAYOFWEEK;
        cronSchedule += " " + YEAR;

        String[] columnValues = {SCHEDULE_NAME, SCHEDULE_DESCRIPTION, DELAY, cronSchedule, MAXJOBEXECUTIONTIME};

        // edit the schedule        
        clickLinkWithXPath(
            XPathExpressionUtil.getImgColumnElement( XPathExpressionUtil.ANCHOR, 5, "edit.gif", columnValues ) );

        inputSchedule( SCHEDULE_NAME_EDIT + "modified", SCHEDULE_DESCRIPTION + "updated", "2", "3", "4", "?", "6", "7",
                       "2021", "8", "9", false );

        cronSchedule = "2 3 4 ? 6 7 2021";

        String[] editedColumnValues =
            {SCHEDULE_NAME_EDIT + "modified", SCHEDULE_DESCRIPTION + "updated", "9", cronSchedule, "8"};

        assertTrue( "Can not edit schedule",
                    getSelenium().isElementPresent( XPathExpressionUtil.getTableRow( editedColumnValues ) ) );

        // check if the active state has been saved
        clickLinkWithXPath(
            XPathExpressionUtil.getImgColumnElement( XPathExpressionUtil.ANCHOR, 5, "edit.gif", editedColumnValues ) );

        assertEquals( "Can disable the schedule", CHECKBOX_UNCHECK, getFieldValue( "active" ) );

        //house keeping
        clickLinkWithText( "Schedules" );
        deleteSchedule( SCHEDULE_NAME_EDIT + "modified" );
    }

    public void testScheduleAddEditPageInputValidation()
    {
        clickButtonWithValue( "Add" );

        assertEditSchedulePage();

        HashMap fields = new HashMap();
        fields.put( FIELD_MAXJOBEXECUTIONTIME, "" );
        boolean valid = false;
        boolean wait = false;

        // test saving without editing anything from the initial edit page except for Max Job Execution Time
        inputSchedule( fields, wait, valid );

        assertTrue( "Name field not validated",
                    getSelenium().isElementPresent( "//tr/td[span='schedule.name.required']" ) );
        assertTrue( "Description field not validated",
                    getSelenium().isElementPresent( "//tr/td[span='schedule.version.required']" ) );
        assertTrue( "Max Job Execution Time field not validated",
                    getSelenium().isElementPresent( "//tr/td[span='schedule.maxJobExecutionTime.required']" ) );

        // go back to the schedules page
        clickLinkWithText( "Schedules" );

        // start new schedule add session
        clickButtonWithValue( "Add" );

        // test saving using spaces for name and description
        fields.put( FIELD_NAME, " " );
        fields.put( FIELD_DESCRIPTION, " " );

        inputSchedule( fields, wait, valid );

        //TODO: Fix text validation, we need real text and not a property in the screen
        assertTrue( "Name field not validated",
                     getSelenium().isElementPresent( "//tr/td[span='schedule.name.required']" ) );
        assertTrue( "Description field not validated",
                     getSelenium().isElementPresent( "//tr/td[span='schedule.version.required']" ) );

        // go back to the schedules page
        clickLinkWithText( "Schedules" );

        // start new schedule add session
        clickButtonWithValue( "Add" );

        // test saving using alpha characters for the maxjobexecution and delay
        // with valid name and description  
        fields.put( FIELD_NAME, SCHEDULE_NAME );
        fields.put( FIELD_DESCRIPTION, SCHEDULE_DESCRIPTION );
        fields.put( FIELD_MAXJOBEXECUTIONTIME, "abcde" );
        fields.put( FIELD_DELAY, "abcde" );

        inputSchedule( fields, wait, valid );

        //TODO: Fix text validation, we need real text and not a property in the screen
        assertFalse( "Name field improperly validated",
                     getSelenium().isElementPresent( "//tr/td[span='schedule.name.required']" ) );
        assertFalse( "Description field improperly validated",
                     getSelenium().isElementPresent( "//tr/td[span='schedule.version.required']" ) );
        assertFalse( "Max Job Execution Time field improperly validated",
                    getSelenium().isElementPresent( "//tr/td[span='schedule.maxJobExecutionTime.required']" ) );
        assertTrue( "MaxJobExecutionTime not validated", isTextPresent( "schedule.maxJobExecutionTime.invalid" ) );
        assertTrue( "Delay not validated", isTextPresent( "schedule.delay.invalid" ) );

        assertEditSchedulePage();
    }


    public void testScheduleAddEditPageDoubleErrorMessages()
    {
        clickButtonWithValue( "Add" );

        assertEditSchedulePage();

        HashMap fields = new HashMap();
        boolean valid = false;
        boolean wait = false;

        // test double error messages issue of webworks
        inputSchedule( fields, wait, valid );
        clickButtonWithValue( "Save", false );

        if ( "schedule.name.required".equals( getSelenium().getText( "//td/span" ) ) )
        {
            assertFalse( "Double Error Messages", "schedule.name.required".equals( getSelenium().getText( "//tr[2]/td/span" ) ) );
        }
        if ( "schedule.version.required".equals( getSelenium().getText( "//tr[4]/td/span" ) ) )
        {
            assertFalse( "Double Error Messages", "schedule.version.required".equals( getSelenium().getText( "//tr[5]/td/span" ) ) );
        }

        assertEditSchedulePage();
    }
*/
    public void testCancelAddSchedule()
    {
        clickButtonWithValue( "Add" );
        clickButtonWithValue( "Cancel" );
        assertSchedulesPage();
    }

    public void testCancelEditSchedule()
    {
        clickLinkWithXPath( "//img[@alt='Edit']" );
        clickButtonWithValue( "Cancel" );
        assertSchedulesPage();
    }

    public void testCancelDeleteSchedule()
    {
        clickLinkWithXPath( "//img[@alt='Delete']" );
        clickButtonWithValue( "Cancel" );
        assertSchedulesPage();
    }

    public void assertSchedulesPage()
    {
        assertPage( SCHEDULES_PAGE_TITLE );

        assertDefaultSchedule();
    }

    public void assertDefaultSchedule()
    {
        String[] columnValues = {DEFAULT_SCHEDULE, DEFAULT_SCHEDULE_DESCRIPTION, DEFAULT_DELAY, DEFAULT_CRONVALUE,
            DEFAULT_MAXJOBEXECUTIONTIME};

        assertTrue( "Default schedule not found",
                    getSelenium().isElementPresent( XPathExpressionUtil.getTableRow( columnValues ) ) );
    }

    public void assertEditSchedulePage()
    {
        assertPage( EDIT_SCHEDULE_PAGE_TITLE );

        //TODO: assert error messages

        assertEditSchedulePageInputFields();
    }

    public void assertEditSchedulePageInputFields()
    {
        //TODO: assert content

    	assertPage( "Continuum - Edit Schedule" );
    	assertTextPresent( "Edit Schedule" );
    	assertTextPresent( "Name*:" );
    	assertElementPresent( "name" );
    	assertTextPresent( "Description*:" );
    	assertElementPresent( "description" );
    	assertTextPresent( "Cron Expression:" );
    	assertTextPresent( "Second:" );
    	assertElementPresent( "saveSchedule_second" );
    	assertTextPresent( "Minute:" );
    	assertElementPresent( "saveSchedule_minute" );
    	assertTextPresent( "Hour:" );
    	assertElementPresent( "saveSchedule_hour" );
    	assertTextPresent( "Day of Month:" );
    	assertElementPresent( "saveSchedule_dayOfMonth" );
    	assertTextPresent( "Month:" );
    	assertElementPresent( "saveSchedule_month" );
    	assertTextPresent( "Day of Week:" );
    	assertElementPresent( "saveSchedule_dayOfWeek" );
    	assertTextPresent( "Year [optional]:" );
    	assertElementPresent( "saveSchedule_year" );
    	assertTextPresent( "Maximum job execution time (seconds)*:" );
    	assertElementPresent( "maxJobExecutionTime" );
    	assertTextPresent( "Quiet Period (seconds):" );
    	assertElementPresent( "delay" );
    	assertTextPresent( "Add Build Queue:" );
    	assertElementPresent( "availableBuildQueues" );
    	assertElementPresent( "selectedBuildQueues" );
    	assertElementPresent( "active" );
    }

    public void deleteSchedule( String scheduleName )
    {
        // after we save the schedule we should be brought back to the schedules page
        assertSchedulesPage();

        String[] columnValues = {scheduleName};

        clickLinkWithXPath(
            XPathExpressionUtil.getImgColumnElement( XPathExpressionUtil.ANCHOR, 5, "delete.gif", columnValues ) );

        // deletion confirmation page
        assertPage( "Schedule Removal" );
        //TODO: assert content
        //TODO: assert schedule name is in deletion confirmation

        clickButtonWithValue( "Delete" );

        // after we confirm the deletion we should be brought back to the schedules page
        assertSchedulesPage();
    }


    public void inputSchedule( String scheduleName, String scheduleDescription, String second, String minute,
                               String hour, String dayOfMonth, String month, String dayOfWeek, String year,
                               String maxJobExecutionTime, String delay, boolean active )
    {
        inputSchedule( scheduleName, scheduleDescription, second, minute, hour, dayOfMonth, month, dayOfWeek, year,
                       maxJobExecutionTime, delay, active, true );
    }


    public void inputSchedule( String scheduleName, String schedule_description, String second, String minute,
                               String hour, String dayOfMonth, String month, String dayOfWeek, String year,
                               String maxJobExecutionTime, String delay, boolean active, boolean wait )
    {
        assertEditSchedulePage();

        HashMap inputFields = new HashMap();

        inputFields.put( "name", scheduleName );
        inputFields.put( "description", schedule_description );
        inputFields.put( "second", second );
        inputFields.put( "minute", minute );
        inputFields.put( "hour", hour );
        inputFields.put( "dayOfMonth", dayOfMonth );
        inputFields.put( "month", month );
        inputFields.put( "dayOfWeek", dayOfWeek );
        inputFields.put( "year", year );
        inputFields.put( "maxJobExecutionTime", maxJobExecutionTime );
        inputFields.put( "delay", delay );

        if ( !active )
        {
            uncheckField( "active" );
        }

        inputSchedule( inputFields, wait, true );
    }

    public void inputSchedule( HashMap fields )
    {
        inputSchedule( fields, true, true );
    }

    public void inputSchedule( HashMap fields, boolean wait, boolean valid )
    {
        //setFieldValues( fields );

        clickButtonWithValue( "Save", wait );

        if ( valid )
        {
            // after we save the schedule we should be brought back to the schedules page        
            assertSchedulesPage();
        }
        else
        {
            assertEditSchedulePage();
        }
    }

    public void tearDown()
        throws Exception
    {
        //logout();
	super.tearDown();
    }
}
