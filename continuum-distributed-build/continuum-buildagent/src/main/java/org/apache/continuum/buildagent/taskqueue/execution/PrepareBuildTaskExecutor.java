package org.apache.continuum.buildagent.taskqueue.execution;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.continuum.buildagent.buildcontext.manager.BuildContextManager;
import org.apache.continuum.buildagent.configuration.ConfigurationService;
import org.apache.continuum.buildagent.model.BuildContext;
import org.apache.continuum.buildagent.transportclient.MasterAgentTransportClient;
import org.apache.continuum.buildagent.utils.BuildContextToBuildDefinition;
import org.apache.continuum.buildagent.utils.BuildContextToProject;
import org.apache.continuum.buildagent.utils.ContinuumBuildAgentUtil;
import org.apache.continuum.utils.ContinuumUtils;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Project;
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
 * @author Maria Catherine Tan
 * @plexus.component role="org.codehaus.plexus.taskqueue.execution.TaskExecutor" role-hint="prepare-build-agent"
 */
public class PrepareBuildTaskExecutor
    implements TaskExecutor
{
    private Logger log = LoggerFactory.getLogger( this.getClass() );

    /**
     * @plexus.requirement
     */
    private ActionManager actionManager;
    
    /**
     * @plexus.requirement
     */
    private BuildContextManager buildContextManager;

    /**
     * @plexus.requirement
     */
    private ConfigurationService configurationService;

    /**
     * @plexus.requirement
     */
    private MasterAgentTransportClient transportClient;

    // do we still need a queue for this?
    public void executeTask( Task task )
        throws TaskExecutionException
    {
        List<BuildContext> buildContexts = buildContextManager.getBuildContextList();

        Map<String, Object> context = null;

        try
        {
            for ( BuildContext buildContext : buildContexts )
            {
                BuildDefinition buildDef = BuildContextToBuildDefinition.getBuildDefinition( buildContext );
    
                log.info( "Check scm root state" );
                if ( !checkProjectScmRoot( buildContext ) )
                {
                    break;
                }
                
                log.info( "Initializing prepare build" );
                initializeActionContext( buildContext );
                
                log.info( "Starting prepare build" );
                context = buildContext.getActionContext();

                try
                {
                    if ( buildDef.isBuildFresh() )
                    {
                        log.info( "Clean up working directory" );
                        cleanWorkingDirectory( buildContext );
                    }
        
                    log.info( "Updating working directory" );
                    updateWorkingDirectory( buildContext );
                }
                finally
                {
                    endProjectPrepareBuild( buildContext );
                }
            }
        }
        finally
        {
            endPrepareBuild( context );
        }

        
    }

    private void initializeActionContext( BuildContext buildContext )
    {
        Map<String, Object> actionContext = new HashMap<String, Object>();

        actionContext.put( ContinuumBuildAgentUtil.KEY_PROJECT_ID, buildContext.getProjectId() );
        actionContext.put( ContinuumBuildAgentUtil.KEY_PROJECT, BuildContextToProject.getProject( buildContext ) );
        actionContext.put( ContinuumBuildAgentUtil.KEY_BUILD_DEFINITION, BuildContextToBuildDefinition.getBuildDefinition( buildContext ) );
        actionContext.put( ContinuumBuildAgentUtil.KEY_SCM_ROOT_STATE, ContinuumProjectState.UPDATING );
        actionContext.put( ContinuumBuildAgentUtil.KEY_PROJECT_GROUP_ID, buildContext.getProjectGroupId() );
        actionContext.put( ContinuumBuildAgentUtil.KEY_SCM_ROOT_ADDRESS, buildContext.getScmRootAddress() );
        
        buildContext.setActionContext( actionContext );
    }

    private boolean checkProjectScmRoot( BuildContext buildContext )
    {
        if ( buildContext.getActionContext() != null && 
             ContinuumBuildAgentUtil.getScmRootState( buildContext.getActionContext() ) == ContinuumProjectState.ERROR )
        {
            return false;
        }

        return true;
    }

    private void cleanWorkingDirectory( BuildContext buildContext )
        throws TaskExecutionException
    {
        performAction( "clean-agent-work-directory", buildContext );
    }

    private void updateWorkingDirectory( BuildContext buildContext )
        throws TaskExecutionException
    {
        Map<String, Object> actionContext = buildContext.getActionContext();

        performAction( "check-agent-working-directory", buildContext );
        
        boolean workingDirectoryExists =
            ContinuumBuildAgentUtil.getBoolean( actionContext, ContinuumBuildAgentUtil.KEY_WORKING_DIRECTORY_EXISTS );
    
        ScmResult scmResult;
    
        if ( workingDirectoryExists )
        {
            performAction( "update-agent-working-directory", buildContext );
    
            scmResult = ContinuumBuildAgentUtil.getUpdateScmResult( actionContext, null );
        }
        else
        {
            Project project = ContinuumBuildAgentUtil.getProject( actionContext );
    
            actionContext.put( ContinuumBuildAgentUtil.KEY_WORKING_DIRECTORY,
                               configurationService.getWorkingDirectory( project.getId() ).getAbsolutePath() );
    
            performAction( "checkout-project", buildContext );
    
            scmResult = ContinuumBuildAgentUtil.getCheckoutScmResult( actionContext, null );
        }
    
        buildContext.setScmResult( scmResult );
        actionContext.put( ContinuumBuildAgentUtil.KEY_SCM_RESULT, scmResult );
    }

    private void endProjectPrepareBuild( BuildContext buildContext )
        throws TaskExecutionException
    {
        Map<String, Object> context = buildContext.getActionContext();

        ScmResult scmResult = ContinuumBuildAgentUtil.getScmResult( context, null );
        Project project = ContinuumBuildAgentUtil.getProject( context );

        if ( scmResult == null || !scmResult.isSuccess() )
        {
            context.put( ContinuumBuildAgentUtil.KEY_SCM_ROOT_STATE, ContinuumProjectState.ERROR );
        }

        try
        {
            transportClient.returnScmResult( createScmResult( buildContext ) );
        }
        catch ( ContinuumException e )
        {
            log.error( "Failed to return scm result", e );
            throw new TaskExecutionException( "Failed to return scm result", e );
        }
    }

    private void endPrepareBuild( Map context )
        throws TaskExecutionException
    {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put( ContinuumBuildAgentUtil.KEY_PROJECT_GROUP_ID, new Integer( ContinuumBuildAgentUtil.getProjectGroupId( context ) ) );
        result.put( ContinuumBuildAgentUtil.KEY_SCM_ROOT_ADDRESS, ContinuumBuildAgentUtil.getScmRootAddress( context ) );
        
        String error = convertScmResultToError( ContinuumBuildAgentUtil.getScmResult( context, null ) );
        if ( StringUtils.isEmpty( error ) )
        {
            result.put( ContinuumBuildAgentUtil.KEY_SCM_ERROR, "" );
        }
        else
        {
            result.put( ContinuumBuildAgentUtil.KEY_SCM_ERROR, error );
        }

        try
        {
            transportClient.prepareBuildFinished( result );
        }
        catch ( ContinuumException e )
        {
            log.error( "Failed to finish prepare build", e );
            throw new TaskExecutionException( "Failed to finish prepare build", e );
        }
    }

    private Map<String, Object> createScmResult( BuildContext buildContext )
    {
        Map<String, Object> result = new HashMap<String, Object>();
        ScmResult scmResult = buildContext.getScmResult();

        result.put( ContinuumBuildAgentUtil.KEY_PROJECT_ID, new Integer( buildContext.getProjectId() ) );
        if ( StringUtils.isEmpty( scmResult.getCommandLine() ) )
        {
            result.put( ContinuumBuildAgentUtil.KEY_SCM_COMMAND_LINE, "" );
        }
        else
        {
            result.put( ContinuumBuildAgentUtil.KEY_SCM_COMMAND_LINE, scmResult.getCommandLine() );
        }
        if ( StringUtils.isEmpty( scmResult.getCommandOutput() ) )
        {
            result.put( ContinuumBuildAgentUtil.KEY_SCM_COMMAND_OUTPUT, "" );
        }
        else
        {
            result.put( ContinuumBuildAgentUtil.KEY_SCM_COMMAND_OUTPUT, scmResult.getCommandOutput() );
        }
        if ( StringUtils.isEmpty( scmResult.getProviderMessage() ) )
        {
            result.put( ContinuumBuildAgentUtil.KEY_SCM_PROVIDER_MESSAGE, "" );
        }
        else
        {
            result.put( ContinuumBuildAgentUtil.KEY_SCM_PROVIDER_MESSAGE, scmResult.getProviderMessage() );
        }
        if ( StringUtils.isEmpty( scmResult.getException() ) )
        {
            result.put( ContinuumBuildAgentUtil.KEY_SCM_EXCEPTION, "" );
        }
        else
        {
            result.put( ContinuumBuildAgentUtil.KEY_SCM_EXCEPTION, scmResult.getException() );
        }
        result.put( ContinuumBuildAgentUtil.KEY_SCM_SUCCESS, new Boolean( scmResult.isSuccess() ) );
        
        return result;
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
        TaskExecutionException exception = null;
    
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
        
        result.setException( ContinuumUtils.throwableToString( exception ) );

        buildContext.setScmResult( result );
        buildContext.getActionContext().put( ContinuumBuildAgentUtil.KEY_UPDATE_SCM_RESULT, result );
        
        throw exception;
    }
}
