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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Based on AddShellProjectTestCase of Emmanuel Venisse.
 *
 * @author José Morales Martínez
 * @version $Id$
 */
@Test( groups = {"shellProject"} )
public class ShellProjectTest
    extends AbstractAdminTest
{
    private String projectGroupId;

    private String projectGroupName;

    private String projectGroupDescription;

    private String scmPassword;

    private String scmUsername;

    private String scmUrl;

    private String scmTag;

    private String projectVersion;

    private String projectDescription;

    private String projectName;

    @BeforeMethod
    public void setUp()
    {
        projectGroupId = getProperty( "SHELL_PROJECT_GROUP_ID" );
        projectGroupName = getProperty( "SHELL_PROJECT_GROUP_NAME" );
        projectGroupDescription = getProperty( "SHELL_PROJECT_GROUP_DESCRIPTION" );
        projectName = getProperty( "SHELL_NAME" );
        projectDescription = getProperty( "SHELL_DESCRIPTION" );
        projectVersion = getProperty( "SHELL_VERSION" );
        scmUrl = getProperty( "SHELL_SCM_URL" );
        scmUsername = getProperty( "SHELL_SCM_USERNAME" );
        scmPassword = getProperty( "SHELL_SCM_PASSWORD" );
        scmTag = getProperty( "SHELL_TAG" );

        addProjectGroup( projectGroupName, projectGroupId, projectGroupDescription, true, false );
    }

    @AfterClass
    public void cleanup()
    {
        removeProjectGroup( projectGroupName );
    }

    public void testAddShellProject()
        throws Exception
    {
        goToAddShellProjectPage();
        addProject( projectName, projectDescription, projectVersion, scmUrl, scmUsername, scmPassword, scmTag,
                    projectGroupName, true, "shell" );
        assertProjectGroupSummaryPage( projectGroupName, projectGroupId, projectGroupDescription );
    }

    public void testAddShellProjectWithInvalidValues()
        throws Exception
    {
        String projectName = "!@#$<>?etc";
        String description = "![]<>'^&etc";
        String version = "<>whitespaces!#etc";
        String tag = "!<>*%etc";
        String scmUrl = "!<>*%etc";
        goToAddShellProjectPage();
        addProject( projectName, description, version, scmUrl, scmUsername, scmPassword, tag, projectGroupName, false,
                    "shell" );
        assertTextPresent( "Name contains invalid characters." );
        assertTextPresent( "Version contains invalid characters." );
        assertTextPresent( "SCM Url contains invalid characters." );
        assertTextPresent( "SCM Tag contains invalid characters." );
    }

    public void testSubmitEmptyForm()
    {
        goToAddShellProjectPage();
        submit();
        assertAddProjectPage( "shell" );
        assertTextPresent( "Name is required and cannot contain null or spaces only" );
        assertTextPresent( "Version is required and cannot contain null or spaces only" );
        assertTextPresent( "SCM Url is required and cannot contain null or spaces only" );
    }

    @Test( dependsOnMethods = {"testAddShellProject"} )
    public void testAddDuplicateShellProject()
        throws Exception
    {
        goToAddShellProjectPage();
        addProject( projectName, projectDescription, projectVersion, scmUrl, scmUsername, scmPassword, scmTag, null,
                    false, "shell" );
        assertTextPresent( "Project name already exist" );
    }

}
