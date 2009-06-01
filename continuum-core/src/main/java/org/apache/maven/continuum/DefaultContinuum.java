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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.continuum.builder.distributed.manager.DistributedBuildManager;
import org.apache.continuum.buildmanager.BuildManagerException;
import org.apache.continuum.buildmanager.BuildsManager;
import org.apache.continuum.buildqueue.BuildQueueService;
import org.apache.continuum.buildqueue.BuildQueueServiceException;
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
import org.apache.continuum.release.distributed.manager.DistributedReleaseManager;
import org.apache.continuum.repository.RepositoryService;
import org.apache.continuum.taskqueue.manager.TaskQueueManager;
import org.apache.continuum.taskqueue.manager.TaskQueueManagerException;
import org.apache.continuum.utils.ProjectSorter;
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
import org.apache.maven.continuum.release.ContinuumReleaseManager;
import org.apache.maven.continuum.store.ContinuumObjectNotFoundException;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.apache.maven.continuum.utils.ContinuumUrlValidator;
import org.apache.maven.continuum.utils.WorkingDirectoryService;
import org.codehaus.plexus.action.Action;
import org.codehaus.plexus.action.ActionManager;
import org.codehaus.plexus.action.ActionNotFoundException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l </a>
 * @version $Id$
 * @plexus.component role="org.apache.maven.continuum.Continuum" role-hint="default"
 */
