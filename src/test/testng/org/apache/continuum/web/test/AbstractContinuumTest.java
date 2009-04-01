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
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.testng.Assert;

/**
 * Based on AbstractContinuumTestCase of Emmanuel Venisse test.
 *
 * @author José Morales Martínez
 * @version $Id$
 */
public abstract class AbstractContinuumTest
    extends AbstractSeleniumTest
{

    // ////////////////////////////////////
    // About
    // ////////////////////////////////////
    public void goToAboutPage()
    {
        clickLinkWithText( "About" );

        assertAboutPage();
    }

    public void assertAboutPage()
    {
        assertPage( "Continuum - About" );
        assertTextPresent( "About Continuum" );
        assertTextPresent( "Version:" );
    }

    // ////////////////////////////////////
    // Build Queue
    // ////////////////////////////////////
    public void goToBuildQueuePage()
    {
        clickLinkWithText( "Build Queue" );

        assertBuildQueuePage();
    }

    public void assertBuildQueuePage()
    {
        // TODO: Bug in title
        assertPage( "Continumm - Parallel Build Queue" );
        assertTextPresent( "Continuum - Parallel Build Queue" );
        assertTextPresent( "Name" );
        assertTextPresent( "DEFAULT_BUILD_QUEUE" );
        assertButtonWithValuePresent( "Add" );
    }

    public void removeBuildQueue( String queueName )
    {
        clickLinkWithXPath( "(//a[contains(@href,'deleteBuildQueue.action') and contains(@href, '" + queueName
            + "')])//img" );
        assertTextPresent( "Delete Parallel Build Queue" );
        assertTextPresent( "Are you sure you want to delete the build queue \"" + queueName + "\"?" );
        assertButtonWithValuePresent( "Delete" );
        assertButtonWithValuePresent( "Cancel" );
        clickButtonWithValue( "Delete" );
        assertBuildQueuePage();
    }

    public void assertAddBuildQueuePage()
    {
        assertPage( "Continuum - Add/Edit Parallel Build Queue" );
        assertTextPresent( "Continuum - Add/Edit Parallel Build Queue" );
        assertTextPresent( "Name*" );
        assertElementPresent( "name" );
        assertButtonWithValuePresent( "Save" );
        assertButtonWithValuePresent( "Cancel" );
    }

    public void addBuildQueue( String name, boolean success )
    {
        goToBuildQueuePage();
        assertBuildQueuePage();
        submit();
        assertAddBuildQueuePage();
        setFieldValue( "name", name );
        submit();
        if ( success )
        {
            assertBuildQueuePage();
            assertTextPresent( name );
        }
        else
        {
            assertAddBuildQueuePage();
        }
    }

    public void setMaxBuildQueue( int maxBuildQueue )
    {
        goToConfigurationPage();
        setFieldValue( "numberOfAllowedBuildsinParallel", String.valueOf( maxBuildQueue ) );
        submit();
    }

    // ////////////////////////////////////
    // Purge
    // ////////////////////////////////////

    public void goToGeneralPurgePage()
    {
        clickLinkWithText( "Purge Configurations" );
        assertGeneralPurgePage();
    }

    public void assertGeneralPurgePage()
    {
        assertPage( "Continuum - Purge Configurations" );
        assertTextPresent( "Repository Purge Configurations" );
        assertTextPresent( "Directory Purge Configurations" );
        assertButtonWithValuePresent( "Add" );
    }

    public void removeRepositoryPurge( String purgeDescription )
    {
        goToGeneralPurgePage();
        clickLinkWithXPath( "(//a[contains(@href,'removePurgeConfig.action') and contains(@href, '" + purgeDescription
            + "')])//img" );
        assertTextPresent( "Delete Purge Configuration" );
        assertTextPresent( "Are you sure you want to delete Purge Configuration \"" + purgeDescription + "\"?" );
        assertButtonWithValuePresent( "Delete" );
        assertButtonWithValuePresent( "Cancel" );
        clickButtonWithValue( "Delete" );
        assertGeneralPurgePage();
    }

    public void removeDirectoryPurge( String purgeDescription )
    {
        goToGeneralPurgePage();
        clickLinkWithXPath( "(//a[contains(@href,'removePurgeConfig.action') and contains(@href, '" + purgeDescription
            + "')])//img" );
        assertTextPresent( "Delete Purge Configuration" );
        assertTextPresent( "Are you sure you want to delete Purge Configuration \"" + purgeDescription + "\"?" );
        assertButtonWithValuePresent( "Delete" );
        assertButtonWithValuePresent( "Cancel" );
        clickButtonWithValue( "Delete" );
        assertGeneralPurgePage();
    }

    public void assertAddRepositoryPurgePage()
    {
        assertPage( "Continuum - Add/Edit Purge Configuration" );
        assertTextPresent( "Add/Edit Purge Configuration" );
        assertTextPresent( "Repository" );
        assertElementPresent( "repositoryId" );
        assertTextPresent( "Days Older" );
        assertElementPresent( "daysOlder" );
        assertTextPresent( "Retention Count" );
        assertElementPresent( "retentionCount" );
        assertElementPresent( "deleteAll" );
        assertElementPresent( "deleteReleasedSnapshots" );
        assertElementPresent( "defaultPurgeConfiguration" );
        assertTextPresent( "Schedule" );
        assertElementPresent( "scheduleId" );
        assertTextPresent( "Description" );
        assertElementPresent( "description" );
        assertButtonWithValuePresent( "Save" );
        assertButtonWithValuePresent( "Cancel" );
    }

    public void assertAddEditDirectoryPurgePage()
    {
        assertPage( "Continuum - Add/Edit Purge Configuration" );
        assertTextPresent( "Add/Edit Purge Configuration" );
        assertTextPresent( "Directory Type" );
        assertElementPresent( "directoryType" );
        assertTextPresent( "Days Older" );
        assertElementPresent( "daysOlder" );
        assertTextPresent( "Retention Count" );
        assertElementPresent( "retentionCount" );
        assertElementPresent( "deleteAll" );
        assertElementPresent( "defaultPurgeConfiguration" );
        assertTextPresent( "Schedule" );
        assertElementPresent( "scheduleId" );
        assertTextPresent( "Description" );
        assertElementPresent( "description" );
        assertButtonWithValuePresent( "Save" );
        assertButtonWithValuePresent( "Cancel" );
    }

    public void goToAddRepositoryPurge()
    {
        goToGeneralPurgePage();
        assertGeneralPurgePage();
        clickLinkWithXPath( "//preceding::input[@value='repository' and @type='hidden']//following::input[@type='submit']" );
        assertAddRepositoryPurgePage();
    }

    public void goToEditRepositoryPurge( String daysOlder, String retentionCount, String description )
    {
        goToGeneralPurgePage();
        assertGeneralPurgePage();
        String xPath = "//preceding::td[text()='" + description + "']//following::img[@alt='Edit']";
        clickLinkWithXPath( xPath );
        assertAddRepositoryPurgePage();
        assertFieldValue( daysOlder, "daysOlder" );
        assertFieldValue( retentionCount, "retentionCount" );
        assertFieldValue( description, "description" );
    }

    public void goToEditDirectoryPurge( String daysOlder, String retentionCount, String description )
    {
        goToGeneralPurgePage();
        assertGeneralPurgePage();
        String xPath = "//preceding::td[text()='" + description + "']//following::img[@alt='Edit']";
        clickLinkWithXPath( xPath );
        assertAddEditDirectoryPurgePage();
        assertFieldValue( daysOlder, "daysOlder" );
        assertFieldValue( retentionCount, "retentionCount" );
        assertFieldValue( description, "description" );
    }

    public void addEditRepositoryPurge( String daysOlder, String retentionCount, String description, boolean success )
    {
        setFieldValue( "daysOlder", daysOlder );
        setFieldValue( "retentionCount", retentionCount );
        setFieldValue( "description", description );
        submit();
        if ( success )
        {
            assertGeneralPurgePage();
        }
        else
        {
            assertAddRepositoryPurgePage();
        }
    }

    public void goToAddDirectoryPurge()
    {
        goToGeneralPurgePage();
        assertGeneralPurgePage();
        clickLinkWithXPath( "//preceding::input[@value='directory' and @type='hidden']//following::input[@type='submit']" );
        assertAddEditDirectoryPurgePage();
    }

    public void addEditDirectoryPurge( String daysOlder, String retentionCount, String description, boolean success )
    {
        setFieldValue( "daysOlder", daysOlder );
        setFieldValue( "retentionCount", retentionCount );
        setFieldValue( "description", description );
        submit();
        if ( success )
        {
            assertGeneralPurgePage();
        }
        else
        {
            assertAddEditDirectoryPurgePage();
        }
    }

    // ////////////////////////////////////
    // LocalRepository
    // ////////////////////////////////////
    public void goToLocalRepositoryPage()
    {
        clickLinkWithText( "Local Repositories" );

        assertLocalRepositoryPage();
    }

    public void assertLocalRepositoryPage()
    {
        assertPage( "Continuum - Local Repositories" );
        assertTextPresent( "Local Repositories" );
        assertTextPresent( "Name" );
        assertTextPresent( "Location" );
        assertTextPresent( "Layout" );
        assertImgWithAlt( "Edit" );
        assertImgWithAlt( "Purge" );
        assertImgWithAlt( "Delete" );
        assertButtonWithValuePresent( "Add" );
    }

    public void assertAddLocalRepositoryPage()
    {
        assertPage( "Continuum - Add/Edit Local Repository" );
        assertTextPresent( "Continuum - Add/Edit Local Repository" );
        assertTextPresent( "Name" );
        assertElementPresent( "repository.name" );
        assertTextPresent( "Location" );
        assertElementPresent( "repository.location" );
        assertTextPresent( "Layout" );
        assertElementPresent( "repository.layout" );
        assertButtonWithValuePresent( "Save" );
        assertButtonWithValuePresent( "Cancel" );
    }

    public void removeLocalRepository( String name )
    {
        goToLocalRepositoryPage();
        String xPath = "//preceding::td[text()='" + name + "']//following::img[@alt='Delete']";
        clickLinkWithXPath( xPath );
        assertTextPresent( "Delete Local Repository" );
        assertTextPresent( "Are you sure you want to delete Local Repository \"" + name + "\" ?" );
        assertButtonWithValuePresent( "Delete" );
        assertButtonWithValuePresent( "Cancel" );
        clickButtonWithValue( "Delete" );
        assertLocalRepositoryPage();
    }

    public void goToAddLocalRepository()
    {
        goToLocalRepositoryPage();
        clickButtonWithValue( "Add" );
        assertAddLocalRepositoryPage();
    }

    public void goToEditLocalRepository( String name, String location )
    {
        goToLocalRepositoryPage();
        String xPath = "//preceding::td[text()='" + name + "']//following::img[@alt='Edit']";
        clickLinkWithXPath( xPath );
        assertAddLocalRepositoryPage();
        assertFieldValue( name, "repository.name" );
        assertFieldValue( location, "repository.location" );
    }

    public void addEditLocalRepository( String name, String location, boolean success )
    {
        setFieldValue( "repository.name", name );
        setFieldValue( "repository.location", location );
        submit();
        if ( success )
        {
            assertLocalRepositoryPage();
        }
        else
        {
            assertAddLocalRepositoryPage();
        }
    }

    // ////////////////////////////////////
    // Schedule
    // ////////////////////////////////////

    public void goToSchedulePage()
    {
        clickLinkWithText( "Schedules" );

        assertSchedulePage();
    }

    public void goToAddSchedule()
    {
        goToSchedulePage();
        clickButtonWithValue( "Add" );
        assertAddSchedulePage();
    }

    public void assertSchedulePage()
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

    public void assertAddSchedulePage()
    {
        assertPage( "Continuum - Edit Schedule" );
        assertTextPresent( "Continuum - Edit Schedule" );
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
        assertElementPresent( "availableBuildQueues" );
        assertElementPresent( "selectedBuildQueues" );
        assertElementPresent( "active" );
        assertTextPresent( "Enable/Disable the schedule" );
        assertButtonWithValuePresent( "Save" );
        assertButtonWithValuePresent( "Cancel" );
    }

    public void addEditSchedule( String name, String description, String second, String minute, String hour,
                                 String dayMonth, String month, String dayWeek, String year, String maxTime,
                                 String period, boolean success )
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

    public void removeSchedule( String name )
    {
        goToSchedulePage();
        clickLinkWithXPath( "(//a[contains(@href,'removeSchedule.action') and contains(@href, '" + name + "')])//img" );
        // TODO: Change Title "Continuum -"
        assertPage( "Schedule Removal" );
        assertTextPresent( "Schedule Removal" );
        assertTextPresent( "Are you sure you want to delete the schedule \"" + name + "\"?" );
        assertButtonWithValuePresent( "Delete" );
        assertButtonWithValuePresent( "Cancel" );
        clickButtonWithValue( "Delete" );
        assertSchedulePage();
    }

    // ////////////////////////////////////
    // Configuration
    // ////////////////////////////////////

    public void goToConfigurationPage()
    {
        clickLinkWithText( "Configuration" );
        assertEditConfigurationPage();
    }

    public void assertEditConfigurationPage()
    {
        assertPage( "Continuum - Configuration" );
        assertTextPresent( "General Configuration " );
        assertTextPresent( "Working Directory" );
        assertElementPresent( "workingDirectory" );
        assertTextPresent( "Build Output Directory" );
        assertElementPresent( "buildOutputDirectory" );
        assertTextPresent( "Release Output Directory" );
        assertElementPresent( "releaseOutputDirectory" );
        assertTextPresent( "Deployment Repository Directory" );
        assertElementPresent( "deploymentRepositoryDirectory" );
        assertTextPresent( "Base URL" );
        assertElementPresent( "baseUrl" );
        assertTextPresent( "Number of Allowed Builds in Parallel" );
        assertElementPresent( "numberOfAllowedBuildsinParallel" );
        assertTextPresent( "Enable Distributed Builds" );
        assertElementPresent( "distributedBuildEnabled" );
        assertButtonWithValuePresent( "Save" );
        assertButtonWithValuePresent( "Cancel" );
    }

    public void assertEditedConfigurationPage( String working, String buildOutput, String releaseOutput,
                                               String deploymentRepository, String baseUrl, String numberBuildParallel )
    {
        assertPage( "Continuum - Configuration" );
        assertTextPresent( "General Configuration " );
        assertTextPresent( "Working Directory" );
        assertElementNotPresent( "workingDirectory" );
        assertTextPresent( working );
        assertTextPresent( "Build Output Directory" );
        assertElementNotPresent( "buildOutputDirectory" );
        assertTextPresent( buildOutput );
        assertTextPresent( "Release Output Directory" );
        assertElementNotPresent( "releaseOutputDirectory" );
        assertTextPresent( releaseOutput );
        assertTextPresent( "Deployment Repository Directory" );
        assertElementNotPresent( "deploymentRepositoryDirectory" );
        assertTextPresent( deploymentRepository );
        assertTextPresent( "Base URL" );
        assertElementNotPresent( "baseUrl" );
        assertTextPresent( baseUrl );
        assertTextPresent( "Number of Allowed Builds in Parallel" );
        assertElementNotPresent( "numberOfAllowedBuildsinParallel" );
        assertTextPresent( numberBuildParallel );
        assertTextPresent( "Enable Distributed Builds" );
        assertElementNotPresent( "distributedBuildEnabled" );
        assertButtonWithValuePresent( "Edit" );

    }

    public void submitConfiguration( String working, String buildOutput, String releaseOutput,
                                     String deploymentRepository, String baseUrl, String numberBuildParallel,
                                     boolean distributed, boolean success )
    {
        setFieldValue( "workingDirectory", working );
        setFieldValue( "buildOutputDirectory", buildOutput );
        setFieldValue( "releaseOutputDirectory", releaseOutput );
        setFieldValue( "deploymentRepositoryDirectory", deploymentRepository );
        setFieldValue( "baseUrl", baseUrl );
        setFieldValue( "numberOfAllowedBuildsinParallel", numberBuildParallel );
        if ( distributed )
        {
            checkField( "distributedBuildEnabled" );
        }
        else
        {
            uncheckField( "distributedBuildEnabled" );
        }
        submit();
        if ( success )
        {
            assertEditedConfigurationPage( working, buildOutput, releaseOutput, deploymentRepository, baseUrl,
                                           numberBuildParallel );
        }else {
            assertEditConfigurationPage();
        }
    }

    // ////////////////////////////////////
    // ANT/SHELL Projects
    // ////////////////////////////////////
    public void assertAddProjectPage( String type )
    {
        String title = type.substring( 0, 1 ).toUpperCase() + type.substring( 1 ).toLowerCase();
        assertPage( "Continuum - Add " + title + " Project" );
        assertTextPresent( "Add " + title + " Project" );
        assertTextPresent( "Project Name" );
        assertElementPresent( "projectName" );
        assertTextPresent( "Version" );
        assertElementPresent( "projectVersion" );
        assertTextPresent( "Scm Url" );
        assertElementPresent( "projectScmUrl" );
        assertTextPresent( "Scm Username" );
        assertElementPresent( "projectScmUsername" );
        assertTextPresent( "Scm Password" );
        assertElementPresent( "projectScmPassword" );
        assertTextPresent( "Scm Branch/Tag" );
        assertElementPresent( "projectScmTag" );
        assertLinkPresent( "Maven SCM URL" );
    }

    public void assertAddAntProjectPage()
    {
        assertAddProjectPage( "ant" );
    }

    public void assertAddShellProjectPage()
    {
        assertAddProjectPage( "shell" );
    }

    // ////////////////////////////////////
    // Project Groups
    // ////////////////////////////////////
    public void goToProjectGroupsSummaryPage()
        throws Exception
    {
        clickLinkWithText( "Show Project Groups" );

        assertProjectGroupsSummaryPage();
    }

    public void assertProjectGroupsSummaryPage()
    {
        assertPage( "Continuum - Group Summary" );
        assertTextPresent( "Project Groups" );

        if ( isTextPresent( "Project Groups list is empty." ) )
        {
            assertTextNotPresent( "Name" );
            assertTextNotPresent( "Group Id" );
        }
        else
        {
            assertTextPresent( "Name" );
            assertTextPresent( "Group Id" );
        }
    }

    // ////////////////////////////////////
    // Project Group
    // ////////////////////////////////////
    public void showProjectGroup( String name, String groupId, String description )
        throws Exception
    {
        goToProjectGroupsSummaryPage();

        // Checks the link to the created Project Group
        assertLinkPresent( name );
        clickLinkWithText( name );

        assertProjectGroupSummaryPage( name, groupId, description );
    }

    public void assertProjectGroupSummaryPage( String name, String groupId, String description )
    {
        assertPage( "Continuum - Project Group" );
        assertTextPresent( "Project Group Name" );
        assertTextPresent( name );
        assertTextPresent( "Project Group Id" );
        assertTextPresent( groupId );
        assertTextPresent( "Description" );
        assertTextPresent( description );

        // Assert the available Project Group Actions
        assertTextPresent( "Group Actions" );
        assertElementPresent( "build" );
        assertElementPresent( "edit" );
        assertElementPresent( "remove" );

        assertTextPresent( "Project Group Scm Root" );

        if ( isTextPresent( "Member Projects" ) )
        {
            assertTextPresent( "Project Name" );
            assertTextPresent( "Version" );
            assertTextPresent( "Build" );
        }
        else
        {
            assertTextNotPresent( "Project Name" );
        }
    }

    public void addProjectGroup( String name, String groupId, String description, boolean success )
        throws Exception
    {
        goToProjectGroupsSummaryPage();

        // Go to Add Project Group Page
        clickButtonWithValue( "Add Project Group" );
        assertAddProjectGroupPage();

        // Enter values into Add Project Group fields, and submit
        setFieldValue( "name", name );
        setFieldValue( "groupId", groupId );
        setFieldValue( "description", description );

        submit();
        if ( success )
        {
            assertProjectGroupsSummaryPage();
        }
        else
        {
            assertAddProjectGroupPage();
        }
    }

    public void assertAddProjectGroupPage()
    {
        assertPage( "Continuum - Add Project Group" );

        assertTextPresent( "Add Project Group" );
        assertTextPresent( "Project Group Name" );
        assertElementPresent( "name" );
        assertTextPresent( "Project Group Id" );
        assertElementPresent( "groupId" );
        assertTextPresent( "Description" );
        assertElementPresent( "description" );
    }

    public void removeProjectGroup( String name, String groupId, String description )
        throws Exception
    {
        showProjectGroup( name, groupId, description );

        // Remove
        clickLinkWithLocator( "remove" );

        // Assert Confirmation
        assertElementPresent( "removeProjectGroup_" );
        assertElementPresent( "Cancel" );

        // Confirm Project Group deletion
        clickButtonWithValue( "Save" );
        assertProjectGroupsSummaryPage();
    }

    public void editProjectGroup( String name, String groupId, String description, String newName, String newDescription )
        throws Exception
    {
        showProjectGroup( name, groupId, description );
        clickButtonWithValue( "Edit" );
        assertEditGroupPage( groupId );
        setFieldValue( "saveProjectGroup_name", newName );
        setFieldValue( "saveProjectGroup_description", newDescription );
        clickButtonWithValue( "Save" );
    }

    public void assertEditGroupPage( String groupId )
        throws Exception
    {
        assertPage( "Continuum - Update Project Group" );
        assertTextPresent( "Update Project Group" );
        assertTextPresent( "Project Group Name" );
        assertTextPresent( "Project Group Id" );
        assertTextPresent( groupId );
        assertTextPresent( "Description" );
        assertTextPresent( "Homepage Url" );
        assertTextPresent( "Local Repository" );
        assertElementPresent( "saveProjectGroup_" );
        assertElementPresent( "Cancel" );
    }

    public void buildProjectGroup( String projectGroupName, String groupId, String description )
        throws Exception
    {
        int currentIt = 1;
        int maxIt = 20;
        showProjectGroup( projectGroupName, groupId, description );
        clickButtonWithValue( "Build all projects" );

        while ( isElementPresent( "//img[@alt='Building']" ) || isElementPresent( "//img[@alt='Updating']" ) )
        {
            Thread.sleep( 10000 );
            getSelenium().refresh();
            waitPage();
            if ( currentIt > maxIt )
            {
                Assert.fail( "Timeout, Can't build project group" );
            }
            currentIt++;
        }
        clickLinkWithText( p.getProperty( "M2_PROJ_GRP_NAME" ) );
        clickLinkWithText( "Builds" );
        clickLinkWithText( "Result" );
        assertTextPresent( "BUILD SUCCESSFUL" );
        clickLinkWithText( "Project Group Summary" );
    }

    public void assertReleaseSuccess()
    {
        assertTextPresent( "Choose Release Goal for Apache Maven" );
    }

    public void addValidM2ProjectFromProjectGroup( String projectGroupName, String groupId, String description,
                                                   String m2PomUrl )
        throws Exception
    {
        showProjectGroup( projectGroupName, groupId, description );
        selectValue( "projectTypes", "Add M2 Project" );
        clickButtonWithValue( "Add" );
        assertAddMavenTwoProjectPage();

        setFieldValue( "m2PomUrl", m2PomUrl );
        clickButtonWithValue( "Add" );

        // if success redirect to summary page
        assertProjectGroupsSummaryPage();
    }

    public void goToGroupBuildDefinitionPage( String projectGroupName, String groupId, String description )
        throws Exception
    {
        showProjectGroup( projectGroupName, groupId, description );
        clickLinkWithText( "Build Definitions" );
        assertTextPresent( "Project Group Build Definitions of " + projectGroupName + " group" );

        assertGroupBuildDefinitionPage( projectGroupName );

    }

    public void assertGroupBuildDefinitionPage( String projectGroupName )
    {

        assertTextPresent( "Project Group Build Definitions of " + projectGroupName + " group" );
    }

    public void assertDeleteBuildDefinitionPage( String description, String goals )
    {
        assertTextPresent( "Are you sure you want to delete the build definition with description \"" + description
            + "\", goals \"" + goals + "\" and id" );
        isButtonWithValuePresent( "Cancel" );
        isButtonWithValuePresent( "Delete" );
    }

    public void assertAddEditBuildDefinitionPage()
    {
        assertTextPresent( "Add/Edit Build Definition" );
        assertTextPresent( "POM filename*:" );
        assertElementPresent( "buildFile" );
        assertTextPresent( "Goals:" );
        assertElementPresent( "goals" );
        assertTextPresent( "Arguments:" );
        assertElementPresent( "arguments" );
        assertTextPresent( "Build Fresh" );
        assertElementPresent( "buildFresh" );
        assertTextPresent( "Always Build" );
        assertElementPresent( "alwaysBuild" );
        assertTextPresent( "Is it default?" );
        assertTextPresent( "Schedule:" );
        assertElementPresent( "scheduleId" );
        assertTextPresent( "Description" );
        assertElementPresent( "description" );
        assertTextPresent( "Type" );
        assertElementPresent( "buildDefinitionType" );
        assertTextPresent( "Build Environment" );
        assertElementPresent( "profileId" );
    }

    public void addEditGroupBuildDefinition( String groupName, String buildFile, String goals, String arguments,
                                             String description, boolean buildFresh, boolean alwaysBuild,
                                             boolean isDefault )
    {
        assertAddEditBuildDefinitionPage();
        // Enter values into Add Build Definition fields, and submit
        setFieldValue( "buildFile", buildFile );
        setFieldValue( "goals", goals );
        setFieldValue( "arguments", arguments );
        setFieldValue( "description", description );

        if ( buildFresh )
        {
            checkField( "buildFresh" );
        }
        else
        {
            uncheckField( "buildFresh" );
        }
        if ( isElementPresent( "defaultBuildDefinition" ) )
        {
            if ( isDefault )
            {
                checkField( "defaultBuildDefinition" );
            }
            else
            {
                uncheckField( "defaultBuildDefinition" );
            }
        }
        if ( alwaysBuild )
        {
            checkField( "alwaysBuild" );
        }
        else
        {
            uncheckField( "alwaysBuild" );
        }

        submit();
        if ( groupName != null )
        {
            assertGroupBuildDefinitionPage( groupName );
        }
        else
        {
            assertProjectInformationPage();
        }
    }

    public void assertGroupNotifierPage( String projectGroupName )
    {
        assertTextPresent( "Project Group Notifiers of group " + projectGroupName );
    }

    public void assertProjectNotifierPage()
    {
        assertTextPresent( "Add Notifier" );
    }

    public void assertAddNotifierPage()
    {
        assertPage( "Continuum - Add Notifier" );
        assertTextPresent( "Add Notifier" );
        assertTextPresent( "Type" );
        assertElementPresent( "notifierType" );
        assertElementPresent( "Cancel" );
    }

    public void assertAddEditMailNotifierPage()
    {
        assertPage( "Continuum - Add/Edit Mail Notifier" );
        assertTextPresent( "Add/Edit Mail Notifier" );
        assertTextPresent( "Mail Recipient Address" );
        assertTextPresent( "Send a mail to latest committers" );
        assertTextPresent( "Send on Success" );
        assertTextPresent( "Send on Failure" );
        assertTextPresent( "Send on Error" );
        assertTextPresent( "Send on Warning" );
        // TODO: Replace On for on
        assertTextPresent( "Send on SCM Failure" );
        assertElementPresent( "address" );
        assertElementPresent( "Cancel" );
    }

    public void assertAddEditIrcNotifierPage()
    {
        assertPage( "Continuum - Add/Edit IRC Notifier" );

        assertTextPresent( "IRC Host" );
        assertElementPresent( "host" );

        assertTextPresent( "IRC port" );
        assertElementPresent( "port" );

        assertTextPresent( "IRC channel" );
        assertElementPresent( "channel" );

        assertTextPresent( "Nick Name" );
        assertElementPresent( "nick" );

        assertTextPresent( "Alternate Nick Name" );
        assertElementPresent( "alternateNick" );

        assertTextPresent( "User Name" );
        assertElementPresent( "username" );

        assertTextPresent( "Full Name" );
        assertElementPresent( "fullName" );

        assertTextPresent( "Password" );
        assertElementPresent( "password" );

        assertTextPresent( "SSL" );
        assertTextPresent( "Send on Success" );
        assertTextPresent( "Send on Failure" );
        assertTextPresent( "Send on Error" );
        assertTextPresent( "Send on Warning" );
        assertTextPresent( "Send on SCM Failure" );
    }

    public void assertAddEditJabberPage()
    {
        assertPage( "Continuum - Add/Edit Jabber Notifier" );

        assertTextPresent( "Jabber Host" );
        assertElementPresent( "host" );
        assertTextPresent( "Jabber port" );
        assertElementPresent( "port" );
        assertTextPresent( "Jabber login" );
        assertElementPresent( "login" );
        assertTextPresent( "Jabber Password" );
        assertElementPresent( "password" );
        assertTextPresent( "Jabber Domain Name" );
        assertElementPresent( "domainName" );
        assertTextPresent( "Jabber Recipient Address" );
        assertElementPresent( "address" );

        assertTextPresent( "Is it a SSL connection?" );
        assertTextPresent( "Is it a Jabber group?" );
        assertTextPresent( "Send on Success" );
        assertTextPresent( "Send on Failure" );
        assertTextPresent( "Send on Error" );
        assertTextPresent( "Send on Warning" );
        // TODO: Change On for on
        assertTextPresent( "Send on SCM Failure" );
    }

    public void assertAddEditMsnPage()
    {
        assertPage( "Continuum - Add/Edit MSN Notifier" );

        assertTextPresent( "MSN login" );
        assertElementPresent( "login" );
        assertTextPresent( "MSN Password" );
        assertElementPresent( "password" );
        assertTextPresent( "MSN Recipient Address" );
        assertElementPresent( "address" );

        assertTextPresent( "Send on Success" );
        assertTextPresent( "Send on Failure" );
        assertTextPresent( "Send on Error" );
        assertTextPresent( "Send on Warning" );
        assertTextPresent( "Send on SCM Failure" );
    }

    public void assertAddEditWagonPage()
    {
        assertPage( "Continuum - Add/Edit Wagon Notifier" );

        assertTextPresent( "Project Site URL" );
        assertTextPresent( "Server Id (defined in your settings.xml for authentication)" );
        assertElementPresent( "url" );
        assertElementPresent( "id" );
        assertTextPresent( "Send on Success" );
        assertTextPresent( "Send on Failure" );
        assertTextPresent( "Send on Error" );
        assertTextPresent( "Send on Warning" );
    }

    public void goToGroupNotifier( String projectGroupName, String projectGroupId, String projectGroupDescription )
        throws Exception
    {
        showProjectGroup( projectGroupName, projectGroupId, projectGroupDescription );
        clickLinkWithText( "Notifiers" );
        assertGroupNotifierPage( projectGroupName );
        clickButtonWithValue( "Add" );
        assertAddNotifierPage();
    }

    public void goToProjectNotifier( String projectGroupName, String projectName )
        throws Exception
    {
        goToProjectInformationPage( projectGroupName, projectName );
        clickLinkWithXPath( "//input[contains(@id,'addProjectNotifier') and @type='submit']" );
        assertAddNotifierPage();
    }

    public void addMailNotifier( String projectGroupName, String projectName, String email, boolean isValid )
        throws Exception
    {
        selectValue( "//select", "Mail" );
        clickButtonWithValue( "Submit" );
        assertAddEditMailNotifierPage();
        setFieldValue( "address", email );
        clickButtonWithValue( "Save" );

        if ( !isValid )
        {
            assertTextPresent( "Address is invalid" );
        }
        else if ( projectName != null )
        {
            assertProjectInformationPage();
        }
        else
        {
            assertGroupNotifierPage( projectGroupName );
        }
    }

    public void editMailNotifier( String projectGroupName, String projectName, String oldMail, String newMail,
                                  boolean isValid )
        throws Exception
    {
        if ( projectName == null )
        {
            clickLinkWithXPath( "(//a[contains(@href,'editProjectGroupNotifier') and contains(@href,'mail')])//img" );
        }
        else
        {
            clickLinkWithXPath( "(//a[contains(@href,'editProjectNotifier') and contains(@href,'mail')])//img" );
        }
        assertAddEditMailNotifierPage();
        assertFieldValue( oldMail, "address" );
        setFieldValue( "address", newMail );
        clickButtonWithValue( "Save" );

        if ( !isValid )
        {
            assertTextPresent( "Address is invalid" );
        }
        else if ( projectName != null )
        {
            assertProjectInformationPage();
        }
        else
        {
            assertGroupNotifierPage( projectGroupName );
        }
    }

    public void addIrcNotifier( String projectGroupName, String projectName, String host, String channel,
                                boolean isValid )
        throws Exception
    {
        selectValue( "//select", "IRC" );
        clickButtonWithValue( "Submit" );
        assertAddEditIrcNotifierPage();
        setFieldValue( "host", host );
        setFieldValue( "channel", channel );

        clickButtonWithValue( "Save" );
        if ( !isValid )
        {
            assertTextPresent( "Host is required" );
            assertTextPresent( "Channel is required" );
        }
        else if ( projectName != null )
        {
            assertProjectInformationPage();
        }
        else
        {
            assertGroupNotifierPage( projectGroupName );
        }
    }

    public void editIrcNotifier( String projectGroupName, String projectName, String oldHost, String oldChannel,
                                 String newHost, String newChannel, boolean isValid )
        throws Exception
    {
        if ( projectName == null )
        {
            clickLinkWithXPath( "(//a[contains(@href,'editProjectGroupNotifier') and contains(@href,'irc')])//img" );
        }
        else
        {
            clickLinkWithXPath( "(//a[contains(@href,'editProjectNotifier') and contains(@href,'irc')])//img" );
        }
        assertAddEditIrcNotifierPage();
        assertFieldValue( oldHost, "host" );
        assertFieldValue( oldChannel, "channel" );
        setFieldValue( "host", newHost );
        setFieldValue( "channel", newChannel );
        clickButtonWithValue( "Save" );

        if ( !isValid )
        {
            assertTextPresent( "Host is required" );
            assertTextPresent( "Channel is required" );
        }
        else if ( projectName != null )
        {
            assertProjectInformationPage();
        }
        else
        {
            assertGroupNotifierPage( projectGroupName );
        }
    }

    public void addJabberNotifier( String projectGroupName, String projectName, String host, String login,
                                   String password, String address, boolean isValid )
        throws Exception
    {
        selectValue( "//select", "Jabber" );
        clickButtonWithValue( "Submit" );
        assertAddEditJabberPage();
        setFieldValue( "host", host );
        setFieldValue( "login", login );
        setFieldValue( "password", password );
        setFieldValue( "address", address );
        clickButtonWithValue( "Save" );

        if ( !isValid )
        {
            assertTextPresent( "Host is required" );
            assertTextPresent( "Login is required" );
            assertTextPresent( "Password is required" );
            assertTextPresent( "Address is required" );
        }
        else if ( projectName != null )
        {
            assertProjectInformationPage();
        }
        else
        {
            assertGroupNotifierPage( projectGroupName );
        }
    }

    public void editJabberNotifier( String projectGroupName, String projectName, String oldHost, String oldLogin,
                                    String oldAddress, String newHost, String newLogin, String newPassword,
                                    String newAddress, boolean isValid )
        throws Exception
    {
        if ( projectName == null )
        {
            clickLinkWithXPath( "(//a[contains(@href,'editProjectGroupNotifier') and contains(@href,'jabber')])//img" );
        }
        else
        {
            clickLinkWithXPath( "(//a[contains(@href,'editProjectNotifier') and contains(@href,'jabber')])//img" );
        }
        assertAddEditJabberPage();
        assertFieldValue( oldHost, "host" );
        assertFieldValue( oldLogin, "login" );
        assertFieldValue( oldAddress, "address" );
        setFieldValue( "host", newHost );
        setFieldValue( "login", newLogin );
        setFieldValue( "password", newPassword );
        setFieldValue( "address", newAddress );
        clickButtonWithValue( "Save" );

        if ( !isValid )
        {
            assertTextPresent( "Host is required" );
            assertTextPresent( "Login is required" );
            assertTextPresent( "Password is required" );
            assertTextPresent( "Address is required" );
        }
        else if ( projectName != null )
        {
            assertProjectInformationPage();
        }
        else
        {
            assertGroupNotifierPage( projectGroupName );
        }
    }

    public void addMsnNotifier( String projectGroupName, String projectName, String login, String password,
                                String recipientAddress, boolean isValid )
        throws Exception
    {
        selectValue( "//select", "MSN" );
        clickButtonWithValue( "Submit" );
        assertAddEditMsnPage();
        setFieldValue( "login", login );
        setFieldValue( "password", password );
        setFieldValue( "address", recipientAddress );
        clickButtonWithValue( "Save" );

        if ( !isValid )
        {
            assertTextPresent( "Login is required" );
            assertTextPresent( "Password is required" );
            assertTextPresent( "Address is required" );
        }
        else if ( projectName != null )
        {
            assertProjectInformationPage();
        }
        else
        {
            assertGroupNotifierPage( projectGroupName );
        }
    }

    public void editMsnNotifier( String projectGroupName, String projectName, String oldLogin, String oldAddress,
                                 String newLogin, String newPassword, String newAddress, boolean isValid )
        throws Exception
    {
        if ( projectName == null )
        {
            clickLinkWithXPath( "(//a[contains(@href,'editProjectGroupNotifier') and contains(@href,'msn')])//img" );
        }
        else
        {
            clickLinkWithXPath( "(//a[contains(@href,'editProjectNotifier') and contains(@href,'msn')])//img" );
        }
        assertAddEditMsnPage();
        assertFieldValue( oldLogin, "login" );
        assertFieldValue( oldAddress, "address" );
        setFieldValue( "login", newLogin );
        setFieldValue( "password", newPassword );
        setFieldValue( "address", newAddress );
        clickButtonWithValue( "Save" );

        if ( !isValid )
        {
            assertTextPresent( "Login is required" );
            assertTextPresent( "Password is required" );
            assertTextPresent( "Address is required" );
        }
        else if ( projectName != null )
        {
            assertProjectInformationPage();
        }
        else
        {
            assertGroupNotifierPage( projectGroupName );
        }
    }

    public void addWagonNotifierPage( String projectGroupName, String projectName, String siteUrl, String serverId,
                                      boolean isValid )
        throws Exception
    {
        selectValue( "//select", "Wagon" );
        clickButtonWithValue( "Submit" );
        assertAddEditWagonPage();
        setFieldValue( "url", siteUrl );
        setFieldValue( "id", serverId );
        clickButtonWithValue( "Save" );

        if ( !isValid )
        {
            assertTextPresent( "Destination URL is required" );
            assertTextPresent( "Server Id is required" );
        }
        else if ( projectName != null )
        {
            assertProjectInformationPage();
        }
        else
        {
            assertGroupNotifierPage( projectGroupName );
        }
    }

    public void editWagonNotifier( String projectGroupName, String projectName, String oldUrl, String oldId,
                                   String newUrl, String newId, boolean isValid )
        throws Exception
    {
        if ( projectName == null )
        {
            clickLinkWithXPath( "(//a[contains(@href,'editProjectGroupNotifier') and contains(@href,'wagon')])//img" );
        }
        else
        {
            clickLinkWithXPath( "(//a[contains(@href,'editProjectNotifier') and contains(@href,'wagon')])//img" );
        }
        assertAddEditWagonPage();
        assertFieldValue( oldUrl, "url" );
        // TODO: BUG: ServerId is not loader
        // assertFieldValue( oldId, "id" );
        setFieldValue( "url", newUrl );
        setFieldValue( "id", newId );
        clickButtonWithValue( "Save" );

        if ( !isValid )
        {
            assertTextPresent( "Destination URL is required" );
            assertTextPresent( "Server Id is required" );
        }
        else if ( projectName != null )
        {
            assertProjectInformationPage();
        }
        else
        {
            assertGroupNotifierPage( projectGroupName );
        }
    }

    // ////////////////////////////////////
    // General Project Pages
    // ////////////////////////////////////
    public void goToEditProjectPage( String projectGroupName, String projectName )
    {
        clickLinkWithText( "Show Project Groups" );
        clickLinkWithText( projectGroupName );
        clickLinkWithText( projectName );
        clickButtonWithValue( "Edit" );

        assertEditProjectPage();
    }

    public void assertEditProjectPage()
    {
        assertTextPresent( "Update Continuum Project" );
        assertTextPresent( "Project Name*:" );
        assertElementPresent( "name" );
        assertTextPresent( "Version*:" );
        assertElementPresent( "version" );
        assertTextPresent( "SCM Url*:" );
        assertElementPresent( "scmUrl" );
        assertTextPresent( "Use SCM Credentials Cache, if available" );
        assertElementPresent( "scmUseCache" );
        assertTextPresent( "SCM Username:" );
        assertElementPresent( "scmUsername" );
        assertTextPresent( "SCM Password:" );
        assertElementPresent( "scmPassword" );
        assertTextPresent( "SCM Branch/Tag:" );
        assertElementPresent( "scmTag" );
    }

    public void goToAddNotifierPage( String projectGroupName, String projectName )
    {
        clickLinkWithText( "Show Project Groups" );
        clickLinkWithText( projectGroupName );
        clickLinkWithText( projectName );
        getSelenium().click( "addProjectNotifier" );
        clickLinkWithXPath( "//input[@id='addProjectNotifier_0']" );

        assertNotifierPage();
    }

    public void assertNotifierPage()
    {
        assertPage( "Continuum - Add Notifier" );
        assertTextPresent( "Add Notifier" );
        assertTextPresent( "Type:" );
        assertElementPresent( "notifierType" );
    }

    public void addMailNotifier( String projectGroupName, String projectName, String email, boolean success,
                                 boolean failure, boolean error, boolean warning )
    {
        goToAddNotifierPage( projectGroupName, projectName );
        clickButtonWithValue( "Submit" );

        // Enter values into Add Notifier fields, and submit
        setFieldValue( "address", email );
        if ( success )
        {
            checkField( "sendOnSuccess" );
        }
        if ( failure )
        {
            checkField( "sendOnFailure" );
        }
        if ( error )
        {
            checkField( "sendOnError" );
        }
        if ( warning )
        {
            checkField( "sendOnWarning" );
        }

        submit();
        assertProjectInformationPage();
    }

    public void goToProjectInformationPage( String projectGroupName, String projectName )
    {
        clickLinkWithText( "Show Project Groups" );
        clickLinkWithText( projectGroupName );
        clickLinkWithText( projectName );

        assertProjectInformationPage();
    }

    public void assertProjectInformationPage()
    {
        assertTextPresent( "Project Group Summary" );
        assertTextPresent( "Project Information" );
        assertTextPresent( "Builds" );
        assertTextPresent( "Working Copy" );
        assertTextPresent( "Build Definitions" );
        assertTextPresent( "Notifiers" );
        assertTextPresent( "Dependencies" );
        assertTextPresent( "Developers" );
    }

    public void moveProjectToProjectGroup( String name, String groupId, String description, String newProjectGroup )
        throws Exception
    {
        showProjectGroup( name, groupId, description );
        clickButtonWithValue( "Edit" );

        assertTextPresent( "Move to Group" );
        selectValue( "//select[contains(@name,'project')]", newProjectGroup );
        clickButtonWithValue( "Save" );
    }

    // ////////////////////////////////////
    // Maven 2.0.x Project
    // ////////////////////////////////////
    public void goToAddMavenTwoProjectPage()
    {
        clickLinkWithText( "Maven 2.0.x Project" );

        assertAddMavenTwoProjectPage();
    }

    public void assertAddMavenTwoProjectPage()
    {
        assertTextPresent( "POM Url" );
        assertElementPresent( "m2PomUrl" );
        assertTextPresent( "Username" );
        assertElementPresent( "scmUsername" );
        assertTextPresent( "Password" );
        assertElementPresent( "scmPassword" );
        assertTextPresent( "Upload POM" );
        assertElementPresent( "m2PomFile" );
        assertTextPresent( "Project Group" );
        assertElementPresent( "selectedProjectGroup" );
    }

    public void addMavenTwoProject( String pomUrl, String username, String password, String projectGroup,
                                    boolean success )
        throws Exception
    {
        goToAddMavenTwoProjectPage();

        // Enter values into Add Maven Two Project fields, and submit
        setFieldValue( "m2PomUrl", pomUrl );
        setFieldValue( "scmUsername", username );
        setFieldValue( "scmPassword", password );

        if ( projectGroup != null )
        {
            selectValue( "addMavenTwoProject_selectedProjectGroup", projectGroup );
        }
        submit();
        String ident;
        if ( success )
        {
            ident = "projectGroupSummary";
        }
        else
        {
            ident = "addMavenTwoProject";
        }
        // TODO: Improve the condition
        String condition = "selenium.browserbot.getCurrentWindow().document.getElementById('" + ident + "')";
        // 'Continuum - Project Group'
        getSelenium().waitForCondition( condition, maxWaitTimeInMs );
    }

    /**
     * submit the page
     *
     * @param m2PomUrl
     * @param validPom
     */
    public void submitAddMavenTwoProjectPage( String m2PomUrl, boolean validPom )
        throws Exception
    {
        addMavenTwoProject( m2PomUrl, "", "", null, validPom );

        if ( validPom )
        {
            assertTextPresent( "Default Project Group" );
            // TODO: Add more tests
        }
    }

    // ////////////////////////////////////
    // Maven 1.x Project
    // ////////////////////////////////////
    public void goToAddMavenOneProjectPage()
    {
        clickLinkWithText( "Maven 1.x Project" );

        assertAddMavenOneProjectPage();
    }

    public void assertAddMavenOneProjectPage()
    {
        assertTextPresent( "POM Url" );
        assertElementPresent( "m1PomUrl" );
        assertTextPresent( "Username" );
        assertElementPresent( "scmUsername" );
        assertTextPresent( "Password" );
        assertElementPresent( "scmPassword" );
        assertTextPresent( "Upload POM" );
        assertElementPresent( "m1PomFile" );
        assertTextPresent( "Project Group" );
        assertElementPresent( "selectedProjectGroup" );
    }

    public void addMavenOneProject( String pomUrl, String username, String password, String projectGroup,
                                    boolean validProject )
    {
        goToAddMavenOneProjectPage();

        // Enter values into Add Maven One Project fields, and submit
        setFieldValue( "m1PomUrl", pomUrl );
        setFieldValue( "scmUsername", username );
        setFieldValue( "scmPassword", password );

        if ( projectGroup != null )
        {
            selectValue( "addMavenOneProject_selectedProjectGroup", projectGroup );
        }

        submit();

        getSelenium().waitForCondition( "'' == document.title", maxWaitTimeInMs );
    }
}
