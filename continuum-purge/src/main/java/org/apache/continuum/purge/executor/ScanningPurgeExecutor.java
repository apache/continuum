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

import org.apache.continuum.purge.repository.scanner.RepositoryScanner;
import org.apache.continuum.purge.repository.scanner.ScannerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class ScanningPurgeExecutor
    implements ContinuumPurgeExecutor, ScannerHandler
{
    private static final Logger log = LoggerFactory.getLogger( ScanningPurgeExecutor.class );

    private RepositoryScanner scanner;

    private ContinuumPurgeExecutor executor;

    public ScanningPurgeExecutor( RepositoryScanner scanner, ContinuumPurgeExecutor executor )
    {
        this.scanner = scanner;
        this.executor = executor;
    }

    public void purge( String path )
        throws ContinuumPurgeExecutorException
    {
        scanner.scan( new File( path ), this );
    }

    public void handle( String path )
    {
        try
        {
            executor.purge( path );
        }
        catch ( ContinuumPurgeExecutorException e )
        {
            log.error( String.format( "handling failed %s: %s", path, e.getMessage() ), e );
        }
    }
}
