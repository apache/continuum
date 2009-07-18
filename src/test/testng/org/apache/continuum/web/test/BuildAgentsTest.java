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


@Test( groups = { "agent" }, dependsOnMethods = { "testDeleteBuildDefinitionTemplate" } )
public class BuildAgentsTest
    extends AbstractBuildAgentsTest
{

    public void testAddBuildAgent()
    {
		String BUILD_AGENT_NAME = getProperty( "BUILD_AGENT_NAME" ) + getTestId();
		String BUILD_AGENT_DESCRIPTION = getProperty( "BUILD_AGENT_DESCRIPTION" );
		String BUILD_AGENT_NAME2 = getProperty( "BUILD_AGENT_NAME2" ) + getTestId();
		String BUILD_AGENT_DESCRIPTION2 = getProperty( "BUILD_AGENT_DESCRIPTION2" );
		String BUILD_AGENT_NAME3 = getProperty( "BUILD_AGENT_NAME3" ) + getTestId();
		String BUILD_AGENT_DESCRIPTION3 = getProperty( "BUILD_AGENT_DESCRIPTION3" );

		enableDistributedBuilds();
        goToAddBuildAgent();
        addBuildAgent( BUILD_AGENT_NAME, BUILD_AGENT_DESCRIPTION, true );
        goToAddBuildAgent();
        addBuildAgent( BUILD_AGENT_NAME2, BUILD_AGENT_DESCRIPTION2, true );
        goToAddBuildAgent();
        addBuildAgent( BUILD_AGENT_NAME3, BUILD_AGENT_DESCRIPTION3, true );
        disableDistributedBuilds();
	}

	@Test( dependsOnMethods = { "testEditBuildAgent" } )
    public void testAddAnExistingBuildAgent()
    {
		String BUILD_AGENT_NAME = getProperty( "BUILD_AGENT_NAME" ) + getTestId();
		String BUILD_AGENT_DESCRIPTION = getProperty( "BUILD_AGENT_DESCRIPTION" );

		enableDistributedBuilds();
		goToAddBuildAgent();
		addBuildAgent( BUILD_AGENT_NAME, BUILD_AGENT_DESCRIPTION, false ) ;
        assertTextPresent( "Build agent already exists" );
        disableDistributedBuilds();
    }

	@Test( dependsOnMethods = { "testAddBuildAgent" } )
	public void testEditBuildAgent()

    {
		String BUILD_AGENT_NAME = getProperty( "BUILD_AGENT_NAME" ) + getTestId();
		String BUILD_AGENT_DESCRIPTION = getProperty( "BUILD_AGENT_DESCRIPTION" );
		String new_agentDescription = "new_agentDescription";

		enableDistributedBuilds();
        goToEditBuildAgent( BUILD_AGENT_NAME, BUILD_AGENT_DESCRIPTION);
		addEditBuildAgent( BUILD_AGENT_NAME, new_agentDescription );
		goToEditBuildAgent( BUILD_AGENT_NAME, new_agentDescription);
		addEditBuildAgent( BUILD_AGENT_NAME, BUILD_AGENT_DESCRIPTION );
		disableDistributedBuilds();
    }

	@Test( dependsOnMethods = { "testAddAnExistingBuildAgent" } )
    public void testDeleteBuildAgent()

    {
	    enableDistributedBuilds();
        goToBuildAgentPage();
        String BUILD_AGENT_NAME3 = getProperty( "BUILD_AGENT_NAME3" + getTestId() );
        removeBuildAgent( BUILD_AGENT_NAME3 );
        assertTextNotPresent( BUILD_AGENT_NAME3 );
        disableDistributedBuilds();
    }

	@Test( dependsOnMethods = { "testDeleteBuildAgent" } )
    public void testAddEmptyBuildAgent()
    {
    	String BUILD_AGENT_DESCRIPTION = getProperty( "BUILD_AGENT_DESCRIPTION" );

    	enableDistributedBuilds();
    	goToAddBuildAgent();
		addBuildAgent( "", BUILD_AGENT_DESCRIPTION, false ) ;
		assertTextPresent( "Build agent url is required." );
		disableDistributedBuilds();
    }

//TESTS FOR BUILD AGENT GROUPS

    @Test( dependsOnMethods = { "testAddBuildAgent" } )
    public void testAddBuildAgentGroup()
    throws Exception
    {
    	String BUILD_AGENT_GROUPNAME = getProperty( "BUILD_AGENT_GROUPNAME" );
    	enableDistributedBuilds();
    	goToAddBuildAgentGroup();
		addEditBuildAgentGroup( BUILD_AGENT_GROUPNAME, new String[] { getProperty( "BUILD_AGENT_NAME" ) + getTestId(), getProperty( "BUILD_AGENT_NAME2" ) + getTestId() }, new String[] {}, true );
		disableDistributedBuilds();
	}

	@Test( dependsOnMethods = { "testAddBuildAgentGroup" } )
    public void testEditBuildAgentGroup()
        throws Exception
    {
    	String BUILD_AGENT_GROUPNAME = getProperty( "BUILD_AGENT_GROUPNAME" );
        String newName = "new_agentgroupname";
        enableDistributedBuilds();
        goToEditBuildAgentGroup( BUILD_AGENT_GROUPNAME, new String[] { getProperty( "BUILD_AGENT_NAME" ) + getTestId(), getProperty( "BUILD_AGENT_NAME2" ) + getTestId() } );
        addEditBuildAgentGroup( newName, new String[] {},
                         new String[] { getProperty( "BUILD_AGENT_NAME2" ) + getTestId() }, true );
        goToEditBuildAgentGroup( newName, new String[] { getProperty( "BUILD_AGENT_NAME" ) + getTestId() } );
        addEditBuildAgentGroup( BUILD_AGENT_GROUPNAME, new String[] { getProperty( "BUILD_AGENT_NAME2" ) + getTestId() },
                         new String[] {}, true );
        disableDistributedBuilds();
    }

	@Test( dependsOnMethods = { "testEditBuildAgentGroup" } )
    public void testAddAnExistingBuildAgentGroup()
    throws Exception
    {
        String BUILD_AGENT_GROUPNAME = getProperty( "BUILD_AGENT_GROUPNAME" );

        enableDistributedBuilds();
    	goToAddBuildAgentGroup();
	   	addEditBuildAgentGroup( BUILD_AGENT_GROUPNAME, new String[] { getProperty( "BUILD_AGENT_NAME" ) + getTestId(), getProperty( "BUILD_AGENT_NAME2" ) + getTestId() }, new String[] {}, false );
	   	assertTextPresent( "Build agent group already exists." );
	   	disableDistributedBuilds();
    }

    @Test( dependsOnMethods = { "testAddAnExistingBuildAgentGroup" } )
    public void testAddEmptyBuildAgentGroupName()
    throws Exception
    {
        enableDistributedBuilds();
    	goToAddBuildAgentGroup();
    	addEditBuildAgentGroup( "", new String[] {}, new String[] {}, false );
		assertTextPresent( "Build agent group name required." );
		disableDistributedBuilds();
    }

    @Test( dependsOnMethods = { "testAddEmptyBuildAgentGroupName" } )
    public void testDeleteBuildAgentGroup()
    {
    	String BUILD_AGENT_GROUPNAME = getProperty( "BUILD_AGENT_GROUPNAME" );
    	enableDistributedBuilds();
        removeBuildAgentGroup( BUILD_AGENT_GROUPNAME );
        disableDistributedBuilds();
    }
}
