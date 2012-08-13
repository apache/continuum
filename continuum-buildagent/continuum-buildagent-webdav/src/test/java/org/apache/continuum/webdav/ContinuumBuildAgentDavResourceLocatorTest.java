package org.apache.continuum.webdav;

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

import junit.framework.TestCase;

public class ContinuumBuildAgentDavResourceLocatorTest
    extends TestCase
{
    private ContinuumBuildAgentDavLocatorFactory factory;

    private ContinuumBuildAgentDavResourceLocator locator;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        factory = new ContinuumBuildAgentDavLocatorFactory();
    }

    public void testAvoidDoubleSlashInHref()
        throws Exception
    {
        String prefix = "http://myhost/";
        String href = "/workingcopy/1/";
        locator = getLocator( prefix, href );

        assertEquals( 1, locator.getProjectId() );
        assertEquals( "", locator.getWorkspaceName() );
        assertEquals( "", locator.getWorkspacePath() );
        assertEquals( "http://myhost/", locator.getPrefix() );
        assertEquals( "http://myhost/workingcopy/1/", locator.getHref( false ) );
        assertEquals( "http://myhost/workingcopy/1/", locator.getHref( true ) );
        assertEquals( "/workingcopy/1", locator.getResourcePath() );
        assertEquals( "/workingcopy/1", locator.getRepositoryPath() );
    }

    public void testLocatorWithPrefixHref()
        throws Exception
    {
        String prefix = "http://myhost/";
        String href = "/workingcopy/1";
        locator = getLocator( prefix, href );

        assertEquals( 1, locator.getProjectId() );
        assertEquals( "", locator.getWorkspaceName() );
        assertEquals( "", locator.getWorkspacePath() );
        assertEquals( "http://myhost/", locator.getPrefix() );
        assertEquals( "http://myhost/workingcopy/1", locator.getHref( false ) );
        assertEquals( "http://myhost/workingcopy/1/", locator.getHref( true ) );
        assertEquals( "/workingcopy/1", locator.getResourcePath() );
        assertEquals( "/workingcopy/1", locator.getRepositoryPath() );
    }

    public void testLocatorWithHrefThatContainsPrefix()
        throws Exception
    {
        String prefix = "http://myhost/";
        String href = "http://myhost/workingcopy/1";
        locator = getLocator( prefix, href );

        assertEquals( 1, locator.getProjectId() );
        assertEquals( "", locator.getWorkspaceName() );
        assertEquals( "", locator.getWorkspacePath() );
        assertEquals( "http://myhost/", locator.getPrefix() );
        assertEquals( "http://myhost/workingcopy/1", locator.getHref( false ) );
        assertEquals( "http://myhost/workingcopy/1/", locator.getHref( true ) );
        assertEquals( "/workingcopy/1", locator.getResourcePath() );
        assertEquals( "/workingcopy/1", locator.getRepositoryPath() );
    }

    public void testLocatorWithRootHref()
        throws Exception
    {
        String prefix = "http://myhost/";
        String href = "/";
        locator = getLocator( prefix, href );

        assertEquals( 0, locator.getProjectId() );
        assertEquals( "", locator.getWorkspaceName() );
        assertEquals( "", locator.getWorkspacePath() );
        assertEquals( "http://myhost/", locator.getPrefix() );
        assertEquals( "http://myhost/", locator.getHref( false ) );
        assertEquals( "http://myhost/", locator.getHref( true ) );
        assertEquals( "/", locator.getResourcePath() );
        assertEquals( "/", locator.getRepositoryPath() );
    }

    private ContinuumBuildAgentDavResourceLocator getLocator( String prefix, String href )
    {
        return (ContinuumBuildAgentDavResourceLocator) factory.createResourceLocator( prefix, href );
    }
}
