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

import org.apache.continuum.web.test.parent.AbstractAdminTest;
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
    extends AbstractAdminTest
{
    public void testAddAntProject()
        throws Exception
    {
        String ANT_NAME = getProperty( "ANT_NAME" );
        String ANT_DESCRIPTION = getProperty( "ANT_DESCRIPTION" );
        String ANT_VERSION = getProperty( "ANT_VERSION" );
        String ANT_TAG = getProperty( "ANT_TAG" );
        String ANT_SCM_URL = getProperty( "ANT_SCM_URL" );
        String ANT_SCM_USERNAME = getProperty( "ANT_SCM_USERNAME" );
        String ANT_SCM_PASSWORD = getProperty( "ANT_SCM_PASSWORD" );
        String TEST_PROJ_GRP_NAME = getProperty( "DEFAULT_PROJ_GRP_NAME" );
        String TEST_PROJ_GRP_ID = getProperty( "DEFAULT_PROJ_GRP_ID" );
        String TEST_PROJ_GRP_DESCRIPTION = getProperty( "DEFAULT_PROJ_GRP_DESCRIPTION" );
        goToAddAntProjectPage();
        addProject( ANT_NAME, ANT_DESCRIPTION, ANT_VERSION, ANT_SCM_URL, ANT_SCM_USERNAME, ANT_SCM_PASSWORD, ANT_TAG,
                    false, TEST_PROJ_GRP_NAME, null, true, "ant" );
        assertProjectGroupSummaryPage( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
    }
    
    public void testAddAntProjectWithInvalidValues()
        throws Exception
    {
        String ANT_NAME = "!@#$<>?etc";
        String ANT_DESCRIPTION = "![]<>'^&etc";
        String ANT_VERSION = "<>whitespaces!#etc";
        String ANT_TAG = "!<>*%etc";
        String ANT_SCM_URL = "!<>*%etc";
        String ANT_SCM_USERNAME = getProperty( "ANT_SCM_USERNAME" );
        String ANT_SCM_PASSWORD = getProperty( "ANT_SCM_PASSWORD" );
        String DEFAULT_PROJ_GRP_NAME = getProperty( "DEFAULT_PROJ_GRP_NAME" );
        goToAddAntProjectPage();
        addProject( ANT_NAME, ANT_DESCRIPTION, ANT_VERSION, ANT_SCM_URL, ANT_SCM_USERNAME,
                    ANT_SCM_PASSWORD, ANT_TAG, false, DEFAULT_PROJ_GRP_NAME, null, false, "ant" );
        assertTextPresent( "Name contains invalid characters." );
        assertTextPresent( "Version contains invalid characters." );
        assertTextPresent( "SCM Url contains invalid characters." );
        assertTextPresent( "SCM Tag contains invalid characters." );
    }

    public void testSubmitEmptyForm()
    {
        goToAddAntProjectPage();
        submit();
        assertAddProjectPage( "ant" );
        assertTextPresent( "Name is required and cannot contain null or spaces only" );
        assertTextPresent( "Version is required and cannot contain null or spaces only" );
        assertTextPresent( "SCM Url is required and cannot contain null or spaces only" );
    }

    @Test( dependsOnMethods = { "testAddAntProject" } )
    public void testAddDuplicateAntProject()
        throws Exception
    {
        String ANT_NAME = getProperty( "ANT_NAME" );
        String ANT_DESCRIPTION = getProperty( "ANT_DESCRIPTION" );
        String ANT_VERSION = getProperty( "ANT_VERSION" );
        String ANT_TAG = getProperty( "ANT_TAG" );
        String ANT_SCM_URL = getProperty( "ANT_SCM_URL" );
        String ANT_SCM_USERNAME = getProperty( "ANT_SCM_USERNAME" );
        String ANT_SCM_PASSWORD = getProperty( "ANT_SCM_PASSWORD" );
        goToAddAntProjectPage();
        addProject( ANT_NAME, ANT_DESCRIPTION, ANT_VERSION, ANT_SCM_URL, ANT_SCM_USERNAME, ANT_SCM_PASSWORD, ANT_TAG,
                    false, null, null, false, "ant" );
        assertTextPresent( "Project name already exist" );
    }
}
