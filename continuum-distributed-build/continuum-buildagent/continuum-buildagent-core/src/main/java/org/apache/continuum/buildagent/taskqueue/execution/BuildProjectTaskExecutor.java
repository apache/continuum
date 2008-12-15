package org.apache.continuum.buildagent.taskqueue.execution;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.continuum.buildagent.buildcontext.BuildContext;
import org.apache.continuum.buildagent.buildcontext.manager.BuildContextManager;
import org.apache.continuum.taskqueue.BuildProjectTask;
import org.apache.continuum.utils.ContinuumUtils;
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
        int buildDefinitionId = buildProjectTask.getBuildDefinitionId();
        int trigger = buildProjectTask.getTrigger();

        log.info( "Initializing build" );
        BuildContext context = buildContextManager.getBuildContext( projectId );
        initializeBuildContext( context );

        if ( !checkScmResult( context ) )
        {
            log.info( "Error updating from SCM, not building" );
            return;
        }
        
        log.info( "Starting build of " + context.getProjectId() );
        startBuild( context );

        try
        {
            Map actionContext = context.getActionContext();

            performAction( "execute-builder", context );

            performAction( "deploy-artifact", context );

            /*
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
            }*/
        }
        finally
        {
            //endBuild( context );
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
    
        // TODO: clean this up. We catch the original exception from the action, and then update the buildresult
        // for it - we need to because of the specialized error message for SCM.
        // If updating the buildresult fails, log the previous error and throw the new one.
        // If updating the buildresult succeeds, throw the original exception. The build result should NOT
        // be updated again - a TaskExecutionException is final, no further action should be taken upon it.
    
        /*
        try
        {
            updateBuildResult( context, error );
        }
        catch ( TaskExecutionException e )
        {
            log.error( "Error updating build result after receiving the following exception: ", exception );
            throw e;
        }*/
    
        throw exception;
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
