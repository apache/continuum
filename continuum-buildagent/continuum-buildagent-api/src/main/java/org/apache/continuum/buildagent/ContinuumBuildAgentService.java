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

public interface ContinuumBuildAgentService
{
    void buildProjects( List<Map> projectsBuildContext )
        throws ContinuumBuildAgentException;
    
    List<Map> getAvailableInstallations()
        throws ContinuumBuildAgentException;

    Map getBuildResult( int projectId )
        throws ContinuumBuildAgentException;
    
    int getProjectCurrentlyBuilding()
        throws ContinuumBuildAgentException;
    
    void cancelBuild()
        throws ContinuumBuildAgentException;

    String generateWorkingCopyContent( int projectId, String userDirectory, String baseUrl, String imagesBaseUrl )
        throws ContinuumBuildAgentException;

    String getProjectFileContent( int projectId, String directory, String filename )
        throws ContinuumBuildAgentException;

    Map getReleasePluginParameters( int projectId, String pomFilename )
        throws ContinuumBuildAgentException;

    List<Map<String, String>> processProject( int projectId, String pomFilename, boolean autoVersionSubmodules )
        throws ContinuumBuildAgentException;

    String releasePrepare( Map project, Map properties, Map releaseVersion, Map developmentVersion, Map<String, String> environments )
        throws ContinuumBuildAgentException;

    Map getReleaseResult( String releaseId )
        throws ContinuumBuildAgentException;

    Map getListener( String releaseId )
        throws ContinuumBuildAgentException;

    void removeListener( String releaseId )
        throws ContinuumBuildAgentException;

    String getPreparedReleaseName( String releaseId )
        throws ContinuumBuildAgentException;

    void releasePerform( String releaseId, String goals, String arguments, boolean useReleaseProfile, Map repository )
        throws ContinuumBuildAgentException;

    String releasePerformFromScm( String goals, String arguments, boolean useReleaseProfile, Map repository, String scmUrl, String scmUsername, 
                                  String scmPassword, String scmTag, String scmTagBase, Map<String, String> environments )
        throws ContinuumBuildAgentException;

    String releaseCleanup( String releaseId )
        throws ContinuumBuildAgentException;

    void releaseRollback( String releaseId, int projectId )
        throws ContinuumBuildAgentException;
}