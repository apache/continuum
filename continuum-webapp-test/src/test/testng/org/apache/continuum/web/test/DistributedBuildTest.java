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
    extends AbstractAdminTest
{
    @BeforeMethod
    public void setUp()
    {
        enableDistributedBuilds();
    }

    @AfterMethod
    public void tearDown()
    {
        disableDistributedBuilds();
    }

    public void testBuildProjectGroupNoBuildAgentConfigured()
        throws Exception
    {
        String M2_PROJ_GRP_NAME = getProperty( "M2_DELETE_PROJ_GRP_NAME" );
        String M2_PROJ_GRP_ID = getProperty( "M2_DELETE_PROJ_GRP_ID" );

        addMaven2Project( M2_PROJ_GRP_NAME );
        clickLinkWithText( M2_PROJ_GRP_NAME );

        assertPage( "Continuum - Project Group" );

        showProjectGroup( M2_PROJ_GRP_NAME, M2_PROJ_GRP_ID, "" );
        clickButtonWithValue( "Build all projects" );

        assertTextPresent( "Unable to build projects because no build agent is configured" );

        removeProjectGroup( M2_PROJ_GRP_NAME );
        assertLinkNotPresent( M2_PROJ_GRP_NAME );
    }

    public void testProjectGroupAllBuildSuccessWithDistributedBuilds()
        throws Exception
    {
        String M2_PROJ_GRP_NAME = getProperty( "M2_DELETE_PROJ_GRP_NAME" );
        String M2_PROJ_GRP_ID = getProperty( "M2_DELETE_PROJ_GRP_ID" );

        addBuildAgent( getBuildAgentUrl() );

        addMaven2Project( M2_PROJ_GRP_NAME );
        clickLinkWithText( M2_PROJ_GRP_NAME );

        assertPage( "Continuum - Project Group" );

        showProjectGroup( M2_PROJ_GRP_NAME, M2_PROJ_GRP_ID, "" );
        clickButtonWithValue( "Build all projects" );

        buildProjectGroup( M2_PROJ_GRP_NAME, M2_PROJ_GRP_ID, "", M2_PROJ_GRP_NAME, true );
    }

    @Test( dependsOnMethods = { "testAddBuildAgentGroupWithEmptyBuildAgent", "testAddBuildEnvironmentWithBuildAgentGroup" } )
    public void testProjectGroupNoBuildAgentConfiguredInBuildAgentGroup()
        throws Exception
    {
        String M2_PROJ_GRP_NAME = getProperty( "M2_DELETE_PROJ_GRP_NAME" );
        String M2_PROJ_GRP_ID = getProperty( "M2_DELETE_PROJ_GRP_ID" );
        String BUILD_ENV_NAME = getProperty( "BUILD_ENV_NAME" );

        addMaven2Project( M2_PROJ_GRP_NAME );
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

        removeProjectGroup( M2_PROJ_GRP_NAME );
        assertLinkNotPresent( M2_PROJ_GRP_NAME );
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

        addBuildAgent( getBuildAgentUrl() );

        addMavenTwoProject( M2_POM_URL, M2_POM_USERNAME, M2_POM_PASSWORD, null, true );
        assertProjectGroupSummaryPage( M2_PROJ_GRP_NAME, M2_PROJ_GRP_ID, M2_PROJ_GRP_DESCRIPTION );

        buildProjectGroup( M2_PROJ_GRP_NAME, M2_PROJ_GRP_ID, M2_PROJ_GRP_DESCRIPTION, M2_PROJ_GRP_NAME, true );

        removeProjectGroup( M2_PROJ_GRP_NAME );
        assertLinkNotPresent( M2_PROJ_GRP_NAME );
    }

}
