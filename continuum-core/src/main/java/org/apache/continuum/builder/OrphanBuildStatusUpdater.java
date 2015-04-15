package org.apache.continuum.builder;

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

import org.apache.continuum.builder.distributed.work.BuildStatusUpdater;
import org.apache.continuum.dao.BuildResultDao;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component( role = BuildStatusUpdater.class, hint = "orphans" )
public class OrphanBuildStatusUpdater
    implements BuildStatusUpdater
{
    private static final Logger log = LoggerFactory.getLogger( OrphanBuildStatusUpdater.class );

    @Requirement
    private BuildResultDao buildResultDao;

    public void performScan()
    {
        try
        {
            log.info( "scanning for orphaned in-progress build results" );
            int updated = buildResultDao.resolveOrphanedInProgressResults();
            log.info( "finished: fixed {} results", updated );
        }
        catch ( ContinuumStoreException e )
        {
            log.warn( "failed to resolve orphaned build results: ", e.getMessage() );
        }
    }
}
