package org.apache.continuum.buildagent.taskqueue.manager;

import org.apache.continuum.taskqueue.manager.TaskQueueManagerException;
import org.codehaus.plexus.taskqueue.TaskQueue;

public interface BuildAgentTaskQueueManager
{
    String ROLE = BuildAgentTaskQueueManager.class.getName();

    TaskQueue getBuildQueue();

    void cancelBuild()
        throws TaskQueueManagerException;

    int getCurrentProjectInBuilding()
        throws TaskQueueManagerException;
}
