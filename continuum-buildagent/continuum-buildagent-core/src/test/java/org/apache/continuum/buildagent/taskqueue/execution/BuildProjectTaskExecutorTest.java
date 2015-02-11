package org.apache.continuum.buildagent.taskqueue.execution;

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

import org.apache.continuum.buildagent.build.execution.ContinuumAgentBuildExecutor;
import org.apache.continuum.buildagent.build.execution.manager.BuildAgentBuildExecutorManager;
import org.apache.continuum.buildagent.buildcontext.BuildContext;
import org.apache.continuum.buildagent.buildcontext.manager.BuildContextManager;
import org.apache.continuum.buildagent.configuration.BuildAgentConfigurationService;
import org.apache.continuum.buildagent.manager.BuildAgentManager;
import org.apache.continuum.buildagent.model.Installation;
import org.apache.continuum.buildagent.model.LocalRepository;
import org.apache.continuum.buildagent.utils.ContinuumBuildAgentUtil;
import org.apache.continuum.taskqueue.BuildProjectTask;
import org.apache.continuum.utils.build.BuildTrigger;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.execution.ContinuumBuildExecutorConstants;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.scm.ScmResult;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.spring.PlexusInSpringTestCase;
import org.codehaus.plexus.taskqueue.execution.TaskExecutor;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

