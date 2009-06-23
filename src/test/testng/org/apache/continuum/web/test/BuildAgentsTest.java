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
		String BUILD_AGENT_NAME = getProperty( "BUILD_AGENT_NAME" );
		String BUILD_AGENT_DESCRIPTION = getProperty( "BUILD_AGENT_DESCRIPTION" );
		String BUILD_AGENT_NAME2 = getProperty( "BUILD_AGENT_NAME2" );
		String BUILD_AGENT_DESCRIPTION2 = getProperty( "BUILD_AGENT_DESCRIPTION2" );
		String BUILD_AGENT_NAME3 = getProperty( "BUILD_AGENT_NAME3" );
		String BUILD_AGENT_DESCRIPTION3 = getProperty( "BUILD_AGENT_DESCRIPTION3" );
		
		enableDistributedBuilds();
		goToAddBuildAgent();
		addBuildAgent( BUILD_AGENT_NAME, BUILD_AGENT_DESCRIPTION, true ) ;
		goToAddBuildAgent();
		addBuildAgent( BUILD_AGENT_NAME2, BUILD_AGENT_DESCRIPTION2, true ) ;
		goToAddBuildAgent();
		addBuildAgent( BUILD_AGENT_NAME3, BUILD_AGENT_DESCRIPTION3, true ) ;
	}
	
	@Test( dependsOnMethods = { "testEditBuildAgent" } )
    public void testAddAnExistingBuildAgent()
    {
		String BUILD_AGENT_NAME = getProperty( "BUILD_AGENT_NAME" );
		String BUILD_AGENT_DESCRIPTION = getProperty( "BUILD_AGENT_DESCRIPTION" );
		
		goToAddBuildAgent();
		addBuildAgent( BUILD_AGENT_NAME, BUILD_AGENT_DESCRIPTION, false ) ;
        assertTextPresent( "Build agent already exists" );
    }
	
	@Test( dependsOnMethods = { "testAddBuildAgent" } )
	public void testEditBuildAgent()
	
    {
		String BUILD_AGENT_NAME = getProperty( "BUILD_AGENT_NAME" );
		String BUILD_AGENT_DESCRIPTION = getProperty( "BUILD_AGENT_DESCRIPTION" );
		String new_agentDescription = "new_agentDescription";
        
        goToEditBuildAgent( BUILD_AGENT_NAME, BUILD_AGENT_DESCRIPTION);
		addEditBuildAgent( BUILD_AGENT_NAME, new_agentDescription );
		goToEditBuildAgent( BUILD_AGENT_NAME, new_agentDescription);
		addEditBuildAgent( BUILD_AGENT_NAME, BUILD_AGENT_DESCRIPTION );
		
    } 
	
	@Test( dependsOnMethods = { "testAddAnExistingBuildAgent" } )
    public void testDeleteBuildAgent()
   
    {
        goToBuildAgentPage();
        String BUILD_AGENT_NAME3 = getProperty( "BUILD_AGENT_NAME3" );
        removeBuildAgent( BUILD_AGENT_NAME3 );
        assertTextNotPresent( BUILD_AGENT_NAME3 );
    }
	
	@Test( dependsOnMethods = { "testDeleteBuildAgent" } )
    public void testAddEmptyBuildAgent()
    {
    	String BUILD_AGENT_DESCRIPTION = getProperty( "BUILD_AGENT_DESCRIPTION" );
    	
    	goToAddBuildAgent();
		addBuildAgent( "", BUILD_AGENT_DESCRIPTION, false ) ;
		assertTextPresent( "Build agent url is required." );
    }
    
//TESTS FOR BUILD AGENT GROUPS    
    
    @Test( dependsOnMethods = { "testAddBuildAgent" } )
    public void testAddBuildAgentGroup()
    throws Exception
    {
    	String BUILD_AGENT_GROUPNAME = getProperty( "BUILD_AGENT_GROUPNAME" );
		
    	goToAddBuildAgentGroup();
		addEditBuildAgentGroup( BUILD_AGENT_GROUPNAME, new String[] { "Agent_url_name", "Second_Agent" }, new String[] {}, true );
    	
	}
	
	@Test( dependsOnMethods = { "testAddBuildAgentGroup" } )
    public void testEditBuildAgentGroup()
        throws Exception
    {
    	String BUILD_AGENT_GROUPNAME = getProperty( "BUILD_AGENT_GROUPNAME" );
        String newName = "new_agentgroupname";
        goToEditBuildAgentGroup( BUILD_AGENT_GROUPNAME, new String[] { "Agent_url_name", "Second_Agent" } );
        addEditBuildAgentGroup( newName, new String[] {},
                         new String[] { "Second_Agent" }, true );
        goToEditBuildAgentGroup( newName, new String[] { "Agent_url_name" } );
        addEditBuildAgentGroup( BUILD_AGENT_GROUPNAME, new String[] { "Second_Agent" },
                         new String[] {}, true );
    }
	
	@Test( dependsOnMethods = { "testEditBuildAgentGroup" } )
    public void testAddAnExistingBuildAgentGroup()
    throws Exception
    {
        String BUILD_AGENT_GROUPNAME = getProperty( "BUILD_AGENT_GROUPNAME" );
		
    	goToAddBuildAgentGroup();
	   	addEditBuildAgentGroup( BUILD_AGENT_GROUPNAME, new String[] { "Agent_url_name", "Second_Agent" }, new String[] {}, false );
	   	assertTextPresent( "Build agent group already exists." );
     
    }   
    
    @Test( dependsOnMethods = { "testAddAnExistingBuildAgentGroup" } )
    public void testAddEmptyBuildAgentGroupName()
    throws Exception
    {
    	
    	goToAddBuildAgentGroup();
    	addEditBuildAgentGroup( "", new String[] {}, new String[] {}, false );
		assertTextPresent( "Build agent group name required." );
    }
    
    @Test( dependsOnMethods = { "testAddEmptyBuildAgentGroupName" } )
    public void testDeleteBuildAgentGroup()
    {
    	String BUILD_AGENT_GROUPNAME = getProperty( "BUILD_AGENT_GROUPNAME" );
        removeBuildAgentGroup( BUILD_AGENT_GROUPNAME );
    }
    
    
}
