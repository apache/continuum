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

import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

@Test( groups = {"distributedRelease"} )
public class DistributedReleaseTest
    extends AbstractReleaseTest
{

    private static final String RELEASE_BUTTON_TEXT = "Release";

    private static final String PROVIDE_RELEASE_PARAMETERS_TEXT = "Provide Release Parameters";

    private String projectGroupName;

    private String projectGroupId;

    private String releaseBuildEnvironment;

    private String releaseBuildAgentGroup;

    private String tagBase;

    private String tag;

    private String releaseVersion;

    private String developmentVersion;

    private String errorMessageNoAgent;

    private String releaseProjectScmUrl;

    @BeforeClass
    public void createAndBuildProject()
    {
        projectGroupName = getProperty( "DIST_RELEASE_PROJECT_GROUP_NAME" );
        projectGroupId = getProperty( "DIST_RELEASE_PROJECT_GROUP_ID" );
        String description = "Distributed Release test projects";

        loginAsAdmin();

        enableDistributedBuilds();

        addBuildAgent( buildAgentUrl );

        String pomUrl = getProperty( "MAVEN2_POM_URL" );
        String pomUsername = getProperty( "MAVEN2_POM_USERNAME" );
        String pomPassword = getProperty( "MAVEN2_POM_PASSWORD" );
        String projectName = getProperty( "MAVEN2_POM_PROJECT_NAME" );

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
        releaseBuildEnvironment = getProperty( "DIST_RELEASE_BUILD_ENV" );
        releaseBuildAgentGroup = getProperty( "DIST_RELEASE_BUILD_AGENT_GROUP" );
        errorMessageNoAgent = getProperty( "DIST_RELEASE_NO_AGENT_MESSAGE" );

        tagBase = getProperty( "DIST_RELEASE_PROJECT_TAGBASE" );
        tag = getProperty( "DIST_RELEASE_PROJECT_TAG" );
        releaseVersion = getProperty( "DIST_RELEASE_PROJECT_VERSION" );
        developmentVersion = getProperty( "DIST_RELEASE_PROJECT_DEVELOPMENT_VERSION" );
        releaseProjectScmUrl = getProperty( "DIST_RELEASE_PROJECT_SCM_URL" );

        File file = new File( "target/conf/prepared-releases.xml" );

        if ( file.exists() && !file.delete() )
        {
            throw new IOException( "Unable to delete existing prepared-releases.xml file" );
        }

        enableDistributedBuilds();

        addBuildAgent( buildAgentUrl );

        createBuildEnvAndBuildagentGroup( releaseBuildEnvironment, releaseBuildAgentGroup );
    }

    @AfterMethod
    public void tearDown()
        throws Exception
    {
        removeBuildagentGroupFromBuildEnv( releaseBuildAgentGroup );

        removeBuildAgentGroup( releaseBuildAgentGroup );

        // enable agent if disabled
        goToBuildAgentPage();
        clickImgWithAlt( "Edit" );
        enableDisableBuildAgent( buildAgentUrl, true );

        disableDistributedBuilds();
    }

    public void testDistributedReleasePrepareWithoutInterveningPerform()
        throws IOException
    {
        // CONTINUUM-2687
        showProjectGroup( projectGroupName, projectGroupId, "" );
        clickButtonWithValue( RELEASE_BUTTON_TEXT );
        assertReleaseChoicePage();

        // first attempt
        releasePrepareProject( "", "", tagBase, "simple-example-1.1", "1.1", "1.2-SNAPSHOT", releaseBuildEnvironment );
        assertReleasePhaseSuccess();
        clickButtonWithValue( "Done" );

        // second attempt
        releasePrepareProject( "", "", tagBase, "simple-example-1.2", "1.2", "1.3-SNAPSHOT", releaseBuildEnvironment );
        assertReleasePhaseSuccess();
        clickButtonWithValue( "Done" );

        // check prepared releases content (timestamp version)
        String str = getPreparedReleasesContent();
        Assert.assertTrue( str.contains( "<releaseId>org.apache.continuum.examples.simple:simple-example:" ) );

        // check that two versions are present
        Assert.assertEquals( Arrays.asList( getSelenium().getSelectOptions( "preparedReleaseId" ) ), Arrays.asList(
            "1.1", "1.2", PROVIDE_RELEASE_PARAMETERS_TEXT ) );

        // check that 1.2 is selected by default
        Assert.assertEquals( getSelenium().getSelectedLabel( "preparedReleaseId" ), "1.2" );

        // test perform on 1.2
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
            "1.1", PROVIDE_RELEASE_PARAMETERS_TEXT ) );
        Assert.assertEquals( getSelenium().getSelectedLabel( "preparedReleaseId" ), "1.1" );
    }

    public void testReleasePrepareWhenAgentGoesDown()
        throws IOException
    {
        // CONTINUUM-2686
        showProjectGroup( projectGroupName, projectGroupId, "" );
        clickButtonWithValue( RELEASE_BUTTON_TEXT );
        assertReleaseChoicePage();

        releasePrepareProject( "", "", tagBase, "simple-example-2.0", "2.0", "2.1-SNAPSHOT", releaseBuildEnvironment );
        assertReleasePhaseSuccess();

        // disable agent
        goToBuildAgentPage();
        clickImgWithAlt( "Edit" );
        enableDisableBuildAgent( buildAgentUrl, false );

        // check prepared releases content
        String str = getPreparedReleasesContent();
        Assert.assertTrue( str.contains( "<releaseId>org.apache.continuum.examples.simple:simple-example" ) );

        // go back to release page
        showProjectGroup( projectGroupName, projectGroupId, "" );
        clickButtonWithValue( RELEASE_BUTTON_TEXT );
        assertReleaseChoicePage();

        // check that the version is present
        Assert.assertEquals( Arrays.asList( getSelenium().getSelectOptions( "preparedReleaseId" ) ), Arrays.asList(
            "2.0", PROVIDE_RELEASE_PARAMETERS_TEXT ) );
    }

    // can't test u/p locally and don't have a suitable SVN server to test against
    @Test( enabled = false )
    public void testReleasePrepareProjectWithInvalidUsernamePasswordInDistributedBuilds()
        throws Exception
    {
        String releaseUsername = "invalid";
        String releasePassword = "invalid";

        showProjectGroup( projectGroupName, projectGroupId, "" );
        clickButtonWithValue( RELEASE_BUTTON_TEXT );
        assertReleaseChoicePage();
        releasePrepareProject( releaseUsername, releasePassword, tagBase, "simple-example-3.0", "3.0", "3.1-SNAPSHOT",
                               releaseBuildEnvironment );
        assertReleasePhaseError();
        assertPreparedReleasesFileContainsBuildAgent();
    }

    /*
     * Test release prepare with no build agent configured in the selected build environment.
     */
    public void testReleasePrepareProjectWithNoBuildagentInBuildEnvironment()
        throws Exception
    {
        detachBuildagentFromGroup( releaseBuildAgentGroup );

        showProjectGroup( projectGroupName, projectGroupId, projectGroupId );

        clickButtonWithValue( RELEASE_BUTTON_TEXT );
        assertReleaseChoicePage();
        releasePrepareProject( "", "", tagBase, tag, releaseVersion, developmentVersion, releaseBuildEnvironment );

        assertReleaseError();

        assertTextPresent( errorMessageNoAgent );
    }

    /*
    * Test release prepare with no build agent group in the selected build environment.
    */
    public void testReleasePrepareProjectWithNoBuildagentGroupInBuildEnvironment()
        throws Exception
    {
        removeBuildagentGroupFromBuildEnv( releaseBuildEnvironment );

        showProjectGroup( projectGroupName, projectGroupId, projectGroupId );

        clickButtonWithValue( RELEASE_BUTTON_TEXT );
        assertReleaseChoicePage();
        releasePrepareProject( "", "", tagBase, tag, releaseVersion, developmentVersion, releaseBuildEnvironment );

        assertReleaseError();

        assertTextPresent( errorMessageNoAgent );
    }

    /*
    * Test release prepare with no build environment selected.
    */
    public void testReleasePrepareProjectWithNoBuildEnvironment()
        throws Exception
    {
        showProjectGroup( projectGroupName, projectGroupId, projectGroupId );

        clickButtonWithValue( RELEASE_BUTTON_TEXT );
        assertReleaseChoicePage();
        releasePrepareProject( "", "", tagBase, tag, releaseVersion, developmentVersion, "" );

        assertReleasePhaseSuccess();
        clickButtonWithValue( "Done" );

        assertPreparedReleasesFileContainsBuildAgent();

        // test subsequent perform
        selectPerformAndSubmit();

        setFieldValue( "goals", "clean validate" );
        submit();

        waitForRelease();

        assertReleasePhaseSuccess();
    }

    @Test( dependsOnMethods = {"testReleasePrepareProjectWithNoBuildEnvironment"} )
    public void testReleasePerformUsingProvidedParametersWithDistributedBuilds()
        throws Exception
    {
        String releaseUsername = "invalid";
        String releasePassword = "invalid";

        showProjectGroup( projectGroupName, projectGroupId, "" );
        clickButtonWithValue( RELEASE_BUTTON_TEXT );
        assertReleaseChoicePage();
        releasePerformProjectWithProvideParameters( releaseUsername, releasePassword, tagBase, tag,
                                                    releaseProjectScmUrl, releaseBuildEnvironment );
        assertPreparedReleasesFileContainsBuildAgent();
    }

    @Test( dependsOnMethods = {"testReleasePrepareProjectWithNoBuildEnvironment"} )
    public void testReleasePerformUsingProvidedParametersWithNoBuildEnvironment()
        throws Exception
    {
        String releaseUsername = "invalid";
        String releasePassword = "invalid";

        showProjectGroup( projectGroupName, projectGroupId, "" );
        clickButtonWithValue( RELEASE_BUTTON_TEXT );
        assertReleaseChoicePage();
        releasePerformProjectWithProvideParameters( releaseUsername, releasePassword, tagBase, tag,
                                                    releaseProjectScmUrl, "" );
        assertPreparedReleasesFileContainsBuildAgent();
    }

    private void createBuildEnvAndBuildagentGroup( String projectBuildEnv, String projectAgentGroup )
    {
        // add build agent group no agents
        goToBuildAgentPage();
        if ( !isTextPresent( projectAgentGroup ) )
        {
            clickAndWait( "//input[@id='editBuildAgentGroup_0']" );
            setFieldValue( "saveBuildAgentGroup_buildAgentGroup_name", projectAgentGroup );
            clickButtonWithValue( "Save" );
        }

        // add build environment with build agent group
        clickLinkWithText( "Build Environments" );
        if ( !isTextPresent( projectBuildEnv ) )
        {
            clickAndWait( "//input[@id='addBuildEnv_0']" );
            setFieldValue( "saveBuildEnv_profile_name", projectBuildEnv );
            clickButtonWithValue( "Save" );
        }

        attachBuildagentGroupToBuildEnv( releaseBuildEnvironment, releaseBuildAgentGroup );

        // attach build agent in build agent group created
        attachBuildagentInGroup( releaseBuildAgentGroup );
    }

    private void attachBuildagentGroupToBuildEnv( String projectBuildEnv, String projectAgentGroup )
    {
        clickLinkWithText( "Build Environments" );
        String xPath = "//preceding::td[text()='" + projectBuildEnv + "']//following::img[@alt='Edit']";
        clickLinkWithXPath( xPath );
        selectValue( "profile.buildAgentGroup", projectAgentGroup );
        clickButtonWithValue( "Save" );
    }

    private void removeBuildagentGroupFromBuildEnv( String projectBuildEnv )
    {
        clickLinkWithText( "Build Environments" );
        String xPath = "//preceding::td[text()='" + projectBuildEnv + "']//following::img[@alt='Edit']";
        if ( isElementPresent( "xpath=" + xPath ) )
        {
            clickLinkWithXPath( xPath );
            selectValue( "profile.buildAgentGroup", "" );
            clickButtonWithValue( "Save" );
        }
    }

    private void attachBuildagentInGroup( String projectAgentGroup )
    {
        String buildAgent = buildAgentUrl;

        clickLinkWithText( "Build Agents" );
        String xPath = "//preceding::td[text()='" + projectAgentGroup + "']//following::img[@alt='Edit']";
        clickLinkWithXPath( xPath );

        if ( isElementPresent(
            "xpath=//select[@id='saveBuildAgentGroup_buildAgentIds']/option[@value='" + buildAgent + "']" ) )
        {
            selectValue( "buildAgentIds", buildAgent );
            clickLinkWithXPath( "//input[@value='->']", false );
            submit();
        }
    }

    private void detachBuildagentFromGroup( String projectAgentGroup )
    {
        String buildAgent = buildAgentUrl;

        clickLinkWithText( "Build Agents" );
        String xPath = "//preceding::td[text()='" + projectAgentGroup + "']//following::img[@alt='Edit']";
        clickLinkWithXPath( xPath );

        if ( isElementPresent(
            "xpath=//select[@id='saveBuildAgentGroup_selectedBuildAgentIds']/option[@value='" + buildAgent + "']" ) )
        {
            selectValue( "selectedBuildAgentIds", buildAgent );
            clickLinkWithXPath( "//input[@value='<-']", false );
            submit();
        }
    }

    private void assertPreparedReleasesFileContainsBuildAgent()
        throws Exception
    {
        String str = getPreparedReleasesContent();

        Assert.assertTrue( str.contains( "<buildAgentUrl>" + buildAgentUrl + "</buildAgentUrl>" ),
                           "prepared-releases.xml was not populated" );
    }

    private String getPreparedReleasesContent()
        throws IOException
    {
        File file = new File( "target/conf/prepared-releases.xml" );
        Assert.assertTrue( file.exists(), "prepared-releases.xml was not created" );

        FileInputStream fis = null;
        BufferedReader reader = null;

        try
        {
            fis = new FileInputStream( file );
            reader = new BufferedReader( new InputStreamReader( fis ) );

            String strLine;
            StringBuilder str = new StringBuilder();
            while ( ( strLine = reader.readLine() ) != null )
            {
                str.append( strLine );
            }
            return str.toString();
        }
        finally
        {
            IOUtils.closeQuietly( reader );
            IOUtils.closeQuietly( fis );
        }
    }
}
