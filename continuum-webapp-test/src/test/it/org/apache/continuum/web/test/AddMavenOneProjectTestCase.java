package org.apache.continuum.web.test;

/*
 * Copyright 2005-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;

/**
 * Class for testing add maven one project UI page.
 */
public class AddMavenOneProjectTestCase
    extends AbstractAuthenticatedAccessTestCase
{
    public String getUsername()
    {
        return adminUsername;
    }

    public String getPassword()
    {
        return adminPassword;
    }

    public void setUp()
        throws Exception
    {
        super.setUp();
    }

    /**
     * submit the page
     *
     * @param m1PomUrl
     * @param validPom
     */
    public void submitAddMavenOneProjectPage( String m1PomUrl, boolean validPom )
    	throws Exception
    {
        addMavenOneProject( m1PomUrl, "", "", null, validPom );

        if ( validPom )
        {
            assertTextPresent( "Default Project Group" );
            //TODO: Add more tests
        }
    }

    /**
     * test with valid pom url
     */
    public void testValidPomUrl()
        throws Exception
    {
        String pomUrl = "http://svn.apache.org/repos/asf/maven/continuum/trunk/continuum-webapp-test/src/test/resources/unit/maven-one-projects/valid-project.xml";
        submitAddMavenOneProjectPage( pomUrl, true );
        //Test the group is created
        assertTextPresent( "Maven One Project" );
        //TODO: add more tests
        removeProjectGroup( "Maven One Project", "maven-one-project", "This is a sample Maven One Project." );
    }

    /**
     * test with no pom file or pom url specified
     */
    public void testNoPomSpecified()
    	throws Exception
    {
        submitAddMavenOneProjectPage( "", false );
        assertTextPresent( "Either POM URL or Upload POM is required." );
    }

    /**
     * test with missing <repository> element in the pom file
     */
    public void testMissingElementInPom()
    	throws Exception
    {
        String pomUrl = "http://svn.apache.org/repos/asf/maven/continuum/trunk/continuum-webapp-test/src/test/resources/unit/maven-one-projects/missing-repository-element-project.xml";
        submitAddMavenOneProjectPage( pomUrl, false );
        assertTextPresent( "Missing 'repository' element in the POM." );
    }


    /**
     * test with <extend> element present in pom file
     */
    public void testWithExtendElementPom()
    	throws Exception
    {
        String pomUrl = "http://svn.apache.org/repos/asf/maven/continuum/trunk/continuum-webapp-test/src/test/resources/unit/maven-one-projects/extend-element-project.xml";
        submitAddMavenOneProjectPage( pomUrl, false );
        assertTextPresent( "Cannot use a POM with an 'extend' element." );
    }

    /**
     * test with unparseable xml content for pom file
     */
    public void testUnparseableXmlContent()
    	throws Exception
    {
        String pomUrl = "http://svn.apache.org/repos/asf/maven/continuum/trunk/continuum-webapp-test/src/test/resources/unit/maven-one-projects/unparseable-content-project.xml";
        submitAddMavenOneProjectPage( pomUrl, false );
        assertTextPresent( "The XML content of the POM can not be parsed." );
    }

    /**
     * test with a malformed pom url
     */
    public void testMalformedPomUrl()
    	throws Exception
    {
        String pomUrl = "aaa";
        submitAddMavenOneProjectPage( pomUrl, false );
        assertTextPresent(
            "The specified resource cannot be accessed. Please try again later or contact your administrator." );
    }

    /**
     * test with an inaccessible pom url
     */
    public void testInaccessiblePomUrl()
    	throws Exception
    {
        String pomUrl = "http://www.google.com";
        submitAddMavenOneProjectPage( pomUrl, false );
        assertTextPresent( "POM file does not exist. Either the POM you specified or one of its modules does not exist." );
    }

    /**
     * test unallowed file protocol
     */
    public void testNotAllowedProtocol()
    	throws Exception
    {
        String pomUrl = "file:///project.xml";
        submitAddMavenOneProjectPage( pomUrl, false );
        assertTextPresent( "The specified resource isn't a file or the protocol used isn't allowed." );
    }
}
