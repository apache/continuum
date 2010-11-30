package org.apache.continuum.xmlrpc.server;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.continuum.release.distributed.manager.DistributedReleaseManager;
import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.release.ContinuumReleaseManager;
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

        continuumService = new ContinuumServiceImplStub();
        continuum = context.mock( Continuum.class );
        continuumService.setContinuum( continuum );

        distributedReleaseManager = context.mock( DistributedReleaseManager.class );
        releaseManager = context.mock( ContinuumReleaseManager.class );
        configurationService = context.mock( ConfigurationService.class );

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

    private Map<String, Object> getListenerMap()
    {
        Map<String, Object> map = new HashMap<String, Object>();
 
        map.put( "release-phases", Arrays.asList( "incomplete-phase" ) );
        map.put( "completed-release-phases", Arrays.asList( "completed-phase" ) );
        return map;
    }
}
