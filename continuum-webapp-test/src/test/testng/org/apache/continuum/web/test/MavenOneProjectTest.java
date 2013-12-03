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
 * Based on AddMavenOneProjectTestCase of Emmanuel Venisse.
 *
 * @author José Morales Martínez
 * @version $Id$
 */
@Test( groups = {"mavenOneProject"} )
public class MavenOneProjectTest
    extends AbstractAdminTest
{
    private String pomUrl;

    private String pomUsername;

    private String projectGroupId;

    private String projectGroupDescription;

    private String projectGroupName;

    private String pomPassword;

    private String pomUrlMissingElement;

    private String pomUrlWithExtend;

    private String pomUrlUnparseableContent;

    private String malformedPomUrl;

    private String inaccessiblePomUrl;

    @BeforeMethod
    protected void setUp()
        throws Exception
    {
        pomUrl = getProperty( "MAVEN1_POM_URL" );
        pomUsername = getProperty( "MAVEN1_POM_USERNAME" );
        pomPassword = getProperty( "MAVEN1_POM_PASSWORD" );
        projectGroupName = getProperty( "MAVEN1_PROJECT_GROUP_NAME" );
        projectGroupId = getProperty( "MAVEN1_PROJECT_GROUP_ID" );
        projectGroupDescription = getProperty( "MAVEN1_PROJECT_GROUP_DESCRIPTION" );

        pomUrlMissingElement = getProperty( "MAVEN1_MISSING_REPO_POM_URL" );
        pomUrlWithExtend = getProperty( "MAVEN1_EXTENDED_POM_URL" );
        pomUrlUnparseableContent = getProperty( "MAVEN1_UNPARSEABLE_POM_URL" );

        malformedPomUrl = "aaa";
        inaccessiblePomUrl = baseUrl + "/inaccessible-pom/";
    }

    @AfterMethod
    protected void tearDown()
    {
        removeProjectGroup( projectGroupName, false );
    }

    public void testAddMavenOneProjectWithNoDefaultBuildDefinitionFromTemplate()
        throws Exception
    {
        removeDefaultBuildDefinitionFromTemplate( "maven1" );

        goToAddMavenOneProjectPage();
        addMavenOneProject( pomUrl, pomUsername, pomPassword, null, true );
        assertProjectGroupSummaryPage( projectGroupName, projectGroupId, projectGroupDescription );

        // Re-add default build definition of template
        addDefaultBuildDefinitionFromTemplate( "maven1" );
    }

    /**
     * test with valid pom url
     */
    public void testValidPomUrl()
        throws Exception
    {
        // Enter values into Add Maven Two Project fields, and submit
        goToAddMavenOneProjectPage();
        addMavenOneProject( pomUrl, pomUsername, pomPassword, null, true );
        assertProjectGroupSummaryPage( projectGroupName, projectGroupId, projectGroupDescription );
    }

    /**
     * test with no pom file or pom url specified
     */
    public void testNoPomSpecified()
        throws Exception
    {
        goToAddMavenOneProjectPage();
        addMavenOneProject( "", "", "", null, false );
        assertTextPresent( "Either POM URL or Upload POM is required." );
    }

    /**
     * test with missing <repository> element in the pom file
     */
    public void testMissingElementInPom()
        throws Exception
    {
        goToAddMavenOneProjectPage();
        addMavenOneProject( pomUrlMissingElement, pomUsername, pomPassword, null, false );
        assertTextPresent( "Missing 'repository' element in the POM." );
    }

    /**
     * test with <extend> element present in pom file
     */
    public void testWithExtendElementPom()
        throws Exception
    {
        goToAddMavenOneProjectPage();
        addMavenOneProject( pomUrlWithExtend, pomUsername, pomPassword, null, false );
        assertTextPresent( "Cannot use a POM with an 'extend' element" );
    }

    /**
     * test with unparseable xml content for pom file
     */
    public void testUnparseableXmlContent()
        throws Exception
    {
        goToAddMavenOneProjectPage();
        addMavenOneProject( pomUrlUnparseableContent, pomUsername, pomPassword, null, false );
        assertTextPresent( "The XML content of the POM can not be parsed." );
    }

    /**
     * test with a malformed pom url
     */
    public void testMalformedPomUrl()
        throws Exception
    {
        goToAddMavenOneProjectPage();
        addMavenOneProject( malformedPomUrl, "", "", null, false );
        assertTextPresent(
            "The specified resource cannot be accessed. Please try again later or contact your administrator." );
    }

    /**
     * test with an inaccessible pom url
     */
    public void testInaccessiblePomUrl()
        throws Exception
    {
        goToAddMavenOneProjectPage();
        addMavenOneProject( inaccessiblePomUrl, "", "", null, false );
        assertTextPresent(
            "POM file does not exist. Either the POM you specified or one of its modules does not exist." );
    }

    /**
     * test cancel button
     */
    public void testCancelButton()
    {
        goToAboutPage();
        goToAddMavenOneProjectPage();
        clickButtonWithValue( "Cancel" );
        assertAboutPage();
    }

    public void testDeleteMavenOneProject()
        throws Exception
    {
        // setup
        goToProjectGroupsSummaryPage();
        addMaven1Project( projectGroupName, pomUrl, pomUsername, pomPassword );

        // delete project - delete icon
        clickLinkWithText( projectGroupName );
        clickLinkWithXPath( "//tbody/tr['0']/td['10']/a/img[@alt='Delete']" );
        assertTextPresent( "Delete Continuum Project" );
        clickButtonWithValue( "Delete" );
        assertPage( "Continuum - Project Group" );
        assertLinkNotPresent( projectGroupName );
    }

    public void testDeleteMavenOneProjects()
        throws Exception
    {
        // setup
        goToProjectGroupsSummaryPage();
        addMaven1Project( projectGroupName, pomUrl, pomUsername, pomPassword );

        // delete project - "Delete Project(s)" button
        clickLinkWithText( projectGroupName );
        checkField( "//tbody/tr['0']/td['0']/input[@name='selectedProjects']" );
        clickButtonWithValue( "Delete Project(s)" );
        assertTextPresent( "Delete Continuum Projects" );
        clickButtonWithValue( "Delete" );
        assertPage( "Continuum - Project Group" );
        assertLinkNotPresent( projectGroupName );
    }

    private void addMaven1Project( String groupName, String pomUrl, String pomUsername, String pomPassword )
    {
        goToAddMavenOneProjectPage();
        assertLinkNotPresent( groupName );
        addMavenOneProject( pomUrl, pomUsername, pomPassword, null, true );
        goToProjectGroupsSummaryPage();
        assertLinkPresent( groupName );
    }
}
