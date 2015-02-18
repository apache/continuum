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
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test( groups = { "antProject" } )
public class IvyJUnitProjectTest
    extends AbstractAdminTest
{
    private String projectName;

    private String projectDescription;

    private String projectVersion;

    private String projectTag;

    private String scmUrl;

    private String scmUsername;

    private String scmPassword;

    private String projectGroupName;

    private String projectGroupId;

    private String projectGroupDescription;

    @BeforeMethod
    protected void setUp()
        throws Exception
    {
        projectName = getProperty( "IVYJU_NAME" );
        projectDescription = getProperty( "IVYJU_DESCRIPTION" );
        projectVersion = getProperty( "IVYJU_VERSION" );
        projectTag = getProperty( "IVYJU_TAG" );
        scmUrl = getProperty( "IVYJU_SCM_URL" );
        scmUsername = getProperty( "IVYJU_SCM_USERNAME" );
        scmPassword = getProperty( "IVYJU_SCM_PASSWORD" );

        projectGroupName = getProperty( "ANT_PROJECT_GROUP_NAME" );
        projectGroupId = getProperty( "ANT_PROJECT_GROUP_ID" );
        projectGroupDescription = getProperty( "ANT_PROJECT_GROUP_DESCRIPTION" );

        // create project group, if it doesn't exist
        addProjectGroup( projectGroupName, projectGroupId, projectGroupDescription, true, false );
    }

    @AfterMethod
    public void tearDown()
        throws Throwable
    {
        removeProjectGroup( projectGroupName, false );
    }

    public void testJUnitReportsArchived()
        throws Exception
    {
        goToAddAntProjectPage();
        addProject( projectName, projectDescription, projectVersion, scmUrl, scmUsername, scmPassword, projectTag,
                    projectGroupName, true, "ant" );
        assertProjectGroupSummaryPage( projectGroupName, projectGroupId, projectGroupDescription );
        clickAndWait( "css=img[alt=\"Build Now\"]" );
        clickAndWait( "link=" + projectName );
        clickAndWait( "link=Builds" );
        clickAndWait( "css=img[alt=\"Building\"]" );
        waitForElementPresent( "css=img[alt=\"Failed\"]" );
        clickAndWait( "link=Surefire Report" );
        assertCellValueFromTable( "2", "id=ec_table", 1, 0 );
        assertCellValueFromTable( "1", "id=ec_table", 1, 2 );
        assertCellValueFromTable( "50.0", "id=ec_table", 1, 3 );
    }

}
