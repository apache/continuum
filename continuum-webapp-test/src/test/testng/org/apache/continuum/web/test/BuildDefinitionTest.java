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

import org.apache.continuum.web.test.parent.AbstractAdminTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author José Morales Martínez
 * @version $Id$
 */
@Test( groups = { "buildDefinition" } )
public class BuildDefinitionTest
    extends AbstractAdminTest
{

    private String defaultProjectGroupName;

    private String defaultProjectGroupId;

    private String defaultProjectGroupDescription;

    private String projectGroupName;

    private String projectGroupId;

    private String projectGroupDescription;

    private String buildDefinitionPomName;

    private String buildDefinitionGoals;

    private String buildDefinitionArguments;

    private String buildDefinitionDescription;

    private String projectName;

    @BeforeClass
    public void createProject()
    {
        projectGroupName = getProperty( "BUILD_DEFINITION_PROJECT_GROUP_NAME" );
        projectGroupId = getProperty( "BUILD_DEFINITION_PROJECT_GROUP_ID" );
        projectGroupDescription = getProperty( "BUILD_DEFINITION_PROJECT_GROUP_DESCRIPTION" );

        projectName = getProperty( "MAVEN2_POM_PROJECT_NAME" );
        String projectPomUrl = getProperty( "MAVEN2_POM_URL" );
        String pomUsername = getProperty( "MAVEN2_POM_USERNAME" );
        String pomPassword = getProperty( "MAVEN2_POM_PASSWORD" );

        loginAsAdmin();
        addProjectGroup( projectGroupName, projectGroupId, projectGroupDescription, true, false );
        clickLinkWithText( projectGroupName );
        if ( !isLinkPresent( projectName ) )
        {
            addMavenTwoProject( projectPomUrl, pomUsername, pomPassword, projectGroupName, true );
        }
    }

    @BeforeMethod
    protected void setUp()
        throws Exception
    {
        defaultProjectGroupName = getProperty( "DEFAULT_PROJECT_GROUP_NAME" );
        defaultProjectGroupId = getProperty( "DEFAULT_PROJECT_GROUP_ID" );
        defaultProjectGroupDescription = getProperty( "DEFAULT_PROJECT_GROUP_DESCRIPTION" );

        buildDefinitionPomName = getProperty( "BUILD_DEFINITION_POM_NAME" );
        buildDefinitionGoals = getProperty( "BUILD_DEFINITION_GOALS" );
        buildDefinitionArguments = getProperty( "BUILD_DEFINITION_ARGUMENTS" );
        buildDefinitionDescription = getProperty( "BUILD_DEFINITION_DESCRIPTION" );
    }

    public void testDefaultGroupBuildDefinition()
        throws Exception
    {
        goToGroupBuildDefinitionPage( defaultProjectGroupName, defaultProjectGroupId, defaultProjectGroupDescription );
        String tableElement = "ec_table";
        assertCellValueFromTable( "Goals", tableElement, 0, 0 );
        assertCellValueFromTable( "Arguments", tableElement, 0, 1 );
        assertCellValueFromTable( "Build File", tableElement, 0, 2 );
        assertCellValueFromTable( "Schedule", tableElement, 0, 3 );
        assertCellValueFromTable( "Build Environment", tableElement, 0, 4 );
        assertCellValueFromTable( "From", tableElement, 0, 5 );
        assertCellValueFromTable( "Build Fresh", tableElement, 0, 6 );
        assertCellValueFromTable( "Default", tableElement, 0, 7 );
        assertCellValueFromTable( "Description", tableElement, 0, 8 );
        assertCellValueFromTable( "Type", tableElement, 0, 9 );
        assertCellValueFromTable( "Always Build", tableElement, 0, 10 );

        assertCellValueFromTable( "clean install", tableElement, 1, 0 );
        assertCellValueFromTable( "--batch-mode --non-recursive", tableElement, 1, 1 );
        assertCellValueFromTable( "pom.xml", tableElement, 1, 2 );
        assertCellValueFromTable( "DEFAULT_SCHEDULE", tableElement, 1, 3 );
        assertCellValueFromTable( "GROUP", tableElement, 1, 5 );
        assertCellValueFromTable( "false", tableElement, 1, 6 );
        assertCellValueFromTable( "true", tableElement, 1, 7 );
        assertCellValueFromTable( "Default Maven Build Definition", tableElement, 1, 8 );
        assertCellValueFromTable( "maven2", tableElement, 1, 9 );
        assertCellValueFromTable( "false", tableElement, 1, 10 );
        assertImgWithAlt( "Edit" );
        assertImgWithAlt( "Delete" );
        assertImgWithAlt( "Build" );
    }

    public void testAddInvalidGroupBuildDefinition()
        throws Exception
    {
        goToGroupBuildDefinitionPage( projectGroupName, projectGroupId, projectGroupDescription );
        clickButtonWithValue( "Add" );
        setFieldValue( "buildFile", "" );
        clickButtonWithValue( "Save" );
        assertTextPresent( "Build file is required and cannot contain spaces only" );
    }

    public void testAddGroupBuildDefinitionWithXSS()
        throws Exception
    {
        goToGroupBuildDefinitionPage( projectGroupName, projectGroupId, projectGroupDescription );
        clickButtonWithValue( "Add" );
        setFieldValue( "buildFile", "<script>alert('xss')</script>" );
        setFieldValue( "description", "<script>alert('xss')</script>" );
        clickButtonWithValue( "Save" );
        assertTextPresent( "Build file contains invalid characters." );
    }

    public void testBuildFromGroupBuildDefinition()
        throws Exception
    {
        goToGroupBuildDefinitionPage( projectGroupName, projectGroupId, projectGroupDescription );
        clickImgWithAlt( "Build" );
        assertProjectGroupSummaryPage( projectGroupName, projectGroupId, projectGroupDescription );
    }

    public void testAddDefaultGroupBuildDefinition()
        throws Exception
    {
        goToGroupBuildDefinitionPage( projectGroupName, projectGroupId, projectGroupDescription );
        clickButtonWithValue( "Add" );
        addEditGroupBuildDefinition( projectGroupName, buildDefinitionPomName, buildDefinitionGoals,
                                     buildDefinitionArguments, buildDefinitionDescription, true, false, true );
    }

    public void testAddNotDefaultGroupBuildDefinition()
        throws Exception
    {
        goToGroupBuildDefinitionPage( projectGroupName, projectGroupId, projectGroupDescription );
        clickButtonWithValue( "Add" );
        addEditGroupBuildDefinition( projectGroupName, buildDefinitionPomName, buildDefinitionGoals,
                                     buildDefinitionArguments, buildDefinitionDescription, false, false, false );
    }

    @Test( dependsOnMethods = { "testAddNotDefaultGroupBuildDefinition" } )
    public void testEditGroupBuildDefinition()
        throws Exception
    {
        String newPom = "newpom.xml";
        String newGoals = "new goals";
        String newArguments = "new arguments";
        String newDescription = "new description";
        goToGroupBuildDefinitionPage( projectGroupName, projectGroupId, projectGroupDescription );
        clickImgWithAlt( "Edit" );
        addEditGroupBuildDefinition( projectGroupName, newPom, newGoals, newArguments, newDescription, false, false,
                                     false );
        clickImgWithAlt( "Edit" );
        addEditGroupBuildDefinition( projectGroupName, buildDefinitionPomName, buildDefinitionGoals,
                                     buildDefinitionArguments, buildDefinitionDescription, true, true, false );
        clickImgWithAlt( "Edit" );
        addEditGroupBuildDefinition( projectGroupName, buildDefinitionPomName, buildDefinitionGoals,
                                     buildDefinitionArguments, buildDefinitionDescription, false, true, false );
    }

    @Test( dependsOnMethods = { "testEditGroupBuildDefinition" } )
    public void testDeleteGroupBuildDefinition()
        throws Exception
    {
        goToGroupBuildDefinitionPage( projectGroupName, projectGroupId, projectGroupDescription );
        // Click in Delete Image
        clickLinkWithXPath( "(//a[contains(@href,'removeGroupBuildDefinition')])//img" );
        assertDeleteBuildDefinitionPage( buildDefinitionDescription, buildDefinitionGoals );
        clickButtonWithValue( "Delete" );
        assertGroupBuildDefinitionPage( projectGroupName );
    }

    public void testAddNotDefaultProjectBuildDefinition()
        throws Exception
    {
        goToProjectInformationPage( projectGroupName, projectName );
        clickLinkWithXPath( "//input[contains(@id,'buildDefinition')]" );
        addEditGroupBuildDefinition( null, buildDefinitionPomName, buildDefinitionGoals, buildDefinitionArguments,
                                     buildDefinitionDescription, false, false, false );
    }

    public void testAddNotDefaultProjectBuildDefinitionWithLongMavenGoal()
        throws Exception
    {
        goToProjectInformationPage( projectGroupName, projectName );
        clickLinkWithXPath( "//input[contains(@id,'buildDefinition')]" );
        addEditGroupBuildDefinition( null, buildDefinitionPomName, "clean org.apache.maven.plugins:maven-compile-plugin:2.4:compile", buildDefinitionArguments,
                                     buildDefinitionDescription, false, false, false );
    }

    @Test( dependsOnMethods = { "testAddNotDefaultProjectBuildDefinition" } )
    public void testDeleteProjectBuildDefinition()
        throws Exception
    {
        goToProjectInformationPage( projectGroupName, projectName );
        // Click in Delete Image
        clickLinkWithXPath( "(//a[contains(@href,'removeProjectBuildDefinition')])//img" );
        assertDeleteBuildDefinitionPage( buildDefinitionDescription, buildDefinitionGoals );
        clickButtonWithValue( "Delete" );
        assertProjectInformationPage();
    }
}
