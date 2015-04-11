package org.apache.continuum.purge.executor.repo;

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

import org.apache.continuum.purge.executor.ContinuumPurgeExecutor;
import org.apache.continuum.purge.executor.ContinuumPurgeExecutorException;
import org.apache.continuum.purge.repository.content.RepositoryManagedContent;
import org.apache.continuum.purge.repository.scanner.RepositoryScanner;
import org.apache.continuum.utils.file.FileSystemManager;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import java.io.File;
import java.io.IOException;

@Component( role = RepositoryPurgeExecutorFactory.class )
public class RepositoryPurgeExecutorFactoryImpl
    implements RepositoryPurgeExecutorFactory
{
    @Requirement( hint = "purge" )
    private RepositoryScanner scanner;

    @Requirement
    private FileSystemManager fsManager;

    public ContinuumPurgeExecutor create( boolean deleteAll, int daysOld, int retentionCount,
                                          boolean deleteReleasedSnapshots, RepositoryManagedContent repoContent )
    {
        if ( deleteAll )
        {
            return new CleanAllPurgeExecutor( fsManager );
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

class CleanAllPurgeExecutor
    implements ContinuumPurgeExecutor
{

    FileSystemManager fsManager;

    CleanAllPurgeExecutor( FileSystemManager fsManager )
    {
        this.fsManager = fsManager;
    }

    public void purge( String path )
        throws ContinuumPurgeExecutorException
    {
        try
        {
            fsManager.wipeDir( new File( path ) );
        }
        catch ( IOException e )
        {
            throw new ContinuumPurgeExecutorException( "failed to remove repo" + path, e );
        }
    }
}