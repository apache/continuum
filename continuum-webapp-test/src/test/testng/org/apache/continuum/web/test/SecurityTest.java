package org.apache.continuum.web.test;

import org.apache.continuum.web.test.parent.AbstractContinuumTest;
import org.testng.annotations.Test;

@Test( groups = { "security" } )
public class SecurityTest
    extends AbstractContinuumTest
{
    @Test( dependsOnMethods = { "testWithCreatedProjectAdminUser", "testWithCorrectUsernamePassword" })
    public void testProjectAdminAccess()
    {
        String username = getProperty( "PROJECT_ADMIN_USERNAME" );
        String password = getProperty( "PROJECT_ADMIN_NEW_PASSWORD" );
        String name = getProperty( "PROJECT_ADMIN_NAME" );

        // enable distributed build
        clickLinkWithText( "Configuration" );
        clickLinkWithLocator( "configuration_distributedBuildEnabled", false );
        clickButtonWithValue( "Save" );

        // logout admin
        clickLinkWithText( "Logout" );

        goToLoginPage();
        getSelenium().type( "loginForm_username", username );
        getSelenium().type( "loginForm_password", password );
        getSelenium().click( "loginForm__login" );
        getSelenium().waitForPageToLoad( maxWaitTimeInMs );

        assertTextPresent( "Current User: " + name + " (" + username + ")" );
        assertLinkPresent( "Edit Details" );
        assertLinkPresent( "Logout" );

        assertProjectAdministratorAccess();

        clickLinkWithText( "Logout" );

        // login as admin again
        goToLoginPage();
        getSelenium().type( "loginForm_username", getProperty( "ADMIN_USERNAME" ) );
        getSelenium().type( "loginForm_password", getProperty( "ADMIN_PASSWORD" ) );
        getSelenium().click( "loginForm__login" );
        getSelenium().waitForPageToLoad( maxWaitTimeInMs );

        // disable distributed build
        clickLinkWithText( "Configuration" );
        clickLinkWithLocator( "configuration_distributedBuildEnabled", false );
        clickButtonWithValue( "Save" );

        // logout admin
        clickLinkWithText( "Logout" );

        goToLoginPage();
        getSelenium().type( "loginForm_username", username );
        getSelenium().type( "loginForm_password", password );
        getSelenium().click( "loginForm__login" );
        getSelenium().waitForPageToLoad( maxWaitTimeInMs );

        assertTextPresent( "Current User: " + name + " (" + username + ")" );
        assertLinkPresent( "Edit Details" );
        assertLinkPresent( "Logout" );

        assertProjectAdministratorAccess();

        clickLinkWithText( "Logout" );
    }

    @Test( dependsOnMethods = { "testProjectAdminAccess" } )
    public void testNotAuthorizedMessageAfterLoginSuccessful()
    {
        assertTextPresent( "Login" );
        assertTextNotPresent( "Edit Details" );
        assertTextNotPresent( "Logout" );

        String url;

        if ( baseUrl.endsWith( "/" ) )
        {
            url = baseUrl + "projectGroupSummary.action?projectGroupId=1";
        }
        else
        {
            url = baseUrl + "/projectGroupSummary.action?projectGroupId=1";
        }

        getSelenium().open( url );
        assertTextPresent( "You are not authorized to access this page. Please contact your administrator to be granted the appropriate permissions." );

        goToLoginPage();
        getSelenium().type( "loginForm_username", getProperty( "ADMIN_USERNAME" ) );
        getSelenium().type( "loginForm_password", getProperty( "ADMIN_PASSWORD" ) );
        getSelenium().click( "loginForm__login" );
        getSelenium().waitForPageToLoad( maxWaitTimeInMs );

        assertTextNotPresent( "You are not authorized to access this page. Please contact your administrator to be granted the appropriate permissions." );
    }
}
