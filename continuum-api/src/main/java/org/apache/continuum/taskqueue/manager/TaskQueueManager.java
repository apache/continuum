package org.apache.continuum.taskqueue.manager;

import java.util.List;

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

    TaskQueue getPurgeQueue();

    boolean isInBuildingQueue( int projectId )
        throws TaskQueueManagerException;

    boolean isInBuildingQueue( int projectId, int buildDefinitionId )
        throws TaskQueueManagerException;

    boolean isInCheckoutQueue( int projectId )
        throws TaskQueueManagerException;

    boolean isInPrepareBuildQueue( int projectId )
        throws TaskQueueManagerException;

    boolean isInPurgeQueue( int purgeConfigurationId )
        throws TaskQueueManagerException;

    /**
     * Check if the repository is already in the purging queue
     * 
     * @param repositoryId the id of the repository purge configuration
     * @return true if the repository is in the purging queue, otherwise false
     * @throws TaskQueueManagerException
     */
    boolean isRepositoryInPurgeQueue( int repositoryId )
        throws TaskQueueManagerException;

    /**
     * Check if the repository is being used by a project that is currently building
     * 
     * @param repositoryId the id of the local repository
     * @return true if the repository is in use, otherwise false
     * @throws TaskQueueManagerException
     */
    boolean isRepositoryInUse( int repositoryId )
        throws TaskQueueManagerException;

    boolean releaseInProgress()
        throws TaskQueueManagerException;

    boolean removeFromBuildingQueue( int projectId, int buildDefinitionId, int trigger, String projectName )
        throws TaskQueueManagerException;

    /**
     * Remove local repository from the purge queue
     * 
     * @param purgeConfigId the id of the purge configuration
     * @return true if the purge configuration was successfully removed from the purge queue, otherwise false
     * @throws TaskQueueManagerException
     */
    boolean removeFromPurgeQueue( int purgeConfigId )
        throws TaskQueueManagerException;

    /**
     * Remove local repositories from the purge queue
     * 
     * @param purgeConfigIds the ids of the purge configuration
     * @return true if the purge configurations were successfully removed from the purge queue, otherwise false
     * @throws TaskQueueManagerException
     */
    boolean removeFromPurgeQueue( int[] purgeConfigIds )
        throws TaskQueueManagerException;

    boolean removeProjectFromBuildingQueue( int projectId )
        throws TaskQueueManagerException;

    boolean removeProjectsFromBuildingQueue( int[] projectsId )
        throws TaskQueueManagerException;

    /**
     * @param hashCodes BuildProjectTask hashCodes
     * @throws TaskQueueManagerException
     */
    void removeProjectsFromBuildingQueueWithHashCodes( int[] hashCodes )
        throws TaskQueueManagerException;

    boolean removeProjectFromCheckoutQueue( int projectId )
        throws TaskQueueManagerException;

    boolean removeProjectsFromCheckoutQueue( int[] projectId )
        throws TaskQueueManagerException;

    /**
     * Remove local repository from the purge queue
     * 
     * @param repositoryId the id of the local repository
     * @throws TaskQueueManagerException
     */
    void removeRepositoryFromPurgeQueue( int repositoryId )
        throws TaskQueueManagerException;

    /**
     * @param hashCodes CheckOutTask hashCodes
     * @throws TaskQueueManagerException
     */
    void removeTasksFromCheckoutQueueWithHashCodes( int[] hashCodes )
        throws TaskQueueManagerException;
}
