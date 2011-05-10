package org.apache.continuum.web.test.parent;

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

/**
 * @author José Morales Martínez
 * @version $Id$
 */
public abstract class AbstractNotifierTest
    extends AbstractContinuumTest
{
    public void assertGroupNotifierPage( String projectGroupName )
    {
        assertTextPresent( "Project Group Notifiers of group " + projectGroupName );
    }

    public void assertProjectNotifierPage()
    {
        assertTextPresent( "Add Notifier" );
    }

    public void assertAddNotifierPage()
    {
        assertPage( "Continuum - Add Notifier" );
        assertTextPresent( "Add Notifier" );
        assertTextPresent( "Type" );
        assertElementPresent( "notifierType" );
        assertElementPresent( "Cancel" );
    }

    public void assertAddEditMailNotifierPage()
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

    public void assertAddEditIrcNotifierPage()
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

    public void assertAddEditJabberPage()
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

    public void assertAddEditMsnPage()
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

    public void assertAddEditWagonPage()
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

    public void goToGroupNotifier( String projectGroupName, String projectGroupId, String projectGroupDescription )
        throws Exception
    {
        showProjectGroup( projectGroupName, projectGroupId, projectGroupDescription );
        clickLinkWithText( "Notifiers" );
        assertGroupNotifierPage( projectGroupName );
        clickButtonWithValue( "Add" );
        assertAddNotifierPage();
    }

    public void goToProjectNotifier( String projectGroupName, String projectName )
        throws Exception
    {
        goToProjectInformationPage( projectGroupName, projectName );
        clickLinkWithXPath( "//input[contains(@id,'addProjectNotifier') and @type='submit']" );
        assertAddNotifierPage();
    }

    public void addMailNotifier( String projectGroupName, String projectName, String email, boolean isValid )
        throws Exception
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

    public void editMailNotifier( String projectGroupName, String projectName, String oldMail, String newMail,
                                  boolean isValid )
        throws Exception
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

    public void addIrcNotifier( String projectGroupName, String projectName, String host, String channel,
                                boolean isValid )
        throws Exception
    {
        selectValue( "//select", "IRC" );
        clickButtonWithValue( "Submit" );
        assertAddEditIrcNotifierPage();
        setFieldValue( "host", host );
        setFieldValue( "channel", channel );

        clickButtonWithValue( "Save" );
        if ( !isValid )
        {
            return;
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

    public void editIrcNotifier( String projectGroupName, String projectName, String oldHost, String oldChannel,
                                 String newHost, String newChannel, boolean isValid )
        throws Exception
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

    public void addJabberNotifier( String projectGroupName, String projectName, String host, String login,
                                   String password, String address, boolean isValid )
        throws Exception
    {
        selectValue( "//select", "Jabber" );
        clickButtonWithValue( "Submit" );
        assertAddEditJabberPage();
        setFieldValue( "host", host );
        setFieldValue( "login", login );
        setFieldValue( "password", password );
        setFieldValue( "address", address );
        clickButtonWithValue( "Save" );

        if ( !isValid )
        {
            return;
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

    public void editJabberNotifier( String projectGroupName, String projectName, String oldHost, String oldLogin,
                                    String oldAddress, String newHost, String newLogin, String newPassword,
                                    String newAddress, boolean isValid )
        throws Exception
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

    public void addMsnNotifier( String projectGroupName, String projectName, String login, String password,
                                String recipientAddress, boolean isValid )
        throws Exception
    {
        selectValue( "//select", "MSN" );
        clickButtonWithValue( "Submit" );
        assertAddEditMsnPage();
        setFieldValue( "login", login );
        setFieldValue( "password", password );
        setFieldValue( "address", recipientAddress );
        clickButtonWithValue( "Save" );

        if ( !isValid )
        {
            return;
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

    public void editMsnNotifier( String projectGroupName, String projectName, String oldLogin, String oldAddress,
                                 String newLogin, String newPassword, String newAddress, boolean isValid )
        throws Exception
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

    public void addWagonNotifierPage( String projectGroupName, String projectName, String siteUrl, String serverId,
                                      boolean isValid )
        throws Exception
    {
        selectValue( "//select", "Wagon" );
        clickButtonWithValue( "Submit" );
        assertAddEditWagonPage();
        setFieldValue( "url", siteUrl );
        setFieldValue( "id", serverId );
        clickButtonWithValue( "Save" );

        if ( !isValid )
        {
            return;
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

    public void editWagonNotifier( String projectGroupName, String projectName, String oldUrl, String oldId,
                                   String newUrl, String newId, boolean isValid )
        throws Exception
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
