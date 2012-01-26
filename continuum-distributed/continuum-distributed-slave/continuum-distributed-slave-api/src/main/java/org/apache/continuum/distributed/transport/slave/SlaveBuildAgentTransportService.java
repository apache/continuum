package org.apache.continuum.distributed.transport.slave;

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

import com.atlassian.xmlrpc.ServiceObject;
/**
 * SlaveBuildAgentTransportService
 */
@ServiceObject("SlaveBuildAgentTransportService")
public interface SlaveBuildAgentTransportService
{
    public Boolean buildProjects( List<Map<String, Object>> projectsBuildContext )
        throws Exception;

    public Map<String, Object> getBuildResult( int projectId )
        throws Exception;

    public Map<String, Object> getProjectCurrentlyBuilding()
        throws Exception;

    public List<Map<String, String>> getAvailableInstallations()
        throws Exception;

    public Boolean ping()
        throws Exception;

    public Boolean cancelBuild()
        throws Exception;

    public String generateWorkingCopyContent( int projectId, String directory, String baseUrl, String imagesBaseUrl )
        throws Exception;

    public Map<String, Object> getProjectFile( int projectId, String directory, String filename )
        throws Exception;

    public Map getReleasePluginParameters( int projectId, String pomFilename )
        throws Exception;

    public List<Map<String, String>> processProject( int projectId, String pomFilename, boolean autoVersionSubmodules )
        throws Exception;

    public String releasePrepare( Map project, Properties properties, Map releaseVersion, Map developmentVersion,
                                  Map environments, String username )
        throws Exception;

    public Map<String, Object> getReleaseResult( String releaseId )
        throws Exception;

    public Map getListener( String releaseId )
        throws Exception;

    public Boolean removeListener( String releaseId )
        throws Exception;

    public String getPreparedReleaseName( String releaseId )
        throws Exception;

    public Boolean releasePerform( String releaseId, String goals, String arguments, boolean useReleaseProfile,
                                   Map repository, String username )
        throws Exception;

    public String releasePerformFromScm( String goals, String arguments, boolean useReleaseProfile, Map repository,
                                         String scmUrl, String scmUsername, String scmPassword, String scmTag,
                                         String scmTagBase, Map environments, String username )
        throws Exception;

    public String releaseCleanup( String releaseId )
        throws Exception;

    public Boolean releaseRollback( String releaseId, int projectId )
        throws Exception;

    public Integer getBuildSizeOfAgent()
        throws Exception;

    public List<Map<String, Object>> getProjectsInPrepareBuildQueue()
        throws Exception;

    public List<Map<String, Object>> getProjectsAndBuildDefinitionsInPrepareBuildQueue()
        throws Exception;

    public List<Map<String, Object>> getProjectsInBuildQueue()
        throws Exception;

    public Map<String, Object> getProjectCurrentlyPreparingBuild()
        throws Exception;

    public List<Map<String, Object>> getProjectsAndBuildDefinitionsCurrentlyPreparingBuild()
        throws Exception;

    public Boolean isProjectGroupInQueue( int projectGroupId )
        throws Exception;

    public Boolean isProjectScmRootInQueue( int projectScmRootId, List<Integer> projectIds )
        throws Exception;

    public Boolean isProjectCurrentlyBuilding( int projectId, int buildDefinitionId )
        throws Exception;

    public Boolean isProjectInBuildQueue( int projectId, int buildDefinitionId )
        throws Exception;

    public Boolean isProjectCurrentlyPreparingBuild( int projectId, int buildDefinitionId )
        throws Exception;

    public Boolean isProjectInPrepareBuildQueue( int projectId, int buildDefinitionId )
        throws Exception;

    public Boolean isProjectGroupInPrepareBuildQueue( int projectGroupId )
        throws Exception;

    public Boolean isProjectGroupCurrentlyPreparingBuild( int projectGroupId )
        throws Exception;

    public Boolean removeFromPrepareBuildQueue( int projectGroupId, int scmRootId )
        throws Exception;

    public Boolean removeFromPrepareBuildQueue( List<String> hashCodes )
        throws Exception;

    public Boolean removeFromBuildQueue( int projectId, int buildDefinitionId )
        throws Exception;

    public Boolean removeFromBuildQueue( List<String> hashCodes )
        throws Exception;
    
    /**
     * Get build agent's platform.
     * 
     * @return The operating system name of the build agent
     * @throws Exception
     */
    public String getBuildAgentPlatform()
        throws Exception;

    /**
     * Execute a directory purge on the build agent
     * 
     * @param directoryType valid types are <i>working</i> and <i>releases</i>
     * @param daysOlder days older
     * @param retentionCount retention count
     * @param deleteAll delete all flag
     * 
     * @throws Exception error that will occur during the purge
     */
    public void executeDirectoryPurge( String directoryType, int daysOlder, int retentionCount, boolean deleteAll ) throws Exception;
}
