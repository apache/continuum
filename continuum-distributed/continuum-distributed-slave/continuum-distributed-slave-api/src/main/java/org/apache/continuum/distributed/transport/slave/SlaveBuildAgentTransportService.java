package org.apache.continuum.distributed.transport.slave;

import java.util.List;
import java.util.Map;

import com.atlassian.xmlrpc.ServiceObject;

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

/**
 * SlaveBuildAgentTransportService
 */
@ServiceObject( "SlaveBuildAgentTransportService" )
public interface SlaveBuildAgentTransportService
{
    public Boolean buildProjects( List<Map> projectsBuildContext ) throws Exception;
    
    public Map getBuildResult( int projectId ) throws Exception;
    
    public Integer getProjectCurrentlyBuilding() throws Exception;
    
    public List<Map> getAvailableInstallations() throws Exception;
    
    public Boolean ping() throws Exception;

    public Boolean cancelBuild() throws Exception;

    public String generateWorkingCopyContent( int projectId, String directory, String baseUrl, String imagesBaseUrl )
        throws Exception;

    public String getProjectFileContent( int projectId, String directory, String filename )
        throws Exception;

    public Map getReleasePluginParameters( int projectId, String pomFilename )
        throws Exception;

    public List<Map<String, String>> processProject( int projectId, String pomFilename, boolean autoVersionSubmodules )
        throws Exception;

    public String releasePrepare( Map project, Map properties, Map releaseVersion, Map developmentVersion, Map environments )
        throws Exception;

    public Map getReleaseResult( String releaseId )
        throws Exception;

    public Map getListener( String releaseId )
        throws Exception;

    public Boolean removeListener( String releaseId )
        throws Exception;

    public String getPreparedReleaseName( String releaseId )
        throws Exception;

    public Boolean releasePerform( String releaseId, String goals, String arguments, boolean useReleaseProfile, Map repository )
        throws Exception;

    public String releasePerformFromScm( String goals, String arguments, boolean useReleaseProfile, Map repository, String scmUrl, 
                                         String scmUsername, String scmPassword, String scmTag, String scmTagBase, Map environments )
        throws Exception;

    public String releaseCleanup( String releaseId )
        throws Exception;

    public Boolean releaseRollback( String releaseId, int projectId )
        throws Exception;
}
