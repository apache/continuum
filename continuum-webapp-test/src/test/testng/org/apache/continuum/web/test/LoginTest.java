package org.apache.continuum.web.test;

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

import org.apache.continuum.web.test.parent.AbstractContinuumTest;
import org.testng.annotations.Test;

/*
 * Bug in TestNG. TESTNG-285: @Test(sequential=true) works incorrectly for classes with inheritance
 * http://code.google.com/p/testng/source/browse/trunk/CHANGES.txt
 * Waiting 5.9 release. It's comming soon.
 */
/**
 * Based on LoginTest of Emmanuel Venisse test.
 *
 * @author José Morales Martínez
 * @version $Id$
 */
@Test( groups = { "login" } )
public class LoginTest
    extends AbstractContinuumTest
{
    public void testWithBadUsername()
    {
        goToLoginPage();
        getSelenium().type( "loginForm_username", "badUsername" );
        getSelenium().type( "loginForm_username", getProperty( "ADMIN_PASSWORD" ) );
        getSelenium().click( "loginForm__login" );
        getSelenium().waitForPageToLoad( maxWaitTimeInMs );
        assertTextPresent( "You have entered an incorrect username and/or password" );
    }

    @Test( dependsOnMethods = { "testWithBadUsername" }, alwaysRun = true )
    public void testWithBadPassword()
    {
        goToLoginPage();
        getSelenium().type( "loginForm_username", getProperty( "ADMIN_USERNAME" ) );
        getSelenium().type( "loginForm_password", "badPassword" );
        getSelenium().click( "loginForm__login" );
        getSelenium().waitForPageToLoad( maxWaitTimeInMs );
        assertTextPresent( "You have entered an incorrect username and/or password" );
    }

    @Test( dependsOnMethods = { "testWithBadPassword" }, alwaysRun = true )
    public void testWithEmptyUsername()
    {
        goToLoginPage();
        getSelenium().type( "loginForm_password", "password" );
        getSelenium().click( "loginForm__login" );
        getSelenium().waitForPageToLoad( maxWaitTimeInMs );
        assertTextPresent( "User Name is required" );
    }

    @Test( dependsOnMethods = { "testWithEmptyUsername" }, alwaysRun = true )
    public void testWithEmptyPassword()
    {
        goToLoginPage();
        getSelenium().type( "loginForm_username", getProperty( "ADMIN_USERNAME" ) );
        getSelenium().click( "loginForm__login" );
        getSelenium().waitForPageToLoad( maxWaitTimeInMs );
        assertTextPresent( "You have entered an incorrect username and/or password" );
    }

    @Test( dependsOnMethods = { "testWithEmptyPassword" } )
    public void testWithCreatedProjectAdminUser()
    {
        goToLoginPage();
        getSelenium().type( "loginForm_username", getProperty( "ADMIN_USERNAME" ) );
        getSelenium().type( "loginForm_password", getProperty( "ADMIN_PASSWORD" ) );
        getSelenium().click( "loginForm__login" );
        getSelenium().waitForPageToLoad( maxWaitTimeInMs );

        clickLinkWithText( "Configuration" );
        clickLinkWithLocator( "configuration_distributedBuildEnabled", false );

        String username = getProperty( "PROJECT_ADMIN_USERNAME" );
        String name = getProperty( "PROJECT_ADMIN_NAME" );
        String email = getProperty( "PROJECT_ADMIN_EMAIL" );
        String oldPassword = getProperty( "PROJECT_ADMIN_OLD_PASSWORD" );
        String newPassword = getProperty( "PROJECT_ADMIN_NEW_PASSWORD" );

        createNewUser( username, name, email, oldPassword );
        assignContinuumRoleToUser( "Continuum Group Project Administrator" );
        clickButtonWithValue( "Submit" );
        assertUserCreatedPage();
        assertLinkPresent( username );
        assertTextPresent( name );
        assertTextPresent( email );

        clickLinkWithText( username );
        assertUserEditPage( username, name, email );
        assertTextNotPresent( "Last Login:" );
        assertTextPresent( "Continuum Group Project User" );
        assertTextPresent( "Continuum Group Project Developer" );
        assertTextPresent( "Continuum Group Project Administrator" );
        clickLinkWithText( "Logout" );
        goToLoginPage();

        getSelenium().type( "loginForm_username", username );
        getSelenium().type( "loginForm_password", oldPassword );
        getSelenium().click( "loginForm__login" );
        getSelenium().waitForPageToLoad( maxWaitTimeInMs );
        assertChangePasswordPage();
        
        getSelenium().type( "passwordForm_existingPassword", oldPassword );
        getSelenium().type( "passwordForm_newPassword", newPassword );
        getSelenium().type( "passwordForm_newPasswordConfirm", newPassword );
        getSelenium().click( "passwordForm__submit" );
        getSelenium().waitForPageToLoad( maxWaitTimeInMs );

        assertLinkPresent( "Edit Details" );
        assertLinkPresent( "Logout" );
        
        clickLinkWithText( "Logout" );
    }

    @Test( groups = { "loginSuccess" }, dependsOnMethods = { "testWithCreatedProjectAdminUser" }, alwaysRun = true )
    public void testWithCorrectUsernamePassword()
    {
        goToLoginPage();
        getSelenium().type( "loginForm_username", getProperty( "ADMIN_USERNAME" ) );
        getSelenium().type( "loginForm_password", getProperty( "ADMIN_PASSWORD" ) );
        getSelenium().click( "loginForm__login" );
        getSelenium().waitForPageToLoad( maxWaitTimeInMs );
        assertTextPresent( "Edit Details" );
        assertTextPresent( "Logout" );
        assertTextPresent( getProperty( "ADMIN_USERNAME" ) );
    }
}
