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
import java.util.List;
import java.util.Map;

import org.apache.continuum.taskqueue.manager.TaskQueueManagerException;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildQueue;
import org.apache.maven.continuum.model.project.Project;
import org.codehaus.plexus.taskqueue.Task;

/**
 * BuildsManager. All builds whether forced or triggered will go through (or have to be added through) a builds manager.
 * 
 * @author <a href="mailto:oching@apache.org">Maria Odea Ching</a>
 */
public interface BuildsManager
{
    /**
     * Build projects using their corresponding build definitions. This method adds the group of projects to the
     * build queue of the overall queue with the least amount of tasks queued.
     * 
     * @param projects
     * @param projectsBuildDefinitionsMap
     * @param trigger
     * @throws BuildManagerException
     */
    void buildProjects( List<Project> projects, Map<Integer, BuildDefinition> projectsBuildDefinitionsMap, int trigger )
        throws BuildManagerException;

    /**
     * Build the project using the specified build definition. Adds the project to the build queue of the overall queue with the
     * least among of tasks queued. The overall queue is chosen from the pool of queues attached to the schedule of the 
     * build definition.
     * 
     * @param projectId
     * @param buildDefinition 
     * @param projectName
     * @param trigger
     * @throws BuildManagerException
     */
    void buildProject( int projectId, BuildDefinition buildDefinition, String projectName, int trigger )
        throws BuildManagerException;

    /**
     * Adds the projects in the prepare-build-queue.
     * 
     * @param projectsBuildDefinitionsMap
     * @param trigger
     * @param projectGroupId TODO
     * @param scmRootAddress TODO
     * @throws BuildManagerException
     */
    void prepareBuildProjects( Map<Integer, Integer> projectsBuildDefinitionsMap, int trigger, int projectGroupId, String scmRootAddress )
        throws BuildManagerException;

    /**
     * Adds the project to the checkout queue of the overall build queue with the least amount of tasks queued.
     * The overall queue is chosen from the pool of queues attached to the schedule of the build definition.
     * 
     * @param projectId
     * @param projectName
     * @param workingDirectory
     * @param scmUsername
     * @param scmPassword
     * @param defaultBuildDefinition
     * @throws BuildManagerException
     */
    void checkoutProject( int projectId, String projectName, File workingDirectory, String scmUsername,
                          String scmPassword, BuildDefinition defaultBuildDefinition )
        throws BuildManagerException;

    /**
     * Cancels the specified project's build.
     * 
     * @param projectId
     * @return
     * @throws BuildManagerException
     */
    boolean cancelBuild( int projectId )
        throws BuildManagerException;

    /**
     * Cancels all the builds in all the overall queues.
     * 
     * @return
     * @throws BuildManagerException
     */
    boolean cancelAllBuilds()
        throws BuildManagerException;

    /**
     * Cancels the current build on the specified overall queue.
     * 
     * @param buildQueueId
     * @return
     * @throws BuildManagerException
     */
    boolean cancelBuildInQueue( int buildQueueId )
        throws BuildManagerException;

    /**
     *  
     * @param projectId
     * @return
     * @throws BuildManagerException
     */
    boolean cancelCheckout( int projectId )
        throws BuildManagerException;

    /**
     * 
     * @return
     * @throws BuildManagerException
     */
    boolean cancelAllCheckouts()
        throws BuildManagerException;

    // public boolean cancelPrepareBuild(int projectId) throws BuildManagerException;

    boolean cancelAllPrepareBuilds() throws BuildManagerException;

    /**
     * Removes the project from the build queue.
     * 
     * @param projectId
     * @throws BuildManagerException
     */
    void removeProjectFromBuildQueue( int projectId )
        throws BuildManagerException;

    /**
     * Removes the project built using the specified build definition from the build queue.
     * 
     * @param projectId
     * @param buildDefinitionId
     * @param trigger
     * @param projectName
     * @throws BuildManagerException
     */
    void removeProjectFromBuildQueue( int projectId, int buildDefinitionId, int trigger, String projectName )
        throws BuildManagerException;

    // TODO: should we throw an exception when one of the projects cannot be removed?
    /**
     * Removes the specified projects from their build queues.
     * 
     * @param projectIds
     */
    void removeProjectsFromBuildQueue( int[] projectIds );

