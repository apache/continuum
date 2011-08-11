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

    public Boolean returnBuildResult( Map<String, Object> buildResult, String buildAgentUrl )
        throws Exception
    {
        distributedBuildService.updateBuildResult( buildResult );
        log.info( "Project {} build finished in build agent {}. Returned build result.", 
                  ContinuumDistributedUtil.getProjectNameAndId( buildResult ), buildAgentUrl );
        return Boolean.TRUE;
    }

    public Boolean ping()
        throws Exception
    {
        log.debug( "Ping master ok" );

        return Boolean.TRUE;
    }

    public Boolean prepareBuildFinished( Map<String, Object> prepareBuildResult, String buildAgentUrl )
        throws Exception
    {
        distributedBuildService.prepareBuildFinished( prepareBuildResult );
        log.info( "Prepare build finished for project {} in build agent {}", 
                   ContinuumDistributedUtil.getProjectNameAndId( prepareBuildResult ), buildAgentUrl );
        return Boolean.TRUE;
    }

    public Boolean startProjectBuild( Integer projectId, String buildAgentUrl )
        throws Exception
    {
        distributedBuildService.startProjectBuild( projectId );
        log.info( "Start building project (projectId={}) in build agent {}.", projectId, buildAgentUrl );
        return Boolean.TRUE;
    }

    public Boolean startPrepareBuild( Map<String, Object> prepareBuildResult, String buildAgentUrl )
        throws Exception
    {
        distributedBuildService.startPrepareBuild( prepareBuildResult );
        log.info( "Start preparing build of project {} in build agent {}",
                   ContinuumDistributedUtil.getProjectNameAndId( prepareBuildResult ), buildAgentUrl );
        return Boolean.TRUE;
    }

    public Map<String, String> getEnvironments( Integer buildDefinitionId, String installationType, String buildAgentUrl )
        throws Exception
    {
        Map<String, String> envs = distributedBuildService.getEnvironments( buildDefinitionId, installationType );
        log.debug( "Retrieving environments by build agent {}. buildDefinitionId={}, installationType={}", 
                   new Object[] { buildAgentUrl, buildDefinitionId, installationType } );
        return envs;
    }

    public Boolean updateProject( Map<String, Object> project )
        throws Exception
    {
        distributedBuildService.updateProject( project );
        log.debug( "Start updating project {}", ContinuumDistributedUtil.getProjectNameAndId( project ) );
        return Boolean.TRUE;
    }

    public Boolean shouldBuild( Map<String, Object> context, String buildAgentUrl )
        throws Exception
    {
        log.debug( "Checking if project {} should build in build agent {}",
                   ContinuumDistributedUtil.getProjectNameAndId( context ), buildAgentUrl );
        return distributedBuildService.shouldBuild( context );
    }
}
