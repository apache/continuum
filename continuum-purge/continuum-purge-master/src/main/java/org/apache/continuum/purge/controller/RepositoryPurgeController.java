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
import java.util.Arrays;
import java.util.List;

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

    @Requirement( hint = "repository-scanner" )
    private RepositoryScanner scanner;

    public void purge( AbstractPurgeConfiguration purgeConfig )
    {
        RepositoryPurgeConfiguration config = (RepositoryPurgeConfiguration) purgeConfig;
        try
        {
            String path = config.getRepository().getLocation();
            RepositoryManagedContent repositoryContent = getManagedContent( config.getRepository().getId() );
            ContinuumPurgeExecutor executor = new RepositoryPurgeExecutorFactoryImpl( scanner )
                .create( config.isDeleteAll(), config.getDaysOlder(), config.getRetentionCount(),
                         config.isDeleteReleasedSnapshots(), repositoryContent );
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

interface RepositoryPurgeExecutorFactory
{
    ContinuumPurgeExecutor create( boolean deleteAll, int daysOld, int retentionCount, boolean deleteReleasedSnapshots,
                                   RepositoryManagedContent repoContent );
}

class MultiplexedPurgeExecutor
    implements ContinuumPurgeExecutor
{
    List<ContinuumPurgeExecutor> constituents;

    public MultiplexedPurgeExecutor( ContinuumPurgeExecutor... executors )
    {
        constituents = Arrays.asList( executors );
    }

    public void purge( String path )
        throws ContinuumPurgeExecutorException
    {
        for ( ContinuumPurgeExecutor child : constituents )
        {
            child.purge( path );
        }
    }
}

class ScanningPurgeExecutor
    implements ContinuumPurgeExecutor, ScannerHandler
{
    private static final Logger log = LoggerFactory.getLogger( ScanningPurgeExecutor.class );

    RepositoryScanner scanner;

    ContinuumPurgeExecutor executor;

    public ScanningPurgeExecutor( RepositoryScanner scanner, ContinuumPurgeExecutor executor )
    {
        this.scanner = scanner;
        this.executor = executor;
    }

    public void purge( String path )
        throws ContinuumPurgeExecutorException
    {
        scanner.scan( new File( path ), this );
    }

    public void handle( String path )
    {
        try
        {
            executor.purge( path );
        }
        catch ( ContinuumPurgeExecutorException e )
        {
            log.error( String.format( "handling failed %s: %s", path, e.getMessage() ), e );
        }
    }
}

class RepositoryPurgeExecutorFactoryImpl
    implements RepositoryPurgeExecutorFactory
{
    RepositoryScanner scanner;

    public RepositoryPurgeExecutorFactoryImpl( RepositoryScanner scanner )
    {
        this.scanner = scanner;
    }

    public ContinuumPurgeExecutor create( boolean deleteAll, int daysOld, int retentionCount,
                                          boolean deleteReleasedSnapshots, RepositoryManagedContent repoContent )
    {
        if ( deleteAll )
        {
            return new CleanAllPurgeExecutor( ContinuumPurgeConstants.PURGE_REPOSITORY );
        }

        ContinuumPurgeExecutor executor;
        if ( daysOld > 0 )
        {
            executor = new DaysOldRepositoryPurgeExecutor( repoContent, daysOld, retentionCount );
        }
        else
        {
            executor = new RetentionCountRepositoryPurgeExecutor( repoContent, retentionCount );
        }

        if ( deleteReleasedSnapshots )
        {
            ContinuumPurgeExecutor snapshots = new ReleasedSnapshotsRepositoryPurgeExecutor( repoContent );
            executor = new MultiplexedPurgeExecutor( snapshots, executor );
        }

        return new ScanningPurgeExecutor( scanner, executor );
    }
}