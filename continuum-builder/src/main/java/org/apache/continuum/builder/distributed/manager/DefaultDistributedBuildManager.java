package org.apache.continuum.builder.distributed.manager;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.continuum.builder.distributed.executor.DistributedBuildTaskQueueExecutor;
import org.apache.continuum.builder.distributed.executor.ThreadedDistributedBuildTaskQueueExecutor;
import org.apache.continuum.builder.utils.ContinuumBuildConstant;
import org.apache.continuum.configuration.BuildAgentConfiguration;
import org.apache.continuum.dao.BuildDefinitionDao;
import org.apache.continuum.dao.BuildResultDao;
import org.apache.continuum.dao.ProjectDao;
import org.apache.continuum.dao.ProjectScmRootDao;
import org.apache.continuum.distributed.transport.slave.SlaveBuildAgentTransportClient;
import org.apache.continuum.model.project.ProjectScmRoot;
import org.apache.continuum.taskqueue.PrepareBuildProjectsTask;
import org.apache.continuum.utils.ContinuumUtils;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectDependency;
import org.apache.maven.continuum.model.scm.ScmResult;
import org.apache.maven.continuum.model.system.Installation;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.store.ContinuumStoreException;

import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Maria Catherine Tan
 */
public class DefaultDistributedBuildManager
    implements DistributedBuildManager, Contextualizable, Initializable
{
    private Logger log = LoggerFactory.getLogger( this.getClass() );

    /**
     * @plexus.requirement
     */
    private ConfigurationService configurationService;

    /**
     * @plexus.requirement
     */
    private ProjectDao projectDao;

    /**
     * @plexus.requirement
     */
    private BuildDefinitionDao buildDefinitionDao;

    /**
     * @plexus.requirement
     */
    private ProjectScmRootDao projectScmRootDao;

    /**
     * @plexus.requirement
     */
    private BuildResultDao buildResultDao;

    private PlexusContainer container;

    private Map<String, ThreadedDistributedBuildTaskQueueExecutor> taskQueueExecutors;

    // --------------------------------
    //  Plexus Lifecycle
    // --------------------------------
    public void contextualize( Context context )
        throws ContextException
    {
        container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }

    public void initialize()
        throws InitializationException
    {
        taskQueueExecutors = new HashMap<String, ThreadedDistributedBuildTaskQueueExecutor>();

        List<BuildAgentConfiguration> agents = configurationService.getBuildAgents();

        if ( agents != null )
        {
            for ( BuildAgentConfiguration agent : agents )
            {
                if ( agent.isEnabled() )
                {
                    try
                    {
                        SlaveBuildAgentTransportClient client = new SlaveBuildAgentTransportClient( new URL( agent.getUrl() ) );
                        
                        if ( client.ping() )
                        {
                            log.info( "agent is enabled, add TaskQueueExecutor for build agent '" + agent.getUrl() + "'" );
                            addTaskQueueExecutor( agent.getUrl() );
                        }
                        else
                        {
                            log.info( "unable to ping build agent '" + agent.getUrl() + "'" );
                        }
                    }
                    catch ( MalformedURLException e )
                    {
                        // do not throw exception, just log it
                        log.info( "Invalid build agent URL " + agent.getUrl() + ", not creating task queue executor" );
                    }
                    catch ( ContinuumException e )
                    {
                        throw new InitializationException( "Error while initializing distributed build task queue executors", e );
                    }
                    catch ( Exception e )
                    {
                        agent.setEnabled( false );
                        log.info( "unable to ping build agent '" + agent.getUrl() + "': " + ContinuumUtils.throwableToString( e ) );
                    }
                }
            }
        }
    }

    public void reload()
        throws ContinuumException
    {
        List<BuildAgentConfiguration> agents = configurationService.getBuildAgents();
        
        for ( BuildAgentConfiguration agent : agents )
        {
            if ( agent.isEnabled() && !taskQueueExecutors.containsKey( agent.getUrl() ) )
            {
                try
                {
                    SlaveBuildAgentTransportClient client = new SlaveBuildAgentTransportClient( new URL( agent.getUrl() ) );
                    
                    if ( client.ping() )
                    {
                        log.info( "agent is enabled, add TaskQueueExecutor for build agent '" + agent.getUrl() + "'" );
                        addTaskQueueExecutor( agent.getUrl() );
                    }
                    else
                    {
                        log.info( "unable to ping build agent '" + agent.getUrl() + "'" );
                    }
                }
                catch ( MalformedURLException e )
                {
                    // do not throw exception, just log it
                    log.info( "Invalid build agent URL " + agent.getUrl() + ", not creating task queue executor" );
                }
                catch ( Exception e )
                {
                    agent.setEnabled( false );
                    log.info( "unable to ping build agent '" + agent.getUrl() + "': " + ContinuumUtils.throwableToString( e ) );
                }
            }
            else if ( !agent.isEnabled() && taskQueueExecutors.containsKey( agent.getUrl() ) )
            {
                log.info( "agent is disabled, remove TaskQueueExecutor for build agent '" + agent.getUrl() + "'" );
                removeAgentFromTaskQueueExecutor( agent.getUrl() );
            }
        }
    }

    public void removeAgentFromTaskQueueExecutor( String buildAgentUrl)
        throws ContinuumException
    {
        log.info( "remove TaskQueueExecutor for build agent '" + buildAgentUrl + "'" );
        ThreadedDistributedBuildTaskQueueExecutor executor = taskQueueExecutors.get( buildAgentUrl );

        try
        {
            executor.stop();
            container.release( executor );
        }
        catch ( StoppingException e )
        {
            throw new ContinuumException( "Error while stopping task queue executor", e );
        }
        catch ( ComponentLifecycleException e )
        {
            throw new ContinuumException( "Error while releasing task queue executor from container", e );
        }

        taskQueueExecutors.remove( buildAgentUrl );
    }

    public boolean isBuildAgentBusy( String buildAgentUrl )
    {
        ThreadedDistributedBuildTaskQueueExecutor executor = taskQueueExecutors.get( buildAgentUrl );
        
        if ( executor != null && executor.getCurrentTask() != null )
        {
            log.info( "build agent '" + buildAgentUrl + "' is busy" );
            return true;
        }

        log.info( "build agent '" + buildAgentUrl + "' is not busy" );
        return false;
    }

    private void addTaskQueueExecutor( String url )
        throws ContinuumException
    {
        try
        {            
            ThreadedDistributedBuildTaskQueueExecutor taskQueueExecutor = (ThreadedDistributedBuildTaskQueueExecutor) container.
                                                                          lookup( DistributedBuildTaskQueueExecutor.class, "distributed-build-project" );
            taskQueueExecutor.setBuildAgentUrl( url );
            taskQueueExecutors.put( url, taskQueueExecutor );
        }
        catch ( ComponentLookupException e )
        {
            throw new ContinuumException( "Unable to lookup TaskQueueExecutor for distributed-build-project", e );
        }
    }

    public void cancelDistributedBuild( String buildAgentUrl, int projectGroupId, String scmRootAddress )
        throws ContinuumException
    {
        ThreadedDistributedBuildTaskQueueExecutor taskQueueExecutor = taskQueueExecutors.get( buildAgentUrl );

        if ( taskQueueExecutor != null )
        {
            if ( taskQueueExecutor.getCurrentTask() != null )
            {
                if ( taskQueueExecutor.getCurrentTask() instanceof PrepareBuildProjectsTask )
                {
                    PrepareBuildProjectsTask currentTask = (PrepareBuildProjectsTask) taskQueueExecutor.getCurrentTask();
                    
                    if ( currentTask.getProjectGroupId() == projectGroupId && 
                         currentTask.getScmRootAddress().equals( scmRootAddress ) )
                    {
                        log.info( "cancelling task for project group " + projectGroupId + 
                                  " with scm root address " + scmRootAddress );
                        taskQueueExecutor.cancelTask( currentTask );

                        try
                        {
                            SlaveBuildAgentTransportClient client = new SlaveBuildAgentTransportClient( new URL( buildAgentUrl ) );
                            client.cancelBuild();
                        }
                        catch ( Exception e )
                        {
                            log.error( "Error while cancelling build in build agent '" + buildAgentUrl + "'" );
                            throw new ContinuumException( "Error while cancelling build in build agent '" + buildAgentUrl + "'", e );
                        }
                    }
                    else
                    {
                        log.info( "current task not for project group " + projectGroupId + 
                                  " with scm root address " + scmRootAddress );
                    }
                }
                else
                {
                    log.info( "current task not a prepare build projects task, not cancelling" );
                }
            }
            else
            {
                log.info( "no current task in build agent '" + buildAgentUrl + "'" );
            }
        }
        else
        {
            log.info( "no task queue executor defined for build agent '" + buildAgentUrl + "'" );
        }
    }

    public void updateScmResult( Map context )
        throws ContinuumException
    {
        try
        {
            int projectId = ContinuumBuildConstant.getProjectId( context );

            log.info( "update scm result of project" + projectId );
            Project project = projectDao.getProjectWithScmDetails( projectId );
            
            ScmResult scmResult = new ScmResult();
            scmResult.setCommandLine( ContinuumBuildConstant.getScmCommandLine( context ) );
            scmResult.setCommandOutput( ContinuumBuildConstant.getScmCommandOutput( context ) );
            scmResult.setException( ContinuumBuildConstant.getScmException( context ) );
            scmResult.setProviderMessage( ContinuumBuildConstant.getScmProviderMessage( context ) );
            scmResult.setSuccess( ContinuumBuildConstant.isScmSuccess( context ) );

            project.setScmResult( scmResult );
            projectDao.updateProject( project );
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumException( "Error updating project's scm result", e );
        }
    }

    public void updateBuildResult( Map context )
        throws ContinuumException
    {
        try
        {
            int projectId = ContinuumBuildConstant.getProjectId( context );
            int buildDefinitionId = ContinuumBuildConstant.getBuildDefinitionId( context );

            log.info( "update build result of project '" + projectId + "'" );

            Project project = projectDao.getProjectWithAllDetails( projectId );
            BuildDefinition buildDefinition = buildDefinitionDao.getBuildDefinition( buildDefinitionId );

            BuildResult oldBuildResult =
                buildResultDao.getLatestBuildResultForBuildDefinition( projectId, buildDefinitionId );

            int buildNumber;

            if ( ContinuumBuildConstant.getBuildState( context ) == ContinuumProjectState.OK )
            {
                buildNumber = project.getBuildNumber() + 1;
            }
            else
            {
                buildNumber = project.getBuildNumber();
            }

            // ----------------------------------------------------------------------
            // Make the buildResult
            // ----------------------------------------------------------------------

            BuildResult buildResult = convertMapToBuildResult( context );
            buildResult.setBuildDefinition( buildDefinition );
            buildResult.setBuildNumber( buildNumber );
            buildResult.setModifiedDependencies( getModifiedDependencies( oldBuildResult, context ) );
            
            buildResultDao.addBuildResult( project, buildResult );
            
            project.setBuildNumber( buildNumber );
            project.setLatestBuildId( buildResult.getId() );
            project.setOldState( project.getState() );
            project.setState( ContinuumBuildConstant.getBuildState( context ) );

            projectDao.updateProject( project );

            //TODO: set state of next project to building
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumException( "Error while updating build result for project", e );
        }
    }

    public void prepareBuildFinished( Map context )
        throws ContinuumException
    {
        int projectGroupId = ContinuumBuildConstant.getProjectGroupId( context );
        String scmRootAddress = ContinuumBuildConstant.getScmRootAddress( context );

        try
        {
            ProjectScmRoot scmRoot = projectScmRootDao.getProjectScmRootByProjectGroupAndScmRootAddress( projectGroupId, scmRootAddress );
            
            String error = ContinuumBuildConstant.getScmError( context );
            
            if ( StringUtils.isEmpty( error ) )
            {
                scmRoot.setState( ContinuumProjectState.UPDATED );
            }
            else
            {
                scmRoot.setState( ContinuumProjectState.ERROR );
                scmRoot.setError( error );
            }

            projectScmRootDao.updateProjectScmRoot( scmRoot );
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumException( "Error while updating project scm root '" + scmRootAddress + "'", e );
        }
    }

    public Map<String, PrepareBuildProjectsTask> getDistributedBuildProjects()
    {
        Map<String, PrepareBuildProjectsTask> map = new HashMap<String, PrepareBuildProjectsTask>();

        for ( String url : taskQueueExecutors.keySet() )
        {
            ThreadedDistributedBuildTaskQueueExecutor taskQueueExecutor = taskQueueExecutors.get( url );

            if ( taskQueueExecutor.getCurrentTask() != null )
            {
                PrepareBuildProjectsTask task = (PrepareBuildProjectsTask) taskQueueExecutor.getCurrentTask();
                
                map.put( url, task );
            }
        }

        return map;
    }

    public List<Installation> getAvailableInstallations( String buildAgentUrl )
        throws ContinuumException
    {
        try
        {
            SlaveBuildAgentTransportClient client = new SlaveBuildAgentTransportClient( new URL( buildAgentUrl ) );
            
            //return client.getAvailableInstallations();
        }
        catch ( Exception e )
        {
            throw new ContinuumException( "Unable to get available installations of build agent", e );
        }

        return null;
    }

    private List<ProjectDependency> getModifiedDependencies( BuildResult oldBuildResult, Map context )
        throws ContinuumException
    {
        if ( oldBuildResult == null )
        {
            return null;
        }

        try
        {
            Project project = projectDao.getProjectWithAllDetails( ContinuumBuildConstant.getProjectId( context ) );
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
                return null;
            }

            List<ProjectDependency> modifiedDependencies = new ArrayList<ProjectDependency>();

            for ( ProjectDependency dep : dependencies )
            {
                Project dependencyProject =
                    projectDao.getProject( dep.getGroupId(), dep.getArtifactId(), dep.getVersion() );

                if ( dependencyProject != null )
                {
                    List buildResults = buildResultDao.getBuildResultsInSuccessForProject( dependencyProject.getId(),
                                                                                           oldBuildResult.getEndTime() );
                    if ( buildResults != null && !buildResults.isEmpty() )
                    {
                        log.debug( "Dependency changed: " + dep.getGroupId() + ":" + dep.getArtifactId() + ":" +
                            dep.getVersion() );
                        modifiedDependencies.add( dep );
                    }
                    else
                    {
                        log.debug( "Dependency not changed: " + dep.getGroupId() + ":" + dep.getArtifactId() +
                            ":" + dep.getVersion() );
                    }
                }
                else
                {
                    log.debug( "Skip non Continuum project: " + dep.getGroupId() + ":" + dep.getArtifactId() +
                        ":" + dep.getVersion() );
                }
            }

            return modifiedDependencies;
        }
        catch ( ContinuumStoreException e )
        {
            log.warn( "Can't get the project dependencies", e );
        }

        return null;
    }

    public void updateProjectCurrentlyBuilding( int projectId )
        throws ContinuumException
    {
        try
        {
            Project project = projectDao.getProject( projectId );
            project.setState( ContinuumProjectState.BUILDING );
            projectDao.updateProject( project );
        }
        catch ( ContinuumStoreException e )
        {
            log.error( "Error while updating project's state", e );
            throw new ContinuumException( "Error while updating project's state", e );
        }
    }

    public Map<String, Object> getBuildResult( int projectId )
        throws ContinuumException
    {
        Map<String, Object> map = new HashMap<String, Object>();
        
        String buildAgentUrl = getBuildAgent( projectId );
        
        if ( buildAgentUrl == null )
        {
            throw new ContinuumException( "Unable to find build agent for project " + projectId );
        }

        try
        {
            SlaveBuildAgentTransportClient client = new SlaveBuildAgentTransportClient( new URL( buildAgentUrl ) );

            Map result = client.getBuildResult( projectId );
            
            if ( result != null )
            {
                int buildDefinitionId = ContinuumBuildConstant.getBuildDefinitionId( result );

                Project project = projectDao.getProjectWithAllDetails( projectId );
                BuildDefinition buildDefinition = buildDefinitionDao.getBuildDefinition( buildDefinitionId );

                BuildResult oldBuildResult =
                    buildResultDao.getLatestBuildResultForBuildDefinition( projectId, buildDefinitionId );

                BuildResult buildResult = convertMapToBuildResult( result );
                buildResult.setBuildDefinition( buildDefinition );
                buildResult.setBuildNumber( project.getBuildNumber() + 1 );
                buildResult.setModifiedDependencies( getModifiedDependencies( oldBuildResult, result ) );

                String buildOutput = ContinuumBuildConstant.getBuildOutput( result );
                
                map.put( ContinuumBuildConstant.KEY_BUILD_RESULT, buildResult );
                map.put( ContinuumBuildConstant.KEY_BUILD_OUTPUT, buildOutput );
            }
        }
        catch ( MalformedURLException e )
        {
            throw new ContinuumException( "Invalid build agent URL '" + buildAgentUrl + "'" );
        }
        catch ( Exception e )
        {
            throw new ContinuumException( "", e );
        }

        return map;
    }

    private String getBuildAgent( int projectId )
        throws ContinuumException
    {
        Map<String, PrepareBuildProjectsTask> map = getDistributedBuildProjects();
        
        for ( String url : map.keySet() )
        {
            PrepareBuildProjectsTask task = map.get( url );
            
            for ( Integer id : task.getProjectsBuildDefinitionsMap().keySet() )
            {
                if ( projectId == id )
                {
                    return url;
                }
            }
        }
        
        return null;
    }

    private BuildResult convertMapToBuildResult( Map context )
    {
        BuildResult buildResult = new BuildResult();

        buildResult.setStartTime( ContinuumBuildConstant.getBuildStart( context ) );
        buildResult.setEndTime( ContinuumBuildConstant.getBuildEnd( context ) );
        buildResult.setError( ContinuumBuildConstant.getBuildError( context ) );
        buildResult.setExitCode( ContinuumBuildConstant.getBuildExitCode( context ) );
        buildResult.setState( ContinuumBuildConstant.getBuildState( context ) );
        buildResult.setTrigger( ContinuumBuildConstant.getTrigger( context ) );

        return buildResult;
    }
}
