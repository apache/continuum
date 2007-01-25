package org.apache.maven.continuum;

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

import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.model.project.Schedule;
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

    public ProjectGroup getProjectGroup( long projectGroupId )
        throws ContinuumException;

    /**
     * Get all {@link ProjectGroup}s and their {@link Project}s
     *
     * @return {@link Collection} &lt;{@link ProjectGroup}>
     */
    public Collection getAllProjectGroupsWithProjects();

    public Collection getAllProjectGroups();

    public ProjectGroup getProjectGroupByProjectId( long projectId )
        throws ContinuumException;

    public Collection getProjectsInGroup( long projectGroupId )
        throws ContinuumException;

    public void removeProjectGroup( long projectGroupId )
        throws ContinuumException;

    public void addProjectGroup( ProjectGroup projectGroup )
        throws ContinuumException;

    public ProjectGroup getProjectGroupWithProjects( long projectGroupId )
        throws ContinuumException;

    public ProjectGroup getProjectGroupByGroupId( String groupId )
        throws ContinuumException;

    public ProjectGroup getProjectGroupByGroupIdWithBuildDetails( String groupId )
        throws ContinuumException;

    // ----------------------------------------------------------------------
    // Project
    // ----------------------------------------------------------------------

    void removeProject( long projectId )
        throws ContinuumException;

    void checkoutProject( long projectId )
        throws ContinuumException;

    Project getProject( long projectId )
        throws ContinuumException;

    Project getProjectWithBuildDetails( long projectId )
        throws ContinuumException;

    List getAllProjectsWithAllDetails( int start, int end );

    Collection getAllProjects( int start, int end )
        throws ContinuumException;

    Collection getProjects()
        throws ContinuumException;

    Collection getProjectsWithDependencies()
        throws ContinuumException;

    BuildResult getLatestBuildResultForProject( long projectId );

    Map getLatestBuildResults();

    Map getBuildResultsInSuccess();

    // ----------------------------------------------------------------------
    // Queues
    // ----------------------------------------------------------------------

    boolean isInBuildingQueue( long projectId )
        throws ContinuumException;

    boolean isInBuildingQueue( long projectId, long buildDefinitionId )
        throws ContinuumException;

    boolean isInCheckoutQueue( long projectId )
        throws ContinuumException;

    // ----------------------------------------------------------------------
    // Building
    // ----------------------------------------------------------------------

    List getProjectsInBuildOrder()
        throws CycleDetectedException, ContinuumException;

    void buildProjects()
        throws ContinuumException;

    void buildProjectsWithBuildDefinition( long buildDefinitionId )
        throws ContinuumException;

    void buildProjects( int trigger )
        throws ContinuumException;

    void buildProjects( int trigger, long buildDefinitionId )
        throws ContinuumException;

    void buildProjects( Schedule schedule )
        throws ContinuumException;

    void buildProject( long projectId )
        throws ContinuumException;

    void buildProject( long projectId, int trigger )
        throws ContinuumException;

    void buildProjectWithBuildDefinition( long projectId, long buildDefinitionId )
        throws ContinuumException;

    void buildProject( long projectId, long buildDefinitionId, int trigger )
        throws ContinuumException;

    public void buildProjectGroup( long projectGroupId )
        throws ContinuumException;

    // ----------------------------------------------------------------------
    // Build information
    // ----------------------------------------------------------------------

    BuildResult getBuildResult( long buildId )
        throws ContinuumException;

    BuildResult getBuildResultByBuildNumber( long projectId, long buildNumber )
        throws ContinuumException;

    String getBuildOutput( long projectId, long buildId )
        throws ContinuumException;

    Collection getBuildResultsForProject( long projectId )
        throws ContinuumException;

    List getChangesSinceLastSuccess( long projectId, long buildResultId )
        throws ContinuumException;

    // ----------------------------------------------------------------------
    // Projects
    // ----------------------------------------------------------------------

    /**
     * Add a project to the list of building projects (ant, shell,...)
     *
     * @param project the project to add
     * @param executorId the id of an {@link org.apache.maven.continuum.execution.ContinuumBuildExecutor}, eg. <code>ant</code> or <code>shell</code>
     * @return id of the project
     * @throws ContinuumException
     */
    long addProject( Project project, String executorId )
        throws ContinuumException;

    /**
     * Add a project to the list of building projects (ant, shell,...)
     *
     * @param project the project to add
     * @param executorId the id of an {@link org.apache.maven.continuum.execution.ContinuumBuildExecutor}, eg. <code>ant</code> or <code>shell</code>
     * @param projectGroupId
     * @return id of the project
     * @throws ContinuumException
     */
    long addProject( Project project, String executorId, long projectGroupId )
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
    ContinuumProjectBuildingResult addMavenTwoProject( String metadataUrl, long projectGroupId )
        throws ContinuumException;

    /**
     * Add a Maven 2 project to the list of projects.
     *
     * @param metadataUrl url of the pom.xml
     * @param projectGroupId id of the project group to use
     * @param checkProtocol check if the protocol is allowed, use false if the pom is uploaded
     * @return a holder with the projects, project groups and errors occurred during the project adding
     * @throws ContinuumException
     */
    ContinuumProjectBuildingResult addMavenTwoProject( String metadataUrl, long projectGroupId, boolean checkProtocol )
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
     * @param projectGroupId id of the project group to use
     * @return a holder with the projects, project groups and errors occurred during the project adding
     * @throws ContinuumException
     */
    ContinuumProjectBuildingResult addMavenOneProject( String metadataUrl, long projectGroupId )
         throws ContinuumException;

    /**
     * Add a Maven 1 project to the list of projects.
     *
     * @param metadataUrl url of the project.xml
     * @param projectGroupId id of the project group to use
     * @param checkProtocol check if the protocol is allowed, use false if the pom is uploaded
     * @return a holder with the projects, project groups and errors occurred during the project adding
     * @throws ContinuumException
     */
    ContinuumProjectBuildingResult addMavenOneProject( String metadataUrl, long projectGroupId, boolean checkProtocol )
         throws ContinuumException;

    void updateProject( Project project )
        throws ContinuumException;

    void updateProjectGroup( ProjectGroup projectGroup )
        throws ContinuumException;

    Project getProjectWithCheckoutResult( long projectId )
        throws ContinuumException;

    Project getProjectWithAllDetails( long projectId )
        throws ContinuumException;

    Project getProjectWithBuilds( long projectId )
        throws ContinuumException;

    // ----------------------------------------------------------------------
    // Notification
    // ----------------------------------------------------------------------

    ProjectNotifier getNotifier( long projectId, long notifierId )
        throws ContinuumException;

    ProjectNotifier updateNotifier( long projectId, ProjectNotifier notifier )
        throws ContinuumException;

    ProjectNotifier addNotifier( long projectId, ProjectNotifier notifier )
        throws ContinuumException;

    void removeNotifier( long projectId, long notifierId )
        throws ContinuumException;

    ProjectNotifier getGroupNotifier( long projectGroupId, long notifierId )
        throws ContinuumException;

    ProjectNotifier updateGroupNotifier( long projectGroupId, ProjectNotifier notifier )
        throws ContinuumException;

    ProjectNotifier addGroupNotifier( long projectGroupId, ProjectNotifier notifier )
        throws ContinuumException;

    void removeGroupNotifier( long projectGroupId, long notifierId )
        throws ContinuumException;

    // ----------------------------------------------------------------------
    // Build Definition
    // ----------------------------------------------------------------------

    /**
     * @deprecated
     */
    List getBuildDefinitions( long projectId )
        throws ContinuumException;

    /**
     * @deprecated
     */
    BuildDefinition getBuildDefinition( long projectId, long buildDefinitionId )
        throws ContinuumException;

    /**
     * @deprecated
     */
    void removeBuildDefinition( long projectId, long buildDefinitionId )
        throws ContinuumException;

    /**
     * returns the build definition from either the project or the project group it is a part of
     *
     * @param buildDefinitionId
     * @return
     */
    BuildDefinition getBuildDefinition( long buildDefinitionId )
        throws ContinuumException;

    /**
     * returns the default build definition for the project
     *
     * 1) if project has default build definition, return that
     * 2) otherwise return default build definition for parent project group
     *
     * @param projectId
     * @return
     * @throws ContinuumException
     */
    BuildDefinition getDefaultBuildDefinition( long projectId )
        throws ContinuumException;

    BuildDefinition addBuildDefinitionToProject( long projectId, BuildDefinition buildDefinition )
        throws ContinuumException;

    BuildDefinition addBuildDefinitionToProjectGroup( long projectGroupId, BuildDefinition buildDefinition )
        throws ContinuumException;

    List getBuildDefinitionsForProject( long projectId )
        throws ContinuumException;

    List getBuildDefinitionsForProjectGroup( long projectGroupId )
        throws ContinuumException;

    void removeBuildDefinitionFromProject( long projectId, long buildDefinitionId )
        throws ContinuumException;

    void removeBuildDefinitionFromProjectGroup( long projectGroupId, long buildDefinitionId )
        throws ContinuumException;

    BuildDefinition updateBuildDefinitionForProject( long projectId, BuildDefinition buildDefinition )
        throws ContinuumException;

    BuildDefinition updateBuildDefinitionForProjectGroup( long projectGroupId, BuildDefinition buildDefinition )
        throws ContinuumException;


    // ----------------------------------------------------------------------
    // Schedule
    // ----------------------------------------------------------------------

    Schedule getSchedule( long id )
        throws ContinuumException;

    Collection getSchedules()
        throws ContinuumException;

    void addSchedule( Schedule schedule )
        throws ContinuumException;

    void updateSchedule( Schedule schedule )
        throws ContinuumException;

    void updateSchedule( long scheduleId, Map configuration )
        throws ContinuumException;

    void removeSchedule( long scheduleId )
        throws ContinuumException;

    // ----------------------------------------------------------------------
    // Working copy
    // ----------------------------------------------------------------------

    File getWorkingDirectory( long projectId )
        throws ContinuumException;

    String getFileContent( long projectId, String directory, String filename )
        throws ContinuumException;

    List getFiles( int projectId, String currentDirectory )
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
