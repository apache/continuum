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
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.UnsupportedEncodingException;

@Test( groups = { "agent" } )
public class BuildAgentsTest
    extends AbstractBuildAgentsTest
{
    private String buildAgentGroup;

    private String buildAgentDescription;

    @BeforeMethod
    public void setUp()
    {
        enableDistributedBuilds();

        buildAgentGroup = getProperty( "BUILD_AGENT_GROUPNAME" );

        buildAgentDescription = getProperty( "BUILD_AGENT_DESCRIPTION" );
    }

    @AfterMethod
    public void tearDown()
        throws Exception
    {
        removeBuildAgentGroup( buildAgentGroup, false );

        removeBuildAgent( buildAgentUrl, false );

        disableDistributedBuilds();
    }

    public void testAddBuildAgent()
    {
        goToAddBuildAgent();
        addBuildAgent( buildAgentUrl, buildAgentDescription, true, true, true );
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
        String url = baseUrl
            + "/security/viewBuildAgent.action?buildAgent.url=test%3Cscript%3Ealert%28%27xss%27%29%3C/script%3E";
        getSelenium().open( url );
        Assert.assertFalse( getSelenium().isAlertPresent() );
        assertTextPresent( "<script>alert('xss')</script>" );
    }

    public void testEditBuildAgentXSS()
    {
        String url = baseUrl
            + "/security/editBuildAgent.action?buildAgent.url=test%3Cscript%3Ealert%28%27xss%27%29%3C/script%3E";
        getSelenium().open( url );
        Assert.assertFalse( getSelenium().isAlertPresent() );
    }

    public void testAddAnExistingBuildAgent()
    {
        addBuildAgent( buildAgentUrl );

        goToAddBuildAgent();
        addBuildAgent( buildAgentUrl, buildAgentDescription, false, false, true );
        assertTextPresent( "Build agent already exists" );
    }

    public void testEditBuildAgent()
        throws Exception
    {
        // reset agent to expected state
        addBuildAgent( buildAgentUrl, buildAgentDescription );

        String new_agentDescription = "new_agentDescription";

        goToEditBuildAgent( buildAgentUrl, buildAgentDescription );
        addEditBuildAgent( buildAgentUrl, new_agentDescription );
        goToEditBuildAgent( buildAgentUrl, new_agentDescription );
        addEditBuildAgent( buildAgentUrl, buildAgentDescription );
    }

    public void testDeleteBuildAgent()
        throws Exception
    {
        addBuildAgent( buildAgentUrl, buildAgentDescription );

        goToBuildAgentPage();
        removeBuildAgent( buildAgentUrl );
        assertTextNotPresent( buildAgentUrl );
    }

    public void testAddEmptyBuildAgent()
    {
        goToAddBuildAgent();
        addBuildAgent( "", buildAgentDescription, false, false, false );
        assertTextPresent( "Build agent url is required." );
    }

    //TESTS FOR BUILD AGENT GROUPS

    public void testAddBuildAgentGroupXSS()
        throws Exception
    {
        addBuildAgent( buildAgentUrl );

        goToAddBuildAgentGroup();
        addEditBuildAgentGroup( "%3Cscript%3Ealert%28%27xss%27%29%3C/script%3E", new String[]{ }, new String[]{ },
                                false );
        assertTextPresent( "Build agent group name contains invalid characters" );
    }

    public void testEditBuildAgentGroupXSS()
    {
        String url = baseUrl
            + "/security/editBuildAgentGroup.action?buildAgentGroup.name=test%3Cscript%3Ealert%28%27xss%27%29%3C/script%3E";
        getSelenium().open( url );
        Assert.assertFalse( getSelenium().isAlertPresent() );
    }

    public void testAddBuildAgentGroup()
        throws Exception
    {
        addBuildAgent( buildAgentUrl );

        goToAddBuildAgentGroup();
        addEditBuildAgentGroup( buildAgentGroup, new String[]{ buildAgentUrl }, new String[]{ }, true );
    }

    public void testEditBuildAgentGroup()
        throws Exception
    {
        addBuildAgent( buildAgentUrl );

        goToAddBuildAgentGroup();
        addEditBuildAgentGroup( buildAgentGroup, new String[]{ buildAgentUrl }, new String[]{ }, true );

        String newName = "new_agentgroupname";
        goToEditBuildAgentGroup( buildAgentGroup, new String[]{ buildAgentUrl } );
        addEditBuildAgentGroup( newName, new String[]{ }, new String[]{ buildAgentUrl }, true );
        goToEditBuildAgentGroup( newName, new String[]{ } );
        addEditBuildAgentGroup( buildAgentGroup, new String[]{ buildAgentUrl }, new String[]{ }, true );
    }

    public void testAddAnExistingBuildAgentGroup()
        throws Exception
    {
        addBuildAgent( buildAgentUrl );

        goToAddBuildAgentGroup();
        addEditBuildAgentGroup( buildAgentGroup, new String[]{ buildAgentUrl }, new String[]{ }, true );

        goToAddBuildAgentGroup();
        addEditBuildAgentGroup( buildAgentGroup, new String[]{ buildAgentUrl }, new String[]{ }, false );
        assertTextPresent( "Build agent group already exists." );
    }

    public void testAddEmptyBuildAgentGroupName()
        throws Exception
    {
        addBuildAgent( buildAgentUrl );

        goToAddBuildAgentGroup();
        addEditBuildAgentGroup( "", new String[]{ }, new String[]{ }, false );
        assertTextPresent( "Build agent group name is required." );
    }

    public void testDeleteBuildAgentGroup()
        throws UnsupportedEncodingException
    {
        addBuildAgent( buildAgentUrl );

        goToAddBuildAgentGroup();
        addEditBuildAgentGroup( buildAgentGroup, new String[]{ buildAgentUrl }, new String[]{ }, true );

        removeBuildAgentGroup( buildAgentGroup );
    }

    public void testAddBuildAgentGroupWithEmptyBuildAgent()
        throws Exception
    {
        addBuildAgent( buildAgentUrl );

        goToAddBuildAgentGroup();
        addEditBuildAgentGroup( buildAgentGroup, new String[]{ }, new String[]{ }, true );
    }
}
