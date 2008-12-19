package org.apache.continuum.buildagent.taskqueue.execution;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.continuum.buildagent.buildcontext.BuildContext;
import org.apache.continuum.buildagent.buildcontext.manager.BuildContextManager;
import org.apache.continuum.buildagent.configuration.BuildAgentConfigurationService;
import org.apache.continuum.buildagent.manager.BuildAgentManager;
import org.apache.continuum.buildagent.utils.BuildContextToBuildDefinition;
import org.apache.continuum.buildagent.utils.BuildContextToProject;
import org.apache.continuum.buildagent.utils.ContinuumBuildAgentUtil;
import org.apache.continuum.taskqueue.BuildProjectTask;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.codehaus.plexus.action.ActionManager;
import org.codehaus.plexus.action.ActionNotFoundException;
import org.codehaus.plexus.taskqueue.Task;
import org.codehaus.plexus.taskqueue.execution.TaskExecutionException;
import org.codehaus.plexus.taskqueue.execution.TaskExecutor;
import org.codehaus.plexus.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @plexus.component role="org.codehaus.plexus.taskqueue.execution.TaskExecutor"
 * role-hint="build-agent"
 */
public class BuildProjectTaskExecutor
    implements TaskExecutor
{
    private Logger log = LoggerFactory.getLogger( this.getClass() );

    /**
     * @plexus.requirement
     */
    private BuildContextManager buildContextManager;

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
        BuildProjectTask buildProjectTask = (BuildProjectTask) task;

        int projectId = buildProjectTask.getProjectId();

        log.info( "Initializing build" );
        BuildContext context = buildContextManager.getBuildContext( projectId );
        initializeBuildContext( context );

        if ( !checkScmResult( context ) )
        {
            log.info( "Error updating from SCM, not building" );
            return;
        }
        
        log.info( "Starting build of " + context.getProjectName() );
        startBuild( context );

        try
        {
            Map actionContext = context.getActionContext();

            performAction( "execute-agent-builder", context );

            updateBuildResult( context, null );
        }
        finally
        {
            endBuild( context );
        }
    }

    private void initializeBuildContext( BuildContext buildContext )
    {
        Map<String, Object> actionContext = buildContext.getActionContext();

        actionContext.put( ContinuumBuildAgentUtil.KEY_PROJECT_ID, buildContext.getProjectId() );
        actionContext.put( ContinuumBuildAgentUtil.KEY_PROJECT, BuildContextToProject.getProject( buildContext ) );
        actionContext.put( ContinuumBuildAgentUtil.KEY_BUILD_DEFINITION, BuildContextToBuildDefinition.getBuildDefinition( buildContext ) );
        actionContext.put( ContinuumBuildAgentUtil.KEY_TRIGGER, buildContext.getTrigger() );

        buildContext.setBuildStartTime( System.currentTimeMillis() );
    }
    
    private boolean checkScmResult( BuildContext buildContext )
    {
        if ( buildContext.getScmResult() == null || !buildContext.getScmResult().isSuccess() )
        {
            return false;
        }

        return true;
    }

    private void startBuild( BuildContext buildContext )
        throws TaskExecutionException
    {
        try
        {
            buildAgentManager.startProjectBuild( buildContext.getProjectId() );
        }
        catch ( ContinuumException e )
        {
            // do not throw exception, just log?
            log.error( "Failed to start project '" + buildContext.getProjectName() + "'", e );
            throw new TaskExecutionException( "Failed to start project '" + buildContext.getProjectName() + "'", e );
        }
    }

    private void endBuild( BuildContext buildContext )
        throws TaskExecutionException
    {
        // return build result to master
        BuildResult buildResult = buildContext.getBuildResult();

        Map<String, Object> result = new HashMap<String, Object>();
        result.put( ContinuumBuildAgentUtil.KEY_PROJECT_ID, new Integer( buildContext.getProjectId() ) );
        result.put( ContinuumBuildAgentUtil.KEY_BUILD_DEFINITION_ID, new Integer( buildContext.getBuildDefinitionId() ) );
        result.put( ContinuumBuildAgentUtil.KEY_TRIGGER, new Integer( buildContext.getTrigger() ) );
        result.put( ContinuumBuildAgentUtil.KEY_BUILD_STATE, new Integer( buildResult.getState() ) );
        result.put( ContinuumBuildAgentUtil.KEY_BUILD_START, new Long( buildResult.getStartTime() ).toString() );
        result.put( ContinuumBuildAgentUtil.KEY_BUILD_END, new Long( buildResult.getEndTime() ).toString() );
        result.put( ContinuumBuildAgentUtil.KEY_BUILD_EXIT_CODE, new Integer( buildResult.getExitCode() ) );
        
        String buildOutput = getBuildOutputText( buildContext.getProjectId() );
        if ( buildOutput == null )
        {
            result.put( ContinuumBuildAgentUtil.KEY_BUILD_OUTPUT, "" );
        }
        else
        {
            result.put( ContinuumBuildAgentUtil.KEY_BUILD_OUTPUT, buildOutput );
        }

        if ( buildResult.getError() != null )
        {
            result.put( ContinuumBuildAgentUtil.KEY_BUILD_ERROR, buildResult.getError() );
        }
        else
        {
            result.put( ContinuumBuildAgentUtil.KEY_BUILD_ERROR, "" );
        }

        try
        {
            buildAgentManager.returnBuildResult( result );
        }
        catch ( ContinuumException e )
        {
            log.error( "Failed to return build result for project '" + buildContext.getProjectName() + "'", e );
            throw new TaskExecutionException( "Failed to return build result for project '" + buildContext.getProjectName() + "'", e );
        }
    }

    private void performAction( String actionName, BuildContext context )
        throws TaskExecutionException
    {
        String error = null;
        TaskExecutionException exception = null;
    
        try
        {
            log.info( "Performing action " + actionName );
            actionManager.lookup( actionName ).execute( context.getActionContext() );
            return;
        }
        catch ( ActionNotFoundException e )
        {
            error = ContinuumBuildAgentUtil.throwableToString( e );
            exception = new TaskExecutionException( "Error looking up action '" + actionName + "'", e );
        }
        catch ( ScmRepositoryException e )
        {
            error = getValidationMessages( e ) + "\n" + ContinuumBuildAgentUtil.throwableToString( e );
    
            exception = new TaskExecutionException( "SCM error while executing '" + actionName + "'", e );
        }
        catch ( ScmException e )
        {
            error = ContinuumBuildAgentUtil.throwableToString( e );
    
            exception = new TaskExecutionException( "SCM error while executing '" + actionName + "'", e );
        }
        catch ( Exception e )
        {
            exception = new TaskExecutionException( "Error executing action '" + actionName + "'", e );
            error = ContinuumBuildAgentUtil.throwableToString( exception );
        }

        updateBuildResult( context, error );

        throw exception;
    }

    private void updateBuildResult( BuildContext context, String error )
    {
        context.setBuildResult( ContinuumBuildAgentUtil.getBuildResult( context.getActionContext(), null ) );
        
        if ( context.getBuildResult() == null )
        {
            BuildResult build = new BuildResult();

            build.setState( ContinuumProjectState.ERROR );

            build.setTrigger( context.getTrigger() );

            build.setStartTime( context.getBuildStartTime() );

            build.setEndTime( System.currentTimeMillis() );

            build.setBuildDefinition( BuildContextToBuildDefinition.getBuildDefinition( context ) );

            if ( error != null )
            {
                build.setError( error );
            }

            context.setBuildResult( build );
        }
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

    private String getBuildOutputText( int projectId )
    {
        try
        {
            File buildOutputFile = buildAgentConfigurationService.getBuildOutputFile( projectId );
        
            if ( buildOutputFile.exists() )
            {
                return StringEscapeUtils.escapeHtml( FileUtils.fileRead( buildOutputFile ) );
            }
        }
        catch ( Exception e )
        {
            // do not throw exception, just log it
            log.error( "Error retrieving build output file", e );
        }

        return null;
    }
}
