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

import org.apache.commons.lang.ArrayUtils;
import org.apache.continuum.configuration.ContinuumConfigurationException;
import org.apache.continuum.dao.BuildDefinitionDao;
import org.apache.continuum.dao.BuildResultDao;
import org.apache.continuum.dao.DaoUtils;
import org.apache.continuum.dao.NotifierDao;
import org.apache.continuum.dao.ProjectDao;
import org.apache.continuum.dao.ProjectGroupDao;
import org.apache.continuum.dao.ScheduleDao;
import org.apache.continuum.purge.ContinuumPurgeManager;
import org.apache.continuum.repository.RepositoryService;
import org.apache.maven.continuum.build.settings.SchedulesActivationException;
import org.apache.maven.continuum.build.settings.SchedulesActivator;
import org.apache.maven.continuum.builddefinition.BuildDefinitionService;
import org.apache.maven.continuum.builddefinition.BuildDefinitionServiceException;
import org.apache.maven.continuum.buildqueue.BuildProjectTask;
import org.apache.maven.continuum.configuration.ConfigurationException;
import org.apache.maven.continuum.configuration.ConfigurationLoadingException;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.core.action.AbstractContinuumAction;
import org.apache.maven.continuum.core.action.CreateProjectsFromMetadataAction;
import org.apache.maven.continuum.execution.ContinuumBuildExecutor;
import org.apache.maven.continuum.execution.ContinuumBuildExecutorConstants;
import org.apache.maven.continuum.execution.manager.BuildExecutorManager;
import org.apache.maven.continuum.initialization.ContinuumInitializationException;
import org.apache.maven.continuum.initialization.ContinuumInitializer;
import org.apache.maven.continuum.installation.InstallationService;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildDefinitionTemplate;
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
import org.apache.maven.continuum.scm.queue.CheckOutTask;
import org.apache.maven.continuum.store.ContinuumObjectNotFoundException;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.apache.maven.continuum.utils.ProjectSorter;
import org.apache.maven.continuum.utils.WorkingDirectoryService;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.action.Action;
import org.codehaus.plexus.action.ActionManager;
import org.codehaus.plexus.action.ActionNotFoundException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.formica.FormicaException;
import org.codehaus.plexus.formica.validation.Validator;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;
import org.codehaus.plexus.taskqueue.Task;
import org.codehaus.plexus.taskqueue.TaskQueue;
import org.codehaus.plexus.taskqueue.TaskQueueException;
import org.codehaus.plexus.taskqueue.execution.TaskQueueExecutor;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.dag.CycleDetectedException;

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

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l </a>
 * @version $Id$
 * @plexus.component role="org.apache.maven.continuum.Continuum" role-hint="default"
 */
