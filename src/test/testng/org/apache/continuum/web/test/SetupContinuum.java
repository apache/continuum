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

/**
 * Based on SetupContinuum of Wendy Smoak test.
 * 
 * @author José Morales Martínez
 * @version $Id$
 */
public class SetupContinuum
    extends AbstractContinuumTest
{

     public void initializeContinuumIfNeeded()
        throws Exception
    {
        getSelenium().open( baseUrl );
        String title = getSelenium().getTitle();
        if ( title.equals( "Create Admin User" ) )
        {
            assertCreateAdmin();
            String fullname = getProperty( "ADMIN_FULLNAME" );
            String username = getProperty( "ADMIN_USERNAME" );
            String mail = getProperty( "ADMIN_MAIL" );
            String password = getProperty( "ADMIN_PASSWORD" );
            submitAdminData( fullname, mail, password );
            assertAutenticatedPage( username );
            assertEditConfigurationPage();
            postAdminUserCreation();
            clickLinkWithText( "Logout" );
        }
    }

    private void postAdminUserCreation()
    {
        if ( getTitle().endsWith( "Continuum - Configuration" ) )
        {
            String workingDir = getFieldValue( "configuration_workingDirectory" );
            String buildOutputDir = getFieldValue( "configuration_buildOutputDirectory" );
            String releaseOutputDir = getFieldValue( "configuration_releaseOutputDirectory" );
            String locationDir = "target/data";
            String data = "data";
            setFieldValue( "workingDirectory", workingDir.replaceFirst( data, locationDir ) );
            setFieldValue( "buildOutputDirectory", buildOutputDir.replaceFirst( data, locationDir ) );
            setFieldValue( "releaseOutputDirectory", releaseOutputDir.replaceFirst( data, locationDir ) );
            setFieldValue( "baseUrl", baseUrl );
            submit();
        }
    }
}