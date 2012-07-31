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

import org.apache.continuum.web.test.parent.AbstractAdminTest;
import org.apache.continuum.web.test.parent.AbstractSeleniumTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Based on MyAccountTest of Emmanuel Venisse test.
 *
 * @author José Morales Martínez
 * @version $Id$
 */
@Test( groups = { "myAccount" } )
public class MyAccountTest
    extends AbstractAdminTest
{
    public String newFullName = "Admin_FullName";

    public String newEmail = "new_admin@mail.com";

    public void testMyAccountEdit()
        throws Exception
    {
        clickLinkWithText( "Edit Details" );
        String email = getFieldValue( "user.email" );
        setFieldValue( "user.fullName", newFullName );
        setFieldValue( "user.email", newEmail );
        submit();
        getSelenium().waitForPageToLoad( maxWaitTimeInMs );
        Assert.assertEquals( "Continuum - Group Summary", getTitle() );
        clickLinkWithText( "Edit Details" );
        assertFieldValue( newFullName, "user.fullName" );
        assertFieldValue( newEmail, "user.email" );
        setFieldValue( "user.fullName", getProperty( "ADMIN_USERNAME" ) );
        setFieldValue( "user.email", email );
        submit();
        clickLinkWithText( "Edit Details" );
        assertFieldValue( getProperty( "ADMIN_USERNAME" ), "user.fullName" );
        assertFieldValue( email, "user.email" );
    }
}
