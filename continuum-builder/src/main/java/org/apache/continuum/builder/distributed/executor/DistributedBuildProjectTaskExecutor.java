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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.apache.maven.continuum.ContinuumException;
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
import org.codehaus.plexus.util.dag.CycleDetectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DistributedBuildProjectTaskExecutor
    implements DistributedBuildTaskExecutor
{
    private Logger log = LoggerFactory.getLogger( this.getClass() );

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
            SlaveBuildAgentTransportClient client = new SlaveBuildAgentTransportClient( new URL( buildAgentUrl ) );

            log.info( "initializing buildContext" );
            List buildContext = initializeBuildContext( prepareBuildTask.getProjectsBuildDefinitionsMap(), 
                                                        prepareBuildTask.getTrigger(), 
                                                        prepareBuildTask.getScmRootAddress() );

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

    private List initializeBuildContext( Map<Integer, Integer> projectsAndBuildDefinitions, 
                                         int trigger, String scmRootAddress )
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

            for ( Project project : projects )
            {                
                int buildDefinitionId = projectsAndBuildDefinitions.get( project.getId() );
                BuildDefinition buildDef = buildDefinitionDao.getBuildDefinition( buildDefinitionId );

                Map context = new HashMap();
                
                context.put( ContinuumBuildConstant.KEY_PROJECT_GROUP_ID, new Integer( project.getProjectGroup().getId() ) );
                context.put( ContinuumBuildConstant.KEY_PROJECT_GROUP_NAME, project.getProjectGroup().getName() );
                context.put( ContinuumBuildConstant.KEY_SCM_ROOT_ADDRESS, scmRootAddress );
                context.put( ContinuumBuildConstant.KEY_PROJECT_ID, new Integer( project.getId() ) );
                context.put( ContinuumBuildConstant.KEY_PROJECT_NAME, project.getName() );
                context.put( ContinuumBuildConstant.KEY_PROJECT_VERSION, project.getVersion() );
                context.put( ContinuumBuildConstant.KEY_EXECUTOR_ID, project.getExecutorId() );
                context.put( ContinuumBuildConstant.KEY_PROJECT_BUILD_NUMBER, new Integer( project.getBuildNumber() ) );
                context.put( ContinuumBuildConstant.KEY_SCM_URL, project.getScmUrl() );
                context.put( ContinuumBuildConstant.KEY_PROJECT_STATE, new Integer( project.getState() ) );

                LocalRepository localRepo = project.getProjectGroup().getLocalRepository();

                if ( localRepo != null )
                {
                    context.put( ContinuumBuildConstant.KEY_LOCAL_REPOSITORY, localRepo.getLocation() );
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

                context.put( ContinuumBuildConstant.KEY_BUILD_DEFINITION_ID, new Integer( buildDefinitionId ) );
                context.put( ContinuumBuildConstant.KEY_BUILD_FILE, buildDef.getBuildFile() );
                context.put( ContinuumBuildConstant.KEY_GOALS, buildDef.getGoals() );

                if ( buildDef.getArguments() == null )
                {
                    context.put( ContinuumBuildConstant.KEY_ARGUMENTS, "" );
                }
                else
                {
                    context.put( ContinuumBuildConstant.KEY_ARGUMENTS, buildDef.getArguments() );
                }
                context.put( ContinuumBuildConstant.KEY_TRIGGER, new Integer( trigger ) );
                context.put( ContinuumBuildConstant.KEY_BUILD_FRESH, new Boolean( buildDef.isBuildFresh() ) );
                context.put( ContinuumBuildConstant.KEY_ALWAYS_BUILD, new Boolean( buildDef.isAlwaysBuild() ) );
                context.put( ContinuumBuildConstant.KEY_OLD_SCM_CHANGES, getOldScmChanges( project.getId(), buildDefinitionId ) );

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
            ProjectScmRoot scmRoot = projectScmRootDao.getProjectScmRootByProjectGroupAndScmRootAddress( task.getProjectGroupId(), task.getScmRootAddress() );

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
                    if ( latestBuildResult == null || ( latestBuildResult.getStartTime() >= startTime && latestBuildResult.getEndTime() > 0 && 
                           latestBuildResult.getEndTime() < endTime ) || latestBuildResult.getStartTime() < startTime )
                    {
                        BuildResult buildResult = new BuildResult();
                        buildResult.setBuildDefinition( buildDef );
                        buildResult.setError( error );
                        buildResult.setState( ContinuumProjectState.ERROR );
                        buildResult.setTrigger( task.getTrigger() );
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

    private List getOldScmChanges( int projectId, int buildDefinitionId )
        throws ContinuumStoreException
    {
        List scmChanges = new ArrayList();

        BuildResult oldBuildResult =
            buildResultDao.getLatestBuildResultForBuildDefinition( projectId, buildDefinitionId );

        if ( oldBuildResult != null )
        {
            ScmResult scmResult = getOldScmResults( projectId, oldBuildResult.getBuildNumber(), oldBuildResult.getEndTime() );

            scmChanges = getScmChanges( scmResult );
        }

        return scmChanges;
    }

    private List getScmChanges( ScmResult scmResult )
    {
        List scmChanges = new ArrayList();

        if ( scmResult != null && scmResult.getChanges() != null )
        {
            for ( Object obj : scmResult.getChanges() )
            {
                ChangeSet changeSet = (ChangeSet) obj; 

                Map map = new HashMap();
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

    private List<Map> getScmChangeFiles( List<ChangeFile> files )
    {
        List<Map> scmChangeFiles = new ArrayList<Map>();

        if ( files != null )
        {
            for ( ChangeFile changeFile : files )
            {
                Map map = new HashMap();

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
