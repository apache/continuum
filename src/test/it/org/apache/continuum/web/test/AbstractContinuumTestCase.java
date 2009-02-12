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


/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public abstract class AbstractContinuumTestCase
    extends AbstractSeleniumTestCase
{
    private String baseUrl = "http://localhost:9595/continuum";

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

    public static final String TEST_PROJECT_GROUP_NAME = "Apache Maven";

    public static final String TEST_PROJECT_NAME = "Apache Maven";

    protected void postAdminUserCreation()
    {
        assertEditConfigurationPage();
        submitConfigurationPage( baseUrl, null, null, null );
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
        return baseUrl;
    }

    //////////////////////////////////////
    // About
    //////////////////////////////////////
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
        assertTextPresent( "1.1-SNAPSHOT" );
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

        if ( isTextPresent( "Project Groups list is empty." ) )
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
        else
        {
            assertTextNotPresent( "Project Name" );
        }
    }

    public void addProjectGroup( String name, String groupId, String description )
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
        waitPage();

        //TODO: Check the result Page
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

    public void assertEditGroupPage( String groupId ) throws Exception
    {
        assertPage( "Continuum - Update Project Group" );
        assertTextPresent( "Update Project Group" );
        assertTextPresent( "Project Group Name" );
        assertElementPresent( "saveProjectGroup_name" );
        assertTextPresent( "Project Group Id" );
        assertTextPresent( groupId );
        assertTextPresent( "Description" );
        assertElementPresent( "saveProjectGroup_description" );

        // Assert Projects actions
        assertTextPresent( "Projects" );
        assertTextPresent( "Project Name" );
        assertTextPresent( "Move to Group" );

        assertElementPresent( "saveProjectGroup_0" );
        assertElementPresent( "Cancel" );
    }

    public void buildProjectGroup( String projectGroupName, String groupId, String description ) throws Exception
    {
        int tries = 0, maxTries = 100;

        showProjectGroup( projectGroupName, groupId, description );
        clickButtonWithValue( "Build" );

        // make sure build will be completed
        while ( isElementPresent( "//img[@alt='Updating sources']" ) )
        {
            Thread.sleep( 5000 );
            showProjectGroup( projectGroupName, groupId, description );

            if ( tries++ >= maxTries )
            {
                assertTrue( "Max tries waiting for the project to build reached.", false );
            }
        }
        // test if successfully built, then return to the original page
        clickLinkWithText( "Apache Maven" );
        clickLinkWithText( "Builds" );
        clickLinkWithText( "Result" );
        assertTextPresent( "BUILD SUCCESSFUL" );
        clickLinkWithText( "Project Group Summary" );
    }

    public void assertReleaseSuccess()
    {
        assertTextPresent( "Choose Release Goal for Apache Maven" );
    }

    public void assertReleaseEmpty()
    {
        assertTextPresent( "Cannot release an empty group" );
    }

    public void addValidM2ProjectFromProjectGroup( String projectGroupName, String groupId, String description,
                                                   String m2PomUrl ) throws Exception
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

    public void goToBuildDefinitionPage( String projectGroupName, String groupId, String description ) throws Exception
    {
        showProjectGroup( projectGroupName, groupId, description );
        clickLinkWithText( "Build Definitions" );
        assertTextPresent( "Project Group Build Definitions of " + projectGroupName + " group" );

        assertBuildDefinitionPage( projectGroupName );

    }

    public void assertBuildDefinitionPage( String projectGroupName )
    {

        assertTextPresent( "Project Group Build Definitions of " + projectGroupName + " group" );
        assertElementPresent( "buildDefinition_0" );
    }

    public void assertNotifierPage( String projectGroupName )
    {
        assertTextPresent( "Project Group Notifiers of " + projectGroupName + " group" );
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
        assertTextPresent( "Send on Success" );
        assertTextPresent( "Send on Failure" );
        assertTextPresent( "Send on Error" );
        assertTextPresent( "Send on Warning" );
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

        assertTextPresent( "Full Name" );
        assertElementPresent( "fullName" );

        assertTextPresent( "Password" );
        assertElementPresent( "password" );

        assertTextPresent( "Send on Success" );
        assertTextPresent( "Send on Failure" );
        assertTextPresent( "Send on Error" );
        assertTextPresent( "Send on Warning" );
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
    }

    public void assertAddEditWagonPage()
    {
        assertPage( "Continuum - Add/Edit Wagon Notifier" );

        assertTextPresent( "Project Site URL" );
        assertElementPresent( "url" );

        assertTextPresent( "Send on Success" );
        assertTextPresent( "Send on Failure" );
        assertTextPresent( "Send on Error" );
        assertTextPresent( "Send on Warning" );
    }

    public void addMailNotifier( String projectGroupName, String projectGroupId, String projectGroupDescription,
                                 String email, boolean isValid ) throws Exception
    {

        showProjectGroup( projectGroupName, projectGroupId, projectGroupDescription );
        clickLinkWithText( "Notifiers" );
        assertNotifierPage( projectGroupName );

        clickButtonWithValue( "Add" );
        assertAddNotifierPage();
        selectValue( "addProjectGroupNotifier_notifierType", "Mail" );
        clickButtonWithValue( "Submit" );
        assertAddEditMailNotifierPage();
        setFieldValue( "address", email );
        clickButtonWithValue( "Save" );

        if ( isValid )
        {
            assertNotifierPage( projectGroupName );
        }
        else
        {
            assertTextPresent( "Address is invalid" );
        }
    }

    public void addIrcNotifier( String projectGroupName, String projectGroupId, String projectGroupDescription,
                                String host, String channel, boolean isValid ) throws Exception
    {
        showProjectGroup( projectGroupName, projectGroupId, projectGroupDescription );

        clickLinkWithText( "Notifiers" );
        assertNotifierPage( projectGroupName );

        clickButtonWithValue( "Add" );
        assertAddNotifierPage();
        selectValue( "addProjectGroupNotifier_notifierType", "IRC" );

        clickButtonWithValue( "Submit" );
        assertAddEditIrcNotifierPage();
        setFieldValue( "host", host );
        setFieldValue( "channel", channel );

        clickButtonWithValue( "Save" );
        if ( isValid )
        {
            assertNotifierPage( projectGroupName );
        }
        else
        {
            assertTextPresent( "Host is required" );
            assertTextPresent( "Channel is required" );
        }
    }

    public void addJabberNotifier( String projectGroupName, String projectGroupId, String projectGroupDescription,
                                   String host, String login, String password, String address, boolean isValid )
        throws Exception
    {
        showProjectGroup( projectGroupName, projectGroupId, projectGroupDescription );

        clickLinkWithText( "Notifiers" );
        assertNotifierPage( projectGroupName );

        clickButtonWithValue( "Add" );
        assertAddNotifierPage();
        selectValue( "addProjectGroupNotifier_notifierType", "Jabber" );
        clickButtonWithValue( "Submit" );

        assertAddEditJabberPage();
        setFieldValue( "host", host );
        setFieldValue( "login", login );
        setFieldValue( "password", password );
        setFieldValue( "address", address );
        clickButtonWithValue( "Save" );

        if ( isValid )
        {
            assertNotifierPage( projectGroupName );
        }
        else
        {
            assertTextPresent( "Host is required" );
            assertTextPresent( "Login is required" );
            assertTextPresent( "Password is required" );
            assertTextPresent( "Address is required" );
        }
    }

    public void addMsnNotifierPage( String projectGroupName, String projectGroupId, String projectGroupDescription,
                                    String login, String password, String recipientAddress, boolean isValid )
        throws Exception
    {
        showProjectGroup( projectGroupName, projectGroupId, projectGroupDescription );

        clickLinkWithText( "Notifiers" );
        assertNotifierPage( projectGroupName );

        clickButtonWithValue( "Add" );
        assertAddNotifierPage();
        selectValue( "addProjectGroupNotifier_notifierType", "MSN" );
        clickButtonWithValue( "Submit" );
        assertAddEditMsnPage();
        setFieldValue( "login", login );
        setFieldValue( "password", password );
        setFieldValue( "address", recipientAddress );
        clickButtonWithValue( "Save" );

        if ( isValid )
        {
            assertNotifierPage( projectGroupName );
        }
        else
        {
            assertTextPresent( "Login is required" );
            assertTextPresent( "Password is required" );
            assertTextPresent( "Address is required" );
        }
    }

    public void addWagonNotifierPage( String projectGroupName, String projectGroupId, String projectGroupDescription,
                                      String siteUrl, boolean isValid ) throws Exception
    {
        showProjectGroup( projectGroupName, projectGroupId, projectGroupDescription );

        clickLinkWithText( "Notifiers" );
        assertNotifierPage( projectGroupName );

        clickButtonWithValue( "Add" );
        assertAddNotifierPage();
        selectValue( "addProjectGroupNotifier_notifierType", "Wagon" );
        clickButtonWithValue( "Submit" );
        assertAddEditWagonPage();
        setFieldValue( "url", siteUrl );
        clickButtonWithValue( "Save" );

        if ( isValid )
        {
            assertNotifierPage( projectGroupName );
        }
        else
        {
            assertTextPresent( "Destination URL is required" );
        }
    }

    //////////////////////////////////////
    // General Project Pages
    //////////////////////////////////////
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

    public void goToAddBuildDefinitionPage( String projectGroupName, String projectName )
    {
        clickLinkWithText( "Show Project Groups" );
        clickLinkWithText( projectGroupName );
        clickLinkWithText( projectName );
        clickButtonWithValue( "Add" );

        assertAddBuildDefinitionPage();
    }

    public void assertAddBuildDefinitionPage()
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
        assertTextPresent( "Is it default?" );
        assertElementPresent( "defaultBuildDefinition" );
        assertTextPresent( "Schedule:" );
        assertElementPresent( "scheduleId" );
    }

    public void addBuildDefinition( String projectGroupName, String projectName, String buildFile, String goals,
                                    String arguments, boolean buildFresh, boolean isDefault )
    {
        goToAddBuildDefinitionPage( projectGroupName, projectName );

        // Enter values into Add Build Definition fields, and submit
        setFieldValue( "buildFile", buildFile );
        setFieldValue( "goals", goals );
        setFieldValue( "arguments", arguments );
        if ( buildFresh )
        {
            checkField( "buildFresh" );
        }
        if ( isDefault )
        {
            checkField( "defaultBuildDefinition" );
        }

        submit();

        assertProjectInformationPage();
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

    //////////////////////////////////////
    // Maven 2.0.x Project
    //////////////////////////////////////
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

    public void addMavenTwoProject( String pomUrl, String username, String password, String projectGroup, boolean validProject )
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

        if ( validProject )
        {
            assertProjectGroupsSummaryPage();
        }
        else
        {
            assertAddMavenTwoProjectPage();
        }
    }

    //TODO: problem with input type="file", selenium.type(..) does not work,
    // TODO: refer to http://forums.openqa.org/thread.jspa?messageID=1365&#1365 for workaround
    /*
    public void addMavenTwoProject( String pomFile, String projectGroup, boolean validProject )
        throws Exception
    {
        goToAddMavenTwoProjectPage();

        // Enter values into Add Maven Two Project fields, and submit  
        setFieldValue( "m2PomFile", pomFile );

        if ( projectGroup != null )
        {
            selectValue( "addMavenTwoProject_selectedProjectGroup", projectGroup );
        }

        submit();

        if ( validProject )
        {
            assertProjectGroupsSummaryPage();
        }
        else
        {
            assertAddMavenTwoProjectPage();
        }
    }
    */

    //////////////////////////////////////
    // Maven 1.x Project
    //////////////////////////////////////
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

    public void addMavenOneProject( String pomUrl, String username, String password, String projectGroup, boolean validProject )
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

        if ( validProject )
        {
            assertProjectGroupsSummaryPage();
        }
        else
        {
            assertAddMavenOneProjectPage();
        }
    }

    //TODO: problem with input type="file", selenium.type(..) does not work,
    // TODO: refer to http://forums.openqa.org/thread.jspa?messageID=1365&#1365 for workaround
    /*
    public void addMavenOneProject( String pomFile, String projectGroup, boolean validProject )
        throws Exception
    {
        goToAddMavenOneProjectPage();

        // Enter values into Add Maven One Project fields, and submit  
        setFieldValue( "m1PomFile", pomFile );

        if ( projectGroup != null )
        {
            selectValue( "addMavenOneProject_selectedProjectGroup", projectGroup );
        }

        submit();

        if ( validProject )
        {
            assertProjectGroupsSummaryPage();
        }
        else
        {
            assertAddMavenOneProjectPage();
        }
    }
    */

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

    public void tearDown()
        throws Exception
    {
        login( adminUsername, adminPassword );

        goToProjectGroupsSummaryPage();

        if ( isLinkPresent( TEST_PROJ_GRP_NAME ) )
        {
            removeProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
        }
        // TODO: clean this up
        if ( isLinkPresent( "Apache Maven" ) )
        {
            removeProjectGroup( "Apache Maven", "org.apache.maven", "Maven is a software project management and comprehension tool. Based on the concept of a project object model (POM), Maven can manage a project's build, reporting and documentation from a central piece of information." );
        }
        if ( isLinkPresent( "Maven One Project" ) )
        {
            removeProjectGroup( "Maven One Project", "maven-one-project", "This is a sample Maven One Project." );
        }
        if ( isLinkPresent( DEFAULT_PROJ_GRP_NAME ) &&
            "0".equals( getCellValueFromTable( "ec_table", 1, 2 ) ) == false )
        {
            removeProjectGroup( DEFAULT_PROJ_GRP_NAME, DEFAULT_PROJ_GRP_ID, DEFAULT_PROJ_GRP_DESCRIPTION );
            addProjectGroup( DEFAULT_PROJ_GRP_NAME, DEFAULT_PROJ_GRP_ID, DEFAULT_PROJ_GRP_DESCRIPTION );
        }

        super.tearDown();
    }

    protected String getWebContext()
    {
        return "/continuum";
    }
}
