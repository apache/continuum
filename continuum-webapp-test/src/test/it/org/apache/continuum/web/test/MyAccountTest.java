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
 * Test update or edit account of the current user.
 */
public class MyAccountTest
    extends AbstractAuthenticatedAdminAccessTestCase
{
    public void setUp()
        throws Exception
    {
        super.setUp();
    }

    public void testMyAccountEdit()
        throws Exception
    {
        goToMyAccount();
        // check current account details
        assertMyAccountDetails( adminUsername, adminFullName, adminEmail );
    }
    
    public void testEditAccountDuplicatePassword() 
    	throws Exception
    {
    	goToMyAccount();
    	assertMyAccountDetails( adminUsername, adminFullName, adminEmail );
    	editMyUserInfo( adminFullName, adminEmail, adminPassword ,adminPassword, adminPassword );
    	assertTextPresent( "Your password cannot match any of your previous 6 password(s)." );
    	clickButtonWithValue( "Cancel" );
    	assertPage( "Login Page" );
    }

}
