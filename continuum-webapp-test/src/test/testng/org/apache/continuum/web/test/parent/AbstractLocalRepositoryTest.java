package org.apache.continuum.web.test.parent;

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

/**
 * @author José Morales Martínez
 * @version $Id$
 */
public abstract class AbstractLocalRepositoryTest
    extends AbstractAdminTest
{
    void goToLocalRepositoryPage()
    {
        clickLinkWithText( "Local Repositories" );

        assertLocalRepositoryPage();
    }

    void assertLocalRepositoryPage()
    {
        assertPage( "Continuum - Local Repositories" );
        assertTextPresent( "Local Repositories" );
        assertTextPresent( "Name" );
        assertTextPresent( "Location" );
        assertTextPresent( "Layout" );
        assertImgWithAlt( "Edit" );
        assertImgWithAlt( "Purge" );
        assertImgWithAlt( "Delete" );
        assertButtonWithValuePresent( "Add" );
    }

    void assertAddLocalRepositoryPage()
    {
        assertPage( "Continuum - Add/Edit Local Repository" );
        assertTextPresent( "Add/Edit Local Repository" );
        assertTextPresent( "Name" );
        assertElementPresent( "repository.name" );
        assertTextPresent( "Location" );
        assertElementPresent( "repository.location" );
        assertTextPresent( "Layout" );
        assertElementPresent( "repository.layout" );
        assertButtonWithValuePresent( "Save" );
        assertButtonWithValuePresent( "Cancel" );
    }

    protected void removeLocalRepository( String name )
    {
        goToLocalRepositoryPage();
        String xPath = "//preceding::td[text()='" + name + "']//following::img[@alt='Delete']";
        clickLinkWithXPath( xPath );
        assertTextPresent( "Delete Local Repository" );
        assertTextPresent( "Are you sure you want to delete Local Repository \"" + name + "\" ?" );
        assertButtonWithValuePresent( "Delete" );
        assertButtonWithValuePresent( "Cancel" );
        clickButtonWithValue( "Delete" );
        assertLocalRepositoryPage();
    }

    protected void goToAddLocalRepository()
    {
        goToLocalRepositoryPage();
        clickButtonWithValue( "Add" );
        assertAddLocalRepositoryPage();
    }

    protected void goToEditLocalRepository( String name, String location )
    {
        goToLocalRepositoryPage();
        String xPath = "//preceding::td[text()='" + name + "']//following::img[@alt='Edit']";
        clickLinkWithXPath( xPath );
        assertAddLocalRepositoryPage();
        assertFieldValue( name, "repository.name" );
        assertFieldValue( location, "repository.location" );
    }

    protected void addEditLocalRepository( String name, String location, boolean success )
    {
        setFieldValue( "repository.name", name );
        setFieldValue( "repository.location", location );
        submit();
        if ( success )
        {
            assertLocalRepositoryPage();
        }
        else
        {
            assertAddLocalRepositoryPage();
        }
    }
}
