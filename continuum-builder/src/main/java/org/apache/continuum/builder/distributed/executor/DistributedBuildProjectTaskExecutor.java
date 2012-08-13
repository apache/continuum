package org.apache.continuum.builder.distributed.executor;

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

import org.apache.continuum.builder.utils.ContinuumBuildConstant;
import org.apache.continuum.dao.BuildDefinitionDao;
import org.apache.continuum.dao.BuildResultDao;
import org.apache.continuum.dao.ProjectDao;
import org.apache.continuum.dao.ProjectScmRootDao;
import org.apache.continuum.distributed.transport.slave.SlaveBuildAgentTransportClient;
import org.apache.continuum.model.project.ProjectScmRoot;
import org.apache.continuum.model.repository.LocalRepository;
import org.apache.continuum.taskqueue.PrepareBuildProjectsTask;
import org.apache.continuum.utils.ContinuumUtils;
import org.apache.continuum.utils.ProjectSorter;
import org.apache.continuum.utils.build.BuildTrigger;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.scm.ChangeFile;
import org.apache.maven.continuum.model.scm.ChangeSet;
import org.apache.maven.continuum.model.scm.ScmResult;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.taskqueue.Task;
import org.codehaus.plexus.taskqueue.execution.TaskExecutionException;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DistributedBuildProjectTaskExecutor
    implements DistributedBuildTaskExecutor
{
    private static final Logger log = LoggerFactory.getLogger( DistributedBuildProjectTaskExecutor.class );

    private String buildAgentUrl;

    private long startTime;

    private long endTime;

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
    private ConfigurationService configurationService;

    public void setBuildAgentUrl( String buildAgentUrl )
    {
        this.buildAgentUrl = buildAgentUrl;
    }

    public String getBuildAgentUrl()
    {
        return buildAgentUrl;
    }

    public void executeTask( Task task )
        throws TaskExecutionException
    {
        PrepareBuildProjectsTask prepareBuildTask = (PrepareBuildProjectsTask) task;

        try
        {
            SlaveBuildAgentTransportClient client = new SlaveBuildAgentTransportClient( new URL( buildAgentUrl ), "",
                                                                                        configurationService.getSharedSecretPassword() );

            log.info( "initializing buildContext for projectGroupId=" + prepareBuildTask.getProjectGroupId() );
            List<Map<String, Object>> buildContext = initializeBuildContext(
                prepareBuildTask.getProjectsBuildDefinitionsMap(), prepareBuildTask.getBuildTrigger(),
                prepareBuildTask.getScmRootAddress(), prepareBuildTask.getProjectScmRootId() );

            startTime = System.currentTimeMillis();
            client.buildProjects( buildContext );
            endTime = System.currentTimeMillis();
        }
        catch ( MalformedURLException e )
        {
            log.error( "Invalid URL " + buildAgentUrl + ", not building" );
            throw new TaskExecutionException( "Invalid URL " + buildAgentUrl, e );
        }
        catch ( Exception e )
        {
            log.error( "Error occurred while building task", e );
            endTime = System.currentTimeMillis();
            createResult( prepareBuildTask, ContinuumUtils.throwableToString( e ) );
        }
    }

    private List<Map<String, Object>> initializeBuildContext( Map<Integer, Integer> projectsAndBuildDefinitions,
                                                              BuildTrigger buildTrigger, String scmRootAddress,
                                                              int scmRootId )
        throws ContinuumException
    {
        List<Map<String, Object>> buildContext = new ArrayList<Map<String, Object>>();

        try
        {
            ProjectScmRoot scmRoot = projectScmRootDao.getProjectScmRoot( scmRootId );

            List<Project> projects = projectDao.getProjectsWithDependenciesByGroupId(
                scmRoot.getProjectGroup().getId() );
            List<Project> sortedProjects = ProjectSorter.getSortedProjects( projects, null );

            for ( Project project : sortedProjects )
            {
                if ( !projectsAndBuildDefinitions.containsKey( project.getId() ) )
                {
                    continue;
                }

                int buildDefinitionId = projectsAndBuildDefinitions.get( project.getId() );
                BuildDefinition buildDef = buildDefinitionDao.getBuildDefinition( buildDefinitionId );
                BuildResult buildResult = buildResultDao.getLatestBuildResultForProject( project.getId() );

                Map<String, Object> context = new HashMap<String, Object>();

                context.put( ContinuumBuildConstant.KEY_PROJECT_GROUP_ID, project.getProjectGroup().getId() );
                context.put( ContinuumBuildConstant.KEY_PROJECT_GROUP_NAME, project.getProjectGroup().getName() );
                context.put( ContinuumBuildConstant.KEY_SCM_ROOT_ID, scmRootId );
                context.put( ContinuumBuildConstant.KEY_SCM_ROOT_ADDRESS, scmRootAddress );
                context.put( ContinuumBuildConstant.KEY_PROJECT_ID, project.getId() );
                context.put( ContinuumBuildConstant.KEY_PROJECT_NAME, project.getName() );
                context.put( ContinuumBuildConstant.KEY_PROJECT_VERSION, project.getVersion() );
                context.put( ContinuumBuildConstant.KEY_EXECUTOR_ID, project.getExecutorId() );
                context.put( ContinuumBuildConstant.KEY_PROJECT_BUILD_NUMBER, project.getBuildNumber() );
                context.put( ContinuumBuildConstant.KEY_SCM_URL, project.getScmUrl() );
                context.put( ContinuumBuildConstant.KEY_PROJECT_STATE, project.getState() );
                if ( buildResult != null )
                {
                    context.put( ContinuumBuildConstant.KEY_LATEST_UPDATE_DATE, new Date(
                        buildResult.getLastChangedDate() ) );
                }

                LocalRepository localRepo = project.getProjectGroup().getLocalRepository();

                if ( localRepo != null )
                {
                    // CONTINUUM-2391
                    context.put( ContinuumBuildConstant.KEY_LOCAL_REPOSITORY, localRepo.getName() );
                }
                else
                {
                    context.put( ContinuumBuildConstant.KEY_LOCAL_REPOSITORY, "" );
                }

                if ( project.getScmUsername() == null )
                {
                    context.put( ContinuumBuildConstant.KEY_SCM_USERNAME, "" );
                }
                else
                {
                    context.put( ContinuumBuildConstant.KEY_SCM_USERNAME, project.getScmUsername() );
                }

                if ( project.getScmPassword() == null )
                {
                    context.put( ContinuumBuildConstant.KEY_SCM_PASSWORD, "" );
                }
                else
                {
                    context.put( ContinuumBuildConstant.KEY_SCM_PASSWORD, project.getScmPassword() );
                }

                if ( project.getScmTag() != null )
                {
                    context.put( ContinuumBuildConstant.KEY_SCM_TAG, project.getScmTag() );
                }
                else
                {
                    context.put( ContinuumBuildConstant.KEY_SCM_TAG, "" );
                }

                context.put( ContinuumBuildConstant.KEY_BUILD_DEFINITION_ID, buildDefinitionId );
                String buildDefinitionLabel = buildDef.getDescription();
                if ( StringUtils.isEmpty( buildDefinitionLabel ) )
                {
                    buildDefinitionLabel = buildDef.getGoals();
                }
                context.put( ContinuumBuildConstant.KEY_BUILD_DEFINITION_LABEL, buildDefinitionLabel );

                context.put( ContinuumBuildConstant.KEY_BUILD_FILE, buildDef.getBuildFile() );

                if ( buildDef.getGoals() == null )
                {
                    context.put( ContinuumBuildConstant.KEY_GOALS, "" );
                }
                else
                {
                    context.put( ContinuumBuildConstant.KEY_GOALS, buildDef.getGoals() );
                }

                if ( buildDef.getArguments() == null )
                {
                    context.put( ContinuumBuildConstant.KEY_ARGUMENTS, "" );
                }
                else
                {
                    context.put( ContinuumBuildConstant.KEY_ARGUMENTS, buildDef.getArguments() );
                }
                context.put( ContinuumBuildConstant.KEY_TRIGGER, buildTrigger.getTrigger() );

                if ( buildTrigger.getTrigger() == ContinuumProjectState.TRIGGER_FORCED )
                {
                    context.put( ContinuumBuildConstant.KEY_USERNAME, buildTrigger.getTriggeredBy() );
                }
                else
                {
                    context.put( ContinuumBuildConstant.KEY_USERNAME, buildDef.getSchedule().getName() );
                }

                context.put( ContinuumBuildConstant.KEY_BUILD_FRESH, buildDef.isBuildFresh() );
                context.put( ContinuumBuildConstant.KEY_ALWAYS_BUILD, buildDef.isAlwaysBuild() );
                context.put( ContinuumBuildConstant.KEY_OLD_SCM_CHANGES, getOldScmChanges( project.getId(),
                                                                                           buildDefinitionId ) );
                context.put( ContinuumBuildConstant.KEY_BUILD_AGENT_URL, buildAgentUrl );
                context.put( ContinuumBuildConstant.KEY_MAX_JOB_EXEC_TIME,
                             buildDef.getSchedule().getMaxJobExecutionTime() );

                buildContext.add( context );
            }

            return buildContext;
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumException( "Error while initializing build context", e );
        }
    }

    private void createResult( PrepareBuildProjectsTask task, String error )
        throws TaskExecutionException
    {
        try
        {
            ProjectScmRoot scmRoot = projectScmRootDao.getProjectScmRootByProjectGroupAndScmRootAddress(
                task.getProjectGroupId(), task.getScmRootAddress() );

            if ( scmRoot.getState() == ContinuumProjectState.UPDATING )
            {
                scmRoot.setState( ContinuumProjectState.ERROR );
                scmRoot.setError( error );
                projectScmRootDao.updateProjectScmRoot( scmRoot );
            }
            else
            {
                Map<Integer, Integer> map = task.getProjectsBuildDefinitionsMap();
                for ( Integer projectId : map.keySet() )
                {
                    int buildDefinitionId = map.get( projectId );
                    Project project = projectDao.getProject( projectId );
                    BuildDefinition buildDef = buildDefinitionDao.getBuildDefinition( buildDefinitionId );
                    BuildResult latestBuildResult = buildResultDao.
                        getLatestBuildResultForBuildDefinition( projectId, buildDefinitionId );
                    if ( latestBuildResult == null ||
                        ( latestBuildResult.getStartTime() >= startTime && latestBuildResult.getEndTime() > 0 &&
                            latestBuildResult.getEndTime() < endTime ) || latestBuildResult.getStartTime() < startTime )
                    {
                        BuildResult buildResult = new BuildResult();
                        buildResult.setBuildDefinition( buildDef );
                        buildResult.setError( error );
                        buildResult.setState( ContinuumProjectState.ERROR );
                        buildResult.setTrigger( task.getBuildTrigger().getTrigger() );
                        buildResult.setUsername( task.getBuildTrigger().getTriggeredBy() );
                        buildResult.setStartTime( startTime );
                        buildResult.setEndTime( endTime );

                        buildResultDao.addBuildResult( project, buildResult );
                    }
                }

            }
        }
        catch ( ContinuumStoreException e )
        {
            throw new TaskExecutionException( "Error while creating result", e );
        }
    }

    private List<Map<String, Object>> getOldScmChanges( int projectId, int buildDefinitionId )
        throws ContinuumStoreException
    {
        List<Map<String, Object>> scmChanges = new ArrayList<Map<String, Object>>();

        BuildResult oldBuildResult = buildResultDao.getLatestBuildResultForBuildDefinition( projectId,
                                                                                            buildDefinitionId );

        if ( oldBuildResult != null )
        {
            ScmResult scmResult = getOldScmResults( projectId, oldBuildResult.getBuildNumber(),
                                                    oldBuildResult.getEndTime() );

            scmChanges = getScmChanges( scmResult );
        }

        return scmChanges;
    }

    private List<Map<String, Object>> getScmChanges( ScmResult scmResult )
    {
        List<Map<String, Object>> scmChanges = new ArrayList<Map<String, Object>>();

        if ( scmResult != null && scmResult.getChanges() != null )
        {
            for ( Object obj : scmResult.getChanges() )
            {
                ChangeSet changeSet = (ChangeSet) obj;

                Map<String, Object> map = new HashMap<String, Object>();
                if ( StringUtils.isNotEmpty( changeSet.getAuthor() ) )
                {
                    map.put( ContinuumBuildConstant.KEY_CHANGESET_AUTHOR, changeSet.getAuthor() );
                }
                else
                {
                    map.put( ContinuumBuildConstant.KEY_CHANGESET_AUTHOR, "" );
                }
                if ( StringUtils.isNotEmpty( changeSet.getComment() ) )
                {
                    map.put( ContinuumBuildConstant.KEY_CHANGESET_COMMENT, changeSet.getComment() );
                }
                else
                {
                    map.put( ContinuumBuildConstant.KEY_CHANGESET_COMMENT, "" );
                }
                if ( changeSet.getDateAsDate() != null )
                {
                    map.put( ContinuumBuildConstant.KEY_CHANGESET_DATE, changeSet.getDateAsDate() );
                }
                map.put( ContinuumBuildConstant.KEY_CHANGESET_FILES, getScmChangeFiles( changeSet.getFiles() ) );
                scmChanges.add( map );
            }
        }

        return scmChanges;
    }

    private List<Map<String, String>> getScmChangeFiles( List<ChangeFile> files )
    {
        List<Map<String, String>> scmChangeFiles = new ArrayList<Map<String, String>>();

        if ( files != null )
        {
            for ( ChangeFile changeFile : files )
            {
                Map<String, String> map = new HashMap<String, String>();

                if ( StringUtils.isNotEmpty( changeFile.getName() ) )
                {
                    map.put( ContinuumBuildConstant.KEY_CHANGEFILE_NAME, changeFile.getName() );
                }
                else
                {
                    map.put( ContinuumBuildConstant.KEY_CHANGEFILE_NAME, "" );
                }
                if ( StringUtils.isNotEmpty( changeFile.getRevision() ) )
                {
                    map.put( ContinuumBuildConstant.KEY_CHANGEFILE_REVISION, changeFile.getRevision() );
                }
                else
                {
                    map.put( ContinuumBuildConstant.KEY_CHANGEFILE_REVISION, "" );
                }
                if ( StringUtils.isNotEmpty( changeFile.getStatus() ) )
                {
                    map.put( ContinuumBuildConstant.KEY_CHANGEFILE_STATUS, changeFile.getStatus() );
                }
                else
                {
                    map.put( ContinuumBuildConstant.KEY_CHANGEFILE_STATUS, "" );
                }
                scmChangeFiles.add( map );
            }
        }
        return scmChangeFiles;
    }

    private ScmResult getOldScmResults( int projectId, long startId, long fromDate )
        throws ContinuumStoreException
    {
        List<BuildResult> results = buildResultDao.getBuildResultsForProjectFromId( projectId, startId );

        ScmResult res = new ScmResult();

        if ( results != null && results.size() > 0 )
        {
            for ( BuildResult result : results )
            {
                ScmResult scmResult = result.getScmResult();

                if ( scmResult != null )
                {
                    List<ChangeSet> changes = scmResult.getChanges();

                    if ( changes != null )
                    {
                        for ( ChangeSet changeSet : changes )
                        {
                            if ( changeSet.getDate() < fromDate )
                            {
                                continue;
                            }
                            if ( !res.getChanges().contains( changeSet ) )
                            {
                                res.addChange( changeSet );
                            }
                        }
                    }
                }
            }
        }

        return res;
    }
}
