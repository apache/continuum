package org.apache.maven.continuum.core.action;

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

import org.apache.continuum.dao.BuildResultDao;
import org.apache.continuum.dao.ProjectDao;
import org.apache.continuum.utils.ContinuumUtils;
import org.apache.continuum.utils.build.BuildTrigger;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.execution.ContinuumBuildCancelledException;
import org.apache.maven.continuum.execution.ContinuumBuildExecutionResult;
import org.apache.maven.continuum.execution.ContinuumBuildExecutor;
import org.apache.maven.continuum.execution.manager.BuildExecutorManager;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectDependency;
import org.apache.maven.continuum.model.scm.ScmResult;
import org.apache.maven.continuum.notification.ContinuumNotificationDispatcher;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
@Component( role = org.codehaus.plexus.action.Action.class, hint = "execute-builder" )
public class ExecuteBuilderContinuumAction
    extends AbstractContinuumAction
{
    private static final String KEY_CANCELLED = "cancelled";

    @Requirement
    private ConfigurationService configurationService;

    @Requirement
    private BuildExecutorManager buildExecutorManager;

    @Requirement
    private BuildResultDao buildResultDao;

    @Requirement
    private ProjectDao projectDao;

    @Requirement
    private ContinuumNotificationDispatcher notifier;

    public void execute( Map context )
        throws Exception
    {
        // ----------------------------------------------------------------------
        // Get parameters from the context
        // ----------------------------------------------------------------------

        Project project = projectDao.getProject( getProject( context ).getId() );

        BuildDefinition buildDefinition = getBuildDefinition( context );

        BuildTrigger buildTrigger = getBuildTrigger( context );

        ScmResult scmResult = getScmResult( context );

        List<ProjectDependency> updatedDependencies = getUpdatedDependencies( context );

        ContinuumBuildExecutor buildExecutor = buildExecutorManager.getBuildExecutor( project.getExecutorId() );

        // ----------------------------------------------------------------------
        // Make the buildResult
        // ----------------------------------------------------------------------

        BuildResult buildResult = new BuildResult();

        buildResult.setStartTime( new Date().getTime() );

        buildResult.setState( ContinuumProjectState.BUILDING );

        buildResult.setTrigger( buildTrigger.getTrigger() );

        buildResult.setUsername( buildTrigger.getTriggeredBy() );

        buildResult.setScmResult( scmResult );

        buildResult.setModifiedDependencies( updatedDependencies );

        buildResult.setBuildDefinition( getBuildDefinition( context ) );

        buildResultDao.addBuildResult( project, buildResult );

        AbstractContinuumAction.setBuildId( context, Integer.toString( buildResult.getId() ) );

        setCancelled( context, false );

        buildResult = buildResultDao.getBuildResult( buildResult.getId() );

        String projectScmRootUrl = getProjectScmRootUrl( context, project.getScmUrl() );
        List<Project> projectsWithCommonScmRoot = getListOfProjectsInGroupWithCommonScmRoot( context );

        try
        {
            notifier.runningGoals( project, buildDefinition, buildResult );

            File buildOutputFile = configurationService.getBuildOutputFile( buildResult.getId(), project.getId() );

            ContinuumBuildExecutionResult result = buildExecutor.build( project, buildDefinition, buildOutputFile,
                                                                        projectsWithCommonScmRoot, projectScmRootUrl );

            buildResult.setState( result.getExitCode() == 0 ? ContinuumProjectState.OK : ContinuumProjectState.FAILED );

            buildResult.setExitCode( result.getExitCode() );
        }
        catch ( ContinuumBuildCancelledException e )
        {
            getLogger().info( "Cancelled build" );

            buildResult.setState( ContinuumProjectState.CANCELLED );

            setCancelled( context, true );
        }
        catch ( Throwable e )
        {
            getLogger().error( "Error running buildResult", e );

            buildResult.setState( ContinuumProjectState.ERROR );

            buildResult.setError( ContinuumUtils.throwableToString( e ) );
        }
        finally
        {
            project = projectDao.getProject( project.getId() );

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

            // ----------------------------------------------------------------------
            // Copy over the buildResult result
            // ----------------------------------------------------------------------

            buildResultDao.updateBuildResult( buildResult );

            buildResult = buildResultDao.getBuildResult( buildResult.getId() );

            notifier.goalsCompleted( project, buildDefinition, buildResult );

            AbstractContinuumAction.setProject( context, project );

            projectDao.updateProject( project );

            projectScmRootUrl = getProjectScmRootUrl( context, project.getScmUrl() );
            projectsWithCommonScmRoot = getListOfProjectsInGroupWithCommonScmRoot( context );

            // ----------------------------------------------------------------------
            // Backup test result files
            // ----------------------------------------------------------------------
            //TODO: Move as a plugin
            buildExecutor.backupTestFiles( project, buildResult.getId(), projectScmRootUrl, projectsWithCommonScmRoot );
        }
    }

    public static boolean isCancelled( Map<String, Object> context )
    {
        return getBoolean( context, KEY_CANCELLED );
    }

    private static void setCancelled( Map<String, Object> context, boolean cancelled )
    {
        context.put( KEY_CANCELLED, cancelled );
    }
}