public class DefaultContinuum
    extends AbstractLogEnabled
    implements Continuum, Contextualizable, Initializable, Startable
{
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
     * @plexus.requirement role-hint="build-project"
     */
    private TaskQueue buildQueue;

    /**
     * @plexus.requirement role-hint="check-out-project"
     */
    private TaskQueue checkoutQueue;

    /**
     * @plexus.requirement
     */
    private ContinuumReleaseManager releaseManager;

    /**
     * @plexus.configuration default-value="${plexus.home}/temp"
     */
    private String workingDirectory;

    /**
     * @plexus.requirement
     */
    private WorkingDirectoryService workingDirectoryService;

    /**
     * @plexus.requirement
     */
    private BuildExecutorManager executorManager;

    private PlexusContainer container;


    /**
     * @plexus.requirement role-hint="url"
     */
    private Validator urlValidator;

    private boolean stopped = false;

    /**
     * @plexus.requirement
     */
    private ContinuumPurgeManager purgeManager;

    /**
     * @plexus.requirement
     */
    private RepositoryService repositoryService;

    public DefaultContinuum()
    {
        Runtime.getRuntime().addShutdownHook( new Thread()
        {
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
            for ( Object o : projectGroup.getProjects() )
            {
                removeProject( ( (Project) o ).getId() );
            }
        }

        getLogger().info( "Remove project group " + projectGroup.getName() + "(" + projectGroup.getId() + ")" );

        Map context = new HashMap();
        context.put( AbstractContinuumAction.KEY_PROJECT_GROUP_ID, new Integer( projectGroup.getId() ) );
        executeAction( "remove-assignable-roles", context );

        projectGroupDao.removeProjectGroup( projectGroup );
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

                buildDefinitionService.addBuildDefinitionTemplateToProjectGroup( new_pg.getId(), buildDefinitionService
                    .getDefaultMavenTwoBuildDefinitionTemplate() );

                Map context = new HashMap();
                context.put( AbstractContinuumAction.KEY_PROJECT_GROUP_ID, new Integer( new_pg.getId() ) );
                executeAction( "add-assignable-roles", context );

                getLogger().info( "Added new project group: " + new_pg.getName() );
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

        if ( builds.isEmpty() )
        {
            return null;
        }
        else
        {
            return builds.get( 0 );
        }
    }

    // ----------------------------------------------------------------------
    // Queues
    // ----------------------------------------------------------------------

    public List<BuildProjectTask> getProjectsInBuildQueue()
        throws ContinuumException
    {
        try
        {
            return buildQueue.getQueueSnapshot();
        }
        catch ( TaskQueueException e )
        {
            throw new ContinuumException( "Error while getting the building queue.", e );
        }
    }

    public boolean isInBuildingQueue( int projectId )
        throws ContinuumException
    {
        return isInBuildingQueue( projectId, -1 );
    }

    public boolean isInBuildingQueue( int projectId, int buildDefinitionId )
        throws ContinuumException
    {
        List<BuildProjectTask> queue = getProjectsInBuildQueue();

        for ( BuildProjectTask task : queue )
        {
            if ( task != null )
            {
                if ( buildDefinitionId < 0 )
                {
                    if ( task.getProjectId() == projectId )
                    {
                        return true;
                    }
                }
                else
                {
                    if ( task.getProjectId() == projectId && task.getBuildDefinitionId() == buildDefinitionId )
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean isInCheckoutQueue( int projectId )
        throws ContinuumException
    {
        List<CheckOutTask> queue = getCheckOutTasksInQueue();

        for ( CheckOutTask task : queue )
        {
            if ( task != null && task.getProjectId() == projectId )
            {
                return true;
            }
        }

        return false;
    }

    public List<CheckOutTask> getCheckOutTasksInQueue()
        throws ContinuumException
    {
        try
        {
            return checkoutQueue.getQueueSnapshot();
        }
        catch ( TaskQueueException e )
        {
            throw new ContinuumException( "Error while getting the checkout queue.", e );
        }
    }

    public TaskQueueExecutor getBuildTaskQueueExecutor()
        throws ContinuumException
    {
        try
        {
            return (TaskQueueExecutor) container.lookup( TaskQueueExecutor.class, "build-project" );
        }
        catch ( ComponentLookupException e )
        {
            throw new ContinuumException( e.getMessage(), e );
        }
    }

    public int getCurrentProjectIdBuilding()
        throws ContinuumException
    {
        Task task = getBuildTaskQueueExecutor().getCurrentTask();
        if ( task != null )
        {
            if ( task instanceof BuildProjectTask )
            {
                return ( (BuildProjectTask) task ).getProjectId();
            }
        }
        return -1;
    }

    public boolean removeFromBuildingQueue( int projectId, int buildDefinitionId, int trigger, String projectName )
        throws ContinuumException
    {
        BuildDefinition buildDefinition = getBuildDefinition( buildDefinitionId );
        String buildDefinitionLabel = buildDefinition.getDescription();
        if ( StringUtils.isEmpty( buildDefinitionLabel ) )
        {
            buildDefinitionLabel = buildDefinition.getGoals();
        }
        BuildProjectTask buildProjectTask =
            new BuildProjectTask( projectId, buildDefinitionId, trigger, projectName, buildDefinitionLabel );
        return this.buildQueue.remove( buildProjectTask );
    }

    public boolean removeProjectsFromBuildingQueue( int[] projectsId )
        throws ContinuumException
    {
        if ( projectsId == null )
        {
            return false;
        }
        if ( projectsId.length < 1 )
        {
            return false;
        }
        List<BuildProjectTask> queue = getProjectsInBuildQueue();

        List<BuildProjectTask> tasks = new ArrayList<BuildProjectTask>();

        for ( BuildProjectTask task : queue )
        {
            if ( task != null )
            {
                if ( ArrayUtils.contains( projectsId, task.getProjectId() ) )
                {
                    tasks.add( task );
                }
            }
        }

        for ( BuildProjectTask buildProjectTask : tasks )
        {
            getLogger().info( "cancel build for project " + buildProjectTask.getProjectId() );
        }
        if ( !tasks.isEmpty() )
        {
            return buildQueue.removeAll( tasks );
        }

        return false;
    }

    public boolean removeProjectsFromCheckoutQueue( int[] projectsId )
        throws ContinuumException
    {
        if ( projectsId == null )
        {
            return false;
        }
        if ( projectsId.length < 1 )
        {
            return false;
        }
        List<CheckOutTask> queue = getCheckOutTasksInQueue();

        List<CheckOutTask> tasks = new ArrayList<CheckOutTask>();

        for ( CheckOutTask task : queue )
        {
            if ( task != null )
            {
                if ( ArrayUtils.contains( projectsId, task.getProjectId() ) )
                {
                    tasks.add( task );
                }
            }
        }
        if ( !tasks.isEmpty() )
        {
            return checkoutQueue.removeAll( tasks );
        }
        return false;
    }

    public boolean removeProjectFromBuildingQueue( int projectId )
        throws ContinuumException
    {
        List<BuildProjectTask> queue = getProjectsInBuildQueue();

        for ( BuildProjectTask task : queue )
        {
            if ( task != null && task.getProjectId() == projectId )
            {
                return buildQueue.remove( task );
            }
        }

        return false;
    }

    public void removeProjectsFromBuildingQueueWithHashCodes( int[] hashCodes )
        throws ContinuumException
    {
        List<BuildProjectTask> queue = getProjectsInBuildQueue();

        for ( BuildProjectTask task : queue )
        {
            if ( ArrayUtils.contains( hashCodes, task.hashCode() ) )
            {
                buildQueue.remove( task );
            }
        }
    }

    public boolean removeProjectFromCheckoutQueue( int projectId )
        throws ContinuumException
    {
        List<CheckOutTask> queue = getCheckOutTasksInQueue();

        for ( CheckOutTask task : queue )
        {
            if ( task != null && task.getProjectId() == projectId )
            {
                return checkoutQueue.remove( task );
            }
        }

        return false;
    }

    public void removeTasksFromCheckoutQueueWithHashCodes( int[] hashCodes )
        throws ContinuumException
    {
        List<CheckOutTask> queue = getCheckOutTasksInQueue();

        for ( CheckOutTask task : queue )
        {
            if ( ArrayUtils.contains( hashCodes, task.hashCode() ) )
            {
                checkoutQueue.remove( task );
            }
        }
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

            getLogger().info( "Remove project " + project.getName() + "(" + projectId + ")" );

            if ( isInCheckoutQueue( projectId ) )
            {
                removeProjectFromCheckoutQueue( projectId );
            }

            if ( isInBuildingQueue( projectId ) )
            {
                removeProjectFromBuildingQueue( projectId );
            }

            // cancel it if currently building

            if ( getCurrentProjectIdBuilding() == projectId )
            {
                Task currentTask = getBuildTaskQueueExecutor().getCurrentTask();
                {
                    if ( currentTask instanceof BuildProjectTask )
                    {
                        if ( ( (BuildProjectTask) currentTask ).getProjectId() == projectId )
                        {
                            getLogger().info( "Cancelling task for project " + projectId );
                            getBuildTaskQueueExecutor().cancelTask( currentTask );
                        }
                    }
                }
            }

            for ( Object o : project.getBuildResults() )
            {
                BuildResult br = (BuildResult) o;
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

        context.put( AbstractContinuumAction.KEY_PROJECT_ID, new Integer( projectId ) );

        executeAction( "add-project-to-checkout-queue", context );
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

    /**
     * fire of the builds of all projects across all project groups using their default build definitions
     *
     * @param trigger
     * @throws ContinuumException
     */
    public void buildProjects( int trigger )
        throws ContinuumException
    {
        Collection<Project> projectsList;

        try
        {
            projectsList = getProjectsInBuildOrder();
        }
        catch ( CycleDetectedException e )
        {
            getLogger().warn( "Cycle detected while sorting projects for building, falling back to unsorted build." );

            projectsList = getProjects();
        }

        for ( Project project : projectsList )
        {
            Integer buildDefId = null;

            try
            {
                buildDefId = new Integer( buildDefinitionDao.getDefaultBuildDefinition( project.getId() ).getId() );
            }
            catch ( ContinuumStoreException e )
            {
                throw new ContinuumException( "Project (id=" + project.getId() +
                    " doens't have a default build definition, this should be impossible, parent should have default definition set." );
            }

            buildProject( project, buildDefId.intValue(), trigger );
        }
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
        Collection<Project> projectsList;

        try
        {
            projectsList = getProjectsInBuildOrder();
        }
        catch ( CycleDetectedException e )
        {
            getLogger().warn( "Cycle detected while sorting projects for building, falling back to unsorted build." );

            projectsList = getProjects();
        }

        for ( Project project : projectsList )
        {
            buildProject( project, buildDefinitionId, trigger );
        }
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
        List<BuildDefinition> groupDefaultBDs = null;

        groupDefaultBDs = getDefaultBuildDefinitionsForProjectGroup( projectGroupId );

        buildProjectGroupWithBuildDefinition( projectGroupId, groupDefaultBDs, true );
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
        List<BuildDefinition> bds = new ArrayList<BuildDefinition>();
        BuildDefinition bd = getBuildDefinition( buildDefinitionId );
        if ( bd != null )
        {
            bds.add( bd );
        }
        buildProjectGroupWithBuildDefinition( projectGroupId, bds, false );
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
        Collection<Project> projectsList;

        try
        {
            projectsList = getProjectsInBuildOrder( projectDao.getProjectsWithDependenciesByGroupId( projectGroupId ) );
        }
        catch ( CycleDetectedException e )
        {
            getLogger().warn( "Cycle detected while sorting projects for building, falling back to unsorted build." );

            projectsList = getProjects();
        }

        //Map buildDefinitionsIds = store.getDefaultBuildDefinitions();

        for ( Project project : projectsList )
        {
            int buildDefId = -1;

            for ( BuildDefinition bd : bds )
            {
                if ( project.getExecutorId().equals( bd.getType() ) || ( StringUtils.isEmpty( bd.getType() ) &&
                    ContinuumBuildExecutorConstants.MAVEN_TWO_BUILD_EXECUTOR.equals( project.getExecutorId() ) ) )
                {
                    buildDefId = bd.getId();
                    break;
                }
            }

            if ( checkDefaultBuildDefinitionForProject )
            {
                BuildDefinition projectDefaultBD = null;
                try
                {
                    projectDefaultBD = buildDefinitionDao.getDefaultBuildDefinitionForProject( project.getId() );
                }
                catch ( ContinuumObjectNotFoundException e )
                {
                    getLogger().debug( e.getMessage() );
                }
                catch ( ContinuumStoreException e )
                {
                    getLogger().debug( e.getMessage() );
                }

                if ( projectDefaultBD != null )
                {
                    buildDefId = projectDefaultBD.getId();
                    getLogger().debug( "Project " + project.getId() +
                        " has own default build definition, will use it instead of group's." );
                }
            }

            if ( buildDefId == -1 )
            {
                getLogger().info( "Project " + project.getId() +
                    " don't have a default build definition defined in the project or project group, will not be included in group build." );
                continue;
            }

            buildProject( project, buildDefId, ContinuumProjectState.TRIGGER_FORCED );
        }
    }

    /**
     * takes a given schedule and determines which projects need to build
     * <p/>
     * The build order is determined by the
     *
     * @param schedule
     * @throws ContinuumException
     */
    public void buildProjects( Schedule schedule )
        throws ContinuumException
    {
        Collection<Project> projectsList;

        Map projectsMap = null;

        try
        {
            projectsMap = daoUtils.getAggregatedProjectIdsAndBuildDefinitionIdsBySchedule( schedule.getId() );

            if ( projectsMap == null || projectsMap.size() == 0 )
            {
                getLogger().debug( "no builds attached to schedule" );
                // We don't have projects attached to this schedule
                return;
            }

            projectsList = getProjectsInBuildOrder();
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumException( "Can't get project list for schedule " + schedule.getName(), e );
        }
        catch ( CycleDetectedException e )
        {
            getLogger().warn( "Cycle detected while sorting projects for building, falling back to unsorted build." );

            projectsList = getProjects();
        }

        for ( Project project : projectsList )
        {
            List<Integer> buildDefIds = (List<Integer>) projectsMap.get( new Integer( project.getId() ) );

            if ( buildDefIds != null && !buildDefIds.isEmpty() )
            {
                for ( Integer buildDefId : buildDefIds )
                {
                    if ( buildDefId != null && !isInBuildingQueue( project.getId(), buildDefId.intValue() ) &&
                        !isInCheckoutQueue( project.getId() ) )
                    {
                        buildProject( project, buildDefId.intValue(), ContinuumProjectState.TRIGGER_SCHEDULED, false );
                    }
                }
            }
        }
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
        BuildDefinition buildDef = getDefaultBuildDefinition( projectId );

        if ( buildDef == null )
        {
            throw new ContinuumException( "Project (id=" + projectId + " doens't have a default build definition." );
        }

        if ( isInBuildingQueue( projectId, buildDef.getId() ) || isInCheckoutQueue( projectId ) )
        {
            return;
        }

        buildProject( projectId, buildDef.getId(), trigger, false );
    }

    public void buildProject( int projectId, int buildDefinitionId, int trigger )
        throws ContinuumException
    {
        buildProject( projectId, buildDefinitionId, trigger, true );
    }

    public void buildProject( Project project, int buildDefinitionId, int trigger )
        throws ContinuumException
    {
        buildProject( project, buildDefinitionId, trigger, true );
    }

    private void buildProject( int projectId, int buildDefinitionId, int trigger, boolean checkQueues )
        throws ContinuumException
    {
        Project project;

        try
        {
            project = projectDao.getProject( projectId );
        }
        catch ( ContinuumStoreException e )
        {
            throw logAndCreateException( "Error while getting project " + projectId + ".", e );
        }

        buildProject( project, buildDefinitionId, trigger, checkQueues );
    }

    private synchronized void buildProject( Project project, int buildDefinitionId, int trigger, boolean checkQueues )
        throws ContinuumException
    {
        if ( checkQueues )
        {
            if ( isInBuildingQueue( project.getId(), buildDefinitionId ) )
            {
                return;
            }
            if ( isInCheckoutQueue( project.getId() ) )
            {
                removeProjectFromCheckoutQueue( project.getId() );
            }
        }

        try
        {
            if ( project.getState() != ContinuumProjectState.NEW &&
                project.getState() != ContinuumProjectState.CHECKEDOUT &&
                project.getState() != ContinuumProjectState.OK && project.getState() != ContinuumProjectState.FAILED &&
                project.getState() != ContinuumProjectState.ERROR )
            {
                ContinuumBuildExecutor executor = executorManager.getBuildExecutor( project.getExecutorId() );

                if ( executor.isBuilding( project ) || project.getState() == ContinuumProjectState.UPDATING )
                {
                    // project is building
                    getLogger().info( "Project '" + project.getName() + "' already being built." );

                    return;
                }
                else
                {
                    project.setOldState( project.getState() );

                    project.setState( ContinuumProjectState.ERROR );

                    projectDao.updateProject( project );

                    project = projectDao.getProject( project.getId() );
                }
            }
            else
            {
                project.setOldState( project.getState() );

                projectDao.updateProject( project );

                project = projectDao.getProject( project.getId() );
            }

            BuildDefinition buildDefinition = getBuildDefinition( buildDefinitionId );
            String buildDefinitionLabel = buildDefinition.getDescription();
            if ( StringUtils.isEmpty( buildDefinitionLabel ) )
            {
                buildDefinitionLabel = buildDefinition.getGoals();
            }

            getLogger().info( "Enqueuing '" + project.getName() + "' with build definition '" + buildDefinitionLabel +
                "' - id=" + buildDefinitionId + ")." );

            BuildProjectTask task = new BuildProjectTask( project.getId(), buildDefinitionId, trigger, project
                .getName(), buildDefinitionLabel );

            task.setMaxExecutionTime( buildDefinitionDao.getBuildDefinition( buildDefinitionId ).getSchedule()
                .getMaxJobExecutionTime() * 1000 );

            buildQueue.put( task );
        }
        catch ( ContinuumStoreException e )
        {
            throw logAndCreateException( "Error while creating build object.", e );
        }
        catch ( TaskQueueException e )
        {
            throw logAndCreateException( "Error while creating enqueuing object.", e );
        }
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
            getLogger().info( "skip error during cleanup build files " + e.getMessage(), e );
        }
        catch ( IOException e )
        {
            getLogger().info( "skip IOException during cleanup build files " + e.getMessage(), e );
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
                BuildResult buildResult = (BuildResult) buildResultsIterator.next();

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

        BuildResult buildResult = (BuildResult) buildResultsIterator.next();

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

            buildResult = (BuildResult) buildResultsIterator.next();
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
        throws CycleDetectedException, ContinuumException
    {
        return getProjectsInBuildOrder( getProjectsWithDependencies() );
    }

    /**
     * take a collection of projects and sort for order
     *
     * @param projects
     * @return
     * @throws CycleDetectedException
     */
    public List<Project> getProjectsInBuildOrder( Collection<Project> projects )
        throws CycleDetectedException
    {
        if ( projects == null || projects.isEmpty() )
        {
            return new ArrayList<Project>();
        }

        return ProjectSorter.getSortedProjects( projects, getLogger() );
    }

    // ----------------------------------------------------------------------
    // Maven 1.x projects
    // ----------------------------------------------------------------------

    public ContinuumProjectBuildingResult addMavenOneProject( String metadataUrl )
        throws ContinuumException
    {
        return addMavenOneProject( metadataUrl, true );
    }

    public ContinuumProjectBuildingResult addMavenOneProject( String metadataUrl, boolean checkProtocol )
        throws ContinuumException
    {
        try
        {
            return executeAddProjectsFromMetadataActivity( metadataUrl, MavenOneContinuumProjectBuilder.ID,
                                                           getDefaultProjectGroup().getId(), checkProtocol,
                                                           buildDefinitionService
                                                               .getDefaultMavenOneBuildDefinitionTemplate().getId() );
        }
        catch ( BuildDefinitionServiceException e )
        {
            throw new ContinuumException( e.getMessage(), e );
        }
    }

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
                                                              int buildDefintionTemplateId )
        throws ContinuumException
    {
        return executeAddProjectsFromMetadataActivity( metadataUrl, MavenOneContinuumProjectBuilder.ID, projectGroupId,
                                                       checkProtocol, useCredentialsCache, true,
                                                       buildDefintionTemplateId );
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
                                                           checkProtocol, buildDefinitionService
                .getDefaultMavenTwoBuildDefinitionTemplate().getId() );
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
                                                           buildDefinitionService.getDefaultMavenTwoBuildDefinitionTemplate()
                                                               .getId() );
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
                                                           recursiveProjects, buildDefinitionService
                .getDefaultMavenTwoBuildDefinitionTemplate().getId() );
        }
        catch ( BuildDefinitionServiceException e )
        {
            throw new ContinuumException( e.getMessage(), e );
        }
    }

    public ContinuumProjectBuildingResult addMavenTwoProject( String metadataUrl, int projectGroupId,
                                                              boolean checkProtocol, boolean useCredentialsCache,
                                                              boolean recursiveProjects, int buildDefintionTemplateId )
        throws ContinuumException
    {
        return executeAddProjectsFromMetadataActivity( metadataUrl, MavenTwoContinuumProjectBuilder.ID, projectGroupId,
                                                       checkProtocol, useCredentialsCache, recursiveProjects,
                                                       buildDefintionTemplateId );
    }

    // ----------------------------------------------------------------------
    // Shell projects
    // ----------------------------------------------------------------------

    /**
     * @see org.apache.maven.continuum.Continuum#addProject(org.apache.maven.continuum.model.project.Project, java.lang.String)
     */
    public int addProject( Project project, String executorId )
        throws ContinuumException
    {
        return addProject( project, executorId, getDefaultProjectGroup().getId() );
    }

    public int addProject( Project project, String executorId, int groupId )
        throws ContinuumException
    {
        return addProject( project, executorId, groupId, -1 );
    }

    /**
     * @see org.apache.maven.continuum.Continuum#addProject(org.apache.maven.continuum.model.project.Project, java.lang.String, int, int)
     */
    public int addProject( Project project, String executorId, int groupId, int buildDefintionTemplateId )
        throws ContinuumException
    {
        project.setExecutorId( executorId );

        List<BuildDefinition> buildDefinitions = Collections.EMPTY_LIST;

        try
        {
            if ( executorId.equalsIgnoreCase( ContinuumBuildExecutorConstants.ANT_BUILD_EXECUTOR ) )
            {
                BuildDefinitionTemplate bdt = null;
                if ( buildDefintionTemplateId <= 0 )
                {
                    bdt = buildDefinitionService.getDefaultAntBuildDefinitionTemplate();
                }
                else
                {
                    bdt = buildDefinitionService.getBuildDefinitionTemplate( buildDefintionTemplateId );
                }
                buildDefinitions = bdt.getBuildDefinitions();
            }
            else
            {
                //shell default

                BuildDefinitionTemplate bdt = null;
                if ( buildDefintionTemplateId <= 0 )
                {
                    bdt = buildDefinitionService.getDefaultShellBuildDefinitionTemplate();
                }
                else
                {
                    bdt = buildDefinitionService.getBuildDefinitionTemplate( buildDefintionTemplateId );
                }
                buildDefinitions = bdt.getBuildDefinitions();
            }
            for ( BuildDefinition buildDefinition : buildDefinitions )
            {

                BuildDefinition cloned = buildDefinitionService.cloneBuildDefinition( buildDefinition );
                cloned.setTemplate( false );
                project.addBuildDefinition( buildDefinitionService.addBuildDefinition( cloned ) );
            }
        }
        catch ( BuildDefinitionServiceException e )
        {
            throw new ContinuumException( e.getMessage(), e );
        }

        return executeAddProjectFromScmActivity( project, groupId );
    }

    // ----------------------------------------------------------------------
    // Activities. These should end up as workflows in werkflow
    // ----------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private int executeAddProjectFromScmActivity( Project project, int groupId )
        throws ContinuumException
    {
        ProjectGroup projectGroup = getProjectGroupWithBuildDetails( groupId );

        Map context = new HashMap();

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        context.put( AbstractContinuumAction.KEY_WORKING_DIRECTORY, getWorkingDirectory() );

        context.put( AbstractContinuumAction.KEY_UNVALIDATED_PROJECT, project );

        context.put( AbstractContinuumAction.KEY_UNVALIDATED_PROJECT_GROUP, projectGroup );

        context.put( AbstractContinuumAction.KEY_PROJECT_GROUP_ID, new Integer( projectGroup.getId() ) );

        executeAction( "validate-project", context );

        executeAction( "store-project", context );

        executeAction( "add-project-to-checkout-queue", context );

        executeAction( "add-assignable-roles", context );

        return ( (Integer) context.get( AbstractContinuumAction.KEY_PROJECT_ID ) ).intValue();
    }

    private ContinuumProjectBuildingResult executeAddProjectsFromMetadataActivity( String metadataUrl,
                                                                                   String projectBuilderId,
                                                                                   int projectGroupId,
                                                                                   boolean checkProtocol,
                                                                                   int buildDefintionTemplateId )
        throws ContinuumException
    {
        return executeAddProjectsFromMetadataActivity( metadataUrl, projectBuilderId, projectGroupId, checkProtocol,
                                                       false, false, buildDefintionTemplateId );
    }


    protected ContinuumProjectBuildingResult executeAddProjectsFromMetadataActivity( String metadataUrl,
                                                                                     String projectBuilderId,
                                                                                     int projectGroupId,
                                                                                     boolean checkProtocol,
                                                                                     boolean useCredentialsCache,
                                                                                     boolean loadRecursiveProjects,
                                                                                     int buildDefintionTemplateId,
                                                                                     boolean addAssignableRoles )
        throws ContinuumException
    {
        if ( checkProtocol )
        {
            try
            {
                if ( !urlValidator.validate( metadataUrl ) )
                {
                    ContinuumProjectBuildingResult res = new ContinuumProjectBuildingResult();
                    res.addError( ContinuumProjectBuildingResult.ERROR_PROTOCOL_NOT_ALLOWED );
                    return res;
                }
            }
            catch ( FormicaException e )
            {
                //can't be thrown
            }
        }

        Map context = new HashMap();

        context.put( CreateProjectsFromMetadataAction.KEY_PROJECT_BUILDER_ID, projectBuilderId );

        context.put( CreateProjectsFromMetadataAction.KEY_URL, metadataUrl );

        context.put( CreateProjectsFromMetadataAction.KEY_LOAD_RECURSIVE_PROJECTS, Boolean
            .valueOf( loadRecursiveProjects ) );

        context.put( CreateProjectsFromMetadataAction.KEY_SCM_USE_CREDENTIALS_CACHE, Boolean
            .valueOf( useCredentialsCache ) );

        context.put( AbstractContinuumAction.KEY_WORKING_DIRECTORY, getWorkingDirectory() );

        // CreateProjectsFromMetadataAction will check null and use default
        if ( buildDefintionTemplateId > 0 )
        {
            try
            {
                context.put( AbstractContinuumAction.KEY_BUILD_DEFINITION_TEMPLATE, buildDefinitionService
                    .getBuildDefinitionTemplate( buildDefintionTemplateId ) );
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

        ContinuumProjectBuildingResult result = (ContinuumProjectBuildingResult) context
            .get( CreateProjectsFromMetadataAction.KEY_PROJECT_BUILDING_RESULT );

        if ( getLogger().isInfoEnabled() )
        {
            if ( result.getProjects() != null )
            {
                getLogger().info( "Created " + result.getProjects().size() + " projects." );
            }
            if ( result.getProjectGroups() != null )
            {
                getLogger().info( "Created " + result.getProjectGroups().size() + " project groups." );
            }
            getLogger().info( result.getErrors().size() + " errors." );

            // ----------------------------------------------------------------------
            // Look for any errors.
            // ----------------------------------------------------------------------

            if ( result.hasErrors() )
            {
                getLogger().info( result.getErrors().size() + " errors during project add: " );
                getLogger().info( result.getErrorsAsString() );
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

        ProjectGroup projectGroup = (ProjectGroup) result.getProjectGroups().iterator().next();

        try
        {
            if ( projectGroupId == -1 )
            {
                try
                {
                    projectGroup = projectGroupDao.getProjectGroupByGroupId( projectGroup.getGroupId() );

                    projectGroupId = projectGroup.getId();

                    getLogger().info(
                        "Using existing project group with the group id: '" + projectGroup.getGroupId() + "'." );
                }
                catch ( ContinuumObjectNotFoundException e )
                {
                    getLogger().info(
                        "Creating project group with the group id: '" + projectGroup.getGroupId() + "'." );

                    Map pgContext = new HashMap();

                    pgContext.put( AbstractContinuumAction.KEY_WORKING_DIRECTORY, getWorkingDirectory() );

                    pgContext.put( AbstractContinuumAction.KEY_UNVALIDATED_PROJECT_GROUP, projectGroup );

                    executeAction( "validate-project-group", pgContext );

                    executeAction( "store-project-group", pgContext );

                    projectGroupId = AbstractContinuumAction.getProjectGroupId( pgContext );
                }
            }

            projectGroup = projectGroupDao.getProjectGroupWithBuildDetailsByProjectGroupId( projectGroupId );

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
                context = new HashMap();

                context.put( AbstractContinuumAction.KEY_UNVALIDATED_PROJECT, project );
                //
                //            executeAction( "validate-project", context );
                //
                //            executeAction( "store-project", context );
                //
                context.put( AbstractContinuumAction.KEY_PROJECT_ID, new Integer( project.getId() ) );

                if ( !StringUtils.isEmpty( scmUserName ) )
                {
                    project.setScmUsername( scmUserName );
                    context.put( AbstractContinuumAction.KEY_SCM_USERNAME, scmUserName );
                }
                if ( !StringUtils.isEmpty( scmPassword ) )
                {
                    project.setScmPassword( scmPassword );
                    context.put( AbstractContinuumAction.KEY_SCM_PASSWORD, scmPassword );
                }
                // FIXME
                // olamy  : read again the project to have values because store.updateProjectGroup( projectGroup ); 
                // remove object data -> we don't display the project name in the build queue
                context.put( AbstractContinuumAction.KEY_PROJECT, projectDao.getProject( project.getId() ) );
                executeAction( "add-project-to-checkout-queue", context );
            }
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumException( "Error adding projects from modules", e );
        }

        context.put( AbstractContinuumAction.KEY_PROJECT_GROUP_ID, new Integer( projectGroup.getId() ) );
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
                                                                                     int buildDefintionTemplateId )
        throws ContinuumException
    {
        return executeAddProjectsFromMetadataActivity( metadataUrl, projectBuilderId, projectGroupId, checkProtocol,
                                                       useCredentialsCache, loadRecursiveProjects,
                                                       buildDefintionTemplateId, true );
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

    /*
    public ProjectNotifier updateNotifier( int projectId, int notifierId, Map configuration )
        throws ContinuumException
    {
        Project project = getProjectWithAllDetails( projectId );

        ProjectNotifier notifier = getNotifier( projectId, notifierId );

        String notifierType = notifier.getType();

        // I remove notifier then add it instead of update it due to a ClassCastException in jpox
        project.removeNotifier( notifier );

        updateProject( project );

        return addNotifier( projectId, notifierType, configuration );
    }

    private Properties createNotifierProperties( Map configuration )
    {
        Properties notifierProperties = new Properties();

        for ( Iterator i = configuration.keySet().iterator(); i.hasNext(); )
        {
            Object key = i.next();

            Object value = configuration.get( key );

            if ( value instanceof String )
            {
                String val = (String) value;
                if ( !"sendOnSuccess".equals( val ) && !"sendOnFailure".equals( val ) && !"sendOnError".equals( val ) &&
                    !"sendOnWarning".equals( val ) )
                {
                    if ( !StringUtils.isEmpty( val ) )
                    {
                        notifierProperties.setProperty( (String) key, val );
                    }
                }
            }
        }

        return notifierProperties;
    }
    */

    public ProjectNotifier addNotifier( int projectId, ProjectNotifier notifier )
        throws ContinuumException
    {
        ProjectNotifier notif = new ProjectNotifier();

        notif.setSendOnSuccess( notifier.isSendOnSuccess() );

        notif.setSendOnFailure( notifier.isSendOnFailure() );

        notif.setSendOnError( notifier.isSendOnError() );

        notif.setSendOnWarning( notifier.isSendOnWarning() );

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

    /*
    public ProjectNotifier addNotifier( int projectId, String notifierType, Map configuration )
        throws ContinuumException
    {
        ProjectNotifier notifier = new ProjectNotifier();

        notifier.setType( notifierType );

        // ----------------------------------------------------------------------
        // Needs to be properties ... but data comes in via a Map
        // ----------------------------------------------------------------------

        Properties notifierProperties = createNotifierProperties( configuration );

        boolean sendOnSuccess = convertBoolean( (String) configuration.get( "sendOnSuccess" ) );

        notifier.setSendOnSuccess( sendOnSuccess );

        boolean sendOnFailure = convertBoolean( (String) configuration.get( "sendOnFailure" ) );

        notifier.setSendOnFailure( sendOnFailure );

        boolean sendOnError = convertBoolean( (String) configuration.get( "sendOnError" ) );

        notifier.setSendOnError( sendOnError );

        boolean sendOnWarning = convertBoolean( (String) configuration.get( "sendOnWarning" ) );

        notifier.setSendOnWarning( sendOnWarning );

        notifier.setConfiguration( notifierProperties );

        return addNotifier( projectId, notifier );
    }
    */

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
        HashMap context = new HashMap();

        context.put( AbstractContinuumAction.KEY_BUILD_DEFINITION, buildDefinition );
        context.put( AbstractContinuumAction.KEY_PROJECT_ID, new Integer( projectId ) );

        executeAction( "add-build-definition-to-project", context );

        return (BuildDefinition) context.get( AbstractContinuumAction.KEY_BUILD_DEFINITION );
    }

    public void removeBuildDefinitionFromProject( int projectId, int buildDefinitionId )
        throws ContinuumException
    {
        HashMap context = new HashMap();

        context.put( AbstractContinuumAction.KEY_BUILD_DEFINITION, getBuildDefinition( buildDefinitionId ) );
        context.put( AbstractContinuumAction.KEY_PROJECT_ID, new Integer( projectId ) );

        executeAction( "remove-build-definition-from-project", context );
    }

    public BuildDefinition updateBuildDefinitionForProject( int projectId, BuildDefinition buildDefinition )
        throws ContinuumException
    {
        HashMap context = new HashMap();

        context.put( AbstractContinuumAction.KEY_BUILD_DEFINITION, buildDefinition );
        context.put( AbstractContinuumAction.KEY_PROJECT_ID, new Integer( projectId ) );

        executeAction( "update-build-definition-from-project", context );

        return (BuildDefinition) context.get( AbstractContinuumAction.KEY_BUILD_DEFINITION );
    }

    public BuildDefinition addBuildDefinitionToProjectGroup( int projectGroupId, BuildDefinition buildDefinition )
        throws ContinuumException
    {
        HashMap context = new HashMap();

        context.put( AbstractContinuumAction.KEY_BUILD_DEFINITION, buildDefinition );
        context.put( AbstractContinuumAction.KEY_PROJECT_GROUP_ID, new Integer( projectGroupId ) );

        executeAction( "add-build-definition-to-project-group", context );

        return (BuildDefinition) context.get( AbstractContinuumAction.KEY_BUILD_DEFINITION );
    }

    public void removeBuildDefinitionFromProjectGroup( int projectGroupId, int buildDefinitionId )
        throws ContinuumException
    {
        HashMap context = new HashMap();

        context.put( AbstractContinuumAction.KEY_BUILD_DEFINITION, getBuildDefinition( buildDefinitionId ) );
        context.put( AbstractContinuumAction.KEY_PROJECT_GROUP_ID, new Integer( projectGroupId ) );

        executeAction( "remove-build-definition-from-project-group", context );
    }

    public BuildDefinition updateBuildDefinitionForProjectGroup( int projectGroupId, BuildDefinition buildDefinition )
        throws ContinuumException
    {
        HashMap context = new HashMap();

        context.put( AbstractContinuumAction.KEY_BUILD_DEFINITION, buildDefinition );
        context.put( AbstractContinuumAction.KEY_PROJECT_GROUP_ID, new Integer( projectGroupId ) );

        executeAction( "update-build-definition-from-project-group", context );

        return (BuildDefinition) context.get( AbstractContinuumAction.KEY_BUILD_DEFINITION );
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
        storeSchedule( schedule );

        if ( updateScheduler )
        {
            try
            {
                if ( schedule.isActive() )
                {
                    // I unactivate it before if it's already active
                    schedulesActivator.unactivateSchedule( schedule, this );

                    schedulesActivator.activateSchedule( schedule, this );
                }
                else
                {
                    schedulesActivator.unactivateSchedule( schedule, this );
                }
            }
            catch ( SchedulesActivationException e )
            {
                getLogger().error( "Can't unactivate schedule. You need to restart Continuum.", e );
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

        schedule.setActive( Boolean.valueOf( configuration.get( "schedule.active" ) ).booleanValue() );

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
            getLogger().error( "Can't unactivate the schedule. You need to restart Continuum.", e );
        }

        try
        {
            scheduleDao.removeSchedule( schedule );
        }
        catch ( Exception e )
        {
            getLogger().error( "Can't remove the schedule.", e );

            try
            {
                schedulesActivator.activateSchedule( schedule, this );
            }
            catch ( SchedulesActivationException sae )
            {
                getLogger().error( "Can't reactivate the schedule. You need to restart Continuum.", e );
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

    public void updateConfiguration( Map<String, Object> configuration )
        throws ContinuumException
    {
        try
        {
            if ( configuration.get( "conf.workingDirectory" ) != null )
            {
                configurationService.setWorkingDirectory( configurationService.getFile( (String) configuration
                    .get( "conf.workingDirectory" ) ) );
            }
            else
            {
                throw new ContinuumException( "working directory can't be null" );
            }

            if ( configuration.get( "conf.buildOutputDirectory" ) != null )
            {
                configurationService.setBuildOutputDirectory( configurationService.getFile( (String) configuration
                    .get( "conf.buildOutputDirectory" ) ) );
            }
            else
            {
                throw new ContinuumException( "build output directory can't be null" );
            }

            if ( configuration.get( "conf.deploymentRepositoryDirectory" ) != null )
            {
                configurationService.setDeploymentRepositoryDirectory( configurationService
                    .getFile( (String) configuration.get( "conf.deploymentRepositoryDirectory" ) ) );
            }

            if ( configuration.get( "conf.url" ) != null )
            {
                configurationService.setUrl( (String) configuration.get( "conf.url" ) );
            }
            else
            {
                throw new ContinuumException( "base url can't be null" );
            }

            configurationService.store();
        }
        catch ( Exception e )
        {
            throw new ContinuumException( "Can't store configuration.", e );
        }
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
        getLogger().info( "Initializing Continuum." );

        File wdFile = new File( workingDirectory );

        if ( wdFile.exists() )
        {
            if ( !wdFile.isDirectory() )
            {
                throw new InitializationException(
                    "The specified working directory isn't a directory: " + "'" + wdFile.getAbsolutePath() + "'." );
            }
        }
        else
        {
            if ( !wdFile.mkdirs() )
            {
                throw new InitializationException(
                    "Could not making the working directory: " + "'" + wdFile.getAbsolutePath() + "'." );
            }
        }

        getLogger().info( "Showing all projects: " );

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
                    getLogger().info( "Fix project state for project " + project.getId() + ":" + project.getName() +
                        ":" + project.getVersion() );

                    projectDao.updateProject( project );

                    Project p = projectDao.getProject( project.getId() );

                    if ( state == p.getState() )
                    {
                        getLogger().info( "Can't fix the project state." );
                    }
                }
                catch ( ContinuumStoreException e )
                {
                    throw new InitializationException( "Database is corrupted.", e );
                }
            }

            getLogger().info( " " + project.getId() + ":" + project.getName() + ":" + project.getVersion() + ":" +
                project.getExecutorId() );
        }
    }

    // --------------------------------
    //  Plexus Lifecycle
    // --------------------------------
    public void contextualize( Context context )
        throws ContextException
    {
        container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );

    }

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
            getLogger().error( "Error activating schedules.", e );
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
            getLogger().info( "Error storing the Continuum configuration.", e );
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

    protected void executeAction( String actionName, Map context )
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
            getLogger().info( "exception", e );
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

        getLogger().error( message, cause );

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

            if ( !p.getScmUrl().equals( project.getScmUrl() ) )
            {
                removeWorkingDirectory = true;
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
        return workingDirectory;
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
        getLogger().info( "Starting Continuum." );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        String banner = StringUtils.repeat( "-", getVersion().length() );

        getLogger().info( "" );
        getLogger().info( "" );
        getLogger().info( "< Continuum " + getVersion() + " started! >" );
        getLogger().info( "-----------------------" + banner );
        getLogger().info( "       \\   ^__^" );
        getLogger().info( "        \\  (oo)\\_______" );
        getLogger().info( "           (__)\\       )\\/\\" );
        getLogger().info( "               ||----w |" );
        getLogger().info( "               ||     ||" );
        getLogger().info( "" );
        getLogger().info( "" );
    }

    private void stopMessage()
    {
        // Yes dorothy, this can happen!
        if ( getLogger() != null )
        {
            getLogger().info( "Stopping Continuum." );

            getLogger().info( "Continuum stopped." );
        }
    }

    private String getVersion()
    {
        try
        {
            Properties properties = new Properties();

            String name = "META-INF/maven/org.apache.maven.continuum/continuum-core/pom.properties";

            InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream( name );

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
    }

    private ProjectGroup getDefaultProjectGroup()
        throws ContinuumException
    {
        try
        {
            return projectGroupDao.getProjectGroupByGroupIdWithProjects( Continuum.DEFAULT_PROJECT_GROUP_GROUP_ID );
        }
        catch ( ContinuumObjectNotFoundException e )
        {
            throw new ContinuumException( "Continuum is not properly initialized, default project group does not exist",
                                          e );
        }
        catch ( ContinuumStoreException ex )
        {
            throw logAndCreateException( "Exception while getting default project group.", ex );
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

}
