package org.apache.continuum.web.test.parent;

//import org.testng.Assert;
import org.apache.continuum.web.test.ConfigurationTest;

import java.net.URLEncoder;

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

public abstract class AbstractBuildAgentsTest
    extends AbstractContinuumTest
{
    public void assertBuildAgentPage()
    {
		assertPage("Continuum - Build Agents");
		assertTextPresent("Build Agents");
		assertTextPresent("Build Agent Groups");
		assertButtonWithValuePresent( "Add" );
    }

	public void goToAddBuildAgent()
    {
		goToBuildAgentPage();
		assertBuildAgentPage();
		clickAndWait("editBuildAgent_0"); //add button
		assertAddEditBuildAgentPage();
    }

	public void assertAddEditBuildAgentPage()
    {
		assertPage( "Continuum - Add/Edit Build Agent" );
        assertTextPresent( "Add/Edit Build Agent" );
        assertTextPresent( "Build Agent URL*:" );
		assertTextPresent( "Description:" );
		assertTextPresent( "Enabled" );
        assertElementPresent( "saveBuildAgent_buildAgent_url" );
		assertElementPresent( "saveBuildAgent_buildAgent_description");
        assertButtonWithValuePresent( "Save" );
        assertButtonWithValuePresent( "Cancel" );
    }

	public void removeBuildAgent( String agentName )
        throws Exception
	{
        clickLinkWithXPath( "//a[contains(@href,'deleteBuildAgent.action') and contains(@href, '" + URLEncoder.encode( agentName, "UTF-8" ) + "')]/img" );
		assertPage("Continuum - Delete Build Agent");
        assertTextPresent( "Delete Build Agent" );
        assertTextPresent( "Are you sure you want to delete build agent " + agentName + " ?" );
        assertButtonWithValuePresent( "Delete" );
        assertButtonWithValuePresent( "Cancel" );
        clickButtonWithValue( "Delete" );
        assertBuildAgentPage();
    }

	public void addBuildAgent( String agentURL, String description, boolean success, boolean enabled )
	{
		setFieldValue( "saveBuildAgent_buildAgent_url", agentURL );
	    setFieldValue("saveBuildAgent_buildAgent_description", description );
	    
	    if ( enabled )
	    {
	        checkField("saveBuildAgent_buildAgent_enabled");
	    }

		submit();

		if ( success )
	    {
	        assertBuildAgentPage();
	        assertElementPresent( "link=" + agentURL );
	        clickLinkWithText( agentURL );
	        assertTextPresent( new Boolean( enabled ).toString() );
	    }
	    else
	    {
	        assertAddEditBuildAgentPage();
	    }
	}

	public void goToEditBuildAgent( String name, String description )
	{
	   goToBuildAgentPage();
	   clickImgWithAlt( "Edit" );
	   assertAddEditBuildAgentPage();
	   assertFieldValue( name, "saveBuildAgent_buildAgent_url" );
	   assertFieldValue( description, "saveBuildAgent_buildAgent_description" );
	}

	public void addEditBuildAgent( String agentName, String newDesc )
	{
		assertFieldValue( agentName, "saveBuildAgent_buildAgent_url" );
		setFieldValue( "saveBuildAgent_buildAgent_description", newDesc );
		submit();
		assertBuildAgentPage();
		assertTextPresent( newDesc );
	}


	public void goToAddBuildAgentGroup()
    {
	    String BUILD_AGENT_NAME = getProperty( "BUILD_AGENT_NAME" );
	    String BUILD_AGENT_NAME2 = getProperty( "BUILD_AGENT_NAME2" );

		goToBuildAgentPage();
		clickAndWait("editBuildAgentGroup_0"); //add button
        String[] options =
            new String[] { "--- Available Build Agents ---", BUILD_AGENT_NAME, BUILD_AGENT_NAME2 };
        assertAddEditBuildAgentGroupPage( options, null );
    }


	public void addEditBuildAgentGroup( String name, String[] addBuildAgents, String[] removeBuildAgents,
            boolean success ) throws Exception
	{
		setFieldValue( "saveBuildAgentGroup_buildAgentGroup_name", name );
		if ( addBuildAgents != null && addBuildAgents.length > 0 )
		{
			for ( String ba : addBuildAgents )
			{
				selectValue( "buildAgentIds", ba );
				clickButtonWithValue( "->", false );
			}
		}
		if ( removeBuildAgents != null && removeBuildAgents.length > 0 )
		{
			for ( String ba : removeBuildAgents )
			{
				selectValue( "selectedBuildAgentIds", ba );
				clickButtonWithValue( "<-", false );
			}
		}
		submit();
		if ( success )
		{
			assertBuildAgentPage();
		}
		else
		{
			assertAddEditBuildAgentGroupPage( null, null );
		}
	}

	public void assertAddEditBuildAgentGroupPage( String[] availableBuildAgents, String[] usedBuildAgents )
    {
		assertPage( "Continuum - Add/Edit Build Agent Group" );
        assertTextPresent( "Add/Edit Build Agent Group" );
        assertTextPresent( "Name*:" );
		assertTextPresent( "Configure the used Build Agents:" );
		assertElementPresent( "buildAgentGroup.name" );
		if ( availableBuildAgents != null && availableBuildAgents.length > 0 )
        {
            assertOptionPresent( "buildAgentIds", availableBuildAgents );
        }
        if ( usedBuildAgents != null && usedBuildAgents.length > 0 )
        {
            assertOptionPresent( "selectedBuildAgentIds", usedBuildAgents );
        }

		assertButtonWithValuePresent( "Save" );
        assertButtonWithValuePresent( "Cancel" );
    }

	public void goToEditBuildAgentGroup( String name, String[] buildAgents )
    {
		goToBuildAgentPage();
        String xPath = "//preceding::td[text()='" + name + "']//following::img[@alt='Edit']";
        clickLinkWithXPath( xPath );
        assertAddEditBuildAgentGroupPage( null, buildAgents );
        assertFieldValue( name, "buildAgentGroup.name" );
    }


	public void removeBuildAgentGroup( String name )
	{
		goToBuildAgentPage();
		clickLinkWithXPath( "(//a[contains(@href,'deleteBuildAgentGroup.action') and contains(@href, '" + name + "')])//img" );
		assertPage("Continuum - Delete Build Agent Group");
		assertTextPresent( "Delete Build Agent" );
        assertTextPresent( "Are you sure you want to delete build agent group " + name + " ?" );
        assertButtonWithValuePresent( "Delete" );
        assertButtonWithValuePresent( "Cancel" );
        clickButtonWithValue( "Delete" );
        assertBuildAgentPage();
    }
}
