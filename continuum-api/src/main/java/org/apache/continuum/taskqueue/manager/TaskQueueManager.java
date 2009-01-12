package org.apache.continuum.taskqueue.manager;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.List;

import org.apache.continuum.taskqueue.BuildProjectTask;
import org.apache.continuum.taskqueue.PrepareBuildProjectsTask;
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

    boolean cancelCheckout( int projectId )
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

    TaskQueue getDistributedBuildQueue();

    List<PrepareBuildProjectsTask> getDistributedBuildProjectsInQueue()
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

    boolean isInCurrentPrepareBuildTask( int projectId )
        throws TaskQueueManagerException;

    boolean isInDistributedBuildQueue( int projectGroupId, String scmRootAddress )
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

    boolean removeFromDistributedBuildQueue( int projectGroupId, String scmRootAddress )
        throws TaskQueueManagerException;

    boolean removeFromPrepareBuildQueue( int projectGroupId, String scmRootAddress )
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

    void removeTasksFromDistributedBuildQueueWithHashCodes( int[] hashCodes )
        throws TaskQueueManagerException;
}
