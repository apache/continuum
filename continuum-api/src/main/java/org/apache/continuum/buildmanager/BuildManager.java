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
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.continuum.taskqueue.OverallBuildQueue;
import org.apache.maven.continuum.model.project.BuildDefinition;

public interface BuildManager
{    
    // TODO: 
    // - move all creation of tasks inside the build manager!
    // - in the implementation, make sure that the number of overall build queue instances are
    //      read from the config file (continuum.xml)!
    
    OverallBuildQueue getOverallBuildQueueWhereProjectIsQueued( int project )
        throws BuildManagerException;
    
    List<OverallBuildQueue> getOverallBuildQueuesInUse();
    
    /* prepare-build-projects queue */
        
    void addProjectToPrepareBuildQueue( Map<Integer, Integer> projectsBuildDefinitionsMap, int trigger )
        throws BuildManagerException;

    /**
     * Add projects to prepare-build-projects queue. 
     * Used when build is triggered at the group level. 
     * 
     * @param projectsBuildDefinitions
     * @param trigger
     * @throws BuildManagerException
     */
    void addProjectsToPrepareBuildQueue( Collection<Map<Integer, Integer>> projectsBuildDefinitions, int trigger )
        throws BuildManagerException;

    /* checkout queue */
    
    void addProjectToCheckoutQueue( int id, File workingDirectory, String projectName, String projectScmUsername,
                                    String projectScmPassword )
        throws BuildManagerException;

    //void addProjectsToCheckoutQueue()
    //    throws BuildManagerException;

    void cancelProjectCheckout( int projectId )
        throws BuildManagerException;
    
    void cancelAllCheckouts()
        throws BuildManagerException;
    
    void removeProjectFromCheckoutQueue( int projectId )
        throws BuildManagerException;

    void removeProjectsFromCheckoutQueue( int[] projectIds )
        throws BuildManagerException;
    
    /* build queue */
    
    void addProjectToBuildQueue( int projectId, BuildDefinition buildDefinition, int trigger, String projectName, String buildDefLabel )
        throws BuildManagerException;

    //void addProjectsToBuildQueue()
    //    throws BuildManagerException;

    void cancelProjectBuild( int projectId )
        throws BuildManagerException;
    
    void cancelAllBuilds()
        throws BuildManagerException;
    
    void removeProjectFromBuildQueue( int projectId, int buildDefinitionId, int trigger, String projectName )
        throws BuildManagerException;

    void removeProjectsFromBuildQueue( int[] projectIds )
        throws BuildManagerException;
}
