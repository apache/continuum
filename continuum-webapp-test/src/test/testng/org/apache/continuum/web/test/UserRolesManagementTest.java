package org.apache.continuum.web.test;

import org.apache.continuum.web.test.parent.AbstractUserRolesManagementTest;
import org.testng.annotations.Test;

@Test( groups = {"userroles"}, sequential = true )
public class UserRolesManagementTest
    extends AbstractUserRolesManagementTest
{
    public void testBasicAddDeleteUser()
    {
        username = getProperty( "GUEST_USERNAME" );
        fullname = getProperty( "GUEST_FULLNAME" );

        createUser( username, fullname, getUserEmail(), getUserRolePassword() );
        deleteUser( username );
        clickLinkWithText( "Logout" );
    }

    /*
     * GUEST USER ROLE
     * Guest Role could only view the About Page. Project Groups should not be shown when clicking
     * Show Project Group link.
    */
    @Test( dependsOnMethods = {"testBasicAddDeleteUser"} )
    public void testAddUserWithGuestRole()
    {
        username = getProperty( "GUEST_USERNAME" );
        fullname = getProperty( "GUEST_FULLNAME" );

        createUser( username, fullname, getUserEmail(), getUserRolePassword() );
        assertCreatedUserInfo( username );
        //checkUserRoleWithValue( fullname );
        clickLinkWithLocator( "addRolesToUser_addNDSelectedRoles", false );
        clickButtonWithValue( "Submit" );
        clickLinkWithText( "Logout" );

        login( username, getUserRolePassword() );
        changePassword( getUserRolePassword(), getUserRoleNewPassword() );
        //assertTextPresent( "Password successfully changed" );        
        clickLinkWithText( "Logout" );
    }

    @Test( dependsOnMethods = {"testAddUserWithGuestRole"} )
    public void testGuestUserRoleFunction()
    {
        username = getProperty( "GUEST_USERNAME" );
        fullname = getProperty( "GUEST_FULLNAME" );
        loginAs( username, getUserRoleNewPassword() );
        assertLeftNavMenuWithRole( fullname );
        goToAboutPage();
        clickLinkWithText( "Show Project Groups" );
        assertTextPresent( "Project Groups list is empty" );
        clickLinkWithText( "Logout" );
    }


    /*
     * REGISTERED USER ROLE
     * Registered User Role could only view the About Page. Project Groups should not be shown when clicking
     * Show Project Group link.
    */
    @Test( dependsOnMethods = {"testBasicAddDeleteUser", "testGuestUserRoleFunction"} )
    public void testAddUserWithRegisteredUserRole()
    {
        username = getProperty( "REGISTERED_USERNAME" );
        fullname = getProperty( "REGISTERED_FULLNAME" );

        createUser( username, fullname, getUserEmail(), getUserRolePassword() );
        assertCreatedUserInfo( username );
        checkUserRoleWithValue( fullname );
        clickButtonWithValue( "Submit" );
        clickLinkWithText( "Logout" );

        login( username, getUserRolePassword() );
        changePassword( getUserRolePassword(), getUserRoleNewPassword() );
        assertTextPresent( "Password successfully changed" );

        clickLinkWithText( "Logout" );
        // assertTextPresent("You are already logged in.");
    }

    @Test( dependsOnMethods = {"testAddUserWithRegisteredUserRole"} )
    public void testRegisteredRoleFunction()
    {
        username = getProperty( "REGISTERED_USERNAME" );
        fullname = getProperty( "REGISTERED_FULLNAME" );
        login( username, getUserRoleNewPassword() );
        assertLeftNavMenuWithRole( fullname );
        goToAboutPage();
        clickLinkWithText( "Show Project Groups" );
        assertTextPresent( "Project Groups list is empty." );
        clickLinkWithText( "Logout" );
    }

    /*
     * SYSTEM ADMINISTRATOR ROLE
     * Has access to all functions in the application.
     *
     * The following tests only asserts elements that could be shown 
     * when system admin user is logged in since the user that is used 
     * to test the other functionalities is a system admin user.
     */
    @Test( dependsOnMethods = {"testBasicAddDeleteUser", "testRegisteredRoleFunction"} )
    public void testAddUserWithSystemAdminRole()
    {
        username = getProperty( "SYSAD_USERNAME" );
        fullname = getProperty( "SYSAD_FULLNAME" );

        createUser( username, fullname, getUserEmail(), getUserRolePassword() );
        assertCreatedUserInfo( username );
        checkUserRoleWithValue( fullname );
        clickButtonWithValue( "Submit" );

        clickLinkWithText( "Logout" );

        login( username, getUserRolePassword() );
        changePassword( getUserRolePassword(), getUserRoleNewPassword() );
        assertTextPresent( "Password successfully changed" );

        clickLinkWithText( "Logout" );
    }

    @Test( dependsOnMethods = {"testAddUserWithSystemAdminRole"} )
    public void testSystemAdminRoleFunction()
    {
        username = getProperty( "SYSAD_USERNAME" );
        fullname = getProperty( "SYSAD_FULLNAME" );
        login( username, getUserRoleNewPassword() );
        assertLeftNavMenuWithRole( fullname );
        clickLinkWithText( "Show Project Groups" );
        assertTextNotPresent( "Project Groups list is empty." );
        assertLinkPresent( "Default Project Group" );

        clickLinkWithText( "Logout" );
    }

    /* 
     * USER ADMIN ROLE
     * User Admin role could only add/edit/delete users and can view user Roles. Cannot view Project Groups
     * but can assign a User to a project.
     *
     */
    @Test( dependsOnMethods = {"testBasicAddDeleteUser", "testSystemAdminRoleFunction"} )
    public void testAddUserWithUserAdminRole()
    {
        username = getProperty( "USERADMIN_USERNAME" );
        fullname = getProperty( "USERADMIN_FULLNAME" );

        createUser( username, fullname, getUserEmail(), getUserRolePassword() );
        assertCreatedUserInfo( username );
        checkUserRoleWithValue( fullname );
        clickButtonWithValue( "Submit" );

        clickLinkWithText( "Logout" );

        login( username, getUserRolePassword() );
        changePassword( getUserRolePassword(), getUserRoleNewPassword() );
        assertTextPresent( "Password successfully changed" );

        clickLinkWithText( "Logout" );
    }

    @Test( dependsOnMethods = {"testAddUserWithUserAdminRole"} )
    public void testUserAdminFunction()
    {
        username = getProperty( "USERADMIN_USERNAME" );
        fullname = getProperty( "USERADMIN_FULLNAME" );
        login( username, getUserRoleNewPassword() );
        assertLeftNavMenuWithRole( fullname );
        clickLinkWithText( "Show Project Groups" );
        assertTextPresent( "Project Groups list is empty." );
        // add user
        clickLinkWithText( "Users" );
        clickButtonWithValue( "Create New User" );
        assertCreateUserPage();
        setFieldValue( "user.username", "guest0" );
        setFieldValue( "user.fullName", "guest0" );
        setFieldValue( "user.email", "guest0@guest0.com" );
        setFieldValue( "user.password", "pass" );
        setFieldValue( "user.confirmPassword", "pass" );
        submit();
        assertUserRolesPage();
        clickButtonWithValue( "Submit" );
        selectValue( "name=ec_rd", "50" );
        waitPage();
        // delete user	
        deleteUser( "guest0" );
        // TODO edit user

        clickLinkWithText( "Logout" );
    }

    /*
     * CONTINUUM GROUP PROJECT ADMIN
     * - Can Add/Edit/Delete Project Group, can Add/Edit/Delete projects, can assign Users
     *    roles to existing projects, can add/edit/delete schedules, can view existing roles for the
     *    projects, can build/release projects
     * - Cannot add users, --- --- ---
     */
    @Test( dependsOnMethods = {"testBasicAddDeleteUser", "testUserAdminFunction"} )
    public void testAddUserWithContinuumGroupProjectAdminRole()
    {
        username = getProperty( "GROUPPROJECTADMIN_USERNAME" );
        fullname = getProperty( "GROUPPROJECTADMIN_FULLNAME" );

        createUser( username, fullname, getUserEmail(), getUserRolePassword() );
        assertCreatedUserInfo( username );
        checkUserRoleWithValue( fullname );
        clickButtonWithValue( "Submit" );

        // enable distributed build
        clickLinkWithText( "Configuration" );
        clickLinkWithLocator( "configuration_distributedBuildEnabled", false );
        clickButtonWithValue( "Save" );

        clickLinkWithText( "Logout" );

        login( username, getUserRolePassword() );
        changePassword( getUserRolePassword(), getUserRoleNewPassword() );
        assertTextPresent( "Password successfully changed" );

        assertProjectAdministratorAccess();

        clickLinkWithText( "Logout" );

        loginAsAdmin();
        // disable distributed build
        clickLinkWithText( "Configuration" );
        clickLinkWithLocator( "configuration_distributedBuildEnabled", false );
        clickButtonWithValue( "Save" );

        clickLinkWithText( "Logout" );

        login( username, getUserRoleNewPassword() );
        assertProjectAdministratorAccess();

        clickLinkWithText( "Logout" );
    }

    @Test( dependsOnMethods = {"testAddUserWithContinuumGroupProjectAdminRole"} )
    public void testContinuumGroupProjectAdmin_AddProjectGroup()
        throws Exception
    {
        username = getProperty( "GROUPPROJECTADMIN_USERNAME" );
        fullname = getProperty( "GROUPPROJECTADMIN_FULLNAME" );
        login( username, getUserRoleNewPassword() );
        assertLeftNavMenuWithRole( fullname );
        clickLinkWithText( "Show Project Groups" );
        assertTextNotPresent( "Project Groups list is empty." );
        // test add project group
        clickButtonWithValue( "Add Project Group" );
        setFieldValue( "name", "Test Group" );
        setFieldValue( "groupId", "Test Group" );
        setFieldValue( "description", "testing project group" );
        submit();
    }

    @Test( dependsOnMethods = {"testContinuumGroupProjectAdmin_AddProjectGroup"} )
    public void testContinuumGroupProjectAdmin_AddProjectToProjectGroup()
        throws Exception
    {
        clickLinkWithText( "Test Group" );
        clickButtonWithValue( "Add" );
        assertAddMavenTwoProjectPage();
        setFieldValue( "m2PomUrl", getProperty( "M2_POM_URL" ) );
        clickButtonWithValue( "Add" );
        waitAddProject( "Continuum - Project Group" );
        assertTextPresent( "ContinuumBuildQueueTestData" );
        waitForProjectCheckout();
    }

    @Test( dependsOnMethods = {"testContinuumGroupProjectAdmin_AddProjectToProjectGroup"} )
    public void testContinuumGroupProjectAdmin_BuildProject()
        throws Exception
    {
        buildProjectGroup( "Test Group", "Test Group", "testing project group", "ContinuumBuildQueueTestData", true );
    }

    @Test( dependsOnMethods = {"testContinuumGroupProjectAdmin_BuildProject"} )
    public void testContinuumGroupProjectAdmin_AssignUserToAGroup()
    {
        clickLinkWithText( "Users" );
        clickLinkWithText( "guest1" );
        clickLinkWithText( "Edit Roles" );
        checkUserRoleWithValue( "Guest" );
        checkResourceRoleWithValue( "Project Developer - Test Group" );
        submit();
        clickLinkWithText( "Logout" );
    }

    /*
     * Uncomment the lines below to release a Project provided that you add
     * the values under RELEASE A PROJECT in testng.properties file (project's pom url, access to project to be released.)
    	
    @Test( dependsOnMethods = { "testContinuumGroupProjectAdmin_AssignUserToAGroup" } )
    public void testContinuumGroupProjectAdmin_ReleaseProject() throws Exception
    {
	String projectUrl = getProperty( "PROJECT_URL_TO_BE_RELEASED" );
	String projectName = getProperty( "PROJECT_NAME_TO_BE_RELEASED" );
	String projectUsername = getProperty( "PROJECT_USERNAME" );
	String projectPassword = getProperty( "PROJECT_USERNAME" );
	// add a project group
	clickLinkWithText( "Show Project Groups" );
	clickButtonWithValue( "Add Project Group" );
	setFieldValue( "name", "Project Group" );
        setFieldValue( "groupId", "Project Group" );
        setFieldValue( "description", "project group for projects to be released" );
	submit();
	// add a project to a project group
	clickLinkWithText( "Project Group" );
	clickButtonWithValue( "Add" );
	assertAddMavenTwoProjectPage();
	setFieldValue( "m2PomUrl", projectUrl );
	// set username and password here
	setFieldValue( "scmUsername", projectUsername );
	setFieldValue( "scmPassword", projectPassword );
	clickButtonWithValue( "Add" );
	String title;
	boolean success = true;
	if ( success )
        {
            title = "Continuum - Project Group";
        }
        else
        {
            title = "Continuum - Add Maven Project";
        }
        waitAddProject( title );
	// build the project added in the project group
	buildProjectGroup( "Project Group", "Project Group", "project group for projects to be released", projectName );
	// release the project
	clickButtonWithValue( "Release" );
	clickLinkWithText( "Logout" );
        login( getAdminUsername(), getAdminPassword() );
    }
    */

    @Test( dependsOnMethods = {"testContinuumGroupProjectAdmin_AssignUserToAGroup"} )
    public void testUserWithContinuumGroupProjectDeveloperRole()
    {
        username = getProperty( "GROUPPROJECTDEVELOPER_USERNAME" );
        fullname = getProperty( "GROUPPROJECTDEVELOPER_FULLNAME" );

        createUser( username, fullname, getUserEmail(), getUserRolePassword() );
        assertCreatedUserInfo( username );
        checkUserRoleWithValue( fullname );
        clickButtonWithValue( "Submit" );
        clickLinkWithText( "Logout" );

        login( username, getUserRolePassword() );
        changePassword( getUserRolePassword(), getUserRoleNewPassword() );
        assertTextPresent( "Password successfully changed" );
        clickLinkWithText( "Logout" );

        login( username, getUserRoleNewPassword() );
        assertLeftNavMenuWithRole( fullname );
        clickLinkWithText( "Logout" );
    }

    @Test( dependsOnMethods = {"testUserWithContinuumGroupProjectDeveloperRole"} )
    public void testUserWithContinuumGroupProjectUserRole()
    {
        username = getProperty( "GROUPPROJECTUSER_USERNAME" );
        fullname = getProperty( "GROUPPROJECTUSER_FULLNAME" );

        createUser( username, fullname, getUserEmail(), getUserRolePassword() );
        assertCreatedUserInfo( username );
        checkUserRoleWithValue( fullname );
        clickButtonWithValue( "Submit" );
        clickLinkWithText( "Logout" );

        login( username, getUserRolePassword() );
        changePassword( getUserRolePassword(), getUserRoleNewPassword() );
        assertTextPresent( "Password successfully changed" );
        clickLinkWithText( "Logout" );

        login( username, getUserRoleNewPassword() );
        assertLeftNavMenuWithRole( fullname );
        clickLinkWithText( "Logout" );
    }

    @Test( dependsOnMethods = {"testUserWithContinuumGroupProjectUserRole"} )
    public void testUserWithContinuumManageBuildEnvironmentRole()
    {
        username = getProperty( "MANAGEBUILDENVIRONMENT_USERNAME" );
        fullname = getProperty( "MANAGEBUILDENVIRONMENT_FULLNAME" );

        createUser( username, fullname, getUserEmail(), getUserRolePassword() );
        assertCreatedUserInfo( username );
        checkUserRoleWithValue( fullname );
        clickButtonWithValue( "Submit" );
        clickLinkWithText( "Logout" );

        login( username, getUserRolePassword() );
        changePassword( getUserRolePassword(), getUserRoleNewPassword() );
        assertTextPresent( "Password successfully changed" );
        clickLinkWithText( "Logout" );

        login( username, getUserRoleNewPassword() );
        assertLeftNavMenuWithRole( fullname );
        clickLinkWithText( "Logout" );
    }

    @Test( dependsOnMethods = {"testUserWithContinuumManageBuildEnvironmentRole"} )
    public void testUserWithContinuumManageBuildTemplatesRole()
    {
        username = getProperty( "MANAGEBUILDTEMPLATES_USERNAME" );
        fullname = getProperty( "MANAGEBUILDTEMPLATES_FULLNAME" );

        createUser( username, fullname, getUserEmail(), getUserRolePassword() );
        assertCreatedUserInfo( username );
        checkUserRoleWithValue( fullname );
        clickButtonWithValue( "Submit" );
        clickLinkWithText( "Logout" );

        login( username, getUserRolePassword() );
        changePassword( getUserRolePassword(), getUserRoleNewPassword() );
        assertTextPresent( "Password successfully changed" );
        clickLinkWithText( "Logout" );

        login( username, getUserRoleNewPassword() );
        assertLeftNavMenuWithRole( fullname );
        clickLinkWithText( "Logout" );
    }

    @Test( dependsOnMethods = {"testUserWithContinuumManageBuildTemplatesRole"} )
    public void testUserWithContinuumManageInstallationsRole()
    {
        username = getProperty( "MANAGEINSTALLATIONS_USERNAME" );
        fullname = getProperty( "MANAGEINSTALLATIONS_FULLNAME" );

        createUser( username, fullname, getUserEmail(), getUserRolePassword() );
        assertCreatedUserInfo( username );
        checkUserRoleWithValue( fullname );
        clickButtonWithValue( "Submit" );
        clickLinkWithText( "Logout" );

        login( username, getUserRolePassword() );
        changePassword( getUserRolePassword(), getUserRoleNewPassword() );
        assertTextPresent( "Password successfully changed" );
        clickLinkWithText( "Logout" );

        login( username, getUserRoleNewPassword() );
        assertLeftNavMenuWithRole( fullname );
        clickLinkWithText( "Logout" );
    }

    @Test( dependsOnMethods = {"testUserWithContinuumManageInstallationsRole"} )
    public void testUserWithContinuumManageLocalRepoRole()
    {
        username = getProperty( "MANAGELOCALREPOS_USERNAME" );
        fullname = getProperty( "MANAGELOCALREPOS_FULLNAME" );

        createUser( username, fullname, getUserEmail(), getUserRolePassword() );
        assertCreatedUserInfo( username );
        checkUserRoleWithValue( fullname );
        clickButtonWithValue( "Submit" );
        clickLinkWithText( "Logout" );

        login( username, getUserRolePassword() );
        changePassword( getUserRolePassword(), getUserRoleNewPassword() );
        assertTextPresent( "Password successfully changed" );
        clickLinkWithText( "Logout" );

        login( username, getUserRoleNewPassword() );
        assertLeftNavMenuWithRole( fullname );
        clickLinkWithText( "Logout" );
    }

    @Test( dependsOnMethods = {"testUserWithContinuumManageLocalRepoRole"} )
    public void testUserWithContinuumManagePurgingRole()
    {
        username = getProperty( "MANAGEPURGING_USERNAME" );
        fullname = getProperty( "MANAGEPURGING_FULLNAME" );

        createUser( username, fullname, getUserEmail(), getUserRolePassword() );
        assertCreatedUserInfo( username );
        checkUserRoleWithValue( fullname );
        clickButtonWithValue( "Submit" );
        clickLinkWithText( "Logout" );

        login( username, getUserRolePassword() );
        changePassword( getUserRolePassword(), getUserRoleNewPassword() );
        assertTextPresent( "Password successfully changed" );
        clickLinkWithText( "Logout" );

        login( username, getUserRoleNewPassword() );
        assertLeftNavMenuWithRole( fullname );
        clickLinkWithText( "Logout" );
    }

    @Test( dependsOnMethods = {"testUserWithContinuumManagePurgingRole"} )
    public void testUserWithContinuumManageQueuesRole()
    {
        username = getProperty( "MANAGEQUEUES_USERNAME" );
        fullname = getProperty( "MANAGEQUEUES_FULLNAME" );

        createUser( username, fullname, getUserEmail(), getUserRolePassword() );
        assertCreatedUserInfo( username );
        checkUserRoleWithValue( fullname );
        clickButtonWithValue( "Submit" );
        clickLinkWithText( "Logout" );

        login( username, getUserRolePassword() );
        changePassword( getUserRolePassword(), getUserRoleNewPassword() );
        assertTextPresent( "Password successfully changed" );
        clickLinkWithText( "Logout" );

        login( username, getUserRoleNewPassword() );
        assertLeftNavMenuWithRole( fullname );
        clickLinkWithText( "Logout" );
    }

    @Test( dependsOnMethods = {"testUserWithContinuumManageQueuesRole"} )
    public void testUserWithContinuumManageSchedulingRole()
    {
        username = getProperty( "MANAGESCHEDULING_USERNAME" );
        fullname = getProperty( "MANAGESCHEDULING_FULLNAME" );

        createUser( username, fullname, getUserEmail(), getUserRolePassword() );
        assertCreatedUserInfo( username );
        checkUserRoleWithValue( fullname );
        clickButtonWithValue( "Submit" );
        clickLinkWithText( "Logout" );

        login( username, getUserRolePassword() );
        changePassword( getUserRolePassword(), getUserRoleNewPassword() );
        assertTextPresent( "Password successfully changed" );
        clickLinkWithText( "Logout" );

        login( username, getUserRoleNewPassword() );
        assertLeftNavMenuWithRole( fullname );
        clickLinkWithText( "Logout" );
    }

    @Test( dependsOnMethods = {"testUserWithContinuumManageSchedulingRole"} )
    public void testUserWithProjectAdminDefaultProjectGroup()
    {
        username = getProperty( "PROJECTADMINISTRATOR_DEFAULTPROJECTGROUP_USERNAME" );
        fullname = getProperty( "PROJECTADMINISTRATOR_DEFAULTPROJECTGROUP_FULLNAME" );

        createUser( username, fullname, getUserEmail(), getUserRolePassword() );
        assertCreatedUserInfo( username );
        checkResourceRoleWithValue( fullname );
        clickButtonWithValue( "Submit" );
        clickLinkWithText( "Logout" );

        login( username, getUserRolePassword() );
        changePassword( getUserRolePassword(), getUserRoleNewPassword() );
        assertTextPresent( "Password successfully changed" );
        clickLinkWithText( "Logout" );

        login( username, getUserRoleNewPassword() );
        assertLeftNavMenuWithRole( fullname );
        clickLinkWithText( "Logout" );
    }

    @Test( dependsOnMethods = {"testUserWithProjectAdminDefaultProjectGroup"} )
    public void testUserWithProjectDevDefaultProjectGroup()
    {
        username = getProperty( "PROJECTDEVELOPER_DEFAULTPROJECTGROUP_USERNAME" );
        fullname = getProperty( "PROJECTDEVELOPER_DEFAULTPROJECTGROUP_FULLNAME" );

        createUser( username, fullname, getUserEmail(), getUserRolePassword() );
        assertCreatedUserInfo( username );
        checkResourceRoleWithValue( fullname );
        clickButtonWithValue( "Submit" );
        clickLinkWithText( "Logout" );

        login( username, getUserRolePassword() );
        changePassword( getUserRolePassword(), getUserRoleNewPassword() );
        assertTextPresent( "Password successfully changed" );
        clickLinkWithText( "Logout" );

        login( username, getUserRoleNewPassword() );
        assertLeftNavMenuWithRole( fullname );
        clickLinkWithText( "Logout" );
    }

    @Test( dependsOnMethods = {"testUserWithProjectDevDefaultProjectGroup"} )
    public void testUserWithProjectUserDefaultProjectGroup()
    {
        username = getProperty( "PROJECTUSER_DEFAULTPROJECTGROUP_USERNAME" );
        fullname = getProperty( "PROJECTUSER_DEFAULTPROJECTGROUP_FULLNAME" );

        createUser( username, fullname, getUserEmail(), getUserRolePassword() );
        assertCreatedUserInfo( username );
        checkResourceRoleWithValue( fullname );
        clickButtonWithValue( "Submit" );
        clickLinkWithText( "Logout" );

        login( username, getUserRolePassword() );
        changePassword( getUserRolePassword(), getUserRoleNewPassword() );
        assertTextPresent( "Password successfully changed" );
        clickLinkWithText( "Logout" );

        login( username, getUserRoleNewPassword() );
        assertLeftNavMenuWithRole( fullname );
        clickLinkWithText( "Logout" );
    }

}
