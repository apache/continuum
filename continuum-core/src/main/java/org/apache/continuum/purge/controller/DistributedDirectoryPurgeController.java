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
import org.apache.continuum.model.repository.DistributedDirectoryPurgeConfiguration;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

/**
 * DirectoryPurgeController
 */
@Component( role = org.apache.continuum.purge.controller.PurgeController.class, hint = "purge-distributed-directory" )
public class DistributedDirectoryPurgeController
    implements PurgeController
{
    private static final Logger log = LoggerFactory.getLogger( DistributedDirectoryPurgeController.class );

    @Requirement
    private ConfigurationService configurationService;

    public void purge( AbstractPurgeConfiguration config )
    {
        DistributedDirectoryPurgeConfiguration dirConfig = (DistributedDirectoryPurgeConfiguration) config;
        String agentUrl = dirConfig.getBuildAgentUrl();
        try
        {
            SlaveBuildAgentTransportService transportClient =
                new SlaveBuildAgentTransportClient( new URL( dirConfig.getBuildAgentUrl() ), "",
                                                    configurationService.getSharedSecretPassword() );

            transportClient.ping();

            if ( log.isInfoEnabled() )
            {
                log.info( "sending request to {} [dirType={},full={},maxAge={},retain={}]",
                          new Object[] {
                              dirConfig.getBuildAgentUrl(), dirConfig.getDirectoryType(),
                              dirConfig.isDeleteAll(), dirConfig.getDaysOlder(), dirConfig.getRetentionCount()
                          } );
            }

            transportClient.executeDirectoryPurge( dirConfig.getDirectoryType(), dirConfig.getDaysOlder(),
                                                   dirConfig.getRetentionCount(), dirConfig.isDeleteAll() );

        }
        catch ( Exception e )
        {
            log.error( String.format( "sending request to %s failed: %s", agentUrl, e.getMessage() ),
                       e );
        }
    }
}
