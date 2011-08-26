package org.apache.continuum.builder.distributed.work;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.continuum.builder.distributed.manager.DistributedBuildManager;
import org.apache.continuum.dao.BuildDefinitionDao;
import org.apache.continuum.dao.BuildResultDao;
import org.apache.continuum.dao.ProjectDao;
import org.apache.continuum.dao.ProjectScmRootDao;
import org.apache.continuum.model.project.ProjectRunSummary;
import org.apache.continuum.model.project.ProjectScmRoot;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @plexus.component role="org.apache.continuum.builder.distributed.work.ContinuumWorker"
 */
public class DefaultContinuumWorker
    implements ContinuumWorker
{
    private static final Logger log = LoggerFactory.getLogger( DefaultContinuumWorker.class );

    /**
     * @plexus.requirement
     */
    private ProjectDao projectDao;

    /**
     * @plexus.requirement
     */
    private ProjectScmRootDao projectScmRootDao;

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
    private DistributedBuildManager distributedBuildManager;

    /**
     * @plexus.requirement
     */
    private ConfigurationService configurationService;

    public synchronized void work()
    {
        if ( !configurationService.isDistributedBuildEnabled() )
        {
            return;
        }

        log.debug( "Start continuum worker..." );

        List<ProjectRunSummary> currentRuns = new ArrayList<ProjectRunSummary>( distributedBuildManager.getCurrentRuns() );
        List<ProjectRunSummary> runsToDelete = new ArrayList<ProjectRunSummary>();

        synchronized ( currentRuns )
        {
            for ( ProjectRunSummary currentRun : currentRuns )
            {
                try
                {
                    // check for scm update
                    ProjectScmRoot scmRoot = projectScmRootDao.getProjectScmRoot( currentRun.getProjectScmRootId() );
    
                    if ( scmRoot != null && scmRoot.getState() == ContinuumProjectState.UPDATING )
                    {
                        // check if it's still updating
                        if ( !distributedBuildManager.isProjectCurrentlyPreparingBuild( currentRun.getProjectId(), currentRun.getBuildDefinitionId() ) )
                        {
                            // no longer updating, but state was not updated.
                            scmRoot.setState( ContinuumProjectState.ERROR );
                            scmRoot.setError( "Problem encountered while returning scm update result to master by build agent '" + currentRun.getBuildAgentUrl() + "'. \n" +
                                              "Make sure build agent is configured properly. Check the logs for more information." );
                            projectScmRootDao.updateProjectScmRoot( scmRoot );
    
                            log.debug( "projectId={}, buildDefinitionId={} is not updating anymore. Problem encountered while return scm update result by build agent {}. Stopping the build.",
                                       new Object[] { currentRun.getProjectId(), currentRun.getBuildDefinitionId(), currentRun.getBuildAgentUrl() } );
                            runsToDelete.add( currentRun );
                        }
                    }
                    else if ( scmRoot != null && scmRoot.getState() == ContinuumProjectState.ERROR )
                    {
                        log.debug( "projectId={}, buildDefinitionId={} is not updating anymore. Problem encountered while return scm update result by build agent {}. Stopping the build.",
                                   new Object[] { currentRun.getProjectId(), currentRun.getBuildDefinitionId(), currentRun.getBuildAgentUrl() } );
                        runsToDelete.add( currentRun );
                    }
                    else
                    {
                        Project project = projectDao.getProject( currentRun.getProjectId() );
    
                        if ( project != null && project.getState() == ContinuumProjectState.BUILDING )
                        {
                            // check if it's still building
                            if ( !distributedBuildManager.isProjectCurrentlyBuilding( currentRun.getProjectId(), currentRun.getBuildDefinitionId() ) )
                            {
                                BuildDefinition buildDefinition = buildDefinitionDao.getBuildDefinition( currentRun.getBuildDefinitionId() );

                                // no longer building, but state was not updated
                                BuildResult buildResult = new BuildResult();
                                buildResult.setBuildDefinition( buildDefinition );
                                buildResult.setBuildUrl( currentRun.getBuildAgentUrl() );
                                buildResult.setTrigger( currentRun.getTrigger() );
                                buildResult.setUsername( currentRun.getTriggeredBy() );
                                buildResult.setState( ContinuumProjectState.ERROR );
                                buildResult.setSuccess( false );
                                buildResult.setStartTime( new Date().getTime() );
                                buildResult.setEndTime( new Date().getTime() );
                                buildResult.setExitCode( 1 );
                                buildResult.setError( "Problem encountered while returning build result to master by build agent '" + currentRun.getBuildAgentUrl() + "'. \n" +
                                                      "Make sure build agent is configured properly. Check the logs for more information." );
                                buildResultDao.addBuildResult( project, buildResult );
    
                                project.setState( ContinuumProjectState.ERROR );
                                project.setLatestBuildId( buildResult.getId() );
                                projectDao.updateProject( project );
    
                                log.debug( "projectId={}, buildDefinitionId={} is not building anymore. Problem encountered while return build result by build agent {}. Stopping the build.",
                                           new Object[] { currentRun.getProjectId(), currentRun.getBuildDefinitionId(), currentRun.getBuildAgentUrl() } );

                                // create a build result
                                runsToDelete.add( currentRun );
                            }
                        }
                    }
                }
                catch ( Exception e )
                {
                    log.error( "Unable to check if projectId={}, buildDefinitionId={} is still updating or building: {}",
                               new Object[] { currentRun.getProjectId(), currentRun.getBuildDefinitionId(), e.getMessage() } );
                }
            }

            if ( runsToDelete.size() > 0 )
            {
                distributedBuildManager.getCurrentRuns().removeAll( runsToDelete );
            }
        }

        log.debug( "End continuum worker..." );
    }

    // for testing
    public void setProjectDao( ProjectDao projectDao )
    {
        this.projectDao = projectDao;
    }

    public void setProjectScmRootDao( ProjectScmRootDao projectScmRootDao )
    {
        this.projectScmRootDao = projectScmRootDao;
    }

    public void setBuildDefinitionDao( BuildDefinitionDao buildDefinitionDao )
    {
        this.buildDefinitionDao = buildDefinitionDao;
    }

    public void setBuildResultDao( BuildResultDao buildResultDao )
    {
        this.buildResultDao = buildResultDao;
    }

    public void setConfigurationService( ConfigurationService configurationService )
    {
        this.configurationService = configurationService;
    }

    public void setDistributedBuildManager( DistributedBuildManager distributedBuildManager )
    {
        this.distributedBuildManager = distributedBuildManager;
    }
}
