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

import org.apache.continuum.web.test.parent.AbstractNotifierTest;
import org.testng.annotations.Test;

/**
 * @author José Morales Martínez
 * @version $Id$
 */
@Test( groups = { "notifier" }, dependsOnMethods = { "testAddMavenTwoProjectFromRemoteSourceToNonDefaultProjectGroup" } )
public class NotifierTest
    extends AbstractNotifierTest
{
    public void testAddValidMailProjectNotifier()
        throws Exception
    {
        String TEST_PROJ_GRP_NAME = getProperty( "TEST_PROJ_GRP_NAME" );
        String M2_PROJ_GRP_NAME = getProperty( "M2_PROJ_GRP_NAME" );
        String MAIL_NOTIFIER_ADDRESS = getProperty( "MAIL_NOTIFIER_ADDRESS" );
        goToProjectNotifier( TEST_PROJ_GRP_NAME, M2_PROJ_GRP_NAME );
        addMailNotifier( TEST_PROJ_GRP_NAME, M2_PROJ_GRP_NAME, MAIL_NOTIFIER_ADDRESS, true );
    }
    
    public void testAddValidMailProjectNotifierWithInvalidValue()
        throws Exception
    {
        String TEST_PROJ_GRP_NAME = getProperty( "TEST_PROJ_GRP_NAME" );
        String M2_PROJ_GRP_NAME = getProperty( "M2_PROJ_GRP_NAME" );
        String MAIL_NOTIFIER_ADDRESS = "<script>alert('xss')</script>";
        goToProjectNotifier( TEST_PROJ_GRP_NAME, M2_PROJ_GRP_NAME );
        addMailNotifier( TEST_PROJ_GRP_NAME, M2_PROJ_GRP_NAME, MAIL_NOTIFIER_ADDRESS, false );
        assertTextPresent( "Address is invalid" );
    }

    @Test( dependsOnMethods = { "testAddValidMailProjectNotifier" } )
    public void testEditValidMailProjectNotifier()
        throws Exception
    {
        String TEST_PROJ_GRP_NAME = getProperty( "TEST_PROJ_GRP_NAME" );
        String M2_PROJ_GRP_NAME = getProperty( "M2_PROJ_GRP_NAME" );
        String MAIL_NOTIFIER_ADDRESS = getProperty( "MAIL_NOTIFIER_ADDRESS" );
        String newMail = "newmail@mail.com";
        goToProjectInformationPage( TEST_PROJ_GRP_NAME, M2_PROJ_GRP_NAME );
        editMailNotifier( TEST_PROJ_GRP_NAME, M2_PROJ_GRP_NAME, MAIL_NOTIFIER_ADDRESS, newMail, true );
        editMailNotifier( TEST_PROJ_GRP_NAME, M2_PROJ_GRP_NAME, newMail, MAIL_NOTIFIER_ADDRESS, true );
    }

    @Test( dependsOnMethods = { "testAddValidMailProjectNotifier" } )
    public void testEditInvalidMailProjectNotifier()
        throws Exception
    {
        String TEST_PROJ_GRP_NAME = getProperty( "TEST_PROJ_GRP_NAME" );
        String M2_PROJ_GRP_NAME = getProperty( "M2_PROJ_GRP_NAME" );
        String MAIL_NOTIFIER_ADDRESS = getProperty( "MAIL_NOTIFIER_ADDRESS" );
        goToProjectInformationPage( TEST_PROJ_GRP_NAME, M2_PROJ_GRP_NAME );
        editMailNotifier( TEST_PROJ_GRP_NAME, M2_PROJ_GRP_NAME, MAIL_NOTIFIER_ADDRESS, "invalid_email_add", false );
    }

    public void testAddInvalidMailProjectNotifier()
        throws Exception
    {
        String TEST_PROJ_GRP_NAME = getProperty( "TEST_PROJ_GRP_NAME" );
        String M2_PROJ_GRP_NAME = getProperty( "M2_PROJ_GRP_NAME" );
        goToProjectNotifier( TEST_PROJ_GRP_NAME, M2_PROJ_GRP_NAME );
        addMailNotifier( TEST_PROJ_GRP_NAME, M2_PROJ_GRP_NAME, "invalid_email_add", false );
    }

    public void testAddValidMailGroupNotifier()
        throws Exception
    {
        String TEST_PROJ_GRP_NAME = getProperty( "TEST_PROJ_GRP_NAME" );
        String TEST_PROJ_GRP_ID = getProperty( "TEST_PROJ_GRP_ID" );
        String TEST_PROJ_GRP_DESCRIPTION = getProperty( "TEST_PROJ_GRP_DESCRIPTION" );
        String MAIL_NOTIFIER_ADDRESS = getProperty( "MAIL_NOTIFIER_ADDRESS" );
        goToGroupNotifier( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
        addMailNotifier( TEST_PROJ_GRP_NAME, null, MAIL_NOTIFIER_ADDRESS, true );
    }

    @Test( dependsOnMethods = { "testAddValidMailGroupNotifier" } )
    public void testEditValidMailGroupNotifier()
        throws Exception
    {
        String TEST_PROJ_GRP_NAME = getProperty( "TEST_PROJ_GRP_NAME" );
        String TEST_PROJ_GRP_ID = getProperty( "TEST_PROJ_GRP_ID" );
        String TEST_PROJ_GRP_DESCRIPTION = getProperty( "TEST_PROJ_GRP_DESCRIPTION" );
        String MAIL_NOTIFIER_ADDRESS = getProperty( "MAIL_NOTIFIER_ADDRESS" );
        String newMail = "newmail@mail.com";
        showProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
        clickLinkWithText( "Notifiers" );
        assertGroupNotifierPage( TEST_PROJ_GRP_NAME );
        editMailNotifier( TEST_PROJ_GRP_NAME, null, MAIL_NOTIFIER_ADDRESS, newMail, true );
        editMailNotifier( TEST_PROJ_GRP_NAME, null, newMail, MAIL_NOTIFIER_ADDRESS, true );
    }

    @Test( dependsOnMethods = { "testAddValidMailGroupNotifier" } )
    public void testEditInvalidMailGroupNotifier()
        throws Exception
    {
        String TEST_PROJ_GRP_NAME = getProperty( "TEST_PROJ_GRP_NAME" );
        String TEST_PROJ_GRP_ID = getProperty( "TEST_PROJ_GRP_ID" );
        String TEST_PROJ_GRP_DESCRIPTION = getProperty( "TEST_PROJ_GRP_DESCRIPTION" );
        String MAIL_NOTIFIER_ADDRESS = getProperty( "MAIL_NOTIFIER_ADDRESS" );
        showProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
        clickLinkWithText( "Notifiers" );
        assertGroupNotifierPage( TEST_PROJ_GRP_NAME );
        editMailNotifier( TEST_PROJ_GRP_NAME, null, MAIL_NOTIFIER_ADDRESS, "invalid_email_add", false );
    }

    public void testAddInvalidMailGroupNotifier()
        throws Exception
    {
        String TEST_PROJ_GRP_NAME = getProperty( "TEST_PROJ_GRP_NAME" );
        String TEST_PROJ_GRP_ID = getProperty( "TEST_PROJ_GRP_ID" );
        String TEST_PROJ_GRP_DESCRIPTION = getProperty( "TEST_PROJ_GRP_DESCRIPTION" );
        goToGroupNotifier( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
        addMailNotifier( TEST_PROJ_GRP_NAME, null, "invalid_email_add", false );
    }

    public void testAddValidIrcProjectNotifier()
        throws Exception
    {
        String TEST_PROJ_GRP_NAME = getProperty( "TEST_PROJ_GRP_NAME" );
        String M2_PROJ_GRP_NAME = getProperty( "M2_PROJ_GRP_NAME" );
        String IRC_NOTIFIER_HOST = getProperty( "IRC_NOTIFIER_HOST" );
        String IRC_NOTIFIER_CHANNEL = getProperty( "IRC_NOTIFIER_CHANNEL" );
        goToProjectNotifier( TEST_PROJ_GRP_NAME, M2_PROJ_GRP_NAME );
        addIrcNotifier( TEST_PROJ_GRP_NAME, M2_PROJ_GRP_NAME, IRC_NOTIFIER_HOST, IRC_NOTIFIER_CHANNEL, true );
    }

    public void testAddProjectNotifierWithInvalidValues()
        throws Exception
    {
        String TEST_PROJ_GRP_NAME = getProperty( "TEST_PROJ_GRP_NAME" );
        String M2_PROJ_GRP_NAME = getProperty( "M2_PROJ_GRP_NAME" );
        String IRC_NOTIFIER_HOST = "!@#$<>?etc";
        String IRC_NOTIFIER_CHANNEL = "!@#$<>?etc";
        goToProjectNotifier( TEST_PROJ_GRP_NAME, M2_PROJ_GRP_NAME );
        addIrcNotifier( TEST_PROJ_GRP_NAME, M2_PROJ_GRP_NAME, IRC_NOTIFIER_HOST, IRC_NOTIFIER_CHANNEL, false );
        assertTextPresent( "Host contains invalid character" );
        assertTextPresent( "Channel contains invalid character" );
    }

    @Test( dependsOnMethods = { "testAddValidIrcProjectNotifier" } )
    public void testEditValidIrcProjectNotifier()
        throws Exception
    {
        String TEST_PROJ_GRP_NAME = getProperty( "TEST_PROJ_GRP_NAME" );
        String M2_PROJ_GRP_NAME = getProperty( "M2_PROJ_GRP_NAME" );
        String IRC_NOTIFIER_HOST = getProperty( "IRC_NOTIFIER_HOST" );
        String IRC_NOTIFIER_CHANNEL = getProperty( "IRC_NOTIFIER_CHANNEL" );
        String newHost = "new.test.com";
        String newChannel = "new_test_channel";
        goToProjectInformationPage( TEST_PROJ_GRP_NAME, M2_PROJ_GRP_NAME );
        editIrcNotifier( TEST_PROJ_GRP_NAME, M2_PROJ_GRP_NAME, IRC_NOTIFIER_HOST, IRC_NOTIFIER_CHANNEL, newHost,
                         newChannel, true );
        editIrcNotifier( TEST_PROJ_GRP_NAME, M2_PROJ_GRP_NAME, newHost, newChannel, IRC_NOTIFIER_HOST,
                         IRC_NOTIFIER_CHANNEL, true );
    }

    @Test( dependsOnMethods = { "testAddValidIrcProjectNotifier" } )
    public void testEditInvalidIrcProjectNotifier()
        throws Exception
    {
        String TEST_PROJ_GRP_NAME = getProperty( "TEST_PROJ_GRP_NAME" );
        String M2_PROJ_GRP_NAME = getProperty( "M2_PROJ_GRP_NAME" );
        String IRC_NOTIFIER_HOST = getProperty( "IRC_NOTIFIER_HOST" );
        String IRC_NOTIFIER_CHANNEL = getProperty( "IRC_NOTIFIER_CHANNEL" );
        goToProjectInformationPage( TEST_PROJ_GRP_NAME, M2_PROJ_GRP_NAME );
        editIrcNotifier( TEST_PROJ_GRP_NAME, M2_PROJ_GRP_NAME, IRC_NOTIFIER_HOST, IRC_NOTIFIER_CHANNEL, "", "", false );
    }

    public void testAddInvalidIrcProjectNotifier()
        throws Exception
    {
        String TEST_PROJ_GRP_NAME = getProperty( "TEST_PROJ_GRP_NAME" );
        String M2_PROJ_GRP_NAME = getProperty( "M2_PROJ_GRP_NAME" );
        goToProjectNotifier( TEST_PROJ_GRP_NAME, M2_PROJ_GRP_NAME );
        addIrcNotifier( TEST_PROJ_GRP_NAME, M2_PROJ_GRP_NAME, "", "", false );
        assertTextPresent( "Host is required" );
        assertTextPresent( "Channel is required" );
    }

    public void testAddValidIrcGroupNotifier()
        throws Exception
    {
        String TEST_PROJ_GRP_NAME = getProperty( "TEST_PROJ_GRP_NAME" );
        String TEST_PROJ_GRP_ID = getProperty( "TEST_PROJ_GRP_ID" );
        String TEST_PROJ_GRP_DESCRIPTION = getProperty( "TEST_PROJ_GRP_DESCRIPTION" );
        String IRC_NOTIFIER_HOST = getProperty( "IRC_NOTIFIER_HOST" );
        String IRC_NOTIFIER_CHANNEL = getProperty( "IRC_NOTIFIER_CHANNEL" );
        goToGroupNotifier( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
        addIrcNotifier( TEST_PROJ_GRP_NAME, null, IRC_NOTIFIER_HOST, IRC_NOTIFIER_CHANNEL, true );
    }

    @Test( dependsOnMethods = { "testAddValidIrcGroupNotifier" } )
    public void testEditValidIrcGroupNotifier()
        throws Exception
    {
        String TEST_PROJ_GRP_NAME = getProperty( "TEST_PROJ_GRP_NAME" );
        String TEST_PROJ_GRP_ID = getProperty( "TEST_PROJ_GRP_ID" );
        String TEST_PROJ_GRP_DESCRIPTION = getProperty( "TEST_PROJ_GRP_DESCRIPTION" );
        String IRC_NOTIFIER_HOST = getProperty( "IRC_NOTIFIER_HOST" );
        String IRC_NOTIFIER_CHANNEL = getProperty( "IRC_NOTIFIER_CHANNEL" );
        String newHost = "new.test.com";
        String newChannel = "new_test_channel";
        showProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
        clickLinkWithText( "Notifiers" );
        assertGroupNotifierPage( TEST_PROJ_GRP_NAME );
        editIrcNotifier( TEST_PROJ_GRP_NAME, null, IRC_NOTIFIER_HOST, IRC_NOTIFIER_CHANNEL, newHost, newChannel, true );
        editIrcNotifier( TEST_PROJ_GRP_NAME, null, newHost, newChannel, IRC_NOTIFIER_HOST, IRC_NOTIFIER_CHANNEL, true );
    }

    @Test( dependsOnMethods = { "testAddValidIrcGroupNotifier" } )
    public void testEditInvalidIrcGroupNotifier()
        throws Exception
    {
        String TEST_PROJ_GRP_NAME = getProperty( "TEST_PROJ_GRP_NAME" );
        String TEST_PROJ_GRP_ID = getProperty( "TEST_PROJ_GRP_ID" );
        String TEST_PROJ_GRP_DESCRIPTION = getProperty( "TEST_PROJ_GRP_DESCRIPTION" );
        String IRC_NOTIFIER_HOST = getProperty( "IRC_NOTIFIER_HOST" );
        String IRC_NOTIFIER_CHANNEL = getProperty( "IRC_NOTIFIER_CHANNEL" );
        showProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
        clickLinkWithText( "Notifiers" );
        assertGroupNotifierPage( TEST_PROJ_GRP_NAME );
        editIrcNotifier( TEST_PROJ_GRP_NAME, null, IRC_NOTIFIER_HOST, IRC_NOTIFIER_CHANNEL, "", "", false );
    }

    public void testAddInvalidIrcGroupNotifier()
        throws Exception
    {
        String TEST_PROJ_GRP_NAME = getProperty( "TEST_PROJ_GRP_NAME" );
        String TEST_PROJ_GRP_ID = getProperty( "TEST_PROJ_GRP_ID" );
        String TEST_PROJ_GRP_DESCRIPTION = getProperty( "TEST_PROJ_GRP_DESCRIPTION" );
        goToGroupNotifier( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
        addIrcNotifier( TEST_PROJ_GRP_NAME, null, "", "", false );
        assertTextPresent( "Host is required" );
        assertTextPresent( "Channel is required" );
    }

    public void testAddValidJabberProjectNotifier()
        throws Exception
    {
        String TEST_PROJ_GRP_NAME = getProperty( "TEST_PROJ_GRP_NAME" );
        String JABBER_NOTIFIER_HOST = getProperty( "JABBER_NOTIFIER_HOST" );
        String JABBER_NOTIFIER_LOGIN = getProperty( "JABBER_NOTIFIER_LOGIN" );
        String JABBER_NOTIFIER_PASSWORD = getProperty( "JABBER_NOTIFIER_PASSWORD" );
        String JABBER_NOTIFIER_ADDRESS = getProperty( "JABBER_NOTIFIER_ADDRESS" );
        String M2_PROJ_GRP_NAME = getProperty( "M2_PROJ_GRP_NAME" );
        goToProjectNotifier( TEST_PROJ_GRP_NAME, M2_PROJ_GRP_NAME );
        addJabberNotifier( TEST_PROJ_GRP_NAME, M2_PROJ_GRP_NAME, JABBER_NOTIFIER_HOST, JABBER_NOTIFIER_LOGIN,
                           JABBER_NOTIFIER_PASSWORD, JABBER_NOTIFIER_ADDRESS, true );
    }
    
    public void testAddJabberProjectNotifierWithInvalidValues()
        throws Exception
    {
        String TEST_PROJ_GRP_NAME = getProperty( "TEST_PROJ_GRP_NAME" );
        String JABBER_NOTIFIER_HOST = "!@#$<>?etc";
        String JABBER_NOTIFIER_LOGIN = getProperty( "JABBER_NOTIFIER_LOGIN" );
        String JABBER_NOTIFIER_PASSWORD = getProperty( "JABBER_NOTIFIER_PASSWORD" );
        String JABBER_NOTIFIER_ADDRESS = "!@#$<>?etc";
        String M2_PROJ_GRP_NAME = getProperty( "M2_PROJ_GRP_NAME" );
        goToProjectNotifier( TEST_PROJ_GRP_NAME, M2_PROJ_GRP_NAME );
        addJabberNotifier( TEST_PROJ_GRP_NAME, M2_PROJ_GRP_NAME, JABBER_NOTIFIER_HOST, JABBER_NOTIFIER_LOGIN,
                           JABBER_NOTIFIER_PASSWORD, JABBER_NOTIFIER_ADDRESS, false );
        assertTextPresent( "Host contains invalid characters" );
        assertTextPresent( "Address is invalid" );
    }

    @Test( dependsOnMethods = { "testAddValidJabberProjectNotifier" } )
    public void testEditValidJabberProjectNotifier()
        throws Exception
    {
        String TEST_PROJ_GRP_NAME = getProperty( "TEST_PROJ_GRP_NAME" );
        String M2_PROJ_GRP_NAME = getProperty( "M2_PROJ_GRP_NAME" );
        String JABBER_NOTIFIER_HOST = getProperty( "JABBER_NOTIFIER_HOST" );
        String JABBER_NOTIFIER_LOGIN = getProperty( "JABBER_NOTIFIER_LOGIN" );
        String JABBER_NOTIFIER_PASSWORD = getProperty( "JABBER_NOTIFIER_PASSWORD" );
        String JABBER_NOTIFIER_ADDRESS = getProperty( "JABBER_NOTIFIER_ADDRESS" );
        String newHost = "new_test";
        String newLogin = "new_test_login";
        String newPassword = "new_password";
        String newAddress = "new.test@mail.com";
        goToProjectInformationPage( TEST_PROJ_GRP_NAME, M2_PROJ_GRP_NAME );
        editJabberNotifier( TEST_PROJ_GRP_NAME, M2_PROJ_GRP_NAME, JABBER_NOTIFIER_HOST, JABBER_NOTIFIER_LOGIN,
                            JABBER_NOTIFIER_ADDRESS, newHost, newLogin, newPassword, newAddress, true );
        editJabberNotifier( TEST_PROJ_GRP_NAME, M2_PROJ_GRP_NAME, newHost, newLogin, newAddress, JABBER_NOTIFIER_HOST,
                            JABBER_NOTIFIER_LOGIN, JABBER_NOTIFIER_PASSWORD, JABBER_NOTIFIER_ADDRESS, true );
    }

    @Test( dependsOnMethods = { "testAddValidJabberProjectNotifier" } )
    public void testEditInvalidJabberProjectNotifier()
        throws Exception
    {
        String TEST_PROJ_GRP_NAME = getProperty( "TEST_PROJ_GRP_NAME" );
        String M2_PROJ_GRP_NAME = getProperty( "M2_PROJ_GRP_NAME" );
        String JABBER_NOTIFIER_HOST = getProperty( "JABBER_NOTIFIER_HOST" );
        String JABBER_NOTIFIER_LOGIN = getProperty( "JABBER_NOTIFIER_LOGIN" );
        String JABBER_NOTIFIER_ADDRESS = getProperty( "JABBER_NOTIFIER_ADDRESS" );
        goToProjectInformationPage( TEST_PROJ_GRP_NAME, M2_PROJ_GRP_NAME );
        editJabberNotifier( TEST_PROJ_GRP_NAME, M2_PROJ_GRP_NAME, JABBER_NOTIFIER_HOST, JABBER_NOTIFIER_LOGIN,
                            JABBER_NOTIFIER_ADDRESS, "", "", "", "", false );
    }

    public void testAddInvalidJabberProjectNotifier()
        throws Exception
    {
        String TEST_PROJ_GRP_NAME = getProperty( "TEST_PROJ_GRP_NAME" );
        String M2_PROJ_GRP_NAME = getProperty( "M2_PROJ_GRP_NAME" );
        goToProjectNotifier( TEST_PROJ_GRP_NAME, M2_PROJ_GRP_NAME );
        addJabberNotifier( TEST_PROJ_GRP_NAME, M2_PROJ_GRP_NAME, "", "", "", "", false );
        assertTextPresent( "Host is required" );
        assertTextPresent( "Login is required" );
        assertTextPresent( "Password is required" );
        assertTextPresent( "Address is required" );
    }

    public void testAddValidJabberGroupNotifier()
        throws Exception
    {
        String TEST_PROJ_GRP_NAME = getProperty( "TEST_PROJ_GRP_NAME" );
        String TEST_PROJ_GRP_ID = getProperty( "TEST_PROJ_GRP_ID" );
        String TEST_PROJ_GRP_DESCRIPTION = getProperty( "TEST_PROJ_GRP_DESCRIPTION" );
        String JABBER_NOTIFIER_HOST = getProperty( "JABBER_NOTIFIER_HOST" );
        String JABBER_NOTIFIER_LOGIN = getProperty( "JABBER_NOTIFIER_LOGIN" );
        String JABBER_NOTIFIER_PASSWORD = getProperty( "JABBER_NOTIFIER_PASSWORD" );
        String JABBER_NOTIFIER_ADDRESS = getProperty( "JABBER_NOTIFIER_ADDRESS" );
        goToGroupNotifier( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
        addJabberNotifier( TEST_PROJ_GRP_NAME, null, JABBER_NOTIFIER_HOST, JABBER_NOTIFIER_LOGIN,
                           JABBER_NOTIFIER_PASSWORD, JABBER_NOTIFIER_ADDRESS, true );
    }

    @Test( dependsOnMethods = { "testAddValidJabberGroupNotifier" } )
    public void testEditValidJabberGroupNotifier()
        throws Exception
    {
        String TEST_PROJ_GRP_NAME = getProperty( "TEST_PROJ_GRP_NAME" );
        String TEST_PROJ_GRP_ID = getProperty( "TEST_PROJ_GRP_ID" );
        String TEST_PROJ_GRP_DESCRIPTION = getProperty( "TEST_PROJ_GRP_DESCRIPTION" );
        String JABBER_NOTIFIER_HOST = getProperty( "JABBER_NOTIFIER_HOST" );
        String JABBER_NOTIFIER_LOGIN = getProperty( "JABBER_NOTIFIER_LOGIN" );
        String JABBER_NOTIFIER_PASSWORD = getProperty( "JABBER_NOTIFIER_PASSWORD" );
        String JABBER_NOTIFIER_ADDRESS = getProperty( "JABBER_NOTIFIER_ADDRESS" );
        String newHost = "new_test";
        String newLogin = "new_test_login";
        String newPassword = "new_password";
        String newAddress = "new.test@mail.com";
        showProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
        clickLinkWithText( "Notifiers" );
        assertGroupNotifierPage( TEST_PROJ_GRP_NAME );
        editJabberNotifier( TEST_PROJ_GRP_NAME, null, JABBER_NOTIFIER_HOST, JABBER_NOTIFIER_LOGIN,
                            JABBER_NOTIFIER_ADDRESS, newHost, newLogin, newPassword, newAddress, true );
        editJabberNotifier( TEST_PROJ_GRP_NAME, null, newHost, newLogin, newAddress, JABBER_NOTIFIER_HOST,
                            JABBER_NOTIFIER_LOGIN, JABBER_NOTIFIER_PASSWORD, JABBER_NOTIFIER_ADDRESS, true );
    }

    @Test( dependsOnMethods = { "testAddValidJabberGroupNotifier" } )
    public void testEditInvalidJabberGroupNotifier()
        throws Exception
    {
        String TEST_PROJ_GRP_NAME = getProperty( "TEST_PROJ_GRP_NAME" );
        String TEST_PROJ_GRP_ID = getProperty( "TEST_PROJ_GRP_ID" );
        String TEST_PROJ_GRP_DESCRIPTION = getProperty( "TEST_PROJ_GRP_DESCRIPTION" );
        String JABBER_NOTIFIER_HOST = getProperty( "JABBER_NOTIFIER_HOST" );
        String JABBER_NOTIFIER_LOGIN = getProperty( "JABBER_NOTIFIER_LOGIN" );
        String JABBER_NOTIFIER_ADDRESS = getProperty( "JABBER_NOTIFIER_ADDRESS" );
        showProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
        clickLinkWithText( "Notifiers" );
        assertGroupNotifierPage( TEST_PROJ_GRP_NAME );
        editJabberNotifier( TEST_PROJ_GRP_NAME, null, JABBER_NOTIFIER_HOST, JABBER_NOTIFIER_LOGIN,
                            JABBER_NOTIFIER_ADDRESS, "", "", "", "", false );
    }

    public void testAddInvalidJabberGroupNotifier()
        throws Exception
    {
        String TEST_PROJ_GRP_NAME = getProperty( "TEST_PROJ_GRP_NAME" );
        String TEST_PROJ_GRP_ID = getProperty( "TEST_PROJ_GRP_ID" );
        String TEST_PROJ_GRP_DESCRIPTION = getProperty( "TEST_PROJ_GRP_DESCRIPTION" );
        goToGroupNotifier( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
        addJabberNotifier( TEST_PROJ_GRP_NAME, null, "", "", "", "", false );
        assertTextPresent( "Host is required" );
        assertTextPresent( "Login is required" );
        assertTextPresent( "Password is required" );
        assertTextPresent( "Address is required" );
    }

    public void testAddValidMsnProjectNotifier()
        throws Exception
    {
        String TEST_PROJ_GRP_NAME = getProperty( "TEST_PROJ_GRP_NAME" );
        String M2_PROJ_GRP_NAME = getProperty( "M2_PROJ_GRP_NAME" );
        String MSN_NOTIFIER_ADDRESS = getProperty( "MSN_NOTIFIER_ADDRESS" );
        String MSN_NOTIFIER_LOGIN = getProperty( "MSN_NOTIFIER_LOGIN" );
        String MSN_NOTIFIER_PASSWORD = getProperty( "MSN_NOTIFIER_PASSWORD" );
        goToProjectNotifier( TEST_PROJ_GRP_NAME, M2_PROJ_GRP_NAME );
        addMsnNotifier( TEST_PROJ_GRP_NAME, M2_PROJ_GRP_NAME, MSN_NOTIFIER_LOGIN, MSN_NOTIFIER_PASSWORD,
                        MSN_NOTIFIER_ADDRESS, true );
    }

    public void testAddMsnProjectNotifierWithInvalidValues()
        throws Exception
    {
        String TEST_PROJ_GRP_NAME = getProperty( "TEST_PROJ_GRP_NAME" );
        String M2_PROJ_GRP_NAME = getProperty( "M2_PROJ_GRP_NAME" );
        String MSN_NOTIFIER_ADDRESS = "!@#$<>?etc";
        String MSN_NOTIFIER_LOGIN = getProperty( "MSN_NOTIFIER_LOGIN" );
        String MSN_NOTIFIER_PASSWORD = getProperty( "MSN_NOTIFIER_PASSWORD" );
        goToProjectNotifier( TEST_PROJ_GRP_NAME, M2_PROJ_GRP_NAME );
        addMsnNotifier( TEST_PROJ_GRP_NAME, M2_PROJ_GRP_NAME, MSN_NOTIFIER_LOGIN, MSN_NOTIFIER_PASSWORD,
                        MSN_NOTIFIER_ADDRESS, false );
        assertTextPresent( "Address is invalid" );
    }

    @Test( dependsOnMethods = { "testAddValidMsnProjectNotifier" } )
    public void testEditValidMsnProjectNotifier()
        throws Exception
    {
        String TEST_PROJ_GRP_NAME = getProperty( "TEST_PROJ_GRP_NAME" );
        String M2_PROJ_GRP_NAME = getProperty( "M2_PROJ_GRP_NAME" );
        String MSN_NOTIFIER_ADDRESS = getProperty( "MSN_NOTIFIER_ADDRESS" );
        String MSN_NOTIFIER_LOGIN = getProperty( "MSN_NOTIFIER_LOGIN" );
        String MSN_NOTIFIER_PASSWORD = getProperty( "MSN_NOTIFIER_PASSWORD" );
        String newLogin = "new_test_login";
        String newPassword = "new_password";
        String newAddress = "new.test@mail.com";
        goToProjectInformationPage( TEST_PROJ_GRP_NAME, M2_PROJ_GRP_NAME );
        editMsnNotifier( TEST_PROJ_GRP_NAME, M2_PROJ_GRP_NAME, MSN_NOTIFIER_LOGIN, MSN_NOTIFIER_ADDRESS, newLogin,
                         newPassword, newAddress, true );
        editMsnNotifier( TEST_PROJ_GRP_NAME, M2_PROJ_GRP_NAME, newLogin, newAddress, MSN_NOTIFIER_LOGIN,
                         MSN_NOTIFIER_PASSWORD, MSN_NOTIFIER_ADDRESS, true );
    }

    @Test( dependsOnMethods = { "testAddValidMsnProjectNotifier" } )
    public void testEditInvalidMsnProjectNotifier()
        throws Exception
    {
        String TEST_PROJ_GRP_NAME = getProperty( "TEST_PROJ_GRP_NAME" );
        String M2_PROJ_GRP_NAME = getProperty( "M2_PROJ_GRP_NAME" );
        String MSN_NOTIFIER_ADDRESS = getProperty( "MSN_NOTIFIER_ADDRESS" );
        String MSN_NOTIFIER_LOGIN = getProperty( "MSN_NOTIFIER_LOGIN" );
        goToProjectInformationPage( TEST_PROJ_GRP_NAME, M2_PROJ_GRP_NAME );
        editMsnNotifier( TEST_PROJ_GRP_NAME, M2_PROJ_GRP_NAME, MSN_NOTIFIER_LOGIN, MSN_NOTIFIER_ADDRESS, "", "", "",
                         false );
    }

    public void testAddInvalidMsnProjectNotifier()
        throws Exception
    {
        String TEST_PROJ_GRP_NAME = getProperty( "TEST_PROJ_GRP_NAME" );
        String M2_PROJ_GRP_NAME = getProperty( "M2_PROJ_GRP_NAME" );
        goToProjectNotifier( TEST_PROJ_GRP_NAME, M2_PROJ_GRP_NAME );
        addMsnNotifier( TEST_PROJ_GRP_NAME, M2_PROJ_GRP_NAME, "", "", "", false );
        assertTextPresent( "Login is required" );
        assertTextPresent( "Password is required" );
        assertTextPresent( "Address is required" );
    }

    public void testAddValidMsnGroupNotifier()
        throws Exception
    {
        String TEST_PROJ_GRP_NAME = getProperty( "TEST_PROJ_GRP_NAME" );
        String TEST_PROJ_GRP_ID = getProperty( "TEST_PROJ_GRP_ID" );
        String TEST_PROJ_GRP_DESCRIPTION = getProperty( "TEST_PROJ_GRP_DESCRIPTION" );
        String MSN_NOTIFIER_ADDRESS = getProperty( "MSN_NOTIFIER_ADDRESS" );
        String MSN_NOTIFIER_LOGIN = getProperty( "MSN_NOTIFIER_LOGIN" );
        String MSN_NOTIFIER_PASSWORD = getProperty( "MSN_NOTIFIER_PASSWORD" );
        goToGroupNotifier( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
        addMsnNotifier( TEST_PROJ_GRP_NAME, null, MSN_NOTIFIER_LOGIN, MSN_NOTIFIER_PASSWORD, MSN_NOTIFIER_ADDRESS, true );
    }

    @Test( dependsOnMethods = { "testAddValidMsnGroupNotifier" } )
    public void testEditValidMsnGroupNotifier()
        throws Exception
    {
        String TEST_PROJ_GRP_NAME = getProperty( "TEST_PROJ_GRP_NAME" );
        String TEST_PROJ_GRP_ID = getProperty( "TEST_PROJ_GRP_ID" );
        String TEST_PROJ_GRP_DESCRIPTION = getProperty( "TEST_PROJ_GRP_DESCRIPTION" );
        String MSN_NOTIFIER_ADDRESS = getProperty( "MSN_NOTIFIER_ADDRESS" );
        String MSN_NOTIFIER_LOGIN = getProperty( "MSN_NOTIFIER_LOGIN" );
        String MSN_NOTIFIER_PASSWORD = getProperty( "MSN_NOTIFIER_PASSWORD" );
        String newLogin = "new_test_login";
        String newPassword = "new_password";
        String newAddress = "new.test@mail.com";
        showProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
        clickLinkWithText( "Notifiers" );
        assertGroupNotifierPage( TEST_PROJ_GRP_NAME );
        editMsnNotifier( TEST_PROJ_GRP_NAME, null, MSN_NOTIFIER_LOGIN, MSN_NOTIFIER_ADDRESS, newLogin, newPassword,
                         newAddress, true );
        editMsnNotifier( TEST_PROJ_GRP_NAME, null, newLogin, newAddress, MSN_NOTIFIER_LOGIN, MSN_NOTIFIER_PASSWORD,
                         MSN_NOTIFIER_ADDRESS, true );
    }

    @Test( dependsOnMethods = { "testAddValidMsnGroupNotifier" } )
    public void testEditInvalidMsnGroupNotifier()
        throws Exception
    {
        String TEST_PROJ_GRP_NAME = getProperty( "TEST_PROJ_GRP_NAME" );
        String TEST_PROJ_GRP_ID = getProperty( "TEST_PROJ_GRP_ID" );
        String TEST_PROJ_GRP_DESCRIPTION = getProperty( "TEST_PROJ_GRP_DESCRIPTION" );
        String MSN_NOTIFIER_ADDRESS = getProperty( "MSN_NOTIFIER_ADDRESS" );
        String MSN_NOTIFIER_LOGIN = getProperty( "MSN_NOTIFIER_LOGIN" );
        showProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
        clickLinkWithText( "Notifiers" );
        assertGroupNotifierPage( TEST_PROJ_GRP_NAME );
        editMsnNotifier( TEST_PROJ_GRP_NAME, null, MSN_NOTIFIER_LOGIN, MSN_NOTIFIER_ADDRESS, "", "", "", false );
    }

    public void testAddInvalidMsnGroupNotifier()
        throws Exception
    {
        String TEST_PROJ_GRP_NAME = getProperty( "TEST_PROJ_GRP_NAME" );
        String TEST_PROJ_GRP_ID = getProperty( "TEST_PROJ_GRP_ID" );
        String TEST_PROJ_GRP_DESCRIPTION = getProperty( "TEST_PROJ_GRP_DESCRIPTION" );
        goToGroupNotifier( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
        addMsnNotifier( TEST_PROJ_GRP_NAME, null, "", "", "", false );
        assertTextPresent( "Login is required" );
        assertTextPresent( "Password is required" );
        assertTextPresent( "Address is required" );
    }

    public void testAddValidWagonProjectNotifier()
        throws Exception
    {
        String TEST_PROJ_GRP_NAME = getProperty( "TEST_PROJ_GRP_NAME" );
        String M2_PROJ_GRP_NAME = getProperty( "M2_PROJ_GRP_NAME" );
        String WAGON_NOTIFIER_URL = getProperty( "WAGON_NOTIFIER_URL" );
        String WAGON_SERVER_ID = getProperty( "WAGON_SERVER_ID" );
        goToProjectNotifier( TEST_PROJ_GRP_NAME, M2_PROJ_GRP_NAME );
        addWagonNotifierPage( TEST_PROJ_GRP_NAME, M2_PROJ_GRP_NAME, WAGON_NOTIFIER_URL, WAGON_SERVER_ID, true );
    }
    
    public void testAddInvalidURLWagonProjectNotifier()
        throws Exception
    {
        String TEST_PROJ_GRP_NAME = getProperty( "TEST_PROJ_GRP_NAME" );
        String M2_PROJ_GRP_NAME = getProperty( "M2_PROJ_GRP_NAME" );
        String WAGON_NOTIFIER_URL = "!@#$<>?etc";
        String WAGON_SERVER_ID = getProperty( "WAGON_SERVER_ID" );
        goToProjectNotifier( TEST_PROJ_GRP_NAME, M2_PROJ_GRP_NAME );
        addWagonNotifierPage( TEST_PROJ_GRP_NAME, M2_PROJ_GRP_NAME, WAGON_NOTIFIER_URL, WAGON_SERVER_ID, false );
        assertTextPresent( "Destination URL is invalid" );
    }

    @Test( dependsOnMethods = { "testAddValidWagonProjectNotifier" } )
    public void testEditValidWagonProjectNotifier()
        throws Exception
    {
        String TEST_PROJ_GRP_NAME = getProperty( "TEST_PROJ_GRP_NAME" );
        String M2_PROJ_GRP_NAME = getProperty( "M2_PROJ_GRP_NAME" );
        String WAGON_NOTIFIER_URL = getProperty( "WAGON_NOTIFIER_URL" );
        String WAGON_SERVER_ID = getProperty( "WAGON_SERVER_ID" );
        String newId = "newId";
        String newUrl = WAGON_NOTIFIER_URL;
        goToProjectInformationPage( TEST_PROJ_GRP_NAME, M2_PROJ_GRP_NAME );
        editWagonNotifier( TEST_PROJ_GRP_NAME, M2_PROJ_GRP_NAME, WAGON_NOTIFIER_URL, WAGON_SERVER_ID, newUrl, newId,
                           true );
        editWagonNotifier( TEST_PROJ_GRP_NAME, M2_PROJ_GRP_NAME, newUrl, newId, WAGON_NOTIFIER_URL, WAGON_SERVER_ID,
                           true );
    }

    @Test( dependsOnMethods = { "testAddValidWagonProjectNotifier" } )
    public void testEditInvalidWagonProjectNotifier()
        throws Exception
    {
        String TEST_PROJ_GRP_NAME = getProperty( "TEST_PROJ_GRP_NAME" );
        String M2_PROJ_GRP_NAME = getProperty( "M2_PROJ_GRP_NAME" );
        String WAGON_NOTIFIER_URL = getProperty( "WAGON_NOTIFIER_URL" );
        String WAGON_SERVER_ID = getProperty( "WAGON_SERVER_ID" );
        goToProjectInformationPage( TEST_PROJ_GRP_NAME, M2_PROJ_GRP_NAME );
        editWagonNotifier( TEST_PROJ_GRP_NAME, M2_PROJ_GRP_NAME, WAGON_NOTIFIER_URL, WAGON_SERVER_ID, "", "", false );
    }

    public void testAddInvalidWagonProjectNotifier()
        throws Exception
    {
        String TEST_PROJ_GRP_NAME = getProperty( "TEST_PROJ_GRP_NAME" );
        String M2_PROJ_GRP_NAME = getProperty( "M2_PROJ_GRP_NAME" );
        goToProjectNotifier( TEST_PROJ_GRP_NAME, M2_PROJ_GRP_NAME );
        addWagonNotifierPage( TEST_PROJ_GRP_NAME, M2_PROJ_GRP_NAME, "", "", false );
        assertTextPresent( "Destination URL is required" );
        assertTextPresent( "Server Id is required" );
    }

    public void testAddValidWagonGroupNotifier()
        throws Exception
    {
        String TEST_PROJ_GRP_NAME = getProperty( "TEST_PROJ_GRP_NAME" );
        String TEST_PROJ_GRP_ID = getProperty( "TEST_PROJ_GRP_ID" );
        String TEST_PROJ_GRP_DESCRIPTION = getProperty( "TEST_PROJ_GRP_DESCRIPTION" );
        String WAGON_NOTIFIER_URL = getProperty( "WAGON_NOTIFIER_URL" );
        String WAGON_SERVER_ID = getProperty( "WAGON_SERVER_ID" );
        goToGroupNotifier( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
        addWagonNotifierPage( TEST_PROJ_GRP_NAME, null, WAGON_NOTIFIER_URL, WAGON_SERVER_ID, true );
    }

    @Test( dependsOnMethods = { "testAddValidWagonGroupNotifier" } )
    public void testEditValidWagonGroupNotifier()
        throws Exception
    {
        String TEST_PROJ_GRP_NAME = getProperty( "TEST_PROJ_GRP_NAME" );
        String TEST_PROJ_GRP_ID = getProperty( "TEST_PROJ_GRP_ID" );
        String TEST_PROJ_GRP_DESCRIPTION = getProperty( "TEST_PROJ_GRP_DESCRIPTION" );
        String WAGON_NOTIFIER_URL = getProperty( "WAGON_NOTIFIER_URL" );
        String WAGON_SERVER_ID = getProperty( "WAGON_SERVER_ID" );
        String newId = "newId";
        String newUrl = WAGON_NOTIFIER_URL;
        showProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
        clickLinkWithText( "Notifiers" );
        assertGroupNotifierPage( TEST_PROJ_GRP_NAME );
        editWagonNotifier( TEST_PROJ_GRP_NAME, null, WAGON_NOTIFIER_URL, WAGON_SERVER_ID, newUrl, newId, true );
        editWagonNotifier( TEST_PROJ_GRP_NAME, null, newUrl, newId, WAGON_NOTIFIER_URL, WAGON_SERVER_ID, true );
    }

    @Test( dependsOnMethods = { "testAddValidWagonGroupNotifier" } )
    public void testEditInvalidWagonGroupNotifier()
        throws Exception
    {
        String TEST_PROJ_GRP_NAME = getProperty( "TEST_PROJ_GRP_NAME" );
        String TEST_PROJ_GRP_ID = getProperty( "TEST_PROJ_GRP_ID" );
        String TEST_PROJ_GRP_DESCRIPTION = getProperty( "TEST_PROJ_GRP_DESCRIPTION" );
        String WAGON_NOTIFIER_URL = getProperty( "WAGON_NOTIFIER_URL" );
        String WAGON_SERVER_ID = getProperty( "WAGON_SERVER_ID" );
        showProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
        clickLinkWithText( "Notifiers" );
        assertGroupNotifierPage( TEST_PROJ_GRP_NAME );
        editWagonNotifier( TEST_PROJ_GRP_NAME, null, WAGON_NOTIFIER_URL, WAGON_SERVER_ID, "", "", false );
    }

    public void testAddInvalidWagonGroupNotifier()
        throws Exception
    {
        String TEST_PROJ_GRP_NAME = getProperty( "TEST_PROJ_GRP_NAME" );
        String TEST_PROJ_GRP_ID = getProperty( "TEST_PROJ_GRP_ID" );
        String TEST_PROJ_GRP_DESCRIPTION = getProperty( "TEST_PROJ_GRP_DESCRIPTION" );
        goToGroupNotifier( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
        addWagonNotifierPage( TEST_PROJ_GRP_NAME, null, "", "", false );
        assertTextPresent( "Destination URL is required" );
        assertTextPresent( "Server Id is required" );
    }

    @Test( dependsOnMethods = { "testEditValidMailGroupNotifier", "testEditInvalidMailGroupNotifier" } )
    public void testDeleteGroupNotifier()
        throws Exception
    {
        String TEST_PROJ_GRP_NAME = getProperty( "TEST_PROJ_GRP_NAME" );
        String TEST_PROJ_GRP_ID = getProperty( "TEST_PROJ_GRP_ID" );
        String TEST_PROJ_GRP_DESCRIPTION = getProperty( "TEST_PROJ_GRP_DESCRIPTION" );
        showProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
        clickLinkWithText( "Notifiers" );
        assertGroupNotifierPage( TEST_PROJ_GRP_NAME );
        clickLinkWithXPath( "(//a[contains(@href,'deleteProjectGroupNotifier') and contains(@href,'mail')])//img" );
        assertButtonWithValuePresent( "Delete" );
        assertButtonWithValuePresent( "Cancel" );
        clickButtonWithValue( "Delete" );
        assertGroupNotifierPage( TEST_PROJ_GRP_NAME );
    }

    @Test( dependsOnMethods = { "testEditValidMailProjectNotifier", "testEditInvalidMailProjectNotifier" } )
    public void testDeleteProjectNotifier()
        throws Exception
    {
        String TEST_PROJ_GRP_NAME = getProperty( "TEST_PROJ_GRP_NAME" );
        String M2_PROJ_GRP_NAME = getProperty( "M2_PROJ_GRP_NAME" );
        goToProjectInformationPage( TEST_PROJ_GRP_NAME, M2_PROJ_GRP_NAME );
        // Delete
        clickLinkWithXPath( "(//a[contains(@href,'deleteProjectNotifier') and contains(@href,'mail')])//img" );
        assertButtonWithValuePresent( "Delete" );
        assertButtonWithValuePresent( "Cancel" );
        clickButtonWithValue( "Delete" );
        assertProjectInformationPage();
    }
}
