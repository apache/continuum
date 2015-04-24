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
import org.apache.continuum.buildmanager.BuildManagerException;
import org.apache.continuum.buildmanager.BuildsManager;
import org.apache.continuum.buildqueue.BuildQueueService;
import org.apache.continuum.buildqueue.BuildQueueServiceException;
import org.apache.continuum.configuration.BuildAgentConfigurationException;
import org.apache.continuum.configuration.ContinuumConfigurationException;
import org.apache.continuum.dao.BuildDefinitionDao;
import org.apache.continuum.dao.BuildResultDao;
import org.apache.continuum.dao.ContinuumReleaseResultDao;
import org.apache.continuum.dao.DaoUtils;
import org.apache.continuum.dao.NotifierDao;
import org.apache.continuum.dao.ProjectDao;
import org.apache.continuum.dao.ProjectGroupDao;
import org.apache.continuum.dao.ProjectScmRootDao;
import org.apache.continuum.dao.ScheduleDao;
import org.apache.continuum.model.project.ProjectGroupSummary;
import org.apache.continuum.model.project.ProjectScmRoot;
import org.apache.continuum.model.release.ContinuumReleaseResult;
import org.apache.continuum.purge.ContinuumPurgeManager;
import org.apache.continuum.purge.PurgeConfigurationService;
import org.apache.continuum.release.config.ContinuumReleaseDescriptor;
import org.apache.continuum.release.distributed.manager.DistributedReleaseManager;
import org.apache.continuum.release.model.PreparedRelease;
import org.apache.continuum.repository.RepositoryService;
import org.apache.continuum.taskqueue.manager.TaskQueueManager;
import org.apache.continuum.taskqueue.manager.TaskQueueManagerException;
import org.apache.continuum.utils.ProjectSorter;
import org.apache.continuum.utils.build.BuildTrigger;
import org.apache.continuum.utils.file.FileSystemManager;
import org.apache.maven.continuum.build.BuildException;
import org.apache.maven.continuum.build.settings.SchedulesActivationException;
import org.apache.maven.continuum.build.settings.SchedulesActivator;
import org.apache.maven.continuum.builddefinition.BuildDefinitionService;
import org.apache.maven.continuum.builddefinition.BuildDefinitionServiceException;
import org.apache.maven.continuum.configuration.ConfigurationException;
import org.apache.maven.continuum.configuration.ConfigurationLoadingException;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.core.action.AbstractContinuumAction;
import org.apache.maven.continuum.core.action.CheckoutProjectContinuumAction;
import org.apache.maven.continuum.core.action.CreateProjectsFromMetadataAction;
import org.apache.maven.continuum.core.action.StoreProjectAction;
import org.apache.maven.continuum.execution.ContinuumBuildExecutorConstants;
import org.apache.maven.continuum.execution.manager.BuildExecutorManager;
import org.apache.maven.continuum.initialization.ContinuumInitializationException;
import org.apache.maven.continuum.initialization.ContinuumInitializer;
import org.apache.maven.continuum.installation.InstallationService;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildDefinitionTemplate;
import org.apache.maven.continuum.model.project.BuildQueue;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.model.project.Schedule;
import org.apache.maven.continuum.model.scm.ChangeSet;
import org.apache.maven.continuum.model.scm.ScmResult;
import org.apache.maven.continuum.profile.ProfileService;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.apache.maven.continuum.project.builder.maven.MavenOneContinuumProjectBuilder;
import org.apache.maven.continuum.project.builder.maven.MavenTwoContinuumProjectBuilder;
import org.apache.maven.continuum.release.ContinuumReleaseException;
import org.apache.maven.continuum.release.ContinuumReleaseManager;
import org.apache.maven.continuum.store.ContinuumObjectNotFoundException;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.apache.maven.continuum.utils.ContinuumUrlValidator;
import org.apache.maven.continuum.utils.WorkingDirectoryService;
import org.apache.maven.shared.release.ReleaseResult;
import org.codehaus.plexus.action.Action;
import org.codehaus.plexus.action.ActionManager;
import org.codehaus.plexus.action.ActionNotFoundException;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l </a>
 */
