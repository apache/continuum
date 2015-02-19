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

import org.apache.continuum.web.test.parent.AbstractLocalRepositoryTest;
import org.testng.annotations.Test;

/**
 * @author José Morales Martínez
 */
@Test( groups = {"repository"} )
public class LocalRepositoriesTest
    extends AbstractLocalRepositoryTest
{
    public void testAddLocalRepository()
    {
        String LOCAL_REPOSITORY_NAME = getProperty( "LOCAL_REPOSITORY_NAME" );
        String LOCAL_REPOSITORY_LOCATION = getProperty( "LOCAL_REPOSITORY_LOCATION" );
        goToAddLocalRepository();
        addEditLocalRepository( LOCAL_REPOSITORY_NAME, LOCAL_REPOSITORY_LOCATION, true );
    }

    public void testAddInvalidLocalRepository()
    {
        goToAddLocalRepository();
        addEditLocalRepository( "", "", false );
        assertTextPresent( "You must define a name." );
        assertTextPresent( "You must define a local repository directory." );
    }

    public void testAddLocalRepositoryWithXSS()
    {
        String invalidName = "Repo <script>alert('gotcha')</script> name";
        String invalidLocation = "/Users/continuum/<script>alert('gotcha')</script>/dir";

        goToAddLocalRepository();
        addEditLocalRepository( invalidName, invalidLocation, false );
        assertTextPresent( "Local repository name contains invalid characters." );
        assertTextPresent( "Local repository location contains invalid characters." );
    }

    @Test( dependsOnMethods = {"testAddLocalRepository"} )
    public void testAddDuplicatedLocalRepository()
    {
        String LOCAL_REPOSITORY_NAME = getProperty( "LOCAL_REPOSITORY_NAME" );
        String LOCAL_REPOSITORY_LOCATION = getProperty( "LOCAL_REPOSITORY_LOCATION" );
        goToAddLocalRepository();
        addEditLocalRepository( LOCAL_REPOSITORY_NAME, LOCAL_REPOSITORY_LOCATION, false );
        assertTextPresent( "Local repository name must be unique" );
        assertTextPresent( "Local repository location must be unique" );
    }

    @Test( dependsOnMethods = {"testAddDuplicatedLocalRepository"} )
    public void testAddDuplicateLocalRepositoryWithTrailingWhitespaces()
    {
        String duplicateRepositoryName = addTrailingWhitespace( getProperty( "LOCAL_REPOSITORY_NAME" ) );
        String duplicateRepositoryLocation = addTrailingWhitespace( getProperty( "LOCAL_REPOSITORY_LOCATION" ) );
        goToAddLocalRepository();
        addEditLocalRepository( duplicateRepositoryName, duplicateRepositoryLocation, false );
        assertTextPresent( "Local repository name must be unique" );
        assertTextPresent( "Local repository location must be unique" );
    }

    @Test( dependsOnMethods = {"testAddDuplicateLocalRepositoryWithTrailingWhitespaces"} )
    public void testEditLocalRepository()
    {
        String LOCAL_REPOSITORY_NAME = getProperty( "LOCAL_REPOSITORY_NAME" );
        String LOCAL_REPOSITORY_LOCATION = getProperty( "LOCAL_REPOSITORY_LOCATION" );
        String newName = "new_name";
        String newLocation = "new_location";
        goToEditLocalRepository( LOCAL_REPOSITORY_NAME, LOCAL_REPOSITORY_LOCATION );
        addEditLocalRepository( newName, newLocation, true );
        goToEditLocalRepository( newName, newLocation );
        addEditLocalRepository( LOCAL_REPOSITORY_NAME, LOCAL_REPOSITORY_LOCATION, true );
    }

    @Test( dependsOnMethods = {"testEditLocalRepository"} )
    public void testDeleteLocalRepository()
    {
        String LOCAL_REPOSITORY_NAME = getProperty( "LOCAL_REPOSITORY_NAME" );
        removeLocalRepository( LOCAL_REPOSITORY_NAME );
    }

    private String addTrailingWhitespace( String str )
    {
        String WHITESPACE = "    ";
        return WHITESPACE.concat( str.concat( WHITESPACE ) );
    }
}
