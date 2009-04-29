package org.apache.continuum.builder.distributed.executor.overall;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.continuum.builder.distributed.executor.DistributedBuildTaskQueueExecutor;
import org.apache.continuum.builder.distributed.manager.DistributedBuildManager;
import org.apache.continuum.configuration.BuildAgentConfiguration;
import org.apache.continuum.configuration.BuildAgentGroupConfiguration;
import org.apache.continuum.dao.BuildDefinitionDao;
import org.apache.continuum.dao.ProjectDao;
import org.apache.continuum.taskqueue.PrepareBuildProjectsTask;
import org.apache.continuum.utils.ProjectSorter;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.system.Profile;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.taskqueue.Task;
import org.codehaus.plexus.taskqueue.TaskQueue;
import org.codehaus.plexus.taskqueue.execution.TaskExecutionException;
import org.codehaus.plexus.taskqueue.execution.TaskExecutor;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.dag.CycleDetectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DistributedBuildProjectTaskExecutorOverall
    implements TaskExecutor
{
    private static final Logger log = LoggerFactory.getLogger( DistributedBuildProjectTaskExecutorOverall.class );

    /**
     * @plexus.requirement
     */
    private ConfigurationService configurationService;

    /**
     * @plexus.requirement
     */
    private ProjectDao projectDao;

    /**
     * @plexus.requirement
     */
    private BuildDefinitionDao buildDefinitionDao;

    /**
     * @plexus.requirement
     */
    private DistributedBuildManager buildManager;

    /**
     * @plexus.requirement
     */
    private TaskQueue deferredTaskQueue;

    public void executeTask( Task task )
        throws TaskExecutionException
    {
        try
        {
            String agentGroup = getBuildAgentGroup( task );

            Map<String, DistributedBuildTaskQueueExecutor> executors = buildManager.getTaskQueueExecutors();

            DistributedBuildTaskQueueExecutor executor = filterExecutors( agentGroup, executors );
            if ( executor != null )
            {
                log.info( "delegating task to build agent task queue executor: " + executor.getBuildAgentUrl() );
                executor.getQueue().put( task );
            }
            else
            {
                // task is added to deferred-queue , which will then be added back to distributed-queue
                // so as not to choke-off the distributed-queue.               
                deferredTaskQueue.put( task );
            }
        }
        catch ( ContinuumException e )
        {
            log.error( "error encountered delegating task to a build agent queue", e );
            throw new TaskExecutionException( e.getMessage(), e );
        }
        catch ( Exception e )
        {
            log.error( "error encountered delegating task to a build agent queue", e );
            throw new TaskExecutionException( e.getMessage(), e );
        }
    }

    private DistributedBuildTaskQueueExecutor filterExecutors(
        Map<String, DistributedBuildTaskQueueExecutor> executors )
    {
        // return the first non-busy taskqueue executor
        for ( String url : executors.keySet() )
        {
            if ( executors.get( url ).getCurrentTask() == null )
            {
                return executors.get( url );
            }
        }
        // else return the first executor
        if ( !executors.isEmpty() )
        {
            return executors.values().iterator().next();
        }
        return null;
    }

    private DistributedBuildTaskQueueExecutor filterExecutors( String agentGroupName,
                                                               Map<String, DistributedBuildTaskQueueExecutor> executors )
    {
        if ( agentGroupName == null ) //it doesnt belong to any group, just return the first non-busy executor.
        {
            return filterExecutors( executors );
        }

        BuildAgentGroupConfiguration agentGroup = configurationService.getBuildAgentGroup( agentGroupName );
        List<BuildAgentConfiguration> buildAgents = agentGroup.getBuildAgents();
        Map<String, DistributedBuildTaskQueueExecutor> mapCandidateExecutors =
            new HashMap<String, DistributedBuildTaskQueueExecutor>();
        List<String> agentUrls = new ArrayList<String>();

        if ( buildAgents != null )
        {
            for ( BuildAgentConfiguration buildAgent : buildAgents )
            {
                agentUrls.add( buildAgent.getUrl() );
            }
        }

        for ( String url : executors.keySet() )
        {
            if ( agentUrls.contains( url ) )
            {
                mapCandidateExecutors.put( url, executors.get( url ) );
            }
        }

        return filterExecutors( mapCandidateExecutors );
    }

    private String getBuildAgentGroup( Task task )
        throws ContinuumException
    {
        try
        {
            List<Project> projects = new ArrayList<Project>();

            PrepareBuildProjectsTask prepareBuildTask = (PrepareBuildProjectsTask) task;
            Map<Integer, Integer> projectsAndBuildDefinitions = prepareBuildTask.getProjectsBuildDefinitionsMap();

            for ( Integer projectId : projectsAndBuildDefinitions.keySet() )
            {
                projects.add( projectDao.getProjectWithDependencies( projectId ) );
            }

            projects = ProjectSorter.getSortedProjects( projects, null );

            int buildDefinitionId = projectsAndBuildDefinitions.get( projects.get( 0 ).getId() );
            BuildDefinition buildDefinition = buildDefinitionDao.getBuildDefinition( buildDefinitionId );

            Profile profile = buildDefinition.getProfile();

            if ( profile != null && !StringUtils.isEmpty( profile.getBuildAgentGroup() ) )
            {
                String groupName = profile.getBuildAgentGroup();

                BuildAgentGroupConfiguration buildAgentGroup = configurationService.getBuildAgentGroup( groupName );

                return buildAgentGroup.getName();
            }
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumException( "Error while getting build agent group", e );
        }

        log.info( "profile build agent group is null" );

        return null;
    }
}
