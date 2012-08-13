package org.apache.maven.continuum.utils;

import org.codehaus.plexus.spring.PlexusInSpringTestCase;

import java.io.File;

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

/**
 * @author <a href="mailto:olamy@apache.org">olamy</a>
 * @version $Id$
 */
public class ContinuumUrlValidatorTest
    extends PlexusInSpringTestCase
{

    protected ContinuumUrlValidator getContinuumUrlValidator()
        throws Exception
    {
        return getContinuumUrlValidator( "continuumUrl" );
    }

    protected ContinuumUrlValidator getContinuumUrlValidator( String roleHint )
        throws Exception
    {
        return (ContinuumUrlValidator) lookup( ContinuumUrlValidator.class, roleHint );
    }

    public void testSuccessHttp()
        throws Exception
    {
        assertTrue( getContinuumUrlValidator().validate( "http://svn.apache.org/repos/asf/continuum/trunk/pom.xml" ) );
    }

    public void testFailureHttp()
        throws Exception
    {
        assertFalse( getContinuumUrlValidator().validate( "ttp://svn.apache.org/repos/asf/continuum/trunk/pom.xml" ) );
    }

    public void testSuccessHttpWithAuth()
        throws Exception
    {
        assertTrue( getContinuumUrlValidator().validate(
            "https://username:password@svn.apache.org/repos/asf/continuum/trunk/pom.xml" ) );
    }

    public void testFailureHttpWithAuth()
        throws Exception
    {
        assertTrue( getContinuumUrlValidator().validate(
            "http://username:passwordsvn.apache.org/repos/asf/continuum/trunk/pom.xml" ) );
    }

    public void testFailureHttpWithFile()
        throws Exception
    {
        assertFalse( getContinuumUrlValidator( "continuumUrlWithoutFile" ).validate( "file:///home/zloug/pom.xml" ) );
    }

    public void testSuccessHttps()
        throws Exception
    {
        assertTrue( getContinuumUrlValidator().validate( "https://svn.apache.org/repos/asf/continuum/trunk/pom.xml" ) );
    }

    public void testSuccessHttpsWithAuth()
        throws Exception
    {
        assertTrue( getContinuumUrlValidator().validate(
            "https://username:password@svn.apache.org/repos/asf/continuum/trunk/pom.xml" ) );
    }

    public void testSuccessHttpviewvc()
        throws Exception
    {
        assertTrue( getContinuumUrlValidator().validate(
            "http://svn.apache.org/viewvc/continuum/trunk/pom.xml?revision=681492&content-type=text%2Fplain" ) );
    }

    public void testSuccessHttpviewvcWithAuth()
        throws Exception
    {
        assertTrue( getContinuumUrlValidator().validate(
            "http://username:password@svn.apache.org/viewvc/continuum/trunk/pom.xml?revision=681492&content-type=text%2Fplain" ) );
    }

    public void testSuccessHttpsviewvc()
        throws Exception
    {
        assertTrue( getContinuumUrlValidator().validate(
            "https://svn.apache.org/viewvc/continuum/trunk/pom.xml?revision=681492&content-type=text%2Fplain" ) );
    }

    public void testSuccessHttpsviewvcWithAuth()
        throws Exception
    {
        assertTrue( getContinuumUrlValidator().validate(
            "https://username:password@svn.apache.org/viewvc/continuum/trunk/pom.xml?revision=681492&content-type=text%2Fplain" ) );
    }

    public void testSuccessHttpfisheye()
        throws Exception
    {
        assertTrue( getContinuumUrlValidator().validate(
            "http://fisheye6.atlassian.com/browse/~raw,r=680040/continuum/trunk/pom.xml" ) );
    }

    public void testSuccessHttpsfisheye()
        throws Exception
    {
        assertTrue( getContinuumUrlValidator().validate(
            "https://fisheye6.atlassian.com/browse/~raw,r=680040/continuum/trunk/pom.xml" ) );
    }

    public void testValidateFile()
        throws Exception
    {
        File rootPom = getTestFile( "src/test/resources/META-INF/continuum/continuum-configuration.xml" );
        assertTrue( rootPom.exists() );
        assertTrue( getContinuumUrlValidator().validate( rootPom.toURL().toExternalForm() ) );
    }


    public void testExtractUserNamePwd()
        throws Exception
    {
        ContinuumUrlValidator continuumUrlValidator = new ContinuumUrlValidator();
        URLUserInfo usrInfo = continuumUrlValidator.extractURLUserInfo(
            "https://username:password@svn.apache.org/repos/asf/continuum/trunk/pom.xml" );
        assertEquals( "username", usrInfo.getUsername() );
        assertEquals( "password", usrInfo.getPassword() );
    }

    public void testExtractUserNameEmptyPwd()
        throws Exception
    {
        ContinuumUrlValidator continuumUrlValidator = new ContinuumUrlValidator();
        URLUserInfo usrInfo = continuumUrlValidator.extractURLUserInfo(
            "https://username@svn.apache.org/repos/asf/continuum/trunk/pom.xml" );
        assertEquals( "username", usrInfo.getUsername() );
        assertNull( usrInfo.getPassword() );
    }

    public void testExtractEmptyUserNameEmptyPwd()
        throws Exception
    {
        ContinuumUrlValidator continuumUrlValidator = new ContinuumUrlValidator();
        URLUserInfo usrInfo = continuumUrlValidator.extractURLUserInfo(
            "https://svn.apache.org/repos/asf/continuum/trunk/pom.xml" );
        assertNull( usrInfo.getUsername() );
        assertNull( usrInfo.getPassword() );
    }
}
