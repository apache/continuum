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

import org.apache.continuum.web.test.parent.AbstractAdminTest;
import org.testng.annotations.Test;

/**
 * @author José Morales Martínez
 * @version $Id$
 */
@Test( groups = {"buildDefinition"} )
public class BuildDefinitionTest
    extends AbstractAdminTest
{
    public void testDefaultGroupBuildDefinition()
        throws Exception
    {
        String DEFAULT_PROJ_GRP_NAME = getProperty( "DEFAULT_PROJ_GRP_NAME" );
        String DEFAULT_PROJ_GRP_ID = getProperty( "DEFAULT_PROJ_GRP_ID" );
        String DEFAULT_PROJ_GRP_DESCRIPTION = getProperty( "DEFAULT_PROJ_GRP_DESCRIPTION" );

        goToGroupBuildDefinitionPage( DEFAULT_PROJ_GRP_NAME, DEFAULT_PROJ_GRP_ID, DEFAULT_PROJ_GRP_DESCRIPTION );
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

    @Test( dependsOnMethods = {"testAddProjectGroup2"} )
    public void testAddInvalidGroupBuildDefinition()
        throws Exception
    {
        String TEST2_PROJ_GRP_NAME = getProperty( "TEST2_PROJ_GRP_NAME" );
        String TEST2_PROJ_GRP_ID = getProperty( "TEST2_PROJ_GRP_ID" );
        String TEST2_PROJ_GRP_DESCRIPTION = getProperty( "TEST2_PROJ_GRP_DESCRIPTION" );
        goToGroupBuildDefinitionPage( TEST2_PROJ_GRP_NAME, TEST2_PROJ_GRP_ID, TEST2_PROJ_GRP_DESCRIPTION );
        clickButtonWithValue( "Add" );
        setFieldValue( "buildFile", "" );
        clickButtonWithValue( "Save" );
        assertTextPresent( "Build file is required and cannot contain spaces only" );
    }

    @Test( dependsOnMethods = {"testAddProjectGroup2"} )
    public void testAddGroupBuildDefinitionWithXSS()
        throws Exception
    {
        String TEST2_PROJ_GRP_NAME = getProperty( "TEST2_PROJ_GRP_NAME" );
        String TEST2_PROJ_GRP_ID = getProperty( "TEST2_PROJ_GRP_ID" );
        String TEST2_PROJ_GRP_DESCRIPTION = getProperty( "TEST2_PROJ_GRP_DESCRIPTION" );
        goToGroupBuildDefinitionPage( TEST2_PROJ_GRP_NAME, TEST2_PROJ_GRP_ID, TEST2_PROJ_GRP_DESCRIPTION );
        clickButtonWithValue( "Add" );
        setFieldValue( "buildFile", "<script>alert('xss')</script>" );
        setFieldValue( "description", "<script>alert('xss')</script>" );
        clickButtonWithValue( "Save" );
        assertTextPresent( "Build file contains invalid characters." );
    }

    @Test( dependsOnMethods = {"testAddProjectGroup2"} )
    public void testBuildFromGroupBuildDefinition()
        throws Exception
    {
        String TEST2_PROJ_GRP_NAME = getProperty( "TEST2_PROJ_GRP_NAME" );
        String TEST2_PROJ_GRP_ID = getProperty( "TEST2_PROJ_GRP_ID" );
        String TEST2_PROJ_GRP_DESCRIPTION = getProperty( "TEST2_PROJ_GRP_DESCRIPTION" );
        goToGroupBuildDefinitionPage( TEST2_PROJ_GRP_NAME, TEST2_PROJ_GRP_ID, TEST2_PROJ_GRP_DESCRIPTION );
        clickImgWithAlt( "Build" );
        assertProjectGroupSummaryPage( TEST2_PROJ_GRP_NAME, TEST2_PROJ_GRP_ID, TEST2_PROJ_GRP_DESCRIPTION );
    }

    @Test( dependsOnMethods = {"testAddProjectGroup2"} )
    public void testAddDefautltGroupBuildDefinition()
        throws Exception
    {
        String TEST2_PROJ_GRP_NAME = getProperty( "TEST2_PROJ_GRP_NAME" );
        String TEST2_PROJ_GRP_ID = getProperty( "TEST2_PROJ_GRP_ID" );
        String TEST2_PROJ_GRP_DESCRIPTION = getProperty( "TEST2_PROJ_GRP_DESCRIPTION" );
        String BUILD_POM_NAME = getProperty( "BUILD_POM_NAME" );
        String BUILD_GOALS = getProperty( "BUILD_GOALS" );
        String BUILD_ARGUMENTS = getProperty( "BUILD_ARGUMENTS" );
        String BUILD_DESCRIPTION = getProperty( "BUILD_DESCRIPTION" );
        goToGroupBuildDefinitionPage( TEST2_PROJ_GRP_NAME, TEST2_PROJ_GRP_ID, TEST2_PROJ_GRP_DESCRIPTION );
        clickButtonWithValue( "Add" );
        addEditGroupBuildDefinition( TEST2_PROJ_GRP_NAME, BUILD_POM_NAME, BUILD_GOALS, BUILD_ARGUMENTS,
                                     BUILD_DESCRIPTION, true, false, true );
    }

    @Test( dependsOnMethods = {"testAddProjectGroup2"} )
    public void testAddNotDefautltGroupBuildDefinition()
        throws Exception
    {
        String TEST2_PROJ_GRP_NAME = getProperty( "TEST2_PROJ_GRP_NAME" );
        String TEST2_PROJ_GRP_ID = getProperty( "TEST2_PROJ_GRP_ID" );
        String TEST2_PROJ_GRP_DESCRIPTION = getProperty( "TEST2_PROJ_GRP_DESCRIPTION" );
        String BUILD_POM_NAME = getProperty( "BUILD_POM_NAME" );
        String BUILD_GOALS = getProperty( "BUILD_GOALS" );
        String BUILD_ARGUMENTS = getProperty( "BUILD_ARGUMENTS" );
        String BUILD_DESCRIPTION = getProperty( "BUILD_DESCRIPTION" );
        goToGroupBuildDefinitionPage( TEST2_PROJ_GRP_NAME, TEST2_PROJ_GRP_ID, TEST2_PROJ_GRP_DESCRIPTION );
        clickButtonWithValue( "Add" );
        addEditGroupBuildDefinition( TEST2_PROJ_GRP_NAME, BUILD_POM_NAME, BUILD_GOALS, BUILD_ARGUMENTS,
                                     BUILD_DESCRIPTION, false, false, false );
    }

    @Test( dependsOnMethods = {"testAddNotDefautltGroupBuildDefinition"} )
    public void testEditGroupBuildDefinition()
        throws Exception
    {
        String TEST2_PROJ_GRP_NAME = getProperty( "TEST2_PROJ_GRP_NAME" );
        String TEST2_PROJ_GRP_ID = getProperty( "TEST2_PROJ_GRP_ID" );
        String TEST2_PROJ_GRP_DESCRIPTION = getProperty( "TEST2_PROJ_GRP_DESCRIPTION" );
        String BUILD_POM_NAME = getProperty( "BUILD_POM_NAME" );
        String BUILD_GOALS = getProperty( "BUILD_GOALS" );
        String BUILD_ARGUMENTS = getProperty( "BUILD_ARGUMENTS" );
        String BUILD_DESCRIPTION = getProperty( "BUILD_DESCRIPTION" );
        String newPom = "newpom.xml";
        String newGoals = "new goals";
        String newArguments = "new arguments";
        String newDescription = "new description";
        goToGroupBuildDefinitionPage( TEST2_PROJ_GRP_NAME, TEST2_PROJ_GRP_ID, TEST2_PROJ_GRP_DESCRIPTION );
        clickImgWithAlt( "Edit" );
        addEditGroupBuildDefinition( TEST2_PROJ_GRP_NAME, newPom, newGoals, newArguments, newDescription, false, false,
                                     false );
        clickImgWithAlt( "Edit" );
        addEditGroupBuildDefinition( TEST2_PROJ_GRP_NAME, BUILD_POM_NAME, BUILD_GOALS, BUILD_ARGUMENTS,
                                     BUILD_DESCRIPTION, true, true, false );
        clickImgWithAlt( "Edit" );
        addEditGroupBuildDefinition( TEST2_PROJ_GRP_NAME, BUILD_POM_NAME, BUILD_GOALS, BUILD_ARGUMENTS,
                                     BUILD_DESCRIPTION, false, true, false );
    }

    @Test( dependsOnMethods = {"testEditGroupBuildDefinition"} )
    public void testDeleteGroupBuildDefinition()
        throws Exception
    {
        String TEST2_PROJ_GRP_NAME = getProperty( "TEST2_PROJ_GRP_NAME" );
        String TEST2_PROJ_GRP_ID = getProperty( "TEST2_PROJ_GRP_ID" );
        String TEST2_PROJ_GRP_DESCRIPTION = getProperty( "TEST2_PROJ_GRP_DESCRIPTION" );
        String BUILD_GOALS = getProperty( "BUILD_GOALS" );
        String BUILD_DESCRIPTION = getProperty( "BUILD_DESCRIPTION" );
        goToGroupBuildDefinitionPage( TEST2_PROJ_GRP_NAME, TEST2_PROJ_GRP_ID, TEST2_PROJ_GRP_DESCRIPTION );
        // Click in Delete Image
        clickLinkWithXPath( "(//a[contains(@href,'removeGroupBuildDefinition')])//img" );
        assertDeleteBuildDefinitionPage( BUILD_DESCRIPTION, BUILD_GOALS );
        clickButtonWithValue( "Delete" );
        assertGroupBuildDefinitionPage( TEST2_PROJ_GRP_NAME );
    }

    @Test( dependsOnMethods = {"testMoveProject"} )
    public void testAddNotDefautltProjectBuildDefinition()
        throws Exception
    {
        String TEST_PROJ_GRP_NAME = getProperty( "TEST_PROJ_GRP_NAME" );
        String M2_PROJ_GRP_NAME = getProperty( "M2_PROJ_GRP_NAME" );
        String BUILD_POM_NAME = getProperty( "BUILD_POM_NAME" );
        String BUILD_GOALS = getProperty( "BUILD_GOALS" );
        String BUILD_ARGUMENTS = getProperty( "BUILD_ARGUMENTS" );
        String BUILD_DESCRIPTION = getProperty( "BUILD_DESCRIPTION" );
        goToProjectInformationPage( TEST_PROJ_GRP_NAME, M2_PROJ_GRP_NAME );
        clickLinkWithXPath( "//input[contains(@id,'buildDefinition')]" );
        addEditGroupBuildDefinition( null, BUILD_POM_NAME, BUILD_GOALS, BUILD_ARGUMENTS, BUILD_DESCRIPTION, false,
                                     false, false );
    }

    @Test( dependsOnMethods = {"testAddNotDefautltProjectBuildDefinition"} )
    public void testDeleteProjectBuildDefinition()
        throws Exception
    {
        String TEST_PROJ_GRP_NAME = getProperty( "TEST_PROJ_GRP_NAME" );
        String M2_PROJ_GRP_NAME = getProperty( "M2_PROJ_GRP_NAME" );
        String BUILD_GOALS = getProperty( "BUILD_GOALS" );
        String BUILD_DESCRIPTION = getProperty( "BUILD_DESCRIPTION" );
        goToProjectInformationPage( TEST_PROJ_GRP_NAME, M2_PROJ_GRP_NAME );
        // Click in Delete Image
        clickLinkWithXPath( "(//a[contains(@href,'removeProjectBuildDefinition')])//img" );
        assertDeleteBuildDefinitionPage( BUILD_DESCRIPTION, BUILD_GOALS );
        clickButtonWithValue( "Delete" );
        assertProjectInformationPage();
    }
}
