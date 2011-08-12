package org.apache.continuum.xmlrpc.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.continuum.builder.distributed.manager.DistributedBuildManager;
import org.apache.continuum.model.project.ProjectScmRoot;
import org.apache.continuum.release.distributed.manager.DistributedReleaseManager;
import org.apache.continuum.xmlrpc.utils.BuildTrigger;
import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.release.ContinuumReleaseManager;
import org.apache.maven.continuum.xmlrpc.project.BuildDefinition;
import org.apache.maven.continuum.xmlrpc.project.ContinuumProjectState;
import org.apache.maven.continuum.xmlrpc.project.ReleaseListenerSummary;
import org.apache.maven.continuum.xmlrpc.server.ContinuumServiceImpl;
import org.codehaus.plexus.spring.PlexusInSpringTestCase;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit3.JUnit3Mockery;
import org.jmock.lib.legacy.ClassImposteriser;

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

        continuumService = new ContinuumServiceImplStub();
        continuum = context.mock( Continuum.class );
        continuumService.setContinuum( continuum );
        continuumService.setDistributedBuildManager( distributedBuildManager );

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
        org.apache.maven.continuum.model.project.BuildDefinition buildDefinition = new org.apache.maven.continuum.model.project.BuildDefinition();
        
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
        });
    
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
    
    public void testGetBuildAgentUrl() throws Exception
    {
        context.checking( new Expectations() 
        {
            {
                one( continuum ).getConfiguration();
                will( returnValue( configurationService ) );
                
                one( configurationService ).isDistributedBuildEnabled();
                will( returnValue ( true ) );
                
                one( distributedBuildManager ).getBuildAgentUrl( 1 );
                will( returnValue( "http://localhost:8181/continuum-buildagent/xmlrpc" ) );
            }
        });
        String buildAgentUrl = continuumService.getBuildAgentUrl( 1 );
        assertEquals( "http://localhost:8181/continuum-buildagent/xmlrpc", buildAgentUrl );

        context.assertIsSatisfied();
    }
    
    public void testGetBuildAgentUrlNotSupported() throws Exception
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
            String buildAgentUrl = continuumService.getBuildAgentUrl( 1 );
            fail ( "ContinuumException is expected to occur here." ); 
        } 
        catch ( ContinuumException e )
        {
            ; //pass
        }
        context.assertIsSatisfied();
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
        private static final long serialVersionUID = 1L;

        @Override
        public List<Project> getProjects()
        {
            throw new RuntimeException( "Can't call getProjects as it will throw JDODetachedFieldAccessException" );
        }
    }
}
