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

import org.apache.commons.io.FileUtils;
import org.apache.continuum.buildagent.configuration.BuildAgentConfigurationService;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.apache.jackrabbit.webdav.DavServletRequest;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.codehaus.plexus.spring.PlexusInSpringTestCase;

import java.io.File;

import static org.mockito.Mockito.*;

public class ContinuumBuildAgentDavResourceFactoryTest
    extends PlexusInSpringTestCase
{
    private DavServletRequest request;

    private DavServletResponse response;

    private BuildAgentConfigurationService buildAgentConfigurationService;

    private ContinuumBuildAgentDavResourceFactory resourceFactory;

    private File workingDirectory;

    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();

        request = mock( DavServletRequest.class );
        response = mock( DavServletResponse.class );
        buildAgentConfigurationService = mock( BuildAgentConfigurationService.class );

        resourceFactory = new ContinuumBuildAgentDavResourceFactory();
        resourceFactory.setBuildAgentConfigurationService( buildAgentConfigurationService );

        String appserverBase = getTestFile( "target/appserver-base" ).getAbsolutePath();
        System.setProperty( "appserver.base", appserverBase );

        workingDirectory = new File( appserverBase, "data/working-directory" );

        new File( workingDirectory, "1/target" ).mkdirs();
        new File( workingDirectory, "1/target/continuum-artifact-1.0.jar" ).createNewFile();
    }

    @Override
    public void tearDown()
        throws Exception
    {
        if ( workingDirectory.exists() )
        {
            FileUtils.deleteDirectory( workingDirectory );
        }

        super.tearDown();
    }

    public void testRequestArtifact()
        throws Exception
    {
        DavResourceLocator locator = new ContinuumBuildAgentDavResourceLocator( "http://myhost/",
                                                                                "/workingcopy/1/target/continuum-artifact-1.0.jar",
                                                                                new ContinuumBuildAgentDavLocatorFactory(),
                                                                                1 );

        try
        {
            when( request.getMethod() ).thenReturn( "GET" );
            when( buildAgentConfigurationService.getWorkingDirectory( 1 ) ).thenReturn( getWorkingDirectory( 1 ) );
            when( request.getDavSession() ).thenReturn( new ContinuumBuildAgentDavSession() );

            DavResource resource = resourceFactory.createResource( locator, request, response );

            assertNotNull( resource );
            assertEquals( locator.getRepositoryPath(), locator.getRepositoryPath() );
        }
        catch ( DavException e )
        {
            fail( "A DavException should not have been thrown!" );
        }
    }

    public void testRequestArtifactDoesNotExist()
        throws Exception
    {
        DavResourceLocator locator = new ContinuumBuildAgentDavResourceLocator( "http://myhost/",
                                                                                "/workingcopy/1/pom.xml",
                                                                                new ContinuumBuildAgentDavLocatorFactory(),
                                                                                1 );

        try
        {
            when( request.getMethod() ).thenReturn( "GET" );
            when( buildAgentConfigurationService.getWorkingDirectory( 1 ) ).thenReturn( getWorkingDirectory( 1 ) );

            resourceFactory.createResource( locator, request, response );

            fail( "A DavException with 404 error code should have been thrown." );
        }
        catch ( DavException e )
        {
            assertEquals( 404, e.getErrorCode() );
        }
    }

    private File getWorkingDirectory( int projectId )
    {
        return new File( workingDirectory, String.valueOf( projectId ) );
    }
}
