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

import java.io.File;
import java.io.IOException;

/**
 * @author Maria Catherine Tan
 */
public class CleanAllPurgeExecutor
    implements ContinuumPurgeExecutor
{
    private FileSystemManager fsManager;

    private final String purgeType;

    public CleanAllPurgeExecutor( FileSystemManager fsManager, String purgeType )
    {
        this.fsManager = fsManager;
        this.purgeType = purgeType;
    }

    public void purge( String path )
        throws ContinuumPurgeExecutorException
    {
        File dir = new File( path );
        try
        {
            if ( ContinuumPurgeConstants.PURGE_DIRECTORY_RELEASES.equals( purgeType ) )
            {
                PurgeBuilder.purge( dir )
                            .dirs()
                            .namedLike( ContinuumPurgeConstants.RELEASE_DIR_PATTERN )
                            .executeWith( new RemoveDirHandler( fsManager ) );
            }
            else if ( ContinuumPurgeConstants.PURGE_DIRECTORY_BUILDOUTPUT.equals( purgeType ) )
            {
                PurgeBuilder.purge( dir )
                            .dirs()
                            .executeWith( new WipeDirHandler( fsManager ) );
            }
            else if ( ContinuumPurgeConstants.PURGE_DIRECTORY_WORKING.equals( purgeType ) )
            {
                PurgeBuilder.purge( dir )
                            .dirs()
                            .notNamedLike( ContinuumPurgeConstants.RELEASE_DIR_PATTERN )
                            .executeWith( new RemoveDirHandler( fsManager ) );
            }
        }
        catch ( PurgeBuilderException e )
        {
            throw new ContinuumPurgeExecutorException( "purge failed: " + e.getMessage() );
        }
    }
}

class WipeDirHandler
    implements Handler
{
    FileSystemManager fsManager;

    WipeDirHandler( FileSystemManager fsManager )
    {
        this.fsManager = fsManager;
    }

    public void handle( File dir )
    {
        try
        {
            fsManager.wipeDir( dir );
        }
        catch ( IOException e )
        {
            //swallow?
        }
    }
}