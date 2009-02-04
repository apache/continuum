package org.apache.continuum.buildmanager;

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

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.continuum.buildqueue.BuildQueueService;
import org.apache.continuum.buildqueue.BuildQueueServiceException;
import org.apache.continuum.dao.BuildDefinitionDao;
import org.apache.continuum.taskqueue.CheckOutTask;
import org.apache.continuum.taskqueue.BuildProjectTask;
import org.apache.continuum.taskqueue.OverallBuildQueue;
import org.apache.continuum.taskqueue.PrepareBuildProjectsTask;
import org.apache.continuum.taskqueueexecutor.ParallelBuildsThreadedTaskQueueExecutor;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildQueue;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;
import org.codehaus.plexus.taskqueue.Task;
import org.codehaus.plexus.taskqueue.TaskQueue;
import org.codehaus.plexus.taskqueue.TaskQueueException;
import org.codehaus.plexus.taskqueue.execution.TaskQueueExecutor;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parallel builds manager. 
 * 
 * @author <a href="mailto:oching@apache.org">Maria Odea Ching</a>
 * @version $Id$
 */
public class ParallelBuildsManager    
    implements BuildsManager, Contextualizable
{
    private Logger log = LoggerFactory.getLogger( ParallelBuildsManager.class );

    // map must be synchronized!
    private Map<Integer, OverallBuildQueue> overallBuildQueues =
        Collections.synchronizedMap( new HashMap<Integer, OverallBuildQueue>() );

    private static final int BUILD_QUEUE = 1;

    private static final int CHECKOUT_QUEUE = 2;

    @Resource
    private BuildDefinitionDao buildDefinitionDao;

    private TaskQueue prepareBuildQueue;

    @Resource
    private ConfigurationService configurationService;
        
    @Resource
    private BuildQueueService buildQueueService;

    private PlexusContainer container;
    
    /**
     * @see BuildsManager#buildProject(int, BuildDefinition, String, int)
     */
    public void buildProject( int projectId, BuildDefinition buildDefinition, String projectName, int trigger )
        throws BuildManagerException
    {
        try
        {
            if ( isInQueue( projectId, BUILD_QUEUE, -1 ) )
            {
                log.warn( "Project already queued." );
                return;
            }
        }
        catch ( TaskQueueException e )
        {
            throw new BuildManagerException( "Error occurred while checking if the project is already in queue: " +
                e.getMessage() );
        }
        
        OverallBuildQueue overallBuildQueue =
            getOverallBuildQueue( projectId, BUILD_QUEUE, buildDefinition.getSchedule().getBuildQueues() );

        String buildDefinitionLabel = buildDefinition.getDescription();
        if ( StringUtils.isEmpty( buildDefinitionLabel ) )
        {
            buildDefinitionLabel = buildDefinition.getGoals();
        }

        Task buildTask =
            new BuildProjectTask( projectId, buildDefinition.getId(), trigger, projectName, buildDefinitionLabel );
        try
        {
            log.info( "Project '" + projectName + "' added to overall build queue '" + overallBuildQueue.getName() + "'." );
            overallBuildQueue.addToBuildQueue( buildTask );
        }
        catch ( TaskQueueException e )
        {
            throw new BuildManagerException( "Error occurred while adding project to build queue: " + e.getMessage() );
        }
    }

    /**
     * @see BuildsManager#buildProjects(List, Map, int)
     */
    public void buildProjects( List<Project> projects, Map<Integer, BuildDefinition> projectsBuildDefinitionsMap,
                               int trigger )
        throws BuildManagerException
    {
        int firstProjectId = 0;
        // get id of the first project in the list that is not yet in the build queue
        for ( Project project : projects )
        {
            try
            {
                if ( !isInQueue( project.getId(), BUILD_QUEUE, -1 ) )
                {
                    firstProjectId = project.getId();
                    break;
                }
            }
            catch ( TaskQueueException e )
            {
                log.warn( "Error occurred while verifying if project is already queued." );
                continue;
            }
        }

        if ( firstProjectId != 0 )
        {
            BuildDefinition buildDef = projectsBuildDefinitionsMap.get( firstProjectId ); 
            OverallBuildQueue overallBuildQueue =
                getOverallBuildQueue( firstProjectId, BUILD_QUEUE, buildDef.getSchedule().getBuildQueues() );

            if ( overallBuildQueue != null )
            {                
                for ( Project project : projects )
                {
                    try
                    {
                        if ( isInQueue( project.getId(), BUILD_QUEUE, projectsBuildDefinitionsMap.get( project.getId() ).getId() ) )
                        {
                            log.warn( "Project '" + project.getId() + "' - '" + project.getName() +
                                "' is already in build queue." );
                            continue;
                        }
                    }
                    catch ( TaskQueueException e )
                    {
                        log.warn( "Error occurred while verifying if project is already queued." );
                        continue;
                    }

                    BuildDefinition buildDefinition = projectsBuildDefinitionsMap.get( project.getId() );
                    String buildDefinitionLabel = buildDefinition.getDescription();
                    if ( StringUtils.isEmpty( buildDefinitionLabel ) )
                    {
                        buildDefinitionLabel = buildDefinition.getGoals();
                    }

                    BuildProjectTask buildTask =
                        new BuildProjectTask( project.getId(), buildDefinition.getId(), trigger, project.getName(),
                                              buildDefinitionLabel );
                    buildTask.setMaxExecutionTime( buildDefinition.getSchedule().getMaxJobExecutionTime() * 1000 );

                    try
                    {
                        log.info( "Project '" + project.getId() + "' - '" + project.getName() +
                            "' added to overall build queue '" + overallBuildQueue.getName() + "'." );
                        
                        overallBuildQueue.addToBuildQueue( buildTask );
                    }
                    catch ( TaskQueueException e )
                    {
                        throw new BuildManagerException( "Error occurred while adding project to build queue: " +
                            e.getMessage() );
                    }
                }
            }
        }
        else
        {
            log.error( "Projects are already in build queue." );
            throw new BuildManagerException( "Projects are already in build queue." );
        }
    }

    /**
     * @see BuildsManager#cancelBuildInQueue(int)
     */
    public boolean cancelBuildInQueue( int buildQueueId )
        throws BuildManagerException
    {
        synchronized ( overallBuildQueues )
        {
            OverallBuildQueue overallBuildQueue = null;            
            overallBuildQueue = overallBuildQueues.get( buildQueueId );
            if ( overallBuildQueue != null )
            {
                overallBuildQueue.cancelCurrentBuild();
            }
            else
            {
                log.warn( "Project not found in any of the build queues." );
            }

            return true;
        }
    }

    /**
     * @see BuildsManager#cancelAllBuilds()
     */
    public boolean cancelAllBuilds()
        throws BuildManagerException
    {
        synchronized ( overallBuildQueues )
        {
            Set<Integer> keySet = overallBuildQueues.keySet();
            OverallBuildQueue overallBuildQueue = null;
            for ( Integer key : keySet )
            {
                overallBuildQueue = overallBuildQueues.get( key );
                overallBuildQueue.cancelCurrentBuild();
            }

            return true;
        }
    }

    /**
     * @see BuildsManager#cancelAllCheckouts()
     */
    public boolean cancelAllCheckouts()
        throws BuildManagerException
    {
        synchronized ( overallBuildQueues )
        {
            Set<Integer> keySet = overallBuildQueues.keySet();
            OverallBuildQueue overallBuildQueue = null;
            for ( Integer key : keySet )
            {
                overallBuildQueue = overallBuildQueues.get( key );
                overallBuildQueue.cancelCurrentCheckout();
            }

            return true;
        }
    }

    /**
     * @see BuildsManager#cancelBuild(int)
     */
    public boolean cancelBuild( int projectId )
        throws BuildManagerException
    {
        try
        {
            OverallBuildQueue overallBuildQueue = getOverallBuildQueueWhereProjectIsQueued( projectId, BUILD_QUEUE );
            if ( overallBuildQueue != null )
            {
                overallBuildQueue.cancelBuildTask( projectId );
            }
            else
            {
                synchronized( overallBuildQueues )
                {
                    Set<Integer> keySet = overallBuildQueues.keySet();
                    for( Integer key : keySet )
                    {
                        overallBuildQueue = overallBuildQueues.get( key );
                        BuildProjectTask buildTask = (BuildProjectTask) overallBuildQueue.getBuildTaskQueueExecutor().getCurrentTask();
                        if( buildTask != null && buildTask.getProjectId() == projectId )
                        {
                            overallBuildQueue.cancelBuildTask( projectId );
                            return true;
                        }
                    }
                    log.error( "Project '" + projectId + "' not found in any of the builds queues." );
                }
            }
        }
        catch ( TaskQueueException e )
        {
            throw new BuildManagerException( "Error occurred while cancelling build: " + e.getMessage() );
        }
        
        return true;
    }

    /**
     * @see BuildsManager#cancelCheckout(int)
     */
    public boolean cancelCheckout( int projectId )
        throws BuildManagerException
    {
        try
        {
            OverallBuildQueue overallBuildQueue = getOverallBuildQueueWhereProjectIsQueued( projectId, CHECKOUT_QUEUE );
            if ( overallBuildQueue != null )
            {
                overallBuildQueue.cancelCheckoutTask( projectId );
            }
            else
            {
                synchronized( overallBuildQueues )
                {
                    Set<Integer> keySet = overallBuildQueues.keySet();
                    for( Integer key : keySet )
                    {
                        overallBuildQueue = overallBuildQueues.get( key );
                        CheckOutTask checkoutTask = (CheckOutTask) overallBuildQueue.getCheckoutTaskQueueExecutor().getCurrentTask();                        
                        if( checkoutTask != null && checkoutTask.getProjectId() == projectId )
                        {
                            overallBuildQueue.cancelCheckoutTask( projectId );
                            return true;
                        }
                    }
                    log.info( "Project '" + projectId + "' not found in any of the checkout queues." );
                }
            }
        }
        catch ( TaskQueueException e )
        {
            throw new BuildManagerException( "Error occurred while cancelling build: " + e.getMessage() );
        }

        return true;
    }

    /**
     * @see BuildsManager#checkoutProject(int, String, File, String, String, BuildDefinition)
     */
    public void checkoutProject( int projectId, String projectName, File workingDirectory, String scmUsername,
                                 String scmPassword, BuildDefinition defaultBuildDefinition )
        throws BuildManagerException
    {
        try
        {
            if ( isInQueue( projectId, CHECKOUT_QUEUE, -1 ) )
            {
                log.warn( "Project already in checkout queue." );
                return;
            }
        }
        catch ( TaskQueueException e )
        {
            throw new BuildManagerException( "Error occurred while checking if the project is already in queue: " +
                e.getMessage() );
        }

        OverallBuildQueue overallBuildQueue =
            getOverallBuildQueue( projectId, CHECKOUT_QUEUE, defaultBuildDefinition.getSchedule().getBuildQueues() );
        CheckOutTask checkoutTask =
            new CheckOutTask( projectId, workingDirectory, projectName, scmUsername, scmPassword );
        try
        {
            if( overallBuildQueue != null )
            {
                log.info( "Project '" + projectName + "' added to overall build queue '" + overallBuildQueue.getName() + "'." );
                overallBuildQueue.addToCheckoutQueue( checkoutTask );
            }
            else
            {
                throw new BuildManagerException( "Unable to add project to checkout queue. No overall build queue configured." );
            }
        }
        catch ( TaskQueueException e )
        {
            throw new BuildManagerException( "Error occurred while adding project to checkout queue: " + e.getMessage() );
        }
    }

    /**
     * @see BuildsManager#isInAnyBuildQueue(int)
     */
    public boolean isInAnyBuildQueue( int projectId )
        throws BuildManagerException
    {
        try
        {
            return isInQueue( projectId, BUILD_QUEUE, -1 );
        }
        catch ( TaskQueueException e )
        {
            throw new BuildManagerException( e.getMessage() );
        }
    }

    /**
     * @see BuildsManager#isInAnyBuildQueue(int, int)
     */
    public boolean isInAnyBuildQueue( int projectId, int buildDefinitionId )
        throws BuildManagerException
    {
        try
        {
            return isInQueue( projectId, BUILD_QUEUE, buildDefinitionId );
        }
        catch ( TaskQueueException e )
        {
            throw new BuildManagerException( e.getMessage() );
        }
    }

    /**
     * @see BuildsManager#isInAnyCheckoutQueue(int)
     */
    public boolean isInAnyCheckoutQueue( int projectId )
        throws BuildManagerException
    {
        try
        {
            return isInQueue( projectId, CHECKOUT_QUEUE, -1 );
        }
        catch ( TaskQueueException e )
        {
            throw new BuildManagerException( e.getMessage() );
        }
    }

    /**
     * @see BuildsManager#isAnyProjectCurrentlyBeingCheckedOut(int[])
     */
    public boolean isAnyProjectCurrentlyBeingCheckedOut( int[] projectIds )
        throws BuildManagerException
    {        
        for( int i = 0; i < projectIds.length; i++ )
        {
            Map<String, Task> checkouts = getCurrentCheckouts();
            Set<String> keySet = checkouts.keySet();
            for( String key : keySet )
            {
                CheckOutTask task = (CheckOutTask) checkouts.get( key );
                if( task.getProjectId() == projectIds[i] )
                {
                    return true;
                }
            }
        }
        return false;
    }
    /**
     * @see BuildsManager#isInPrepareBuildQueue(int)
     */
    public boolean isInPrepareBuildQueue( int projectId )
        throws BuildManagerException
    {
        try
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
        }
        catch ( TaskQueueException e )
        {
            throw new BuildManagerException( e.getMessage() );
        }

        return false;
    }

    /**
     * @see BuildsManager#isProjectInAnyCurrentBuild(int)
     */
    public boolean isProjectInAnyCurrentBuild( int projectId )
        throws BuildManagerException
    {
        synchronized( overallBuildQueues )
        {
            Set<Integer> keys = overallBuildQueues.keySet();
            for( Integer key : keys )
            {
                OverallBuildQueue overallBuildQueue = overallBuildQueues.get( key );
                BuildProjectTask task = (BuildProjectTask) overallBuildQueue.getBuildTaskQueueExecutor().getCurrentTask();
                if( task != null && task.getProjectId() == projectId )
                {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * @see BuildsManager#prepareBuildProjects(Map, int, int, String, String, int)
     */
    public void prepareBuildProjects( Map<Integer, Integer> projectsBuildDefinitionsMap, int trigger, int projectGroupId, String projectGroupName, String scmRootAddress, int scmRootId )
        throws BuildManagerException
    {
        try
        {            
            PrepareBuildProjectsTask task =
                new PrepareBuildProjectsTask( projectsBuildDefinitionsMap, trigger, projectGroupId, projectGroupName, scmRootAddress, scmRootId );
            
            log.info( "Queueing prepare-build-project task '" + task + "' to prepare-build queue." );
            prepareBuildQueue.put( task );
        }
        catch ( TaskQueueException e )
        {
            throw new BuildManagerException( "Error occurred while creating prepare-build-project task: " +
                e.getMessage() );
        }
    }

    /**
     * @see BuildsManager#removeProjectFromBuildQueue(int)
     */
    public void removeProjectFromBuildQueue( int projectId )
        throws BuildManagerException
    {
        try
        {
            OverallBuildQueue overallBuildQueue = getOverallBuildQueueWhereProjectIsQueued( projectId, BUILD_QUEUE );
            if ( overallBuildQueue != null )
            {
                overallBuildQueue.removeProjectFromBuildQueue( projectId );
            }
            else
            {
                log.info( "Project '" + projectId + "' not found in any of the build queues." );
            }
        }
        catch ( TaskQueueException e )
        {
            throw new BuildManagerException( "Error occurred while removing project from build queue: " +
                e.getMessage() );
        }
    }

    /**
     * @see BuildsManager#removeProjectFromBuildQueue(int, int, int, String)
     */
    public void removeProjectFromBuildQueue( int projectId, int buildDefinitionId, int trigger, String projectName )
        throws BuildManagerException
    {
        try
        {
            OverallBuildQueue overallBuildQueue = getOverallBuildQueueWhereProjectIsQueued( projectId, BUILD_QUEUE );
            if ( overallBuildQueue != null )
            {
                overallBuildQueue.removeProjectFromBuildQueue( projectId, buildDefinitionId, trigger, projectName );
            }
            else
            {
                log.info( "Project '" + projectId + "' not found in any of the build queues." );
            }
        }
        catch ( TaskQueueException e )
        {
            throw new BuildManagerException( "Error occurred while removing project from build queue: " +
                e.getMessage() );
        }
    }

    /**
     * @see BuildsManager#removeProjectFromCheckoutQueue(int)
     */
    public void removeProjectFromCheckoutQueue( int projectId )
        throws BuildManagerException
    {
        try
        {
            OverallBuildQueue overallBuildQueue = getOverallBuildQueueWhereProjectIsQueued( projectId, CHECKOUT_QUEUE );
            if ( overallBuildQueue != null )
            {
                overallBuildQueue.removeProjectFromCheckoutQueue( projectId );
            }
            else
            {
                log.info( "Project '" + projectId + "' not found in any of the checkout queues." );
            }
        }
        catch ( TaskQueueException e )
        {
            throw new BuildManagerException( "Error occurred while removing project from checkout queue: " +
                e.getMessage() );
        }
    }

    /**
     * @see BuildsManager#removeProjectsFromBuildQueue(int[])
     */
    public void removeProjectsFromBuildQueue( int[] projectIds )
    {
        for ( int i = 0; i < projectIds.length; i++ )
        {
            try
            {
                OverallBuildQueue overallBuildQueue =
                    getOverallBuildQueueWhereProjectIsQueued( projectIds[i], BUILD_QUEUE );
                if ( overallBuildQueue != null )
                {
                    overallBuildQueue.removeProjectFromBuildQueue( projectIds[i] );
                }
                else
                {
                    log.error( "Project '" + projectIds[i] + "' not found in any of the build queues." );
                    continue;
                }
            }
            catch ( TaskQueueException e )
            {
                log.error( "Error occurred while removing project '" + projectIds[i] + "' from build queue." );
                continue;
            }
        }
    }

    /**
     * @see BuildsManager#removeProjectsFromCheckoutQueue(int[])
     */
    public void removeProjectsFromCheckoutQueue( int[] projectIds )
    {
        for ( int i = 0; i < projectIds.length; i++ )
        {
            try
            {
                OverallBuildQueue overallBuildQueue =
                    getOverallBuildQueueWhereProjectIsQueued( projectIds[i], CHECKOUT_QUEUE );
                if ( overallBuildQueue != null )
                {
                    overallBuildQueue.removeProjectFromCheckoutQueue( projectIds[i] );
                }
                else
                {
                    log.error( "Project '" + projectIds[i] + "' not found in any of the checkout queues." );
                    continue;
                }
            }
            catch ( TaskQueueException e )
            {
                log.error( "Error occurred while removing project '" + projectIds[i] + "' from checkout queue." );
                continue;
            }
        }
    }

    /**
     * @see BuildsManager#removeProjectsFromCheckoutQueueWithHashcodes(int[])
     */
    public void removeProjectsFromCheckoutQueueWithHashcodes( int[] hashcodes )
        throws BuildManagerException
    {
        try
        {
            synchronized ( overallBuildQueues )
            {
                Set<Integer> keySet = overallBuildQueues.keySet();
                for ( Integer key : keySet )
                {
                    OverallBuildQueue overallBuildQueue = overallBuildQueues.get( key );
                    overallBuildQueue.removeTasksFromCheckoutQueueWithHashCodes( hashcodes );                   
                }
            }
        }
        catch ( TaskQueueException e )
        {
            throw new BuildManagerException( "Error encountered while removing project(s) from checkout queue.", e );
        }
    }

    /**
     * @see BuildsManager#removeProjectsFromBuildQueueWithHashcodes(int[])
     */
    public void removeProjectsFromBuildQueueWithHashcodes( int[] hashcodes )
        throws BuildManagerException
    {
        try
        {
            synchronized ( overallBuildQueues )
            {
                Set<Integer> keySet = overallBuildQueues.keySet();
                for ( Integer key : keySet )
                {
                    OverallBuildQueue overallBuildQueue = overallBuildQueues.get( key );
                    overallBuildQueue.removeProjectsFromBuildQueueWithHashCodes( hashcodes );
                }
            }
        }
        catch ( TaskQueueException e )
        {
            throw new BuildManagerException( "Error encountered while removing project(s) from build queue.", e );
        }
    }
    
    public boolean removeProjectGroupFromPrepareBuildQueue( int projectGroupId, String scmRootAddress )
        throws BuildManagerException
    {
        try
        {
            List<PrepareBuildProjectsTask> queue = prepareBuildQueue.getQueueSnapshot();

            for ( PrepareBuildProjectsTask task : queue )
            {
                if ( task != null && task.getProjectGroupId() == projectGroupId &&
                    task.getScmRootAddress().equals( scmRootAddress ) )
                {
                    return prepareBuildQueue.remove( task );
                }
            }
            return false;
        }
        catch ( TaskQueueException e )
        {
            throw new BuildManagerException( "Error while getting the prepare build projects task in queue", e );
        }
    }
    
    /**
     * @see BuildsManager#addOverallBuildQueue(BuildQueue)
     */
    public void addOverallBuildQueue( BuildQueue buildQueue )
        throws BuildManagerException
    {
        synchronized ( overallBuildQueues )
        {
            try
            {
                OverallBuildQueue overallBuildQueue = (OverallBuildQueue) container.lookup( OverallBuildQueue.class );
                overallBuildQueue.setId( buildQueue.getId() );
                overallBuildQueue.setName( buildQueue.getName() );
                
                if ( overallBuildQueues.get( buildQueue.getId() ) == null )
                {
                    log.info( "Adding overall build queue to map : " + overallBuildQueue.getName() );
                    overallBuildQueues.put( overallBuildQueue.getId(), overallBuildQueue );
                }
                else
                {
                    log.warn( "Overall build queue already in the map." );
                }
            }
            catch ( ComponentLookupException e )
            {                
                throw new BuildManagerException( "Error creating overall build queue.", e );
            }
        }
    }

    /**
     * @see BuildsManager#removeOverallBuildQueue(int)
     */
    public void removeOverallBuildQueue( int overallBuildQueueId )
        throws BuildManagerException
    {
        List<Task> tasks = null;
        List<CheckOutTask> checkoutTasks = null;

        synchronized ( overallBuildQueues )
        {                        
            OverallBuildQueue overallBuildQueue = overallBuildQueues.get( overallBuildQueueId );
            if ( overallBuildQueue.getName().equals( ConfigurationService.DEFAULT_BUILD_QUEUE_NAME ) )
            {
                throw new BuildManagerException( "Cannot remove default build queue." );
            }

            try
            {
                if( overallBuildQueue.getBuildTaskQueueExecutor().getCurrentTask() != null ||
                                overallBuildQueue.getCheckoutTaskQueueExecutor().getCurrentTask() != null )
                {
                    throw new BuildManagerException( "Cannot remove build queue. A task is currently executing." );
                }

                tasks = overallBuildQueue.getProjectsInBuildQueue();
                checkoutTasks = overallBuildQueue.getProjectsInCheckoutQueue();

                overallBuildQueue.getBuildQueue().removeAll( tasks );
                overallBuildQueue.getCheckoutQueue().removeAll( checkoutTasks );

                ( (ParallelBuildsThreadedTaskQueueExecutor) overallBuildQueue.getBuildTaskQueueExecutor() ).stop();
                ( (ParallelBuildsThreadedTaskQueueExecutor) overallBuildQueue.getCheckoutTaskQueueExecutor() ).stop();
                container.release( overallBuildQueue );
            }
            catch ( TaskQueueException e )
            {
                throw new BuildManagerException(
                                                 "Cannot remove build queue. An error occurred while retrieving queued tasks." );
            }
            catch ( ComponentLifecycleException e )
            {
                throw new BuildManagerException(
                                                 "Cannot remove build queue. An error occurred while destroying the build queue: " +
                                                     e.getMessage() );
            }
            catch ( StoppingException e )
            {
                throw new BuildManagerException(
                                                "Cannot remove build queue. An error occurred while stopping the build queue: " +
                                                    e.getMessage() );
            }

            this.overallBuildQueues.remove( overallBuildQueueId );
            log.info( "Removed overall build queue '" + overallBuildQueueId + "' from build queues map." );
        }

        
        for ( Task task : tasks )
        {            
            BuildProjectTask buildTask = (BuildProjectTask) task;
            try
            {
                BuildDefinition buildDefinition =
                    buildDefinitionDao.getBuildDefinition( buildTask.getBuildDefinitionId() );
            
                buildProject( buildTask.getProjectId(), buildDefinition, buildTask.getProjectName(),
                              buildTask.getTrigger() );
            }
            catch ( ContinuumStoreException e )
            {
                log.error( "Unable to queue build task for project '" + buildTask.getProjectName() + "'" );
                continue;
            }
        }

        for ( CheckOutTask task : checkoutTasks )
        {
            try
            {
                BuildDefinition buildDefinition = buildDefinitionDao.getDefaultBuildDefinition( task.getProjectId() );
                checkoutProject( task.getProjectId(), task.getProjectName(), task.getWorkingDirectory(),
                                 task.getScmUserName(), task.getScmPassword(), buildDefinition );            
            }
            catch ( ContinuumStoreException e )
            {
                log.error( "Unable to queue checkout task for project '" + task.getProjectName() + "'" );
                continue;
            }
        }        
    }

    public Map<Integer, OverallBuildQueue> getOverallBuildQueues()
    {
        return overallBuildQueues;
    }

    /**
     * @see BuildsManager#getCurrentBuilds()
     */
    public Map<String, Task> getCurrentBuilds()
        throws BuildManagerException
    {
        synchronized( overallBuildQueues )
        {
            Map<String, Task> currentBuilds = new HashMap<String, Task>();            
            Set<Integer> keys = overallBuildQueues.keySet();
            for( Integer key : keys )
            {
                OverallBuildQueue overallBuildQueue = overallBuildQueues.get( key );                
                Task task = overallBuildQueue.getBuildTaskQueueExecutor().getCurrentTask();
                if( task != null )
                {
                    currentBuilds.put( overallBuildQueue.getName(), task );
                }                
            }            
            return currentBuilds;
        }
    }

    /**
     * @see BuildsManager#getCurrentCheckouts()
     */
    public Map<String, Task> getCurrentCheckouts()
        throws BuildManagerException
    {
        synchronized( overallBuildQueues )
        {
            Map<String, Task> currentCheckouts = new HashMap<String, Task>();
            Set<Integer> keys = overallBuildQueues.keySet();
            for( Integer key : keys )
            {
                OverallBuildQueue overallBuildQueue = overallBuildQueues.get( key );                
                Task task = overallBuildQueue.getCheckoutTaskQueueExecutor().getCurrentTask();
                if( task != null )
                {
                    currentCheckouts.put( overallBuildQueue.getName(), task );
                }                
            }            
            return currentCheckouts;           
        }
    }

    /**
     * @see BuildsManager#getProjectsInBuildQueues()
     */
    public Map<String, List<Task>> getProjectsInBuildQueues()
        throws BuildManagerException
    {
        synchronized ( overallBuildQueues )
        {
            Map<String, List<Task>> queuedBuilds = new HashMap<String, List<Task>>();
            Set<Integer> keySet = overallBuildQueues.keySet();
            for ( Integer key : keySet )
            {
                OverallBuildQueue overallBuildQueue = overallBuildQueues.get( key );
                try
                {
                    queuedBuilds.put( overallBuildQueue.getName(), overallBuildQueue.getProjectsInBuildQueue() );
                }
                catch ( TaskQueueException e )
                {
                    throw new BuildManagerException( "Error occurred while getting projects in build queue '" +
                        overallBuildQueue.getName() + "'.", e );
                }
            }
            return queuedBuilds;
        }
    }

    /**
     * @see BuildsManager#getProjectsInCheckoutQueues()
     */
    public Map<String, List<Task>> getProjectsInCheckoutQueues()
        throws BuildManagerException
    {
        synchronized ( overallBuildQueues )
        {
            Map<String, List<Task>> queuedCheckouts = new HashMap<String, List<Task>>();
            Set<Integer> keySet = overallBuildQueues.keySet();
            for ( Integer key : keySet )
            {
                OverallBuildQueue overallBuildQueue = overallBuildQueues.get( key );
                try
                {
                    queuedCheckouts.put( overallBuildQueue.getName(), overallBuildQueue.getProjectsInCheckoutQueue() );
                }
                catch ( TaskQueueException e )
                {
                    throw new BuildManagerException( "Error occurred while getting projects in build queue '" +
                        overallBuildQueue.getName() + "'.", e );
                }
            }
            return queuedCheckouts;
        }
    }
    
    /**
     * @see BuildsManager#cancelAllPrepareBuilds()
     */
    public boolean cancelAllPrepareBuilds() throws BuildManagerException
    {
        try
        {
            TaskQueueExecutor executor = ( TaskQueueExecutor ) container.lookup( TaskQueueExecutor.class, "prepare-build-project" );
            Task task = executor.getCurrentTask();            
            if( task != null )
            {
                executor.cancelTask( task );
            }
        }
        catch ( ComponentLookupException e )
        {
            throw new BuildManagerException( "Error looking up prepare-build-queue.", e );
        }
        
        return false;
    }

    /**
     * @see BuildsManager#isBuildInProgress()
     */
    public boolean isBuildInProgress()
    {        
        synchronized( overallBuildQueues )
        {
            Set<Integer> keySet = overallBuildQueues.keySet();
            for( Integer key : keySet )
            {
                OverallBuildQueue overallBuildQueue = overallBuildQueues.get( key );
                if( overallBuildQueue.getBuildTaskQueueExecutor().getCurrentTask() != null )
                {
                    return true;
                }
            }
            return false;
        }
    }

    private boolean isInQueue( int projectId, int typeOfQueue, int buildDefinitionId )
        throws TaskQueueException
    {
        synchronized ( overallBuildQueues )
        {
            Set<Integer> keySet = overallBuildQueues.keySet();
            for ( Integer key : keySet )
            {
                OverallBuildQueue overallBuildQueue = overallBuildQueues.get( key );
                if ( typeOfQueue == BUILD_QUEUE )
                {
                    if ( buildDefinitionId < 0 )
                    {
                        if ( overallBuildQueue.isInBuildQueue( projectId ) )
                        {
                            return true;
                        }
                    }
                    else
                    {
                        if ( overallBuildQueue.isInBuildQueue( projectId, buildDefinitionId ) )
                        {
                            return true;
                        }
                    }
                }
                else if ( typeOfQueue == CHECKOUT_QUEUE )
                {
                    if ( overallBuildQueue.isInCheckoutQueue( projectId ) )
                    {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    // get overall queue where project is queued
    private OverallBuildQueue getOverallBuildQueueWhereProjectIsQueued( int projectId, int typeOfQueue )
        throws TaskQueueException
    {
        synchronized ( overallBuildQueues )
        {
            OverallBuildQueue whereQueued = null;
            Set<Integer> keySet = overallBuildQueues.keySet();

            for ( Integer key : keySet )
            {
                OverallBuildQueue overallBuildQueue = overallBuildQueues.get( key );
                if ( typeOfQueue == BUILD_QUEUE )
                {
                    if ( overallBuildQueue.isInBuildQueue( projectId ) )
                    {
                        whereQueued = overallBuildQueue;
                        break;
                    }
                }
                else if ( typeOfQueue == CHECKOUT_QUEUE )
                {
                    if ( overallBuildQueue.isInCheckoutQueue( projectId ) )
                    {
                        whereQueued = overallBuildQueue;
                        break;
                    }
                }
            }

            return whereQueued;
        }
    }

    // get overall queue where project will be queued
    private OverallBuildQueue getOverallBuildQueue( int projectId, int typeOfQueue, List<BuildQueue> buildQueues )
        throws BuildManagerException
    {
        OverallBuildQueue whereToBeQueued = null;
        synchronized ( overallBuildQueues )
        {
            if ( overallBuildQueues == null || overallBuildQueues.isEmpty() )
            {
                throw new BuildManagerException( "No build queues configured." );
            }

            int size = 0;
            int idx = 0;            
            int allowedBuilds = configurationService.getNumberOfBuildsInParallel();
            
            try
            {
                int count = 1;
                for ( BuildQueue buildQueue : buildQueues )
                {            
                    if( count <= allowedBuilds )
                    {   
                        OverallBuildQueue overallBuildQueue = overallBuildQueues.get( buildQueue.getId() );
                        if ( overallBuildQueue != null )
                        {
                            TaskQueue taskQueue = null;
                            if ( typeOfQueue == BUILD_QUEUE )
                            {
                                taskQueue = overallBuildQueue.getBuildQueue();
                            }
                            else if ( typeOfQueue == CHECKOUT_QUEUE )
                            {
                                taskQueue = overallBuildQueue.getCheckoutQueue();
                            }
                            
                            if ( idx == 0 )
                            {
                                size = taskQueue.getQueueSnapshot().size();
                                whereToBeQueued = overallBuildQueue;
                            }
    
                            if ( taskQueue.getQueueSnapshot().size() < size )
                            {
                                whereToBeQueued = overallBuildQueue;
                                size = taskQueue.getQueueSnapshot().size();
                            }
    
                            idx++;
                        }
                        else
                        {
                            log.error( "Build queue not found." );                            
                        }
                        count++;
                    }
                    else
                    {
                        break;
                    }   
                }
            }
            catch ( TaskQueueException e )
            {
                throw new BuildManagerException( "Error occurred while retrieving task quueue: " + e.getMessage() );
            }
        }

        // use default overall build queue if none is configured
        if ( whereToBeQueued == null )
        {                
            Set<Integer> keySet = overallBuildQueues.keySet();
            for( Integer key : keySet )
            {
                OverallBuildQueue overallBuildQueue = overallBuildQueues.get( key );                
                if( overallBuildQueue.getName().equals( ConfigurationService.DEFAULT_BUILD_QUEUE_NAME ) )
                {
                    return overallBuildQueue;
                }
            }
        }

        return whereToBeQueued;
    }
    
    public void contextualize( Context context )
        throws ContextException
    {
        container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY ); 
        
        synchronized ( overallBuildQueues )
        {
            try
            {
                // create all the build queues configured in the database, not just the default!               
                List<BuildQueue> buildQueues = buildQueueService.getAllBuildQueues();
                for( BuildQueue buildQueue : buildQueues )
                {   
                    createOverallBuildQueue( buildQueue );
                }               

                // add default overall build queue if not yet added to the map
                BuildQueue defaultBuildQueue = configurationService.getDefaultBuildQueue();
                if( overallBuildQueues.get( defaultBuildQueue.getId() ) == null )
                {
                    createOverallBuildQueue( defaultBuildQueue );
                }
            }
            catch ( ComponentLookupException e )
            {
                log.error( "Cannot create overall build queue: " + e.getMessage() );
            }
            catch ( BuildQueueServiceException e )
            {
                log.error( "Cannot create overall build queue: " + e.getMessage() );
            }
        }
    }

    public void setContainer( PlexusContainer container )
    {
        this.container = container;
    }
    
    private OverallBuildQueue createOverallBuildQueue( BuildQueue defaultBuildQueue )
        throws ComponentLookupException
    {
        OverallBuildQueue overallBuildQueue =
            (OverallBuildQueue) container.lookup( OverallBuildQueue.class );
        overallBuildQueue.setId( defaultBuildQueue.getId() );
        overallBuildQueue.setName( defaultBuildQueue.getName() );
    
        overallBuildQueues.put( overallBuildQueue.getId(), overallBuildQueue );
        
        return overallBuildQueue;
    }
    
    public TaskQueue getPrepareBuildQueue()
    {
        return prepareBuildQueue;
    }
    
    public void setPrepareBuildQueue( TaskQueue prepareBuildQueue )
    {
        this.prepareBuildQueue = prepareBuildQueue;
    }
    
    // for unit tests.. 
    
    public void setOverallBuildQueues( Map<Integer, OverallBuildQueue> overallBuildQueues )
    {
        this.overallBuildQueues = overallBuildQueues;
    }

    public void setConfigurationService( ConfigurationService configurationService )
    {
        this.configurationService = configurationService;
    }

    public void setBuildQueueService( BuildQueueService buildQueueService )
    {
        this.buildQueueService = buildQueueService;
    }
    
    public void setBuildDefinitionDao( BuildDefinitionDao buildDefinitionDao )
    {
        this.buildDefinitionDao = buildDefinitionDao;
    }
}