public class BuildProjectTaskExecutorTest
    extends PlexusInSpringTestCase
{
    private BuildContextManager buildContextManager;

    private BuildAgentBuildExecutorManager buildAgentBuildExecutorManager;

    private BuildAgentConfigurationService buildAgentConfigurationService;

    private BuildAgentManager buildAgentManager;

    private BuildProjectTaskExecutor buildProjectExecutor;

    private BuildContext buildContext;

    private List<LocalRepository> localRepos;

    private Map<String, String> masterBuildEnvironments;

    private List<Installation> slaveBuildEnvironments = new ArrayList<Installation>();

    private ContinuumAgentBuildExecutor executor;

    private File workingDir;

    private MavenProject project;

    private File outputFile;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        buildContextManager = mock( BuildContextManager.class );
        buildAgentBuildExecutorManager = mock( BuildAgentBuildExecutorManager.class );
        buildAgentConfigurationService = mock( BuildAgentConfigurationService.class );
        buildAgentManager = mock( BuildAgentManager.class );
        executor = mock( ContinuumAgentBuildExecutor.class );

        buildProjectExecutor = (BuildProjectTaskExecutor) lookup( TaskExecutor.class, "build-agent" );
        buildProjectExecutor.setBuildAgentBuildExecutorManager( buildAgentBuildExecutorManager );
        buildProjectExecutor.setBuildAgentConfigurationService( buildAgentConfigurationService );
        buildProjectExecutor.setBuildContextManager( buildContextManager );
        buildProjectExecutor.setBuildAgentManager( buildAgentManager );

        buildContext = createBuildContext();

        localRepos = new ArrayList<LocalRepository>();
        LocalRepository localRepo = createLocalRepository( "temp", "/tmp/.m2/repository", "default" );
        localRepos.add( localRepo );
        localRepo = createLocalRepository( "default", "/home/user/.m2/repository", "default" );
        localRepos.add( localRepo );

        masterBuildEnvironments = new HashMap<String, String>();
        masterBuildEnvironments.put( "M2_HOME", "/tmp/apache-maven-2.2.1" );

        slaveBuildEnvironments = new ArrayList<Installation>();

        workingDir = new File( "/tmp/data/working-directory/project-test" );
        project = new MavenProject();
        outputFile = new File( "/tmp/data/build-output-directory/output.txt" );

        when( buildContextManager.getBuildContext( 1 ) ).thenReturn( buildContext );
        when( buildAgentManager.getEnvironments( 1, "maven2" ) ).thenReturn( masterBuildEnvironments );
        when( buildAgentConfigurationService.getAvailableInstallations() ).thenReturn( slaveBuildEnvironments );
        when( buildAgentConfigurationService.getLocalRepositories() ).thenReturn( localRepos );
        when( buildAgentManager.shouldBuild( anyMap() ) ).thenReturn( true );
        when( buildAgentBuildExecutorManager.getBuildExecutor(
            ContinuumBuildExecutorConstants.MAVEN_TWO_BUILD_EXECUTOR ) ).thenReturn( executor );
        when( buildAgentConfigurationService.getWorkingDirectory( 1 ) ).thenReturn( workingDir );
        when( executor.getMavenProject( any( File.class ), any( BuildDefinition.class ) ) ).thenReturn( project );
        when( buildAgentConfigurationService.getBuildOutputFile( 1 ) ).thenReturn( outputFile );
    }

    // CONTINUUM-2391
    // Note: The checking of the local repo set in the context is in ContinuumActionStub. If the
    // local repo path is incorrect, an exception will be thrown by the action stub.
    public void testBuildProjectLocalRepository()
        throws Exception
    {
        try
        {
            buildProjectExecutor.executeTask( createBuildProjectTask() );
        }
        catch ( Exception e )
        {
            fail( "An exception should not have been thrown!" );
        }

        assertBuilt();
    }

    public void testBuildProjectWithConfiguredInstallationsFromBuildAgent()
        throws Exception
    {
        Installation slaveBuildEnvironment = createInstallation( "M2_HOME", "/home/user/apache-maven-2.2.1" );
        slaveBuildEnvironments.add( slaveBuildEnvironment );
        slaveBuildEnvironment = createInstallation( "EXTRA_VAR", "/home/user/extra" );
        slaveBuildEnvironments.add( slaveBuildEnvironment );

        try
        {
            buildProjectExecutor.executeTask( createBuildProjectTask() );

            Map<String, String> environments = (Map<String, String>) buildContext.getActionContext().get(
                ContinuumBuildAgentUtil.KEY_ENVIRONMENTS );
            assertEquals( 2, environments.size() );
            assertTrue( environments.containsKey( "M2_HOME" ) );
            assertTrue( environments.containsKey( "EXTRA_VAR" ) );
            // shows that build agent's environment variables overwrites Continuum Master's environment variables
            assertEquals( "/home/user/apache-maven-2.2.1", environments.get( "M2_HOME" ) );
            assertEquals( "/home/user/extra", environments.get( "EXTRA_VAR" ) );
        }
        catch ( Exception e )
        {
            fail( "An exception should not have been thrown!" );
        }

        assertBuilt();
    }

    public void testBuildProjectWithNoConfiguredInstallationsFromBuildAgent()
        throws Exception
    {
        try
        {
            buildProjectExecutor.executeTask( createBuildProjectTask() );

            Map<String, String> environments = (Map<String, String>) buildContext.getActionContext().get(
                ContinuumBuildAgentUtil.KEY_ENVIRONMENTS );
            assertEquals( 1, environments.size() );
            assertTrue( environments.containsKey( "M2_HOME" ) );
            assertEquals( "/tmp/apache-maven-2.2.1", environments.get( "M2_HOME" ) );
        }
        catch ( Exception e )
        {
            fail( "An exception should not have been thrown!" );
        }

        assertBuilt();
    }

    private void assertBuilt()
        throws ContinuumException
    {
        verify( buildAgentManager ).startProjectBuild( 1 );
        verify( buildAgentManager ).returnBuildResult( anyMap() );
        verify( buildContextManager ).removeBuildContext( 1 );
    }

    private BuildProjectTask createBuildProjectTask()
    {
        BuildProjectTask task = new BuildProjectTask( 1, 1, new BuildTrigger( 1 ), "Test Project",
                                                      "Default Build Definition", new ScmResult(), 1 );
        return task;
    }

    private BuildContext createBuildContext()
    {
        BuildContext context = new BuildContext();
        context.setProjectId( 1 );
        context.setProjectVersion( "1.0-SNAPSHOT" );
        context.setBuildDefinitionId( 1 );
        context.setBuildFile( "pom.xml" );
        context.setExecutorId( ContinuumBuildExecutorConstants.MAVEN_TWO_BUILD_EXECUTOR );
        context.setGoals( "clean intall" );
        context.setArguments( "--batch --non-recursive" );
        context.setScmUrl( "scm:svn:http://svn.example.com/repos/dummy/trunk" );
        context.setScmUsername( "" );
        context.setScmPassword( "" );
        context.setBuildFresh( false );
        context.setProjectGroupId( 1 );
        context.setProjectGroupName( "Test Project Group" );
        context.setScmRootAddress( "scm:svn:http://svn.example.com/repos/dummy" );
        context.setScmRootId( 1 );
        context.setProjectName( "Test Project" );
        context.setProjectState( 1 );
        context.setTrigger( 1 );
        context.setUsername( "" );
        context.setLocalRepository( "default" );
        context.setBuildNumber( 1 );

        ScmResult scmResult = new ScmResult();
        scmResult.setSuccess( true );

        context.setScmResult( scmResult );
        context.setLatestUpdateDate( Calendar.getInstance().getTime() );
        context.setBuildAgentUrl( "http://localhost:8181/continuum-buildagent/xmlrpc" );
        context.setMaxExecutionTime( 7200 );
        context.setBuildDefinitionLabel( "Default Build Definition" );
        context.setScmTag( "scm:svn:http://svn.example.com/repos/dummy/tags" );

        return context;
    }

    private LocalRepository createLocalRepository( String name, String locataion, String layout )
    {
        LocalRepository localRepository = new LocalRepository();
        localRepository.setName( name );
        localRepository.setLocation( locataion );
        localRepository.setLayout( layout );
        return localRepository;
    }

    private Installation createInstallation( String varName, String varValue )
    {
        Installation installation = new Installation();
        installation.setVarName( varName );
        installation.setVarValue( varValue );
        return installation;
    }
}