package org.apache.maven.continuum.web.action;

import java.io.File;
import java.util.HashMap;

import org.apache.continuum.builder.distributed.manager.DistributedBuildManager;
import org.apache.continuum.buildmanager.BuildsManager;
import org.apache.continuum.taskqueue.BuildProjectTask;
import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.web.action.stub.BuildResultActionStub;
import org.apache.maven.continuum.xmlrpc.project.ContinuumProjectState;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class BuildResultActionTest
    extends MockObjectTestCase
{
    private BuildResultActionStub action;

    private Mock continuum;

    private Mock configurationService;

    private Mock distributedBuildManager;

    private Mock buildsManager;

    protected void setUp()
    {
        action = new BuildResultActionStub();
        continuum = mock( Continuum.class );
        configurationService = mock( ConfigurationService.class );
        distributedBuildManager = mock( DistributedBuildManager.class );
        buildsManager = mock( BuildsManager.class );

        action.setContinuum( (Continuum) continuum.proxy() );
        action.setDistributedBuildManager( (DistributedBuildManager) distributedBuildManager.proxy() );
    }

    public void testViewPreviousBuild()
        throws Exception
    {
        Project project = createProject( "stub-project" );
        BuildResult buildResult = createBuildResult( project );

        continuum.expects( once() ).method( "getProject" ).will( returnValue( project ) );
        continuum.expects( once() ).method( "getBuildResult" ).will( returnValue( buildResult ) );
        continuum.expects( atLeastOnce() ).method( "getConfiguration" ).will( returnValue( (ConfigurationService) configurationService.proxy()  ) );
        configurationService.expects( once() ).method( "isDistributedBuildEnabled" ).will( returnValue( false ) );
        configurationService.expects( once() ).method( "getTestReportsDirectory" ).will( returnValue( new File( "testReportsDir" ) ) );
        continuum.expects( once() ).method( "getChangesSinceLastSuccess" ).will( returnValue( null ) );
        configurationService.expects( once() ).method( "getBuildOutputFile" ).will( returnValue( new File( "buildOutputFile" ) ) );
        continuum.expects( once() ).method( "getBuildsManager" ).will( returnValue( buildsManager.proxy() ) );
        buildsManager.expects( once() ).method( "getCurrentBuilds" ).will( returnValue( new HashMap<String, BuildProjectTask>() ) );

        action.execute();
        continuum.verify();
    }

    public void testViewCurrentBuildInDistributedBuildAgent()
        throws Exception
    {
        Project project = createProject( "stub-project" );

        continuum.expects( once() ).method( "getProject" ).will( returnValue( project ) );
        continuum.expects( once() ).method( "getBuildResult" ).will( returnValue( null ) );
        continuum.expects( once() ).method( "getConfiguration" ).will( returnValue( (ConfigurationService) configurationService.proxy()  ) );
        configurationService.expects( once() ).method( "isDistributedBuildEnabled" ).will( returnValue( true ) );
        distributedBuildManager.expects( once() ).method( "getBuildResult" ).will( returnValue( new HashMap<String, Object>() ) );

        action.execute();
        continuum.verify();
    }

    private Project createProject( String name )
    {
        Project project = new Project();
        project.setId( 1 );
        project.setName( name );
        project.setArtifactId( "foo:bar" );
        project.setVersion( "1.0" );
        project.setState( ContinuumProjectState.BUILDING );

        return project;
    }

    private BuildResult createBuildResult( Project project )
    {
        BuildResult buildResult = new BuildResult();
        buildResult.setId( 1 );
        buildResult.setProject( project );

        return buildResult;
    }
}
