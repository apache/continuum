package org.apache.continuum.buildagent;

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
import java.util.Properties;

public interface ContinuumBuildAgentService
{
    void buildProjects( List<Map<String, Object>> projectsBuildContext )
        throws ContinuumBuildAgentException;

    List<Map<String, String>> getAvailableInstallations()
        throws ContinuumBuildAgentException;

    Map<String, Object> getBuildResult( int projectId )
        throws ContinuumBuildAgentException;

    Map<String, Object> getProjectCurrentlyBuilding()
        throws ContinuumBuildAgentException;

    void cancelBuild()
        throws ContinuumBuildAgentException;

    String generateWorkingCopyContent( int projectId, String userDirectory, String baseUrl, String imagesBaseUrl )
        throws ContinuumBuildAgentException;

    Map<String, Object> getProjectFile( int projectId, String directory, String filename )
        throws ContinuumBuildAgentException;

    Map<String, Object> getReleasePluginParameters( int projectId, String pomFilename )
        throws ContinuumBuildAgentException;

    List<Map<String, String>> processProject( int projectId, String pomFilename, boolean autoVersionSubmodules )
        throws ContinuumBuildAgentException;

    String releasePrepare( Map project, Properties properties, Map releaseVersion, Map developmentVersion,
                           Map<String, String> environments, String username )
        throws ContinuumBuildAgentException;

    Map<String, Object> getReleaseResult( String releaseId )
        throws ContinuumBuildAgentException;

    Map<String, Object> getListener( String releaseId )
        throws ContinuumBuildAgentException;

    void removeListener( String releaseId )
        throws ContinuumBuildAgentException;

    String getPreparedReleaseName( String releaseId )
        throws ContinuumBuildAgentException;

    void releasePerform( String releaseId, String goals, String arguments, boolean useReleaseProfile, Map repository,
                         String username )
        throws ContinuumBuildAgentException;

    String releasePerformFromScm( String goals, String arguments, boolean useReleaseProfile, Map repository,
                                  String scmUrl, String scmUsername, String scmPassword, String scmTag,
                                  String scmTagBase, Map<String, String> environments, String username )
        throws ContinuumBuildAgentException;

    String releaseCleanup( String releaseId )
        throws ContinuumBuildAgentException;

    void releaseRollback( String releaseId, int projectId )
        throws ContinuumBuildAgentException;

    List<Map<String, Object>> getProjectsInPrepareBuildQueue()
        throws ContinuumBuildAgentException;

    List<Map<String, Object>> getProjectsAndBuildDefinitionsInPrepareBuildQueue()
        throws ContinuumBuildAgentException;

    List<Map<String, Object>> getProjectsInBuildQueue()
        throws ContinuumBuildAgentException;

    int getBuildSizeOfAgent()
        throws ContinuumBuildAgentException;

    Map<String, Object> getProjectCurrentlyPreparingBuild()
        throws ContinuumBuildAgentException;

    List<Map<String, Object>> getProjectsAndBuildDefinitionsCurrentlyPreparingBuild()
        throws ContinuumBuildAgentException;

    boolean isProjectGroupInQueue( int projectGroupId );

    boolean isProjectScmRootInQueue( int projectScmRootId, List<Integer> projectIds );

    boolean isProjectCurrentlyBuilding( int projectId, int buildDefinitionId );

    boolean isProjectInBuildQueue( int projectId, int buildDefinitionId );

    boolean isProjectGroupInPrepareBuildQueue( int projectGroupId );

    boolean isProjectGroupCurrentlyPreparingBuild( int projectGroupId );

    boolean isProjectInPrepareBuildQueue( int projectId, int buildDefinitionId );

    boolean isProjectCurrentlyPreparingBuild( int projectId, int buildDefinitionId );

    boolean removeFromPrepareBuildQueue( int projectGroupId, int scmRootId )
        throws ContinuumBuildAgentException;

    void removeFromPrepareBuildQueue( List<String> hashCodes )
        throws ContinuumBuildAgentException;

    boolean removeFromBuildQueue( int projectId, int builddefinitonId )
        throws ContinuumBuildAgentException;

    void removeFromBuildQueue( List<String> hashCodes )
        throws ContinuumBuildAgentException;

    boolean ping()
        throws ContinuumBuildAgentException;

    /**
     * Get build agent's platform.
     *
     * @return The operating system name of the build agent
     * @throws Exception
     */
    String getBuildAgentPlatform()
        throws ContinuumBuildAgentException;

    /**
     * Determines if build agent is currently executing a build
     *
     * @return true if executing build; false otherwise
     */
    boolean isExecutingBuild();

    /**
     * Determines if build agent is currently executing a release
     *
     * @return true if executing release; false otherwise
     * @throws ContinuumBuildAgentException if unable to determine if buildagent is executing a release
     */
    boolean isExecutingRelease()
        throws ContinuumBuildAgentException;

    /**
     * Execute a directory purge on the build agent
     *
     * @param directoryType  valid types are <i>working</i> and <i>releases</i>
     * @param daysOlder      days older
     * @param retentionCount retention count
     * @param deleteAll      delete all flag
     * @return true if purge is successful; false otherwise
     * @throws ContinuumBuildAgentException error that will occur during the purge
     */
    void executeDirectoryPurge( String directoryType, int daysOlder, int retentionCount, boolean deleteAll )
        throws ContinuumBuildAgentException;

    /**
     * Execute a repository purge on the build agent
     *
     * @param repoName                used to determine location at the build agent
     * @param daysOlder               age in days when file is eligible for purging
     * @param retentionCount          number of artifact versions required to retain
     * @param deleteAll               triggers full deletion
     * @param deleteReleasedSnapshots whether to remove all snapshots matching a released artifact version
     * @throws Exception
     */
    public void executeRepositoryPurge( String repoName, int daysOlder, int retentionCount, boolean deleteAll,
                                        boolean deleteReleasedSnapshots )
        throws ContinuumBuildAgentException;

}
