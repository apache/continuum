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

import java.util.Arrays;

public abstract class AbstractReleaseTest
    extends AbstractAdminTest
{
    protected static final String RELEASE_BUTTON_TEXT = "Release";

    protected static final String PROVIDE_RELEASE_PARAMETERS_TEXT = "Provide Release Parameters";

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

        waitForRelease();
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

        waitForRelease();

        assertReleasePhaseError();
    }

    private void goToReleasePreparePage()
    {
        clickLinkWithLocator( "goal", false );
        submit();
        assertReleasePreparePage();
    }

    private void goToReleasePerformProvideParametersPage()
    {
        selectValue( "preparedReleaseId", PROVIDE_RELEASE_PARAMETERS_TEXT );
        selectPerformAndSubmit();
        assertReleasePerformProvideParametersPage();
    }

    protected void selectPerformAndSubmit()
    {
        clickLinkWithLocator( "//input[@name='goal' and @value='perform']", false );
        submit();
    }

    void assertReleasePreparePage()
    {
        assertPage( "Continuum - Release Project" );
        assertTextPresent( "Prepare Project for Release" );
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

    void assertReleaseError()
    {
        assertTextPresent( "Release Error" );
    }

    protected void assertReleasePhaseError()
    {
        assertButtonWithValuePresent( "Rollback changes" );
        assertImgWithAlt( "Error" );
    }

    protected void assertReleasePhaseSuccess()
    {
        assertButtonWithValuePresent( "Rollback changes" );
        assertElementNotPresent( "//img[@alt='Error']" );
    }

    protected void waitForRelease()
    {
        String doneButtonLocator = "//input[@id='releaseCleanup_0']";
        String errorTextLocator = "//h3[text()='Release Error']";

        // condition for release is complete; "Done" button or "Release Error" in page is present
        waitForOneOfElementsPresent( Arrays.asList( doneButtonLocator, errorTextLocator ), true );
    }
}
