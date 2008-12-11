package org.apache.continuum.buildagent.taskqueue.manager;

import java.util.List;

import org.apache.continuum.taskqueue.manager.TaskQueueManagerException;
import org.apache.maven.continuum.buildqueue.BuildProjectTask;
import org.codehaus.plexus.taskqueue.TaskQueue;
import org.codehaus.plexus.taskqueue.execution.TaskQueueExecutor;

/**
 * @author <a href="mailto:ctan@apache.org">Maria Catherine Tan</a>
 */
public interface TaskQueueManager
{
    String ROLE = TaskQueueManager.class.getName();

    boolean buildInProgress()
        throws TaskQueueManagerException;

    void cancelBuildTask( int projectId )
        throws TaskQueueManagerException;

    boolean cancelCurrentBuild()
        throws TaskQueueManagerException;

    TaskQueue getBuildQueue();

    TaskQueueExecutor getBuildTaskQueueExecutor()
        throws TaskQueueManagerException;

    TaskQueue getCheckoutQueue();

    List /* CheckOutTask */getCheckOutTasksInQueue()
        throws TaskQueueManagerException;

    int getCurrentProjectIdBuilding()
        throws TaskQueueManagerException;

    TaskQueue getPrepareBuildQueue();

    TaskQueueExecutor getPrepareBuildTaskQueueExecutor()
        throws TaskQueueManagerException;

    public List<BuildProjectTask> getProjectsInBuildQueue()
        throws TaskQueueManagerException;
    
    boolean isInBuildingQueue( int projectId )
        throws TaskQueueManagerException;

    boolean isInBuildingQueue( int projectId, int buildDefinitionId )
        throws TaskQueueManagerException;

    boolean isInCheckoutQueue( int projectId )
        throws TaskQueueManagerException;
    
    boolean isInPrepareBuildQueue( int projectId )
    throws TaskQueueManagerException;
}