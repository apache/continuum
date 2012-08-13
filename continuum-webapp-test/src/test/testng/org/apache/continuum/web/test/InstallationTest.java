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
import org.testng.annotations.Test;

/**
 * @author José Morales Martínez
 * @version $Id$
 */
@Test( groups = {"installation"} )
public class InstallationTest
    extends AbstractInstallationTest
{
    public void testAddJdkToolWithoutBuildEnvironment()
    {
        String INSTALL_TOOL_JDK_NAME = getProperty( "INSTALL_TOOL_JDK_NAME" );
        String INSTALL_TOOL_JDK_PATH = isWindows() ? getProperty( "INSTALL_TOOL_JDK_PATH" ) : getEscapeProperty(
            "INSTALL_TOOL_JDK_PATH" );
        goToAddInstallationTool();
        addInstallation( INSTALL_TOOL_JDK_NAME, "JDK", INSTALL_TOOL_JDK_PATH, false, true, true );
    }

    public void testAddJdkToolWithoutBuildEnvironmentWithInvalidValues()
    {
        String INSTALL_TOOL_JDK_NAME = "!@#$<>?etc";
        String INSTALL_TOOL_JDK_PATH = "!@#$<>?etc";
        goToAddInstallationTool();
        addInstallation( INSTALL_TOOL_JDK_NAME, "JDK", INSTALL_TOOL_JDK_PATH, false, true, false );
        assertTextPresent( "Installation name contains invalid characters." );
        assertTextPresent( "Installation value contains invalid characters." );
    }

    public void testAddMavenToolWithBuildEnvironment()
    {
        String INSTALL_TOOL_MAVEN_NAME = getProperty( "INSTALL_TOOL_MAVEN_NAME" );
        String INSTALL_TOOL_MAVEN_PATH = isWindows() ? getProperty( "INSTALL_TOOL_MAVEN_PATH" ) : getEscapeProperty(
            "INSTALL_TOOL_MAVEN_PATH" );
        goToAddInstallationTool();
        addInstallation( INSTALL_TOOL_MAVEN_NAME, "Maven", INSTALL_TOOL_MAVEN_PATH, true, true, true );
        // TODO: Validate build environment

    }

    public void testAddInstallationVariableWithBuildEnvironment()
    {
        String INSTALL_VAR_NAME = getProperty( "INSTALL_VAR_NAME" );
        String INSTALL_VAR_VARIABLE_NAME = getProperty( "INSTALL_VAR_VARIABLE_NAME" );
        String INSTALL_VAR_PATH = getProperty( "INSTALL_VAR_PATH" );
        goToAddInstallationVariable();
        addInstallation( INSTALL_VAR_NAME, INSTALL_VAR_VARIABLE_NAME, INSTALL_VAR_PATH, true, false, true );
        // TODO: Validate build environment
    }

    public void testAddInstallationVariableWithoutBuildEnvironment()
    {
        String INSTALL_VAR_NAME = "var_without_build_environment";
        String INSTALL_VAR_VARIABLE_NAME = "var_name";
        String INSTALL_VAR_PATH = "path";
        goToAddInstallationVariable();
        addInstallation( INSTALL_VAR_NAME, INSTALL_VAR_VARIABLE_NAME, INSTALL_VAR_PATH, false, false, true );
    }

    public void testAddInstallationVariableWithoutBuildEnvironmentWithInvalidValues()
    {
        String INSTALL_VAR_NAME = "!@#$<>?etc";
        String INSTALL_VAR_VARIABLE_NAME = "!@#$<>?etc";
        String INSTALL_VAR_PATH = "!@#$<>?etc";
        goToAddInstallationVariable();
        addInstallation( INSTALL_VAR_NAME, INSTALL_VAR_VARIABLE_NAME, INSTALL_VAR_PATH, false, false, false );
        assertTextPresent( "Installation name contains invalid characters." );
        assertTextPresent( "Environment variable name contains invalid characters." );
        assertTextPresent( "Installation value contains invalid characters." );
    }

    public void testAddInvalidInstallationTool()
    {
        goToAddInstallationTool();
        addInstallation( "", "JDK", "", false, true, false );
        assertTextPresent( "You must define a name" );
        assertTextPresent( "You must define a value" );
    }

    public void testAddInvalidPathInstallationTool()
    {
        goToAddInstallationTool();
        addInstallation( "name", "JDK", "invalid_path", false, true, false );
        assertTextPresent( "Failed to validate installation, check server log" );
    }

    public void testAddInvalidInstallationVariable()
    {
        goToAddInstallationVariable();
        addInstallation( "", "", "", false, false, false );
        assertTextPresent( "You must define a name" );
        assertTextPresent( "You must define a value" );
    }

    public void testAddInvalidVarNameInstallationVariable()
    {
        goToAddInstallationVariable();
        addInstallation( "name", "", "path", false, false, false );
        assertTextPresent( "You must define an environment variable" );
    }

    @Test( dependsOnMethods = {"testAddJdkToolWithoutBuildEnvironment"} )
    public void testAddDuplicatedInstallationTool()
    {
        String INSTALL_TOOL_JDK_NAME = getProperty( "INSTALL_TOOL_JDK_NAME" );
        String INSTALL_TOOL_JDK_PATH = isWindows() ? getProperty( "INSTALL_TOOL_JDK_PATH" ) : getEscapeProperty(
            "INSTALL_TOOL_JDK_PATH" );
        goToAddInstallationTool();
        addInstallation( INSTALL_TOOL_JDK_NAME, "JDK", INSTALL_TOOL_JDK_PATH, false, true, false );
        assertTextPresent( "Installation name already exists" );

    }

    @Test( dependsOnMethods = {"testAddInstallationVariableWithBuildEnvironment"} )
    public void testAddDuplicatedInstallationVariable()
    {
        String INSTALL_VAR_NAME = getProperty( "INSTALL_VAR_NAME" );
        String INSTALL_VAR_VARIABLE_NAME = getProperty( "INSTALL_VAR_VARIABLE_NAME" );
        String INSTALL_VAR_PATH = getProperty( "INSTALL_VAR_PATH" );
        goToAddInstallationVariable();
        addInstallation( INSTALL_VAR_NAME, INSTALL_VAR_VARIABLE_NAME, INSTALL_VAR_PATH, false, false, false );
        assertTextPresent( "Installation name already exists" );
    }

    @Test( dependsOnMethods = {"testAddJdkToolWithoutBuildEnvironment"} )
    public void testEditInstallationTool()
    {
        String INSTALL_TOOL_JDK_NAME = getProperty( "INSTALL_TOOL_JDK_NAME" );
        String INSTALL_TOOL_JDK_PATH = isWindows() ? getProperty( "INSTALL_TOOL_JDK_PATH" ) : getEscapeProperty(
            "INSTALL_TOOL_JDK_PATH" );
        String newName = "new_name";
        goToEditInstallation( INSTALL_TOOL_JDK_NAME, "JDK", INSTALL_TOOL_JDK_PATH, true );
        editInstallation( newName, "JDK", INSTALL_TOOL_JDK_PATH, true, true );
        goToEditInstallation( newName, "JDK", INSTALL_TOOL_JDK_PATH, true );
        editInstallation( INSTALL_TOOL_JDK_NAME, "JDK", INSTALL_TOOL_JDK_PATH, true, true );
    }

    @Test( dependsOnMethods = {"testAddInstallationVariableWithBuildEnvironment"} )
    public void testEditInstallationVariable()
    {
        String INSTALL_VAR_NAME = getProperty( "INSTALL_VAR_NAME" );
        String INSTALL_VAR_VARIABLE_NAME = getProperty( "INSTALL_VAR_VARIABLE_NAME" );
        String INSTALL_VAR_PATH = getProperty( "INSTALL_VAR_PATH" );
        String newName = "new_name";
        String newVarName = "new_var_name";
        String newPath = "new_path";
        goToEditInstallation( INSTALL_VAR_NAME, INSTALL_VAR_VARIABLE_NAME, INSTALL_VAR_PATH, false );
        editInstallation( newName, newVarName, newPath, false, true );
        goToEditInstallation( newName, newVarName, newPath, false );
        editInstallation( INSTALL_VAR_NAME, INSTALL_VAR_VARIABLE_NAME, INSTALL_VAR_PATH, false, true );
    }

    @Test( dependsOnMethods = {"testEditInstallationTool", "testAddDuplicatedInstallationTool"} )
    public void testDeleteInstallationTool()
    {
        String INSTALL_TOOL_JDK_NAME = getProperty( "INSTALL_TOOL_JDK_NAME" );
        removeInstallation( INSTALL_TOOL_JDK_NAME );
    }

    @Test( dependsOnMethods = {"testEditInstallationVariable", "testAddDuplicatedInstallationVariable"} )
    public void testDeleteInstallationVariable()
    {
        String INSTALL_VAR_NAME = getProperty( "INSTALL_VAR_NAME" );
        removeInstallation( INSTALL_VAR_NAME );
    }

    public static boolean isWindows()
    {
        String os = System.getProperty( "os.name" ).toLowerCase();
        //windows
        return os.contains( "win" );
    }
}
