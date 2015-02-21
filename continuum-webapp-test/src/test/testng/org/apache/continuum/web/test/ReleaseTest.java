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

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;

@Test( groups = {"release"} )
public class ReleaseTest
    extends AbstractReleaseTest
{
    private String projectGroupName;

    private String projectGroupId;

    private String tagBase;

    private String tag;

    private String releaseVersion;

    private String developmentVersion;

    private String releaseProjectScmUrl;

    @BeforeClass
    public void createAndBuildProject()
    {
        projectGroupName = getProperty( "RELEASE_PROJECT_GROUP_NAME" );
        projectGroupId = getProperty( "RELEASE_PROJECT_GROUP_ID" );
        String description = "Release test projects";

        loginAsAdmin();

        String pomUrl = getProperty( "MAVEN2_POM_URL" );
        String pomUsername = getProperty( "MAVEN2_POM_USERNAME" );
        String pomPassword = getProperty( "MAVEN2_POM_PASSWORD" );
        String projectName = getProperty( "MAVEN2_POM_PROJECT_NAME" );

        removeProjectGroup( projectGroupName, false );

        addProjectGroup( projectGroupName, projectGroupId, description, true, false );
        clickLinkWithText( projectGroupName );

        if ( !isLinkPresent( projectName ) )
        {
            addMavenTwoProject( pomUrl, pomUsername, pomPassword, projectGroupName, true );

            buildProjectGroup( projectGroupName, projectGroupId, "", projectName, true );
        }
    }

    @BeforeMethod
    public void setUp()
        throws IOException
    {
        tagBase = getProperty( "RELEASE_PROJECT_TAGBASE" );
        tag = getProperty( "RELEASE_PROJECT_TAG" );
        releaseVersion = getProperty( "RELEASE_PROJECT_VERSION" );
        developmentVersion = getProperty( "RELEASE_PROJECT_DEVELOPMENT_VERSION" );
        releaseProjectScmUrl = getProperty( "RELEASE_PROJECT_SCM_URL" );
    }

    @AfterMethod
    public void tearDown()
        throws Exception
    {
    }

    // can't test u/p locally and don't have a suitable SVN server to test against
    @Test( enabled = false )
    public void testReleasePrepareProjectWithInvalidUsernamePassword()
        throws Exception
    {
        String releaseUsername = "invalid";
        String releasePassword = "invalid";

        showProjectGroup( projectGroupName, projectGroupId, "" );
        clickButtonWithValue( RELEASE_BUTTON_TEXT );
        assertReleaseChoicePage();
        releasePrepareProject( releaseUsername, releasePassword, tagBase, "simple-example-13.0", "13.0",
                               "13.1-SNAPSHOT", null );
        assertReleasePhaseError();
    }

    public void testReleasePrepareProject()
        throws Exception
    {
        showProjectGroup( projectGroupName, projectGroupId, projectGroupId );

        clickButtonWithValue( RELEASE_BUTTON_TEXT );
        assertReleaseChoicePage();
        releasePrepareProject( "", "", tagBase, tag, releaseVersion, developmentVersion, "" );

        assertReleasePhaseSuccess();
    }

    @Test( dependsOnMethods = {"testReleasePrepareProject"} )
    public void testReleasePerformUsingProvidedParameters()
        throws Exception
    {
        String releaseUsername = "invalid";
        String releasePassword = "invalid";

        showProjectGroup( projectGroupName, projectGroupId, "" );
        clickButtonWithValue( RELEASE_BUTTON_TEXT );
        assertReleaseChoicePage();
        releasePerformProjectWithProvideParameters( releaseUsername, releasePassword, tagBase, tag,
                                                    releaseProjectScmUrl, "" );
    }

    // avoid the above test running between these so that the list of prepared releases is correct
    @Test( dependsOnMethods = {"testReleasePrepareProject"} )
    public void testReleasePrepareWithoutInterveningPerform()
        throws IOException
    {
        // CONTINUUM-2687
        showProjectGroup( projectGroupName, projectGroupId, "" );
        clickButtonWithValue( RELEASE_BUTTON_TEXT );
        assertReleaseChoicePage();

        // first attempt
        releasePrepareProject( "", "", tagBase, "simple-example-10.1", "10.1", "10.2-SNAPSHOT", "" );
        assertReleasePhaseSuccess();
        clickButtonWithValue( "Done" );

        // second attempt
        releasePrepareProject( "", "", tagBase, "simple-example-10.2", "10.2", "10.3-SNAPSHOT", "" );
        assertReleasePhaseSuccess();
        clickButtonWithValue( "Done" );

        // check that two versions are present
        Assert.assertEquals( Arrays.asList( getSelenium().getSelectOptions( "preparedReleaseId" ) ), Arrays.asList(
            "10.0", "10.1", "10.2", PROVIDE_RELEASE_PARAMETERS_TEXT ) );

        // check that 10.2 is selected by default
        Assert.assertEquals( getSelenium().getSelectedLabel( "preparedReleaseId" ), "10.2" );

        // test perform on 10.2
        selectPerformAndSubmit();

        setFieldValue( "goals", "clean validate" );
        submit();

        waitForRelease();

        assertReleasePhaseSuccess();
        clickButtonWithValue( "Done" );

        // verify that it is removed from the list again
        showProjectGroup( projectGroupName, projectGroupId, "" );
        clickButtonWithValue( RELEASE_BUTTON_TEXT );
        assertReleaseChoicePage();
        Assert.assertEquals( Arrays.asList( getSelenium().getSelectOptions( "preparedReleaseId" ) ), Arrays.asList(
            "10.0", "10.1", PROVIDE_RELEASE_PARAMETERS_TEXT ) );
        Assert.assertEquals( getSelenium().getSelectedLabel( "preparedReleaseId" ), "10.1" );
    }

}
