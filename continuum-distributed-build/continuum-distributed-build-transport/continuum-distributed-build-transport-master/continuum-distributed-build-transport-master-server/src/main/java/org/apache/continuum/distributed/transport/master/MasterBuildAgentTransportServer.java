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

import org.apache.continuum.builder.distributed.manager.DistributedBuildManager;
import org.apache.continuum.distributed.transport.master.MasterBuildAgentTransportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MasterBuildAgentTransportServer
 */
public class MasterBuildAgentTransportServer
    implements MasterBuildAgentTransportService
{
    private Logger log = LoggerFactory.getLogger( this.getClass() );

    /**
     * @plexus.requirement
     */
    private DistributedBuildManager distributedBuildManager;

    public Boolean returnBuildResult( Map buildResult )
        throws Exception
    {
        log.info( "Build result returned." );
        distributedBuildManager.updateBuildResult( buildResult );
        return Boolean.TRUE;
    }

    public Boolean returnScmResult( Map scmResult )
        throws Exception
    {
        log.info( "SCM result returned." );
        distributedBuildManager.updateScmResult( scmResult );
        return Boolean.TRUE;
    }

    public Boolean ping()
        throws Exception
    {
        log.info( "Ping ok" );
        
        return Boolean.TRUE;
    }
    
    // TODO: add prepareBuildFinished() method
}
