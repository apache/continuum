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

import org.apache.continuum.buildagent.NoBuildAgentException;
import org.apache.continuum.buildagent.NoBuildAgentInGroupException;
import org.apache.continuum.builder.distributed.manager.DistributedBuildManager;
import org.apache.continuum.buildmanager.BuildsManager;
import org.apache.continuum.model.project.ProjectGroupSummary;
import org.apache.continuum.model.project.ProjectScmRoot;
import org.apache.continuum.model.release.ContinuumReleaseResult;
import org.apache.continuum.purge.ContinuumPurgeManager;
import org.apache.continuum.purge.PurgeConfigurationService;
import org.apache.continuum.release.distributed.manager.DistributedReleaseManager;
import org.apache.continuum.repository.RepositoryService;
import org.apache.continuum.taskqueue.manager.TaskQueueManager;
import org.apache.continuum.utils.build.BuildTrigger;
import org.apache.maven.continuum.builddefinition.BuildDefinitionService;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.installation.InstallationService;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildQueue;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.model.project.Schedule;
import org.apache.maven.continuum.model.scm.ChangeSet;
import org.apache.maven.continuum.profile.ProfileService;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.apache.maven.continuum.release.ContinuumReleaseManager;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public interface Continuum
{
    String ROLE = Continuum.class.getName();

    // ----------------------------------------------------------------------
    // Project Groups
    // ----------------------------------------------------------------------

    public ProjectGroup getProjectGroup( int projectGroupId )
        throws ContinuumException;

    public List<ProjectGroup> getAllProjectGroupsWithBuildDetails();

    public List<ProjectGroup> getAllProjectGroups();

    public ProjectGroup getProjectGroupByProjectId( int projectId )
        throws ContinuumException;

    public Collection<Project> getProjectsInGroup( int projectGroupId )
        throws ContinuumException;

    public Collection<Project> getProjectsInGroupWithDependencies( int projectGroupId )
        throws ContinuumException;

    public void removeProjectGroup( int projectGroupId )
        throws ContinuumException;

    public void addProjectGroup( ProjectGroup projectGroup )
        throws ContinuumException;

    public ProjectGroup getProjectGroupWithProjects( int projectGroupId )
        throws ContinuumException;

    public ProjectGroup getProjectGroupWithBuildDetails( int projectGroupId )
        throws ContinuumException;

    public ProjectGroup getProjectGroupByGroupId( String groupId )
        throws ContinuumException;

    public ProjectGroup getProjectGroupByGroupIdWithBuildDetails( String groupId )
        throws ContinuumException;

    public List<ProjectGroup> getAllProjectGroupsWithRepository( int repositoryId );

    // ----------------------------------------------------------------------
    // Project
    // ----------------------------------------------------------------------

    void removeProject( int projectId )
        throws ContinuumException;


    /**
     * @param projectId
     * @throws ContinuumException
     * @deprecated
     */
    @Deprecated
    void checkoutProject( int projectId )
        throws ContinuumException;

    Project getProject( int projectId )
        throws ContinuumException;

    Project getProjectWithBuildDetails( int projectId )
        throws ContinuumException;

    Collection<Project> getProjects()
        throws ContinuumException;

    Collection<Project> getProjectsWithDependencies()
        throws ContinuumException;

    BuildResult getLatestBuildResultForProject( int projectId );

    Map<Integer, BuildResult> getLatestBuildResults( int projectGroupId );

    Map<Integer, BuildResult> getBuildResultsInSuccess( int projectGroupId );

    Map<Integer, ProjectGroupSummary> getProjectsSummaryByGroups();

    // ----------------------------------------------------------------------
    // Building
    // ----------------------------------------------------------------------

    /**
     * take a collection of projects and sort for order
     *
     * @param projects
     * @return
     */
    List<Project> getProjectsInBuildOrder( Collection<Project> projects );

    void buildProjects( String username )
        throws ContinuumException, NoBuildAgentException, NoBuildAgentInGroupException;

    void buildProjectsWithBuildDefinition( List<Project> projects, List<BuildDefinition> bds )
        throws ContinuumException, NoBuildAgentException, NoBuildAgentInGroupException;

    void buildProjectsWithBuildDefinition( List<Project> projects, int buildDefinitionId )
        throws ContinuumException, NoBuildAgentException, NoBuildAgentInGroupException;

    void buildProjects( BuildTrigger buildTrigger )
        throws ContinuumException, NoBuildAgentException, NoBuildAgentInGroupException;

    void buildProjects( Schedule schedule )
        throws ContinuumException;

    void buildProject( int projectId, String username )
        throws ContinuumException, NoBuildAgentException, NoBuildAgentInGroupException;

    void buildProject( int projectId, BuildTrigger buildTrigger )
        throws ContinuumException, NoBuildAgentException, NoBuildAgentInGroupException;

    void buildProjectWithBuildDefinition( int projectId, int buildDefinitionId, BuildTrigger buildTrigger )
        throws ContinuumException, NoBuildAgentException, NoBuildAgentInGroupException;

    void buildProject( int projectId, int buildDefinitionId, BuildTrigger buildTrigger )
        throws ContinuumException, NoBuildAgentException, NoBuildAgentInGroupException;

    public void buildProjectGroup( int projectGroupId, BuildTrigger buildTrigger )
        throws ContinuumException, NoBuildAgentException, NoBuildAgentInGroupException;

    public void buildProjectGroupWithBuildDefinition( int projectGroupId, int buildDefinitionId,
                                                      BuildTrigger buildTrigger )
        throws ContinuumException, NoBuildAgentException, NoBuildAgentInGroupException;

    // ----------------------------------------------------------------------
    // Build information
    // ----------------------------------------------------------------------

    BuildResult getBuildResult( int buildId )
        throws ContinuumException;

    BuildResult getBuildResultByBuildNumber( int projectId, int buildNumber )
        throws ContinuumException;

    String getBuildOutput( int projectId, int buildId )
        throws ContinuumException;

    long getNbBuildResultsForProject( int projectId );

    Collection<BuildResult> getBuildResultsForProject( int projectId )
        throws ContinuumException;

    List<ChangeSet> getChangesSinceLastSuccess( int projectId, int buildResultId )
        throws ContinuumException;

    void removeBuildResult( int buildId )
        throws ContinuumException;

    List<BuildResult> getBuildResultsInRange( int projectGroupId, Date fromDate, Date toDate, int state,
                                              String triggeredBy );

    // ----------------------------------------------------------------------
    // Projects
    // ----------------------------------------------------------------------

    /**
     * Add a project to the list of building projects (ant, shell,...)
     *
     * @param project        the project to add
     * @param executorId     the id of an {@link org.apache.maven.continuum.execution.ContinuumBuildExecutor}, eg. <code>ant</code> or <code>shell</code>
     * @param projectGroupId
     * @return id of the project
     * @throws ContinuumException
     */
    int addProject( Project project, String executorId, int projectGroupId )
        throws ContinuumException;

    /**
     * Add a project to the list of building projects (ant, shell,...)
     *
     * @param project        the project to add
     * @param executorId     the id of an {@link org.apache.maven.continuum.execution.ContinuumBuildExecutor}, eg. <code>ant</code> or <code>shell</code>
     * @param projectGroupId
     * @return id of the project
     * @throws ContinuumException
     */
    int addProject( Project project, String executorId, int projectGroupId, int buildDefintionTemplateId )
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
     * @param metadataUrl   url of the pom.xml
     * @param checkProtocol check if the protocol is allowed, use false if the pom is uploaded
     * @return a holder with the projects, project groups and errors occurred during the project adding
     * @throws ContinuumException
     */
    ContinuumProjectBuildingResult addMavenTwoProject( String metadataUrl, boolean checkProtocol )
        throws ContinuumException;

    /**
     * Add a Maven 2 project to the list of projects.
     *
     * @param metadataUrl    url of the pom.xml
     * @param projectGroupId id of the project group to use
     * @return a holder with the projects, project groups and errors occurred during the project adding
     * @throws ContinuumException
     */
    ContinuumProjectBuildingResult addMavenTwoProject( String metadataUrl, int projectGroupId )
        throws ContinuumException;

    /**
     * Add a Maven 2 project to the list of projects.
     *
     * @param metadataUrl    url of the pom.xml
     * @param projectGroupId id of the project group to use
     * @param checkProtocol  check if the protocol is allowed, use false if the pom is uploaded
     * @return a holder with the projects, project groups and errors occurred during the project adding
     * @throws ContinuumException
     */
    ContinuumProjectBuildingResult addMavenTwoProject( String metadataUrl, int projectGroupId, boolean checkProtocol )
        throws ContinuumException;

    /**
     * Add a Maven 2 project to the list of projects.
     *
     * @param metadataUrl         url of the pom.xml
     * @param projectGroupId      id of the project group to use
     * @param checkProtocol       check if the protocol is allowed, use false if the pom is uploaded
     * @param useCredentialsCache whether to use cached scm account credentials or not
     * @return a holder with the projects, project groups and errors occurred during the project adding
     * @throws ContinuumException
     */
    ContinuumProjectBuildingResult addMavenTwoProject( String metadataUrl, int projectGroupId, boolean checkProtocol,
                                                       boolean useCredentialsCache )
        throws ContinuumException;


    /**
     * Add a Maven 2 project to the list of projects.
     *
     * @param metadataUrl           url of the pom.xml
     * @param projectGroupId        id of the project group to use
     * @param checkProtocol         check if the protocol is allowed, use false if the pom is uploaded
     * @param useCredentialsCache   whether to use cached scm account credentials or not
     * @param loadRecursiveProjects if multi modules project record all projects (if false only root project added)
     * @return a holder with the projects, project groups and errors occurred during the project adding
     * @throws ContinuumException
     */
    public ContinuumProjectBuildingResult addMavenTwoProject( String metadataUrl, int projectGroupId,
                                                              boolean checkProtocol, boolean useCredentialsCache,
                                                              boolean loadRecursiveProjects )
        throws ContinuumException;

    /**
     * Add a Maven 2 project to the list of projects.
     *
     * @param metadataUrl               url of the pom.xml
     * @param projectGroupId            id of the project group to use
     * @param checkProtocol             check if the protocol is allowed, use false if the pom is uploaded
     * @param useCredentialsCache       whether to use cached scm account credentials or not
     * @param loadRecursiveProjects     if multi modules project record all projects (if false only root project added)
     * @param buildDefintionTemplateId  buildDefintionTemplateId
     * @param checkoutInSingleDirectory TODO
     * @return a holder with the projects, project groups and errors occurred during the project adding
     * @throws ContinuumException
     */
    public ContinuumProjectBuildingResult addMavenTwoProject( String metadataUrl, int projectGroupId,
                                                              boolean checkProtocol, boolean useCredentialsCache,
                                                              boolean loadRecursiveProjects,
                                                              int buildDefintionTemplateId,
                                                              boolean checkoutInSingleDirectory )
        throws ContinuumException;

    /**
     * Add a Maven 1 project to the list of projects.
     *
     * @param metadataUrl    url of the project.xml
     * @param projectGroupId id of the project group to use
     * @return a holder with the projects, project groups and errors occurred during the project adding
     * @throws ContinuumException
     */
    ContinuumProjectBuildingResult addMavenOneProject( String metadataUrl, int projectGroupId )
        throws ContinuumException;

    /**
     * Add a Maven 1 project to the list of projects.
     *
     * @param metadataUrl    url of the project.xml
     * @param projectGroupId id of the project group to use
     * @param checkProtocol  check if the protocol is allowed, use false if the pom is uploaded
     * @return a holder with the projects, project groups and errors occurred during the project adding
     * @throws ContinuumException
     */
    ContinuumProjectBuildingResult addMavenOneProject( String metadataUrl, int projectGroupId, boolean checkProtocol )
        throws ContinuumException;

    /**
     * Add a Maven 2 project to the list of projects.
     *
     * @param metadataUrl         url of the pom.xml
     * @param projectGroupId      id of the project group to use
     * @param checkProtocol       check if the protocol is allowed, use false if the pom is uploaded
     * @param useCredentialsCache whether to use cached scm account credentials or not
     * @return a holder with the projects, project groups and errors occurred during the project adding
     * @throws ContinuumException
     */
    ContinuumProjectBuildingResult addMavenOneProject( String metadataUrl, int projectGroupId, boolean checkProtocol,
                                                       boolean useCredentialsCache )
        throws ContinuumException;

    ContinuumProjectBuildingResult addMavenOneProject( String metadataUrl, int projectGroupId, boolean checkProtocol,
                                                       boolean useCredentialsCache, int buildDefintionTemplateId )
        throws ContinuumException;

    void updateProject( Project project )
        throws ContinuumException;

    void updateProjectGroup( ProjectGroup projectGroup )
        throws ContinuumException;

    Project getProjectWithCheckoutResult( int projectId )
        throws ContinuumException;

    Project getProjectWithAllDetails( int projectId )
        throws ContinuumException;

    Project getProjectWithBuilds( int projectId )
        throws ContinuumException;

    // ----------------------------------------------------------------------
    // Notification
    // ----------------------------------------------------------------------

    ProjectNotifier getNotifier( int projectId, int notifierId )
        throws ContinuumException;

    ProjectNotifier updateNotifier( int projectId, ProjectNotifier notifier )
        throws ContinuumException;

    ProjectNotifier addNotifier( int projectId, ProjectNotifier notifier )
        throws ContinuumException;

    void removeNotifier( int projectId, int notifierId )
        throws ContinuumException;

    ProjectNotifier getGroupNotifier( int projectGroupId, int notifierId )
        throws ContinuumException;

    ProjectNotifier updateGroupNotifier( int projectGroupId, ProjectNotifier notifier )
        throws ContinuumException;

    ProjectNotifier addGroupNotifier( int projectGroupId, ProjectNotifier notifier )
        throws ContinuumException;

    void removeGroupNotifier( int projectGroupId, int notifierId )
        throws ContinuumException;

    // ----------------------------------------------------------------------
    // Build Definition
    // ----------------------------------------------------------------------

    /**
     * @deprecated
     */
    @Deprecated
    List<BuildDefinition> getBuildDefinitions( int projectId )
        throws ContinuumException;

    /**
     * @deprecated
     */
    @Deprecated
    BuildDefinition getBuildDefinition( int projectId, int buildDefinitionId )
        throws ContinuumException;

    /**
     * @deprecated
     */
    @Deprecated
    void removeBuildDefinition( int projectId, int buildDefinitionId )
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
     * <p/>
     * 1) if project has default build definition, return that
     * 2) otherwise return default build definition for parent project group
     *
     * @param projectId
     * @return
     * @throws ContinuumException
     */
    BuildDefinition getDefaultBuildDefinition( int projectId )
        throws ContinuumException;

    public List<BuildDefinition> getDefaultBuildDefinitionsForProjectGroup( int projectGroupId )
        throws ContinuumException;

    BuildDefinition addBuildDefinitionToProject( int projectId, BuildDefinition buildDefinition )
        throws ContinuumException;

    BuildDefinition addBuildDefinitionToProjectGroup( int projectGroupId, BuildDefinition buildDefinition )
        throws ContinuumException;

    List<BuildDefinition> getBuildDefinitionsForProject( int projectId )
        throws ContinuumException;

    List<BuildDefinition> getBuildDefinitionsForProjectGroup( int projectGroupId )
        throws ContinuumException;

    void removeBuildDefinitionFromProject( int projectId, int buildDefinitionId )
        throws ContinuumException;

    void removeBuildDefinitionFromProjectGroup( int projectGroupId, int buildDefinitionId )
        throws ContinuumException;

    BuildDefinition updateBuildDefinitionForProject( int projectId, BuildDefinition buildDefinition )
        throws ContinuumException;

    BuildDefinition updateBuildDefinitionForProjectGroup( int projectGroupId, BuildDefinition buildDefinition )
        throws ContinuumException;

    // ----------------------------------------------------------------------
    // Schedule
    // ----------------------------------------------------------------------

    Schedule getScheduleByName( String scheduleName )
        throws ContinuumException;

    Schedule getSchedule( int id )
        throws ContinuumException;

    Collection<Schedule> getSchedules()
        throws ContinuumException;

    void addSchedule( Schedule schedule )
        throws ContinuumException;

    void updateSchedule( Schedule schedule )
        throws ContinuumException;

    void updateSchedule( int scheduleId, Map<String, String> configuration )
        throws ContinuumException;

    void removeSchedule( int scheduleId )
        throws ContinuumException;

    void activePurgeSchedule( Schedule schedule );

    void activeBuildDefinitionSchedule( Schedule schedule );

    // ----------------------------------------------------------------------
    // Working copy
    // ----------------------------------------------------------------------

    File getWorkingDirectory( int projectId )
        throws ContinuumException;

    String getFileContent( int projectId, String directory, String filename )
        throws ContinuumException;

    List<File> getFiles( int projectId, String currentDirectory )
        throws ContinuumException;

    // ----------------------------------------------------------------------
    // Configuration
    // ----------------------------------------------------------------------

    ConfigurationService getConfiguration();

    void reloadConfiguration()
        throws ContinuumException;

    // ----------------------------------------------------------------------
    // Continuum Release
    // ----------------------------------------------------------------------
    ContinuumReleaseManager getReleaseManager();

    // ----------------------------------------------------------------------
    // Installation
    // ----------------------------------------------------------------------    

    InstallationService getInstallationService();

    ProfileService getProfileService();

    BuildDefinitionService getBuildDefinitionService();

    // ----------------------------------------------------------------------
    // Continuum Purge
    // ----------------------------------------------------------------------
    ContinuumPurgeManager getPurgeManager();

    PurgeConfigurationService getPurgeConfigurationService();

    // ----------------------------------------------------------------------
    // Repository Service
    // ----------------------------------------------------------------------
    RepositoryService getRepositoryService();

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------
    List<ProjectScmRoot> getProjectScmRootByProjectGroup( int projectGroupId );

    ProjectScmRoot getProjectScmRoot( int projectScmRootId )
        throws ContinuumException;

    ProjectScmRoot getProjectScmRootByProject( int projectId )
        throws ContinuumException;

    ProjectScmRoot getProjectScmRootByProjectGroupAndScmRootAddress( int projectGroupId, String scmRootAddress )
        throws ContinuumException;

    // ----------------------------------------------------------------------
    // Task Queue Manager
    // ----------------------------------------------------------------------
    TaskQueueManager getTaskQueueManager();

    // ----------------------------------------------------------------------
    // Builds Manager
    // ----------------------------------------------------------------------
    BuildsManager getBuildsManager();

    // ----------------------------------------------------------------------
    // Build Queue
    // ----------------------------------------------------------------------

    BuildQueue addBuildQueue( BuildQueue buildQueue )
        throws ContinuumException;

    BuildQueue getBuildQueue( int buildQueueId )
        throws ContinuumException;

    BuildQueue getBuildQueueByName( String buildQueueName )
        throws ContinuumException;

    void removeBuildQueue( BuildQueue buildQueue )
        throws ContinuumException;

    BuildQueue storeBuildQueue( BuildQueue buildQueue )
        throws ContinuumException;

    List<BuildQueue> getAllBuildQueues()
        throws ContinuumException;

    public void startup()
        throws ContinuumException;

    ContinuumReleaseResult addContinuumReleaseResult( int projectId, String releaseId, String releaseType )
        throws ContinuumException;

    ContinuumReleaseResult addContinuumReleaseResult( ContinuumReleaseResult releaseResult )
        throws ContinuumException;

    void removeContinuumReleaseResult( int releaseResultId )
        throws ContinuumException;

    ContinuumReleaseResult getContinuumReleaseResult( int releaseResultId )
        throws ContinuumException;

    List<ContinuumReleaseResult> getContinuumReleaseResultsByProjectGroup( int projectGroupId );

    List<ContinuumReleaseResult> getAllContinuumReleaseResults();

    ContinuumReleaseResult getContinuumReleaseResult( int projectId, String releaseGoal, long startTime, long endTime )
        throws ContinuumException;

    String getReleaseOutput( int releaseResultId )
        throws ContinuumException;

    DistributedBuildManager getDistributedBuildManager();

    DistributedReleaseManager getDistributedReleaseManager();
}
