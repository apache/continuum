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
public abstract class AbstractBuildDefinitionTemplateTest
    extends AbstractAdminTest
{
    void goToBuildDefinitionTemplatePage()
    {
        clickLinkWithText( "Build Definition Templates" );
        assertBuildDefinitionTemplatePage();
    }

    void assertBuildDefinitionTemplatePage()
    {
        assertPage( "Continuum - Build Definition Templates" );
        assertTextPresent( "Available Templates" );
        assertTextPresent( "Available Build Definitions" );
        assertButtonWithIdPresent( "buildDefinitionTemplate_0" );
        assertButtonWithIdPresent( "buildDefinitionAsTemplate_0" );
    }

    protected void goToAddTemplate()
    {
        goToBuildDefinitionTemplatePage();
        clickSubmitWithLocator( "buildDefinitionTemplate_0" );
        String[] options =
            new String[] { "--- Available Build Definitions ---", "Default Ant Build Definition",
                "Default Maven 1 Build Definition", "Default Maven Build Definition",
                "Default Shell Build Definition" };
        assertAddEditTemplatePage( options, null );
    }

    void assertAddEditTemplatePage( String[] pendingSelectBuild, String[] selectedBuild )
    {
        assertPage( "Continuum - Build Definition Template" );
        assertTextPresent( "Build Definition Template" );
        assertTextPresent( "Name" );
        assertElementPresent( "buildDefinitionTemplate.name" );
        assertTextPresent( "Configure the used Build Definitions" );
        if ( pendingSelectBuild != null && pendingSelectBuild.length > 0 )
        {
            assertOptionPresent( "buildDefinitionIds", pendingSelectBuild );
        }
        if ( selectedBuild != null && selectedBuild.length > 0 )
        {
            assertOptionPresent( "selectedBuildDefinitionIds", selectedBuild );
        }
        assertButtonWithValuePresent( "Save" );
        assertButtonWithValuePresent( "Cancel" );
    }

    protected void addEditTemplate( String name, String[] addBuildDefinitions, String[] removeBuildDefinitions,
                                    boolean success )
    {
        setFieldValue( "buildDefinitionTemplate.name", name );
        if ( addBuildDefinitions != null && addBuildDefinitions.length > 0 )
        {
            for ( String bd : addBuildDefinitions )
            {
                selectValue( "buildDefinitionIds", bd );
                clickButtonWithValue( "->", false );
            }
        }
        if ( removeBuildDefinitions != null && removeBuildDefinitions.length > 0 )
        {
            for ( String bd : removeBuildDefinitions )
            {
                selectValue( "selectedBuildDefinitionIds", bd );
                clickButtonWithValue( "<-", false );
            }
        }
        submit();
        if ( success )
        {
            assertBuildDefinitionTemplatePage();
        }
        else
        {
            assertAddEditTemplatePage( null, null );
        }
    }

    protected void goToEditTemplate( String name, String[] buildDefinitions )
    {
        goToBuildDefinitionTemplatePage();
        String xPath = "//preceding::td[text()='" + name + "']//following::img[@alt='Edit']";
        clickLinkWithXPath( xPath );
        assertAddEditTemplatePage( null, buildDefinitions );
        assertFieldValue( name, "buildDefinitionTemplate.name" );
    }

    protected void removeTemplate( String name )
    {
        goToBuildDefinitionTemplatePage();
        clickLinkWithXPath( "(//a[contains(@href,'deleteDefinitionTemplate') and contains(@href, '" + name
            + "')])//img" );
        assertPage( "Continuum - Delete Build Definition Template" );
        assertTextPresent( "Delete Build Definition Template" );
        assertTextPresent( "Are you sure you want to delete build definition template \"" + name + "\"?" );
        assertButtonWithValuePresent( "Delete" );
        assertButtonWithValuePresent( "Cancel" );
        clickButtonWithValue( "Delete" );
        assertBuildDefinitionTemplatePage();
    }

    protected void goToAddBuildDefinitionTemplate()
    {
        goToBuildDefinitionTemplatePage();
        clickSubmitWithLocator( "buildDefinitionAsTemplate_0" );
        assertAddEditBuildDefinitionTemplatePage();
    }

    protected void goToEditBuildDefinitionTemplate( String description )
    {
        goToBuildDefinitionTemplatePage();
        String xPath = "//preceding::td[text()='" + description + "']//following::img[@alt='Edit']";
        clickLinkWithXPath( xPath );
        assertAddEditBuildDefinitionTemplatePage();
    }

    void assertAddEditBuildDefinitionTemplatePage()
    {
        assertPage( "Continuum - Build Definition Template" );
        assertTextPresent( "Build Definition Template" );
        assertTextPresent( "POM filename*:" );
        assertElementPresent( "buildDefinition.buildFile" );
        assertTextPresent( "Goals:" );
        assertElementPresent( "buildDefinition.goals" );
        assertTextPresent( "Arguments:" );
        assertElementPresent( "buildDefinition.arguments" );
        assertTextPresent( "Build Fresh" );
        assertElementPresent( "buildDefinition.buildFresh" );
        assertTextPresent( "Always Build" );
        assertElementPresent( "buildDefinition.alwaysBuild" );
        assertTextPresent( "Is it default?" );
        assertTextPresent( "Schedule:" );
        assertElementPresent( "buildDefinition.schedule.id" );
        assertTextPresent( "Description" );
        assertElementPresent( "buildDefinition.description" );
        assertTextPresent( "Type" );
        assertElementPresent( "buildDefinition.type" );
        assertTextPresent( "Build Environment" );
        assertElementPresent( "buildDefinition.profile.id" );
        assertButtonWithValuePresent( "Save" );
        assertButtonWithValuePresent( "Cancel" );
    }

    protected void addEditBuildDefinitionTemplate( String buildFile, String goals, String arguments, String description,
                                                   boolean buildFresh, boolean alwaysBuild, boolean isDefault,
                                                   boolean success )
    {
        // Enter values into Add Build Definition fields, and submit
        setFieldValue( "buildDefinition.buildFile", buildFile );
        setFieldValue( "buildDefinition.goals", goals );
        setFieldValue( "buildDefinition.arguments", arguments );
        setFieldValue( "buildDefinition.description", description );
        if ( buildFresh )
        {
            checkField( "buildDefinition.buildFresh" );
        }
        else
        {
            uncheckField( "buildDefinition.buildFresh" );
        }
        if ( isDefault )
        {
            checkField( "buildDefinition.defaultForProject" );
        }
        else
        {
            uncheckField( "buildDefinition.defaultForProject" );
        }
        if ( alwaysBuild )
        {
            checkField( "buildDefinition.alwaysBuild" );
        }
        else
        {
            uncheckField( "buildDefinition.alwaysBuild" );
        }
        submit();
        if ( success )
        {
            assertBuildDefinitionTemplatePage();
        }
        else
        {
            assertAddEditBuildDefinitionTemplatePage();
        }
    }

    protected void removeBuildDefinitionTemplate( String description )
    {
        goToBuildDefinitionTemplatePage();
        String xPath = "//preceding::td[text()='" + description + "']//following::img[@alt='Delete']";
        clickLinkWithXPath( xPath );
        assertPage( "Continuum - Delete Build Definition Template" );
        assertTextPresent( "Delete Build Definition Template" );
        assertTextPresent( "Are you sure you want to delete build definition template \"" + description + "\"?" );
        assertButtonWithValuePresent( "Delete" );
        assertButtonWithValuePresent( "Cancel" );
        clickButtonWithValue( "Delete" );
        assertBuildDefinitionTemplatePage();
    }
}
