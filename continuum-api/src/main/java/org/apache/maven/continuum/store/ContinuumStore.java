package org.apache.maven.continuum.store;

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

import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildDefinitionTemplate;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.model.system.Installation;
import org.apache.maven.continuum.model.system.SystemConfiguration;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 * @todo remove old stuff
 */
public interface ContinuumStore
{
    String ROLE = ContinuumStore.class.getName();

    void removeNotifier( ProjectNotifier notifier )
        throws ContinuumStoreException;

    ProjectNotifier storeNotifier( ProjectNotifier notifier )
        throws ContinuumStoreException;

    Map getDefaultBuildDefinitions();

    /**
     * returns the default build definition of the project, if the project
     * doesn't have on declared the default of the project group will be
     * returned <p/> this should be the most common usage of the default build
     * definition accessing methods
     *
     * @param projectId
     * @return
     * @throws ContinuumStoreException
     * @throws ContinuumObjectNotFoundException
     *
     */
    BuildDefinition getDefaultBuildDefinition( int projectId )
        throws ContinuumStoreException, ContinuumObjectNotFoundException;

    //  BuildDefinitionTemplate
    // ------------------------------------------------------

    List<BuildDefinitionTemplate> getAllBuildDefinitionTemplate()
        throws ContinuumStoreException;

    BuildDefinitionTemplate getBuildDefinitionTemplate( int id )
        throws ContinuumStoreException, ContinuumObjectNotFoundException;

    BuildDefinitionTemplate addBuildDefinitionTemplate( BuildDefinitionTemplate buildDefinitionTemplate )
        throws ContinuumStoreException;

    BuildDefinitionTemplate updateBuildDefinitionTemplate( BuildDefinitionTemplate buildDefinitionTemplate )
        throws ContinuumStoreException;

    void removeBuildDefinitionTemplate( BuildDefinitionTemplate buildDefinitionTemplate )
        throws ContinuumStoreException;

    public List<BuildDefinitionTemplate> getBuildDefinitionTemplatesWithType( String type )
        throws ContinuumStoreException;

    public List<BuildDefinitionTemplate> getContinuumBuildDefinitionTemplates()
        throws ContinuumStoreException;

    /**
     * @param type
     * @return BuildDefinitionTemplate null if not found
     * @throws ContinuumStoreException
     */
    BuildDefinitionTemplate getContinuumBuildDefinitionTemplateWithType( String type )
        throws ContinuumStoreException;

    /**
     * the list returned will contains only continuumDefaults {@link BuildDefinition}
     *
     * @return List<BuildDefinitionTemplate>
     * @throws ContinuumStoreException
     */
    List<BuildDefinitionTemplate> getContinuumDefaultdDefinitions()
        throws ContinuumStoreException;

    List<BuildResult> getAllBuildsForAProjectByDate( int projectId );

    Map getProjectIdsAndBuildDefinitionsIdsBySchedule( int scheduleId )
        throws ContinuumStoreException;

    Map getProjectGroupIdsAndBuildDefinitionsIdsBySchedule( int scheduleId )
        throws ContinuumStoreException;

    public Map getAggregatedProjectIdsAndBuildDefinitionIdsBySchedule( int scheduleId )
        throws ContinuumStoreException;

    BuildResult getBuildResult( int buildId )
        throws ContinuumObjectNotFoundException, ContinuumStoreException;

    void removeBuildResult( BuildResult buildResult );

    BuildResult getLatestBuildResultForProject( int projectId );

    BuildResult getLatestBuildResultForBuildDefinition( int projectId, int buildDefinitionId );

    List<BuildResult> getBuildResultsInSuccessForProject( int projectId, long fromDate );

    long getNbBuildResultsForProject( int projectId );

    List<BuildResult> getBuildResultsForProject( int projectId );

    List<BuildResult> getBuildResultsForProject( int projectId, long startIndex, long endIndex );

    List<BuildResult> getBuildResultsForProject( int projectId, long fromDate );

    Map<Integer, BuildResult> getLatestBuildResultsByProjectGroupId( int projectGroupId );

    Map<Integer, BuildResult> getLatestBuildResults();

    List<BuildResult> getBuildResultByBuildNumber( int projectId, int buildNumber );

    List<BuildResult> getBuildResultsByBuildDefinition( int projectId, int buildDefinitionId );

    List<BuildResult> getBuildResultsByBuildDefinition( int projectId, int buildDefinitionId, long startIndex,
                                                        long endIndex );

    Map<Integer, BuildResult> getBuildResultsInSuccess();

    Map<Integer, BuildResult> getBuildResultsInSuccessByProjectGroupId( int projectGroupId );

    void addBuildResult( Project project, BuildResult build )
        throws ContinuumStoreException, ContinuumObjectNotFoundException;

    void updateBuildResult( BuildResult build )
        throws ContinuumStoreException;

    SystemConfiguration addSystemConfiguration( SystemConfiguration systemConf );

    void updateSystemConfiguration( SystemConfiguration systemConf )
        throws ContinuumStoreException;

    SystemConfiguration getSystemConfiguration()
        throws ContinuumStoreException;

    void closeStore();

    void eraseDatabase();
}
