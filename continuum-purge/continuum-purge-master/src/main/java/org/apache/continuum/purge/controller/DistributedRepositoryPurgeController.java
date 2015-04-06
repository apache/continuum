package org.apache.continuum.purge.controller;

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

import org.apache.continuum.distributed.transport.slave.SlaveBuildAgentTransportClient;
import org.apache.continuum.distributed.transport.slave.SlaveBuildAgentTransportService;
import org.apache.continuum.model.repository.AbstractPurgeConfiguration;
import org.apache.continuum.model.repository.DistributedRepositoryPurgeConfiguration;
import org.apache.continuum.purge.executor.ContinuumPurgeExecutorException;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

/**
 * DirectoryPurgeController
 */
@Component( role = PurgeController.class, hint = "purge-distributed-repository" )
public class DistributedRepositoryPurgeController
    implements PurgeController
{
    private static final Logger log = LoggerFactory.getLogger( DistributedRepositoryPurgeController.class );

    @Requirement
    private ConfigurationService configurationService;

    private SlaveBuildAgentTransportService transportClient;

    public void doPurge( String path )
    {
        log.warn( "doPurge( String ) is not supported for {}",
                  DistributedRepositoryPurgeController.class.getSimpleName() );
    }

    public void doPurge( AbstractPurgeConfiguration purgeConfig )
    {
        DistributedRepositoryPurgeConfiguration repoPurge = (DistributedRepositoryPurgeConfiguration) purgeConfig;
        try
        {
            transportClient.ping();

            if ( log.isDebugEnabled() )
            {
                StringBuilder logMsg = new StringBuilder().append(
                    "Executing repository purge with the following settings[" )
                                                          .append( "repo=" )
                                                          .append( repoPurge.getRepositoryName() )
                                                          .append( ",daysOlder=" )
                                                          .append( repoPurge.getDaysOlder() )
                                                          .append( ", retentionCount=" )
                                                          .append( repoPurge.getRetentionCount() )
                                                          .append( ", deleteAll=" )
                                                          .append( repoPurge.isDeleteAll() )
                                                          .append( ",deleteReleasedSnapshots=" )
                                                          .append( repoPurge.isDeleteReleasedSnapshots() )
                                                          .append( "]" );
                log.debug( logMsg.toString() );
            }

            transportClient.executeRepositoryPurge( repoPurge.getRepositoryName(), repoPurge.getDaysOlder(),
                                                    repoPurge.getRetentionCount(), repoPurge.isDeleteAll(),
                                                    repoPurge.isDeleteReleasedSnapshots() );
        }
        catch ( Exception e )
        {
            log.error( "Unable to execute purge: " + e.getMessage(), e );
        }
    }

    public void initializeExecutors( AbstractPurgeConfiguration purgeConfig )
        throws ContinuumPurgeExecutorException
    {
        DistributedRepositoryPurgeConfiguration repoPurge = (DistributedRepositoryPurgeConfiguration) purgeConfig;
        try
        {
            transportClient = new SlaveBuildAgentTransportClient( new URL( repoPurge.getBuildAgentUrl() ), "",
                                                                  configurationService.getSharedSecretPassword() );
        }
        catch ( Exception e )
        {
            throw new ContinuumPurgeExecutorException( e.getMessage(), e );
        }
    }
}
