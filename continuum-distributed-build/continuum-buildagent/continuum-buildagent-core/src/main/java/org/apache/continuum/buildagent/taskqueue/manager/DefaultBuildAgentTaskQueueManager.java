package org.apache.continuum.buildagent.taskqueue.manager;

import java.util.List;

import org.apache.continuum.taskqueue.BuildProjectTask;
import org.apache.continuum.taskqueue.manager.TaskQueueManagerException;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.taskqueue.Task;
import org.codehaus.plexus.taskqueue.TaskQueue;
import org.codehaus.plexus.taskqueue.TaskQueueException;
import org.codehaus.plexus.taskqueue.execution.TaskQueueExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @plexus.component role="org.apache.continuum.buildagent.taskqueue.manager.BuildAgentTaskQueueManager"
 */
public class DefaultBuildAgentTaskQueueManager
    implements BuildAgentTaskQueueManager, Contextualizable
{
    private Logger log = LoggerFactory.getLogger( this.getClass() );

    /**
     * @plexus.requirement role-hint="build-agent"
     */
    private TaskQueue buildQueue;

    private PlexusContainer container;

    public void cancelBuild()
        throws TaskQueueManagerException
    {
        removeProjectsFromBuildQueue();
        cancelCurrentBuild();
    }

    public TaskQueue getBuildQueue()
    {
        return buildQueue;
    }

    public int getCurrentProjectInBuilding()
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
    
    private void removeProjectsFromBuildQueue()
        throws TaskQueueManagerException
    {
        try
        {
            List<BuildProjectTask> queues = buildQueue.getQueueSnapshot();
        
            if ( queues != null )
            {
                for ( BuildProjectTask task : queues )
                {
                    log.info( "remove project '" + task.getProjectName() + "' from build queue" );
                    buildQueue.remove( task );
                }
            }
            else
            {
                log.info( "no build task in queue" );
            }
        }
        catch ( TaskQueueException e )
        {
            throw new TaskQueueManagerException( "Error while getting build tasks from queue", e );
        }
    }

    private boolean cancelCurrentBuild()
        throws TaskQueueManagerException
    {
        Task task = getBuildTaskQueueExecutor().getCurrentTask();
        
        if ( task != null )
        {
            if ( task instanceof BuildProjectTask )
            {
                log.info( "Cancelling current build task" );
                return getBuildTaskQueueExecutor().cancelTask( task );
            }
            else
            {
                log.warn( "Current task not a BuildProjectTask - not cancelling" );
            }
        }
        else
        {
            log.warn( "No task running - not cancelling" );
        }
        return false;
    }

    public TaskQueueExecutor getBuildTaskQueueExecutor()
        throws TaskQueueManagerException
    {
        try
        {
            return (TaskQueueExecutor) container.lookup( TaskQueueExecutor.class, "build-agent" );
        }
        catch ( ComponentLookupException e )
        {
            throw new TaskQueueManagerException( e.getMessage(), e );
        }
    }
    
    public void contextualize( Context context )
        throws ContextException
    {
        container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }
}
