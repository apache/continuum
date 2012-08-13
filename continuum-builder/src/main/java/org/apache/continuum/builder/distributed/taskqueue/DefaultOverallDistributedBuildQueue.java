package org.apache.continuum.builder.distributed.taskqueue;

import org.apache.commons.lang.ArrayUtils;
import org.apache.continuum.builder.distributed.executor.DistributedBuildTaskQueueExecutor;
import org.apache.continuum.builder.distributed.executor.ThreadedDistributedBuildTaskQueueExecutor;
import org.apache.continuum.taskqueue.OverallDistributedBuildQueue;
import org.apache.continuum.taskqueue.PrepareBuildProjectsTask;
import org.codehaus.plexus.taskqueue.Task;
import org.codehaus.plexus.taskqueue.TaskQueue;
import org.codehaus.plexus.taskqueue.TaskQueueException;

import java.util.ArrayList;
import java.util.List;

public class DefaultOverallDistributedBuildQueue
    implements OverallDistributedBuildQueue
{
    private String buildAgentUrl;

    private DistributedBuildTaskQueueExecutor distributedBuildTaskQueueExecutor;

    public void addToDistributedBuildQueue( Task distributedBuildTask )
        throws TaskQueueException
    {
        getDistributedBuildQueue().put( distributedBuildTask );
    }

    public String getBuildAgentUrl()
    {
        return buildAgentUrl;
    }

    public TaskQueue getDistributedBuildQueue()
    {
        return ( (ThreadedDistributedBuildTaskQueueExecutor) distributedBuildTaskQueueExecutor ).getQueue();
    }

    public DistributedBuildTaskQueueExecutor getDistributedBuildTaskQueueExecutor()
    {
        return distributedBuildTaskQueueExecutor;
    }

    public List<PrepareBuildProjectsTask> getProjectsInQueue()
        throws TaskQueueException
    {
        return getDistributedBuildQueue().getQueueSnapshot();
    }

    public boolean isInDistributedBuildQueue( int projectGroupId, int scmRootId )
        throws TaskQueueException
    {
        List<PrepareBuildProjectsTask> tasks = getProjectsInQueue();

        for ( PrepareBuildProjectsTask task : tasks )
        {
            if ( task != null )
            {
                if ( task.getProjectGroupId() == projectGroupId && task.getProjectScmRootId() == scmRootId )
                {
                    return true;
                }
            }
        }

        return false;
    }

    public void removeFromDistributedBuildQueue( int projectGroupId, int scmRootId )
        throws TaskQueueException
    {
        List<PrepareBuildProjectsTask> tasks = getProjectsInQueue();

        for ( PrepareBuildProjectsTask task : tasks )
        {
            if ( task != null )
            {
                if ( task.getProjectGroupId() == projectGroupId && task.getProjectScmRootId() == scmRootId )
                {
                    getDistributedBuildQueue().remove( task );
                    return;
                }
            }
        }
    }

    public void removeFromDistributedBuildQueue( int[] hashCodes )
        throws TaskQueueException
    {
        List<PrepareBuildProjectsTask> tasks = getProjectsInQueue();

        List<PrepareBuildProjectsTask> tasksToRemove = new ArrayList<PrepareBuildProjectsTask>();

        for ( PrepareBuildProjectsTask task : tasks )
        {
            if ( task != null )
            {
                if ( ArrayUtils.contains( hashCodes, task.getHashCode() ) )
                {
                    tasksToRemove.add( task );
                }
            }
        }

        if ( !tasksToRemove.isEmpty() )
        {
            getDistributedBuildQueue().removeAll( tasksToRemove );
        }
    }

    public void removeFromDistributedBuildQueueByHashCode( int hashCode )
        throws TaskQueueException
    {
        List<PrepareBuildProjectsTask> tasks = getProjectsInQueue();

        for ( PrepareBuildProjectsTask task : tasks )
        {
            if ( task != null )
            {
                if ( task.getHashCode() == hashCode )
                {
                    getDistributedBuildQueue().remove( task );
                    return;
                }
            }
        }
    }

    public void setBuildAgentUrl( String buildAgentUrl )
    {
        this.buildAgentUrl = buildAgentUrl;
    }

    public void setDistributedBuildTaskQueueExecutor(
        DistributedBuildTaskQueueExecutor distributedBuildTaskQueueExecutor )
    {
        this.distributedBuildTaskQueueExecutor = distributedBuildTaskQueueExecutor;
    }
}
