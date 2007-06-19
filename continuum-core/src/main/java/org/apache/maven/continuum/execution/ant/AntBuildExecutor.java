package org.apache.maven.continuum.execution.ant;

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

import org.apache.maven.continuum.execution.AbstractBuildExecutor;
import org.apache.maven.continuum.execution.ContinuumBuildExecutionResult;
import org.apache.maven.continuum.execution.ContinuumBuildExecutor;
import org.apache.maven.continuum.execution.ContinuumBuildExecutorException;
import org.apache.maven.continuum.installation.InstallationService;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.system.Installation;
import org.apache.maven.continuum.model.system.Profile;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class AntBuildExecutor
    extends AbstractBuildExecutor
    implements ContinuumBuildExecutor
{
    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public static final String CONFIGURATION_EXECUTABLE = "executable";

    public static final String CONFIGURATION_TARGETS = "targets";

    public static final String ID = "ant";

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public AntBuildExecutor()
    {
        super( ID, true );
    }

    // ----------------------------------------------------------------------
    // ContinuumBuilder Implementation
    // ----------------------------------------------------------------------

    public ContinuumBuildExecutionResult build( Project project, BuildDefinition buildDefinition, File buildOutput )
        throws ContinuumBuildExecutorException
    {
        // TODO: get from installation
//        String executable = project.getExecutable();
        String executable = "ant";

        String arguments = "";

        String buildFile = buildDefinition.getBuildFile();

        if ( !StringUtils.isEmpty( buildFile ) )
        {
            arguments = "-f " + buildFile + " ";
        }

        arguments += StringUtils.clean( buildDefinition.getArguments() ) + " "
            + StringUtils.clean( buildDefinition.getGoals() );

        return executeShellCommand( project, executable, arguments, buildOutput, getEnvironments( buildDefinition ) );
    }

    protected Map<String, String> getEnvironments( BuildDefinition buildDefinition )
    {
        Profile profile = buildDefinition.getProfile();
        if ( profile == null )
        {
            return Collections.EMPTY_MAP;
        }
        Map<String, String> envVars = new HashMap<String, String>();
        String javaHome = getJavaHomeValue( buildDefinition );
        if ( !StringUtils.isEmpty( javaHome ) )
        {
            envVars.put( getInstallationService().getEnvVar( InstallationService.JDK_TYPE ), javaHome );
        }
        Installation builder = profile.getBuilder();
        if ( builder != null )
        {
            envVars.put( getInstallationService().getEnvVar( InstallationService.ANT_TYPE ), builder.getVarValue() );
        }
        envVars.putAll( getEnvironmentVariable( buildDefinition ) );
        return envVars;

    }

    public void updateProjectFromCheckOut( File workingDirectory, Project p, BuildDefinition buildDefinition )
        throws ContinuumBuildExecutorException
    {
    }
}
