package org.apache.continuum.web.test.parent;

import org.testng.Assert;

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
public abstract class AbstractBuildQueueTest
    extends AbstractAdminTest
{
    public void goToBuildQueuePage()
    {
        clickLinkWithText( "Build Queue" );

        assertBuildQueuePage();
    }

    public void assertBuildQueuePage()
    {
        assertPage( "Continumm - Parallel Build Queue" );
        assertTextPresent( "Continuum - Parallel Build Queue" );
        assertTextPresent( "Name" );
        assertTextPresent( "DEFAULT_BUILD_QUEUE" );
        assertButtonWithValuePresent( "Add" );
    }

    public void removeBuildQueue( String queueName )
    {
        clickLinkWithXPath( "(//a[contains(@href,'deleteBuildQueue.action') and contains(@href, '" + queueName
            + "')])//img" );
        assertTextPresent( "Delete Parallel Build Queue" );
        assertTextPresent( "Are you sure you want to delete the build queue \"" + queueName + "\"?" );
        assertButtonWithValuePresent( "Delete" );
        assertButtonWithValuePresent( "Cancel" );
        clickButtonWithValue( "Delete" );
        assertBuildQueuePage();
    }

    public void assertAddBuildQueuePage()
    {
        assertPage( "Continuum - Add/Edit Parallel Build Queue" );
        assertTextPresent( "Continuum - Add/Edit Parallel Build Queue" );
        assertTextPresent( "Name*" );
        assertElementPresent( "name" );
        assertButtonWithValuePresent( "Save" );
        assertButtonWithValuePresent( "Cancel" );
    }

	public void addBuildQueue( String name, boolean success )
	{
	    goToBuildQueuePage();
	    assertBuildQueuePage();
	    submit();
	    assertAddBuildQueuePage();
	    setFieldValue( "name", name );
	    submit();
	    if ( success )
	    {
	        assertBuildQueuePage();
	        assertTextPresent( name );
	    }
	    else
	    {
	        assertAddBuildQueuePage();
	    }
	}
	    
	public void buildProjectForQueuePageTest( String projectGroupName, String groupId, String description, String projectName )
    throws Exception
    {       
        showProjectGroup( projectGroupName, groupId, description );
        clickButtonWithValue( "Build all projects" );
        waitForElementPresent( "//img[@alt='Building']" );
     }
	    
	    
}
