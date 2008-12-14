package org.apache.continuum.buildagent.taskqueue.manager;

import org.codehaus.plexus.taskqueue.TaskQueue;

public interface TaskQueueManager
{
    TaskQueue getPrepareBuildQueue();

    TaskQueue getBuildQueue();
}
