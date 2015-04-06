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
import org.apache.continuum.purge.repository.scanner.RepositoryScanner;
import org.apache.continuum.purge.repository.scanner.ScannerHandler;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * DefaultPurgeController
 *
 * @author Maria Catherine Tan
 */
@Component( role = org.apache.continuum.purge.controller.PurgeController.class, hint = "purge-repository" )
public class RepositoryPurgeController
    implements PurgeController, ScannerHandler
{
    private static final Logger log = LoggerFactory.getLogger( RepositoryPurgeController.class );

    private ContinuumPurgeExecutor purgeExecutor;

    private ContinuumPurgeExecutor purgeReleasedSnapshotsExecutor;

    @Requirement
    private PurgeConfigurationService purgeConfigurationService;

    @Requirement( hint = "repository-scanner" )
    private RepositoryScanner scanner;

    private boolean deleteReleasedSnapshots = false;

    private boolean deleteAll = false;

    public void initializeExecutors( AbstractPurgeConfiguration purgeConfig )
        throws ContinuumPurgeExecutorException
    {
        RepositoryManagedContent repositoryContent;

        RepositoryPurgeConfiguration repoPurge = (RepositoryPurgeConfiguration) purgeConfig;

        try
        {
            repositoryContent = purgeConfigurationService.getManagedRepositoryContent(
                repoPurge.getRepository().getId() );
        }
        catch ( PurgeConfigurationServiceException e )
        {
            throw new ContinuumPurgeExecutorException( "Error while initializing purge executors", e );
        }

        if ( repoPurge.isDeleteAll() )
        {
            deleteAll = true;
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
                purgeExecutor = new RetentionCountRepositoryPurgeExecutor( repositoryContent,
                                                                           repoPurge.getRetentionCount() );
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
        log.info( "--- Start: Purging repository {} ---", path );
        if ( deleteAll )
        {
            handle( path );
        }
        else
        {
            try
            {
                scan( path );
            }
            catch ( ContinuumPurgeExecutorException e )
            {
                log.error( "failure while scanning", e );
            }
        }
        log.info( "--- End: Purging repository {} ---", path );
    }

    private void scan( String path )
        throws ContinuumPurgeExecutorException
    {
        scanner.scan( new File( path ), this );
    }

    public void handle( String path )
    {
        try
        {
            if ( !deleteAll && deleteReleasedSnapshots )
            {
                purgeReleasedSnapshotsExecutor.purge( path );
            }
            purgeExecutor.purge( path );
        }
        catch ( ContinuumPurgeExecutorException e )
        {
            log.error( String.format( "failure handling path '%s'", path ), e );
        }
    }
}