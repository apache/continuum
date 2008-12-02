package org.apache.continuum.distributed.manager;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.continuum.configuration.BuildAgentConfiguration;
import org.apache.continuum.dao.BuildDefinitionDao;
import org.apache.continuum.dao.BuildResultDao;
import org.apache.continuum.dao.ProjectDao;
import org.apache.continuum.dao.ProjectScmRootDao;
import org.apache.continuum.distributed.BuildAgent;
import org.apache.continuum.model.project.ProjectScmRoot;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectDependency;
import org.apache.maven.continuum.model.scm.ScmResult;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.scm.queue.PrepareBuildProjectsTask;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.apache.maven.continuum.utils.ProjectSorter;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.dag.CycleDetectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Maria Catherine Tan
 */
public class DefaultDistributedBuildManager
    extends AbstractDistributedBuildManager
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

    private List<PrepareBuildProjectsTask> projectsBuildQueue;
    
    private List<BuildAgent> buildAgents;

    public void initialize()
    {
        List<BuildAgentConfiguration> agents = configurationService.getBuildAgents();

        if ( buildAgents == null )
        {
            buildAgents = new ArrayList<BuildAgent>();
        }
        
        if ( agents != null )
        {
            for ( BuildAgentConfiguration agent : agents )
            {
                if ( agent.isEnabled() )
                { 
                    boolean found = false;
    
                    for ( BuildAgent buildAgent : buildAgents )
                    {
                        if ( buildAgent.getUrl().equals( agent.getUrl() ) )
                        {
                            found = true;
                            break;
                        }
                    }
    
                    if ( !found )
                    {
                        // ping it 
                        BuildAgent buildAgent = new BuildAgent();
                        buildAgent.setUrl( agent.getUrl() );
                        buildAgent.setBusy( false );
                        buildAgents.add( buildAgent );
                    }
                }
            }
        }
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
        for ( PrepareBuildProjectsTask task : projectsBuildQueue )
        {
            Map projectsAndBuildDefinitions = task.getProjectsBuildDefinitionsMap();
            int trigger = task.getTrigger();
            
            buildProjects( projectsAndBuildDefinitions, trigger, true );
        }
    }

    public synchronized void buildProjects( Map<Integer, Integer> projectsAndBuildDefinitionsMap, int trigger, boolean inBuildQueue )
        throws ContinuumException
    {
        boolean found = false;
        
        for ( BuildAgent buildAgent : buildAgents )
        {
            if ( !buildAgent.isBusy() )
            {
                List buildContext = initializeBuildContext( projectsAndBuildDefinitionsMap, trigger, buildAgent );

                //BuildAgentXMLRpcClient client = new BuildAgentXmlRpcClient( buildAgent.getUrl(), null, null );
                
                //try
                //{
                    //client.buildProjects( buildContext );
                //}
                //catch ( InterruptedException e )
                //{
                    //do something about the server Url
                    //client.getServerUrl();
                    //get projects of buildagent and set to build error the first project.
                //}
                log.info( "dispatched build to " + buildAgent.getUrl() );
                found = true;
            }
        }

        if ( !found && !inBuildQueue )
        {
            // all build agents are busy, put into projectBuildQueue for now
            if ( projectsBuildQueue == null )
            {
                projectsBuildQueue = new ArrayList<PrepareBuildProjectsTask>();
            }

            PrepareBuildProjectsTask prepareBuildTask = new PrepareBuildProjectsTask( projectsAndBuildDefinitionsMap, trigger );
            projectsBuildQueue.add( prepareBuildTask );
        }
    }

    public void updateProjectScmRoot( Map context )
        throws ContinuumException
    {
        try
        {
            int projectId = getProjectId( context );
            
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

            Project project = projectDao.getProjectWithBuildDetails( projectId );
            BuildDefinition buildDefinition = buildDefinitionDao.getBuildDefinition( buildDefinitionId );

            BuildResult oldBuildResult =
                buildResultDao.getLatestBuildResultForBuildDefinition( projectId, buildDefinitionId );

            int buildNumber = project.getBuildNumber() + 1;

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
            buildResult.setProject( project );
            buildResult.setState( getBuildState( context ) );
            buildResult.setTrigger( getTrigger( context ) );
            
            buildResultDao.addBuildResult( project, buildResult );
            
            project.setBuildNumber( buildNumber );
            project.setLatestBuildId( buildResult.getId() );
            project.setOldState( project.getState() );
            project.setState( getBuildState( context ) );

            projectDao.updateProject( project );

            updateBuildAgent( context );
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumException( "Error while updating build result for project", e );
        }
    }

    public void reload()
    {
        this.initialize();
    }

    private List initializeBuildContext( Map<Integer, Integer> projectsAndBuildDefinitions, int trigger, BuildAgent buildAgent )
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
            
            buildAgent.setBusy( true );
            buildAgent.setProjects( projects );

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

    private void updateBuildAgent( Map context )
        throws ContinuumException
    {
        for ( BuildAgent buildAgent : buildAgents )
        {
            for ( Project project : buildAgent.getProjects() )
            {
                if ( project.getId() == getProjectId( context ) )
                {
                    buildAgent.getProjects().remove( project );
                    
                    if ( buildAgent.isBusy() && ( buildAgent.getProjects() == null || buildAgent.getProjects().size() == 0 ) )
                    {
                        buildAgent.setBusy( false );
                    }

                    buildProjectsInQueue();
                    return;
                }
            }
        }
    }

    public List<BuildAgent> getBuildAgents()
    {
        return buildAgents;
    }
}
