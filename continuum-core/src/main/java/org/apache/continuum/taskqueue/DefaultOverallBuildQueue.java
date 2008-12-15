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
{   
    /**
     * @plexus.requirement role-hint="build-project"
     */
    private TaskQueue buildQueue;

    /**
     * @plexus.requirement role-hint="check-out-project"
     */
    private TaskQueue checkoutQueue;

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
}
