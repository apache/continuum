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
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 * @author José Morales Martínez
 * @version $Id$
 */
public abstract class AbstractBuildEnvironmentTest
    extends AbstractAdminTest
{
    void goToBuildEnvironmentPage()
    {
        clickLinkWithText( "Build Environments" );
        assertBuildEnvironmentPage();
    }

    void assertBuildEnvironmentPage()
    {
        assertPage( "Continuum - Build Environments" );
        assertTextPresent( "Build Environments" );
        assertButtonWithValuePresent( "Add" );
    }

    protected void goToAddBuildEnvironment()
    {
        goToBuildEnvironmentPage();
        clickButtonWithValue( "Add" );
        assertAddBuildEnvironmentPage();
    }

    void assertAddBuildEnvironmentPage()
    {
        assertPage( "Continuum - Build Environment" );
        assertTextPresent( "Build Environment" );
        assertTextPresent( "Build Environment Name" );
        assertElementPresent( "profile.name" );
        assertButtonWithValuePresent( "Save" );
        assertButtonWithValuePresent( "Cancel" );
    }

    void assertEditBuildEnvironmentPage( String name )
    {
        assertAddBuildEnvironmentPage();
        assertTextPresent( "Installation Name" );
        assertTextPresent( "Type" );
        assertFieldValue( name, "profile.name" );
    }

    protected void addBuildEnvironment( String name, String[] installations, boolean success )
    {
        setFieldValue( "profile.name", name );
        submit();
        editBuildEnvironment( name, installations, success );
    }

    protected void addBuildEnvironmentWithBuildAgentGroup( String name, String[] installations,
                                                           String buildAgentGroupName )
    {
        setFieldValue( "profile.name", name );
        submit();
        editBuildEnvironmentWithBuildAgentGroup( name, installations, buildAgentGroupName, true );
    }

    protected void editBuildEnvironment( String name, String[] installations, boolean success )
    {
        setFieldValue( "profile.name", name );
        for ( String i : installations )
        {
            selectValue( "installationId", i );
            clickButtonWithValue( "Add" );
        }
        submit();
        if ( success )
        {
            assertBuildEnvironmentPage();
        }
        else
        {
            assertAddBuildEnvironmentPage();
        }
    }

    protected void editBuildEnvironmentWithBuildAgentGroup( String name, String[] installations,
                                                            String buildAgentGroupName, boolean success )
    {
        setFieldValue( "profile.name", name );
        selectValue( "profile.buildAgentGroup", buildAgentGroupName );
        for ( String i : installations )
        {
            selectValue( "installationId", i );
            clickButtonWithValue( "Add" );
        }
        submit();
        if ( success )
        {
            assertBuildEnvironmentPage();
        }
        else
        {
            assertAddBuildEnvironmentPage();
        }
    }

    protected void goToEditBuildEnvironment( String name )
    {
        goToBuildEnvironmentPage();
        String xPath = "//preceding::td[text()='" + name + "']//following::img[@alt='Edit']";
        clickLinkWithXPath( xPath );
        assertEditBuildEnvironmentPage( name );
    }

    protected void removeBuildEnvironment( String name )
    {
        goToBuildEnvironmentPage();
        String xPath = "//preceding::td[text()='" + name + "']//following::img[@alt='Delete']";
        clickLinkWithXPath( xPath );
        assertPage( "Continuum - Delete Build Environment" );
        assertTextPresent( "Delete Build Environment" );
        assertTextPresent( "Are you sure you want to delete Build Environment \"" + name + "\" ?" );
        assertButtonWithValuePresent( "Delete" );
        assertButtonWithValuePresent( "Cancel" );
        clickButtonWithValue( "Delete" );
        assertBuildEnvironmentPage();
    }
}
