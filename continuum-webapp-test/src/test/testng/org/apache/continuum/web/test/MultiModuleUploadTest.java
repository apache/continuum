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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import static org.testng.Assert.assertTrue;

/**
 * This tests functionality relating to file upload for multi-module maven2+ projects.
 */
@Test( groups = { "upload" } )
public class MultiModuleUploadTest
    extends AbstractAdminTest
{

    public static final String BAD_IMPORT_TYPE_MSG = "requires single multi-module project import type";

    private String projectGroupName;

    private String projectName;

    private String projectGroupId;

    private File projectPom;

    @BeforeClass( alwaysRun = true )
    @Parameters( { "sampleProjectsDir" } )
    public void initialize( @Optional( "src/test/example-projects" ) String projectsDir )
    {
        File dir = new File( projectsDir );
        assertTrue( dir.exists() && dir.isDirectory() );

        projectPom = new File( dir, getProperty( "MAVEN2_MODULES_WITH_VARS_PROJECT_RELPATH" ) );
        assertTrue( projectPom.exists() && projectPom.isFile() && projectPom.canRead() );

        projectGroupName = getProperty( "UPLOAD_PROJECT_GROUP_NAME" );
        projectGroupId = getProperty( "UPLOAD_PROJECT_GROUP_ID" );
        projectName = getProperty( "MAVEN2_MODULES_WITH_VARS_PROJECT_NAME" );

        loginAsAdmin();
    }

    @BeforeMethod
    public void setUp()
        throws IOException
    {
        addProjectGroup( projectGroupName, projectGroupId, "Upload test projects", true, false );
    }

    @AfterMethod
    public void tearDown()
    {
        removeProjectGroup( projectGroupName, false );
    }

    public void testMultiModuleUpload()
        throws Exception
    {
        clickLinkWithText( projectGroupName );
        assertLinkNotPresent( projectName );
        uploadMavenTwoProject( projectPom, projectGroupName, "SINGLE_MULTI_MODULE", true );
        buildProjectGroup( projectGroupName, projectGroupId, "", projectName, true );
    }

    public void testMultiModuleUploadFailsWithSeparateScm()
        throws Exception
    {
        clickLinkWithText( projectGroupName );
        assertLinkNotPresent( projectName );
        uploadMavenTwoProject( projectPom, projectGroupName, "SEPARATE_SCM", false );
        assertTextPresent( BAD_IMPORT_TYPE_MSG );
    }

    public void testMultiModuleUploadFailsWithSingleScm()
        throws Exception
    {
        clickLinkWithText( projectGroupName );
        assertLinkNotPresent( projectName );
        uploadMavenTwoProject( projectPom, projectGroupName, "SINGLE_SCM", false );
        assertTextPresent( BAD_IMPORT_TYPE_MSG );
    }
}
