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
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.release.ContinuumReleaseManager;
import org.apache.maven.continuum.xmlrpc.project.BuildAgentConfiguration;
import org.apache.maven.continuum.xmlrpc.project.BuildDefinition;
import org.apache.maven.continuum.xmlrpc.project.ContinuumProjectState;
import org.apache.maven.continuum.xmlrpc.project.ProjectGroupSummary;
import org.apache.maven.continuum.xmlrpc.project.ReleaseListenerSummary;
import org.apache.maven.continuum.xmlrpc.server.ContinuumServiceImpl;
import org.codehaus.plexus.redback.role.RoleManager;
import org.codehaus.plexus.spring.PlexusInSpringTestCase;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit3.JUnit3Mockery;
import org.jmock.lib.legacy.ClassImposteriser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContinuumServiceImplTest
    extends PlexusInSpringTestCase
{
    private ContinuumServiceImpl continuumService;

    private Mockery context;

    private Continuum continuum;

    private DistributedReleaseManager distributedReleaseManager;

    private ContinuumReleaseManager releaseManager;

    private DistributedBuildManager distributedBuildManager;

    private ConfigurationService configurationService;

    private Project project;

    private Map<String, Object> params;

    private RoleManager roleManager;

    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();

        context = new JUnit3Mockery();
        context.setImposteriser( ClassImposteriser.INSTANCE );

        distributedReleaseManager = context.mock( DistributedReleaseManager.class );
        releaseManager = context.mock( ContinuumReleaseManager.class );
        configurationService = context.mock( ConfigurationService.class );
        distributedBuildManager = context.mock( DistributedBuildManager.class );
        roleManager = context.mock( RoleManager.class );

        continuumService = new ContinuumServiceImplStub();
        continuum = context.mock( Continuum.class );
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

    public void testGetReleasePluginParameters()
        throws Exception
    {
        params = new HashMap<String, Object>();
        params.put( "scm-tag", "" );
        params.put( "scm-tagbase", "" );

        context.checking( new Expectations()
        {
            {
                one( continuum ).getProject( 1 );
                will( returnValue( project ) );

                one( continuum ).getConfiguration();
                will( returnValue( configurationService ) );

                one( configurationService ).isDistributedBuildEnabled();
                will( returnValue( true ) );

                one( continuum ).getDistributedReleaseManager();
                will( returnValue( distributedReleaseManager ) );

                one( distributedReleaseManager ).getReleasePluginParameters( 1, "pom.xml" );
                will( returnValue( params ) );

                one( continuum ).getReleaseManager();
                will( returnValue( releaseManager ) );

                one( releaseManager ).sanitizeTagName( "scm:svn:http://svn.test.org/repository/project",
                                                       "continuum-test-1.0" );
            }
        } );

        Map<String, Object> releaseParams = continuumService.getReleasePluginParameters( 1 );
        assertEquals( "continuum-test-1.0", releaseParams.get( "scm-tag" ) );
        assertEquals( "http://svn.test.org/repository/project/tags", releaseParams.get( "scm-tagbase" ) );

        context.assertIsSatisfied();
    }

    public void testGetListenerWithDistributedBuilds()
        throws Exception
    {
        final Map map = getListenerMap();

        context.checking( new Expectations()
        {
            {
                one( continuum ).getProject( 1 );
                will( returnValue( project ) );

                one( continuum ).getConfiguration();
                will( returnValue( configurationService ) );

                one( configurationService ).isDistributedBuildEnabled();
                will( returnValue( true ) );

                one( continuum ).getDistributedReleaseManager();
                will( returnValue( distributedReleaseManager ) );

                one( distributedReleaseManager ).getListener( "releaseId-1" );
                will( returnValue( map ) );
            }
        } );

        ReleaseListenerSummary summary = continuumService.getListener( 1, "releaseId-1" );
        assertNotNull( summary );
        assertEquals( "incomplete-phase", summary.getPhases().get( 0 ) );
        assertEquals( "completed-phase", summary.getCompletedPhases().get( 0 ) );
    }

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

        context.checking( new Expectations()
        {
            {
                atLeast( 1 ).of( continuum ).getProject( project.getId() );
                will( returnValue( project ) );

                atLeast( 1 ).of( continuum ).getProjectGroupByProjectId( project.getId() );
                will( returnValue( projectGroup ) );
            }
        } );

        int result = continuumService.buildProject( project.getId(), buildDef.getId(), buildTrigger );
        assertEquals( 0, result );

    }

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

        context.checking( new Expectations()
        {
            {
                atLeast( 1 ).of( continuum ).getProjectScmRootByProjectGroup( projectGroup.getId() );
                will( returnValue( scmRoots ) );

                atLeast( 1 ).of( continuum ).getProjectGroup( projectGroup.getId() );
                will( returnValue( projectGroup ) );
            }
        } );

        List<org.apache.maven.continuum.xmlrpc.project.ProjectScmRoot> projectScmRoots =
            continuumService.getProjectScmRootByProjectGroup( projectGroup.getId() );
        assertEquals( 2, projectScmRoots.size() );
        assertEquals( 1, projectScmRoots.get( 0 ).getState() );
        assertEquals( 2, projectScmRoots.get( 1 ).getState() );
    }

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

        context.checking( new Expectations()
        {
            {
                atLeast( 1 ).of( continuum ).getProjectScmRootByProject( projectId );
                will( returnValue( scmRoot ) );
            }
        } );

        org.apache.maven.continuum.xmlrpc.project.ProjectScmRoot projectScmRoot =
            continuumService.getProjectScmRootByProject( projectId );
        assertNotNull( projectScmRoot );
        assertEquals( 1, projectScmRoot.getState() );
        assertEquals( 3, projectScmRoot.getOldState() );
        assertEquals( "address1", projectScmRoot.getScmRootAddress() );
    }

    public void testGetBuildAgentUrl()
        throws Exception
    {
        context.checking( new Expectations()
        {
            {
                one( continuum ).getConfiguration();
                will( returnValue( configurationService ) );

                one( configurationService ).isDistributedBuildEnabled();
                will( returnValue( true ) );

                one( distributedBuildManager ).getBuildAgentUrl( 1, 1 );
                will( returnValue( "http://localhost:8181/continuum-buildagent/xmlrpc" ) );
            }
        } );
        String buildAgentUrl = continuumService.getBuildAgentUrl( 1, 1 );
        assertEquals( "http://localhost:8181/continuum-buildagent/xmlrpc", buildAgentUrl );

        context.assertIsSatisfied();
    }

    public void testGetBuildAgentUrlNotSupported()
        throws Exception
    {
        context.checking( new Expectations()
        {
            {
                one( continuum ).getConfiguration();
                will( returnValue( configurationService ) );

                one( configurationService ).isDistributedBuildEnabled();
                will( returnValue( false ) );
            }
        } );

        try
        {
            continuumService.getBuildAgentUrl( 1, 1 );
            fail( "ContinuumException is expected to occur here." );
        }
        catch ( ContinuumException e )
        {
            //pass
        }
        context.assertIsSatisfied();
    }

    public void testGetNonExistingBuildAgentGroup()
        throws Exception
    {
        context.checking( new Expectations()
        {
            {
                one( continuum ).getConfiguration();
                will( returnValue( configurationService ) );

                one( configurationService ).getBuildAgentGroup( "Agent Group Name" );
                will( returnValue( null ) );
            }
        } );
        int result = continuumService.removeBuildAgentGroup( "Agent Group Name" );
        assertEquals( 0, result );

        context.assertIsSatisfied();
    }

    public void testRemoveNonExistingBuildAgentGroup()
        throws Exception
    {
        context.checking( new Expectations()
        {
            {
                one( continuum ).getConfiguration();
                will( returnValue( configurationService ) );

                one( configurationService ).getBuildAgentGroup( "Agent Group Name" );
                will( returnValue( null ) );

                never( configurationService ).removeBuildAgentGroup( with( any(
                    BuildAgentGroupConfiguration.class ) ) );
            }
        } );

        continuumService.removeBuildAgentGroup( "Agent Group Name" );
        context.assertIsSatisfied();
    }

    public void testGetBuildAgentsWithInstallations()
        throws Exception
    {
        final List<org.apache.continuum.configuration.BuildAgentConfiguration> buildAgents =
            new ArrayList<org.apache.continuum.configuration.BuildAgentConfiguration>();

        org.apache.continuum.configuration.BuildAgentConfiguration buildAgent =
            new org.apache.continuum.configuration.BuildAgentConfiguration();
        buildAgent.setUrl( "http://localhost:8080/xmlrpc" );
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

        context.checking( new Expectations()
        {
            {
                one( continuum ).getConfiguration();
                will( returnValue( configurationService ) );

                one( configurationService ).getBuildAgents();
                will( returnValue( buildAgents ) );

                one( distributedBuildManager ).getBuildAgentPlatform( "http://localhost:8080/xmlrpc" );
                will( returnValue( "Linux" ) );

                one( distributedBuildManager ).getAvailableInstallations( "http://localhost:8080/xmlrpc" );
                will( returnValue( buildAgentInstallations ) );
            }
        } );
        List<BuildAgentConfiguration> agents = continuumService.getBuildAgentsWithInstallations();
        assertEquals( 1, agents.size() );
        BuildAgentConfiguration agent = agents.get( 0 );
        assertEquals( "http://localhost:8080/xmlrpc", agent.getUrl() );
        assertEquals( "Linux", agent.getPlatform() );
        assertEquals( 1, agent.getInstallations().size() );

        context.assertIsSatisfied();
    }

    public void testAddProjectGroupWithPunctuation()
        throws Exception
    {
        final String name = "Test :: Group Name (with punctuation)";
        final String groupId = "com.example.long-group-id";
        final String description = "Description";

        context.checking( new Expectations()
        {
            {
                ProjectGroup group = createProjectGroup( name, groupId, description );
                one( continuum ).addProjectGroup( group );

                group = createProjectGroup( name, groupId, description );
                one( continuum ).getProjectGroupByGroupId( groupId );
                will( returnValue( group ) );
//
//                one( continuum ).getProjectGroup( projectGroupId );
//                will( returnValue( group ) );
            }
        } );

        ProjectGroupSummary group = continuumService.addProjectGroup( name, groupId, description );
        assertEquals( name, group.getName() );
        assertEquals( groupId, group.getGroupId() );
        assertEquals( description, group.getDescription() );

        context.assertIsSatisfied();
    }

    public void testEditProjectGroupWithPunctuation()
        throws Exception
    {
        final String newName = "Test :: Group Name (with punctuation)";
        final String newGroupId = "com.example.long-group-id";
        final String newDescription = "Description";
        final int projectGroupId = 1;

        context.checking( new Expectations()
        {
            {
                ProjectGroup group = createProjectGroup( "name", "groupId", "description" );

                one( continuum ).addProjectGroup( group );

                group = createProjectGroup( projectGroupId );
                one( continuum ).getProjectGroupByGroupId( "groupId" );
                will( returnValue( group ) );

                one( continuum ).getProjectGroupWithProjects( projectGroupId );
                will( returnValue( group ) );

                for ( String role : Arrays.asList( "project-administrator", "project-developer", "project-user" ) )
                {
                    one( roleManager ).updateRole( role, "name", newName );
                }

                ProjectGroup newProjectGroup = createProjectGroup( projectGroupId, newName, newGroupId,
                                                                   newDescription );
                one( continuum ).updateProjectGroup( newProjectGroup );

                exactly( 3 ).of( continuum ).getProjectGroup( projectGroupId );
                onConsecutiveCalls( returnValue( group ), returnValue( group ), returnValue( newProjectGroup ) );
            }
        } );

        ProjectGroupSummary group = continuumService.addProjectGroup( "name", "groupId", "description" );
        group.setName( newName );
        group.setGroupId( newGroupId );
        group.setDescription( newDescription );

        continuumService.updateProjectGroup( group );

        context.assertIsSatisfied();
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

    private static ProjectGroup createProjectGroup( int projectGroupId )
    {
        return createProjectGroup( projectGroupId, "name", "groupId", "description" );
    }

    private BuildDefinition createBuildDefinition()
    {
        BuildDefinition buildDef = new BuildDefinition();
        buildDef.setArguments( "--batch-mode" );
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
