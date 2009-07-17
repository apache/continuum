package org.apache.continuum.web.test;

import org.apache.continuum.web.test.parent.AbstractUserRolesManagementTest;
import org.testng.annotations.Test;

@Test( groups = { "userroles" }, dependsOnMethods = { "testWithCorrectUsernamePassword" } )
public class UserRolesManagementTest
    extends AbstractUserRolesManagementTest
{
    public void testBasicAddDeleteUser()
    {
        username = getProperty( "GUEST_USERNAME" ) + getTestId();
        fullname = getProperty( "GUEST_FULLNAME" );

        createUser( username, fullname, getUserEmail(), getUserRolePassword(), true );
        deleteUser( username, fullname, getUserEmail() );
        clickLinkWithText( "Logout" );
        login( getAdminUsername(), getAdminPassword() );
    }

    @Test( dependsOnMethods = { "testBasicAddDeleteUser" } )
    public void testUserWithGuestRole()
    {
        username = getProperty( "GUEST_USERNAME" ) + getTestId();
        fullname = getProperty( "GUEST_FULLNAME" );

        createUser( username, fullname, getUserEmail(), getUserRolePassword(), true );
        assertCreatedUserInfo( username );
        checkUserRoleWithValue( fullname );
		clickButtonWithValue( "Submit" );

        clickLinkWithText( "Logout" );

        login( username, getUserRolePassword() );
        changePassword( getUserRolePassword(), getUserRoleNewPassword() );
        // this section will be removed if issue from redback after changing password will be fixed.
        getSelenium().goBack();
        waitPage();
        clickLinkWithText( "Logout" );
        // assertTextPresent("You are already logged in.");

        login( username, getUserRoleNewPassword() );
        assertLeftNavMenuWithRole( fullname );
        clickLinkWithText( "Show Project Groups" );
        assertTextPresent( "Project Groups list is empty." );
        clickLinkWithText( "Logout" );
        login( getAdminUsername(), getAdminPassword() );
    }

    @Test( dependsOnMethods = { "testBasicAddDeleteUser" } )
    public void testUserWithRegisteredUserRole()
    {
        username = getProperty( "REGISTERED_USERNAME" );
        fullname = getProperty( "REGISTERED_FULLNAME" );

        createUser( username, fullname, getUserEmail(), getUserRolePassword(), true );
        assertCreatedUserInfo( username );
        checkUserRoleWithValue( fullname );
		clickButtonWithValue( "Submit" );

        clickLinkWithText( "Logout" );

        login( username, getUserRolePassword() );
        changePassword( getUserRolePassword(), getUserRoleNewPassword() );

        // this section will be removed if issue from redback after changing password will be fixed.
        getSelenium().goBack();
        waitPage();
        clickLinkWithText( "Logout" );
        // assertTextPresent("You are already logged in.");

        login( username, getUserRoleNewPassword() );
        assertLeftNavMenuWithRole( fullname );
        clickLinkWithText( "Show Project Groups" );
        assertTextPresent( "Project Groups list is empty." );
        clickLinkWithText( "Logout" );
        login( getAdminUsername(), getAdminPassword() );
    }

    @Test( dependsOnMethods = { "testBasicAddDeleteUser" } )
    public void testUserWithSystemAdminRole()
    {
        username = getProperty( "SYSAD_USERNAME" );
        fullname = getProperty( "SYSAD_FULLNAME" );

        createUser( username, fullname, getUserEmail(), getUserRolePassword(), true );
        assertCreatedUserInfo( username );
        checkUserRoleWithValue( fullname );
		clickButtonWithValue( "Submit" );

        clickLinkWithText( "Logout" );

        login( username, getUserRolePassword() );
        changePassword( getUserRolePassword(), getUserRoleNewPassword() );

        // this section will be removed if issue from redback after changing password will be fixed.
        getSelenium().goBack();
        waitPage();
        clickLinkWithText( "Logout" );
        // assertTextPresent("You are already logged in.");

        login( username, getUserRoleNewPassword() );
        assertLeftNavMenuWithRole( fullname );
        clickLinkWithText( "Show Project Groups" );
        assertTextNotPresent( "Project Groups list is empty." );
        clickLinkWithText( "Logout" );
        login( getAdminUsername(), getAdminPassword() );
    }

    @Test( dependsOnMethods = { "testBasicAddDeleteUser" } )
    public void testUserWithUserAdminRole()
    {
        username = getProperty( "USERADMIN_USERNAME" );
        fullname = getProperty( "USERADMIN_FULLNAME" );

        createUser( username, fullname, getUserEmail(), getUserRolePassword(), true );
        assertCreatedUserInfo( username );
            checkUserRoleWithValue( fullname );
		clickButtonWithValue( "Submit" );

        clickLinkWithText( "Logout" );

        login( username, getUserRolePassword() );
        changePassword( getUserRolePassword(), getUserRoleNewPassword() );

        // this section will be removed if issue from redback after changing password will be fixed.
        getSelenium().goBack();
        waitPage();
        clickLinkWithText( "Logout" );
        // assertTextPresent("You are already logged in.");

        login( username, getUserRoleNewPassword() );
        assertLeftNavMenuWithRole( fullname );
        clickLinkWithText( "Show Project Groups" );
        assertTextPresent( "Project Groups list is empty." );
        clickLinkWithText( "Logout" );
        login( getAdminUsername(), getAdminPassword() );
    }

    @Test( dependsOnMethods = { "testBasicAddDeleteUser" } )
    public void testUserWithContinuumGroupProjectAdminRole()
    {
        username = getProperty( "GROUPPROJECTADMIN_USERNAME" );
        fullname = getProperty( "GROUPPROJECTADMIN_FULLNAME" );

        createUser( username, fullname, getUserEmail(), getUserRolePassword(), true );
        assertCreatedUserInfo( username );
        checkUserRoleWithValue( fullname );
        clickButtonWithValue( "Submit" );

        clickLinkWithText( "Logout" );

        login( username, getUserRolePassword() );
        changePassword( getUserRolePassword(), getUserRoleNewPassword() );

        // this section will be removed if issue from redback after changing password will be fixed.
        getSelenium().goBack();
        waitPage();
        clickLinkWithText( "Logout" );
        // assertTextPresent("You are already logged in.");

        login( username, getUserRoleNewPassword() );
        assertLeftNavMenuWithRole( fullname );
        clickLinkWithText( "Logout" );
        login( getAdminUsername(), getAdminPassword() );
    }

    @Test( dependsOnMethods = { "testBasicAddDeleteUser" } )
    public void testUserWithContinuumGroupProjectDeveloperRole()
    {
        username = getProperty( "GROUPPROJECTDEVELOPER_USERNAME" );
        fullname = getProperty( "GROUPPROJECTDEVELOPER_FULLNAME" );

        createUser( username, fullname, getUserEmail(), getUserRolePassword(), true );
        assertCreatedUserInfo( username );
        checkUserRoleWithValue( fullname );
		clickButtonWithValue( "Submit" );

        clickLinkWithText( "Logout" );

        login( username, getUserRolePassword() );
        changePassword( getUserRolePassword(), getUserRoleNewPassword() );

        // this section will be removed if issue from redback after changing password will be fixed.
        getSelenium().goBack();
        waitPage();
        clickLinkWithText( "Logout" );
        // assertTextPresent("You are already logged in.");

        login( username, getUserRoleNewPassword() );
        assertLeftNavMenuWithRole( fullname );
        clickLinkWithText( "Logout" );
        login( getAdminUsername(), getAdminPassword() );
    }

    @Test( dependsOnMethods = { "testBasicAddDeleteUser" } )
    public void testUserWithContinuumGroupProjectUserRole()
    {
        username = getProperty( "GROUPPROJECTUSER_USERNAME" );
        fullname = getProperty( "GROUPPROJECTUSER_FULLNAME" );

        createUser( username, fullname, getUserEmail(), getUserRolePassword(), true );
        assertCreatedUserInfo( username );
        checkUserRoleWithValue( fullname );
		clickButtonWithValue( "Submit" );

        clickLinkWithText( "Logout" );

        login( username, getUserRolePassword() );
        changePassword( getUserRolePassword(), getUserRoleNewPassword() );

        // this section will be removed if issue from redback after changing password will be fixed.
        getSelenium().goBack();
        waitPage();
        clickLinkWithText( "Logout" );
        // assertTextPresent("You are already logged in.");

        login( username, getUserRoleNewPassword() );
        assertLeftNavMenuWithRole( fullname );
        clickLinkWithText( "Logout" );
        login( getAdminUsername(), getAdminPassword() );
    }

    @Test( dependsOnMethods = { "testBasicAddDeleteUser" } )
    public void testUserWithContinuumManageBuildEnvironmentRole()
    {
        username = getProperty( "MANAGEBUILDENVIRONMENT_USERNAME" );
        fullname = getProperty( "MANAGEBUILDENVIRONMENT_FULLNAME" );

        createUser( username, fullname, getUserEmail(), getUserRolePassword(), true );
        assertCreatedUserInfo( username );
        checkUserRoleWithValue( fullname );
		clickButtonWithValue( "Submit" );

        clickLinkWithText( "Logout" );

        login( username, getUserRolePassword() );
        changePassword( getUserRolePassword(), getUserRoleNewPassword() );

        // this section will be removed if issue from redback after changing password will be fixed.
        getSelenium().goBack();
        waitPage();
        clickLinkWithText( "Logout" );
        // assertTextPresent("You are already logged in.");

        login( username, getUserRoleNewPassword() );
        assertLeftNavMenuWithRole( fullname );
        clickLinkWithText( "Logout" );
        login( getAdminUsername(), getAdminPassword() );
    }

    @Test( dependsOnMethods = { "testBasicAddDeleteUser" } )
    public void testUserWithContinuumManageBuildTemplatesRole()
    {
        username = getProperty( "MANAGEBUILDTEMPLATES_USERNAME" );
        fullname = getProperty( "MANAGEBUILDTEMPLATES_FULLNAME" );

        createUser( username, fullname, getUserEmail(), getUserRolePassword(), true );
        assertCreatedUserInfo( username );
        checkUserRoleWithValue( fullname );
		clickButtonWithValue( "Submit" );

        clickLinkWithText( "Logout" );

        login( username, getUserRolePassword() );
        changePassword( getUserRolePassword(), getUserRoleNewPassword() );

        // this section will be removed if issue from redback after changing password will be fixed.
        getSelenium().goBack();
        waitPage();
        clickLinkWithText( "Logout" );
        // assertTextPresent("You are already logged in.");

        login( username, getUserRoleNewPassword() );
        assertLeftNavMenuWithRole( fullname );
        clickLinkWithText( "Logout" );
        login( getAdminUsername(), getAdminPassword() );
    }

    @Test( dependsOnMethods = { "testBasicAddDeleteUser" } )
    public void testUserWithContinuumManageInstallationsRole()
    {
        username = getProperty( "MANAGEINSTALLATIONS_USERNAME" );
        fullname = getProperty( "MANAGEINSTALLATIONS_FULLNAME" );

        createUser( username, fullname, getUserEmail(), getUserRolePassword(), true );
        assertCreatedUserInfo( username );
        checkUserRoleWithValue( fullname );
		clickButtonWithValue( "Submit" );

        clickLinkWithText( "Logout" );

        login( username, getUserRolePassword() );
        changePassword( getUserRolePassword(), getUserRoleNewPassword() );

        // this section will be removed if issue from redback after changing password will be fixed.
        getSelenium().goBack();
        waitPage();
        clickLinkWithText( "Logout" );
        // assertTextPresent("You are already logged in.");

        login( username, getUserRoleNewPassword() );
        assertLeftNavMenuWithRole( fullname );
        clickLinkWithText( "Logout" );
        login( getAdminUsername(), getAdminPassword() );
    }

    @Test( dependsOnMethods = { "testBasicAddDeleteUser" } )
    public void testUserWithContinuumManageLocalRepoRole()
    {
        username = getProperty( "MANAGELOCALREPOS_USERNAME" );
        fullname = getProperty( "MANAGELOCALREPOS_FULLNAME" );

        createUser( username, fullname, getUserEmail(), getUserRolePassword(), true );
        assertCreatedUserInfo( username );
        checkUserRoleWithValue( fullname );
		clickButtonWithValue( "Submit" );

        clickLinkWithText( "Logout" );

        login( username, getUserRolePassword() );
        changePassword( getUserRolePassword(), getUserRoleNewPassword() );

        // this section will be removed if issue from redback after changing password will be fixed.
        getSelenium().goBack();
        waitPage();
        clickLinkWithText( "Logout" );
        // assertTextPresent("You are already logged in.");

        login( username, getUserRoleNewPassword() );
        assertLeftNavMenuWithRole( fullname );
        clickLinkWithText( "Logout" );
        login( getAdminUsername(), getAdminPassword() );
    }

    @Test( dependsOnMethods = { "testBasicAddDeleteUser" } )
    public void testUserWithContinuumManagePurgingRole()
    {
        username = getProperty( "MANAGEPURGING_USERNAME" );
        fullname = getProperty( "MANAGEPURGING_FULLNAME" );

        createUser( username, fullname, getUserEmail(), getUserRolePassword(), true );
        assertCreatedUserInfo( username );
        checkUserRoleWithValue( fullname );
		clickButtonWithValue( "Submit" );

        clickLinkWithText( "Logout" );

        login( username, getUserRolePassword() );
        changePassword( getUserRolePassword(), getUserRoleNewPassword() );

        // this section will be removed if issue from redback after changing password will be fixed.
        getSelenium().goBack();
        waitPage();
        clickLinkWithText( "Logout" );
        // assertTextPresent("You are already logged in.");

        login( username, getUserRoleNewPassword() );
        assertLeftNavMenuWithRole( fullname );
        clickLinkWithText( "Logout" );
        login( getAdminUsername(), getAdminPassword() );
    }

    @Test( dependsOnMethods = { "testBasicAddDeleteUser" } )
    public void testUserWithContinuumManageQueuesRole()
    {
        username = getProperty( "MANAGEQUEUES_USERNAME" );
        fullname = getProperty( "MANAGEQUEUES_FULLNAME" );

        createUser( username, fullname, getUserEmail(), getUserRolePassword(), true );
        assertCreatedUserInfo( username );
        checkUserRoleWithValue( fullname );
		clickButtonWithValue( "Submit" );

        clickLinkWithText( "Logout" );

        login( username, getUserRolePassword() );
        changePassword( getUserRolePassword(), getUserRoleNewPassword() );

        // this section will be removed if issue from redback after changing password will be fixed.
        getSelenium().goBack();
        waitPage();
        clickLinkWithText( "Logout" );
        // assertTextPresent("You are already logged in.");

        login( username, getUserRoleNewPassword() );
        assertLeftNavMenuWithRole( fullname );
        clickLinkWithText( "Logout" );
        login( getAdminUsername(), getAdminPassword() );
    }

    @Test( dependsOnMethods = { "testBasicAddDeleteUser" } )
    public void testUserWithContinuumManageSchedulingRole()
    {
        username = getProperty( "MANAGESCHEDULING_USERNAME" );
        fullname = getProperty( "MANAGESCHEDULING_FULLNAME" );

        createUser( username, fullname, getUserEmail(), getUserRolePassword(), true );
        assertCreatedUserInfo( username );
        checkUserRoleWithValue( fullname );
		clickButtonWithValue( "Submit" );
        clickLinkWithText( "Logout" );

        login( username, getUserRolePassword() );
        changePassword( getUserRolePassword(), getUserRoleNewPassword() );

        // this section will be removed if issue from redback after changing password will be fixed.
        getSelenium().goBack();
        waitPage();
        clickLinkWithText( "Logout" );
        // assertTextPresent("You are already logged in.");

        login( username, getUserRoleNewPassword() );
        assertLeftNavMenuWithRole( fullname );
        clickLinkWithText( "Logout" );
        login( getAdminUsername(), getAdminPassword() );
    }

    @Test( dependsOnMethods = { "testBasicAddDeleteUser" } )
    public void testUserWithProjectAdminDefaultProjectGroup()
    {
        username = getProperty( "PROJECTADMINISTRATOR_DEFAULTPROJECTGROUP_USERNAME" );
        fullname = getProperty( "PROJECTADMINISTRATOR_DEFAULTPROJECTGROUP_FULLNAME" );

        createUser( username, fullname, getUserEmail(), getUserRolePassword(), true );
        assertCreatedUserInfo( username );
        checkResourceRoleWithValue( fullname );
		clickButtonWithValue( "Submit" );

        clickLinkWithText( "Logout" );

        login( username, getUserRolePassword() );
        changePassword( getUserRolePassword(), getUserRoleNewPassword() );

        // this section will be removed if issue from redback after changing password will be fixed.
        getSelenium().goBack();
        waitPage();
        clickLinkWithText( "Logout" );
        // assertTextPresent("You are already logged in.");

        login( username, getUserRoleNewPassword() );
        assertLeftNavMenuWithRole( fullname );
        clickLinkWithText( "Logout" );
        login( getAdminUsername(), getAdminPassword() );
    }

    @Test( dependsOnMethods = { "testBasicAddDeleteUser" } )
    public void testUserWithProjectDevDefaultProjectGroup()
    {
        username = getProperty( "PROJECTDEVELOPER_DEFAULTPROJECTGROUP_USERNAME" );
        fullname = getProperty( "PROJECTDEVELOPER_DEFAULTPROJECTGROUP_FULLNAME" );

        createUser( username, fullname, getUserEmail(), getUserRolePassword(), true );
        assertCreatedUserInfo( username );
        checkResourceRoleWithValue( fullname );
		clickButtonWithValue( "Submit" );

        clickLinkWithText( "Logout" );

        login( username, getUserRolePassword() );
        changePassword( getUserRolePassword(), getUserRoleNewPassword() );

        // this section will be removed if issue from redback after changing password will be fixed.
        getSelenium().goBack();
        waitPage();
        clickLinkWithText( "Logout" );
        // assertTextPresent("You are already logged in.");

        login( username, getUserRoleNewPassword() );
        assertLeftNavMenuWithRole( fullname );
        clickLinkWithText( "Logout" );
        login( getAdminUsername(), getAdminPassword() );
    }

    @Test( dependsOnMethods = { "testBasicAddDeleteUser" } )
    public void testUserWithProjectUserDefaultProjectGroup()
    {
        username = getProperty( "PROJECTUSER_DEFAULTPROJECTGROUP_USERNAME" );
        fullname = getProperty( "PROJECTUSER_DEFAULTPROJECTGROUP_FULLNAME" );

        createUser( username, fullname, getUserEmail(), getUserRolePassword(), true );
        assertCreatedUserInfo( username );
        checkResourceRoleWithValue( fullname );
		clickButtonWithValue( "Submit" );
        clickLinkWithText( "Logout" );

        login( username, getUserRolePassword() );
        changePassword( getUserRolePassword(), getUserRoleNewPassword() );

        // this section will be removed if issue from redback after changing password will be fixed.
        getSelenium().goBack();
        waitPage();
        clickLinkWithText( "Logout" );
        // assertTextPresent("You are already logged in.");

        login( username, getUserRoleNewPassword() );
        assertLeftNavMenuWithRole( fullname );
        clickLinkWithText( "Logout" );
        login( getAdminUsername(), getAdminPassword() );
    }

}
