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

import org.apache.continuum.purge.executor.ContinuumPurgeExecutor;
import org.apache.continuum.purge.executor.ContinuumPurgeExecutorException;
import org.apache.continuum.utils.file.FileSystemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import static org.apache.continuum.purge.ContinuumPurgeConstants.*;

/**
 * @author Maria Catherine Tan
 */
public class DirectoryPurgeExecutor
    implements ContinuumPurgeExecutor
{
    private Logger log = LoggerFactory.getLogger( DirectoryPurgeExecutor.class );

    private final FileSystemManager fsManager;

    private final int daysOlder;

    private final int retentionCount;

    private final String directoryType;

    public DirectoryPurgeExecutor( FileSystemManager fsManager, int daysOlder, int retentionCount,
                                   String directoryType )
    {
        this.fsManager = fsManager;
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
            if ( PURGE_DIRECTORY_RELEASES.equals( directoryType ) )
            {
                PurgeBuilder.purge( dir )
                            .dirs()
                            .namedLike( RELEASE_DIR_PATTERN )
                            .olderThan( daysOlder )
                            .inAgeOrder()
                            .retainLast( retentionCount )
                            .executeWith( new RemoveDirHandler( fsManager ) );
            }
            else if ( PURGE_DIRECTORY_WORKING.equals( directoryType ) )
            {
                PurgeBuilder.purge( dir )
                            .dirs()
                            .notNamedLike( RELEASE_DIR_PATTERN )
                            .olderThan( daysOlder )
                            .inAgeOrder()
                            .retainLast( retentionCount )
                            .executeWith( new RemoveDirHandler( fsManager ) );
            }
            else if ( PURGE_DIRECTORY_BUILDOUTPUT.equals( directoryType ) )
            {
                for ( File projectDir : dir.listFiles() )
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
        catch ( PurgeBuilderException pbe )
        {
            throw new ContinuumPurgeExecutorException( "purge failed: " + pbe.getMessage() );
        }
    }
}

class BuildOutputHandler
    implements Handler
{
    FileSystemManager fsManager;

    BuildOutputHandler( FileSystemManager fsManager )
    {
        this.fsManager = fsManager;
    }

    public void handle( File dir )
    {
        try
        {
            fsManager.removeDir( dir );
            File logFile = new File( dir.getAbsoluteFile() + ".log.txt" );
            if ( logFile.exists() )
            {
                logFile.delete();
            }
        }
        catch ( IOException e )
        {
            //swallow?
        }
    }
}