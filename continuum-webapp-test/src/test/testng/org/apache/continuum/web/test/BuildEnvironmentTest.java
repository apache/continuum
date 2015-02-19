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

import org.apache.continuum.web.test.parent.AbstractInstallationTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author José Morales Martínez
 */
@Test( groups = { "buildEnvironment" } )
public class BuildEnvironmentTest
    extends AbstractInstallationTest
{

    public static final String INSTALLATION_NAME = "varForBuildEnv";

    private static final String INSTALLATION_BUILD_ENV = "installationBuildEnv";

    private static final String NEW_BUILD_ENV = "NEW_BUILD_ENV";

    private String buildEnvName;

    @BeforeClass(alwaysRun = true)
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
        goToEditBuildEnvironment( newName );
        editBuildEnvironment( buildEnvName, new String[]{ }, true );
    }

    @Test( dependsOnMethods = { "testAddBuildEnvironment" })
    public void testAddInstallationToBuildEnvironment()
    {
        addBuildEnvironment( INSTALLATION_BUILD_ENV, new String[]{ }, true );

        goToInstallationPage();
        if ( !isTextPresent( INSTALLATION_NAME ) )
        {
            goToAddInstallationVariable();
            addInstallation( INSTALLATION_NAME, "VAR_BUILD_ENV", "var_value", false, false, true );
        }

        goToEditBuildEnvironment( INSTALLATION_BUILD_ENV );
        editBuildEnvironment( INSTALLATION_BUILD_ENV, new String[] { INSTALLATION_NAME }, true );
    }

    @Test( dependsOnMethods = { "testAddInstallationToBuildEnvironment" })
    public void testEditInstallationOnBuildEnvironment()
    {
        goToEditBuildEnvironment( INSTALLATION_BUILD_ENV );
        clickLinkWithText( INSTALLATION_NAME );
        assertEditInstallationVariablePage();
        assert INSTALLATION_NAME.equals( getFieldValue( "installation.name" ) );
    }

    @Test( dependsOnMethods = { "testEditInstallationOnBuildEnvironment" })
    public void testRemoveInstallationOnBuildEnvironment()
    {
        goToEditBuildEnvironment( INSTALLATION_BUILD_ENV );
        assertLinkPresent( INSTALLATION_NAME );
        clickImgWithAlt( "Delete" );
        assertEditBuildEnvironmentPage( INSTALLATION_BUILD_ENV );
        assertLinkNotPresent( INSTALLATION_NAME );
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
        goToAddBuildEnvironment();
        addBuildEnvironment( NEW_BUILD_ENV, new String[]{ }, true );
        goToEditBuildEnvironment( NEW_BUILD_ENV );
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

    @AfterClass(alwaysRun = true)
    public void tearDown()
    {
        removeBuildEnvironment( buildEnvName, false );
        removeBuildEnvironment( INSTALLATION_BUILD_ENV, false );
        removeBuildEnvironment( NEW_BUILD_ENV, false );
    }
}
