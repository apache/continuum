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

import org.apache.continuum.web.test.parent.AbstractAdminTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Based on MyAccountTest of Emmanuel Venisse test.
 *
 * @author José Morales Martínez
 * @version $Id$
 */
@Test( groups = {"myAccount"} )
public class MyAccountTest
    extends AbstractAdminTest
{

    private static final String NEW_FULL_NAME = "Admin_FullName";

    private static final String NEW_EMAIL = "new_admin@mail.com";

    public void testMyAccountEdit()
        throws Exception
    {
        clickLinkWithText( "Edit Details" );
        String email = getFieldValue( "user.email" );
        String fullName = getFieldValue( "user.fullName" );
        setFieldValue( "user.fullName", NEW_FULL_NAME );
        setFieldValue( "user.email", NEW_EMAIL );
        submit();
        getSelenium().waitForPageToLoad( maxWaitTimeInMs );
        Assert.assertEquals( "Continuum - Group Summary", getTitle() );
        clickLinkWithText( "Edit Details" );
        assertFieldValue( NEW_FULL_NAME, "user.fullName" );
        assertFieldValue( NEW_EMAIL, "user.email" );
        setFieldValue( "user.fullName", fullName );
        setFieldValue( "user.email", email );
        submit();
        clickLinkWithText( "Edit Details" );
        assertFieldValue( fullName, "user.fullName" );
        assertFieldValue( email, "user.email" );
    }
}
