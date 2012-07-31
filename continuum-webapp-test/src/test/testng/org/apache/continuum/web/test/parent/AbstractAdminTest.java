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

import org.testng.annotations.BeforeMethod;

public abstract class AbstractAdminTest
    extends AbstractContinuumTest
{
    @BeforeMethod( alwaysRun = true )
    public void loginAsAdmin()
    {
        loginAs( getProperty( "ADMIN_USERNAME" ), getProperty( "ADMIN_PASSWORD" ) );
    }

    protected void loginAs( String username, String password )
    {
        if ( !getSelenium().isElementPresent( "//span[@class='username' and text()='" + username + "']" ) )
        {
            login( username, password );
        }
    }

    protected String getBuildAgentUrl()
    {
        return baseUrl.replace( "/continuum", "/continuum-buildagent/xmlrpc" );
    }
}
