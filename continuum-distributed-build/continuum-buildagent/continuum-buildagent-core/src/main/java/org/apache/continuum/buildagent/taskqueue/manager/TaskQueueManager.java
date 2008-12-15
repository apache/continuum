package org.apache.continuum.buildagent.taskqueue.manager;

import org.apache.continuum.taskqueue.manager.TaskQueueManagerException;
import org.codehaus.plexus.taskqueue.TaskQueue;

public interface TaskQueueManager
{
    TaskQueue getBuildQueue();

    TaskQueue getPrepareBuildQueue();

    void cancelBuild()
        throws TaskQueueManagerException;
}
