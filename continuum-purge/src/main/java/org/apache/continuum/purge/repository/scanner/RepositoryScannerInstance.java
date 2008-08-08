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

import org.apache.continuum.model.repository.LocalRepository;
import org.apache.continuum.purge.controller.PurgeController;
import org.apache.maven.archiva.common.utils.BaseFile;
import org.codehaus.plexus.util.DirectoryWalkListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Codes were taken from Archiva and made some few changes
 */
public class RepositoryScannerInstance
    implements DirectoryWalkListener
{
    private Logger log = LoggerFactory.getLogger( RepositoryScannerInstance.class );

    private LocalRepository repository;

    private PurgeController purgeController;

    public RepositoryScannerInstance( LocalRepository repository, PurgeController purgeController )
    {
        this.repository = repository;
        this.purgeController = purgeController;
    }

    public void debug( String message )
    {
        log.debug( "Repository Scanner: " + message );
    }

    public void directoryWalkFinished()
    {
        log.info( "Walk Finished: [" + this.repository.getId() + "] " + this.repository.getLocation() );
    }

    public void directoryWalkStarting( File file )
    {
        log.info( "Walk started [" + this.repository.getId() + "] " + this.repository.getLocation() );
    }

    public void directoryWalkStep( int percentage, File file )
    {
        BaseFile basefile = new BaseFile( repository.getLocation(), file );
        purgeController.doPurge( basefile.getRelativePath() );
    }

}