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
import org.testng.Assert;
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
    public void createProjects()
    {
        projectGroupName = getProperty( "MAVEN2_BUILD_RESULT_PROJECT_GROUP_NAME" );
        projectGroupId = getProperty( "MAVEN2_BUILD_RESULT_PROJECT_GROUP_ID" );
        projectGroupDescription = getProperty( "MAVEN2_BUILD_RESULT_PROJECT_GROUP_DESCRIPTION" );

        projectName = getProperty( "MAVEN2_TAIL_PROJECT_NAME" );
        String projectPomUrl = getProperty( "MAVEN2_TAIL_POM_URL" );
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

    private void assertText( String locator, String pattern )
        throws InterruptedException
    {
        for ( int second = 0; ; second++ )
        {
            if ( second >= Integer.parseInt( maxWaitTimeInMs ) )
                Assert.fail(
                    String.format( "timed out waiting for %s text to match '%s'", locator, pattern ) );
            try
            {
                if ( getSelenium().getText( locator ).matches( pattern ) )
                    break;
            }
            catch ( Exception e )
            {
            }
            Thread.sleep( 1000 );
        }
    }

    /**
     * A rough test that verifies the ability to tail build output.
     *
     * @throws InterruptedException
     */
    public void testTailBuildOutput()
        throws InterruptedException
    {
        showProjectGroup( projectGroupName, projectGroupId, projectGroupDescription );
        clickAndWait( "css=img[alt=\"Build Now\"]" );

        // Wait on group page until updating icon (normally immediately after clicking build)
        waitForElementPresent( "xpath=(//img[@alt='Updating'])[2]" );
        clickAndWait( "link=" + projectName );
        clickAndWait( "link=Builds" );

        // Matches and clicks first result
        clickAndWait( "css=img[alt=\"Building\"]" );

        assertPage( "Continuum - Build result" );
        assertElementPresent( "css=img[alt=\"Building\"]" );    // confirm build is still in progress
        assertElementPresent( "id=outputArea" );                // confirm text area is in page
        assertText( "id=outputArea", ".*Sleeping[.][.][.].*" ); // wait for conditions that should stream in
        assertText( "id=outputArea", ".*Woke Up[.].*" );
        assertText( "id=outputArea", ".*BUILD SUCCESS.*" );

        waitForElementPresent( "css=img[alt=\"Success\"]" );    // Verifies page is reloaded on completion
        assertElementPresent( "link=Surefire Report" );         // Check that the surefire link is present too
    }

}
