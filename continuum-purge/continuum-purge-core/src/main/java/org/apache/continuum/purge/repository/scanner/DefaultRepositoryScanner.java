package org.apache.continuum.purge.repository.scanner;

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

import org.apache.commons.collections.CollectionUtils;
import org.apache.continuum.purge.executor.ContinuumPurgeExecutorException;
import org.apache.continuum.purge.repository.utils.FileTypes;
import org.apache.maven.archiva.common.utils.BaseFile;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.DirectoryWalkListener;
import org.codehaus.plexus.util.DirectoryWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Codes were taken from Archiva and made some changes.
 */
@Component( role = RepositoryScanner.class, hint = "purge" )
public class DefaultRepositoryScanner
    implements RepositoryScanner
{

    @Requirement( hint = "file-types" )
    private FileTypes filetypes;

    public void scan( File repoLocation, ScannerHandler handler )
        throws ContinuumPurgeExecutorException
    {
        List<String> ignoredPatterns = filetypes.getIgnoredFileTypePatterns();
        scan( repoLocation, handler, ignoredPatterns );
    }

    public void scan( File repositoryLocation, ScannerHandler handler, List<String> ignoredContentPatterns )
        throws ContinuumPurgeExecutorException
    {

        if ( !repositoryLocation.exists() )
        {
            throw new UnsupportedOperationException(
                "Unable to scan a repository, directory " + repositoryLocation.getAbsolutePath() + " does not exist." );
        }

        if ( !repositoryLocation.isDirectory() )
        {
            throw new UnsupportedOperationException(
                "Unable to scan a repository, path " + repositoryLocation.getAbsolutePath() + " is not a directory." );
        }

        // Setup Includes / Excludes.

        List<String> allExcludes = new ArrayList<String>();
        List<String> allIncludes = new ArrayList<String>();

        if ( CollectionUtils.isNotEmpty( ignoredContentPatterns ) )
        {
            allExcludes.addAll( ignoredContentPatterns );
        }

        // Scan All Content. (intentional)
        allIncludes.add( "**/*" );

        // Setup Directory Walker
        DirectoryWalker dirWalker = new DirectoryWalker();

        dirWalker.setBaseDir( repositoryLocation );

        dirWalker.setIncludes( allIncludes );
        dirWalker.setExcludes( allExcludes );

        ScanListener listener = new ScanListener( repositoryLocation, handler );

        dirWalker.addDirectoryWalkListener( listener );

        // Execute scan.
        dirWalker.scan();
    }
}

class ScanListener
    implements DirectoryWalkListener
{
    private static final Logger log = LoggerFactory.getLogger( ScanListener.class );

    private final File repository;

    private final ScannerHandler handler;

    public ScanListener( File repoLocation, ScannerHandler handler )
    {
        this.repository = repoLocation;
        this.handler = handler;
    }

    public void debug( String message )
    {
        log.debug( "Repository Scanner: " + message );
    }

    public void directoryWalkFinished()
    {
        log.info( "scan stopped: {}", repository );
    }

    public void directoryWalkStarting( File file )
    {
        log.info( "scan started: {}", repository );
    }

    public void directoryWalkStep( int percentage, File file )
    {
        BaseFile basefile = new BaseFile( repository, file );
        handler.handle( basefile.getRelativePath() );
    }
}