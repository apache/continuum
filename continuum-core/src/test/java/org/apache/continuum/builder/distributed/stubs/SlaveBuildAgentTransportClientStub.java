package org.apache.continuum.builder.distributed.stubs;

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

import org.apache.continuum.distributed.transport.slave.SlaveBuildAgentTransportService;

public class SlaveBuildAgentTransportClientStub
    implements SlaveBuildAgentTransportService
{
    public Boolean buildProjects( List<Map<String, Object>> projectsBuildContext )
        throws Exception
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean cancelBuild()
        throws Exception
    {
        // TODO Auto-generated method stub
        return null;
    }

    public String generateWorkingCopyContent( int projectId, String directory, String baseUrl, String imagesBaseUrl )
        throws Exception
    {
        // TODO Auto-generated method stub
        return null;
    }

    public List<Map<String, String>> getAvailableInstallations()
        throws Exception
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Map<String, Object> getBuildResult( int projectId )
        throws Exception
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Integer getBuildSizeOfAgent()
        throws Exception
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public Map getListener( String releaseId )
        throws Exception
    {
        // TODO Auto-generated method stub
        return null;
    }

    public String getPreparedReleaseName( String releaseId )
        throws Exception
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Map<String, Object> getProjectCurrentlyBuilding()
        throws Exception
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Map<String, Object> getProjectCurrentlyPreparingBuild()
        throws Exception
    {
        // TODO Auto-generated method stub
        return null;
    }

    public String getProjectFileContent( int projectId, String directory, String filename )
        throws Exception
    {
        // TODO Auto-generated method stub
        return null;
    }

    public List<Map<String, Object>> getProjectsInBuildQueue()
        throws Exception
    {
        // TODO Auto-generated method stub
        return null;
    }

    public List<Map<String, Object>> getProjectsInPrepareBuildQueue()
        throws Exception
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Map getReleasePluginParameters( int projectId, String pomFilename )
        throws Exception
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Map<String, Object> getReleaseResult( String releaseId )
        throws Exception
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean isProjectCurrentlyBuilding( int projectId, int buildDefinitionId )
        throws Exception
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean isProjectGroupInQueue( int projectGroupId )
        throws Exception
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean isProjectScmRootInQueue( int projectScmRootId, List<Integer> projectIds )
    {
        return true;
    }

    public Boolean isProjectInBuildQueue( int projectId, int buildDefinitionId )
        throws Exception
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean ping()
        throws Exception
    {
        return true;
    }

    public List<Map<String, String>> processProject( int projectId, String pomFilename, boolean autoVersionSubmodules )
        throws Exception
    {
        // TODO Auto-generated method stub
        return null;
    }

    public String releaseCleanup( String releaseId )
        throws Exception
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean releasePerform( String releaseId, String goals, String arguments, boolean useReleaseProfile,
                                   Map repository, String username )
        throws Exception
    {
        // TODO Auto-generated method stub
        return null;
    }

    public String releasePerformFromScm( String goals, String arguments, boolean useReleaseProfile, Map repository,
                                         String scmUrl, String scmUsername, String scmPassword, String scmTag,
                                         String scmTagBase, Map environments, String username )
        throws Exception
    {
        // TODO Auto-generated method stub
        return null;
    }

    public String releasePrepare( Map project, Properties properties, Map releaseVersion, Map developmentVersion,
                                  Map environments, String username )
        throws Exception
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean releaseRollback( String releaseId, int projectId )
        throws Exception
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean removeFromBuildQueue( int projectId, int buildDefinitionId )
        throws Exception
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean removeFromBuildQueue( List<String> hashCodes )
        throws Exception
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean removeFromPrepareBuildQueue( int projectGroupId, int scmRootId )
        throws Exception
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean removeFromPrepareBuildQueue( List<String> hashCodes )
        throws Exception
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean removeListener( String releaseId )
        throws Exception
    {
        // TODO Auto-generated method stub
        return null;
    }

    public List<Map<String, Object>> getProjectsAndBuildDefinitionsCurrentlyPreparingBuild()
        throws Exception
    {
        // TODO Auto-generated method stub
        return null;
    }

    public List<Map<String, Object>> getProjectsAndBuildDefinitionsInPrepareBuildQueue()
        throws Exception
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean isProjectGroupInPrepareBuildQueue( int projectGroupId )
        throws Exception
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean isProjectGroupCurrentlyPreparingBuild( int projectGroupId )
        throws Exception
    {
        // TODO Auto-generated method stub
        return null;
    }

    public String getBuildAgentPlatform()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean isProjectCurrentlyPreparingBuild( int projectId, int buildDefinitionId )
        throws Exception
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Boolean isProjectInPrepareBuildQueue( int projectId, int buildDefinitionId )
        throws Exception
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void executeDirectoryPurge( String directoryType, int daysOlder, int retentionCount, boolean deleteAll )
        throws Exception
    {
        // TODO Auto-generated method stub
        
    }

}
