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
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.model.project.Schedule;
import org.apache.maven.continuum.model.system.Installation;
import org.apache.maven.continuum.model.system.Profile;
import org.apache.maven.continuum.model.system.SystemConfiguration;

import java.util.Collection;
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

    /**
     * returns the default build definition for the project without consulting
     * the project group
     *
     * @param projectId
     * @return
     * @throws ContinuumStoreException
     * @throws ContinuumObjectNotFoundException
     *
     */
    BuildDefinition getDefaultBuildDefinitionForProject( int projectId )
        throws ContinuumStoreException, ContinuumObjectNotFoundException;

    /**
     * returns the default build definitions for the project group and there
     * should always be at least one declared
     *
     * @param projectGroupId
     * @return
     * @throws ContinuumStoreException
     * @throws ContinuumObjectNotFoundException
     *
     */
    List<BuildDefinition> getDefaultBuildDefinitionsForProjectGroup( int projectGroupId )
        throws ContinuumStoreException, ContinuumObjectNotFoundException;

    BuildDefinition getBuildDefinition( int buildDefinitionId )
        throws ContinuumStoreException, ContinuumObjectNotFoundException;

    void removeBuildDefinition( BuildDefinition buildDefinition )
        throws ContinuumStoreException;

    BuildDefinition storeBuildDefinition( BuildDefinition buildDefinition )
        throws ContinuumStoreException;

    BuildDefinition addBuildDefinition( BuildDefinition buildDefinition )
        throws ContinuumStoreException;

    List<BuildDefinition> getAllBuildDefinitions()
        throws ContinuumStoreException;

    List<BuildDefinition> getAllTemplates()
        throws ContinuumStoreException;

    // ------------------------------------------------------
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

    // ------------------------------------------------------
    //  Project Group
    // ------------------------------------------------------

    ProjectGroup addProjectGroup( ProjectGroup group );

    ProjectGroup getProjectGroup( int projectGroupId )
        throws ContinuumStoreException, ContinuumObjectNotFoundException;

    public ProjectGroup getProjectGroupByProjectId( int projectId )
        throws ContinuumObjectNotFoundException;

    void updateProjectGroup( ProjectGroup group )
        throws ContinuumStoreException;

    Collection<ProjectGroup> getAllProjectGroupsWithProjects();

    Collection<ProjectGroup> getAllProjectGroups();

    List<Project> getAllProjectsByName();

    List<Project> getAllProjectsByNameWithDependencies();

    public List<Project> getProjectsWithDependenciesByGroupId( int projectGroupId );

    List<Project> getAllProjectsByNameWithBuildDetails();

    List<Schedule> getAllSchedulesByName();

    Schedule addSchedule( Schedule schedule );

    Schedule getScheduleByName( String name )
        throws ContinuumStoreException;

    Schedule storeSchedule( Schedule schedule )
        throws ContinuumStoreException;

    // ----------------------------------------------------------------
    // Profile
    // ----------------------------------------------------------------    
    List<Profile> getAllProfilesByName();

    Profile addProfile( Profile profile );

    Installation addInstallation( Installation installation )
        throws ContinuumStoreException;

    Profile getProfile( int profileId )
        throws ContinuumObjectNotFoundException, ContinuumStoreException;

    void updateProfile( Profile profile )
        throws ContinuumStoreException;

    void removeProfile( Profile profile );

    // ----------------------------------------------------------------
    // Installation
    // ----------------------------------------------------------------  

    List<Installation> getAllInstallations()
        throws ContinuumStoreException;

    void removeInstallation( Installation installation )
        throws ContinuumStoreException, ContinuumObjectNotFoundException;

    void updateInstallation( Installation installation )
        throws ContinuumStoreException, ContinuumObjectNotFoundException;

    Installation getInstallation( int installationId )
        throws ContinuumStoreException, ContinuumObjectNotFoundException;

    List<BuildResult> getAllBuildsForAProjectByDate( int projectId );

    Project getProject( int projectId )
        throws ContinuumStoreException, ContinuumObjectNotFoundException;

    Project getProject( String groupId, String artifactId, String version )
        throws ContinuumStoreException;

    Project getProjectByName( String name )
        throws ContinuumStoreException;

    Map getProjectIdsAndBuildDefinitionsIdsBySchedule( int scheduleId )
        throws ContinuumStoreException;

    Map getProjectGroupIdsAndBuildDefinitionsIdsBySchedule( int scheduleId )
        throws ContinuumStoreException;

    public Map getAggregatedProjectIdsAndBuildDefinitionIdsBySchedule( int scheduleId )
        throws ContinuumStoreException;

    void updateProject( Project project )
        throws ContinuumStoreException;

    void updateSchedule( Schedule schedule )
        throws ContinuumStoreException;

    Project getProjectWithBuilds( int projectId )
        throws ContinuumStoreException, ContinuumObjectNotFoundException;

    void removeSchedule( Schedule schedule );

    Project getProjectWithCheckoutResult( int projectId )
        throws ContinuumObjectNotFoundException, ContinuumStoreException;

    BuildResult getBuildResult( int buildId )
        throws ContinuumObjectNotFoundException, ContinuumStoreException;

    void removeBuildResult( BuildResult buildResult );

    void removeProject( Project project );

    void removeProjectGroup( ProjectGroup projectGroup );

    ProjectGroup getProjectGroupWithBuildDetailsByProjectGroupId( int projectGroupId )
        throws ContinuumObjectNotFoundException, ContinuumStoreException;

    List<Project> getProjectsInGroup( int projectGroupId )
        throws ContinuumObjectNotFoundException, ContinuumStoreException;

    List<Project> getProjectsInGroupWithDependencies( int projectGroupId )
        throws ContinuumObjectNotFoundException, ContinuumStoreException;

    ProjectGroup getProjectGroupWithProjects( int projectGroupId )
        throws ContinuumObjectNotFoundException, ContinuumStoreException;

    List<ProjectGroup> getAllProjectGroupsWithBuildDetails();

    List<Project> getAllProjectsWithAllDetails();

    Project getProjectWithAllDetails( int projectId )
        throws ContinuumObjectNotFoundException, ContinuumStoreException;

    Schedule getSchedule( int scheduleId )
        throws ContinuumObjectNotFoundException, ContinuumStoreException;

    ProjectGroup getProjectGroupByGroupId( String groupId )
        throws ContinuumStoreException, ContinuumObjectNotFoundException;

    ProjectGroup getProjectGroupByGroupIdWithBuildDetails( String groupId )
        throws ContinuumStoreException, ContinuumObjectNotFoundException;

    ProjectGroup getProjectGroupByGroupIdWithProjects( String groupId )
        throws ContinuumStoreException, ContinuumObjectNotFoundException;

    BuildResult getLatestBuildResultForProject( int projectId );

    BuildResult getLatestBuildResultForBuildDefinition( int projectId, int buildDefinitionId );

    List<BuildResult> getBuildResultsInSuccessForProject( int projectId, long fromDate );

    List<BuildResult> getBuildResultsForProject( int projectId, long fromDate );

    Map getLatestBuildResultsByProjectGroupId( int projectGroupId );

    Map getLatestBuildResults();

    List<BuildResult> getBuildResultByBuildNumber( int projectId, int buildNumber );

    Map getBuildResultsInSuccess();

    Map getBuildResultsInSuccessByProjectGroupId( int projectGroupId );

    void addBuildResult( Project project, BuildResult build )
        throws ContinuumStoreException, ContinuumObjectNotFoundException;

    void updateBuildResult( BuildResult build )
        throws ContinuumStoreException;

    Project getProjectWithBuildDetails( int projectId )
        throws ContinuumObjectNotFoundException, ContinuumStoreException;

    SystemConfiguration addSystemConfiguration( SystemConfiguration systemConf );

    void updateSystemConfiguration( SystemConfiguration systemConf )
        throws ContinuumStoreException;

    SystemConfiguration getSystemConfiguration()
        throws ContinuumStoreException;

    void closeStore();

    Collection<ProjectGroup> getAllProjectGroupsWithTheLot();

    void eraseDatabase();
}
