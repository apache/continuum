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
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.continuum.buildagent.configuration.BuildAgentConfigurationException;
import org.apache.continuum.buildagent.configuration.BuildAgentConfigurationService;
import org.apache.continuum.buildagent.model.LocalRepository;
import org.apache.continuum.purge.executor.ContinuumPurgeExecutor;
import org.apache.continuum.purge.executor.ContinuumPurgeExecutorException;
import org.apache.continuum.purge.executor.RepositoryPurgeExecutorFactory;
import org.apache.continuum.purge.repository.content.RepositoryManagedContent;
import org.apache.continuum.purge.repository.content.RepositoryManagedContentFactory;
import org.apache.continuum.utils.m2.LocalRepositoryHelper;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;

@Component( role = org.apache.continuum.buildagent.manager.BuildAgentPurgeManager.class, hint = "default" )
public class DefaultBuildAgentPurgeManager
    implements BuildAgentPurgeManager
{
    private static final Logger log = LoggerFactory.getLogger( DefaultBuildAgentPurgeManager.class );

    @Requirement
    private BuildAgentConfigurationService buildAgentConfigurationService;

    @Requirement
    private RepositoryPurgeExecutorFactory repoExecutorFactory;

    @Requirement
    private RepositoryManagedContentFactory contentFactory;

    @Requirement
    private LocalRepositoryHelper localRepositoryHelper;

    public void executeDirectoryPurge( String directoryType, int daysOlder, int retentionCount, boolean deleteAll )
        throws Exception
    {
        if ( "working".equals( directoryType ) || "releases".equals( directoryType ) )
        {
            File directory = buildAgentConfigurationService.getWorkingDirectory();
            String path = directory.getPath();
            log.info( "purging directory '{}' [type={},full={},age={},retain={}]",
                      new Object[] { path, directoryType, deleteAll, daysOlder, retentionCount } );
            if ( deleteAll )
            {
                purgeAll( directory, directoryType );
            }
            else
            {
                purgeFiles( directory, directoryType, daysOlder, retentionCount );
            }
            log.info( "purge completed '{}'", path );
        }
        else
        {
            log.warn( "ignoring directory purge, directory type {} is invalid.", directoryType );
        }

    }

    private void purgeAll( File directory, String directoryType )
        throws Exception
    {
        AndFileFilter filter = new AndFileFilter();
        filter.addFileFilter( DirectoryFileFilter.DIRECTORY );
        filter.addFileFilter( createFileFilterForDirectoryType( directoryType ) );

        File[] files = directory.listFiles( (FileFilter) filter );
        if ( files == null )
        {
            return;
        }
        for ( File file : files )
        {
            try
            {
                FileUtils.deleteDirectory( file );
            }
            catch ( IOException e )
            {
                log.warn( "failed to purge {} directory {}: {}",
                          new Object[] { directoryType, file.getName(), e.getMessage() } );
            }
        }
    }

    private void purgeFiles( File directory, String directoryType, int daysOlder, int retentionCount )
    {
        AndFileFilter filter = new AndFileFilter();
        filter.addFileFilter( DirectoryFileFilter.DIRECTORY );
        filter.addFileFilter( createFileFilterForDirectoryType( directoryType ) );

        File[] files = directory.listFiles( (FileFilter) filter );

        if ( files == null )
        {
            return;
        }

        //calculate to include files not in the dayold category
        int countToPurge = files.length - retentionCount;

        if ( daysOlder > 0 )
        {
            long cutoff = System.currentTimeMillis() - ( 24 * 60 * 26 * 1000 * daysOlder );
            filter.addFileFilter( new AgeFileFilter( cutoff ) );
        }

        files = directory.listFiles( (FileFilter) filter );

        if ( files == null )
        {
            return;
        }

        Arrays.sort( files, LastModifiedFileComparator.LASTMODIFIED_COMPARATOR );

        for ( File file : files )
        {
            if ( countToPurge - 1 < 0 )
            {
                break;
            }
            try
            {
                FileUtils.deleteDirectory( file );
                countToPurge--;
            }
            catch ( IOException e )
            {
                log.warn( "failed to purge {} directory {}: {}",
                          new Object[] { directoryType, file.getName(), e.getMessage() } );
            }
        }

    }

    private IOFileFilter createFileFilterForDirectoryType( String directoryType )
    {
        WildcardFileFilter releasesFilter = new WildcardFileFilter( "releases-*" );

        if ( "working".equals( directoryType ) )
        {
            return new NotFileFilter( releasesFilter );
        }
        else if ( "releases".equals( directoryType ) )
        {
            return releasesFilter;
        }
        else
        {
            return null;
        }
    }

    private RepositoryManagedContent getManagedContent( LocalRepository localRepo )
        throws BuildAgentConfigurationException
    {
        String layout = localRepo.getLayout();
        try
        {
            RepositoryManagedContent managedContent = contentFactory.create( layout );
            managedContent.setRepository( localRepositoryHelper.convertAgentRepo( localRepo ) );
            return managedContent;
        }
        catch ( ComponentLookupException e )
        {
            throw new BuildAgentConfigurationException(
                String.format( "managed repo layout %s not found", layout ) );
        }
    }

    public void executeRepositoryPurge( String repoName, int daysOlder, int retentionCount, boolean deleteAll,
                                        boolean deleteReleasedSnapshots )
        throws ContinuumPurgeExecutorException
    {
        try
        {
            LocalRepository localRepo = buildAgentConfigurationService.getLocalRepositoryByName( repoName );
            String path = localRepo.getLocation(), layout = localRepo.getLayout();
            RepositoryManagedContent managedContent = getManagedContent( localRepo );
            ContinuumPurgeExecutor executor = repoExecutorFactory.create( deleteAll, daysOlder, retentionCount,
                                                                          deleteReleasedSnapshots, managedContent );
            log.info( "purging repo '{}' [full={},age={},retain={},snapshots={},layout={}]",
                      new Object[] { path, deleteAll, daysOlder, retentionCount, deleteReleasedSnapshots, layout } );
            executor.purge( path );
            log.info( "purge completed '{}'", path );
        }
        catch ( BuildAgentConfigurationException e )
        {
            log.warn( "ignoring repository purge, check agent repo configuration: {}", e.getMessage() );
        }
    }

    public void setBuildAgentConfigurationService( BuildAgentConfigurationService buildAgentConfigurationService )
    {
        this.buildAgentConfigurationService = buildAgentConfigurationService;
    }
}
