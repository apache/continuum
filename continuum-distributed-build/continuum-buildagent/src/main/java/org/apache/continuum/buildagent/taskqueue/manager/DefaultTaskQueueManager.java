package org.apache.continuum.buildagent.taskqueue.manager;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.continuum.scm.queue.PrepareBuildProjectsTask;
import org.apache.continuum.taskqueue.manager.TaskQueueManagerException;
import org.apache.maven.continuum.buildqueue.BuildProjectTask;
import org.apache.maven.continuum.scm.queue.CheckOutTask;
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

/**
 * @plexus.component role="org.apache.continuum.buildagent.taskqueue.manager.TaskQueueManager"
 *                   role-hint="task-queue-manager-dist"
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

    private PlexusContainer container;

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

        if ( currentTask instanceof BuildProjectTask )
        {
            if ( ( (BuildProjectTask) currentTask ).getProjectId() == projectId )
            {
                getLogger().info( "Cancelling task for project " + projectId );
                getBuildTaskQueueExecutor().cancelTask( currentTask );
            }
        }
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

    public void contextualize( Context context )
        throws ContextException
    {
        container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
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
                    Map<Integer, Integer> map = ( (PrepareBuildProjectsTask) task ).getProjectsBuildDefinitionsMap();

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

}