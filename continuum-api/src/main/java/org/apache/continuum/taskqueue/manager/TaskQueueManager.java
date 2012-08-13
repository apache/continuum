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

import org.codehaus.plexus.taskqueue.TaskQueue;

/**
 * @author <a href="mailto:ctan@apache.org">Maria Catherine Tan</a>
 */
public interface TaskQueueManager
{
    String ROLE = TaskQueueManager.class.getName();

    TaskQueue getPurgeQueue();

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

    /**
     * Check whether a project is in the release stage based on the given releaseId.
     *
     * @param releaseId
     * @return
     * @throws TaskQueueManagerException
     */
    boolean isProjectInReleaseStage( String releaseId )
        throws TaskQueueManagerException;

    boolean releaseInProgress()
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

    /**
     * Remove local repository from the purge queue
     *
     * @param repositoryId the id of the local repository
     * @throws TaskQueueManagerException
     */
    void removeRepositoryFromPurgeQueue( int repositoryId )
        throws TaskQueueManagerException;
}
