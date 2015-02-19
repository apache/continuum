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

import org.apache.continuum.web.test.parent.AbstractAdminTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author José Morales Martínez
 */
@Test( groups = { "notifier" } )
public class NotifierTest
    extends AbstractAdminTest
{
    private String projectGroupName;

    private String projectGroupId;

    private String projectGroupDescription;

    private String projectName;

    private String mailNotifierAddress;

    private String ircNotifierHost;

    private String ircNotifierChannel;

    private String jabberNotifierHost;

    private String jabberNotifierLogin;

    private String jabberNotifierPassword;

    private String jabberNotifierAddress;

    private String msnNotifierAddress;

    private String msnNotifierLogin;

    private String msnNotifierPassword;

    private String wagonNotifierUrl;

    private String wagonServerId;

    @BeforeClass
    public void createProject()
    {
        projectGroupName = getProperty( "NOTIFIER_PROJECT_GROUP_NAME" );
        projectGroupId = getProperty( "NOTIFIER_PROJECT_GROUP_ID" );
        projectGroupDescription = getProperty( "NOTIFIER_PROJECT_GROUP_DESCRIPTION" );

        projectName = getProperty( "MAVEN2_POM_PROJECT_NAME" );
        String projectPomUrl = getProperty( "MAVEN2_POM_URL" );
        String pomUsername = getProperty( "MAVEN2_POM_USERNAME" );
        String pomPassword = getProperty( "MAVEN2_POM_PASSWORD" );

        loginAsAdmin();

        removeProjectGroup( projectGroupName, false );

        addProjectGroup( projectGroupName, projectGroupId, projectGroupDescription, true, true );
        clickLinkWithText( projectGroupName );

        addMavenTwoProject( projectPomUrl, pomUsername, pomPassword, projectGroupName, true );
    }

    @BeforeMethod
    protected void setUp()
        throws Exception
    {
        mailNotifierAddress = getProperty( "MAIL_NOTIFIER_ADDRESS" );
        ircNotifierHost = getProperty( "IRC_NOTIFIER_HOST" );
        ircNotifierChannel = getProperty( "IRC_NOTIFIER_CHANNEL" );
        jabberNotifierHost = getProperty( "JABBER_NOTIFIER_HOST" );
        jabberNotifierLogin = getProperty( "JABBER_NOTIFIER_LOGIN" );
        jabberNotifierPassword = getProperty( "JABBER_NOTIFIER_PASSWORD" );
        jabberNotifierAddress = getProperty( "JABBER_NOTIFIER_ADDRESS" );
        msnNotifierAddress = getProperty( "MSN_NOTIFIER_ADDRESS" );
        msnNotifierLogin = getProperty( "MSN_NOTIFIER_LOGIN" );
        msnNotifierPassword = getProperty( "MSN_NOTIFIER_PASSWORD" );
        wagonNotifierUrl = getProperty( "WAGON_NOTIFIER_URL" );
        wagonServerId = getProperty( "WAGON_SERVER_ID" );
    }

    public void testAddValidMailProjectNotifier()
        throws Exception
    {
        goToProjectNotifier( projectGroupName, projectName );
        addMailNotifier( projectGroupName, projectName, mailNotifierAddress, true );
    }

    public void testAddValidMailProjectNotifierWithInvalidValue()
        throws Exception
    {
        String mailNotifierAddress = "<script>alert('xss')</script>";
        goToProjectNotifier( projectGroupName, projectName );
        addMailNotifier( projectGroupName, projectName, mailNotifierAddress, false );
        assertTextPresent( "Address is invalid" );
    }

    @Test( dependsOnMethods = { "testAddValidMailProjectNotifier" } )
    public void testEditValidMailProjectNotifier()
        throws Exception
    {
        String newMail = "newmail@mail.com";
        goToProjectInformationPage( projectGroupName, projectName );
        editMailNotifier( projectGroupName, projectName, mailNotifierAddress, newMail, true );
        editMailNotifier( projectGroupName, projectName, newMail, mailNotifierAddress, true );
    }

    @Test( dependsOnMethods = { "testAddValidMailProjectNotifier" } )
    public void testEditInvalidMailProjectNotifier()
        throws Exception
    {
        goToProjectInformationPage( projectGroupName, projectName );
        editMailNotifier( projectGroupName, projectName, mailNotifierAddress, "invalid_email_add", false );
    }

    public void testAddInvalidMailProjectNotifier()
        throws Exception
    {
        goToProjectNotifier( projectGroupName, projectName );
        addMailNotifier( projectGroupName, projectName, "invalid_email_add", false );
    }

    public void testAddValidMailGroupNotifier()
        throws Exception
    {
        goToGroupNotifier( projectGroupName, projectGroupId, projectGroupDescription );
        addMailNotifier( projectGroupName, null, mailNotifierAddress, true );
    }

    @Test( dependsOnMethods = { "testAddValidMailGroupNotifier" } )
    public void testEditValidMailGroupNotifier()
        throws Exception
    {
        String newMail = "newmail@mail.com";
        showProjectGroup( projectGroupName, projectGroupId, projectGroupDescription );
        clickLinkWithText( "Notifiers" );
        assertGroupNotifierPage( projectGroupName );
        editMailNotifier( projectGroupName, null, mailNotifierAddress, newMail, true );
        editMailNotifier( projectGroupName, null, newMail, mailNotifierAddress, true );
    }

    @Test( dependsOnMethods = { "testAddValidMailGroupNotifier" } )
    public void testEditInvalidMailGroupNotifier()
        throws Exception
    {
        showProjectGroup( projectGroupName, projectGroupId, projectGroupDescription );
        clickLinkWithText( "Notifiers" );
        assertGroupNotifierPage( projectGroupName );
        editMailNotifier( projectGroupName, null, mailNotifierAddress, "invalid_email_add", false );
    }

    public void testAddInvalidMailGroupNotifier()
        throws Exception
    {
        goToGroupNotifier( projectGroupName, projectGroupId, projectGroupDescription );
        addMailNotifier( projectGroupName, null, "invalid_email_add", false );
    }

    public void testAddValidIrcProjectNotifier()
        throws Exception
    {
        goToProjectNotifier( projectGroupName, projectName );
        addIrcNotifier( projectGroupName, projectName, ircNotifierHost, ircNotifierChannel, true );
    }

    public void testAddProjectNotifierWithInvalidValues()
        throws Exception
    {
        String ircNotifierHost = "!@#$<>?etc";
        String ircNotifierChannel = "!@#$<>?etc";
        goToProjectNotifier( projectGroupName, projectName );
        addIrcNotifier( projectGroupName, projectName, ircNotifierHost, ircNotifierChannel, false );
        assertTextPresent( "Host contains invalid character" );
        assertTextPresent( "Channel contains invalid character" );
    }

    @Test( dependsOnMethods = { "testAddValidIrcProjectNotifier" } )
    public void testEditValidIrcProjectNotifier()
        throws Exception
    {
        String newHost = "new.test.com";
        String newChannel = "new_test_channel";
        goToProjectInformationPage( projectGroupName, projectName );
        editIrcNotifier( projectGroupName, projectName, ircNotifierHost, ircNotifierChannel, newHost, newChannel, true );
        editIrcNotifier( projectGroupName, projectName, newHost, newChannel, ircNotifierHost, ircNotifierChannel, true );
    }

    @Test( dependsOnMethods = { "testAddValidIrcProjectNotifier" } )
    public void testEditInvalidIrcProjectNotifier()
        throws Exception
    {
        goToProjectInformationPage( projectGroupName, projectName );
        editIrcNotifier( projectGroupName, projectName, ircNotifierHost, ircNotifierChannel, "", "", false );
    }

    public void testAddInvalidIrcProjectNotifier()
        throws Exception
    {
        goToProjectNotifier( projectGroupName, projectName );
        addIrcNotifier( projectGroupName, projectName, "", "", false );
        assertTextPresent( "Host is required" );
        assertTextPresent( "Channel is required" );
    }

    public void testAddValidIrcGroupNotifier()
        throws Exception
    {
        goToGroupNotifier( projectGroupName, projectGroupId, projectGroupDescription );
        addIrcNotifier( projectGroupName, null, ircNotifierHost, ircNotifierChannel, true );
    }

    @Test( dependsOnMethods = { "testAddValidIrcGroupNotifier" } )
    public void testEditValidIrcGroupNotifier()
        throws Exception
    {
        String newHost = "new.test.com";
        String newChannel = "new_test_channel";
        showProjectGroup( projectGroupName, projectGroupId, projectGroupDescription );
        clickLinkWithText( "Notifiers" );
        assertGroupNotifierPage( projectGroupName );
        editIrcNotifier( projectGroupName, null, ircNotifierHost, ircNotifierChannel, newHost, newChannel, true );
        editIrcNotifier( projectGroupName, null, newHost, newChannel, ircNotifierHost, ircNotifierChannel, true );
    }

    @Test( dependsOnMethods = { "testAddValidIrcGroupNotifier" } )
    public void testEditInvalidIrcGroupNotifier()
        throws Exception
    {
        showProjectGroup( projectGroupName, projectGroupId, projectGroupDescription );
        clickLinkWithText( "Notifiers" );
        assertGroupNotifierPage( projectGroupName );
        editIrcNotifier( projectGroupName, null, ircNotifierHost, ircNotifierChannel, "", "", false );
    }

    public void testAddInvalidIrcGroupNotifier()
        throws Exception
    {
        goToGroupNotifier( projectGroupName, projectGroupId, projectGroupDescription );
        addIrcNotifier( projectGroupName, null, "", "", false );
        assertTextPresent( "Host is required" );
        assertTextPresent( "Channel is required" );
    }

    public void testAddValidJabberProjectNotifier()
        throws Exception
    {
        goToProjectNotifier( projectGroupName, projectName );
        addJabberNotifier( projectGroupName, projectName, jabberNotifierHost, jabberNotifierLogin,
                           jabberNotifierPassword, jabberNotifierAddress, true );
    }

    public void testAddJabberProjectNotifierWithInvalidValues()
        throws Exception
    {
        String jabberNotifierHost = "!@#$<>?etc";
        String jabberNotifierAddress = "!@#$<>?etc";
        goToProjectNotifier( projectGroupName, projectName );
        addJabberNotifier( projectGroupName, projectName, jabberNotifierHost, jabberNotifierLogin,
                           jabberNotifierPassword, jabberNotifierAddress, false );
        assertTextPresent( "Host contains invalid characters" );
        assertTextPresent( "Address is invalid" );
    }

    @Test( dependsOnMethods = { "testAddValidJabberProjectNotifier" } )
    public void testEditValidJabberProjectNotifier()
        throws Exception
    {
        String newHost = "new_test";
        String newLogin = "new_test_login";
        String newPassword = "new_password";
        String newAddress = "new.test@mail.com";
        goToProjectInformationPage( projectGroupName, projectName );
        editJabberNotifier( projectGroupName, projectName, jabberNotifierHost, jabberNotifierLogin,
                            jabberNotifierAddress, newHost, newLogin, newPassword, newAddress, true );
        editJabberNotifier( projectGroupName, projectName, newHost, newLogin, newAddress, jabberNotifierHost,
                            jabberNotifierLogin, jabberNotifierPassword, jabberNotifierAddress, true );
    }

    @Test( dependsOnMethods = { "testAddValidJabberProjectNotifier" } )
    public void testEditInvalidJabberProjectNotifier()
        throws Exception
    {
        goToProjectInformationPage( projectGroupName, projectName );
        editJabberNotifier( projectGroupName, projectName, jabberNotifierHost, jabberNotifierLogin,
                            jabberNotifierAddress, "", "", "", "", false );
    }

    public void testAddInvalidJabberProjectNotifier()
        throws Exception
    {
        goToProjectNotifier( projectGroupName, projectName );
        addJabberNotifier( projectGroupName, projectName, "", "", "", "", false );
        assertTextPresent( "Host is required" );
        assertTextPresent( "Login is required" );
        assertTextPresent( "Password is required" );
        assertTextPresent( "Address is required" );
    }

    public void testAddValidJabberGroupNotifier()
        throws Exception
    {
        goToGroupNotifier( projectGroupName, projectGroupId, projectGroupDescription );
        addJabberNotifier( projectGroupName, null, jabberNotifierHost, jabberNotifierLogin, jabberNotifierPassword,
                           jabberNotifierAddress, true );
    }

    @Test( dependsOnMethods = { "testAddValidJabberGroupNotifier" } )
    public void testEditValidJabberGroupNotifier()
        throws Exception
    {
        String newHost = "new_test";
        String newLogin = "new_test_login";
        String newPassword = "new_password";
        String newAddress = "new.test@mail.com";
        showProjectGroup( projectGroupName, projectGroupId, projectGroupDescription );
        clickLinkWithText( "Notifiers" );
        assertGroupNotifierPage( projectGroupName );
        editJabberNotifier( projectGroupName, null, jabberNotifierHost, jabberNotifierLogin, jabberNotifierAddress,
                            newHost, newLogin, newPassword, newAddress, true );
        editJabberNotifier( projectGroupName, null, newHost, newLogin, newAddress, jabberNotifierHost,
                            jabberNotifierLogin, jabberNotifierPassword, jabberNotifierAddress, true );
    }

    @Test( dependsOnMethods = { "testAddValidJabberGroupNotifier" } )
    public void testEditInvalidJabberGroupNotifier()
        throws Exception
    {
        showProjectGroup( projectGroupName, projectGroupId, projectGroupDescription );
        clickLinkWithText( "Notifiers" );
        assertGroupNotifierPage( projectGroupName );
        editJabberNotifier( projectGroupName, null, jabberNotifierHost, jabberNotifierLogin, jabberNotifierAddress, "",
                            "", "", "", false );
    }

    public void testAddInvalidJabberGroupNotifier()
        throws Exception
    {
        goToGroupNotifier( projectGroupName, projectGroupId, projectGroupDescription );
        addJabberNotifier( projectGroupName, null, "", "", "", "", false );
        assertTextPresent( "Host is required" );
        assertTextPresent( "Login is required" );
        assertTextPresent( "Password is required" );
        assertTextPresent( "Address is required" );
    }

    public void testAddValidMsnProjectNotifier()
        throws Exception
    {
        goToProjectNotifier( projectGroupName, projectName );
        addMsnNotifier( projectGroupName, projectName, msnNotifierLogin, msnNotifierPassword, msnNotifierAddress, true );
    }

    public void testAddMsnProjectNotifierWithInvalidValues()
        throws Exception
    {
        String msnNotifierAddress = "!@#$<>?etc";
        goToProjectNotifier( projectGroupName, projectName );
        addMsnNotifier( projectGroupName, projectName, msnNotifierLogin, msnNotifierPassword, msnNotifierAddress,
                        false );
        assertTextPresent( "Address is invalid" );
    }

    @Test( dependsOnMethods = { "testAddValidMsnProjectNotifier" } )
    public void testEditValidMsnProjectNotifier()
        throws Exception
    {
        String newLogin = "new_test_login";
        String newPassword = "new_password";
        String newAddress = "new.test@mail.com";
        goToProjectInformationPage( projectGroupName, projectName );
        editMsnNotifier( projectGroupName, projectName, msnNotifierLogin, msnNotifierAddress, newLogin, newPassword,
                         newAddress, true );
        editMsnNotifier( projectGroupName, projectName, newLogin, newAddress, msnNotifierLogin, msnNotifierPassword,
                         msnNotifierAddress, true );
    }

    @Test( dependsOnMethods = { "testAddValidMsnProjectNotifier" } )
    public void testEditInvalidMsnProjectNotifier()
        throws Exception
    {
        goToProjectInformationPage( projectGroupName, projectName );
        editMsnNotifier( projectGroupName, projectName, msnNotifierLogin, msnNotifierAddress, "", "", "", false );
    }

    public void testAddInvalidMsnProjectNotifier()
        throws Exception
    {
        goToProjectNotifier( projectGroupName, projectName );
        addMsnNotifier( projectGroupName, projectName, "", "", "", false );
        assertTextPresent( "Login is required" );
        assertTextPresent( "Password is required" );
        assertTextPresent( "Address is required" );
    }

    public void testAddValidMsnGroupNotifier()
        throws Exception
    {
        goToGroupNotifier( projectGroupName, projectGroupId, projectGroupDescription );
        addMsnNotifier( projectGroupName, null, msnNotifierLogin, msnNotifierPassword, msnNotifierAddress, true );
    }

    @Test( dependsOnMethods = { "testAddValidMsnGroupNotifier" } )
    public void testEditValidMsnGroupNotifier()
        throws Exception
    {
        String newLogin = "new_test_login";
        String newPassword = "new_password";
        String newAddress = "new.test@mail.com";
        showProjectGroup( projectGroupName, projectGroupId, projectGroupDescription );
        clickLinkWithText( "Notifiers" );
        assertGroupNotifierPage( projectGroupName );
        editMsnNotifier( projectGroupName, null, msnNotifierLogin, msnNotifierAddress, newLogin, newPassword,
                         newAddress, true );
        editMsnNotifier( projectGroupName, null, newLogin, newAddress, msnNotifierLogin, msnNotifierPassword,
                         msnNotifierAddress, true );
    }

    @Test( dependsOnMethods = { "testAddValidMsnGroupNotifier" } )
    public void testEditInvalidMsnGroupNotifier()
        throws Exception
    {
        showProjectGroup( projectGroupName, projectGroupId, projectGroupDescription );
        clickLinkWithText( "Notifiers" );
        assertGroupNotifierPage( projectGroupName );
        editMsnNotifier( projectGroupName, null, msnNotifierLogin, msnNotifierAddress, "", "", "", false );
    }

    public void testAddInvalidMsnGroupNotifier()
        throws Exception
    {
        goToGroupNotifier( projectGroupName, projectGroupId, projectGroupDescription );
        addMsnNotifier( projectGroupName, null, "", "", "", false );
        assertTextPresent( "Login is required" );
        assertTextPresent( "Password is required" );
        assertTextPresent( "Address is required" );
    }

    public void testAddValidWagonProjectNotifier()
        throws Exception
    {
        goToProjectNotifier( projectGroupName, projectName );
        addWagonNotifierPage( projectGroupName, projectName, wagonNotifierUrl, wagonServerId, true );
    }

    public void testAddInvalidURLWagonProjectNotifier()
        throws Exception
    {
        String wagonNotifierUrl = "!@#$<>?etc";
        goToProjectNotifier( projectGroupName, projectName );
        addWagonNotifierPage( projectGroupName, projectName, wagonNotifierUrl, wagonServerId, false );
        assertTextPresent( "Destination URL is invalid" );
    }

    @Test( dependsOnMethods = { "testAddValidWagonProjectNotifier" } )
    public void testEditValidWagonProjectNotifier()
        throws Exception
    {
        String newId = "newId";
        goToProjectInformationPage( projectGroupName, projectName );
        editWagonNotifier( projectGroupName, projectName, wagonNotifierUrl, wagonServerId, wagonNotifierUrl, newId,
                           true );
        editWagonNotifier( projectGroupName, projectName, wagonNotifierUrl, newId, wagonNotifierUrl, wagonServerId,
                           true );
    }

    @Test( dependsOnMethods = { "testAddValidWagonProjectNotifier" } )
    public void testEditInvalidWagonProjectNotifier()
        throws Exception
    {
        goToProjectInformationPage( projectGroupName, projectName );
        editWagonNotifier( projectGroupName, projectName, wagonNotifierUrl, wagonServerId, "", "", false );
    }

    public void testAddInvalidWagonProjectNotifier()
        throws Exception
    {
        goToProjectNotifier( projectGroupName, projectName );
        addWagonNotifierPage( projectGroupName, projectName, "", "", false );
        assertTextPresent( "Destination URL is required" );
        assertTextPresent( "Server Id is required" );
    }

    public void testAddValidWagonGroupNotifier()
        throws Exception
    {
        goToGroupNotifier( projectGroupName, projectGroupId, projectGroupDescription );
        addWagonNotifierPage( projectGroupName, null, wagonNotifierUrl, wagonServerId, true );
    }

    @Test( dependsOnMethods = { "testAddValidWagonGroupNotifier" } )
    public void testEditValidWagonGroupNotifier()
        throws Exception
    {
        String newId = "newId";
        showProjectGroup( projectGroupName, projectGroupId, projectGroupDescription );
        clickLinkWithText( "Notifiers" );
        assertGroupNotifierPage( projectGroupName );
        editWagonNotifier( projectGroupName, null, wagonNotifierUrl, wagonServerId, wagonNotifierUrl, newId, true );
        editWagonNotifier( projectGroupName, null, wagonNotifierUrl, newId, wagonNotifierUrl, wagonServerId, true );
    }

    @Test( dependsOnMethods = { "testAddValidWagonGroupNotifier" } )
    public void testEditInvalidWagonGroupNotifier()
        throws Exception
    {
        showProjectGroup( projectGroupName, projectGroupId, projectGroupDescription );
        clickLinkWithText( "Notifiers" );
        assertGroupNotifierPage( projectGroupName );
        editWagonNotifier( projectGroupName, null, wagonNotifierUrl, wagonServerId, "", "", false );
    }

    public void testAddInvalidWagonGroupNotifier()
        throws Exception
    {
        goToGroupNotifier( projectGroupName, projectGroupId, projectGroupDescription );
        addWagonNotifierPage( projectGroupName, null, "", "", false );
        assertTextPresent( "Destination URL is required" );
        assertTextPresent( "Server Id is required" );
    }

    @Test( dependsOnMethods = { "testEditValidMailGroupNotifier", "testEditInvalidMailGroupNotifier" } )
    public void testDeleteGroupNotifier()
        throws Exception
    {
        showProjectGroup( projectGroupName, projectGroupId, projectGroupDescription );
        clickLinkWithText( "Notifiers" );
        assertGroupNotifierPage( projectGroupName );
        clickLinkWithXPath( "(//a[contains(@href,'deleteProjectGroupNotifier') and contains(@href,'mail')])//img" );
        assertButtonWithValuePresent( "Delete" );
        assertButtonWithValuePresent( "Cancel" );
        clickButtonWithValue( "Delete" );
        assertGroupNotifierPage( projectGroupName );
    }

    @Test( dependsOnMethods = { "testEditValidMailProjectNotifier", "testEditInvalidMailProjectNotifier" } )
    public void testDeleteProjectNotifier()
        throws Exception
    {
        goToProjectInformationPage( projectGroupName, projectName );
        // Delete
        clickLinkWithXPath( "(//a[contains(@href,'deleteProjectNotifier') and contains(@href,'mail')])//img" );
        assertButtonWithValuePresent( "Delete" );
        assertButtonWithValuePresent( "Cancel" );
        clickButtonWithValue( "Delete" );
        assertProjectInformationPage();
    }

    public void testDeleteProjectNotifierFromGroupNotifierPage()
        throws Exception
    {
        String mailNotifierAddress = "testDeleteProjectNotifierFromGroupNotifierPage@test.com";

        goToProjectGroupsSummaryPage();
        goToProjectNotifier( projectGroupName, projectName );
        addMailNotifier( projectGroupName, projectName, mailNotifierAddress, true );

        showProjectGroup( projectGroupName, projectGroupId, projectGroupDescription );
        clickLinkWithText( "Notifiers" );
        assertGroupNotifierPage( projectGroupName );

        // Delete
        clickLinkWithXPath( "//preceding::td[text()='" + mailNotifierAddress + "']//following::img[@alt='Delete']" );
        assertButtonWithValuePresent( "Delete" );
        assertButtonWithValuePresent( "Cancel" );
        clickButtonWithValue( "Delete" );
        assertGroupNotifierPage( projectGroupName );

        assertTextNotPresent( mailNotifierAddress );
    }

    protected void assertGroupNotifierPage( String projectGroupName )
    {
        assertTextPresent( "Project Group Notifiers of group " + projectGroupName );
    }

    void assertAddNotifierPage()
    {
        assertPage( "Continuum - Add Notifier" );
        assertTextPresent( "Add Notifier" );
        assertTextPresent( "Type" );
        assertElementPresent( "notifierType" );
        assertElementPresent( "Cancel" );
    }

    void assertAddEditMailNotifierPage()
    {
        assertPage( "Continuum - Add/Edit Mail Notifier" );
        assertTextPresent( "Add/Edit Mail Notifier" );
        assertTextPresent( "Mail Recipient Address" );
        assertTextPresent( "Send a mail to latest committers" );
        assertTextPresent( "Send on Success" );
        assertTextPresent( "Send on Failure" );
        assertTextPresent( "Send on Error" );
        assertTextPresent( "Send on Warning" );
        assertTextPresent( "Send on SCM Failure" );
        assertElementPresent( "address" );
        assertElementPresent( "Cancel" );
    }

    void assertAddEditIrcNotifierPage()
    {
        assertPage( "Continuum - Add/Edit IRC Notifier" );

        assertTextPresent( "IRC Host" );
        assertElementPresent( "host" );

        assertTextPresent( "IRC port" );
        assertElementPresent( "port" );

        assertTextPresent( "IRC channel" );
        assertElementPresent( "channel" );

        assertTextPresent( "Nick Name" );
        assertElementPresent( "nick" );

        assertTextPresent( "Alternate Nick Name" );
        assertElementPresent( "alternateNick" );

        assertTextPresent( "User Name" );
        assertElementPresent( "username" );

        assertTextPresent( "Full Name" );
        assertElementPresent( "fullName" );

        assertTextPresent( "Password" );
        assertElementPresent( "password" );

        assertTextPresent( "SSL" );
        assertTextPresent( "Send on Success" );
        assertTextPresent( "Send on Failure" );
        assertTextPresent( "Send on Error" );
        assertTextPresent( "Send on Warning" );
        assertTextPresent( "Send on SCM Failure" );
    }

    void assertAddEditJabberPage()
    {
        assertPage( "Continuum - Add/Edit Jabber Notifier" );

        assertTextPresent( "Jabber Host" );
        assertElementPresent( "host" );
        assertTextPresent( "Jabber port" );
        assertElementPresent( "port" );
        assertTextPresent( "Jabber login" );
        assertElementPresent( "login" );
        assertTextPresent( "Jabber Password" );
        assertElementPresent( "password" );
        assertTextPresent( "Jabber Domain Name" );
        assertElementPresent( "domainName" );
        assertTextPresent( "Jabber Recipient Address" );
        assertElementPresent( "address" );

        assertTextPresent( "Is it a SSL connection?" );
        assertTextPresent( "Is it a Jabber group?" );
        assertTextPresent( "Send on Success" );
        assertTextPresent( "Send on Failure" );
        assertTextPresent( "Send on Error" );
        assertTextPresent( "Send on Warning" );
        assertTextPresent( "Send on SCM Failure" );
    }

    void assertAddEditMsnPage()
    {
        assertPage( "Continuum - Add/Edit MSN Notifier" );

        assertTextPresent( "MSN login" );
        assertElementPresent( "login" );
        assertTextPresent( "MSN Password" );
        assertElementPresent( "password" );
        assertTextPresent( "MSN Recipient Address" );
        assertElementPresent( "address" );

        assertTextPresent( "Send on Success" );
        assertTextPresent( "Send on Failure" );
        assertTextPresent( "Send on Error" );
        assertTextPresent( "Send on Warning" );
        assertTextPresent( "Send on SCM Failure" );
    }

    void assertAddEditWagonPage()
    {
        assertPage( "Continuum - Add/Edit Wagon Notifier" );

        assertTextPresent( "Project Site URL" );
        assertTextPresent( "Server Id (defined in your settings.xml for authentication)" );
        assertElementPresent( "url" );
        assertElementPresent( "id" );
        assertTextPresent( "Send on Success" );
        assertTextPresent( "Send on Failure" );
        assertTextPresent( "Send on Error" );
        assertTextPresent( "Send on Warning" );
    }

    protected void goToGroupNotifier( String projectGroupName, String projectGroupId, String projectGroupDescription )
    {
        showProjectGroup( projectGroupName, projectGroupId, projectGroupDescription );
        clickLinkWithText( "Notifiers" );
        assertGroupNotifierPage( projectGroupName );
        clickButtonWithValue( "Add" );
        assertAddNotifierPage();
    }

    protected void goToProjectNotifier( String projectGroupName, String projectName )
    {
        goToProjectInformationPage( projectGroupName, projectName );
        clickLinkWithXPath( "//input[contains(@id,'addProjectNotifier') and @type='submit']" );
        assertAddNotifierPage();
    }

    protected void addMailNotifier( String projectGroupName, String projectName, String email, boolean isValid )
    {
        selectValue( "//select", "Mail" );
        clickButtonWithValue( "Submit" );
        assertAddEditMailNotifierPage();
        setFieldValue( "address", email );
        clickButtonWithValue( "Save" );

        if ( !isValid )
        {
            assertTextPresent( "Address is invalid" );
        }
        else if ( projectName != null )
        {
            assertProjectInformationPage();
        }
        else
        {
            assertGroupNotifierPage( projectGroupName );
        }
    }

    protected void editMailNotifier( String projectGroupName, String projectName, String oldMail, String newMail,
                                     boolean isValid )
    {
        if ( projectName == null )
        {
            clickLinkWithXPath( "(//a[contains(@href,'editProjectGroupNotifier') and contains(@href,'mail')])//img" );
        }
        else
        {
            clickLinkWithXPath( "(//a[contains(@href,'editProjectNotifier') and contains(@href,'mail')])//img" );
        }
        assertAddEditMailNotifierPage();
        assertFieldValue( oldMail, "address" );
        setFieldValue( "address", newMail );
        clickButtonWithValue( "Save" );

        if ( !isValid )
        {
            assertTextPresent( "Address is invalid" );
        }
        else if ( projectName != null )
        {
            assertProjectInformationPage();
        }
        else
        {
            assertGroupNotifierPage( projectGroupName );
        }
    }

    protected void addIrcNotifier( String projectGroupName, String projectName, String host, String channel,
                                   boolean isValid )
    {
        selectValue( "//select", "IRC" );
        clickButtonWithValue( "Submit" );
        assertAddEditIrcNotifierPage();
        setFieldValue( "host", host );
        setFieldValue( "channel", channel );

        clickButtonWithValue( "Save" );
        if ( isValid )
        {
            if ( projectName != null )
            {
                assertProjectInformationPage();
            }
            else
            {
                assertGroupNotifierPage( projectGroupName );
            }
        }
    }

    protected void editIrcNotifier( String projectGroupName, String projectName, String oldHost, String oldChannel,
                                    String newHost, String newChannel, boolean isValid )
    {
        if ( projectName == null )
        {
            clickLinkWithXPath( "(//a[contains(@href,'editProjectGroupNotifier') and contains(@href,'irc')])//img" );
        }
        else
        {
            clickLinkWithXPath( "(//a[contains(@href,'editProjectNotifier') and contains(@href,'irc')])//img" );
        }
        assertAddEditIrcNotifierPage();
        assertFieldValue( oldHost, "host" );
        assertFieldValue( oldChannel, "channel" );
        setFieldValue( "host", newHost );
        setFieldValue( "channel", newChannel );
        clickButtonWithValue( "Save" );

        if ( !isValid )
        {
            assertTextPresent( "Host is required" );
            assertTextPresent( "Channel is required" );
        }
        else if ( projectName != null )
        {
            assertProjectInformationPage();
        }
        else
        {
            assertGroupNotifierPage( projectGroupName );
        }
    }

    protected void addJabberNotifier( String projectGroupName, String projectName, String host, String login,
                                      String password, String address, boolean isValid )
    {
        selectValue( "//select", "Jabber" );
        clickButtonWithValue( "Submit" );
        assertAddEditJabberPage();
        setFieldValue( "host", host );
        setFieldValue( "login", login );
        setFieldValue( "password", password );
        setFieldValue( "address", address );
        clickButtonWithValue( "Save" );

        if ( isValid )
        {
            if ( projectName != null )
            {
                assertProjectInformationPage();
            }
            else
            {
                assertGroupNotifierPage( projectGroupName );
            }
        }
    }

    protected void editJabberNotifier( String projectGroupName, String projectName, String oldHost, String oldLogin,
                                       String oldAddress, String newHost, String newLogin, String newPassword,
                                       String newAddress, boolean isValid )
    {
        if ( projectName == null )
        {
            clickLinkWithXPath( "(//a[contains(@href,'editProjectGroupNotifier') and contains(@href,'jabber')])//img" );
        }
        else
        {
            clickLinkWithXPath( "(//a[contains(@href,'editProjectNotifier') and contains(@href,'jabber')])//img" );
        }
        assertAddEditJabberPage();
        assertFieldValue( oldHost, "host" );
        assertFieldValue( oldLogin, "login" );
        assertFieldValue( oldAddress, "address" );
        setFieldValue( "host", newHost );
        setFieldValue( "login", newLogin );
        setFieldValue( "password", newPassword );
        setFieldValue( "address", newAddress );
        clickButtonWithValue( "Save" );

        if ( !isValid )
        {
            assertTextPresent( "Host is required" );
            assertTextPresent( "Login is required" );
            assertTextPresent( "Password is required" );
            assertTextPresent( "Address is required" );
        }
        else if ( projectName != null )
        {
            assertProjectInformationPage();
        }
        else
        {
            assertGroupNotifierPage( projectGroupName );
        }
    }

    protected void addMsnNotifier( String projectGroupName, String projectName, String login, String password,
                                   String recipientAddress, boolean isValid )
    {
        selectValue( "//select", "MSN" );
        clickButtonWithValue( "Submit" );
        assertAddEditMsnPage();
        setFieldValue( "login", login );
        setFieldValue( "password", password );
        setFieldValue( "address", recipientAddress );
        clickButtonWithValue( "Save" );

        if ( isValid )
        {
            if ( projectName != null )
            {
                assertProjectInformationPage();
            }
            else
            {
                assertGroupNotifierPage( projectGroupName );
            }
        }
    }

    protected void editMsnNotifier( String projectGroupName, String projectName, String oldLogin, String oldAddress,
                                    String newLogin, String newPassword, String newAddress, boolean isValid )
    {
        if ( projectName == null )
        {
            clickLinkWithXPath( "(//a[contains(@href,'editProjectGroupNotifier') and contains(@href,'msn')])//img" );
        }
        else
        {
            clickLinkWithXPath( "(//a[contains(@href,'editProjectNotifier') and contains(@href,'msn')])//img" );
        }
        assertAddEditMsnPage();
        assertFieldValue( oldLogin, "login" );
        assertFieldValue( oldAddress, "address" );
        setFieldValue( "login", newLogin );
        setFieldValue( "password", newPassword );
        setFieldValue( "address", newAddress );
        clickButtonWithValue( "Save" );

        if ( !isValid )
        {
            assertTextPresent( "Login is required" );
            assertTextPresent( "Password is required" );
            assertTextPresent( "Address is required" );
        }
        else if ( projectName != null )
        {
            assertProjectInformationPage();
        }
        else
        {
            assertGroupNotifierPage( projectGroupName );
        }
    }

    protected void addWagonNotifierPage( String projectGroupName, String projectName, String siteUrl, String serverId,
                                         boolean isValid )
    {
        selectValue( "//select", "Wagon" );
        clickButtonWithValue( "Submit" );
        assertAddEditWagonPage();
        setFieldValue( "url", siteUrl );
        setFieldValue( "id", serverId );
        clickButtonWithValue( "Save" );

        if ( isValid )
        {
            if ( projectName != null )
            {
                assertProjectInformationPage();
            }
            else
            {
                assertGroupNotifierPage( projectGroupName );
            }
        }
    }

    protected void editWagonNotifier( String projectGroupName, String projectName, String oldUrl, String oldId,
                                      String newUrl, String newId, boolean isValid )
    {
        if ( projectName == null )
        {
            clickLinkWithXPath( "(//a[contains(@href,'editProjectGroupNotifier') and contains(@href,'wagon')])//img" );
        }
        else
        {
            clickLinkWithXPath( "(//a[contains(@href,'editProjectNotifier') and contains(@href,'wagon')])//img" );
        }
        assertAddEditWagonPage();
        assertFieldValue( oldUrl, "url" );
        assertFieldValue( oldId, "id" );
        setFieldValue( "url", newUrl );
        setFieldValue( "id", newId );
        clickButtonWithValue( "Save" );

        if ( !isValid )
        {
            assertTextPresent( "Destination URL is required" );
            assertTextPresent( "Server Id is required" );
        }
        else if ( projectName != null )
        {
            assertProjectInformationPage();
        }
        else
        {
            assertGroupNotifierPage( projectGroupName );
        }
    }
}
