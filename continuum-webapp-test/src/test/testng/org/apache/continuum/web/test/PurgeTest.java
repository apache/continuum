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

import org.apache.continuum.web.test.parent.AbstractPurgeTest;
import org.testng.annotations.Test;

/**
 * @author José Morales Martínez
 * @version $Id$
 */
@Test( groups = {"purge"} )
public class PurgeTest
    extends AbstractPurgeTest
{
    public void testAddRepositoryPurge()
    {
        String PURGE_REPOSITORY_DESCRIPTION = getProperty( "PURGE_REPOSITORY_DESCRIPTION" );
        String PURGE_REPOSITORY_DAYS = getProperty( "PURGE_REPOSITORY_DAYS" );
        String PURGE_REPOSITORY_RETETION = getProperty( "PURGE_REPOSITORY_RETETION" );
        goToAddRepositoryPurge();
        addEditRepositoryPurge( PURGE_REPOSITORY_DAYS, PURGE_REPOSITORY_RETETION, PURGE_REPOSITORY_DESCRIPTION, true );
    }

    public void testAddInvalidRepositoryPurge()
    {
        String PURGE_REPOSITORY_DESCRIPTION = getProperty( "PURGE_REPOSITORY_DESCRIPTION" );
        goToAddRepositoryPurge();
        addEditRepositoryPurge( "", "", PURGE_REPOSITORY_DESCRIPTION, false );
        assertTextPresent( "Retention Count must be greater than 0." );
    }

    @Test( dependsOnMethods = {"testAddRepositoryPurge"} )
    public void testEditRepositoryPurge()
    {
        String PURGE_REPOSITORY_DESCRIPTION = getProperty( "PURGE_REPOSITORY_DESCRIPTION" );
        String PURGE_REPOSITORY_DAYS = getProperty( "PURGE_REPOSITORY_DAYS" );
        String PURGE_REPOSITORY_RETETION = getProperty( "PURGE_REPOSITORY_RETETION" );
        String newDescription = "new_description";
        String newDays = "45";
        String newRetention = "4";
        goToEditRepositoryPurge( PURGE_REPOSITORY_DAYS, PURGE_REPOSITORY_RETETION, PURGE_REPOSITORY_DESCRIPTION );
        addEditRepositoryPurge( newDays, newRetention, newDescription, true );
        goToEditRepositoryPurge( newDays, newRetention, newDescription );
        addEditRepositoryPurge( PURGE_REPOSITORY_DAYS, PURGE_REPOSITORY_RETETION, PURGE_REPOSITORY_DESCRIPTION, true );
    }

    @Test( dependsOnMethods = {"testEditRepositoryPurge"} )
    public void testDeleteRepositoryPurge()
    {
        String PURGE_REPOSITORY_DESCRIPTION = getProperty( "PURGE_REPOSITORY_DESCRIPTION" );
        removeRepositoryPurge( PURGE_REPOSITORY_DESCRIPTION );
    }

    public void testAddDirectoryPurge()
    {
        String PURGE_DIRECTORY_DESCRIPTION = getProperty( "PURGE_DIRECTORY_DESCRIPTION" );
        String PURGE_DIRECTORY_DAYS = getProperty( "PURGE_DIRECTORY_DAYS" );
        String PURGE_DIRECTORY_RETETION = getProperty( "PURGE_DIRECTORY_RETETION" );
        goToAddDirectoryPurge();
        addEditDirectoryPurge( PURGE_DIRECTORY_DAYS, PURGE_DIRECTORY_RETETION, PURGE_DIRECTORY_DESCRIPTION, true );
    }

    public void testAddInvalidDirectoryPurge()
    {
        String PURGE_DIRECTORY_DESCRIPTION = getProperty( "PURGE_DIRECTORY_DESCRIPTION" );
        goToAddDirectoryPurge();
        addEditDirectoryPurge( "", "", PURGE_DIRECTORY_DESCRIPTION, false );
        assertTextPresent( "Retention Count must be greater than 0." );
    }

    @Test( dependsOnMethods = {"testAddDirectoryPurge"} )
    public void testEditDirectoryPurge()
    {
        String PURGE_DIRECTORY_DESCRIPTION = getProperty( "PURGE_DIRECTORY_DESCRIPTION" );
        String PURGE_DIRECTORY_DAYS = getProperty( "PURGE_DIRECTORY_DAYS" );
        String PURGE_DIRECTORY_RETETION = getProperty( "PURGE_DIRECTORY_RETETION" );
        String newDescription = "new_description";
        String newDays = "45";
        String newRetention = "4";
        goToEditDirectoryPurge( PURGE_DIRECTORY_DAYS, PURGE_DIRECTORY_RETETION, PURGE_DIRECTORY_DESCRIPTION );
        addEditDirectoryPurge( newDays, newRetention, newDescription, true );
        goToEditDirectoryPurge( newDays, newRetention, newDescription );
        addEditDirectoryPurge( PURGE_DIRECTORY_DAYS, PURGE_DIRECTORY_RETETION, PURGE_DIRECTORY_DESCRIPTION, true );
    }

    @Test( dependsOnMethods = {"testEditDirectoryPurge"} )
    public void testDeleteDirectoryPurge()
    {
        String PURGE_DIRECTORY_DESCRIPTION = getProperty( "PURGE_DIRECTORY_DESCRIPTION" );
        removeDirectoryPurge( PURGE_DIRECTORY_DESCRIPTION );
    }
}
