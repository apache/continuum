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

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
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

    BuildResult getLatestBuildResultForProjectWithDetails( int projectId );

    BuildResult getLatestBuildResultForBuildDefinition( int projectId, int buildDefinitionId );

    BuildResult getPreviousBuildResultForBuildDefinition( int projectId, int buildDefinitionId, int buildResultId );

    BuildResult getLatestBuildResultInSuccess( int projectId );

    BuildResult getPreviousBuildResultInSuccess( int projectId, int buildResultId );

    /**
     * Marks results in the BUILDING status with a start time before the specified cutoff as CANCELLED.
     *
     * @param ageCutoff the time in milliseconds, before which a building result is considered orphaned
     * @return the set of ids considered orphaned, and consequently marked CANCELLED
     * @throws ContinuumStoreException
     */
    Set<Integer> resolveOrphanedInProgressResults( long ageCutoff )
        throws ContinuumStoreException;

    long getNbBuildResultsForProject( int projectId );

    /**
     * Returns the list of build results between the fromdate and the buildResult defined by its toBuildResultId
     *
     * @param projectId    The project id
     * @param fromResultId the from date
     * @param toResultId   the build result id
     * @return the list of build results
     */
    List<BuildResult> getBuildResultsForProjectWithDetails( int projectId, int fromResultId, int toResultId );

    /**
     * Returns the number of build results in success since fromDate
     *
     * @param projectId The project id
     * @param fromDate  The from date
     * @return the number of build results
     */
    long getNbBuildResultsInSuccessForProject( int projectId, long fromDate );

    /**
     * Looks up a range of build results for a project ordered from most recent to earliest.
     *
     * @param projectId   id of project to fetch results for
     * @param startIndex  zero-based index indicating start of result interval
     * @param endIndex    zero-based index indicating boundary of result interval
     * @param fullDetails whether to use a fetch plan that includes full build result details
     * @return a list of results in the interval [start, end)
     */
    List<BuildResult> getBuildResultsForProject( int projectId, long startIndex, long endIndex, boolean fullDetails );

    /**
     * @param projectId
     * @param startId
     * @return the returned list will contains all BuildResult for this project after the startId
     * @since 1.2
     */
    List<BuildResult> getBuildResultsForProjectFromId( int projectId, long startId )
        throws ContinuumStoreException;

    Map<Integer, BuildResult> getLatestBuildResultsByProjectGroupId( int projectGroupId );

    Map<Integer, BuildResult> getBuildResultsInSuccessByProjectGroupId( int projectGroupId );

    List<BuildResult> getBuildResultsByBuildDefinition( int projectId, int buildDefinitionId );

    List<BuildResult> getBuildResultsByBuildDefinition( int projectId, int buildDefinitionId, long startIndex,
                                                        long endIndex );

    List<BuildResult> getAllBuildsForAProjectByDate( int projectId );

    List<BuildResult> getBuildResultsInRange( Date fromDate, Date toDate, int state, String triggeredBy,
                                              Collection<Integer> projectGroupIds, int offset, int length );
}
