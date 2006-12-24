package org.apache.maven.continuum.store;

/*
 * Copyright 2004-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Profile;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.model.project.Schedule;
import org.apache.maven.continuum.model.system.Installation;
import org.apache.maven.continuum.model.system.SystemConfiguration;
import org.apache.maven.continuum.key.GroupProjectKey;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 * @todo remove old stuff
 * @todo review some of the method names
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
     * Returns the default build definition of the project, if the project 
     * doesn't have a build definition declared then the default for the 
     * project group will be returned.
     * <p>
     * This should be the most common usage of the default build definition 
     * accessing methods
     *
     * @param groupProjectKey Composite key that identifies the target project
     *                          under a group.
     * @return the build definitions for the entity specified by the composite 
     *              {@link GroupProjectKey}.
     * @throws ContinuumStoreException
     * @throws ContinuumObjectNotFoundException
     *
     */
    BuildDefinition getDefaultBuildDefinition( GroupProjectKey groupProjectKey )
        throws ContinuumStoreException, ContinuumObjectNotFoundException;

    /**
     * Returns the default build definition for the project without consulting 
     * the project group
     *
     * @param groupProjectKey Composite key that identifies the target project
     *                          under a group.
     * @return the build definitions for the {@link Project} specified by the 
     *          composite {@link GroupProjectKey}.
     * @throws ContinuumStoreException
     * @throws ContinuumObjectNotFoundException
     *
     */
    BuildDefinition getDefaultBuildDefinitionForProject( GroupProjectKey groupProjectKey )
        throws ContinuumStoreException, ContinuumObjectNotFoundException;

    /**
     * Returns the default build definition for the {@link ProjectGroup} and 
     * there should always be one declared.
     *
     * @param groupProjectKey Composite key that identifies the target project 
     *                          group.
     * @return {@link BuildDefinition} for the specified {@link ProjectGroup}.
     * @throws ContinuumStoreException
     * @throws ContinuumObjectNotFoundException
     *
     */
    BuildDefinition getDefaultBuildDefinitionForProjectGroup( GroupProjectKey groupProjectKey )
        throws ContinuumStoreException, ContinuumObjectNotFoundException;

    BuildDefinition getBuildDefinition( int buildDefinitionId )
        throws ContinuumStoreException, ContinuumObjectNotFoundException;

    void removeBuildDefinition( BuildDefinition buildDefinition )
        throws ContinuumStoreException;

    BuildDefinition storeBuildDefinition( BuildDefinition buildDefinition )
        throws ContinuumStoreException;

    ProjectGroup addProjectGroup( ProjectGroup group );

    /**
     * Obtains and returns a {@link ProjectGroup} instance that matches the 
     * Project Group specified by the passed in {@link GroupProjectKey}.
     *   
     * @param groupProjectKey Composite key that identifies the target project 
     *                          group to be looked up.
     * @return {@link ProjectGroup} instance that matches the group key 
     *          specified by {@link GroupProjectKey}. 
     * @throws ContinuumStoreException
     * @throws ContinuumObjectNotFoundException
     */
    ProjectGroup getProjectGroup( GroupProjectKey groupProjectKey )
        throws ContinuumStoreException, ContinuumObjectNotFoundException;

    /**
     * Obtains and returns a {@link ProjectGroup} instance that matches the 
     * Project Group for the {@link Project} specified by the passed in 
     * {@link GroupProjectKey#getProjectKey()}.
     *  
     * @param groupProjectKey Composite key that identifies the target project 
     *                          to be looked up.
     * @return {@link ProjectGroup} instance that matches the specified group 
     *          key.
     * @throws ContinuumObjectNotFoundException
     */
    public ProjectGroup getProjectGroupByProjectId( GroupProjectKey groupProjectKey )
        throws ContinuumObjectNotFoundException;

    void updateProjectGroup( ProjectGroup group )
        throws ContinuumStoreException;

    Collection getAllProjectGroupsWithProjects();

    Collection getAllProjectGroups();

    List getAllProjectsByName();

    List getAllProjectsByNameWithDependencies();

    /**
     * Obtains and returns a list of all {@link Project}s and their 
     * dependencies for the group key specified by the passed in 
     * {@link GroupProjectKey}.
     *  
     * @param groupProjectKey Composite key that identifies the target project
     *                          group.
     * @return list of all {@link Project}s and dependencies that 
     *          match the specified group key.
     */
    public List getProjectsWithDependenciesByGroupId( GroupProjectKey groupProjectKey );

    List getAllProjectsByNameWithBuildDetails();

    List getAllSchedulesByName();

    Schedule addSchedule( Schedule schedule );

    Schedule getScheduleByName( String name )
        throws ContinuumStoreException;

    Schedule storeSchedule( Schedule schedule )
        throws ContinuumStoreException;

    List getAllProfilesByName();

    Profile addProfile( Profile profile );

    Installation addInstallation( Installation installation );

    List getAllInstallations();

    /**
     * Returns a list of all the builds for a {@link Project}, ordered by date.
     * 
     * @param groupProjectKey Composite key that identifies the target project 
     *                          to be looked up.
     * @return List of all builds for the specified project, ordered by date. 
     */
    List getAllBuildsForAProjectByDate( GroupProjectKey groupProjectKey );

    /**
     * Returns a {@link Project} instance for the specified project key.
     *  
     * @param groupProjectKey Composite key that identifies the target project 
     *                          to be looked up.
     * @return {@link Project} instance for the specified project key.
     * @throws ContinuumStoreException
     * @throws ContinuumObjectNotFoundException
     */
    Project getProject( GroupProjectKey groupProjectKey )
        throws ContinuumStoreException, ContinuumObjectNotFoundException;

    /**
     * TODO: Candidate for refactoring - check with Jesse?
     * 
     * @param groupId
     * @param artifactId
     * @param version
     * @return
     * @throws ContinuumStoreException
     */
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

    void updateProfile( Profile profile )
        throws ContinuumStoreException;

    void updateSchedule( Schedule schedule )
        throws ContinuumStoreException;

    /**
     * Returns a {@link Project} instance for the specified 
     * {@link GroupProjectKey}.
     *  
     * @param groupProjectKey Composite key that identifies the target project 
     *                          to be looked up.
     * @return {@link Project} instance that matches the passed in 
     *          {@link GroupProjectKey.
     * @throws ContinuumStoreException
     * @throws ContinuumObjectNotFoundException
     */
    Project getProjectWithBuilds( GroupProjectKey groupProjectKey )
        throws ContinuumStoreException, ContinuumObjectNotFoundException;

    void removeProfile( Profile profile );

    void removeSchedule( Schedule schedule );

    /**
     * TODO: document!
     * 
     * @param groupProjectkey Composite key that identifies the target project 
     *                          to be looked up.
     * @return {@link Project} instance that matches the specified 
     *         {@link GroupProjectKey}.
     * @throws ContinuumObjectNotFoundException
     * @throws ContinuumStoreException
     */
    Project getProjectWithCheckoutResult( GroupProjectKey groupProjectkey )
        throws ContinuumObjectNotFoundException, ContinuumStoreException;

    BuildResult getBuildResult( int buildId )
        throws ContinuumObjectNotFoundException, ContinuumStoreException;

    void removeProject( Project project );

    void removeProjectGroup( ProjectGroup projectGroup );

    /**
     * Return the {@link ProjectGroup} instance that matches the group key
     * specified by the passed in {@link GroupProjectKey}.
     * 
     * @param groupProjectKey Composite key that identifies the target project 
     *                          group to be looked up.
     * @return {@link ProjectGroup} instance that matches the group key passed 
     *          in {@link GroupProjectKey}
     * @throws ContinuumObjectNotFoundException
     * @throws ContinuumStoreException
     */
    ProjectGroup getProjectGroupWithBuildDetails( GroupProjectKey groupProjectKey )
        throws ContinuumObjectNotFoundException, ContinuumStoreException;

    /**
     * Returns a list of all the {@link Project}s within a 
     * {@link ProjectGroup}. The {@link ProjectGroup} is determined from the
     * passed in {@link GroupProjectKey}.
     * 
     * @param groupProjectkey Composite key that identifies the target project 
     *                          group to be looked up.
     * @return List of all the {@link Project} instances for the specified 
     *          {@link ProjectGroup}.
     * @throws ContinuumObjectNotFoundException
     * @throws ContinuumStoreException
     */
    List getProjectsInGroup( GroupProjectKey groupProjectkey )
        throws ContinuumObjectNotFoundException, ContinuumStoreException;

    /**
     * Returns a {@link ProjectGroup} instance along with its member 
     * {@link Project}s for the specified group key. The group key is 
     * determined from the passed in {@link GroupProjectKey}.
     *  
     * @param groupProjectKey Composite key that identifies the target project 
     *                          group to be looked up.
     * @return {@link ProjectGroup} instance along with the member 
     *          {@link Project}s for the specified group key. 
     * @throws ContinuumObjectNotFoundException
     * @throws ContinuumStoreException
     */
    ProjectGroup getProjectGroupWithProjects( GroupProjectKey groupProjectKey )
        throws ContinuumObjectNotFoundException, ContinuumStoreException;

    List getAllProjectGroupsWithBuildDetails();

    List getAllProjectsWithAllDetails();

    /**
     * Returns a {@link Project} instance along with all its details for the
     * specified composite project key. The project key can be determined from 
     * the passed in {@link GroupProjectKey}. 
     * 
     * @param groupProjectKey Composite key that identifies the target project 
     *                          group to be looked up.
     * @return {@link Project} instance with all its details for the specified
     *          project key.
     * @throws ContinuumObjectNotFoundException
     * @throws ContinuumStoreException
     */
    Project getProjectWithAllDetails( GroupProjectKey groupProjectKey )
        throws ContinuumObjectNotFoundException, ContinuumStoreException;

    Schedule getSchedule( int scheduleId )
        throws ContinuumObjectNotFoundException, ContinuumStoreException;

    Profile getProfile( int profileId )
        throws ContinuumObjectNotFoundException, ContinuumStoreException;

    /**
     * Returns a {@link ProjectGroup} for the specified group key. The group 
     * key can be determined from the passed in {@link GroupProjectKey}. 
     * 
     * @param groupProjectKey Composite key that identifies the target project 
     *                          group to be looked up.
     * @return {@link ProjectGroup} instance that matches the passed group key.
     * @throws ContinuumStoreException
     * @throws ContinuumObjectNotFoundException
     */
    ProjectGroup getProjectGroupByGroupId( GroupProjectKey groupProjectKey )
        throws ContinuumStoreException, ContinuumObjectNotFoundException;

    /**
     * Returns a {@link ProjectGroup} instance along with all the build details
     * for that group, for the specified group key. The group key can be 
     * determined  from the {@link GroupProjectKey}.
     *  
     * @param groupProjectKey Composite key that identifies the target project 
     *                          group to be looked up.
     * @return {@link ProjectGroup} instance along with all the build details
     *          for the specified group key sourced from the passed in 
     *          {@link GroupProjectKey}.
     * @throws ContinuumStoreException
     * @throws ContinuumObjectNotFoundException
     */
    ProjectGroup getProjectGroupByGroupIdWithBuildDetails( GroupProjectKey groupProjectKey )
        throws ContinuumStoreException, ContinuumObjectNotFoundException;

    /**
     * Returns a {@link ProjectGroup} instance along with its member 
     * {@link Project} instances for the specified group key. The group can be
     * determined from the passed in {@link GroupProjectKey}.
     * 
     * @param groupProjectKey Composite key that identifies the target project 
     *                          group to be looked up.
     * @return {@link ProjectGroup} along with its member {@link Project}s that
     *          matches the specified group key.
     * @throws ContinuumStoreException
     * @throws ContinuumObjectNotFoundException
     */
    ProjectGroup getProjectGroupByGroupIdWithProjects( GroupProjectKey groupProjectKey )
        throws ContinuumStoreException, ContinuumObjectNotFoundException;

    /**
     * Returns the {@link BuildResult} for a {@link Project} determined by the 
     * project key. The project key can be determined from the passed in 
     * {@link GroupProjectKey}.
     *  
     * @param groupProjectKey Composite key that identifies the target project 
     *                          to be looked up.
     * @return {@link BuildResult} for the {@link Project} that matches the 
     *          project key source from passed in {@link GroupProjectKey}.
     */
    BuildResult getLatestBuildResultForProject( GroupProjectKey groupProjectKey );

    /**
     * Returns a list of all the {@link BuildResult} instances for a specified 
     * {@link Project} where the result was a <code>SUCCESS</code>.
     * <p>
     * The target project's key is determined from the passed in 
     * {@link GroupProjectKey}.
     * 
     * @param groupProjectKey Composite key that identifies the target project 
     *                          to be looked up.
     * @param fromDate start date to filter out any older build results.
     * @return list of build results for the specified project that are newer
     *          than the time specified.
     */
    List getBuildResultsInSuccessForProject( GroupProjectKey groupProjectKey, long fromDate );

    /**
     * Returns a List of all {@link BuildResult} instances for a specified 
     * {@link Project} irrespective of the result type.
     * <p>
     * The target project's key is determined from the passed in 
     * {@link GroupProjectKey}. 
     * 
     * @param groupProjectKey Composite key that identifies the target project 
     *                          to be looked up.
     * @param fromDate start date to filter out any older build results.
     * @return list of build results for the specified project that are newer
     *          than the time specified and have <code>SUCCESS</code> state.
     */
    List getBuildResultsForProject( GroupProjectKey groupProjectKey, long fromDate );

    Map getLatestBuildResults();

    /**
     * Returns a list of all the {@link BuildResult} instances for a specified 
     * {@link Project} and given a build number. (TODO: Is this the Build 
     * definition Id?).
     * <p>
     * The project key is determined from the passed in 
     * {@link GroupProjectKey}.
     * 
     * @param groupProjectKey Composite key that identifies the target project 
     *                          to be looked up.
     * @param buildNumber TODO: Document!
     * @return List of all the {@link BuildResult} instances for the specified 
     *          project and a build number.
     */
    List getBuildResultByBuildNumber( GroupProjectKey groupProjectKey, int buildNumber );

    Map getBuildResultsInSuccess();

    void addBuildResult( Project project, BuildResult build )
        throws ContinuumStoreException, ContinuumObjectNotFoundException;

    void updateBuildResult( BuildResult build )
        throws ContinuumStoreException;

    /**
     * Returns a {@link Project} along with build details, that matches the 
     * specified project key. The project key is determined from the passed in 
     * {@link GroupProjectKey}. 
     * 
     * @param groupProjectKey Composite key that identifies the target project 
     *                          to be looked up.
     * @return {@link Project} instance along with its build details, that 
     *          matches the specified {@link Project} key.
     * @throws ContinuumObjectNotFoundException
     * @throws ContinuumStoreException
     */
    Project getProjectWithBuildDetails( GroupProjectKey groupProjectKey )
        throws ContinuumObjectNotFoundException, ContinuumStoreException;

    SystemConfiguration addSystemConfiguration( SystemConfiguration systemConf );

    void updateSystemConfiguration( SystemConfiguration systemConf )
        throws ContinuumStoreException;

    SystemConfiguration getSystemConfiguration()
        throws ContinuumStoreException;

    void closeStore();

    Collection getAllProjectGroupsWithTheLot();

    void eraseDatabase();
}
