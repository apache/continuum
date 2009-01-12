package org.apache.continuum.builder.distributed.manager;

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
import java.util.Map;

import org.apache.continuum.taskqueue.PrepareBuildProjectsTask;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.system.Installation;

public interface DistributedBuildManager
{
    String ROLE = DistributedBuildManager.class.getName();

    void cancelDistributedBuild( String buildAgentUrl, int projectGroupId, String scmRootAddress )
        throws ContinuumException;

    void updateScmResult( Map context )
        throws ContinuumException;

    void updateBuildResult( Map context )
        throws ContinuumException;

    void prepareBuildFinished( Map context )
        throws ContinuumException;

    void startProjectBuild( int projectId )
        throws ContinuumException;

    void startPrepareBuild( Map context )
        throws ContinuumException;

    void reload()
        throws ContinuumException;
    
    void removeAgentFromTaskQueueExecutor( String buildAgentUrl )
        throws ContinuumException;

    boolean isBuildAgentBusy( String buildAgentUrl );

    List<Installation> getAvailableInstallations( String buildAgentUrl )
        throws ContinuumException;

    Map<String, PrepareBuildProjectsTask> getDistributedBuildProjects();

    Map<String, Object> getBuildResult( int projectId )
        throws ContinuumException;

    Map<String, String> getEnvironments( int buildDefinitionId, String installationType )
        throws ContinuumException;
}
