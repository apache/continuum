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

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class AntTest
    extends AbstractAuthenticatedAdminAccessTestCase
{
/*    public void testAddAntProject()
        throws Exception
    {
        goToAddAntPage();
        setFieldValue( "projectName", "Foo" );
        setFieldValue( "projectVersion", "1.0-SNAPSHOT" );
        setFieldValue( "projectScmUrl",
                       "https://svn.apache.org/repos/asf/maven/continuum/trunk/continuum-test-projects/ant/" );
        clickButtonWithValue( "Add" );

        assertProjectGroupsSummaryPage();
        assertTextPresent( "Default Project Group" );
        clickLinkWithText( "Default Project Group" );
        assertTextPresent( "Foo" );

        //TODO Add more tests (values in Default Project Group, values in project view, notifiers, build defintions, delete, build,...)
    }
*/
    public void testSubmitEmptyForm()
    {
        goToAddAntPage();
        clickButtonWithValue( "Add" );
        assertTextPresent( "Name is required and cannot contain null or spaces only" );
        assertTextPresent( "Version is required and cannot contain null or spaces only" );
        assertTextPresent( "SCM Url is required and cannot contain null or spaces only" );
	assertAddAntProjectPage();
    }

/*    public void testSubmitEmptyProjectName()
    {
        goToAddAntPage();
        clickButtonWithValue( "Add", false );
        assertAddAntProjectPage();
        assertTextPresent( "Name is required" );
    }

    public void testSubmitEmptyVersion()
    {
        goToAddAntPage();
        clickButtonWithValue( "Add", false );
        assertAddAntProjectPage();
        assertTextPresent( "Version is required" );
    }

    public void testSubmitEmptyScmUrl()
    {
        goToAddAntPage();
        clickButtonWithValue( "Add", false );
        assertAddAntProjectPage();
        assertTextPresent( "SCM Url is required" );
    }

    public void testSubmitDoubleErrorMessages()
    {
        goToAddAntPage();
        clickButtonWithValue( "Add", false );
        clickButtonWithValue( "Add", false );
        assertAddAntProjectPage();
        if ( "Name is required".equals( getSelenium().getText( "//td/span" ) ) )
        {
            assertFalse( "Double Error Messages", "Name is required".equals( getSelenium().getText( "//tr[2]/td/span" ) ) );
        }
        if ( "Version is required".equals( getSelenium().getText( "//tr[4]/td/span" ) ) )
        {
            assertFalse( "Double Error Messages", "Version is required".equals( getSelenium().getText( "//tr[5]/td/span" ) ) );
        }
        if ( "SCM Url is required".equals( getSelenium().getText( "//tr[7]/td/span" ) ) )
        {
            assertFalse( "Double Error Messages", "SCM Url is required".equals( getSelenium().getText( "//tr[8]/td/span" ) ) );
        }
    }
*/
    public void testCancelButton()
    {
        goToAboutPage();
        goToAddAntPage();
        clickButtonWithValue( "Cancel" );
	assertTextPresent( "Project Groups" );
        //assertAboutPage();
    }

    private void goToAddAntPage()
    {
        clickLinkWithText( "Ant Project" );
        assertAddAntProjectPage();
    }

}
