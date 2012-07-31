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

import org.apache.continuum.web.test.parent.AbstractBuildDefinitionTemplateTest;
import org.testng.annotations.Test;

/**
 * @author José Morales Martínez
 * @version $Id$
 */
@Test( groups = { "buildDefinitionTemplate" } )
public class BuildDefinitionTemplateTest
    extends AbstractBuildDefinitionTemplateTest
{
    public void testAddTemplate()
        throws Exception
    {
        String TEMPLATE_NAME = getProperty( "TEMPLATE_NAME" );
        goToAddTemplate();
        addEditTemplate( TEMPLATE_NAME, new String[] { "Default Maven Build Definition",
            "Default Maven 1 Build Definition" }, new String[] {}, true );
    }

    public void testAddInvalidTemplate()
        throws Exception
    {
        goToAddTemplate();
        addEditTemplate( "", new String[] {}, new String[] {}, false );
        assertTextPresent( "Name is required" );
    }

    public void testAddTemplateWithXSS()
        throws Exception
    {
        goToAddTemplate();
        addEditTemplate( "Name <script>alert('gotcha')</script>", new String[] {}, new String[] {}, false );
        assertTextPresent( "Name contains invalid characters" );
    }

    @Test( dependsOnMethods = { "testAddTemplate" } )
    public void testEditTemplate()
        throws Exception
    {
        String TEMPLATE_NAME = getProperty( "TEMPLATE_NAME" );
        String newName = "new_name";
        goToEditTemplate( TEMPLATE_NAME, new String[] { "Default Maven Build Definition",
            "Default Maven 1 Build Definition" } );
        addEditTemplate( newName, new String[] { "Default Shell Build Definition" },
                         new String[] { "Default Maven Build Definition" }, true );
        goToEditTemplate( newName,
                          new String[] { "Default Maven 1 Build Definition", "Default Shell Build Definition" } );
        addEditTemplate( TEMPLATE_NAME, new String[] { "Default Maven Build Definition" },
                         new String[] { "Default Shell Build Definition" }, true );
    }

    @Test( dependsOnMethods = { "testEditTemplate" } )
    public void testDeleteTemplate()
    {
        String TEMPLATE_NAME = getProperty( "TEMPLATE_NAME" );
        removeTemplate( TEMPLATE_NAME );
    }

    public void testAddBuildDefinitionTemplate()
        throws Exception
    {
        String TEMPLATE_BUILD_POM_NAME = getProperty( "TEMPLATE_BUILD_POM_NAME" );
        String TEMPLATE_BUILD_GOALS = getProperty( "TEMPLATE_BUILD_GOALS" );
        String TEMPLATE_BUILD_ARGUMENTS = getProperty( "TEMPLATE_BUILD_ARGUMENTS" );
        String TEMPLATE_BUILD_DESCRIPTION = getProperty( "TEMPLATE_BUILD_DESCRIPTION" );
        goToAddBuildDefinitionTemplate();
        addEditBuildDefinitionTemplate( TEMPLATE_BUILD_POM_NAME, TEMPLATE_BUILD_GOALS, TEMPLATE_BUILD_ARGUMENTS,
                                        TEMPLATE_BUILD_DESCRIPTION, true, true, true, true );
    }

    public void testAddInvalidBuildDefinitionTemplate()
        throws Exception
    {
        goToAddBuildDefinitionTemplate();
        addEditBuildDefinitionTemplate( "", "", "", "", true, true, true, false );
        assertTextPresent( "BuildFile is required" );
        assertTextPresent( "Description is required" );
    }

    public void testAddBuildDefinitionTemplateWithXSS()
        throws Exception
    {
        String invalidString = "<script>alert('gotcha')</script>";
        goToAddBuildDefinitionTemplate();
        addEditBuildDefinitionTemplate( invalidString, invalidString, invalidString, invalidString, true, true, true, false );
        assertTextPresent( "BuildFile contains invalid characters" );
        assertTextPresent( "Goals contain invalid characters" );
        assertTextPresent( "Arguments contain invalid characters" );
    }

    @Test( dependsOnMethods = { "testAddBuildDefinitionTemplate" } )
    public void testEditBuildDefinitionTemplate()
        throws Exception
    {
        String TEMPLATE_BUILD_POM_NAME = getProperty( "TEMPLATE_BUILD_POM_NAME" );
        String TEMPLATE_BUILD_GOALS = getProperty( "TEMPLATE_BUILD_GOALS" );
        String TEMPLATE_BUILD_ARGUMENTS = getProperty( "TEMPLATE_BUILD_ARGUMENTS" );
        String TEMPLATE_BUILD_DESCRIPTION = getProperty( "TEMPLATE_BUILD_DESCRIPTION" );
        goToEditBuildDefinitionTemplate( TEMPLATE_BUILD_DESCRIPTION );
        addEditBuildDefinitionTemplate( TEMPLATE_BUILD_POM_NAME, TEMPLATE_BUILD_GOALS, TEMPLATE_BUILD_ARGUMENTS,
                                        TEMPLATE_BUILD_DESCRIPTION, false, false, false, true );
    }

    @Test( dependsOnMethods = { "testEditBuildDefinitionTemplate" } )
    public void testDeleteBuildDefinitionTemplate()
    {
        String TEMPLATE_BUILD_DESCRIPTION = getProperty( "TEMPLATE_BUILD_DESCRIPTION" );
        removeBuildDefinitionTemplate( TEMPLATE_BUILD_DESCRIPTION );
    }
}
