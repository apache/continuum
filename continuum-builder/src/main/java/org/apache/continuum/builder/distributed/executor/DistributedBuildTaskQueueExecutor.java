package org.apache.continuum.builder.distributed.executor;

import org.codehaus.plexus.taskqueue.Task;

public interface DistributedBuildTaskQueueExecutor
{
    String ROLE = DistributedBuildTaskQueueExecutor.class.getName();

    /**
     * Returns the build agent url of task queue executor
     * 
     * @return the build agent url
     */
    String getBuildAgentUrl();

    /**
     * Sets the build agent url of this task queue executor
     * 
     * @param buildAgentUrl
     */
    void setBuildAgentUrl( String buildAgentUrl );

    /**
     * Returns the currently executing task.
     *
     * @return the currently executing task.
     */
    Task getCurrentTask();

    /**
     * Cancels execution of this task, if it's currently running.
     * Does NOT remove it from the associated queue!
     *
     * @param task The task to cancel
     * @return true if the task was cancelled, false if the task was not executing.
     */
    boolean cancelTask( Task task );
}
