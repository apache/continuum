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
public abstract class AbstractPurgeTest
    extends AbstractAdminTest
{
    public void goToGeneralPurgePage()
    {
        clickLinkWithText( "Purge Configurations" );
        assertGeneralPurgePage();
    }

    public void assertGeneralPurgePage()
    {
        assertPage( "Continuum - Purge Configurations" );
        assertTextPresent( "Repository Purge Configurations" );
        assertTextPresent( "Directory Purge Configurations" );
        assertButtonWithValuePresent( "Add" );
    }

    public void removeRepositoryPurge( String purgeDescription )
    {
        goToGeneralPurgePage();
        clickLinkWithXPath( "(//a[contains(@href,'removePurgeConfig.action') and contains(@href, '" + purgeDescription
            + "')])//img" );
        assertTextPresent( "Delete Purge Configuration" );
        assertTextPresent( "Are you sure you want to delete Purge Configuration \"" + purgeDescription + "\"?" );
        assertButtonWithValuePresent( "Delete" );
        assertButtonWithValuePresent( "Cancel" );
        clickButtonWithValue( "Delete" );
        assertGeneralPurgePage();
    }

    public void removeDirectoryPurge( String purgeDescription )
    {
        goToGeneralPurgePage();
        clickLinkWithXPath( "(//a[contains(@href,'removePurgeConfig.action') and contains(@href, '" + purgeDescription
            + "')])//img" );
        assertTextPresent( "Delete Purge Configuration" );
        assertTextPresent( "Are you sure you want to delete Purge Configuration \"" + purgeDescription + "\"?" );
        assertButtonWithValuePresent( "Delete" );
        assertButtonWithValuePresent( "Cancel" );
        clickButtonWithValue( "Delete" );
        assertGeneralPurgePage();
    }

    public void assertAddRepositoryPurgePage()
    {
        assertPage( "Continuum - Add/Edit Purge Configuration" );
        assertTextPresent( "Add/Edit Purge Configuration" );
        assertTextPresent( "Repository" );
        assertElementPresent( "repositoryId" );
        assertTextPresent( "Days Older" );
        assertElementPresent( "daysOlder" );
        assertTextPresent( "Retention Count" );
        assertElementPresent( "retentionCount" );
        assertElementPresent( "deleteAll" );
        assertElementPresent( "deleteReleasedSnapshots" );
        assertElementPresent( "defaultPurgeConfiguration" );
        assertTextPresent( "Schedule" );
        assertElementPresent( "scheduleId" );
        assertTextPresent( "Description" );
        assertElementPresent( "description" );
        assertButtonWithValuePresent( "Save" );
        assertButtonWithValuePresent( "Cancel" );
    }

    public void assertAddEditDirectoryPurgePage()
    {
        assertPage( "Continuum - Add/Edit Purge Configuration" );
        assertTextPresent( "Add/Edit Purge Configuration" );
        assertTextPresent( "Directory Type" );
        assertElementPresent( "directoryType" );
        assertTextPresent( "Days Older" );
        assertElementPresent( "daysOlder" );
        assertTextPresent( "Retention Count" );
        assertElementPresent( "retentionCount" );
        assertElementPresent( "deleteAll" );
        assertElementPresent( "defaultPurgeConfiguration" );
        assertTextPresent( "Schedule" );
        assertElementPresent( "scheduleId" );
        assertTextPresent( "Description" );
        assertElementPresent( "description" );
        assertButtonWithValuePresent( "Save" );
        assertButtonWithValuePresent( "Cancel" );
    }

    public void goToAddRepositoryPurge()
    {
        goToGeneralPurgePage();
        assertGeneralPurgePage();
        clickLinkWithXPath( "//preceding::input[@value='repository' and @type='hidden']//following::input[@type='submit']" );
        assertAddRepositoryPurgePage();
    }

    public void goToEditRepositoryPurge( String daysOlder, String retentionCount, String description )
    {
        goToGeneralPurgePage();
        assertGeneralPurgePage();
        String xPath = "//preceding::td[text()='" + description + "']//following::img[@alt='Edit']";
        clickLinkWithXPath( xPath );
        assertAddRepositoryPurgePage();
        assertFieldValue( daysOlder, "daysOlder" );
        assertFieldValue( retentionCount, "retentionCount" );
        assertFieldValue( description, "description" );
    }

    public void goToEditDirectoryPurge( String daysOlder, String retentionCount, String description )
    {
        goToGeneralPurgePage();
        assertGeneralPurgePage();
        String xPath = "//preceding::td[text()='" + description + "']//following::img[@alt='Edit']";
        clickLinkWithXPath( xPath );
        assertAddEditDirectoryPurgePage();
        assertFieldValue( daysOlder, "daysOlder" );
        assertFieldValue( retentionCount, "retentionCount" );
        assertFieldValue( description, "description" );
    }

    public void addEditRepositoryPurge( String daysOlder, String retentionCount, String description, boolean success )
    {
        setFieldValue( "daysOlder", daysOlder );
        setFieldValue( "retentionCount", retentionCount );
        setFieldValue( "description", description );
        submit();
        if ( success )
        {
            assertGeneralPurgePage();
        }
        else
        {
            assertAddRepositoryPurgePage();
        }
    }

    public void goToAddDirectoryPurge()
    {
        goToGeneralPurgePage();
        assertGeneralPurgePage();
        clickLinkWithXPath( "//preceding::input[@value='directory' and @type='hidden']//following::input[@type='submit']" );
        assertAddEditDirectoryPurgePage();
    }

    public void addEditDirectoryPurge( String daysOlder, String retentionCount, String description, boolean success )
    {
        setFieldValue( "daysOlder", daysOlder );
        setFieldValue( "retentionCount", retentionCount );
        setFieldValue( "description", description );
        submit();
        if ( success )
        {
            assertGeneralPurgePage();
        }
        else
        {
            assertAddEditDirectoryPurgePage();
        }
    }
}
