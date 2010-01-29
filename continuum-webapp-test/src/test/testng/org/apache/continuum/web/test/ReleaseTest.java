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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.apache.continuum.web.test.parent.AbstractReleaseTest;
import org.testng.annotations.Test;
import org.testng.Assert;

@Test( groups = { "release" }, dependsOnMethods = { "testWithCorrectUsernamePassword" } )
public class ReleaseTest
    extends AbstractReleaseTest
{
    @Test( dependsOnMethods = { "testProjectGroupAllBuildSuccessWithDistributedBuilds" } )
    public void testReleasePrepareProjectWithInvalidUsernamePasswordInDistributedBuilds()
        throws Exception
    {
        String M2_PROJ_GRP_NAME = getProperty( "M2_DELETE_PROJ_GRP_NAME" );
        String M2_PROJ_GRP_ID = getProperty( "M2_DELETE_PROJ_GRP_ID" );

        String M2_PROJ_USERNAME = "invalid";
        String M2_PROJ_PASSWORD = "invalid";
        String M2_PROJ_TAGBASE = getProperty( "M2_DELETE_PROJ_TAGBASE" );
        String M2_PROJ_TAG = getProperty( "M2_DELETE_PROJ_TAG" );
        String M2_PROJ_RELEASE_VERSION = getProperty( "M2_DELETE_PROJ_RELEASE_VERSION" );
        String M2_PROJ_DEVELOPMENT_VERSION = getProperty( "M2_DELETE_PROJ_DEVELOPMENT_VERSION" );

        init();
        enableDistributedBuilds();
        showProjectGroup( M2_PROJ_GRP_NAME, M2_PROJ_GRP_ID, "" );
        clickButtonWithValue( "Release" );
        assertReleaseSuccess();
        releasePrepareProject( M2_PROJ_USERNAME, M2_PROJ_PASSWORD, M2_PROJ_TAGBASE, M2_PROJ_TAG,
                               M2_PROJ_RELEASE_VERSION, M2_PROJ_DEVELOPMENT_VERSION, false );

        File file = new File( "target/conf/prepared-releases.xml" );
        Assert.assertTrue( file.exists(), "prepared-releases.xml was not created" );

        FileInputStream fis = new FileInputStream( file );
        BufferedReader reader = new BufferedReader( new InputStreamReader( fis ) );

        String BUILD_AGENT_URL = getProperty( "BUILD_AGENT_NAME2" );
        String strLine;
        StringBuffer str = new StringBuffer();
        while( ( strLine = reader.readLine() ) != null )
        {
            str.append( strLine );
        }

        Assert.assertTrue( str.toString().contains( "<buildAgentUrl>" + BUILD_AGENT_URL + "</buildAgentUrl>" ), "prepared-releases.xml was not populated" );
        disableDistributedBuilds();
    }

    private void init()
    {
        File file = new File( "target/conf/prepared-releases.xml" );

        if ( file.exists() )
        {
            file.delete();
        }
    }
}
