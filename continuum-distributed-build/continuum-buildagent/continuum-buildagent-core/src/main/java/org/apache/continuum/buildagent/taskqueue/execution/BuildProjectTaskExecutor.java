package org.apache.continuum.buildagent.taskqueue.execution;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.continuum.buildagent.buildcontext.BuildContext;
import org.apache.continuum.buildagent.buildcontext.manager.BuildContextManager;
import org.apache.continuum.buildagent.utils.BuildContextToBuildDefinition;
import org.apache.continuum.buildagent.utils.ContinuumBuildAgentUtil;
import org.apache.continuum.taskqueue.BuildProjectTask;
import org.apache.continuum.utils.ContinuumUtils;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.codehaus.plexus.action.ActionManager;
import org.codehaus.plexus.action.ActionNotFoundException;
import org.codehaus.plexus.taskqueue.Task;
import org.codehaus.plexus.taskqueue.execution.TaskExecutionException;
import org.codehaus.plexus.taskqueue.execution.TaskExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

            performAction( "execute-builder", context );

            updateBuildResult( context, null );
        }
        finally
        {
            endBuild( context );
        }
    }

    private void initializeBuildContext( BuildContext buildContext )
    {
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
    {
        // inform master that project is building ( to set the state )
        
    }

    private void endBuild( BuildContext buildContext )
    {
        // return build result to master
        BuildResult buildResult = buildContext.getBuildResult();

        Map<String, Object> result = new HashMap<String, Object>();
        result.put( ContinuumBuildAgentUtil.KEY_PROJECT_ID, new Integer( buildContext.getProjectId() ) );
        result.put( ContinuumBuildAgentUtil.KEY_BUILD_DEFINITION_ID, new Integer( buildContext.getBuildDefinitionId() ) );
        result.put( ContinuumBuildAgentUtil.KEY_TRIGGER, new Integer( buildContext.getTrigger() ) );
        result.put( ContinuumBuildAgentUtil.KEY_BUILD_STATE, new Integer( buildResult.getState() ) );
        result.put( ContinuumBuildAgentUtil.KEY_BUILD_START, new Long( buildResult.getStartTime() ) );
        result.put( ContinuumBuildAgentUtil.KEY_BUILD_END, new Long( buildResult.getEndTime() ) );
        result.put( ContinuumBuildAgentUtil.KEY_BUILD_EXIT_CODE, new Integer( buildResult.getExitCode() ) );
        if ( buildResult.getError() != null )
        {
            result.put( ContinuumBuildAgentUtil.KEY_BUILD_ERROR, buildResult.getError() );
        }
        else
        {
            result.put( ContinuumBuildAgentUtil.KEY_BUILD_ERROR, "" );
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
}
