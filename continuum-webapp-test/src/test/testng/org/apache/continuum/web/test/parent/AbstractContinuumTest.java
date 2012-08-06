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
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.continuum.web.test.ConfigurationTest;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import static org.testng.Assert.assertEquals;

/**
 * Based on AbstractContinuumTestCase of Emmanuel Venisse test.
 *
 * @author José Morales Martínez
 * @version $Id$
 */
public abstract class AbstractContinuumTest
    extends AbstractSeleniumTest
{

    static final String SHARED_SECRET = "continuum1234";

    // ////////////////////////////////////
    // Create Admin User
    // ////////////////////////////////////
    void assertCreateAdmin()
    {
        assertPage( "Create Admin User" );
        assertTextPresent( "Username" );
        assertFieldValue( "admin", "user.username" );
        assertTextPresent( "Full Name*" );
        assertElementPresent( "user.fullName" );
        assertTextPresent( "Email Address*" );
        assertElementPresent( "user.email" );
        assertTextPresent( "Password*" );
        assertElementPresent( "user.password" );
        assertTextPresent( "Confirm Password*" );
        assertElementPresent( "user.confirmPassword" );
        assertButtonWithValuePresent( "Create Admin" );
    }

    void submitAdminData( String fullname, String email, String password )
    {
        setFieldValue( "user.fullName", fullname );
        setFieldValue( "user.email", email );
        setFieldValue( "user.password", password );
        setFieldValue( "user.confirmPassword", password );
        submit();
    }

    // ////////////////////////////////////
    // About
    // ////////////////////////////////////
    protected void goToAboutPage()
    {
        getSelenium().open( baseUrl );
        clickLinkWithText( "About" );
        assertAboutPage();
    }

    protected void assertAboutPage()
    {
        assertPage( "Continuum - About" );
        assertTextPresent( "About Continuum" );
        assertTextPresent( "Version:" );
    }

    // ////////////////////////////////////
    // Login
    // ////////////////////////////////////

    protected void goToLoginPage()
    {
        getSelenium().deleteAllVisibleCookies();
        getSelenium().open( baseUrl );
        clickLinkWithText( "Login" );
        assertLoginPage();
    }

    void assertLoginPage()
    {
        assertPage( "Login Page" );
        assertTextPresent( "Login" );
        assertTextPresent( "Register" );
        assertTextPresent( "Username" );
        assertElementPresent( "username" );
        assertTextPresent( "Password" );
        assertElementPresent( "password" );
        assertTextPresent( "Remember Me" );
        assertElementPresent( "rememberMe" );
        assertButtonWithValuePresent( "Login" );
        assertButtonWithValuePresent( "Cancel" );
        assertTextPresent( "Need an Account? Register!" );
        assertTextPresent( "Forgot your Password? Request a password reset." );
    }

    void assertAutenticatedPage( String username )
    {
        assertTextPresent( "Current User" );
        assertTextPresent( "Edit Details" );
        assertTextPresent( "Logout" );
        assertTextNotPresent( "Login" );
        assertTextPresent( username );
    }

    // ////////////////////////////////////
    // Configuration
    // ////////////////////////////////////

    void assertEditConfigurationPage()
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

    // ////////////////////////////////////
    // Build Queue
    // ////////////////////////////////////

    protected void setMaxBuildQueue( int maxBuildQueue )
    {
        clickLinkWithText( "Configuration" );
        setFieldValue( "numberOfAllowedBuildsinParallel", String.valueOf( maxBuildQueue ) );
        submit();
    }

    // ////////////////////////////////////
    // Project Groups
    // ////////////////////////////////////
    protected void goToProjectGroupsSummaryPage()
    {
        getSelenium().open( "/continuum/groupSummary.action" );
        waitPage();

        assertProjectGroupsSummaryPage();
    }

    protected void assertProjectGroupsSummaryPage()
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
    protected void showProjectGroup( String name, String groupId, String description )
    {
        goToProjectGroupsSummaryPage();

        // Checks the link to the created Project Group
        assertLinkPresent( name );
        clickLinkWithText( name );

        assertProjectGroupSummaryPage( name, groupId, description );
    }

    protected void assertProjectGroupSummaryPage( String name, String groupId, String description )
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
        // assertElementPresent( "remove" );

        assertTextPresent( "Project Group Scm Root" );
        assertTextPresent( "Scm Root URL" );

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

    protected void addProjectGroup( String name, String groupId, String description, boolean success )
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

    void assertAddProjectGroupPage()
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

    protected void removeProjectGroup( String groupName )
    {
        removeProjectGroup( groupName, true );
    }

    protected void removeProjectGroup( String groupName, boolean failIfMissing )
    {
        goToProjectGroupsSummaryPage();
        if ( failIfMissing || isLinkPresent( groupName ) )
        {
            clickLinkWithText( groupName );
            clickButtonWithValue( "Delete Group" );
            assertTextPresent( "Project Group Removal" );
            clickButtonWithValue( "Delete" );
            assertProjectGroupsSummaryPage();
        }
    }

    protected void editProjectGroup( String name, String groupId, String description, String newName,
                                     String newDescription )
    {
        showProjectGroup( name, groupId, description );
        clickButtonWithValue( "Edit" );
        assertEditGroupPage( groupId );
        setFieldValue( "name", newName );
        setFieldValue( "description", newDescription );
        clickButtonWithValue( "Save" );
    }

    void assertEditGroupPage( String groupId )
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

    protected void buildProjectGroup( String projectGroupName, String groupId, String description, String projectName,
                                      boolean success )
    {
        showProjectGroup( projectGroupName, groupId, description );
        waitForProjectUpdate();
        clickButtonWithValue( "Build all projects" );

        // wait for project to finish building
        waitForProjectBuild();
        
        // wait for the success status of project
        if ( success )
        {
            if ( !isElementPresent( "//a/img[@alt='Success']" ) )
            {
                waitForElementPresent( "//a/img[@alt='Success']" );
            }
        }
        else
        {
            if ( !isElementPresent( "//a/img[@alt='Failed']" ) )
            {
                waitForElementPresent( "//a/img[@alt='Failed']" );
            }
        }
        
        // wait for the projectName link
        if ( !isLinkPresent( projectName ) )
        {
            waitForElementPresent( "link=" + projectName );
        }

        clickLinkWithText( projectName );
        waitForElementPresent( "link=Builds" );
        clickLinkWithText( "Builds" );
        clickLinkWithText( "Result" );

        assertPage( "Continuum - Build result" );
        assertTextPresent( "Build result for " + projectName );

        if ( success )
        {
            assertImgWithAlt( "Success" );
        }
        else
        {
            assertImgWithAlt( "Failed" );
        }

        clickLinkWithText( "Project Group Summary" );
    }

    protected void assertReleaseSuccess()
    {
        assertTextPresent( "Choose Release Goal for " );
        assertTextPresent( "Prepare project for release " );
        assertTextPresent( "Perform project release" );
        assertElementPresent( "goal" );
        assertElementPresent( "preparedReleaseId" );
        assertButtonWithValuePresent( "Submit" );
    }

    protected void goToGroupBuildDefinitionPage( String projectGroupName, String groupId, String description )
    {
        showProjectGroup( projectGroupName, groupId, description );
        clickLinkWithText( "Build Definitions" );
        assertGroupBuildDefinitionPage( projectGroupName );
    }

    protected void assertGroupBuildDefinitionPage( String projectGroupName )
    {
        assertTextPresent( "Project Group Build Definitions of " + projectGroupName + " group" );
    }

    protected void assertDeleteBuildDefinitionPage( String description, String goals )
    {
        assertTextPresent( "Are you sure you want to delete the build definition with description \"" + description
            + "\", goals \"" + goals + "\" and id" );
        isButtonWithValuePresent( "Cancel" );
        isButtonWithValuePresent( "Delete" );
    }

    protected void assertAddEditBuildDefinitionPage()
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
        assertEnabled();
    }

    protected void addEditGroupBuildDefinition( String groupName, String buildFile, String goals, String arguments,
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
            if ( isChecked( "buildFresh" ) )
            {
                uncheckField( "buildFresh" );
            }

            // need to do this for the onclick event
            click( "buildFresh" );
        }
        else
        {
            if ( !isChecked( "buildFresh" ) )
            {
                checkField( "buildFresh" );
            }

            // need to do this for the onclick event
            click( "buildFresh" );
        }

        assertEnabled();
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

        selectValue( "scheduleId", "DEFAULT_SCHEDULE" );

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

    protected void goToProjectInformationPage( String projectGroupName, String projectName )
    {
        clickLinkWithText( "Show Project Groups" );
        clickLinkWithText( projectGroupName );
        clickLinkWithText( projectName );

        assertProjectInformationPage();
    }

    protected void assertProjectInformationPage()
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

    protected void moveProjectToProjectGroup( String groupName, String groupId, String groupDescription,
                                              String projectName, String newProjectGroup )
    {
        showProjectGroup( groupName, groupId, groupDescription );
        
        // wait for project not being used
        waitForProjectBuild();
        
        String id = getFieldValue( "name=projectGroupId" );
        String url = baseUrl + "/editProjectGroup.action?projectGroupId=" + id;
        getSelenium().open( url );
        waitPage();

        assertTextPresent( "Move to Group" );
        String xPath = "//preceding::th/label[contains(text(),'" + projectName + "')]//following::select";
        selectValue( xPath, newProjectGroup );
        clickButtonWithValue( "Save" );
        assertProjectGroupSummaryPage( groupName, groupId, groupDescription );
    }

    // ////////////////////////////////////
    // Maven 2.0+ Project
    // ////////////////////////////////////
    protected void goToAddMavenTwoProjectPage()
    {
        clickLinkWithText( "Maven Project" );

        assertAddMavenTwoProjectPage();
    }

    protected void assertAddMavenTwoProjectPage()
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

    protected void addMavenTwoProject( String pomUrl, String username, String password, String projectGroup,
                                       boolean success )
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
        String title;
        if ( success )
        {
            title = "Continuum - Project Group";
        }
        else
        {
            title = "Continuum - Add Maven Project";
        }
        waitAddProject( title );
    }

    protected void submitAddMavenTwoProjectPage( String m2PomUrl )
    {
        addMavenTwoProject( m2PomUrl, "", "", null, false );
    }

    // ////////////////////////////////////
    // Maven 1.x Project
    // ////////////////////////////////////
    protected void goToAddMavenOneProjectPage()
    {
        clickLinkWithText( "Maven 1.x Project" );
        assertAddMavenOneProjectPage();
    }

    void assertAddMavenOneProjectPage()
    {
        assertPage( "Continuum - Add Maven 1 Project" );
        assertTextPresent( "Add Maven 1.x Project" );
        assertTextPresent( "M1 POM Url" );
        assertElementPresent( "m1PomUrl" );
        assertTextPresent( "Username" );
        assertElementPresent( "scmUsername" );
        assertTextPresent( "Password" );
        assertElementPresent( "scmPassword" );
        assertTextPresent( "OR" );
        assertTextPresent( "Upload POM" );
        assertElementPresent( "m1PomFile" );
        assertTextPresent( "Project Group" );
        assertElementPresent( "selectedProjectGroup" );
        assertOptionPresent( "selectedProjectGroup", new String[] { "Defined by POM", "Default Project Group" } );
        assertTextPresent( "Build Definition Template" );
        assertElementPresent( "buildDefinitionTemplateId" );
        assertOptionPresent( "buildDefinitionTemplateId", new String[] { "Default", "Default Ant Template",
            "Default Maven 1 Template", "Default Maven Template", "Default Shell Template" } );
        assertButtonWithValuePresent( "Add" );
        assertButtonWithValuePresent( "Cancel" );
    }

    protected void addMavenOneProject( String pomUrl, String username, String password, String projectGroup,
                                       boolean success )
    {
        setFieldValue( "m1PomUrl", pomUrl );
        setFieldValue( "scmUsername", username );
        setFieldValue( "scmPassword", password );

        if ( projectGroup != null )
        {
            selectValue( "selectedProjectGroup", projectGroup );
        }
        submit();
        String title;
        if ( success )
        {
            title = "Continuum - Project Group";
        }
        else
        {
            title = "Continuum - Add Maven 1 Project";
        }
        waitAddProject( title );
    }

    // ////////////////////////////////////
    // ANT/SHELL Projects
    // ////////////////////////////////////

    protected void goToAddAntProjectPage()
    {
        clickLinkWithText( "Ant Project" );
        assertAddProjectPage( "ant" );
    }

    protected void goToAddShellProjectPage()
    {
        clickLinkWithText( "Shell Project" );
        assertAddProjectPage( "shell" );
    }

    protected void assertAddProjectPage( String type )
    {
        String title = type.substring( 0, 1 ).toUpperCase() + type.substring( 1 ).toLowerCase();
        assertPage( "Continuum - Add " + title + " Project" );
        assertTextPresent( "Add " + title + " Project" );
        assertTextPresent( "Project Name" );
        assertElementPresent( "projectName" );
        assertTextPresent( "Description" );
        assertElementPresent( "projectDescription" );
        assertTextPresent( "Version" );
        assertElementPresent( "projectVersion" );
        assertTextPresent( "Scm Url" );
        assertElementPresent( "projectScmUrl" );
        assertLinkPresent( "Maven SCM URL" );
        assertTextPresent( "Scm Username" );
        assertElementPresent( "projectScmUsername" );
        assertTextPresent( "Scm Password" );
        assertElementPresent( "projectScmPassword" );
        assertTextPresent( "Scm Branch/Tag" );
        assertElementPresent( "projectScmTag" );
        assertTextPresent( "Use SCM Credentials Cache, if available" );
        assertElementPresent( "projectScmUseCache" );
        assertTextPresent( "Project Group" );
        assertElementPresent( "selectedProjectGroup" );
        assertOptionPresent( "selectedProjectGroup", new String[] { "Default Project Group" } );
        assertTextPresent( "Build Definition Template" );
        assertElementPresent( "buildDefinitionTemplateId" );
        assertOptionPresent( "buildDefinitionTemplateId", new String[] { "Default", "Default Ant Template",
            "Default Maven 1 Template", "Default Maven Template", "Default Shell Template" } );
        assertButtonWithValuePresent( "Add" );
        assertButtonWithValuePresent( "Cancel" );
    }

    protected void addProject( String name, String description, String version, String scmUrl, String scmUser,
                               String scmPassword, String scmTag, String projectGroup, boolean success, String type )
    {
        setFieldValue( "projectName", name );
        setFieldValue( "projectDescription", description );
        setFieldValue( "projectVersion", version );
        setFieldValue( "projectScmUrl", scmUrl );
        setFieldValue( "projectScmUsername", scmUser );
        setFieldValue( "projectScmPassword", scmPassword );
        setFieldValue( "projectScmTag", scmTag );
        if ( projectGroup != null )
        {
            selectValue( "selectedProjectGroup", projectGroup );
        }
        submit();
        String title;
        type = type.substring( 0, 1 ).toUpperCase() + type.substring( 1 ).toLowerCase();
        if ( success )
        {
            title = "Continuum - Project Group";
        }
        else
        {
            title = "Continuum - Add " + type + " Project";
        }
        waitAddProject( title );
    }

    protected void waitAddProject( String title )
    {
        // the "adding project" interstitial page has an empty title, so we wait for a real title to appear

        if ( browser.equals( "*iexplore" ) )
        {
            int currentIt = 1;
            int maxIt = 20;

            // there's a problem with ie using waitForCondition
            while( getTitle().equals( "" ) && currentIt <= maxIt )
            {
                waitPage();
                currentIt++;
            }
        }
        else
        {
            String condition = "selenium.browserbot.getCurrentWindow().document.title.replace(/^\\s*/, \"\").replace(/\\s*$/, \"\") != '' && selenium.browserbot.getCurrentWindow().document.getElementById('footer') != null";
            waitForCondition( condition );
        }

        assertEquals( getTitle(), title );
    }

    protected void createAndAddUserAsDeveloperToGroup( String username, String name, String email, String groupName )
    {
        clickLinkWithText( "Users" );
        assertPage( "[Admin] User List" );
        assertTextNotPresent( username );
        clickButtonWithValue( "Create New User" );
        assertPage( "[Admin] User Create" );
        setFieldValue( "user.fullName", name );
        setFieldValue( "user.username", username );
        setFieldValue( "user.email", email );
        setFieldValue( "user.password", "password123" );
        setFieldValue( "user.confirmPassword", "password123" );
        clickButtonWithValue( "Create User" );
        assertPage( "[Admin] User Edit" );
        assignContinuumResourceRoleToUser( groupName );
        clickButtonWithValue( "Submit" );
        assertPage( "[Admin] User List" );
        assertTextPresent( username );
        assertTextPresent( name );
        assertTextPresent( email );
    }

    protected void showMembers( String name, String groupId, String description )
    {
        showProjectGroup( name, groupId, description );
        clickLinkWithText( "Members" );
        assertTextPresent( "Member Projects of " + name + " group" );
        assertTextPresent( "Users" );
    }

    protected void assertUserPresent( String username, String name, String email )
    {
        assertTextPresent( username );
        assertTextPresent( name );
        assertTextPresent( email );
    }

    protected void assertUserNotPresent( String username, String name, String email )
    {
        assertTextNotPresent( username );
        assertTextNotPresent( name );
        assertTextNotPresent( email );
    }

    protected void waitForProjectCheckout()
    {
        // wait for project to finish checking out
        waitForElementPresent( "//img[@alt='Checking Out']", false );
    }
    
    void waitForProjectUpdate()
    {
        if ( isElementPresent( "//img[@alt='Checking Out']" ) )
        {
            waitForProjectCheckout();
        }
        
        // wait for project to finish updating
        waitForElementPresent( "//img[@alt='Updating']", false );
    }
    
    void waitForProjectBuild()
    {
        if ( isElementPresent( "//img[@alt='Checking Out']" ) || isElementPresent( "//img[@alt='Updating']" ) )
        {
            waitForProjectUpdate();
        }
        
        // wait for project to finish building
        waitForElementPresent( "//img[@alt='Building']", false );
    }

    void assignContinuumResourceRoleToUser( String groupName )
    {
        clickLinkWithXPath( "//input[@name='addDSelectedRoles' and @value='" + "Project Developer" + " - " + groupName + "']", false );
    }

    protected void removeDefaultBuildDefinitionFromTemplate( String type )
    {
        goToEditBuildDefinitionTemplate( type );
        clickLinkWithXPath( "//input[@value='<-']", false );
        submit();
    }

    protected void addDefaultBuildDefinitionFromTemplate( String type )
    {
        goToEditBuildDefinitionTemplate( type );

        if ( "maven2".equals( type ) )
        {
            getSelenium().addSelection(
                "saveBuildDefinitionTemplate_buildDefinitionIds", "label=" + "Default Maven Build Definition" );
        }

        clickLinkWithXPath( "//input[@value='->']", false );
        submit();
    }

    void goToEditBuildDefinitionTemplate( String type )
    {
        clickLinkWithText( "Build Definition Templates" );

        assertBuildDefinitionTemplatesPage();

        if ( "maven2".equals( type ) )
        {
            clickLinkWithXPath( "//table[@id='ec_table']/tbody/tr[3]/td[2]/a/img", true );
        }
        else if ( "maven1".equals( type ) )
        {
            clickLinkWithXPath( "//table[@id='ec_table']/tbody/tr[2]/td[2]/a/img", true );
        }
        else if ( "ant".equals( type ) )
        {
            clickLinkWithXPath( "//img[@alt='Edit']", true );
        }
        else
        {
            clickLinkWithXPath( "//table[@id='ec_table']/tbody/tr[4]/td[2]/a/img", true );
        }

        assertPage( "Continuum - Build Definition Template" );
    }

    void assertBuildDefinitionTemplatesPage()
    {
        assertPage( "Continuum - Build Definition Templates" );
        assertTextPresent( "Default Ant Template" );
        assertTextPresent( "Default Maven 1 Template" );
        assertTextPresent( "Default Maven Template" );
        assertTextPresent( "Default Shell Template" );
        assertTextPresent( "Available Build Definitions" );
        assertTextPresent( "Default Ant Build Definition" );
        assertTextPresent( "Default Maven 1 Build Definition" );
        assertTextPresent( "Default Maven Build Definition" );
        assertTextPresent( "Default Shell Build Definition" );
    }
    
    // ////////////////////////////////////
    // Distributed Builds
    // ////////////////////////////////////

    protected void enableDistributedBuilds()
    {
        ConfigurationTest config = new ConfigurationTest();
        config.goToConfigurationPage();
        setFieldValue( "numberOfAllowedBuildsinParallel", "2" );
        if ( !isChecked( "configuration_distributedBuildEnabled" ) )
        {
            // must use click here so the JavaScript enabling the shared secret gets triggered
            click( "configuration_distributedBuildEnabled" );
        }
        setFieldValue( "configuration_sharedSecretPassword", SHARED_SECRET );
        clickAndWait( "configuration_" );
        assertTextPresent( "true" );
        assertTextPresent( "Distributed Builds" );
        assertElementPresent( "link=Build Agents" );
    }

    protected void disableDistributedBuilds()
    {
        ConfigurationTest config = new ConfigurationTest();
        config.goToConfigurationPage();
        setFieldValue( "numberOfAllowedBuildsinParallel", "2" );
        if ( isChecked( "configuration_distributedBuildEnabled" ) )
        {
            uncheckField( "configuration_distributedBuildEnabled" );
        }
        submit();
        assertTextPresent( "false" );
        assertElementNotPresent( "link=Build Agents" );
    }

    protected void goToBuildAgentPage()
    {
        clickAndWait("link=Build Agents");
        assertPage("Continuum - Build Agents");
    }

    void assertBuildAgentPage()
    {
        assertPage("Continuum - Build Agents");
        assertTextPresent("Build Agents");
        assertTextPresent("Build Agent Groups");
        assertButtonWithValuePresent( "Add" );
    }

    // ////////////////////////////////////
    // Reports
    // ////////////////////////////////////

    protected void goToProjectBuildsReport()
    {
        clickLinkWithText( "Project Builds" );
        assertViewBuildsReportPage();
    }

    void assertViewBuildsReportPage()
    {
        assertPage( "Continuum - Project Builds Report" );
        assertTextPresent( "Project Group" );
        assertElementPresent( "projectGroupId" );
        assertTextPresent( "Start Date" );
        assertElementPresent( "startDate" );
        assertTextPresent( "End Date" );
        assertElementPresent( "endDate" );
        assertTextPresent( "Triggered By" );
        assertElementPresent( "triggeredBy" );
        assertTextPresent( "Build Status" );
        assertElementPresent( "buildStatus" );
        assertTextPresent( "Row Count" );
        assertElementPresent( "rowCount" );
        assertButtonWithValuePresent( "View Report" );
        assertTextNotPresent( "Results" );
        assertTextNotPresent( "No Results Found" );
        assertTextNotPresent( "Export to CSV" );
    }
    
    protected void assertProjectBuildReportWithResult()
    {
        assertTextPresent( "Results" );
        assertTextPresent( "Project Group" );
        assertTextPresent( "Project Name" );
        assertTextPresent( "Build Date" );
        assertTextPresent( "Triggered By" );
        assertTextPresent( "Build Status" );
        assertTextPresent( "Prev" );
        assertTextPresent( "Next" );
        assertTextPresent( "Export to CSV" );
    }

    protected void assertProjectBuildReportWithNoResult()
    {
        assertTextNotPresent( "Build Date" );
        assertTextNotPresent( "Prev" );
        assertTextNotPresent( "Next" );
        assertTextNotPresent( "Export to CSV" );
        assertTextPresent( "Results" );
        assertTextPresent( "No Results Found" );
    }

    protected void assertProjectBuildReportWithFieldError()
    {
        assertTextNotPresent( "Build Date" );
        assertTextNotPresent( "Prev" );
        assertTextNotPresent( "Next" );
        assertTextNotPresent( "Export to CSV" );
        assertTextNotPresent( "Results" );
        assertTextNotPresent( "No Results Found" );
    }

    @BeforeSuite( alwaysRun = true )
    @Parameters( { "baseUrl", "browser", "seleniumHost", "seleniumPort" } )
    public void initializeContinuum( @Optional( "http://localhost:9595/continuum" ) String baseUrl,
                                     @Optional( "*firefox" ) String browser,
                                     @Optional( "localhost" ) String seleniumHost,
                                     @Optional( "4444" ) int seleniumPort )
        throws Exception
    {
        super.open( baseUrl, browser, seleniumHost, seleniumPort );
        Assert.assertNotNull( getSelenium(), "Selenium is not initialized" );
        getSelenium().open( baseUrl );
        String title = getSelenium().getTitle();
        if ( title.equals( "Create Admin User" ) )
        {
            assertCreateAdmin();
            String fullname = getProperty( "ADMIN_FULLNAME" );
            String username = getProperty( "ADMIN_USERNAME" );
            String mail = getProperty( "ADMIN_MAIL" );
            String password = getProperty( "ADMIN_PASSWORD" );
            submitAdminData( fullname, mail, password );
            assertAutenticatedPage( username );
            assertEditConfigurationPage();
            postAdminUserCreation();
            disableDefaultSchedule();
            clickLinkWithText( "Logout" );
        }
    }

    private void postAdminUserCreation()
    {
        if ( getTitle().endsWith( "Continuum - Configuration" ) )
        {
            String workingDir = getFieldValue( "configuration_workingDirectory" );
            String buildOutputDir = getFieldValue( "configuration_buildOutputDirectory" );
            String releaseOutputDir = getFieldValue( "configuration_releaseOutputDirectory" );
            String locationDir = "target/data";
            String data = "data";
            setFieldValue( "workingDirectory", workingDir.replaceFirst( data, locationDir ) );
            setFieldValue( "buildOutputDirectory", buildOutputDir.replaceFirst( data, locationDir ) );
            setFieldValue( "releaseOutputDirectory", releaseOutputDir.replaceFirst( data, locationDir ) );
            setFieldValue( "baseUrl", baseUrl );
            submit();
        }
    }

    private void disableDefaultSchedule()
    {
        clickLinkWithText( "Schedules" );
        String xPath = "//preceding::td[text()='DEFAULT_SCHEDULE']//following::img[@alt='Edit']";
        clickLinkWithXPath( xPath );
        if ( isChecked( "saveSchedule_active" ) )
        {
            uncheckField( "saveSchedule_active" );
        }
        clickButtonWithValue( "Save" );
    }

    protected void login( String username, String password )
    {
        goToLoginPage();
        getSelenium().type( "loginForm_username", username );
        getSelenium().type( "loginForm_password", password );
        getSelenium().click( "loginForm__login" );
        getSelenium().waitForPageToLoad( maxWaitTimeInMs );
    }
}
