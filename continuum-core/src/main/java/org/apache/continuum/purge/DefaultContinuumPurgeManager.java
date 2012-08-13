package org.apache.continuum.purge;

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

import org.apache.continuum.buildmanager.BuildsManager;
import org.apache.continuum.model.repository.DirectoryPurgeConfiguration;
import org.apache.continuum.model.repository.DistributedDirectoryPurgeConfiguration;
import org.apache.continuum.model.repository.LocalRepository;
import org.apache.continuum.model.repository.RepositoryPurgeConfiguration;
import org.apache.continuum.purge.task.PurgeTask;
import org.apache.continuum.taskqueue.manager.TaskQueueManager;
import org.apache.continuum.taskqueue.manager.TaskQueueManagerException;
import org.apache.maven.continuum.build.settings.SchedulesActivationException;
import org.apache.maven.continuum.build.settings.SchedulesActivator;
import org.apache.maven.continuum.model.project.Schedule;
import org.codehaus.plexus.taskqueue.TaskQueueException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * DefaultContinuumPurgeManager
 *
 * @author Maria Catherine Tan
 * @version $Id$
 * @plexus.component role="org.apache.continuum.purge.ContinuumPurgeManager" role-hint="default"
 * @since 25 jul 07
 */
public class DefaultContinuumPurgeManager
    implements ContinuumPurgeManager
{
    private static final Logger log = LoggerFactory.getLogger( DefaultContinuumPurgeManager.class );

    /**
     * @plexus.requirement
     */
    private SchedulesActivator schedulesActivator;

    /**
     * @plexus.requirement
     */
    private PurgeConfigurationService purgeConfigurationService;

    /**
     * @plexus.requirement
     */
    private TaskQueueManager taskQueueManager;

    /**
     * @plexus.requirement role-hint="parallel"
     */
    private BuildsManager parallelBuildsManager;

    public void purge( Schedule schedule )
        throws ContinuumPurgeManagerException
    {
        List<RepositoryPurgeConfiguration> repoPurgeList = null;
        List<DirectoryPurgeConfiguration> dirPurgeList = null;
        List<DistributedDirectoryPurgeConfiguration> distributedDirPurgeList = null;

        repoPurgeList = purgeConfigurationService.getEnableRepositoryPurgeConfigurationsBySchedule( schedule.getId() );
        dirPurgeList = purgeConfigurationService.getEnableDirectoryPurgeConfigurationsBySchedule( schedule.getId() );
        distributedDirPurgeList = purgeConfigurationService.getEnableDistributedDirectoryPurgeConfigurationsBySchedule(
            schedule.getId() );

        boolean hasRepoPurge = repoPurgeList != null && repoPurgeList.size() > 0;
        boolean hasDirPurge = dirPurgeList != null && dirPurgeList.size() > 0;
        boolean hasDitributedDirPurge = distributedDirPurgeList != null && distributedDirPurgeList.size() > 0;

        if ( hasRepoPurge )
        {
            for ( RepositoryPurgeConfiguration repoPurge : repoPurgeList )
            {
                purgeRepository( repoPurge );
            }
        }

        if ( hasDirPurge )
        {
            for ( DirectoryPurgeConfiguration dirPurge : dirPurgeList )
            {
                purgeDirectory( dirPurge );
            }
        }

        if ( hasDitributedDirPurge )
        {
            for ( DistributedDirectoryPurgeConfiguration dirPurge : distributedDirPurgeList )
            {
                purgeDistributedDirectory( dirPurge );
            }
        }

        if ( !hasRepoPurge && !hasDirPurge && !hasDitributedDirPurge )
        {
            // This purge is not enable for a purge process.
            try
            {
                schedulesActivator.unactivateOrphanPurgeSchedule( schedule );
            }
            catch ( SchedulesActivationException e )
            {
                log.debug( String.format( "Can't unactivate orphan schedule '%s' for purgeConfiguration",
                                          schedule.getName() ) );
            }
        }
    }

    public void purgeRepository( RepositoryPurgeConfiguration repoPurge )
        throws ContinuumPurgeManagerException
    {
        try
        {
            LocalRepository repository = repoPurge.getRepository();

            // do not purge if repository is in use and if repository is already in purge queue
            if ( !taskQueueManager.isRepositoryInUse( repository.getId() ) && !taskQueueManager.isInPurgeQueue(
                repoPurge.getId() ) )
            {
                taskQueueManager.getPurgeQueue().put( new PurgeTask( repoPurge.getId() ) );
            }
        }
        catch ( TaskQueueException e )
        {
            throw new ContinuumPurgeManagerException( "Error while enqueuing repository", e );
        }
        catch ( TaskQueueManagerException e )
        {
            throw new ContinuumPurgeManagerException( e.getMessage(), e );
        }
    }

    public void purgeDirectory( DirectoryPurgeConfiguration dirPurge )
        throws ContinuumPurgeManagerException
    {
        try
        {
            if ( "releases".equals( dirPurge.getDirectoryType() ) )
            {
                // do not purge if release in progress
                if ( !taskQueueManager.releaseInProgress() && !taskQueueManager.isInPurgeQueue( dirPurge.getId() ) )
                {
                    taskQueueManager.getPurgeQueue().put( new PurgeTask( dirPurge.getId() ) );
                }
            }
            else if ( "buildOutput".equals( dirPurge.getDirectoryType() ) )
            {
                // do not purge if build in progress
                if ( !parallelBuildsManager.isBuildInProgress() && !taskQueueManager.isInPurgeQueue(
                    dirPurge.getId() ) )
                {
                    taskQueueManager.getPurgeQueue().put( new PurgeTask( dirPurge.getId() ) );
                }
            }

        }
        catch ( TaskQueueException e )
        {
            throw new ContinuumPurgeManagerException( "Error while enqueuing directory", e );
        }
        catch ( TaskQueueManagerException e )
        {
            throw new ContinuumPurgeManagerException( e.getMessage(), e );
        }
    }

    public void purgeDistributedDirectory( DistributedDirectoryPurgeConfiguration dirPurge )
        throws ContinuumPurgeManagerException
    {
        try
        {
            taskQueueManager.getPurgeQueue().put( new PurgeTask( dirPurge.getId() ) );
        }
        catch ( TaskQueueException e )
        {
            throw new ContinuumPurgeManagerException( "Error while enqueuing distributed directory", e );
        }
    }

}