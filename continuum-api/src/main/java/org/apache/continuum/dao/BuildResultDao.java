package org.apache.continuum.dao;

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

import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.store.ContinuumStoreException;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public interface BuildResultDao
{
    BuildResult getBuildResult( int buildId )
        throws ContinuumStoreException;

    void addBuildResult( Project project, BuildResult build )
        throws ContinuumStoreException;

    void updateBuildResult( BuildResult build )
        throws ContinuumStoreException;

    void removeBuildResult( BuildResult buildResult );

    BuildResult getLatestBuildResultForProject( int projectId );

    BuildResult getLatestBuildResultForBuildDefinition( int projectId, int buildDefinitionId );

    List<BuildResult> getBuildResultsInSuccessForProject( int projectId, long fromDate );

    long getNbBuildResultsForProject( int projectId );

    List<BuildResult> getBuildResultsForProject( int projectId );

    List<BuildResult> getBuildResultsForProject( int projectId, long startIndex, long endIndex );

    /**
     * @since 1.2
     * @param projectId
     * @param startIndex
     * @return the returned list will contains all BuildResult for this project after the startId
     */
    List<BuildResult> getBuildResultsForProjectFromId( int projectId, long startId )
        throws ContinuumStoreException;   
    
    List<BuildResult> getBuildResultsForProject( int projectId, long fromDate );

    Map<Integer, BuildResult> getLatestBuildResultsByProjectGroupId( int projectGroupId );

    Map<Integer, BuildResult> getLatestBuildResults();

    List<BuildResult> getBuildResultByBuildNumber( int projectId, int buildNumber );

    List<BuildResult> getBuildResultsByBuildDefinition( int projectId, int buildDefinitionId );

    List<BuildResult> getBuildResultsByBuildDefinition( int projectId, int buildDefinitionId, long startIndex,
                                                        long endIndex );

    Map<Integer, BuildResult> getBuildResultsInSuccess();

    Map<Integer, BuildResult> getBuildResultsInSuccessByProjectGroupId( int projectGroupId );

    List<BuildResult> getAllBuildsForAProjectByDate( int projectId );
}
