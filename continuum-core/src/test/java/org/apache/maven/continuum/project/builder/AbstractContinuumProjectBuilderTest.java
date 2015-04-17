package org.apache.maven.continuum.project.builder;

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
import org.apache.maven.continuum.model.project.BuildDefinitionTemplate;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Test for {@link AbstractContinuumProjectBuilder}
 *
 * @author <a href="mailto:carlos@apache.org">Carlos Sanchez</a>
 */
public abstract class AbstractContinuumProjectBuilderTest
{

    private ContinuumProjectBuilder builder;

    private File importRoot;

    @Before
    public void setUp()
        throws Exception
    {
        File tmpDir = new File( System.getProperty( "java.io.tmpdir" ) );
        importRoot = File.createTempFile( getClass().getSimpleName(), "", tmpDir );
        builder = new ContinuumProjectBuilder();
    }

    @After
    public void tearDown()
        throws IOException
    {
        if ( importRoot.exists() )
        {
            FileUtils.deleteDirectory( importRoot );
        }
    }

    @Test
    @Ignore( "requires a password protected resource under https" )
    public void testCreateMetadataFileURLStringString()
        throws Exception
    {

        URL url = new URL( "https://someurl/pom.xml" );
        String username = "myusername";
        String password = "mypassword";
        builder.createMetadataFile( importRoot, url, username, password, new ContinuumProjectBuildingResult() );
    }

    private class ContinuumProjectBuilder
        extends AbstractContinuumProjectBuilder
    {

        public ContinuumProjectBuildingResult buildProjectsFromMetadata( URL url, String username, String password )
            throws ContinuumProjectBuilderException
        {
            return null;
        }

        public ContinuumProjectBuildingResult buildProjectsFromMetadata( URL url, String username, String password,
                                                                         boolean recursiveProjects,
                                                                         boolean checkoutInSingleDirectory )
            throws ContinuumProjectBuilderException
        {
            return null;
        }

        public ContinuumProjectBuildingResult buildProjectsFromMetadata( URL url, String username, String password,
                                                                         boolean recursiveProjects,
                                                                         BuildDefinitionTemplate buildDefinitionTemplate,
                                                                         boolean checkoutInSingleDirectory )
            throws ContinuumProjectBuilderException
        {
            return null;
        }

        public ContinuumProjectBuildingResult buildProjectsFromMetadata( URL url, String username, String password,
                                                                         boolean recursiveProjects,
                                                                         BuildDefinitionTemplate buildDefinitionTemplate,
                                                                         boolean checkoutInSingleDirectory,
                                                                         int projectGroupId )
            throws ContinuumProjectBuilderException
        {
            return null;
        }

        public BuildDefinitionTemplate getDefaultBuildDefinitionTemplate()
            throws ContinuumProjectBuilderException
        {
            return null;
        }

    }

}
