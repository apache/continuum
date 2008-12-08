package org.apache.continuum.builder.distributed.manager;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.continuum.builder.AbstractContinuumBuilder;
import org.apache.continuum.builder.distributed.BuildAgentListener;
import org.apache.continuum.builder.distributed.DefaultBuildAgentListener;
import org.apache.continuum.builder.distributed.manager.DistributedBuildManager;
import org.apache.continuum.configuration.BuildAgentConfiguration;
import org.apache.continuum.dao.BuildDefinitionDao;
import org.apache.continuum.dao.BuildResultDao;
import org.apache.continuum.dao.ProjectDao;
import org.apache.continuum.dao.ProjectScmRootDao;
import org.apache.continuum.model.project.ProjectScmRoot;
import org.apache.continuum.scm.queue.PrepareBuildProjectsTask;
import org.apache.continuum.utils.ProjectSorter;
//import org.apache.continuum.xmlrpc.distributed.client.ContinuumDistributedBuildClient;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectDependency;
import org.apache.maven.continuum.model.scm.ScmResult;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.apache.xmlrpc.XmlRpcException;

import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.dag.CycleDetectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Maria Catherine Tan
 */
public class DefaultDistributedBuildManager
    extends AbstractContinuumBuilder
    implements DistributedBuildManager
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

    private List<PrepareBuildProjectsTask> projectsBuildInQueue;
    
    private List<BuildAgentListener> listeners;

    public void initialize()
        throws ContinuumException
    {
        List<BuildAgentConfiguration> agents = configurationService.getBuildAgents();

        if ( listeners == null )
        {
            listeners = new ArrayList<BuildAgentListener>();
        }
        
        if ( agents != null )
        {
            for ( BuildAgentConfiguration agent : agents )
            {
                boolean found = false;

                for ( BuildAgentListener listener : listeners )
                {
                    if ( listener.getUrl().equals( agent.getUrl() ) )
                    {
                        found = true;
                        listener.setEnabled( agent.isEnabled() );
                        break;
                    }
                }

                if ( !found )
                {
                    BuildAgentListener listener = new DefaultBuildAgentListener( agent.getUrl(), false, agent.isEnabled() );
                    listeners.add( listener );
                    log.info( "add listener for build agent '" + agent.getUrl() + "'" );
                }
            }
        }

        buildProjectsInQueue();
    }

    public ConfigurationService getConfigurationService()
    {
        return configurationService;
    }

    public void setConfigurationService( ConfigurationService configurationService )
    {
        this.configurationService = configurationService;
    }

    public ProjectDao getProjectDao()
    {
        return projectDao;
    }

    public void setProjectDao( ProjectDao projectDao )
    {
        this.projectDao = projectDao;
    }

    public BuildDefinitionDao getBuildDefinitionDao()
    {
        return buildDefinitionDao;
    }

    public void setBuildDefinitionDao( BuildDefinitionDao buildDefinitionDao )
    {
        this.buildDefinitionDao = buildDefinitionDao;
    }

    public BuildResultDao getBuildResultDao()
    {
        return buildResultDao;
    }

    public void setBuildResultDao( BuildResultDao buildResultDao )
    {
        this.buildResultDao = buildResultDao;
    }

    public ProjectScmRootDao getProjectScmRootDao()
    {
        return projectScmRootDao;
    }

    public void setProjectScmRootDao( ProjectScmRootDao projectScmRootDao )
    {
        this.projectScmRootDao = projectScmRootDao;
    }

    public void buildProjects( Map<Integer, Integer> projectsAndBuildDefinitionsMap, int trigger )
        throws ContinuumException
    {
        buildProjects( projectsAndBuildDefinitionsMap, trigger, false );
    }

    public void buildProjectsInQueue()
        throws ContinuumException
    {
        if ( projectsBuildInQueue != null )
        {
            for ( PrepareBuildProjectsTask task : projectsBuildInQueue )
            {
                Map projectsAndBuildDefinitions = task.getProjectsBuildDefinitionsMap();
                int trigger = task.getTrigger();
                
                buildProjects( projectsAndBuildDefinitions, trigger, true );
            }
        }
    }

    public synchronized void buildProjects( Map<Integer, Integer> projectsAndBuildDefinitionsMap, int trigger, boolean inBuildQueue )
        throws ContinuumException
    {
        boolean found = false;
        
        for ( BuildAgentListener listener : listeners )
        {
            if ( !listener.isBusy() && listener.isEnabled() )
            {
                log.info( "initializing buildContext" );
                List buildContext = initializeBuildContext( projectsAndBuildDefinitionsMap, trigger, listener );
/*
                try
                {
                    ContinuumDistributedBuildClient client = new ContinuumDistributedBuildClient( new URL( listener.getUrl() ) );
                    client.ping();
                }
                catch ( MalformedURLException e )
                {
                    throw new ContinuumException( "Invalid url", e );
                }
                catch ( XmlRpcException e )
                {
                    throw new ContinuumException( "", e );
                }
                catch ( Exception e )
                {
                    
                }*/

                //{
                    //client.ping();
                    //found = true; 
                    //client.buildProjects( buildContext );
                //}
                //catch ( XmlRpcException e )
                //{
                    //do something about the server Url
                    //client.getServerUrl();
                    //get projects of buildagent and set to build error the first project.
                //}
                log.info( "dispatched build to " + listener.getUrl() );
                found = true;
            }
        }

        if ( !found && !inBuildQueue )
        {
            // all build agents are busy, put into projectBuildQueue for now
            if ( projectsBuildInQueue == null )
            {
                projectsBuildInQueue = new ArrayList<PrepareBuildProjectsTask>();
            }

            log.info( "no build agent available, put in queue" );
            PrepareBuildProjectsTask prepareBuildTask = new PrepareBuildProjectsTask( projectsAndBuildDefinitionsMap, trigger );
            projectsBuildInQueue.add( prepareBuildTask );
        }
    }

    public void updateProjectScmRoot( Map context )
        throws ContinuumException
    {
        try
        {
            int projectId = getProjectId( context );
        
            log.info( "update scm result of project" + projectId );
            Project project = projectDao.getProjectWithScmDetails( projectId );
            
            ScmResult scmResult = new ScmResult();
            scmResult.setCommandLine( getScmCommandLine( context ) );
            scmResult.setCommandOutput( getScmCommandOutput( context ) );
            scmResult.setException( getScmException( context ) );
            scmResult.setProviderMessage( getScmProviderMessage( context ) );

            String error = convertScmResultToError( scmResult );

            if ( error == null )
            {
                scmResult.setSuccess( true );
            }
            else
            {
                scmResult.setSuccess( false );
            }

            project.setScmResult( scmResult );
            projectDao.updateProject( project );

            if ( error != null || isPrepareBuildFinished( context ) )
            {
                List<ProjectScmRoot> scmRoots = projectScmRootDao.getProjectScmRootByProjectGroup( project.getProjectGroup().getId() );
                
                for ( ProjectScmRoot scmRoot : scmRoots )
                {
                    if ( project.getScmUrl().startsWith( scmRoot.getScmRootAddress() ) )
                    {
                        if ( error != null )
                        {
                            scmRoot.setError( error );
                            scmRoot.setState( ContinuumProjectState.ERROR );
                        }
                        else
                        {
                            scmRoot.setState( ContinuumProjectState.UPDATED );
                        }
                        projectScmRootDao.updateProjectScmRoot( scmRoot );
                    }
                }
            }

            if ( error != null )
            {
                log.info( "scm error, not building" );
                updateBuildAgent( project.getId(), true );
            }
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumException( "Error updating project scm root", e );
        }
    }

    public void updateBuildResult( Map context )
        throws ContinuumException
    {
        try
        {
            int projectId = getProjectId( context );
            int buildDefinitionId = getBuildDefinitionId( context );

            log.info( "update build result of project '" + projectId + "'" );

            Project project = projectDao.getProjectWithAllDetails( projectId );
            BuildDefinition buildDefinition = buildDefinitionDao.getBuildDefinition( buildDefinitionId );

            BuildResult oldBuildResult =
                buildResultDao.getLatestBuildResultForBuildDefinition( projectId, buildDefinitionId );

            int buildNumber;

            if ( getBuildState( context ) == ContinuumProjectState.OK )
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

            BuildResult buildResult = new BuildResult();

            buildResult.setStartTime( getBuildStart( context ) );
            buildResult.setEndTime( getBuildEnd( context ) );
            buildResult.setBuildDefinition( buildDefinition );
            buildResult.setBuildNumber( buildNumber );
            buildResult.setError( getBuildError( context ) );
            buildResult.setExitCode( getBuildExitCode( context ) );
            buildResult.setModifiedDependencies( getModifiedDependencies( oldBuildResult, context ) );
            buildResult.setState( getBuildState( context ) );
            buildResult.setTrigger( getTrigger( context ) );
            
            buildResultDao.addBuildResult( project, buildResult );
            
            project.setBuildNumber( buildNumber );
            project.setLatestBuildId( buildResult.getId() );
            project.setOldState( project.getState() );
            project.setState( getBuildState( context ) );

            projectDao.updateProject( project );

            updateBuildAgent( project.getId(), false );
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumException( "Error while updating build result for project", e );
        }
    }

    public void reload()
        throws ContinuumException
    {
        this.initialize();
    }

    private List initializeBuildContext( Map<Integer, Integer> projectsAndBuildDefinitions, 
                                         int trigger, BuildAgentListener listener )
        throws ContinuumException
    {
        List buildContext = new ArrayList();
        List<Project> projects = new ArrayList<Project>();

        try
        {
            for ( Integer projectId : projectsAndBuildDefinitions.keySet() )
            {
                Project project = projectDao.getProjectWithDependencies( projectId );
                projects.add( project );
            }

            try
            {
                projects = ProjectSorter.getSortedProjects( projects, null );
            }
            catch ( CycleDetectedException e )
            {
                log.info( "Cycle Detected" );
            }

            int ctr = 0;
            
            for ( Project project : projects )
            {
                if ( ctr == 0 )
                {
                    List<ProjectScmRoot> scmRoots = projectScmRootDao.getProjectScmRootByProjectGroup( project.getProjectGroup().getId() );
                    for ( ProjectScmRoot scmRoot : scmRoots )
                    {
                        if ( project.getScmUrl().startsWith( scmRoot.getScmRootAddress() ) )
                        {
                            scmRoot.setOldState( scmRoot.getState() );
                            scmRoot.setState( ContinuumProjectState.UPDATING );
                            projectScmRootDao.updateProjectScmRoot( scmRoot );
                            break;
                        }
                    }
                }
                
                int buildDefinitionId = projectsAndBuildDefinitions.get( project.getId() );
                BuildDefinition buildDef = buildDefinitionDao.getBuildDefinition( buildDefinitionId );
                BuildResult oldBuildResult =
                    buildResultDao.getLatestBuildResultForBuildDefinition( project.getId(), buildDefinitionId );

                Map context = new HashMap();
                context.put( KEY_PROJECT_ID, project.getId() );
                context.put( KEY_EXECUTOR_ID, project.getExecutorId() );
                context.put( KEY_SCM_URL, project.getScmUrl() );
                context.put( KEY_SCM_USERNAME, project.getScmUsername() );
                context.put( KEY_SCM_PASSWORD, project.getScmPassword() );
                context.put( KEY_BUILD_DEFINITION_ID, buildDefinitionId );
                context.put( KEY_BUILD_FILE, buildDef.getBuildFile() );
                context.put( KEY_GOALS, buildDef.getGoals() );
                context.put( KEY_ARGUMENTS, buildDef.getArguments() );
                context.put( KEY_TRIGGER, trigger );
                context.put( KEY_BUILD_FRESH, buildDef.isBuildFresh() );
                
                buildContext.add( context );
                ctr++;
            }
            
            listener.setBusy( true );
            listener.setProjects( projects );

            return buildContext;
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumException( "Error while initializing build context", e );
        }
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
            Project project = projectDao.getProjectWithAllDetails( getProjectId( context ) );
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

    private void updateBuildAgent( int projectId, boolean removeAll )
        throws ContinuumException
    {
        for ( BuildAgentListener listener : listeners )
        {
            for ( Project project : listener.getProjects() )
            {
                if ( project.getId() == projectId )
                {
                    if ( removeAll )
                    {
                        log.info( "available build agent '" + listener.getUrl() + "'" );

                        listener.setProjects( null );
                        listener.setBusy( false );

                        buildProjectsInQueue();
                    }
                    else
                    {
                        listener.getProjects().remove( project );

                        if ( !listener.hasProjects() )
                        {
                            log.info( "available build agent '" + listener.getUrl() + "'" );
                            
                            listener.setBusy( false );

                            buildProjectsInQueue();
                        }
                    }
                    return;
                }
            }
        }
    }

    public List<BuildAgentListener> getBuildAgentListeners()
    {
        return listeners;
    }
}
