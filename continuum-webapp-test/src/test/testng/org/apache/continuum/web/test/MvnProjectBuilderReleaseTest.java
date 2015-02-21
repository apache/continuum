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

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * This tests that a project that requires variable interpolation in the pom can successfully be prepared for release.
 * This was originally reported as CONTINUUM-2094.
 */
@Test( groups = { "release" } )
public class MvnProjectBuilderReleaseTest
    extends AbstractReleaseTest
{
    private String projectGroupName;

    private String projectGroupId;

    private String tagBase;

    private String tag;

    private String releaseVersion;

    private String developmentVersion;

    @BeforeClass
    public void createAndBuildProject()
    {
        projectGroupName = getProperty( "RELEASE_PROJECT_GROUP_NAME" );
        projectGroupId = getProperty( "RELEASE_PROJECT_GROUP_ID" );
        String description = "Release test projects";

        loginAsAdmin();

        String pomUrl = getProperty( "MAVEN2_MODULES_WITH_VARS_PROJECT_URL" );
        String pomUsername = getProperty( "MAVEN2_POM_USERNAME" );
        String pomPassword = getProperty( "MAVEN2_POM_PASSWORD" );
        String projectName = getProperty( "MAVEN2_MODULES_WITH_VARS_PROJECT_NAME" );

        addProjectGroup( projectGroupName, projectGroupId, description, true, false );
        clickLinkWithText( projectGroupName );

        if ( !isLinkPresent( projectName ) )
        {
            addMavenTwoProject( pomUrl, pomUsername, pomPassword, projectGroupName, true );

            buildProjectGroup( projectGroupName, projectGroupId, "", projectName, true );
        }
    }

    @BeforeMethod
    public void setUp()
        throws IOException
    {
        tagBase = getProperty( "RELEASE_PROJECT_TAGBASE" );
        tag = getProperty( "MAVEN2_MODULES_WITH_VARS_TAG" );
        releaseVersion = getProperty( "MAVEN2_MODULES_WITH_VARS_VERSION" );
        developmentVersion = getProperty( "MAVEN2_MODULES_WITH_VARS_DEVELOPMENT_VERSION" );
    }

    public void testReleasePrepareProjectWithVersionExpression()
        throws Exception
    {
        showProjectGroup( projectGroupName, projectGroupId, projectGroupId );

        clickButtonWithValue( RELEASE_BUTTON_TEXT );
        assertReleaseChoicePage();
        releasePrepareProject( "", "", tagBase, tag, releaseVersion, developmentVersion, "" );

        assertReleasePhaseSuccess();
    }
}
