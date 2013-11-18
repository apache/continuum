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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Based on ProjectGroupTest of Emmanuel Venisse test.
 *
 * @author José Morales Martínez
 * @version $Id$
 */
@Test( groups = {"projectGroup"} )
public class ProjectGroupTest
    extends AbstractAdminTest
{
    private String projectGroupName;

    private String projectGroupId;

    private String projectGroupDescription;

    @BeforeMethod
    protected void setUp()
        throws Exception
    {
        projectGroupName = getProperty( "TEST_PROJ_GRP_NAME" );
        projectGroupId = getProperty( "TEST_PROJ_GRP_ID" );
        projectGroupDescription = getProperty( "TEST_PROJ_GRP_DESCRIPTION" );
    }

    @AfterClass
    public void tearDown()
    {
        removeProjectGroup( projectGroupName, false );
    }

    public void testAddProjectGroup()
        throws Exception
    {
        addProjectGroup( projectGroupName, projectGroupId, projectGroupDescription, true );
        showProjectGroup( projectGroupName, projectGroupId, projectGroupDescription );
    }

    public void testAddProjectGroupWithInvalidValues()
        throws Exception
    {
        String name = "!@#$<>?etch";
        String groupId = "-!@#<>etc";
        String description = "![]<>'^&etc";

        addProjectGroup( name, groupId, description, false );
        assertTextPresent( "Name contains invalid characters." );
        assertTextPresent( "Id contains invalid characters." );
    }

    public void testAddProjectGroupWithDashedGroupId()
        throws Exception
    {
        String name = "Test Project Group with Dashes";
        String groupId = "com.example.this-is-a-long-group-id";
        String description = "";

        try {
            addProjectGroup( name, groupId, description, true );
        } finally {
            removeProjectGroup( name, false );
        }
    }

    public void testAddProjectGroupWithPunctuation()
        throws Exception
    {
        String name = "Test :: Test Project Group (with Punctuation)";
        String groupId = "com.example.test";
        String description = "";

        try {
            addProjectGroup( name, groupId, description, true );
        } finally {
            removeProjectGroup( name, false );
        }
    }

    public void testAddProjectGroupWithEmptyString()
        throws Exception
    {
        addProjectGroup( "", "", "", false );
        assertTextPresent( "Project Group Name is required" );
        assertTextPresent( "Project Group ID is required" );
    }

    public void testAddProjectGroupWithWhitespaceString()
        throws Exception
    {
        addProjectGroup( " ", " ", " ", false );
        assertTextPresent( "Project Group Name is required" );
        assertTextPresent( "Project Group ID is required" );
    }

    @Test( dependsOnMethods = {"testAddProjectGroup"} )
    public void testEditProjectGroupWithValidValues()
        throws Exception
    {
        final String newName = "Test :: New Project Group Name (with valid values)";
        final String newDescription = "New Project Group Description";

        editProjectGroup( projectGroupName, projectGroupId, projectGroupDescription, newName, newDescription );
        assertProjectGroupSummaryPage( newName, projectGroupId, newDescription );

        editProjectGroup( newName, projectGroupId, newDescription, projectGroupName, projectGroupDescription );
        assertProjectGroupSummaryPage( projectGroupName, projectGroupId, projectGroupDescription );
    }

    @Test( dependsOnMethods = {"testAddProjectGroup"} )
    public void testEditProjectGroupWithInvalidValues()
        throws Exception
    {
        editProjectGroup( projectGroupName, projectGroupId, projectGroupDescription, " ", projectGroupDescription );
        assertTextPresent( "Project Group Name is required" );
    }

    @Test( dependsOnMethods = {"testAddProjectGroup"} )
    public void testEditProjectGroupWithXSS()
        throws Exception
    {
        String newName = "<script>alert('XSS')</script>";
        String newDescription = "<script>alert('XSS')</script>";
        editProjectGroup( projectGroupName, projectGroupId, projectGroupDescription, newName, newDescription );
        assertTextPresent( "Name contains invalid characters." );
    }

    public void testDeleteProjectGroup()
        throws Exception
    {
        String name = getProperty( "TEST_DELETE_GRP_NAME" );
        String groupId = getProperty( "TEST_DELETE_GRP_ID" );
        String description = getProperty( "TEST_DELETE_GRP_DESCRIPTION" );

        // delete group - delete icon
        addProjectGroup( name, groupId, description, true );
        assertLinkPresent( name );
        clickLinkWithXPath( "//tbody/tr['0']/td['4']/a/img[@alt='Delete Group']" );
        assertTextPresent( "Project Group Removal" );
        clickButtonWithValue( "Delete" );
        assertProjectGroupsSummaryPage();
        assertLinkNotPresent( name );

        // delete group - "Delete Group" button
        addProjectGroup( name, groupId, description, true );
        assertLinkPresent( name );
        removeProjectGroup( name );
        assertLinkNotPresent( name );
        assertProjectGroupsSummaryPage();
        assertLinkNotPresent( name );
    }

    public void testProjectGroupMembers()
        throws Exception
    {
        String name1 = getProperty( "TEST_PROJ_GRP_NAME_ONE" );
        String groupId1 = getProperty( "TEST_PROJ_GRP_ID_ONE" );
        String description1 = getProperty( "TEST_PROJ_GRP_DESCRIPTION_ONE" );
        String name2 = getProperty( "TEST_PROJ_GRP_NAME_TWO" );
        String groupId2 = getProperty( "TEST_PROJ_GRP_ID_TWO" );
        String description2 = getProperty( "TEST_PROJ_GRP_DESCRIPTION_TWO" );
        String name3 = getProperty( "TEST_PROJ_GRP_NAME_THREE" );
        String groupId3 = getProperty( "TEST_PROJ_GRP_ID_THREE" );
        String description3 = getProperty( "TEST_PROJ_GRP_DESCRIPTION_THREE" );

        addProjectGroup( name1, groupId1, description1, true, false );
        assertLinkPresent( name1 );

        addProjectGroup( name2, groupId2, description2, true, false );
        assertLinkPresent( name2 );

        addProjectGroup( name3, groupId3, description3, true, false );
        assertLinkPresent( name3 );

        createAndAddUserAsDeveloperToGroup( "username1", "user1", "user1@something.com", name1 );
        createAndAddUserAsDeveloperToGroup( "username2", "user2", "user2@something.com", name1 );
        createAndAddUserAsDeveloperToGroup( "username3", "user3", "user3@something.com", name2 );
        createAndAddUserAsDeveloperToGroup( "username4", "user4", "user4@something.com", name3 );

        showMembers( name1, groupId1, description1 );
        assertUserPresent( "username1", "user1", "user1@something.com" );
        assertUserPresent( "username2", "user2", "user2@something.com" );
        assertUserNotPresent( "username3", "user3", "user3@something.com" );
        assertUserNotPresent( "username4", "user4", "user4@something.com" );

        showMembers( name2, groupId2, description2 );
        assertUserNotPresent( "username1", "user1", "user1@something.com" );
        assertUserNotPresent( "username2", "user2", "user2@something.com" );
        assertUserPresent( "username3", "user3", "user3@something.com" );
        assertUserNotPresent( "username4", "user4", "user4@something.com" );

        showMembers( name3, groupId3, description3 );
        assertUserNotPresent( "username1", "user1", "user1@something.com" );
        assertUserNotPresent( "username2", "user2", "user2@something.com" );
        assertUserNotPresent( "username3", "user3", "user3@something.com" );
        assertUserPresent( "username4", "user4", "user4@something.com" );

        removeProjectGroup( name1 );
        assertLinkNotPresent( name1 );
        removeProjectGroup( name2 );
        assertLinkNotPresent( name2 );
        removeProjectGroup( name3 );
        assertLinkNotPresent( name3 );
    }
}
