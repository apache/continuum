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

import org.apache.commons.lang.ArrayUtils;
import org.apache.continuum.dao.BuildDefinitionDao;
import org.apache.maven.continuum.buildqueue.BuildProjectTask;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.scm.queue.CheckOutTask;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.taskqueue.Task;
import org.codehaus.plexus.taskqueue.TaskQueue;
import org.codehaus.plexus.taskqueue.TaskQueueException;
import org.codehaus.plexus.taskqueue.execution.ThreadedTaskQueueExecutor;
import org.codehaus.plexus.util.StringUtils;

/**
 * "Overall" build queue which has a checkout queue and a build queue. 
 * 
 * @author <a href="mailto:oching@apache.org">Maria Odea Ching</a>
 * @plexus.component role="org.apache.continuum.taskqueue.OverallBuildQueue" instantiation-strategy="per-lookup"
 */
public class DefaultOverallBuildQueue
    extends AbstractLogEnabled
    implements OverallBuildQueue
    //, Contextualizable 
{
    // TODO: deng parallel builds
    // - might need to set a task queue executor for each task queue! 
    //      change getXXXXTaskQueueExecutor() methods
    
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
    //private TaskQueue prepareBuildQueue;

    /**
     * @plexus.requirement
     */
    private BuildDefinitionDao buildDefinitionDao;

    private PlexusContainer container;
    
    private int id;
    
    private String name;
    
    public int getId()
    {
        return id;
    }

    public void setId( int id )
    {
        this.id = id;
    }
    
    public String getName()
    {
        return name;
    }
    
    public void setName( String name )
    {
        this.name = name;
    }

    /* Checkout Queue */

    /*public TaskQueueExecutor getCheckoutTaskQueueExecutor()
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
    }*/

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

    /*public TaskQueueExecutor getPrepareBuildTaskQueueExecutor()
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
    }*/

    /* Build Queue */

    /*public TaskQueueExecutor getBuildTaskQueueExecutor()
        throws ComponentLookupException
    {   
        return ( TaskQueueExecutor ) container.lookup( TaskQueueExecutor.class, "build-project" );        
    }*/

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

    public List<Task> getProjectsInBuildQueue()
        throws TaskQueueException
    {   
        return buildQueue.getQueueSnapshot();        
    }

    /*public boolean isBuildInProgress()
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
    }*/

    public boolean isInBuildQueue( int projectId )
        throws TaskQueueException
    {
        return isInBuildQueue( projectId, -1 );
    }

    public boolean isInBuildQueue( int projectId, int buildDefinitionId )
        throws TaskQueueException
    {
        List<Task> queue = getProjectsInBuildQueue();

        for ( Task task : queue )
        {
            BuildProjectTask buildTask = (BuildProjectTask) task;
            if ( task != null )
            {
                if ( buildDefinitionId < 0 )
                {
                    if ( buildTask.getProjectId() == projectId )
                    {
                        return true;
                    }
                }
                else
                {
                    if ( buildTask.getProjectId() == projectId && buildTask.getBuildDefinitionId() == buildDefinitionId )
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public void cancelBuildTask( int projectId )
        throws ComponentLookupException
    {  
        getLogger().info( "\n========= [OverallBuildQueue] CANCEL BUILD TASK ============" );
        List<Object> objects = container.lookupList( ThreadedTaskQueueExecutor.class );
        for( Object obj : objects )
        {
            getLogger().info( "\n object --> " + obj );
            getLogger().info( "\n object class --> " + obj.getClass() );                
            ThreadedTaskQueueExecutor executor = ( ThreadedTaskQueueExecutor ) obj;
            Task task = executor.getCurrentTask();
            if( task != null && task instanceof BuildProjectTask )
            {
                if( ( (BuildProjectTask) task ).getProjectId() == projectId )
                {
                    getLogger().info( "Cancelling task for project " + projectId );
                    executor.cancelTask( task );
                    getLogger().info( "current task is a BuildProjectTask." );
                }
            }
        }            
        
        
        /*try
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
        }*/
    }

    public boolean cancelCurrentBuild()
        throws ComponentLookupException
    {
        getLogger().info( "\n========= [OverallBuildQueue] CANCEL CURRENT BUILD ============" );
        List<Object> objects = container.lookupList( ThreadedTaskQueueExecutor.class );
        for( Object obj : objects )
        {
            getLogger().info( "\n object --> " + obj );
            getLogger().info( "\n object class --> " + obj.getClass() );                
            ThreadedTaskQueueExecutor executor = ( ThreadedTaskQueueExecutor ) obj;
            Task task = executor.getCurrentTask();
            if( task != null && task instanceof BuildProjectTask )
            {   
                BuildProjectTask buildTask = (BuildProjectTask) task;
                getLogger().info( "Cancelling build task for project '" + buildTask.getProjectId() );
                executor.cancelTask( task );
                getLogger().info( "current task is a BuildProjectTask." );
            }
        }
        
        /*try
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
        }*/
        
        return false;
    }

    public boolean removeProjectFromBuildQueue( int projectId, int buildDefinitionId, int trigger, String projectName )
        throws TaskQueueException
    {
        BuildDefinition buildDefinition;

        // TODO: deng - maybe we could just pass the label as a parameter to eliminate 
        //          dependency to BuildDefinitionDAO?
        
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
        List<Task> queue = getProjectsInBuildQueue();

        List<BuildProjectTask> tasks = new ArrayList<BuildProjectTask>();

        for ( Task task : queue )
        {
            BuildProjectTask buildTask = (BuildProjectTask) task;
            if ( task != null )
            {
                if ( ArrayUtils.contains( projectIds, buildTask.getProjectId() ) )
                {
                    tasks.add( buildTask );
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
        List<Task> queue = getProjectsInBuildQueue();

        for ( Task task : queue )
        {
            BuildProjectTask buildTask = (BuildProjectTask) task;
            if ( task != null && buildTask.getProjectId() == projectId )
            {
                return buildQueue.remove( task );
            }
        }

        return false;
    }

    public void removeProjectsFromBuildQueueWithHashCodes( int[] hashCodes )
        throws TaskQueueException
    {
        List<Task> queue = getProjectsInBuildQueue();
        for ( Task task : queue )        
        {            
            if ( ArrayUtils.contains( hashCodes, task.hashCode() ) )
            {
                buildQueue.remove( task );
            }
        }
    }

    /*public void contextualize( Context context )
        throws ContextException
    {
        container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }*/

    public TaskQueue getCheckoutQueue()
    {
        return checkoutQueue;
    }

    public TaskQueue getBuildQueue()
    {
        return buildQueue;
    }

    public void setContainer( PlexusContainer container )
    {
        this.container = container;
    }
    // TODO: change this!
    /*private Task getCurrentTask( String task )
        throws ComponentLookupException
    {
        
        TaskQueueExecutor executor = (TaskQueueExecutor) container.lookup( TaskQueueExecutor.class, task );
        return executor.getCurrentTask();       
    }    */
}
