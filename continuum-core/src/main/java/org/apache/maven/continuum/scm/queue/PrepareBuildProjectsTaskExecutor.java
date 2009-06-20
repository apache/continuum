package org.apache.maven.continuum.scm.queue;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.continuum.dao.BuildDefinitionDao;
import org.apache.continuum.dao.BuildResultDao;
import org.apache.continuum.dao.ProjectDao;
import org.apache.continuum.dao.ProjectScmRootDao;
import org.apache.continuum.model.project.ProjectScmRoot;
import org.apache.continuum.taskqueue.PrepareBuildProjectsTask;
import org.apache.continuum.utils.ContinuumUtils;
import org.apache.continuum.utils.ProjectSorter;
import org.apache.continuum.utils.build.BuildTrigger;
import org.apache.maven.continuum.core.action.AbstractContinuumAction;
import org.apache.maven.continuum.core.action.CheckWorkingDirectoryAction;
import org.apache.maven.continuum.core.action.CheckoutProjectContinuumAction;
import org.apache.maven.continuum.core.action.UpdateWorkingDirectoryFromScmContinuumAction;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.scm.ChangeSet;
import org.apache.maven.continuum.model.scm.ScmResult;
import org.apache.maven.continuum.notification.ContinuumNotificationDispatcher;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.apache.maven.continuum.utils.WorkingDirectoryService;
import org.codehaus.plexus.action.ActionManager;
import org.codehaus.plexus.action.ActionNotFoundException;
import org.codehaus.plexus.taskqueue.Task;
import org.codehaus.plexus.taskqueue.execution.TaskExecutionException;
import org.codehaus.plexus.taskqueue.execution.TaskExecutor;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:ctan@apache.org">Maria Catherine Tan</a>
 * @version $Id$
 * @plexus.component role="org.codehaus.plexus.taskqueue.execution.TaskExecutor"
 * role-hint="prepare-build-project"
 */
