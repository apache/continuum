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

import org.apache.continuum.web.test.parent.AbstractConfigurationTest;
import org.testng.annotations.Test;

/**
 * @author José Morales Martínez
 * @version $Id$
 */
@Test( groups = { "configuration" }, dependsOnMethods = { "testWithCorrectUsernamePassword" } )
public class ConfigurationTest
    extends AbstractConfigurationTest
{
    private String WORKING_DIRECTORY;

    private String BASE_URL;

    private String BUILD_OUTPUT_DIRECTORY;

    private String RELEASE_OUTPUT_DIRECTORY;

    private String DEPLOYMENT_REPOSITORY_DIRECTORY;

    private String NUMBER_ALLOWED_PARALLEL;

    public void defaultConfiguration()
    {
        goToConfigurationPage();
        WORKING_DIRECTORY = getFieldValue( "workingDirectory" );
        BASE_URL = getFieldValue( "baseUrl" );
        BUILD_OUTPUT_DIRECTORY = getFieldValue( "buildOutputDirectory" );
        RELEASE_OUTPUT_DIRECTORY = getFieldValue( "releaseOutputDirectory" );
        DEPLOYMENT_REPOSITORY_DIRECTORY = getFieldValue( "deploymentRepositoryDirectory" );
        NUMBER_ALLOWED_PARALLEL = getFieldValue( "numberOfAllowedBuildsinParallel" );
    }

    @Test( dependsOnMethods = { "defaultConfiguration" } )
    public void editConfiguration()
    {
        String newWorking = "newWorking";
        String newUrl = "http://localhost:8181";
        String newBuildOutput = "newBuildOutput";
        String newReleaseOutput = "newReleaseOutput";
        String newDeployRepository = "newDeployRepository";
        String newNumberParallel = "9";
        goToConfigurationPage();
        submitConfiguration( newWorking, newBuildOutput, newReleaseOutput, newDeployRepository, newUrl,
                             newNumberParallel, true, true );
        clickButtonWithValue( "Edit" );
        submitConfiguration( WORKING_DIRECTORY, BUILD_OUTPUT_DIRECTORY, RELEASE_OUTPUT_DIRECTORY,
                             DEPLOYMENT_REPOSITORY_DIRECTORY, BASE_URL, NUMBER_ALLOWED_PARALLEL, false, true );
    }

    public void setInvalidConfiguration()
    {
        goToConfigurationPage();
        submitConfiguration( "", "", "", "", "", "", true, false );
        assertTextPresent( "You must define a working directory" );
        assertTextPresent( "You must define a build output directory" );
        assertTextPresent( "You must define a URL" );
    }

    public void setZeroParallelBuilds()
    {
        setMaxBuildQueue( 0 );
        assertTextPresent( "Number of Allowed Builds in Parallel must be greater than zero" );
    }

    public void testSetConfigurationWithXSS()
    {
        String invalidString = "<script>alert('gotcha')</script>";
        goToConfigurationPage();
        submitConfiguration( invalidString, invalidString, invalidString, invalidString, 
                             invalidString, invalidString, true, false );
        assertTextPresent( "Working directory contains invalid characters." );
        assertTextPresent( "Build output directory contains invalid characters." );
        assertTextPresent( "Release output directory contains invalid characters." );
        assertTextPresent( "Deployment repository directory contains invalid characters." );
        assertTextPresent( "You must define a valid URL." );
    }
}
