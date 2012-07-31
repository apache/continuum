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
public abstract class AbstractConfigurationTest
    extends AbstractAdminTest
{
    public void goToConfigurationPage()
    {
        clickLinkWithText( "Configuration" );
        assertEditConfigurationPage();
    }

    public void assertEditedConfigurationPage( String working, String buildOutput, String releaseOutput,
                                               String deploymentRepository, String baseUrl, String numberBuildParallel )
    {
        assertPage( "Continuum - Configuration" );
        assertTextPresent( "General Configuration " );
        assertTextPresent( "Working Directory" );
        assertElementNotPresent( "workingDirectory" );
        assertTextPresent( working );
        assertTextPresent( "Build Output Directory" );
        assertElementNotPresent( "buildOutputDirectory" );
        assertTextPresent( buildOutput );
        assertTextPresent( "Release Output Directory" );
        assertElementNotPresent( "releaseOutputDirectory" );
        assertTextPresent( releaseOutput );
        assertTextPresent( "Deployment Repository Directory" );
        assertElementNotPresent( "deploymentRepositoryDirectory" );
        assertTextPresent( deploymentRepository );
        assertTextPresent( "Base URL" );
        assertElementNotPresent( "baseUrl" );
        assertTextPresent( baseUrl );
        assertTextPresent( "Number of Allowed Builds in Parallel" );
        assertElementNotPresent( "numberOfAllowedBuildsinParallel" );
        assertTextPresent( numberBuildParallel );
        assertTextPresent( "Enable Distributed Builds" );
        assertElementNotPresent( "distributedBuildEnabled" );
        assertButtonWithValuePresent( "Edit" );

    }

    public void submitConfiguration( String working, String buildOutput, String releaseOutput,
                                     String deploymentRepository, String baseUrl, String numberBuildParallel,
                                     boolean distributed, boolean success )
    {
        setFieldValue( "workingDirectory", working );
        setFieldValue( "buildOutputDirectory", buildOutput );
        setFieldValue( "releaseOutputDirectory", releaseOutput );
        setFieldValue( "deploymentRepositoryDirectory", deploymentRepository );
        setFieldValue( "baseUrl", baseUrl );
        setFieldValue( "numberOfAllowedBuildsinParallel", numberBuildParallel );
        if ( distributed )
        {
            checkField( "distributedBuildEnabled" );
        }
        else
        {
            uncheckField( "distributedBuildEnabled" );
        }
        submit();
        if ( success )
        {
            assertEditedConfigurationPage( working, buildOutput, releaseOutput, deploymentRepository, baseUrl,
                                           numberBuildParallel );
        }
        else
        {
            assertEditConfigurationPage();
        }
    }

    protected void goToAppearancePage()
    {
        clickLinkWithText( "Appearance" );
        assertAppearancePage();
    }

    protected void assertAppearancePage()
    {
        assertPage( "Configure Appearance" );
        assertTextPresent( "Company Details" );
        assertTextPresent( "Footer Content" );
    }
}
