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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test( groups = { "buildResult" } )
public class BuildResultTest
    extends AbstractAdminTest
{
    private String projectGroupName;

    private String projectGroupId;

    private String projectGroupDescription;

    private String projectName;

    @BeforeClass
    public void createProject()
    {
        projectGroupName = getProperty( "BUILD_RESULT_PROJECT_GROUP_NAME" );
        projectGroupId = getProperty( "BUILD_RESULT_PROJECT_GROUP_ID" );
        projectGroupDescription = getProperty( "BUILD_RESULT_PROJECT_GROUP_DESCRIPTION" );

        projectName = getProperty( "MAVEN2_POM_PROJECT_NAME" );
        String projectPomUrl = getProperty( "MAVEN2_POM_URL" );
        String pomUsername = getProperty( "MAVEN2_POM_USERNAME" );
        String pomPassword = getProperty( "MAVEN2_POM_PASSWORD" );

        loginAsAdmin();
        addProjectGroup( projectGroupName, projectGroupId, projectGroupDescription, true, false );
        clickLinkWithText( projectGroupName );
        if ( !isLinkPresent( projectName ) )
        {
            addMavenTwoProject( projectPomUrl, pomUsername, pomPassword, projectGroupName, true );
        }
    }

    public void testDeleteBuildResult()
    {
        buildProjectGroup( projectGroupName, projectGroupId, projectGroupDescription, projectName, true );

        // go to build results page
        clickAndWait( "css=img[title='Build History']" );

        assertPage( "Continuum - Build results" );
        assertElementPresent( "css=tbody.tableBody tr" );

        assertElementPresent( "selectedBuildResults_selector" );
        getSelenium().click( "selectedBuildResults_selector" );
        clickButtonWithValue( "Delete" );

        assertPage( "Continuum - Delete Build Results" );

        clickButtonWithValue( "Delete" );

        assertPage( "Continuum - Build results" );
        assertElementNotPresent( "css=tbody.tableBody tr" );
    }

}
