package org.apache.continuum.buildagent.action;

import java.io.File;
import java.util.Date;
import java.util.Map;

import org.apache.continuum.buildagent.build.execution.ContinuumBuildCancelledException;
import org.apache.continuum.buildagent.build.execution.ContinuumBuildExecutionResult;
import org.apache.continuum.buildagent.build.execution.ContinuumBuildExecutor;
import org.apache.continuum.buildagent.build.execution.manager.BuildExecutorManager;
import org.apache.continuum.buildagent.configuration.ConfigurationService;
import org.apache.continuum.buildagent.utils.ContinuumBuildAgentUtil;
import org.apache.continuum.utils.ContinuumUtils;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.codehaus.plexus.action.AbstractAction;

/**
 * @plexus.component role="org.codehaus.plexus.action.Action" role-hint="execute-agent-builder"
 */
public class ExecuteBuilderAction
    extends AbstractAction
{
    /**
     * @plexus.requirement
     */
    private BuildExecutorManager buildExecutorManager;

    /**
     * @plexus.requirement
     */
    private ConfigurationService configurationService;

    public void execute( Map context )
        throws Exception
    {
        // ----------------------------------------------------------------------
        // Get parameters from the context
        // ----------------------------------------------------------------------
        
        Project project = ContinuumBuildAgentUtil.getProject( context );

        BuildDefinition buildDefinition = ContinuumBuildAgentUtil.getBuildDefinition( context );

        int trigger = ContinuumBuildAgentUtil.getTrigger( context );

        ContinuumBuildExecutor buildExecutor = buildExecutorManager.getBuildExecutor( project.getExecutorId() );
        
        // ----------------------------------------------------------------------
        // Make the buildResult
        // ----------------------------------------------------------------------

        BuildResult buildResult = new BuildResult();

        buildResult.setStartTime( new Date().getTime() );

        buildResult.setState( ContinuumProjectState.BUILDING );

        buildResult.setTrigger( trigger );

        buildResult.setBuildDefinition( buildDefinition );

        context.put( ContinuumBuildAgentUtil.KEY_BUILD_RESULT, buildResult );

        try
        {
            File buildOutputFile = configurationService.getBuildOutputFile( project.getId() );

            ContinuumBuildExecutionResult result = buildExecutor.build( project, buildDefinition, buildOutputFile );

            buildResult.setState( result.getExitCode() == 0 ? ContinuumProjectState.OK : ContinuumProjectState.FAILED );

            buildResult.setExitCode( result.getExitCode() );
        }
        catch ( ContinuumBuildCancelledException e )
        {
            getLogger().info( "Cancelled build" );
            
            buildResult.setState( ContinuumProjectState.CANCELLED );
        }
        catch ( Throwable e )
        {
            getLogger().error( "Error running buildResult", e );

            buildResult.setState( ContinuumProjectState.ERROR );

            buildResult.setError( ContinuumUtils.throwableToString( e ) );
        }
        finally
        {
            buildResult.setEndTime( new Date().getTime() );

            if ( buildResult.getState() != ContinuumProjectState.OK &&
                 buildResult.getState() != ContinuumProjectState.FAILED &&
                 buildResult.getState() != ContinuumProjectState.ERROR )
            {
                buildResult.setState( ContinuumProjectState.ERROR );
            }

            context.put( ContinuumBuildAgentUtil.KEY_BUILD_RESULT, buildResult );
        }
    }
}