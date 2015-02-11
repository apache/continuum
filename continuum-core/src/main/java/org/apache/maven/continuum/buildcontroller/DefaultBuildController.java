package org.apache.maven.continuum.buildcontroller;

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

import org.apache.continuum.dao.BuildDefinitionDao;
import org.apache.continuum.dao.BuildResultDao;
import org.apache.continuum.dao.ProjectDao;
import org.apache.continuum.dao.ProjectGroupDao;
import org.apache.continuum.dao.ProjectScmRootDao;
import org.apache.continuum.model.project.ProjectScmRoot;
import org.apache.continuum.utils.ContinuumUtils;
import org.apache.continuum.utils.build.BuildTrigger;
import org.apache.maven.continuum.core.action.AbstractContinuumAction;
import org.apache.maven.continuum.core.action.ExecuteBuilderContinuumAction;
import org.apache.maven.continuum.execution.ContinuumBuildExecutor;
import org.apache.maven.continuum.execution.ContinuumBuildExecutorConstants;
import org.apache.maven.continuum.execution.manager.BuildExecutorManager;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectDependency;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.scm.ChangeFile;
import org.apache.maven.continuum.model.scm.ChangeSet;
import org.apache.maven.continuum.model.scm.ScmResult;
import org.apache.maven.continuum.notification.ContinuumNotificationDispatcher;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.store.ContinuumObjectNotFoundException;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.apache.maven.continuum.utils.WorkingDirectoryService;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.codehaus.plexus.action.ActionManager;
import org.codehaus.plexus.action.ActionNotFoundException;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.taskqueue.execution.TaskExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
@Component( role = org.apache.maven.continuum.buildcontroller.BuildController.class, hint = "default" )
public class DefaultBuildController
    implements BuildController
{
    private static final Logger log = LoggerFactory.getLogger( DefaultBuildController.class );

    @Requirement
    private BuildDefinitionDao buildDefinitionDao;

    @Requirement
    private BuildResultDao buildResultDao;

    @Requirement
    private ProjectDao projectDao;

    @Requirement
    private ProjectGroupDao projectGroupDao;

    @Requirement
    private ProjectScmRootDao projectScmRootDao;

    @Requirement
    private ContinuumNotificationDispatcher notifierDispatcher;

    @Requirement
    private ActionManager actionManager;

    @Requirement
    private WorkingDirectoryService workingDirectoryService;

    @Requirement
    private BuildExecutorManager buildExecutorManager;

    // ----------------------------------------------------------------------
    // BuildController Implementation
    // ----------------------------------------------------------------------

    /**
     * @param projectId
     * @param buildDefinitionId
     * @param buildTrigger
     * @param scmResult
     * @throws TaskExecutionException
     */
    public void build( int projectId, int buildDefinitionId, BuildTrigger buildTrigger, ScmResult scmResult )
        throws TaskExecutionException
    {
        log.info( "Initializing build" );
        BuildContext context = initializeBuildContext( projectId, buildDefinitionId, buildTrigger, scmResult );

        // ignore this if AlwaysBuild ?
        if ( !checkScmResult( context ) )
        {
            log.info( "Error updating from SCM, not building" );
            return;
        }

        log.info( "Starting build of " + context.getProject().getName() );
        startBuild( context );

        try
        {
            checkProjectDependencies( context );

            if ( !shouldBuild( context ) )
            {
                return;
            }

            Map<String, Object> actionContext = context.getActionContext();

            try
            {
                performAction( "update-project-from-working-directory", context );
            }
            catch ( TaskExecutionException e )
            {
                updateBuildResult( context, ContinuumUtils.throwableToString( e ) );

                //just log the error but don't stop the build from progressing in order not to suppress any build result messages there
                log.error( "Error executing action update-project-from-working-directory '", e );
            }

            performAction( "execute-builder", context );

            performAction( "deploy-artifact", context );

            context.setCancelled( ExecuteBuilderContinuumAction.isCancelled( actionContext ) );

            String s = AbstractContinuumAction.getBuildId( actionContext, null );

            if ( s != null && !context.isCancelled() )
            {
                try
                {
                    context.setBuildResult( buildResultDao.getBuildResult( Integer.valueOf( s ) ) );
                }
                catch ( NumberFormatException e )
                {
                    throw new TaskExecutionException( "Internal error: build id not an integer", e );
                }
                catch ( ContinuumObjectNotFoundException e )
                {
                    throw new TaskExecutionException( "Internal error: Cannot find build result", e );
                }
                catch ( ContinuumStoreException e )
                {
                    throw new TaskExecutionException( "Error loading build result", e );
                }
            }
        }
        finally
        {
            endBuild( context );
        }
    }

    /**
     * Checks if the build should be marked as ERROR and notifies the end of the build.
     *
     * @param context
     * @throws TaskExecutionException
     */
    private void endBuild( BuildContext context )
        throws TaskExecutionException
    {
        Project project = context.getProject();

        try
        {
            if ( project.getState() != ContinuumProjectState.NEW &&
                project.getState() != ContinuumProjectState.CHECKEDOUT &&
                project.getState() != ContinuumProjectState.OK && project.getState() != ContinuumProjectState.FAILED &&
                project.getState() != ContinuumProjectState.ERROR && !context.isCancelled() )
            {
                try
                {
                    String s = AbstractContinuumAction.getBuildId( context.getActionContext(), null );

                    if ( s != null )
                    {
                        BuildResult buildResult = buildResultDao.getBuildResult( Integer.valueOf( s ) );
                        project.setState( buildResult.getState() );
                        projectDao.updateProject( project );
                    }
                }
                catch ( ContinuumStoreException e )
                {
                    throw new TaskExecutionException( "Error storing the project", e );
                }
            }
        }
        finally
        {
            if ( !context.isCancelled() )
            {
                notifierDispatcher.buildComplete( project, context.getBuildDefinition(), context.getBuildResult() );
            }
        }
    }

    private void updateBuildResult( BuildContext context, String error )
        throws TaskExecutionException
    {
        BuildResult build = context.getBuildResult();

        if ( build == null )
        {
            build = makeAndStoreBuildResult( context, error );
        }
        else
        {
            updateBuildResult( build, context );

            build.setError( error );

            try
            {
                buildResultDao.updateBuildResult( build );

                build = buildResultDao.getBuildResult( build.getId() );

                context.setBuildResult( build );
            }
            catch ( ContinuumStoreException e )
            {
                throw new TaskExecutionException( "Error updating build result", e );
            }
        }

        context.getProject().setState( build.getState() );

        try
        {
            projectDao.updateProject( context.getProject() );
        }
        catch ( ContinuumStoreException e )
        {
            throw new TaskExecutionException( "Error updating project", e );
        }
    }

    private void updateBuildResult( BuildResult build, BuildContext context )
    {
        if ( build.getScmResult() == null && context.getScmResult() != null )
        {
            build.setScmResult( context.getScmResult() );
        }

        if ( build.getModifiedDependencies() == null && context.getModifiedDependencies() != null )
        {
            build.setModifiedDependencies( context.getModifiedDependencies() );
        }
    }

    private void startBuild( BuildContext context )
        throws TaskExecutionException
    {

        Project project = context.getProject();

        project.setOldState( project.getState() );

        project.setState( ContinuumProjectState.BUILDING );

        try
        {
            projectDao.updateProject( project );
        }
        catch ( ContinuumStoreException e )
        {
            throw new TaskExecutionException( "Error persisting project", e );
        }

        notifierDispatcher.buildStarted( project, context.getBuildDefinition() );

    }

    /**
     * Initializes a BuildContext for the build.
     *
     * @param projectId
     * @param buildDefinitionId
     * @param buildTrigger
     * @param scmResult
     * @return
     * @throws TaskExecutionException
     */
    @SuppressWarnings( "unchecked" )
    protected BuildContext initializeBuildContext( int projectId, int buildDefinitionId, BuildTrigger buildTrigger,
                                                   ScmResult scmResult )
        throws TaskExecutionException
    {
        BuildContext context = new BuildContext();

        context.setStartTime( System.currentTimeMillis() );

        Map actionContext = context.getActionContext();

        try
        {
            Project project = projectDao.getProject( projectId );

            context.setProject( project );

            BuildDefinition buildDefinition = buildDefinitionDao.getBuildDefinition( buildDefinitionId );

            BuildTrigger newBuildTrigger = buildTrigger;

            if ( newBuildTrigger.getTrigger() == ContinuumProjectState.TRIGGER_SCHEDULED )
            {
                newBuildTrigger.setTriggeredBy( buildDefinition.getSchedule().getName() );
            }

            context.setBuildTrigger( newBuildTrigger );

            context.setBuildDefinition( buildDefinition );

            BuildResult oldBuildResult = buildResultDao.getLatestBuildResultForBuildDefinition( projectId,
                                                                                                buildDefinitionId );

            context.setOldBuildResult( oldBuildResult );

            context.setScmResult( scmResult );

            // CONTINUUM-2193
            ProjectGroup projectGroup = project.getProjectGroup();
            List<ProjectScmRoot> scmRoots = projectScmRootDao.getProjectScmRootByProjectGroup( projectGroup.getId() );
            String projectScmUrl = project.getScmUrl();
            String projectScmRootAddress = "";

            for ( ProjectScmRoot projectScmRoot : scmRoots )
            {
                projectScmRootAddress = projectScmRoot.getScmRootAddress();
                if ( projectScmUrl.startsWith( projectScmRoot.getScmRootAddress() ) )
                {
                    AbstractContinuumAction.setProjectScmRootUrl( actionContext, projectScmRoot.getScmRootAddress() );
                    break;
                }
            }

            if ( project.isCheckedOutInSingleDirectory() )
            {
                List<Project> projectsInGroup = projectGroupDao.getProjectGroupWithProjects(
                    projectGroup.getId() ).getProjects();
                List<Project> projectsWithCommonScmRoot = new ArrayList<Project>();
                for ( Project projectInGroup : projectsInGroup )
                {
                    if ( projectInGroup.getScmUrl().startsWith( projectScmRootAddress ) )
                    {
                        projectsWithCommonScmRoot.add( projectInGroup );
                    }
                }
                AbstractContinuumAction.setListOfProjectsInGroupWithCommonScmRoot( actionContext,
                                                                                   projectsWithCommonScmRoot );
            }

            // CONTINUUM-1871 olamy if continuum is killed during building oldBuildResult will have a endTime 0
            // this means all changes since the project has been loaded in continuum will be in memory
            // now we will load all BuildResult with an Id bigger or equals than the oldBuildResult one
            //if ( oldBuildResult != null )
            //{
            //    context.setOldScmResult(
            //        getOldScmResults( projectId, oldBuildResult.getBuildNumber(), oldBuildResult.getEndTime() ) );
            //}
        }
        catch ( ContinuumStoreException e )
        {
            throw new TaskExecutionException( "Error initializing the build context", e );
        }

        // Map<String, Object> actionContext = context.getActionContext();

        AbstractContinuumAction.setProjectId( actionContext, projectId );

        AbstractContinuumAction.setProject( actionContext, context.getProject() );

        AbstractContinuumAction.setBuildDefinitionId( actionContext, buildDefinitionId );

        AbstractContinuumAction.setBuildDefinition( actionContext, context.getBuildDefinition() );

        AbstractContinuumAction.setBuildTrigger( actionContext, buildTrigger );

        AbstractContinuumAction.setScmResult( actionContext, context.getScmResult() );

        if ( context.getOldBuildResult() != null )
        {
            AbstractContinuumAction.setOldBuildId( actionContext, context.getOldBuildResult().getId() );
        }

        return context;
    }

    private void performAction( String actionName, BuildContext context )
        throws TaskExecutionException
    {
        String error;
        TaskExecutionException exception;

        try
        {
            log.info( "Performing action " + actionName );
            actionManager.lookup( actionName ).execute( context.getActionContext() );
            return;
        }
        catch ( ActionNotFoundException e )
        {
            error = ContinuumUtils.throwableToString( e );
            exception = new TaskExecutionException( "Error looking up action '" + actionName + "'", e );
        }
        catch ( ScmRepositoryException e )
        {
            error = getValidationMessages( e ) + "\n" + ContinuumUtils.throwableToString( e );

            exception = new TaskExecutionException( "SCM error while executing '" + actionName + "'", e );
        }
        catch ( ScmException e )
        {
            error = ContinuumUtils.throwableToString( e );

            exception = new TaskExecutionException( "SCM error while executing '" + actionName + "'", e );
        }
        catch ( Exception e )
        {
            exception = new TaskExecutionException( "Error executing action '" + actionName + "'", e );
            error = ContinuumUtils.throwableToString( exception );
        }

        // TODO: clean this up. We catch the original exception from the action, and then update the buildresult
        // for it - we need to because of the specialized error message for SCM.
        // If updating the buildresult fails, log the previous error and throw the new one.
        // If updating the buildresult succeeds, throw the original exception. The build result should NOT
        // be updated again - a TaskExecutionException is final, no further action should be taken upon it.

        try
        {
            updateBuildResult( context, error );
        }
        catch ( TaskExecutionException e )
        {
            log.error( "Error updating build result after receiving the following exception: ", exception );
            throw e;
        }

        throw exception;
    }

    protected boolean shouldBuild( BuildContext context )
        throws TaskExecutionException
    {
        BuildDefinition buildDefinition = context.getBuildDefinition();
        if ( buildDefinition.isAlwaysBuild() )
        {
            log.info( "AlwaysBuild configured, building" );
            return true;
        }
        if ( context.getOldBuildResult() == null )
        {
            log.info( "The project has never been built with the current build definition, building" );
            return true;
        }

        Project project = context.getProject();

        //CONTINUUM-1428
        if ( project.getOldState() == ContinuumProjectState.ERROR ||
            context.getOldBuildResult().getState() == ContinuumProjectState.ERROR )
        {
            log.info( "Latest state was 'ERROR', building" );
            return true;
        }

        if ( context.getBuildTrigger().getTrigger() == ContinuumProjectState.TRIGGER_FORCED )
        {
            log.info( "The project build is forced, building" );
            return true;
        }

        boolean shouldBuild = false;

        boolean allChangesUnknown = true;

        if ( project.getOldState() != ContinuumProjectState.NEW &&
            project.getOldState() != ContinuumProjectState.CHECKEDOUT &&
            context.getBuildTrigger().getTrigger() != ContinuumProjectState.TRIGGER_FORCED &&
            project.getState() != ContinuumProjectState.NEW && project.getState() != ContinuumProjectState.CHECKEDOUT )
        {
            // Check SCM changes
            if ( context.getScmResult() != null )
            {
                allChangesUnknown = checkAllChangesUnknown( context.getScmResult().getChanges() );
            }

            if ( allChangesUnknown )
            {
                if ( context.getScmResult() != null && !context.getScmResult().getChanges().isEmpty() )
                {
                    log.info(
                        "The project was not built because all changes are unknown (maybe local modifications or ignored files not defined in your SCM tool." );
                }
                else
                {
                    log.info(
                        "The project was not built because no changes were detected in sources since the last build." );
                }
            }

            // Check dependencies changes
            if ( context.getModifiedDependencies() != null && !context.getModifiedDependencies().isEmpty() )
            {
                log.info( "Found dependencies changes, building" );
                shouldBuild = true;
            }
        }

        // Check changes
        if ( !shouldBuild && ( ( !allChangesUnknown && context.getScmResult() != null &&
            !context.getScmResult().getChanges().isEmpty() ) || project.getExecutorId().equals(
            ContinuumBuildExecutorConstants.MAVEN_TWO_BUILD_EXECUTOR ) ) )
        {
            try
            {
                ContinuumBuildExecutor executor = buildExecutorManager.getBuildExecutor( project.getExecutorId() );

                Map<String, Object> actionContext = context.getActionContext();
                List<Project> projectsWithCommonScmRoot =
                    AbstractContinuumAction.getListOfProjectsInGroupWithCommonScmRoot( actionContext );
                String projectScmRootUrl = AbstractContinuumAction.getProjectScmRootUrl( actionContext,
                                                                                         project.getScmUrl() );

                if ( executor == null )
                {
                    log.warn( "No continuum build executor found for project " + project.getId() +
                                  " with executor '" + project.getExecutorId() + "'" );
                }
                else if ( context.getScmResult() != null )
                {
                    shouldBuild = executor.shouldBuild( context.getScmResult().getChanges(), project,
                                                        workingDirectoryService.getWorkingDirectory( project,
                                                                                                     projectScmRootUrl,
                                                                                                     projectsWithCommonScmRoot ),
                                                        context.getBuildDefinition() );
                }
            }
            catch ( Exception e )
            {
                updateBuildResult( context, ContinuumUtils.throwableToString( e ) );
                throw new TaskExecutionException( "Can't determine if the project should build or not", e );
            }
        }

        if ( shouldBuild )
        {
            log.info( "Changes found in the current project, building" );
        }
        else
        {
            project.setState( project.getOldState() );

            project.setOldState( 0 );

            try
            {
                projectDao.updateProject( project );
            }
            catch ( ContinuumStoreException e )
            {
                throw new TaskExecutionException( "Error storing project", e );
            }
            log.info( "No changes in the current project, not building" );

        }

        return shouldBuild;
    }

    private boolean checkAllChangesUnknown( List<ChangeSet> changes )
    {
        for ( ChangeSet changeSet : changes )
        {
            List<ChangeFile> changeFiles = changeSet.getFiles();

            for ( ChangeFile changeFile : changeFiles )
            {
                if ( !"unknown".equalsIgnoreCase( changeFile.getStatus() ) )
                {
                    return false;
                }
            }
        }

        return true;
    }

    private String getValidationMessages( ScmRepositoryException ex )
    {
        List<String> messages = ex.getValidationMessages();

        StringBuffer message = new StringBuffer();

        if ( messages != null && !messages.isEmpty() )
        {
            for ( Iterator<String> i = messages.iterator(); i.hasNext(); )
            {
                message.append( i.next() );

                if ( i.hasNext() )
                {
                    message.append( System.getProperty( "line.separator" ) );
                }
            }
        }
        return message.toString();
    }

    protected void checkProjectDependencies( BuildContext context )
    {
        if ( context.getOldBuildResult() == null )
        {
            return;
        }

        try
        {
            Project project = projectDao.getProjectWithDependencies( context.getProject().getId() );
            List<ProjectDependency> dependencies = project.getDependencies();

            if ( dependencies == null )
            {
                dependencies = new ArrayList<ProjectDependency>();
            }

            if ( project.getParent() != null )
            {
                dependencies.add( project.getParent() );
            }

            if ( dependencies.isEmpty() )
            {
                return;
            }

            List<ProjectDependency> modifiedDependencies = new ArrayList<ProjectDependency>();

            for ( ProjectDependency dep : dependencies )
            {
                Project dependencyProject = projectDao.getProject( dep.getGroupId(), dep.getArtifactId(),
                                                                   dep.getVersion() );

                if ( dependencyProject != null )
                {
                    long nbBuild = buildResultDao.getNbBuildResultsInSuccessForProject( dependencyProject.getId(),
                                                                                        context.getOldBuildResult().getEndTime() );
                    if ( nbBuild > 0 )
                    {
                        log.debug( "Dependency changed: " + dep.getGroupId() + ":" + dep.getArtifactId() + ":" +
                                       dep.getVersion() );
                        modifiedDependencies.add( dep );
                    }
                    else
                    {
                        log.debug( "Dependency not changed: " + dep.getGroupId() + ":" + dep.getArtifactId() + ":" +
                                       dep.getVersion() );
                    }
                }
                else
                {
                    log.debug( "Skip non Continuum project: " + dep.getGroupId() + ":" + dep.getArtifactId() + ":" +
                                   dep.getVersion() );
                }
            }

            context.setModifiedDependencies( modifiedDependencies );
            AbstractContinuumAction.setUpdatedDependencies( context.getActionContext(), modifiedDependencies );
        }
        catch ( ContinuumStoreException e )
        {
            log.warn( "Can't get the project dependencies", e );
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private BuildResult makeAndStoreBuildResult( BuildContext context, String error )
        throws TaskExecutionException
    {
        // Project project, ScmResult scmResult, long startTime, int trigger )
        // project, scmResult, startTime, trigger );

        BuildResult build = new BuildResult();

        build.setState( ContinuumProjectState.ERROR );

        build.setTrigger( context.getBuildTrigger().getTrigger() );

        build.setUsername( context.getBuildTrigger().getTriggeredBy() );

        build.setStartTime( context.getStartTime() );

        build.setEndTime( System.currentTimeMillis() );

        updateBuildResult( build, context );

        build.setScmResult( context.getScmResult() );

        build.setBuildDefinition( context.getBuildDefinition() );

        if ( error != null )
        {
            build.setError( error );
        }

        try
        {
            buildResultDao.addBuildResult( context.getProject(), build );

            build = buildResultDao.getBuildResult( build.getId() );

            context.setBuildResult( build );

            return build;
        }
        catch ( ContinuumStoreException e )
        {
            throw new TaskExecutionException( "Error storing build result", e );
        }
    }

    /**
     * Check to see if there was a error while checking out/updating the project
     *
     * @param context The build context
     * @return true if scm result is ok
     * @throws TaskExecutionException
     */
    private boolean checkScmResult( BuildContext context )
        throws TaskExecutionException
    {
        Project project = context.getProject();

        int projectGroupId = project.getProjectGroup().getId();

        List<ProjectScmRoot> scmRoots = projectScmRootDao.getProjectScmRootByProjectGroup( projectGroupId );

        for ( ProjectScmRoot projectScmRoot : scmRoots )
        {
            if ( project.getScmUrl().startsWith( projectScmRoot.getScmRootAddress() ) )
            {
                if ( projectScmRoot.getState() == ContinuumProjectState.UPDATED )
                {
                    return true;
                }

                break;
            }
        }

        return false;
    }
}
