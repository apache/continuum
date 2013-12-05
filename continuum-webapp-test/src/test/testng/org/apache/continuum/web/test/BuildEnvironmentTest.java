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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author José Morales Martínez
 * @version $Id$
 */
@Test( groups = { "buildEnvironment" } )
public class BuildEnvironmentTest
    extends AbstractAdminTest
{

    private String buildEnvName;

    @BeforeClass
    public void setUp()
    {
        buildEnvName = getProperty( "BUILD_ENV_NAME" );
    }

    public void testAddBuildEnvironment()
    {
        goToAddBuildEnvironment();
        addBuildEnvironment( buildEnvName, new String[]{ }, true );
    }

    public void testAddInvalidBuildEnvironment()
    {
        goToAddBuildEnvironment();
        addBuildEnvironment( "", new String[]{ }, false );
        assertTextPresent( "You must define a name" );
    }

    public void testAddBuildEnvironmentWithXSS()
    {
        goToAddBuildEnvironment();
        addBuildEnvironment( "<script>alert('gotcha')</script>", new String[]{ }, false );
        assertTextPresent( "Build environment name contains invalid characters." );
    }

    @Test( dependsOnMethods = { "testAddBuildEnvironment" } )
    public void testEditInvalidBuildEnvironment()
    {
        goToEditBuildEnvironment( buildEnvName );
        editBuildEnvironment( "", new String[]{ }, false );
        assertTextPresent( "You must define a name" );
    }

    @Test( dependsOnMethods = { "testAddBuildEnvironment" } )
    public void testAddDuplicatedBuildEnvironment()
    {
        goToAddBuildEnvironment();
        addBuildEnvironment( buildEnvName, new String[]{ }, false );
        assertTextPresent( "A Build Environment with the same name already exists" );
    }

    @Test( dependsOnMethods = { "testAddBuildEnvironment" } )
    public void testEditBuildEnvironment()
    {
        String newName = "new_name";
        goToEditBuildEnvironment( buildEnvName );
        editBuildEnvironment( newName, new String[]{ }, true );
        // TODO: ADD INSTALLATIONS TO ENVIROTMENT
        goToEditBuildEnvironment( newName );
        editBuildEnvironment( buildEnvName, new String[]{ }, true );
    }

    @Test( dependsOnMethods = { "testEditInvalidBuildEnvironment", "testEditBuildEnvironment",
        "testAddDuplicatedBuildEnvironment", "testEditInvalidBuildEnvironment" } )
    public void testDeleteBuildEnvironment()
    {
        removeBuildEnvironment( buildEnvName );
    }

    @Test( dependsOnMethods = { "testAddBuildEnvironment" } )
    public void testEditDuplicatedBuildEnvironmentParallelBuilds()
    {
        String newName = "NEW_BUILD_ENV";
        goToAddBuildEnvironment();
        addBuildEnvironment( newName, new String[]{ }, true );
        goToEditBuildEnvironment( newName );
        editBuildEnvironment( buildEnvName, new String[]{ }, false );
        assertTextPresent( "A Build Environment with the same name already exists" );
    }

    protected void addBuildEnvironment( String name, String[] installations, boolean success )
    {
        setFieldValue( "profile.name", name );
        submit();
        editBuildEnvironment( name, installations, success );
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

    @AfterClass
    public void tearDown()
    {
        removeBuildEnvironment( buildEnvName, false );
    }
}
