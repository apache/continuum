package org.apache.maven.continuum.utils;

import junit.framework.TestCase;

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
    extends TestCase
{

    public void testSuccessHttp()
        throws Exception
    {
        assertTrue( new ContinuumUrlValidator().validate( "http://svn.apache.org/repos/asf/continuum/trunk/pom.xml" ) );
    }

    public void testFailureHttp()
        throws Exception
    {
        assertFalse( new ContinuumUrlValidator().validate( "ttp://svn.apache.org/repos/asf/continuum/trunk/pom.xml" ) );
    }

    public void testSuccessHttpWithAuth()
        throws Exception
    {
        assertTrue( new ContinuumUrlValidator()
            .validate( "https://username:password@svn.apache.org/repos/asf/continuum/trunk/pom.xml" ) );
    }

    public void testFailureHttpWithAuth()
        throws Exception
    {
        assertFalse( new ContinuumUrlValidator()
            .validate( "http://username:passwordsvn.apache.org/repos/asf/continuum/trunk/pom.xml" ) );
    }

    public void testFailureHttpWithFile()
        throws Exception
    {
        assertFalse( new ContinuumUrlValidator().validate( "file:///home/zloug/pom.xml" ) );
    }

    public void testSuccessHttps()
        throws Exception
    {
        assertTrue( new ContinuumUrlValidator().validate( "https://svn.apache.org/repos/asf/continuum/trunk/pom.xml" ) );
    }

    public void testSuccessHttpsWithAuth()
        throws Exception
    {
        assertTrue( new ContinuumUrlValidator()
            .validate( "https://username:password@svn.apache.org/repos/asf/continuum/trunk/pom.xml" ) );
    }

    public void testSuccessHttpviewvc()
        throws Exception
    {
        assertTrue( new ContinuumUrlValidator()
            .validate( "http://svn.apache.org/viewvc/continuum/trunk/pom.xml?revision=681492&content-type=text%2Fplain" ) );
    }

    public void testSuccessHttpviewvcWithAuth()
        throws Exception
    {
        assertTrue( new ContinuumUrlValidator()
            .validate( "http://username:password@svn.apache.org/viewvc/continuum/trunk/pom.xml?revision=681492&content-type=text%2Fplain" ) );
    }

    public void testSuccessHttpsviewvc()
        throws Exception
    {
        assertTrue( new ContinuumUrlValidator()
            .validate( "https://svn.apache.org/viewvc/continuum/trunk/pom.xml?revision=681492&content-type=text%2Fplain" ) );
    }

    public void testSuccessHttpsviewvcWithAuth()
        throws Exception
    {
        assertTrue( new ContinuumUrlValidator()
            .validate( "https://username:password@svn.apache.org/viewvc/continuum/trunk/pom.xml?revision=681492&content-type=text%2Fplain" ) );
    }

    public void testSuccessHttpfisheye()
        throws Exception
    {
        assertTrue( new ContinuumUrlValidator()
            .validate( "http://fisheye6.atlassian.com/browse/~raw,r=680040/continuum/trunk/pom.xml" ) );
    }

    public void testSuccessHttpsfisheye()
        throws Exception
    {
        assertTrue( new ContinuumUrlValidator()
            .validate( "https://fisheye6.atlassian.com/browse/~raw,r=680040/continuum/trunk/pom.xml" ) );
    }
   
}
