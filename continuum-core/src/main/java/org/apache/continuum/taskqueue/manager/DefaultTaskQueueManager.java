package org.apache.continuum.taskqueue.manager;

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

import org.apache.commons.lang.ArrayUtils;
import org.apache.continuum.buildmanager.BuildManagerException;
import org.apache.continuum.buildmanager.BuildsManager;
import org.apache.continuum.dao.ProjectDao;
import org.apache.continuum.model.repository.LocalRepository;
import org.apache.continuum.model.repository.RepositoryPurgeConfiguration;
import org.apache.continuum.purge.PurgeConfigurationService;
import org.apache.continuum.purge.task.PurgeTask;
import org.apache.continuum.taskqueue.BuildProjectTask;
import org.apache.continuum.taskqueue.PrepareBuildProjectsTask;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.release.tasks.PerformReleaseProjectTask;
import org.apache.maven.continuum.release.tasks.PrepareReleaseProjectTask;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:ctan@apache.org">Maria Catherine Tan</a>
 * @plexus.component role="org.apache.continuum.taskqueue.manager.TaskQueueManager" role-hint="default"
 */
public class DefaultTaskQueueManager
    implements TaskQueueManager, Contextualizable
{
    private static final Logger log = LoggerFactory.getLogger( DefaultTaskQueueManager.class );

    /**
     * @plexus.requirement role-hint="distributed-build-project"
     */
    private TaskQueue distributedBuildQueue;

    /**
     * @plexus.requirement role-hint="purge"
     */
    private TaskQueue purgeQueue;

    /**
     * @plexus.requirement role-hint="prepare-release"
     */
    private TaskQueue prepareReleaseQueue;

    /**
     * @plexus.requirement role-hint="perform-release"
     */
    private TaskQueue performReleaseQueue;

    /**
     * @plexus.requirement
     */
    private ProjectDao projectDao;

    /**
     * @plexus.requirement
     */
    private PurgeConfigurationService purgeConfigurationService;

    /**
     * @plexus.requirement role-hint="parallel"
     */
    private BuildsManager buildsManager;

    private PlexusContainer container;

    public TaskQueue getDistributedBuildQueue()
    {
        return distributedBuildQueue;
    }

    public List<PrepareBuildProjectsTask> getDistributedBuildProjectsInQueue()
        throws TaskQueueManagerException
    {
        try
        {
            return distributedBuildQueue.getQueueSnapshot();
        }
        catch ( TaskQueueException e )
        {
            throw new TaskQueueManagerException( "Error while getting the distributed building queue", e );
        }
    }

    public TaskQueue getPurgeQueue()
    {
        return purgeQueue;
    }

    public boolean isInDistributedBuildQueue( int projectGroupId, String scmRootAddress )
        throws TaskQueueManagerException
    {
        try
        {
            List<PrepareBuildProjectsTask> queue = distributedBuildQueue.getQueueSnapshot();

            for ( PrepareBuildProjectsTask task : queue )
            {
                if ( task != null )
                {
                    if ( task.getProjectGroupId() == projectGroupId && task.getScmRootAddress().equals(
                        scmRootAddress ) )
                    {
                        return true;
                    }
                }
            }

            return false;
        }
        catch ( TaskQueueException e )
        {
            throw new TaskQueueManagerException( "Error while getting the tasks in distributed build queue", e );
        }
    }

    public boolean isInPurgeQueue( int purgeConfigId )
        throws TaskQueueManagerException
    {
        List<PurgeTask> queue = getAllPurgeConfigurationsInPurgeQueue();

        for ( PurgeTask task : queue )
        {
            if ( task != null && task.getPurgeConfigurationId() == purgeConfigId )
            {
                return true;
            }
        }
        return false;
    }

    public boolean isRepositoryInPurgeQueue( int repositoryId )
        throws TaskQueueManagerException
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
        throws TaskQueueManagerException
    {
        try
        {
            Map<String, BuildProjectTask> currentBuilds = buildsManager.getCurrentBuilds();
            Set<String> keys = currentBuilds.keySet();

            for ( String key : keys )
            {
                BuildProjectTask task = currentBuilds.get( key );
                if ( task != null )
                {
                    int projectId = task.getProjectId();

                    Project project = projectDao.getProject( projectId );
                    LocalRepository repository = project.getProjectGroup().getLocalRepository();

                    if ( repository != null && repository.getId() == repositoryId )
                    {
                        return true;
                    }
                }
            }

            return false;
        }
        catch ( BuildManagerException e )
        {
            log.error( "Error occured while getting current builds: " + e.getMessage() );
            throw new TaskQueueManagerException( e.getMessage(), e );
        }
        catch ( ContinuumStoreException e )
        {
            log.error( "Error occured while getting project details: " + e.getMessage() );
            throw new TaskQueueManagerException( e.getMessage(), e );
        }
    }

    public boolean isProjectInReleaseStage( String releaseId )
        throws TaskQueueManagerException
    {
        Task prepareTask = getCurrentTask( "prepare-release" );
        if ( prepareTask != null && prepareTask instanceof PrepareReleaseProjectTask )
        {
            if ( ( (PrepareReleaseProjectTask) prepareTask ).getReleaseId().equals( releaseId ) )
            {
                return true;
            }
            else
            {
                try
                {
                    // check if in queue
                    List<Task> tasks = prepareReleaseQueue.getQueueSnapshot();
                    for ( Task prepareReleaseTask : tasks )
                    {
                        if ( ( (PrepareReleaseProjectTask) prepareReleaseTask ).getReleaseId().equals( releaseId ) )
                        {
                            return true;
                        }
                    }
                }
                catch ( TaskQueueException e )
                {
                    throw new TaskQueueManagerException( e );
                }
            }
        }

        Task performTask = getCurrentTask( "perform-release" );
        if ( performTask != null && performTask instanceof PerformReleaseProjectTask )
        {
            if ( ( (PerformReleaseProjectTask) performTask ).getReleaseId().equals( releaseId ) )
            {
                return true;
            }
            else
            {
                try
                {
                    // check if in queue
                    List<Task> tasks = performReleaseQueue.getQueueSnapshot();
                    for ( Task performReleaseTask : tasks )
                    {
                        if ( ( (PerformReleaseProjectTask) performReleaseTask ).getReleaseId().equals( releaseId ) )
                        {
                            return true;
                        }
                    }
                }
                catch ( TaskQueueException e )
                {
                    throw new TaskQueueManagerException( e );
                }
            }
        }

        return false;
    }

    public boolean releaseInProgress()
        throws TaskQueueManagerException
    {
        Task task = getCurrentTask( "perform-release" );

        return task != null && task instanceof PerformReleaseProjectTask;
    }

    public void removeFromDistributedBuildQueue( int projectGroupId, String scmRootAddress )
        throws TaskQueueManagerException
    {
        List<PrepareBuildProjectsTask> queue = getDistributedBuildProjectsInQueue();

        for ( PrepareBuildProjectsTask task : queue )
        {
            if ( task.getProjectGroupId() == projectGroupId && task.getScmRootAddress().equals( scmRootAddress ) )
            {
                distributedBuildQueue.remove( task );
            }
        }
    }

    public boolean removeFromPurgeQueue( int purgeConfigId )
        throws TaskQueueManagerException
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

    public boolean removeFromPurgeQueue( int[] purgeConfigIds )
        throws TaskQueueManagerException
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

        return !tasks.isEmpty() && purgeQueue.removeAll( tasks );
    }

    public void removeRepositoryFromPurgeQueue( int repositoryId )
        throws TaskQueueManagerException
    {
        List<RepositoryPurgeConfiguration> repoPurgeConfigs =
            purgeConfigurationService.getRepositoryPurgeConfigurationsByRepository( repositoryId );

        for ( RepositoryPurgeConfiguration repoPurge : repoPurgeConfigs )
        {
            removeFromPurgeQueue( repoPurge.getId() );
        }
    }

    public void removeTasksFromDistributedBuildQueueWithHashCodes( int[] hashCodes )
        throws TaskQueueManagerException
    {
        List<PrepareBuildProjectsTask> queue = getDistributedBuildProjectsInQueue();

        for ( PrepareBuildProjectsTask task : queue )
        {
            if ( ArrayUtils.contains( hashCodes, task.hashCode() ) )
            {
                distributedBuildQueue.remove( task );
            }
        }
    }

    public void contextualize( Context context )
        throws ContextException
    {
        container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }

    private List<PurgeTask> getAllPurgeConfigurationsInPurgeQueue()
        throws TaskQueueManagerException
    {
        try
        {
            return purgeQueue.getQueueSnapshot();
        }
        catch ( TaskQueueException e )
        {
            throw new TaskQueueManagerException( "Error while getting the purge configs in purge queue", e );
        }
    }

    private Task getCurrentTask( String task )
        throws TaskQueueManagerException
    {
        try
        {
            TaskQueueExecutor executor = (TaskQueueExecutor) container.lookup( TaskQueueExecutor.class, task );
            return executor.getCurrentTask();
        }
        catch ( ComponentLookupException e )
        {
            throw new TaskQueueManagerException( "Unable to lookup current task", e );
        }
    }
}
