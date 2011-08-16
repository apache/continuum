package org.apache.continuum.buildagent.taskqueue.execution;

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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.continuum.buildagent.buildcontext.BuildContext;
import org.apache.continuum.buildagent.configuration.BuildAgentConfigurationService;
import org.apache.continuum.buildagent.manager.BuildAgentManager;
import org.apache.continuum.buildagent.taskqueue.PrepareBuildProjectsTask;
import org.apache.continuum.buildagent.utils.BuildContextToBuildDefinition;
import org.apache.continuum.buildagent.utils.BuildContextToProject;
import org.apache.continuum.buildagent.utils.ContinuumBuildAgentUtil;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.scm.ChangeSet;
import org.apache.maven.continuum.model.scm.ScmResult;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.codehaus.plexus.action.ActionManager;
import org.codehaus.plexus.action.ActionNotFoundException;
import org.codehaus.plexus.taskqueue.Task;
import org.codehaus.plexus.taskqueue.execution.TaskExecutionException;
import org.codehaus.plexus.taskqueue.execution.TaskExecutor;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @plexus.component role="org.codehaus.plexus.taskqueue.execution.TaskExecutor"
 * role-hint="prepare-build-agent"
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
    private BuildAgentConfigurationService buildAgentConfigurationService;

    /**
     * @plexus.requirement
     */
    private BuildAgentManager buildAgentManager;

    public void executeTask( Task task )
        throws TaskExecutionException
    {
        List<BuildContext> buildContexts = ( (PrepareBuildProjectsTask) task ).getBuildContexts();

        Map<String, Object> context = null;

        try
        {
            if ( buildContexts != null && buildContexts.size() > 0 )
            {
                try
                {
                    for ( BuildContext buildContext : buildContexts )
                    {
                        BuildDefinition buildDef = BuildContextToBuildDefinition.getBuildDefinition( buildContext );
    
                        log.debug( "Check scm root state of project group '{}'",  buildContext.getProjectGroupName() );
                        if ( !checkProjectScmRoot( context ) )
                        {
                            break;
                        }
    
                        log.info( "Starting prepare build of project group '{}'", buildContext.getProjectGroupName() );
                        startPrepareBuild( buildContext );
    
                        log.info( "Initializing prepare build" );
                        initializeActionContext( buildContext );
    
                        try
                        {
                            if ( buildDef.isBuildFresh() )
                            {
                                log.info( "Clean up working directory of project '{}'", buildContext.getProjectName() );
                                cleanWorkingDirectory( buildContext );
                            }
    
                            log.info( "Updating working directory of project '{}'", buildContext.getProjectName() );
                            updateWorkingDirectory( buildContext );
    
                            //CONTINUUM-1393
                            if ( !buildDef.isBuildFresh() )
                            {
                                log.info( "Merging SCM results of project '{}'", buildContext.getProjectName() );
                                mergeScmResults( buildContext );
                            }
                        }
                        finally
                        {
                            endProjectPrepareBuild( buildContext );
                            context = buildContext.getActionContext();
                        }
                    }
                }
                finally
                {
                    endPrepareBuild( context );
                }
    
                if ( checkProjectScmRoot( context ) )
                {
                    log.debug( "Successful prepare build. Creating build task" );
                    buildProjects( buildContexts );
                }
            }
            else
            {
                throw new TaskExecutionException( "No project build context" );
            }
        }
        catch ( TaskExecutionException e )
        {
            log.error( "Error while preparing build of project: {}", e.getMessage() );
        }
    }

    private void startPrepareBuild( BuildContext buildContext )
        throws TaskExecutionException
    {
        Map<String, Object> actionContext = buildContext.getActionContext();

        if ( actionContext == null ||
            !( ContinuumBuildAgentUtil.getScmRootState( actionContext ) == ContinuumProjectState.UPDATING ) )
        {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put( ContinuumBuildAgentUtil.KEY_PROJECT_GROUP_ID, buildContext.getProjectGroupId() );
            map.put( ContinuumBuildAgentUtil.KEY_SCM_ROOT_ADDRESS, buildContext.getScmRootAddress() );
            map.put( ContinuumBuildAgentUtil.KEY_BUILD_AGENT_URL, buildContext.getBuildAgentUrl() );

            try
            {
                buildAgentManager.startPrepareBuild( map );
            }
            catch ( ContinuumException e )
            {
                throw new TaskExecutionException( e.getMessage(), e );
            }
        }
    }

    private void initializeActionContext( BuildContext buildContext )
    {
        Map<String, Object> actionContext = new HashMap<String, Object>();

        actionContext.put( ContinuumBuildAgentUtil.KEY_PROJECT_ID, buildContext.getProjectId() );
        actionContext.put( ContinuumBuildAgentUtil.KEY_PROJECT, BuildContextToProject.getProject( buildContext ) );
        actionContext.put( ContinuumBuildAgentUtil.KEY_BUILD_DEFINITION,
                           BuildContextToBuildDefinition.getBuildDefinition( buildContext ) );
        actionContext.put( ContinuumBuildAgentUtil.KEY_SCM_ROOT_STATE, ContinuumProjectState.UPDATING );
        actionContext.put( ContinuumBuildAgentUtil.KEY_PROJECT_GROUP_ID, buildContext.getProjectGroupId() );
        actionContext.put( ContinuumBuildAgentUtil.KEY_SCM_ROOT_ADDRESS, buildContext.getScmRootAddress() );
        actionContext.put( ContinuumBuildAgentUtil.KEY_OLD_SCM_RESULT, buildContext.getOldScmResult() );
        actionContext.put( ContinuumBuildAgentUtil.KEY_LATEST_UPDATE_DATE, buildContext.getLatestUpdateDate() );
        actionContext.put( ContinuumBuildAgentUtil.KEY_TRIGGER, buildContext.getTrigger() );
        actionContext.put( ContinuumBuildAgentUtil.KEY_USERNAME, buildContext.getUsername() );
        actionContext.put( ContinuumBuildAgentUtil.KEY_SCM_USERNAME, buildContext.getScmUsername() );
        actionContext.put( ContinuumBuildAgentUtil.KEY_SCM_PASSWORD, buildContext.getScmPassword() );
        actionContext.put( ContinuumBuildAgentUtil.KEY_BUILD_AGENT_URL, buildContext.getBuildAgentUrl() );

        buildContext.setActionContext( actionContext );
    }

    private boolean checkProjectScmRoot( Map<String, Object> context )
    {
        return !( context != null &&
            ContinuumBuildAgentUtil.getScmRootState( context ) == ContinuumProjectState.ERROR );

    }

    private void cleanWorkingDirectory( BuildContext buildContext )
        throws TaskExecutionException
    {
        performAction( "clean-agent-working-directory", buildContext );
    }

    private void updateWorkingDirectory( BuildContext buildContext )
        throws TaskExecutionException
    {
        Map<String, Object> actionContext = buildContext.getActionContext();

        performAction( "check-agent-working-directory", buildContext );

        boolean workingDirectoryExists =
            ContinuumBuildAgentUtil.getBoolean( actionContext, ContinuumBuildAgentUtil.KEY_WORKING_DIRECTORY_EXISTS );

        ScmResult scmResult;

        Date date;

        if ( workingDirectoryExists )
        {
            performAction( "update-agent-working-directory", buildContext );

            scmResult = ContinuumBuildAgentUtil.getUpdateScmResult( actionContext, null );

            date = ContinuumBuildAgentUtil.getLatestUpdateDate( actionContext );
        }
        else
        {
            Project project = ContinuumBuildAgentUtil.getProject( actionContext );

            actionContext.put( ContinuumBuildAgentUtil.KEY_WORKING_DIRECTORY,
                               buildAgentConfigurationService.getWorkingDirectory(
                                   project.getId() ).getAbsolutePath() );

            performAction( "checkout-agent-project", buildContext );

            scmResult = ContinuumBuildAgentUtil.getCheckoutScmResult( actionContext, null );

            performAction( "changelog-agent-project", buildContext );

            date = ContinuumBuildAgentUtil.getLatestUpdateDate( actionContext );
        }

        buildContext.setScmResult( scmResult );
        buildContext.setLatestUpdateDate( date );
        actionContext.put( ContinuumBuildAgentUtil.KEY_SCM_RESULT, scmResult );
    }

    private void endProjectPrepareBuild( BuildContext buildContext )
        throws TaskExecutionException
    {
        Map<String, Object> context = buildContext.getActionContext();

        ScmResult scmResult = ContinuumBuildAgentUtil.getScmResult( context, null );

        log.debug( "End prepare build of project '{}'", buildContext.getProjectName() );

        if ( scmResult == null || !scmResult.isSuccess() )
        {
            context.put( ContinuumBuildAgentUtil.KEY_SCM_ROOT_STATE, ContinuumProjectState.ERROR );
        }
        else
        {
            buildContext.setScmResult( scmResult );
        }
    }

    private void endPrepareBuild( Map<String, Object> context )
        throws TaskExecutionException
    {
        if ( context != null )
        {
            Map<String, Object> result = new HashMap<String, Object>();
            result.put( ContinuumBuildAgentUtil.KEY_PROJECT_GROUP_ID,
                        ContinuumBuildAgentUtil.getProjectGroupId( context ) );
            result.put( ContinuumBuildAgentUtil.KEY_SCM_ROOT_ADDRESS,
                        ContinuumBuildAgentUtil.getScmRootAddress( context ) );
            result.put( ContinuumBuildAgentUtil.KEY_SCM_ROOT_STATE,
                        ContinuumBuildAgentUtil.getScmRootState( context ) );
            result.put( ContinuumBuildAgentUtil.KEY_BUILD_AGENT_URL,
                        ContinuumBuildAgentUtil.getBuildAgentUrl( context ) );

            if ( ContinuumBuildAgentUtil.getScmRootState( context ) == ContinuumProjectState.ERROR )
            {
                String error = convertScmResultToError( ContinuumBuildAgentUtil.getScmResult( context, null ) );

                if ( StringUtils.isEmpty( error ) )
                {
                    result.put( ContinuumBuildAgentUtil.KEY_SCM_ERROR, "" );
                }
                else
                {
                    result.put( ContinuumBuildAgentUtil.KEY_SCM_ERROR, error );
                }
            }
            else
            {
                result.put( ContinuumBuildAgentUtil.KEY_SCM_ERROR, "" );
            }

            try
            {
                log.debug( "End prepare build of project group '{}'", ContinuumBuildAgentUtil.getProjectGroupId( context ) );
                buildAgentManager.endPrepareBuild( result );
            }
            catch ( ContinuumException e )
            {
                throw new TaskExecutionException( e.getMessage(), e );
            }
        }
        else
        {
            throw new TaskExecutionException( "No project build context" );
        }
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

    private void performAction( String actionName, BuildContext buildContext )
        throws TaskExecutionException
    {
        TaskExecutionException exception;

        try
        {
            log.info( "Performing action " + actionName );
            actionManager.lookup( actionName ).execute( buildContext.getActionContext() );
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

        result.setException( ContinuumBuildAgentUtil.throwableToString( exception ) );

        buildContext.setScmResult( result );
        buildContext.getActionContext().put( ContinuumBuildAgentUtil.KEY_UPDATE_SCM_RESULT, result );

        throw exception;
    }

    private void mergeScmResults( BuildContext buildContext )
    {
        Map<String, Object> context = buildContext.getActionContext();
        ScmResult oldScmResult = ContinuumBuildAgentUtil.getOldScmResult( context, null );
        ScmResult newScmResult = ContinuumBuildAgentUtil.getScmResult( context, null );

        if ( oldScmResult != null )
        {
            if ( newScmResult == null )
            {
                context.put( ContinuumBuildAgentUtil.KEY_SCM_RESULT, oldScmResult );
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

    private void buildProjects( List<BuildContext> buildContexts )
        throws TaskExecutionException
    {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put( ContinuumBuildAgentUtil.KEY_BUILD_CONTEXTS, buildContexts );

        BuildContext context = new BuildContext();
        context.setActionContext( map );

        performAction( "create-agent-build-project-task", context );
    }
}
