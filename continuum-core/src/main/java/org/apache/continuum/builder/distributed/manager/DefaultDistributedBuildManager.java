package org.apache.continuum.builder.distributed.manager;

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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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
import org.apache.maven.continuum.configuration.ConfigurationException;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.execution.ContinuumBuildExecutorConstants;
import org.apache.maven.continuum.installation.InstallationService;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectDependency;
import org.apache.maven.continuum.model.project.ProjectDeveloper;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.model.scm.ChangeFile;
import org.apache.maven.continuum.model.scm.ChangeSet;
import org.apache.maven.continuum.model.scm.ScmResult;
import org.apache.maven.continuum.model.system.Installation;
import org.apache.maven.continuum.model.system.Profile;
import org.apache.maven.continuum.notification.ContinuumNotificationDispatcher;
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
 * @plexus.component role="org.apache.continuum.builder.distributed.manager.DistributedBuildManager"
 */
public class DefaultDistributedBuildManager
    implements DistributedBuildManager, Contextualizable, Initializable
{
    private static final Logger log = LoggerFactory.getLogger( DefaultDistributedBuildManager.class );

    /**
     * @plexus.requirement
     */
    private ConfigurationService configurationService;

    /**
     * @plexus.requirement
     */
    private InstallationService installationService;

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

    /**
     * @plexus.requirement
     */
    private ContinuumNotificationDispatcher notifierDispatcher;

    private PlexusContainer container;

    private Map<String, DistributedBuildTaskQueueExecutor> taskQueueExecutors;

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
        taskQueueExecutors = new HashMap<String, DistributedBuildTaskQueueExecutor>();

        List<BuildAgentConfiguration> agents = configurationService.getBuildAgents();

        if ( agents != null )
        {
            for ( BuildAgentConfiguration agent : agents )
            {
                if ( agent.isEnabled() )
                {
                    try
                    {
                        SlaveBuildAgentTransportClient client =
                            new SlaveBuildAgentTransportClient( new URL( agent.getUrl() ) );

                        if ( client.ping() )
                        {
                            log.info(
                                "agent is enabled, add TaskQueueExecutor for build agent '" + agent.getUrl() + "'" );
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
                        throw new InitializationException(
                            "Error while initializing distributed build task queue executors", e );
                    }
                    catch ( Exception e )
                    {
                        agent.setEnabled( false );
                        log.info( "unable to ping build agent '" + agent.getUrl() + "': " +
                            ContinuumUtils.throwableToString( e ) );
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
                    SlaveBuildAgentTransportClient client =
                        new SlaveBuildAgentTransportClient( new URL( agent.getUrl() ) );

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
                    log.info( "unable to ping build agent '" + agent.getUrl() + "': " +
                        ContinuumUtils.throwableToString( e ) );
                }
            }
            else if ( !agent.isEnabled() && taskQueueExecutors.containsKey( agent.getUrl() ) )
            {
                log.info( "agent is disabled, remove TaskQueueExecutor for build agent '" + agent.getUrl() + "'" );
                removeAgentFromTaskQueueExecutor( agent.getUrl() );
            }
        }
    }

    public void removeAgentFromTaskQueueExecutor( String buildAgentUrl )
        throws ContinuumException
    {
        log.info( "remove TaskQueueExecutor for build agent '" + buildAgentUrl + "'" );
        ThreadedDistributedBuildTaskQueueExecutor executor =
            (ThreadedDistributedBuildTaskQueueExecutor) taskQueueExecutors.get( buildAgentUrl );

        if ( executor == null )
        {
            return;
        }

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
        DistributedBuildTaskQueueExecutor executor = taskQueueExecutors.get( buildAgentUrl );

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
            DistributedBuildTaskQueueExecutor taskQueueExecutor = (DistributedBuildTaskQueueExecutor) container.
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
        DistributedBuildTaskQueueExecutor taskQueueExecutor = taskQueueExecutors.get( buildAgentUrl );

        if ( taskQueueExecutor != null )
        {
            if ( taskQueueExecutor.getCurrentTask() != null )
            {
                if ( taskQueueExecutor.getCurrentTask() instanceof PrepareBuildProjectsTask )
                {
                    PrepareBuildProjectsTask currentTask = (PrepareBuildProjectsTask) taskQueueExecutor.getCurrentTask()
                        ;

                    if ( currentTask.getProjectGroupId() == projectGroupId &&
                        currentTask.getScmRootAddress().equals( scmRootAddress ) )
                    {
                        log.info( "cancelling task for project group " + projectGroupId + " with scm root address " +
                            scmRootAddress );
                        taskQueueExecutor.cancelTask( currentTask );

                        try
                        {
                            SlaveBuildAgentTransportClient client =
                                new SlaveBuildAgentTransportClient( new URL( buildAgentUrl ) );
                            client.cancelBuild();
                        }
                        catch ( Exception e )
                        {
                            log.error( "Error while cancelling build in build agent '" + buildAgentUrl + "'" );
                            throw new ContinuumException(
                                "Error while cancelling build in build agent '" + buildAgentUrl + "'", e );
                        }
                    }
                    else
                    {
                        log.info( "current task not for project group " + projectGroupId + " with scm root address " +
                            scmRootAddress );
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

            if ( buildResult.getState() != ContinuumProjectState.CANCELLED )
            {
                buildResult.setBuildDefinition( buildDefinition );
                buildResult.setBuildNumber( buildNumber );
                buildResult.setModifiedDependencies( getModifiedDependencies( oldBuildResult, context ) );
                buildResult.setScmResult( getScmResult( context ) );

                Date date = ContinuumBuildConstant.getLatestUpdateDate( context );
                if ( date != null )
                {
                    buildResult.setLastChangedDate( date.getTime() );
                }
                else if ( oldBuildResult != null )
                {
                    buildResult.setLastChangedDate( oldBuildResult.getLastChangedDate() );
                }

                buildResultDao.addBuildResult( project, buildResult );

                project.setOldState( project.getState() );
                project.setState( ContinuumBuildConstant.getBuildState( context ) );
                project.setBuildNumber( buildNumber );
                project.setLatestBuildId( buildResult.getId() );
            }
            else
            {
                project.setState( project.getOldState() );
                project.setOldState( 0 );
            }

            projectDao.updateProject( project );

            File buildOutputFile = configurationService.getBuildOutputFile( buildResult.getId(), project.getId() );

            FileWriter fstream = new FileWriter( buildOutputFile );
            BufferedWriter out = new BufferedWriter( fstream );
            out.write( ContinuumBuildConstant.getBuildOutput( context ) == null ? ""
                : ContinuumBuildConstant.getBuildOutput( context ) );
            out.close();

            if ( buildResult.getState() != ContinuumProjectState.CANCELLED )
            {
                notifierDispatcher.buildComplete( project, buildDefinition, buildResult );
            }
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumException( "Error while updating build result for project", e );
        }
        catch ( ConfigurationException e )
        {
            throw new ContinuumException( "Error retrieving build output file", e );
        }
        catch ( IOException e )
        {
            throw new ContinuumException( "Error while writing build output to file", e );
        }
    }

    public void prepareBuildFinished( Map context )
        throws ContinuumException
    {
        int projectGroupId = ContinuumBuildConstant.getProjectGroupId( context );
        String scmRootAddress = ContinuumBuildConstant.getScmRootAddress( context );

        try
        {
            ProjectScmRoot scmRoot =
                projectScmRootDao.getProjectScmRootByProjectGroupAndScmRootAddress( projectGroupId, scmRootAddress );

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

            notifierDispatcher.prepareBuildComplete( scmRoot );
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
            DistributedBuildTaskQueueExecutor taskQueueExecutor = taskQueueExecutors.get( url );

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
        List<Installation> installations = new ArrayList<Installation>();

        try
        {
            SlaveBuildAgentTransportClient client = new SlaveBuildAgentTransportClient( new URL( buildAgentUrl ) );

            List<Map<String, String>> installationsList = client.getAvailableInstallations();

            for ( Map context : installationsList )
            {
                Installation installation = new Installation();
                installation.setName( ContinuumBuildConstant.getInstallationName( context ) );
                installation.setType( ContinuumBuildConstant.getInstallationType( context ) );
                installation.setVarName( ContinuumBuildConstant.getInstallationVarName( context ) );
                installation.setVarValue( ContinuumBuildConstant.getInstallationVarValue( context ) );
                installations.add( installation );
            }

            return installations;
        }
        catch ( Exception e )
        {
            throw new ContinuumException( "Unable to get available installations of build agent", e );
        }
    }

    public void startProjectBuild( int projectId )
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

    public void startPrepareBuild( Map context )
        throws ContinuumException
    {
        try
        {
            int projectGroupId = ContinuumBuildConstant.getProjectGroupId( context );
            String scmRootAddress = ContinuumBuildConstant.getScmRootAddress( context );

            ProjectScmRoot scmRoot =
                projectScmRootDao.getProjectScmRootByProjectGroupAndScmRootAddress( projectGroupId, scmRootAddress );
            scmRoot.setOldState( scmRoot.getState() );
            scmRoot.setState( ContinuumProjectState.UPDATING );
            projectScmRootDao.updateProjectScmRoot( scmRoot );
        }
        catch ( ContinuumStoreException e )
        {
            log.error( "Error while updating project scm root's state", e );
            throw new ContinuumException( "Error while updating project scm root's state", e );
        }
    }

    public Map<String, Object> getBuildResult( int projectId )
        throws ContinuumException
    {
        Map<String, Object> map = new HashMap<String, Object>();

        String buildAgentUrl = getBuildAgent( projectId );

        if ( buildAgentUrl == null )
        {
            return null;
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
                buildResult.setScmResult( getScmResult( result ) );

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
            throw new ContinuumException( "Error while retrieving build result for project" + projectId, e );
        }

        return map;
    }

    public Map<String, String> getEnvironments( int buildDefinitionId, String installationType )
        throws ContinuumException
    {
        BuildDefinition buildDefinition;

        try
        {
            buildDefinition = buildDefinitionDao.getBuildDefinition( buildDefinitionId );
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumException( "Failed to retrieve build definition: " + buildDefinitionId, e );
        }

        Profile profile = buildDefinition.getProfile();
        if ( profile == null )
        {
            return Collections.EMPTY_MAP;
        }
        Map<String, String> envVars = new HashMap<String, String>();
        String javaHome = getJavaHomeValue( buildDefinition );
        if ( !StringUtils.isEmpty( javaHome ) )
        {
            envVars.put( installationService.getEnvVar( InstallationService.JDK_TYPE ), javaHome );
        }
        Installation builder = profile.getBuilder();
        if ( builder != null )
        {
            envVars.put( installationService.getEnvVar( installationType ), builder.getVarValue() );
        }
        envVars.putAll( getEnvironmentVariables( buildDefinition ) );
        return envVars;
    }

    public void updateProject( Map context )
        throws ContinuumException
    {
        try
        {
            Project project = projectDao.getProject( ContinuumBuildConstant.getProjectId( context ) );

            if ( StringUtils.isNotBlank( ContinuumBuildConstant.getGroupId( context ) ) )
            {
                project.setGroupId( ContinuumBuildConstant.getGroupId( context ) );
            }
            if ( StringUtils.isNotBlank( ContinuumBuildConstant.getArtifactId( context ) ) )
            {
                project.setArtifactId( ContinuumBuildConstant.getArtifactId( context ) );
            }
            if ( StringUtils.isNotBlank( ContinuumBuildConstant.getVersion( context ) ) )
            {
                project.setVersion( ContinuumBuildConstant.getVersion( context ) );
            }
            if ( StringUtils.isNotBlank( ContinuumBuildConstant.getProjectName( context ) ) )
            {
                project.setName( ContinuumBuildConstant.getProjectName( context ) );
            }
            if ( StringUtils.isNotBlank( ContinuumBuildConstant.getProjectDescription( context ) ) )
            {
                project.setDescription( ContinuumBuildConstant.getProjectDescription( context ) );
            }
            if ( StringUtils.isNotBlank( ContinuumBuildConstant.getProjectUrl( context ) ) )
            {
                project.setUrl( ContinuumBuildConstant.getProjectUrl( context ) );
            }
            if ( StringUtils.isNotBlank( ContinuumBuildConstant.getScmUrl( context ) ) )
            {
                project.setScmUrl( ContinuumBuildConstant.getScmUrl( context ) );
            }
            if ( StringUtils.isNotBlank( ContinuumBuildConstant.getScmTag( context ) ) )
            {
                project.setScmTag( ContinuumBuildConstant.getScmTag( context ) );
            }
            project.setParent( getProjectParent( context ) );
            project.setDependencies( getProjectDependencies( context ) );
            project.setDevelopers( getProjectDevelopers( context ) );
            project.setNotifiers( getProjectNotifiers( context ) );

            projectDao.updateProject( project );
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumException( "Unable to update project from working copy", e );
        }
    }

    public String generateWorkingCopyContent( int projectId, String directory, String baseUrl, String imageBaseUrl )
        throws ContinuumException
    {
        BuildResult buildResult = buildResultDao.getLatestBuildResultForProject( projectId );

        if ( buildResult != null )
        {
            String buildAgentUrl = buildResult.getBuildUrl();

            if ( buildAgentUrl == null )
            {
                return "";
            }

            try
            {
                if ( directory == null )
                {
                    directory = "";
                }

                SlaveBuildAgentTransportClient client = new SlaveBuildAgentTransportClient( new URL( buildAgentUrl ) );
                return client.generateWorkingCopyContent( projectId, directory, baseUrl, imageBaseUrl );
            }
            catch ( MalformedURLException e )
            {
                log.error( "Invalid build agent url " + buildAgentUrl );
            }
            catch ( Exception e )
            {
                log.error( "Error while generating working copy content from build agent " + buildAgentUrl, e );
            }
        }
        return "";
    }

    public String getFileContent( int projectId, String directory, String filename )
        throws ContinuumException
    {
        BuildResult buildResult = buildResultDao.getLatestBuildResultForProject( projectId );

        if ( buildResult != null )
        {
            String buildAgentUrl = buildResult.getBuildUrl();

            if ( buildAgentUrl == null )
            {
                return "";
            }

            try
            {
                SlaveBuildAgentTransportClient client = new SlaveBuildAgentTransportClient( new URL( buildAgentUrl ) );
                return client.getProjectFileContent( projectId, directory, filename );
            }
            catch ( MalformedURLException e )
            {
                log.error( "Invalid build agent url " + buildAgentUrl );
            }
            catch ( Exception e )
            {
                log.error( "Error while retrieving content of " + filename, e );
            }
        }
        return "";
    }

    public boolean shouldBuild( Map context )
    {
        try
        {
            int projectId = ContinuumBuildConstant.getProjectId( context );

            int buildDefinitionId = ContinuumBuildConstant.getBuildDefinitionId( context );

            int trigger = ContinuumBuildConstant.getTrigger( context );

            Project project = projectDao.getProjectWithAllDetails( projectId );

            BuildDefinition buildDefinition = buildDefinitionDao.getBuildDefinition( buildDefinitionId );

            BuildResult oldBuildResult =
                buildResultDao.getLatestBuildResultForBuildDefinition( projectId, buildDefinitionId );

            List<ProjectDependency> modifiedDependencies = getModifiedDependencies( oldBuildResult, context );

            List<ChangeSet> changes = getScmChanges( context );

            if ( buildDefinition.isBuildFresh() )
            {
                log.info( "FreshBuild configured, building" );
                return true;
            }
            if ( buildDefinition.isAlwaysBuild() )
            {
                log.info( "AlwaysBuild configured, building" );
                return true;
            }
            if ( oldBuildResult == null )
            {
                log.info( "The project was never be built with the current build definition, building" );
                return true;
            }

            //CONTINUUM-1428
            if ( project.getOldState() == ContinuumProjectState.ERROR ||
                oldBuildResult.getState() == ContinuumProjectState.ERROR )
            {
                log.info( "Latest state was 'ERROR', building" );
                return true;
            }

            if ( trigger == ContinuumProjectState.TRIGGER_FORCED )
            {
                log.info( "The project build is forced, building" );
                return true;
            }

            Date date = ContinuumBuildConstant.getLatestUpdateDate( context );
            if ( date != null && oldBuildResult.getLastChangedDate() >= date.getTime() )
            {
                log.info( "No changes found,not building" );
                return false;
            }
            else if ( date != null && changes.isEmpty() )
            {
                // fresh checkout from build agent that's why changes is empty
                log.info( "Changes found in the current project, building" );
                return true;
            }

            boolean shouldBuild = false;

            boolean allChangesUnknown = true;

            if ( project.getOldState() != ContinuumProjectState.NEW &&
                project.getOldState() != ContinuumProjectState.CHECKEDOUT &&
                project.getState() != ContinuumProjectState.NEW &&
                project.getState() != ContinuumProjectState.CHECKEDOUT )
            {
                // Check SCM changes
                allChangesUnknown = checkAllChangesUnknown( changes );

                if ( allChangesUnknown )
                {
                    if ( !changes.isEmpty() )
                    {
                        log.info(
                            "The project was not built because all changes are unknown (maybe local modifications or ignored files not defined in your SCM tool." );
                    }
                    else
                    {
                        log.info(
                            "The project was not built because no changes were detected in sources since the last build." );
                    }
                }

                // Check dependencies changes
                if ( modifiedDependencies != null && !modifiedDependencies.isEmpty() )
                {
                    log.info( "Found dependencies changes, building" );
                    shouldBuild = true;
                }
            }

            // Check changes
            if ( !shouldBuild && ( ( !allChangesUnknown && !changes.isEmpty() ) ||
                project.getExecutorId().equals( ContinuumBuildExecutorConstants.MAVEN_TWO_BUILD_EXECUTOR ) ) )
            {
                shouldBuild = shouldBuild( changes, buildDefinition, project, getMavenProjectVersion( context ),
                                           getMavenProjectModules( context ) );
            }

            if ( shouldBuild )
            {
                log.info( "Changes found in the current project, building" );
            }
            else
            {
                log.info( "No changes in the current project, not building" );
            }

            return shouldBuild;
        }
        catch ( ContinuumStoreException e )
        {
            log.error( "Failed to determine if project should build", e );
        }
        catch ( ContinuumException e )
        {
            log.error( "Failed to determine if project should build", e );
        }

        return false;
    }

    private boolean shouldBuild( List<ChangeSet> changes, BuildDefinition buildDefinition, Project project,
                                 String mavenProjectVersion, List<String> mavenProjectModules )
    {
        //Check if it's a recursive build
        boolean isRecursive = false;
        if ( StringUtils.isNotEmpty( buildDefinition.getArguments() ) )
        {
            isRecursive = buildDefinition.getArguments().indexOf( "-N" ) < 0 &&
                buildDefinition.getArguments().indexOf( "--non-recursive" ) < 0;
        }

        if ( isRecursive && changes != null && !changes.isEmpty() )
        {
            if ( log.isInfoEnabled() )
            {
                log.info( "recursive build and changes found --> building" );
            }
            return true;
        }

        if ( !project.getVersion().equals( mavenProjectVersion ) )
        {
            log.info( "Found changes in project's version ( maybe project was recently released ), building" );
            return true;
        }

        if ( changes == null || changes.isEmpty() )
        {
            if ( log.isInfoEnabled() )
            {
                log.info( "Found no changes, not building" );
            }
            return false;
        }

        //check if changes are only in sub-modules or not
        List<ChangeFile> files = new ArrayList<ChangeFile>();
        for ( ChangeSet changeSet : changes )
        {
            files.addAll( changeSet.getFiles() );
        }

        int i = 0;
        while ( i <= files.size() - 1 )
        {
            ChangeFile file = files.get( i );
            if ( log.isDebugEnabled() )
            {
                log.debug( "changeFile.name " + file.getName() );
                log.debug( "check in modules " + mavenProjectModules );
            }
            boolean found = false;
            if ( mavenProjectModules != null )
            {
                for ( String module : mavenProjectModules )
                {
                    if ( file.getName().indexOf( module ) >= 0 )
                    {
                        if ( log.isDebugEnabled() )
                        {
                            log.debug( "changeFile.name " + file.getName() + " removed because in a module" );
                        }
                        files.remove( file );
                        found = true;
                        break;
                    }
                    if ( log.isDebugEnabled() )
                    {
                        log.debug( "not removing file " + file.getName() + " not in module " + module );
                    }
                }
            }
            if ( !found )
            {
                i++;
            }
        }

        boolean shouldBuild = !files.isEmpty();

        if ( !shouldBuild )
        {
            log.info( "Changes are only in sub-modules." );
        }

        if ( log.isDebugEnabled() )
        {
            log.debug( "shoulbuild = " + shouldBuild );
        }

        return shouldBuild;
    }

    private boolean checkAllChangesUnknown( List<ChangeSet> changes )
    {
        for ( ChangeSet changeSet : changes )
        {
            List<ChangeFile> changeFiles = changeSet.getFiles();

            for ( ChangeFile changeFile : changeFiles )
            {
                if ( !"unknown".equalsIgnoreCase( changeFile.getStatus() ) )
                {
                    return false;
                }
            }
        }

        return true;
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
                    List<BuildResult> buildResults =
                        buildResultDao.getBuildResultsInSuccessForProject( dependencyProject.getId(),
                                                                           oldBuildResult.getEndTime() );
                    if ( buildResults != null && !buildResults.isEmpty() )
                    {
                        log.debug( "Dependency changed: " + dep.getGroupId() + ":" + dep.getArtifactId() + ":" +
                            dep.getVersion() );
                        modifiedDependencies.add( dep );
                    }
                    else
                    {
                        log.debug( "Dependency not changed: " + dep.getGroupId() + ":" + dep.getArtifactId() + ":" +
                            dep.getVersion() );
                    }
                }
                else
                {
                    log.debug( "Skip non Continuum project: " + dep.getGroupId() + ":" + dep.getArtifactId() + ":" +
                        dep.getVersion() );
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

        buildResult.setStartTime( ContinuumBuildConstant.getStartTime( context ) );
        buildResult.setEndTime( ContinuumBuildConstant.getEndTime( context ) );
        buildResult.setError( ContinuumBuildConstant.getBuildError( context ) );
        buildResult.setExitCode( ContinuumBuildConstant.getBuildExitCode( context ) );
        buildResult.setState( ContinuumBuildConstant.getBuildState( context ) );
        buildResult.setTrigger( ContinuumBuildConstant.getTrigger( context ) );
        buildResult.setBuildUrl( ContinuumBuildConstant.getBuildAgentUrl( context ) );

        return buildResult;
    }

    private String getJavaHomeValue( BuildDefinition buildDefinition )
    {
        Profile profile = buildDefinition.getProfile();
        if ( profile == null )
        {
            return null;
        }
        Installation jdk = profile.getJdk();
        if ( jdk == null )
        {
            return null;
        }
        return jdk.getVarValue();
    }

    private Map<String, String> getEnvironmentVariables( BuildDefinition buildDefinition )
    {
        Profile profile = buildDefinition.getProfile();
        Map<String, String> envVars = new HashMap<String, String>();
        if ( profile == null )
        {
            return envVars;
        }
        List<Installation> environmentVariables = profile.getEnvironmentVariables();
        if ( environmentVariables.isEmpty() )
        {
            return envVars;
        }
        for ( Installation installation : environmentVariables )
        {
            envVars.put( installation.getVarName(), installation.getVarValue() );
        }
        return envVars;
    }

    private List<ChangeSet> getScmChanges( Map context )
    {
        List<ChangeSet> changes = new ArrayList<ChangeSet>();
        List<Map> scmChanges = ContinuumBuildConstant.getScmChanges( context );

        if ( scmChanges != null )
        {
            for ( Map map : scmChanges )
            {
                ChangeSet changeSet = new ChangeSet();
                changeSet.setAuthor( ContinuumBuildConstant.getChangeSetAuthor( map ) );
                changeSet.setComment( ContinuumBuildConstant.getChangeSetComment( map ) );
                changeSet.setDate( ContinuumBuildConstant.getChangeSetDate( map ) );
                setChangeFiles( changeSet, map );
                changes.add( changeSet );
            }
        }

        return changes;
    }

    private void setChangeFiles( ChangeSet changeSet, Map context )
    {
        List<Map> changeFiles = ContinuumBuildConstant.getChangeSetFiles( context );

        if ( changeFiles != null )
        {
            for ( Map map : changeFiles )
            {
                ChangeFile changeFile = new ChangeFile();
                changeFile.setName( ContinuumBuildConstant.getChangeFileName( map ) );
                changeFile.setRevision( ContinuumBuildConstant.getChangeFileRevision( map ) );
                changeFile.setStatus( ContinuumBuildConstant.getChangeFileStatus( map ) );

                changeSet.addFile( changeFile );
            }
        }
    }

    private ProjectDependency getProjectParent( Map context )
    {
        Map map = ContinuumBuildConstant.getProjectParent( context );

        if ( map != null && map.size() > 0 )
        {
            ProjectDependency parent = new ProjectDependency();
            parent.setGroupId( ContinuumBuildConstant.getGroupId( map ) );
            parent.setArtifactId( ContinuumBuildConstant.getArtifactId( map ) );
            parent.setVersion( ContinuumBuildConstant.getVersion( map ) );

            return parent;
        }

        return null;
    }

    private List<ProjectDependency> getProjectDependencies( Map context )
    {
        List<ProjectDependency> projectDependencies = new ArrayList<ProjectDependency>();

        List<Map> dependencies = ContinuumBuildConstant.getProjectDependencies( context );

        if ( dependencies != null )
        {
            for ( Map map : dependencies )
            {
                ProjectDependency dependency = new ProjectDependency();
                dependency.setGroupId( ContinuumBuildConstant.getGroupId( map ) );
                dependency.setArtifactId( ContinuumBuildConstant.getArtifactId( map ) );
                dependency.setVersion( ContinuumBuildConstant.getVersion( map ) );

                projectDependencies.add( dependency );
            }
        }
        return projectDependencies;
    }

    private List<ProjectDeveloper> getProjectDevelopers( Map context )
    {
        List<ProjectDeveloper> projectDevelopers = new ArrayList<ProjectDeveloper>();

        List<Map> developers = ContinuumBuildConstant.getProjectDevelopers( context );

        if ( developers != null )
        {
            for ( Map map : developers )
            {
                ProjectDeveloper developer = new ProjectDeveloper();
                developer.setName( ContinuumBuildConstant.getDeveloperName( map ) );
                developer.setEmail( ContinuumBuildConstant.getDeveloperEmail( map ) );
                developer.setScmId( ContinuumBuildConstant.getDeveloperScmId( map ) );

                projectDevelopers.add( developer );
            }
        }
        return projectDevelopers;
    }

    private List<ProjectNotifier> getProjectNotifiers( Map context )
    {
        List<ProjectNotifier> projectNotifiers = new ArrayList<ProjectNotifier>();

        List<Map> notifiers = ContinuumBuildConstant.getProjectNotifiers( context );

        if ( notifiers != null )
        {
            for ( Map map : notifiers )
            {
                ProjectNotifier notifier = new ProjectNotifier();
                notifier.setConfiguration( ContinuumBuildConstant.getNotifierConfiguration( map ) );
                notifier.setEnabled( ContinuumBuildConstant.isNotifierEnabled( map ) );
                notifier.setFrom( ContinuumBuildConstant.getNotifierFrom( map ) );
                notifier.setRecipientType( ContinuumBuildConstant.getNotifierRecipientType( map ) );
                notifier.setSendOnError( ContinuumBuildConstant.isNotifierSendOnError( map ) );
                notifier.setSendOnFailure( ContinuumBuildConstant.isNotifierSendOnFailure( map ) );
                notifier.setSendOnScmFailure( ContinuumBuildConstant.isNotifierSendOnScmFailure( map ) );
                notifier.setSendOnSuccess( ContinuumBuildConstant.isNotifierSendOnSuccess( map ) );
                notifier.setSendOnWarning( ContinuumBuildConstant.isNotifierSendOnWarning( map ) );
                notifier.setType( ContinuumBuildConstant.getNotifierType( map ) );

                projectNotifiers.add( notifier );
            }
        }
        return projectNotifiers;
    }

    private ScmResult getScmResult( Map context )
    {
        Map map = ContinuumBuildConstant.getScmResult( context );

        if ( !map.isEmpty() )
        {
            ScmResult scmResult = new ScmResult();
            scmResult.setCommandLine( ContinuumBuildConstant.getScmCommandLine( map ) );
            scmResult.setCommandOutput( ContinuumBuildConstant.getScmCommandOutput( map ) );
            scmResult.setException( ContinuumBuildConstant.getScmException( map ) );
            scmResult.setProviderMessage( ContinuumBuildConstant.getScmProviderMessage( map ) );
            scmResult.setSuccess( ContinuumBuildConstant.isScmSuccess( map ) );
            scmResult.setChanges( getScmChanges( map ) );

            return scmResult;
        }

        return null;
    }

    private String getMavenProjectVersion( Map context )
    {
        Map map = ContinuumBuildConstant.getMavenProject( context );

        if ( !map.isEmpty() )
        {
            return ContinuumBuildConstant.getVersion( map );
        }

        return null;
    }

    private List<String> getMavenProjectModules( Map context )
    {
        Map map = ContinuumBuildConstant.getMavenProject( context );

        if ( !map.isEmpty() )
        {
            return ContinuumBuildConstant.getProjectModules( map );
        }

        return null;
    }

    public Map<String, DistributedBuildTaskQueueExecutor> getTaskQueueExecutors()
    {
        return taskQueueExecutors;
    }
}
