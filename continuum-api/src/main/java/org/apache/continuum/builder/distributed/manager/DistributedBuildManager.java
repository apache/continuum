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

import org.apache.continuum.buildagent.NoBuildAgentException;
import org.apache.continuum.buildagent.NoBuildAgentInGroupException;
import org.apache.continuum.configuration.BuildAgentConfiguration;
import org.apache.continuum.model.project.ProjectRunSummary;
import org.apache.continuum.model.project.ProjectScmRoot;
import org.apache.continuum.taskqueue.BuildProjectTask;
import org.apache.continuum.taskqueue.PrepareBuildProjectsTask;
import org.apache.continuum.utils.build.BuildTrigger;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.system.Installation;

public interface DistributedBuildManager
{
    String ROLE = DistributedBuildManager.class.getName();

    void cancelDistributedBuild( String buildAgentUrl )
        throws ContinuumException;

    void reload()
        throws ContinuumException;

    void update( BuildAgentConfiguration buildAgent )
        throws ContinuumException;

    void removeDistributedBuildQueueOfAgent( String buildAgentUrl )
        throws ContinuumException;

    boolean isBuildAgentBusy( String buildAgentUrl );

    List<Installation> getAvailableInstallations( String buildAgentUrl )
        throws ContinuumException;
    
    String getBuildAgentPlatform( String buildAgentUrl )
        throws ContinuumException;

    Map<String, List<PrepareBuildProjectsTask>> getProjectsInPrepareBuildQueue()
        throws ContinuumException;

    Map<String, List<BuildProjectTask>> getProjectsInBuildQueue()
        throws ContinuumException;

    Map<String, Object> getBuildResult( int projectId )
        throws ContinuumException;

    String generateWorkingCopyContent( int projectId, String directory, String baseUrl, String imagesBaseUrl )
        throws ContinuumException;

    Map<String, Object> getFileContent( int projectId, String directory, String filename )
        throws ContinuumException;

    void prepareBuildProjects( Map<Integer, Integer> projectsBuildDefinitionsMap, BuildTrigger buildTrigger, int projectGroupId,  
                               String projectGroupName, String scmRootAddress, int scmRootId, List<ProjectScmRoot> scmRoots )
        throws ContinuumException, NoBuildAgentException, NoBuildAgentInGroupException;

    Map<String, PrepareBuildProjectsTask> getProjectsCurrentlyPreparingBuild()
        throws ContinuumException;

    Map<String, BuildProjectTask> getProjectsCurrentlyBuilding()
        throws ContinuumException;

    void removeFromPrepareBuildQueue( String buildAgnetUrl, int projectGroupId, int scmRootId )
        throws ContinuumException;

    void removeFromPrepareBuildQueue( List<String> hashCodes )
        throws ContinuumException;

    void removeFromBuildQueue( String buildAgentUrl, int projectId, int buildDefinitionId )
        throws ContinuumException;

    void removeFromBuildQueue( List<String> hashCodes )
        throws ContinuumException;

    boolean isAgentAvailable( String buildAgentUrl )
        throws ContinuumException;

    boolean pingBuildAgent( String buildAgentUrl )
        throws ContinuumException;

    boolean isProjectInAnyPrepareBuildQueue( int projectId, int buildDefinitionId )
        throws ContinuumException;

    boolean isProjectInAnyBuildQueue( int projectId, int buildDefinitionId )
        throws ContinuumException;

    boolean isProjectCurrentlyPreparingBuild( int projectId, int buildDefinitionId )
        throws ContinuumException;

    boolean isProjectCurrentlyBuilding( int projectId, int buildDefinitionId )
        throws ContinuumException;
        
    String getBuildAgentUrl( int projectId, int buildDefinitionId )
        throws ContinuumException;

    List<ProjectRunSummary> getCurrentRuns();

    void removeCurrentRun( int projectId, int buildDefinitionId );

    void cancelBuild( int projectId )
        throws ContinuumException;

    void cancelGroupBuild( int projectGroupId )
        throws ContinuumException;
}
