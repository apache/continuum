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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * @author José Morales Martínez
 * @version $Id$
 */
@Test( groups = {"installation"} )
public class InstallationTest
    extends AbstractInstallationTest
{
    private static final String JDK_VAR_NAME = "JDK";

    private static final String MAVEN_VAR_NAME = "Maven";

    private String jdkName;

    private String jdkPath;

    private String varName;

    private String varVariableName;

    private String varPath;

    private String mavenName;

    private String mavenPath;

    private String varNameNoBE;

    @BeforeMethod
    protected void setUp()
        throws Exception
    {
        jdkName = getProperty( "INSTALL_TOOL_JDK_NAME" );
        jdkPath = getProperty( "INSTALL_TOOL_JDK_PATH" );
        varName = getProperty( "INSTALL_VAR_NAME" );
        varVariableName = getProperty( "INSTALL_VAR_VARIABLE_NAME" );
        varPath = getProperty( "INSTALL_VAR_PATH" );
        mavenName = getProperty( "INSTALL_TOOL_MAVEN_NAME" );
        mavenPath = getProperty( "INSTALL_TOOL_MAVEN_PATH" );
        varNameNoBE = "var_without_build_environment";
    }

    @AfterClass
    public void cleanup()
    {
        for ( String installation : Arrays.asList( jdkName, varName, mavenName, varNameNoBE ) )
        {
            removeInstallation( installation, false );
            removeBuildEnvironment( installation, false );
        }
    }

    public void testAddJdkToolWithoutBuildEnvironment()
    {
        goToAddInstallationTool();
        addInstallation( jdkName, JDK_VAR_NAME, jdkPath, false, true, true );
    }

    public void testAddJdkToolWithoutBuildEnvironmentWithInvalidValues()
    {
        String jdkName = "!@#$<>?etc";
        String jdkPath = "!@#$<>?etc";
        goToAddInstallationTool();
        addInstallation( jdkName, JDK_VAR_NAME, jdkPath, false, true, false );
        assertTextPresent( "Installation name contains invalid characters." );
        assertTextPresent( "Installation value contains invalid characters." );
    }

    public void testAddMavenToolWithBuildEnvironment()
    {
        goToAddInstallationTool();
        addInstallation( mavenName, MAVEN_VAR_NAME, mavenPath, true, true, true );
        // TODO: Validate build environment

    }

    public void testAddInstallationVariableWithBuildEnvironment()
    {
        goToAddInstallationVariable();
        addInstallation( varName, varVariableName, varPath, true, false, true );
        // TODO: Validate build environment
    }

    public void testAddInstallationVariableWithoutBuildEnvironment()
    {
        String varVariableName = "var_name";
        String varPath = "path";
        goToAddInstallationVariable();
        addInstallation( varNameNoBE, varVariableName, varPath, false, false, true );
    }

    public void testAddInstallationVariableWithoutBuildEnvironmentWithInvalidValues()
    {
        String varName = "!@#$<>?etc";
        String varVariableName = "!@#$<>?etc";
        String varPath = "!@#$<>?etc";
        goToAddInstallationVariable();
        addInstallation( varName, varVariableName, varPath, false, false, false );
        assertTextPresent( "Installation name contains invalid characters." );
        assertTextPresent( "Environment variable name contains invalid characters." );
        assertTextPresent( "Installation value contains invalid characters." );
    }

    public void testAddInvalidInstallationTool()
    {
        goToAddInstallationTool();
        addInstallation( "", JDK_VAR_NAME, "", false, true, false );
        assertTextPresent( "You must define a name" );
        assertTextPresent( "You must define a value" );
    }

    public void testAddInvalidPathInstallationTool()
    {
        goToAddInstallationTool();
        addInstallation( "name", JDK_VAR_NAME, "invalid_path", false, true, false );
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
        goToAddInstallationTool();
        addInstallation( jdkName, JDK_VAR_NAME, jdkPath, false, true, false );
        assertTextPresent( "Installation name already exists" );

    }

    @Test( dependsOnMethods = {"testAddInstallationVariableWithBuildEnvironment"} )
    public void testAddDuplicatedInstallationVariable()
    {
        goToAddInstallationVariable();
        addInstallation( varName, varVariableName, varPath, false, false, false );
        assertTextPresent( "Installation name already exists" );
    }

    @Test( dependsOnMethods = {"testAddJdkToolWithoutBuildEnvironment"} )
    public void testEditInstallationTool()
    {
        String newName = "new_name";
        goToEditInstallation( jdkName, JDK_VAR_NAME, jdkPath, true );
        editInstallation( newName, JDK_VAR_NAME, jdkPath, true, true );
        goToEditInstallation( newName, JDK_VAR_NAME, jdkPath, true );
        editInstallation( jdkName, JDK_VAR_NAME, jdkPath, true, true );
    }

    @Test( dependsOnMethods = {"testAddInstallationVariableWithBuildEnvironment"} )
    public void testEditInstallationVariable()
    {
        String newName = "new_name";
        String newVarName = "new_var_name";
        String newPath = "new_path";
        goToEditInstallation( varName, varVariableName, varPath, false );
        editInstallation( newName, newVarName, newPath, false, true );
        goToEditInstallation( newName, newVarName, newPath, false );
        editInstallation( varName, varVariableName, varPath, false, true );
    }

    @Test( dependsOnMethods = {"testEditInstallationTool", "testAddDuplicatedInstallationTool"} )
    public void testDeleteInstallationTool()
    {
        removeInstallation( jdkName, true );
    }

    @Test( dependsOnMethods = {"testEditInstallationVariable", "testAddDuplicatedInstallationVariable"} )
    public void testDeleteInstallationVariable()
    {
        removeInstallation( varName, true );
    }
}
