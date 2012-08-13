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
 *   http://www.apache.org/licenses/LICENSE-2.0
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
@Test( groups = {"login"} )
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

    public void testWithBadPassword()
    {
        login( getProperty( "ADMIN_USERNAME" ), "badPassword" );
        assertTextPresent( "You have entered an incorrect username and/or password" );
    }

    public void testWithEmptyUsername()
    {
        goToLoginPage();
        getSelenium().type( "loginForm_password", "password" );
        getSelenium().click( "loginForm__login" );
        getSelenium().waitForPageToLoad( maxWaitTimeInMs );
        assertTextPresent( "User Name is required" );
    }

    public void testWithEmptyPassword()
    {
        goToLoginPage();
        getSelenium().type( "loginForm_username", getProperty( "ADMIN_USERNAME" ) );
        getSelenium().click( "loginForm__login" );
        getSelenium().waitForPageToLoad( maxWaitTimeInMs );
        assertTextPresent( "You have entered an incorrect username and/or password" );
    }

    public void testWithCorrectUsernamePassword()
    {
        String username = getProperty( "ADMIN_USERNAME" );
        String password = getProperty( "ADMIN_PASSWORD" );
        login( username, password );
        assertTextPresent( "Edit Details" );
        assertTextPresent( "Logout" );
        assertTextPresent( username );
    }

}
