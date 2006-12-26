package org.apache.maven.continuum;

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

import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.execution.ContinuumBuildExecutor;
import org.apache.maven.continuum.key.GroupProjectKey;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.model.project.Schedule;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.apache.maven.continuum.release.ContinuumReleaseManager;
import org.codehaus.plexus.util.dag.CycleDetectedException;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public interface Continuum
{
    String ROLE = Continuum.class.getName();

    // ----------------------------------------------------------------------
    // Project Groups
    // ----------------------------------------------------------------------

    public static final String DEFAULT_PROJECT_GROUP_GROUP_ID = "default";

    public ProjectGroup getProjectGroup( GroupProjectKey groupProjectKey )
        throws ContinuumException;

    /**
     * Get all {@link ProjectGroup}s and their {@link Project}s
     * 
     * @return {@link Collection} &lt;{@link ProjectGroup}>
     */
    public Collection getAllProjectGroupsWithProjects();
    
    public Collection getAllProjectGroups();

    public ProjectGroup getProjectGroupByProjectId( GroupProjectKey groupProjectKey )
        throws ContinuumException;

    public Collection getProjectsInGroup( GroupProjectKey groupProjectKey )
        throws ContinuumException;

    public void removeProjectGroup( GroupProjectKey groupProjectKey )
        throws ContinuumException;

    public void addProjectGroup( ProjectGroup projectGroup )
        throws ContinuumException;
    
    public ProjectGroup getProjectGroupWithProjects( GroupProjectKey groupProjectKey )
        throws ContinuumException;

    /**
     * @deprecated not in use
     * @param groupId
     * @return
     * @throws ContinuumException
     */
    public ProjectGroup getProjectGroupByGroupId( String groupId )
        throws ContinuumException;

    /**
     * @deprecated not in use
     * @param groupId
     * @return
     * @throws ContinuumException
     */
    public ProjectGroup getProjectGroupByGroupIdWithBuildDetails( String groupId )
        throws ContinuumException;
    
    // ----------------------------------------------------------------------
    // Project
    // ----------------------------------------------------------------------

    void removeProject( GroupProjectKey groupProjectKey )
        throws ContinuumException;

    /**
     * @deprecate not in use
     * @param projectId
     * @throws ContinuumException
     */
    void checkoutProject( int projectId )
        throws ContinuumException;


    Project getProject( GroupProjectKey groupProjectKey )
        throws ContinuumException;

    Project getProjectWithBuildDetails( GroupProjectKey groupProjectKey )
        throws ContinuumException;

    /**
     * @deprecated validate usage and refactor or remove based on keys
     * @param start
     * @param end
     * @return
     */
    List getAllProjectsWithAllDetails( int start, int end );

    /**
     * @deprecated validate usage and refactor or removed based on keys
     * @param start
     * @param end
     * @return
     * @throws ContinuumException
     */
    Collection getAllProjects( int start, int end )
        throws ContinuumException;

    Collection getProjects()
        throws ContinuumException;

    Collection getProjectsWithDependencies()
        throws ContinuumException;

    BuildResult getLatestBuildResultForProject( GroupProjectKey groupProjectKey );

    Map getLatestBuildResults();

    Map getBuildResultsInSuccess();

    // ----------------------------------------------------------------------
    // Queues
    // ----------------------------------------------------------------------

    boolean isInBuildingQueue( GroupProjectKey groupProjectKey )
        throws ContinuumException;

    boolean isInBuildingQueue( GroupProjectKey groupProjectKey, int buildDefinitionId )
        throws ContinuumException;

    boolean isInCheckoutQueue( GroupProjectKey groupProjectKey )
        throws ContinuumException;

    // ----------------------------------------------------------------------
    // Building
    // ----------------------------------------------------------------------

    List getProjectsInBuildOrder()
        throws CycleDetectedException, ContinuumException;

    void buildProjects()
        throws ContinuumException;

    void buildProjects( int trigger )
        throws ContinuumException;

    void buildProjects( Schedule schedule )
        throws ContinuumException;

    void buildProject( GroupProjectKey groupProjectKey )
        throws ContinuumException;

    void buildProject( GroupProjectKey groupProjectKey, int trigger )
        throws ContinuumException;

    void buildProject( GroupProjectKey groupProjectKey, int buildDefinitionId, int trigger )
        throws ContinuumException;

    public void buildProjectGroup( GroupProjectKey groupProjectKey )
        throws ContinuumException;

    // ----------------------------------------------------------------------
    // Build information
    // ----------------------------------------------------------------------

    BuildResult getBuildResult( int buildId )
        throws ContinuumException;

    /**
     * @deprecated not in use
     * @param projectId
     * @param buildNumber
     * @return
     * @throws ContinuumException
     */
    BuildResult getBuildResultByBuildNumber( int projectId, int buildNumber )
        throws ContinuumException;


    String getBuildOutput( GroupProjectKey groupProjectKey, int buildId )
        throws ContinuumException;

    Collection getBuildResultsForProject( GroupProjectKey groupProjectKey )
        throws ContinuumException;

    List getChangesSinceLastSuccess( GroupProjectKey groupProjectKey, int buildResultId )
        throws ContinuumException;

    // ----------------------------------------------------------------------
    // Projects
    // ----------------------------------------------------------------------

    /**
     * Add a project to the list of building projects (ant, shell,...)
     *
     * @TODO fix for key based project addition
     *
     * @param project the project to add
     * @param executorId the id of an {@link ContinuumBuildExecutor}, eg. <code>ant</code> or <code>shell</code> 
     * @return id of the project
     * @throws ContinuumException
     */
    int addProject( Project project, String executorId )
        throws ContinuumException;

    /**
     * Add a Maven 2 project to the list of projects. 
     * 
     * @param metadataUrl url of the pom.xml
     * @return a holder with the projects, project groups and errors occurred during the project adding
     * @throws ContinuumException
     */
    ContinuumProjectBuildingResult addMavenTwoProject( String metadataUrl )
        throws ContinuumException;

    /**
     * Add a Maven 2 project to the list of projects. 
     * 
     * @param metadataUrl url of the pom.xml
     * @param checkProtocol check if the protocol is allowed, use false if the pom is uploaded
     * @return a holder with the projects, project groups and errors occurred during the project adding
     * @throws ContinuumException
     */
    ContinuumProjectBuildingResult addMavenTwoProject( String metadataUrl, boolean checkProtocol )
        throws ContinuumException;

    /**
     * Add a Maven 2 project to the list of projects.
     *
     * @param metadataUrl url of the pom.xml
     * @param projectGroupId id of the project group to use
     * @return a holder with the projects, project groups and errors occurred during the project adding
     * @throws ContinuumException
     */
    ContinuumProjectBuildingResult addMavenTwoProject( String metadataUrl, int projectGroupId )
        throws ContinuumException;

    /**
     * Add a Maven 2 project to the list of projects.
     *
     * @param metadataUrl url of the pom.xml
     * @param groupProjectKey
     *@param checkProtocol check if the protocol is allowed, use false if the pom is uploaded @return a holder with the projects, project groups and errors occurred during the project adding
     * @throws ContinuumException
     */
    ContinuumProjectBuildingResult addMavenTwoProject( String metadataUrl, GroupProjectKey groupProjectKey, boolean checkProtocol )
        throws ContinuumException;

    /**
     * Add a Maven 1 project to the list of projects. 
     * 
     * @param metadataUrl url of the project.xml
     * @return a holder with the projects, project groups and errors occurred during the project adding
     * @throws ContinuumException
     */
   ContinuumProjectBuildingResult addMavenOneProject( String metadataUrl )
        throws ContinuumException;

    /**
     * Add a Maven 1 project to the list of projects. 
     * 
     * @param metadataUrl url of the project.xml
     * @param checkProtocol check if the protocol is allowed, use false if the pom is uploaded
     * @return a holder with the projects, project groups and errors occurred during the project adding
     * @throws ContinuumException
     */
   ContinuumProjectBuildingResult addMavenOneProject( String metadataUrl, boolean checkProtocol )
        throws ContinuumException;

    /**
     * Add a Maven 1 project to the list of projects.
     *
     * @param metadataUrl url of the project.xml
     * @param groupProjectKey
     * @return a holder with the projects, project groups and errors occurred during the project adding
     * @throws ContinuumException
     */
    ContinuumProjectBuildingResult addMavenOneProject( String metadataUrl, GroupProjectKey groupProjectKey )
         throws ContinuumException;

    /**
     * Add a Maven 1 project to the list of projects.
     *
     * @param metadataUrl url of the project.xml
     * @param groupProjectKey
     *@param checkProtocol check if the protocol is allowed, use false if the pom is uploaded @return a holder with the projects, project groups and errors occurred during the project adding
     * @throws ContinuumException
     */
    ContinuumProjectBuildingResult addMavenOneProject( String metadataUrl, GroupProjectKey groupProjectKey, boolean checkProtocol )
         throws ContinuumException;

    void updateProject( Project project )
        throws ContinuumException;

    void updateProjectGroup( ProjectGroup projectGroup )
        throws ContinuumException;

    Project getProjectWithCheckoutResult( GroupProjectKey groupProjectKey )
        throws ContinuumException;

    Project getProjectWithAllDetails( GroupProjectKey groupProjectKey )
        throws ContinuumException;

    Project getProjectWithBuilds( GroupProjectKey groupProjectKey )
        throws ContinuumException;

    // ----------------------------------------------------------------------
    // Notification
    // ----------------------------------------------------------------------

    /**
     * Get a particular notifier.
     *
     * @param groupProjectKey
     * @param notifierId
     * @return
     * @throws ContinuumException
     */
    ProjectNotifier getNotifier( GroupProjectKey groupProjectKey, int notifierId )
        throws ContinuumException;

    /**
     * update the notifier and return the updated notifier
     *
     * @param groupProjectKey
     * @param notifier
     * @return
     * @throws ContinuumException
     */
    ProjectNotifier updateNotifier( GroupProjectKey groupProjectKey, ProjectNotifier notifier )
        throws ContinuumException;

    /**
     * Add a notifier.
     *
     * * if groupProjectKey has projectKey defined, add notifier to project
     * * otherwise add to project group for child project inheritence
     *
     * @param groupProjectKey
     * @param notifier
     * @return
     * @throws ContinuumException
     */
    ProjectNotifier addNotifier( GroupProjectKey groupProjectKey, ProjectNotifier notifier )
        throws ContinuumException;

    /**
     *
     * @param groupProjectKey
     * @param notifierId
     * @throws ContinuumException
     */
    void removeNotifier( GroupProjectKey groupProjectKey, int notifierId )
        throws ContinuumException;

    /*
    ProjectNotifier getGroupNotifier( GroupProjectKey groupProjectKey, int notifierId )
        throws ContinuumException;

    ProjectNotifier updateGroupNotifier( GroupProjectKey groupProjectKey, ProjectNotifier notifier )
        throws ContinuumException;

    ProjectNotifier addGroupNotifier( GroupProjectKey groupProjectKey, ProjectNotifier notifier )
        throws ContinuumException;

    void removeGroupNotifier( GroupProjectKey groupProjectKey, int notifierId )
        throws ContinuumException;
    */
    // ----------------------------------------------------------------------
    // Build Definition
    // ----------------------------------------------------------------------


    /**
     * Get the list of {@see BuildDefinitions} that are present based on the {@see GroupProjectKey}
     * being passed in.  If the groupProjectKey has a project key specified then return the list of
     * project level build definitions, otherwise return the project group lvl build definitions.
     *
     * @param groupProjectKey
     * @return
     * @throws ContinuumException
     */
    List getBuildDefinitions( GroupProjectKey groupProjectKey )
        throws ContinuumException;

    /**
     * Get a particular build definition from the group or project, context depending on contents of
     * the groupProjectKey.
     *
     * @param groupProjectKey
     * @param buildDefinitionId
     * @return
     * @throws ContinuumException
     */
    BuildDefinition getBuildDefinition( GroupProjectKey groupProjectKey, int buildDefinitionId )
        throws ContinuumException;

    /**
     *
     * @param groupProjectKey
     * @param buildDefinitionId
     * @throws ContinuumException
     */
    void removeBuildDefinition( GroupProjectKey groupProjectKey, int buildDefinitionId )
        throws ContinuumException;

    /**
     * returns the build definition from either the project or the project group it is a part of
     *
     * @param buildDefinitionId
     * @return
     */
    BuildDefinition getBuildDefinition( int buildDefinitionId )
        throws ContinuumException;

    /**
     * returns the default build definition for the project
     *
     * 1) if project has default build definition, return that
     * 2) otherwise return default build definition for parent project group
     *
     * @param groupProjectKey
     * @return default build definition for project or group
     * @throws ContinuumException
     */

    BuildDefinition getDefaultBuildDefinition( GroupProjectKey groupProjectKey )
        throws ContinuumException;
    /*

    BuildDefinition addBuildDefinitionToProject( GroupProjectKey groupProjectKey, BuildDefinition buildDefinition )
        throws ContinuumException;

    BuildDefinition addBuildDefinitionToProjectGroup( GroupProjectKey groupProjectKey, BuildDefinition buildDefinition )
        throws ContinuumException;    

    List getBuildDefinitionsForProject( GroupProjectKey groupProjectKey )
        throws ContinuumException;

    List getBuildDefinitionsForProjectGroup( GroupProjectKey groupProjectKey )
        throws ContinuumException;

    void removeBuildDefinitionFromProject( GroupProjectKey groupProjectKey, int buildDefinitionId )
        throws ContinuumException;

    void removeBuildDefinitionFromProjectGroup( int projectGroupId, int buildDefinitionId )
        throws ContinuumException;

    BuildDefinition updateBuildDefinitionForProject( int projectId, BuildDefinition buildDefinition )
        throws ContinuumException;

    BuildDefinition updateBuildDefinitionForProjectGroup( int projectGroupId, BuildDefinition buildDefinition )
        throws ContinuumException;
    */

    // ----------------------------------------------------------------------
    // Schedule
    // ----------------------------------------------------------------------

    Schedule getSchedule( int id )
        throws ContinuumException;

    Collection getSchedules()
        throws ContinuumException;

    void addSchedule( Schedule schedule )
        throws ContinuumException;

    void updateSchedule( Schedule schedule )
        throws ContinuumException;

    void updateSchedule( int scheduleId, Map configuration )
        throws ContinuumException;

    void removeSchedule( int scheduleId )
        throws ContinuumException;

    // ----------------------------------------------------------------------
    // Working copy
    // ----------------------------------------------------------------------

    File getWorkingDirectory( GroupProjectKey groupProjectKey )
        throws ContinuumException;

    String getFileContent( GroupProjectKey groupProjectKey, String directory, String filename )
        throws ContinuumException;

    List getFiles( GroupProjectKey groupProjectKey, String currentDirectory )
        throws ContinuumException;

    // ----------------------------------------------------------------------
    // Configuration
    // ----------------------------------------------------------------------

    ConfigurationService getConfiguration();

    void updateConfiguration( Map parameters )
        throws ContinuumException;

    void reloadConfiguration()
        throws ContinuumException;


    // ----------------------------------------------------------------------
    // Continuum Release
    // ----------------------------------------------------------------------
    ContinuumReleaseManager getReleaseManager();
}
