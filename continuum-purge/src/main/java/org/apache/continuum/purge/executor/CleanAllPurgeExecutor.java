package org.apache.continuum.purge.executor;

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

import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.continuum.purge.ContinuumPurgeConstants;
import org.apache.continuum.utils.file.FileSystemManager;
import org.apache.maven.archiva.consumers.core.repository.ArtifactFilenameFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;

/**
 * @author Maria Catherine Tan
 */
public class CleanAllPurgeExecutor
    implements ContinuumPurgeExecutor
{
    private Logger log = LoggerFactory.getLogger( CleanAllPurgeExecutor.class );

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
        if ( purgeType.equals( ContinuumPurgeConstants.PURGE_REPOSITORY ) )
        {
            purgeRepository( path );
        }
        else if ( purgeType.equals( ContinuumPurgeConstants.PURGE_DIRECTORY_RELEASES ) )
        {
            purgeReleases( path );
        }
        else if ( purgeType.equals( ContinuumPurgeConstants.PURGE_DIRECTORY_BUILDOUTPUT ) )
        {
            purgeBuildOutput( path );
        }
    }

    private void purgeRepository( String path )
        throws ContinuumPurgeExecutorException
    {
        try
        {
            fsManager.wipeDir( new File( path ) );
        }
        catch ( IOException e )
        {
            throw new ContinuumPurgeExecutorException( "Error while purging all artifacts or directories in " + path,
                                                       e );
        }
        log.info( ContinuumPurgeConstants.PURGE_REPO_CONTENTS + " - " + path );
    }

    private void purgeReleases( String path )
        throws ContinuumPurgeExecutorException
    {
        File workingDir = new File( path );
        FilenameFilter filter = new ArtifactFilenameFilter( "releases-" );
        File[] releasesDir = workingDir.listFiles( filter );
        try
        {
            for ( File releaseDir : releasesDir )
            {
                fsManager.removeDir( releaseDir );
                log.info( ContinuumPurgeConstants.PURGE_DIR_CONTENTS + " - " + releaseDir.getName() );
            }
        }
        catch ( IOException e )
        {
            throw new ContinuumPurgeExecutorException( "Error while purging all releases directories", e );
        }
    }

    private void purgeBuildOutput( String path )
        throws ContinuumPurgeExecutorException
    {
        File buildOutputDir = new File( path );
        FileFilter filter = DirectoryFileFilter.DIRECTORY;
        File[] projectsDir = buildOutputDir.listFiles( filter );
        try
        {
            for ( File projectDir : projectsDir )
            {
                fsManager.wipeDir( projectDir );
                log.info( ContinuumPurgeConstants.PURGE_DIR_CONTENTS + " - " + projectDir.getName() );
            }
        }
        catch ( IOException e )
        {
            throw new ContinuumPurgeExecutorException( "Error while purging all buildOutput directories", e );
        }
    }
}
