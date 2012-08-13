package org.apache.continuum.buildagent.taskqueue.manager;

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

import org.apache.continuum.buildagent.taskqueue.PrepareBuildProjectsTask;
import org.apache.continuum.taskqueue.BuildProjectTask;
import org.apache.continuum.taskqueue.manager.TaskQueueManagerException;
import org.apache.continuum.utils.build.BuildTrigger;
import org.codehaus.plexus.taskqueue.TaskQueue;

import java.util.List;

public interface BuildAgentTaskQueueManager
{
    String ROLE = BuildAgentTaskQueueManager.class.getName();

    TaskQueue getBuildQueue();

    TaskQueue getPrepareBuildQueue();

    void cancelBuild()
        throws TaskQueueManagerException;

    int getIdOfProjectCurrentlyBuilding()
        throws TaskQueueManagerException;

    BuildProjectTask getCurrentProjectInBuilding()
        throws TaskQueueManagerException;

    PrepareBuildProjectsTask getCurrentProjectInPrepareBuild()
        throws TaskQueueManagerException;

    boolean hasBuildTaskInQueue()
        throws TaskQueueManagerException;

    boolean isProjectInBuildQueue( int projectId )
        throws TaskQueueManagerException;

    boolean isInPrepareBuildQueue( int projectGroupId, BuildTrigger trigger, String scmRootAddress )
        throws TaskQueueManagerException;

    List<PrepareBuildProjectsTask> getProjectsInPrepareBuildQueue()
        throws TaskQueueManagerException;

    List<BuildProjectTask> getProjectsInBuildQueue()
        throws TaskQueueManagerException;

    boolean removeFromPrepareBuildQueue( int projectGroupId, int scmRootId )
        throws TaskQueueManagerException;

    void removeFromPrepareBuildQueue( int[] hashCodes )
        throws TaskQueueManagerException;

    boolean removeFromBuildQueue( int projectId, int buildDefinitionId )
        throws TaskQueueManagerException;

    void removeFromBuildQueue( int[] hashCodes )
        throws TaskQueueManagerException;
}
