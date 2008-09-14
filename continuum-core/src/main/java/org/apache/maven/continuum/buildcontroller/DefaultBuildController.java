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
import org.apache.maven.continuum.core.action.AbstractContinuumAction;
import org.apache.maven.continuum.execution.ContinuumBuildExecutor;
import org.apache.maven.continuum.execution.manager.BuildExecutorManager;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectDependency;
import org.apache.maven.continuum.model.scm.ChangeFile;
import org.apache.maven.continuum.model.scm.ChangeSet;
//import org.apache.maven.continuum.model.scm.ScmResult;
import org.apache.maven.continuum.notification.ContinuumNotificationDispatcher;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.store.ContinuumObjectNotFoundException;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.apache.maven.continuum.utils.ContinuumUtils;
import org.apache.maven.continuum.utils.WorkingDirectoryService;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.codehaus.plexus.action.ActionManager;
import org.codehaus.plexus.action.ActionNotFoundException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.taskqueue.execution.TaskExecutionException;
//import org.codehaus.plexus.util.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 * @plexus.component role="org.apache.maven.continuum.buildcontroller.BuildController" role-hint="default"
 */
public class DefaultBuildController
    extends AbstractLogEnabled
    implements BuildController
{
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
    private ProjectDao projectDao;
    
    /**
     * @plexus.requirement
     */
    private ProjectGroupDao projectGroupDao;
    
    /**
     * @plexus.requirement
     */
    private ProjectScmRootDao projectScmRootDao;

    /**
     * @plexus.requirement
     */
    private ContinuumNotificationDispatcher notifierDispatcher;

    /**
     * @plexus.requirement
     */
    private ActionManager actionManager;

    /**
     * @plexus.requirement
     */
    private WorkingDirectoryService workingDirectoryService;

    /**
     * @plexus.requirement
     */
    private BuildExecutorManager buildExecutorManager;

    // ----------------------------------------------------------------------
    // BuildController Implementation
    // ----------------------------------------------------------------------

    /**
     * @param projectId
     * @param buildDefinitionId
     * @param trigger
     * @throws TaskExecutionException
     */
    public void build( int projectId, int buildDefinitionId, int trigger )
        throws TaskExecutionException
    {
        getLogger().info( "Initializing build" );
        BuildContext context = initializeBuildContext( projectId, buildDefinitionId, trigger );

        // ignore this if AlwaysBuild ?
        if ( !checkScmResult( context ) )
        {
            getLogger().info( "Error updating from SCM, not building" );
            return;
        }
        
        getLogger().info( "Starting build of " + context.getProject().getName() );
        startBuild( context );

        try
        {
            // check if build definition requires smoking the existing checkout and rechecking out project
            //if ( context.getBuildDefinition().isBuildFresh() )
            //{
            //    getLogger().info( "Purging exiting working copy" );
            //    cleanWorkingDirectory( context );
            //}

            // ----------------------------------------------------------------------
            // TODO: Centralize the error handling from the SCM related actions.
            // ContinuumScmResult should return a ContinuumScmResult from all
            // methods, even in a case of failure.
            // ----------------------------------------------------------------------
            //getLogger().info( "Updating working dir" );
            //updateWorkingDirectory( context );

            //getLogger().info( "Merging SCM results" );
            //CONTINUUM-1393
            //if ( !context.getBuildDefinition().isBuildFresh() )
            //{
            //    mergeScmResults( context );
            //}

            checkProjectDependencies( context );

            if ( !shouldBuild( context ) )
            {
                return;
            }

            Map actionContext = context.getActionContext();

            try
            {
                performAction( "update-project-from-working-directory", context );
            }
            catch ( TaskExecutionException e )
            {
                //just log the error but don't stop the build from progressing in order not to suppress any build result messages there 
                getLogger().error( "Error executing action update-project-from-working-directory '", e );
            }

            performAction( "execute-builder", context );
            
            performAction( "deploy-artifact", context );

            context.setCancelled( (Boolean) actionContext.get( AbstractContinuumAction.KEY_CANCELLED ) );
            
            String s = (String) actionContext.get( AbstractContinuumAction.KEY_BUILD_ID );

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
                    String s = (String) context.getActionContext().get( AbstractContinuumAction.KEY_BUILD_ID );

                    if ( s != null )
                    {
                        BuildResult buildResult = buildResultDao.getBuildResult( Integer.valueOf( s ) );
                        project.setState( buildResult.getState() );
                    }
                    else
                    {
                        project.setState( ContinuumProjectState.ERROR );
                    }

                    projectDao.updateProject( project );
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
        //if ( build.getScmResult() == null && context.getScmResult() != null )
        //{
        //    build.setScmResult( context.getScmResult() );
        //}

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
     * @param trigger
     * @return
     * @throws TaskExecutionException
     */
    protected BuildContext initializeBuildContext( int projectId, int buildDefinitionId, int trigger )
        throws TaskExecutionException
    {
        BuildContext context = new BuildContext();

        context.setStartTime( System.currentTimeMillis() );

        context.setTrigger( trigger );

        try
        {
            Project project = projectDao.getProjectWithScmDetails( projectId );
            
            context.setProject( project );

            BuildDefinition buildDefinition = buildDefinitionDao.getBuildDefinition( buildDefinitionId );

            context.setBuildDefinition( buildDefinition );

            BuildResult oldBuildResult =
                buildResultDao.getLatestBuildResultForBuildDefinition( projectId, buildDefinitionId );

            context.setOldBuildResult( oldBuildResult );
            
            context.setScmResult( project.getScmResult() );

            //if ( oldBuildResult != null )
            //{
            //    context.setOldScmResult( getOldScmResult( projectId, oldBuildResult.getEndTime() ) );
            //}
        }
        catch ( ContinuumStoreException e )
        {
            throw new TaskExecutionException( "Error initializing the build context", e );
        }

        Map actionContext = context.getActionContext();

        actionContext.put( AbstractContinuumAction.KEY_PROJECT_ID, projectId );

        actionContext.put( AbstractContinuumAction.KEY_PROJECT, context.getProject() );

        actionContext.put( AbstractContinuumAction.KEY_BUILD_DEFINITION_ID, buildDefinitionId );

        actionContext.put( AbstractContinuumAction.KEY_BUILD_DEFINITION, context.getBuildDefinition() );

        actionContext.put( AbstractContinuumAction.KEY_TRIGGER, trigger );

        actionContext.put( AbstractContinuumAction.KEY_FIRST_RUN, context.getOldBuildResult() == null );

        if ( context.getOldBuildResult() != null )
        {
            actionContext.put( AbstractContinuumAction.KEY_OLD_BUILD_ID, context.getOldBuildResult().getId() );
        }
        
        return context;
    }
/*
    private void cleanWorkingDirectory( BuildContext context )
        throws TaskExecutionException
    {
        performAction( "clean-working-directory", context );
    }

    private void updateWorkingDirectory( BuildContext context )
        throws TaskExecutionException
    {
        Map actionContext = context.getActionContext();

        performAction( "check-working-directory", context );

        boolean workingDirectoryExists =
            AbstractContinuumAction.getBoolean( actionContext, AbstractContinuumAction.KEY_WORKING_DIRECTORY_EXISTS );

        ScmResult scmResult;

        if ( workingDirectoryExists )
        {
            performAction( "update-working-directory-from-scm", context );

            scmResult = AbstractContinuumAction.getUpdateScmResult( actionContext, null );
        }
        else
        {
            Project project = (Project) actionContext.get( AbstractContinuumAction.KEY_PROJECT );

            actionContext.put( AbstractContinuumAction.KEY_WORKING_DIRECTORY,
                               workingDirectoryService.getWorkingDirectory( project ).getAbsolutePath() );

            performAction( "checkout-project", context );

            scmResult = AbstractContinuumAction.getCheckoutResult( actionContext, null );
        }

        context.setScmResult( scmResult );
    }
*/
    private void performAction( String actionName, BuildContext context )
        throws TaskExecutionException
    {
        String error = null;
        TaskExecutionException exception = null;

        try
        {
            getLogger().info( "Performing action " + actionName );
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
            getLogger().error( "Error updating build result after receiving the following exception: ", exception );
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
            getLogger().info( "AlwaysBuild configured, building" );
            return true;
        }
        if ( context.getOldBuildResult() == null )
        {
            getLogger().info( "The project was never be built with the current build definition, building" );
            return true;
        }

        Project project = context.getProject();

        //CONTINUUM-1428
        if ( project.getOldState() == ContinuumProjectState.ERROR ||
            context.getOldBuildResult().getState() == ContinuumProjectState.ERROR )
        {
            getLogger().info( "Latest state was 'ERROR', building" );
            return true;
        }

        if ( context.getTrigger() == ContinuumProjectState.TRIGGER_FORCED )
        {
            getLogger().info( "The project build is forced, building" );
            return true;
        }

        boolean shouldBuild = false;

        boolean allChangesUnknown = true;

        if ( project.getOldState() != ContinuumProjectState.NEW &&
            project.getOldState() != ContinuumProjectState.CHECKEDOUT &&
            context.getTrigger() != ContinuumProjectState.TRIGGER_FORCED &&
            project.getState() != ContinuumProjectState.NEW && project.getState() != ContinuumProjectState.CHECKEDOUT )
        {
            // Check SCM changes
            allChangesUnknown = checkAllChangesUnknown( context.getScmResult().getChanges() );

            if ( allChangesUnknown )
            {
                if ( !context.getScmResult().getChanges().isEmpty() )
                {
                    getLogger().info(
                        "The project was not built because all changes are unknown (maybe local modifications or ignored files not defined in your SCM tool." );
                }
                else
                {
                    getLogger().info(
                        "The project was not built because no changes were detected in sources since the last build." );
                }
            }

            // Check dependencies changes
            if ( context.getModifiedDependencies() != null && !context.getModifiedDependencies().isEmpty() )
            {
                getLogger().info( "Found dependencies changes, building" );
                shouldBuild = true;
            }
        }

        // Check changes
        if ( !shouldBuild && !allChangesUnknown && !context.getScmResult().getChanges().isEmpty() )
        {
            try
            {
                ContinuumBuildExecutor executor = buildExecutorManager.getBuildExecutor( project.getExecutorId() );
                shouldBuild = executor.shouldBuild( context.getScmResult().getChanges(), project,
                                                    workingDirectoryService.getWorkingDirectory( project ),
                                                    context.getBuildDefinition() );
            }
            catch ( Exception e )
            {
                throw new TaskExecutionException( "Can't determine if the project should build or not", e );
            }
        }

        if ( shouldBuild )
        {
            getLogger().info( "Changes found in the current project, building" );
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
            getLogger().info( "No changes in the current project, not building" );

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
            Project project = projectDao.getProjectWithAllDetails( context.getProject().getId() );
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
                Project dependencyProject =
                    projectDao.getProject( dep.getGroupId(), dep.getArtifactId(), dep.getVersion() );

                if ( dependencyProject != null )
                {
                    List buildResults = buildResultDao.getBuildResultsInSuccessForProject( dependencyProject.getId(),
                                                                                           context.getOldBuildResult().getEndTime() );
                    if ( buildResults != null && !buildResults.isEmpty() )
                    {
                        getLogger().debug( "Dependency changed: " + dep.getGroupId() + ":" + dep.getArtifactId() + ":" +
                            dep.getVersion() );
                        modifiedDependencies.add( dep );
                    }
                    else
                    {
                        getLogger().debug( "Dependency not changed: " + dep.getGroupId() + ":" + dep.getArtifactId() +
                            ":" + dep.getVersion() );
                    }
                }
                else
                {
                    getLogger().debug( "Skip non Continuum project: " + dep.getGroupId() + ":" + dep.getArtifactId() +
                        ":" + dep.getVersion() );
                }
            }

            context.setModifiedDependencies( modifiedDependencies );
            context.getActionContext().put( AbstractContinuumAction.KEY_UPDATE_DEPENDENCIES, modifiedDependencies );
        }
        catch ( ContinuumStoreException e )
        {
            getLogger().warn( "Can't get the project dependencies", e );
        }
    }

    /*
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
*/
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

        build.setTrigger( context.getTrigger() );

        build.setStartTime( context.getStartTime() );

        build.setEndTime( System.currentTimeMillis() );

        updateBuildResult( build, context );

        //build.setScmResult( context.getScmResult() );

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
/*
    private ScmResult getOldScmResult( int projectId, long fromDate )
    {
        List<BuildResult> results = buildResultDao.getBuildResultsForProject( projectId, fromDate );

        ScmResult res = new ScmResult();

        if ( results != null )
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
/*    private void mergeScmResults( BuildContext context )
    {
        ScmResult oldScmResult = context.getOldScmResult();
        ScmResult newScmResult = context.getScmResult();

        if ( oldScmResult != null )
        {
            if ( newScmResult == null )
            {
                context.setScmResult( oldScmResult );
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
*/
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
        
        
        /*
        ScmResult scmResult = context.getScmResult();

        if ( scmResult == null || !scmResult.isSuccess() )
        {
            // scmResult must be converted before storing it because jpox modifies values of all fields to null
            String error = convertScmResultToError( scmResult );

            BuildResult build = makeAndStoreBuildResult( context, error );

            try
            {
                Project project = context.getProject();

                project.setState( build.getState() );

                projectDao.updateProject( project );

                return false;
            }
            catch ( ContinuumStoreException e )
            {
                throw new TaskExecutionException( "Error storing project", e );
            }
        }
        
        context.getActionContext().put( AbstractContinuumAction.KEY_UPDATE_SCM_RESULT, scmResult );
*/
    }

}
