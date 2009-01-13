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

import java.util.List;

import org.codehaus.plexus.taskqueue.Task;
import org.codehaus.plexus.taskqueue.TaskQueue;
import org.codehaus.plexus.taskqueue.TaskQueueException;
import org.codehaus.plexus.taskqueue.execution.TaskQueueExecutor;

/**
 * 
 * @author <a href="mailto:oching@apache.org">Maria Odea Ching</a>
 */
public interface OverallBuildQueue
{       
    /**
     * Returns the id of the "overall" build queue
     * 
     * @return
     */
    int getId();
    
    void setId( int id );
    
    /**
     * Returns the name of the "overall" build queue
     * @return
     */
    String getName();
    
    void setName( String name );
    
    /* Checkout Queue */
    
    /**
     * Returns the checkout queue.
     * 
     * @return
     */
    TaskQueue getCheckoutQueue();

    /**
     * Add checkout task to checkout queue.
     * 
     * @param checkoutTask
     * @throws TaskQueueException TODO
     */
    void addToCheckoutQueue( Task checkoutTask )
        throws TaskQueueException;
    
   /**
    * Add checkout tasks to checkout queue.
    * 
    * @param checkoutTasks
    * @throws TaskQueueException TODO
    */
    void addToCheckoutQueue( List<Task> checkoutTasks )
        throws TaskQueueException;

    /**
     * Get all checkout tasks in checkout queue.
     * 
     * @return
     * @throws TaskQueueException TODO
     */
    List /* CheckOutTask */getProjectsInCheckoutQueue()
        throws TaskQueueException;

    /**
     * Check if the project is in the checkout queue.
     * 
     * @param projectId
     * @return
     * @throws TaskQueueException TODO
     */
    boolean isInCheckoutQueue( int projectId )
        throws TaskQueueException;
    
    /**
     * Cancel checkout of project.
     * 
     * @param projectId
     * @throws TaskQueueException
     */
    void cancelCheckoutTask( int projectId )
        throws TaskQueueException;
    
    /**
     * Cancel current checkout.
     * @return TODO
     */
    boolean cancelCurrentCheckout();

    /**
     * Remove project from checkout queue.
     * 
     * @param projectId
     * @return
     * @throws TaskQueueException TODO
     */
    boolean removeProjectFromCheckoutQueue( int projectId )
        throws TaskQueueException;

    /**
     * Remove the specified projects in the checkout queue.
     * 
     * @param projectId
     * @return
     * @throws TaskQueueException TODO
     */
    boolean removeProjectsFromCheckoutQueue( int[] projectId )
        throws TaskQueueException;

    /**
     * 
     * @param hashCodes
     * @throws TaskQueueException TODO
     */
    void removeTasksFromCheckoutQueueWithHashCodes( int[] hashCodes )
        throws TaskQueueException;

    /* Build Queue */

    /**
     * Returns the build queue.
     * 
     * @return
     */
    TaskQueue getBuildQueue();

    /**
     * Add the build task to the build queue.
     * 
     * @param buildTask
     * @throws Exception
     */
    void addToBuildQueue( Task buildTask )
        throws TaskQueueException;
    
    /**
     * Add the build tasks to the build queue.
     * 
     * @param buildTasks
     * @throws TaskQueueException TODO
     */
    void addToBuildQueue( List<Task> buildTasks )
        throws TaskQueueException;

    /**
     * Returns the build tasks in the build queue.
     * 
     * @return
     * @throws TaskQueueException TODO
     */
    List<Task> getProjectsInBuildQueue()
        throws TaskQueueException;

    /**
     * Checks if the specified project is in the build queue.
     * 
     * @param projectId
     * @return
     * @throws TaskQueueException TODO
     */
    boolean isInBuildQueue( int projectId )
        throws TaskQueueException;

    /**
     * Checks if the specified project with the specified build definition is in the build queue.
     * 
     * @param projectId
     * @param buildDefinitionId
     * @return
     * @throws TaskQueueException TODO
     */
    boolean isInBuildQueue( int projectId, int buildDefinitionId )
        throws TaskQueueException;

    /**
     * Cancel the build task of the corresponding project.
     * 
     * @param projectId
     */
    void cancelBuildTask( int projectId );

    /**
     * Cancel the current build.
     * 
     * @return
     */
    boolean cancelCurrentBuild();

    /**
     * Remove the project matching the specified id, name, build definition and trigger from the build queue.
     * 
     * @param projectId
     * @param buildDefinitionId
     * @param trigger
     * @param projectName
     * @return
     * @throws TaskQueueException TODO
     */
    boolean removeProjectFromBuildQueue( int projectId, int buildDefinitionId, int trigger, String projectName )
        throws TaskQueueException;

    /**
     * Remove the specified project from the build queue.
     * 
     * @param projectId
     * @return
     * @throws TaskQueueException TODO
     */
    boolean removeProjectFromBuildQueue( int projectId )
        throws TaskQueueException;

    /**
     * Remove the specified projects from the build queue.
     * 
     * @param projectIds
     * @return
     * @throws TaskQueueException TODO
     */
    boolean removeProjectsFromBuildQueue( int[] projectIds )
        throws TaskQueueException;

    /**
     * Remove the projects matching the specified hashcodes from the build queue.
     * 
     * @param hashCodes
     * @throws TaskQueueException TODO
     */
    void removeProjectsFromBuildQueueWithHashCodes( int[] hashCodes )
        throws TaskQueueException;
    
    /**
     * Returns the build task queue executor used.
     * 
     * @return
     */
    TaskQueueExecutor getBuildTaskQueueExecutor();
    
    /**
     * Returns the checkout task queue executor used.
     * 
     * @return
     */
    TaskQueueExecutor getCheckoutTaskQueueExecutor();
}
