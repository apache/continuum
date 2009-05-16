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

import org.apache.continuum.web.test.parent.AbstractBuildQueueTest;
import org.testng.annotations.Test;

/**
 * @author José Morales Martínez
 * @version $Id$
 */
@Test( groups = { "queue" }, dependsOnMethods = { "testWithCorrectUsernamePassword" } )
public class QueueTest
    extends AbstractBuildQueueTest
{

    public void testAddBuildQueue()
    {
        setMaxBuildQueue( 2 );
        String BUILD_QUEUE_NAME = p.getProperty( "BUILD_QUEUE_NAME" );
        addBuildQueue( BUILD_QUEUE_NAME, true );
    }

    @Test( dependsOnMethods = { "testAddBuildQueue" } )
    public void testAddNotAllowedBuildQueue()
    {
        setMaxBuildQueue( 1 );
        String secodQueue = "second_queue_name";
        addBuildQueue( secodQueue, false );
        assertTextPresent( "You are only allowed 1 number of builds in parallel." );
    }

    @Test( dependsOnMethods = { "testAddBuildQueue" } )
    public void testAddAlreadyExistBuildQueue()
    {
        setMaxBuildQueue( 3 );
        String BUILD_QUEUE_NAME = p.getProperty( "BUILD_QUEUE_NAME" );
        addBuildQueue( BUILD_QUEUE_NAME, false );
        assertTextPresent( "Build queue name already exists." );
    }

    public void testAddEmptyBuildQueue()
    {
        setMaxBuildQueue( 3 );
        addBuildQueue( "", false );
        assertTextPresent( "You must define a name" );
    }

    @Test( dependsOnMethods = { "testAddBuildQueue", "testAddAlreadyExistBuildQueue" } )
    public void testDeleteBuildQueue()
    {
        goToBuildQueuePage();
        String BUILD_QUEUE_NAME = p.getProperty( "BUILD_QUEUE_NAME" );
        removeBuildQueue( BUILD_QUEUE_NAME );
    }

    public void testQueuePage()
    {
        clickLinkWithText( "Queues" );
        assertPage( "Continuum - Build Queue" );
        assertTextPresent( "Current Build" );
        assertTextPresent( "Continuum - Build Queue" );
        assertTextPresent( "Current Checkout" );
        assertTextPresent( "Checkout Queue" );
    }
}
