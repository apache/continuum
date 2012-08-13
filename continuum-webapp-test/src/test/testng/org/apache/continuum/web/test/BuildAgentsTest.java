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

import org.apache.continuum.web.test.parent.AbstractBuildAgentsTest;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test( groups = {"agent"} )
public class BuildAgentsTest
    extends AbstractBuildAgentsTest
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

    public void testAddBuildAgent()
    {
        String BUILD_AGENT_NAME = getBuildAgentUrl();
        String BUILD_AGENT_DESCRIPTION = getProperty( "BUILD_AGENT_DESCRIPTION" );

        goToAddBuildAgent();
        addBuildAgent( BUILD_AGENT_NAME, BUILD_AGENT_DESCRIPTION, true, true, true );
    }

    public void testAddBuildAgentWithXSS()
    {
        String invalidUrl = "http://sampleagent/<script>alert('gotcha')</script>";
        String invalidDescription = "blah blah <script>alert('gotcha')</script> blah blah";
        goToAddBuildAgent();
        addBuildAgent( invalidUrl, invalidDescription, false, true, false );

        assertTextPresent( "Build agent url is invalid." );
    }

    public void testViewBuildAgentInstallationXSS()
    {
        getSelenium().open( baseUrl +
                                "/security/viewBuildAgent.action?buildAgent.url=test%3Cscript%3Ealert%28%27xss%27%29%3C/script%3E" );
        Assert.assertFalse( getSelenium().isAlertPresent() );
        assertTextPresent( "<script>alert('xss')</script>" );
    }

    public void testEditBuildAgentXSS()
    {
        getSelenium().open( baseUrl +
                                "/security/editBuildAgent.action?buildAgent.url=test%3Cscript%3Ealert%28%27xss%27%29%3C/script%3E" );
        Assert.assertFalse( getSelenium().isAlertPresent() );
    }

    @Test( dependsOnMethods = {"testEditBuildAgent"} )
    public void testAddAnExistingBuildAgent()
    {
        String BUILD_AGENT_NAME = getBuildAgentUrl();
        String BUILD_AGENT_DESCRIPTION = getProperty( "BUILD_AGENT_DESCRIPTION" );

        goToAddBuildAgent();
        addBuildAgent( BUILD_AGENT_NAME, BUILD_AGENT_DESCRIPTION, false, false, true );
        assertTextPresent( "Build agent already exists" );
    }

    @Test( dependsOnMethods = {"testAddBuildAgent"} )
    public void testEditBuildAgent()
    {
        String BUILD_AGENT_DESCRIPTION = getProperty( "BUILD_AGENT_DESCRIPTION" );

        addBuildAgent( getBuildAgentUrl(), BUILD_AGENT_DESCRIPTION );

        String BUILD_AGENT_NAME = getBuildAgentUrl();
        String new_agentDescription = "new_agentDescription";

        goToEditBuildAgent( BUILD_AGENT_NAME, BUILD_AGENT_DESCRIPTION );
        addEditBuildAgent( BUILD_AGENT_NAME, new_agentDescription );
        goToEditBuildAgent( BUILD_AGENT_NAME, new_agentDescription );
        addEditBuildAgent( BUILD_AGENT_NAME, BUILD_AGENT_DESCRIPTION );
    }

    @Test( dependsOnMethods = {"testAddAnExistingBuildAgent", "testDeleteBuildAgentGroup"} )
    public void testDeleteBuildAgent()
        throws Exception
    {
        goToBuildAgentPage();
        String BUILD_AGENT_NAME = getBuildAgentUrl();
        removeBuildAgent( BUILD_AGENT_NAME );
        assertTextNotPresent( BUILD_AGENT_NAME );
    }

    @Test( dependsOnMethods = {"testDeleteBuildAgent"} )
    public void testAddEmptyBuildAgent()
    {
        String BUILD_AGENT_DESCRIPTION = getProperty( "BUILD_AGENT_DESCRIPTION" );

        goToAddBuildAgent();
        addBuildAgent( "", BUILD_AGENT_DESCRIPTION, false, false, false );
        assertTextPresent( "Build agent url is required." );
    }

    @Test( dependsOnMethods = {"testDeleteBuildAgent"}, enabled = false )
    public void testBuildSuccessWithDistributedBuildsAfterDisableEnableOfBuildAgent()
        throws Exception
    {
        addBuildAgent( getBuildAgentUrl() );

        String BUILD_AGENT_NAME = getBuildAgentUrl();
        String BUILD_AGENT_DESCRIPTION = getProperty( "BUILD_AGENT_DESCRIPTION" );
        String M2_PROJ_GRP_NAME = getProperty( "M2_DELETE_PROJ_GRP_NAME" );
        String M2_PROJ_GRP_ID = getProperty( "M2_DELETE_PROJ_GRP_ID" );
        String M2_POM_URL = getProperty( "M2_DELETE_POM_URL" );
        String M2_POM_USERNAME = getProperty( "M2_POM_USERNAME" );
        String M2_POM_PASSWORD = getProperty( "M2_POM_PASSWORD" );

        addMavenTwoProject( M2_POM_URL, M2_POM_USERNAME, M2_POM_PASSWORD, null, true );
        goToProjectGroupsSummaryPage();
        assertLinkPresent( M2_PROJ_GRP_NAME );

        clickLinkWithText( M2_PROJ_GRP_NAME );

        assertPage( "Continuum - Project Group" );

        // disable then enable build agent
        goToEditBuildAgent( BUILD_AGENT_NAME, BUILD_AGENT_DESCRIPTION );
        enableDisableBuildAgent( BUILD_AGENT_NAME, false );
        goToEditBuildAgent( BUILD_AGENT_NAME, BUILD_AGENT_DESCRIPTION );
        enableDisableBuildAgent( BUILD_AGENT_NAME, true );

        buildProjectGroup( M2_PROJ_GRP_NAME, M2_PROJ_GRP_ID, "", M2_PROJ_GRP_NAME, true );

        removeProjectGroup( M2_PROJ_GRP_NAME );
        assertLinkNotPresent( M2_PROJ_GRP_NAME );
    }

    //TESTS FOR BUILD AGENT GROUPS

    @Test( dependsOnMethods = {"testAddBuildAgent"} )
    public void testAddBuildAgentGroupXSS()
        throws Exception
    {
        addBuildAgent( getBuildAgentUrl() );

        goToAddBuildAgentGroup();
        addEditBuildAgentGroup( "%3Cscript%3Ealert%28%27xss%27%29%3C/script%3E", new String[]{}, new String[]{},
                                false );
        assertTextPresent( "Build agent group name contains invalid characters" );
    }

    public void testEditBuildAgentGroupXSS()
    {
        getSelenium().open( baseUrl +
                                "/security/editBuildAgentGroup.action?buildAgentGroup.name=test%3Cscript%3Ealert%28%27xss%27%29%3C/script%3E" );
        Assert.assertFalse( getSelenium().isAlertPresent() );
    }

    public void testAddBuildAgentGroup()
        throws Exception
    {
        addBuildAgent( getBuildAgentUrl() );

        String BUILD_AGENT_NAME = getBuildAgentUrl();
        String BUILD_AGENT_GROUPNAME = getProperty( "BUILD_AGENT_GROUPNAME" );

        goToAddBuildAgentGroup();
        addEditBuildAgentGroup( BUILD_AGENT_GROUPNAME, new String[]{BUILD_AGENT_NAME}, new String[]{}, true );
    }

    @Test( dependsOnMethods = {"testAddBuildAgentGroup"} )
    public void testEditBuildAgentGroup()
        throws Exception
    {
        String BUILD_AGENT_NAME = getBuildAgentUrl();
        String BUILD_AGENT_GROUPNAME = getProperty( "BUILD_AGENT_GROUPNAME" );

        String newName = "new_agentgroupname";
        goToEditBuildAgentGroup( BUILD_AGENT_GROUPNAME, new String[]{BUILD_AGENT_NAME} );
        addEditBuildAgentGroup( newName, new String[]{}, new String[]{BUILD_AGENT_NAME}, true );
        goToEditBuildAgentGroup( newName, new String[]{} );
        addEditBuildAgentGroup( BUILD_AGENT_GROUPNAME, new String[]{BUILD_AGENT_NAME}, new String[]{}, true );
    }

    @Test( dependsOnMethods = {"testEditBuildAgentGroup"} )
    public void testAddAnExistingBuildAgentGroup()
        throws Exception
    {
        String BUILD_AGENT_NAME = getBuildAgentUrl();
        String BUILD_AGENT_GROUPNAME = getProperty( "BUILD_AGENT_GROUPNAME" );

        goToAddBuildAgentGroup();
        addEditBuildAgentGroup( BUILD_AGENT_GROUPNAME, new String[]{BUILD_AGENT_NAME}, new String[]{}, false );
        assertTextPresent( "Build agent group already exists." );
    }

    @Test( dependsOnMethods = {"testAddAnExistingBuildAgentGroup"} )
    public void testAddEmptyBuildAgentGroupName()
        throws Exception
    {
        goToAddBuildAgentGroup();
        addEditBuildAgentGroup( "", new String[]{}, new String[]{}, false );
        assertTextPresent( "Build agent group name is required." );
    }

    @Test( dependsOnMethods = {"testAddEmptyBuildAgentGroupName"} )
    public void testDeleteBuildAgentGroup()
    {
        String BUILD_AGENT_GROUPNAME = getProperty( "BUILD_AGENT_GROUPNAME" );

        removeBuildAgentGroup( BUILD_AGENT_GROUPNAME );
    }

    @Test( dependsOnMethods = {"testDeleteBuildAgentGroup"} )
    public void testAddBuildAgentGroupWithEmptyBuildAgent()
        throws Exception
    {
        String BUILD_AGENT_GROUPNAME = getProperty( "BUILD_AGENT_GROUPNAME" );

        goToAddBuildAgentGroup();
        addEditBuildAgentGroup( BUILD_AGENT_GROUPNAME, new String[]{}, new String[]{}, true );
    }
}
