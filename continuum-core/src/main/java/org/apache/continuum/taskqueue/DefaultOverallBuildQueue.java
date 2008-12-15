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
import org.apache.continuum.taskqueueexecutor.ParallelBuildsThreadedTaskQueueExecutor;
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
import org.codehaus.plexus.taskqueue.execution.TaskQueueExecutor;
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
    //private TaskQueue buildQueue;

    /**
     * @plexus.requirement role-hint="check-out-project"
     */
    //private TaskQueue checkoutQueue;

    /**
     * @plexus.requirement
     */
    private BuildDefinitionDao buildDefinitionDao;

    private PlexusContainer container;
    
    /**
     * @plexus.requirement role-hint="build-project"
     */
    private TaskQueueExecutor buildTaskQueueExecutor;
    
    /**
     * @plexus.requirement role-hint="check-out-project"
     */
    private TaskQueueExecutor checkoutTaskQueueExecutor;

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

    /**
     * @see OverallBuildQueue#addToCheckoutQueue(Task)
     */
    public void addToCheckoutQueue( Task checkoutTask )
        throws TaskQueueException
    {
        getCheckoutQueue().put( checkoutTask );
    }

    /**
     * @see OverallBuildQueue#addToCheckoutQueue(List)
     */
    public void addToCheckoutQueue( List<Task> checkoutTasks )
        throws TaskQueueException
    {
        for ( Task checkoutTask : checkoutTasks )
        {
            getCheckoutQueue().put( checkoutTask );
        }
    }

    /**
     * @see OverallBuildQueue#getCheckOutTasksInQueue()
     */
    public List<CheckOutTask> getCheckOutTasksInQueue()
        throws TaskQueueException
    {
        return getCheckoutQueue().getQueueSnapshot();
    }

    /**
     * @see OverallBuildQueue#isInCheckoutQueue(int)
     */
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

    /**
     * @see OverallBuildQueue#removeProjectFromCheckoutQueue(int)
     */
    public boolean removeProjectFromCheckoutQueue( int projectId )
        throws TaskQueueException
    {
        List<CheckOutTask> queue = getCheckOutTasksInQueue();

        for ( CheckOutTask task : queue )
        {
            if ( task != null && task.getProjectId() == projectId )
            {
                return getCheckoutQueue().remove( task );
            }
        }

        return false;
    }

    /**
     * @see OverallBuildQueue#removeProjectsFromCheckoutQueue(int[])
     */
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
            return getCheckoutQueue().removeAll( tasks );
        }
        return false;
    }

    /**
     * @see OverallBuildQueue#removeTasksFromCheckoutQueueWithHashCodes(int[])
     */
    public void removeTasksFromCheckoutQueueWithHashCodes( int[] hashCodes )
        throws TaskQueueException
    {
        List<CheckOutTask> queue = getCheckOutTasksInQueue();

        for ( CheckOutTask task : queue )
        {
            if ( ArrayUtils.contains( hashCodes, task.hashCode() ) )
            {
                getCheckoutQueue().remove( task );
            }
        }
    }

    /**
     * @see OverallBuildQueue#addToBuildQueue(Task)
     */
    public void addToBuildQueue( Task buildTask )
        throws TaskQueueException
    {
        getBuildQueue().put( buildTask );
    }

    /**
     * @see OverallBuildQueue#addToBuildQueue(List)
     */
    public void addToBuildQueue( List<Task> buildTasks )
        throws TaskQueueException
    {
        for ( Task buildTask : buildTasks )
        {
            getBuildQueue().put( buildTask );
        }
    }

    /**
     * @see OverallBuildQueue#getProjectsInBuildQueue()
     */
    public List<Task> getProjectsInBuildQueue()
        throws TaskQueueException
    {
        return getBuildQueue().getQueueSnapshot();
    }

    /**
     * @see OverallBuildQueue#isInBuildQueue(int)
     */
    public boolean isInBuildQueue( int projectId )
        throws TaskQueueException
    {
        return isInBuildQueue( projectId, -1 );
    }

    /**
     * @see OverallBuildQueue#isInBuildQueue(int, int)
     */
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

    /**
     * @see OverallBuildQueue#cancelBuildTask(int)
     */
    public void cancelBuildTask( int projectId )
        throws ComponentLookupException
    {
        List<Object> objects = container.lookupList( ThreadedTaskQueueExecutor.class );
        for ( Object obj : objects )
        {
            ThreadedTaskQueueExecutor executor = (ThreadedTaskQueueExecutor) obj;
            Task task = executor.getCurrentTask();
            if ( task != null && task instanceof BuildProjectTask )
            {
                if ( ( (BuildProjectTask) task ).getProjectId() == projectId )
                {
                    getLogger().info(
                                      "Cancelling build task for project '" + projectId + "' in task executor '" +
                                          executor );
                    executor.cancelTask( task );
                }
            }
        }

    }

    /**
     * @see OverallBuildQueue#cancelCurrentBuild()
     */
    public boolean cancelCurrentBuild()
        throws ComponentLookupException
    {
        List<Object> objects = container.lookupList( ThreadedTaskQueueExecutor.class );
        for ( Object obj : objects )
        {
            ThreadedTaskQueueExecutor executor = (ThreadedTaskQueueExecutor) obj;
            Task task = executor.getCurrentTask();
            if ( task != null && task instanceof BuildProjectTask )
            {
                BuildProjectTask buildTask = (BuildProjectTask) task;
                getLogger().info(
                                  "Cancelling task for project '" + buildTask.getProjectId() + "' in task executor '" +
                                      executor );
                executor.cancelTask( task );
            }
        }
        return false;
    }

    /**
     * @see OverallBuildQueue#removeProjectFromBuildQueue(int, int, int, String)
     */
    public boolean removeProjectFromBuildQueue( int projectId, int buildDefinitionId, int trigger, String projectName )
        throws TaskQueueException
    {
        BuildDefinition buildDefinition;

        // maybe we could just pass the label as a parameter to eliminate dependency to BuildDefinitionDAO?
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

        return getBuildQueue().remove( buildProjectTask );
    }

    /**
     * @see OverallBuildQueue#removeProjectsFromBuildQueue(int[])
     */
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
            return getBuildQueue().removeAll( tasks );
        }

        return false;
    }

    /**
     * @see OverallBuildQueue#removeProjectFromBuildQueue(int)
     */
    public boolean removeProjectFromBuildQueue( int projectId )
        throws TaskQueueException
    {
        List<Task> queue = getProjectsInBuildQueue();

        for ( Task task : queue )
        {
            BuildProjectTask buildTask = (BuildProjectTask) task;
            if ( task != null && buildTask.getProjectId() == projectId )
            {
                return getBuildQueue().remove( task );
            }
        }

        return false;
    }

    /**
     * @see OverallBuildQueue#removeProjectsFromBuildQueueWithHashCodes(int[])
     */
    public void removeProjectsFromBuildQueueWithHashCodes( int[] hashCodes )
        throws TaskQueueException
    {
        List<Task> queue = getProjectsInBuildQueue();
        for ( Task task : queue )
        {
            if ( ArrayUtils.contains( hashCodes, task.hashCode() ) )
            {
                getBuildQueue().remove( task );
            }
        }
    }

    /** 
     * @see OverallBuildQueue#getCheckoutQueue()
     */
    public TaskQueue getCheckoutQueue()
    {
        return ( ( ParallelBuildsThreadedTaskQueueExecutor ) checkoutTaskQueueExecutor ).getQueue();
    }

    /**
     * @see OverallBuildQueue#getBuildQueue()
     */
    public TaskQueue getBuildQueue()
    {
        return ( ( ParallelBuildsThreadedTaskQueueExecutor ) buildTaskQueueExecutor ).getQueue();
    }

    /*public void setContainer( PlexusContainer container )
    {
        this.container = container;
    }*/

    public TaskQueueExecutor getBuildTaskQueueExecutor()
    {
        return buildTaskQueueExecutor;
    }

    public TaskQueueExecutor getCheckoutTaskQueueExecutor()
    {
        return checkoutTaskQueueExecutor;
    }
}
