package org.apache.continuum.web.test;

import org.apache.continuum.web.test.parent.AbstractUserRolesManagementTest;
import org.testng.annotations.Test;


@Test( groups = { "userroles" }, dependsOnMethods = { "testWithCorrectUsernamePassword" } )
public class UserRolesManagementTest 
	extends AbstractUserRolesManagementTest
{
	public void testBasicAddDeleteUser()
	{
		username = p.getProperty( "GUEST_USERNAME" );
		fullname = p.getProperty( "GUEST_FULLNAME" );
		
		createUser( username, fullname, getUserEmail(), getUserRolePassword(), true);
		deleteUser( username, fullname, getUserEmail() );
		clickLinkWithText( "Logout" );
	}
	
        @Test( dependsOnMethods = { "testBasicAddDeleteUser" } )
	public void testUserWithGuestRole()
	{
		username = p.getProperty("GUEST_USERNAME");
		fullname = p.getProperty("GUEST_FULLNAME");
		
		createUser(username, fullname, getUserEmail(), getUserRolePassword(), true);
		clickLinkWithText( username );
		clickLinkWithText( "Edit Roles" );
		checkUserRoleWithValue( fullname );
		clickButtonWithValue( "Submit" );
		
		clickLinkWithText("Logout");
		
		login( username, getUserRolePassword() );
		changePassword( getUserRolePassword(), getUserRoleNewPassword() );
		// this section will be removed if issue from redback after changing password will be fixed.
		getSelenium().goBack();
		waitPage();
		clickLinkWithText("Logout");
		//assertTextPresent("You are already logged in.");
		
		login(username, getUserRoleNewPassword());
		assertLeftNavMenuWithRole( fullname );
		clickLinkWithText( "Show Project Groups" );
		assertTextPresent( "Project Groups list is empty." );
		clickLinkWithText("Logout");
	}

        @Test( dependsOnMethods = { "testBasicAddDeleteUser" } )
	public void testUserWithRegisteredUserRole()
	{
		username = p.getProperty( "REGISTERED_USERNAME" );
		fullname = p.getProperty( "REGISTERED_FULLNAME" );
		
		createUser(username, fullname, getUserEmail(), getUserRolePassword(), true);
		clickLinkWithText( username );
		clickLinkWithText( "Edit Roles" );
		checkUserRoleWithValue( fullname );
		clickButtonWithValue( "Submit" );
		
		clickLinkWithText("Logout");
		
		login( username, getUserRolePassword() );
		changePassword( getUserRolePassword(), getUserRoleNewPassword() );
		
		// this section will be removed if issue from redback after changing password will be fixed.
		getSelenium().goBack();
		waitPage();
		clickLinkWithText("Logout");
		//assertTextPresent("You are already logged in.");
		
		login(username, getUserRoleNewPassword());
		assertLeftNavMenuWithRole( fullname );
		clickLinkWithText( "Show Project Groups" );
		assertTextPresent( "Project Groups list is empty." );
		clickLinkWithText("Logout");
		login( getAdminUsername(), getAdminPassword());
	}

        @Test( dependsOnMethods = { "testBasicAddDeleteUser" } )	
	public void testUserWithSystemAdminRole()
	{
		username = p.getProperty( "SYSAD_USERNAME" );
		fullname = p.getProperty( "SYSAD_FULLNAME" );
		
		createUser(username, fullname, getUserEmail(), getUserRolePassword(), true);
		clickLinkWithText( username );
		clickLinkWithText( "Edit Roles" );
		checkUserRoleWithValue( fullname );
		clickButtonWithValue( "Submit" );
		
		clickLinkWithText("Logout");
		
		login( username, getUserRolePassword() );
		changePassword( getUserRolePassword(), getUserRoleNewPassword() );
		
		// this section will be removed if issue from redback after changing password will be fixed.
		getSelenium().goBack();
		waitPage();
		clickLinkWithText("Logout");
		//assertTextPresent("You are already logged in.");
		
		login(username, getUserRoleNewPassword());
		assertLeftNavMenuWithRole( fullname );
		clickLinkWithText( "Show Project Groups" );
		assertTextNotPresent( "Project Groups list is empty." );
		clickLinkWithText("Logout");
		login( getAdminUsername(), getAdminPassword());
	}
	
        @Test( dependsOnMethods = { "testBasicAddDeleteUser" } )
	public void testUserWithUserAdminRole()
	{
		username = p.getProperty( "USERADMIN_USERNAME" );
		fullname = p.getProperty( "USERADMIN_FULLNAME" );
		
		createUser(username, fullname, getUserEmail(), getUserRolePassword(), true);
		selectValue( "name=ec_rd" , "50" );
		waitPage();
		clickLinkWithText( username );
		clickLinkWithText( "Edit Roles" );
		checkUserRoleWithValue( fullname );
		clickButtonWithValue( "Submit" );
		
		clickLinkWithText("Logout");
		
		login( username, getUserRolePassword() );
		changePassword( getUserRolePassword(), getUserRoleNewPassword() );
		
		// this section will be removed if issue from redback after changing password will be fixed.
		getSelenium().goBack();
		waitPage();
		clickLinkWithText("Logout");
		//assertTextPresent("You are already logged in.");
		
		login(username, getUserRoleNewPassword());
		assertLeftNavMenuWithRole( fullname );
		clickLinkWithText( "Show Project Groups" );
		assertTextPresent( "Project Groups list is empty." );
		clickLinkWithText("Logout");
		login( getAdminUsername(), getAdminPassword());
	}
	
        @Test( dependsOnMethods = { "testBasicAddDeleteUser" } )
	public void testUserWithContinuumGroupProjectAdminRole()
	{
		username = p.getProperty( "GROUPPROJECTADMIN_USERNAME" );
		fullname = p.getProperty( "GROUPPROJECTADMIN_FULLNAME" );
		
		createUser(username, fullname, getUserEmail(), getUserRolePassword(), true);
		clickLinkWithText( username );
		clickLinkWithText( "Edit Roles" );
		checkUserRoleWithValue( fullname );
		clickButtonWithValue( "Submit" );
		
		clickLinkWithText("Logout");
		
		login( username, getUserRolePassword() );
		changePassword( getUserRolePassword(), getUserRoleNewPassword() );
		
		// this section will be removed if issue from redback after changing password will be fixed.
		getSelenium().goBack();
		waitPage();
		clickLinkWithText("Logout");
		//assertTextPresent("You are already logged in.");
		
		login(username, getUserRoleNewPassword());
		assertLeftNavMenuWithRole( fullname );
		clickLinkWithText("Logout");
		login( getAdminUsername(), getAdminPassword());
	}
	
        @Test( dependsOnMethods = { "testBasicAddDeleteUser" } )
	public void testUserWithContinuumGroupProjectDeveloperRole()
	{
		username = p.getProperty( "GROUPPROJECTDEVELOPER_USERNAME" );
		fullname = p.getProperty( "GROUPPROJECTDEVELOPER_FULLNAME" );
		
		createUser(username, fullname, getUserEmail(), getUserRolePassword(), true);
		clickLinkWithText( username );
		clickLinkWithText( "Edit Roles" );
		checkUserRoleWithValue( fullname );
		clickButtonWithValue( "Submit" );
		
		clickLinkWithText("Logout");
		
		login( username, getUserRolePassword() );
		changePassword( getUserRolePassword(), getUserRoleNewPassword() );
		
		// this section will be removed if issue from redback after changing password will be fixed.
		getSelenium().goBack();
		waitPage();
		clickLinkWithText("Logout");
		//assertTextPresent("You are already logged in.");
		
		login(username, getUserRoleNewPassword());
		assertLeftNavMenuWithRole( fullname );
		clickLinkWithText("Logout");
		login( getAdminUsername(), getAdminPassword());
	}
	
        @Test( dependsOnMethods = { "testBasicAddDeleteUser" } )
	public void testUserWithContinuumGroupProjectUserRole()
	{
		username = p.getProperty( "GROUPPROJECTUSER_USERNAME" );
		fullname = p.getProperty( "GROUPPROJECTUSER_FULLNAME" );
		
		createUser(username, fullname, getUserEmail(), getUserRolePassword(), true);
		clickLinkWithText( username );
		clickLinkWithText( "Edit Roles" );
		checkUserRoleWithValue( fullname );
		clickButtonWithValue( "Submit" );
		
		clickLinkWithText("Logout");
		
		login( username, getUserRolePassword() );
		changePassword( getUserRolePassword(), getUserRoleNewPassword() );
		
		// this section will be removed if issue from redback after changing password will be fixed.
		getSelenium().goBack();
		waitPage();
		clickLinkWithText("Logout");
		//assertTextPresent("You are already logged in.");
		
		login(username, getUserRoleNewPassword());
		assertLeftNavMenuWithRole( fullname );
		clickLinkWithText("Logout");
		login( getAdminUsername(), getAdminPassword());
	}
	
        @Test( dependsOnMethods = { "testBasicAddDeleteUser" } )
	public void testUserWithContinuumManageBuildEnvironmentRole()
	{
		username = p.getProperty( "MANAGEBUILDENVIRONMENT_USERNAME" );
		fullname = p.getProperty( "MANAGEBUILDENVIRONMENT_FULLNAME" );
		
		createUser(username, fullname, getUserEmail(), getUserRolePassword(), true);
		clickLinkWithText( username );
		clickLinkWithText( "Edit Roles" );
		checkUserRoleWithValue( fullname );
		clickButtonWithValue( "Submit" );
		
		clickLinkWithText("Logout");
		
		login( username, getUserRolePassword() );
		changePassword( getUserRolePassword(), getUserRoleNewPassword() );
		
		// this section will be removed if issue from redback after changing password will be fixed.
		getSelenium().goBack();
		waitPage();
		clickLinkWithText("Logout");
		//assertTextPresent("You are already logged in.");
		
		login(username, getUserRoleNewPassword());
		assertLeftNavMenuWithRole( fullname );
		clickLinkWithText("Logout");
		login( getAdminUsername(), getAdminPassword());
	}
	
        @Test( dependsOnMethods = { "testBasicAddDeleteUser" } )
	public void testUserWithContinuumManageBuildTemplatesRole()
	{
		username = p.getProperty( "MANAGEBUILDTEMPLATES_USERNAME" );
		fullname = p.getProperty( "MANAGEBUILDTEMPLATES_FULLNAME" );
		
		createUser(username, fullname, getUserEmail(), getUserRolePassword(), true);
		clickLinkWithText( username );
		clickLinkWithText( "Edit Roles" );
		checkUserRoleWithValue( fullname );
		clickButtonWithValue( "Submit" );
		
		clickLinkWithText("Logout");
		
		login( username, getUserRolePassword() );
		changePassword( getUserRolePassword(), getUserRoleNewPassword() );
		
		// this section will be removed if issue from redback after changing password will be fixed.
		getSelenium().goBack();
		waitPage();
		clickLinkWithText("Logout");
		//assertTextPresent("You are already logged in.");
		
		login(username, getUserRoleNewPassword());
		assertLeftNavMenuWithRole( fullname );
		clickLinkWithText("Logout");
		login( getAdminUsername(), getAdminPassword());
	}
	
        @Test( dependsOnMethods = { "testBasicAddDeleteUser" } )
	public void testUserWithContinuumManageInstallationsRole()
	{
		username = p.getProperty( "MANAGEINSTALLATIONS_USERNAME" );
		fullname = p.getProperty( "MANAGEINSTALLATIONS_FULLNAME" );
		
		createUser(username, fullname, getUserEmail(), getUserRolePassword(), true);
		clickLinkWithText( username );
		clickLinkWithText( "Edit Roles" );
		checkUserRoleWithValue( fullname );
		clickButtonWithValue( "Submit" );
		
		clickLinkWithText("Logout");
		
		login( username, getUserRolePassword() );
		changePassword( getUserRolePassword(), getUserRoleNewPassword() );
		
		// this section will be removed if issue from redback after changing password will be fixed.
		getSelenium().goBack();
		waitPage();
		clickLinkWithText("Logout");
		//assertTextPresent("You are already logged in.");
		
		login(username, getUserRoleNewPassword());
		assertLeftNavMenuWithRole( fullname );
		clickLinkWithText("Logout");
		login( getAdminUsername(), getAdminPassword());
	}
	
        @Test( dependsOnMethods = { "testBasicAddDeleteUser" } )
	public void testUserWithContinuumManageLocalRepoRole()
	{
		username = p.getProperty( "MANAGELOCALREPOS_USERNAME" );
		fullname = p.getProperty( "MANAGELOCALREPOS_FULLNAME" );
		
		createUser(username, fullname, getUserEmail(), getUserRolePassword(), true);
		clickLinkWithText( username );
		clickLinkWithText( "Edit Roles" );
		checkUserRoleWithValue( fullname );
		clickButtonWithValue( "Submit" );
		
		clickLinkWithText("Logout");
		
		login( username, getUserRolePassword() );
		changePassword( getUserRolePassword(), getUserRoleNewPassword() );
		
		// this section will be removed if issue from redback after changing password will be fixed.
		getSelenium().goBack();
		waitPage();
		clickLinkWithText("Logout");
		//assertTextPresent("You are already logged in.");
		
		login(username, getUserRoleNewPassword());
		assertLeftNavMenuWithRole( fullname );
		clickLinkWithText("Logout");
		login( getAdminUsername(), getAdminPassword());
	}
	
        @Test( dependsOnMethods = { "testBasicAddDeleteUser" } )
	public void testUserWithContinuumManagePurgingRole()
	{
		username = p.getProperty( "MANAGEPURGING_USERNAME" );
		fullname = p.getProperty( "MANAGEPURGING_FULLNAME" );
		
		createUser(username, fullname, getUserEmail(), getUserRolePassword(), true);
		clickLinkWithText( username );
		clickLinkWithText( "Edit Roles" );
		checkUserRoleWithValue( fullname );
		clickButtonWithValue( "Submit" );
		
		clickLinkWithText("Logout");
		
		login( username, getUserRolePassword() );
		changePassword( getUserRolePassword(), getUserRoleNewPassword() );
		
		// this section will be removed if issue from redback after changing password will be fixed.
		getSelenium().goBack();
		waitPage();
		clickLinkWithText("Logout");
		//assertTextPresent("You are already logged in.");
		
		login(username, getUserRoleNewPassword());
		assertLeftNavMenuWithRole( fullname );
		clickLinkWithText("Logout");
		login( getAdminUsername(), getAdminPassword());
	}
	
        @Test( dependsOnMethods = { "testBasicAddDeleteUser" } )
	public void testUserWithContinuumManageQueuesRole()
	{
		username = p.getProperty( "MANAGEQUEUES_USERNAME" );
		fullname = p.getProperty( "MANAGEQUEUES_FULLNAME" );
		
		createUser(username, fullname, getUserEmail(), getUserRolePassword(), true);
		clickLinkWithText( username );
		clickLinkWithText( "Edit Roles" );
		checkUserRoleWithValue( fullname );
		clickButtonWithValue( "Submit" );
		
		clickLinkWithText("Logout");
		
		login( username, getUserRolePassword() );
		changePassword( getUserRolePassword(), getUserRoleNewPassword() );
		
		// this section will be removed if issue from redback after changing password will be fixed.
		getSelenium().goBack();
		waitPage();
		clickLinkWithText("Logout");
		//assertTextPresent("You are already logged in.");
		
		login(username, getUserRoleNewPassword());
		assertLeftNavMenuWithRole( fullname );
		clickLinkWithText("Logout");
		login( getAdminUsername(), getAdminPassword());
	}
	
        @Test( dependsOnMethods = { "testBasicAddDeleteUser" } )
	public void testUserWithContinuumManageSchedulingRole()
	{
		username = p.getProperty( "MANAGESCHEDULING_USERNAME" );
		fullname = p.getProperty( "MANAGESCHEDULING_FULLNAME" );
		
		createUser(username, fullname, getUserEmail(), getUserRolePassword(), true);
		clickLinkWithText( username );
		clickLinkWithText( "Edit Roles" );
		checkUserRoleWithValue( fullname );
		clickButtonWithValue( "Submit" );
		
		clickLinkWithText("Logout");
		
		login( username, getUserRolePassword() );
		changePassword( getUserRolePassword(), getUserRoleNewPassword() );
		
		// this section will be removed if issue from redback after changing password will be fixed.
		getSelenium().goBack();
		waitPage();
		clickLinkWithText("Logout");
		//assertTextPresent("You are already logged in.");
		
		login(username, getUserRoleNewPassword());
		assertLeftNavMenuWithRole( fullname );
		clickLinkWithText("Logout");
		login( getAdminUsername(), getAdminPassword());
	}
	
        @Test( dependsOnMethods = { "testBasicAddDeleteUser" } )
	public void testUserWithProjectAdminDefaultProjectGroup()
	{
		username = p.getProperty( "PROJECTADMINISTRATOR_DEFAULTPROJECTGROUP_USERNAME" );
		fullname = p.getProperty( "PROJECTADMINISTRATOR_DEFAULTPROJECTGROUP_FULLNAME" );
		
		createUser(username, fullname, getUserEmail(), getUserRolePassword(), true);
		clickLinkWithText( username );
		clickLinkWithText( "Edit Roles" );
		checkResourceRoleWithValue( fullname );
		clickButtonWithValue( "Submit" );
		
		clickLinkWithText("Logout");
		
		login( username, getUserRolePassword() );
		changePassword( getUserRolePassword(), getUserRoleNewPassword() );
		
		// this section will be removed if issue from redback after changing password will be fixed.
		getSelenium().goBack();
		waitPage();
		clickLinkWithText("Logout");
		//assertTextPresent("You are already logged in.");
		
		login(username, getUserRoleNewPassword());
		assertLeftNavMenuWithRole( fullname );
		clickLinkWithText("Logout");
		login( getAdminUsername(), getAdminPassword());
	}
	
        @Test( dependsOnMethods = { "testBasicAddDeleteUser" } )
	public void testUserWithProjectDevDefaultProjectGroup()
	{
		username = p.getProperty( "PROJECTDEVELOPER_DEFAULTPROJECTGROUP_USERNAME" );
		fullname = p.getProperty( "PROJECTDEVELOPER_DEFAULTPROJECTGROUP_FULLNAME" );
		
		createUser(username, fullname, getUserEmail(), getUserRolePassword(), true);
		clickLinkWithText( username );
		clickLinkWithText( "Edit Roles" );
		checkResourceRoleWithValue( fullname );
		clickButtonWithValue( "Submit" );
		
		clickLinkWithText("Logout");
		
		login( username, getUserRolePassword() );
		changePassword( getUserRolePassword(), getUserRoleNewPassword() );
		
		// this section will be removed if issue from redback after changing password will be fixed.
		getSelenium().goBack();
		waitPage();
		clickLinkWithText("Logout");
		//assertTextPresent("You are already logged in.");
		
		login(username, getUserRoleNewPassword());
		assertLeftNavMenuWithRole( fullname );
		clickLinkWithText("Logout");
		login( getAdminUsername(), getAdminPassword());		
	}
	
        @Test( dependsOnMethods = { "testBasicAddDeleteUser" } )
	public void testUserWithProjectUserDefaultProjectGroup()
	{
		username = p.getProperty( "PROJECTUSER_DEFAULTPROJECTGROUP_USERNAME" );
		fullname = p.getProperty( "PROJECTUSER_DEFAULTPROJECTGROUP_FULLNAME" );
		
		createUser(username, fullname, getUserEmail(), getUserRolePassword(), true);
		clickLinkWithText( username );
		clickLinkWithText( "Edit Roles" );
		checkResourceRoleWithValue( fullname );
		clickButtonWithValue( "Submit" );
		
		clickLinkWithText("Logout");
		
		login( username, getUserRolePassword() );
		changePassword( getUserRolePassword(), getUserRoleNewPassword() );
		
		// this section will be removed if issue from redback after changing password will be fixed.
		getSelenium().goBack();
		waitPage();
		clickLinkWithText("Logout");
		//assertTextPresent("You are already logged in.");
		
		login(username, getUserRoleNewPassword());
		assertLeftNavMenuWithRole( fullname );
		clickLinkWithText("Logout");
		login( getAdminUsername(), getAdminPassword());
	}

}
