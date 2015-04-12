package org.apache.continuum.xmlrpc.server;

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

import org.apache.continuum.builder.distributed.manager.DistributedBuildManager;
import org.apache.continuum.configuration.BuildAgentGroupConfiguration;
import org.apache.continuum.model.project.ProjectScmRoot;
import org.apache.continuum.release.distributed.manager.DistributedReleaseManager;
import org.apache.continuum.xmlrpc.utils.BuildTrigger;
import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.PlexusSpringTestCase;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.installation.InstallationException;
import org.apache.maven.continuum.installation.InstallationService;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.release.ContinuumReleaseManager;
import org.apache.maven.continuum.xmlrpc.project.BuildAgentConfiguration;
import org.apache.maven.continuum.xmlrpc.project.BuildDefinition;
import org.apache.maven.continuum.xmlrpc.project.ContinuumProjectState;
import org.apache.maven.continuum.xmlrpc.project.ProjectGroupSummary;
import org.apache.maven.continuum.xmlrpc.project.ReleaseListenerSummary;
import org.apache.maven.continuum.xmlrpc.server.ContinuumServiceImpl;
import org.apache.maven.continuum.xmlrpc.system.Installation;
import org.codehaus.plexus.redback.role.RoleManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ContinuumServiceImplTest
    extends PlexusSpringTestCase
{
    private ContinuumServiceImpl continuumService;

    private Continuum continuum;

    private DistributedReleaseManager distributedReleaseManager;

    private ContinuumReleaseManager releaseManager;

    private DistributedBuildManager distributedBuildManager;

    private ConfigurationService configurationService;

    private Project project;

    private Map<String, Object> params;

    private RoleManager roleManager;

    @Before
    public void setUp()
        throws Exception
    {
        distributedReleaseManager = mock( DistributedReleaseManager.class );
        releaseManager = mock( ContinuumReleaseManager.class );
        configurationService = mock( ConfigurationService.class );
        distributedBuildManager = mock( DistributedBuildManager.class );
        roleManager = mock( RoleManager.class );

        continuumService = new ContinuumServiceImplStub();
        continuum = mock( Continuum.class );
        continuumService.setContinuum( continuum );
        continuumService.setDistributedBuildManager( distributedBuildManager );
        continuumService.setRoleManager( roleManager );

        ProjectGroup projectGroup = new ProjectGroup();
        projectGroup.setName( "test-group" );

        project = new Project();
        project.setId( 1 );
        project.setProjectGroup( projectGroup );
        project.setVersion( "1.0-SNAPSHOT" );
        project.setArtifactId( "continuum-test" );
        project.setScmUrl( "scm:svn:http://svn.test.org/repository/project" );
    }

    @Test
    public void testGetReleasePluginParameters()
        throws Exception
    {
        params = new HashMap<String, Object>();
        params.put( "scm-tag", "" );
        params.put( "scm-tagbase", "" );

        when( continuum.getProject( 1 ) ).thenReturn( project );
        when( continuum.getConfiguration() ).thenReturn( configurationService );
        when( configurationService.isDistributedBuildEnabled() ).thenReturn( true );
        when( continuum.getDistributedReleaseManager() ).thenReturn( distributedReleaseManager );
        when( distributedReleaseManager.getReleasePluginParameters( 1, "pom.xml" ) ).thenReturn( params );
        when( continuum.getReleaseManager() ).thenReturn( releaseManager );

        Map<String, Object> releaseParams = continuumService.getReleasePluginParameters( 1 );
        assertEquals( "continuum-test-1.0", releaseParams.get( "scm-tag" ) );
        assertEquals( "http://svn.test.org/repository/project/tags", releaseParams.get( "scm-tagbase" ) );

        verify( releaseManager ).sanitizeTagName( "scm:svn:http://svn.test.org/repository/project",
                                                  "continuum-test-1.0" );
    }

    @Test
    public void testGetListenerWithDistributedBuilds()
        throws Exception
    {
        Map map = getListenerMap();

        when( continuum.getProject( 1 ) ).thenReturn( project );
        when( continuum.getConfiguration() ).thenReturn( configurationService );
        when( configurationService.isDistributedBuildEnabled() ).thenReturn( true );
        when( continuum.getDistributedReleaseManager() ).thenReturn( distributedReleaseManager );
        when( distributedReleaseManager.getListener( "releaseId-1" ) ).thenReturn( map );

        ReleaseListenerSummary summary = continuumService.getListener( 1, "releaseId-1" );

        assertNotNull( summary );
        assertEquals( "incomplete-phase", summary.getPhases().get( 0 ) );
        assertEquals( "completed-phase", summary.getCompletedPhases().get( 0 ) );
    }

    @Test
    public void testPopulateBuildDefinition()
        throws Exception
    {
        ContinuumServiceImplStub continuumServiceStub = new ContinuumServiceImplStub();

        BuildDefinition buildDef = createBuildDefinition();
        org.apache.maven.continuum.model.project.BuildDefinition buildDefinition =
            new org.apache.maven.continuum.model.project.BuildDefinition();

        buildDefinition = continuumServiceStub.getBuildDefinition( buildDef, buildDefinition );

        assertEquals( buildDef.getArguments(), buildDefinition.getArguments() );
        assertEquals( buildDef.getBuildFile(), buildDefinition.getBuildFile() );
        assertEquals( buildDef.getDescription(), buildDefinition.getDescription() );
        assertEquals( buildDef.getGoals(), buildDefinition.getGoals() );
        assertEquals( buildDef.getType(), buildDefinition.getType() );
        assertEquals( buildDef.isAlwaysBuild(), buildDefinition.isAlwaysBuild() );
        assertEquals( buildDef.isBuildFresh(), buildDefinition.isBuildFresh() );
        assertEquals( buildDef.isDefaultForProject(), buildDefinition.isDefaultForProject() );
    }

    @Test
    public void testBuildProjectWithBuildTrigger()
        throws Exception
    {
        final ProjectGroup projectGroup = new ProjectGroup();
        projectGroup.setName( "test-group" );

        BuildTrigger buildTrigger = new BuildTrigger();
        buildTrigger.setTrigger( ContinuumProjectState.TRIGGER_FORCED );
        buildTrigger.setTriggeredBy( "username" );

        BuildDefinition buildDef = createBuildDefinition();
        buildDef.setId( 1 );

        when( continuum.getProject( project.getId() ) ).thenReturn( project );
        when( continuum.getProjectGroupByProjectId( project.getId() ) ).thenReturn( projectGroup );

        int result = continuumService.buildProject( project.getId(), buildDef.getId(), buildTrigger );

        assertEquals( 0, result );
    }

    @Test
    public void testGetProjectScmRootByProjectGroup()
        throws Exception
    {
        final ProjectGroup projectGroup = new ProjectGroup();
        projectGroup.setName( "test-group" );
        projectGroup.setId( 1 );

        final List<ProjectScmRoot> scmRoots = new ArrayList<ProjectScmRoot>();

        ProjectScmRoot scmRoot = new ProjectScmRoot();
        scmRoot.setState( 1 );
        scmRoot.setOldState( 3 );
        scmRoot.setScmRootAddress( "address1" );
        scmRoot.setProjectGroup( projectGroup );
        scmRoots.add( scmRoot );

        scmRoot = new ProjectScmRoot();
        scmRoot.setState( 2 );
        scmRoot.setOldState( 4 );
        scmRoot.setScmRootAddress( "address2" );
        scmRoot.setProjectGroup( projectGroup );
        scmRoots.add( scmRoot );

        when( continuum.getProjectScmRootByProjectGroup( projectGroup.getId() ) ).thenReturn( scmRoots );
        when( continuum.getProjectGroup( projectGroup.getId() ) ).thenReturn( projectGroup );

        List<org.apache.maven.continuum.xmlrpc.project.ProjectScmRoot> projectScmRoots =
            continuumService.getProjectScmRootByProjectGroup( projectGroup.getId() );

        assertEquals( 2, projectScmRoots.size() );
        assertEquals( 1, projectScmRoots.get( 0 ).getState() );
        assertEquals( 2, projectScmRoots.get( 1 ).getState() );
    }

    @Test
    public void testGetProjectScmRootByProject()
        throws Exception
    {
        final ProjectGroup projectGroup = new ProjectGroupStub();
        projectGroup.setName( "test-group" );
        projectGroup.setId( 1 );

        final int projectId = 1;

        final ProjectScmRoot scmRoot = new ProjectScmRoot();
        scmRoot.setState( 1 );
        scmRoot.setOldState( 3 );
        scmRoot.setScmRootAddress( "address1" );
        scmRoot.setProjectGroup( projectGroup );

        when( continuum.getProjectScmRootByProject( projectId ) ).thenReturn( scmRoot );

        org.apache.maven.continuum.xmlrpc.project.ProjectScmRoot projectScmRoot =
            continuumService.getProjectScmRootByProject( projectId );

        assertNotNull( projectScmRoot );
        assertEquals( 1, projectScmRoot.getState() );
        assertEquals( 3, projectScmRoot.getOldState() );
        assertEquals( "address1", projectScmRoot.getScmRootAddress() );
    }

    @Test
    public void testGetBuildAgentUrl()
        throws Exception
    {
        String expectedUrl = "http://localhost:8181/continuum-buildagent/xmlrpc";

        when( continuum.getConfiguration() ).thenReturn( configurationService );
        when( configurationService.isDistributedBuildEnabled() ).thenReturn( true );
        when( distributedBuildManager.getBuildAgentUrl( 1, 1 ) ).thenReturn( expectedUrl );

        String buildAgentUrl = continuumService.getBuildAgentUrl( 1, 1 );

        assertEquals( expectedUrl, buildAgentUrl );
    }

    @Test
    public void testGetBuildAgentUrlNotSupported()
        throws Exception
    {
        when( continuum.getConfiguration() ).thenReturn( configurationService );
        when( configurationService.isDistributedBuildEnabled() ).thenReturn( false );

        try
        {
            continuumService.getBuildAgentUrl( 1, 1 );
            fail( "ContinuumException is expected to occur here." );
        }
        catch ( ContinuumException e )
        {
            //pass
        }
    }

    @Test
    public void testGetNonExistingBuildAgentGroup()
        throws Exception
    {
        String groupName = "Agent Group Name";
        when( continuum.getConfiguration() ).thenReturn( configurationService );
        when( configurationService.getBuildAgentGroup( groupName ) ).thenReturn( null );

        int result = continuumService.removeBuildAgentGroup( groupName );

        assertEquals( 0, result );
    }

    @Test
    public void testRemoveNonExistingBuildAgentGroup()
        throws Exception
    {
        String groupName = "Agent Group Name";
        when( continuum.getConfiguration() ).thenReturn( configurationService );
        when( configurationService.getBuildAgentGroup( groupName ) ).thenReturn( null );

        continuumService.removeBuildAgentGroup( groupName );

        verify( configurationService, never() ).removeBuildAgentGroup( any( BuildAgentGroupConfiguration.class ) );
    }

    @Test
    public void testGetBuildAgentsWithInstallations()
        throws Exception
    {
        final List<org.apache.continuum.configuration.BuildAgentConfiguration> buildAgents =
            new ArrayList<org.apache.continuum.configuration.BuildAgentConfiguration>();

        org.apache.continuum.configuration.BuildAgentConfiguration buildAgent =
            new org.apache.continuum.configuration.BuildAgentConfiguration();

        String buildAgentUrl = "http://localhost:8080/xmlrpc";

        buildAgent.setUrl( buildAgentUrl );
        buildAgent.setEnabled( true );
        buildAgents.add( buildAgent );

        org.apache.continuum.configuration.BuildAgentConfiguration buildAgent2 =
            new org.apache.continuum.configuration.BuildAgentConfiguration();
        buildAgent2.setUrl( "http://localhost:8181/xmlrpc" );
        buildAgent2.setEnabled( false );
        buildAgents.add( buildAgent2 );

        final List<org.apache.maven.continuum.model.system.Installation> buildAgentInstallations =
            new ArrayList<org.apache.maven.continuum.model.system.Installation>();

        org.apache.maven.continuum.model.system.Installation buildAgentInstallation =
            new org.apache.maven.continuum.model.system.Installation();
        buildAgentInstallation.setInstallationId( 1 );
        buildAgentInstallation.setName( "JDK 6" );
        buildAgentInstallation.setType( "jdk" );
        buildAgentInstallation.setVarName( "JAVA_HOME" );
        buildAgentInstallation.setVarValue( "/opt/java" );
        buildAgentInstallations.add( buildAgentInstallation );

        when( continuum.getConfiguration() ).thenReturn( configurationService );
        when( configurationService.getBuildAgents() ).thenReturn( buildAgents );
        when( distributedBuildManager.getBuildAgentPlatform( buildAgentUrl ) ).thenReturn( "Linux" );
        when( distributedBuildManager.getAvailableInstallations( buildAgentUrl ) ).thenReturn(
            buildAgentInstallations );

        List<BuildAgentConfiguration> agents = continuumService.getBuildAgentsWithInstallations();

        assertEquals( 1, agents.size() );
        BuildAgentConfiguration agent = agents.get( 0 );
        assertEquals( buildAgentUrl, agent.getUrl() );
        assertEquals( "Linux", agent.getPlatform() );
        assertEquals( 1, agent.getInstallations().size() );
    }

    @Test
    public void testAddProjectGroupWithPunctuation()
        throws Exception
    {
        String name = "Test :: Group Name (with punctuation)";
        String groupId = "com.example.long-group-id";
        String description = "Description";
        ProjectGroup group = createProjectGroup( name, groupId, description );

        when( continuum.getProjectGroupByGroupId( groupId ) ).thenReturn( group );

        ProjectGroupSummary groupSummary = continuumService.addProjectGroup( name, groupId, description );

        assertEquals( name, groupSummary.getName() );
        assertEquals( groupId, groupSummary.getGroupId() );
        assertEquals( description, groupSummary.getDescription() );
        verify( continuum ).addProjectGroup( group );
    }

    @Test
    public void testEditProjectGroupWithPunctuation()
        throws Exception
    {
        int projectGroupId = 1;
        String origName = "name", origGroupId = "groupId", origDescription = "description";
        final String newName = "Test :: Group Name (with punctuation)";
        final String newGroupId = "com.example.long-group-id";
        final String newDescription = "Description";

        List<String> roles = Arrays.asList( "project-administrator", "project-developer", "project-user" );

        ProjectGroup unsavedGroup = createProjectGroup( origName, origGroupId, origDescription );
        ProjectGroup savedGroup = createProjectGroup( projectGroupId, origName, origGroupId, origDescription );
        ProjectGroup editedGroup = createProjectGroup( projectGroupId, newName, newGroupId, newDescription );

        when( continuum.getProjectGroupByGroupId( origGroupId ) ).thenReturn( savedGroup );

        ProjectGroupSummary groupSummary = continuumService.addProjectGroup( origName, origGroupId, origDescription );

        verify( continuum ).addProjectGroup( unsavedGroup );

        groupSummary.setName( newName );
        groupSummary.setGroupId( newGroupId );
        groupSummary.setDescription( newDescription );

        when( continuum.getProjectGroupWithProjects( projectGroupId ) ).thenReturn( savedGroup );
        when( continuum.getProjectGroup( projectGroupId ) ).thenReturn( savedGroup, savedGroup, editedGroup );

        continuumService.updateProjectGroup( groupSummary );

        verify( continuum ).updateProjectGroup( editedGroup );
        for ( String role : roles )
        {
            verify( roleManager ).updateRole( role, origName, newName );
        }
    }

    @Test
    public void testInstallationEnvironmentVariableWithOtherOptions()
        throws ContinuumException, InstallationException
    {
        Installation target = new Installation();
        target.setName( "name" );
        target.setType( "envvar" );
        target.setVarName( "JAVA_OPTS" );
        target.setVarValue( "-XX:+CompressedOops" );

        org.apache.maven.continuum.model.system.Installation returned =
            new org.apache.maven.continuum.model.system.Installation();
        returned.setName( "name" );
        returned.setType( "envvar" );
        returned.setVarName( "JAVA_OPTS" );
        returned.setVarValue( "-XX:+CompressedOops" );

        InstallationService installationService = mock( InstallationService.class );
        when( continuum.getInstallationService() ).thenReturn( installationService );
        // Need to return a value for the xml mapper
        when( installationService.add( any( org.apache.maven.continuum.model.system.Installation.class ) ) ).thenReturn(
            returned
        );

        Installation marshaledResult = continuumService.addInstallation( target );

        ArgumentCaptor<org.apache.maven.continuum.model.system.Installation> arg =
            ArgumentCaptor.forClass( org.apache.maven.continuum.model.system.Installation.class );
        verify( installationService ).add( arg.capture() );

        // verify properties are correct for added installation
        org.apache.maven.continuum.model.system.Installation added = arg.getValue();
        assertEquals( target.getName(), added.getName() );
        assertEquals( target.getType(), added.getType() );
        assertEquals( target.getVarName(), added.getVarName() );
        assertEquals( target.getVarValue(), added.getVarValue() );

        // verify properties for serialized result
        assertEquals( target.getName(), marshaledResult.getName() );
        assertEquals( target.getType(), marshaledResult.getType() );
        assertEquals( target.getVarName(), marshaledResult.getVarName() );
        assertEquals( target.getVarValue(), marshaledResult.getVarValue() );
    }

    private static ProjectGroup createProjectGroup( String name, String groupId, String description )
    {
        ProjectGroup group = new ProjectGroup();
        group.setName( name );
        group.setGroupId( groupId );
        group.setDescription( description );
        return group;
    }

    private static ProjectGroup createProjectGroup( int projectGroupId, String name, String groupId,
                                                    String description )
    {
        ProjectGroup group = createProjectGroup( name, groupId, description );
        group.setId( projectGroupId );
        return group;
    }

    private BuildDefinition createBuildDefinition()
    {
        BuildDefinition buildDef = new BuildDefinition();
        buildDef.setArguments( "--batch-mode -P!dev" );
        buildDef.setBuildFile( "pom.xml" );
        buildDef.setType( "maven2" );
        buildDef.setBuildFresh( false );
        buildDef.setAlwaysBuild( true );
        buildDef.setDefaultForProject( true );
        buildDef.setGoals( "clean install" );
        buildDef.setDescription( "Test Build Definition" );

        return buildDef;
    }

    private Map<String, Object> getListenerMap()
    {
        Map<String, Object> map = new HashMap<String, Object>();

        map.put( "release-phases", Arrays.asList( "incomplete-phase" ) );
        map.put( "completed-release-phases", Arrays.asList( "completed-phase" ) );
        return map;
    }

    public class ProjectGroupStub
        extends ProjectGroup
    {
        @Override
        public List<Project> getProjects()
        {
            throw new RuntimeException( "Can't call getProjects as it will throw JDODetachedFieldAccessException" );
        }
    }
}
