package org.apache.continuum.taskqueue;

import org.apache.continuum.builder.distributed.executor.DistributedBuildTaskQueueExecutor;
import org.codehaus.plexus.taskqueue.Task;
import org.codehaus.plexus.taskqueue.TaskQueue;
import org.codehaus.plexus.taskqueue.TaskQueueException;

import java.util.List;

public interface OverallDistributedBuildQueue
{
    String getBuildAgentUrl();

    void setBuildAgentUrl( String buildAgentUrl );

    TaskQueue getDistributedBuildQueue();

    void addToDistributedBuildQueue( Task distributedBuildTask )
        throws TaskQueueException;

    List<PrepareBuildProjectsTask> getProjectsInQueue()
        throws TaskQueueException;

    boolean isInDistributedBuildQueue( int projectGroupId, int scmRootId )
        throws TaskQueueException;

    void removeFromDistributedBuildQueue( int projectGroupId, int scmRootId )
        throws TaskQueueException;

    void removeFromDistributedBuildQueue( int[] hashCodes )
        throws TaskQueueException;

    void removeFromDistributedBuildQueueByHashCode( int hashCode )
        throws TaskQueueException;

    DistributedBuildTaskQueueExecutor getDistributedBuildTaskQueueExecutor();
}
