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

import java.util.List;

import org.apache.continuum.buildagent.taskqueue.PrepareBuildProjectsTask;
import org.apache.continuum.taskqueue.BuildProjectTask;
import org.apache.continuum.taskqueue.manager.TaskQueueManagerException;
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
        removeProjectsFromBuildQueue();
        cancelCurrentBuild();
    }

    public TaskQueue getBuildQueue()
    {
        return buildAgentBuildQueue;
    }

    public int getCurrentProjectInBuilding()
        throws TaskQueueManagerException
    {
        Task task = getBuildTaskQueueExecutor().getCurrentTask();
        if ( task != null )
        {
            if ( task instanceof BuildProjectTask )
            {
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
                    log.info( "remove project '" + task.getProjectName() + "' from build queue" );
                    buildAgentBuildQueue.remove( task );
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

    private boolean cancelCurrentBuild()
        throws TaskQueueManagerException
    {
        Task task = getBuildTaskQueueExecutor().getCurrentTask();

        if ( task != null )
        {
            if ( task instanceof BuildProjectTask )
            {
                log.info( "Cancelling current build task" );
                return getBuildTaskQueueExecutor().cancelTask( task );
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
        return false;
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
                    if ( task.getProjectId() == projectId )
                    {
                        log.info( "project already in build queue" );
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

    public boolean isInPrepareBuildQueue( int projectGroupId, int trigger, String scmRootAddress )
        throws TaskQueueManagerException
    {
        try
        {
            List<PrepareBuildProjectsTask> queues = buildAgentPrepareBuildQueue.getQueueSnapshot();

            if ( queues != null )
            {
                for ( PrepareBuildProjectsTask task : queues )
                {
                    if ( task.getProjectGroupId() == projectGroupId && task.getTrigger() == trigger &&
                        task.getScmRootAddress().equals( scmRootAddress ) )
                    {
                        log.info( "projects already in build queue" );
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

    public void contextualize( Context context )
        throws ContextException
    {
        container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }
}
