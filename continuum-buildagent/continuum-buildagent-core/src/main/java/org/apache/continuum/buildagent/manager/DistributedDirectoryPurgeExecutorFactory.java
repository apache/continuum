package org.apache.continuum.buildagent.manager;

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
import org.apache.commons.io.filefilter.AgeFileFilter;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.continuum.purge.executor.ContinuumPurgeExecutor;
import org.apache.continuum.purge.executor.ContinuumPurgeExecutorException;
import org.apache.continuum.purge.executor.DirectoryPurgeExecutorFactory;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Enables creation of purge executors for distributed agent working directories.
 */
@Component( role = DirectoryPurgeExecutorFactory.class, hint = "distributed" )
public class DistributedDirectoryPurgeExecutorFactory
    implements DirectoryPurgeExecutorFactory
{
    private Set<String> supportedTypes = new HashSet<String>();

    public DistributedDirectoryPurgeExecutorFactory()
    {
        supportedTypes.add( AbstractPurgeExecutor.RELEASE_TYPE );
        supportedTypes.add( AbstractPurgeExecutor.WORKING_TYPE );
    }

    /**
     * Creates an {@link ContinuumPurgeExecutor} appropriate for the specified parameters.
     *
     * @param deleteAll      whether to delete all files in the directory, when true other params are ignored
     * @param daysOld        file age in days, only files older than this will be considered for deletion
     * @param retentionCount number of of deletion candidates to keep
     * @param dirType        directory type considered, one of "working" or "releases", others are safely ignored.
     * @return an executor, safe for a single use
     */
    public ContinuumPurgeExecutor create( boolean deleteAll, int daysOld, int retentionCount, String dirType )
    {
        if ( supportedTypes.contains( dirType ) )
        {
            if ( deleteAll )
            {
                return new DeleteAllPurgeExecutor( dirType );
            }
            return new DefaultPurgeExecutor( daysOld, retentionCount, dirType );
        }
        return new UnsupportedPurgeExecutor( dirType );
    }
}

/**
 * Encapsulates how to purge both working and release directories. This can and probably should be simplified more and
 * combined with the code in master into a single file system purge executor.
 */
abstract class AbstractPurgeExecutor
    implements ContinuumPurgeExecutor
{
    private static final Logger log = LoggerFactory.getLogger( AbstractPurgeExecutor.class );

    public static final String WORKING_TYPE = "working";

    public static final String RELEASE_TYPE = "releases";

    protected String type;

    AbstractPurgeExecutor( String dirType )
    {
        this.type = dirType;
    }

    public void purge( String path )
    {
        File directory = new File( path );
        if ( !directory.exists() )
        {
            log.warn( "skipping purge, directory '{}' does not exist", directory );
            return;
        }
        if ( !directory.isDirectory() )
        {
            log.warn( "skipping purge, specified path '{}' is not a directory" );
            return;
        }
        purgeDir( directory );
    }

    abstract void purgeDir( File directory );

    /**
     * Lists files in the given directory, using the specified filter.
     *
     * @param directory the directory to list
     * @param filter    the filter to use on the list
     * @return an non-null array of resulting files
     */
    protected File[] listFiles( File directory, FileFilter filter )
    {
        File[] files = directory.listFiles( filter );
        if ( files == null )
        {
            return new File[] {};
        }
        return files;
    }

    /**
     * Creates an extensible filter based on the rules for supported distributed directory types:
     * <ul>
     * <li>releases - directories named releases-*</li>
     * <li>working - directories not named releases-*</li>
     * </ul>
     *
     * @param directoryType one of "working" or "releases"
     * @return a configured AndFileFilter, which can be augmented by adding additional filters
     */
    protected AndFileFilter createFilter( String directoryType )
    {
        AndFileFilter resultFilter = new AndFileFilter();
        resultFilter.addFileFilter( DirectoryFileFilter.DIRECTORY );
        WildcardFileFilter releasesFilter = new WildcardFileFilter( "releases-*" );
        if ( WORKING_TYPE.equals( directoryType ) )
        {
            resultFilter.addFileFilter( new NotFileFilter( releasesFilter ) );
        }
        else
        {
            resultFilter.addFileFilter( releasesFilter );
        }
        return resultFilter;
    }
}

class DeleteAllPurgeExecutor
    extends AbstractPurgeExecutor
{
    private static final Logger log = LoggerFactory.getLogger( DeleteAllPurgeExecutor.class );

    DeleteAllPurgeExecutor( String dirType )
    {
        super( dirType );
    }

    public void purgeDir( File directory )
    {
        for ( File file : listFiles( directory, type ) )
        {
            try
            {
                FileUtils.deleteDirectory( file );
            }
            catch ( IOException e )
            {
                log.warn( "failed to purge {} directory {}: {}",
                          new Object[] { type, file.getName(), e.getMessage() } );
            }
        }
    }

    private File[] listFiles( File directory, String directoryType )
    {
        return listFiles( directory, createFilter( directoryType ) );
    }
}

class DefaultPurgeExecutor
    extends AbstractPurgeExecutor
{
    private static Logger log = LoggerFactory.getLogger( DefaultPurgeExecutor.class );

    private int daysOld;

    private int retentionCount;

    DefaultPurgeExecutor( int daysOld, int retentionCount, String dirType )
    {
        super( dirType );
        this.daysOld = daysOld;
        this.retentionCount = retentionCount;
    }

    public void purgeDir( File directory )
    {
        purgeFiles( directory );
    }

    private void purgeFiles( File directory )
    {
        File[] files = listFiles( directory, type, daysOld );
        int remaining = files.length - retentionCount;
        for ( File file : files )
        {
            if ( remaining <= 0 )
            {
                break;
            }
            try
            {
                FileUtils.deleteDirectory( file );
                remaining--;
            }
            catch ( IOException e )
            {
                log.warn( "failed to purge {} directory {}: {}",
                          new Object[] { type, file.getName(), e.getMessage() } );
            }
        }
    }

    /**
     * Returns a sorted list of files for the specified directory type and age.
     *
     * @param directory     the directory to list
     * @param directoryType one of "working" or "releases"
     * @param daysOld       min age of file in days to accept, 0 to disable age filtering
     * @return non-null array of files, sorted from oldest-to-newest
     */
    private File[] listFiles( File directory, String directoryType, int daysOld )
    {
        AndFileFilter filter = createFilter( directoryType );
        if ( daysOld > 0 )
        {
            long cutoff = System.currentTimeMillis() - ( 24 * 60 * 26 * 1000 * daysOld );
            filter.addFileFilter( new AgeFileFilter( cutoff ) );
        }
        File[] files = listFiles( directory, filter );
        Arrays.sort( files, LastModifiedFileComparator.LASTMODIFIED_COMPARATOR );
        return files;
    }
}

/**
 * A stub executor for handling unknown directory types.
 */
class UnsupportedPurgeExecutor
    implements ContinuumPurgeExecutor
{
    private static Logger log = LoggerFactory.getLogger( UnsupportedPurgeExecutor.class );

    private String type;

    UnsupportedPurgeExecutor( String dirType )
    {
        this.type = dirType;
    }

    public void purge( String path )
        throws ContinuumPurgeExecutorException
    {
        log.warn( "ignoring directory purge, directory type {} is not supported.", type );
    }
}