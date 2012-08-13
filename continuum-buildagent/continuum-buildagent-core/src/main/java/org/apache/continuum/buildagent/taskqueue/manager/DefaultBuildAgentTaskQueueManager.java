package org.apache.continuum.buildagent.taskqueue.manager;

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
import org.apache.continuum.buildagent.taskqueue.PrepareBuildProjectsTask;
import org.apache.continuum.taskqueue.BuildProjectTask;
import org.apache.continuum.taskqueue.manager.TaskQueueManagerException;
import org.apache.continuum.utils.build.BuildTrigger;
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

import java.util.List;

/**
 * @plexus.component role="org.apache.continuum.buildagent.taskqueue.manager.BuildAgentTaskQueueManager" role-hint="default"
 */
public class DefaultBuildAgentTaskQueueManager
    implements BuildAgentTaskQueueManager, Contextualizable
{
    private static final Logger log = LoggerFactory.getLogger( DefaultBuildAgentTaskQueueManager.class );

    /**
     * @plexus.requirement role-hint="build-agent"
     */
    private TaskQueue buildAgentBuildQueue;

    /**
     * @plexus.requirement role-hint="prepare-build-agent"
     */
    private TaskQueue buildAgentPrepareBuildQueue;

    private PlexusContainer container;

    public void cancelBuild()
        throws TaskQueueManagerException
    {
        Task task = getBuildTaskQueueExecutor().getCurrentTask();

        if ( task != null )
        {
            if ( task instanceof BuildProjectTask )
            {
                log.info( "Cancelling current build task of project " + ( (BuildProjectTask) task ).getProjectId() );
                getBuildTaskQueueExecutor().cancelTask( task );
            }
            else
            {
                log.warn( "Current task not a BuildProjectTask - not cancelling" );
            }
        }
        else
        {
            log.warn( "No task running - not cancelling" );
        }
    }

    public TaskQueue getBuildQueue()
    {
        return buildAgentBuildQueue;
    }

    public int getIdOfProjectCurrentlyBuilding()
        throws TaskQueueManagerException
    {
        Task task = getBuildTaskQueueExecutor().getCurrentTask();
        if ( task != null )
        {
            if ( task instanceof BuildProjectTask )
            {
                log.debug( "Current project building: {}", ( (BuildProjectTask) task ).getProjectName() );
                return ( (BuildProjectTask) task ).getProjectId();
            }
        }
        return -1;
    }

    public TaskQueue getPrepareBuildQueue()
    {
        return buildAgentPrepareBuildQueue;
    }

    private void removeProjectsFromBuildQueue()
        throws TaskQueueManagerException
    {
        try
        {
            List<BuildProjectTask> queues = buildAgentBuildQueue.getQueueSnapshot();

            if ( queues != null )
            {
                for ( BuildProjectTask task : queues )
                {
                    if ( task != null )
                    {
                        log.info( "remove project '{}' from build queue", task.getProjectName() );
                        buildAgentBuildQueue.remove( task );
                    }
                }
            }
            else
            {
                log.info( "no build task in queue" );
            }
        }
        catch ( TaskQueueException e )
        {
            throw new TaskQueueManagerException( "Error while getting build tasks from queue", e );
        }
    }

    public TaskQueueExecutor getBuildTaskQueueExecutor()
        throws TaskQueueManagerException
    {
        try
        {
            return (TaskQueueExecutor) container.lookup( TaskQueueExecutor.class, "build-agent" );
        }
        catch ( ComponentLookupException e )
        {
            throw new TaskQueueManagerException( e.getMessage(), e );
        }
    }

    public TaskQueueExecutor getPrepareBuildTaskQueueExecutor()
        throws TaskQueueManagerException
    {
        try
        {
            return (TaskQueueExecutor) container.lookup( TaskQueueExecutor.class, "prepare-build-agent" );
        }
        catch ( ComponentLookupException e )
        {
            throw new TaskQueueManagerException( e.getMessage(), e );
        }
    }

    public boolean hasBuildTaskInQueue()
        throws TaskQueueManagerException
    {
        try
        {
            if ( getBuildQueue().getQueueSnapshot() != null && getBuildQueue().getQueueSnapshot().size() > 0 )
            {
                return true;
            }
        }
        catch ( TaskQueueException e )
        {
            throw new TaskQueueManagerException( e.getMessage(), e );
        }
        return false;
    }

    public boolean isProjectInBuildQueue( int projectId )
        throws TaskQueueManagerException
    {
        try
        {
            List<BuildProjectTask> queues = buildAgentBuildQueue.getQueueSnapshot();

            if ( queues != null )
            {
                for ( BuildProjectTask task : queues )
                {
                    if ( task != null && task.getProjectId() == projectId )
                    {
                        log.debug( "project {} is in build queue", task.getProjectName() );
                        return true;
                    }
                }
            }
            else
            {
                log.info( "no build task in queue" );
            }
        }
        catch ( TaskQueueException e )
        {
            throw new TaskQueueManagerException( e.getMessage(), e );
        }

        return false;
    }

    public boolean isInPrepareBuildQueue( int projectGroupId, BuildTrigger buildTrigger, String scmRootAddress )
        throws TaskQueueManagerException
    {
        try
        {
            List<PrepareBuildProjectsTask> queues = buildAgentPrepareBuildQueue.getQueueSnapshot();

            if ( queues != null )
            {
                for ( PrepareBuildProjectsTask task : queues )
                {
                    if ( task != null && task.getProjectGroupId() == projectGroupId &&
                        task.getBuildTrigger().getTrigger() == buildTrigger.getTrigger() &&
                        task.getScmRootAddress().equals( scmRootAddress ) )
                    {
                        log.info( "project group {} in prepare build queue", task.getProjectGroupId() );
                        return true;
                    }
                }
            }
            else
            {
                log.info( "no prepare build task in queue" );
            }
        }
        catch ( TaskQueueException e )
        {
            throw new TaskQueueManagerException( e.getMessage(), e );
        }

        return false;
    }

    public List<PrepareBuildProjectsTask> getProjectsInPrepareBuildQueue()
        throws TaskQueueManagerException
    {
        try
        {
            return buildAgentPrepareBuildQueue.getQueueSnapshot();
        }
        catch ( TaskQueueException e )
        {
            log.error( "Error occurred while retrieving projects in prepare build queue", e );
            throw new TaskQueueManagerException( "Error occurred while retrieving projects in prepare build queue", e );
        }
    }

    public List<BuildProjectTask> getProjectsInBuildQueue()
        throws TaskQueueManagerException
    {
        try
        {
            return buildAgentBuildQueue.getQueueSnapshot();
        }
        catch ( TaskQueueException e )
        {
            log.error( "Error occurred while retrieving projects in build queue", e );
            throw new TaskQueueManagerException( "Error occurred while retrieving projects in build queue", e );
        }
    }

    public PrepareBuildProjectsTask getCurrentProjectInPrepareBuild()
        throws TaskQueueManagerException
    {
        Task task = getPrepareBuildTaskQueueExecutor().getCurrentTask();

        if ( task != null )
        {
            log.debug( "Current project group preparing build: {}",
                       ( (PrepareBuildProjectsTask) task ).getProjectGroupId() );
            return (PrepareBuildProjectsTask) task;
        }
        return null;
    }

    public BuildProjectTask getCurrentProjectInBuilding()
        throws TaskQueueManagerException
    {
        Task task = getBuildTaskQueueExecutor().getCurrentTask();

        if ( task != null )
        {
            log.debug( "Current project building: {}", ( (BuildProjectTask) task ).getProjectName() );
            return (BuildProjectTask) task;
        }

        return null;
    }

    public boolean removeFromPrepareBuildQueue( int projectGroupId, int scmRootId )
        throws TaskQueueManagerException
    {
        List<PrepareBuildProjectsTask> tasks = getProjectsInPrepareBuildQueue();

        if ( tasks != null )
        {
            for ( PrepareBuildProjectsTask task : tasks )
            {
                if ( task != null && task.getProjectGroupId() == projectGroupId && task.getScmRootId() == scmRootId )
                {
                    log.debug( "Remove project group {} from prepare build queue", projectGroupId );
                    return getPrepareBuildQueue().remove( task );
                }
            }
        }

        return false;
    }

    public void removeFromPrepareBuildQueue( int[] hashCodes )
        throws TaskQueueManagerException
    {
        List<PrepareBuildProjectsTask> tasks = getProjectsInPrepareBuildQueue();

        if ( tasks != null )
        {
            for ( PrepareBuildProjectsTask task : tasks )
            {
                if ( task != null && ArrayUtils.contains( hashCodes, task.getHashCode() ) )
                {
                    log.debug( "Remove project group '{}' from prepare build queue", task.getProjectGroupId() );
                    getPrepareBuildQueue().remove( task );
                }
            }
        }
    }

    public boolean removeFromBuildQueue( int projectId, int buildDefinitionId )
        throws TaskQueueManagerException
    {
        List<BuildProjectTask> tasks = getProjectsInBuildQueue();

        if ( tasks != null )
        {
            for ( BuildProjectTask task : tasks )
            {
                if ( task != null && task.getProjectId() == projectId &&
                    task.getBuildDefinitionId() == buildDefinitionId )
                {
                    log.debug( "Remove project {} with buildDefinition{} from build queue", task.getProjectName(),
                               task.getBuildDefinitionId() );
                    return getBuildQueue().remove( task );
                }
            }
        }

        return false;
    }

    public void removeFromBuildQueue( int[] hashCodes )
        throws TaskQueueManagerException
    {
        List<BuildProjectTask> tasks = getProjectsInBuildQueue();

        if ( tasks != null )
        {
            for ( BuildProjectTask task : tasks )
            {
                if ( task != null && ArrayUtils.contains( hashCodes, task.getHashCode() ) )
                {
                    log.debug( "Remove project '{}' from build queue", task.getProjectName() );
                    getBuildQueue().remove( task );
                }
            }
        }
    }

    public void contextualize( Context context )
        throws ContextException
    {
        container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }

}
