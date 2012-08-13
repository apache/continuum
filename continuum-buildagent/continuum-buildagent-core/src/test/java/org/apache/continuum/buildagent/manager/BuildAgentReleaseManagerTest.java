package org.apache.continuum.buildagent.manager;

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

import org.apache.continuum.buildagent.configuration.BuildAgentConfigurationService;
import org.apache.continuum.buildagent.model.LocalRepository;
import org.apache.continuum.buildagent.utils.ContinuumBuildAgentUtil;
import org.apache.maven.continuum.release.ContinuumReleaseException;
import org.codehaus.plexus.spring.PlexusInSpringTestCase;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit3.JUnit3Mockery;
import org.jmock.lib.legacy.ClassImposteriser;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * For the CONTINUUM-2391 tests, checking of the local repository details is in ContinuumReleaseManagerStub. An
 * exception is thrown if the set local repository in the repository map is incorrect.
 */
public class BuildAgentReleaseManagerTest
    extends PlexusInSpringTestCase
{
    private Mockery context;

    private BuildAgentConfigurationService buildAgentConfigurationService;

    private DefaultBuildAgentReleaseManager releaseManager;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        context = new JUnit3Mockery();
        context.setImposteriser( ClassImposteriser.INSTANCE );

        releaseManager = (DefaultBuildAgentReleaseManager) lookup( BuildAgentReleaseManager.class );

        buildAgentConfigurationService = context.mock( BuildAgentConfigurationService.class );

        releaseManager.setBuildAgentConfigurationService( buildAgentConfigurationService );
    }

    protected void tearDown()
        throws Exception
    {
        releaseManager = null;

        super.tearDown();
    }

    // CONTINUUM-2391
    public void testLocalRepositoryInReleasePrepare()
        throws Exception
    {
        final List<LocalRepository> localRepos = createLocalRepositories();
        final File workingDir = new File( getBasedir(), "target/test-classes/working-dir" );

        context.checking( new Expectations()
        {
            {
                one( buildAgentConfigurationService ).getLocalRepositories();
                will( returnValue( localRepos ) );

                one( buildAgentConfigurationService ).getWorkingDirectory( 1 );
                will( returnValue( workingDir ) );

                one( buildAgentConfigurationService ).getAvailableInstallations();
                will( returnValue( null ) );
            }
        } );

        try
        {
            releaseManager.releasePrepare( createProjectMap(), createProperties(), createReleaseVersionMap(),
                                           createDevVersionMap(), createEnvironmentsMap(), "user" );
        }
        catch ( ContinuumReleaseException e )
        {
            fail( "An exception should not have been thrown!" );
        }
    }

    // CONTINUUM-2391
    public void testLocalRepositoryNameMismatchedCaseInReleasePrepare()
        throws Exception
    {
        final List<LocalRepository> localRepos = createLocalRepositories();
        final File workingDir = new File( getBasedir(), "target/test-classes/working-dir" );

        context.checking( new Expectations()
        {
            {
                one( buildAgentConfigurationService ).getLocalRepositories();
                will( returnValue( localRepos ) );

                one( buildAgentConfigurationService ).getWorkingDirectory( 1 );
                will( returnValue( workingDir ) );

                one( buildAgentConfigurationService ).getAvailableInstallations();
                will( returnValue( null ) );
            }
        } );

        Map<String, Object> map = createProjectMap();

        try
        {
            releaseManager.releasePrepare( map, createProperties(), createReleaseVersionMap(), createDevVersionMap(),
                                           createEnvironmentsMap(), "user" );
        }
        catch ( ContinuumReleaseException e )
        {
            fail( "An exception should not have been thrown!" );
        }
    }

    // CONTINUUM-2391
    @SuppressWarnings( "unchecked" )
    public void testLocalRepositoryInReleasePerform()
        throws Exception
    {
        final List<LocalRepository> localRepos = createLocalRepositories();
        final File workingDir = new File( getBasedir(), "target/test-classes/working-dir" );

        context.checking( new Expectations()
        {
            {
                one( buildAgentConfigurationService ).getLocalRepositories();
                will( returnValue( localRepos ) );

                one( buildAgentConfigurationService ).getWorkingDirectory();
                will( returnValue( workingDir ) );
            }
        } );

        Map repository = createRepositoryMap();
        repository.put( ContinuumBuildAgentUtil.KEY_LOCAL_REPOSITORY_NAME, "DEFAULT" );

        try
        {
            releaseManager.releasePerform( "1", "clean deploy", "", true, repository, "user" );
        }
        catch ( ContinuumReleaseException e )
        {
            fail( "An exception should not have been thrown!" );
        }
    }

    // CONTINUUM-2391
    public void testLocalRepositoryNameMismatchedCaseInReleasePerform()
        throws Exception
    {
        final List<LocalRepository> localRepos = createLocalRepositories();
        final File workingDir = new File( getBasedir(), "target/test-classes/working-dir" );

        context.checking( new Expectations()
        {
            {
                one( buildAgentConfigurationService ).getLocalRepositories();
                will( returnValue( localRepos ) );

                one( buildAgentConfigurationService ).getWorkingDirectory();
                will( returnValue( workingDir ) );
            }
        } );

        try
        {
            releaseManager.releasePerform( "1", "clean deploy", "", true, createRepositoryMap(), "user" );
        }
        catch ( ContinuumReleaseException e )
        {
            fail( "An exception should not have been thrown!" );
        }
    }

    // CONTINUUM-2391
    @SuppressWarnings( "unchecked" )
    public void testLocalRepositoryInReleasePerformFromScm()
        throws Exception
    {
        final List<LocalRepository> localRepos = createLocalRepositories();
        final File workingDir = new File( getBasedir(), "target/test-classes/working-dir" );

        context.checking( new Expectations()
        {
            {
                one( buildAgentConfigurationService ).getLocalRepositories();
                will( returnValue( localRepos ) );

                one( buildAgentConfigurationService ).getWorkingDirectory();
                will( returnValue( workingDir ) );
            }
        } );

        Map repository = new HashMap();
        repository.put( ContinuumBuildAgentUtil.KEY_USERNAME, "user" );
        repository.put( ContinuumBuildAgentUtil.KEY_LOCAL_REPOSITORY_NAME, "default" );

        try
        {
            releaseManager.releasePerformFromScm( "clean deploy", "", true, repository,
                                                  "scm:svn:http://svn.example.com/repos/test-project", "user",
                                                  "mypasswrd",
                                                  "scm:svn:http://svn.example.com/repos/test-project/tags/test-project-1.0",
                                                  "scm:svn:http://svn.example.com/repos/test-project/tags", null,
                                                  "user" );
        }
        catch ( ContinuumReleaseException e )
        {
            fail( "An exception should not have been thrown!" );
        }
    }

    private List<LocalRepository> createLocalRepositories()
    {
        List<LocalRepository> localRepos = new ArrayList<LocalRepository>();
        LocalRepository localRepo = new LocalRepository();
        localRepo.setName( "temp" );
        localRepo.setLocation( "/tmp/.m2/repository" );
        localRepo.setLayout( "default" );

        localRepos.add( localRepo );

        localRepo = new LocalRepository();
        localRepo.setName( "default" );
        localRepo.setLocation( "/home/user/.m2/repository" );
        localRepo.setLayout( "default" );

        localRepos.add( localRepo );

        return localRepos;
    }

    private Map<String, String> createEnvironmentsMap()
    {
        Map<String, String> environments = new HashMap<String, String>();
        environments.put( "M2_HOME", "/tmp/bin/apache-maven-2.2.1" );

        return environments;
    }

    private Map<String, String> createDevVersionMap()
    {
        Map<String, String> devVersion = new HashMap<String, String>();
        devVersion.put( "1.1-SNAPSHOT", "1.1-SNAPSHOT" );

        return devVersion;
    }

    private Map<String, String> createReleaseVersionMap()
    {
        Map<String, String> releaseVersion = new HashMap<String, String>();
        releaseVersion.put( "1.0", "1.0" );

        return releaseVersion;
    }

    private Properties createProperties()
    {
        Properties properties = new Properties();
        properties.put( ContinuumBuildAgentUtil.KEY_SCM_USERNAME, "scmusername" );
        properties.put( ContinuumBuildAgentUtil.KEY_SCM_PASSWORD, "scmpassword" );
        properties.put( ContinuumBuildAgentUtil.KEY_SCM_TAGBASE,
                        "scm:svn:http://svn.example.com/repos/test-project/tags" );
        properties.put( ContinuumBuildAgentUtil.KEY_PREPARE_GOALS, "clean install" );
        properties.put( ContinuumBuildAgentUtil.KEY_ARGUMENTS, "" );
        properties.put( ContinuumBuildAgentUtil.KEY_SCM_TAG, "test-project-1.0" );

        return properties;
    }

    private Map<String, Object> createProjectMap()
    {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put( ContinuumBuildAgentUtil.KEY_LOCAL_REPOSITORY_NAME, "default" );
        map.put( ContinuumBuildAgentUtil.KEY_PROJECT_ID, 1 );
        map.put( ContinuumBuildAgentUtil.KEY_GROUP_ID, "1" );
        map.put( ContinuumBuildAgentUtil.KEY_ARTIFACT_ID, "test-project" );
        map.put( ContinuumBuildAgentUtil.KEY_SCM_URL, "scm:svn:http://svn.example.com/repos/test-project/trunk" );

        return map;
    }

    @SuppressWarnings( "unchecked" )
    private Map createRepositoryMap()
    {
        Map repository = new HashMap();
        repository.put( ContinuumBuildAgentUtil.KEY_USERNAME, "user" );
        repository.put( ContinuumBuildAgentUtil.KEY_LOCAL_REPOSITORY_NAME, "default" );

        return repository;
    }
}
