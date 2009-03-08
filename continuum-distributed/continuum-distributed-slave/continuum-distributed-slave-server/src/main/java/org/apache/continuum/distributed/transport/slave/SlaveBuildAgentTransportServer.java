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

import org.apache.continuum.buildagent.ContinuumBuildAgentException;
import org.apache.continuum.buildagent.ContinuumBuildAgentService;
import org.apache.continuum.distributed.transport.slave.SlaveBuildAgentTransportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ProxyMasterBuildAgentTransportService
 */
public class SlaveBuildAgentTransportServer
    implements SlaveBuildAgentTransportService
{
    private Logger log = LoggerFactory.getLogger( this.getClass() );

    private ContinuumBuildAgentService continuumBuildAgentService;
    
    public SlaveBuildAgentTransportServer( ContinuumBuildAgentService continuumBuildAgentService )
    {
        this.continuumBuildAgentService = continuumBuildAgentService;
    }
    
    public Boolean buildProjects( List<Map> projectsBuildContext )
        throws Exception
    {
        Boolean result = Boolean.FALSE;
        
        try
        {
            continuumBuildAgentService.buildProjects( projectsBuildContext );
            result = Boolean.TRUE;
            
            log.info( "Building projects." );
        }
        catch ( ContinuumBuildAgentException e )
        {
            log.error( "Failed to build projects.", e );
            throw e;
        }
        
        return result;
    }

    public List<Map> getAvailableInstallations()
        throws Exception
    {
        List<Map> installations = null;

        try
        {
            installations = continuumBuildAgentService.getAvailableInstallations();
            log.info( "Available installations: " + installations.size() );
        }
        catch ( ContinuumBuildAgentException e )
        {
            log.error( "Failed to get available installations.", e );
            throw e;
        }

        return installations;
    }

    public Map getBuildResult( int projectId )
        throws Exception
    {
        Map buildResult = null;
        
        try
        {
            buildResult = continuumBuildAgentService.getBuildResult( projectId );
            log.info( "Build result for project " + projectId + " acquired." );
        }
        catch ( ContinuumBuildAgentException e )
        {
            log.error( "Failed to get build result for project " + projectId, e );
            throw e;
        }
        
        return buildResult;
    }

    public Integer getProjectCurrentlyBuilding()
        throws Exception
    {
        Integer projectId = new Integer( continuumBuildAgentService.getProjectCurrentlyBuilding() );
        
        log.info( "Currently building project " + projectId.intValue() );
        
        return projectId;
    }

    public Boolean ping()
        throws Exception
    {
        log.info( "Ping ok" );
        
        return Boolean.TRUE;
    }

    public Boolean cancelBuild()
        throws Exception
    {
        Boolean result = Boolean.FALSE;

        try
        {
            continuumBuildAgentService.cancelBuild();
            result = Boolean.TRUE;
            log.info( "Cancelled build" );
        }
        catch ( ContinuumBuildAgentException e )
        {
            log.error( "Failed to cancel build", e );
            throw e;
        }

        return result;
    }

    public String generateWorkingCopyContent( int projectId, String directory, String baseUrl, String imagesBaseUrl )
        throws Exception
    {
        try
        {
            return continuumBuildAgentService.generateWorkingCopyContent( projectId, directory, baseUrl, imagesBaseUrl );
        }
        catch ( ContinuumBuildAgentException e )
        {
           log.error( "Failed to generate working copy content", e );
           throw e;
        }
    }

    public String getProjectFileContent( int projectId, String directory, String filename )
        throws Exception
    {
        try
        {
            return continuumBuildAgentService.getProjectFileContent( projectId, directory, filename );
        }
        catch ( ContinuumBuildAgentException e )
        {
            log.error( "Failed to retrieve project file content", e );
            throw e;
        }
    }

    public Map getReleasePluginParameters( int projectId, String pomFilename )
        throws Exception
    {
        try
        {
            return continuumBuildAgentService.getReleasePluginParameters( projectId, pomFilename );
        }
        catch ( ContinuumBuildAgentException e )
        {
            log.error( "Failed to retrieve release plugin parameters", e );
            throw e;
        }
    }

    public List<Map<String, String>> processProject( int projectId, String pomFilename, boolean autoVersionSubmodules )
        throws Exception
    {
        try
        {
            return continuumBuildAgentService.processProject( projectId, pomFilename, autoVersionSubmodules );
        }
        catch ( ContinuumBuildAgentException e )
        {
            log.error( "Failed to process project", e );
            throw e;
        }
    }

    public String releasePrepare( Map project, Map properties, Map releaseVersion, Map developmentVersion, Map environments )
        throws Exception
    {
        try
        {
            return continuumBuildAgentService.releasePrepare( project, properties, releaseVersion, developmentVersion, environments );
        }
        catch ( ContinuumBuildAgentException e )
        {
            log.error( "Failed to prepare release", e );
            throw e;
        }
    }

    public Map getListener( String releaseId )
        throws Exception
    {
        try
        {
            return continuumBuildAgentService.getListener( releaseId );
        }
        catch ( ContinuumBuildAgentException e )
        {
            log.error( "Failed to retrieve listener state of " + releaseId, e );
            throw e;
        }
    }

    public Map getReleaseResult( String releaseId )
        throws Exception
    {
        try
        {
            return continuumBuildAgentService.getReleaseResult( releaseId );
        }
        catch ( ContinuumBuildAgentException e )
        {
            log.error( "Failed to retrieve release result of " + releaseId, e );
            throw e;
        }
    }

    public Boolean removeListener( String releaseId )
        throws Exception
    {
        Boolean result = Boolean.FALSE;

        try
        {
            continuumBuildAgentService.removeListener( releaseId );
            result = Boolean.TRUE;
        }
        catch ( ContinuumBuildAgentException e )
        {
            log.error( "Failed to remove listener of " + releaseId, e );
            throw e;
        }

        return result;
    }

    public String getPreparedReleaseName( String releaseId )
        throws Exception
    {
        try
        {
            return continuumBuildAgentService.getPreparedReleaseName( releaseId );
        }
        catch ( ContinuumBuildAgentException e )
        {
            log.error( "Failed to retrieve prepared release name of " + releaseId );
            throw e;
        }
    }

    public Boolean releasePerform( String releaseId, String goals, String arguments, boolean useReleaseProfile, Map repository )
        throws Exception
    {
        Boolean result = Boolean.FALSE;

        try
        {
            continuumBuildAgentService.releasePerform( releaseId, goals, arguments, useReleaseProfile, repository );
            result = Boolean.TRUE;
        }
        catch ( ContinuumBuildAgentException e )
        {
            log.error( "Unable to perform release", e );
            throw e;
        }

        return result;
    }

    public Boolean releasePerformFromScm( String goals, String arguments, boolean useReleaseProfile, Map repository, String scmUrl,
                                          String scmUsername, String scmPassword, String scmTag, String scmTagBase, Map environments )
        throws Exception
    {
        Boolean result = Boolean.FALSE;
    
        try
        {
            continuumBuildAgentService.releasePerformFromScm( goals, arguments, useReleaseProfile, repository, scmUrl, scmUsername,
                                                              scmPassword, scmTag, scmTagBase, environments );
            result = Boolean.TRUE;
        }
        catch ( ContinuumBuildAgentException e )
        {
            log.error( "Unable to perform release", e );
            throw e;
        }
    
        return result;
    }

    public String releaseCleanup( String releaseId )
        throws Exception
    {
        try
        {
            return continuumBuildAgentService.releaseCleanup( releaseId );
        }
        catch ( ContinuumBuildAgentException e )
        {
            log.error( "Unable to cleanup release of " + releaseId, e );
            throw e;
        }
    }
}
