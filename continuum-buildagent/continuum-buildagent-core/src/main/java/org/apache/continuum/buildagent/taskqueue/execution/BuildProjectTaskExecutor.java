package org.apache.continuum.buildagent.taskqueue.execution;

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

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.continuum.buildagent.build.execution.ContinuumAgentBuildExecutor;
import org.apache.continuum.buildagent.build.execution.ContinuumAgentBuildExecutorException;
import org.apache.continuum.buildagent.build.execution.manager.BuildAgentBuildExecutorManager;
import org.apache.continuum.buildagent.buildcontext.BuildContext;
import org.apache.continuum.buildagent.buildcontext.manager.BuildContextManager;
import org.apache.continuum.buildagent.configuration.BuildAgentConfigurationException;
import org.apache.continuum.buildagent.configuration.BuildAgentConfigurationService;
import org.apache.continuum.buildagent.installation.BuildAgentInstallationService;
import org.apache.continuum.buildagent.manager.BuildAgentManager;
import org.apache.continuum.buildagent.model.Installation;
import org.apache.continuum.buildagent.model.LocalRepository;
import org.apache.continuum.buildagent.utils.BuildContextToBuildDefinition;
import org.apache.continuum.buildagent.utils.BuildContextToProject;
import org.apache.continuum.buildagent.utils.ContinuumBuildAgentUtil;
import org.apache.continuum.taskqueue.BuildProjectTask;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.execution.ContinuumBuildExecutorConstants;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.scm.ChangeFile;
import org.apache.maven.continuum.model.scm.ChangeSet;
import org.apache.maven.continuum.model.scm.ScmResult;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.project.MavenProject;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.codehaus.plexus.action.ActionManager;
import org.codehaus.plexus.action.ActionNotFoundException;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.taskqueue.Task;
import org.codehaus.plexus.taskqueue.execution.TaskExecutionException;
import org.codehaus.plexus.taskqueue.execution.TaskExecutor;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component( role = org.codehaus.plexus.taskqueue.execution.TaskExecutor.class, hint = "build-agent" )
public class BuildProjectTaskExecutor
    implements TaskExecutor
{
    private static final Logger log = LoggerFactory.getLogger( BuildProjectTaskExecutor.class );

    @Requirement
    private BuildContextManager buildContextManager;

    @Requirement
    private ActionManager actionManager;

    @Requirement
    private BuildAgentConfigurationService buildAgentConfigurationService;

    @Requirement
    private BuildAgentManager buildAgentManager;

    @Requirement
    private BuildAgentBuildExecutorManager buildAgentBuildExecutorManager;

    public void executeTask( Task task )
        throws TaskExecutionException
    {
        BuildProjectTask buildProjectTask = (BuildProjectTask) task;

        int projectId = buildProjectTask.getProjectId();

        try
        {
            log.info( "Initializing build (projectId={})", projectId );
            BuildContext context = buildContextManager.getBuildContext( projectId );
            initializeBuildContext( context );

            if ( !checkScmResult( context ) )
            {
                return;
            }

            log.info( "Checking if project '{}' should build", context.getProjectName() );
            if ( !shouldBuild( context ) )
            {
                return;
            }

            log.info( "Starting build of {}", context.getProjectName() );
            startBuild( context );

            try
            {
                try
                {
                    performAction( "update-project-from-agent-working-directory", context );
                }
                catch ( TaskExecutionException e )
                {
                    updateBuildResult( context, ContinuumBuildAgentUtil.throwableToString( e ) );

                    //just log the error but don't stop the build from progressing in order not to suppress any build result messages there
                    log.error( "Error executing action update-project-from-agent-working-directory '", e );
                }

                performAction( "execute-agent-builder", context );

                log.info( "Updating build result of project '{}'", context.getProjectName() );
                updateBuildResult( context, null );
            }
            finally
            {
                log.info( "End build of project '{}'", context.getProjectName() );
                endBuild( context );
            }
        }
        catch ( TaskExecutionException e )
        {
            log.error( "Error while trying to build the project {}: {}", projectId, e.getMessage() );
        }
    }

    private void initializeBuildContext( BuildContext buildContext )
        throws TaskExecutionException
    {
        Map<String, Object> actionContext = new HashMap<String, Object>();

        actionContext.put( ContinuumBuildAgentUtil.KEY_PROJECT_ID, buildContext.getProjectId() );

        Project project = BuildContextToProject.getProject( buildContext );
        ProjectGroup projectGroup = new ProjectGroup();
        projectGroup.setId( buildContext.getProjectGroupId() );
        projectGroup.setName( buildContext.getProjectGroupName() );
        project.setProjectGroup( projectGroup );

        actionContext.put( ContinuumBuildAgentUtil.KEY_PROJECT, project );
        actionContext.put( ContinuumBuildAgentUtil.KEY_BUILD_DEFINITION,
                           BuildContextToBuildDefinition.getBuildDefinition( buildContext ) );
        actionContext.put( ContinuumBuildAgentUtil.KEY_BUILD_DEFINITION_ID, buildContext.getBuildDefinitionId() );
        actionContext.put( ContinuumBuildAgentUtil.KEY_TRIGGER, buildContext.getTrigger() );
        actionContext.put( ContinuumBuildAgentUtil.KEY_USERNAME, buildContext.getUsername() );
        actionContext.put( ContinuumBuildAgentUtil.KEY_ENVIRONMENTS, getEnvironments(
            buildContext.getBuildDefinitionId(), getInstallationType( buildContext ) ) );

        // CONTINUUM-2391
        String localRepoName = buildContext.getLocalRepository();
        if ( localRepoName != null )
        {
            try
            {
                LocalRepository localRepo = buildAgentConfigurationService.getLocalRepositoryByName( localRepoName );
                actionContext.put( ContinuumBuildAgentUtil.KEY_LOCAL_REPOSITORY, localRepo.getLocation() );
            }
            catch ( BuildAgentConfigurationException e )
            {
                log.warn( "failed to initialize local repo", e );
            }
        }

        actionContext.put( ContinuumBuildAgentUtil.KEY_SCM_RESULT, buildContext.getScmResult() );
        buildContext.setActionContext( actionContext );

        buildContext.setBuildStartTime( System.currentTimeMillis() );
    }

    private boolean checkScmResult( BuildContext buildContext )
    {
        if ( buildContext.getScmResult() == null )
        {
            log.info( "Error updating from SCM, SCM result is null, not building" );
            return false;
        }
        else if ( !buildContext.getScmResult().isSuccess() )
        {
            log.info( "Error updating from SCM, SCM result has errors, not building" );
            return false;
        }
        return true;
    }

    private void startBuild( BuildContext buildContext )
        throws TaskExecutionException
    {
        try
        {
            buildAgentManager.startProjectBuild( buildContext.getProjectId(), buildContext.getBuildDefinitionId() );
        }
        catch ( ContinuumException e )
        {
            // do not throw exception, just log?
            log.error( "Failed to start project '" + buildContext.getProjectName() + "'", e );
            throw new TaskExecutionException( "Failed to start project '" + buildContext.getProjectName() + "'", e );
        }
    }

    private void endBuild( BuildContext buildContext )
        throws TaskExecutionException
    {
        // return build result to master
        BuildResult buildResult = buildContext.getBuildResult();

        Map<String, Object> result = new HashMap<String, Object>();
        result.put( ContinuumBuildAgentUtil.KEY_PROJECT_ID, buildContext.getProjectId() );
        result.put( ContinuumBuildAgentUtil.KEY_BUILD_DEFINITION_ID, buildContext.getBuildDefinitionId() );
        result.put( ContinuumBuildAgentUtil.KEY_TRIGGER, buildContext.getTrigger() );
        result.put( ContinuumBuildAgentUtil.KEY_USERNAME, buildContext.getUsername() );
        result.put( ContinuumBuildAgentUtil.KEY_BUILD_STATE, buildResult.getState() );
        result.put( ContinuumBuildAgentUtil.KEY_START_TIME, Long.toString( buildResult.getStartTime() ) );
        result.put( ContinuumBuildAgentUtil.KEY_END_TIME, Long.toString( buildResult.getEndTime() ) );
        result.put( ContinuumBuildAgentUtil.KEY_BUILD_EXIT_CODE, buildResult.getExitCode() );
        if ( buildContext.getLatestUpdateDate() != null )
        {
            result.put( ContinuumBuildAgentUtil.KEY_LATEST_UPDATE_DATE, buildContext.getLatestUpdateDate() );
        }

        String buildOutput = getBuildOutputText( buildContext.getProjectId() );
        if ( buildOutput == null )
        {
            result.put( ContinuumBuildAgentUtil.KEY_BUILD_OUTPUT, "" );
        }
        else
        {
            result.put( ContinuumBuildAgentUtil.KEY_BUILD_OUTPUT, buildOutput );
        }

        if ( buildResult.getError() != null )
        {
            result.put( ContinuumBuildAgentUtil.KEY_BUILD_ERROR, buildResult.getError() );
        }
        else
        {
            result.put( ContinuumBuildAgentUtil.KEY_BUILD_ERROR, "" );
        }

        result.put( ContinuumBuildAgentUtil.KEY_SCM_RESULT, ContinuumBuildAgentUtil.createScmResult( buildContext ) );
        result.put( ContinuumBuildAgentUtil.KEY_BUILD_AGENT_URL, buildContext.getBuildAgentUrl() );

        try
        {
            buildAgentManager.returnBuildResult( result );
            buildContextManager.removeBuildContext( buildContext.getProjectId() );
        }
        catch ( ContinuumException e )
        {
            log.error( "Failed to return build result for project '" + buildContext.getProjectName() + "'", e );
            throw new TaskExecutionException(
                "Failed to return build result for project '" + buildContext.getProjectName() + "'", e );
        }
    }

    private void performAction( String actionName, BuildContext context )
        throws TaskExecutionException
    {
        String error;
        TaskExecutionException exception;

        try
        {
            log.info( "Performing action " + actionName );
            actionManager.lookup( actionName ).execute( context.getActionContext() );
            return;
        }
        catch ( ActionNotFoundException e )
        {
            error = ContinuumBuildAgentUtil.throwableToString( e );
            exception = new TaskExecutionException( "Error looking up action '" + actionName + "'", e );
        }
        catch ( ScmRepositoryException e )
        {
            error = getValidationMessages( e ) + "\n" + ContinuumBuildAgentUtil.throwableToString( e );

            exception = new TaskExecutionException( "SCM error while executing '" + actionName + "'", e );
        }
        catch ( ScmException e )
        {
            error = ContinuumBuildAgentUtil.throwableToString( e );

            exception = new TaskExecutionException( "SCM error while executing '" + actionName + "'", e );
        }
        catch ( Exception e )
        {
            exception = new TaskExecutionException( "Error executing action '" + actionName + "'", e );
            error = ContinuumBuildAgentUtil.throwableToString( exception );
        }

        updateBuildResult( context, error );

        throw exception;
    }

    private void updateBuildResult( BuildContext context, String error )
    {
        context.setBuildResult( ContinuumBuildAgentUtil.getBuildResult( context.getActionContext(), null ) );

        if ( context.getBuildResult() == null )
        {
            BuildResult build = new BuildResult();

            build.setState( ContinuumProjectState.ERROR );

            build.setTrigger( context.getTrigger() );

            build.setUsername( context.getUsername() );

            build.setStartTime( context.getBuildStartTime() );

            build.setEndTime( System.currentTimeMillis() );

            build.setBuildDefinition( BuildContextToBuildDefinition.getBuildDefinition( context ) );

            build.setScmResult( context.getScmResult() );

            if ( error != null )
            {
                build.setError( error );
            }

            context.setBuildResult( build );
        }
    }

    private String getValidationMessages( ScmRepositoryException ex )
    {
        List<String> messages = ex.getValidationMessages();

        StringBuffer message = new StringBuffer();

        if ( messages != null && !messages.isEmpty() )
        {
            for ( Iterator<String> i = messages.iterator(); i.hasNext(); )
            {
                message.append( i.next() );

                if ( i.hasNext() )
                {
                    message.append( System.getProperty( "line.separator" ) );
                }
            }
        }
        return message.toString();
    }

    private String getBuildOutputText( int projectId )
    {
        try
        {
            File buildOutputFile = buildAgentConfigurationService.getBuildOutputFile( projectId );

            if ( buildOutputFile.exists() )
            {
                return StringEscapeUtils.escapeHtml( FileUtils.fileRead( buildOutputFile ) );
            }
        }
        catch ( Exception e )
        {
            // do not throw exception, just log it
            log.error( "Error retrieving build output file", e );
        }

        return null;
    }

    private Map<String, String> getEnvironments( int buildDefinitionId, String installationType )
        throws TaskExecutionException
    {
        try
        {
            // get environments from Master (Continuum)
            Map<String, String> environments = buildAgentManager.getEnvironments( buildDefinitionId, installationType );

            List<Installation> installations = buildAgentConfigurationService.getAvailableInstallations();

            if ( installations != null )
            {
                // get environments from Slave (Build Agent)
                for ( Installation installation : installations )
                {
                    // combine environments (Master and Slave); Slave's environments overwrite Master's environments
                    environments.put( installation.getVarName(), installation.getVarValue() );
                }
            }

            return environments;
        }
        catch ( ContinuumException e )
        {
            log.error( "Error while retrieving environments of build definition: " + buildDefinitionId, e );
            throw new TaskExecutionException(
                "Error while retrieving environments of build definition: " + buildDefinitionId, e );
        }
    }

    private String getInstallationType( BuildContext buildContext )
    {
        String executorId = buildContext.getExecutorId();

        if ( ContinuumBuildExecutorConstants.MAVEN_TWO_BUILD_EXECUTOR.equals( executorId ) )
        {
            return BuildAgentInstallationService.MAVEN2_TYPE;
        }
        else if ( ContinuumBuildExecutorConstants.MAVEN_ONE_BUILD_EXECUTOR.equals( executorId ) )
        {
            return BuildAgentInstallationService.MAVEN1_TYPE;
        }
        else if ( ContinuumBuildExecutorConstants.ANT_BUILD_EXECUTOR.equals( executorId ) )
        {
            return BuildAgentInstallationService.ANT_TYPE;
        }

        return "";
    }

    private boolean shouldBuild( BuildContext context )
        throws TaskExecutionException
    {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put( ContinuumBuildAgentUtil.KEY_PROJECT_ID, context.getProjectId() );
        map.put( ContinuumBuildAgentUtil.KEY_BUILD_DEFINITION_ID, context.getBuildDefinitionId() );
        map.put( ContinuumBuildAgentUtil.KEY_TRIGGER, context.getTrigger() );
        map.put( ContinuumBuildAgentUtil.KEY_USERNAME, context.getUsername() );
        map.put( ContinuumBuildAgentUtil.KEY_SCM_CHANGES, getScmChanges( context.getScmResult() ) );
        map.put( ContinuumBuildAgentUtil.KEY_BUILD_AGENT_URL, context.getBuildAgentUrl() );

        if ( context.getExecutorId().equals( ContinuumBuildExecutorConstants.MAVEN_TWO_BUILD_EXECUTOR ) )
        {
            map.put( ContinuumBuildAgentUtil.KEY_MAVEN_PROJECT, getMavenProject( context ) );
        }

        if ( context.getLatestUpdateDate() != null )
        {
            map.put( ContinuumBuildAgentUtil.KEY_LATEST_UPDATE_DATE, context.getLatestUpdateDate() );
        }

        try
        {
            return buildAgentManager.shouldBuild( map );
        }
        catch ( ContinuumException e )
        {
            log.error( "Failed to determine if project should build", e );
            throw new TaskExecutionException( "Failed to determine if project should build", e );
        }
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
                    map.put( ContinuumBuildAgentUtil.KEY_CHANGESET_AUTHOR, changeSet.getAuthor() );
                }
                else
                {
                    map.put( ContinuumBuildAgentUtil.KEY_CHANGESET_AUTHOR, "" );
                }
                if ( StringUtils.isNotEmpty( changeSet.getComment() ) )
                {
                    map.put( ContinuumBuildAgentUtil.KEY_CHANGESET_COMMENT, changeSet.getComment() );
                }
                else
                {
                    map.put( ContinuumBuildAgentUtil.KEY_CHANGESET_COMMENT, "" );
                }
                if ( changeSet.getDateAsDate() != null )
                {
                    map.put( ContinuumBuildAgentUtil.KEY_CHANGESET_DATE, changeSet.getDateAsDate() );
                }
                map.put( ContinuumBuildAgentUtil.KEY_CHANGESET_FILES, getScmChangeFiles( changeSet.getFiles() ) );
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
                    map.put( ContinuumBuildAgentUtil.KEY_CHANGEFILE_NAME, changeFile.getName() );
                }
                else
                {
                    map.put( ContinuumBuildAgentUtil.KEY_CHANGEFILE_NAME, "" );
                }
                if ( StringUtils.isNotEmpty( changeFile.getRevision() ) )
                {
                    map.put( ContinuumBuildAgentUtil.KEY_CHANGEFILE_REVISION, changeFile.getRevision() );
                }
                else
                {
                    map.put( ContinuumBuildAgentUtil.KEY_CHANGEFILE_REVISION, "" );
                }
                if ( StringUtils.isNotEmpty( changeFile.getStatus() ) )
                {
                    map.put( ContinuumBuildAgentUtil.KEY_CHANGEFILE_STATUS, changeFile.getStatus() );
                }
                else
                {
                    map.put( ContinuumBuildAgentUtil.KEY_CHANGEFILE_STATUS, "" );
                }
                scmChangeFiles.add( map );
            }
        }
        return scmChangeFiles;
    }

    private Map getMavenProject( BuildContext context )
        throws TaskExecutionException
    {
        Map<String, Object> mavenProject = new HashMap<String, Object>();

        try
        {
            ContinuumAgentBuildExecutor buildExecutor = buildAgentBuildExecutorManager.getBuildExecutor(
                context.getExecutorId() );

            BuildDefinition buildDefinition = BuildContextToBuildDefinition.getBuildDefinition( context );

            File workingDirectory = buildAgentConfigurationService.getWorkingDirectory( context.getProjectId() );

            MavenProject project = buildExecutor.getMavenProject( workingDirectory, buildDefinition );

            mavenProject.put( ContinuumBuildAgentUtil.KEY_PROJECT_VERSION, project.getVersion() );

            if ( project.getModules() != null )
            {
                mavenProject.put( ContinuumBuildAgentUtil.KEY_PROJECT_MODULES, project.getModules() );
            }
        }
        catch ( ContinuumAgentBuildExecutorException e )
        {
            log.error( "Error getting maven project", e );
        }
        catch ( ContinuumException e )
        {
            log.error( "Error getting build executor", e );
        }

        return mavenProject;
    }

    public void setBuildContextManager( BuildContextManager buildContextManager )
    {
        this.buildContextManager = buildContextManager;
    }

    public void setActionManager( ActionManager actionManager )
    {
        this.actionManager = actionManager;
    }

    public void setBuildAgentConfigurationService( BuildAgentConfigurationService buildAgentConfigurationService )
    {
        this.buildAgentConfigurationService = buildAgentConfigurationService;
    }

    public void setBuildAgentManager( BuildAgentManager buildAgentManager )
    {
        this.buildAgentManager = buildAgentManager;
    }

    public void setBuildAgentBuildExecutorManager( BuildAgentBuildExecutorManager buildAgentBuildExecutorManager )
    {
        this.buildAgentBuildExecutorManager = buildAgentBuildExecutorManager;
    }
}
