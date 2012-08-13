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
@Test( groups = {"distributed"} )
public class DistributedBuildTest
    extends AbstractBuildAgentsTest
{
    private String projectGroupName;

    @BeforeMethod
    public void setUp()
    {
        enableDistributedBuilds();

        projectGroupName = null;
    }

    @AfterMethod
    public void tearDown()
        throws Exception
    {
        if ( projectGroupName != null )
        {
            removeProjectGroup( projectGroupName, false );
        }

        disableDistributedBuilds();
    }

    @Test( dependsOnMethods = {"testDeleteBuildAgentGroup"} )
    public void testBuildProjectGroupNoBuildAgentConfigured()
        throws Exception
    {
        goToBuildAgentPage();
        removeBuildAgent( getBuildAgentUrl(), false );

        String M2_PROJ_GRP_NAME = getProperty( "M2_DELETE_PROJ_GRP_NAME" );
        String M2_PROJ_GRP_ID = getProperty( "M2_DELETE_PROJ_GRP_ID" );
        projectGroupName = M2_PROJ_GRP_NAME;

        addMavenTwoProject( getProperty( "M2_DELETE_POM_URL" ), getProperty( "M2_POM_USERNAME" ), getProperty(
            "M2_POM_PASSWORD" ), null, true );
        goToProjectGroupsSummaryPage();
        assertLinkPresent( projectGroupName );
        clickLinkWithText( projectGroupName );

        assertPage( "Continuum - Project Group" );

        showProjectGroup( projectGroupName, M2_PROJ_GRP_ID, "" );
        clickButtonWithValue( "Build all projects" );

        assertTextPresent( "Unable to build projects because no build agent is configured" );
    }

    public void testProjectGroupAllBuildSuccessWithDistributedBuilds()
        throws Exception
    {
        String M2_PROJ_GRP_NAME = getProperty( "M2_DELETE_PROJ_GRP_NAME" );
        String M2_PROJ_GRP_ID = getProperty( "M2_DELETE_PROJ_GRP_ID" );
        projectGroupName = M2_PROJ_GRP_NAME;

        addBuildAgent( getBuildAgentUrl() );

        addMavenTwoProject( getProperty( "M2_DELETE_POM_URL" ), getProperty( "M2_POM_USERNAME" ), getProperty(
            "M2_POM_PASSWORD" ), null, true );

        buildProjectGroup( M2_PROJ_GRP_NAME, M2_PROJ_GRP_ID, "", M2_PROJ_GRP_NAME, true );
    }

    @Test(
        dependsOnMethods = {"testAddBuildAgentGroupWithEmptyBuildAgent", "testAddBuildEnvironmentWithBuildAgentGroup"} )
    public void testProjectGroupNoBuildAgentConfiguredInBuildAgentGroup()
        throws Exception
    {
        String M2_PROJ_GRP_NAME = getProperty( "M2_DELETE_PROJ_GRP_NAME" );
        String M2_PROJ_GRP_ID = getProperty( "M2_DELETE_PROJ_GRP_ID" );
        String BUILD_ENV_NAME = getProperty( "BUILD_ENV_NAME" );
        projectGroupName = M2_PROJ_GRP_NAME;

        addMavenTwoProject( getProperty( "M2_DELETE_POM_URL" ), getProperty( "M2_POM_USERNAME" ), getProperty(
            "M2_POM_PASSWORD" ), null, true );
        goToProjectGroupsSummaryPage();
        assertLinkPresent( M2_PROJ_GRP_NAME );
        clickLinkWithText( M2_PROJ_GRP_NAME );

        assertPage( "Continuum - Project Group" );

        goToGroupBuildDefinitionPage( M2_PROJ_GRP_NAME, M2_PROJ_GRP_ID, "" );
        clickImgWithAlt( "Edit" );
        assertAddEditBuildDefinitionPage();
        selectValue( "profileId", BUILD_ENV_NAME );
        submit();
        assertGroupBuildDefinitionPage( M2_PROJ_GRP_NAME );

        clickLinkWithText( "Project Group Summary" );
        clickButtonWithValue( "Build all projects" );

        assertTextPresent( "Unable to build projects because no build agent is configured in the build agent group" );
    }

    public void testBuildMaven2ProjectWithTagDistributedBuild()
        throws Exception
    {
        String M2_POM_URL = getProperty( "M2_PROJ_WITH_TAG_POM_URL" );
        String M2_POM_USERNAME = getProperty( "M2_POM_USERNAME" );
        String M2_POM_PASSWORD = getProperty( "M2_POM_PASSWORD" );

        String M2_PROJ_GRP_NAME = getProperty( "M2_PROJ_WITH_TAG_PROJ_GRP_NAME" );
        String M2_PROJ_GRP_ID = getProperty( "M2_PROJ_WITH_TAG_PROJ_GRP_ID" );
        String M2_PROJ_GRP_DESCRIPTION = "";

        projectGroupName = M2_PROJ_GRP_NAME;

        addBuildAgent( getBuildAgentUrl() );

        addMavenTwoProject( M2_POM_URL, M2_POM_USERNAME, M2_POM_PASSWORD, null, true );
        assertProjectGroupSummaryPage( M2_PROJ_GRP_NAME, M2_PROJ_GRP_ID, M2_PROJ_GRP_DESCRIPTION );

        buildProjectGroup( M2_PROJ_GRP_NAME, M2_PROJ_GRP_ID, M2_PROJ_GRP_DESCRIPTION, M2_PROJ_GRP_NAME, true );
    }

    public void testBuildShellProjectWithDistributedBuildsEnabled()
        throws Exception
    {
        String SHELL_GROUP_NAME = getProperty( "SHELL_GROUP_NAME" );
        String SHELL_GROUP_ID = getProperty( "SHELL_GROUP_ID" );
        String SHELL_GROUP_DESC = getProperty( "SHELL_GROUP_DESC" );

        String SHELL_NAME = getProperty( "SHELL_NAME_TWO" );
        String SHELL_DESCRIPTION = getProperty( "SHELL_DESCRIPTION_TWO" );
        String SHELL_VERSION = getProperty( "SHELL_VERSION_TWO" );
        String SHELL_TAG = getProperty( "SHELL_TAG_TWO" );
        String SHELL_SCM_URL = getProperty( "SHELL_SCM_URL_TWO" );
        String SHELL_SCM_USERNAME = getProperty( "SHELL_SCM_USERNAME_TWO" );
        String SHELL_SCM_PASSWORD = getProperty( "SHELL_SCM_PASSWORD_TWO" );

        addProjectGroup( SHELL_GROUP_NAME, SHELL_GROUP_ID, SHELL_GROUP_DESC, true );

        projectGroupName = SHELL_GROUP_NAME;

        addBuildAgent( getBuildAgentUrl() );

        goToAddShellProjectPage();
        addProject( SHELL_NAME, SHELL_DESCRIPTION, SHELL_VERSION, SHELL_SCM_URL, SHELL_SCM_USERNAME, SHELL_SCM_PASSWORD,
                    SHELL_TAG, SHELL_GROUP_NAME, true, "shell" );
        assertProjectGroupSummaryPage( SHELL_GROUP_NAME, SHELL_GROUP_ID, SHELL_GROUP_DESC );

        goToProjectGroupsSummaryPage();
        clickLinkWithText( SHELL_GROUP_NAME );
        clickLinkWithText( "Build Definitions" );
        clickLinkWithXPath( "//table[@id='ec_table']/tbody/tr/td[14]/a/img" );

        editBuildDefinitionShellType();

        buildProjectGroup( SHELL_GROUP_NAME, SHELL_GROUP_ID, SHELL_GROUP_DESC, SHELL_NAME, true );

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
