package org.apache.continuum.taskqueue.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.continuum.dao.BuildDefinitionDao;
import org.apache.continuum.dao.ProjectDao;
import org.apache.continuum.model.repository.LocalRepository;
import org.apache.continuum.model.repository.RepositoryPurgeConfiguration;
import org.apache.continuum.purge.PurgeConfigurationService;
import org.apache.continuum.purge.task.PurgeTask;
import org.apache.continuum.scm.queue.PrepareBuildProjectsTask;
import org.apache.maven.continuum.buildqueue.BuildProjectTask;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.release.tasks.PerformReleaseProjectTask;
import org.apache.maven.continuum.scm.queue.CheckOutTask;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.taskqueue.Task;
import org.codehaus.plexus.taskqueue.TaskQueue;
import org.codehaus.plexus.taskqueue.TaskQueueException;
import org.codehaus.plexus.taskqueue.execution.TaskQueueExecutor;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:ctan@apache.org">Maria Catherine Tan</a>
 * @plexus.component role="org.apache.continuum.taskqueue.manager.TaskQueueManager" role-hint="default"
 */
public class DefaultTaskQueueManager
    extends AbstractLogEnabled
    implements TaskQueueManager, Contextualizable
{
    /**
     * @plexus.requirement role-hint="build-project"
     */
    private TaskQueue buildQueue;

    /**
     * @plexus.requirement role-hint="check-out-project"
     */
    private TaskQueue checkoutQueue;

    /**
     * @plexus.requirement role-hint="prepare-build-project"
     */
    private TaskQueue prepareBuildQueue;

    /**
     * @plexus.requirement role-hint="purge"
     */
    private TaskQueue purgeQueue;
    
    /**
     * @plexus.requirement
     */
    private BuildDefinitionDao buildDefinitionDao;
    
    /**
     * @plexus.requirement
     */
    private ProjectDao projectDao;

    /**
     * @plexus.requirement
     */
    private PurgeConfigurationService purgeConfigurationService;
    
    private PlexusContainer container;

    /**
     * @plexus.requirement role-hint="distributed-build-project"
     */
    private TaskQueue distributedBuildQueue;

    public boolean buildInProgress()
        throws TaskQueueManagerException
    {
        Task task = getCurrentTask( "build-project" );
    
        if ( task != null && task instanceof BuildProjectTask )
        {
            return true;
        }
    
        return false;
    }
    
    public void cancelBuildTask( int projectId )
        throws TaskQueueManagerException
    {
        Task currentTask = getBuildTaskQueueExecutor().getCurrentTask();
        
        if ( currentTask != null )
        {
            if ( currentTask instanceof BuildProjectTask )
            {
                if ( ( (BuildProjectTask) currentTask ).getProjectId() == projectId )
                {
                    getLogger().info( "Cancelling task for project " + projectId );
                    getBuildTaskQueueExecutor().cancelTask( currentTask );
                }
                else
                {
                    getLogger().warn( "Current task is not for the given projectId (" + projectId + "): "
                                          + ( (BuildProjectTask) currentTask ).getProjectId() + "; not cancelling" );
                }
            }
            else
            {
                getLogger().warn( "Current task not a BuildProjectTask - not cancelling" );
            }
        }
        else
        {
            getLogger().warn( "No task running - not cancelling" );
        }
    }

    public boolean cancelCheckout( int projectId )
        throws TaskQueueManagerException
    {
        Task task = getCheckoutTaskQueueExecutor().getCurrentTask();

        if ( task != null )
        {
            if ( task instanceof CheckOutTask )
            {
                if ( ( (CheckOutTask) task ).getProjectId() == projectId )
                {
                    getLogger().info( "Cancelling checkout for project " + projectId );
                    return getCheckoutTaskQueueExecutor().cancelTask( task );
                }
                else
                {
                    getLogger().warn( "Current task is not for the given projectId (" + projectId + "): "
                                          + ( (CheckOutTask) task ).getProjectId() + "; not cancelling checkout" );
                }
            }
            else
            {
                getLogger().warn( "Current task not a CheckOutTask - not cancelling checkout" );
            }
        }
        else
        {
            getLogger().warn( "No task running - not cancelling checkout" );
        }
        return false;
    }

    public boolean cancelCurrentBuild()
        throws TaskQueueManagerException
    {
        Task task = getBuildTaskQueueExecutor().getCurrentTask();
        
        if ( task != null )
        {
            if ( task instanceof BuildProjectTask )
            {
                getLogger().info( "Cancelling current build task" );
                return getBuildTaskQueueExecutor().cancelTask( task );
            }
            else
            {
                getLogger().warn( "Current task not a BuildProjectTask - not cancelling" );
            }
        }
        else
        {
            getLogger().warn( "No task running - not cancelling" );
        }
        return false;
    }

    public TaskQueue getBuildQueue()
    {
        return buildQueue;
    }

    public TaskQueueExecutor getBuildTaskQueueExecutor()
        throws TaskQueueManagerException
    {
        try
        {
            return (TaskQueueExecutor) container.lookup( TaskQueueExecutor.class, "build-project" );
        }
        catch ( ComponentLookupException e )
        {
            throw new TaskQueueManagerException( e.getMessage(), e );
        }
    }

    public TaskQueueExecutor getCheckoutTaskQueueExecutor()
        throws TaskQueueManagerException
    {
        try
        {
            return (TaskQueueExecutor) container.lookup( TaskQueueExecutor.class, "check-out-project" );
        }
        catch ( ComponentLookupException e )
        {
            throw new TaskQueueManagerException( e.getMessage(), e );
        }
    }

    public TaskQueue getCheckoutQueue()
    {
        return checkoutQueue;
    }
    
    public List<CheckOutTask> getCheckOutTasksInQueue()
        throws TaskQueueManagerException
    {
        try
        {
            return checkoutQueue.getQueueSnapshot();
        }
        catch ( TaskQueueException e )
        {
            throw new TaskQueueManagerException( "Error while getting the checkout queue.", e );
        }
    }

    public int getCurrentProjectIdBuilding()
        throws TaskQueueManagerException
    {
        Task task = getBuildTaskQueueExecutor().getCurrentTask();
        if ( task != null )
        {
            if ( task instanceof BuildProjectTask )
            {
                return ( (BuildProjectTask) task ).getProjectId();
            }
        }
        return -1;
    }

    public TaskQueue getDistributedBuildQueue()
    {
        return distributedBuildQueue;
    }

    public List<PrepareBuildProjectsTask> getDistributedBuildProjectsInQueue()
        throws TaskQueueManagerException
    {
        try
        {
            return distributedBuildQueue.getQueueSnapshot();
        }
        catch ( TaskQueueException e )
        {
            throw new TaskQueueManagerException( "Error while getting the distributed building queue", e );
        }
    }

    public TaskQueue getPrepareBuildQueue()
    {
        return prepareBuildQueue;
    }   

    public TaskQueueExecutor getPrepareBuildTaskQueueExecutor()
        throws TaskQueueManagerException
    {
        try
        {
            return (TaskQueueExecutor) container.lookup( TaskQueueExecutor.class, "prepare-build-project" );
        }
        catch ( ComponentLookupException e )
        {
            throw new TaskQueueManagerException( e.getMessage(), e );
        }
    }

    public List<BuildProjectTask> getProjectsInBuildQueue()
        throws TaskQueueManagerException
    {
        try
        {
            return buildQueue.getQueueSnapshot();
        }
        catch ( TaskQueueException e )
        {
            throw new TaskQueueManagerException( "Error while getting the building queue.", e );
        }
    }
    
    public TaskQueue getPurgeQueue()
    {
        return purgeQueue;
    }
    
    public boolean isInBuildingQueue( int projectId )
        throws TaskQueueManagerException
    {
        return isInBuildingQueue( projectId, -1 );
    }
    
    public boolean isInBuildingQueue( int projectId, int buildDefinitionId )
        throws TaskQueueManagerException
    {
        List<BuildProjectTask> queue = getProjectsInBuildQueue();
    
        for ( BuildProjectTask task : queue )
        {
            if ( task != null )
            {
                if ( buildDefinitionId < 0 )
                {
                    if ( task.getProjectId() == projectId )
                    {
                        return true;
                    }
                }
                else
                {
                    if ( task.getProjectId() == projectId && task.getBuildDefinitionId() == buildDefinitionId )
                    {
                        return true;
                    }
                }
            }
        }
    
        return false;
    }
    
    public boolean isInCheckoutQueue( int projectId )
        throws TaskQueueManagerException
    {
        List<CheckOutTask> queue = getCheckOutTasksInQueue();
    
        for ( CheckOutTask task : queue )
        {
            if ( task != null && task.getProjectId() == projectId )
            {
                return true;
            }
        }
    
        return false;
    }

    public boolean isInCurrentPrepareBuildTask( int projectId )
        throws TaskQueueManagerException
    {
        Task task = getPrepareBuildTaskQueueExecutor().getCurrentTask();

        if ( task != null &&  task instanceof PrepareBuildProjectsTask )
        {
            Map<Integer, Integer> map = ( (PrepareBuildProjectsTask) task).getProjectsBuildDefinitionsMap();
            
            if ( map.size() > 0 )
            {
                Set<Integer> projectIds = map.keySet();
                
                if ( projectIds.contains( new Integer( projectId ) ) )
                {
                    return true;
                }
            }
        }
        
        return false;
    }

    public boolean isInDistributedBuildQueue( int projectGroupId, String scmRootAddress )
        throws TaskQueueManagerException
    {
        try
        {
            List<PrepareBuildProjectsTask> queue = distributedBuildQueue.getQueueSnapshot();

            for ( PrepareBuildProjectsTask task : queue )
            {
                if ( task != null )
                {
                    if ( task.getProjectGroupId() == projectGroupId && task.getScmRootAddress().equals( scmRootAddress ) )
                    {
                        return true;
                    }
                }
            }

            return false;
        }
        catch ( TaskQueueException e )
        {
            throw new TaskQueueManagerException( "Error while getting the tasks in distributed build queue", e );
        }
    }

    public boolean isInPrepareBuildQueue( int projectId )
        throws TaskQueueManagerException
    {
        try
        {
            List<PrepareBuildProjectsTask> queue = prepareBuildQueue.getQueueSnapshot();
            
            for ( PrepareBuildProjectsTask task : queue )
            {
                if ( task != null )
                {
                    Map<Integer, Integer> map = task.getProjectsBuildDefinitionsMap();
                    
                    if ( map.size() > 0 )
                    {
                        Set<Integer> projectIds = map.keySet();
                        
                        if ( projectIds.contains( new Integer( projectId ) ) )
                        {
                            return true;
                        }
                    }
                }
            }
            
            return false;
        }
        catch ( TaskQueueException e )
        {
            throw new TaskQueueManagerException( "Error while getting the tasks in prepare build queue", e );
        }
    }
    
    public boolean isInPurgeQueue( int purgeConfigId )
        throws TaskQueueManagerException
    {
        List<PurgeTask> queue = getAllPurgeConfigurationsInPurgeQueue();
    
        for ( PurgeTask task : queue )
        {
            if ( task != null && task.getPurgeConfigurationId() == purgeConfigId )
            {
                return true;
            }
        }
        return false;
    }
    
    public boolean isRepositoryInPurgeQueue( int repositoryId )
        throws TaskQueueManagerException
    {
        List<RepositoryPurgeConfiguration> repoPurgeConfigs =
            purgeConfigurationService.getRepositoryPurgeConfigurationsByRepository( repositoryId );
    
        for ( RepositoryPurgeConfiguration repoPurge : repoPurgeConfigs )
        {
            if ( isInPurgeQueue( repoPurge.getId() ) )
            {
                return true;
            }
        }
        return false;
    }
    
    public boolean isRepositoryInUse( int repositoryId )
        throws TaskQueueManagerException
    {
        try
        {
            Task task = getCurrentTask( "build-project" );
    
            if ( task != null && task instanceof BuildProjectTask )
            {
                int projectId = ( (BuildProjectTask) task ).getProjectId();
    
                Project project = projectDao.getProject( projectId );
                LocalRepository repository = project.getProjectGroup().getLocalRepository();
    
                if ( repository != null && repository.getId() == repositoryId )
                {
                    return true;
                }
            }
            return false;
        }
        catch ( ContinuumStoreException e )
        {
            throw new TaskQueueManagerException( e.getMessage(), e );
        }
    }

    public boolean releaseInProgress()
        throws TaskQueueManagerException
    {
        Task task = getCurrentTask( "perform-release" );
    
        if ( task != null && task instanceof PerformReleaseProjectTask )
        {
            return true;
        }
    
        return false;
    }

    public boolean removeFromBuildingQueue( int projectId, int buildDefinitionId, int trigger, String projectName )
        throws TaskQueueManagerException
    {
        BuildDefinition buildDefinition;
        
        try
        {
            buildDefinition = buildDefinitionDao.getBuildDefinition( buildDefinitionId );
        }
        catch ( ContinuumStoreException e )
        {
            throw new TaskQueueManagerException( "Error while removing project from build queue: " + projectName, e );
        }
        
        String buildDefinitionLabel = buildDefinition.getDescription();
        if ( StringUtils.isEmpty( buildDefinitionLabel ) )
        {
            buildDefinitionLabel = buildDefinition.getGoals();
        }
        BuildProjectTask buildProjectTask =
            new BuildProjectTask( projectId, buildDefinitionId, trigger, projectName, buildDefinitionLabel );
        return this.buildQueue.remove( buildProjectTask );
    }

    public boolean removeFromPurgeQueue( int purgeConfigId )
        throws TaskQueueManagerException
    {
        List<PurgeTask> queue = getAllPurgeConfigurationsInPurgeQueue();
    
        for ( PurgeTask task : queue )
        {
            if ( task != null && task.getPurgeConfigurationId() == purgeConfigId )
            {
                return purgeQueue.remove( task );
            }
        }
        return false;
    }


    public boolean removeFromPurgeQueue( int[] purgeConfigIds )
        throws TaskQueueManagerException
    {
        if ( purgeConfigIds == null )
        {
            return false;
        }
    
        if ( purgeConfigIds.length < 1 )
        {
            return false;
        }
    
        List<PurgeTask> queue = getAllPurgeConfigurationsInPurgeQueue();
    
        List<PurgeTask> tasks = new ArrayList<PurgeTask>();
    
        for ( PurgeTask task : queue )
        {
            if ( task != null )
            {
                if ( ArrayUtils.contains( purgeConfigIds, task.getPurgeConfigurationId() ) )
                {
                    tasks.add( task );
                }
            }
        }
    
        if ( !tasks.isEmpty() )
        {
            return purgeQueue.removeAll( tasks );
        }
    
        return false;
    }
    
    public boolean removeProjectsFromBuildingQueue( int[] projectsId )
        throws TaskQueueManagerException
    {
        if ( projectsId == null )
        {
            return false;
        }
        if ( projectsId.length < 1 )
        {
            return false;
        }
        List<BuildProjectTask> queue = getProjectsInBuildQueue();
    
        List<BuildProjectTask> tasks = new ArrayList<BuildProjectTask>();
    
        for ( BuildProjectTask task : queue )
        {
            if ( task != null )
            {
                if ( ArrayUtils.contains( projectsId, task.getProjectId() ) )
                {
                    tasks.add( task );
                }
            }
        }

        for ( BuildProjectTask buildProjectTask : tasks )
        {
            getLogger().info( "cancel build for project " + buildProjectTask.getProjectId() );
        }
        if ( !tasks.isEmpty() )
        {
            return buildQueue.removeAll( tasks );
        }
    
        return false;
    }
    
    public boolean removeProjectFromBuildingQueue( int projectId )
        throws TaskQueueManagerException
    {
        List<BuildProjectTask> queue = getProjectsInBuildQueue();
    
        for ( BuildProjectTask task : queue )
        {
            if ( task != null && task.getProjectId() == projectId )
            {
                return buildQueue.remove( task );
            }
        }
    
        return false;
    }
    
    public boolean removeProjectsFromCheckoutQueue( int[] projectsId )
        throws TaskQueueManagerException
    {
        if ( projectsId == null )
        {
            return false;
        }
        if ( projectsId.length < 1 )
        {
            return false;
        }
        List<CheckOutTask> queue = getCheckOutTasksInQueue();
    
        List<CheckOutTask> tasks = new ArrayList<CheckOutTask>();
    
        for ( CheckOutTask task : queue )
        {
            if ( task != null )
            {
                if ( ArrayUtils.contains( projectsId, task.getProjectId() ) )
                {
                    tasks.add( task );
                }
            }
        }
        if ( !tasks.isEmpty() )
        {
            return checkoutQueue.removeAll( tasks );
        }
        return false;
    }
    
    public void removeProjectsFromBuildingQueueWithHashCodes( int[] hashCodes )
        throws TaskQueueManagerException
    {
        List<BuildProjectTask> queue = getProjectsInBuildQueue();
    
        for ( BuildProjectTask task : queue )
        {
            if ( ArrayUtils.contains( hashCodes, task.hashCode() ) )
            {
                buildQueue.remove( task );
            }
        }
    }
    
    public boolean removeProjectFromCheckoutQueue( int projectId )
        throws TaskQueueManagerException
    {
        List<CheckOutTask> queue = getCheckOutTasksInQueue();
    
        for ( CheckOutTask task : queue )
        {
            if ( task != null && task.getProjectId() == projectId )
            {
                return checkoutQueue.remove( task );
            }
        }
    
        return false;
    }
    
    public void removeRepositoryFromPurgeQueue( int repositoryId )
        throws TaskQueueManagerException
    {
        List<RepositoryPurgeConfiguration> repoPurgeConfigs =
            purgeConfigurationService.getRepositoryPurgeConfigurationsByRepository( repositoryId );
    
        for ( RepositoryPurgeConfiguration repoPurge : repoPurgeConfigs )
        {
            removeFromPurgeQueue( repoPurge.getId() );
        }
    }

    public void removeTasksFromCheckoutQueueWithHashCodes( int[] hashCodes )
        throws TaskQueueManagerException
    {
        List<CheckOutTask> queue = getCheckOutTasksInQueue();
    
        for ( CheckOutTask task : queue )
        {
            if ( ArrayUtils.contains( hashCodes, task.hashCode() ) )
            {
                checkoutQueue.remove( task );
            }
        }
    }

    public boolean removeFromPrepareBuildQueue( int projectGroupId, String scmRootAddress )
        throws TaskQueueManagerException
    {
        try
        {
            List<PrepareBuildProjectsTask> queue = prepareBuildQueue.getQueueSnapshot();
            
            for ( PrepareBuildProjectsTask task : queue )
            {
                if ( task != null && task.getProjectGroupId() == projectGroupId && task.getScmRootAddress().equals( scmRootAddress ) )
                {
                    return prepareBuildQueue.remove( task );
                }
            }
            return false;
        }
        catch ( TaskQueueException e )
        {
            throw new TaskQueueManagerException( "Error while getting the prepare build projects task in queue", e );
        }
    }
    
    public void contextualize( Context context )
        throws ContextException
    {
        container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }
    
    private List<PurgeTask> getAllPurgeConfigurationsInPurgeQueue()
        throws TaskQueueManagerException
    {
        try
        {
            return purgeQueue.getQueueSnapshot();
        }
        catch ( TaskQueueException e )
        {
            throw new TaskQueueManagerException( "Error while getting the purge configs in purge queue", e );
        }
    }
    
    private Task getCurrentTask( String task )
        throws TaskQueueManagerException
    {
        try
        {
            TaskQueueExecutor executor = (TaskQueueExecutor) container.lookup( TaskQueueExecutor.class, task );
            return executor.getCurrentTask();
        }
        catch ( ComponentLookupException e )
        {
            throw new TaskQueueManagerException( "Unable to lookup current task", e );
        }
    }
}
