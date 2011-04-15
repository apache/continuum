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

//import org.apache.continuum.web.test.parent.AbstractBuildQueueTest;
import org.testng.annotations.Test;
import org.apache.continuum.web.test.parent.AbstractBuildAgentsTest;
import org.testng.Assert;

@Test( groups = { "agent" }, dependsOnMethods = { "testWithCorrectUsernamePassword" } )
public class BuildAgentsTest
    extends AbstractBuildAgentsTest
{
    public void testAddBuildAgent()
    {
        String BUILD_AGENT_NAME = getProperty( "BUILD_AGENT_NAME" );
        String BUILD_AGENT_DESCRIPTION = getProperty( "BUILD_AGENT_DESCRIPTION" );
        String BUILD_AGENT_NAME2 = getProperty( "BUILD_AGENT_NAME2" );
        String BUILD_AGENT_DESCRIPTION2 = getProperty( "BUILD_AGENT_DESCRIPTION2" );
        String BUILD_AGENT_NAME3 = getProperty( "BUILD_AGENT_NAME3" );
        String BUILD_AGENT_DESCRIPTION3 = getProperty( "BUILD_AGENT_DESCRIPTION3" );

        try
        {
            enableDistributedBuilds();
            goToAddBuildAgent();
            addBuildAgent( BUILD_AGENT_NAME, BUILD_AGENT_DESCRIPTION, true, true, false );
            goToAddBuildAgent();
            addBuildAgent( BUILD_AGENT_NAME2, BUILD_AGENT_DESCRIPTION2, true, true, true );
            goToAddBuildAgent();
            addBuildAgent( BUILD_AGENT_NAME3, BUILD_AGENT_DESCRIPTION3, true, false, false );
        }
        finally
        {
            disableDistributedBuilds();
        }
    }

    public void testAddBuildAgentWithXSS()
    {
        try
        {
            String invalidUrl = "http://sampleagent/<script>alert('gotcha')</script>";
            String invalidDescription = "blah blah <script>alert('gotcha')</script> blah blah";
            enableDistributedBuilds();
            goToAddBuildAgent();
            addBuildAgent( invalidUrl, invalidDescription, false, true, false );

            assertTextPresent( "Build agent url is invalid." );
            assertTextPresent( "Build agent description contains invalid characters." );
        }
        finally
        {
            disableDistributedBuilds();
        }
    }

    public void testViewBuildAgentInstallationXSS()
    {
        getSelenium().open( baseUrl + "/security/viewBuildAgent.action?buildAgent.url=test%3Cscript%3Ealert%28%27xss%27%29%3C/script%3E" );
        Assert.assertFalse( getSelenium().isAlertPresent() );
        assertTextPresent( "<script>alert('xss')</script>" );
    }

    public void testEditBuildAgentXSS()
    {
        getSelenium().open( baseUrl + "/security/editBuildAgent.action?buildAgent.url=test%3Cscript%3Ealert%28%27xss%27%29%3C/script%3E" );
        Assert.assertFalse( getSelenium().isAlertPresent() );
    }

    @Test( dependsOnMethods = { "testEditBuildAgent" } )
    public void testAddAnExistingBuildAgent()
    {
        String BUILD_AGENT_NAME = getProperty( "BUILD_AGENT_NAME" );
        String BUILD_AGENT_DESCRIPTION = getProperty( "BUILD_AGENT_DESCRIPTION" );

        try
        {
            enableDistributedBuilds();
            goToAddBuildAgent();
            addBuildAgent( BUILD_AGENT_NAME, BUILD_AGENT_DESCRIPTION, false, false, false ) ;
            assertTextPresent( "Build agent already exists" );
        }
        finally
        {
            disableDistributedBuilds();
        }
    }

    @Test( dependsOnMethods = { "testAddBuildAgent" } )
    public void testEditBuildAgent()
    {
        String BUILD_AGENT_NAME = getProperty( "BUILD_AGENT_NAME" );
        String BUILD_AGENT_DESCRIPTION = getProperty( "BUILD_AGENT_DESCRIPTION" );
        String new_agentDescription = "new_agentDescription";

        try
        {
            enableDistributedBuilds();
            goToEditBuildAgent( BUILD_AGENT_NAME, BUILD_AGENT_DESCRIPTION );
            addEditBuildAgent( BUILD_AGENT_NAME, new_agentDescription );
            goToEditBuildAgent( BUILD_AGENT_NAME, new_agentDescription);
            addEditBuildAgent( BUILD_AGENT_NAME, BUILD_AGENT_DESCRIPTION );
        }
        finally
        {
            disableDistributedBuilds();
        }
    }

    @Test( dependsOnMethods = { "testAddAnExistingBuildAgent" } )
    public void testDeleteBuildAgent()
    {
        try
        {
            enableDistributedBuilds();
            goToBuildAgentPage();
            String BUILD_AGENT_NAME = getProperty( "BUILD_AGENT_NAME" );
            removeBuildAgent( BUILD_AGENT_NAME );
            assertTextNotPresent( BUILD_AGENT_NAME );
        }
        finally
        {
            disableDistributedBuilds();
        }
    }

	@Test( dependsOnMethods = { "testDeleteBuildAgent" } )
    public void testAddEmptyBuildAgent()
    {
    	String BUILD_AGENT_DESCRIPTION = getProperty( "BUILD_AGENT_DESCRIPTION" );

    	try
    	{
    	    enableDistributedBuilds();
    	    goToAddBuildAgent();
    	    addBuildAgent( "", BUILD_AGENT_DESCRIPTION, false, false, false ) ;
    	    assertTextPresent( "Build agent url is required." );
    	}
    	finally
    	{
    	    disableDistributedBuilds();
    	}
    }

    @Test( dependsOnMethods = { "testDeleteBuildAgent" }, enabled=false )
    public void testBuildSuccessWithDistributedBuildsAfterDisableEnableOfBuildAgent()
        throws Exception
    {
        String BUILD_AGENT_NAME = getProperty( "BUILD_AGENT_NAME2" );
        String BUILD_AGENT_DESCRIPTION = getProperty( "BUILD_AGENT_DESCRIPTION2" );
        String M2_PROJ_GRP_NAME = getProperty( "M2_DELETE_PROJ_GRP_NAME" );
        String M2_PROJ_GRP_ID = getProperty( "M2_DELETE_PROJ_GRP_ID" );
        String M2_POM_URL = getProperty( "M2_DELETE_POM_URL" );
        String M2_POM_USERNAME = getProperty( "M2_POM_USERNAME" );
        String M2_POM_PASSWORD = getProperty( "M2_POM_PASSWORD" );
        String M2_PROJ_GRP_DESCRIPTION = getProperty( "M2_DELETE_PROJ_GRP_DESCRIPTION" );

        try
        {
            enableDistributedBuilds();

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
        finally
        {
            disableDistributedBuilds();
        }
    }

//TESTS FOR BUILD AGENT GROUPS

    @Test( dependsOnMethods = { "testAddBuildAgent" } )
    public void testAddBuildAgentGroupXSS()
        throws Exception
    {
        try
        {
            enableDistributedBuilds();
            goToAddBuildAgentGroup();
            addEditBuildAgentGroup( "%3Cscript%3Ealert%28%27xss%27%29%3C/script%3E", new String[]{}, new String[] {}, false );
            assertTextPresent( "Build agent group name contains invalid characters" );
        }
        finally
        {
            disableDistributedBuilds();
        }
    }

    public void testEditBuildAgentGroupXSS()
    {
        getSelenium().open( baseUrl + "/security/editBuildAgentGroup.action?buildAgentGroup.name=test%3Cscript%3Ealert%28%27xss%27%29%3C/script%3E" );
        Assert.assertFalse( getSelenium().isAlertPresent() );
    }

    @Test( dependsOnMethods = { "testAddBuildAgent", "testDeleteBuildAgent" } )
    public void testAddBuildAgentGroup()
        throws Exception
    {
        String BUILD_AGENT_NAME2 = getProperty( "BUILD_AGENT_NAME2" );
        String BUILD_AGENT_NAME3 = getProperty( "BUILD_AGENT_NAME3" );
        String BUILD_AGENT_GROUPNAME = getProperty( "BUILD_AGENT_GROUPNAME" );

        try
        {
            enableDistributedBuilds();
            goToAddBuildAgentGroup();
            addEditBuildAgentGroup( BUILD_AGENT_GROUPNAME, new String[] { BUILD_AGENT_NAME2, BUILD_AGENT_NAME3 }, new String[] {}, true );
        }
        finally
        {
            disableDistributedBuilds();
        }
    }

    @Test( dependsOnMethods = { "testAddBuildAgentGroup" } )
    public void testEditBuildAgentGroup()
        throws Exception
    {
        String BUILD_AGENT_NAME2 = getProperty( "BUILD_AGENT_NAME2" );
        String BUILD_AGENT_NAME3 = getProperty( "BUILD_AGENT_NAME3" );
        String BUILD_AGENT_GROUPNAME = getProperty( "BUILD_AGENT_GROUPNAME" );

        String newName = "new_agentgroupname";
        try
        {
            enableDistributedBuilds();
            goToEditBuildAgentGroup( BUILD_AGENT_GROUPNAME, new String[] { BUILD_AGENT_NAME2, BUILD_AGENT_NAME3 } );
            addEditBuildAgentGroup( newName, new String[] {},
                             new String[] { BUILD_AGENT_NAME3 }, true );
            goToEditBuildAgentGroup( newName, new String[] { BUILD_AGENT_NAME2 } );
            addEditBuildAgentGroup( BUILD_AGENT_GROUPNAME, new String[] { BUILD_AGENT_NAME3 },
                             new String[] {}, true );
        }
        finally
        {
            disableDistributedBuilds();
        }
    }

    @Test( dependsOnMethods = { "testEditBuildAgentGroup" } )
    public void testAddAnExistingBuildAgentGroup()
        throws Exception
    {
        String BUILD_AGENT_NAME2 = getProperty( "BUILD_AGENT_NAME2" );
        String BUILD_AGENT_NAME3 = getProperty( "BUILD_AGENT_NAME3" );
        String BUILD_AGENT_GROUPNAME = getProperty( "BUILD_AGENT_GROUPNAME" );

        try
        {
            enableDistributedBuilds();
            goToAddBuildAgentGroup();
           	addEditBuildAgentGroup( BUILD_AGENT_GROUPNAME, new String[] { BUILD_AGENT_NAME2, BUILD_AGENT_NAME3 }, new String[] {}, false );
           	assertTextPresent( "Build agent group already exists." );
        }
        finally
        {
            disableDistributedBuilds();
        }
    }

    @Test( dependsOnMethods = { "testAddAnExistingBuildAgentGroup" } )
    public void testAddEmptyBuildAgentGroupName()
        throws Exception
    {
        try
        {
            enableDistributedBuilds();
            goToAddBuildAgentGroup();
            addEditBuildAgentGroup( "", new String[] {}, new String[] {}, false );
            assertTextPresent( "Build agent group name required." );
        }
        finally
        {
            disableDistributedBuilds();
        }
    }

    @Test( dependsOnMethods = { "testAddEmptyBuildAgentGroupName" } )
    public void testDeleteBuildAgentGroup()
    {
        String BUILD_AGENT_GROUPNAME = getProperty( "BUILD_AGENT_GROUPNAME" );

        try
        {
            enableDistributedBuilds();
            removeBuildAgentGroup( BUILD_AGENT_GROUPNAME );
        }
        finally
        {
            disableDistributedBuilds();
        }
    }

    @Test( dependsOnMethods = { "testDeleteBuildAgentGroup" } )
    public void testAddBuildAgentGroupWithEmptyBuildAgent()
        throws Exception
    {
        String BUILD_AGENT_GROUPNAME = getProperty( "BUILD_AGENT_GROUPNAME" );

        try
        {
            enableDistributedBuilds();
            goToAddBuildAgentGroup();
            addEditBuildAgentGroup( BUILD_AGENT_GROUPNAME, new String[] {}, new String[] {}, true );
        }
        finally
        {
            disableDistributedBuilds();
        }
    }
}
