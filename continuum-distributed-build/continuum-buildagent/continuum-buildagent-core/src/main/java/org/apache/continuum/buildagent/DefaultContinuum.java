package org.apache.continuum.buildagent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.continuum.buildagent.buildcontext.BuildContext;
import org.apache.continuum.buildagent.buildcontext.manager.BuildContextManager;
import org.apache.continuum.buildagent.configuration.ConfigurationService;
import org.apache.continuum.buildagent.taskqueue.manager.TaskQueueManager;
import org.apache.continuum.buildagent.utils.BuildContextToBuildDefinition;
import org.apache.continuum.buildagent.utils.BuildContextToProject;
import org.apache.continuum.buildagent.utils.ContinuumBuildAgentUtil;
import org.apache.continuum.taskqueue.BuildProjectTask;
import org.apache.continuum.utils.ContinuumUtils;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.scm.ScmResult;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.codehaus.plexus.action.ActionManager;
import org.codehaus.plexus.action.ActionNotFoundException;
import org.codehaus.plexus.taskqueue.TaskQueueException;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @plexus.component role="org.apache.continuum.buildagent.Continuum" role-hint="default"
 */
public class DefaultContinuum
    implements Continuum
{
    private Logger log = LoggerFactory.getLogger( this.getClass() );

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
    private TaskQueueManager taskQueueManager;

    public void prepareBuildProjects( List<BuildContext> buildContexts)
        throws ContinuumException
    {
        Map<String, Object> context = null;

        try
        {
            for ( BuildContext buildContext : buildContexts )
            {
                context = buildContext.getActionContext();

                BuildDefinition buildDef = BuildContextToBuildDefinition.getBuildDefinition( buildContext );
    
                log.info( "Check scm root state" );
                if ( !checkProjectScmRoot( context ) )
                {
                    break;
                }
                
                log.info( "Initializing prepare build" );
                initializeActionContext( buildContext );
                
                log.info( "Starting prepare build" );

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

        if ( !checkProjectScmRoot( context ) )
        {
            buildProjects( buildContexts );
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

    private boolean checkProjectScmRoot( Map context )
    {
        if ( context != null && ContinuumBuildAgentUtil.getScmRootState( context ) == ContinuumProjectState.ERROR )
        {
            return false;
        }

        return true;
    }

    private void cleanWorkingDirectory( BuildContext buildContext )
        throws ContinuumException
    {
        performAction( "clean-agent-work-directory", buildContext );
    }

    private void updateWorkingDirectory( BuildContext buildContext )
        throws ContinuumException
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
        throws ContinuumException
    {
        Map<String, Object> context = buildContext.getActionContext();

        ScmResult scmResult = ContinuumBuildAgentUtil.getScmResult( context, null );
        Project project = ContinuumBuildAgentUtil.getProject( context );

        if ( scmResult == null || !scmResult.isSuccess() )
        {
            context.put( ContinuumBuildAgentUtil.KEY_SCM_ROOT_STATE, ContinuumProjectState.ERROR );
        }
    }

    private void endPrepareBuild( Map context )
        throws ContinuumException
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
        throws ContinuumException
    {
        ContinuumException exception = null;
    
        try
        {
            log.info( "Performing action " + actionName );
            actionManager.lookup( actionName ).execute( buildContext.getActionContext() );
            return;
        }
        catch ( ActionNotFoundException e )
        {
            exception = new ContinuumException( "Error looking up action '" + actionName + "'", e );
        }
        catch ( Exception e )
        {
            exception = new ContinuumException( "Error executing action '" + actionName + "'", e );
        }
        
        ScmResult result = new ScmResult();
        
        result.setSuccess( false );
        
        result.setException( ContinuumUtils.throwableToString( exception ) );

        buildContext.setScmResult( result );
        buildContext.getActionContext().put( ContinuumBuildAgentUtil.KEY_UPDATE_SCM_RESULT, result );
        
        throw exception;
    }
    
    private void buildProjects( List<BuildContext> buildContexts )
        throws ContinuumException
    {
        for ( BuildContext buildContext : buildContexts )
        {
            BuildProjectTask buildProjectTask = new BuildProjectTask( buildContext.getProjectId(),
                                                                      buildContext.getBuildDefinitionId(),
                                                                      buildContext.getTrigger(),
                                                                      buildContext.getProjectName(),
                                                                      "" );
            try
            {
                taskQueueManager.getBuildQueue().put( buildProjectTask );
            }
            catch ( TaskQueueException e )
            {
                log.error( "Error while enqueing build task for project " + buildContext.getProjectId(), e );
                throw new ContinuumException( "Error while enqueuing build task for project " + buildContext.getProjectId(), e );
            }
        }
    }

}
