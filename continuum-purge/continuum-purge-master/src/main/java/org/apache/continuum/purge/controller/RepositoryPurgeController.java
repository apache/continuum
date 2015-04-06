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

import org.apache.continuum.model.repository.AbstractPurgeConfiguration;
import org.apache.continuum.model.repository.RepositoryPurgeConfiguration;
import org.apache.continuum.purge.PurgeConfigurationService;
import org.apache.continuum.purge.PurgeConfigurationServiceException;
import org.apache.continuum.purge.executor.ContinuumPurgeExecutor;
import org.apache.continuum.purge.executor.ContinuumPurgeExecutorException;
import org.apache.continuum.purge.executor.RepositoryPurgeExecutorFactory;
import org.apache.continuum.purge.repository.content.RepositoryManagedContent;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DefaultPurgeController
 *
 * @author Maria Catherine Tan
 */
@Component( role = org.apache.continuum.purge.controller.PurgeController.class, hint = "purge-repository" )
public class RepositoryPurgeController
    implements PurgeController
{
    private static final Logger log = LoggerFactory.getLogger( RepositoryPurgeController.class );

    @Requirement
    private PurgeConfigurationService purgeConfigurationService;

    @Requirement
    private RepositoryPurgeExecutorFactory executorFactory;

    public void purge( AbstractPurgeConfiguration purgeConfig )
    {
        RepositoryPurgeConfiguration config = (RepositoryPurgeConfiguration) purgeConfig;
        try
        {
            String path = config.getRepository().getLocation();
            RepositoryManagedContent repositoryContent = getManagedContent( config.getRepository().getId() );
            ContinuumPurgeExecutor executor = executorFactory.create( config.isDeleteAll(), config.getDaysOlder(),
                                                                      config.getRetentionCount(),
                                                                      config.isDeleteReleasedSnapshots(),
                                                                      repositoryContent );
            log.info( "purging repository '{}'", path );
            executor.purge( path );
            log.info( "purge complete '{}'", path );
        }
        catch ( ContinuumPurgeExecutorException e )
        {
            log.error( "failure during repo purge", e );
        }
    }

    private RepositoryManagedContent getManagedContent( int repoId )
        throws ContinuumPurgeExecutorException
    {
        try
        {
            return purgeConfigurationService.getManagedRepositoryContent( repoId );
        }
        catch ( PurgeConfigurationServiceException e )
        {
            throw new ContinuumPurgeExecutorException( "Error while initializing purge executors", e );
        }
    }
}