@Component( role = org.apache.maven.continuum.Continuum.class, hint = "default" )
public class DefaultContinuum
    implements Continuum, Initializable, Startable
{
    private static final Logger log = LoggerFactory.getLogger( DefaultContinuum.class );

    @Requirement
    private ActionManager actionManager;

    @Requirement
    private ConfigurationService configurationService;

    @Requirement
    private DaoUtils daoUtils;

    @Requirement
    private BuildDefinitionDao buildDefinitionDao;

    @Requirement
    private BuildResultDao buildResultDao;

    @Requirement
    private NotifierDao notifierDao;

    @Requirement
    private ProjectDao projectDao;

    @Requirement
    private ProjectGroupDao projectGroupDao;

    @Requirement
    private ScheduleDao scheduleDao;

    @Requirement
    private ContinuumReleaseResultDao releaseResultDao;

    @Requirement
    private ProjectScmRootDao projectScmRootDao;

    @Requirement
    private ContinuumInitializer initializer;

    @Requirement
    private SchedulesActivator schedulesActivator;

    @Requirement
    private InstallationService installationService;

    @Requirement
    private ProfileService profileService;

    @Requirement
    private BuildDefinitionService buildDefinitionService;

    // ----------------------------------------------------------------------
    // Moved from core
    // ----------------------------------------------------------------------

    @Requirement
    private ContinuumReleaseManager releaseManager;

    @Requirement
    private WorkingDirectoryService workingDirectoryService;

    @Requirement
    private BuildExecutorManager executorManager;

    @Requirement( hint = "continuumUrl" )
    private ContinuumUrlValidator urlValidator;

    private boolean stopped = false;

    @Requirement
    private ContinuumPurgeManager purgeManager;

    @Requirement
    private RepositoryService repositoryService;

    @Requirement
    private PurgeConfigurationService purgeConfigurationService;

    @Requirement
    private TaskQueueManager taskQueueManager;

    @Requirement( hint = "parallel" )
    private BuildsManager parallelBuildsManager;

    @Requirement
    private BuildQueueService buildQueueService;

    @Requirement
    private DistributedBuildManager distributedBuildManager;

    @Requirement
    private DistributedReleaseManager distributedReleaseManager;

    @Requirement
    private FileSystemManager fsManager;

    public DefaultContinuum()
    {
        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            @Override
            public void run()
            {
                stopContinuum();
            }
        } );
    }

    public ContinuumReleaseManager getReleaseManager()
    {
        return releaseManager;
    }

    public ContinuumPurgeManager getPurgeManager()
    {
        return purgeManager;
    }

    public RepositoryService getRepositoryService()
    {
        return repositoryService;
    }

    public TaskQueueManager getTaskQueueManager()
    {
        return taskQueueManager;
    }

    public PurgeConfigurationService getPurgeConfigurationService()
    {
        return purgeConfigurationService;
    }

    public BuildsManager getBuildsManager()
    {
        return parallelBuildsManager;
    }

    public DistributedReleaseManager getDistributedReleaseManager()
    {
        return distributedReleaseManager;
    }

    // ----------------------------------------------------------------------
    // Project Groups
    // ----------------------------------------------------------------------
    public ProjectGroup getProjectGroup( int projectGroupId )
        throws ContinuumException
    {
        try
        {
            return projectGroupDao.getProjectGroup( projectGroupId );
        }
        catch ( ContinuumObjectNotFoundException e )
        {
            throw new ContinuumException( "invalid group id", e );
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumException( "Error while querying for project group.", e );
        }
    }

    public ProjectGroup getProjectGroupWithProjects( int projectGroupId )
        throws ContinuumException
    {
        try
        {
            return projectGroupDao.getProjectGroupWithProjects( projectGroupId );
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumException( "could not find project group containing " + projectGroupId );
        }
    }

    public ProjectGroup getProjectGroupByProjectId( int projectId )
        throws ContinuumException
    {
        try
        {
            return projectGroupDao.getProjectGroupByProjectId( projectId );
        }
        catch ( ContinuumObjectNotFoundException e )
        {
            throw new ContinuumException( "could not find project group containing " + projectId );
        }
    }

    public void removeProjectGroup( int projectGroupId )
        throws ContinuumException
    {
        ProjectGroup projectGroup = getProjectGroupWithProjects( projectGroupId );

        if ( projectGroup != null )
        {
            List<Project> projects = projectGroup.getProjects();
            int[] projectIds = new int[projects.size()];

            int idx = 0;
            for ( Project project : projects )
            {
                projectIds[idx] = project.getId();
                idx++;
            }

            // check if any project is still being checked out
            // canceling the checkout and proceeding with the delete results to a cannot delete directory error!
            try
            {
                if ( parallelBuildsManager.isAnyProjectCurrentlyBeingCheckedOut( projectIds ) )
                {
                    throw new ContinuumException(
                        "unable to remove group while project is being checked out" );
                }

                if ( parallelBuildsManager.isAnyProjectCurrentlyPreparingBuild( projectIds ) )
                {
                    throw new ContinuumException(
                        "unable to remove group while build is being prepared" );
                }

                if ( parallelBuildsManager.isAnyProjectCurrentlyBuilding( projectIds ) )
                {
                    throw new ContinuumException(
                        "unable to remove group while project is building" );
                }

                if ( isAnyProjectsInReleaseStage( projects ) )
                {
                    throw new ContinuumException(
                        "unable to remove group while project is being released" );
                }
            }
            catch ( BuildManagerException e )
            {
                throw new ContinuumException( e.getMessage() );
            }

            for ( int projectId : projectIds )
            {
                removeProject( projectId );
            }

            // check if there are any project scm root left
            List<ProjectScmRoot> scmRoots = getProjectScmRootByProjectGroup( projectGroupId );

            for ( ProjectScmRoot scmRoot : scmRoots )
            {
                removeProjectScmRoot( scmRoot );
            }

            log.info( "Remove project group " + projectGroup.getName() + "(" + projectGroup.getId() + ")" );

            Map<String, Object> context = new HashMap<String, Object>();
            AbstractContinuumAction.setProjectGroupId( context, projectGroup.getId() );
            executeAction( "remove-assignable-roles", context );

            projectGroupDao.removeProjectGroup( projectGroup );
        }
    }

    public void addProjectGroup( ProjectGroup projectGroup )
        throws ContinuumException
    {
        ProjectGroup pg = null;

        try
        {
            pg = projectGroupDao.getProjectGroupByGroupId( projectGroup.getGroupId() );
        }
        catch ( ContinuumObjectNotFoundException e )
        {
            //since we want to add a new project group, we should be getting
            //this exception
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumException( "Unable to add the requested project group", e );
        }

        if ( pg == null )
        {
            //CONTINUUM-1502
            projectGroup.setName( projectGroup.getName().trim() );
            try
            {
                ProjectGroup new_pg = projectGroupDao.addProjectGroup( projectGroup );

                buildDefinitionService.addBuildDefinitionTemplateToProjectGroup( new_pg.getId(),
                                                                                 buildDefinitionService.getDefaultMavenTwoBuildDefinitionTemplate() );

                Map<String, Object> context = new HashMap<String, Object>();
                AbstractContinuumAction.setProjectGroupId( context, new_pg.getId() );
                executeAction( "add-assignable-roles", context );

                log.info( "Added new project group: " + new_pg.getName() );
            }
            catch ( BuildDefinitionServiceException e )
            {
                throw new ContinuumException( e.getMessage(), e );
            }
            catch ( ContinuumObjectNotFoundException e )
            {
                throw new ContinuumException( e.getMessage(), e );
            }

        }
        else
        {
            throw new ContinuumException( "Unable to add the requested project group: groupId already exists." );
        }

    }

    public List<ProjectGroup> getAllProjectGroups()
    {
        return new ArrayList<ProjectGroup>( projectGroupDao.getAllProjectGroups() );
    }

    public ProjectGroup getProjectGroupByGroupId( String groupId )
        throws ContinuumException
    {
        try
        {
            return projectGroupDao.getProjectGroupByGroupId( groupId );
        }
        catch ( ContinuumObjectNotFoundException e )
        {
            throw new ContinuumException( "Unable to find project group", e );
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumException( "Error retrieving", e );
        }
    }

    public ProjectGroup getProjectGroupByGroupIdWithBuildDetails( String groupId )
        throws ContinuumException
    {
        try
        {
            return projectGroupDao.getProjectGroupByGroupIdWithBuildDetails( groupId );
        }
        catch ( ContinuumObjectNotFoundException e )
        {
            throw new ContinuumException( "Unable to find project group", e );
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumException( "Error retrieving", e );
        }
    }

    public List<ProjectGroup> getAllProjectGroupsWithRepository( int repositoryId )
    {
        return projectGroupDao.getProjectGroupByRepository( repositoryId );
    }

    // ----------------------------------------------------------------------
    // Projects
    // ----------------------------------------------------------------------

    /**
     * TODO: Remove this method
     */
    public Collection<Project> getProjects()
        throws ContinuumException
    {
        return projectDao.getAllProjectsByName();
    }

    /**
     * TODO: Remove this method
     */
    public Collection<Project> getProjectsWithDependencies()
        throws ContinuumException
    {
        return projectDao.getAllProjectsByNameWithDependencies();
    }

    public Map<Integer, BuildResult> getLatestBuildResults( int projectGroupId )
    {
        Map<Integer, BuildResult> result = buildResultDao.getLatestBuildResultsByProjectGroupId( projectGroupId );

        if ( result == null )
        {
            result = new HashMap<Integer, BuildResult>();
        }

        return result;
    }

    public Map<Integer, BuildResult> getBuildResultsInSuccess( int projectGroupId )
    {
        Map<Integer, BuildResult> result = buildResultDao.getBuildResultsInSuccessByProjectGroupId( projectGroupId );

        if ( result == null )
        {
            result = new HashMap<Integer, BuildResult>();
        }

        return result;
    }

    public BuildResult getLatestBuildResultForProject( int projectId )
    {
        return buildResultDao.getLatestBuildResultForProject( projectId );
    }

    public BuildResult getBuildResultByBuildNumber( int projectId, int buildNumber )
        throws ContinuumException
    {
        List<BuildResult> builds = buildResultDao.getBuildResultByBuildNumber( projectId, buildNumber );

        return ( builds.isEmpty() ? null : builds.get( 0 ) );
    }

    public List<BuildResult> getBuildResultsInRange( int projectGroupId, Date fromDate, Date toDate, int state,
                                                     String triggeredBy )
    {
        return buildResultDao.getBuildResultsInRange( fromDate, toDate, state, triggeredBy, projectGroupId );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public void removeProject( int projectId )
        throws ContinuumException
    {
        try
        {
            Project project = getProject( projectId );

            try
            {
                if ( parallelBuildsManager.isProjectCurrentlyBeingCheckedOut( projectId ) )
                {
                    throw new ContinuumException(
                        "Unable to remove project " + projectId + " because it is currently being checked out" );
                }

                if ( parallelBuildsManager.isProjectInAnyCurrentBuild( projectId ) )
                {
                    throw new ContinuumException(
                        "Unable to remove project " + projectId + " because it is currently building" );
                }
            }
            catch ( BuildManagerException e )
            {
                throw new ContinuumException( e.getMessage(), e );
            }

            if ( isProjectInReleaseStage( project ) )
            {
                throw new ContinuumException(
                    "Unable to remove project " + projectId + " because it is in release stage" );
            }

            try
            {
                parallelBuildsManager.removeProjectFromCheckoutQueue( projectId );

                parallelBuildsManager.removeProjectFromBuildQueue( projectId );
            }
            catch ( BuildManagerException e )
            {
                throw new ContinuumException( e.getMessage(), e );
            }

            List<ContinuumReleaseResult> releaseResults = releaseResultDao.getContinuumReleaseResultsByProject(
                projectId );

            ProjectScmRoot scmRoot = getProjectScmRootByProject( projectId );

            try
            {
                for ( ContinuumReleaseResult releaseResult : releaseResults )
                {
                    releaseResultDao.removeContinuumReleaseResult( releaseResult );
                }

                File releaseOutputDirectory = configurationService.getReleaseOutputDirectory(
                    project.getProjectGroup().getId() );

                if ( releaseOutputDirectory != null )
                {
                    fsManager.removeDir( releaseOutputDirectory );
                }
            }
            catch ( ContinuumStoreException e )
            {
                throw new ContinuumException( "Error while deleting continuum release result of project group", e );
            }
            catch ( IOException e )
            {
                throw logAndCreateException( "Error while deleting project group release output directory.", e );
            }

            log.info( "Remove project " + project.getName() + "(" + projectId + ")" );

            // remove dependencies first to avoid key clash with build results
            project = projectDao.getProjectWithDependencies( projectId );
            project.setParent( null );
            project.getDependencies().clear();
            projectDao.updateProject( project );

            Collection<BuildResult> buildResults = getBuildResultsForProject( projectId );

            for ( BuildResult br : buildResults )
            {
                br.setBuildDefinition( null );
                //Remove all modified dependencies to prevent SQL errors
                br.getModifiedDependencies().clear();
                buildResultDao.updateBuildResult( br );
                removeBuildResult( br );
            }

            File workingDirectory = getWorkingDirectory( projectId );

            fsManager.removeDir( workingDirectory );

            File buildOutputDirectory = configurationService.getBuildOutputDirectory( projectId );

            fsManager.removeDir( buildOutputDirectory );

            projectDao.removeProject( projectDao.getProject( projectId ) );

            removeProjectScmRoot( scmRoot );
        }
        catch ( ContinuumStoreException ex )
        {
            throw logAndCreateException( "Error while removing project in database.", ex );
        }
        catch ( IOException e )
        {
            throw logAndCreateException( "Error while deleting project working directory.", e );
        }
    }

    /**
     * @see org.apache.maven.continuum.Continuum#checkoutProject(int)
     */
    public void checkoutProject( int projectId )
        throws ContinuumException
    {
        Map<String, Object> context = new HashMap<String, Object>();

        AbstractContinuumAction.setProjectId( context, projectId );

        try
        {
            BuildDefinition buildDefinition = buildDefinitionDao.getDefaultBuildDefinition( projectId );
            AbstractContinuumAction.setBuildDefinition( context, buildDefinition );

            executeAction( "add-project-to-checkout-queue", context );
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumException( e.getMessage(), e );
        }
    }

    public Project getProject( int projectId )
        throws ContinuumException
    {
        try
        {
            return projectDao.getProject( projectId );
        }
        catch ( ContinuumStoreException ex )
        {
            throw logAndCreateException( "Exception while getting project '" + projectId + "'.", ex );
        }
    }

    public Project getProjectWithBuildDetails( int projectId )
        throws ContinuumException
    {
        try
        {
            return projectDao.getProjectWithBuildDetails( projectId );
        }
        catch ( ContinuumStoreException ex )
        {
            throw logAndCreateException( "Exception while getting project '" + projectId + "'.", ex );
        }
    }

    public Map<Integer, ProjectGroupSummary> getProjectsSummaryByGroups()
    {
        return projectDao.getProjectsSummary();
    }

    // ----------------------------------------------------------------------
    // Building
    // ----------------------------------------------------------------------

    public void buildProjects( String username )
        throws ContinuumException, NoBuildAgentException, NoBuildAgentInGroupException
    {
        buildProjects( new BuildTrigger( ContinuumProjectState.TRIGGER_FORCED, username ) );
    }

    public void buildProjectsWithBuildDefinition( List<Project> projects, List<BuildDefinition> bds )
        throws ContinuumException, NoBuildAgentException, NoBuildAgentInGroupException
    {
        Collection<Project> filteredProjectsList = getProjectsNotInReleaseStage( projects );

        prepareBuildProjects( filteredProjectsList, bds, true, new BuildTrigger( ContinuumProjectState.TRIGGER_FORCED,
                                                                                 "" ) );
    }

    public void buildProjectsWithBuildDefinition( List<Project> projects, int buildDefinitionId )
        throws ContinuumException, NoBuildAgentException, NoBuildAgentInGroupException
    {
        Collection<Project> filteredProjectsList = getProjectsNotInReleaseStage( projects );

        prepareBuildProjects( filteredProjectsList, buildDefinitionId, new BuildTrigger(
            ContinuumProjectState.TRIGGER_FORCED, "" ) );
    }

    /**
     * fire of the builds of all projects across all project groups using their default build definitions
     * TODO:Remove this method
     *
     * @param buildTrigger
     * @throws ContinuumException
     */
    public void buildProjects( BuildTrigger buildTrigger )
        throws ContinuumException, NoBuildAgentException, NoBuildAgentInGroupException
    {
        Collection<Project> projectsList = getProjectsInBuildOrder();

        Collection<Project> filteredProjectsList = getProjectsNotInReleaseStage( projectsList );

        prepareBuildProjects( filteredProjectsList, null, true, buildTrigger );
    }

    /**
     * fire off a build for all of the projects in a project group using their default builds
     *
     * @param projectGroupId
     * @param buildTrigger
     * @throws ContinuumException
     */
    public void buildProjectGroup( int projectGroupId, BuildTrigger buildTrigger )
        throws ContinuumException, NoBuildAgentException, NoBuildAgentInGroupException
    {
        List<BuildDefinition> groupDefaultBDs;

        if ( !isAnyProjectInGroupInReleaseStage( projectGroupId ) )
        {
            groupDefaultBDs = getDefaultBuildDefinitionsForProjectGroup( projectGroupId );

            buildProjectGroupWithBuildDefinition( projectGroupId, groupDefaultBDs, true, buildTrigger );
        }
    }

    /**
     * fire off a build for all of the projects in a project group using their default builds.
     *
     * @param projectGroupId    the project group id
     * @param buildDefinitionId the build definition id to use
     * @param buildTrigger      the trigger state and the username
     * @throws ContinuumException
     */
    public void buildProjectGroupWithBuildDefinition( int projectGroupId, int buildDefinitionId,
                                                      BuildTrigger buildTrigger )
        throws ContinuumException, NoBuildAgentException, NoBuildAgentInGroupException
    {
        if ( !isAnyProjectInGroupInReleaseStage( projectGroupId ) )
        {
            List<BuildDefinition> bds = new ArrayList<BuildDefinition>();
            BuildDefinition bd = getBuildDefinition( buildDefinitionId );
            if ( bd != null )
            {
                bds.add( bd );
            }
            buildProjectGroupWithBuildDefinition( projectGroupId, bds, false, buildTrigger );
        }
    }

    /**
     * fire off a build for all of the projects in a project group using their default builds
     *
     * @param projectGroupId
     * @param bds
     * @param checkDefaultBuildDefinitionForProject
     * @param buildTrigger
     * @throws ContinuumException
     */
    private void buildProjectGroupWithBuildDefinition( int projectGroupId, List<BuildDefinition> bds,
                                                       boolean checkDefaultBuildDefinitionForProject,
                                                       BuildTrigger buildTrigger )
        throws ContinuumException, NoBuildAgentException, NoBuildAgentInGroupException
    {
        if ( !isAnyProjectInGroupInReleaseStage( projectGroupId ) )
        {
            Collection<Project> projectsList;

            projectsList = getProjectsInBuildOrder( projectDao.getProjectsWithDependenciesByGroupId( projectGroupId ) );

            buildTrigger.setTrigger( ContinuumProjectState.TRIGGER_FORCED );

            prepareBuildProjects( projectsList, bds, checkDefaultBuildDefinitionForProject, buildTrigger );
        }
    }

    /**
     * takes a given schedule and determines which projects need to build
     * The build order is determined by the dependencies
     *
     * @param schedule The schedule
     * @throws ContinuumException
     */
    public void buildProjects( Schedule schedule )
        throws ContinuumException
    {
        Collection<Project> projectsList;

        Map<Integer, Object> projectsMap;

        try
        {
            projectsMap = daoUtils.getAggregatedProjectIdsAndBuildDefinitionIdsBySchedule( schedule.getId() );

            if ( projectsMap == null || projectsMap.size() == 0 )
            {
                log.debug( "no builds attached to schedule" );
                try
                {
                    schedulesActivator.unactivateOrphanBuildSchedule( schedule );
                }
                catch ( SchedulesActivationException e )
                {
                    log.debug( "Can't unactivate orphan shcedule for buildDefinitions" );
                }
                // We don't have projects attached to this schedule. This is because it's only is setting for a
                // templateBuildDefinition
                return;
            }

            //TODO: As all projects are built in the same queue for a project group, it would be better to get them by
            // project group and add them in queues in parallel to save few seconds
            projectsList = getProjectsInBuildOrder();
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumException( "Can't get project list for schedule " + schedule.getName(), e );
        }

        Map<ProjectScmRoot, Map<Integer, Integer>> map = new HashMap<ProjectScmRoot, Map<Integer, Integer>>();
        List<ProjectScmRoot> sortedScmRoot = new ArrayList<ProjectScmRoot>();

        boolean signalIgnored = false;

        for ( Project project : projectsList )
        {
            List<Integer> buildDefIds = (List<Integer>) projectsMap.get( project.getId() );
            int projectId = project.getId();

            if ( buildDefIds != null && !buildDefIds.isEmpty() )
            {
                for ( Integer buildDefId : buildDefIds )
                {
                    try
                    {
                        assertBuildable( project.getId(), buildDefId );
                    }
                    catch ( BuildException be )
                    {
                        log.info( "project not queued for build preparation: {}", be.getLocalizedMessage() );
                        signalIgnored = true;
                        continue;
                    }

                    ProjectScmRoot scmRoot = getProjectScmRootByProject( project.getId() );

                    Map<Integer, Integer> projectsAndBuildDefinitionsMap = map.get( scmRoot );

                    if ( projectsAndBuildDefinitionsMap == null )
                    {
                        projectsAndBuildDefinitionsMap = new HashMap<Integer, Integer>();
                    }

                    projectsAndBuildDefinitionsMap.put( projectId, buildDefId );

                    map.put( scmRoot, projectsAndBuildDefinitionsMap );

                    if ( !sortedScmRoot.contains( scmRoot ) )
                    {
                        sortedScmRoot.add( scmRoot );
                    }
                }
            }
        }

        BuildTrigger buildTrigger = new BuildTrigger( ContinuumProjectState.TRIGGER_SCHEDULED, schedule.getName() );

        for ( ProjectScmRoot scmRoot : sortedScmRoot )
        {
            try
            {
                prepareBuildProjects( map.get( scmRoot ), buildTrigger, scmRoot.getScmRootAddress(),
                                      scmRoot.getProjectGroup().getId(), scmRoot.getId(), sortedScmRoot );
            }
            catch ( NoBuildAgentException e )
            {
                log.error( "Unable to build projects in project group " + scmRoot.getProjectGroup().getName() +
                               " because there is no build agent configured" );
            }
            catch ( NoBuildAgentInGroupException e )
            {
                log.error( "Unable to build projects in project group " + scmRoot.getProjectGroup().getName() +
                               " because there is no build agent configured in build agent group" );
            }
        }

        if ( signalIgnored )
        {
            throw new BuildException( "some projects were not queued due to their current build state",
                                      "build.projects.someNotQueued" );
        }
    }

    public void buildProject( int projectId, String username )
        throws ContinuumException, NoBuildAgentException, NoBuildAgentInGroupException
    {
        buildProject( projectId, new BuildTrigger( ContinuumProjectState.TRIGGER_FORCED, username ) );
    }

    public void buildProjectWithBuildDefinition( int projectId, int buildDefinitionId, BuildTrigger buildTrigger )
        throws ContinuumException, NoBuildAgentException, NoBuildAgentInGroupException
    {
        buildTrigger.setTrigger( ContinuumProjectState.TRIGGER_FORCED );
        buildProject( projectId, buildDefinitionId, buildTrigger );
    }

    public void buildProject( int projectId, BuildTrigger buildTrigger )
        throws ContinuumException, NoBuildAgentException, NoBuildAgentInGroupException
    {
        Project project = getProject( projectId );
        if ( isProjectInReleaseStage( project ) )
        {
            throw new ContinuumException( "Project (id=" + projectId + ") is currently in release stage." );
        }

        BuildDefinition buildDef = getDefaultBuildDefinition( projectId );

        if ( buildDef == null )
        {
            throw new ContinuumException( "Project (id=" + projectId + ") doesn't have a default build definition." );
        }

        assertBuildable( projectId, buildDef.getId() );

        Map<Integer, Integer> projectsBuildDefinitionsMap = new HashMap<Integer, Integer>();
        projectsBuildDefinitionsMap.put( projectId, buildDef.getId() );

        ProjectScmRoot scmRoot = getProjectScmRootByProject( projectId );
        List<ProjectScmRoot> sortedScmRoot = new ArrayList<ProjectScmRoot>();
        sortedScmRoot.add( scmRoot );

        prepareBuildProjects( projectsBuildDefinitionsMap, buildTrigger, scmRoot.getScmRootAddress(),
                              scmRoot.getProjectGroup().getId(), scmRoot.getId(), sortedScmRoot );
    }

    public void buildProject( int projectId, int buildDefinitionId, BuildTrigger buildTrigger )
        throws ContinuumException, NoBuildAgentException, NoBuildAgentInGroupException
    {
        Project project = getProject( projectId );
        if ( isProjectInReleaseStage( project ) )
        {
            throw new ContinuumException( "Project (id=" + projectId + ") is currently in release stage." );
        }

        assertBuildable( projectId, buildDefinitionId );

        Map<Integer, Integer> projectsBuildDefinitionsMap = new HashMap<Integer, Integer>();
        projectsBuildDefinitionsMap.put( projectId, buildDefinitionId );

        ProjectScmRoot scmRoot = getProjectScmRootByProject( projectId );
        List<ProjectScmRoot> sortedScmRoot = new ArrayList<ProjectScmRoot>();
        sortedScmRoot.add( scmRoot );

        prepareBuildProjects( projectsBuildDefinitionsMap, buildTrigger, scmRoot.getScmRootAddress(),
                              scmRoot.getProjectGroup().getId(), scmRoot.getId(), sortedScmRoot );
    }

    public BuildResult getBuildResult( int buildId )
        throws ContinuumException
    {
        try
        {
            return buildResultDao.getBuildResult( buildId );
        }
        catch ( ContinuumStoreException e )
        {
            throw logAndCreateException( "Exception while getting build result for project.", e );
        }
    }

    public void removeBuildResult( int buildId )
        throws ContinuumException
    {
        BuildResult buildResult = getBuildResult( buildId );

        // check first if build result is currently being used by a building project
        Project project = buildResult.getProject();
        BuildResult bResult = getLatestBuildResultForProject( project.getId() );

        try
        {
            if ( bResult != null && buildResult.getId() == bResult.getId() &&
                parallelBuildsManager.isProjectInAnyCurrentBuild( project.getId() ) )
            {
                throw new ContinuumException(
                    "Unable to remove build result because it is currently being used by a building project " +
                        project.getId() );
            }

            int projectId = buildResult.getProject().getId();
            int buildDefId = buildResult.getBuildDefinition().getId();
            boolean resultPending = false;

            try
            {
                resultPending =
                    distributedBuildManager.getCurrentRun( projectId, buildDefId ).getBuildResultId() == buildId;
            }
            catch ( ContinuumException e )
            {
                // No current run for given project/builddef
            }

            if ( resultPending )
            {
                throw new ContinuumException(
                    String.format( "Unable to remove build result %s, response is pending from build agent %s.",
                                   buildId, buildResult.getBuildUrl() ) );
            }
        }
        catch ( BuildManagerException e )
        {
            throw new ContinuumException( e.getMessage(), e );
        }

        buildResult.getModifiedDependencies().clear();
        buildResult.setBuildDefinition( null );

        try
        {
            buildResultDao.updateBuildResult( buildResult );
        }
        catch ( ContinuumStoreException e )
        {
            throw logAndCreateException( "Error while removing build result in database.", e );
        }
        removeBuildResult( buildResult );
    }

    private void removeBuildResult( BuildResult buildResult )
    {
        buildResultDao.removeBuildResult( buildResult );

        // cleanup some files
        try
        {
            File buildOutputDirectory = getConfiguration().getBuildOutputDirectory( buildResult.getProject().getId() );
            File buildDirectory = new File( buildOutputDirectory, Integer.toString( buildResult.getId() ) );

            if ( buildDirectory.exists() )
            {
                fsManager.removeDir( buildDirectory );
            }
            File buildOutputFile = getConfiguration().getBuildOutputFile( buildResult.getId(),
                                                                          buildResult.getProject().getId() );
            if ( buildOutputFile.exists() )
            {
                fsManager.delete( buildOutputFile );
            }
        }
        catch ( ConfigurationException e )
        {
            log.info( "skip error during cleanup build files " + e.getMessage(), e );
        }
        catch ( IOException e )
        {
            log.info( "skip IOException during cleanup build files " + e.getMessage(), e );
        }

    }

    public String getBuildOutput( int projectId, int buildId )
        throws ContinuumException
    {
        try
        {
            return configurationService.getBuildOutput( buildId, projectId );
        }
        catch ( ConfigurationException e )
        {
            throw logAndCreateException( "Exception while getting build result for project.", e );
        }
    }

    /**
     * TODO: Must be done by build definition
     */
    public List<ChangeSet> getChangesSinceLastSuccess( int projectId, int buildResultId )
        throws ContinuumException
    {
        BuildResult previousBuildResult = null;
        try
        {
            previousBuildResult = buildResultDao.getPreviousBuildResultInSuccess( projectId, buildResultId );
        }
        catch ( ContinuumStoreException e )
        {
            //No previous build in success, Nothing to do
        }
        long startTime = previousBuildResult == null ? 0 : previousBuildResult.getStartTime();
        ArrayList<BuildResult> buildResults = new ArrayList<BuildResult>(
            buildResultDao.getBuildResultsForProjectWithDetails( projectId, startTime, buildResultId ) );

        Collections.reverse( buildResults );

        Iterator<BuildResult> buildResultsIterator = buildResults.iterator();

        boolean stop = false;

        //TODO: Shouldn't be used now with the previous call of buildResultDao.getBuildResultsForProjectWithDetails
        while ( !stop )
        {
            if ( buildResultsIterator.hasNext() )
            {
                BuildResult buildResult = buildResultsIterator.next();

                if ( buildResult.getId() == buildResultId )
                {
                    stop = true;
                }
            }
            else
            {
                stop = true;
            }
        }

        if ( !buildResultsIterator.hasNext() )
        {
            return null;
        }

        BuildResult buildResult = buildResultsIterator.next();

        List<ChangeSet> changes = null;

        while ( buildResult.getState() != ContinuumProjectState.OK )
        {
            if ( changes == null )
            {
                changes = new ArrayList<ChangeSet>();
            }

            ScmResult scmResult = buildResult.getScmResult();

            if ( scmResult != null )
            {
                changes.addAll( scmResult.getChanges() );
            }

            if ( !buildResultsIterator.hasNext() )
            {
                return changes;
            }

            buildResult = buildResultsIterator.next();
        }

        if ( changes == null )
        {
            changes = Collections.EMPTY_LIST;
        }

        return changes;
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    /**
     * TODO: Remove this method when it won't be used
     */
    private List<Project> getProjectsInBuildOrder()
        throws ContinuumException
    {
        return getProjectsInBuildOrder( getProjectsWithDependencies() );
    }

    /**
     * take a collection of projects and sort for order
     *
     * @param projects
     * @return
     */
    public List<Project> getProjectsInBuildOrder( Collection<Project> projects )
    {
        if ( projects == null || projects.isEmpty() )
        {
            return new ArrayList<Project>();
        }

        return ProjectSorter.getSortedProjects( projects, log );
    }

    // ----------------------------------------------------------------------
    // Maven 1.x projects
    // ----------------------------------------------------------------------

    public ContinuumProjectBuildingResult addMavenOneProject( String metadataUrl, int projectGroupId )
        throws ContinuumException
    {
        return addMavenOneProject( metadataUrl, projectGroupId, true );
    }

    public ContinuumProjectBuildingResult addMavenOneProject( String metadataUrl, int projectGroupId,
                                                              boolean checkProtocol )
        throws ContinuumException
    {
        return addMavenOneProject( metadataUrl, projectGroupId, checkProtocol, false );
    }

    public ContinuumProjectBuildingResult addMavenOneProject( String metadataUrl, int projectGroupId,
                                                              boolean checkProtocol, boolean useCredentialsCache )
        throws ContinuumException
    {
        try
        {
            return addMavenOneProject( metadataUrl, projectGroupId, checkProtocol, useCredentialsCache,
                                       buildDefinitionService.getDefaultMavenOneBuildDefinitionTemplate().getId() );
        }
        catch ( BuildDefinitionServiceException e )
        {
            throw new ContinuumException( e.getMessage(), e );
        }
    }

    public ContinuumProjectBuildingResult addMavenOneProject( String metadataUrl, int projectGroupId,
                                                              boolean checkProtocol, boolean useCredentialsCache,
                                                              int buildDefinitionTemplateId )
        throws ContinuumException
    {
        return executeAddProjectsFromMetadataActivity( metadataUrl, MavenOneContinuumProjectBuilder.ID, projectGroupId,
                                                       checkProtocol, useCredentialsCache, true,
                                                       buildDefinitionTemplateId, false );
    }

    // ----------------------------------------------------------------------
    // Maven 2.x projects
    // ----------------------------------------------------------------------

    public ContinuumProjectBuildingResult addMavenTwoProject( String metadataUrl )
        throws ContinuumException
    {
        return addMavenTwoProject( metadataUrl, true );
    }

    public ContinuumProjectBuildingResult addMavenTwoProject( String metadataUrl, boolean checkProtocol )
        throws ContinuumException
    {
        try
        {
            return executeAddProjectsFromMetadataActivity( metadataUrl, MavenTwoContinuumProjectBuilder.ID, -1,
                                                           checkProtocol,
                                                           buildDefinitionService.getDefaultMavenTwoBuildDefinitionTemplate().getId() );
        }
        catch ( BuildDefinitionServiceException e )
        {
            throw new ContinuumException( e.getMessage(), e );
        }
    }

    public ContinuumProjectBuildingResult addMavenTwoProject( String metadataUrl, int projectGroupId )
        throws ContinuumException
    {
        return addMavenTwoProject( metadataUrl, projectGroupId, true );
    }

    public ContinuumProjectBuildingResult addMavenTwoProject( String metadataUrl, int projectGroupId,
                                                              boolean checkProtocol )
        throws ContinuumException
    {
        return addMavenTwoProject( metadataUrl, projectGroupId, checkProtocol, false );
    }

    public ContinuumProjectBuildingResult addMavenTwoProject( String metadataUrl, int projectGroupId,
                                                              boolean checkProtocol, boolean useCredentialsCache )
        throws ContinuumException
    {
        try
        {
            return executeAddProjectsFromMetadataActivity( metadataUrl, MavenTwoContinuumProjectBuilder.ID,
                                                           projectGroupId, checkProtocol, useCredentialsCache, true,
                                                           buildDefinitionService.getDefaultMavenTwoBuildDefinitionTemplate().getId(),
                                                           false );
        }
        catch ( BuildDefinitionServiceException e )
        {
            throw new ContinuumException( e.getMessage(), e );
        }
    }

    public ContinuumProjectBuildingResult addMavenTwoProject( String metadataUrl, int projectGroupId,
                                                              boolean checkProtocol, boolean useCredentialsCache,
                                                              boolean recursiveProjects )
        throws ContinuumException
    {
        try
        {
            return executeAddProjectsFromMetadataActivity( metadataUrl, MavenTwoContinuumProjectBuilder.ID,
                                                           projectGroupId, checkProtocol, useCredentialsCache,
                                                           recursiveProjects,
                                                           buildDefinitionService.getDefaultMavenTwoBuildDefinitionTemplate().getId(),
                                                           false );
        }
        catch ( BuildDefinitionServiceException e )
        {
            throw new ContinuumException( e.getMessage(), e );
        }
    }

    public ContinuumProjectBuildingResult addMavenTwoProject( String metadataUrl, int projectGroupId,
                                                              boolean checkProtocol, boolean useCredentialsCache,
                                                              boolean recursiveProjects, int buildDefinitionTemplateId,
                                                              boolean checkoutInSingleDirectory )
        throws ContinuumException
    {
        return executeAddProjectsFromMetadataActivity( metadataUrl, MavenTwoContinuumProjectBuilder.ID, projectGroupId,
                                                       checkProtocol, useCredentialsCache, recursiveProjects,
                                                       buildDefinitionTemplateId, checkoutInSingleDirectory );
    }

    // ----------------------------------------------------------------------
    // Shell projects
    // ----------------------------------------------------------------------

    public int addProject( Project project, String executorId, int groupId )
        throws ContinuumException
    {
        return addProject( project, executorId, groupId, -1 );
    }

    /**
     * @see org.apache.maven.continuum.Continuum#addProject(org.apache.maven.continuum.model.project.Project, java.lang.String, int, int)
     */
    public int addProject( Project project, String executorId, int groupId, int buildDefinitionTemplateId )
        throws ContinuumException
    {
        project.setExecutorId( executorId );

        return executeAddProjectFromScmActivity( project, groupId, buildDefinitionTemplateId );
    }

    // ----------------------------------------------------------------------
    // Activities. These should end up as workflows in werkflow
    // ----------------------------------------------------------------------

    private int executeAddProjectFromScmActivity( Project project, int groupId, int buildDefinitionTemplateId )
        throws ContinuumException
    {
        String executorId = project.getExecutorId();

        ProjectGroup projectGroup = getProjectGroupWithBuildDetails( groupId );

        Map<String, Object> context = new HashMap<String, Object>();

        String scmUrl = project.getScmUrl();

        List<ProjectScmRoot> scmRoots = getProjectScmRootByProjectGroup( groupId );

        boolean found = false;

        for ( ProjectScmRoot scmRoot : scmRoots )
        {
            if ( scmUrl.startsWith( scmRoot.getScmRootAddress() ) )
            {
                found = true;
                break;
            }
        }

        if ( !found )
        {
            createProjectScmRoot( projectGroup, scmUrl );
        }

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        AbstractContinuumAction.setWorkingDirectory( context, getWorkingDirectory() );

        AbstractContinuumAction.setUnvalidatedProject( context, project );

        AbstractContinuumAction.setUnvalidatedProjectGroup( context, projectGroup );

        AbstractContinuumAction.setProjectGroupId( context, projectGroup.getId() );

        StoreProjectAction.setUseScmCredentialsCache( context, project.isScmUseCache() );

        // set for initial checkout
        String scmUsername = project.getScmUsername();
        String scmPassword = project.getScmPassword();

        if ( scmUsername != null && !StringUtils.isEmpty( scmUsername ) )
        {
            CheckoutProjectContinuumAction.setScmUsername( context, scmUsername );
        }

        if ( scmPassword != null && !StringUtils.isEmpty( scmPassword ) )
        {
            CheckoutProjectContinuumAction.setScmPassword( context, scmPassword );
        }

        executeAction( "validate-project", context );

        executeAction( "store-project", context );

        try
        {
            BuildDefinitionTemplate bdt;

            if ( executorId.equalsIgnoreCase( ContinuumBuildExecutorConstants.ANT_BUILD_EXECUTOR ) )
            {
                if ( buildDefinitionTemplateId <= 0 )
                {
                    bdt = buildDefinitionService.getDefaultAntBuildDefinitionTemplate();
                }
                else
                {
                    bdt = buildDefinitionService.getBuildDefinitionTemplate( buildDefinitionTemplateId );
                }
            }
            else
            {
                //shell default
                if ( buildDefinitionTemplateId <= 0 )
                {
                    bdt = buildDefinitionService.getDefaultShellBuildDefinitionTemplate();
                }
                else
                {
                    bdt = buildDefinitionService.getBuildDefinitionTemplate( buildDefinitionTemplateId );
                }
            }

            buildDefinitionService.addTemplateInProject( bdt.getId(), getProject( AbstractContinuumAction.getProjectId(
                context ) ) );
        }
        catch ( BuildDefinitionServiceException e )
        {
            throw new ContinuumException( e.getMessage(), e );
        }

        if ( !configurationService.isDistributedBuildEnabled() )
        {
            // used by BuildManager to determine on which build queue will the project be put
            BuildDefinition bd = (BuildDefinition) getProjectWithBuildDetails( AbstractContinuumAction.getProjectId(
                context ) ).getBuildDefinitions().get( 0 );
            AbstractContinuumAction.setBuildDefinition( context, bd );

            executeAction( "add-project-to-checkout-queue", context );
        }

        executeAction( "add-assignable-roles", context );

        return AbstractContinuumAction.getProjectId( context );
    }

    private ContinuumProjectBuildingResult executeAddProjectsFromMetadataActivity( String metadataUrl,
                                                                                   String projectBuilderId,
                                                                                   int projectGroupId,
                                                                                   boolean checkProtocol,
                                                                                   int buildDefinitionTemplateId )
        throws ContinuumException
    {
        return executeAddProjectsFromMetadataActivity( metadataUrl, projectBuilderId, projectGroupId, checkProtocol,
                                                       false, false, buildDefinitionTemplateId, false );
    }

    protected ContinuumProjectBuildingResult executeAddProjectsFromMetadataActivity( String metadataUrl,
                                                                                     String projectBuilderId,
                                                                                     int projectGroupId,
                                                                                     boolean checkProtocol,
                                                                                     boolean useCredentialsCache,
                                                                                     boolean loadRecursiveProjects,
                                                                                     int buildDefinitionTemplateId,
                                                                                     boolean addAssignableRoles,
                                                                                     boolean checkoutInSingleDirectory )
        throws ContinuumException
    {
        if ( checkProtocol )
        {
            if ( !urlValidator.validate( metadataUrl ) )
            {
                ContinuumProjectBuildingResult res = new ContinuumProjectBuildingResult();
                res.addError( ContinuumProjectBuildingResult.ERROR_PROTOCOL_NOT_ALLOWED );
                return res;
            }
        }

        Map<String, Object> context = new HashMap<String, Object>();

        CreateProjectsFromMetadataAction.setProjectBuilderId( context, projectBuilderId );

        CreateProjectsFromMetadataAction.setUrl( context, metadataUrl );

        CreateProjectsFromMetadataAction.setLoadRecursiveProject( context, loadRecursiveProjects );

        StoreProjectAction.setUseScmCredentialsCache( context, useCredentialsCache );

        AbstractContinuumAction.setWorkingDirectory( context, getWorkingDirectory() );

        CreateProjectsFromMetadataAction.setCheckoutProjectsInSingleDirectory( context, checkoutInSingleDirectory );

        // CreateProjectsFromMetadataAction will check null and use default
        if ( buildDefinitionTemplateId > 0 )
        {
            try
            {
                AbstractContinuumAction.setBuildDefinitionTemplate( context,
                                                                    buildDefinitionService.getBuildDefinitionTemplate(
                                                                        buildDefinitionTemplateId ) );
            }
            catch ( BuildDefinitionServiceException e )
            {
                throw new ContinuumException( e.getMessage(), e );
            }
        }
        // ----------------------------------------------------------------------
        // Create the projects from the URL
        // ----------------------------------------------------------------------

        ProjectGroup projectGroup;

        if ( projectGroupId != -1 )
        {
            CreateProjectsFromMetadataAction.setProjectGroupId( context, projectGroupId );
        }

        executeAction( "create-projects-from-metadata", context );

        ContinuumProjectBuildingResult result = CreateProjectsFromMetadataAction.getProjectBuildingResult( context );

        if ( log.isInfoEnabled() )
        {
            if ( result.getProjects() != null )
            {
                log.info( "Created " + result.getProjects().size() + " projects." );
            }
            if ( result.getProjectGroups() != null )
            {
                log.info( "Created " + result.getProjectGroups().size() + " project groups." );
            }
            log.info( result.getErrors().size() + " errors." );

            // ----------------------------------------------------------------------
            // Look for any errors.
            // ----------------------------------------------------------------------

            if ( result.hasErrors() )
            {
                log.info( result.getErrors().size() + " errors during project add: " );
                log.info( result.getErrorsAsString() );
                return result;
            }
        }

        // ----------------------------------------------------------------------
        // Save any new project groups that we've found. Currently all projects
        // will go into the first project group in the list.
        // ----------------------------------------------------------------------

        if ( result.getProjectGroups().size() != 1 )
        {
            throw new ContinuumException( "The project building result has to contain exactly one project group." );
        }

        boolean projectGroupCreation = false;

        try
        {
            if ( projectGroupId == -1 )
            {
                projectGroup = result.getProjectGroups().iterator().next();

                try
                {
                    projectGroup = projectGroupDao.getProjectGroupByGroupId( projectGroup.getGroupId() );

                    projectGroupId = projectGroup.getId();

                    log.info( "Using existing project group with the group id: '" + projectGroup.getGroupId() + "'." );
                }
                catch ( ContinuumObjectNotFoundException e )
                {
                    log.info( "Creating project group with the group id: '" + projectGroup.getGroupId() + "'." );

                    Map<String, Object> pgContext = new HashMap<String, Object>();

                    AbstractContinuumAction.setWorkingDirectory( pgContext, getWorkingDirectory() );

                    AbstractContinuumAction.setUnvalidatedProjectGroup( pgContext, projectGroup );

                    executeAction( "validate-project-group", pgContext );

                    executeAction( "store-project-group", pgContext );

                    projectGroupId = AbstractContinuumAction.getProjectGroupId( pgContext );

                    projectGroupCreation = true;
                }
            }

            projectGroup = projectGroupDao.getProjectGroupWithBuildDetailsByProjectGroupId( projectGroupId );

            //String url = CreateProjectsFromMetadataAction.getUrl( context );
            String url = AbstractContinuumAction.getProjectScmRootUrl( context, null );

            List<ProjectScmRoot> scmRoots = getProjectScmRootByProjectGroup( projectGroup.getId() );

            boolean found = false;

            for ( ProjectScmRoot scmRoot : scmRoots )
            {
                if ( url.startsWith( scmRoot.getScmRootAddress() ) )
                {
                    found = true;
                    break;
                }
            }

            if ( !found )
            {
                createProjectScmRoot( projectGroup, url );
            }

            /* add the project group loaded from database, which has more info, like id */
            result.getProjectGroups().remove( 0 );
            result.getProjectGroups().add( projectGroup );
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumException( "Error while querying for project group.", e );
        }

        // ----------------------------------------------------------------------
        // Save all the projects if recursive mode asked
        // TODO: Validate all the projects before saving them
        // ----------------------------------------------------------------------

        List<Project> projects = result.getProjects();

        String scmUserName = null;
        String scmPassword = null;

        for ( Project project : projects )
        {
            checkForDuplicateProjectInGroup( projectGroup, project, result );

            if ( result.hasErrors() )
            {
                log.info( result.getErrors().size() + " errors during project add: " );
                log.info( result.getErrorsAsString() );
                return result;
            }

            project.setScmUseCache( useCredentialsCache );

            // values backup for first checkout
            scmUserName = project.getScmUsername();
            scmPassword = project.getScmPassword();
            // CONTINUUM-1792 : we don't store it
            if ( useCredentialsCache )
            {
                project.setScmUsername( null );
                project.setScmPassword( null );
            }

            projectGroup.addProject( project );
        }

        try
        {
            projectGroupDao.updateProjectGroup( projectGroup );

            if ( !checkoutInSingleDirectory )
            {
                for ( Project project : projects )
                {
                    context = new HashMap<String, Object>();

                    Project fetchedProject = projectDao.getProjectWithBuildDetails( project.getId() );

                    addProjectToCheckoutQueue( projectBuilderId, buildDefinitionTemplateId, context,
                                               projectGroupCreation, scmUserName, scmPassword, project,
                                               isDefaultProjectBuildDefSet( fetchedProject ) );
                }
            }
            else
            {
                Project project = result.getRootProject();

                if ( project != null )
                {
                    String scmRootUrl = AbstractContinuumAction.getProjectScmRootUrl( context, null );
                    context = new HashMap<String, Object>();

                    AbstractContinuumAction.setProjectScmRootUrl( context, scmRootUrl );

                    List<Project> projectsWithSimilarScmRoot = new ArrayList<Project>();
                    for ( Project projectWithSimilarScmRoot : projects )
                    {
                        projectsWithSimilarScmRoot.add( projectWithSimilarScmRoot );
                    }

                    AbstractContinuumAction.setListOfProjectsInGroupWithCommonScmRoot( context,
                                                                                       projectsWithSimilarScmRoot );

                    Project fetchedProject = projectDao.getProjectWithBuildDetails( project.getId() );

                    addProjectToCheckoutQueue( projectBuilderId, buildDefinitionTemplateId, context,
                                               projectGroupCreation, scmUserName, scmPassword, project,
                                               isDefaultProjectBuildDefSet( fetchedProject ) );
                }
            }
        }
        catch ( BuildDefinitionServiceException e )
        {
            throw new ContinuumException( "Error attaching buildDefintionTemplate to project ", e );
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumException( "Error adding projects from modules", e );
        }

        AbstractContinuumAction.setProjectGroupId( context, projectGroup.getId() );
        // add the relevant security administration roles for this project
        if ( addAssignableRoles )
        {
            executeAction( "add-assignable-roles", context );
        }
        return result;
    }

    private boolean isDefaultProjectBuildDefSet( Project project )
    {
        for ( BuildDefinition bd : project.getBuildDefinitions() )
        {
            if ( bd.isDefaultForProject() )
            {
                return true;
            }
        }

        return false;
    }

    private void addProjectToCheckoutQueue( String projectBuilderId, int buildDefinitionTemplateId,
                                            Map<String, Object> context, boolean projectGroupCreation,
                                            String scmUserName, String scmPassword, Project project,
                                            boolean defaultProjectBuildDefSet )
        throws BuildDefinitionServiceException, ContinuumStoreException, ContinuumException
    {
        // CONTINUUM-1953 olamy : attached buildDefs from template here
        // if no group creation
        if ( !projectGroupCreation && buildDefinitionTemplateId > 0 && !defaultProjectBuildDefSet )
        {
            buildDefinitionService.addTemplateInProject( buildDefinitionTemplateId, projectDao.getProject(
                project.getId() ) );
        }

        AbstractContinuumAction.setUnvalidatedProject( context, project );
        //
        //            executeAction( "validate-project", context );
        //
        //            executeAction( "store-project", context );
        //

        AbstractContinuumAction.setProjectId( context, project.getId() );

        // does the scm username & password really have to be set in the project?
        if ( !StringUtils.isEmpty( scmUserName ) )
        {
            project.setScmUsername( scmUserName );
            CheckoutProjectContinuumAction.setScmUsername( context, scmUserName );
        }
        if ( !StringUtils.isEmpty( scmPassword ) )
        {
            project.setScmPassword( scmPassword );
            CheckoutProjectContinuumAction.setScmPassword( context, scmPassword );
        }
        //FIXME
        // olamy  : read again the project to have values because store.updateProjectGroup( projectGroup );
        // remove object data -> we don't display the project name in the build queue
        AbstractContinuumAction.setProject( context, projectDao.getProject( project.getId() ) );

        BuildDefinition defaultBuildDefinition = null;
        BuildDefinitionTemplate template = null;
        if ( projectBuilderId.equals( MavenTwoContinuumProjectBuilder.ID ) )
        {
            template = buildDefinitionService.getDefaultMavenTwoBuildDefinitionTemplate();

            if ( template != null && template.getBuildDefinitions().size() > 0 )
            {
                defaultBuildDefinition = template.getBuildDefinitions().get( 0 );
            }
        }
        else if ( projectBuilderId.equals( MavenOneContinuumProjectBuilder.ID ) )
        {
            template = buildDefinitionService.getDefaultMavenOneBuildDefinitionTemplate();

            if ( template != null && template.getBuildDefinitions().size() > 0 )
            {
                defaultBuildDefinition = template.getBuildDefinitions().get( 0 );
            }
        }

        if ( defaultBuildDefinition == null )
        {
            // do not throw exception
            // project already added so might as well continue with the rest
            log.warn( "No default build definition found in the template. Project cannot be checked out." );
        }
        else
        {
            // used by BuildManager to determine on which build queue will the project be put
            AbstractContinuumAction.setBuildDefinition( context, defaultBuildDefinition );

            if ( !configurationService.isDistributedBuildEnabled() )
            {
                executeAction( "add-project-to-checkout-queue", context );
            }
        }
    }

    private ContinuumProjectBuildingResult executeAddProjectsFromMetadataActivity( String metadataUrl,
                                                                                   String projectBuilderId,
                                                                                   int projectGroupId,
                                                                                   boolean checkProtocol,
                                                                                   boolean useCredentialsCache,
                                                                                   boolean loadRecursiveProjects,
                                                                                   int buildDefinitionTemplateId,
                                                                                   boolean checkoutInSingleDirectory )
        throws ContinuumException
    {
        return executeAddProjectsFromMetadataActivity( metadataUrl, projectBuilderId, projectGroupId, checkProtocol,
                                                       useCredentialsCache, loadRecursiveProjects,
                                                       buildDefinitionTemplateId, true, checkoutInSingleDirectory );
    }

    // ----------------------------------------------------------------------
    // Notification
    // ----------------------------------------------------------------------

    // This whole section needs a scrub but will need to be dealt with generally
    // when we add schedules and profiles to the mix.

    public ProjectNotifier getNotifier( int projectId, int notifierId )
        throws ContinuumException
    {
        Project project = getProjectWithAllDetails( projectId );

        List<ProjectNotifier> notifiers = project.getNotifiers();

        ProjectNotifier notifier = null;

        for ( ProjectNotifier notif : notifiers )
        {
            notifier = notif;

            if ( notifier.getId() == notifierId )
            {
                break;
            }
        }

        return notifier;
    }

    public ProjectNotifier getGroupNotifier( int projectGroupId, int notifierId )
        throws ContinuumException
    {
        ProjectGroup projectGroup = getProjectGroupWithBuildDetails( projectGroupId );

        List<ProjectNotifier> notifiers = projectGroup.getNotifiers();

        ProjectNotifier notifier = null;

        for ( ProjectNotifier notif : notifiers )
        {
            notifier = notif;

            if ( notifier.getId() == notifierId )
            {
                break;
            }
        }

        return notifier;
    }

    public ProjectNotifier updateNotifier( int projectId, ProjectNotifier notifier )
        throws ContinuumException
    {
        Project project = getProjectWithAllDetails( projectId );

        ProjectNotifier notif = getNotifier( projectId, notifier.getId() );

        // I remove notifier then add it instead of update it due to a ClassCastException in jpox
        project.removeNotifier( notif );

        updateProject( project );

        return addNotifier( projectId, notifier );
    }

    public ProjectNotifier updateGroupNotifier( int projectGroupId, ProjectNotifier notifier )
        throws ContinuumException
    {
        ProjectGroup projectGroup = getProjectGroupWithBuildDetails( projectGroupId );

        ProjectNotifier notif = getGroupNotifier( projectGroupId, notifier.getId() );

        // I remove notifier then add it instead of update it due to a ClassCastException in jpox
        projectGroup.removeNotifier( notif );

        try
        {
            projectGroupDao.updateProjectGroup( projectGroup );
        }
        catch ( ContinuumStoreException cse )
        {
            throw new ContinuumException( "Unable to update project group.", cse );
        }

        return addGroupNotifier( projectGroupId, notifier );
    }

    public ProjectNotifier addNotifier( int projectId, ProjectNotifier notifier )
        throws ContinuumException
    {
        ProjectNotifier notif = new ProjectNotifier();

        notif.setSendOnSuccess( notifier.isSendOnSuccess() );

        notif.setSendOnFailure( notifier.isSendOnFailure() );

        notif.setSendOnError( notifier.isSendOnError() );

        notif.setSendOnWarning( notifier.isSendOnWarning() );

        notif.setSendOnScmFailure( notifier.isSendOnScmFailure() );

        notif.setConfiguration( notifier.getConfiguration() );

        notif.setType( notifier.getType() );

        notif.setFrom( ProjectNotifier.FROM_USER );

        Project project = getProjectWithAllDetails( projectId );

        project.addNotifier( notif );

        updateProject( project );

        return notif;
    }

    public ProjectNotifier addGroupNotifier( int projectGroupId, ProjectNotifier notifier )
        throws ContinuumException
    {
        ProjectNotifier notif = new ProjectNotifier();

        notif.setSendOnSuccess( notifier.isSendOnSuccess() );

        notif.setSendOnFailure( notifier.isSendOnFailure() );

        notif.setSendOnError( notifier.isSendOnError() );

        notif.setSendOnWarning( notifier.isSendOnWarning() );

        notif.setSendOnScmFailure( notifier.isSendOnScmFailure() );

        notif.setConfiguration( notifier.getConfiguration() );

        notif.setType( notifier.getType() );

        notif.setFrom( ProjectNotifier.FROM_USER );

        ProjectGroup projectGroup = getProjectGroupWithBuildDetails( projectGroupId );

        projectGroup.addNotifier( notif );
        try
        {
            projectGroupDao.updateProjectGroup( projectGroup );
        }
        catch ( ContinuumStoreException cse )
        {
            throw new ContinuumException( "unable to add notifier to project group", cse );
        }

        return notif;
    }

    public void removeNotifier( int projectId, int notifierId )
        throws ContinuumException
    {
        Project project = getProjectWithAllDetails( projectId );

        ProjectNotifier n = getNotifier( projectId, notifierId );

        if ( n != null )
        {
            if ( n.isFromProject() )
            {
                n.setEnabled( false );

                storeNotifier( n );
            }
            else
            {
                project.removeNotifier( n );

                updateProject( project );
            }
        }
    }

    public void removeGroupNotifier( int projectGroupId, int notifierId )
        throws ContinuumException
    {
        ProjectGroup projectGroup = getProjectGroupWithBuildDetails( projectGroupId );

        ProjectNotifier n = getGroupNotifier( projectGroupId, notifierId );

        if ( n != null )
        {
            if ( n.isFromProject() )
            {
                n.setEnabled( false );

                storeNotifier( n );
            }
            else
            {
                projectGroup.removeNotifier( n );

                try
                {
                    projectGroupDao.updateProjectGroup( projectGroup );
                }
                catch ( ContinuumStoreException cse )
                {
                    throw new ContinuumException( "Unable to remove notifer from project group.", cse );
                }
            }
        }
    }

    // ----------------------------------------------------------------------
    // Build Definition
    // ----------------------------------------------------------------------

    public List<BuildDefinition> getBuildDefinitions( int projectId )
        throws ContinuumException
    {
        Project project = getProjectWithAllDetails( projectId );

        return project.getBuildDefinitions();
    }

    public BuildDefinition getBuildDefinition( int projectId, int buildDefinitionId )
        throws ContinuumException
    {
        List<BuildDefinition> buildDefinitions = getBuildDefinitions( projectId );

        BuildDefinition buildDefinition = null;

        for ( BuildDefinition bd : buildDefinitions )
        {
            if ( bd.getId() == buildDefinitionId )
            {
                buildDefinition = bd;
                break;
            }
        }

        return buildDefinition;
    }

    public BuildDefinition getDefaultBuildDefinition( int projectId )
        throws ContinuumException
    {
        try
        {
            return buildDefinitionDao.getDefaultBuildDefinition( projectId );
        }
        catch ( ContinuumObjectNotFoundException cne )
        {
            throw new ContinuumException( "no default build definition for project", cne );
        }
        catch ( ContinuumStoreException cse )
        {
            throw new ContinuumException(
                "error attempting to access default build definition for project " + projectId, cse );
        }
    }

    public List<BuildDefinition> getDefaultBuildDefinitionsForProjectGroup( int projectGroupId )
        throws ContinuumException
    {
        try
        {
            return buildDefinitionDao.getDefaultBuildDefinitionsForProjectGroup( projectGroupId );
        }
        catch ( ContinuumObjectNotFoundException cne )
        {
            throw new ContinuumException( "Project Group (id=" + projectGroupId +
                                              ") doesn't have a default build definition, this should be impossible, it should always have a default definition set." );
        }
        catch ( ContinuumStoreException cse )
        {
            throw new ContinuumException( "Project Group (id=" + projectGroupId +
                                              ") doesn't have a default build definition, this should be impossible, it should always have a default definition set." );
        }
    }

    public BuildDefinition getBuildDefinition( int buildDefinitionId )
        throws ContinuumException
    {
        try
        {
            return buildDefinitionDao.getBuildDefinition( buildDefinitionId );
        }
        catch ( ContinuumObjectNotFoundException cne )
        {
            throw new ContinuumException( "no build definition found", cne );
        }
        catch ( ContinuumStoreException cse )
        {
            throw new ContinuumException( "error attempting to access build definition", cse );
        }
    }

    public List<BuildDefinition> getBuildDefinitionsForProject( int projectId )
        throws ContinuumException
    {
        Project project = getProjectWithAllDetails( projectId );

        return project.getBuildDefinitions();
    }

    public List<BuildDefinition> getBuildDefinitionsForProjectGroup( int projectGroupId )
        throws ContinuumException
    {

        ProjectGroup projectGroup = getProjectGroupWithBuildDetails( projectGroupId );

        return projectGroup.getBuildDefinitions();
    }

    public BuildDefinition addBuildDefinitionToProject( int projectId, BuildDefinition buildDefinition )
        throws ContinuumException
    {
        HashMap<String, Object> context = new HashMap<String, Object>();
        Schedule schedule = buildDefinition.getSchedule();

        AbstractContinuumAction.setBuildDefinition( context, buildDefinition );
        AbstractContinuumAction.setProjectId( context, projectId );

        executeAction( "add-build-definition-to-project", context );

        activeBuildDefinitionSchedule( schedule );

        return AbstractContinuumAction.getBuildDefinition( context );
    }

    public void removeBuildDefinitionFromProject( int projectId, int buildDefinitionId )
        throws ContinuumException
    {
        HashMap<String, Object> context = new HashMap<String, Object>();
        BuildDefinition buildDefinition = getBuildDefinition( buildDefinitionId );

        AbstractContinuumAction.setBuildDefinition( context, buildDefinition );
        AbstractContinuumAction.setProjectId( context, projectId );

        executeAction( "remove-build-definition-from-project", context );
    }

    public BuildDefinition updateBuildDefinitionForProject( int projectId, BuildDefinition buildDefinition )
        throws ContinuumException
    {
        HashMap<String, Object> context = new HashMap<String, Object>();
        Schedule schedule = buildDefinition.getSchedule();

        AbstractContinuumAction.setBuildDefinition( context, buildDefinition );
        AbstractContinuumAction.setProjectId( context, projectId );

        executeAction( "update-build-definition-from-project", context );

        activeBuildDefinitionSchedule( schedule );

        return AbstractContinuumAction.getBuildDefinition( context );
    }

    public BuildDefinition addBuildDefinitionToProjectGroup( int projectGroupId, BuildDefinition buildDefinition )
        throws ContinuumException
    {
        HashMap<String, Object> context = new HashMap<String, Object>();
        Schedule schedule = buildDefinition.getSchedule();

        AbstractContinuumAction.setBuildDefinition( context, buildDefinition );
        AbstractContinuumAction.setProjectGroupId( context, projectGroupId );

        executeAction( "add-build-definition-to-project-group", context );

        activeBuildDefinitionSchedule( schedule );

        return AbstractContinuumAction.getBuildDefinition( context );
    }

    public void removeBuildDefinitionFromProjectGroup( int projectGroupId, int buildDefinitionId )
        throws ContinuumException
    {
        HashMap<String, Object> context = new HashMap<String, Object>();

        AbstractContinuumAction.setBuildDefinition( context, getBuildDefinition( buildDefinitionId ) );
        AbstractContinuumAction.setProjectGroupId( context, projectGroupId );

        executeAction( "remove-build-definition-from-project-group", context );
    }

    public BuildDefinition updateBuildDefinitionForProjectGroup( int projectGroupId, BuildDefinition buildDefinition )
        throws ContinuumException
    {
        HashMap<String, Object> context = new HashMap<String, Object>();
        Schedule schedule = buildDefinition.getSchedule();

        AbstractContinuumAction.setBuildDefinition( context, buildDefinition );
        AbstractContinuumAction.setProjectGroupId( context, projectGroupId );

        executeAction( "update-build-definition-from-project-group", context );

        activeBuildDefinitionSchedule( schedule );

        return AbstractContinuumAction.getBuildDefinition( context );
    }

    public void removeBuildDefinition( int projectId, int buildDefinitionId )
        throws ContinuumException
    {
        Project project = getProjectWithAllDetails( projectId );

        BuildDefinition buildDefinition = getBuildDefinition( projectId, buildDefinitionId );

        if ( buildDefinition != null )
        {
            project.removeBuildDefinition( buildDefinition );

            updateProject( project );
        }
    }

    // ----------------------------------------------------------------------
    // Schedule
    // ----------------------------------------------------------------------

    public Schedule getSchedule( int scheduleId )
        throws ContinuumException
    {
        try
        {
            return scheduleDao.getSchedule( scheduleId );
        }
        catch ( Exception ex )
        {
            throw logAndCreateException( "Error while getting schedule.", ex );
        }
    }

    public Schedule getScheduleByName( String scheduleName )
        throws ContinuumException
    {
        try
        {
            return scheduleDao.getScheduleByName( scheduleName );
        }
        catch ( ContinuumStoreException e )
        {
            throw logAndCreateException( "Error while accessing the store.", e );
        }
    }

    public Collection<Schedule> getSchedules()
        throws ContinuumException
    {
        return scheduleDao.getAllSchedulesByName();
    }

    public void addSchedule( Schedule schedule )
        throws ContinuumException
    {
        Schedule s;

        s = getScheduleByName( schedule.getName() );

        if ( s != null )
        {
            throw logAndCreateException( "Can't create schedule. A schedule with the same name already exists.", null );
        }

        s = scheduleDao.addSchedule( schedule );

        try
        {
            schedulesActivator.activateSchedule( s, this );
        }
        catch ( SchedulesActivationException e )
        {
            throw new ContinuumException( "Error activating schedule " + s.getName() + ".", e );
        }
    }

    public void updateSchedule( Schedule schedule )
        throws ContinuumException
    {
        updateSchedule( schedule, true );
    }

    private void updateSchedule( Schedule schedule, boolean updateScheduler )
        throws ContinuumException
    {

        Schedule old = getSchedule( schedule.getId() );

        storeSchedule( schedule );

        if ( updateScheduler )
        {
            try
            {
                if ( schedule.isActive() )
                {
                    // I unactivate old shcedule (could change name) before if it's already active
                    schedulesActivator.unactivateSchedule( old, this );

                    schedulesActivator.activateSchedule( schedule, this );
                }
                else
                {
                    // Unactivate old because could change name in new schedule
                    schedulesActivator.unactivateSchedule( old, this );
                }
            }
            catch ( SchedulesActivationException e )
            {
                log.error( "Can't unactivate schedule. You need to restart Continuum.", e );
            }
        }
    }

    public void updateSchedule( int scheduleId, Map<String, String> configuration )
        throws ContinuumException
    {
        Schedule schedule = getSchedule( scheduleId );

        schedule.setName( configuration.get( "schedule.name" ) );

        schedule.setDescription( configuration.get( "schedule.description" ) );

        schedule.setCronExpression( configuration.get( "schedule.cronExpression" ) );

        schedule.setDelay( Integer.parseInt( configuration.get( "schedule.delay" ) ) );

        schedule.setActive( Boolean.valueOf( configuration.get( "schedule.active" ) ) );

        updateSchedule( schedule, true );
    }

    public void removeSchedule( int scheduleId )
        throws ContinuumException
    {
        Schedule schedule = getSchedule( scheduleId );

        try
        {
            schedulesActivator.unactivateSchedule( schedule, this );
        }
        catch ( SchedulesActivationException e )
        {
            log.error( "Can't unactivate the schedule. You need to restart Continuum.", e );
        }

        try
        {
            scheduleDao.removeSchedule( schedule );
        }
        catch ( Exception e )
        {
            log.error( "Can't remove the schedule.", e );

            try
            {
                schedulesActivator.activateSchedule( schedule, this );
            }
            catch ( SchedulesActivationException sae )
            {
                log.error( "Can't reactivate the schedule. You need to restart Continuum.", e );
            }
            throw new ContinuumException( "Can't remove the schedule", e );
        }
    }

    private Schedule storeSchedule( Schedule schedule )
        throws ContinuumException
    {
        try
        {
            return scheduleDao.storeSchedule( schedule );
        }
        catch ( ContinuumStoreException ex )
        {
            throw logAndCreateException( "Error while storing schedule.", ex );
        }
    }

    public void activePurgeSchedule( Schedule schedule )
    {
        try
        {
            schedulesActivator.activatePurgeSchedule( schedule, this );
        }
        catch ( SchedulesActivationException e )
        {
            log.error( "Can't activate schedule for purgeConfiguration" );
        }
    }

    public void activeBuildDefinitionSchedule( Schedule schedule )
    {
        try
        {
            schedulesActivator.activateBuildSchedule( schedule, this );
        }
        catch ( SchedulesActivationException e )
        {
            log.error( "Can't activate schedule for buildDefinition" );
        }
    }
    // ----------------------------------------------------------------------
    // Working copy
    // ----------------------------------------------------------------------

    public File getWorkingDirectory( int projectId )
        throws ContinuumException
    {
        try
        {
            return workingDirectoryService.getWorkingDirectory( projectDao.getProject( projectId ) );
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumException( "Can't get files list.", e );
        }
    }

    public String getFileContent( int projectId, String directory, String filename )
        throws ContinuumException
    {
        String relativePath = "\\.\\./"; // prevent users from using relative paths.
        Pattern pattern = Pattern.compile( relativePath );
        Matcher matcher = pattern.matcher( directory );
        String filteredDirectory = matcher.replaceAll( "" );

        matcher = pattern.matcher( filename );
        String filteredFilename = matcher.replaceAll( "" );

        File workingDirectory = getWorkingDirectory( projectId );

        File fileDirectory = new File( workingDirectory, filteredDirectory );

        File userFile = new File( fileDirectory, filteredFilename );

        try
        {
            return fsManager.fileContents( userFile );
        }
        catch ( IOException e )
        {
            throw new ContinuumException( "Can't read file " + filename, e );
        }
    }

    public List<File> getFiles( int projectId, String userDirectory )
        throws ContinuumException
    {
        File workingDirectory = getWorkingDirectory( projectId );

        return getFiles( workingDirectory, null, userDirectory );
    }

    private List<File> getFiles( File baseDirectory, String currentSubDirectory, String userDirectory )
    {
        List<File> dirs = new ArrayList<File>();

        File workingDirectory;

        if ( currentSubDirectory != null )
        {
            workingDirectory = new File( baseDirectory, currentSubDirectory );
        }
        else
        {
            workingDirectory = baseDirectory;
        }

        String[] files = workingDirectory.list();
        Arrays.sort( files, String.CASE_INSENSITIVE_ORDER );

        if ( files != null )
        {
            for ( String file : files )
            {
                File current = new File( workingDirectory, file );

                String currentFile;

                if ( currentSubDirectory == null )
                {
                    currentFile = file;
                }
                else
                {
                    currentFile = currentSubDirectory + "/" + file;
                }

                if ( userDirectory != null && current.isDirectory() && userDirectory.startsWith( currentFile ) )
                {
                    dirs.add( current );

                    dirs.addAll( getFiles( baseDirectory, currentFile, userDirectory ) );
                }
                else
                {
                    dirs.add( current );
                }
            }
        }

        return dirs;
    }

    // ----------------------------------------------------------------------
    // Configuration
    // ----------------------------------------------------------------------

    public ConfigurationService getConfiguration()
    {
        return configurationService;
    }

    public void reloadConfiguration()
        throws ContinuumException
    {
        try
        {
            configurationService.reload();
        }
        catch ( Exception e )
        {
            throw new ContinuumException( "Can't reload configuration.", e );
        }
    }

    // ----------------------------------------------------------------------
    // Lifecycle Management
    // ----------------------------------------------------------------------

    public void initialize()
        throws InitializationException
    {
        log.info( "Initializing Continuum." );

        log.info( "Showing all groups:" );
        try
        {
            for ( ProjectGroup group : projectGroupDao.getAllProjectGroups() )
            {
                createProjectScmRootForProjectGroup( group );
            }
        }
        catch ( ContinuumException e )
        {
            throw new InitializationException( "Error while creating project scm root for the project group", e );
        }

        log.info( "Showing all projects: " );

        for ( Project project : projectDao.getAllProjectsByNameWithBuildDetails() )
        {
            for ( ProjectNotifier notifier : (List<ProjectNotifier>) project.getNotifiers() )
            {
                if ( StringUtils.isEmpty( notifier.getType() ) )
                {
                    try
                    {
                        removeNotifier( project.getId(), notifier.getId() );
                    }
                    catch ( ContinuumException e )
                    {
                        throw new InitializationException( "Database is corrupted.", e );
                    }
                }
            }

            if ( project.getState() != ContinuumProjectState.NEW &&
                project.getState() != ContinuumProjectState.CHECKEDOUT &&
                project.getState() != ContinuumProjectState.OK && project.getState() != ContinuumProjectState.FAILED &&
                project.getState() != ContinuumProjectState.ERROR )
            {
                int state = project.getState();

                project.setState( project.getOldState() );

                project.setOldState( 0 );

                try
                {
                    log.info( "Fix project state for project " + project.getId() + ":" + project.getName() + ":" +
                                  project.getVersion() );

                    projectDao.updateProject( project );

                    Project p = projectDao.getProject( project.getId() );

                    if ( state == p.getState() )
                    {
                        log.info( "Can't fix the project state." );
                    }
                }
                catch ( ContinuumStoreException e )
                {
                    throw new InitializationException( "Database is corrupted.", e );
                }
            }

            log.info( " " + project.getId() + ":" + project.getName() + ":" + project.getVersion() + ":" +
                          project.getExecutorId() );
        }

        for ( ProjectScmRoot projectScmRoot : projectScmRootDao.getAllProjectScmRoots() )
        {
            if ( projectScmRoot.getState() == ContinuumProjectState.UPDATING )
            {
                projectScmRoot.setState( projectScmRoot.getOldState() );

                projectScmRoot.setOldState( 0 );

                try
                {
                    log.info( "Fix state for projectScmRoot " + projectScmRoot.getScmRootAddress() );

                    projectScmRootDao.updateProjectScmRoot( projectScmRoot );
                }
                catch ( ContinuumStoreException e )
                {
                    throw new InitializationException( "Database is corrupted.", e );
                }
            }
        }
    }

    // --------------------------------
    //  Plexus Lifecycle
    // --------------------------------
    public void start()
        throws StartingException
    {
        startMessage();

        try
        {
            initializer.initialize();

            configurationService.reload();
        }
        catch ( ConfigurationLoadingException e )
        {
            throw new StartingException( "Error loading the Continuum configuration.", e );
        }
        catch ( ContinuumConfigurationException e )
        {
            throw new StartingException( "Error loading the Continuum configuration.", e );
        }
        catch ( ContinuumInitializationException e )
        {
            throw new StartingException( "Cannot initializing Continuum for the first time.", e );
        }

        try
        {
            // ----------------------------------------------------------------------
            // Activate all the schedules in the system
            // ----------------------------------------------------------------------
            schedulesActivator.activateSchedules( this );
        }
        catch ( SchedulesActivationException e )
        {
            // We don't throw an exception here, so users will can modify schedules in interface instead of database
            log.error( "Error activating schedules.", e );
        }
    }

    public void stop()
        throws StoppingException
    {
        stopContinuum();
    }

    private void closeStore()
    {
        if ( daoUtils != null )
        {
            daoUtils.closeStore();
        }
    }

    public void startup()
        throws ContinuumException
    {
        try
        {
            this.start();
        }
        catch ( StartingException e )
        {
            throw new ContinuumException( e.getMessage(), e );
        }
    }

    private void stopContinuum()
    {
        //TODO: Remove all projects from queues, stop scheduler and wait the end of current builds so build results will be ok
        if ( stopped )
        {
            return;
        }

        try
        {
            if ( configurationService != null )
            {
                configurationService.store();
            }
        }
        catch ( Exception e )
        {
            log.info( "Error storing the Continuum configuration.", e );
        }

        closeStore();

        stopMessage();

        stopped = true;
    }

    public long getNbBuildResultsForProject( int projectId )
    {
        return buildResultDao.getNbBuildResultsForProject( projectId );
    }

    public Collection<BuildResult> getBuildResultsForProject( int projectId )
        throws ContinuumException
    {
        return buildResultDao.getBuildResultsForProject( projectId );
    }

    // ----------------------------------------------------------------------
    // Workflow
    // ----------------------------------------------------------------------

    protected void executeAction( String actionName, Map<String, Object> context )
        throws ContinuumException
    {
        try
        {
            Action action = actionManager.lookup( actionName );

            action.execute( context );
        }
        catch ( ActionNotFoundException e )
        {
            e.printStackTrace();
            throw new ContinuumException( "Error while executing the action '" + actionName + "'.", e );
        }
        catch ( ContinuumException e )
        {
            throw e;
        }
        catch ( Exception e )
        {
            log.info( "exception", e );
            throw new ContinuumException( "Error while executing the action '" + actionName + "'.", e );
        }
    }

    // ----------------------------------------------------------------------
    // Logging
    // ----------------------------------------------------------------------

    private ContinuumException logAndCreateException( String message, Throwable cause )
    {
        if ( cause instanceof ContinuumObjectNotFoundException )
        {
            return new ContinuumException( "No such object.", cause );
        }

        log.error( message, cause );

        return new ContinuumException( message, cause );
    }

    // ----------------------------------------------------------------------
    // Build settings
    // ----------------------------------------------------------------------

    // core

    public void updateProject( Project project )
        throws ContinuumException
    {
        try
        {
            boolean removeWorkingDirectory = false;

            Project p = projectDao.getProject( project.getId() );
            ProjectScmRoot projectScmRoot = null;

            if ( !p.getScmUrl().equals( project.getScmUrl() ) )
            {
                removeWorkingDirectory = true;
                projectScmRoot = getProjectScmRootByProject( project.getId() );
            }

            if ( !p.getProjectGroup().equals( project.getProjectGroup() ) )
            {
                projectScmRoot = getProjectScmRootByProject( project.getId() );
            }

            if ( StringUtils.isEmpty( p.getScmTag() ) && !StringUtils.isEmpty( project.getScmTag() ) )
            {
                removeWorkingDirectory = true;
            }
            else if ( !StringUtils.isEmpty( p.getScmTag() ) && StringUtils.isEmpty( project.getScmTag() ) )
            {
                removeWorkingDirectory = true;
            }
            else if ( !StringUtils.isEmpty( p.getScmTag() ) && !p.getScmTag().equals( project.getScmTag() ) )
            {
                removeWorkingDirectory = true;
            }

            if ( removeWorkingDirectory )
            {
                File workingDirectory = getWorkingDirectory( project.getId() );

                fsManager.removeDir( workingDirectory );
            }

            if ( StringUtils.isEmpty( project.getScmTag() ) )
            {
                project.setScmTag( null );
            }

            projectDao.updateProject( project );

            if ( projectScmRoot != null )
            {
                updateProjectScmRoot( projectScmRoot, project );
            }
        }
        catch ( ContinuumStoreException ex )
        {
            throw logAndCreateException( "Error while updating project.", ex );
        }
        catch ( IOException ex )
        {
            throw logAndCreateException( "Error while updating project.", ex );
        }
    }

    public void updateProjectGroup( ProjectGroup projectGroup )
        throws ContinuumException
    {
        //CONTINUUM-1502
        projectGroup.setName( projectGroup.getName().trim() );
        try
        {
            projectGroupDao.updateProjectGroup( projectGroup );
        }
        catch ( ContinuumStoreException cse )
        {
            throw logAndCreateException( "Error while updating project group.", cse );
        }
    }

    private ProjectNotifier storeNotifier( ProjectNotifier notifier )
        throws ContinuumException
    {
        try
        {
            return notifierDao.storeNotifier( notifier );
        }
        catch ( ContinuumStoreException ex )
        {
            throw logAndCreateException( "Error while storing notifier.", ex );
        }
    }

    private String getWorkingDirectory()
    {
        return configurationService.getWorkingDirectory().getAbsolutePath();
    }

    public Project getProjectWithCheckoutResult( int projectId )
        throws ContinuumException
    {
        try
        {
            return projectDao.getProjectWithCheckoutResult( projectId );
        }
        catch ( ContinuumObjectNotFoundException e )
        {
            throw new ContinuumException( "Unable to find the requested project", e );
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumException( "Error retrieving the requested project", e );
        }
    }

    public Project getProjectWithAllDetails( int projectId )
        throws ContinuumException
    {
        try
        {
            return projectDao.getProjectWithAllDetails( projectId );
        }
        catch ( ContinuumObjectNotFoundException e )
        {
            throw new ContinuumException( "Unable to find the requested project", e );
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumException( "Error retrieving the requested project", e );
        }
    }

    public ProjectGroup getProjectGroupWithBuildDetails( int projectGroupId )
        throws ContinuumException
    {
        try
        {
            return projectGroupDao.getProjectGroupWithBuildDetailsByProjectGroupId( projectGroupId );
        }
        catch ( ContinuumObjectNotFoundException e )
        {
            throw new ContinuumException( "Unable to find the requested project", e );
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumException( "Error retrieving the requested project", e );
        }
    }

    public Project getProjectWithBuilds( int projectId )
        throws ContinuumException
    {
        try
        {
            return projectDao.getProjectWithBuilds( projectId );
        }
        catch ( ContinuumObjectNotFoundException e )
        {
            throw new ContinuumException( "Unable to find the requested project", e );
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumException( "Error retrieving the requested project", e );
        }
    }

    public List<ProjectGroup> getAllProjectGroupsWithBuildDetails()
    {
        return projectGroupDao.getAllProjectGroupsWithBuildDetails();
    }

    public Collection<Project> getProjectsInGroup( int projectGroupId )
        throws ContinuumException
    {
        try
        {
            return projectDao.getProjectsInGroup( projectGroupId );
        }
        catch ( ContinuumObjectNotFoundException e )
        {
            throw new ContinuumException( "Unable to find the requested project", e );
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumException( "Error retrieving the requested project", e );
        }
    }

    public Collection<Project> getProjectsInGroupWithDependencies( int projectGroupId )
        throws ContinuumException
    {
        try
        {
            return projectDao.getProjectsInGroupWithDependencies( projectGroupId );
        }
        catch ( ContinuumObjectNotFoundException e )
        {
            throw new ContinuumException( "Unable to find the requested project", e );
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumException( "Error retrieving the requested project", e );
        }
    }

    // ----------------------------------------------------------------------
    // Private Utilities
    // ----------------------------------------------------------------------

    private void startMessage()
    {
        log.info( "Starting Continuum." );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        String banner = StringUtils.repeat( "-", getVersion().length() );

        log.info( "" );
        log.info( "" );
        log.info( "< Continuum " + getVersion() + " started! >" );
        log.info( "-----------------------" + banner );
        log.info( "       \\   ^__^" );
        log.info( "        \\  (oo)\\_______" );
        log.info( "           (__)\\       )\\/\\" );
        log.info( "               ||----w |" );
        log.info( "               ||     ||" );
        log.info( "" );
        log.info( "" );
    }

    private void stopMessage()
    {
        // Yes dorothy, this can happen!
        if ( log != null )
        {
            log.info( "Stopping Continuum." );

            log.info( "Continuum stopped." );
        }
    }

    private String getVersion()
    {
        InputStream resourceAsStream = null;
        try
        {
            Properties properties = new Properties();

            String name = "META-INF/maven/org.apache.continuum/continuum-core/pom.properties";

            resourceAsStream = getClass().getClassLoader().getResourceAsStream( name );

            if ( resourceAsStream == null )
            {
                return "unknown";
            }

            properties.load( resourceAsStream );

            return properties.getProperty( "version", "unknown" );
        }
        catch ( IOException e )
        {
            return "unknown";
        }
        finally
        {
            if ( resourceAsStream != null )
            {
                IOUtil.close( resourceAsStream );
            }
        }
    }

    public InstallationService getInstallationService()
    {
        return installationService;
    }

    public ProfileService getProfileService()
    {
        return profileService;
    }

    public BuildDefinitionService getBuildDefinitionService()
    {
        return buildDefinitionService;
    }

    public ContinuumReleaseResult addContinuumReleaseResult( int projectId, String releaseId, String releaseType )
        throws ContinuumException
    {
        ReleaseResult result;
        String releaseBy = "";

        if ( getConfiguration().isDistributedBuildEnabled() )
        {
            try
            {
                result = (ReleaseResult) distributedReleaseManager.getReleaseResult( releaseId );
                PreparedRelease preparedRelease = distributedReleaseManager.getPreparedRelease( releaseId,
                                                                                                releaseType );
                if ( preparedRelease != null )
                {
                    releaseBy = preparedRelease.getReleaseBy();
                }
            }
            catch ( ContinuumReleaseException e )
            {
                throw new ContinuumException( "Failed to release project: " + projectId, e );
            }
            catch ( BuildAgentConfigurationException e )
            {
                throw new ContinuumException( "Failed to release project: " + projectId, e );
            }
        }
        else
        {
            result = (ReleaseResult) releaseManager.getReleaseResults().get( releaseId );
            ContinuumReleaseDescriptor descriptor =
                (ContinuumReleaseDescriptor) releaseManager.getPreparedReleases().get( releaseId );
            if ( descriptor != null )
            {
                releaseBy = descriptor.getReleaseBy();
            }
        }

        if ( result != null && getContinuumReleaseResult( projectId, releaseType, result.getStartTime(),
                                                          result.getEndTime() ) == null )
        {
            ContinuumReleaseResult releaseResult = createContinuumReleaseResult( projectId, releaseType, result,
                                                                                 releaseBy );
            return addContinuumReleaseResult( releaseResult );
        }

        return null;
    }

    private ContinuumReleaseResult createContinuumReleaseResult( int projectId, String releaseGoals,
                                                                 ReleaseResult result, String releaseBy )
        throws ContinuumException
    {
        ContinuumReleaseResult releaseResult = new ContinuumReleaseResult();
        releaseResult.setStartTime( result.getStartTime() );
        releaseResult.setEndTime( result.getEndTime() );
        releaseResult.setResultCode( result.getResultCode() );

        Project project = getProject( projectId );
        ProjectGroup projectGroup = project.getProjectGroup();
        releaseResult.setProjectGroup( projectGroup );
        releaseResult.setProject( project );
        releaseResult.setReleaseGoal( releaseGoals );
        releaseResult.setUsername( releaseBy );

        String releaseName = "releases-" + result.getStartTime();

        try
        {
            File logFile = getConfiguration().getReleaseOutputFile( projectGroup.getId(), releaseName );

            PrintWriter writer = new PrintWriter( new FileWriter( logFile ) );
            writer.write( result.getOutput() );
            writer.close();
        }
        catch ( ConfigurationException e )
        {
            throw new ContinuumException( e.getMessage(), e );
        }
        catch ( IOException e )
        {
            throw new ContinuumException( "Unable to write output to file", e );
        }

        return releaseResult;
    }

    public ContinuumReleaseResult addContinuumReleaseResult( ContinuumReleaseResult releaseResult )
        throws ContinuumException
    {
        try
        {
            return releaseResultDao.addContinuumReleaseResult( releaseResult );
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumException( "Error while adding continuumReleaseResult", e );
        }
    }

    public void removeContinuumReleaseResult( int releaseResultId )
        throws ContinuumException
    {
        ContinuumReleaseResult releaseResult = getContinuumReleaseResult( releaseResultId );

        try
        {
            releaseResultDao.removeContinuumReleaseResult( releaseResult );
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumException( "Error while deleting continuumReleaseResult: " + releaseResultId, e );
        }

        try
        {
            int projectGroupId = releaseResult.getProjectGroup().getId();

            String name = "releases-" + releaseResult.getStartTime();

            File releaseFile = getConfiguration().getReleaseOutputFile( projectGroupId, name );

            if ( releaseFile.exists() )
            {
                try
                {
                    fsManager.delete( releaseFile );
                }
                catch ( IOException e )
                {
                    throw new ContinuumException( "Can't delete " + releaseFile.getAbsolutePath(), e );
                }
            }
        }
        catch ( ConfigurationException e )
        {
            log.info( "skip error during cleanup release files " + e.getMessage(), e );
        }
    }

    public ContinuumReleaseResult getContinuumReleaseResult( int releaseResultId )
        throws ContinuumException
    {
        try
        {
            return releaseResultDao.getContinuumReleaseResult( releaseResultId );
        }
        catch ( ContinuumObjectNotFoundException e )
        {
            throw new ContinuumException( "No continuumReleaseResult found: " + releaseResultId );
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumException( "Error while retrieving continuumReleaseResult: " + releaseResultId, e );
        }
    }

    public List<ContinuumReleaseResult> getAllContinuumReleaseResults()
    {
        return releaseResultDao.getAllContinuumReleaseResults();
    }

    public List<ContinuumReleaseResult> getContinuumReleaseResultsByProjectGroup( int projectGroupId )
    {
        return releaseResultDao.getContinuumReleaseResultsByProjectGroup( projectGroupId );
    }

    public ContinuumReleaseResult getContinuumReleaseResult( int projectId, String releaseGoal, long startTime,
                                                             long endTime )
        throws ContinuumException
    {
        try
        {
            return releaseResultDao.getContinuumReleaseResult( projectId, releaseGoal, startTime, endTime );
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumException(
                "Error while retrieving continuumReleaseResult of projectId " + projectId + " with releaseGoal: " +
                    releaseGoal, e );
        }
    }

    public String getReleaseOutput( int releaseResultId )
        throws ContinuumException
    {
        ContinuumReleaseResult releaseResult = getContinuumReleaseResult( releaseResultId );

        ProjectGroup projectGroup = releaseResult.getProjectGroup();

        try
        {
            return configurationService.getReleaseOutput( projectGroup.getId(),
                                                          "releases-" + releaseResult.getStartTime() );
        }
        catch ( ConfigurationException e )
        {
            throw new ContinuumException( "Error while retrieving release output for release: " + releaseResultId );
        }
    }

    public List<ProjectScmRoot> getProjectScmRootByProjectGroup( int projectGroupId )
    {
        return projectScmRootDao.getProjectScmRootByProjectGroup( projectGroupId );
    }

    public ProjectScmRoot getProjectScmRoot( int projectScmRootId )
        throws ContinuumException
    {
        try
        {
            return projectScmRootDao.getProjectScmRoot( projectScmRootId );
        }
        catch ( ContinuumObjectNotFoundException e )
        {
            throw new ContinuumException( "No projectScmRoot found with the given id: " + projectScmRootId );
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumException( "Error while retrieving projectScmRoot ", e );
        }
    }

    public ProjectScmRoot getProjectScmRootByProject( int projectId )
        throws ContinuumException
    {
        Project project = getProject( projectId );
        ProjectGroup group = getProjectGroupByProjectId( projectId );

        List<ProjectScmRoot> scmRoots = getProjectScmRootByProjectGroup( group.getId() );

        for ( ProjectScmRoot scmRoot : scmRoots )
        {
            if ( project.getScmUrl() != null && project.getScmUrl().startsWith( scmRoot.getScmRootAddress() ) )
            {
                return scmRoot;
            }
        }
        return null;
    }

    public ProjectScmRoot getProjectScmRootByProjectGroupAndScmRootAddress( int projectGroupId, String scmRootAddress )
        throws ContinuumException
    {
        try
        {
            return projectScmRootDao.getProjectScmRootByProjectGroupAndScmRootAddress( projectGroupId, scmRootAddress );
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumException( "Error while retrieving project scm root for " + projectGroupId, e );
        }
    }

    private void removeProjectScmRoot( ProjectScmRoot projectScmRoot )
        throws ContinuumException
    {
        if ( projectScmRoot == null )
        {
            return;
        }

        //get all projects in the group
        ProjectGroup group = getProjectGroupWithProjects( projectScmRoot.getProjectGroup().getId() );

        List<Project> projects = group.getProjects();

        boolean found = false;
        for ( Project project : projects )
        {
            if ( project.getScmUrl() != null && project.getScmUrl().startsWith( projectScmRoot.getScmRootAddress() ) )
            {
                found = true;
                break;
            }
        }

        if ( !found )
        {
            log.info( "Removing project scm root '" + projectScmRoot.getScmRootAddress() + "'" );
            try
            {
                projectScmRootDao.removeProjectScmRoot( projectScmRoot );
            }
            catch ( ContinuumStoreException e )
            {
                log.error( "Failed to remove project scm root '" + projectScmRoot.getScmRootAddress() + "'", e );
                throw new ContinuumException(
                    "Error while removing project scm root '" + projectScmRoot.getScmRootAddress() + "'", e );
            }
        }
        else
        {
            log.info(
                "Project scm root '" + projectScmRoot.getScmRootAddress() + "' still has projects, not removing" );
        }
    }

    public BuildQueue addBuildQueue( BuildQueue buildQueue )
        throws ContinuumException
    {
        try
        {
            return buildQueueService.addBuildQueue( buildQueue );
        }
        catch ( BuildQueueServiceException e )
        {
            throw new ContinuumException( "Error adding build queue to the database.", e );
        }
    }

    public BuildQueue getBuildQueue( int buildQueueId )
        throws ContinuumException
    {
        try
        {
            return buildQueueService.getBuildQueue( buildQueueId );
        }
        catch ( BuildQueueServiceException e )
        {
            throw new ContinuumException( "Error retrieving build queue.", e );
        }
    }

    public BuildQueue getBuildQueueByName( String buildQueueName )
        throws ContinuumException
    {
        try
        {
            return buildQueueService.getBuildQueueByName( buildQueueName );
        }
        catch ( BuildQueueServiceException e )
        {
            throw new ContinuumException( "Error retrieving build queue.", e );
        }
    }

    public void removeBuildQueue( BuildQueue buildQueue )
        throws ContinuumException
    {
        try
        {
            buildQueueService.removeBuildQueue( buildQueue );
        }
        catch ( BuildQueueServiceException e )
        {
            throw new ContinuumException( "Error deleting build queue from database.", e );
        }
    }

    public BuildQueue storeBuildQueue( BuildQueue buildQueue )
        throws ContinuumException
    {
        try
        {
            return buildQueueService.updateBuildQueue( buildQueue );
        }
        catch ( BuildQueueServiceException e )
        {
            throw new ContinuumException( "Error updating build queue.", e );
        }
    }

    public List<BuildQueue> getAllBuildQueues()
        throws ContinuumException
    {
        try
        {
            return buildQueueService.getAllBuildQueues();
        }
        catch ( BuildQueueServiceException e )
        {
            throw new ContinuumException( "Error adding build queue.", e );
        }
    }

    private void prepareBuildProjects( Collection<Project> projects, List<BuildDefinition> bds,
                                       boolean checkDefaultBuildDefinitionForProject, BuildTrigger buildTrigger )
        throws ContinuumException, NoBuildAgentException, NoBuildAgentInGroupException
    {
        Map<ProjectScmRoot, Map<Integer, Integer>> map = new HashMap<ProjectScmRoot, Map<Integer, Integer>>();
        List<ProjectScmRoot> sortedScmRoot = new ArrayList<ProjectScmRoot>();

        boolean signalIgnored = false;

        for ( Project project : projects )
        {
            int projectId = project.getId();

            int buildDefId = -1;

            if ( bds != null )
            {
                for ( BuildDefinition bd : bds )
                {
                    if ( project.getExecutorId().equals( bd.getType() ) || ( StringUtils.isEmpty( bd.getType() ) &&
                        ContinuumBuildExecutorConstants.MAVEN_TWO_BUILD_EXECUTOR.equals( project.getExecutorId() ) ) )
                    {
                        buildDefId = bd.getId();
                        break;
                    }
                }
            }

            if ( checkDefaultBuildDefinitionForProject )
            {
                BuildDefinition projectDefaultBD = null;
                try
                {
                    projectDefaultBD = buildDefinitionDao.getDefaultBuildDefinitionForProject( projectId );
                }
                catch ( ContinuumObjectNotFoundException e )
                {
                    log.debug( e.getMessage() );
                }
                catch ( ContinuumStoreException e )
                {
                    log.debug( e.getMessage() );
                }

                if ( projectDefaultBD != null )
                {
                    buildDefId = projectDefaultBD.getId();
                    log.debug( "Project " + project.getId() +
                                   " has own default build definition, will use it instead of group's." );
                }
            }

            if ( buildDefId == -1 )
            {
                log.info( "Project " + projectId +
                              " don't have a default build definition defined in the project or project group, will not be included in group build." );
                continue;
            }

            try
            {
                assertBuildable( project.getId(), buildDefId );
            }
            catch ( BuildException be )
            {
                log.info( "project not queued for build preparation: {}", be.getLocalizedMessage() );
                signalIgnored = true;
                continue;
            }

            ProjectScmRoot scmRoot = getProjectScmRootByProject( projectId );

            Map<Integer, Integer> projectsAndBuildDefinitionsMap = map.get( scmRoot );

            if ( projectsAndBuildDefinitionsMap == null )
            {
                projectsAndBuildDefinitionsMap = new HashMap<Integer, Integer>();
            }

            projectsAndBuildDefinitionsMap.put( projectId, buildDefId );

            map.put( scmRoot, projectsAndBuildDefinitionsMap );

            if ( !sortedScmRoot.contains( scmRoot ) )
            {
                sortedScmRoot.add( scmRoot );
            }
        }

        prepareBuildProjects( map, buildTrigger, sortedScmRoot );

        if ( signalIgnored )
        {
            throw new BuildException( "some projects were not queued due to their current build state",
                                      "build.projects.someNotQueued" );
        }
    }

    private void prepareBuildProjects( Collection<Project> projects, int buildDefinitionId, BuildTrigger buildTrigger )
        throws ContinuumException, NoBuildAgentException, NoBuildAgentInGroupException
    {
        Map<ProjectScmRoot, Map<Integer, Integer>> map = new HashMap<ProjectScmRoot, Map<Integer, Integer>>();
        List<ProjectScmRoot> sortedScmRoot = new ArrayList<ProjectScmRoot>();

        boolean signalIgnored = false;

        for ( Project project : projects )
        {
            int projectId = project.getId();

            // check if project already in queue
            try
            {
                assertBuildable( projectId, buildDefinitionId );
            }
            catch ( BuildException be )
            {
                log.info( "project not queued for build preparation: {}", be.getLocalizedMessage() );
                signalIgnored = true;
                continue;
            }

            ProjectScmRoot scmRoot = getProjectScmRootByProject( projectId );

            Map<Integer, Integer> projectsAndBuildDefinitionsMap = map.get( scmRoot );

            if ( projectsAndBuildDefinitionsMap == null )
            {
                projectsAndBuildDefinitionsMap = new HashMap<Integer, Integer>();
            }

            projectsAndBuildDefinitionsMap.put( projectId, buildDefinitionId );

            map.put( scmRoot, projectsAndBuildDefinitionsMap );

            if ( !sortedScmRoot.contains( scmRoot ) )
            {
                sortedScmRoot.add( scmRoot );
            }
        }

        prepareBuildProjects( map, buildTrigger, sortedScmRoot );

        if ( signalIgnored )
        {
            throw new BuildException( "some projects were not queued due to their current build state",
                                      "build.projects.someNotQueued" );
        }
    }

    private void prepareBuildProjects( Map<ProjectScmRoot, Map<Integer, Integer>> map, BuildTrigger buildTrigger,
                                       List<ProjectScmRoot> scmRoots )
        throws ContinuumException, NoBuildAgentException, NoBuildAgentInGroupException
    {
        for ( ProjectScmRoot scmRoot : scmRoots )
        {
            prepareBuildProjects( map.get( scmRoot ), buildTrigger, scmRoot.getScmRootAddress(),
                                  scmRoot.getProjectGroup().getId(), scmRoot.getId(), scmRoots );
        }
    }

    private void prepareBuildProjects( Map<Integer, Integer> projectsBuildDefinitionsMap, BuildTrigger buildTrigger,
                                       String scmRootAddress, int projectGroupId, int scmRootId,
                                       List<ProjectScmRoot> scmRoots )
        throws ContinuumException, NoBuildAgentException, NoBuildAgentInGroupException
    {
        ProjectGroup group = getProjectGroup( projectGroupId );

        try
        {
            if ( configurationService.isDistributedBuildEnabled() )
            {
                distributedBuildManager.prepareBuildProjects( projectsBuildDefinitionsMap, buildTrigger, projectGroupId,
                                                              group.getName(), scmRootAddress, scmRootId, scmRoots );
            }
            else
            {
                parallelBuildsManager.prepareBuildProjects( projectsBuildDefinitionsMap, buildTrigger, projectGroupId,
                                                            group.getName(), scmRootAddress, scmRootId );
            }
        }
        catch ( BuildManagerException e )
        {
            throw logAndCreateException( "Error while creating enqueuing object.", e );
        }
    }

    private void createProjectScmRootForProjectGroup( ProjectGroup projectGroup )
        throws ContinuumException
    {
        List<Project> projectsList;

        projectsList = getProjectsInBuildOrder( projectDao.getProjectsWithDependenciesByGroupId(
            projectGroup.getId() ) );

        List<ProjectScmRoot> scmRoots = getProjectScmRootByProjectGroup( projectGroup.getId() );

        String url = "";

        for ( Project project : projectsList )
        {
            boolean found = false;

            if ( StringUtils.isEmpty( url ) || !project.getScmUrl().startsWith( url ) )
            {
                // this is a root project or the project is part of a flat multi module
                url = project.getScmUrl();
                //createProjectScmRoot( projectGroup, url );

                for ( ProjectScmRoot scmRoot : scmRoots )
                {
                    if ( url.startsWith( scmRoot.getScmRootAddress() ) )
                    {
                        found = true;
                    }
                }

                if ( !found )
                {
                    createProjectScmRoot( projectGroup, url );
                }
            }
        }
    }

    private ProjectScmRoot createProjectScmRoot( ProjectGroup projectGroup, String url )
        throws ContinuumException
    {
        if ( StringUtils.isEmpty( url ) )
        {
            return null;
        }

        try
        {
            ProjectScmRoot scmRoot = projectScmRootDao.getProjectScmRootByProjectGroupAndScmRootAddress(
                projectGroup.getId(), url );

            if ( scmRoot != null )
            {
                return null;
            }

            ProjectScmRoot projectScmRoot = new ProjectScmRoot();

            projectScmRoot.setProjectGroup( projectGroup );

            projectScmRoot.setScmRootAddress( url );

            return projectScmRootDao.addProjectScmRoot( projectScmRoot );
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumException( "Error while creating project scm root with scm root address:" + url );
        }
    }

    private void updateProjectScmRoot( ProjectScmRoot oldScmRoot, Project project )
        throws ContinuumException
    {
        try
        {
            removeProjectScmRoot( oldScmRoot );
            ProjectScmRoot scmRoot = projectScmRootDao.getProjectScmRootByProjectGroupAndScmRootAddress(
                project.getProjectGroup().getId(), project.getScmUrl() );
            if ( scmRoot == null )
            {
                ProjectScmRoot newScmRoot = new ProjectScmRoot();
                if ( project.getScmUrl().equals( oldScmRoot.getScmRootAddress() ) )
                {
                    BeanUtils.copyProperties( oldScmRoot, newScmRoot, new String[] { "id", "projectGroup" } );
                }
                else
                {
                    newScmRoot.setScmRootAddress( project.getScmUrl() );
                }
                newScmRoot.setProjectGroup( project.getProjectGroup() );
                projectScmRootDao.addProjectScmRoot( newScmRoot );
            }
        }
        catch ( ContinuumStoreException ex )
        {
            throw logAndCreateException( "Error while updating project.", ex );
        }
    }

    private boolean isProjectInReleaseStage( Project project )
        throws ContinuumException
    {
        String releaseId = project.getGroupId() + ":" + project.getArtifactId();
        try
        {
            return taskQueueManager.isProjectInReleaseStage( releaseId );
        }
        catch ( TaskQueueManagerException e )
        {
            throw new ContinuumException( "Error occurred while checking if project is currently being released.", e );
        }
    }

    private boolean isAnyProjectInGroupInReleaseStage( int projectGroupId )
        throws ContinuumException
    {
        Collection<Project> projects = getProjectsInGroup( projectGroupId );
        for ( Project project : projects )
        {
            if ( isProjectInReleaseStage( project ) )
            {
                throw new ContinuumException( "Cannot build project group. Project (id=" + project.getId() +
                                                  ") in group is currently in release stage." );
            }
        }
        return false;
    }

    private boolean isAnyProjectsInReleaseStage( List<Project> projects )
        throws ContinuumException
    {
        for ( Project project : projects )
        {
            if ( isProjectInReleaseStage( project ) )
            {
                return true;
            }
        }

        return false;
    }

    private Collection<Project> getProjectsNotInReleaseStage( Collection<Project> projectsList )
        throws ContinuumException
    {
        // filter the projects to be built
        // projects that are in the release stage will not be built
        Collection<Project> filteredProjectsList = new ArrayList<Project>();
        for ( Project project : projectsList )
        {
            if ( !isProjectInReleaseStage( project ) )
            {
                filteredProjectsList.add( project );
            }
            else
            {
                log.warn(
                    "Project (id=" + project.getId() + ") will not be built. It is currently in the release stage." );
            }
        }
        return filteredProjectsList;
    }

    private void checkForDuplicateProjectInGroup( ProjectGroup projectGroup, Project projectToCheck,
                                                  ContinuumProjectBuildingResult result )
    {
        List<Project> projectsInGroup = projectGroup.getProjects();

        if ( projectsInGroup == null )
        {
            return;
        }

        for ( Project project : projectGroup.getProjects() )
        {
            // projectToCheck is first in the equals check, as projectToCheck must be a Maven project and will have
            // non-null values for each. project may be an Ant or Shell project and have null values.
            if ( projectToCheck.getGroupId().equals( project.getGroupId() ) && projectToCheck.getArtifactId().equals(
                project.getArtifactId() ) && projectToCheck.getVersion().equals( project.getVersion() ) )
            {
                result.addError( ContinuumProjectBuildingResult.ERROR_DUPLICATE_PROJECTS );
                return;
            }
        }
    }

    private void assertBuildable( int projectId, int buildDefinitionId )
        throws ContinuumException
    {
        if ( configurationService.isDistributedBuildEnabled() )
        {
            if ( distributedBuildManager.isProjectInAnyPrepareBuildQueue( projectId, buildDefinitionId )
                || distributedBuildManager.isProjectInAnyBuildQueue( projectId, buildDefinitionId ) )
            {
                throw new BuildException( "project is already queued", "build.project.alreadyQueued" );
            }

            if ( distributedBuildManager.isProjectCurrentlyPreparingBuild( projectId, buildDefinitionId )
                || distributedBuildManager.isProjectCurrentlyBuilding( projectId, buildDefinitionId ) )
            {
                throw new BuildException( "project is already building", "build.project.alreadyBuilding" );
            }
        }
        else
        {
            try
            {
                if ( parallelBuildsManager.isInAnyBuildQueue( projectId, buildDefinitionId )
                    || parallelBuildsManager.isInAnyCheckoutQueue( projectId )
                    || parallelBuildsManager.isInPrepareBuildQueue( projectId ) )
                {
                    throw new BuildException( "project is already queued", "build.project.alreadyQueued" );
                }

                if ( parallelBuildsManager.isProjectCurrentlyPreparingBuild( projectId )
                    || parallelBuildsManager.isProjectInAnyCurrentBuild( projectId ) )
                {
                    throw new BuildException( "project is already building", "build.project.alreadyBuilding" );
                }
            }
            catch ( BuildManagerException e )
            {
                throw new ContinuumException( e.getMessage(), e );
            }
        }
    }

    void setTaskQueueManager( TaskQueueManager taskQueueManager )
    {
        this.taskQueueManager = taskQueueManager;
    }

    void setProjectDao( ProjectDao projectDao )
    {
        this.projectDao = projectDao;
    }

    public DistributedBuildManager getDistributedBuildManager()
    {
        return distributedBuildManager;
    }
}
