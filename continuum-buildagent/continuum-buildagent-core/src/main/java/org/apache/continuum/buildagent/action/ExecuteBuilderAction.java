package org.apache.continuum.buildagent.action;

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

import org.apache.continuum.buildagent.build.execution.ContinuumAgentBuildCancelledException;
import org.apache.continuum.buildagent.build.execution.ContinuumAgentBuildExecutionResult;
import org.apache.continuum.buildagent.build.execution.ContinuumAgentBuildExecutor;
import org.apache.continuum.buildagent.build.execution.manager.BuildAgentBuildExecutorManager;
import org.apache.continuum.buildagent.configuration.BuildAgentConfigurationService;
import org.apache.continuum.buildagent.utils.ContinuumBuildAgentUtil;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.codehaus.plexus.action.AbstractAction;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import java.io.File;
import java.util.Date;
import java.util.Map;

@Component( role = org.codehaus.plexus.action.Action.class, hint = "execute-agent-builder" )
public class ExecuteBuilderAction
    extends AbstractAction
{

    @Requirement
    private BuildAgentBuildExecutorManager buildAgentBuildExecutorManager;

    @Requirement
    private BuildAgentConfigurationService buildAgentConfigurationService;

    public void execute( Map context )
        throws Exception
    {
        // ----------------------------------------------------------------------
        // Get parameters from the context
        // ----------------------------------------------------------------------

        Project project = ContinuumBuildAgentUtil.getProject( context );

        BuildDefinition buildDefinition = ContinuumBuildAgentUtil.getBuildDefinition( context );

        Map<String, String> environments = ContinuumBuildAgentUtil.getEnvironments( context );

        String localRepository = ContinuumBuildAgentUtil.getLocalRepository( context );

        int trigger = ContinuumBuildAgentUtil.getTrigger( context );

        String username = ContinuumBuildAgentUtil.getUsername( context );

        ContinuumAgentBuildExecutor buildExecutor = buildAgentBuildExecutorManager.getBuildExecutor(
            project.getExecutorId() );

        // ----------------------------------------------------------------------
        // Make the buildResult
        // ----------------------------------------------------------------------

        BuildResult buildResult = new BuildResult();

        buildResult.setStartTime( new Date().getTime() );

        buildResult.setState( ContinuumProjectState.BUILDING );

        buildResult.setTrigger( trigger );

        buildResult.setUsername( username );

        buildResult.setBuildDefinition( buildDefinition );

        buildResult.setScmResult( ContinuumBuildAgentUtil.getScmResult( context, null ) );

        context.put( ContinuumBuildAgentUtil.KEY_BUILD_RESULT, buildResult );

        try
        {
            File buildOutputFile = buildAgentConfigurationService.getBuildOutputFile( project.getId() );

            getLogger().debug( "Start building of project " + project.getId() );
            ContinuumAgentBuildExecutionResult result = buildExecutor.build( project, buildDefinition, buildOutputFile,
                                                                             environments, localRepository );

            buildResult.setState( result.getExitCode() == 0 ? ContinuumProjectState.OK : ContinuumProjectState.FAILED );

            buildResult.setExitCode( result.getExitCode() );
        }
        catch ( ContinuumAgentBuildCancelledException e )
        {
            getLogger().info( "Cancelled build" );

            buildResult.setState( ContinuumProjectState.CANCELLED );
        }
        catch ( Throwable e )
        {
            getLogger().error( "Error running buildResult", e );

            buildResult.setState( ContinuumProjectState.ERROR );

            buildResult.setError( ContinuumBuildAgentUtil.throwableToString( e ) );
        }
        finally
        {
            buildResult.setEndTime( new Date().getTime() );

            if ( buildResult.getState() != ContinuumProjectState.OK &&
                buildResult.getState() != ContinuumProjectState.FAILED &&
                buildResult.getState() != ContinuumProjectState.ERROR &&
                buildResult.getState() != ContinuumProjectState.CANCELLED )
            {
                buildResult.setState( ContinuumProjectState.ERROR );
            }

            context.put( ContinuumBuildAgentUtil.KEY_BUILD_RESULT, buildResult );
        }
    }
}