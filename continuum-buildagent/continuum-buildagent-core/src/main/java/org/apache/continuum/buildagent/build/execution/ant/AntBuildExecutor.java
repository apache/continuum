package org.apache.continuum.buildagent.build.execution.ant;

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

import org.apache.continuum.buildagent.build.execution.AbstractBuildExecutor;
import org.apache.continuum.buildagent.build.execution.ContinuumAgentBuildCancelledException;
import org.apache.continuum.buildagent.build.execution.ContinuumAgentBuildExecutionResult;
import org.apache.continuum.buildagent.build.execution.ContinuumAgentBuildExecutor;
import org.apache.continuum.buildagent.build.execution.ContinuumAgentBuildExecutorException;
import org.apache.continuum.buildagent.installation.BuildAgentInstallationService;
import org.apache.maven.continuum.execution.ContinuumBuildExecutorConstants;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Project;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

public class AntBuildExecutor
    extends AbstractBuildExecutor
    implements ContinuumAgentBuildExecutor
{
    public static final String CONFIGURATION_EXECUTABLE = "executable";

    public static final String CONFIGURATION_TARGETS = "targets";

    public static final String ID = ContinuumBuildExecutorConstants.ANT_BUILD_EXECUTOR;

    protected AntBuildExecutor()
    {
        super( ID, true );
    }

    public ContinuumAgentBuildExecutionResult build( Project project, BuildDefinition buildDefinition, File buildOutput,
                                                     Map<String, String> environments, String localRepository )
        throws ContinuumAgentBuildExecutorException, ContinuumAgentBuildCancelledException
    {
        String executable = getBuildAgentInstallationService().getExecutorConfigurator(
            BuildAgentInstallationService.ANT_TYPE ).getExecutable();

        StringBuffer arguments = new StringBuffer();

        String buildFile = getBuildFileForProject( buildDefinition );

        if ( !StringUtils.isEmpty( buildFile ) )
        {
            arguments.append( "-f " ).append( buildFile ).append( " " );
        }

        arguments.append( StringUtils.clean( buildDefinition.getArguments() ) ).append( " " );

        Properties props = getContinuumSystemProperties( project );
        for ( Enumeration itr = props.propertyNames(); itr.hasMoreElements(); )
        {
            String name = (String) itr.nextElement();
            String value = props.getProperty( name );
            arguments.append( "\"-D" ).append( name ).append( "=" ).append( value ).append( "\" " );
        }

        arguments.append( StringUtils.clean( buildDefinition.getGoals() ) );

        String antHome = null;

        if ( environments != null )
        {
            antHome = environments.get( getBuildAgentInstallationService().getEnvVar(
                BuildAgentInstallationService.ANT_TYPE ) );
        }

        if ( StringUtils.isNotEmpty( antHome ) )
        {
            executable = antHome + File.separator + "bin" + File.separator + executable;
            setResolveExecutable( false );
        }

        return executeShellCommand( project, executable, arguments.toString(), buildOutput, environments );
    }

    public void updateProjectFromWorkingDirectory( File workingDirectory, Project project,
                                                   BuildDefinition buildDefinition )
        throws ContinuumAgentBuildExecutorException
    {
        // nothing to do here
    }
}
