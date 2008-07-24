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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.continuum.model.repository.DirectoryPurgeConfiguration;
import org.apache.continuum.model.repository.LocalRepository;
import org.apache.continuum.model.repository.RepositoryPurgeConfiguration;
import org.apache.continuum.purge.ContinuumPurgeManager;
import org.apache.continuum.purge.ContinuumPurgeManagerException;
import org.apache.continuum.purge.PurgeConfigurationService;
import org.apache.continuum.purge.task.PurgeTask;
import org.apache.maven.continuum.buildqueue.BuildProjectTask;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.Schedule;
import org.apache.maven.continuum.release.tasks.PerformReleaseProjectTask;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.taskqueue.Task;
import org.codehaus.plexus.taskqueue.TaskQueue;
import org.codehaus.plexus.taskqueue.TaskQueueException;
import org.codehaus.plexus.taskqueue.execution.TaskQueueExecutor;

/**
 * DefaultContinuumPurgeManager
 * 
 * @author Maria Catherine Tan
 * @plexus.component role="org.apache.continuum.purge.ContinuumPurgeManager" role-hint="default"
 */
public class DefaultContinuumPurgeManager
    implements ContinuumPurgeManager, Contextualizable
{
    /**
     * @plexus.requirement role-hint="jdo"
     */
    private ContinuumStore store;
    
    /**
     * @plexus.requirement role-hint="purge"
     */
    private TaskQueue purgeQueue;
    
    /**
     * @plexus.requirement
     */
    private PurgeConfigurationService purgeConfigurationService;
    
    private PlexusContainer container;
    
    public void purge( Schedule schedule )
        throws ContinuumPurgeManagerException
    {
        List<RepositoryPurgeConfiguration> repoPurgeList = null;
        List<DirectoryPurgeConfiguration> dirPurgeList = null;
        
        repoPurgeList = purgeConfigurationService.getRepositoryPurgeConfigurationsBySchedule( schedule.getId() );
        dirPurgeList = purgeConfigurationService.getDirectoryPurgeConfigurationsBySchedule( schedule.getId() );

        if ( repoPurgeList != null && repoPurgeList.size() > 0 )
        {
            for ( RepositoryPurgeConfiguration repoPurge : repoPurgeList )
            {
                purgeRepository( repoPurge );
            }
        }
        
        if ( dirPurgeList != null && dirPurgeList.size() > 0 )
        {
            for ( DirectoryPurgeConfiguration dirPurge : dirPurgeList )
            {
                purgeDirectory( dirPurge );
            }
        }
    }
    
    public boolean isRepositoryInPurgeQueue( int repositoryId )
        throws ContinuumPurgeManagerException
    {
        List<RepositoryPurgeConfiguration> repoPurgeConfigs = 
            purgeConfigurationService.getRepositoryPurgeConfigurationsByRepository( repositoryId );
            
        for ( RepositoryPurgeConfiguration repoPurge : repoPurgeConfigs )
        {
            if ( isInPurgeQueue( repoPurge.getId() ) )
            {
                return true;
            }
        }
        return false;
    }
    
    public boolean isRepositoryInUse( int repositoryId )
        throws ContinuumPurgeManagerException
    {
        try
        {
            Task task = getCurrentTask( "build-project" );
        
            if ( task != null && task instanceof BuildProjectTask )
            {
                int projectId = ((BuildProjectTask) task).getProjectId();
        
                Project project = store.getProject( projectId );
                LocalRepository repository = project.getProjectGroup().getLocalRepository();
                
                if ( repository != null && repository.getId() == repositoryId )
                {
                    return true;
                }
            }
            return false;
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumPurgeManagerException( e.getMessage(), e );
        }
    }

    public void removeRepositoryFromPurgeQueue( int repositoryId )
        throws ContinuumPurgeManagerException
    {
        List<RepositoryPurgeConfiguration> repoPurgeConfigs = 
            purgeConfigurationService.getRepositoryPurgeConfigurationsByRepository( repositoryId );
            
        for ( RepositoryPurgeConfiguration repoPurge : repoPurgeConfigs )
        {
            removeFromPurgeQueue( repoPurge.getId() );
        }
    }
    
    public boolean removeFromPurgeQueue( int[] purgeConfigIds )
        throws ContinuumPurgeManagerException
    {
        if ( purgeConfigIds == null )
        {
            return false;
        }
        
        if ( purgeConfigIds.length < 1 )
        {
            return false;
        }
        
        List<PurgeTask> queue = getAllPurgeConfigurationsInPurgeQueue();
        
        List<PurgeTask> tasks = new ArrayList<PurgeTask>();
        
        for ( PurgeTask task : queue )
        {
            if ( task != null )
            {
                if ( ArrayUtils.contains( purgeConfigIds, task.getPurgeConfigurationId() ) )
                {
                    tasks.add( task );
                }
            }
        }
        
        if ( !tasks.isEmpty() )
        {
            return purgeQueue.removeAll( tasks );
        }
    
        return false;
    }
    
    public boolean removeFromPurgeQueue( int purgeConfigId )
        throws ContinuumPurgeManagerException
    {
        List<PurgeTask> queue = getAllPurgeConfigurationsInPurgeQueue();
        
        for ( PurgeTask task : queue )
        {
            if ( task != null && task.getPurgeConfigurationId() == purgeConfigId )
            {
                return purgeQueue.remove( task );
            }
        }
        return false;
    }

    public void purgeRepository( RepositoryPurgeConfiguration repoPurge )
        throws ContinuumPurgeManagerException
    {
        try
        {
            LocalRepository repository = repoPurge.getRepository();
            
            // do not purge if repository is in use and if repository is already in purge queue
            if ( !isRepositoryInUse( repository.getId() ) && 
                 !isInPurgeQueue( repoPurge.getId() ) )
            {
                purgeQueue.put( new PurgeTask( repoPurge.getId() ) );
            }
        }
        catch ( TaskQueueException e )
        {
            throw new ContinuumPurgeManagerException( "Error while enqueuing repository", e );
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
                if ( !releaseInProgress() && !isInPurgeQueue( dirPurge.getId() ) )
                {
                    purgeQueue.put( new PurgeTask( dirPurge.getId() ) );
                }
            }
            else if ( "buildOutput".equals( dirPurge.getDirectoryType() ) )
            {
                // do not purge if build in progress
                if ( !buildInProgress() && !isInPurgeQueue( dirPurge.getId() ) )
                {
                    purgeQueue.put(  new PurgeTask( dirPurge.getId() ) );
                }
            }
            
        }
        catch ( TaskQueueException e )
        {
            throw new ContinuumPurgeManagerException( "Error while enqueuing repository", e );
        }
    }
    
    public void contextualize( Context context )
        throws ContextException
    {
        container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }

    private boolean isInPurgeQueue( int purgeConfigId )
        throws ContinuumPurgeManagerException
    {
        List<PurgeTask> queue = getAllPurgeConfigurationsInPurgeQueue();
        
        for ( PurgeTask task : queue )
        {
            if ( task != null && task.getPurgeConfigurationId() == purgeConfigId)
            {
                return true;
            }
        }
        return false;
    }
    
    private List<PurgeTask> getAllPurgeConfigurationsInPurgeQueue()
        throws ContinuumPurgeManagerException
    {
        try
        {
            return purgeQueue.getQueueSnapshot();
        }
        catch ( TaskQueueException e )
        {
            throw new ContinuumPurgeManagerException( "Error while getting the purge configs in purge queue", e );
        }
    }
    
    private Task getCurrentTask( String task )
        throws ContinuumPurgeManagerException
    {
        try
        {
            TaskQueueExecutor executor = (TaskQueueExecutor) container.lookup( TaskQueueExecutor.class, task );
            return executor.getCurrentTask();
        }
        catch ( ComponentLookupException e )
        {
            throw new ContinuumPurgeManagerException( "Unable to lookup current task", e );
        }
    }
    
    private boolean buildInProgress() 
        throws ContinuumPurgeManagerException
    {
        Task task = getCurrentTask( "build-project" );
        
        if ( task != null && task instanceof BuildProjectTask )
        {
            return true;
        }
        
        return false;
    }
    
    private boolean releaseInProgress()
        throws ContinuumPurgeManagerException
    {
        Task task = getCurrentTask( "perform-release" );
        
        if ( task != null && task instanceof PerformReleaseProjectTask )
        {
            return true;
        }
        
        return false;
    }
}