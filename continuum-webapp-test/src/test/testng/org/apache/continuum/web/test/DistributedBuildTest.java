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

import org.apache.continuum.web.test.parent.AbstractBuildAgentsTest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Based on AddMavenTwoProjectTest of Emmanuel Venisse test.
 *
 * @author José Morales Martínez
 * @version $Id$
 */
@Test( groups = { "distributed" } )
public class DistributedBuildTest
    extends AbstractBuildAgentsTest
{
    private String projectGroupName;

    private String projectGroupId;

    private String projectGroupDescription;

    private String pomUrl;

    private String pomUsername;

    private String pomPassword;

    private String projectName;

    private String buildEnvName;

    private String buildAgentGroupName;

    private String newBuildEnv;

    @BeforeMethod
    public void setUp()
    {
        enableDistributedBuilds();

        addBuildAgent( buildAgentUrl );

        projectGroupName = getProperty( "DISTRIBUTED_PROJECT_GROUP_NAME" );
        projectGroupId = getProperty( "DISTRIBUTED_PROJECT_GROUP_ID" );
        projectGroupDescription = getProperty( "DISTRIBUTED_PROJECT_GROUP_DESCRIPTION" );

        addProjectGroup( projectGroupName, projectGroupId, projectGroupDescription, true, false );

        pomUrl = getProperty( "MAVEN2_POM_URL" );
        pomUsername = getProperty( "MAVEN2_POM_USERNAME" );
        pomPassword = getProperty( "MAVEN2_POM_PASSWORD" );
        projectName = getProperty( "MAVEN2_POM_PROJECT_NAME" );

        buildAgentGroupName = getProperty( "DISTRIBUTED_BUILD_AGENT_GROUP_NAME" );
        buildEnvName = getProperty( "DISTRIBUTED_BUILD_ENV_NAME" );
        newBuildEnv = getProperty( "DISTRIBUTED_DUPLICATE_BUILD_ENV" );
    }

    @AfterMethod
    public void tearDown()
        throws Throwable
    {
        removeProjectGroup( projectGroupName, false );

        removeBuildEnvironment( buildEnvName, false );

        removeBuildEnvironment( newBuildEnv, false );

        removeBuildAgentGroup( buildAgentGroupName, false );

        disableDistributedBuilds();
    }

    public void testBuildProjectGroupNoBuildAgentConfigured()
        throws Exception
    {
        goToBuildAgentPage();
        removeBuildAgent( buildAgentUrl, false );

        addMavenTwoProject( pomUrl, pomUsername, pomPassword, projectGroupName, true );
        goToProjectGroupsSummaryPage();
        assertLinkPresent( projectGroupName );
        clickLinkWithText( projectGroupName );

        assertPage( "Continuum - Project Group" );

        clickButtonWithValue( "Build all projects" );

        assertTextPresent( "Unable to build projects because no build agent is configured" );
    }

    public void testProjectGroupAllBuildSuccessWithDistributedBuilds()
        throws Exception
    {
        addMavenTwoProject( pomUrl, pomUsername, pomPassword, projectGroupName, true );

        buildProjectGroup( projectGroupName, projectGroupId, projectGroupDescription, projectName, true );
    }

    public void testBuildMaven2ProjectWithTagDistributedBuild()
        throws Exception
    {
        String pomUrl = getProperty( "MAVEN2_PROJECT_WITH_TAG_POM_URL" );
        String pomUsername = "";
        String pomPassword = "";
        String projectName = getProperty( "MAVEN2_PROJECT_WITH_TAG_POM_PROJECT_NAME" );

        addMavenTwoProject( pomUrl, pomUsername, pomPassword, projectGroupName, true );
        assertProjectGroupSummaryPage( projectGroupName, projectGroupId, projectGroupDescription );

        buildProjectGroup( projectGroupName, projectGroupId, projectGroupDescription, projectName, true );
    }

    public void testBuildShellProjectWithDistributedBuildsEnabled()
        throws Exception
    {
        String projectName = getProperty( "SHELL_PROJECT_NAME" );
        String projectDescription = getProperty( "SHELL_PROJECT_DESCRIPTION" );
        String projectVersion = getProperty( "SHELL_PROJECT_VERSION" );
        String projectTag = getProperty( "SHELL_PROJECT_TAG" );
        String projectScmUrl = getProperty( "SHELL_PROJECT_SCM_URL" );
        String projectScmUsername = getProperty( "SHELL_PROJECT_SCM_USERNAME" );
        String projectScmPassword = getProperty( "SHELL_PROJECT_SCM_PASSWORD" );

        goToAddShellProjectPage();
        addProject( projectName, projectDescription, projectVersion, projectScmUrl, projectScmUsername,
                    projectScmPassword, projectTag, projectGroupName, true, "shell" );
        assertProjectGroupSummaryPage( projectGroupName, projectGroupId, projectGroupDescription );

        goToProjectGroupsSummaryPage();
        clickLinkWithText( projectGroupName );
        clickLinkWithText( "Build Definitions" );
        clickLinkWithXPath( "//table[@id='ec_table']/tbody/tr/td[14]/a/img" );

        editBuildDefinitionShellType();

        buildProjectGroup( projectGroupName, projectGroupId, projectGroupDescription, projectName, true );
    }

    public void testQueuePageWithProjectCurrentlyBuildingInDistributedBuilds()
        throws Exception
    {
        String pomUrl = getProperty( "MAVEN2_QUEUE_TEST_POM_URL" );
        String pomUsername = getProperty( "MAVEN2_QUEUE_TEST_POM_USERNAME" );
        String pomPassword = getProperty( "MAVEN2_QUEUE_TEST_POM_PASSWORD" );

        goToAddMavenTwoProjectPage();
        addMavenTwoProject( pomUrl, pomUsername, pomPassword, projectGroupName, true );

        buildProjectForQueuePageTest( projectGroupName, projectGroupId, projectGroupDescription );

        //check queue page while building
        getSelenium().open( "/continuum/admin/displayQueues!display.action" );
        assertPage( "Continuum - View Distributed Builds" );
        assertTextPresent( "Current Build" );
        assertTextPresent( "Build Queue" );
        assertTextPresent( "Current Prepare Build" );
        assertTextPresent( "Prepare Build Queue" );
        assertTextPresent( projectGroupName );
        assertTextPresent( "Build Agent URL" );
    }

    public void testAddBuildEnvironmentWithBuildAgentGroup()
    {
        addBuildAgentGroupAndEnvironment( new String[]{ buildAgentUrl } );
    }

    public void testProjectGroupNoBuildAgentConfiguredInBuildAgentGroup()
        throws Exception
    {
        addBuildAgentGroupAndEnvironment( new String[]{ } );

        addMavenTwoProject( pomUrl, pomUsername, pomPassword, projectGroupName, true );

        goToGroupBuildDefinitionPage( projectGroupName, projectGroupId, projectGroupDescription );
        clickImgWithAlt( "Edit" );
        assertAddEditBuildDefinitionPage();
        selectValue( "profileId", buildEnvName );
        submit();
        assertGroupBuildDefinitionPage( projectGroupName );

        clickLinkWithText( "Project Group Summary" );
        clickButtonWithValue( "Build all projects" );

        assertTextPresent( "Unable to build projects because no build agent is configured in the build agent group" );
    }

    public void testEditDuplicatedBuildEnvironmentDistributedBuilds()
    {
        addBuildAgentGroupAndEnvironment( new String[]{ buildAgentUrl } );

        goToAddBuildEnvironment();
        addBuildEnvironmentWithBuildAgentGroup( newBuildEnv, new String[]{ }, buildAgentGroupName );

        goToEditBuildEnvironment( newBuildEnv );
        editBuildEnvironmentWithBuildAgentGroup( buildEnvName, new String[]{ }, buildAgentGroupName, false );
        assertTextPresent( "A Build Environment with the same name already exists" );
    }

    public void testBuildSuccessWithDistributedBuildsAfterDisableEnableOfBuildAgent()
        throws Exception
    {
        addMavenTwoProject( pomUrl, pomUsername, pomPassword, projectGroupName, true );

        // disable then enable build agent
        goToBuildAgentPage();
        clickImgWithAlt( "Edit" );
        enableDisableBuildAgent( buildAgentUrl, false );
        clickImgWithAlt( "Edit" );
        enableDisableBuildAgent( buildAgentUrl, true );

        buildProjectGroup( projectGroupName, projectGroupId, projectGroupDescription, projectName, true );
    }

    private void addBuildAgentGroupAndEnvironment( String[] buildAgents )
    {
        // create build agent group
        goToAddBuildAgentGroup();
        addEditBuildAgentGroup( buildAgentGroupName, buildAgents, new String[]{ }, true );

        goToAddBuildEnvironment();
        addBuildEnvironmentWithBuildAgentGroup( buildEnvName, new String[]{ }, buildAgentGroupName );
    }

    private void editBuildDefinitionShellType()
    {
        setFieldValue( "buildFile", "build.sh" );
        setFieldValue( "arguments", "" );
        setFieldValue( "description", "description" );
        setFieldValue( "buildDefinitionType", "shell" );
        checkField( "alwaysBuild" );

        submit();
    }
}
