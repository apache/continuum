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
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
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
    private String pomUrl;

    private String pomUsername;

    private String pomPassword;

    private String projectGroupName;

    private String projectGroupId;

    private String projectGroupDescription;

    private String projectGroupScmRootUrl;

    private boolean readdDefaultBuildDefinitionToTemplate;

    private String projectName;

    @BeforeMethod
    protected void setUp()
        throws Exception
    {
        pomUrl = getProperty( "MAVEN2_POM_URL" );
        pomUsername = getProperty( "MAVEN2_POM_USERNAME" );
        pomPassword = getProperty( "MAVEN2_POM_PASSWORD" );

        projectName = getProperty( "MAVEN2_POM_PROJECT_NAME" );
        projectGroupName = getProperty( "MAVEN2_POM_PROJECT_GROUP_NAME" );
        projectGroupId = getProperty( "MAVEN2_POM_PROJECT_GROUP_ID" );
        projectGroupDescription = getProperty( "MAVEN2_POM_PROJECT_GROUP_DESCRIPTION" );
        projectGroupScmRootUrl = getProperty( "MAVEN2_POM_PROJECT_GROUP_SCM_ROOT_URL" );
    }

    @AfterMethod
    public void tearDown()
    {
        removeProjectGroup( projectGroupName, false );
    }

    public void testAddMavenTwoProject()
        throws Exception
    {
        // Enter values into Add Maven Two Project fields, and submit
        addMavenTwoProject( pomUrl, pomUsername, pomPassword, null, true );

        // Wait Struts Listener
        assertProjectGroupSummaryPage( projectGroupName, projectGroupId, projectGroupDescription );

        assertTextPresent( projectGroupScmRootUrl );
    }

    /**
     * Test flat multi module project with names that start with the same letter
     */
    public void testAddMavenTwoProjectModuleNameWithSameLetter()
        throws Exception
    {
        pomUrl = getProperty( "MAVEN2_SAME_LETTER_FLAT_POM_URL" );
        pomUsername = "";
        pomPassword = "";

        projectGroupName = getProperty( "MAVEN2_SAME_LETTER_FLAT_PROJECT_GROUP_NAME" );
        projectGroupId = getProperty( "MAVEN2_SAME_LETTER_FLAT_PROJECT_GROUP_ID" );
        projectGroupDescription = getProperty( "MAVEN2_SAME_LETTER_FLAT_PROJECT_GROUP_DESCRIPTION" );
        projectGroupScmRootUrl = getProperty( "MAVEN2_SAME_LETTER_FLAT_PROJECT_GROUP_SCM_ROOT_URL" );

        addMavenTwoProject( pomUrl, pomUsername, pomPassword, null, true );

        assertProjectGroupSummaryPage( projectGroupName, projectGroupId, projectGroupDescription );

        assertTextPresent( projectGroupScmRootUrl );
    }

    public void testAddMavenTwoProjectFromRemoteSourceToNonDefaultProjectGroup()
        throws Exception
    {
        projectGroupName = getProperty( "MAVEN2_NON_DEFAULT_PROJECT_GROUP_NAME" );
        projectGroupId = getProperty( "MAVEN2_NON_DEFAULT_PROJECT_GROUP_ID" );
        projectGroupDescription = getProperty( "MAVEN2_NON_DEFAULT_PROJECT_GROUP_DESCRIPTION" );

        addProjectGroup( projectGroupName, projectGroupId, projectGroupDescription, true );

        addMavenTwoProject( pomUrl, pomUsername, pomPassword, projectGroupName, true );

        assertProjectGroupSummaryPage( projectGroupName, projectGroupId, projectGroupDescription );

        assertTextPresent( projectGroupScmRootUrl );
    }

    public void testMoveProject()
        throws Exception
    {
        addMavenTwoProject( pomUrl, pomUsername, pomPassword, null, true );
        assertProjectGroupSummaryPage( projectGroupName, projectGroupId, projectGroupDescription );
        assertTextPresent( projectGroupScmRootUrl );

        String targetGroupName = getProperty( "MAVEN2_MOVE_PROJECT_TARGET_PROJECT_GROUP_NAME" );
        String targetGroupId = getProperty( "MAVEN2_MOVE_PROJECT_TARGET_PROJECT_GROUP_ID" );
        String targetGroupDescription = getProperty( "MAVEN2_MOVE_PROJECT_TARGET_PROJECT_GROUP_DESCRIPTION" );
        addProjectGroup( targetGroupName, targetGroupId, targetGroupDescription, true );

        try {
            // Move the project
            moveProjectToProjectGroup( projectGroupName, projectGroupId, projectGroupDescription, projectName,
                                       targetGroupName );
            showProjectGroup( targetGroupName, targetGroupId, targetGroupDescription );
            assertTextPresent( "Member Projects" );
            assertTextPresent( projectName );

            showProjectGroup( projectGroupName, projectGroupId, projectGroupDescription );
            assertTextNotPresent( "Member Projects" );
        } finally {
            removeProjectGroup( targetGroupName, false );
        }
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
        String pomUrl = getProperty( "MAVEN2_NO_SCM_POM_URL" );
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
        assertTextPresent(
            "The specified resource cannot be accessed. Please try again later or contact your administrator." );
    }

    /**
     * Test when the connection element is missing from the scm tag
     */
    public void testMissingConnectionElement()
        throws Exception
    {
        String pomUrl = getProperty( "MAVEN2_MISS_CONNECTION_POM_URL" );
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
        String pomUrl = getProperty( "MAVEN2_MISS_PARENT_POM_URL" );
        submitAddMavenTwoProjectPage( pomUrl );
        assertTextPresent(
            "Missing artifact trying to build the POM. Check that its parent POM is available or add it first in Continuum." );
    }

    /**
     * Test when the modules/subprojects specified in the pom are not found
     */
    public void testMissingModules()
        throws Exception
    {
        String pomUrl = getProperty( "MAVEN2_MISS_SUBPRO_POM_URL" );
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
        assertTextPresent(
            "POM file does not exist. Either the POM you specified or one of its modules does not exist." );
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

    public void testDeleteMavenTwoProject()
        throws Exception
    {
        goToProjectGroupsSummaryPage();

        addMavenTwoProject( pomUrl, pomUsername, pomPassword, null, true );
        goToProjectGroupsSummaryPage();
        assertLinkPresent( projectGroupName );
        clickLinkWithText( projectGroupName );

        assertPage( "Continuum - Project Group" );
        assertTextPresent( projectGroupScmRootUrl );

        // wait for project to finish checkout
        waitForProjectCheckout();

        clickLinkWithXPath( "//tbody/tr['0']/td['10']/a/img[@alt='Delete']" );
        assertTextPresent( "Delete Continuum Project" );
        clickButtonWithValue( "Delete" );
        assertPage( "Continuum - Project Group" );
        assertTextNotPresent( "Unable to delete project" );
        assertLinkNotPresent( projectGroupName );
        assertTextNotPresent( projectGroupScmRootUrl );
    }

    public void testDeleteMavenTwoProjects()
        throws Exception
    {
        goToProjectGroupsSummaryPage();

        addMavenTwoProject( pomUrl, pomUsername, pomPassword, null, true );
        goToProjectGroupsSummaryPage();
        assertLinkPresent( projectGroupName );
        clickLinkWithText( projectGroupName );

        assertPage( "Continuum - Project Group" );

        //wait for project to finish checkout
        waitForProjectCheckout();

        checkField( "//tbody/tr['0']/td['0']/input[@name='selectedProjects']" );
        clickButtonWithValue( "Delete Project(s)" );
        assertTextPresent( "Delete Continuum Projects" );
        clickButtonWithValue( "Delete" );
        assertPage( "Continuum - Project Group" );
        assertTextNotPresent( "Unable to delete project" );
        assertLinkNotPresent( projectGroupName );
        assertTextNotPresent( projectGroupScmRootUrl );
    }

    public void testBuildMaven2ProjectWithTag()
        throws Exception
    {
        pomUrl = getProperty( "MAVEN2_PROJECT_WITH_TAG_POM_URL" );
        pomUsername = "";
        pomPassword = "";

        projectGroupName = getProperty( "MAVEN2_PROJECT_WITH_TAG_POM_PROJECT_GROUP_NAME" );
        projectGroupId = getProperty( "MAVEN2_PROJECT_WITH_TAG_POM_PROJECT_GROUP_ID" );
        projectGroupDescription = getProperty( "MAVEN2_PROJECT_WITH_TAG_POM_PROJECT_GROUP_DESCRIPTION" );

        addMavenTwoProject( pomUrl, pomUsername, pomPassword, null, true );
        assertProjectGroupSummaryPage( projectGroupName, projectGroupId, projectGroupDescription );

        buildProjectGroup( projectGroupName, projectGroupId, projectGroupDescription, projectGroupName, true );
    }
}