public class PrepareBuildProjectsTaskExecutor
    implements TaskExecutor
{
    private static final Logger log = LoggerFactory.getLogger( PrepareBuildProjectsTaskExecutor.class );

    /**
     * @plexus.requirement
     */
    private ActionManager actionManager;

    /**
     * @plexus.requirement
     */
    private ProjectDao projectDao;

    /**
     * @plexus.requirement
     */
    private BuildDefinitionDao buildDefinitionDao;

    /**
     * @plexus.requirement
     */
    private ProjectScmRootDao projectScmRootDao;

    /**
     * @plexus.requirement
     */
    private BuildResultDao buildResultDao;

    /**
     * @plexus.requirement
     */
    private WorkingDirectoryService workingDirectoryService;

    /**
     * @plexus.requirement
     */
    private ContinuumNotificationDispatcher notifierDispatcher;

    public void executeTask( Task task )
        throws TaskExecutionException
    {
        PrepareBuildProjectsTask prepareTask = (PrepareBuildProjectsTask) task;

        Map<Integer, Integer> projectsBuildDefinitionsMap = prepareTask.getProjectsBuildDefinitionsMap();
        BuildTrigger buildTrigger = prepareTask.getBuildTrigger();
        Set<Integer> projectsId = projectsBuildDefinitionsMap.keySet();
        Map<String, Object> context = new HashMap<String, Object>();
        Map<Integer, ScmResult> scmResultMap = new HashMap<Integer, ScmResult>();

        try
        {
            for ( Integer projectId : projectsId )
            {
                int buildDefinitionId = projectsBuildDefinitionsMap.get( projectId );

                log.info( "Initializing prepare build" );
                context = initializeContext( projectId, buildDefinitionId, prepareTask.getBuildTrigger() );

                log.info(
                    "Starting prepare build of project: " + AbstractContinuumAction.getProject( context ).getName() );
                startPrepareBuild( context );

                if ( !checkProjectScmRoot( context ) )
                {
                    break;
                }

                try
                {
                    if ( AbstractContinuumAction.getBuildDefinition( context ).isBuildFresh() )
                    {
                        log.info( "Purging existing working copy" );
                        cleanWorkingDirectory( context );
                    }

                    // ----------------------------------------------------------------------
                    // TODO: Centralize the error handling from the SCM related actions.
                    // ContinuumScmResult should return a ContinuumScmResult from all
                    // methods, even in a case of failure.
                    // ----------------------------------------------------------------------
                    log.info( "Updating working dir" );
                    updateWorkingDirectory( context );

                    log.info( "Merging SCM results" );
                    //CONTINUUM-1393
                    if ( !AbstractContinuumAction.getBuildDefinition( context ).isBuildFresh() )
                    {
                        mergeScmResults( context );
                    }
                }
                finally
                {
                    log.info(
                        "Ending prepare build of project: " + AbstractContinuumAction.getProject( context ).getName() );
                    scmResultMap.put( AbstractContinuumAction.getProjectId( context ),
                                      AbstractContinuumAction.getScmResult( context, null ) );
                    endProjectPrepareBuild( context );
                }
            }
        }
        finally
        {
            log.info( "Ending prepare build" );
            endPrepareBuild( context );
        }

        if ( checkProjectScmRoot( context ) )
        {
            int projectGroupId = AbstractContinuumAction.getProjectGroupId( context );
            buildProjects( projectGroupId, projectsBuildDefinitionsMap, buildTrigger, scmResultMap );
        }
    }

    private Map<String, Object> initializeContext( int projectId, int buildDefinitionId, BuildTrigger buildTrigger )
        throws TaskExecutionException
    {
        Map<String, Object> context = new HashMap<String, Object>();

        try
        {
            Project project = projectDao.getProject( projectId );
            ProjectGroup projectGroup = project.getProjectGroup();

            List<ProjectScmRoot> scmRoots = projectScmRootDao.getProjectScmRootByProjectGroup( projectGroup.getId() );
            String projectScmUrl = project.getScmUrl();

            for ( ProjectScmRoot projectScmRoot : scmRoots )
            {
                if ( projectScmUrl.startsWith( projectScmRoot.getScmRootAddress() ) )
                {
                    AbstractContinuumAction.setProjectScmRoot( context, projectScmRoot );
                    break;
                }
            }

            AbstractContinuumAction.setProjectGroupId( context, projectGroup.getId() );
            AbstractContinuumAction.setProjectId( context, projectId );
            AbstractContinuumAction.setProject( context, project );
            AbstractContinuumAction.setBuildTrigger( context, buildTrigger );

            AbstractContinuumAction.setBuildDefinitionId( context, buildDefinitionId );
            AbstractContinuumAction.setBuildDefinition( context,
                                                        buildDefinitionDao.getBuildDefinition( buildDefinitionId ) );

            BuildResult oldBuildResult =
                buildResultDao.getLatestBuildResultForBuildDefinition( projectId, buildDefinitionId );

            if ( oldBuildResult != null )
            {
                AbstractContinuumAction.setOldScmResult( context,
                                                         getOldScmResults( projectId, oldBuildResult.getBuildNumber(),
                                                                           oldBuildResult.getEndTime() ) );
            }
            else
            {
                AbstractContinuumAction.setOldScmResult( context, null );
            }
        }
        catch ( ContinuumStoreException e )
        {
            throw new TaskExecutionException( "Error initializing pre-build context", e );
        }

        return context;
    }

    private void cleanWorkingDirectory( Map<String, Object> context )
        throws TaskExecutionException
    {
        performAction( "clean-working-directory", context );
    }

    private void updateWorkingDirectory( Map<String, Object> context )
        throws TaskExecutionException
    {
        performAction( "check-working-directory", context );

        boolean workingDirectoryExists = CheckWorkingDirectoryAction.isWorkingDirectoryExist( context );

        ScmResult scmResult;

        if ( workingDirectoryExists )
        {
            performAction( "update-working-directory-from-scm", context );

            scmResult = UpdateWorkingDirectoryFromScmContinuumAction.getUpdateScmResult( context );
        }
        else
        {
            Project project = AbstractContinuumAction.getProject( context );

            AbstractContinuumAction.setWorkingDirectory( context, workingDirectoryService.getWorkingDirectory(
                project ).getAbsolutePath() );

            performAction( "checkout-project", context );

            scmResult = CheckoutProjectContinuumAction.getCheckoutResult( context, null );
        }

        AbstractContinuumAction.setScmResult( context, scmResult );
    }

    private boolean checkProjectScmRoot( Map<String, Object> context )
        throws TaskExecutionException
    {
        ProjectScmRoot projectScmRoot = AbstractContinuumAction.getProjectScmRoot( context );

        // check state of scm root
        return projectScmRoot.getState() != ContinuumProjectState.ERROR;

    }

    private void startPrepareBuild( Map<String, Object> context )
        throws TaskExecutionException
    {
        ProjectScmRoot projectScmRoot = AbstractContinuumAction.getProjectScmRoot( context );
        if ( projectScmRoot.getState() != ContinuumProjectState.UPDATING )
        {
            try
            {
                projectScmRoot.setOldState( projectScmRoot.getState() );
                projectScmRoot.setState( ContinuumProjectState.UPDATING );
                projectScmRootDao.updateProjectScmRoot( projectScmRoot );
            }
            catch ( ContinuumStoreException e )
            {
                throw new TaskExecutionException( "Error persisting projectScmRoot", e );
            }
        }
    }

    private void endPrepareBuild( Map<String, Object> context )
        throws TaskExecutionException
    {
        ProjectScmRoot projectScmRoot = AbstractContinuumAction.getProjectScmRoot( context );

        if ( projectScmRoot.getState() != ContinuumProjectState.ERROR )
        {
            projectScmRoot.setState( ContinuumProjectState.UPDATED );
            projectScmRoot.setError( null );

            try
            {
                projectScmRootDao.updateProjectScmRoot( projectScmRoot );
            }
            catch ( ContinuumStoreException e )
            {
                throw new TaskExecutionException( "Error persisting projectScmRoot", e );
            }
        }

        notifierDispatcher.prepareBuildComplete( projectScmRoot );
    }

    /**
     * @param context
     * @throws TaskExecutionException
     */
    private void endProjectPrepareBuild( Map<String, Object> context )
        throws TaskExecutionException
    {
        ScmResult scmResult = AbstractContinuumAction.getScmResult( context, null );

        if ( scmResult == null || !scmResult.isSuccess() )
        {
            String error = convertScmResultToError( scmResult );

            updateProjectScmRoot( context, error );
        }
    }

    private ScmResult getOldScmResults( int projectId, long startId, long fromDate )
        throws ContinuumStoreException
    {
        List<BuildResult> results = buildResultDao.getBuildResultsForProjectFromId( projectId, startId );

        ScmResult res = new ScmResult();

        if ( results != null && results.size() > 0 )
        {
            for ( BuildResult result : results )
            {
                ScmResult scmResult = result.getScmResult();

                if ( scmResult != null )
                {
                    List<ChangeSet> changes = scmResult.getChanges();

                    if ( changes != null )
                    {
                        for ( ChangeSet changeSet : changes )
                        {
                            if ( changeSet.getDate() < fromDate )
                            {
                                continue;
                            }
                            if ( !res.getChanges().contains( changeSet ) )
                            {
                                res.addChange( changeSet );
                            }
                        }
                    }
                }
            }
        }

        return res;
    }

    /**
     * Merges scm results so we'll have all changes since last execution of current build definition
     *
     * @param context The build context
     */
    private void mergeScmResults( Map<String, Object> context )
    {
        ScmResult oldScmResult = AbstractContinuumAction.getOldScmResult( context );
        ScmResult newScmResult = AbstractContinuumAction.getScmResult( context, null );

        if ( oldScmResult != null )
        {
            if ( newScmResult == null )
            {
                AbstractContinuumAction.setScmResult( context, oldScmResult );
            }
            else
            {
                List<ChangeSet> oldChanges = oldScmResult.getChanges();

                List<ChangeSet> newChanges = newScmResult.getChanges();

                for ( ChangeSet change : newChanges )
                {
                    if ( !oldChanges.contains( change ) )
                    {
                        oldChanges.add( change );
                    }
                }

                newScmResult.setChanges( oldChanges );
            }
        }
    }

    private void performAction( String actionName, Map<String, Object> context )
        throws TaskExecutionException
    {
        TaskExecutionException exception;

        try
        {
            log.info( "Performing action " + actionName );
            actionManager.lookup( actionName ).execute( context );
            return;
        }
        catch ( ActionNotFoundException e )
        {
            exception = new TaskExecutionException( "Error looking up action '" + actionName + "'", e );
        }
        catch ( Exception e )
        {
            exception = new TaskExecutionException( "Error executing action '" + actionName + "'", e );
        }

        ScmResult result = new ScmResult();

        result.setSuccess( false );

        result.setException( ContinuumUtils.throwableToString( exception ) );

        AbstractContinuumAction.setScmResult( context, result );

        throw exception;
    }

    private String convertScmResultToError( ScmResult result )
    {
        String error = "";

        if ( result == null )
        {
            error = "Scm result is null.";
        }
        else
        {
            if ( result.getCommandLine() != null )
            {
                error = "Command line: " + StringUtils.clean( result.getCommandLine() ) +
                    System.getProperty( "line.separator" );
            }

            if ( result.getProviderMessage() != null )
            {
                error = "Provider message: " + StringUtils.clean( result.getProviderMessage() ) +
                    System.getProperty( "line.separator" );
            }

            if ( result.getCommandOutput() != null )
            {
                error += "Command output: " + System.getProperty( "line.separator" );
                error += "-------------------------------------------------------------------------------" +
                    System.getProperty( "line.separator" );
                error += StringUtils.clean( result.getCommandOutput() ) + System.getProperty( "line.separator" );
                error += "-------------------------------------------------------------------------------" +
                    System.getProperty( "line.separator" );
            }

            if ( result.getException() != null )
            {
                error += "Exception:" + System.getProperty( "line.separator" );
                error += result.getException();
            }
        }

        return error;
    }

    private void updateProjectScmRoot( Map<String, Object> context, String error )
        throws TaskExecutionException
    {
        ProjectScmRoot projectScmRoot = AbstractContinuumAction.getProjectScmRoot( context );

        try
        {
            projectScmRoot.setState( ContinuumProjectState.ERROR );
            projectScmRoot.setError( error );

            projectScmRootDao.updateProjectScmRoot( projectScmRoot );

            AbstractContinuumAction.setProjectScmRoot( context, projectScmRoot );
        }
        catch ( ContinuumStoreException e )
        {
            throw new TaskExecutionException( "Error storing project scm root", e );
        }
    }

    private void buildProjects( int projectGroupId, Map<Integer, Integer> projectsAndBuildDefinitionsMap,
    		                    BuildTrigger buildTrigger, Map<Integer, ScmResult> scmResultMap )
        throws TaskExecutionException
    {
        List<Project> projects = projectDao.getProjectsWithDependenciesByGroupId( projectGroupId );
        List<Project> projectList;

        projectList = ProjectSorter.getSortedProjects( projects, log );

        List<Project> projectsToBeBuilt = new ArrayList<Project>();
        Map<Integer, BuildDefinition> projectsBuildDefinitionsMap = new HashMap<Integer, BuildDefinition>();

        for ( Project project : projectList )
        {
            int buildDefinitionId;

            if ( projectsAndBuildDefinitionsMap.get( project.getId() ) != null )
            {
                buildDefinitionId = projectsAndBuildDefinitionsMap.get( project.getId() );

                try
                {
                    BuildDefinition buildDefinition = buildDefinitionDao.getBuildDefinition( buildDefinitionId );
                    projectsBuildDefinitionsMap.put( project.getId(), buildDefinition );
                    projectsToBeBuilt.add( project );
                }
                catch ( ContinuumStoreException e )
                {
                    log.error( "Error while creating build object", e );
                    throw new TaskExecutionException( "Error while creating build object", e );
                }
            }
            else if ( project.getState() == ContinuumProjectState.CHECKEDOUT ||
                project.getState() == ContinuumProjectState.NEW ) //check if no build result yet for project
            {
                try
                {
                    //get default build definition for project
                    BuildDefinition buildDefinition = buildDefinitionDao.getDefaultBuildDefinition( project.getId() );
                    projectsBuildDefinitionsMap.put( project.getId(), buildDefinition );
                    projectsToBeBuilt.add( project );
                }
                catch ( ContinuumStoreException e )
                {
                    log.error( "Error while creating build object", e );
                    throw new TaskExecutionException( "Error while creating build object", e );
                }
                catch ( Exception e )
                {
                    log.error( e.getMessage(), e );
                    throw new TaskExecutionException( "Error executing action 'build-project'", e );
                }
            }
        }

        try
        {
            Map<String, Object> context = new HashMap<String, Object>();
            AbstractContinuumAction.setListOfProjects( context, projectsToBeBuilt );
            AbstractContinuumAction.setProjectsBuildDefinitionsMap( context, projectsBuildDefinitionsMap );
            AbstractContinuumAction.setBuildTrigger( context, buildTrigger );
            AbstractContinuumAction.setScmResultMap( context, scmResultMap );
            AbstractContinuumAction.setProjectGroupId( context, projectGroupId );

            log.info( "Performing action create-build-project-task" );
            actionManager.lookup( "create-build-project-task" ).execute( context );
        }
        catch ( ActionNotFoundException e )
        {
            log.error( "Error looking up action 'build-project'" );
            throw new TaskExecutionException( "Error looking up action 'build-project'", e );
        }
        catch ( Exception e )
        {
            log.error( e.getMessage(), e );
            throw new TaskExecutionException( "Error executing action 'build-project'", e );
        }
    }
}