    /**
     * Removes a set of projects using the specified hashcodes from the build queues.
     * 
     * @param hascodes
     * @throws BuildManagerException
     */
    void removeProjectsFromBuildQueueWithHashcodes( int[] hascodes )
        throws BuildManagerException;

    /**
     * Removes the project from the checkout queue.
     * 
     * @param projectId
     * @throws BuildManagerException
     */
    void removeProjectFromCheckoutQueue( int projectId )
        throws BuildManagerException;

    /**
     * Removes the specified projects from their checkout queues.
     * 
     * @param projectIds
     */
    void removeProjectsFromCheckoutQueue( int[] projectIds );

    /**
     * Removes a set of projects using the specified hashcodes from the checkout queues.
     * 
     * @param hashcodes
     * @throws BuildManagerException
     */
    void removeProjectsFromCheckoutQueueWithHashcodes( int[] hashcodes )
        throws BuildManagerException;
    
    boolean removeProjectGroupFromPrepareBuildQueue( int projectGroupId, String scmRootAddress )
        throws BuildManagerException;
    
    /*void removeProjectFromPrepareBuildQueue( int projectId ) throws BuildManagerException;

    void removeProjectsFromPrepareBuildQueue( int[] projectIds ) throws BuildManagerException;*/

    /**
     * Add an overall build queue.
     * 
     * @param buildQueue
     * @throws BuildManagerException TODO
     */
    void addOverallBuildQueue( BuildQueue buildQueue ) throws BuildManagerException;

    /**
     * Remove an overall build queue.
     * 
     * @param overallBuildQueueId
     * @throws BuildManagerException
     */
    void removeOverallBuildQueue( int overallBuildQueueId )
        throws BuildManagerException;

    /**
     * Checks whether the project build is queued.
     * 
     * @param projectId
     * @return
     * @throws BuildManagerException
     */
    boolean isInAnyBuildQueue( int projectId )
        throws BuildManagerException;

    /**
     * Checks whether the project build using the specified build definition is queued.
     * 
     * @param projectId
     * @param buildDefinitionId
     * @return
     * @throws BuildManagerException
     */
    boolean isInAnyBuildQueue( int projectId, int buildDefinitionId )
        throws BuildManagerException;

    /**
     * Checks whether the project checkout is already queued.
     * 
     * @param projectId
     * @return
     * @throws BuildManagerException
     */
    boolean isInAnyCheckoutQueue( int projectId )
        throws BuildManagerException;
    
    /**
     * Checks if at least one of the projects is currently being checked out.
     * 
     * @param projectIds
     * @return
     * @throws BuildManagerException
     */
    boolean isAnyProjectCurrentlyBeingCheckedOut( int[] projectIds )
        throws BuildManagerException;

    /**
     * Checks whether the project is already in the prepare-build queue.
     * 
     * @param projectId
     * @return
     * @throws BuildManagerException
     */
    boolean isInPrepareBuildQueue( int projectId )
        throws BuildManagerException;

    /**
     * Checks whether the project is currently being built.
     * 
     * @param projectId
     * @return
     * @throws BuildManagerException
     */
    boolean isProjectInAnyCurrentBuild( int projectId )
        throws BuildManagerException;

    // needed in QueuesAction

    /**
     * Returns all the build tasks currently being executed.
     * 
     * @return
     * @throws BuildManagerException
     */
    Map<String, Task> getCurrentBuilds()
        throws BuildManagerException;

    /**
     * Returns all the checkout tasks currently being executed.
     * @return
     * @throws BuildManagerException
     */
    Map<String, Task> getCurrentCheckouts()
        throws BuildManagerException;

    /**
     * Returns all the overall build queues together with a list of the build tasks in it's build queue.
     * 
     * @return
     * @throws BuildManagerException
     */
    Map<String, List<Task>> getProjectsInBuildQueues()
        throws BuildManagerException;

    /**
     * Returns all the overall build queues together with a list of checkout tasks in it's checkout queue.
     * 
     * @return
     * @throws BuildManagerException
     */
    Map<String, List<Task>> getProjectsInCheckoutQueues()
        throws BuildManagerException;

    /**
     * Checks whether a build is in progress.
     * 
     * @return
     */
    boolean isBuildInProgress();
}
