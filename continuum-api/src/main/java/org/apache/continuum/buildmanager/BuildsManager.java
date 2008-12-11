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

import org.apache.continuum.taskqueue.OverallBuildQueue;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Project;

/**
 * 
 * @author <a href="mailto:oching@apache.org">Maria Odea Ching</a>
 *
 */
public interface BuildsManager
{       
    // NOTE: deng parallel builds 
    // I think we can move out the prepare build queue from the build manager?
    //      only builds and checkouts should be executed in parallel? :D
    
    public void buildProjects( List<Project> projects, Map<Integer, BuildDefinition> projectsBuildDefinitionsMap, int trigger ) throws BuildManagerException;
    
    public void buildProject( int projectId, BuildDefinition buildDefinition, String projectName, int trigger ) throws BuildManagerException;
    
    //public void prepareBuildProjects( Collection<Map<Integer, Integer>> projectsBuildDefinitions, int trigger, int scheduleId );
    
    //public void prepareBuildProject( int projectId, BuildDefinition buildDefinition, String projectName, int trigger, int scheduleId );
    
    // project checkout doesn't require dependency checking
    public void checkoutProject( int projectId, String projectName, File workingDirectory, String scmUsername, String scmPassword, BuildDefinition defaultBuildDefinition ) throws BuildManagerException;
        
    public boolean cancelBuild( int projectId ) throws BuildManagerException;
    
    public boolean cancelAllBuilds() throws BuildManagerException;
    
    public boolean cancelBuildInQueue( int buildQueueId ) throws BuildManagerException;
    
    public boolean cancelCheckout(int projectId) throws BuildManagerException;
    
    public boolean cancelAllCheckouts() throws BuildManagerException;
    
    //public boolean cancelPrepareBuild(int projectId) throws BuildManagerException;
    
    //public boolean cancelAllPrepareBuilds() throws BuildManagerException;
    
    public void removeProjectFromBuildQueue( int projectId ) throws BuildManagerException;
    
    public void removeProjectsFromBuildQueue( int[] projectIds );
    
    public void removeProjectFromCheckoutQueue( int projectId ) throws BuildManagerException;
    
    public void removeProjectsFromCheckoutQueue( int[] projectIds );
    
    //public void removeProjectFromPrepareBuildQueue( int projectId );
    
   // public void removeProjectsFromPrepareBuildQueue( int[] projectIds );
    
    public void addOverallBuildQueue( OverallBuildQueue overallBuildQueue );
    
    public void removeOverallBuildQueue( int overallBuildQueueId ) throws BuildManagerException;

}
