package org.apache.continuum.buildagent.configuration;

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

import org.apache.continuum.buildagent.model.Installation;
import org.apache.continuum.buildagent.model.LocalRepository;
import org.codehaus.plexus.spring.PlexusInSpringTestCase;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BuildAgentConfigurationTest
    extends PlexusInSpringTestCase
{
    public void testInitialize()
        throws Exception
    {
        DefaultBuildAgentConfiguration config = (DefaultBuildAgentConfiguration) lookup(
            BuildAgentConfiguration.class );

        config.setConfigurationFile( new File( getBasedir(),
                                               "target/test-classes/buildagent-config/continuum-buildagent.xml" ) );

        config.initialize();

        GeneralBuildAgentConfiguration generalConfig = config.getContinuumBuildAgentConfiguration();
        assertEquals( "http://localhost:9595/continuum/master-xmlrpc", generalConfig.getContinuumServerUrl() );
        assertEquals( new File( "/tmp/data/build-output-directory" ), generalConfig.getBuildOutputDirectory() );
        assertEquals( new File( "/tmp/data/working-directory" ), generalConfig.getWorkingDirectory() );
        assertEquals( 1, generalConfig.getInstallations().size() );

        Installation installation = generalConfig.getInstallations().get( 0 );
        assertEquals( "Tool", installation.getType() );
        assertEquals( "Maven 2.2.1 Installation", installation.getName() );
        assertEquals( "M2_HOME", installation.getVarName() );
        assertEquals( "/tmp/apache-maven-2.2.1", installation.getVarValue() );

        LocalRepository localRepo = generalConfig.getLocalRepositories().get( 0 );
        assertLocalRepository( getExpectedLocalRepo(), localRepo );
    }

    public void testSaveExistingConfiguration()
        throws Exception
    {
        DefaultBuildAgentConfiguration config = (DefaultBuildAgentConfiguration) lookup(
            BuildAgentConfiguration.class );

        config.setConfigurationFile( new File( getBasedir(),
                                               "target/test-classes/buildagent-config/continuum-buildagent-edit.xml" ) );

        config.initialize();

        String expected = "http://192.165.240.12:8080/continuum/master-xmlrpc";

        GeneralBuildAgentConfiguration generalConfig = config.getContinuumBuildAgentConfiguration();

        assertEquals( "http://localhost:9595/continuum/master-xmlrpc", generalConfig.getContinuumServerUrl() );
        assertEquals( 1, generalConfig.getInstallations().size() );

        generalConfig.setContinuumServerUrl( expected );

        Installation expectedInstallation = getExpectedInstallation();
        generalConfig.getInstallations().add( expectedInstallation );

        LocalRepository expectedLocalRepo = getExpectedLocalRepo();

        List<LocalRepository> localRepos = new ArrayList<LocalRepository>();
        localRepos.add( expectedLocalRepo );

        generalConfig.setLocalRepositories( localRepos );

        config.save();

        config.reload();

        assertEquals( expected, config.getContinuumBuildAgentConfiguration().getContinuumServerUrl() );
        assertEquals( 2, config.getContinuumBuildAgentConfiguration().getInstallations().size() );

        Installation installation = generalConfig.getInstallations().get( 1 );
        assertInstallation( expectedInstallation, installation );

        LocalRepository localRepo = generalConfig.getLocalRepositories().get( 0 );
        assertLocalRepository( expectedLocalRepo, localRepo );
    }

    public void testSaveNewConfiguration()
        throws Exception
    {
        File configFile = new File( getBasedir(),
                                    "target/test-classes/buildagent-config/continuum-buildagent-new.xml" );
        DefaultBuildAgentConfiguration config = (DefaultBuildAgentConfiguration) lookup(
            BuildAgentConfiguration.class );

        config.setConfigurationFile( configFile );

        config.initialize();

        String expectedUrl = "http://localhost:8080/continuum/master-xmlrpc";
        File expectedBuildOutputDir = new File( "/tmp/data/build-output-directory" );
        File expectedWorkingDir = new File( "/tmp/data/working-directory" );

        GeneralBuildAgentConfiguration generalConfig = config.getContinuumBuildAgentConfiguration();

        assertNull( generalConfig.getContinuumServerUrl() );
        assertNull( generalConfig.getBuildOutputDirectory() );
        assertNull( generalConfig.getWorkingDirectory() );
        assertNull( generalConfig.getInstallations() );

        Installation expectedInstallation = getExpectedInstallation();

        List<Installation> installations = new ArrayList<Installation>();
        installations.add( expectedInstallation );

        LocalRepository expectedLocalRepo = getExpectedLocalRepo();

        List<LocalRepository> localRepos = new ArrayList<LocalRepository>();
        localRepos.add( expectedLocalRepo );

        generalConfig.setContinuumServerUrl( expectedUrl );
        generalConfig.setBuildOutputDirectory( expectedBuildOutputDir );
        generalConfig.setWorkingDirectory( expectedWorkingDir );
        generalConfig.setInstallations( installations );
        generalConfig.setLocalRepositories( localRepos );

        config.save();

        config.reload();

        assertTrue( configFile.exists() );
        assertEquals( expectedUrl, config.getContinuumBuildAgentConfiguration().getContinuumServerUrl() );
        assertEquals( expectedBuildOutputDir, config.getContinuumBuildAgentConfiguration().getBuildOutputDirectory() );
        assertEquals( expectedWorkingDir, config.getContinuumBuildAgentConfiguration().getWorkingDirectory() );
        assertEquals( 1, config.getContinuumBuildAgentConfiguration().getInstallations().size() );

        Installation installation = generalConfig.getInstallations().get( 0 );
        assertInstallation( expectedInstallation, installation );

        LocalRepository localRepo = generalConfig.getLocalRepositories().get( 0 );
        assertLocalRepository( expectedLocalRepo, localRepo );
    }

    private Installation getExpectedInstallation()
    {
        Installation expectedInstallation = new Installation();
        expectedInstallation.setName( "Maven 2.0.10 Installation" );
        expectedInstallation.setType( "Tool" );
        expectedInstallation.setVarName( "M2_HOME" );
        expectedInstallation.setVarValue( "/tmp/apache-maven-2.1.10" );
        return expectedInstallation;
    }

    private LocalRepository getExpectedLocalRepo()
    {
        LocalRepository expectedLocalRepo = new LocalRepository();
        expectedLocalRepo.setName( "default" );
        expectedLocalRepo.setLayout( "default" );
        expectedLocalRepo.setLocation( "/tmp/.m2/repository" );
        return expectedLocalRepo;
    }

    private void assertLocalRepository( LocalRepository expectedLocalRepo, LocalRepository localRepo )
    {
        assertEquals( expectedLocalRepo.getName(), localRepo.getName() );
        assertEquals( expectedLocalRepo.getLayout(), localRepo.getLayout() );
        assertEquals( expectedLocalRepo.getLocation(), localRepo.getLocation() );
    }

    private void assertInstallation( Installation expectedInstallation, Installation installation )
    {
        assertEquals( expectedInstallation.getType(), installation.getType() );
        assertEquals( expectedInstallation.getName(), installation.getName() );
        assertEquals( expectedInstallation.getVarName(), installation.getVarName() );
        assertEquals( expectedInstallation.getVarValue(), installation.getVarValue() );
    }
}
