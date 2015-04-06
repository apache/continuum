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
import org.apache.continuum.model.repository.LocalRepository;
import org.apache.continuum.purge.controller.PurgeController;
import org.apache.continuum.purge.executor.ContinuumPurgeExecutorException;
import org.apache.continuum.purge.repository.utils.FileTypes;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.DirectoryWalker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Codes were taken from Archiva and made some changes.
 */
@Component( role = org.apache.continuum.purge.repository.scanner.RepositoryScanner.class, hint = "repository-scanner" )
public class DefaultRepositoryScanner
    implements RepositoryScanner
{

    @Requirement( hint = "file-types" )
    private FileTypes filetypes;

    public void scan( LocalRepository repository, PurgeController purgeController )
        throws ContinuumPurgeExecutorException
    {
        List<String> ignoredPatterns = filetypes.getIgnoredFileTypePatterns();
        scan( repository, purgeController, ignoredPatterns );
    }

    public void scan( LocalRepository repository, PurgeController purgeController, List<String> ignoredContentPatterns )
        throws ContinuumPurgeExecutorException
    {
        File repositoryBase = new File( repository.getLocation() );

        if ( !repositoryBase.exists() )
        {
            throw new UnsupportedOperationException(
                "Unable to scan a repository, directory " + repositoryBase.getAbsolutePath() + " does not exist." );
        }

        if ( !repositoryBase.isDirectory() )
        {
            throw new UnsupportedOperationException(
                "Unable to scan a repository, path " + repositoryBase.getAbsolutePath() + " is not a directory." );
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

        dirWalker.setBaseDir( repositoryBase );

        dirWalker.setIncludes( allIncludes );
        dirWalker.setExcludes( allExcludes );

        RepositoryScannerInstance scannerInstance = new RepositoryScannerInstance( repository, purgeController );

        dirWalker.addDirectoryWalkListener( scannerInstance );

        // Execute scan.
        dirWalker.scan();

    }
}