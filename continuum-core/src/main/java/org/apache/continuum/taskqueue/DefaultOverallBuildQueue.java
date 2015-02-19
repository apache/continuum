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

import org.apache.commons.lang.ArrayUtils;
import org.apache.continuum.dao.BuildDefinitionDao;
import org.apache.continuum.taskqueueexecutor.ParallelBuildsThreadedTaskQueueExecutor;
import org.apache.continuum.utils.build.BuildTrigger;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.taskqueue.Task;
import org.codehaus.plexus.taskqueue.TaskQueue;
import org.codehaus.plexus.taskqueue.TaskQueueException;
import org.codehaus.plexus.taskqueue.execution.TaskQueueExecutor;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Resource;

/**
 * "Overall" build queue which has a checkout queue and a build queue.
 *
 * @author <a href="mailto:oching@apache.org">Maria Odea Ching</a>
 */
public class DefaultOverallBuildQueue
    implements OverallBuildQueue
{
    private static final Logger log = LoggerFactory.getLogger( DefaultOverallBuildQueue.class );

    @Resource
    private BuildDefinitionDao buildDefinitionDao;

    private TaskQueueExecutor buildTaskQueueExecutor;

    private TaskQueueExecutor checkoutTaskQueueExecutor;

    private TaskQueueExecutor prepareBuildTaskQueueExecutor;

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
     * @see OverallBuildQueue#addToCheckoutQueue(CheckOutTask)
     */
    public void addToCheckoutQueue( CheckOutTask checkoutTask )
        throws TaskQueueException
    {
        getCheckoutQueue().put( checkoutTask );
    }

    /**
     * @see OverallBuildQueue#addToCheckoutQueue(List)
     */
    public void addToCheckoutQueue( List<CheckOutTask> checkoutTasks )
        throws TaskQueueException
    {
        for ( CheckOutTask checkoutTask : checkoutTasks )
        {
            getCheckoutQueue().put( checkoutTask );
        }
    }

    /**
     * @see OverallBuildQueue#getProjectsInCheckoutQueue()
     */
    public List<CheckOutTask> getProjectsInCheckoutQueue()
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
        List<CheckOutTask> queue = getProjectsInCheckoutQueue();

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
        List<CheckOutTask> queue = getProjectsInCheckoutQueue();

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
        List<CheckOutTask> queue = getProjectsInCheckoutQueue();

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
        return !tasks.isEmpty() && getCheckoutQueue().removeAll( tasks );
    }

    /**
     * @see OverallBuildQueue#removeTasksFromCheckoutQueueWithHashCodes(int[])
     */
    public void removeTasksFromCheckoutQueueWithHashCodes( int[] hashCodes )
        throws TaskQueueException
    {
        List<CheckOutTask> queue = getProjectsInCheckoutQueue();

        for ( CheckOutTask task : queue )
        {
            if ( ArrayUtils.contains( hashCodes, task.hashCode() ) )
            {
                getCheckoutQueue().remove( task );
            }
        }
    }

    /**
     * @see OverallBuildQueue#addToBuildQueue(BuildProjectTask)
     */
    public void addToBuildQueue( BuildProjectTask buildTask )
        throws TaskQueueException
    {
        getBuildQueue().put( buildTask );
    }

    /**
     * @see OverallBuildQueue#addToBuildQueue(List)
     */
    public void addToBuildQueue( List<BuildProjectTask> buildTasks )
        throws TaskQueueException
    {
        for ( BuildProjectTask buildTask : buildTasks )
        {
            getBuildQueue().put( buildTask );
        }
    }

    /**
     * @see OverallBuildQueue#getProjectsInBuildQueue()
     */
    public List<BuildProjectTask> getProjectsInBuildQueue()
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
        List<BuildProjectTask> queue = getProjectsInBuildQueue();
        for ( BuildProjectTask buildTask : queue )
        {
            if ( buildTask != null )
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
                    if ( buildTask.getProjectId() == projectId &&
                        buildTask.getBuildDefinitionId() == buildDefinitionId )
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
    {
        BuildProjectTask task = (BuildProjectTask) buildTaskQueueExecutor.getCurrentTask();
        if ( task != null && task.getProjectId() == projectId )
        {
            log.info(
                "Cancelling build task for project '" + projectId + "' in task executor '" + buildTaskQueueExecutor );
            buildTaskQueueExecutor.cancelTask( task );
        }
    }

    /**
     * @see OverallBuildQueue#cancelCheckoutTask(int)
     */
    public void cancelCheckoutTask( int projectId )
        throws TaskQueueException
    {
        CheckOutTask task = (CheckOutTask) checkoutTaskQueueExecutor.getCurrentTask();
        if ( task != null && task.getProjectId() == projectId )
        {
            log.info( "Cancelling checkout task for project '" + projectId + "' in task executor '" +
                          checkoutTaskQueueExecutor );
            checkoutTaskQueueExecutor.cancelTask( task );
        }
    }

    /**
     * @see OverallBuildQueue#cancelCurrentBuild()
     */
    public boolean cancelCurrentBuild()
    {
        Task task = buildTaskQueueExecutor.getCurrentTask();
        if ( task != null )
        {
            return buildTaskQueueExecutor.cancelTask( task );
        }

        log.info( "No build task currently executing on build executor: " + buildTaskQueueExecutor );
        return false;
    }

    /**
     * @see OverallBuildQueue#cancelCurrentCheckout()
     */
    public boolean cancelCurrentCheckout()
    {
        Task task = checkoutTaskQueueExecutor.getCurrentTask();
        if ( task != null )
        {
            return checkoutTaskQueueExecutor.cancelTask( task );
        }

        log.info( "No checkout task currently executing on checkout task executor: " + checkoutTaskQueueExecutor );
        return false;
    }

    /**
     * @see OverallBuildQueue#removeProjectFromBuildQueue(int, int, BuildTrigger, String, int)
     */
    public boolean removeProjectFromBuildQueue( int projectId, int buildDefinitionId, BuildTrigger buildTrigger,
                                                String projectName, int projectGroupId )
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

        BuildProjectTask buildProjectTask = new BuildProjectTask( projectId, buildDefinitionId, buildTrigger,
                                                                  projectName, buildDefinitionLabel, null,
                                                                  projectGroupId );

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
        List<BuildProjectTask> queue = getProjectsInBuildQueue();

        List<BuildProjectTask> tasks = new ArrayList<BuildProjectTask>();

        for ( BuildProjectTask buildTask : queue )
        {
            if ( buildTask != null )
            {
                if ( ArrayUtils.contains( projectIds, buildTask.getProjectId() ) )
                {
                    tasks.add( buildTask );
                }
            }
        }

        for ( BuildProjectTask buildProjectTask : tasks )
        {
            log.info( "cancel build for project " + buildProjectTask.getProjectId() );
        }

        return !tasks.isEmpty() && getBuildQueue().removeAll( tasks );
    }

    /**
     * @see OverallBuildQueue#removeProjectFromBuildQueue(int)
     */
    public boolean removeProjectFromBuildQueue( int projectId )
        throws TaskQueueException
    {
        List<BuildProjectTask> queue = getProjectsInBuildQueue();

        for ( BuildProjectTask buildTask : queue )
        {
            if ( buildTask != null && buildTask.getProjectId() == projectId )
            {
                return getBuildQueue().remove( buildTask );
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
        List<BuildProjectTask> queue = getProjectsInBuildQueue();
        for ( BuildProjectTask task : queue )
        {
            if ( ArrayUtils.contains( hashCodes, task.hashCode() ) )
            {
                getBuildQueue().remove( task );
            }
        }
    }

    /* Prepare Build */

    /**
     * @see OverallBuildQueue#addToPrepareBuildQueue(PrepareBuildProjectsTask)
     */
    public void addToPrepareBuildQueue( PrepareBuildProjectsTask prepareBuildTask )
        throws TaskQueueException
    {
        getPrepareBuildQueue().put( prepareBuildTask );
    }

    /**
     * @see OverallBuildQueue#addToPrepareBuildQueue(List)
     */
    public void addToPrepareBuildQueue( List<PrepareBuildProjectsTask> prepareBuildTasks )
        throws TaskQueueException
    {
        for ( PrepareBuildProjectsTask prepareBuildTask : prepareBuildTasks )
        {
            getPrepareBuildQueue().put( prepareBuildTask );
        }
    }

    /**
     * @see OverallBuildQueue#getProjectsInPrepareBuildQueue()
     */
    public List<PrepareBuildProjectsTask> getProjectsInPrepareBuildQueue()
        throws TaskQueueException
    {
        return getPrepareBuildQueue().getQueueSnapshot();
    }

    /**
     * @see OverallBuildQueue#isInPrepareBuildQueue(int)
     */
    public boolean isInPrepareBuildQueue( int projectId )
        throws TaskQueueException
    {
        List<PrepareBuildProjectsTask> queue = getProjectsInPrepareBuildQueue();
        for ( PrepareBuildProjectsTask task : queue )
        {
            if ( task != null )
            {
                Map<Integer, Integer> map = task.getProjectsBuildDefinitionsMap();

                if ( map.size() > 0 )
                {
                    Set<Integer> projectIds = map.keySet();

                    if ( projectIds.contains( new Integer( projectId ) ) )
                    {
                        log.info( "Project {} is already in prepare build queue", projectId );
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * @see OverallBuildQueue#isInPrepareBuildQueue(int, int)
     */
    public boolean isInPrepareBuildQueue( int projectGroupId, int scmRootId )
        throws TaskQueueException
    {
        List<PrepareBuildProjectsTask> queue = getProjectsInPrepareBuildQueue();
        for ( PrepareBuildProjectsTask task : queue )
        {
            if ( task != null && task.getProjectGroupId() == projectGroupId && task.getProjectScmRootId() == scmRootId )
            {
                log.info( "Project group {} with scm root {} is in prepare build queue {}",
                          new Object[]{projectGroupId, scmRootId, task} );
                return true;
            }
        }

        return false;
    }

    /**
     * @see OverallBuildQueue#isInPrepareBuildQueue(int, String)
     */
    public boolean isInPrepareBuildQueue( int projectGroupId, String scmRootAddress )
        throws TaskQueueException
    {
        List<PrepareBuildProjectsTask> queue = getProjectsInPrepareBuildQueue();
        for ( PrepareBuildProjectsTask task : queue )
        {
            if ( task != null && task.getProjectGroupId() == projectGroupId && task.getScmRootAddress().equals(
                scmRootAddress ) )
            {
                log.info( "Project group {} with scm root {} is in prepare build queue {}",
                          new Object[]{projectGroupId, scmRootAddress, task} );
                return true;
            }
        }

        return false;
    }

    /**
     * @see OverallBuildQueue#cancelPrepareBuildTask(int, int)
     */
    public void cancelPrepareBuildTask( int projectGroupId, int scmRootId )
    {
        PrepareBuildProjectsTask task = (PrepareBuildProjectsTask) prepareBuildTaskQueueExecutor.getCurrentTask();
        if ( task != null && task.getProjectGroupId() == projectGroupId && task.getProjectScmRootId() == scmRootId )
        {
            log.info( "Cancelling prepare build task for project group '{}' with scmRootId '{}' in task executor '{}'",
                      new Object[]{projectGroupId, scmRootId, prepareBuildTaskQueueExecutor} );
            prepareBuildTaskQueueExecutor.cancelTask( task );
        }
    }

    /**
     * @see OverallBuildQueue#cancelPrepareBuildTask(int)
     */
    public void cancelPrepareBuildTask( int projectId )
    {
        PrepareBuildProjectsTask task = (PrepareBuildProjectsTask) prepareBuildTaskQueueExecutor.getCurrentTask();
        if ( task != null )
        {
            Map<Integer, Integer> map = task.getProjectsBuildDefinitionsMap();

            if ( map.size() > 0 )
            {
                Set<Integer> projectIds = map.keySet();

                if ( projectIds.contains( new Integer( projectId ) ) )
                {
                    log.info( "Cancelling prepare build task for project '{}' in task executor '{}'", projectId,
                              prepareBuildTaskQueueExecutor );
                    prepareBuildTaskQueueExecutor.cancelTask( task );
                }
            }
        }
    }

    /**
     * @see OverallBuildQueue#cancelCurrentPrepareBuild()
     */
    public boolean cancelCurrentPrepareBuild()
    {
        Task task = prepareBuildTaskQueueExecutor.getCurrentTask();
        if ( task != null )
        {
            return prepareBuildTaskQueueExecutor.cancelTask( task );
        }

        log.info( "No prepare build task currently executing on build executor: {}", buildTaskQueueExecutor );
        return false;
    }

    /**
     * @see OverallBuildQueue#removeProjectFromPrepareBuildQueue(int, int)
     */
    public boolean removeProjectFromPrepareBuildQueue( int projectGroupId, int scmRootId )
        throws TaskQueueException
    {
        List<PrepareBuildProjectsTask> tasks = getProjectsInPrepareBuildQueue();

        if ( tasks != null )
        {
            for ( PrepareBuildProjectsTask task : tasks )
            {
                if ( task.getProjectGroupId() == projectGroupId && task.getProjectScmRootId() == scmRootId )
                {
                    return getPrepareBuildQueue().remove( task );
                }
            }
        }

        return false;
    }

    /**
     * @see OverallBuildQueue#removeProjectFromPrepareBuildQueue(int, String)
     */
    public boolean removeProjectFromPrepareBuildQueue( int projectGroupId, String scmRootAddress )
        throws TaskQueueException
    {
        List<PrepareBuildProjectsTask> queue = getProjectsInPrepareBuildQueue();

        for ( PrepareBuildProjectsTask task : queue )
        {
            if ( task != null && task.getProjectGroupId() == projectGroupId &&
                task.getScmRootAddress().equals( scmRootAddress ) )
            {
                return getPrepareBuildQueue().remove( task );
            }
        }

        return false;
    }

    /**
     * @see OverallBuildQueue#removeProjectsFromPrepareBuildQueueWithHashCodes(int[])
     */
    public void removeProjectsFromPrepareBuildQueueWithHashCodes( int[] hashCodes )
        throws TaskQueueException
    {
        List<PrepareBuildProjectsTask> tasks = getProjectsInPrepareBuildQueue();

        if ( tasks != null )
        {
            for ( PrepareBuildProjectsTask task : tasks )
            {
                if ( ArrayUtils.contains( hashCodes, task.getHashCode() ) )
                {
                    getPrepareBuildQueue().remove( task );
                }
            }
        }
    }

    /**
     * @see OverallBuildQueue#getCheckoutQueue()
     */
    public TaskQueue getCheckoutQueue()
    {
        return ( (ParallelBuildsThreadedTaskQueueExecutor) checkoutTaskQueueExecutor ).getQueue();
    }

    /**
     * @see OverallBuildQueue#getBuildQueue()
     */
    public TaskQueue getBuildQueue()
    {
        return ( (ParallelBuildsThreadedTaskQueueExecutor) buildTaskQueueExecutor ).getQueue();
    }

    /**
     * @see OverallBuildQueue#getPrepareBuildQueue()
     */
    public TaskQueue getPrepareBuildQueue()
    {
        return ( (ParallelBuildsThreadedTaskQueueExecutor) prepareBuildTaskQueueExecutor ).getQueue();
    }

    /**
     * @see OverallBuildQueue#getBuildTaskQueueExecutor()
     */
    public TaskQueueExecutor getBuildTaskQueueExecutor()
    {
        return buildTaskQueueExecutor;
    }

    /**
     * @see OverallBuildQueue#getCheckoutTaskQueueExecutor()
     */
    public TaskQueueExecutor getCheckoutTaskQueueExecutor()
    {
        return checkoutTaskQueueExecutor;
    }

    /**
     * @see OverallBuildQueue#getPrepareBuildTaskQueueExecutor()
     */
    public TaskQueueExecutor getPrepareBuildTaskQueueExecutor()
    {
        return prepareBuildTaskQueueExecutor;
    }

    public void setBuildDefinitionDao( BuildDefinitionDao buildDefinitionDao )
    {
        this.buildDefinitionDao = buildDefinitionDao;
    }

    public void setBuildTaskQueueExecutor( TaskQueueExecutor buildTaskQueueExecutor )
    {
        this.buildTaskQueueExecutor = buildTaskQueueExecutor;
    }

    public void setCheckoutTaskQueueExecutor( TaskQueueExecutor checkoutTaskQueueExecutor )
    {
        this.checkoutTaskQueueExecutor = checkoutTaskQueueExecutor;
    }

    public void setPrepareBuildTaskQueueExecutor( TaskQueueExecutor prepareBuildTaskQueueExecutor )
    {
        this.prepareBuildTaskQueueExecutor = prepareBuildTaskQueueExecutor;
    }
}
