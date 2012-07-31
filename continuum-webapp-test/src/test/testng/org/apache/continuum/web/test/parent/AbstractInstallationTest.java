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

/**
 * @author José Morales Martínez
 * @version $Id$
 */
public abstract class AbstractInstallationTest
    extends AbstractAdminTest
{
    public void goToInstallationPage()
    {
        clickLinkWithText( "Installations" );
        assertInstallationPage();
    }

    public void assertInstallationPage()
    {
        assertPage( "Continuum - Installations" );
        assertTextPresent( "Installations" );
        assertButtonWithValuePresent( "Add" );
    }

    public void goToAddInstallationTool()
    {
        goToInstallationPage();
        clickButtonWithValue( "Add" );
        assertAddChoiceTypeInstallation();
        selectValue( "installationType", "Tool" );
        clickButtonWithValue( "Add" );
        assertAddInstallationToolPage();
    }

    public void goToAddInstallationVariable()
    {
        goToInstallationPage();
        clickButtonWithValue( "Add" );
        assertAddChoiceTypeInstallation();
        selectValue( "installationType", "Environment Variable" );
        clickButtonWithValue( "Add" );
        assertAddInstallationVariablePage();
    }

    public void assertAddChoiceTypeInstallation()
    {
        assertPage( "Continuum - Installation Type Choice" );
        assertTextPresent( "Installation Type Choice" );
        assertTextPresent( "Installation Type" );
        assertOptionPresent( "installationType", new String[] { "Tool", "Environment Variable" } );
        assertButtonWithValuePresent( "Add" );
        assertButtonWithValuePresent( "Cancel" );
    }

    public void assertAddInstallationToolPage()
    {
        assertEditInstallationToolPage();
        assertElementPresent( "automaticProfile" );
        assertTextPresent( "Create a Build Environment with the Installation name" );
    }

    public void assertEditInstallationToolPage()
    {
        assertPage( "Continuum - Installation" );
        assertTextPresent( "Continuum - Installation" );
        assertTextPresent( "Name" );
        assertElementPresent( "installation.name" );
        assertTextPresent( "Type" );
        assertOptionPresent( "installation.type", new String[] { "JDK", "Maven", "Maven 1", "ANT" } );
        assertTextPresent( "Value/Path" );
        assertElementPresent( "installation.varValue" );
        assertButtonWithValuePresent( "Save" );
        assertButtonWithValuePresent( "Cancel" );
    }

    public void assertAddInstallationVariablePage()
    {
        assertEditInstallationVariablePage();
        assertElementPresent( "automaticProfile" );
        assertTextPresent( "Create a Build Environment with the Installation name" );
    }

    public void assertEditInstallationVariablePage()
    {
        assertPage( "Continuum - Installation" );
        assertTextPresent( "Continuum - Installation" );
        assertTextPresent( "Name" );
        assertElementPresent( "installation.name" );
        assertTextPresent( "Environment Variable Name" );
        assertElementPresent( "installation.varName" );
        assertTextPresent( "Value/Path" );
        assertElementPresent( "installation.varValue" );
        assertButtonWithValuePresent( "Save" );
        assertButtonWithValuePresent( "Cancel" );
    }

    public void addInstallation( String name, String var, String path, boolean createBuildEnv, boolean tool,
                                 boolean success )
    {
        if ( createBuildEnv )
        {
            checkField( "automaticProfile" );
        }
        else
        {
            uncheckField( "automaticProfile" );
        }
        editInstallation( name, var, path, tool, success );
    }

    public void editInstallation( String name, String var, String path, boolean tool, boolean success )
    {
        setFieldValue( "installation.name", name );
        setFieldValue( "installation.varValue", path );
        if ( tool )
        {
            selectValue( "installation.type", var );
        }
        else
        {
            setFieldValue( "installation.varName", var );
        }
        submit();
        if ( success )
        {
            assertInstallationPage();
        }
        else if ( tool )
        {
            assertAddInstallationToolPage();
        }
        else
        {
            assertAddInstallationVariablePage();
        }
    }

    public void goToEditInstallation( String name, String var, String path, boolean tool )
    {
        goToInstallationPage();
        String xPath = "//preceding::td[text()='" + name + "']//following::img[@alt='Edit']";
        clickLinkWithXPath( xPath );
        if ( tool )
        {
            assertEditInstallationToolPage();
        }
        else
        {
            assertEditInstallationVariablePage();
            assertFieldValue( var, "installation.varName" );
        }
        assertFieldValue( name, "installation.name" );
        assertFieldValue( path, "installation.varValue" );
    }

    public void removeInstallation( String name )
    {
        goToInstallationPage();
        clickLinkWithXPath( "(//a[contains(@href,'deleteInstallation') and contains(@href, '" + name + "')])//img" );
        assertPage( "Continuum - Delete Installation" );
        assertTextPresent( "Delete Installation" );
        assertTextPresent( "Are you sure you want to delete \"" + name + "\" installation ?" );
        assertButtonWithValuePresent( "Delete" );
        assertButtonWithValuePresent( "Cancel" );
        clickButtonWithValue( "Delete" );
        assertInstallationPage();
    }

}
