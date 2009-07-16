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

import org.apache.continuum.web.test.parent.AbstractContinuumTest;
import org.testng.annotations.Test;

/**
 * Based on AddAntProjectTestCase of Emmanuel Venisse.
 *
 * @author José Morales Martínez
 * @version $Id$
 */
@Test( groups = { "antProject" } )
public class AntProjectTest
    extends AbstractContinuumTest
{
    @Test( dependsOnMethods = { "testAddProjectGroup" } )
    public void testAddAntProject()
        throws Exception
    {
        loginAsAdminIfNeeded();
        String ANT_NAME = getProperty( "ANT_NAME" );
        String ANT_DESCRIPTION = getProperty( "ANT_DESCRIPTION" );
        String ANT_VERSION = getProperty( "ANT_VERSION" );
        String ANT_TAG = getProperty( "ANT_TAG" );
        String ANT_SCM_URL = getProperty( "ANT_SCM_URL" );
        String ANT_SCM_USERNAME = getProperty( "ANT_SCM_USERNAME" );
        String ANT_SCM_PASSWORD = getProperty( "ANT_SCM_PASSWORD" );
        String TEST_PROJ_GRP_NAME = getTestGroupName();
        String TEST_PROJ_GRP_ID = getTestGroupId();
        String TEST_PROJ_GRP_DESCRIPTION = getProperty( "TEST_PROJ_GRP_DESCRIPTION" );
        goToAddAntProjectPage();
        addProject( ANT_NAME, ANT_DESCRIPTION, ANT_VERSION, ANT_SCM_URL, ANT_SCM_USERNAME, ANT_SCM_PASSWORD, ANT_TAG,
                    false, TEST_PROJ_GRP_NAME, null, true );
        assertProjectGroupSummaryPage( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
    }

    public void testSubmitEmptyForm()
    {
        loginAsAdminIfNeeded();
        goToAddAntProjectPage();
        submit();
        assertAddProjectPage( "ant" );
        assertTextPresent( "Name is required and cannot contain null or spaces only" );
        assertTextPresent( "Version is required and cannot contain null or spaces only" );
        assertTextPresent( "SCM Url is required and cannot contain null or spaces only" );
    }

    @Test( dependsOnMethods = { "testAddAntProject" } )
    public void testAddDupliedAntProject()
        throws Exception
    {
        loginAsAdminIfNeeded();
        String ANT_NAME = getProperty( "ANT_NAME" );
        String ANT_DESCRIPTION = getProperty( "ANT_DESCRIPTION" );
        String ANT_VERSION = getProperty( "ANT_VERSION" );
        String ANT_TAG = getProperty( "ANT_TAG" );
        String ANT_SCM_URL = getProperty( "ANT_SCM_URL" );
        String ANT_SCM_USERNAME = getProperty( "ANT_SCM_USERNAME" );
        String ANT_SCM_PASSWORD = getProperty( "ANT_SCM_PASSWORD" );
        goToAddAntProjectPage();
        addProject( ANT_NAME, ANT_DESCRIPTION, ANT_VERSION, ANT_SCM_URL, ANT_SCM_USERNAME, ANT_SCM_PASSWORD, ANT_TAG,
                    false, null, null, false );
        assertTextPresent( "Project name already exist" );
    }
}
