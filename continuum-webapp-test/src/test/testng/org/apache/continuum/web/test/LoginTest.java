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

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
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
    extends AbstractSeleniumTest
{
    public void testWithBadUsername()
    {
        diplayLoginPage();
        geSelenium().type( "loginForm_username", "badUsername" );
        geSelenium().type( "loginForm_username", p.getProperty( "ADMIN_PASSWORD" ) );
        geSelenium().click( "loginForm__login" );
        geSelenium().waitForPageToLoad( maxWaitTimeInMs );
        assertTextPresent( "You have entered an incorrect username and/or password" );
    }

    @Test( dependsOnMethods = { "testWithBadUsername" }, alwaysRun = true )
    public void testWithBadPassword()
    {
        diplayLoginPage();
        geSelenium().type( "loginForm_username", p.getProperty( "ADMIN_USERNAME" ) );
        geSelenium().type( "loginForm_password", "badPassword" );
        geSelenium().click( "loginForm__login" );
        geSelenium().waitForPageToLoad( maxWaitTimeInMs );
        assertTextPresent( "You have entered an incorrect username and/or password" );
    }

    @Test( dependsOnMethods = { "testWithBadPassword" }, alwaysRun = true )
    public void testWithEmptyUsername()
    {
        diplayLoginPage();
        geSelenium().type( "loginForm_password", "password" );
        geSelenium().click( "loginForm__login" );
        geSelenium().waitForPageToLoad( maxWaitTimeInMs );
        assertTextPresent( "User Name is required" );
    }

    @Test( dependsOnMethods = { "testWithEmptyUsername" }, alwaysRun = true )
    public void testWithEmptyPassword()
    {
        diplayLoginPage();
        geSelenium().type( "loginForm_username", p.getProperty( "ADMIN_USERNAME" ) );
        geSelenium().click( "loginForm__login" );
        geSelenium().waitForPageToLoad( maxWaitTimeInMs );
        assertTextPresent( "You have entered an incorrect username and/or password" );
    }

    @Test( groups = { "loginSuccess" }, dependsOnMethods = { "testWithEmptyPassword" }, alwaysRun = true )
    public void testWithCorrectUsernamePassword()
    {
        diplayLoginPage();
        geSelenium().type( "loginForm_username", p.getProperty( "ADMIN_USERNAME" ) );
        geSelenium().type( "loginForm_password", p.getProperty( "ADMIN_PASSWORD" ) );
        geSelenium().click( "loginForm__login" );
        geSelenium().waitForPageToLoad( maxWaitTimeInMs );
        assertTextPresent( "Edit Details" );
        assertTextPresent( "Logout" );
        assertTextPresent( p.getProperty( "ADMIN_USERNAME" ) );
    }


    @BeforeTest
    public void open()
        throws Exception
    {
        super.open(2);
    }

    @Override
    @AfterTest
    public void close()
        throws Exception
    {
        super.close();
    }

    private void diplayLoginPage()
    {
        geSelenium().open( baseUrl + "/security/login.action" );
        waitPage();
    }
}
