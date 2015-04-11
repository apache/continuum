package org.apache.continuum.purge.executor.dir;

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

import org.apache.continuum.purge.ContinuumPurgeConstants;
import org.apache.continuum.purge.executor.ContinuumPurgeExecutor;
import org.apache.continuum.purge.executor.ContinuumPurgeExecutorException;
import org.apache.continuum.utils.file.FileSystemManager;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import static org.apache.commons.io.filefilter.DirectoryFileFilter.DIRECTORY;
import static org.apache.continuum.purge.ContinuumPurgeConstants.*;

@Component( role = DirectoryPurgeExecutorFactory.class )
public class DirectoryPurgeExecutorFactoryImpl
    implements DirectoryPurgeExecutorFactory
{
    @Requirement
    private FileSystemManager fsManager;

    public ContinuumPurgeExecutor create( boolean deleteAll, int daysOld, int retentionCount, String dirType )
    {
        if ( PURGE_DIRECTORY_RELEASES.equals( dirType ) )
        {
            return new ReleasesPurgeExecutor( fsManager, deleteAll, daysOld, retentionCount, dirType );
        }
        if ( PURGE_DIRECTORY_WORKING.equals( dirType ) )
        {
            return new WorkingPurgeExecutor( fsManager, deleteAll, daysOld, retentionCount, dirType );
        }
        if ( PURGE_DIRECTORY_BUILDOUTPUT.equals( dirType ) )
        {
            return new BuildOutputPurgeExecutor( fsManager, deleteAll, daysOld, retentionCount, dirType );
        }
        return new UnsupportedPurgeExecutor( dirType );
    }
}

class UnsupportedPurgeExecutor
    implements ContinuumPurgeExecutor
{
    private static Logger log = LoggerFactory.getLogger( UnsupportedPurgeExecutor.class );

    private String dirType;

    UnsupportedPurgeExecutor( String dirType )
    {
        this.dirType = dirType;
    }

    public void purge( String path )
        throws ContinuumPurgeExecutorException
    {
        log.warn( "ignoring purge request, directory type {} no supported", dirType );
    }
}

abstract class AbstractPurgeExecutor
    implements ContinuumPurgeExecutor
{
    protected final FileSystemManager fsManager;

    protected boolean deleteAll;

    protected final int daysOlder;

    protected final int retentionCount;

    protected final String directoryType;

    AbstractPurgeExecutor( FileSystemManager fsManager, boolean deleteAll, int daysOlder, int retentionCount,
                           String directoryType )
    {
        this.fsManager = fsManager;
        this.deleteAll = deleteAll;
        this.daysOlder = daysOlder;
        this.retentionCount = retentionCount;
        this.directoryType = directoryType;
    }

    public void purge( String path )
        throws ContinuumPurgeExecutorException
    {
        try
        {
            File dir = new File( path );
            purge( dir );
        }
        catch ( PurgeBuilderException e )
        {
            throw new ContinuumPurgeExecutorException( "purge failed: " + e.getMessage() );
        }
    }

    abstract void purge( File dir )
        throws PurgeBuilderException;
}

class ReleasesPurgeExecutor
    extends AbstractPurgeExecutor
{
    ReleasesPurgeExecutor( FileSystemManager fsManager, boolean deleteAll, int daysOlder, int retentionCount,
                           String directoryType )
    {
        super( fsManager, deleteAll, daysOlder, retentionCount, directoryType );
    }

    @Override
    void purge( File dir )
        throws PurgeBuilderException
    {
        if ( deleteAll )
        {
            PurgeBuilder.purge( dir )
                        .dirs()
                        .namedLike( ContinuumPurgeConstants.RELEASE_DIR_PATTERN )
                        .executeWith( new RemoveDirHandler( fsManager ) );
        }
        else
        {
            PurgeBuilder.purge( dir )
                        .dirs()
                        .namedLike( RELEASE_DIR_PATTERN )
                        .olderThan( daysOlder )
                        .inAgeOrder()
                        .retainLast( retentionCount )
                        .executeWith( new RemoveDirHandler( fsManager ) );
        }
    }
}

class BuildOutputPurgeExecutor
    extends AbstractPurgeExecutor
{
    BuildOutputPurgeExecutor( FileSystemManager fsManager, boolean deleteAll, int daysOlder, int retentionCount,
                              String directoryType )
    {
        super( fsManager, deleteAll, daysOlder, retentionCount, directoryType );
    }

    @Override
    void purge( File dir )
        throws PurgeBuilderException
    {
        if ( deleteAll )
        {
            PurgeBuilder.purge( dir )
                        .dirs()
                        .executeWith( new WipeDirHandler( fsManager ) );
        }
        else
        {
            File[] projectDirs = dir.listFiles( (FileFilter) DIRECTORY );

            if ( projectDirs == null )
                return;

            for ( File projectDir : projectDirs )
            {
                if ( projectDir.isDirectory() )
                {
                    PurgeBuilder.purge( projectDir )
                                .dirs()
                                .olderThan( daysOlder )
                                .inAgeOrder()
                                .retainLast( retentionCount )
                                .executeWith( new BuildOutputHandler( fsManager ) );
                }
            }
        }
    }
}

class WorkingPurgeExecutor
    extends AbstractPurgeExecutor
{
    WorkingPurgeExecutor( FileSystemManager fsManager, boolean deleteAll, int daysOlder, int retentionCount,
                          String directoryType )
    {
        super( fsManager, deleteAll, daysOlder, retentionCount, directoryType );
    }

    @Override
    void purge( File dir )
        throws PurgeBuilderException
    {
        if ( deleteAll )
        {
            PurgeBuilder.purge( dir )
                        .dirs()
                        .notNamedLike( ContinuumPurgeConstants.RELEASE_DIR_PATTERN )
                        .executeWith( new RemoveDirHandler( fsManager ) );
        }
        else
        {
            PurgeBuilder.purge( dir )
                        .dirs()
                        .notNamedLike( RELEASE_DIR_PATTERN )
                        .olderThan( daysOlder )
                        .inAgeOrder()
                        .retainLast( retentionCount )
                        .executeWith( new RemoveDirHandler( fsManager ) );
        }
    }
}

abstract class AbstractFSHandler
    implements Handler
{
    private Logger log = LoggerFactory.getLogger( getClass() );

    protected FileSystemManager fsManager;

    AbstractFSHandler( FileSystemManager fsManager )
    {
        this.fsManager = fsManager;
    }

    public void handle( File dir )
    {
        try
        {
            handleFile( dir );
        }
        catch ( IOException e )
        {
            log.warn( "failed to purge file {}: {}", dir, e.getMessage() );
        }
    }

    abstract void handleFile( File dir )
        throws IOException;
}

class WipeDirHandler
    extends AbstractFSHandler
{
    WipeDirHandler( FileSystemManager fsManager )
    {
        super( fsManager );
    }

    public void handleFile( File dir )
        throws IOException
    {
        fsManager.wipeDir( dir );
    }
}

class BuildOutputHandler
    extends AbstractFSHandler
{
    BuildOutputHandler( FileSystemManager fsManager )
    {
        super( fsManager );
    }

    public void handleFile( File dir )
        throws IOException
    {
        fsManager.removeDir( dir );
        File logFile = new File( dir.getAbsoluteFile() + ".log.txt" );
        if ( logFile.exists() )
        {
            logFile.delete();
        }
    }
}

class RemoveDirHandler
    extends AbstractFSHandler
{
    RemoveDirHandler( FileSystemManager fsManager )
    {
        super( fsManager );
    }

    public void handleFile( File dir )
        throws IOException
    {
        fsManager.removeDir( dir );
    }
}