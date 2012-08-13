package org.apache.continuum.web.test.parent;

public abstract class AbstractUserRolesManagementTest
    extends AbstractAdminTest
{
    protected String username;

    protected String fullname;

    protected String getUserEmail()
    {
        return getProperty( "USERROLE_EMAIL" );
    }

    protected String getUserRolePassword()
    {
        return getProperty( "USERROLE_PASSWORD" );
    }

    protected String getUserRoleNewPassword()
    {
        return getProperty( "NEW_USERROLE_PASSWORD" );
    }

    ////////////////////////////
    // Assertions
    ////////////////////////////
    protected void assertCreateUserPage()
    {
        assertPage( "[Admin] User Create" );
        assertTextPresent( "[Admin] User Create" );
        assertTextPresent( "Username*:" );
        assertElementPresent( "user.username" );
        assertTextPresent( "Full Name*:" );
        assertElementPresent( "user.fullName" );
        assertTextPresent( "Email Address*:" );
        assertElementPresent( "user.email" );
        assertTextPresent( "Password*:" );
        assertElementPresent( "user.password" );
        assertTextPresent( "Confirm Password*:" );
        assertElementPresent( "user.confirmPassword" );
        assertButtonWithValuePresent( "Create User" );
    }

    protected void assertUserRolesPage()
    {
        assertPage( "[Admin] User Edit" );
        assertTextPresent( "[Admin] User Roles" );
        String userRoles =
            "Username,Full Name,Email,Guest,Registered User,System Administrator,User Administrator,Continuum Group Project Administrator,Continuum Group Project Developer,Continuum Group Project User,Continuum Manage Build Environments,Continuum Manage Build Templates,Continuum Manage Installations,Continuum Manage Local Repositories,Continuum Manage Purging,Continuum Manage Queues,Continuum Manage Scheduling,Project Administrator,Project Developer,Project User,Default Project Group";
        String[] arrayUserRoles = userRoles.split( "," );
        for ( String userroles : arrayUserRoles )
        {
            assertTextPresent( userroles );
        }
    }

    protected void assertCreatedUserInfo( String username )
    {
        selectValue( "name=ec_rd", "50" );
        waitPage();
        clickLinkWithText( username );
        clickLinkWithText( "Edit Roles" );
    }

    void assertUserRoleCheckBoxPresent( String value )
    {
        getSelenium().isElementPresent(
            "xpath=//input[@id='addRolesToUser_addNDSelectedRoles' and @name='addNDSelectedRoles' and @value='" +
                value + "']" );
    }

    void assertResourceRolesCheckBoxPresent( String value )
    {
        getSelenium().isElementPresent( "xpath=//input[@name='addDSelectedRoles' and @value='" + value + "']" );
    }

    protected void checkUserRoleWithValue( String value )
    {
        assertUserRoleCheckBoxPresent( value );
        getSelenium().click(
            "xpath=//input[@id='addRolesToUser_addNDSelectedRoles' and @name='addNDSelectedRoles' and @value='" +
                value + "']" );
    }

    protected void checkResourceRoleWithValue( String value )
    {
        assertResourceRolesCheckBoxPresent( value );
        getSelenium().click( "xpath=//input[@name='addDSelectedRoles' and @value='" + value + "']" );
    }

    protected void assertLeftNavMenuWithRole( String role )
    {
        if ( "System Administrator".equals( role ) )
        {
            String navMenu =
                "About,Show Project Groups,Maven Project,Maven 1.x Project,Ant Project,Shell Project,Local Repositories,Purge Configurations,Schedules,Installations,Build Environments,Queues,Build Definition Templates,Configuration,Appearance,Users,Roles,Build Queue";
            String[] arrayNavMenu = navMenu.split( "," );
            for ( String navmenu : arrayNavMenu )
            {
                assertLinkPresent( navmenu );
            }
        }
        else if ( "User Administrator".equals( role ) )
        {
            String navMenu = "About,Show Project Groups,Users,Roles";
            String[] arrayNavMenu = navMenu.split( "," );
            for ( String navmenu : arrayNavMenu )
            {
                assertLinkPresent( navmenu );
            }
        }
        else if ( "Continuum Group Project Administrator".equals( role ) )
        {
            String navMenu =
                "About,Show Project Groups,Maven Project,Maven 1.x Project,Ant Project,Shell Project,Schedules,Queues,Users,Roles";
            String[] arrayNavMenu = navMenu.split( "," );
            for ( String navmenu : arrayNavMenu )
            {
                assertLinkPresent( navmenu );
            }
        }
        else if ( "Continuum Group Project Developer".equals( role ) )
        {
            String navMenu = "About,Show Project Groups,Queues";
            String[] arrayNavMenu = navMenu.split( "," );
            for ( String navmenu : arrayNavMenu )
            {
                assertLinkPresent( navmenu );
            }
        }
        else if ( "Continuum Group Project User".equals( role ) )
        {
            String navMenu = "About,Show Project Groups,Queues";
            String[] arrayNavMenu = navMenu.split( "," );
            for ( String navmenu : arrayNavMenu )
            {
                assertLinkPresent( navmenu );
            }
        }
        else if ( "Continuum Manage Build Environments".equals( role ) )
        {
            String navMenu = "About,Show Project Groups,Build Environments";
            String[] arrayNavMenu = navMenu.split( "," );
            for ( String navmenu : arrayNavMenu )
            {
                assertLinkPresent( navmenu );
            }
        }
        else if ( "Continuum Manage Build Templates".equals( role ) )
        {
            String navMenu = "About,Show Project Groups,Build Definition Templates";
            String[] arrayNavMenu = navMenu.split( "," );
            for ( String navmenu : arrayNavMenu )
            {
                assertLinkPresent( navmenu );
            }
        }
        else if ( "Continuum Manage Installations".equals( role ) )
        {
            String navMenu = "About,Show Project Groups,Installations";
            String[] arrayNavMenu = navMenu.split( "," );
            for ( String navmenu : arrayNavMenu )
            {
                assertLinkPresent( navmenu );
            }
        }
        else if ( "Continuum Manage Local Repositories".equals( role ) )
        {
            String navMenu = "About,Show Project Groups,Local Repositories";
            String[] arrayNavMenu = navMenu.split( "," );
            for ( String navmenu : arrayNavMenu )
            {
                assertLinkPresent( navmenu );
            }
        }
        else if ( "Continuum Manage Purging".equals( role ) )
        {
            String navMenu = "About,Show Project Groups,Purge Configurations";
            String[] arrayNavMenu = navMenu.split( "," );
            for ( String navmenu : arrayNavMenu )
            {
                assertLinkPresent( navmenu );
            }
        }
        else if ( "Continuum Manage Queues".equals( role ) )
        {
            String navMenu = "About,Show Project Groups,Queues";
            String[] arrayNavMenu = navMenu.split( "," );
            for ( String navmenu : arrayNavMenu )
            {
                assertLinkPresent( navmenu );
            }
        }
        else if ( "Continuum Manage Scheduling".equals( role ) )
        {
            String navMenu = "About,Show Project Groups,Schedules";
            String[] arrayNavMenu = navMenu.split( "," );
            for ( String navmenu : arrayNavMenu )
            {
                assertLinkPresent( navmenu );
            }
        }
        else if ( "Project Administrator - Default Project Group".equals( role ) )
        {
            String navMenu = "About,Show Project Groups,Queues,Users,Roles";
            String[] arrayNavMenu = navMenu.split( "," );
            for ( String navmenu : arrayNavMenu )
            {
                assertLinkPresent( navmenu );
            }
        }
        else if ( "Project Developer - Default Project Group".equals( role ) ||
            "Project User - Default Project Group".equals( role ) )
        {
            String navMenu = "About,Show Project Groups,Queues";
            String[] arrayNavMenu = navMenu.split( "," );
            for ( String navmenu : arrayNavMenu )
            {
                assertLinkPresent( navmenu );
            }
        }
        else
        {
            String navMenu = "About,Show Project Groups";
            String[] arrayNavMenu = navMenu.split( "," );
            for ( String navmenu : arrayNavMenu )
            {
                assertLinkPresent( navmenu );
            }
            assertTextPresent( "Project Groups" );
            //assertTextPresent( "Project Groups list is empty." );
        }

    }

    void assertDeleteUserPage( String username )
    {
        assertPage( "[Admin] User Delete" );
        assertTextPresent( "[Admin] User Delete" );
        assertTextPresent( "The following user will be deleted:" );
        assertTextPresent( "Username: " + username );
        assertButtonWithValuePresent( "Delete User" );
    }

    protected void assertProjectAdministratorAccess()
    {
        assertLinkPresent( "About" );
        assertLinkPresent( "Show Project Groups" );
        assertLinkPresent( "Maven Project" );
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
    protected void changePassword( String oldPassword, String newPassword )
    {
        assertPage( "Change Password" );
        setFieldValue( "existingPassword", oldPassword );
        setFieldValue( "newPassword", newPassword );
        setFieldValue( "newPasswordConfirm", newPassword );
        clickButtonWithValue( "Change Password" );
    }

    protected void createUser( String userName, String fullName, String email, String password )
    {
        createUser( userName, fullName, email, password, password );
    }

    private void createUser( String userName, String fullName, String emailAd, String password, String confirmPassword )
    {
        loginAsAdmin();
        clickLinkWithText( "Users" );
        clickButtonWithValue( "Create New User" );
        assertCreateUserPage();
        setFieldValue( "user.username", userName );
        setFieldValue( "user.fullName", fullName );
        setFieldValue( "user.email", emailAd );
        setFieldValue( "user.password", password );
        setFieldValue( "user.confirmPassword", confirmPassword );
        submit();

        assertUserRolesPage();
        clickButtonWithValue( "Submit" );
    }


    protected void deleteUser( String userName )
    {
        //clickLinkWithText( "userlist" );
        clickLinkWithXPath( "//table[@id='ec_table']/tbody[2]/tr[3]/td[7]/a/img" );
        assertDeleteUserPage( userName );
        submit();
        assertElementNotPresent( userName );
    }
}
