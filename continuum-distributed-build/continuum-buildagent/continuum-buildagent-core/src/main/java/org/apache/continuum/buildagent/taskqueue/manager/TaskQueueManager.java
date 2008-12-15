package org.apache.continuum.buildagent.taskqueue.manager;

import org.apache.continuum.taskqueue.manager.TaskQueueManagerException;
import org.codehaus.plexus.taskqueue.TaskQueue;

public interface TaskQueueManager
{
    String ROLE = TaskQueueManager.class.getName();

    TaskQueue getBuildQueue();

    void cancelBuild()
        throws TaskQueueManagerException;

    int getCurrentProjectInBuilding()
        throws TaskQueueManagerException;
}
