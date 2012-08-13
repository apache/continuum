package org.apache.continuum.builder.distributed.executor;

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

import org.codehaus.plexus.taskqueue.Task;
import org.codehaus.plexus.taskqueue.TaskQueue;

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

    /**
     * Returns the task queue
     *
     * @return the TaskQueue of the task queue executor
     */
    public TaskQueue getQueue();
}
