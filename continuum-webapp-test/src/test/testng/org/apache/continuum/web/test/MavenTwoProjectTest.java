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

import org.apache.continuum.web.test.parent.AbstractAdminTest;
import org.testng.annotations.Test;

/**
 * Based on AddMavenTwoProjectTest of Emmanuel Venisse test.
 *
 * @author José Morales Martínez
 * @version $Id$
 */
@Test( groups = { "mavenTwoProject" } )
public class MavenTwoProjectTest
    extends AbstractAdminTest
{
    public void testAddMavenTwoProjectWithNoDefaultBuildDefinitionInTemplate()
        throws Exception
    {
        String M2_POM_URL = getProperty( "M2_POM_URL" );
        String M2_POM_USERNAME = getProperty( "M2_POM_USERNAME" );
        String M2_POM_PASSWORD = getProperty( "M2_POM_PASSWORD" );

        String M2_PROJ_GRP_NAME = getProperty( "M2_PROJ_GRP_NAME" );
        String M2_PROJ_GRP_ID = getProperty( "M2_PROJ_GRP_ID" );
        String M2_PROJ_GRP_DESCRIPTION = getProperty( "M2_PROJ_GRP_DESCRIPTION" );
        String M2_PROJ_GRP_SCM_ROOT_URL = getProperty( "M2_PROJ_GRP_SCM_ROOT_URL" );

        removeDefaultBuildDefinitionFromTemplate( "maven2" );

        addMavenTwoProject( M2_POM_URL, M2_POM_USERNAME, M2_POM_PASSWORD, null, true );

        assertProjectGroupSummaryPage( M2_PROJ_GRP_NAME, M2_PROJ_GRP_ID, M2_PROJ_GRP_DESCRIPTION );

        assertTextPresent( M2_PROJ_GRP_SCM_ROOT_URL );

        // Delete project group
        removeProjectGroup( M2_PROJ_GRP_NAME );

        // Re-add default build definition of template
        addDefaultBuildDefinitionFromTemplate( "maven2" );
    }

    @Test( dependsOnMethods = { "testAddMavenTwoProjectWithNoDefaultBuildDefinitionInTemplate" } )
    public void testAddMavenTwoProject()
        throws Exception
    {
        String M2_POM_URL = getProperty( "M2_POM_URL" );
        String M2_POM_USERNAME = getProperty( "M2_POM_USERNAME" );
        String M2_POM_PASSWORD = getProperty( "M2_POM_PASSWORD" );

        String M2_PROJ_GRP_NAME = getProperty( "M2_PROJ_GRP_NAME" );
        String M2_PROJ_GRP_ID = getProperty( "M2_PROJ_GRP_ID" );
        String M2_PROJ_GRP_DESCRIPTION = getProperty( "M2_PROJ_GRP_DESCRIPTION" );
        String M2_PROJ_GRP_SCM_ROOT_URL = getProperty( "M2_PROJ_GRP_SCM_ROOT_URL" );
        
        // Enter values into Add Maven Two Project fields, and submit
        addMavenTwoProject( M2_POM_URL, M2_POM_USERNAME, M2_POM_PASSWORD, null, true );
        // Wait Struct Listener
        assertProjectGroupSummaryPage( M2_PROJ_GRP_NAME, M2_PROJ_GRP_ID, M2_PROJ_GRP_DESCRIPTION );

        assertTextPresent( M2_PROJ_GRP_SCM_ROOT_URL );
    }

    /**
     * Test flat multi module project with names that start with the same letter
     */
    public void testAddMavenTwoProjectModuleNameWithSameLetter()
        throws Exception
    {
        String M2_POM_URL = getProperty( "M2_SAME_LETTER_POM_URL" );
        String M2_POM_USERNAME = getProperty( "M2_POM_USERNAME" );
        String M2_POM_PASSWORD = getProperty( "M2_POM_PASSWORD" );

        String M2_PROJ_GRP_NAME = getProperty( "M2_SAME_LETTER_PROJ_GRP_NAME" );
        String M2_PROJ_GRP_ID = getProperty( "M2_SAME_LETTER_PROJ_GRP_ID" );
        String M2_PROJ_GRP_DESCRIPTION = getProperty( "M2_SAME_LETTER_PROJ_GRP_DESCRIPTION" );

        String M2_PROJ_GRP_SCM_ROOT_URL = getProperty( "M2_SAME_LETTER_PROJ_GRP_SCM_ROOT_URL" );

        addMavenTwoProject( M2_POM_URL, M2_POM_USERNAME, M2_POM_PASSWORD, null, true );

        assertProjectGroupSummaryPage( M2_PROJ_GRP_NAME, M2_PROJ_GRP_ID, M2_PROJ_GRP_DESCRIPTION );

        assertTextPresent( M2_PROJ_GRP_SCM_ROOT_URL );
    }

    public void testAddMavenTwoProjectFromRemoteSourceToNonDefaultProjectGroup()
        throws Exception
    {
        String TEST_PROJ_GRP_NAME = getProperty( "TEST_PROJ_GRP_NAME" );
        String TEST_PROJ_GRP_ID = getProperty( "TEST_PROJ_GRP_ID" );
        String TEST_PROJ_GRP_DESCRIPTION = getProperty( "TEST_PROJ_GRP_DESCRIPTION" );
        String TEST_PROJ_GRP_SCM_ROOT_URL = getProperty( "M2_PROJ_GRP_SCM_ROOT_URL" );

        removeProjectGroup( TEST_PROJ_GRP_NAME, false );

        addProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION, true );

        String M2_POM_URL = getProperty( "M2_POM_URL" );
        String M2_POM_USERNAME = getProperty( "M2_POM_USERNAME" );
        String M2_POM_PASSWORD = getProperty( "M2_POM_PASSWORD" );
        
        addMavenTwoProject( M2_POM_URL, M2_POM_USERNAME, M2_POM_PASSWORD, TEST_PROJ_GRP_NAME, true );

        assertProjectGroupSummaryPage( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );

        assertTextPresent( TEST_PROJ_GRP_SCM_ROOT_URL );

        removeProjectGroup( TEST_PROJ_GRP_NAME );
    }

    @Test( dependsOnMethods = { "testProjectGroupAllBuildSuccess" } )
    public void testMoveProject()
        throws Exception
    {
        String TEST_PROJ_GRP_NAME = getProperty( "M2_PROJ_GRP_NAME" );
        String TEST_PROJ_GRP_ID = getProperty( "M2_PROJ_GRP_ID" );
        String TEST_PROJ_GRP_DESCRIPTION = getProperty( "M2_PROJ_GRP_DESCRIPTION" );
        String DEFAULT_PROJ_GRP_NAME = getProperty( "DEFAULT_PROJ_GRP_NAME" );
        String DEFAULT_PROJ_GRP_ID = getProperty( "DEFAULT_PROJ_GRP_NAME" );
        String DEFAULT_PROJ_GRP_DESCRIPTION = getProperty( "DEFAULT_PROJ_GRP_NAME" );
        String M2_PROJ_GRP_NAME = getProperty( "M2_PROJ_GRP_NAME" );

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

    /**
     * Test invalid pom url
     */
    public void testNoPomSpecified()
        throws Exception
    {
        submitAddMavenTwoProjectPage( "" );
        assertTextPresent( "Either POM URL or Upload POM is required." );
    }

    /**
     * Test when scm element is missing from pom
     */
    public void testMissingScmElementPom()
        throws Exception
    {
        String pomUrl = getProperty( "NOT_SCM_POM_URL" );
        submitAddMavenTwoProjectPage( pomUrl );
        assertTextPresent( "Missing ''scm'' element in the POM, project Maven Two Project" );
    }

    /**
     * test with a malformed pom url
     */
    public void testMalformedPomUrl()
        throws Exception
    {
        String pomUrl = "aaa";
        submitAddMavenTwoProjectPage( pomUrl );
        assertTextPresent( "The specified resource cannot be accessed. Please try again later or contact your administrator." );
    }

    /**
     * Test when the connection element is missing from the scm tag
     */
    public void testMissingConnectionElement()
        throws Exception
    {
        String pomUrl = getProperty( "MISS_CONECT_POM_URL" );
        submitAddMavenTwoProjectPage( pomUrl );
        assertTextPresent( "Missing 'connection' sub-element in the 'scm' element in the POM." );
    }

    /**
     * test unallowed file protocol
     */
    public void testNotAllowedProtocol()
        throws Exception
    {
        String pomUrl = "file:///pom.xml";
        submitAddMavenTwoProjectPage( pomUrl );
        assertTextPresent( "The specified resource isn't a file or the protocol used isn't allowed." );
    }

    /**
     * Test when the parent pom is missing or not yet added in continuum
     */
    public void testMissingParentPom()
        throws Exception
    {
        String pomUrl = getProperty( "MISS_PARENT_POM_URL" );
        submitAddMavenTwoProjectPage( pomUrl );
        assertTextPresent( "Missing artifact trying to build the POM. Check that its parent POM is available or add it first in Continuum." );
    }

    /**
     * Test when the modules/subprojects specified in the pom are not found
     */
    public void testMissingModules()
        throws Exception
    {
        String pomUrl = getProperty( "MISS_SUBPRO_POM_URL" );
        submitAddMavenTwoProjectPage( pomUrl );
        assertTextPresent( "Unknown error trying to build POM." );
    }

    /**
     * test with an inaccessible pom url
     */
    public void testInaccessiblePomUrl()
        throws Exception
    {
        String pomUrl = "http://localhost:9595/";
        submitAddMavenTwoProjectPage( pomUrl );
        assertTextPresent( "POM file does not exist. Either the POM you specified or one of its modules does not exist." );
    }

    /**
     * test cancel button
     */
    public void testCancelButton()
    {
        goToAboutPage();
        goToAddMavenTwoProjectPage();
        clickButtonWithValue( "Cancel" );
        assertAboutPage();
    }

    @Test( dependsOnMethods = { "testAddMavenTwoProject" } )
    public void testDeleteMavenTwoProject()
        throws Exception
    {
        String M2_PROJ_GRP_NAME = getProperty( "M2_DELETE_PROJ_GRP_NAME" );
        String M2_PROJ_GRP_SCM_ROOT_URL = getProperty( "M2_DELETE_PROJ_GRP_SCM_ROOT_URL" );
        goToProjectGroupsSummaryPage();
        
        // delete project - delete icon
        addMavenTwoProject( getProperty( "M2_DELETE_POM_URL" ), getProperty( "M2_POM_USERNAME" ),
                            getProperty( "M2_POM_PASSWORD" ), null, true );
        goToProjectGroupsSummaryPage();
        assertLinkPresent( M2_PROJ_GRP_NAME );
        clickLinkWithText( M2_PROJ_GRP_NAME );

        assertPage( "Continuum - Project Group" );
        assertTextPresent( M2_PROJ_GRP_SCM_ROOT_URL );

        // TODO: this doesn't always seem to work, perhaps because of changes in the way icons are displayed
        // wait for project to finish checkout
        waitForProjectCheckout();

        clickLinkWithXPath( "//tbody/tr['0']/td['10']/a/img[@alt='Delete']" );
        assertTextPresent( "Delete Continuum Project" );
        clickButtonWithValue( "Delete" );
        assertPage( "Continuum - Project Group" );
        assertTextNotPresent( "Unable to delete project" );
        assertLinkNotPresent( M2_PROJ_GRP_NAME );
        assertTextNotPresent( M2_PROJ_GRP_SCM_ROOT_URL );

        // remove group for next test
        removeProjectGroup( M2_PROJ_GRP_NAME );
        assertLinkNotPresent( M2_PROJ_GRP_NAME );

        // delete project - "Delete Project(s)" button
        addMavenTwoProject( getProperty( "M2_DELETE_POM_URL" ), getProperty( "M2_POM_USERNAME" ),
                            getProperty( "M2_POM_PASSWORD" ), null, true );
        goToProjectGroupsSummaryPage();
        assertLinkPresent( M2_PROJ_GRP_NAME );
        clickLinkWithText( M2_PROJ_GRP_NAME );

        assertPage( "Continuum - Project Group" );
        //wait for project to finish checkout
        waitForProjectCheckout();

        checkField( "//tbody/tr['0']/td['0']/input[@name='selectedProjects']" );
        clickButtonWithValue( "Delete Project(s)" );
        assertTextPresent( "Delete Continuum Projects" );
        clickButtonWithValue( "Delete" );
        assertPage( "Continuum - Project Group" );
        assertTextNotPresent( "Unable to delete project" );
        assertLinkNotPresent( M2_PROJ_GRP_NAME );
        assertTextNotPresent( M2_PROJ_GRP_SCM_ROOT_URL );

        // remove project group
        removeProjectGroup( M2_PROJ_GRP_NAME );
        assertLinkNotPresent( M2_PROJ_GRP_NAME );
    }

    public void testBuildMaven2ProjectWithTag()
        throws Exception
    {
        String M2_POM_URL = getProperty( "M2_PROJ_WITH_TAG_POM_URL" );
        String M2_POM_USERNAME = getProperty( "M2_POM_USERNAME" );
        String M2_POM_PASSWORD = getProperty( "M2_POM_PASSWORD" );
    
        String M2_PROJ_GRP_NAME = getProperty( "M2_PROJ_WITH_TAG_PROJ_GRP_NAME" );
        String M2_PROJ_GRP_ID = getProperty( "M2_PROJ_WITH_TAG_PROJ_GRP_ID" );
        String M2_PROJ_GRP_DESCRIPTION = "";
    
        addMavenTwoProject( M2_POM_URL, M2_POM_USERNAME, M2_POM_PASSWORD, null, true );
        assertProjectGroupSummaryPage( M2_PROJ_GRP_NAME, M2_PROJ_GRP_ID, M2_PROJ_GRP_DESCRIPTION );
    
        buildProjectGroup( M2_PROJ_GRP_NAME, M2_PROJ_GRP_ID, M2_PROJ_GRP_DESCRIPTION, M2_PROJ_GRP_NAME, true );
    
        removeProjectGroup( M2_PROJ_GRP_NAME );
        assertLinkNotPresent( M2_PROJ_GRP_NAME );
    }

    @Test( dependsOnMethods = { "testAddMavenTwoProject" } )
    public void testProjectGroupAllBuildSuccess()
        throws Exception
    {
        String M2_PROJ_GRP_NAME = getProperty( "M2_PROJ_GRP_NAME" );
        String M2_PROJ_GRP_ID = getProperty( "M2_PROJ_GRP_ID" );
        String M2_PROJ_GRP_DESCRIPTION = getProperty( "M2_PROJ_GRP_DESCRIPTION" );
        buildProjectGroup( M2_PROJ_GRP_NAME, M2_PROJ_GRP_ID, M2_PROJ_GRP_DESCRIPTION, M2_PROJ_GRP_NAME, true );
        clickButtonWithValue( "Release" );
        assertReleaseSuccess();
    }
}
