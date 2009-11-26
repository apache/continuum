package org.apache.continuum.distributed.transport.master;

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

import java.util.Map;

import org.apache.continuum.builder.distributed.DistributedBuildService;
import org.apache.continuum.distributed.commons.utils.ContinuumDistributedUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MasterBuildAgentTransportServer
 */
public class MasterBuildAgentTransportServer
    implements MasterBuildAgentTransportService
{
    private static final Logger log = LoggerFactory.getLogger( MasterBuildAgentTransportServer.class );

    private final DistributedBuildService distributedBuildService;

    public MasterBuildAgentTransportServer( DistributedBuildService distributedBuildService )
    {
        this.distributedBuildService = distributedBuildService;
    }

    public Boolean returnBuildResult( Map<String, Object> buildResult )
        throws Exception
    {
        log.info( "Build result returned for project " + ContinuumDistributedUtil.getProjectNameAndId( buildResult ) + "." );
        distributedBuildService.updateBuildResult( buildResult );
        return Boolean.TRUE;
    }

    public Boolean ping()
        throws Exception
    {
        log.info( "Ping ok" );

        return Boolean.TRUE;
    }

    public Boolean prepareBuildFinished( Map<String, Object> prepareBuildResult )
        throws Exception
    {
        log.info( "Prepare build finished for project " + ContinuumDistributedUtil.getProjectNameAndId( prepareBuildResult ) + "." );
        distributedBuildService.prepareBuildFinished( prepareBuildResult );
        return Boolean.TRUE;
    }

    public Boolean startProjectBuild( Integer projectId )
        throws Exception
    {
        log.info( "Start project '" + projectId + "' build." );
        distributedBuildService.startProjectBuild( projectId );
        return Boolean.TRUE;
    }

    public Boolean startPrepareBuild( Map<String, Object> prepareBuildResult )
        throws Exception
    {
        log.info( "Start prepare build of project " + ContinuumDistributedUtil.getProjectNameAndId( prepareBuildResult ) + "." );
        distributedBuildService.startPrepareBuild( prepareBuildResult );
        return Boolean.TRUE;
    }

    public Map<String, String> getEnvironments( Integer buildDefinitionId, String installationType )
        throws Exception
    {
        log.info( "Retrieving environments. buildDefinitionId=" + buildDefinitionId + ", installationType=" + installationType );
        return distributedBuildService.getEnvironments( buildDefinitionId, installationType );
    }

    public Boolean updateProject( Map<String, Object> project )
        throws Exception
    {
        log.info( "Start updating project " + ContinuumDistributedUtil.getProjectNameAndId( project ) );
        distributedBuildService.updateProject( project );
        return Boolean.TRUE;
    }

    public Boolean shouldBuild( Map<String, Object> context )
        throws Exception
    {
        log.info( "Checking if project " + ContinuumDistributedUtil.getProjectNameAndId( context ) + " should build" );
        return distributedBuildService.shouldBuild( context );
    }
}
