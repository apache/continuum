package org.apache.continuum.buildagent.action;

import org.apache.continuum.dao.BuildResultDao;
import org.apache.continuum.dao.ProjectDao;
import org.apache.maven.continuum.execution.ContinuumBuildCancelledException;
import org.apache.maven.continuum.execution.ContinuumBuildExecutionResult;
import org.apache.maven.continuum.execution.ContinuumBuildExecutor;
import org.apache.maven.continuum.execution.manager.BuildExecutorManager;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
//import org.apache.maven.continuum.model.scm.ScmResult;
import org.apache.maven.continuum.notification.ContinuumNotificationDispatcher;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.utils.ContinuumUtils;
import org.apache.continuum.buildagent.configuration.ConfigurationService;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @plexus.component role="org.codehaus.plexus.action.Action"
 * role-hint="execute-builder-dist"
 */
public class ExecuteBuilderContinuumAction
    extends AbstractContinuumAction
{
    /**
     * @plexus.requirement
     */
    private ConfigurationService configurationService;

    /**
     * @plexus.requirement
     */
    private BuildExecutorManager buildExecutorManager;

    public void execute( Map context )
        throws Exception
    {
        // ----------------------------------------------------------------------
        // Get parameters from the context
        // ----------------------------------------------------------------------

        Project project =  getProject( context ) ;

        BuildDefinition buildDefinition = getBuildDefinition( context );

        int trigger = getTrigger( context );

        List updatedDependencies = getUpdatedDependencies( context );

        ContinuumBuildExecutor buildExecutor = buildExecutorManager.getBuildExecutor( project.getExecutorId() );

        // ----------------------------------------------------------------------
        // Make the buildResult
        // ----------------------------------------------------------------------

        BuildResult buildResult = new BuildResult();

        buildResult.setStartTime( new Date().getTime() );

        buildResult.setState( ContinuumProjectState.BUILDING );

        buildResult.setTrigger( trigger );

        buildResult.setModifiedDependencies( updatedDependencies );

        buildResult.setBuildDefinition( getBuildDefinition( context ) );

        //buildResultDao.addBuildResult( project, buildResult );

        context.put( KEY_BUILD_ID, Integer.toString( buildResult.getId() ) );

        context.put( KEY_CANCELLED, new Boolean( false ) );

        //buildResult = buildResultDao.getBuildResult( buildResult.getId() );

        try
        {
            //notifier.runningGoals( project, buildDefinition, buildResult );

            File buildOutputFile = configurationService.getBuildOutputFile( project.getId() );

            ContinuumBuildExecutionResult result = buildExecutor.build( project, buildDefinition, buildOutputFile );

            buildResult.setState( result.getExitCode() == 0 ? ContinuumProjectState.OK : ContinuumProjectState.FAILED );

            buildResult.setExitCode( result.getExitCode() );
        }
        catch ( ContinuumBuildCancelledException e )
        {
            getLogger().info( "Cancelled build" );
            
            buildResult.setState( ContinuumProjectState.CANCELLED );
            
            context.put( KEY_CANCELLED, new Boolean( true ) );
        }
        catch ( Throwable e )
        {
            getLogger().error( "Error running buildResult", e );

            buildResult.setState( ContinuumProjectState.ERROR );

            buildResult.setError( ContinuumUtils.throwableToString( e ) );
        }
        finally
        {
            

            if ( buildResult.getState() == ContinuumProjectState.CANCELLED )
            {
                project.setState( project.getOldState() );

                project.setOldState( 0 );

                int buildResultId = getOldBuildId( context ); 

                project.setLatestBuildId( buildResultId );

              
            }
            else
            {
                buildResult.setEndTime( new Date().getTime() );

                if ( buildResult.getState() == ContinuumProjectState.OK )
                {
                    project.setBuildNumber( project.getBuildNumber() + 1 );
                }

                project.setLatestBuildId( buildResult.getId() );

                buildResult.setBuildNumber( project.getBuildNumber() );

                if ( buildResult.getState() != ContinuumProjectState.OK &&
                    buildResult.getState() != ContinuumProjectState.FAILED &&
                    buildResult.getState() != ContinuumProjectState.ERROR )
                {
                    buildResult.setState( ContinuumProjectState.ERROR );
                }

                project.setState( buildResult.getState() );              
              
            }

            context.put( KEY_PROJECT, project );
            

            // ----------------------------------------------------------------------
            // Backup test result files
            // ----------------------------------------------------------------------
            //TODO: Move as a plugin
            buildExecutor.backupTestFiles( project, buildResult.getId() );
        }
    }
}

