package org.apache.continuum.web.test.parent;

import java.io.File;

import org.apache.continuum.web.test.XPathExpressionUtil;

public abstract class AbstractUserRolesManagementTest
	extends AbstractContinuumTest
{
	protected String username;
	protected String fullname;

	public String getUserEmail()
	{
		String email = getProperty( "USERROLE_EMAIL" );
		return email;
	}

	public String getUserRolePassword()
	{
		String password = getProperty( "USERROLE_PASSWORD" );
		return password;
	}

	public String getUserRoleNewPassword()
	{
		String password_new = getProperty( "NEW_USERROLE_PASSWORD" );
		return password_new;
	}

	public String getBasedir()
    {
        String basedir = System.getProperty( "basedir" );

        if ( basedir == null )
        {
            basedir = new File( "" ).getAbsolutePath();
        }

        return basedir;
    }

	public String getAdminUsername()
	{
		String adminUsername = getProperty( "ADMIN_USERNAME" );
		return adminUsername;
	}

	public String getAdminPassword()
	{
		String adminPassword = getProperty( "ADMIN_PASSWORD" );
		return adminPassword;
	}

	////////////////////////////
	// Assertions
	////////////////////////////
	public void assertCreateUserPage()
	{
		assertPage( "[Admin] User Create" );
		assertTextPresent( "[Admin] User Create" );
		assertTextPresent( "Username*:" );
		assertElementPresent( "user.username" );
		assertTextPresent( "Full Name*:");
		assertElementPresent( "user.fullName" );
		assertTextPresent( "Email Address*:" );
		assertElementPresent( "user.email" );
		assertTextPresent( "Password*:" );
		assertElementPresent( "user.password" );
		assertTextPresent( "Confirm Password*:" );
		assertElementPresent( "user.confirmPassword" );
		assertButtonWithValuePresent( "Create User" );
	}

	public void assertUserRolesPage()
	{
		assertPage( "[Admin] User Edit" );
		assertTextPresent( "[Admin] User Roles" );
		String userRoles = "Username,Full Name,Email,Guest,Registered User,System Administrator,User Administrator,Continuum Group Project Administrator,Continuum Group Project Developer,Continuum Group Project User,Continuum Manage Build Environments,Continuum Manage Build Templates,Continuum Manage Installations,Continuum Manage Local Repositories,Continuum Manage Purging,Continuum Manage Queues,Continuum Manage Scheduling,Project Administrator,Project Developer,Project User,Default Project Group";
		String[] arrayUserRoles = userRoles.split( "," );
			for ( String userroles : arrayUserRoles )
				assertTextPresent( userroles );
	}

	   public void assertCreatedUserInfo( String username )
    {
        selectValue( "name=ec_rd", "50" );
        waitPage();
        clickLinkWithText( username );
        clickLinkWithText( "Edit Roles" );
    }

	public void assertUserRoleCheckBoxPresent( String value )
    {
    	getSelenium().isElementPresent( "xpath=//input[@id='addRolesToUser_addNDSelectedRoles' and @name='addNDSelectedRoles' and @value='"+ value + "']" );
    }

    public void assertResourceRolesCheckBoxPresent( String value )
    {
    	getSelenium().isElementPresent( "xpath=//input[@name='addDSelectedRoles' and @value='" + value + "']" );
    }

    public void checkUserRoleWithValue( String value )
    {
    	assertUserRoleCheckBoxPresent( value );
    	getSelenium().click( "xpath=//input[@id='addRolesToUser_addNDSelectedRoles' and @name='addNDSelectedRoles' and @value='"+ value + "']" );
    }

    public void checkResourceRoleWithValue( String value )
    {
    	assertResourceRolesCheckBoxPresent( value );
    	getSelenium().click( "xpath=//input[@name='addDSelectedRoles' and @value='" + value + "']" );
    }

	public void assertLeftNavMenuWithRole( String role )
	{
		if( role == "System Administrator" )
    	{
    		String navMenu = "About,Show Project Groups,Maven 2.0.x Project,Maven 1.x Project,Ant Project,Shell Project,Local Repositories,Purge Configurations,Schedules,Installations,Build Environments,Queues,Build Definition Templates,Configuration,Appearance,Users,Roles,Build Queue";
    		String[] arrayNavMenu = navMenu.split( "," );
    		for( String navmenu : arrayNavMenu )
    			assertLinkPresent( navmenu );
    	}
    	else if( role == "User Administrator" )
    	{
    		String navMenu = "About,Show Project Groups,Users,Roles";
    		String[] arrayNavMenu = navMenu.split( "," );
    		for( String navmenu : arrayNavMenu )
    			assertLinkPresent( navmenu );
    	}
    	else if( role == "Continuum Group Project Administrator" )
    	{
    		String navMenu = "About,Show Project Groups,Maven 2.0.x Project,Maven 1.x Project,Ant Project,Shell Project,Schedules,Queues,Users,Roles";
    		String[] arrayNavMenu = navMenu.split( "," );
    		for( String navmenu : arrayNavMenu )
    			assertLinkPresent( navmenu );
    	}
    	else if( role == "Continuum Group Project Developer" )
    	{
    		String navMenu = "About,Show Project Groups,Queues";
    		String[] arrayNavMenu = navMenu.split( "," );
    		for( String navmenu : arrayNavMenu )
    			assertLinkPresent( navmenu );
    	}
    	else if( role == "Continuum Group Project User" )
    	{
    		String navMenu = "About,Show Project Groups,Queues";
    		String[] arrayNavMenu = navMenu.split( "," );
    		for( String navmenu : arrayNavMenu )
    			assertLinkPresent( navmenu );
    	}
    	else if( role == "Continuum Manage Build Environments" )
    	{
    		String navMenu = "About,Show Project Groups,Build Environments";
    		String[] arrayNavMenu = navMenu.split( "," );
    		for( String navmenu : arrayNavMenu )
    			assertLinkPresent( navmenu );
    	}
    	else if( role == "Continuum Manage Build Templates" )
    	{
    		String navMenu = "About,Show Project Groups,Build Definition Templates";
    		String[] arrayNavMenu = navMenu.split( "," );
    		for( String navmenu : arrayNavMenu )
    			assertLinkPresent( navmenu );
    	}
    	else if( role == "Continuum Manage Installations" )
    	{
    		String navMenu = "About,Show Project Groups,Installations";
    		String[] arrayNavMenu = navMenu.split( "," );
    		for( String navmenu : arrayNavMenu )
    			assertLinkPresent( navmenu );
    	}
    	else if( role == "Continuum Manage Local Repositories" )
    	{
    		String navMenu = "About,Show Project Groups,Local Repositories";
    		String[] arrayNavMenu = navMenu.split( "," );
    		for( String navmenu : arrayNavMenu )
    			assertLinkPresent( navmenu );
    	}
    	else if( role == "Continuum Manage Purging" )
    	{
    		String navMenu = "About,Show Project Groups,Purge Configurations";
    		String[] arrayNavMenu = navMenu.split( "," );
    		for( String navmenu : arrayNavMenu )
    			assertLinkPresent( navmenu );
    	}
    	else if( role == "Continuum Manage Queues" )
    	{
    		String navMenu = "About,Show Project Groups,Queues";
    		String[] arrayNavMenu = navMenu.split( "," );
    		for( String navmenu : arrayNavMenu )
    			assertLinkPresent( navmenu );
    	}
    	else if( role == "Continuum Manage Scheduling" )
    	{
    		String navMenu = "About,Show Project Groups,Schedules";
    		String[] arrayNavMenu = navMenu.split( "," );
    		for( String navmenu : arrayNavMenu )
    			assertLinkPresent( navmenu );
    	}
    	else if( role == "Project Administrator - Default Project Group" )
    	{
    		String navMenu = "About,Show Project Groups,Queues,Users,Roles";
    		String[] arrayNavMenu = navMenu.split( "," );
    		for( String navmenu : arrayNavMenu )
    			assertLinkPresent( navmenu );
    	}
    	else if( role == "Project Developer - Default Project Group" || role == "Project User - Default Project Group" )
    	{
    		String navMenu = "About,Show Project Groups,Queues";
    		String[] arrayNavMenu = navMenu.split( "," );
    		for( String navmenu : arrayNavMenu )
    			assertLinkPresent( navmenu );
    	}
    	else
    	{
    		String navMenu = "About,Show Project Groups";
    		String[] arrayNavMenu = navMenu.split( "," );
    		for( String navmenu : arrayNavMenu )
    			assertLinkPresent( navmenu );
    		assertTextPresent( "Project Groups" );
    		//assertTextPresent( "Project Groups list is empty." );
    	}

	}

    public void assertDeleteUserPage( String username )
    {
        assertPage( "[Admin] User Delete" ); //TODO
        assertTextPresent( "[Admin] User Delete" );
        assertTextPresent( "The following user will be deleted:" );
        assertTextPresent( "Username: " + username );
        assertButtonWithValuePresent( "Delete User" );
    }

	public void assertProjectAdministratorAccess()
    {
        assertLinkPresent( "About" );
        assertLinkPresent( "Show Project Groups" );
        assertLinkPresent( "Maven 2.0.x Project" );
        assertLinkPresent( "Maven 1.x Project" );
        assertLinkPresent( "Ant Project" );
        assertLinkPresent( "Shell Project" );
        assertLinkPresent( "Schedules" );
        assertLinkPresent( "Queues" );
        assertLinkPresent( "Users" );
        assertLinkPresent( "Roles" );
        assertLinkNotPresent( "Local Repositories" );
        assertLinkNotPresent( "Purge Configurations" );
        assertLinkNotPresent( "Installations" );
        assertLinkNotPresent( "Build Environments" );
        assertLinkNotPresent( "Build Definition Templates" );
        assertLinkNotPresent( "Configuration" );
        assertLinkNotPresent( "Appearance" );
        assertLinkNotPresent( "Build Queue" );
        assertLinkNotPresent( "Build Agent" );
    }

	/////////////////////////////////////////
	// User Roles Management
	/////////////////////////////////////////
    public void changePassword( String oldPassword, String newPassword )
	{
		assertPage( "Change Password" );
		setFieldValue( "existingPassword", oldPassword );
		setFieldValue( "newPassword", newPassword );
		setFieldValue( "newPasswordConfirm", newPassword );
		clickButtonWithValue( "Change Password" );
	}

	public void createUser( String userName, String fullName, String email, String password, boolean valid )
	{
		createUser( userName, fullName, email, password, password, valid );
	}

	private void createUser( String userName, String fullName, String emailAd, String password, String confirmPassword,
                             boolean valid )
	{
		login( getAdminUsername() , getAdminPassword() );
		clickLinkWithText( "Users" );
		clickButtonWithValue( "Create New User" );
		assertCreateUserPage();
        setFieldValue( "user.username", userName );
        setFieldValue( "user.fullName", fullName );
        setFieldValue( "user.email", emailAd );
        setFieldValue( "user.password", password );
        setFieldValue( "user.confirmPassword", confirmPassword );
        submit();

        assertUserRolesPage( );
        clickButtonWithValue( "Submit" );

        /*if (valid )
        {
        	String[] columnValues = {userName, fullName, emailAd};
            assertElementPresent( XPathExpressionUtil.getTableRow( columnValues ) );
        }
        else
        {
            assertCreateUserPage();
        }*/
	}


	public void login( String username, String password )
	{
	    login( username, password, true, "Login Page" );
	}

    public void login( String username, String password, boolean valid, String assertReturnPage )
	{
        if ( isLinkPresent( "Login" ) )
	    {
            goToLoginPage();

            submitLoginPage( username, password, false, valid, assertReturnPage );
	    }
    }

    public void submitLoginPage( String username, String password )
    {
        submitLoginPage( username, password, false, true, "Login Page" );
    }

    public void submitLoginPage( String username, String password, boolean validUsernamePassword )
    {
        submitLoginPage( username, password, false, validUsernamePassword, "Login Page" );
    }

    public void submitLoginPage( String username, String password, boolean rememberMe, boolean validUsernamePassword,
                                 String assertReturnPage )
    {
        assertLoginPage();
        setFieldValue( "username", username );
        setFieldValue( "password", password );
        if ( rememberMe )
        {
            checkField( "rememberMe" );
        }
        clickButtonWithValue( "Login" );

        if ( validUsernamePassword )
        {
            assertTextPresent( "Current User:" );
            assertTextPresent( username );
            assertLinkPresent( "Edit Details" );
            assertLinkPresent( "Logout" );
        }
        else
        {
            if ( "Login Page".equals( assertReturnPage ) )
            {
                assertLoginPage();
            }
            else
            {
                assertPage( assertReturnPage );
            }
        }
    }

	public void deleteUser( String userName, String fullName, String emailAdd )
    {
        deleteUser( userName, fullName, emailAdd, false, false );
    }

    public void deleteUser( String userName, String fullName, String emailAd, boolean validated, boolean locked )
    {
	    //clickLinkWithText( "userlist" );
        clickLinkWithXPath( "//table[@id='ec_table']/tbody[2]/tr[3]/td[7]/a/img" );
        assertDeleteUserPage( userName );
        submit();
        assertElementNotPresent( userName );
    }
}
