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

import org.apache.continuum.dao.BuildDefinitionDao;
import org.apache.continuum.taskqueue.OverallBuildQueue;
import org.apache.maven.continuum.buildqueue.BuildProjectTask;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildQueue;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.scm.queue.CheckOutTask;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.taskqueue.Task;
import org.codehaus.plexus.taskqueue.TaskQueue;
import org.codehaus.plexus.taskqueue.TaskQueueException;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parallel builds manager.
 * 
 * @author <a href="mailto:oching@apache.org">Maria Odea Ching</a>
 * @plexus.component role="org.apache.continuum.buildmanager.BuildsManager" role-hint="parallel"
 */
public class ParallelBuildsManager
    implements BuildsManager
{
    private Logger log = LoggerFactory.getLogger( ParallelBuildsManager.class );
        
    // map must be synchronized
    private Map<Integer, OverallBuildQueue> overallBuildQueues =
        Collections.synchronizedMap( new HashMap<Integer, OverallBuildQueue>() );
    
    private static final int BUILD_QUEUE = 1;
    
    private static final int CHECKOUT_QUEUE = 2;
    
    private static final int PREPARE_BUILD_QUEUE = 3;
    
    /**
     * @plexus.requirement
     */
    private BuildDefinitionDao buildDefinitionDao;
        
    // REQUIREMENTS:
    // UI:
    // - add a new page for adding a build queue. It should have a build queue name that 
    //   will be used to associate it with a schedule. The number of build queues that can 
    //   be added should respect the "Number of Allowed Builds in Parallel" set in the 
    //   General Configuration.
    // - in the add/edit schedule page, add a list box that contains the build queues which 
    //   would allow the user to select which build queue(s) to associate with the schedule.
    //
    // Back-end:
    // 1. when a build is triggered:
    //    * check for available build queue(s) associated with the schedule. Get the first available 
    //          build queue.
    //    * add the project to the associated build queue's checkout queue or build queue depending 
    //          whether the project is configured to always "build fresh"
    //    * once the build finishes, remove the project build instance from the associated build queue
    //    * build the next project in the queue
    //2. for releases:
    //    * projects to be released will be built in sequence in a queue of their own. As long as 
    //          they don't modify the build state or working copy it is ok to build the original 
    //          project simultaneously. If the working copy changes are made in place for bumping 
    //          versions, suggest blocking it and being built elsewhere.
        
    public void buildProject( int projectId, BuildDefinition buildDefinition, String projectName, int trigger ) throws BuildManagerException
    {   
        try
        {
            if( isInQueue( projectId, BUILD_QUEUE ) )
            {
                log.warn( "Project already queued." );
                return;
            }
        }
        catch ( TaskQueueException e )
        {
            throw new BuildManagerException( "Error occurred while checking if the project is already in queue: " + e.getMessage() );
        }
        
        OverallBuildQueue overallBuildQueue =
            getOverallBuildQueue( projectId, BUILD_QUEUE, buildDefinition.getSchedule().getBuildQueues() );
                
        String buildDefinitionLabel = buildDefinition.getDescription();
        if ( StringUtils.isEmpty( buildDefinitionLabel ) )
        {
            buildDefinitionLabel = buildDefinition.getGoals();
        }
        
        Task buildTask = new BuildProjectTask( projectId, buildDefinition.getId(), trigger, projectName,
                                                 buildDefinitionLabel);
        try
        {   
            overallBuildQueue.addToBuildQueue( buildTask );
        }
        catch ( TaskQueueException e )
        {
            throw new BuildManagerException( "Error occurred while adding project to build queue: "  + e.getMessage() );
        }        
    }
    
    public void buildProjects( List<Project> projects,
                   Map<Integer, BuildDefinition> projectsBuildDefinitionsMap, int trigger ) throws BuildManagerException
    {   
        int firstProjectId = 0;        
        // get id of the first project in the list that is not yet in the build queue
        for( Project project : projects )
        {
            try
            {
                if( !isInQueue( project.getId(), BUILD_QUEUE ) )
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
         
        if( firstProjectId != 0 )
        {
            BuildDefinition buildDef = projectsBuildDefinitionsMap.get( firstProjectId );
            OverallBuildQueue overallBuildQueue =
                getOverallBuildQueue( firstProjectId, BUILD_QUEUE, buildDef.getSchedule().getBuildQueues() );;
            
            if( overallBuildQueue != null )
            {
                for( Project project :  projects )
                {   
                    try
                    {
                        if( isInQueue( project.getId(), BUILD_QUEUE ) )
                        {
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
                    
                    Task buildTask = new BuildProjectTask( project.getId(), buildDefinition.getId(), trigger, project.getName(),
                                                             buildDefinitionLabel);
                    try
                    {   
                        overallBuildQueue.addToBuildQueue( buildTask );
                    }
                    catch ( TaskQueueException e )
                    {
                        throw new BuildManagerException( "Error occurred while adding project to build queue: "  + e.getMessage() );
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
    
    public boolean cancelBuildInQueue( int buildQueueId ) throws BuildManagerException
    {            
        synchronized( overallBuildQueues )
        {       
            OverallBuildQueue overallBuildQueue = null;
            try
            {                
                overallBuildQueue = overallBuildQueues.get( buildQueueId );
                if( overallBuildQueue != null )
                {
                    overallBuildQueue.cancelCurrentBuild();
                }
                else
                {
                    log.warn( "Project not found in any of the build queues." );
                }
            }
            catch ( TaskQueueException e )
            {
                log.error( "Cannot cancel build on build queue '" + overallBuildQueue.getName() + "'." );
                throw new BuildManagerException( "Cannot cancel build on build queue '" + overallBuildQueue.getName() +
                                 "': " + e.getMessage() );
            }            
            return true;
        }                
    }

    public boolean cancelAllBuilds() throws BuildManagerException
    {   
        synchronized( overallBuildQueues )
        {
            Set<Integer> keySet = overallBuildQueues.keySet();
            OverallBuildQueue overallBuildQueue = null;
            
            try
            {                
                for( Integer key : keySet )
                {
                    overallBuildQueue = overallBuildQueues.get( key );                
                    overallBuildQueue.cancelCurrentBuild();                
                }
            }
            catch ( TaskQueueException e )
            {
                log.error( "Cannot cancel build on build queue '" + overallBuildQueue.getName() + "'." );
                throw new BuildManagerException( "Cannot cancel build on build queue '" + overallBuildQueue.getName() +
                                 "': " + e.getMessage() );
            }            
            return true;
        }
    }

    public boolean cancelAllCheckouts() throws BuildManagerException
    {
        // TODO Auto-generated method stub
        return false;
    }

    /*public boolean cancelAllPrepareBuilds() throws BuildManagerException
    {
        // TODO Auto-generated method stub
        return false;
    }*/

    public boolean cancelBuild(int projectId) throws BuildManagerException
    {   
        try
        {
            OverallBuildQueue overallBuildQueue = getOverallBuildQueue( projectId, BUILD_QUEUE );
            if( overallBuildQueue != null )
            {
                overallBuildQueue.cancelBuildTask(  projectId );
            }
            else
            {
                throw new BuildManagerException( "Project not found in any of the build queues." );
            }
        }
        catch( TaskQueueException e )
        {
            throw new BuildManagerException( "Error occurred while cancelling build: " +
                 e.getMessage() );        
        }
        
        return true;
    }

    // TODO: should this be permitted? (might need to execute svn cleanup?)
    public boolean cancelCheckout(int projectId) throws BuildManagerException
    {
        try
        {
            OverallBuildQueue overallBuildQueue = getOverallBuildQueue( projectId, CHECKOUT_QUEUE );
            if( overallBuildQueue != null )
            {
                //overallBuildQueue.getCheckoutQueue()
            }
            else
            {
                throw new BuildManagerException( "Project not found in any of the checkout queues." );
            }
        }
        catch( TaskQueueException e )
        {
            throw new BuildManagerException( "Error occurred while cancelling build: " +
                 e.getMessage() );        
        }
        
        return true;
    }

    /*public boolean cancelPrepareBuild(int projectId) throws BuildManagerException
    {
        // TODO Auto-generated method stub
        return false;
    }*/

    public void checkoutProject( int projectId, String projectName, File workingDirectory, String scmUsername, String scmPassword, BuildDefinition defaultBuildDefinition ) throws BuildManagerException
    {   
        try
        {
            if( isInQueue( projectId, CHECKOUT_QUEUE ) )
            {
                log.warn( "Project already in checkout queue." );
                return;
            }
        }
        catch ( TaskQueueException e )
        {
            throw new BuildManagerException( "Error occurred while checking if the project is already in queue: " + e.getMessage() );
        }
        
        OverallBuildQueue overallBuildQueue =
            getOverallBuildQueue( projectId, CHECKOUT_QUEUE, defaultBuildDefinition.getSchedule().getBuildQueues() );
        
        CheckOutTask checkoutTask =
            new CheckOutTask( projectId, workingDirectory, projectName, scmUsername, scmPassword );
        try
        {
            overallBuildQueue.addToCheckoutQueue( checkoutTask );
        }
        catch ( TaskQueueException e )
        {
            throw new BuildManagerException( "Error occurred while adding project to checkout queue: " + e.getMessage() );
        }        
    }

    /*public void prepareBuildProject( int projectId, BuildDefinition buildDefinition, String projectName, int trigger, int scheduleId )
    {
        // TODO Auto-generated method stub

    }*/

    /*public void prepareBuildProjects( Collection<Map<Integer, Integer>> projectsBuildDefinitions, int trigger, int scheduleId )
    {
        // TODO Auto-generated method stub

    }*/

    public void removeProjectFromBuildQueue( int projectId ) throws BuildManagerException
    {
        try
        {
            OverallBuildQueue overallBuildQueue = getOverallBuildQueue( projectId, BUILD_QUEUE );
            if( overallBuildQueue != null )
            {
                overallBuildQueue.removeProjectFromBuildQueue( projectId );
            }
            else
            {
                throw new BuildManagerException( "Project not found in any of the build queues." );
            }
        }
        catch ( TaskQueueException e )
        {
            throw new BuildManagerException( "Error occurred while removing project from build queue: " +
                e.getMessage() );
        }
    }

    public void removeProjectFromCheckoutQueue( int projectId ) throws BuildManagerException
    {
        try
        {
            OverallBuildQueue overallBuildQueue = getOverallBuildQueue( projectId, CHECKOUT_QUEUE );
            if( overallBuildQueue != null )
            {
                overallBuildQueue.removeProjectFromCheckoutQueue( projectId );
            }
            else
            {
                throw new BuildManagerException( "Project not found in any of the checkout queues." );
            }
        }
        catch ( TaskQueueException e )
        {
            throw new BuildManagerException( "Error occurred while removing project from checkout queue: " +
                e.getMessage() );
        }
    }

    /*public void removeProjectFromPrepareBuildQueue( int projectId )
    {
        // TODO Auto-generated method stub

    }*/

    public void removeProjectsFromBuildQueue( int[] projectIds )
    {
        for( int i = 0; i < projectIds.length; i++ )
        {
            try
            {
                OverallBuildQueue overallBuildQueue = getOverallBuildQueue( projectIds[i], BUILD_QUEUE );
                if( overallBuildQueue != null )
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

    public void removeProjectsFromCheckoutQueue( int[] projectIds )
    {
        for( int i = 0; i < projectIds.length; i++ )
        {
            try
            {
                OverallBuildQueue overallBuildQueue = getOverallBuildQueue( projectIds[i], CHECKOUT_QUEUE );
                if( overallBuildQueue != null )
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

    public void removeProjectsFromPrepareBuildQueue( int[] projectIds )
    {
        // TODO Auto-generated method stub
    }
    
    public void addOverallBuildQueue( OverallBuildQueue overallBuildQueue )
    {
        synchronized( overallBuildQueues )
        {
            this.overallBuildQueues.put( overallBuildQueue.getId(), overallBuildQueue );
        }
    }
    
    public void removeOverallBuildQueue( int overallBuildQueueId ) throws BuildManagerException
    {
        List<BuildProjectTask> tasks = null;
        List<CheckOutTask> checkoutTasks = null;
        
        synchronized( overallBuildQueues )        
        {   
            OverallBuildQueue overallBuildQueue = overallBuildQueues.get( overallBuildQueueId );
            try
            {
                tasks = overallBuildQueue.getProjectsInBuildQueue();                
                checkoutTasks = overallBuildQueue.getCheckOutTasksInQueue();
                
                overallBuildQueue.getBuildQueue().removeAll( tasks );
                overallBuildQueue.getCheckoutQueue().removeAll( checkoutTasks );
                 
                overallBuildQueue = null;
            }
            catch ( TaskQueueException e )
            {
                throw new BuildManagerException(
                         "Cannot remove build queue. An error occurred while retrieving queued tasks." );
            }
                        
            this.overallBuildQueues.remove( overallBuildQueueId );
        }
        
        try
        {
            for( BuildProjectTask task : tasks )
            {
                BuildDefinition buildDefinition = buildDefinitionDao.getBuildDefinition( task.getBuildDefinitionId() );
                buildProject( task.getProjectId(), buildDefinition, task.getProjectName(), task.getTrigger() );                    
            }
         
            for( CheckOutTask task : checkoutTasks )
            {
                BuildDefinition buildDefinition = buildDefinitionDao.getDefaultBuildDefinition( task.getProjectId() );
                checkoutProject( task.getProjectId(), task.getProjectName(), task.getWorkingDirectory(),
                                      task.getScmUserName(), task.getScmPassword(), buildDefinition );                    
            }
        }
        catch ( ContinuumStoreException e )
        {
            throw new BuildManagerException(
                "Cannot remove build queue: " + e.getMessage() );
        }
    }
    
    public Map<Integer, OverallBuildQueue> getOverallBuildQueues()
    {
        return overallBuildQueues;
    }
    
    private boolean isInQueue( int projectId, int typeOfQueue )
        throws TaskQueueException
    {   
        synchronized( overallBuildQueues )
        {
            Set<Integer> keySet = overallBuildQueues.keySet();        
            for( Integer key : keySet )
            {
                OverallBuildQueue overallBuildQueue = overallBuildQueues.get( key );
                if( typeOfQueue == BUILD_QUEUE )
                {
                    if( overallBuildQueue.isInBuildQueue( projectId ) )
                    {
                        return true;
                    }
                }
                else if( typeOfQueue == CHECKOUT_QUEUE )
                {
                    if( overallBuildQueue.isInCheckoutQueue( projectId ) )
                    {
                        return true;
                    }
                }
                /*else if( typeOfQueue == PREPARE_BUILD_QUEUE )
                {
                    if( overallBuildQueue.isInPrepareBuildQueue( projectId ) )
                    {
                        return true;
                    }
                }*/
            }
            
            return false;
        }
    }
    
    private OverallBuildQueue getOverallBuildQueue( int projectId, int typeOfQueue )    
        throws TaskQueueException
    {
        synchronized( overallBuildQueues )
        {
            OverallBuildQueue whereQueued = null;
            Set<Integer> keySet = overallBuildQueues.keySet();
            
            for( Integer key : keySet )
            {
                OverallBuildQueue overallBuildQueue = overallBuildQueues.get( key );
                if( typeOfQueue == BUILD_QUEUE )
                {
                    if( overallBuildQueue.isInBuildQueue( projectId ) )
                    {
                        whereQueued = overallBuildQueue;
                        break;
                    }
                }
                else if( typeOfQueue == CHECKOUT_QUEUE )
                {
                    if( overallBuildQueue.isInCheckoutQueue( projectId ) )
                    {
                        whereQueued = overallBuildQueue;
                        break;
                    }
                }
                /*else if( typeOfQueue == PREPARE_BUILD_QUEUE )
                {
                    if( overallBuildQueue.isInPrepareBuildQueue( projectId ) )
                    {
                        whereQueued = overallBuildQueue;
                        break;
                    }
                }*/
            }
            
            return whereQueued;
        }
    }
    
    private OverallBuildQueue getOverallBuildQueue( int projectId, int typeOfQueue, List<BuildQueue> buildQueues )
        throws BuildManagerException
    {
        OverallBuildQueue whereToBeQueued = null; 
        synchronized( overallBuildQueues )
        {
            if( overallBuildQueues == null || overallBuildQueues.isEmpty() )
            {
                throw new BuildManagerException( "No build queues configured." );
            }
            
            int size = 0;
            int idx = 0;
            try
            {
                for( BuildQueue buildQueue : buildQueues )
                {
                    OverallBuildQueue overallBuildQueue = overallBuildQueues.get( buildQueue.getId() ); 
                    if( overallBuildQueue != null )
                    {
                        TaskQueue taskQueue = null;
                        if( typeOfQueue == BUILD_QUEUE )
                        {
                            taskQueue = overallBuildQueue.getBuildQueue();
                        }
                        else if( typeOfQueue == CHECKOUT_QUEUE )
                        {   
                            taskQueue = overallBuildQueue.getCheckoutQueue();
                        }
                        /*else if( typeOfQueue == PREPARE_BUILD_QUEUE )
                        {
                            taskQueue = overallBuildQueue.getPrepareBuildQueue();
                        }*/
                                                
                        if( idx == 0 )
                        {
                            size = taskQueue.getQueueSnapshot().size();
                            whereToBeQueued = overallBuildQueue;
                        }
                        
                        if( taskQueue.getQueueSnapshot().size() < size )
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
                }
            }
            catch ( TaskQueueException e )
            {
                throw new BuildManagerException( "Error occurred while retrieving task quueue: " + e.getMessage() );
            }
        }
        
        if( whereToBeQueued == null )
        {
            throw new BuildManagerException( "No build queue found." );
        }
        
        return whereToBeQueued;
    }

    public void setBuildDefinitionDao( BuildDefinitionDao buildDefinitionDao )
    {
        this.buildDefinitionDao = buildDefinitionDao;
    }
}
