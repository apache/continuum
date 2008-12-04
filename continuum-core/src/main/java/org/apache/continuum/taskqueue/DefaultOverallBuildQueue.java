package org.apache.continuum.taskqueue;

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
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.continuum.dao.BuildDefinitionDao;
import org.apache.continuum.taskqueue.manager.TaskQueueManagerException;
import org.apache.maven.continuum.buildqueue.BuildProjectTask;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.scm.queue.CheckOutTask;
import org.apache.maven.continuum.scm.queue.PrepareBuildProjectsTask;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.taskqueue.Task;
import org.codehaus.plexus.taskqueue.TaskQueue;
import org.codehaus.plexus.taskqueue.TaskQueueException;
import org.codehaus.plexus.taskqueue.execution.TaskQueueExecutor;
import org.codehaus.plexus.util.StringUtils;

/**
 * "Overall" build queue which has a checkout queue, a prepare-build queue, and a build queue. All builds whether forced
 * or triggered will go through (or have to be added through) the "overall" build queue.
 * 
 * @author <a href="mailto:oching@apache.org">Maria Odea Ching</a>
 * @plexus.component role="org.apache.continuum.taskqueue.OverallBuildQueue" instantiation-strategy="per-lookup"
 */
public class DefaultOverallBuildQueue
    extends AbstractLogEnabled
    implements OverallBuildQueue, Contextualizable
{
    // TODO:
    // - need to specify each task queue to be instantiated each time it is looked up!!!

    /**
     * @plexus.requirement role-hint="build-project"
     */
    private TaskQueue buildQueue;

    /**
     * @plexus.requirement role-hint="check-out-project"
     */
    private TaskQueue checkoutQueue;

    /**
     * @plexus.requirement role-hint="prepare-build-project"
     */
    private TaskQueue prepareBuildQueue;

    /**
     * @plexus.requirement
     */
    private BuildDefinitionDao buildDefinitionDao;

    private PlexusContainer container;
    
    private int id;
    
    public int getId()
    {
        return id;
    }

    public void setId( int id )
    {
        this.id = id;
    }

    /* Checkout Queue */

    public TaskQueueExecutor getCheckoutTaskQueueExecutor()
        throws TaskQueueManagerException
    {
        try
        {
            return (TaskQueueExecutor) container.lookup( TaskQueueExecutor.class, "check-out-project" );
        }
        catch ( ComponentLookupException e )
        {
            throw new TaskQueueManagerException( e.getMessage(), e );
        }
    }

    public void addToCheckoutQueue( Task checkoutTask )
        throws TaskQueueException
    {
        checkoutQueue.put( checkoutTask );
    }

    public void addToCheckoutQueue( List<Task> checkoutTasks )
        throws TaskQueueException
    {
        for ( Task checkoutTask : checkoutTasks )
        {
            checkoutQueue.put( checkoutTask );
        }
    }

    public List<CheckOutTask> getCheckOutTasksInQueue()
        throws TaskQueueException
    {
        return checkoutQueue.getQueueSnapshot();       
    }

    public boolean isInCheckoutQueue( int projectId )
        throws TaskQueueException
    {
        List<CheckOutTask> queue = getCheckOutTasksInQueue();

        for ( CheckOutTask task : queue )
        {
            if ( task != null && task.getProjectId() == projectId )
            {
                return true;
            }
        }

        return false;
    }

    public boolean removeProjectFromCheckoutQueue( int projectId )
        throws TaskQueueException
    {
        List<CheckOutTask> queue = getCheckOutTasksInQueue();

        for ( CheckOutTask task : queue )
        {
            if ( task != null && task.getProjectId() == projectId )
            {
                return checkoutQueue.remove( task );
            }
        }

        return false;
    }

    public boolean removeProjectsFromCheckoutQueue( int[] projectsId )
        throws TaskQueueException
    {
        if ( projectsId == null )
        {
            return false;
        }
        if ( projectsId.length < 1 )
        {
            return false;
        }
        List<CheckOutTask> queue = getCheckOutTasksInQueue();

        List<CheckOutTask> tasks = new ArrayList<CheckOutTask>();

        for ( CheckOutTask task : queue )
        {
            if ( task != null )
            {
                if ( ArrayUtils.contains( projectsId, task.getProjectId() ) )
                {
                    tasks.add( task );
                }
            }
        }
        if ( !tasks.isEmpty() )
        {
            return checkoutQueue.removeAll( tasks );
        }
        return false;
    }

    public void removeTasksFromCheckoutQueueWithHashCodes( int[] hashCodes )
        throws TaskQueueException
    {
        List<CheckOutTask> queue = getCheckOutTasksInQueue();

        for ( CheckOutTask task : queue )
        {
            if ( ArrayUtils.contains( hashCodes, task.hashCode() ) )
            {
                checkoutQueue.remove( task );
            }
        }
    }

    /* Prepare-build-projects Queue */

    public TaskQueueExecutor getPrepareBuildTaskQueueExecutor()
        throws ComponentLookupException
    {        
        return (TaskQueueExecutor) container.lookup( TaskQueueExecutor.class, "prepare-build-project" );        
    }

    public void addToPrepareBuildQueue( Task prepareBuildTask )
        throws TaskQueueException
    {
        prepareBuildQueue.put( prepareBuildTask );
    }

    public void addToPrepareBuildQueue( List<Task> prepareBuildTasks )
        throws TaskQueueException
    {
        for ( Task prepareBuildTask : prepareBuildTasks )
        {
            prepareBuildQueue.put( prepareBuildTask );
        }
    }

    public boolean isInPrepareBuildQueue( int projectId )
        throws TaskQueueException
    {        
        List<PrepareBuildProjectsTask> queue = prepareBuildQueue.getQueueSnapshot();

        for ( PrepareBuildProjectsTask task : queue )
        {
            if ( task != null )
            {
                Map<Integer, Integer> map = ( (PrepareBuildProjectsTask) task ).getProjectsBuildDefinitionsMap();

                if ( map.size() > 0 )
                {
                    Set<Integer> projectIds = map.keySet();

                    if ( projectIds.contains( new Integer( projectId ) ) )
                    {
                        return true;
                    }
                }
            }
        }

        return false;        
    }

    public boolean isCurrentPrepareBuildTaskInExecution( int projectId )
        throws TaskQueueException
    {
        try
        {
            Task task = getPrepareBuildTaskQueueExecutor().getCurrentTask();
    
            if ( task != null && task instanceof PrepareBuildProjectsTask )
            {
                Map<Integer, Integer> map = ( (PrepareBuildProjectsTask) task ).getProjectsBuildDefinitionsMap();
    
                if ( map.size() > 0 )
                {
                    Set<Integer> projectIds = map.keySet();
    
                    if ( projectIds.contains( new Integer( projectId ) ) )
                    {
                        return true;
                    }
                }
            }
        }
        catch ( ComponentLookupException e )
        {
            throw new TaskQueueException( "Error looking up prepare-build-project task queue executor." );
        }
        
        return false;
    }

    /* Build Queue */

    public TaskQueueExecutor getBuildTaskQueueExecutor()
        throws ComponentLookupException
    {        
        return (TaskQueueExecutor) container.lookup( TaskQueueExecutor.class, "build-project" );        
    }

    public void addToBuildQueue( Task buildTask )
        throws TaskQueueException
    {
        buildQueue.put( buildTask );
    }

    public void addToBuildQueue( List<Task> buildTasks )
        throws TaskQueueException
    {
        for ( Task buildTask : buildTasks )
        {
            buildQueue.put( buildTask );
        }
    }

    public int getProjectIdInCurrentBuild()
        throws TaskQueueException
    {
        try
        {
            Task task = getBuildTaskQueueExecutor().getCurrentTask();
            if ( task != null )
            {
                if ( task instanceof BuildProjectTask )
                {
                    return ( (BuildProjectTask) task ).getProjectId();
                }
            }
        }
        catch ( ComponentLookupException e )
        {
            throw new TaskQueueException( "Error occurred while looking up the build task queue executor. " );
        }
        
        return -1;
    }

    public List<BuildProjectTask> getProjectsInBuildQueue()
        throws TaskQueueException
    {   
        return buildQueue.getQueueSnapshot();        
    }

    public boolean isBuildInProgress()
        throws TaskQueueException
    {
        try
        {
            Task task = getCurrentTask( "build-project" );
    
            if ( task != null && task instanceof BuildProjectTask )
            {
                return true;
            }
        }
        catch( ComponentLookupException e )
        {
            // should we wrap this in a different exception instead of a TaskQueueException
            throw new TaskQueueException( e.getMessage() );
        }
        
        return false;
    }

    public boolean isInBuildQueue( int projectId )
        throws TaskQueueException
    {
        return isInBuildQueue( projectId, -1 );
    }

    public boolean isInBuildQueue( int projectId, int buildDefinitionId )
        throws TaskQueueException
    {
        List<BuildProjectTask> queue = getProjectsInBuildQueue();

        for ( BuildProjectTask task : queue )
        {
            if ( task != null )
            {
                if ( buildDefinitionId < 0 )
                {
                    if ( task.getProjectId() == projectId )
                    {
                        return true;
                    }
                }
                else
                {
                    if ( task.getProjectId() == projectId && task.getBuildDefinitionId() == buildDefinitionId )
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public void cancelBuildTask( int projectId )
        throws TaskQueueException
    {
        try
        {
            Task currentTask = getBuildTaskQueueExecutor().getCurrentTask();
    
            if ( currentTask instanceof BuildProjectTask )
            {
                if ( ( (BuildProjectTask) currentTask ).getProjectId() == projectId )
                {
                    getLogger().info( "Cancelling task for project " + projectId );
                    getBuildTaskQueueExecutor().cancelTask( currentTask );
                }
            }
        }
        catch ( ComponentLookupException e )
        {
            throw new TaskQueueException( e.getMessage() );
        }
    }

    public boolean cancelCurrentBuild()
        throws TaskQueueException
    {
        try
        {
            Task task = getBuildTaskQueueExecutor().getCurrentTask();
    
            if ( task != null )
            {
                if ( task instanceof BuildProjectTask )
                {
                    getLogger().info( "Cancelling current build task" );
                    return getBuildTaskQueueExecutor().cancelTask( task );
                }
                else
                {
                    getLogger().warn( "Current task not a BuildProjectTask - not cancelling" );
                }
            }
            else
            {
                getLogger().warn( "No task running - not cancelling" );
            }
        }
        catch ( ComponentLookupException e )
        {
            throw new TaskQueueException( e.getMessage() );
        }
        
        return false;
    }

    public boolean removeProjectFromBuildQueue( int projectId, int buildDefinitionId, int trigger, String projectName )
        throws TaskQueueException
    {
        BuildDefinition buildDefinition;

        try
        {
            buildDefinition = buildDefinitionDao.getBuildDefinition( buildDefinitionId );
        }
        catch ( ContinuumStoreException e )
        {
            throw new TaskQueueException( "Error while removing project from build queue: " + projectName, e );
        }

        String buildDefinitionLabel = buildDefinition.getDescription();
        if ( StringUtils.isEmpty( buildDefinitionLabel ) )
        {
            buildDefinitionLabel = buildDefinition.getGoals();
        }
        
        BuildProjectTask buildProjectTask =
            new BuildProjectTask( projectId, buildDefinitionId, trigger, projectName, buildDefinitionLabel );
        
        return this.buildQueue.remove( buildProjectTask );
    }

    public boolean removeProjectsFromBuildQueue( int[] projectIds )
        throws TaskQueueException
    {
        if ( projectIds == null )
        {
            return false;
        }
        if ( projectIds.length < 1 )
        {
            return false;
        }
        List<BuildProjectTask> queue = getProjectsInBuildQueue();

        List<BuildProjectTask> tasks = new ArrayList<BuildProjectTask>();

        for ( BuildProjectTask task : queue )
        {
            if ( task != null )
            {
                if ( ArrayUtils.contains( projectIds, task.getProjectId() ) )
                {
                    tasks.add( task );
                }
            }
        }

        for ( BuildProjectTask buildProjectTask : tasks )
        {
            getLogger().info( "cancel build for project " + buildProjectTask.getProjectId() );
        }
        if ( !tasks.isEmpty() )
        {
            return buildQueue.removeAll( tasks );
        }

        return false;
    }

    public boolean removeProjectFromBuildQueue( int projectId )
        throws TaskQueueException
    {
        List<BuildProjectTask> queue = getProjectsInBuildQueue();

        for ( BuildProjectTask task : queue )
        {
            if ( task != null && task.getProjectId() == projectId )
            {
                return buildQueue.remove( task );
            }
        }

        return false;
    }

    public void removeProjectsFromBuildQueueWithHashCodes( int[] hashCodes )
        throws TaskQueueException
    {
        List<BuildProjectTask> queue = getProjectsInBuildQueue();

        for ( BuildProjectTask task : queue )
        {
            if ( ArrayUtils.contains( hashCodes, task.hashCode() ) )
            {
                buildQueue.remove( task );
            }
        }
    }

    public void contextualize( Context context )
        throws ContextException
    {
        container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }

    public TaskQueue getCheckoutQueue()
    {
        return checkoutQueue;
    }

    public TaskQueue getPrepareBuildQueue()
    {
        return prepareBuildQueue;
    }

    public TaskQueue getBuildQueue()
    {
        return buildQueue;
    }

    private Task getCurrentTask( String task )
        throws ComponentLookupException
    {
        TaskQueueExecutor executor = (TaskQueueExecutor) container.lookup( TaskQueueExecutor.class, task );
        return executor.getCurrentTask();       
    }    
}
