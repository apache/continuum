package org.apache.continuum.builder.distributed.work;

import org.apache.continuum.builder.distributed.manager.DistributedBuildManager;
import org.apache.continuum.dao.BuildDefinitionDao;
import org.apache.continuum.dao.BuildResultDao;
import org.apache.continuum.dao.ProjectDao;
import org.apache.continuum.dao.ProjectScmRootDao;
import org.apache.continuum.model.project.ProjectRunSummary;
import org.apache.continuum.model.project.ProjectScmRoot;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.codehaus.plexus.spring.PlexusInSpringTestCase;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit3.JUnit3Mockery;
import org.jmock.lib.legacy.ClassImposteriser;

import java.util.ArrayList;
import java.util.List;

public class DefaultContinuumWorkerTest
    extends PlexusInSpringTestCase
{
    private Mockery context;

    private ProjectDao projectDao;

    private ProjectScmRootDao projectScmRootDao;

    private BuildDefinitionDao buildDefinitionDao;

    private BuildResultDao buildResultDao;

    private DistributedBuildManager distributedBuildManager;

    private ConfigurationService configurationService;

    private DefaultContinuumWorker worker;

    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();
        context = new JUnit3Mockery();
        context.setImposteriser( ClassImposteriser.INSTANCE );

        projectDao = context.mock( ProjectDao.class );
        projectScmRootDao = context.mock( ProjectScmRootDao.class );
        buildDefinitionDao = context.mock( BuildDefinitionDao.class );
        buildResultDao = context.mock( BuildResultDao.class );
        configurationService = context.mock( ConfigurationService.class );
        distributedBuildManager = context.mock( DistributedBuildManager.class );

        worker = (DefaultContinuumWorker) lookup( ContinuumWorker.class );
        worker.setBuildDefinitionDao( buildDefinitionDao );
        worker.setBuildResultDao( buildResultDao );
        worker.setProjectDao( projectDao );
        worker.setProjectScmRootDao( projectScmRootDao );
        worker.setConfigurationService( configurationService );
        worker.setDistributedBuildManager( distributedBuildManager );
    }

    public void testWorkerWithStuckBuild()
        throws Exception
    {
        recordOfStuckBuild();

        worker.work();

        context.assertIsSatisfied();
    }

    public void testWorkerWithStuckScm()
        throws Exception
    {
        recordOfStuckScm();

        worker.work();

        context.assertIsSatisfied();
    }

    private void recordOfStuckBuild()
        throws Exception
    {
        context.checking( new Expectations()
        {
            {
                one( configurationService ).isDistributedBuildEnabled();
                will( returnValue( true ) );

                exactly( 2 ).of( distributedBuildManager ).getCurrentRuns();
                will( returnValue( getCurrentRuns() ) );

                exactly( 2 ).of( projectScmRootDao ).getProjectScmRoot( 1 );
                will( returnValue( getScmRoot( ContinuumProjectState.OK ) ) );

                Project proj1 = getProject( 1, ContinuumProjectState.BUILDING );
                one( projectDao ).getProject( 1 );
                will( returnValue( proj1 ) );

                one( distributedBuildManager ).isProjectCurrentlyBuilding( 1, 1 );
                will( returnValue( false ) );

                one( buildDefinitionDao ).getBuildDefinition( 1 );
                will( returnValue( new BuildDefinition() ) );

                one( buildResultDao ).addBuildResult( with( any( Project.class ) ), with( any( BuildResult.class ) ) );
                one( projectDao ).updateProject( proj1 );

                one( projectDao ).getProject( 2 );
                will( returnValue( getProject( 2, ContinuumProjectState.OK ) ) );
            }
        } );
    }

    private void recordOfStuckScm()
        throws Exception
    {
        context.checking( new Expectations()
        {
            {
                one( configurationService ).isDistributedBuildEnabled();
                will( returnValue( true ) );

                exactly( 2 ).of( distributedBuildManager ).getCurrentRuns();
                will( returnValue( getCurrentRuns() ) );

                ProjectScmRoot scmRootUpdating = getScmRoot( ContinuumProjectState.UPDATING );
                one( projectScmRootDao ).getProjectScmRoot( 1 );
                will( returnValue( scmRootUpdating ) );

                one( distributedBuildManager ).isProjectCurrentlyPreparingBuild( 1, 1 );
                will( returnValue( false ) );

                one( projectScmRootDao ).updateProjectScmRoot( scmRootUpdating );

                one( projectScmRootDao ).getProjectScmRoot( 1 );
                will( returnValue( getScmRoot( ContinuumProjectState.ERROR ) ) );
            }
        } );
    }

    private List<ProjectRunSummary> getCurrentRuns()
    {
        List<ProjectRunSummary> runs = new ArrayList<ProjectRunSummary>();

        ProjectRunSummary run1 = new ProjectRunSummary();
        run1.setProjectId( 1 );
        run1.setBuildDefinitionId( 1 );
        run1.setProjectGroupId( 1 );
        run1.setProjectScmRootId( 1 );
        run1.setTrigger( 1 );
        run1.setTriggeredBy( "user" );
        run1.setBuildAgentUrl( "http://localhost:8181/continuum-buildagent/xmlrpc" );
        runs.add( run1 );

        ProjectRunSummary run2 = new ProjectRunSummary();
        run2.setProjectId( 2 );
        run2.setBuildDefinitionId( 2 );
        run2.setProjectGroupId( 1 );
        run2.setProjectScmRootId( 1 );
        run2.setTrigger( 1 );
        run2.setTriggeredBy( "user" );
        run2.setBuildAgentUrl( "http://localhost:8181/continuum-buildagent/xmlrpc" );
        runs.add( run2 );

        return runs;
    }

    private ProjectScmRoot getScmRoot( int state )
    {
        ProjectScmRoot scmRoot = new ProjectScmRoot();
        scmRoot.setState( state );
        return scmRoot;
    }

    private Project getProject( int projectId, int state )
    {
        Project project = new Project();
        project.setId( projectId );
        project.setState( state );
        return project;
    }
}
