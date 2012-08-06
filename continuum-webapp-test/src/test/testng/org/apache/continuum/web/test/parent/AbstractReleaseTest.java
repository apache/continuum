package org.apache.continuum.web.test.parent;

import org.testng.Assert;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

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

public abstract class AbstractReleaseTest
    extends AbstractAdminTest
{
    protected void releasePrepareProject( String username, String password, String tagBase, String tag,
                                          String releaseVersion, String developmentVersion, String buildEnv )
    {
        goToReleasePreparePage();
        setFieldValue( "scmUsername", username );
        setFieldValue( "scmPassword", password );
        setFieldValue( "scmTag", tag );
        setFieldValue( "scmTagBase", tagBase );
        setFieldValue( "prepareGoals", "clean" );
        selectValue( "profileId", buildEnv );
        setFieldValue( "relVersions", releaseVersion );
        setFieldValue( "devVersions", developmentVersion );
        submit();

        assertRelease();
    }

    protected void releasePerformProjectWithProvideParameters( String username, String password, String tagBase,
                                                               String tag, String scmUrl, String buildEnv )
    {
        goToReleasePerformProvideParametersPage();
        setFieldValue( "scmUrl", scmUrl );
        setFieldValue( "scmUsername", username );
        setFieldValue( "scmPassword", password );
        setFieldValue( "scmTag", tag );
        setFieldValue( "scmTagBase", tagBase );
        setFieldValue( "goals", "clean deploy" );
        selectValue( "profileId", buildEnv );
        submit();

        assertRelease();
    }

    void goToReleasePreparePage()
    {
        clickLinkWithLocator( "goal", false );
        submit();
        assertReleasePreparePage();
    }

    void goToReleasePerformProvideParametersPage()
    {
        clickLinkWithLocator( "//input[@name='goal' and @value='perform']", false );
        submit();
        assertReleasePerformProvideParametersPage();
    }

    void assertReleasePreparePage()
    {
        assertPage( "Continuum - Release Project" );
        assertTextPresent( "Prepare Project for Release" );
        assertTextPresent( "Release Prepare Parameters" );
        assertTextPresent( "SCM Username" );
        assertTextPresent( "SCM Password" );
        assertTextPresent( "SCM Tag" );
        assertTextPresent( "SCM Tag Base" );
        assertTextPresent( "SCM Comment Prefix" );
        assertTextPresent( "Preparation Goals" );
        assertTextPresent( "Arguments" );
        assertTextPresent( "Build Environment" );
        assertTextPresent( "Release Version" );
        assertTextPresent( "Next Development Version" );
        assertButtonWithValuePresent( "Submit" );
    }

    void assertReleasePerformProvideParametersPage()
    {
        assertPage( "Continuum - Perform Project Release" );
        assertTextPresent( "Perform Project Release" );
        assertTextPresent( "Release Perform Parameters" );
        assertTextPresent( "SCM Connection URL" );
        assertTextPresent( "SCM Username" );
        assertTextPresent( "SCM Password" );
        assertTextPresent( "SCM Tag" );
        assertTextPresent( "SCM Tag Base" );
        assertTextPresent( "Perform Goals" );
        assertTextPresent( "Arguments" );
        assertTextPresent( "Build Environment" );
        assertButtonWithValuePresent( "Submit" );
    }

    void assertRelease()
    {
        String doneButtonLocator = "//input[@id='releaseCleanup_0']";
        String errorTextLocator = "//h3[text()='Release Error']";
        
        // condition for release is complete; "Done" button or "Release Error" in page is present
        waitForOneOfElementsPresent( Arrays.asList( doneButtonLocator, errorTextLocator ), true );

        assertButtonWithValuePresent( "Rollback changes" );

        assertImgWithAlt( "Error" );
    }

    protected void assertPreparedReleasesFileCreated()
        throws Exception
    {
        File file = new File( "target/conf/prepared-releases.xml" );
        Assert.assertTrue( file.exists(), "prepared-releases.xml was not created" );

        FileInputStream fis = new FileInputStream( file );
        BufferedReader reader = new BufferedReader( new InputStreamReader( fis ) );

        String BUILD_AGENT_URL = getBuildAgentUrl();
        String strLine;
        StringBuilder str = new StringBuilder();
        while( ( strLine = reader.readLine() ) != null )
        {
            str.append( strLine );
        }

        Assert.assertTrue( str.toString().contains( "<buildAgentUrl>" + BUILD_AGENT_URL + "</buildAgentUrl>" ), "prepared-releases.xml was not populated" );
    }
}
