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

/**
 * Based on ProjectGroupTest of Emmanuel Venisse test.
 *
 * @author José Morales Martínez
 * @version $Id$
 */
@Test( groups = { "projectGroup" }, dependsOnMethods = { "testWithCorrectUsernamePassword" } )
public class ProjectGroupTest
    extends AbstractContinuumTest
{

    public void testAddProjectGroup()
        throws Exception
    {
        String TEST_PROJ_GRP_NAME = getProperty( "TEST_PROJ_GRP_NAME" );
        String TEST_PROJ_GRP_ID = getProperty( "TEST_PROJ_GRP_ID" );
        String TEST_PROJ_GRP_DESCRIPTION = getProperty( "TEST_PROJ_GRP_DESCRIPTION" );

        addProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION, true );
        showProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
    }

    public void testAddProjectGroup2()
        throws Exception
    {
        String TEST2_PROJ_GRP_NAME = getProperty( "TEST2_PROJ_GRP_NAME" );
        String TEST2_PROJ_GRP_ID = getProperty( "TEST2_PROJ_GRP_ID" );
        String TEST2_PROJ_GRP_DESCRIPTION = getProperty( "TEST2_PROJ_GRP_DESCRIPTION" );

        addProjectGroup( TEST2_PROJ_GRP_NAME, TEST2_PROJ_GRP_ID, TEST2_PROJ_GRP_DESCRIPTION, true );
        showProjectGroup( TEST2_PROJ_GRP_NAME, TEST2_PROJ_GRP_ID, TEST2_PROJ_GRP_DESCRIPTION );
    }
    
    public void testAddProjectGroupWithInvalidValues()
        throws Exception
    {
        String TEST2_PROJ_GRP_NAME = "!@#$<>?etch";
        String TEST2_PROJ_GRP_ID = "-!@#<>etc";
        String TEST2_PROJ_GRP_DESCRIPTION = "![]<>'^&etc";
        
        addProjectGroup( TEST2_PROJ_GRP_NAME, TEST2_PROJ_GRP_ID, TEST2_PROJ_GRP_DESCRIPTION, false );
        assertTextPresent( "Name contains invalid characters." );
        assertTextPresent( "Id contains invalid characters." );
        assertTextPresent( "Description contains invalid characters." );
    }

    @Test( dependsOnMethods = { "testAddMavenTwoProjectFromRemoteSourceToNonDefaultProjectGroup" } )
    public void testMoveProject()
        throws Exception
    {
        String TEST_PROJ_GRP_NAME = getProperty( "TEST_PROJ_GRP_NAME" );
        String TEST_PROJ_GRP_ID = getProperty( "TEST_PROJ_GRP_ID" );
        String TEST_PROJ_GRP_DESCRIPTION = getProperty( "TEST_PROJ_GRP_DESCRIPTION" );
        String DEFAULT_PROJ_GRP_NAME = getProperty( "DEFAULT_PROJ_GRP_NAME" );
        String DEFAULT_PROJ_GRP_ID = getProperty( "DEFAULT_PROJ_GRP_NAME" );
        String DEFAULT_PROJ_GRP_DESCRIPTION = getProperty( "DEFAULT_PROJ_GRP_NAME" );
        String M2_PROJ_GRP_NAME = getProperty( "M2_PROJ_GRP_NAME" );

        // TODO: need to wait for checkout to complete. Can we add a special IT type of project that doesn't require checkout?
        //       currently we get away with it due to the usualy duration between the dependant test and this test
        // move the project of the test project group to the default project group
        moveProjectToProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION, M2_PROJ_GRP_NAME,
                                   DEFAULT_PROJ_GRP_NAME );
        showProjectGroup( DEFAULT_PROJ_GRP_NAME, DEFAULT_PROJ_GRP_ID, DEFAULT_PROJ_GRP_DESCRIPTION );
        assertTextPresent( "Member Projects" );
        // Restore project to test project group
        moveProjectToProjectGroup( DEFAULT_PROJ_GRP_NAME, DEFAULT_PROJ_GRP_ID, DEFAULT_PROJ_GRP_DESCRIPTION,
                                   M2_PROJ_GRP_NAME, TEST_PROJ_GRP_NAME );
        showProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
        assertTextPresent( "Member Projects" );
    }

    public void testAddProjectGroupWithEmptyString()
        throws Exception
    {
        addProjectGroup( "", "", "", false );
        assertTextPresent( "Project Group Name is required." );
        assertTextPresent( "Project Group ID is required." );
    }

    public void testAddProjectGroupWithWhitespaceString()
        throws Exception
    {
        addProjectGroup( " ", " ", " ", false );
        assertTextPresent( "Project Group Name cannot contain spaces only." );
        assertTextPresent( "Project Group ID cannot contain spaces only." );
    }

    @Test( dependsOnMethods = { "testAddProjectGroup2" } )
    public void testEditProjectGroupWithValidValues()
        throws Exception
    {
        final String sNewProjectName = "New Project Group Name";
        final String sNewProjectDescription = "New Project Group Description";

        String TEST2_PROJ_GRP_NAME = getProperty( "TEST2_PROJ_GRP_NAME" );
        String TEST2_PROJ_GRP_ID = getProperty( "TEST2_PROJ_GRP_ID" );
        String TEST2_PROJ_GRP_DESCRIPTION = getProperty( "TEST2_PROJ_GRP_DESCRIPTION" );

        editProjectGroup( TEST2_PROJ_GRP_NAME, TEST2_PROJ_GRP_ID, TEST2_PROJ_GRP_DESCRIPTION, sNewProjectName,
                          sNewProjectDescription );
        assertProjectGroupSummaryPage( sNewProjectName, TEST2_PROJ_GRP_ID, sNewProjectDescription );

        editProjectGroup( sNewProjectName, TEST2_PROJ_GRP_ID, sNewProjectDescription, TEST2_PROJ_GRP_NAME,
                          TEST2_PROJ_GRP_DESCRIPTION );
        assertProjectGroupSummaryPage( TEST2_PROJ_GRP_NAME, TEST2_PROJ_GRP_ID, TEST2_PROJ_GRP_DESCRIPTION );
    }

    @Test( dependsOnMethods = { "testAddProjectGroup2" } )
    public void testEditProjectGroupWithInvalidValues()
        throws Exception
    {

        String TEST2_PROJ_GRP_NAME = getProperty( "TEST2_PROJ_GRP_NAME" );
        String TEST2_PROJ_GRP_ID = getProperty( "TEST2_PROJ_GRP_ID" );
        String TEST2_PROJ_GRP_DESCRIPTION = getProperty( "TEST2_PROJ_GRP_DESCRIPTION" );
        editProjectGroup( TEST2_PROJ_GRP_NAME, TEST2_PROJ_GRP_ID, TEST2_PROJ_GRP_DESCRIPTION, " ",
                          TEST2_PROJ_GRP_DESCRIPTION );
        assertTextPresent( "Project Group Name cannot contain spaces only" );
    }
    
    @Test( dependsOnMethods = { "testAddProjectGroup2" } )
    public void testEditProjectGroupWithXSS()
        throws Exception
    {
        String TEST2_PROJ_GRP_NAME = getProperty( "TEST2_PROJ_GRP_NAME" );
        String TEST2_PROJ_GRP_ID = getProperty( "TEST2_PROJ_GRP_ID" );
        String TEST2_PROJ_GRP_DESCRIPTION = getProperty( "TEST2_PROJ_GRP_DESCRIPTION" );
        String NEW_PROJ_GRP_NAME = "<script>alert('XSS')</script>";
        String NEW_PROJ_GRP_DESCRIPTION = "<script>alert('XSS')</script>";
        editProjectGroup( TEST2_PROJ_GRP_NAME, TEST2_PROJ_GRP_ID, TEST2_PROJ_GRP_DESCRIPTION, NEW_PROJ_GRP_NAME,
                          NEW_PROJ_GRP_DESCRIPTION );
        assertTextPresent( "Name contains invalid characters." );
        assertTextPresent( "Description contains invalid characters." );
    }

    @Test( dependsOnMethods = { "testAddMavenTwoProject" } )
    public void testProjectGroupAllBuildSuccess()
        throws Exception
    {
        String M2_PROJ_GRP_NAME = getProperty( "M2_PROJ_GRP_NAME" );
        String M2_PROJ_GRP_ID = getProperty( "M2_PROJ_GRP_ID" );
        String M2_PROJ_GRP_DESCRIPTION = getProperty( "M2_PROJ_GRP_DESCRIPTION" );
        buildProjectGroup( M2_PROJ_GRP_NAME, M2_PROJ_GRP_ID, M2_PROJ_GRP_DESCRIPTION, M2_PROJ_GRP_NAME );
        clickButtonWithValue( "Release" );
        assertReleaseSuccess();
    }

    @Test( dependsOnMethods = { "testAddProjectGroup" } )
    public void testDeleteProjectGroup()
        throws Exception
    {
        String TEST_GRP_NAME = getProperty( "TEST_DELETE_GRP_NAME" );
        String TEST_GRP_ID = getProperty( "TEST_DELETE_GRP_ID" );
        String TEST_GRP_DESCRIPTION = getProperty( "TEST_DELETE_GRP_DESCRIPTION" );
        
        // delete group - delete icon
        addProjectGroup( TEST_GRP_NAME, TEST_GRP_ID, TEST_GRP_DESCRIPTION, true );
        assertLinkPresent( TEST_GRP_NAME );
        clickLinkWithXPath( "//tbody/tr['0']/td['4']/a/img[@alt='Delete Group']" );
        assertTextPresent( "Project Group Removal" );
        clickButtonWithValue( "Delete" );
        assertProjectGroupsSummaryPage();
        assertLinkNotPresent( TEST_GRP_NAME );
        
        // delete group - "Delete Group" button
        addProjectGroup( TEST_GRP_NAME, TEST_GRP_ID, TEST_GRP_DESCRIPTION, true );
        assertLinkPresent( TEST_GRP_NAME );
        removeProjectGroup( TEST_GRP_NAME );
        assertLinkNotPresent( TEST_GRP_NAME );
        assertProjectGroupsSummaryPage();
        assertLinkNotPresent( TEST_GRP_NAME );
    }

    public void testProjectGroupMembers()
        throws Exception
    {
        String GRP_NAME_ONE = getProperty( "TEST_PROJ_GRP_NAME_ONE" );
        String GRP_ID_ONE = getProperty( "TEST_PROJ_GRP_ID_ONE" );
        String GRP_DESCRIPTION_ONE = getProperty( "TEST_PROJ_GRP_DESCRIPTION_ONE" );
        String GRP_NAME_TWO = getProperty( "TEST_PROJ_GRP_NAME_TWO" );
        String GRP_ID_TWO = getProperty( "TEST_PROJ_GRP_ID_TWO" );
        String GRP_DESCRIPTION_TWO = getProperty( "TEST_PROJ_GRP_DESCRIPTION_TWO" );
        String GRP_NAME_THREE = getProperty( "TEST_PROJ_GRP_NAME_THREE" );
        String GRP_ID_THREE = getProperty( "TEST_PROJ_GRP_ID_THREE" );
        String GRP_DESCRIPTION_THREE = getProperty( "TEST_PROJ_GRP_DESCRIPTION_THREE" );

        addProjectGroup( GRP_NAME_ONE, GRP_ID_ONE, GRP_DESCRIPTION_ONE, true );
        assertLinkPresent( GRP_NAME_ONE );

        addProjectGroup( GRP_NAME_TWO, GRP_ID_TWO, GRP_DESCRIPTION_TWO, true );
        assertLinkPresent( GRP_NAME_TWO );

        addProjectGroup( GRP_NAME_THREE, GRP_ID_THREE, GRP_DESCRIPTION_THREE, true );
        assertLinkPresent( GRP_NAME_THREE );

        createAndAddUserAsDeveloperToGroup( "username1", "user1", "user1@something.com", "password123", GRP_NAME_ONE );
        createAndAddUserAsDeveloperToGroup( "username2", "user2", "user2@something.com", "password123", GRP_NAME_ONE );
        createAndAddUserAsDeveloperToGroup( "username3", "user3", "user3@something.com", "password123", GRP_NAME_TWO );
        createAndAddUserAsDeveloperToGroup( "username4", "user4", "user4@something.com", "password123", GRP_NAME_THREE );

        showMembers( GRP_NAME_ONE, GRP_ID_ONE, GRP_DESCRIPTION_ONE );
        assertUserPresent( "username1", "user1", "user1@something.com" );
        assertUserPresent( "username2", "user2", "user2@something.com" );
        assertUserNotPresent( "username3", "user3", "user3@something.com" );
        assertUserNotPresent( "username4", "user4", "user4@something.com" );

        showMembers( GRP_NAME_TWO, GRP_ID_TWO, GRP_DESCRIPTION_TWO );
        assertUserNotPresent( "username1", "user1", "user1@something.com" );
        assertUserNotPresent( "username2", "user2", "user2@something.com" );
        assertUserPresent( "username3", "user3", "user3@something.com" );
        assertUserNotPresent( "username4", "user4", "user4@something.com" );

        showMembers( GRP_NAME_THREE, GRP_ID_THREE, GRP_DESCRIPTION_THREE );
        assertUserNotPresent( "username1", "user1", "user1@something.com" );
        assertUserNotPresent( "username2", "user2", "user2@something.com" );
        assertUserNotPresent( "username3", "user3", "user3@something.com" );
        assertUserPresent( "username4", "user4", "user4@something.com" );

        removeProjectGroup( GRP_NAME_ONE );
        assertLinkNotPresent( GRP_NAME_ONE );
        removeProjectGroup( GRP_NAME_TWO );
        assertLinkNotPresent( GRP_NAME_TWO );
        removeProjectGroup( GRP_NAME_THREE );
        assertLinkNotPresent( GRP_NAME_THREE );
    }
}
