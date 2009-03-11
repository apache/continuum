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

import org.testng.annotations.Test;

/**
 * Based on AddMavenTwoProjectTest of Emmanuel Venisse test.
 *
 * @author José Morales Martínez
 * @version $Id$
 */
@Test( groups = { "mavenTwoProject" }, dependsOnMethods = { "testWithCorrectUsernamePassword" })
public class MavenTwoProjectTest
    extends AbstractContinuumTest
{

    public void testAddMavenTwoProject()
        throws Exception
    {
        String M2_POM_URL = p.getProperty( "M2_POM_URL" );
        String M2_POM_USERNAME = p.getProperty( "M2_POM_USERNAME" );
        String M2_POM_PASSWORD = p.getProperty( "M2_POM_PASSWORD" );
        String M2_PROJ_GRP_NAME = p.getProperty( "M2_PROJ_GRP_NAME" );
        String M2_PROJ_GRP_ID = p.getProperty( "M2_PROJ_GRP_ID" );
        String M2_PROJ_GRP_DESCRIPTION = p.getProperty( "M2_PROJ_GRP_DESCRIPTION" );
        // Enter values into Add Maven Two Project fields, and submit
        addMavenTwoProject( M2_POM_URL, M2_POM_USERNAME, M2_POM_PASSWORD, null, true );
        // Wait Struct Listener
        assertProjectGroupSummaryPage( M2_PROJ_GRP_NAME, M2_PROJ_GRP_ID, M2_PROJ_GRP_DESCRIPTION );
    }

    @Test( dependsOnMethods = { "testAddProjectGroup" })
    public void testAddMavenTwoProjectFromRemoteSourceToNonDefaultProjectGroup()
        throws Exception
    {
        String TEST_PROJ_GRP_NAME = p.getProperty( "TEST_PROJ_GRP_NAME" );
        String TEST_PROJ_GRP_ID = p.getProperty( "TEST_PROJ_GRP_ID" );
        String TEST_PROJ_GRP_DESCRIPTION = p.getProperty( "TEST_PROJ_GRP_DESCRIPTION" );

        String M2_POM_URL = p.getProperty( "M2_POM_URL" );
        String M2_POM_USERNAME = p.getProperty( "M2_POM_USERNAME" );
        String M2_POM_PASSWORD = p.getProperty( "M2_POM_PASSWORD" );
        addMavenTwoProject( M2_POM_URL, M2_POM_USERNAME, M2_POM_PASSWORD, TEST_PROJ_GRP_NAME, true );

        assertProjectGroupSummaryPage( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
    }

    /**
     * Test invalid pom url
     */
    public void testNoPomSpecified()
        throws Exception
    {
        submitAddMavenTwoProjectPage( "", false );
        assertTextPresent( "Either POM URL or Upload POM is required." );
    }

    /**
     * Test when scm element is missing from pom
     */
    public void testMissingScmElementPom()
        throws Exception
    {
        String pomUrl = p.getProperty( "NOT_SCM_POM_URL" );
        submitAddMavenTwoProjectPage( pomUrl, false );
        assertTextPresent( "Missing ''scm'' element in the POM, project Maven Two Project" );
    }

    /**
     * test with a malformed pom url
     */
    public void testMalformedPomUrl()
        throws Exception
    {
        String pomUrl = "aaa";
        submitAddMavenTwoProjectPage( pomUrl, false);
        assertTextPresent( "The specified resource cannot be accessed. Please try again later or contact your administrator." );
    }

    /**
     * Test when the connection element is missing from the scm tag
     */
    public void testMissingConnectionElement()
        throws Exception
    {
        String pomUrl = p.getProperty( "MISS_CONECT_POM_URL" );
        submitAddMavenTwoProjectPage( pomUrl, false );
        assertTextPresent( "Missing 'connection' sub-element in the 'scm' element in the POM." );
    }

    /**
     * test unallowed file protocol
     */
    public void testNotAllowedProtocol()
        throws Exception
    {
        String pomUrl = "file:///pom.xml";
        submitAddMavenTwoProjectPage( pomUrl, false );
        assertTextPresent( "The specified resource isn't a file or the protocol used isn't allowed." );
    }

    /**
     * Test when the parent pom is missing or not yet added in continuum
     */
    public void testMissingParentPom()
        throws Exception
    {
        String pomUrl = p.getProperty( "MISS_PARENT_POM_URL" );
        submitAddMavenTwoProjectPage( pomUrl, false );
        assertTextPresent( "Missing artifact trying to build the POM. Check that its parent POM is available or add it first in Continuum." );
    }

    /**
     * Test when the modules/subprojects specified in the pom are not found
     */
    public void testMissingModules()
        throws Exception
    {
        String pomUrl = p.getProperty( "MISS_SUBPRO_POM_URL" );
        submitAddMavenTwoProjectPage( pomUrl, false );
        assertTextPresent( "Unknown error trying to build POM." );
    }

    /**
     * test with an inaccessible pom url
     */
    public void testInaccessiblePomUrl()
        throws Exception
    {
        String pomUrl = "http://www.google.com";
        submitAddMavenTwoProjectPage( pomUrl, false );
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
}
