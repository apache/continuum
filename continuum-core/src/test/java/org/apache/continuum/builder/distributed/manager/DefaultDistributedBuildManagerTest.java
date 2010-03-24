package org.apache.continuum.builder.distributed.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.continuum.builder.distributed.executor.DistributedBuildTaskQueueExecutor;
import org.apache.continuum.builder.distributed.executor.ThreadedDistributedBuildTaskQueueExecutor;
import org.apache.continuum.configuration.BuildAgentConfiguration;
import org.apache.continuum.dao.BuildDefinitionDao;
import org.apache.continuum.dao.BuildResultDao;
import org.apache.continuum.dao.ProjectDao;
import org.apache.continuum.taskqueue.BuildProjectTask;
import org.apache.continuum.taskqueue.OverallDistributedBuildQueue;
import org.apache.continuum.taskqueue.PrepareBuildProjectsTask;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.codehaus.plexus.spring.PlexusInSpringTestCase;
import org.codehaus.plexus.taskqueue.TaskQueue;
import org.codehaus.plexus.taskqueue.execution.TaskQueueExecutor;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit3.JUnit3Mockery;
import org.jmock.lib.legacy.ClassImposteriser;

public class DefaultDistributedBuildManagerTest
    extends PlexusInSpringTestCase
{
    private final String TEST_BUILD_AGENT = "http://sampleagent";

    private DefaultDistributedBuildManager distributedBuildManager;

    private Mockery context;

    private OverallDistributedBuildQueue overallDistributedBuildQueue;

    private BuildDefinitionDao buildDefinitionDao;

    private BuildResultDao buildResultDao;

    private ProjectDao projectDao;

    private ConfigurationService configurationService;

    private List<BuildAgentConfiguration> buildAgents;

    private BuildAgentConfiguration buildAgent;

    private ThreadedDistributedBuildTaskQueueExecutor distributedBuildTaskQueueExecutor;

    private TaskQueue distributedBuildQueue;

    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();

        context = new JUnit3Mockery();
        context.setImposteriser( ClassImposteriser.INSTANCE );

        distributedBuildManager = (DefaultDistributedBuildManager) lookup( DistributedBuildManager.class );

        buildDefinitionDao = context.mock( BuildDefinitionDao.class );
        distributedBuildManager.setBuildDefinitionDao( buildDefinitionDao );

        buildResultDao = context.mock( BuildResultDao.class );
        distributedBuildManager.setBuildResultDao( buildResultDao );

        projectDao = context.mock( ProjectDao.class );
        distributedBuildManager.setProjectDao( projectDao );

        configurationService = context.mock( ConfigurationService.class );

        buildAgent = new BuildAgentConfiguration();
        buildAgent.setEnabled( true );
        buildAgent.setUrl( TEST_BUILD_AGENT );

        distributedBuildManager.setConfigurationService( configurationService );

        buildAgents = new ArrayList<BuildAgentConfiguration>();
        buildAgents.add( buildAgent );

        distributedBuildTaskQueueExecutor = (ThreadedDistributedBuildTaskQueueExecutor) context.mock( ThreadedDistributedBuildTaskQueueExecutor.class, "distributed-build-project" );

        distributedBuildQueue = context.mock( TaskQueue.class, "distributed-build-queue" );
    }

    public void testViewQueuesAfterBuildAgentIsLost()
        throws Exception
    {
        setUpMockOverallDistributedBuildQueues();

        recordDisableOfBuildAgent();

        Map<String, List<PrepareBuildProjectsTask>> prepareBuildQueues = distributedBuildManager.getProjectsInPrepareBuildQueue();
        Map<String, List<BuildProjectTask>> buildQueues = distributedBuildManager.getProjectsInBuildQueue();
        Map<String, PrepareBuildProjectsTask> currentPrepareBuild = distributedBuildManager.getProjectsCurrentlyPreparingBuild();
        Map<String, BuildProjectTask> currentBuild = distributedBuildManager.getProjectsCurrentlyBuilding();

        assertEquals( prepareBuildQueues.size(), 0 );
        assertEquals( buildQueues.size(), 0 );
        assertEquals( currentPrepareBuild.size(), 0 );
        assertEquals( currentBuild.size(), 0 );

        context.assertIsSatisfied();
    }

    public void testDisableBuildAgentWhenUnavailableToPing()
        throws Exception
    {
        setUpMockOverallDistributedBuildQueues();

        recordDisableOfBuildAgent();

        distributedBuildManager.isAgentAvailable( TEST_BUILD_AGENT );

        context.assertIsSatisfied();
    }

    private void setUpMockOverallDistributedBuildQueues()
    {
        Map<String, OverallDistributedBuildQueue> overallDistributedBuildQueues =
            Collections.synchronizedMap( new HashMap<String, OverallDistributedBuildQueue>() );
        overallDistributedBuildQueue = context.mock( OverallDistributedBuildQueue.class );

        overallDistributedBuildQueues.put( TEST_BUILD_AGENT, overallDistributedBuildQueue );
        distributedBuildManager.setOverallDistributedBuildQueues( overallDistributedBuildQueues );
    }

    private void recordDisableOfBuildAgent()
        throws Exception
    {
        context.checking( new Expectations()
        {
            {
                one( configurationService ).getBuildAgents();
                will( returnValue( buildAgents )  );

                one( configurationService ).updateBuildAgent( buildAgent );
                one( configurationService ).store();

                exactly( 2 ).of( overallDistributedBuildQueue ).getDistributedBuildTaskQueueExecutor();
                will( returnValue( distributedBuildTaskQueueExecutor ) );

                one( distributedBuildTaskQueueExecutor ).getCurrentTask();
                will( returnValue( null ) );

                one( overallDistributedBuildQueue ).getProjectsInQueue();
                will( returnValue( new ArrayList<PrepareBuildProjectsTask>() ) );

                one( overallDistributedBuildQueue ).getDistributedBuildQueue();
                will( returnValue( distributedBuildQueue ) );

                one( distributedBuildQueue ).removeAll( new ArrayList<PrepareBuildProjectsTask>() );

                one( distributedBuildTaskQueueExecutor ).stop();
            }
        } );
    }
}