public class DefaultContinuum
    implements Continuum, Initializable, Startable
{
    private static final Logger log = LoggerFactory.getLogger( DefaultContinuum.class );

    /**
     * @plexus.requirement
     */
    private ActionManager actionManager;

    /**
     * @plexus.requirement
     */
    private ConfigurationService configurationService;

    /**
     * @plexus.requirement
     */
    private DaoUtils daoUtils;

    /**
     * @plexus.requirement
     */
    private BuildDefinitionDao buildDefinitionDao;

    /**
     * @plexus.requirement
     */
    private BuildResultDao buildResultDao;

    /**
     * @plexus.requirement
     */
    private NotifierDao notifierDao;

    /**
     * @plexus.requirement
     */
    private ProjectDao projectDao;

    /**
     * @plexus.requirement
     */
    private ProjectGroupDao projectGroupDao;

    /**
     * @plexus.requirement
     */
    private ScheduleDao scheduleDao;

    /**
     * @plexus.requirement
     */
    private ContinuumReleaseResultDao releaseResultDao;

    /**
     * @plexus.requirement
     */
    private ProjectScmRootDao projectScmRootDao;

    /**
     * @plexus.requirement
     */
    private ContinuumInitializer initializer;

    /**
     * @plexus.requirement
     */
    private SchedulesActivator schedulesActivator;

    /**
     * @plexus.requirement
     */
    private InstallationService installationService;

    /**
     * @plexus.requirement
     */
    private ProfileService profileService;

    /**
     * @plexus.requirement
     */
    private BuildDefinitionService buildDefinitionService;

    // ----------------------------------------------------------------------
    // Moved from core
    // ----------------------------------------------------------------------

    /**
     * @plexus.requirement
     */
    private ContinuumReleaseManager releaseManager;

    /**
     * @plexus.requirement
     */
    private WorkingDirectoryService workingDirectoryService;

    /**
     * @plexus.requirement
     */
    private BuildExecutorManager executorManager;

    /**
     * @plexus.requirement role-hint="continuumUrl"
     */
    private ContinuumUrlValidator urlValidator;

    private boolean stopped = false;

    /**
     * @plexus.requirement
     */
    private ContinuumPurgeManager purgeManager;

    /**
     * @plexus.requirement
     */
    private RepositoryService repositoryService;

    /**
     * @plexus.requirement
     */
    private PurgeConfigurationService purgeConfigurationService;

    /**
     * @plexus.requirement
     */
    private TaskQueueManager taskQueueManager;

    /**
     * @plexus.requirement role-hint="parallel"
     */
    private BuildsManager parallelBuildsManager;

    /**
     * @plexus.requirement
     */
    private BuildQueueService buildQueueService;

    /**
     * @plexus.requirement
     */
    private DistributedBuildManager distributedBuildManager;

    /**
     * @plexus.requirement
     */
    private DistributedReleaseManager distributedReleaseManager;

    public DefaultContinuum()
    {
        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    stopContinuum();
                }
                catch ( StoppingException e )
                {
                    e.printStackTrace();
                }
            }
        } );
    }

    public ContinuumReleaseManager getReleaseManager()
    {
        return releaseManager;
    }

    public void setActionManager( ActionManager actionManager )
    {
        this.actionManager = actionManager;
    }

    public ActionManager getActionManager()
    {
        return actionManager;
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
                        "Unable to delete group. At least one project in group is still being checked out." );
                }

                if ( parallelBuildsManager.isAnyProjectCurrentlyBuilding( projectIds ) )
                {
                    throw new ContinuumException(
                        "Unable to delete group. At least one project in group is still building." );
                }

                if ( isAnyProjectsInReleaseStage( projects ) )
                {
                    throw new ContinuumException(
                        "Unable to delete group. At least one project in group is in release stage" );
                }
            }
            catch ( BuildManagerException e )
            {
                throw new ContinuumException( "Unable to delete group.", e );
            }

            for ( int projectId : projectIds )
            {
                removeProject( projectId );
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

    public Collection<Project> getProjects()
        throws ContinuumException
    {
        return projectDao.getAllProjectsByName();
    }

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

    public Map<Integer, BuildResult> getLatestBuildResults()
    {
        Map<Integer, BuildResult> result = buildResultDao.getLatestBuildResults();

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

    public Map<Integer, BuildResult> getBuildResultsInSuccess()
    {
        Map<Integer, BuildResult> result = buildResultDao.getBuildResultsInSuccess();

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

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public void removeProject( int projectId )
        throws ContinuumException
    {
        try
        {
            Project project = getProjectWithBuilds( projectId );

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

            List<ContinuumReleaseResult> releaseResults =
                releaseResultDao.getContinuumReleaseResultsByProject( projectId );

            ProjectScmRoot scmRoot = getProjectScmRootByProject( projectId );

            try
            {
                for ( ContinuumReleaseResult releaseResult : releaseResults )
                {
                    releaseResultDao.removeContinuumReleaseResult( releaseResult );
                }

                File releaseOutputDirectory =
                    configurationService.getReleaseOutputDirectory( project.getProjectGroup().getId() );

                if ( releaseOutputDirectory != null )
                {
                    FileUtils.deleteDirectory( releaseOutputDirectory );
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

            for ( Object o : project.getBuildResults() )
            {
                BuildResult br = (BuildResult) o;
                br.setBuildDefinition( null );
                //Remove all modified dependencies to prevent SQL errors
                br.setModifiedDependencies( null );
                buildResultDao.updateBuildResult( br );
                removeBuildResult( br );
            }

            File workingDirectory = getWorkingDirectory( projectId );

            FileUtils.deleteDirectory( workingDirectory );

            File buildOutputDirectory = configurationService.getBuildOutputDirectory( projectId );

            FileUtils.deleteDirectory( buildOutputDirectory );

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

    public Collection<Project> getAllProjects( int start, int end )
        throws ContinuumException
    {
        return projectDao.getAllProjectsByName();
    }

    public Map<Integer, ProjectGroupSummary> getProjectsSummaryByGroups()
    {
        return projectDao.getProjectsSummary();
    }

    // ----------------------------------------------------------------------
    // Building
    // ----------------------------------------------------------------------

    public void buildProjects()
        throws ContinuumException
    {
        buildProjects( ContinuumProjectState.TRIGGER_FORCED );
    }

    public void buildProjectsWithBuildDefinition( int buildDefinitionId )
        throws ContinuumException
    {
        buildProjects( ContinuumProjectState.TRIGGER_FORCED, buildDefinitionId );
    }

    public void buildProjectsWithBuildDefinition( List<Project> projects, List<BuildDefinition> bds )
        throws ContinuumException
    {
        Collection<Project> filteredProjectsList = getProjectsNotInReleaseStage( projects );

        prepareBuildProjects( filteredProjectsList, bds, true, ContinuumProjectState.TRIGGER_FORCED );
    }

    public void buildProjectsWithBuildDefinition( List<Project> projects, int buildDefinitionId )
        throws ContinuumException
    {
        Collection<Project> filteredProjectsList = getProjectsNotInReleaseStage( projects );

        prepareBuildProjects( filteredProjectsList, buildDefinitionId, ContinuumProjectState.TRIGGER_FORCED );
    }

    /**
     * fire of the builds of all projects across all project groups using their default build definitions
     *
     * @param trigger
     * @throws ContinuumException
     */
    public void buildProjects( int trigger )
        throws ContinuumException
    {
        Collection<Project> projectsList = getProjectsInBuildOrder();

        Collection<Project> filteredProjectsList = getProjectsNotInReleaseStage( projectsList );

        prepareBuildProjects( filteredProjectsList, null, true, trigger );
    }

    /**
     * fire of the builds of all projects across all project groups using the group build definition
     *
     * @param trigger
     * @param buildDefinitionId
     * @throws ContinuumException
     */
    public void buildProjects( int trigger, int buildDefinitionId )
        throws ContinuumException
    {
        Collection<Project> projectsList = getProjectsInBuildOrder();

        Collection<Project> filteredProjectsList = getProjectsNotInReleaseStage( projectsList );

        prepareBuildProjects( filteredProjectsList, buildDefinitionId, trigger );
    }

    /**
     * fire off a build for all of the projects in a project group using their default builds
     *
     * @param projectGroupId
     * @throws ContinuumException
     */
    public void buildProjectGroup( int projectGroupId )
        throws ContinuumException
    {
        List<BuildDefinition> groupDefaultBDs;

        if ( !isAnyProjectInGroupInReleaseStage( projectGroupId ) )
        {
            groupDefaultBDs = getDefaultBuildDefinitionsForProjectGroup( projectGroupId );

            buildProjectGroupWithBuildDefinition( projectGroupId, groupDefaultBDs, true );
        }
    }

    /**
     * fire off a build for all of the projects in a project group using their default builds.
     *
     * @param projectGroupId    the project group id
     * @param buildDefinitionId the build definition id to use
     * @throws ContinuumException
     */
    public void buildProjectGroupWithBuildDefinition( int projectGroupId, int buildDefinitionId )
        throws ContinuumException
    {
        if ( !isAnyProjectInGroupInReleaseStage( projectGroupId ) )
        {
            List<BuildDefinition> bds = new ArrayList<BuildDefinition>();
            BuildDefinition bd = getBuildDefinition( buildDefinitionId );
            if ( bd != null )
            {
                bds.add( bd );
            }
            buildProjectGroupWithBuildDefinition( projectGroupId, bds, false );
        }
    }

    /**
     * fire off a build for all of the projects in a project group using their default builds
     *
     * @param projectGroupId
     * @throws ContinuumException
     */
    private void buildProjectGroupWithBuildDefinition( int projectGroupId, List<BuildDefinition> bds,
                                                       boolean checkDefaultBuildDefinitionForProject )
        throws ContinuumException
    {
        if ( !isAnyProjectInGroupInReleaseStage( projectGroupId ) )
        {
            Collection<Project> projectsList;

            projectsList = getProjectsInBuildOrder( projectDao.getProjectsWithDependenciesByGroupId( projectGroupId ) );

            prepareBuildProjects( projectsList, bds, checkDefaultBuildDefinitionForProject,
                                  ContinuumProjectState.TRIGGER_FORCED );
        }
    }

    /**
     * takes a given schedule and determines which projects need to build
     * <p/>
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

            projectsList = getProjectsInBuildOrder();
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumException( "Can't get project list for schedule " + schedule.getName(), e );
        }

        Map<ProjectScmRoot, Map<Integer, Integer>> map = new HashMap<ProjectScmRoot, Map<Integer, Integer>>();
        List<ProjectScmRoot> sortedScmRoot = new ArrayList<ProjectScmRoot>();

        for ( Project project : projectsList )
        {
            List<Integer> buildDefIds = (List<Integer>) projectsMap.get( project.getId() );

            if ( buildDefIds != null && !buildDefIds.isEmpty() )
            {
                for ( Integer buildDefId : buildDefIds )
                {
                    try
                    {
                        if ( buildDefId != null &&
                            !parallelBuildsManager.isInAnyBuildQueue( project.getId(), buildDefId ) &&
                            !parallelBuildsManager.isInAnyCheckoutQueue( project.getId() ) &&
                            !parallelBuildsManager.isInPrepareBuildQueue( project.getId() ) )
                        {
                            ProjectScmRoot scmRoot = getProjectScmRootByProject( project.getId() );

                            Map<Integer, Integer> projectsAndBuildDefinitionsMap = map.get( scmRoot );

                            if ( projectsAndBuildDefinitionsMap == null )
                            {
                                projectsAndBuildDefinitionsMap = new HashMap<Integer, Integer>();
                            }

                            projectsAndBuildDefinitionsMap.put( project.getId(), buildDefId );

                            map.put( scmRoot, projectsAndBuildDefinitionsMap );

                            if ( !sortedScmRoot.contains( scmRoot ) )
                            {
                                sortedScmRoot.add( scmRoot );
                            }
                        }
                    }
                    catch ( BuildManagerException e )
                    {
                        throw new ContinuumException( e.getMessage(), e );
                    }
                }
            }
        }

        prepareBuildProjects( map, ContinuumProjectState.TRIGGER_SCHEDULED, sortedScmRoot );
    }

    public void buildProject( int projectId )
        throws ContinuumException
    {
        buildProject( projectId, ContinuumProjectState.TRIGGER_FORCED );
    }

    public void buildProjectWithBuildDefinition( int projectId, int buildDefinitionId )
        throws ContinuumException
    {
        buildProject( projectId, buildDefinitionId, ContinuumProjectState.TRIGGER_FORCED );
    }

    public void buildProject( int projectId, int trigger )
        throws ContinuumException
    {
        Project project = getProject( projectId );
        if ( isProjectInReleaseStage( project ) )
        {
            throw new ContinuumException( "Project (id=" + projectId + ") is currently in release stage." );
        }

        BuildDefinition buildDef = getDefaultBuildDefinition( projectId );

        if ( buildDef == null )
        {
            throw new ContinuumException( "Project (id=" + projectId + " doens't have a default build definition." );
        }

        try
        {
            if ( parallelBuildsManager.isInAnyBuildQueue( projectId, buildDef.getId() ) ||
                parallelBuildsManager.isInAnyCheckoutQueue( projectId ) ||
                parallelBuildsManager.isInPrepareBuildQueue( projectId ) )
            {
                return;
            }
        }
        catch ( BuildManagerException e )
        {
            throw new ContinuumException( e.getMessage(), e );
        }

        Map<Integer, Integer> projectsBuildDefinitionsMap = new HashMap<Integer, Integer>();
        projectsBuildDefinitionsMap.put( projectId, buildDef.getId() );

        ProjectScmRoot scmRoot = getProjectScmRootByProject( projectId );
        prepareBuildProjects( projectsBuildDefinitionsMap, trigger, scmRoot.getScmRootAddress(),
                              scmRoot.getProjectGroup().getId(), scmRoot.getId() );
    }

    public void buildProject( int projectId, int buildDefinitionId, int trigger )
        throws ContinuumException
    {
        Project project = getProject( projectId );
        if ( isProjectInReleaseStage( project ) )
        {
            throw new ContinuumException( "Project (id=" + projectId + ") is currently in release stage." );
        }

        try
        {
            if ( parallelBuildsManager.isInAnyBuildQueue( projectId, buildDefinitionId ) ||
                parallelBuildsManager.isInAnyCheckoutQueue( projectId ) ||
                parallelBuildsManager.isInPrepareBuildQueue( projectId ) )
            {
                return;
            }
        }
        catch ( BuildManagerException e )
        {
            throw new ContinuumException( e.getMessage(), e );
        }

        Map<Integer, Integer> projectsBuildDefinitionsMap = new HashMap<Integer, Integer>();
        projectsBuildDefinitionsMap.put( projectId, buildDefinitionId );

        ProjectScmRoot scmRoot = getProjectScmRootByProject( projectId );
        prepareBuildProjects( projectsBuildDefinitionsMap, trigger, scmRoot.getScmRootAddress(),
                              scmRoot.getProjectGroup().getId(), scmRoot.getId() );
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
                    "Unable to remove build result because it is currently being used by" + "a building project " +
                        project.getId() );
            }
        }
        catch ( BuildManagerException e )
        {
            throw new ContinuumException( e.getMessage(), e );
        }

        buildResult.setModifiedDependencies( null );
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
        throws ContinuumException
    {
        buildResultDao.removeBuildResult( buildResult );

        // cleanup some files
        try
        {
            File buildOutputDirectory = getConfiguration().getBuildOutputDirectory( buildResult.getProject().getId() );
            File buildDirectory = new File( buildOutputDirectory, Integer.toString( buildResult.getId() ) );

            if ( buildDirectory.exists() )
            {
                FileUtils.deleteDirectory( buildDirectory );
            }
            File buildOutputFile =
                getConfiguration().getBuildOutputFile( buildResult.getId(), buildResult.getProject().getId() );
            if ( buildOutputFile.exists() )
            {
                buildOutputFile.delete();
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

    public List<ChangeSet> getChangesSinceLastSuccess( int projectId, int buildResultId )
        throws ContinuumException
    {
        ArrayList<BuildResult> buildResults =
            new ArrayList<BuildResult>( buildResultDao.getBuildResultsForProject( projectId, 0 ) );

        Collections.reverse( buildResults );

        Iterator<BuildResult> buildResultsIterator = buildResults.iterator();

        boolean stop = false;

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

    public List<Project> getProjectsInBuildOrder()
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
                                                       buildDefinitionTemplateId );
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
                                                           buildDefinitionService.getDefaultMavenTwoBuildDefinitionTemplate().getId() );
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
                                                           buildDefinitionService.getDefaultMavenTwoBuildDefinitionTemplate().getId() );
        }
        catch ( BuildDefinitionServiceException e )
        {
            throw new ContinuumException( e.getMessage(), e );
        }
    }

    public ContinuumProjectBuildingResult addMavenTwoProject( String metadataUrl, int projectGroupId,
                                                              boolean checkProtocol, boolean useCredentialsCache,
                                                              boolean recursiveProjects, int buildDefinitionTemplateId )
        throws ContinuumException
    {
        return executeAddProjectsFromMetadataActivity( metadataUrl, MavenTwoContinuumProjectBuilder.ID, projectGroupId,
                                                       checkProtocol, useCredentialsCache, recursiveProjects,
                                                       buildDefinitionTemplateId );
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
        createProjectScmRoot( projectGroup, scmUrl );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        AbstractContinuumAction.setWorkingDirectory( context, getWorkingDirectory() );

        AbstractContinuumAction.setUnvalidatedProject( context, project );

        AbstractContinuumAction.setUnvalidatedProjectGroup( context, projectGroup );

        AbstractContinuumAction.setProjectGroupId( context, projectGroup.getId() );

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

            buildDefinitionService.addTemplateInProject( bdt.getId(), getProject(
                AbstractContinuumAction.getProjectId( context ) ) );
        }
        catch ( BuildDefinitionServiceException e )
        {
            throw new ContinuumException( e.getMessage(), e );
        }

        if ( !configurationService.isDistributedBuildEnabled() )
        {
            // used by BuildManager to determine on which build queue will the project be put
            BuildDefinition bd = (BuildDefinition) getProjectWithBuildDetails(
                AbstractContinuumAction.getProjectId( context ) ).getBuildDefinitions().get( 0 );
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
                                                       false, false, buildDefinitionTemplateId );
    }


    protected ContinuumProjectBuildingResult executeAddProjectsFromMetadataActivity( String metadataUrl,
                                                                                     String projectBuilderId,
                                                                                     int projectGroupId,
                                                                                     boolean checkProtocol,
                                                                                     boolean useCredentialsCache,
                                                                                     boolean loadRecursiveProjects,
                                                                                     int buildDefinitionTemplateId,
                                                                                     boolean addAssignableRoles )
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

        ProjectGroup projectGroup = result.getProjectGroups().iterator().next();

        ProjectScmRoot projectScmRoot;

        boolean projectGroupCreation = false;

        try
        {
            if ( projectGroupId == -1 )
            {
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

            String url = CreateProjectsFromMetadataAction.getUrl( context );

            projectScmRoot = getProjectScmRootByProjectGroupAndScmRootAddress( projectGroup.getId(), url );

            if ( projectScmRoot == null )
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

            for ( Project project : projects )
            {
                context = new HashMap<String, Object>();

                // CONTINUUM-1953 olamy : attached buildDefs from template here
                // if no group creation
                if ( !projectGroupCreation && buildDefinitionTemplateId > 0 )
                {
                    buildDefinitionService.addTemplateInProject( buildDefinitionTemplateId,
                                                                 projectDao.getProject( project.getId() ) );
                }

                AbstractContinuumAction.setUnvalidatedProject( context, project );
                //
                //            executeAction( "validate-project", context );
                //
                //            executeAction( "store-project", context );
                //
                AbstractContinuumAction.setProjectId( context, project.getId() );

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
                // FIXME
                // olamy  : read again the project to have values because store.updateProjectGroup( projectGroup );
                // remove object data -> we don't display the project name in the build queue
                AbstractContinuumAction.setProject( context, projectDao.getProject( project.getId() ) );

                BuildDefinition defaultBuildDefinition = null;
                if ( projectBuilderId.equals( MavenTwoContinuumProjectBuilder.ID ) )
                {
                    defaultBuildDefinition =
                        (BuildDefinition) buildDefinitionService.getDefaultMavenTwoBuildDefinitionTemplate().getBuildDefinitions().get(
                            0 );
                }
                else if ( projectBuilderId.equals( MavenOneContinuumProjectBuilder.ID ) )
                {
                    defaultBuildDefinition =
                        (BuildDefinition) buildDefinitionService.getDefaultMavenOneBuildDefinitionTemplate().getBuildDefinitions().get(
                            0 );
                }

                // used by BuildManager to determine on which build queue will the project be put
                AbstractContinuumAction.setBuildDefinition( context, defaultBuildDefinition );

                if ( !configurationService.isDistributedBuildEnabled() )
                {
                    executeAction( "add-project-to-checkout-queue", context );
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
        // add the relevent security administration roles for this project
        if ( addAssignableRoles )
        {
            executeAction( "add-assignable-roles", context );
        }
        return result;
    }

    protected ContinuumProjectBuildingResult executeAddProjectsFromMetadataActivity( String metadataUrl,
                                                                                     String projectBuilderId,
                                                                                     int projectGroupId,
                                                                                     boolean checkProtocol,
                                                                                     boolean useCredentialsCache,
                                                                                     boolean loadRecursiveProjects,
                                                                                     int buildDefinitionTemplateId )
        throws ContinuumException
    {
        return executeAddProjectsFromMetadataActivity( metadataUrl, projectBuilderId, projectGroupId, checkProtocol,
                                                       useCredentialsCache, loadRecursiveProjects,
                                                       buildDefinitionTemplateId, true );
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
                " doens't have a default build definition, this should be impossible, it should always have a default definition set." );
        }
        catch ( ContinuumStoreException cse )
        {
            throw new ContinuumException( "Project Group (id=" + projectGroupId +
                " doens't have a default build definition, this should be impossible, it should always have a default definition set." );
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
        Schedule schedule = buildDefinition.getSchedule();

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

    public void removeBuildDefinition( BuildDefinition buildDefinition )
        throws ContinuumException
    {
        try
        {
            buildDefinitionDao.removeBuildDefinition( buildDefinition );
        }
        catch ( ContinuumStoreException ex )
        {
            throw logAndCreateException( "Error while removing build definition.", ex );
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

    public Schedule storeSchedule( Schedule schedule )
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
            return FileUtils.fileRead( userFile );
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

    public void stopContinuum()
        throws StoppingException
    {
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

                FileUtils.deleteDirectory( workingDirectory );
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

    public void removeNotifier( ProjectNotifier notifier )
        throws ContinuumException
    {
        try
        {
            notifierDao.removeNotifier( notifier );
        }
        catch ( ContinuumStoreException ex )
        {
            throw logAndCreateException( "Error while removing notifier.", ex );
        }
    }

    public ProjectNotifier storeNotifier( ProjectNotifier notifier )
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

    public String getWorkingDirectory()
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

    public List<Project> getAllProjectsWithAllDetails( int start, int end )
    {
        return projectDao.getAllProjectsWithAllDetails();
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

    public Collection<ProjectGroup> getAllProjectGroupsWithProjects()
    {
        //TODO: check why this interface isn't throwing exceptions on this guy
        return projectGroupDao.getAllProjectGroupsWithProjects();
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
                releaseFile.delete();
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
                                       boolean checkDefaultBuildDefinitionForProject, int trigger )
        throws ContinuumException
    {
        Map<ProjectScmRoot, Map<Integer, Integer>> map = new HashMap<ProjectScmRoot, Map<Integer, Integer>>();
        List<ProjectScmRoot> sortedScmRoot = new ArrayList<ProjectScmRoot>();

        for ( Project project : projects )
        {
            int projectId = project.getId();

            try
            {
                // check if project already in queue
                if ( parallelBuildsManager.isInAnyBuildQueue( projectId ) ||
                    parallelBuildsManager.isProjectInAnyCurrentBuild( projectId ) )
                {
                    continue;
                }

                if ( parallelBuildsManager.isInAnyCheckoutQueue( projectId ) )
                {
                    parallelBuildsManager.removeProjectFromCheckoutQueue( projectId );
                }
            }
            catch ( BuildManagerException e )
            {
                throw new ContinuumException( e.getMessage(), e );
            }

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
                    " don't have a default build definition defined in the project or project group, will not be included in group prepare." );
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

        prepareBuildProjects( map, trigger, sortedScmRoot );
    }

    private void prepareBuildProjects( Collection<Project> projects, int buildDefinitionId, int trigger )
        throws ContinuumException
    {
        Map<ProjectScmRoot, Map<Integer, Integer>> map = new HashMap<ProjectScmRoot, Map<Integer, Integer>>();
        List<ProjectScmRoot> sortedScmRoot = new ArrayList<ProjectScmRoot>();

        for ( Project project : projects )
        {
            int projectId = project.getId();

            try
            {
                // check if project already in queue
                if ( parallelBuildsManager.isInAnyBuildQueue( projectId ) ||
                    parallelBuildsManager.isProjectInAnyCurrentBuild( projectId ) )
                {
                    continue;
                }

                if ( parallelBuildsManager.isInAnyCheckoutQueue( projectId ) )
                {
                    parallelBuildsManager.removeProjectFromCheckoutQueue( projectId );
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
            catch ( BuildManagerException e )
            {
                throw new ContinuumException( e.getMessage(), e );
            }
        }

        prepareBuildProjects( map, trigger, sortedScmRoot );
    }

    private void prepareBuildProjects( Map<ProjectScmRoot, Map<Integer, Integer>> map, int trigger,
                                       List<ProjectScmRoot> scmRoots )
        throws ContinuumException
    {
        for ( ProjectScmRoot scmRoot : scmRoots )
        {
            prepareBuildProjects( map.get( scmRoot ), trigger, scmRoot.getScmRootAddress(),
                                  scmRoot.getProjectGroup().getId(), scmRoot.getId() );
        }
    }

    private void prepareBuildProjects( Map<Integer, Integer> projectsBuildDefinitionsMap, int trigger,
                                       String scmRootAddress, int projectGroupId, int scmRootId )
        throws ContinuumException
    {
        ProjectGroup group = getProjectGroup( projectGroupId );

        try
        {
            if ( configurationService.isDistributedBuildEnabled() )
            {
                distributedBuildManager.prepareBuildProjects( projectsBuildDefinitionsMap, trigger, projectGroupId,
                                                              group.getName(), scmRootAddress, scmRootId );
            }
            else
            {
                parallelBuildsManager.prepareBuildProjects( projectsBuildDefinitionsMap, trigger, projectGroupId,
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

        projectsList =
            getProjectsInBuildOrder( projectDao.getProjectsWithDependenciesByGroupId( projectGroup.getId() ) );

        String url = "";

        for ( Project project : projectsList )
        {
            if ( StringUtils.isEmpty( url ) || !project.getScmUrl().startsWith( url ) )
            {
                // this is a root
                url = project.getScmUrl();
                createProjectScmRoot( projectGroup, url );
            }
        }
    }

    private ProjectScmRoot createProjectScmRoot( ProjectGroup projectGroup, String url )
        throws ContinuumException
    {
        try
        {
            ProjectScmRoot scmRoot =
                projectScmRootDao.getProjectScmRootByProjectGroupAndScmRootAddress( projectGroup.getId(), url );

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
            ProjectScmRoot scmRoot =
                projectScmRootDao.getProjectScmRootByProjectGroupAndScmRootAddress( project.getProjectGroup().getId(),
                                                                                    project.getScmUrl() );
            if ( scmRoot == null )
            {
                ProjectScmRoot newScmRoot = new ProjectScmRoot();
                if ( project.getScmUrl().equals( oldScmRoot.getScmRootAddress() ) )
                {
                    BeanUtils.copyProperties( oldScmRoot, newScmRoot, new String[]{"id", "projectGroup"} );
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
        String duplicateProjects = "";

        List<Project> projectsInGroup = projectGroup.getProjects();

        if ( projectsInGroup == null )
        {
            return;
        }

        for ( Project project : (List<Project>) projectGroup.getProjects() )
        {

            if ( project.getGroupId().equals( projectToCheck.getGroupId() ) &&
                project.getArtifactId().equals( projectToCheck.getArtifactId() ) &&
                project.getVersion().equals( projectToCheck.getVersion() ) )
            {
                result.addError( result.ERROR_DUPLICATE_PROJECTS );
                return;
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
