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
import org.apache.continuum.purge.ContinuumPurgeConstants;
import org.apache.continuum.purge.PurgeConfigurationService;
import org.apache.continuum.purge.PurgeConfigurationServiceException;
import org.apache.continuum.purge.executor.CleanAllPurgeExecutor;
import org.apache.continuum.purge.executor.ContinuumPurgeExecutor;
import org.apache.continuum.purge.executor.ContinuumPurgeExecutorException;
import org.apache.continuum.purge.executor.DaysOldRepositoryPurgeExecutor;
import org.apache.continuum.purge.executor.ReleasedSnapshotsRepositoryPurgeExecutor;
import org.apache.continuum.purge.executor.RetentionCountRepositoryPurgeExecutor;
import org.apache.continuum.purge.repository.content.RepositoryManagedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DefaultPurgeController
 *
 * @author Maria Catherine Tan
 * @plexus.component role="org.apache.continuum.purge.controller.PurgeController" role-hint="purge-repository"
 */
public class RepositoryPurgeController
    implements PurgeController
{
    private static final Logger log = LoggerFactory.getLogger( RepositoryPurgeController.class );

    private ContinuumPurgeExecutor purgeExecutor;

    private ContinuumPurgeExecutor purgeReleasedSnapshotsExecutor;

    /**
     * @plexus.requirement
     */
    private PurgeConfigurationService purgeConfigurationService;

    private boolean deleteReleasedSnapshots = false;

    public void initializeExecutors( AbstractPurgeConfiguration purgeConfig )
        throws ContinuumPurgeExecutorException
    {
        RepositoryManagedContent repositoryContent;

        RepositoryPurgeConfiguration repoPurge = (RepositoryPurgeConfiguration) purgeConfig;

        try
        {
            repositoryContent =
                purgeConfigurationService.getManagedRepositoryContent( repoPurge.getRepository().getId() );
        }
        catch ( PurgeConfigurationServiceException e )
        {
            throw new ContinuumPurgeExecutorException( "Error while initializing purge executors", e );
        }

        if ( repoPurge.isDeleteAll() )
        {
            purgeExecutor = new CleanAllPurgeExecutor( ContinuumPurgeConstants.PURGE_REPOSITORY );
        }
        else
        {
            if ( repoPurge.getDaysOlder() > 0 )
            {
                purgeExecutor = new DaysOldRepositoryPurgeExecutor( repositoryContent, repoPurge.getDaysOlder(),
                                                                    repoPurge.getRetentionCount() );
            }
            else
            {
                purgeExecutor =
                    new RetentionCountRepositoryPurgeExecutor( repositoryContent, repoPurge.getRetentionCount() );
            }

            purgeReleasedSnapshotsExecutor = new ReleasedSnapshotsRepositoryPurgeExecutor( repositoryContent );
            deleteReleasedSnapshots = repoPurge.isDeleteReleasedSnapshots();
        }
    }

    public void doPurge( AbstractPurgeConfiguration purgeConfig )
    {
        RepositoryPurgeConfiguration repoPurge = (RepositoryPurgeConfiguration) purgeConfig;
        doPurge( repoPurge.getRepository().getLocation() );
    }
    
    public void doPurge( String path )
    {
        try
        {
            if ( deleteReleasedSnapshots )
            {
                purgeReleasedSnapshotsExecutor.purge( path );
            }

            purgeExecutor.purge( path );
        }
        catch ( ContinuumPurgeExecutorException e )
        {
            log.error( e.getMessage(), e );
        }
    }
}