package org.apache.continuum.buildagent.build.execution.shell;

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

import java.io.File;
import java.util.Map;

import org.apache.continuum.buildagent.build.execution.AbstractBuildExecutor;
import org.apache.continuum.buildagent.build.execution.ContinuumAgentBuildCancelledException;
import org.apache.continuum.buildagent.build.execution.ContinuumAgentBuildExecutionResult;
import org.apache.continuum.buildagent.build.execution.ContinuumAgentBuildExecutor;
import org.apache.continuum.buildagent.build.execution.ContinuumAgentBuildExecutorException;
import org.apache.maven.continuum.execution.ContinuumBuildExecutorConstants;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Project;

public class ShellBuildExecutor
    extends AbstractBuildExecutor
    implements ContinuumAgentBuildExecutor
{
    public static final String CONFIGURATION_EXECUTABLE = "executable";

    public static final String ID = ContinuumBuildExecutorConstants.SHELL_BUILD_EXECUTOR;

    public ShellBuildExecutor()
    {
        super( ID, false );
    }

    public ContinuumAgentBuildExecutionResult build( Project project, BuildDefinition buildDefinition, 
                                                     File buildOutput, Map<String, String> environments,
                                                     String localRepository )
        throws ContinuumAgentBuildExecutorException, ContinuumAgentBuildCancelledException
    {
        String executable = getBuildFileForProject( project, buildDefinition );

        return executeShellCommand( project, executable, buildDefinition.getArguments(), buildOutput, environments );
    }

    public void updateProjectFromWorkingDirectory( File workingDirectory, Project project,
                                                   BuildDefinition buildDefinition )
        throws ContinuumAgentBuildExecutorException
    {
        // nothing to do here   
    }
}
