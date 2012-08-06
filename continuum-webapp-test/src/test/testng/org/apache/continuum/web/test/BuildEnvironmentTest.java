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

import org.apache.continuum.web.test.parent.AbstractBuildEnvironmentTest;
import org.testng.annotations.Test;

/**
 * @author José Morales Martínez
 * @version $Id$
 */
@Test( groups = { "buildEnvironment" } )
public class BuildEnvironmentTest
    extends AbstractBuildEnvironmentTest
{
    public void testAddBuildEnvironment()
    {
        String BUILD_ENV_NAME = getProperty( "BUILD_ENV_NAME" );
        goToAddBuildEnvironment();
        addBuildEnvironment( BUILD_ENV_NAME, new String[] {}, true );
    }

    public void testAddInvalidBuildEnvironment()
    {
        goToAddBuildEnvironment();
        addBuildEnvironment( "", new String[] {}, false );
        assertTextPresent( "You must define a name" );
    }

    public void testAddBuildEnvironmentWithXSS()
    {
        goToAddBuildEnvironment();
        addBuildEnvironment( "<script>alert('gotcha')</script>", new String[] {}, false );
        assertTextPresent( "Build environment name contains invalid characters." );
    }

    @Test( dependsOnMethods = { "testAddBuildEnvironment" } )
    public void testEditInvalidBuildEnvironment()
    {
        String BUILD_ENV_NAME = getProperty( "BUILD_ENV_NAME" );
        goToEditBuildEnvironment( BUILD_ENV_NAME );
        editBuildEnvironment( "", new String[] {}, false );
        assertTextPresent( "You must define a name" );
    }

    @Test( dependsOnMethods = { "testAddBuildEnvironment" } )
    public void testAddDuplicatedBuildEnvironment()
    {
        String BUILD_ENV_NAME = getProperty( "BUILD_ENV_NAME" );
        goToAddBuildEnvironment();
        addBuildEnvironment( BUILD_ENV_NAME, new String[] {}, false );
        assertTextPresent( "A Build Environment with the same name already exists" );
    }

    @Test( dependsOnMethods = { "testAddBuildEnvironment" } )
    public void testEditBuildEnvironment()
    {
        String BUILD_ENV_NAME = getProperty( "BUILD_ENV_NAME" );
        String newName = "new_name";
        goToEditBuildEnvironment( BUILD_ENV_NAME );
        editBuildEnvironment( newName, new String[] {}, true );
        // TODO: ADD INSTALLATIONS TO ENVIROTMENT
        goToEditBuildEnvironment( newName );
        editBuildEnvironment( BUILD_ENV_NAME, new String[] {}, true );
    }

    @Test( dependsOnMethods = { "testEditInvalidBuildEnvironment", "testEditBuildEnvironment",
        "testAddDuplicatedBuildEnvironment", "testEditInvalidBuildEnvironment" } )
    public void testDeleteBuildEnvironment()
    {
        String BUILD_ENV_NAME = getProperty( "BUILD_ENV_NAME" );
        removeBuildEnvironment( BUILD_ENV_NAME );
    }

    @Test( dependsOnMethods = { "testDeleteBuildEnvironment", "testAddBuildAgentGroupWithEmptyBuildAgent" } )
    public void testAddBuildEnvironmentWithBuildAgentGroup()
    {
        try
        {
            enableDistributedBuilds();

            String BUILD_ENV_NAME = getProperty( "BUILD_ENV_NAME" );
            String BUILD_AGENT_GROUPNAME = getProperty( "BUILD_AGENT_GROUPNAME" );
            goToAddBuildEnvironment();
            addBuildEnvironmentWithBuildAgentGroup( BUILD_ENV_NAME, new String[] {}, BUILD_AGENT_GROUPNAME );
        }
        finally
        {
            disableDistributedBuilds();
        }
    }
    
    @Test( dependsOnMethods = { "testAddBuildEnvironmentWithBuildAgentGroup" } )
    public void testEditDuplicatedBuildEnvironmentParallelBuilds()
    {
        String BUILD_ENV_NAME = getProperty( "BUILD_ENV_NAME" );
        String newName = "NEW_BUILD_ENV";
        goToAddBuildEnvironment();
        addBuildEnvironment( newName, new String[] {}, true );
        goToEditBuildEnvironment( newName );
        editBuildEnvironment( BUILD_ENV_NAME, new String[] {}, false );
        assertTextPresent( "A Build Environment with the same name already exists" );
    }
    
    @Test( dependsOnMethods = { "testEditDuplicatedBuildEnvironmentParallelBuilds" } )
    public void testEditDuplicatedBuildEnvironmentDistributedBuilds()
    {
    try
        {
            enableDistributedBuilds();

            String BUILD_ENV_NAME = getProperty( "BUILD_ENV_NAME" );
            String BUILD_AGENT_GROUPNAME = getProperty( "BUILD_AGENT_GROUPNAME" );
            String newName = "NEW_BUILD_ENV";
            goToEditBuildEnvironment( newName );
            editBuildEnvironmentWithBuildAgentGroup( BUILD_ENV_NAME, new String[] {}, BUILD_AGENT_GROUPNAME, false );
            assertTextPresent( "A Build Environment with the same name already exists" );
        }
        finally
        {
            disableDistributedBuilds();
        }
    }
}
