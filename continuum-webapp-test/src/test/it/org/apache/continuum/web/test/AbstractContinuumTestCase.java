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

import org.apache.maven.shared.web.test.AbstractSeleniumTestCase;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public abstract class AbstractContinuumTestCase
    extends AbstractSeleniumTestCase
{
    protected String adminUsername = "admin";

    protected String adminPassword = "admin1";

    protected String adminFullName = "Continuum Admin";

    protected String adminEmail = "admin@localhost.localdomain.com";

    private String baseUrl = "http://localhost:9595";

    public final static String DEFAULT_PROJ_GRP_NAME = "Default Project Group";

    public final static String DEFAULT_PROJ_GRP_ID = "default";

    public final static String DEFAULT_PROJ_GRP_DESCRIPTION =
        "Contains all projects that do not have a group of their own";

    public final static String TEST_PROJ_GRP_NAME = "Test Project Group Name";

    public final static String TEST_PROJ_GRP_ID = "Test Project Group Id";

    public final static String TEST_PROJ_GRP_DESCRIPTION = "Test Project Group Description";

    public final static String TEST_POM_URL = "http://svn.apache.org/repos/asf/maven/pom/trunk/maven/pom.xml";

    public final static String TEST_POM_USERNAME = "dummy";

    public final static String TEST_POM_PASSWORD = "dummy";

    private final static int ONE_SECOND = 1000;

    private final static int ONE_MINUTE = 60 * ONE_SECOND;

    private final static int LONG_WAIT = 5 * ONE_MINUTE;

    /**
     * We create an admin user if it doesn't exist
     */
    protected void initialize()
    {
        getSelenium().setTimeout( String.valueOf( 5 * ONE_MINUTE ) );
        open( "/continuum" );

        if ( "Create Admin User".equals( getTitle() ) )
        {
            assertCreateAdminUserPage();
            submitCreateAdminUserPage( adminFullName, adminEmail, adminPassword, adminPassword );
            assertLoginPage();
            submitLoginPage( adminUsername, adminPassword );
            assertEditConfigurationPage();
            submitConfigurationPage( baseUrl, null, null, null );

            // logout with a longer delay
            assertTrue( "User wasn't authenticated.", isAuthenticated() );
            clickLinkWithText( "Logout", false );
            waitPage( LONG_WAIT );
            assertFalse( "The user is always authenticated after a logout.", isAuthenticated() );
        }
    }

    //////////////////////////////////////
    // Overriden AbstractSeleniumTestCase methods
    //////////////////////////////////////
    protected String getApplicationName()
    {
        return "Continuum";
    }

    protected String getInceptionYear()
    {
        return "2005";
    }

    public void assertHeader()
    {
        //TODO
    }

    public String getBaseUrl()
    {
        return "http://localhost:9595/continuum";
    }

    //////////////////////////////////////
    // Create Admin User
    //////////////////////////////////////
    public void assertCreateAdminUserPage()
    {
        assertPage( "Create Admin User" );
        assertTextPresent( "Create Admin User" );
        assertTextPresent( "Username" );
        assertElementPresent( "user.username" );
        assertTextPresent( "Full Name" );
        assertElementPresent( "user.fullName" );
        assertTextPresent( "Email Address" );
        assertElementPresent( "user.email" );
        assertTextPresent( "Password" );
        assertElementPresent( "user.password" );
        assertTextPresent( "Confirm Password" );
        assertElementPresent( "user.confirmPassword" );
    }

    public void submitCreateAdminUserPage( String fullName, String email, String password, String confirmPassword )
    {
        setFieldValue( "user.fullName", fullName );
        setFieldValue( "user.email", email );
        setFieldValue( "user.password", password );
        setFieldValue( "user.confirmPassword", confirmPassword );
        submit();
        waitPage();
    }

    //////////////////////////////////////
    // Configuration
    //////////////////////////////////////
    public void assertEditConfigurationPage()
    {
        assertPage( "Continuum - Configuration" );
        assertTextPresent( "Working Directory" );
        assertElementPresent( "workingDirectory" );
        assertTextPresent( "Build Output Directory" );
        assertElementPresent( "buildOutputDirectory" );
        assertTextPresent( "Deployment Repository Directory" );
        assertElementPresent( "deploymentRepositoryDirectory" );
        assertTextPresent( "Base URL" );
        assertElementPresent( "baseUrl" );
        assertTextPresent( "Company Name" );
        assertElementPresent( "companyName" );
        assertTextPresent( "Company Logo" );
        assertElementPresent( "companyLogo" );
        assertTextPresent( "Company URL" );
        assertElementPresent( "companyUrl" );
    }

    public void submitConfigurationPage( String baseUrl, String companyName, String companyLogo, String companyUrl )
    {
        setFieldValue( "baseUrl", baseUrl );
        if ( companyName != null )
        {
            setFieldValue( "companyName", companyName );
        }
        if ( companyLogo != null )
        {
            setFieldValue( "companyLogo", companyLogo );
        }
        if ( companyUrl != null )
        {
            setFieldValue( "companyUrl", companyUrl );
        }
        submit();
        waitPage();
    }

    //////////////////////////////////////
    // ANT/SHELL Projects
    //////////////////////////////////////
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

    public void assertAddAdminUserListPage()
        throws Exception
    {
        assertPage( "[Admin] User List" );
    }

    //////////////////////////////////////
    // Project Groups
    //////////////////////////////////////
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

        if ( isTextPresent( "No Project Groups Known." ) )
        {
            assertTextNotPresent( "Name" );
            assertTextNotPresent( "Group Id" );
            assertTextNotPresent( "Projects" );
            assertTextNotPresent( "Build Status" );
        }
        else
        {
            assertTextPresent( "Name" );
            assertTextPresent( "Group Id" );
            assertTextPresent( "Projects" );
            assertTextPresent( "Build Status" );
        }

        assertLinkPresent( "Add a Project Group" );
    }

    //////////////////////////////////////
    // Project Group
    //////////////////////////////////////
    public void showProjectGroup( String name, String groupId, String description )
        throws Exception
    {
        goToProjectGroupsSummaryPage();

        // Checks the link to the created Project Group
        assertLinkPresent( name );
        clickLinkWithText( name );

        assertProjectGroupSummaryPage( name, groupId, description);
    }

    public void assertProjectGroupSummaryPage( String name, String groupId, String description )
    {
        assertTextPresent( "Project Group Name" );
        assertTextPresent( name );
        assertTextPresent( "Group Id" );
        assertTextPresent( groupId );
        assertTextPresent( "Description" );
        assertTextPresent( description );

        // Assert the available Project Group Actions
        assertTextPresent( "Project Group Actions" );
        assertElementPresent( "build" );
        assertElementPresent( "edit" );
        assertElementPresent( "remove" );

        if ( isTextPresent( "Projects" ) )
        {
            assertTextPresent( "Project Name" );
            assertTextPresent( "Version" );
            assertTextPresent( "Build" );
        }

        assertLinkPresent( "Add Maven 2.0+ Project" );

        //TODO: Add these links in the page
        //assertLinkPresent( "Add Maven 1.x Project");
        //assertLinkPresent( "Add Ant Project");
        //assertLinkPresent( "Add Shell Project");
    }

    public void addProjectGroup( String name, String groupId, String description )
        throws Exception
    {
        goToProjectGroupsSummaryPage();

        // Go to Add Project Group Page
        assertLinkPresent( "Add a Project Group" );
        clickLinkWithText( "Add a Project Group" );
        assertAddProjectGroupPage();

        // Enter values into Add Project Group fields, and submit  
        setFieldValue( "name", name );
        setFieldValue( "groupId", groupId );
        setFieldValue( "description", description );

        submit();
        waitPage();

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
        clickSubmitWithLocator( "remove" );

        // Assert Confirmation
        assertElementPresent( "removeProjectGroup_0" );
        assertElementPresent( "Cancel" );

        // Confirm Project Group deletion
        clickSubmitWithLocator( "removeProjectGroup_0" );

    }

    //////////////////////////////////////
    // Maven 2.0.x Project
    //////////////////////////////////////
    public void goToAddMavenProjectPage()
        throws Exception
    {
        clickLinkWithText( "Maven 2.0.x Project" );

        assertAddMavenTwoProjectPage();
    }

    public void assertAddMavenTwoProjectPage()
        throws Exception
    {
        assertTextPresent( "POM Url" );
        assertElementPresent( "m2PomUrl" );
        assertTextPresent( "Username" );
        assertElementPresent( "username" );
        assertTextPresent( "Password" );
        assertElementPresent( "password" );
        assertTextPresent( "Upload POM" );
        assertElementPresent( "m2PomFile" );
        assertTextPresent( "Project Group" );
        assertElementPresent( "selectedProjectGroup" );
    }

    public void addMavenTwoProject( String pomUrl, String username, String password, String projectGroup )
        throws Exception
    {
        goToAddMavenProjectPage();

        // Enter values into Add Maven Two Project fields, and submit  
        setFieldValue( "m2PomUrl", pomUrl );
        setFieldValue( "username", username );
        setFieldValue( "password", password );

        if ( projectGroup != null )
        {
            selectValue( "addMavenTwoProject_selectedProjectGroup", projectGroup );
        }

        submit();
    }

    public void moveProjectToProjectGroup( String name, String groupId, String description, String newProjectGroup )
        throws Exception
    {
        showProjectGroup( name, groupId, description );

        assertElementPresent( "edit" );
        clickButtonWithValue( "Edit" );

        assertTextPresent( "Move to Group" );
        selectValue( "//select", newProjectGroup );

        assertElementPresent( "saveProjectGroup_0" );
        clickButtonWithValue( "Save" );
    }

    //////////////////////////////////////
    // My Account
    //////////////////////////////////////
    public void goToMyAccount()
    {
        clickLinkWithText( "Edit user info" );
    }

    public void assertMyAccountDetails( String username, String newFullName, String newEmailAddress )
        throws Exception
    {
        assertPage( "Account Details" );

        isTextPresent( "Username" );
        assertTextPresent( "Username" );
        assertElementPresent( "registerForm_user_username" );
        assertCellValueFromTable( username, "//form/table", 0, 1 );

        assertTextPresent( "Full Name" );
        assertElementPresent( "user.fullName" );
        assertEquals( newFullName, getFieldValue( "user.fullName" ) );

        assertTextPresent( "Email Address" );
        assertElementPresent( "user.email" );
        assertEquals( newEmailAddress, getFieldValue( "user.email" ) );

        assertTextPresent( "Password" );
        assertElementPresent( "user.password" );

        assertTextPresent( "Confirm Password" );
        assertElementPresent( "user.confirmPassword" );

        assertTextPresent( "Last Password Change" );
        assertElementPresent( "registerForm_user_timestampLastPasswordChange" );

    }

    public void editMyUserInfo( String newFullName, String newEmailAddress, String newPassword,
                                String confirmNewPassword )
    {
        goToMyAccount();

        setFieldValue( "user.fullName", newFullName );
        setFieldValue( "user.email", newEmailAddress );
        setFieldValue( "user.password", newPassword );
        setFieldValue( "user.confirmPassword", confirmNewPassword );
    }

    public void tearDown()
    {
        try
        {
            goToProjectGroupsSummaryPage();

            if ( isLinkPresent( TEST_PROJ_GRP_NAME ) )
            {
                removeProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
            }
            if ( isLinkPresent( DEFAULT_PROJ_GRP_NAME ) &&
                "0".equals( getCellValueFromTable( "ec_table", 1, 2 ) ) == false )
            {
                removeProjectGroup( DEFAULT_PROJ_GRP_NAME, DEFAULT_PROJ_GRP_ID, DEFAULT_PROJ_GRP_DESCRIPTION );
                addProjectGroup( DEFAULT_PROJ_GRP_NAME, DEFAULT_PROJ_GRP_ID, DEFAULT_PROJ_GRP_DESCRIPTION );
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }

        super.tearDown();
    }
}
