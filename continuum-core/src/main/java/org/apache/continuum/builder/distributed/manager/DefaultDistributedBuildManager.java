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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.continuum.buildagent.NoBuildAgentException;
import org.apache.continuum.buildagent.NoBuildAgentInGroupException;
import org.apache.continuum.builder.distributed.executor.ThreadedDistributedBuildTaskQueueExecutor;
import org.apache.continuum.builder.distributed.util.DistributedBuildUtil;
import org.apache.continuum.builder.utils.ContinuumBuildConstant;
import org.apache.continuum.configuration.BuildAgentConfiguration;
import org.apache.continuum.configuration.BuildAgentGroupConfiguration;
import org.apache.continuum.dao.BuildDefinitionDao;
import org.apache.continuum.dao.BuildResultDao;
import org.apache.continuum.dao.ProjectDao;
import org.apache.continuum.dao.ProjectScmRootDao;
import org.apache.continuum.distributed.transport.slave.SlaveBuildAgentTransportClient;
import org.apache.continuum.distributed.transport.slave.SlaveBuildAgentTransportService;
import org.apache.continuum.model.project.ProjectRunSummary;
import org.apache.continuum.model.project.ProjectScmRoot;
import org.apache.continuum.taskqueue.BuildProjectTask;
import org.apache.continuum.taskqueue.OverallDistributedBuildQueue;
import org.apache.continuum.taskqueue.PrepareBuildProjectsTask;
import org.apache.continuum.utils.ContinuumUtils;
import org.apache.continuum.utils.ProjectSorter;
import org.apache.continuum.utils.build.BuildTrigger;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.system.Installation;
import org.apache.maven.continuum.model.system.Profile;
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
import org.codehaus.plexus.taskqueue.Task;
import org.codehaus.plexus.taskqueue.TaskQueueException;
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

    private Map<String, OverallDistributedBuildQueue> overallDistributedBuildQueues =
        Collections.synchronizedMap( new HashMap<String, OverallDistributedBuildQueue>() );

    private List<ProjectRunSummary> currentRuns = Collections.synchronizedList( new ArrayList<ProjectRunSummary>() );

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
    private BuildResultDao buildResultDao;

    /**
     * @plexus.requirement
     */
    private ProjectScmRootDao projectScmRootDao;

    /**
     * @plexus.requirement
     */
    private DistributedBuildUtil distributedBuildUtil;

    private PlexusContainer container;

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
        List<BuildAgentConfiguration> agents = configurationService.getBuildAgents();

        if ( agents != null )
        {
            synchronized( overallDistributedBuildQueues )
            {
                for ( BuildAgentConfiguration agent : agents )
                {
                    if ( agent.isEnabled() )
                    {
                        try
                        {
                            SlaveBuildAgentTransportService client = createSlaveBuildAgentTransportClientConnection( agent.getUrl() );

                            if ( client.ping() )
                            {
                                log.debug(
                                    "agent is enabled, create distributed build queue for build agent '{}'", agent.getUrl() );
                                createDistributedBuildQueueForAgent( agent.getUrl() );
                            }
                            else
                            {
                                log.debug( "unable to ping build agent '{}'", agent.getUrl() );
                            }
                        }
                        catch ( MalformedURLException e )
                        {
                            // do not throw exception, just log it
                            log.error( "Invalid build agent URL {}, not creating distributed build queue", agent.getUrl() );
                        }
                        catch ( ContinuumException e )
                        {
                            throw new InitializationException(
                                "Error while initializing distributed build queues", e );
                        }
                        catch ( Exception e )
                        {
                            agent.setEnabled( false );
                            log.debug( "unable to ping build agent '{}' : {}", agent.getUrl(),
                                ContinuumUtils.throwableToString( e ) );
                        }
                    }
                    else
                    {
                        log.debug( "agent {} is disabled, not creating distributed build queue", agent.getUrl() );
                    }
                }
            }
        }
    }

    public void reload()
        throws ContinuumException
    {
        List<BuildAgentConfiguration> agents = configurationService.getBuildAgents();

        if ( agents == null )
        {
            return;
        }

        synchronized( overallDistributedBuildQueues )
        {
            for ( BuildAgentConfiguration agent : agents )
            {
                if ( agent.isEnabled() && !overallDistributedBuildQueues.containsKey( agent.getUrl() ) )
                {
                    SlaveBuildAgentTransportService client = null;

                    try
                    {
                        client = createSlaveBuildAgentTransportClientConnection( agent.getUrl() );
                    }
                    catch ( MalformedURLException e )
                    {
                        log.error( "Invalid build agent URL {}, not creating distributed build queue", agent.getUrl() );
                        throw new ContinuumException( "Malformed build agent url " + agent.getUrl() );
                    }
                    catch ( Exception e )
                    {
                        agent.setEnabled( false );
                        configurationService.updateBuildAgent( agent );

                        log.error( "Error binding build agent {} service : {} ", agent.getUrl(), ContinuumUtils.throwableToString( e ) );
                        throw new ContinuumException( e.getMessage() );
                    }

                    boolean ping = false;

                    try
                    {
                        ping = client.ping();
                    }
                    catch ( Exception e )
                    {
                        agent.setEnabled( false );
                        log.error( "Unable to ping build agent '{}': {}", agent.getUrl(),
                                   ContinuumUtils.throwableToString( e ) );
                    }

                    if ( ping )
                    {
                        try
                        {
                            createDistributedBuildQueueForAgent( agent.getUrl() );
                            log.debug( "Agent is enabled, create distributed build queue for build agent '{}'", agent.getUrl() );
                        }
                        catch ( Exception e )
                        {
                            agent.setEnabled( false );
                            log.error( "Unable to create distributed queue for build agent {} : {}", agent.getUrl(), ContinuumUtils.throwableToString( e ) );
                        }
                    }
                    else
                    {
                        agent.setEnabled( false );
                        log.error( "Unable to ping build agent '{}'", agent.getUrl() );
                    }

                    configurationService.updateBuildAgent( agent );
                }
                else if ( !agent.isEnabled() && overallDistributedBuildQueues.containsKey( agent.getUrl() ) )
                {
                    log.debug( "agent is disabled, remove distributed build queue for build agent '{}'", agent.getUrl() );
                    removeDistributedBuildQueueOfAgent( agent.getUrl() );
                }
            }
        }
    }

    public void update( BuildAgentConfiguration agent )
        throws ContinuumException
    {
        synchronized( overallDistributedBuildQueues )
        {
            if ( agent.isEnabled() && !overallDistributedBuildQueues.containsKey( agent.getUrl() ) )
            {
                SlaveBuildAgentTransportService client = null;

                try
                {
                    client = createSlaveBuildAgentTransportClientConnection( agent.getUrl() );
                }
                catch ( MalformedURLException e )
                {
                    configurationService.removeBuildAgent( agent );
                    log.error( "Invalid build agent URL {}, not creating distributed build queue", agent.getUrl() );
                    throw new ContinuumException( "Malformed build agent url " + agent.getUrl() );
                }
                catch ( Exception e )
                {
                    configurationService.removeBuildAgent( agent );
                    log.error( "Error binding build agent {} service : {} ", agent.getUrl(), ContinuumUtils.throwableToString( e ) );
                    throw new ContinuumException( e.getMessage() );
                }

                boolean ping = false;

                try
                {
                    ping = client.ping();
                }
                catch ( Exception e )
                {
                    configurationService.removeBuildAgent( agent );
                    log.error( "Unable to ping build agent '{}': {}", agent.getUrl(),
                               ContinuumUtils.throwableToString( e ) );
                    throw new ContinuumException( "Unable to ping build agent " + agent.getUrl() );
                }

                if ( ping )
                {
                    try
                    {
                        createDistributedBuildQueueForAgent( agent.getUrl() );
                        log.debug( "Agent is enabled, create distributed build queue for build agent '{}'", agent.getUrl() );
                    }
                    catch ( Exception e )
                    {
                        configurationService.removeBuildAgent( agent );
                        log.error( "Unable to create distributed queue for build agent {} : {}", agent.getUrl(), ContinuumUtils.throwableToString( e ) );
                    }
                }
                else
                {
                    configurationService.removeBuildAgent( agent );
                    log.error( "Unable to ping build agent '{}'", agent.getUrl() );
                    throw new ContinuumException( "Unable to ping build agent " + agent.getUrl() );
                }
            }
            else if ( !agent.isEnabled() && overallDistributedBuildQueues.containsKey( agent.getUrl() ) )
            {
                log.debug( "agent is disabled, remove distributed build queue for build agent '{}'", agent.getUrl() );
                removeDistributedBuildQueueOfAgent( agent.getUrl() );
            }
        }
    }

    @SuppressWarnings( "unused" )
    public void prepareBuildProjects( Map<Integer, Integer>projectsBuildDefinitionsMap, BuildTrigger buildTrigger, int projectGroupId, 
                                      String projectGroupName, String scmRootAddress, int scmRootId, List<ProjectScmRoot> scmRoots )
        throws ContinuumException, NoBuildAgentException, NoBuildAgentInGroupException
    {
    	PrepareBuildProjectsTask task = new PrepareBuildProjectsTask( projectsBuildDefinitionsMap, buildTrigger,
                                                                      projectGroupId, projectGroupName, 
                                                                      scmRootAddress, scmRootId );

    	if ( buildTrigger.getTrigger() == ContinuumProjectState.TRIGGER_FORCED )
    	{
    	    log.debug( "Build project (projectGroupId={}) triggered manually by {}", projectGroupId, buildTrigger.getTriggeredBy() );
    	}
    	else
    	{
    	    log.debug( "Build project (projectGroupId={}) triggered by schedule {}", projectGroupId, buildTrigger.getTriggeredBy() );
    	}

    	if ( log.isDebugEnabled() )
    	{
    	    Map<String, BuildProjectTask> buildTasks = getProjectsCurrentlyBuilding();

    	    for ( String key : buildTasks.keySet() )
    	    {
    	        log.debug( "Current build of agent {} :: Project {}", key, buildTasks.get( key ).getProjectName() );
    	    }

    	    Map<String, List<BuildProjectTask>> buildQueues = getProjectsInBuildQueue();

    	    for ( String key : buildQueues.keySet() )
    	    {
    	        for ( BuildProjectTask buildTask : buildQueues.get( key ) )
    	        {
    	            log.debug( "Build Queue of agent {} :: Project {}", key, buildTask.getProjectName() );
    	        }
    	    }

    	    Map<String, PrepareBuildProjectsTask> prepareBuildTasks = getProjectsCurrentlyPreparingBuild();

    	    for( String key : prepareBuildTasks.keySet() )
    	    {
    	        PrepareBuildProjectsTask prepareBuildTask = prepareBuildTasks.get( key );
    	        log.debug( "Current prepare build of agent {} :: Project Group {} - Scm Root {}", 
    	                   new Object[] { key, prepareBuildTask.getProjectGroupName(), prepareBuildTask.getProjectScmRootId() } );
    	    }

    	    Map<String, List<PrepareBuildProjectsTask>> prepareBuildQueues = getProjectsInPrepareBuildQueue();

    	    for ( String key : prepareBuildQueues.keySet() )
    	    {
    	        for ( PrepareBuildProjectsTask prepareBuildTask : prepareBuildQueues.get( key ) )
    	        {
    	            log.debug( "Prepare Build Queue of agent {} : Project Group {} - Scm Root {}", 
    	                       new Object[] { key, prepareBuildTask.getProjectGroupName(), prepareBuildTask.getProjectScmRootId() } );
    	        }
    	    }
    	}

    	log.debug( "Determining which build agent should build the project..." );

    	OverallDistributedBuildQueue overallDistributedBuildQueue = getOverallDistributedBuildQueueByGroup( projectGroupId, scmRoots, scmRootId );

        if ( overallDistributedBuildQueue == null )
        {
            log.debug( "No projects with the same continuum group is currently building, checking if build definition has an attached build agent group" );

            if ( hasBuildagentGroup( projectsBuildDefinitionsMap ) )
            {
                log.debug( "Build definition used has an attached build agent group, checking if there are configured build agents in the group" );

                if ( !hasBuildagentInGroup( projectsBuildDefinitionsMap ) )
                {
                    log.warn( "No build agent configured in build agent group. Not building projects." );
    
                    throw new NoBuildAgentInGroupException( "No build agent configured in build agent group" );
                }
                else
                {
                    // get overall distributed build queue from build agent group
                    log.info( "Getting the least busy build agent within the build agent group" );
                    overallDistributedBuildQueue = getOverallDistributedBuildQueueByAgentGroup( projectsBuildDefinitionsMap );
                }
            }
            else
            {
                // project does not have build agent group
                log.info( "Project does not have a build agent group, getting the least busy of all build agents" );
                overallDistributedBuildQueue = getOverallDistributedBuildQueue();
            }
        }

        if ( overallDistributedBuildQueue != null )
        {
            try
            {
                String agentUrl = overallDistributedBuildQueue.getBuildAgentUrl();
                log.info( "Building project in the least busy agent {}", agentUrl );
                overallDistributedBuildQueue.addToDistributedBuildQueue( task );
                createProjectRunSummaries( task, agentUrl );
            }
            catch ( TaskQueueException e )
            {
                log.error( "Error while enqueuing prepare build task", e );
                throw new ContinuumException( "Error occurred while enqueuing prepare build task", e );
            }
        }
        else
        {
            log.warn( "Unable to determine which build agent should build the project. No build agent configured. Not building projects." );

            throw new NoBuildAgentException( "No build agent configured" );
        }

        // call in case we disabled a build agent
        reload();
    }

    public void removeDistributedBuildQueueOfAgent( String buildAgentUrl )
        throws ContinuumException
    {
        if ( overallDistributedBuildQueues.containsKey( buildAgentUrl ) )
        {
            List<PrepareBuildProjectsTask> tasks = null;

            synchronized( overallDistributedBuildQueues )
            {
                OverallDistributedBuildQueue overallDistributedBuildQueue = overallDistributedBuildQueues.get( buildAgentUrl );

                try
                {
                    if ( overallDistributedBuildQueue.getDistributedBuildTaskQueueExecutor().getCurrentTask() != null )
                    {
                        log.error( "Unable to remove build agent because it is currently being used" );
                        throw new ContinuumException( "Unable to remove build agent because it is currently being used" );
                    }

                    tasks = overallDistributedBuildQueue.getProjectsInQueue();

                    overallDistributedBuildQueue.getDistributedBuildQueue().removeAll( tasks );

                    ( (ThreadedDistributedBuildTaskQueueExecutor) overallDistributedBuildQueue.getDistributedBuildTaskQueueExecutor() ).stop();

                    container.release( overallDistributedBuildQueue );

                    overallDistributedBuildQueues.remove( buildAgentUrl );

                    log.debug( "remove distributed build queue for build agent '{}'", buildAgentUrl );
                }
                catch ( TaskQueueException e )
                {
                    log.error( "Error occurred while removing build agent {}", buildAgentUrl, e );
                    throw new ContinuumException( "Error occurred while removing build agent " + buildAgentUrl, e );
                }
                catch ( ComponentLifecycleException e )
                {
                    log.error( "Error occurred while removing build agent {}", buildAgentUrl, e );
                    throw new ContinuumException( "Error occurred while removing build agent " + buildAgentUrl, e );
                }
                catch ( StoppingException e )
                {
                    log.error( "Error occurred while removing build agent {}", buildAgentUrl, e );
                    throw new ContinuumException( "Error occurred while removing build agent " + buildAgentUrl, e );
                }
            }
        }
    }

    public Map<String, List<PrepareBuildProjectsTask>> getProjectsInPrepareBuildQueue()
        throws ContinuumException
    {
        Map<String, List<PrepareBuildProjectsTask>> map = new HashMap<String, List<PrepareBuildProjectsTask>>();

        synchronized( overallDistributedBuildQueues )
        {
            for ( String buildAgentUrl : overallDistributedBuildQueues.keySet() )
            {
                List<PrepareBuildProjectsTask> tasks = new ArrayList<PrepareBuildProjectsTask>();

                try
                {
                    if ( isAgentAvailable( buildAgentUrl ) )
                    {
                        log.debug( "Getting projects in prepare build queue of build agent {}", buildAgentUrl );

                        SlaveBuildAgentTransportService client = createSlaveBuildAgentTransportClientConnection( buildAgentUrl );

                        List<Map<String, Object>> projects = client.getProjectsInPrepareBuildQueue();
    
                        for ( Map<String, Object> context : projects )
                        {
                            tasks.add( getPrepareBuildProjectsTask( context ) );
                        }
    
                        map.put( buildAgentUrl, tasks );
                    }
                    else
                    {
                        log.debug( "Unable to get projects in prepare build queue. Build agent {} not available", buildAgentUrl );
                    }
                }
                catch ( MalformedURLException e )
                {
                    throw new ContinuumException( "Invalid build agent url: " + buildAgentUrl ); 
                }
                catch ( Exception e )
                {
                    throw new ContinuumException( "Error while retrieving projects in prepare build queue", e );
                }
            }
        }

        // call reload in case we disable a build agent
        reload();

        return map;
    }

    public Map<String, PrepareBuildProjectsTask> getProjectsCurrentlyPreparingBuild()
        throws ContinuumException
    {
        Map<String, PrepareBuildProjectsTask> map = new HashMap<String, PrepareBuildProjectsTask>();

        synchronized( overallDistributedBuildQueues )
        {
            for ( String buildAgentUrl : overallDistributedBuildQueues.keySet() )
            {
                try
                {
                    if ( isAgentAvailable( buildAgentUrl ) )
                    {
                        log.debug( "Getting project currently preparing build in build agent {}", buildAgentUrl );

                        SlaveBuildAgentTransportService client = createSlaveBuildAgentTransportClientConnection( buildAgentUrl );
                        Map<String, Object> project = client.getProjectCurrentlyPreparingBuild();
    
                        if ( !project.isEmpty() )
                        {
                            map.put( buildAgentUrl, getPrepareBuildProjectsTask( project ) );
                        }
                    }
                    else
                    {
                        log.debug( "Unable to get projects currently preparing build. Build agent {} is not available", buildAgentUrl );
                    }
                }
                catch ( MalformedURLException e )
                {
                    throw new ContinuumException( "Invalid build agent url: " + buildAgentUrl );
                }
                catch ( Exception e )
                {
                    throw new ContinuumException( "Error retrieving projects currently preparing build in " + buildAgentUrl, e );
                }
            }
        }

        // call reload in case we disable a build agent
        reload();

        return map;
    }
 
    public Map<String, BuildProjectTask> getProjectsCurrentlyBuilding()
        throws ContinuumException
    {
        Map<String, BuildProjectTask> map = new HashMap<String, BuildProjectTask>();

        synchronized( overallDistributedBuildQueues )
        {
            for ( String buildAgentUrl : overallDistributedBuildQueues.keySet() )
            {
                try
                {
                    if ( isAgentAvailable( buildAgentUrl ) )
                    {
                        log.debug( "Getting projects currently building in build agent {}", buildAgentUrl );

                        SlaveBuildAgentTransportService client = createSlaveBuildAgentTransportClientConnection( buildAgentUrl );
                        Map<String, Object> project = client.getProjectCurrentlyBuilding();
    
                        if ( !project.isEmpty() )
                        {
                            map.put( buildAgentUrl, getBuildProjectTask( project ) );
                        }
                    }
                    else
                    {
                        log.debug( "Unable to get projects currently building. Build agent {} is not available", buildAgentUrl );
                    }
                }
                catch ( MalformedURLException e )
                {
                    throw new ContinuumException( "Invalid build agent url: " + buildAgentUrl );
                }
                catch ( Exception e )
                {
                    throw new ContinuumException( "Error retrieving projects currently building in " + buildAgentUrl, e );
                }
            }
        }

        // call reload in case we disable a build agent
        reload();

        return map;
    }

    public Map<String, List<BuildProjectTask>> getProjectsInBuildQueue()
        throws ContinuumException
    {
        Map<String, List<BuildProjectTask>> map = new HashMap<String, List<BuildProjectTask>>();

        synchronized( overallDistributedBuildQueues )
        {
            for ( String buildAgentUrl : overallDistributedBuildQueues.keySet() )
            {
                List<BuildProjectTask> tasks = new ArrayList<BuildProjectTask>();

                try
                {
                    if ( isAgentAvailable( buildAgentUrl ) )
                    {
                        log.debug( "Getting projects in build queue in build agent {}", buildAgentUrl );

                        SlaveBuildAgentTransportService client = createSlaveBuildAgentTransportClientConnection( buildAgentUrl );
                        List<Map<String, Object>> projects = client.getProjectsInBuildQueue();
    
                        for ( Map<String, Object> context : projects )
                        {
                            tasks.add( getBuildProjectTask( context ) );
                        }
    
                        map.put( buildAgentUrl, tasks );
                    }
                    else
                    {
                        log.debug( "Unable to get projects in build queue. Build agent {} is not available", buildAgentUrl );
                    }
                }
                catch ( MalformedURLException e )
                {
                    throw new ContinuumException( "Invalid build agent url: " + buildAgentUrl ); 
                }
                catch ( Exception e )
                {
                    throw new ContinuumException( "Error while retrieving projects in build queue", e );
                }
            }
        }

        // call reload in case we disable a build agent
        reload();

        return map;
    }

    public boolean isBuildAgentBusy( String buildAgentUrl )
    {
        synchronized ( overallDistributedBuildQueues )
        {
            OverallDistributedBuildQueue overallDistributedBuildQueue = overallDistributedBuildQueues.get( buildAgentUrl );

            if ( overallDistributedBuildQueue != null && 
                 overallDistributedBuildQueue.getDistributedBuildTaskQueueExecutor().getCurrentTask() != null )
            {
                log.debug( "build agent '" + buildAgentUrl + "' is busy" );
                return true;
            }

            log.debug( "build agent '" + buildAgentUrl + "' is not busy" );
            return false;
        }
    }

    public void cancelDistributedBuild( String buildAgentUrl )
        throws ContinuumException
    {
        try
        {
            if ( isAgentAvailable( buildAgentUrl ) )
            {
                log.debug( "Cancelling build in build agent {}", buildAgentUrl );

                SlaveBuildAgentTransportService client = createSlaveBuildAgentTransportClientConnection( buildAgentUrl );
    
                client.cancelBuild();
            }
            else
            {
                log.debug( "Unable to cancel build, build agent {} is not available", buildAgentUrl );
            }

            // call reload in case we disable the build agent
            reload();
        }
        catch ( MalformedURLException e )
        {
            log.error( "Error cancelling build in build agent: Invalid build agent url " + buildAgentUrl );
            throw new ContinuumException( "Error cancelling build in build agent: Invalid build agent url " + buildAgentUrl );
        }
        catch ( Exception e )
        {
            log.error( "Error occurred while cancelling build in build agent " + buildAgentUrl, e );
            throw new ContinuumException( "Error occurred while cancelling build in build agent " + buildAgentUrl, e );
        }
    }

    public void cancelGroupBuild( int projectGroupId )
        throws ContinuumException
    {
        log.debug( "Cancelling all builds of project group {}", projectGroupId );

        List<ProjectRunSummary> runsToDelete = new ArrayList<ProjectRunSummary>();

        synchronized( currentRuns )
        {
            for ( ProjectRunSummary run : currentRuns )
            {
                if ( run.getProjectGroupId() == projectGroupId )
                {
                    cancelCurrentRun( run, runsToDelete );
                }
            }

            if ( runsToDelete.size() > 0 )
            {
                currentRuns.removeAll( runsToDelete );
            }
        }
    }

    public void cancelBuild( int projectId )
        throws ContinuumException
    {
        log.debug( "Cancelling all builds of project {}", projectId );

        List<ProjectRunSummary> runsToDelete = new ArrayList<ProjectRunSummary>();

        synchronized( currentRuns )
        {
            for ( ProjectRunSummary run : currentRuns )
            {
                if ( run.getProjectId() == projectId )
                {
                    cancelCurrentRun( run, runsToDelete );
                }
            }

            if ( runsToDelete.size() > 0 )
            {
                currentRuns.removeAll( runsToDelete );
            }
        }
    }

    private void cancelCurrentRun( ProjectRunSummary run, List<ProjectRunSummary> runsToDelete )
        throws ContinuumException
    {
        int projectId = run.getProjectId();
        int buildDefinitionId = run.getBuildDefinitionId();
        String buildAgentUrl = run.getBuildAgentUrl();

        // try to remove from any queue first
        removeFromPrepareBuildQueue( buildAgentUrl, run.getProjectGroupId(), run.getProjectScmRootId() );
        removeFromBuildQueue( buildAgentUrl, projectId, buildDefinitionId );

        if ( isProjectCurrentlyPreparingBuild( projectId, buildDefinitionId ) )
        {
            log.debug( "Unable to cancel build of projectId={}, buildDefinitionId={} in build agent{}. Project is currently doing scm update." );
            return;
        }
        else if ( isProjectCurrentlyBuilding( projectId, buildDefinitionId ) )
        {
            log.debug( "Cancel build of projectId={}, buildDefinitionId={} in build agent {}", 
                       new Object[] { projectId, buildDefinitionId, buildAgentUrl } );
            cancelDistributedBuild( buildAgentUrl );
            runsToDelete.add( run );
        }
        else
        {
            try
            {
                ProjectScmRoot scmRoot = projectScmRootDao.getProjectScmRoot( run.getProjectScmRootId() );

                if ( scmRoot != null && scmRoot.getState() == ContinuumProjectState.UPDATING )
                {
                    // no longer updating, but state was not updated.
                    scmRoot.setState( ContinuumProjectState.ERROR );
                    scmRoot.setError( "Problem encountered while returning scm update result to master by build agent '" + buildAgentUrl + "'. \n" +
                                      "Make sure build agent is configured properly. Check the logs for more information." );
                    projectScmRootDao.updateProjectScmRoot( scmRoot );

                    log.debug( "projectId={}, buildDefinitionId={} is not updating anymore. Problem encountered while return scm update result by build agent {}. Stopping the build.",
                               new Object[] { projectId, buildDefinitionId, buildAgentUrl } );
                    runsToDelete.add( run );
                }
                else if ( scmRoot != null && scmRoot.getState() == ContinuumProjectState.ERROR )
                {
                    log.debug( "projectId={}, buildDefinitionId={} is not updating anymore. Problem encountered while return scm update result by build agent {}. Stopping the build.",
                               new Object[] { projectId, buildDefinitionId, buildAgentUrl } );
                    runsToDelete.add( run );
                }
                else
                {
                    Project project = projectDao.getProject( projectId );
                    BuildDefinition buildDefinition = buildDefinitionDao.getBuildDefinition( buildDefinitionId );
    
                    // no longer building, but state was not updated
                    BuildResult buildResult = new BuildResult();
                    buildResult.setBuildDefinition( buildDefinition );
                    buildResult.setBuildUrl( run.getBuildAgentUrl() );
                    buildResult.setTrigger( run.getTrigger() );
                    buildResult.setUsername( run.getTriggeredBy() );
                    buildResult.setState( ContinuumProjectState.ERROR );
                    buildResult.setSuccess( false );
                    buildResult.setStartTime( new Date().getTime() );
                    buildResult.setEndTime( new Date().getTime() );
                    buildResult.setExitCode( 1 );
                    buildResult.setError( "Problem encountered while returning build result to master by build agent '" + buildAgentUrl + "'. \n" +
                                          "Make sure build agent is configured properly. Check the logs for more information." );
                    buildResultDao.addBuildResult( project, buildResult );
    
                    project.setState( ContinuumProjectState.ERROR );
                    project.setLatestBuildId( buildResult.getId() );
                    projectDao.updateProject( project );
    
                    log.debug( "projectId={}, buildDefinitionId={} is not building anymore. Problem encountered while return build result by build agent {}. Stopping the build.",
                               new Object[] { projectId, buildDefinitionId, buildAgentUrl } );
    
                    // create a build result
                    runsToDelete.add( run );
                }
            }
            catch ( Exception e )
            {
                log.error( "Unable to end build for projectId={}, buildDefinitionId={} : {}",
                           new Object[] { projectId, buildDefinitionId, e.getMessage() } );
            }
        }
    }

    public Map<String, Object> getBuildResult( int projectId )
        throws ContinuumException
    {
        Map<String, Object> map = new HashMap<String, Object>();

        String buildAgentUrl = getBuildAgent( projectId );

        if ( buildAgentUrl == null )
        {
            log.debug( "Unable to determine the build agent where project is building" );
            return null;
        }

        try
        {
            if ( isAgentAvailable( buildAgentUrl ) )
            {
                log.debug( "Getting build result of project in build agent {}", buildAgentUrl );
                SlaveBuildAgentTransportService client = createSlaveBuildAgentTransportClientConnection( buildAgentUrl );

                Map<String, Object> result = client.getBuildResult( projectId );

                if ( result != null )
                {
                    int buildDefinitionId = ContinuumBuildConstant.getBuildDefinitionId( result );

                    Project project = projectDao.getProjectWithAllDetails( projectId );
                    BuildDefinition buildDefinition = buildDefinitionDao.getBuildDefinition( buildDefinitionId );

                    BuildResult oldBuildResult =
                        buildResultDao.getLatestBuildResultForBuildDefinition( projectId, buildDefinitionId );

                    BuildResult buildResult = distributedBuildUtil.convertMapToBuildResult( result );
                    buildResult.setBuildDefinition( buildDefinition );
                    buildResult.setBuildNumber( project.getBuildNumber() + 1 );
                    buildResult.setModifiedDependencies( distributedBuildUtil.getModifiedDependencies( oldBuildResult, result ) );
                    buildResult.setScmResult( distributedBuildUtil.getScmResult( result ) );

                    String buildOutput = ContinuumBuildConstant.getBuildOutput( result );

                    map.put( ContinuumBuildConstant.KEY_BUILD_RESULT, buildResult );
                    map.put( ContinuumBuildConstant.KEY_BUILD_OUTPUT, buildOutput );
                }
                else
                {
                    log.debug( "No build result returned by build agent {}", buildAgentUrl );
                }
            }
            else
            {
                log.debug( "Unable to get build result of project. Build agent {} is not available", buildAgentUrl );
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

        // call reload in case we disable the build agent
        reload();

        return map;
    }

    public String getBuildAgentPlatform( String buildAgentUrl )
        throws ContinuumException
    {
        try
        {
            String platform = "";
            if ( isAgentAvailable( buildAgentUrl ) )
            {
                log.debug( "Getting build agent {} platform", buildAgentUrl );

                SlaveBuildAgentTransportService client = createSlaveBuildAgentTransportClientConnection( buildAgentUrl );
                platform = client.getBuildAgentPlatform();
            }
            else
            {
                log.debug( "Unable to get build agent platform. Build agent {} is not available", buildAgentUrl );
            }
            // call reload in case we disable the build agent
            reload();
            return platform;
        }
        catch ( Exception e )
        {
            throw new ContinuumException( "Unable to get platform of build agent", e );
        }
    }

    
    public List<Installation> getAvailableInstallations( String buildAgentUrl )
        throws ContinuumException
    {
        List<Installation> installations = new ArrayList<Installation>();

        try
        {
            if ( isAgentAvailable( buildAgentUrl ) )
            {
                log.debug( "Getting available installations in build agent {}", buildAgentUrl );

                SlaveBuildAgentTransportService client = createSlaveBuildAgentTransportClientConnection( buildAgentUrl );

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
            }
            else
            {
                log.debug( "Unable to get available installations. Build agent {} is not available", buildAgentUrl );
            }

            // call reload in case we disable the build agent
            reload();

            return installations;
        }
        catch ( Exception e )
        {
            throw new ContinuumException( "Unable to get available installations of build agent", e );
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
                log.debug( "Unable to determine the build agent where project last built" );

                return "";
            }

            try
            {
                if ( directory == null )
                {
                    directory = "";
                }

                if ( isAgentAvailable( buildAgentUrl ) )
                {
                    log.debug( "Generating working copy content of project in build agent {}", buildAgentUrl );

                    SlaveBuildAgentTransportService client = createSlaveBuildAgentTransportClientConnection( buildAgentUrl );
                    return client.generateWorkingCopyContent( projectId, directory, baseUrl, imageBaseUrl );
                }
                else
                {
                    log.debug( "Unable to generate working copy content of project. Build agent {} is not available", buildAgentUrl );
                }
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
        else
        {
            log.debug( "Unable to generate working copy content. Project hasn't been built yet." );
        }

        // call reload in case we disable the build agent
        reload();

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
                log.debug( "Unable to determine build agent where project last built" );
                return "";
            }

            try
            {
                if ( isAgentAvailable( buildAgentUrl ) )
                {
                    SlaveBuildAgentTransportService client = createSlaveBuildAgentTransportClientConnection( buildAgentUrl );
                    return client.getProjectFileContent( projectId, directory, filename );
                }
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
        else
        {
            log.debug( "Unable to get file content because project hasn't been built yet" );
        }

        // call reload in case we disable the build agent
        reload();

        return "";
    }

    public void removeFromPrepareBuildQueue( String buildAgentUrl, int projectGroupId, int scmRootId )
        throws ContinuumException
    {
        try
        {
            if ( isAgentAvailable( buildAgentUrl ) )
            {
                log.info( "Removing projectGroupId {} from prepare build queue of build agent {}", projectGroupId, buildAgentUrl );

                SlaveBuildAgentTransportService client = createSlaveBuildAgentTransportClientConnection( buildAgentUrl );
                client.removeFromPrepareBuildQueue( projectGroupId, scmRootId );
            }
            else
            {
                log.debug( "Unable to remove projectGroupId {} from prepare build queue. Build agent {} is not available", 
                           projectGroupId, buildAgentUrl );
            }
        }
        catch ( MalformedURLException e )
        {
            log.error( "Unable to remove projectGroupId=" + projectGroupId + " scmRootId=" + scmRootId + 
                       " from prepare build queue: Invalid build agent url " + buildAgentUrl );
            throw new ContinuumException( "Unable to remove projectGroupId=" + projectGroupId + " scmRootId=" + scmRootId + 
                                          " from prepare build queue: Invalid build agent url " + buildAgentUrl );
        }
        catch ( Exception e )
        {
            log.error( "Error occurred while removing projectGroupId=" + projectGroupId + " scmRootId=" + scmRootId + 
                       " from prepare build queue of agent " + buildAgentUrl, e );
            throw new ContinuumException( "Error occurred while removing projectGroupId=" + projectGroupId + " scmRootId=" +
                                          scmRootId + " from prepare build queue of agent " + buildAgentUrl, e );
        }

        // call reload in case we disable the build agent
        reload();
    }

    public void removeFromBuildQueue( String buildAgentUrl, int projectId, int buildDefinitionId )
        throws ContinuumException
    {
        try
        {
            if ( isAgentAvailable( buildAgentUrl ) )
            {
                log.info( "Removing projectId {} from build queue of build agent {}", projectId, buildAgentUrl );
                SlaveBuildAgentTransportService client = createSlaveBuildAgentTransportClientConnection( buildAgentUrl );
                client.removeFromBuildQueue( projectId, buildDefinitionId );
            }
            else
            {
                log.debug( "Unable to remove projectId {} from build queue. Build agent {} is not available", projectId, buildAgentUrl );
            }
        }
        catch ( MalformedURLException e )
        {
            log.error( "Unable to remove project " + projectId + 
                       " from build queue: Invalid build agent url " + buildAgentUrl );
            throw new ContinuumException( "Unable to remove project " + projectId + 
                                          " from build queue: Invalid build agent url " + buildAgentUrl );
        }
        catch ( Exception e )
        {
            log.error( "Error occurred while removing project " + projectId +
                       " from build queue of agent " + buildAgentUrl, e );
            throw new ContinuumException( "Error occurred while removing project " + projectId + 
                                          " from build queue of agent " + buildAgentUrl, e );
        }

        // call reload in case we disable the build agent
        reload();
    }

    public void removeFromPrepareBuildQueue( List<String> hashCodes )
        throws ContinuumException
    {
        synchronized ( overallDistributedBuildQueues )
        {
            for ( String buildAgentUrl : overallDistributedBuildQueues.keySet() )
            {
                try
                {
                    if ( isAgentAvailable( buildAgentUrl ) )
                    {
                        log.info( "Removing project groups from prepare build queue of build agent {}", buildAgentUrl );

                        SlaveBuildAgentTransportService client = createSlaveBuildAgentTransportClientConnection( buildAgentUrl );
                        client.removeFromPrepareBuildQueue( hashCodes );
                    }
                    else
                    {
                        log.debug( "Unable to remove project groups from prepare build queue. Build agent {} is not available", buildAgentUrl );
                    }
                }
                catch ( MalformedURLException e )
                {
                    log.error( "Error trying to remove projects from prepare build queue. Invalid build agent url: " + buildAgentUrl );
                }
                catch ( Exception e )
                {
                    log.error( "Error trying to remove projects from prepare build queue of agent " + buildAgentUrl, e );
                }
            }
        }

        // call reload in case we disable a build agent
        reload();
    }

    public void removeFromBuildQueue( List<String> hashCodes )
        throws ContinuumException
    {
        synchronized ( overallDistributedBuildQueues )
        {
            for ( String buildAgentUrl : overallDistributedBuildQueues.keySet() )
            {
                try
                {
                    if ( isAgentAvailable( buildAgentUrl ) )
                    {
                        log.info( "Removing projects from build queue of build agent {}", buildAgentUrl );

                        SlaveBuildAgentTransportService client = createSlaveBuildAgentTransportClientConnection( buildAgentUrl );
                        client.removeFromBuildQueue( hashCodes );
                    }
                    else
                    {
                        log.debug( "Unable to remove projects from build queue. Build agent {} is not available", buildAgentUrl );
                    }
                }
                catch ( MalformedURLException e )
                {
                    log.error( "Error trying to remove projects from build queue. Invalid build agent url: " + buildAgentUrl );
                }
                catch ( Exception e )
                {
                    log.error( "Error trying to remove projects from build queue of agent " + buildAgentUrl, e );
                }
            }
        }

        // call reload in case we disable a build agent
        reload();
    }

    public boolean isProjectInAnyPrepareBuildQueue( int projectId, int buildDefinitionId )
        throws ContinuumException
    {
        boolean found = false;

        synchronized( overallDistributedBuildQueues )
        {
            for ( String buildAgentUrl : overallDistributedBuildQueues.keySet() )
            {
                try
                {
                    if ( isAgentAvailable( buildAgentUrl ) )
                    {
                        log.debug( "Checking if project {} is in prepare build queue of build agent {}", projectId, buildAgentUrl );

                        SlaveBuildAgentTransportService client = createSlaveBuildAgentTransportClientConnection( buildAgentUrl );

                        List<Map<String, Object>> projects = client.getProjectsAndBuildDefinitionsInPrepareBuildQueue();
    
                        for ( Map<String, Object> context : projects )
                        {
                            int pid = ContinuumBuildConstant.getProjectId( context );
                            int buildId = ContinuumBuildConstant.getBuildDefinitionId( context );

                            if ( pid == projectId && ( buildId == buildDefinitionId || buildDefinitionId == -1 ) )
                            {
                                found = true;
                                break;
                            }

                        }
                    }
                    else
                    {
                        log.debug( "Unable to check if project {} is in prepare build queue. Build agent {} is not available",
                                   projectId, buildAgentUrl );
                    }

                    if ( found )
                    {
                        break;
                    }
                }
                catch ( MalformedURLException e )
                {
                    throw new ContinuumException( "Invalid build agent url: " + buildAgentUrl ); 
                }
                catch ( Exception e )
                {
                    throw new ContinuumException( "Error while retrieving projects in prepare build queue", e );
                }
            }
        }

        // call reload in case we disable a build agent
        reload();

        if ( found )
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean isProjectInAnyBuildQueue( int projectId, int buildDefinitionId )
        throws ContinuumException
    {
        Map<String, List<BuildProjectTask>> map = getProjectsInBuildQueue();

        for ( String url : map.keySet() )
        {
            for ( BuildProjectTask task : map.get( url ) )
            {
                if ( task.getProjectId() == projectId && 
                   ( buildDefinitionId == -1 || task.getBuildDefinitionId() == buildDefinitionId ) )
                {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isProjectCurrentlyPreparingBuild( int projectId, int buildDefinitionId )
        throws ContinuumException
    {
        boolean found = false;

        synchronized( overallDistributedBuildQueues )
        {
            for ( String buildAgentUrl : overallDistributedBuildQueues.keySet() )
            {
                try
                {
                    if ( isAgentAvailable( buildAgentUrl ) )
                    {
                        log.debug( "Checking if project {} is currently preparing build in build agent {}", projectId, buildAgentUrl );

                        SlaveBuildAgentTransportService client = createSlaveBuildAgentTransportClientConnection( buildAgentUrl );
                        List<Map<String, Object>> projects = client.getProjectsAndBuildDefinitionsCurrentlyPreparingBuild();
    
                        for ( Map<String, Object> context : projects )
                        {
                            int pid = ContinuumBuildConstant.getProjectId( context );
                            int buildId = ContinuumBuildConstant.getBuildDefinitionId( context );
    
                            if ( pid == projectId && ( buildDefinitionId == -1 || buildId == buildDefinitionId ) )
                            {
                                found = true;
                                break;
                            }
                        }
                    }
                    else
                    {
                        log.debug( "Unable to check if project {} is currently preparing build. Build agent {} is not available",
                                   projectId, buildAgentUrl );
                    }

                    if ( found )
                    {
                        break;
                    }
                }
                catch ( MalformedURLException e )
                {
                    throw new ContinuumException( "Invalid build agent url: " + buildAgentUrl );
                }
                catch ( Exception e )
                {
                    throw new ContinuumException( "Error retrieving projects currently preparing build in " + buildAgentUrl, e );
                }
            }
        }

        // call reload in case we disable a build agent
        reload();

        if ( found )
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean isProjectCurrentlyBuilding( int projectId, int buildDefinitionId )
        throws ContinuumException
    {
        Map<String, BuildProjectTask> map = getProjectsCurrentlyBuilding();

        for ( String url : map.keySet() )
        {
            BuildProjectTask task = map.get( url );

            if ( task.getProjectId() == projectId && 
               ( buildDefinitionId == -1 || task.getBuildDefinitionId() == buildDefinitionId ) )
            {
                return true;
            }
        }

        return false;
    }

    private String getBuildAgent( int projectId )
        throws ContinuumException
    {
        String agentUrl = null;

        synchronized( overallDistributedBuildQueues )
        {
            for ( String buildAgentUrl : overallDistributedBuildQueues.keySet() )
            {
                OverallDistributedBuildQueue overallDistributedBuildQueue = 
                    overallDistributedBuildQueues.get( buildAgentUrl );
    
                if ( overallDistributedBuildQueue != null )
                {
                    try
                    {
                        if ( isAgentAvailable( buildAgentUrl ) )
                        {
                            SlaveBuildAgentTransportService client = createSlaveBuildAgentTransportClientConnection( buildAgentUrl );

                            // TODO: should check for specific build definition and handle that in UI;
                            // project with different build definitions can build at the same time in different agents and
                            // currently, we can't handle that in the ui.
                            if ( client.isProjectCurrentlyBuilding( projectId, -1 ) )
                            {
                                agentUrl = buildAgentUrl;
                                break;
                            }
                        }
                        else
                        {
                            log.debug( "Unable to check if project {} is currently building. Build agent {} is not available",
                                       projectId, buildAgentUrl );
                        }
                    }
                    catch ( MalformedURLException e )
                    {
                        log.warn( "Unable to check if project {} is currently building in agent: Invalid build agent url {}",
                                  projectId, buildAgentUrl );
                    }
                    catch ( Exception e )
                    {
                        log.warn( "Unable to check if project {} is currently building in agent", projectId, e );
                    }
                }
            }
        }

        // call reload in case we disable a build agent
        reload();

        return agentUrl;
    }

    public String getBuildAgentUrl( int projectId, int buildDefinitionId )
        throws ContinuumException
    {
        String agentUrl = null;

        synchronized( overallDistributedBuildQueues )
        {
            for ( String buildAgentUrl : overallDistributedBuildQueues.keySet() )
            {
                OverallDistributedBuildQueue overallDistributedBuildQueue = 
                    overallDistributedBuildQueues.get( buildAgentUrl );
    
                if ( overallDistributedBuildQueue != null )
                {
                    try
                    {
                        if ( isAgentAvailable( buildAgentUrl ) )
                        {
                            log.debug( "Checking if project {} with build definition {} is currently queued or processed in agent {}", 
                                       new Object[] { projectId, buildDefinitionId, buildAgentUrl } );

                            SlaveBuildAgentTransportService client = createSlaveBuildAgentTransportClientConnection( buildAgentUrl );
                            
                            if ( client.isProjectInPrepareBuildQueue( projectId, buildDefinitionId ) ||
                                 client.isProjectCurrentlyPreparingBuild( projectId, buildDefinitionId ) ||
                                 client.isProjectInBuildQueue( projectId, buildDefinitionId ) ||
                                 client.isProjectCurrentlyBuilding( projectId, buildDefinitionId ) )
                            {
                                log.debug( "Project {} with build definition {} is currently queued or processed in agent {}", 
                                           new Object[] { projectId, buildDefinitionId, buildAgentUrl } );
                                agentUrl = buildAgentUrl;
                                break;
                            }
                        }
                        else
                        {
                            log.debug( "Unable to check if project {} is currently queued or processed in agent. Build agent {} is not available", 
                                       projectId, buildAgentUrl );
                        }
                    }
                    catch ( MalformedURLException e )
                    {
                        log.warn( "Unable to check if project {} is currently queued or processed in agent: Invalid build agent url {}",
                                  projectId, buildAgentUrl );
                    }
                    catch ( Exception e )
                    {
                        log.warn( "Unable to check if project {} is currently queued or processed in agent", projectId, e );
                    }
                }
            }
        }

        // call reload in case we disable a build agent
        reload();

        return agentUrl;
    }

    private void createDistributedBuildQueueForAgent( String buildAgentUrl )
        throws ComponentLookupException
    {
        if ( !overallDistributedBuildQueues.containsKey( buildAgentUrl ) )
        {
            OverallDistributedBuildQueue overallDistributedBuildQueue =
                (OverallDistributedBuildQueue) container.lookup( OverallDistributedBuildQueue.class );
            overallDistributedBuildQueue.setBuildAgentUrl( buildAgentUrl );
            overallDistributedBuildQueue.getDistributedBuildTaskQueueExecutor().setBuildAgentUrl( buildAgentUrl );

            overallDistributedBuildQueues.put( buildAgentUrl, overallDistributedBuildQueue );
        }
    }

    private OverallDistributedBuildQueue getOverallDistributedBuildQueueByScmRoot( ProjectScmRoot scmRoot, int projectGroupId )
        throws ContinuumException
    {
        int scmRootId = scmRoot.getId();

        synchronized( overallDistributedBuildQueues )
        {
            for ( String buildAgentUrl : overallDistributedBuildQueues.keySet() )
            {
                OverallDistributedBuildQueue distributedBuildQueue = overallDistributedBuildQueues.get( buildAgentUrl );

                try
                {
                    for ( PrepareBuildProjectsTask task : distributedBuildQueue.getProjectsInQueue() )
                    {
                        if ( task.getProjectScmRootId() == scmRootId )
                        {
                            log.debug( "Projects in the same continuum group are building in build agent: {}. Also building project in the same agent.", buildAgentUrl );
                            return distributedBuildQueue;
                        }
                    }

                    Task task = distributedBuildQueue.getDistributedBuildTaskQueueExecutor().getCurrentTask();
                    if ( task != null && ( (PrepareBuildProjectsTask) task ).getProjectScmRootId() == scmRootId )
                    {
                        log.debug( "Projects in the same continuum group are building in build agent: {}. Also building project in the same agent.", buildAgentUrl );
                        return distributedBuildQueue;
                    }

                    if ( isAgentAvailable( buildAgentUrl ) )
                    {
                        List<Project> projects = projectDao.getProjectsInGroup( projectGroupId );
                        List<Integer> pIds = new ArrayList<Integer>();

                        for ( Project project : projects )
                        {
                            if ( project.getScmUrl().startsWith( scmRoot.getScmRootAddress() ) )
                            {
                                pIds.add( project.getId() );
                            }
                        }

                        SlaveBuildAgentTransportService client = createSlaveBuildAgentTransportClientConnection( buildAgentUrl );

                        if ( client.isProjectScmRootInQueue( scmRootId, pIds ) )
                        {
                            log.debug( "Projects in the same continuum group are building in build agent: {}. Also building project in the same agent.", buildAgentUrl );
                            return distributedBuildQueue;
                        }
                    }
                    else
                    {
                        log.debug( "Build agent {} is not available. Skipping...", buildAgentUrl );
                    }
                }
                catch ( TaskQueueException e )
                {
                    log.error( "Error occurred while retrieving distributed build queue of scmRootId=" + scmRootId, e );
                    throw new ContinuumException( "Error occurred while retrieving distributed build queue of scmRoot", e );
                }
                catch ( MalformedURLException e )
                {
                    log.error( "Error occurred while retrieving distributed build queue of scmRootId=" + scmRootId + 
                               ": Invalid build agent url " + buildAgentUrl );
                    throw new ContinuumException( "Error occurred while retrieving distributed build queue of scmRootId=" + scmRootId + 
                               ": Invalid build agent url " + buildAgentUrl );
                }
                catch ( Exception e )
                {
                    log.error( "Error occurred while retrieving distributed build queue of scmRootId=" + scmRootId, e );
                    throw new ContinuumException( "Error occurred while retrieving distributed build queue of scmRoot", e );
                }
            }
        }

        return null;
    }

    private OverallDistributedBuildQueue getOverallDistributedBuildQueueByGroup( int projectGroupId, List<ProjectScmRoot> scmRoots, int scmRootId )
        throws ContinuumException
    {
        if ( scmRoots != null )
        {
            log.debug( "Checking if the project group is already building in one of the build agents" );

            for ( ProjectScmRoot scmRoot : scmRoots )
            {
                if ( scmRoot.getId() == scmRootId )
                {
                    break;
                }
                else if ( scmRoot.getProjectGroup().getId() == projectGroupId )
                {
                    return getOverallDistributedBuildQueueByScmRoot( scmRoot, projectGroupId );
                }
            }
        }
        return null;
    }

    private OverallDistributedBuildQueue getOverallDistributedBuildQueueByAgentGroup( Map<Integer, Integer> projectsAndBuildDefinitionsMap )
        throws ContinuumException
    {
        OverallDistributedBuildQueue whereToBeQueued = null;

        BuildAgentGroupConfiguration buildAgentGroup = getBuildAgentGroup( projectsAndBuildDefinitionsMap );

        if ( buildAgentGroup != null )
        {
            List<BuildAgentConfiguration> buildAgents = buildAgentGroup.getBuildAgents();

            if ( buildAgents != null && buildAgents.size() > 0 )
            {
                List<String> buildAgentUrls = new ArrayList<String>();
                
                for ( BuildAgentConfiguration buildAgent : buildAgents )
                {
                    buildAgentUrls.add( buildAgent.getUrl() );
                }

                synchronized( overallDistributedBuildQueues )
                {
                    int idx = 0;
                    int size = 0;
                    
                    for ( String buildAgentUrl : overallDistributedBuildQueues.keySet() )
                    {
                        if ( !buildAgentUrls.isEmpty() && buildAgentUrls.contains( buildAgentUrl ) )
                        {
                            OverallDistributedBuildQueue distributedBuildQueue = overallDistributedBuildQueues.get( buildAgentUrl );

                            if ( distributedBuildQueue != null )
                            {
                                try
                                {
                                    if ( isAgentAvailable( buildAgentUrl ) )
                                    {
                                        log.debug( "Build agent {} is available", buildAgentUrl );

                                        SlaveBuildAgentTransportService client = createSlaveBuildAgentTransportClientConnection( buildAgentUrl );
                                        int agentBuildSize = client.getBuildSizeOfAgent();
    
                                        log.debug( "Number of projects currently building in agent: {}", agentBuildSize );
                                        if ( idx == 0 )
                                        {
                                            log.debug( "Current least busy agent: {}", buildAgentUrl );
                                            whereToBeQueued = distributedBuildQueue;
                                            size = agentBuildSize;
                                            idx++;
                                        }
    
                                        if ( agentBuildSize < size )
                                        {
                                            log.debug( "Current least busy agent: {}", buildAgentUrl );
                                            whereToBeQueued = distributedBuildQueue;
                                            size = agentBuildSize;
                                        }
                                    }
                                    else
                                    {
                                        log.debug( "Build agent {} is not available. Skipping...", buildAgentUrl );
                                    }
                                }
                                catch ( MalformedURLException e )
                                {
                                    log.error( "Error occurred while retrieving distributed build queue: Invalid build agent url " + buildAgentUrl );
                                }
                                catch ( Exception e )
                                {
                                    log.error( "Error occurred while retrieving distributed build queue ", e );
                                }
                            }
                        }
                    }
                }
            }
        }

        return whereToBeQueued;
    }

    private OverallDistributedBuildQueue getOverallDistributedBuildQueue()
        throws ContinuumException
    {
        OverallDistributedBuildQueue whereToBeQueued = null;

        synchronized ( overallDistributedBuildQueues )
        {
            if ( overallDistributedBuildQueues.isEmpty() )
            {
                log.info( "No distributed build queues are configured for build agents" );
                return null;
            }

            int idx = 0;
            int size = 0;

            for ( String buildAgentUrl : overallDistributedBuildQueues.keySet() )
            {
                OverallDistributedBuildQueue distributedBuildQueue = overallDistributedBuildQueues.get( buildAgentUrl );

                if ( distributedBuildQueue != null )
                {
                    try
                    {
                        if ( isAgentAvailable( buildAgentUrl ) )
                        {
                            log.debug( "Build agent {} is available", buildAgentUrl );

                            SlaveBuildAgentTransportService client = createSlaveBuildAgentTransportClientConnection( buildAgentUrl );
                            int agentBuildSize = client.getBuildSizeOfAgent();

                            log.debug( "Number of projects currently building in agent: {}", agentBuildSize );
                            if ( idx == 0 )
                            {
                                log.debug( "Current least busy agent: {}", buildAgentUrl );
                                whereToBeQueued = distributedBuildQueue;
                                size = agentBuildSize;
                                idx++;
                            }
    
                            if ( agentBuildSize < size )
                            {
                                log.debug( "Current least busy agent: {}", buildAgentUrl );
                                whereToBeQueued = distributedBuildQueue;
                                size = agentBuildSize;
                            }
                        }
                        else
                        {
                            log.debug( "Build agent {} is not available. Skipping...", buildAgentUrl );
                        }
                    }
                    catch ( MalformedURLException e )
                    {
                        log.error( "Error occurred while retrieving distributed build queue: invalid build agent url " + buildAgentUrl );
                    }
                    catch ( Exception e )
                    {
                        log.error( "Error occurred while retrieving distributed build queue", e );
                        throw new ContinuumException( "Error occurred while retrieving distributed build queue", e );
                    }
                }
            }
        }

        return whereToBeQueued;
    }

    private BuildAgentGroupConfiguration getBuildAgentGroup( Map<Integer, Integer> projectsAndBuildDefinitions )
        throws ContinuumException
    {
        if ( projectsAndBuildDefinitions == null )
        {
            return null;
        }
        
        try
        {
            List<Project> projects = new ArrayList<Project>();

            for ( Integer projectId : projectsAndBuildDefinitions.keySet() )
            {
                projects.add( projectDao.getProjectWithDependencies( projectId ) );
            }

            projects = ProjectSorter.getSortedProjects( projects, log );

            int buildDefinitionId = projectsAndBuildDefinitions.get( projects.get( 0 ).getId() );
            BuildDefinition buildDefinition = buildDefinitionDao.getBuildDefinition( buildDefinitionId );

            Profile profile = buildDefinition.getProfile();

            if ( profile != null && !StringUtils.isEmpty( profile.getBuildAgentGroup() ) )
            {
                String groupName = profile.getBuildAgentGroup();

                BuildAgentGroupConfiguration buildAgentGroup = configurationService.getBuildAgentGroup( groupName );

                return buildAgentGroup;
            }
        }
        catch ( ContinuumStoreException e )
        {
            log.error( "Error while getting build agent group", e );
            throw new ContinuumException( "Error while getting build agent group", e );
        }

        log.info( "profile build agent group is null" );

        return null;
    }
 
    private PrepareBuildProjectsTask getPrepareBuildProjectsTask( Map context )
    {
        int projectGroupId = ContinuumBuildConstant.getProjectGroupId( context );
        int scmRootId = ContinuumBuildConstant.getScmRootId( context );
        String scmRootAddress = ContinuumBuildConstant.getScmRootAddress( context );
        BuildTrigger buildTrigger = new BuildTrigger( ContinuumBuildConstant.getTrigger( context ), ContinuumBuildConstant.getUsername( context ) );

        return new PrepareBuildProjectsTask( null, buildTrigger, projectGroupId, null, scmRootAddress, scmRootId );
    }

    private BuildProjectTask getBuildProjectTask( Map context )
    {
        int projectId = ContinuumBuildConstant.getProjectId( context );
        int buildDefinitionId = ContinuumBuildConstant.getBuildDefinitionId( context );
        BuildTrigger buildTrigger = new BuildTrigger( ContinuumBuildConstant.getTrigger( context ), ContinuumBuildConstant.getUsername( context ) );
        int projectGroupId = ContinuumBuildConstant.getProjectGroupId( context );
        String buildDefinitionLabel = ContinuumBuildConstant.getBuildDefinitionLabel( context );

        return new BuildProjectTask( projectId, buildDefinitionId, buildTrigger, null, buildDefinitionLabel, null, projectGroupId );
    }

    public boolean isAgentAvailable( String buildAgentUrl )
        throws ContinuumException
    {
        try
        {
            if ( pingBuildAgent( buildAgentUrl ) )
            {
                return true;
            }
        }
        catch ( Exception e )
        {
            log.warn( "Disable build agent: {}; Unable to ping due to {}", buildAgentUrl,  e );
        }

        // disable it
        disableBuildAgent( buildAgentUrl );

        return false;
    }

    public boolean pingBuildAgent( String buildAgentUrl )
        throws ContinuumException
    {
        try
        {
            SlaveBuildAgentTransportService client = createSlaveBuildAgentTransportClientConnection( buildAgentUrl );

            return client.ping();
        }
        catch ( MalformedURLException e )
        {
            log.warn( "Invalid build agent url {}", buildAgentUrl );
        }
        catch ( Exception e )
        {
            throw new ContinuumException( "Unable to ping build agent " + buildAgentUrl + " : " + e.getMessage() );
        }

        return false;
    }

    public List<ProjectRunSummary> getCurrentRuns()
    {
        return currentRuns;
    }

    public void removeCurrentRun( int projectId, int buildDefinitionId )
    {
        synchronized( currentRuns )
        {
            boolean found = false;
            ProjectRunSummary runToDelete = null;

            for ( ProjectRunSummary currentRun : currentRuns )
            {
                if ( currentRun.getProjectId() == projectId && currentRun.getBuildDefinitionId() == buildDefinitionId )
                {
                    found = true;
                    runToDelete = currentRun;
                    break;
                }
            }

            if ( found )
            {
                currentRuns.remove( runToDelete );
            }
        }
    }

    private void createProjectRunSummaries( PrepareBuildProjectsTask task, String buildAgentUrl )
    {
        synchronized( currentRuns )
        {
            int projectGroupId = task.getProjectGroupId();
            int projectScmRootId = task.getProjectScmRootId();
            Map<Integer, Integer> map = task.getProjectsBuildDefinitionsMap();
    
            for ( int projectId : map.keySet() )
            {
                int buildDefinitionId = map.get( projectId );

                ProjectRunSummary runToDelete = null;

                // check if there is an existing current run with that project id and build definition id
                for ( ProjectRunSummary currentRun : currentRuns )
                {
                    if ( currentRun.getProjectId() == projectId && currentRun.getBuildDefinitionId() == buildDefinitionId )
                    {
                        runToDelete = currentRun;
                        break;
                    }
                }

                if ( runToDelete != null )
                {
                    // previous run already finished, but was not removed from the list
                    // removed it
                    currentRuns.remove( runToDelete );
                }

                ProjectRunSummary run = new ProjectRunSummary();
                run.setProjectGroupId( projectGroupId );
                run.setProjectScmRootId( projectScmRootId );
                run.setProjectId( projectId );
                run.setBuildDefinitionId( buildDefinitionId );
                run.setTriggeredBy( task.getBuildTrigger().getTriggeredBy() );
                run.setTrigger( task.getBuildTrigger().getTrigger() );
                run.setBuildAgentUrl( buildAgentUrl );
                currentRuns.add( run );
            }
        }
    }

    private void disableBuildAgent( String buildAgentUrl )
        throws ContinuumException
    {
        List<BuildAgentConfiguration> agents = configurationService.getBuildAgents();

        for ( BuildAgentConfiguration agent : agents )
        {
            if ( agent.getUrl().equals( buildAgentUrl ) )
            {
                agent.setEnabled( false );
                configurationService.updateBuildAgent( agent );

                try
                {
                    configurationService.store();

                    log.debug( "Disabled build agent {}", buildAgentUrl );
                }
                catch ( Exception e )
                {
                    throw new ContinuumException( "Unable to disable build agent: " + buildAgentUrl, e );
                }
            }
        }
    }
    
    private boolean hasBuildagentGroup( Map<Integer, Integer> projectsAndBuildDefinitionsMap )
        throws ContinuumException
    {
        BuildAgentGroupConfiguration buildAgentGroup = getBuildAgentGroup( projectsAndBuildDefinitionsMap );

        return buildAgentGroup != null &&
               buildAgentGroup.getName().length() > 0 ? true : false;
    }
    
    private boolean hasBuildagentInGroup( Map<Integer, Integer> projectsAndBuildDefinitionsMap )
        throws ContinuumException
    {
        BuildAgentGroupConfiguration buildAgentGroup = getBuildAgentGroup( projectsAndBuildDefinitionsMap );

        return buildAgentGroup != null &&
               buildAgentGroup.getBuildAgents().size() > 0 ? true : false;
    }

    public SlaveBuildAgentTransportService createSlaveBuildAgentTransportClientConnection( String buildAgentUrl ) 
        throws MalformedURLException, Exception
    {
        return new SlaveBuildAgentTransportClient( new URL( buildAgentUrl ), "", configurationService.getSharedSecretPassword() );
    }

    // for unit testing

    public void setOverallDistributedBuildQueues( Map<String, OverallDistributedBuildQueue> overallDistributedBuildQueues )
    {
        this.overallDistributedBuildQueues = overallDistributedBuildQueues;
    }

    public void setConfigurationService( ConfigurationService configurationService )
    {
        this.configurationService = configurationService;
    }

    public void setProjectDao( ProjectDao projectDao )
    {
        this.projectDao = projectDao;
    }

    public void setBuildDefinitionDao( BuildDefinitionDao buildDefinitionDao )
    {
        this.buildDefinitionDao = buildDefinitionDao;
    }

    public void setBuildResultDao( BuildResultDao buildResultDao )
    {
        this.buildResultDao = buildResultDao;
    }

    public void setContainer( PlexusContainer container )
    {
        this.container = container;
    }

    public void setCurrentRuns( List<ProjectRunSummary> currentRuns )
    {
        this.currentRuns = currentRuns;
    }

    public void setProjectScmRootDao( ProjectScmRootDao projectScmRootDao )
    {
        this.projectScmRootDao = projectScmRootDao;
    }
}
