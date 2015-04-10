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

import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.lang.time.DateUtils;
import org.apache.continuum.purge.ContinuumPurgeConstants;
import org.apache.continuum.purge.executor.ContinuumPurgeExecutor;
import org.apache.continuum.purge.executor.ContinuumPurgeExecutorException;
import org.apache.continuum.utils.file.FileSystemManager;
import org.apache.maven.archiva.consumers.core.repository.ArtifactFilenameFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;

/**
 * @author Maria Catherine Tan
 */
public class DaysOldDirectoryPurgeExecutor
    implements ContinuumPurgeExecutor
{
    private Logger log = LoggerFactory.getLogger( DaysOldDirectoryPurgeExecutor.class );

    private final FileSystemManager fsManager;

    private final int daysOlder;

    private final int retentionCount;

    private final String directoryType;

    public DaysOldDirectoryPurgeExecutor( FileSystemManager fsManager, int daysOlder, int retentionCount,
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
        if ( directoryType.equals( ContinuumPurgeConstants.PURGE_DIRECTORY_RELEASES ) )
        {
            purgeReleaseDirectory( path );
        }
        else if ( directoryType.equals( ContinuumPurgeConstants.PURGE_DIRECTORY_BUILDOUTPUT ) )
        {
            purgeBuildOutputDirectory( path );
        }
    }

    private void purgeReleaseDirectory( String path )
    {
        File releaseDir = new File( path );
        FilenameFilter filter = new ArtifactFilenameFilter( "releases-" );
        File[] releasesDir = releaseDir.listFiles( filter );

        if ( releasesDir == null || retentionCount > releasesDir.length )
        {
            return;
        }

        Arrays.sort( releasesDir, LastModifiedFileComparator.LASTMODIFIED_COMPARATOR );

        Calendar olderThanThisDate = Calendar.getInstance( DateUtils.UTC_TIME_ZONE );
        olderThanThisDate.add( Calendar.DATE, -daysOlder );

        int countToPurge = releasesDir.length - retentionCount;

        for ( File dir : releasesDir )
        {
            if ( countToPurge <= 0 )
            {
                break;
            }

            if ( dir.lastModified() < olderThanThisDate.getTimeInMillis() )
            {
                try
                {
                    log.info( ContinuumPurgeConstants.PURGE_DIR_CONTENTS + " - " + dir.getName() );
                    fsManager.removeDir( dir );
                    countToPurge--;
                }
                catch ( IOException e )
                {
                    //throw new ContinuumPurgeExecutorException( "Error while purging release directories", e );
                }
            }
        }
    }

    private void purgeBuildOutputDirectory( String path )
    {
        File buildOutputDir = new File( path );
        FileFilter filter = DirectoryFileFilter.DIRECTORY;
        File[] projectsDir = buildOutputDir.listFiles( filter );

        for ( File projectDir : projectsDir )
        {
            File[] buildsDir = projectDir.listFiles( filter );
            if ( retentionCount > buildsDir.length )
            {
                continue;
            }

            int countToPurge = buildsDir.length - retentionCount;
            Calendar olderThanThisDate = Calendar.getInstance( DateUtils.UTC_TIME_ZONE );
            olderThanThisDate.add( Calendar.DATE, -daysOlder );

            Arrays.sort( buildsDir, LastModifiedFileComparator.LASTMODIFIED_COMPARATOR );

            for ( File buildDir : buildsDir )
            {
                if ( countToPurge <= 0 )
                {
                    break;
                }

                if ( buildDir.lastModified() < olderThanThisDate.getTimeInMillis() )
                {
                    try
                    {
                        log.info( ContinuumPurgeConstants.PURGE_DIR_CONTENTS + " - " + buildDir.getName() );
                        fsManager.removeDir( buildDir );
                        File logFile = new File( buildDir.getAbsoluteFile() + ".log.txt" );
                        if ( logFile.exists() )
                        {
                            log.info( ContinuumPurgeConstants.PURGE_FILE + " - " + logFile.getName() );
                            logFile.delete();
                        }
                        countToPurge--;
                    }
                    catch ( IOException e )
                    {
                        // swallow?
                    }
                }
            }
        }
    }
}
