package org.apache.continuum.web.test.parent;

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

import org.testng.annotations.BeforeMethod;

public abstract class AbstractAdminTest
    extends AbstractContinuumTest
{
    @BeforeMethod( alwaysRun = true )
    public void loginAsAdmin()
    {
        loginAs( getProperty( "ADMIN_USERNAME" ), getProperty( "ADMIN_PASSWORD" ) );
    }

    protected void loginAs( String username, String password )
    {
        if ( !getSelenium().isElementPresent( "//span[@class='username' and text()='" + username + "']" ) )
        {
            login( username, password );
        }
    }

    protected String getBuildAgentUrl()
    {
        return baseUrl.substring( 0, baseUrl.indexOf( "/continuum" ) ) + "/continuum-buildagent/xmlrpc";
    }

    protected void addBuildAgent( String buildAgentUrl )
    {
        addBuildAgent( buildAgentUrl, "Default description" );
    }

    protected void addBuildAgent( String buildAgentUrl, String description )
    {
        goToBuildAgentPage();
        assertBuildAgentPage();

        if ( !isElementPresent( "link=" + buildAgentUrl ) )
        {

            clickAndWait( "editBuildAgent_0" ); //add button
            assertAddEditBuildAgentPage( true );

            setFieldValue( "saveBuildAgent_buildAgent_url", buildAgentUrl );
            setFieldValue( "saveBuildAgent_buildAgent_description", description );
            checkField( "saveBuildAgent_buildAgent_enabled" );

            submit();

            assertBuildAgentPage();
            assertElementPresent( "link=" + buildAgentUrl );
        }
    }

    public void goToAddBuildAgent()
    {
        goToBuildAgentPage();
        assertBuildAgentPage();
        clickAndWait("editBuildAgent_0"); //add button
        assertAddEditBuildAgentPage( true );
    }

    public void assertAddEditBuildAgentPage( boolean isChecked )
    {
        assertPage( "Continuum - Add/Edit Build Agent" );
        assertTextPresent( "Add/Edit Build Agent" );
        assertTextPresent( "Build Agent URL*:" );
        assertTextPresent( "Description:" );
        assertTextPresent( "Enabled" );
        assertElementPresent( "saveBuildAgent_buildAgent_url" );
        assertElementPresent( "saveBuildAgent_buildAgent_description");

        if ( isChecked )
        {
            assertIsChecked( "saveBuildAgent_buildAgent_enabled" );
        }

        assertButtonWithValuePresent( "Save" );
        assertButtonWithValuePresent( "Cancel" );
    }

    protected void addMaven2Project( String groupName )
        throws Exception
    {
        String M2_POM_URL = getProperty( "M2_DELETE_POM_URL" );
        String M2_POM_USERNAME = getProperty( "M2_POM_USERNAME" );
        String M2_POM_PASSWORD = getProperty( "M2_POM_PASSWORD" );
        String M2_PROJ_GRP_DESCRIPTION = getProperty( "M2_DELETE_PROJ_GRP_DESCRIPTION" );

        addMavenTwoProject( M2_POM_URL, M2_POM_USERNAME, M2_POM_PASSWORD, null, true );
        goToProjectGroupsSummaryPage();
        assertLinkPresent( groupName );
    }
}
