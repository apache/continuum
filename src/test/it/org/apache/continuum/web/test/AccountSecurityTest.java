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


public class AccountSecurityTest
    extends AbstractAuthenticatedAccessTestCase
{
    public final String SIMPLE_POM = getBasedir() + "/target/test-classes/unit/simple-project/pom.xml";

    // create user fields
    public static final String CREATE_FORM_USERNAME_FIELD = "userCreateForm_user_username";

    public static final String CREATE_FORM_FULLNAME_FIELD = "userCreateForm_user_fullName";

    public static final String CREATE_FORM_EMAILADD_FIELD = "userCreateForm_user_email";

    public static final String CREATE_FORM_PASSWORD_FIELD = "userCreateForm_user_password";

    public static final String CREATE_FORM_CONFIRM_PASSWORD_FIELD = "userCreateForm_user_confirmPassword";

    public static final String PASSWORD_FIELD = "user.password";

    public static final String CONFIRM_PASSWORD_FIELD = "user.confirmPassword";

    public static final String DUMMY_USER = "user_dummy";
    
    public static final String GUEST_USER = "new_guest";
    
    public static final String REGISTERED_USER = "reguser";

    public static final String SYSTEM_ADMINISTRATOR = "sysad";
    
    public static final String USER_ADMINISTRATOR = "useradmin";
    
    public static final String GROUP_PROJECT_ADMIN = "projadmingroup";
    
    public static final String GROUP_PROJECT_DEVELOPER = "projdevgroup";
    
    public static final String GROUP_PROJECT_USER = "projusergroup";
    
    public static final String MANAGE_BUILD_ENV = "managebuildenv";
    
    public static final String MANAGE_BUILD_TEMPLATES = "managebuildtemp";
    
    public static final String INSTALLATION = "manageinstallation";
    
    public static final String LOCAL_REPOSITORIES = "managelocalrepo";
    
    public static final String PURGING = "managepurging";
    
    public static final String QUEUES = "managequeues";
    
    public static final String SCHEDULING = "scheduling";
    
    public static final String PROJECT_ADMIN = "projectadmin";
    
    public static final String PROJECT_DEV = "projectdev";
    
    public static final String PROJECT_USER = "projectuser";
    
    public static final String CUSTOM_FULLNAME = "custom fullname";

    public static final String CUSTOM_EMAILADD = "custom@custom.com";

    public static final String CUSTOM_PASSWORD = "custompassword1";

    public static final String CUSTOM_PASSWORD1 = "user1234";
    
    public String getUsername()
    {
        return super.adminUsername;
    }

    public String getPassword()
    {
        return super.adminPassword;
    }

    public void testBasicUserAddDelete()
    {
        createUser( GUEST_USER, CUSTOM_FULLNAME, CUSTOM_EMAILADD, CUSTOM_PASSWORD, true );

        // delete custom user
        deleteUser( GUEST_USER, CUSTOM_FULLNAME, CUSTOM_EMAILADD );
    }
    
    public void testPasswordConfirmation()
    	throws Exception
    {
    	// initial user account creation ignores the password creation checks
    	createUser( USER_ADMINISTRATOR, CUSTOM_FULLNAME, CUSTOM_EMAILADD, CUSTOM_PASSWORD, true );
    	logout();

    	// start password creation validation test
    	login( USER_ADMINISTRATOR, CUSTOM_PASSWORD );

    	// Edit user informations
    	goToMyAccount();

    	//TODO: verify account details page
    	assertPage( "Change Password" );

	    // test password confirmation
	    setFieldValue( "existingPassword" , CUSTOM_PASSWORD );
	    setFieldValue( "newPassword", CUSTOM_PASSWORD );
	    setFieldValue( "newPasswordConfirm", CUSTOM_PASSWORD + "error" );
	    submit();
	    
	    // we should still be in Account Details
	    assertPage( "Change Password" );
	    isTextPresent( "Password confirmation failed. Passwords do not match." );
	
	    logout();
	
	    // house keeping
	    login( getUsername(), getPassword() );
	    deleteUser( USER_ADMINISTRATOR, CUSTOM_FULLNAME, CUSTOM_EMAILADD );
	    logout();
	}
    
    
    public void testTenStrikeRule()
    	throws Exception
    {
    	createUser( GROUP_PROJECT_ADMIN, CUSTOM_FULLNAME, CUSTOM_EMAILADD, CUSTOM_PASSWORD, true );
    	logout();

    	login( GROUP_PROJECT_ADMIN, CUSTOM_PASSWORD );

	assertPage( "Change Password" );
    	setFieldValue( "existingPassword" , CUSTOM_PASSWORD );
	setFieldValue( "newPassword", CUSTOM_PASSWORD1 );
	setFieldValue( "newPasswordConfirm", CUSTOM_PASSWORD1 );
	clickButtonWithValue( "Change Password" );
	logout();
    
    	int numberOfTries = 10;

    	for ( int nIndex = 0; nIndex < numberOfTries; nIndex++ )
    	{
    		if ( nIndex < 9 )
    		{
    			login( GROUP_PROJECT_ADMIN, CUSTOM_PASSWORD, false, "Login Page" );
    			// login should fail
    			assertTextPresent( "You have entered an incorrect username and/or password." );
    			assertFalse( "user is authenticated using wrong password", isAuthenticated() );
    		}
    		else
    		{
    			// on the 10nth try, account is locked and we are returned to the Group Summary Page
    			login( GROUP_PROJECT_ADMIN, CUSTOM_PASSWORD, false, "Continuum - Group Summary" );
    			assertTextPresent( "Account Locked" );
    		}
    	}

    	// house keeping
    	login( getUsername(), getPassword() );
    	deleteUser( GROUP_PROJECT_ADMIN, CUSTOM_FULLNAME, CUSTOM_EMAILADD, false, true );
    	logout();
	}

    public void testDefaultRolesOfNewSystemAdministrator()
    	throws Exception
    {
	    // initialize
	    createUser( SYSTEM_ADMINISTRATOR, CUSTOM_FULLNAME, CUSTOM_EMAILADD, CUSTOM_PASSWORD, true );
	
	    // upgrade the role of the user to system administrator
	    assertUsersListPage();
	    clickLinkWithText( SYSTEM_ADMINISTRATOR );
	    clickLinkWithText( "Edit Roles" );
	    checkUserRoleWithValue( "System Administrator" );
	    submit();
	
	    // after adding roles, we are returned to the list of users
	    //TODO: check Permanent/validated/locked columns
	    clickLinkWithText( SYSTEM_ADMINISTRATOR );
	
	    assertPage( "[Admin] User Edit" );
	    // verify roles
	    String sysadRoles = "Continuum Group Project Administrator,Continuum Group Project Developer,Continuum Group Project User,Continuum Manage Build Environments,Continuum Manage Build Templates,Continuum Manage Installations,Continuum Manage Local Repositories,Continuum Manage Purging,Continuum Manage Queues,Continuum Manage Scheduling,Project Administrator - Default Project Group,Project Developer - Default Project Group,Project User - Default Project Group,System Administrator,User Administrator";
	    String[] arraySysad = sysadRoles.split( "," );
	    for( String sysadroles : arraySysad )
	    	assertTextPresent( sysadroles );
	    logout();
	    
	    login( getUsername(), getPassword() );
	    deleteUser( SYSTEM_ADMINISTRATOR, CUSTOM_FULLNAME, CUSTOM_EMAILADD );
	    logout();
	}
   
    public void testDefaultRolesOfUserAdmin() 
    {
    	createUser( USER_ADMINISTRATOR, CUSTOM_FULLNAME, CUSTOM_EMAILADD, CUSTOM_PASSWORD, true);
    	assertUsersListPage();
    	clickLinkWithText( USER_ADMINISTRATOR );
    	clickLinkWithText( "Edit Roles" );
    	checkUserRoleWithValue( "User Administrator" );
    	submit();
    	clickLinkWithText( USER_ADMINISTRATOR );
    	
    	assertPage( "[Admin] User Edit" );
    	assertTextPresent( "User Administrator" );
    	
    	logout();
    	
    	login(USER_ADMINISTRATOR, CUSTOM_PASSWORD);
    	changePassword();
	    assertPagesWithUserRoles( "User Administrator" );
    	logout();
    	
	    login( getUsername(), getPassword() );
    	deleteUser( USER_ADMINISTRATOR, CUSTOM_FULLNAME, CUSTOM_EMAILADD );
    	logout();
    }
    
    public void testDefaultRolesOfRegisteredUser()
    {
    	createUser( REGISTERED_USER, CUSTOM_FULLNAME, CUSTOM_EMAILADD, CUSTOM_PASSWORD, true);
    	assertUsersListPage();
    	clickLinkWithText( REGISTERED_USER );
    	clickLinkWithText( "Edit Roles" );
    	checkUserRoleWithValue( "Registered User" );
    	submit();
    	//check registered user available roles if correct
    	clickLinkWithText( REGISTERED_USER );
    	assertPage( "[Admin] User Edit" );
    	assertTextPresent( "Registered User" );
    	logout();
    	
    	//check registered user's access to continuum page
    	login( REGISTERED_USER, CUSTOM_PASSWORD );
    	changePassword();
	    assertPagesWithUserRoles( "Registered User" );
	    logout();
    	
	    //house keeping
	    login( getUsername(), getPassword() );
    	deleteUser( REGISTERED_USER, CUSTOM_FULLNAME, CUSTOM_EMAILADD );
    	logout();
    }
    
    
    public void testDefaultRolesOfGuestUser()
    {
    	createUser( GUEST_USER, CUSTOM_FULLNAME, CUSTOM_EMAILADD, CUSTOM_PASSWORD, true);
    	assertUsersListPage();
    	clickLinkWithText( GUEST_USER );
    	clickLinkWithText( "Edit Roles" );
    	checkUserRoleWithValue( "Guest" );
    	submit();
    	//check guest user's available roles if correct 
    	clickLinkWithText( GUEST_USER );
    	assertPage( "[Admin] User Edit" );
    	assertTextPresent( "Guest" );
    	logout();
    	
    	//check access to continuum page
    	login( GUEST_USER, CUSTOM_PASSWORD );
    	changePassword();
	    assertPagesWithUserRoles( "Guest" );
	    logout();
	    
	    login( getUsername(), getPassword() );
    	deleteUser( GUEST_USER, CUSTOM_FULLNAME, CUSTOM_EMAILADD );
    	logout();
    }
    
    
    public void testDefaultRolesOfProjectGroupAdmin()
    {
    	createUser( GROUP_PROJECT_ADMIN, CUSTOM_FULLNAME, CUSTOM_EMAILADD, CUSTOM_PASSWORD, true);
    	assertUsersListPage();
    	clickLinkWithText( GROUP_PROJECT_ADMIN );
    	clickLinkWithText( "Edit Roles" );
    	checkUserRoleWithValue( "Continuum Group Project Administrator" );
    	submit();
    	//check project group admin's available roles if correct
    	clickLinkWithText( GROUP_PROJECT_ADMIN );
    	assertPage( "[Admin] User Edit" );
    	String userProjectGroupAdmin = "Continuum Group Project Administrator,Continuum Group Project Developer,Continuum Group Project User,Project Administrator - Default Project Group,Project Developer - Default Project Group,Project User - Default Project Group";
    	String[] arrayProjectGroupAdmin = userProjectGroupAdmin.split( "," );
    	for( String projectgroupadmin : arrayProjectGroupAdmin )
    		assertTextPresent( projectgroupadmin );
    	logout();
    	//check access to continuum page
    	login( GROUP_PROJECT_ADMIN, CUSTOM_PASSWORD );
    	changePassword();
	    assertPagesWithUserRoles( "Continuum Group Project Administrator" );
	    logout();
	    
	    login( getUsername(), getPassword() );
    	deleteUser( GROUP_PROJECT_ADMIN, CUSTOM_FULLNAME, CUSTOM_EMAILADD );
    	logout();
    }
    
    
    public void testDefaultRolesOfProjectGroupDev()
    {
    	createUser( GROUP_PROJECT_DEVELOPER, CUSTOM_FULLNAME, CUSTOM_EMAILADD, CUSTOM_PASSWORD, true);
    	assertUsersListPage();
    	clickLinkWithText( GROUP_PROJECT_DEVELOPER );
    	clickLinkWithText( "Edit Roles" );
    	checkUserRoleWithValue( "Continuum Group Project Developer" );
    	submit();
    	//check available roles if correct
    	clickLinkWithText( GROUP_PROJECT_DEVELOPER );
    	assertPage( "[Admin] User Edit" );
    	String userProjectGroupDev = "Continuum Group Project Developer,Continuum Group Project User,Project Developer - Default Project Group,Project User - Default Project Group";
    	String[] arrayProjectGroupDev = userProjectGroupDev.split( "," );
    	for( String projectgroupdev : arrayProjectGroupDev )
    		assertTextPresent( projectgroupdev );
    	logout();
    	//check access to continuum page
    	login( GROUP_PROJECT_DEVELOPER, CUSTOM_PASSWORD );
    	changePassword();
	    assertPagesWithUserRoles( "Continuum Group Project Developer" );
	    logout();
    	
	    login( getUsername(), getPassword() );
    	deleteUser( GROUP_PROJECT_DEVELOPER, CUSTOM_FULLNAME, CUSTOM_EMAILADD );
    	logout();
    }
   
    public void testDefaultRolesOfProjectGroupUser()
    {
    	createUser( GROUP_PROJECT_USER, CUSTOM_FULLNAME, CUSTOM_EMAILADD, CUSTOM_PASSWORD, true);
    	assertUsersListPage();
    	clickLinkWithText( GROUP_PROJECT_USER );
    	clickLinkWithText( "Edit Roles" );
    	checkUserRoleWithValue( "Continuum Group Project User" );
    	submit();
    	//check available roles if correct
    	clickLinkWithText( GROUP_PROJECT_USER );
    	assertPage( "[Admin] User Edit" );
    	assertTextPresent( "Continuum Group Project User" );
    	assertTextPresent( "Project User - Default Project Group" );
    	logout();
    	//check access to continuum page
    	login( GROUP_PROJECT_USER, CUSTOM_PASSWORD );
    	changePassword();
	    assertPagesWithUserRoles( "Continuum Group Project Developer" );
	    logout();
    	
	    login( getUsername(), getPassword() );
    	
    	deleteUser( GROUP_PROJECT_USER, CUSTOM_FULLNAME, CUSTOM_EMAILADD );
    	logout();
    }
    
    public void testDefaultRolesOfBuildEnvironments() 
    {
    	createUser( MANAGE_BUILD_ENV, CUSTOM_FULLNAME, CUSTOM_EMAILADD, CUSTOM_PASSWORD, true);
    	assertUsersListPage();
    	clickLinkWithText( MANAGE_BUILD_ENV );
    	clickLinkWithText( "Edit Roles" );
    	checkUserRoleWithValue( "Continuum Manage Build Environments" );
    	submit();
    	
    	clickLinkWithText( MANAGE_BUILD_ENV );
    	assertPage( "[Admin] User Edit" );
    	assertTextPresent( "Continuum Manage Build Environments" );
    	logout();
    	
    	//check access to continuum page
    	login( MANAGE_BUILD_ENV, CUSTOM_PASSWORD );
    	changePassword();
	    assertPagesWithUserRoles( "Continuum Manage Build Environments" );
	    logout();
    	
	    login( getUsername(), getPassword() );
    	deleteUser( MANAGE_BUILD_ENV, CUSTOM_FULLNAME, CUSTOM_EMAILADD );
    	logout();
    }
    
    public void testDefaultRolesOfBuildTemp() 
    {
    	createUser( MANAGE_BUILD_TEMPLATES, CUSTOM_FULLNAME, CUSTOM_EMAILADD, CUSTOM_PASSWORD, true);
    	assertUsersListPage();
    	clickLinkWithText( MANAGE_BUILD_TEMPLATES );
    	clickLinkWithText( "Edit Roles" );
    	checkUserRoleWithValue( "Continuum Manage Build Templates" );
    	submit();
    	
    	clickLinkWithText( MANAGE_BUILD_TEMPLATES );
    	assertPage( "[Admin] User Edit" );
    	assertTextPresent( "Continuum Manage Build Templates" );
    	logout();
    	
    	//check access to continuum page
    	login( MANAGE_BUILD_TEMPLATES, CUSTOM_PASSWORD );
    	changePassword();
	    assertPagesWithUserRoles( "Continuum Manage Build Templates" );
	    logout();
    	
	    login( getUsername(), getPassword() );
    	deleteUser( MANAGE_BUILD_TEMPLATES, CUSTOM_FULLNAME, CUSTOM_EMAILADD );
    	logout();
    }
    
    public void testDefaultRolesOfInstallations() 
    {
    	createUser( INSTALLATION, CUSTOM_FULLNAME, CUSTOM_EMAILADD, CUSTOM_PASSWORD, true);
    	assertUsersListPage();
    	clickLinkWithText( INSTALLATION );
    	clickLinkWithText( "Edit Roles" );
    	checkUserRoleWithValue( "Continuum Manage Installations" );
    	submit();
    	
    	clickLinkWithText( INSTALLATION );
    	assertPage( "[Admin] User Edit" );
    	assertTextPresent( "Continuum Manage Installations" );
    	logout();
    	
    	//check access to continuum page
    	login( INSTALLATION, CUSTOM_PASSWORD );
    	changePassword();
	    assertPagesWithUserRoles( "Continuum Manage Installations" );
	    logout();
    	
	    login( getUsername(), getPassword() );
    	deleteUser( INSTALLATION, CUSTOM_FULLNAME, CUSTOM_EMAILADD );
    	logout();
    }
    
    public void testDefaultRolesOfLocalRepositories()
    {
    	createUser( LOCAL_REPOSITORIES, CUSTOM_FULLNAME, CUSTOM_EMAILADD, CUSTOM_PASSWORD, true);
    	assertUsersListPage();
    	clickLinkWithText( LOCAL_REPOSITORIES );
    	clickLinkWithText( "Edit Roles" );
    	checkUserRoleWithValue( "Continuum Manage Local Repositories" );
    	submit();
    	
    	clickLinkWithText( LOCAL_REPOSITORIES );
    	assertPage( "[Admin] User Edit" );
    	assertTextPresent( "Continuum Manage Local Repositories" );
    	logout();
    	
    	//check access to continuum page
    	login( LOCAL_REPOSITORIES, CUSTOM_PASSWORD );
    	changePassword();
	    assertPagesWithUserRoles( "Continuum Manage Local Repositories" );
	    logout();
    	
	    login( getUsername(), getPassword() );
    	deleteUser( LOCAL_REPOSITORIES, CUSTOM_FULLNAME, CUSTOM_EMAILADD );
    	logout();
    }
    
    public void testDefaultRolesOfPurging()
    {
    	createUser( PURGING, CUSTOM_FULLNAME, CUSTOM_EMAILADD, CUSTOM_PASSWORD, true);
    	assertUsersListPage();
    	clickLinkWithText( PURGING );
    	clickLinkWithText( "Edit Roles" );
    	checkUserRoleWithValue( "Continuum Manage Purging" );
    	submit();
    	
    	clickLinkWithText( PURGING );
    	assertPage( "[Admin] User Edit" );
    	assertTextPresent( "Continuum Manage Purging" );
    	logout();
    	//check access to continuum page
    	login( PURGING, CUSTOM_PASSWORD );
    	changePassword();
	    assertPagesWithUserRoles( "Continuum Manage Purging" );
	    logout();
    	
	    login( getUsername(), getPassword() );	
    	deleteUser( PURGING, CUSTOM_FULLNAME, CUSTOM_EMAILADD );
    	logout();
    }
    
    public void testDefaultRolesOfQueues()
    {
    	createUser( QUEUES, CUSTOM_FULLNAME, CUSTOM_EMAILADD, CUSTOM_PASSWORD, true);
    	assertUsersListPage();
    	clickLinkWithText( QUEUES );
    	clickLinkWithText( "Edit Roles" );
    	checkUserRoleWithValue( "Continuum Manage Queues" );
    	submit();
    	
    	clickLinkWithText( QUEUES );
    	assertPage( "[Admin] User Edit" );
    	assertTextPresent( "Continuum Manage Queues" );
    	logout();
    	
    	//check access to continuum page
    	login( QUEUES, CUSTOM_PASSWORD );
    	changePassword();
	    assertPagesWithUserRoles( "Continuum Manage Queues" );
	    logout();
	    
	    login( getUsername(), getPassword() );
    	deleteUser( QUEUES, CUSTOM_FULLNAME, CUSTOM_EMAILADD );
    	logout();
    }
    
    public void testDefaultRolesOfScheduling()
    {
    	createUser( SCHEDULING, CUSTOM_FULLNAME, CUSTOM_EMAILADD, CUSTOM_PASSWORD, true);
    	assertUsersListPage();
    	clickLinkWithText( SCHEDULING );
    	clickLinkWithText( "Edit Roles" );
    	checkUserRoleWithValue( "Continuum Manage Scheduling" );
    	submit();
    	
    	clickLinkWithText( SCHEDULING );
    	assertPage( "[Admin] User Edit" );
    	assertTextPresent( "Continuum Manage Scheduling" );
    	logout(); 	
    	//check access to continuum page
    	login( SCHEDULING, CUSTOM_PASSWORD );
    	changePassword();
	    assertPagesWithUserRoles( "Continuum Manage Scheduling" );
	    logout();
	    
	    login( getUsername(), getPassword() );
    	deleteUser( SCHEDULING, CUSTOM_FULLNAME, CUSTOM_EMAILADD );
    	logout();
    }
    
    public void testDefaultRolesOfProjectAdmin() 
    {
    	createUser( PROJECT_ADMIN, CUSTOM_FULLNAME, CUSTOM_EMAILADD, CUSTOM_PASSWORD, true);
    	assertUsersListPage();
    	clickLinkWithText( PROJECT_ADMIN );
    	clickLinkWithText( "Edit Roles" );
    	checkResourceRoleWithValue( "Project Administrator - Default Project Group" );
    	submit();
    	
    	clickLinkWithText( PROJECT_ADMIN );
    	assertPage( "[Admin] User Edit" );
    	assertTextPresent( "Project Administrator - Default Project Group" );
    	assertTextPresent( "Project Developer - Default Project Group" );
    	assertTextPresent( "Project User - Default Project Group" );
    	logout();
    	//check access to continuum page
    	login( PROJECT_ADMIN, CUSTOM_PASSWORD );
    	changePassword();
	    assertPagesWithUserRoles( "Project Administrator - Default Project Group" );
	    logout();
	    
	    login( getUsername(), getPassword() );
    	deleteUser( PROJECT_ADMIN, CUSTOM_FULLNAME, CUSTOM_EMAILADD );
    	logout();
    }
    
    public void testDefaultRolesOfProjectDev() 
    {
    	createUser( PROJECT_DEV, CUSTOM_FULLNAME, CUSTOM_EMAILADD, CUSTOM_PASSWORD, true);
    	assertUsersListPage();
    	clickLinkWithText( PROJECT_DEV );
    	clickLinkWithText( "Edit Roles" );
    	checkResourceRoleWithValue( "Project Developer - Default Project Group" );
    	submit();
    	
    	clickLinkWithText( PROJECT_DEV );
    	assertPage( "[Admin] User Edit" );
    	assertTextPresent( "Project Developer - Default Project Group" );
    	assertTextPresent( "Project User - Default Project Group" );
    	logout();
    	//check access to continuum page
    	login( PROJECT_DEV, CUSTOM_PASSWORD );
    	changePassword();
	    assertPagesWithUserRoles( "Project Developer - Default Project Group" );
	    logout();
	    
	    login( getUsername(), getPassword() );
    	deleteUser( PROJECT_DEV, CUSTOM_FULLNAME, CUSTOM_EMAILADD );
    	logout();
    }
    
    public void testDefaultRolesOfProjectUser() 
    {
    	createUser( PROJECT_USER, CUSTOM_FULLNAME, CUSTOM_EMAILADD, CUSTOM_PASSWORD, true);
    	assertUsersListPage();
    	clickLinkWithText( PROJECT_USER );
    	clickLinkWithText( "Edit Roles" );
    	checkResourceRoleWithValue( "Project User - Default Project Group" );
    	submit();
    	
    	clickLinkWithText( PROJECT_USER );
    	assertPage( "[Admin] User Edit" );
    	assertTextPresent( "Project User - Default Project Group" );
    	logout();
    	//check access to continuum page
    	login( PROJECT_USER, CUSTOM_PASSWORD );
    	changePassword();
	    assertPagesWithUserRoles( "Project User - Default Project Group" );
	    logout();
	    
	    login( getUsername(), getPassword() );
    	deleteUser( PROJECT_USER, CUSTOM_FULLNAME, CUSTOM_EMAILADD );
    	logout();
    }
    
/*    public void testPasswordCreationValidation()
	    throws Exception
	{
	    // initial user account creation ignores the password creation checks
	    createUser( DUMMY_USER, CUSTOM_FULLNAME, CUSTOM_EMAILADD, CUSTOM_PASSWORD, true );
	    logout();
	
	    // start password creation validation test
	    login( DUMMY_USER, CUSTOM_PASSWORD );
	
	    // password test
	    String alphaTest = "abcdef";
	    String numericalTest = "123456";
	    String characterLengthTest = "aaaaaaa12";
	    String validPassword = "abc123";
	
	    //TODO: verify account details page
	    assertPage( "Change Password" );
	
	    // test all alpha
	    setFieldValue( "existingPassword" , CUSTOM_PASSWORD );
	    setFieldValue( "newPassword", alphaTest );
	    setFieldValue( "newPasswordConfirm", alphaTest );
	    clickButtonWithValue( "Change Password" );
	
	    // we should still be in Account Details
	    assertPage( "Change Password" );
	    isTextPresent( "You must provide a password containing at least 1 numeric character(s)." );
	
	    setFieldValue( "existingPassword" , CUSTOM_PASSWORD );
	    setFieldValue( "newPassword", numericalTest );
	    setFieldValue( "newPasswordConfirm", numericalTest );
	    clickButtonWithValue( "Change Password" );
	
	    // we should still be in Account Details
	    assertPage( "Change Password" );
	    isTextPresent( "You must provide a password containing at least 1 alphabetic character(s)." );
	
	    setFieldValue( "existingPassword" , CUSTOM_PASSWORD );
	    setFieldValue( "newPassword", characterLengthTest );
	    setFieldValue( "newPasswordConfirm", characterLengthTest );
	    clickButtonWithValue( "Change Password" );
	
	    // we should still be in Account Details
	    assertPage( "Account Details" );
	    isTextPresent( "You must provide a password between 1 and 8 characters in length." );
	
	    // we should still be in Account Details
	    assertPage( "Account Details" );
	    isTextPresent( "You must provide a password containing at least 1 alphabetic character(s)." );
	
	    setFieldValue( "existingPassword" , CUSTOM_PASSWORD );
	    setFieldValue( "newPassword", validPassword );
	    setFieldValue( "newPasswordConfirm", validPassword );
	    clickButtonWithValue( "Submit" );
	
	    // we should still be in Account Details
	    assertPage( "Continuum - Group Summary" );
	
	    logout();
	
	    // house keeping
	    login( getUsername(), getPassword() );
	    deleteUser( DUMMY_USER, CUSTOM_FULLNAME, CUSTOM_EMAILADD );
	    logout();
	}
*/
   
    private void createUser( String userName, String fullName, String emailAdd, String password, boolean valid )
    {
        createUser( userName, fullName, emailAdd, password, password, valid );
    }

    private void createUser( String userName, String fullName, String emailAdd, String password, String confirmPassword,
                             boolean valid )
    {
        clickLinkWithText( "Users" );
        assertUsersListPage();

        // create user
        clickButtonWithValue( "Create New User" );
        assertCreateUserPage();
        setFieldValue( "user.username", userName );
        setFieldValue( "user.fullName", fullName );
        setFieldValue( "user.email", emailAdd );
        setFieldValue( "user.password", password );
        setFieldValue( "user.confirmPassword", confirmPassword );
        submit();

        // click past second page without adding any roles
        assertAddUserRolesPage();
        clickButtonWithValue( "Submit" );

        if ( valid )
        {
            assertUsersListPage();

            String[] columnValues = {userName, fullName, emailAdd};

            // check if custom user is created
            assertElementPresent( XPathExpressionUtil.getTableRow( columnValues ) );
            //TODO: check Permanent/validated/locked columns
        }
        else
        {
            assertCreateUserPage();
        }
    }

    private void deleteUser( String userName, String fullName, String emailAdd )
    {
        deleteUser( userName, fullName, emailAdd, false, false );
    }

    private void deleteUser( String userName, String fullName, String emailAdd, boolean validated, boolean locked )
    {
        //TODO: Add permanent/validated/locked values
        String[] columnValues = {userName, fullName, emailAdd};

        clickLinkWithText( "Users" );

        // delete user
        clickLinkWithXPath( "//table[@id='ec_table']/tbody[2]/tr[3]/td[7]/a/img" );
        
        // confirm
        assertDeleteUserPage( userName );
        submit();

        // check if account is successfuly deleted
        assertElementNotPresent( XPathExpressionUtil.getTableRow( columnValues ) );
    }
    
    public void changePassword()
    {
    	assertPage( "Change Password" );
    	setFieldValue( "existingPassword" , CUSTOM_PASSWORD );
	    setFieldValue( "newPassword", CUSTOM_PASSWORD1 );
	    setFieldValue( "newPasswordConfirm", CUSTOM_PASSWORD1 );
	    clickButtonWithValue( "Change Password" );
    }
    
    /*
     * User assertions starts here...
     * */
    
    public void assertUsersListPage()
    {
        assertPage( "[Admin] User List" );
        assertTextPresent( "[Admin] List of Users in Role: Any" );
        assertLinkPresent( "guest" );
        assertLinkPresent( "admin" );
    }

    public void assertCreateUserPage()
    {
        assertPage( "[Admin] User Create" );
        assertTextPresent( "[Admin] User Create" );
        assertTextPresent( "Username*:" );
        assertElementPresent( CREATE_FORM_USERNAME_FIELD );
        assertTextPresent( "Full Name*:" );
        assertElementPresent( CREATE_FORM_FULLNAME_FIELD );
        assertTextPresent( "Email Address*:" );
        assertElementPresent( CREATE_FORM_EMAILADD_FIELD );
        assertTextPresent( "Password*:" );
        assertElementPresent( CREATE_FORM_PASSWORD_FIELD );
        assertTextPresent( "Confirm Password*:" );
        assertElementPresent( CREATE_FORM_CONFIRM_PASSWORD_FIELD );
        assertButtonWithValuePresent( "Create User" );
    }

    public void assertAddUserRolesPage()
    {
        assertPage( "[Admin] User Edit" );
        assertTextPresent( "[Admin] User Roles" );
        assertTextPresent( "redback-xwork-integration-core" );
        assertTextPresent( "Redback XWork Integration Security Core" );
        assertTextPresent( "Available Roles:" );
        String rolesCheckbox = "Guest,Registered User,System Administrator,User Administrator,Continuum Group Project Administrator,Continuum Group Project Developer,Continuum Group Project User,Continuum Manage Build Environments,Continuum Manage Build Templates,Continuum Manage Installations,Continuum Manage Local Repositories,Continuum Manage Purging,Continuum Manage Queues,Continuum Manage Scheduling";
        String[] arrayUserRoles = rolesCheckbox.split( "," );
        for( String userRoles : arrayUserRoles )
        	assertUserRoleCheckBoxPresent( userRoles );
        assertTextPresent( "Resource Roles:" );
        assertResourceRolesCheckBoxPresent( "Project Administrator - Default Project Group" );
        assertResourceRolesCheckBoxPresent( "Project Developer - Default Project Group" );
        assertResourceRolesCheckBoxPresent( "Project User - Default Project Group" );
    }

    public void assertDeleteUserPage( String username )
    {
        assertPage( "[Admin] User Delete" );
        assertTextPresent( "[Admin] User Delete" );
        assertTextPresent( "The following user will be deleted:" );
        assertTextPresent( "Username: " + username );
        assertButtonWithValuePresent( "Delete User" );
    }
    
    public void assertPagesWithUserRoles( String role ) 
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
}
