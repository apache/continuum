package org.apache.continuum.buildagent.build.execution.maven.m2;

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

import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.MavenSettingsBuilder;
import org.apache.maven.settings.Profile;
import org.apache.maven.settings.Repository;
import org.apache.maven.settings.RepositoryPolicy;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.spring.PlexusInSpringTestCase;

import java.io.File;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultBuildAgentMavenBuilderHelperTest
    extends PlexusInSpringTestCase
{
    private DefaultBuildAgentMavenBuilderHelper helper;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        helper = (DefaultBuildAgentMavenBuilderHelper) lookup( BuildAgentMavenBuilderHelper.class );

        MavenSettingsBuilder builder = mock(MavenSettingsBuilder.class);
        when( builder.buildSettings( false )).thenReturn( createSettings() );
        helper.setMavenSettingsBuilder( builder );
    }

    private static Settings createSettings()
    {
        Settings settings = new Settings();
        settings.setLocalRepository( getTestFile( "target/local-repository" ).getAbsolutePath() );
        Profile profile = new Profile();
        profile.setId( "repo" );
        Repository repository = new Repository();
        repository.setId( "central" );
        repository.setUrl( getTestFile( "src/test/test-repo" ).toURI().toString() );
        RepositoryPolicy policy = new RepositoryPolicy();
        policy.setEnabled( true );
        policy.setUpdatePolicy( "always" );
        repository.setSnapshots( policy );
        profile.addRepository( repository );
        settings.addProfile( profile );
        settings.addActiveProfile( "repo" );
        return settings;
    }

    public void testGetMavenProjectWithMaven3Metadata()
        throws Exception
    {
        File pomFile = getTestFile( "src/test/test-projects/maven3-metadata/pom.xml" );
        MavenProject project = helper.getMavenProject( new ContinuumProjectBuildingResult(), pomFile );
        assertNotNull( project );
    }
}