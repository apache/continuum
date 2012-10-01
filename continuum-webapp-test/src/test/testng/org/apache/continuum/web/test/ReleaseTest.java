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
import org.apache.continuum.web.test.parent.AbstractBuildAgentsTest;
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

@Test( groups = {"release"} )
public class ReleaseTest
    extends AbstractBuildAgentsTest
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
        projectGroupName = getProperty( "RELEASE_PROJECT_GROUP_NAME" );
        projectGroupId = getProperty( "RELEASE_PROJECT_GROUP_ID" );
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
        releaseBuildEnvironment = getProperty( "RELEASE_BUILD_ENV" );
        releaseBuildAgentGroup = getProperty( "RELEASE_BUILD_AGENT_GROUP" );
        errorMessageNoAgent = getProperty( "RELEASE_NO_AGENT_MESSAGE" );

        tagBase = getProperty( "RELEASE_PROJECT_TAGBASE" );
        tag = getProperty( "RELEASE_PROJECT_TAG" );
        releaseVersion = getProperty( "RELEASE_PROJECT_VERSION" );
        developmentVersion = getProperty( "RELEASE_PROJECT_DEVELOPMENT_VERSION" );
        releaseProjectScmUrl = getProperty( "RELEASE_PROJECT_SCM_URL" );

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

    public void testReleasePrepareWithoutInterveningPerform()
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

        // test perform on 1.1
        selectPerformAndSubmit();

        setFieldValue( "goals", "clean validate" );
        submit();

        waitForRelease();

        assertReleasePhaseSuccess();
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

    public void testReleasePrepareProjectWithInvalidUsernamePasswordInDistributedBuilds()
        throws Exception
    {
        String releaseUsername = "invalid";
        String releasePassword = "invalid";

        showProjectGroup( projectGroupName, projectGroupId, "" );
        clickButtonWithValue( RELEASE_BUTTON_TEXT );
        assertReleaseChoicePage();
        releasePrepareProject( releaseUsername, releasePassword, tagBase, tag, releaseVersion, developmentVersion,
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

        assertPreparedReleasesFileContainsBuildAgent();
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

    private void releasePrepareProject( String username, String password, String tagBase, String tag,
                                        String releaseVersion, String developmentVersion, String buildEnv )
    {
        goToReleasePreparePage();
        setFieldValue( "scmUsername", username );
        setFieldValue( "scmPassword", password );
        setFieldValue( "scmTag", tag );
        setFieldValue( "scmTagBase", tagBase );
        setFieldValue( "prepareGoals", "clean" );
        selectValue( "profileId", buildEnv );
        setFieldValue( "relVersions", releaseVersion );
        setFieldValue( "devVersions", developmentVersion );
        submit();

        waitForRelease();
    }

    private void releasePerformProjectWithProvideParameters( String username, String password, String tagBase,
                                                             String tag, String scmUrl, String buildEnv )
    {
        goToReleasePerformProvideParametersPage();
        setFieldValue( "scmUrl", scmUrl );
        setFieldValue( "scmUsername", username );
        setFieldValue( "scmPassword", password );
        setFieldValue( "scmTag", tag );
        setFieldValue( "scmTagBase", tagBase );
        setFieldValue( "goals", "clean deploy" );
        selectValue( "profileId", buildEnv );
        submit();

        waitForRelease();

        assertReleasePhaseError();
    }

    private void goToReleasePreparePage()
    {
        clickLinkWithLocator( "goal", false );
        submit();
        assertReleasePreparePage();
    }

    private void goToReleasePerformProvideParametersPage()
    {
        selectPerformAndSubmit();
        assertReleasePerformProvideParametersPage();
    }

    private void selectPerformAndSubmit()
    {
        clickLinkWithLocator( "//input[@name='goal' and @value='perform']", false );
        submit();
    }

    private void assertReleasePreparePage()
    {
        assertPage( "Continuum - Release Project" );
        assertTextPresent( "Prepare Project for Release" );
        assertTextPresent( "Release Prepare Parameters" );
        assertTextPresent( "SCM Username" );
        assertTextPresent( "SCM Password" );
        assertTextPresent( "SCM Tag" );
        assertTextPresent( "SCM Tag Base" );
        assertTextPresent( "SCM Comment Prefix" );
        assertTextPresent( "Preparation Goals" );
        assertTextPresent( "Arguments" );
        assertTextPresent( "Build Environment" );
        assertTextPresent( "Release Version" );
        assertTextPresent( "Next Development Version" );
        assertButtonWithValuePresent( "Submit" );
    }

    private void assertReleasePerformProvideParametersPage()
    {
        assertPage( "Continuum - Perform Project Release" );
        assertTextPresent( "Perform Project Release" );
        assertTextPresent( "Release Perform Parameters" );
        assertTextPresent( "SCM Connection URL" );
        assertTextPresent( "SCM Username" );
        assertTextPresent( "SCM Password" );
        assertTextPresent( "SCM Tag" );
        assertTextPresent( "SCM Tag Base" );
        assertTextPresent( "Perform Goals" );
        assertTextPresent( "Arguments" );
        assertTextPresent( "Build Environment" );
        assertButtonWithValuePresent( "Submit" );
    }

    private void assertReleaseError()
    {
        assertTextPresent( "Release Error" );
    }

    private void assertReleasePhaseError()
    {
        assertButtonWithValuePresent( "Rollback changes" );
        assertImgWithAlt( "Error" );
    }

    private void assertReleasePhaseSuccess()
    {
        assertButtonWithValuePresent( "Rollback changes" );
        assertElementNotPresent( "//img[@alt='Error']" );
    }

    private void waitForRelease()
    {
        String doneButtonLocator = "//input[@id='releaseCleanup_0']";
        String errorTextLocator = "//h3[text()='Release Error']";

        // condition for release is complete; "Done" button or "Release Error" in page is present
        waitForOneOfElementsPresent( Arrays.asList( doneButtonLocator, errorTextLocator ), true );
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
