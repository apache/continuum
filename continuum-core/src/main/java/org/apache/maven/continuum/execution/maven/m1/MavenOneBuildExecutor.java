package org.apache.maven.continuum.execution.maven.m1;

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
public class MavenOneBuildExecutor
    extends AbstractBuildExecutor
    implements ContinuumBuildExecutor
{
    public final static String CONFIGURATION_GOALS = "goals";

    public final static String ID = "maven-1";

    /**
     * @plexus.requirement
     */
    private MavenOneMetadataHelper metadataHelper;

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public MavenOneBuildExecutor()
    {
        super( ID, true );
    }

    // ----------------------------------------------------------------------
    // Builder Implementation
    // ----------------------------------------------------------------------

    public ContinuumBuildExecutionResult build( Project project, BuildDefinition buildDefinition, File buildOutput )
        throws ContinuumBuildExecutorException
    {
        String executable = getInstallationService().getExecutorConfigurator( InstallationService.MAVEN1_TYPE )
            .getExecutable();

        String arguments = "";

        String buildFile = StringUtils.clean( buildDefinition.getBuildFile() );

        if ( !StringUtils.isEmpty( buildFile ) && !"project.xml".equals( buildFile ) )
        {
            arguments = "-p " + buildFile + " ";
        }

        arguments +=
            StringUtils.clean( buildDefinition.getArguments() ) + " " + StringUtils.clean( buildDefinition.getGoals() );

        Map<String, String> environments = getEnvironments( buildDefinition );
        String m1Home = environments.get( getInstallationService().getEnvVar( InstallationService.MAVEN1_TYPE ) );
        if ( StringUtils.isNotEmpty( m1Home ) )
        {
            executable = m1Home + File.separator + "bin" + File.separator + executable;
            setResolveExecutable( false );
        }

        return executeShellCommand( project, executable, arguments, buildOutput, environments );
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
            envVars.put( getInstallationService().getEnvVar( InstallationService.MAVEN1_TYPE ), builder.getVarValue() );
        }
        envVars.putAll( getEnvironmentVariables( buildDefinition ) );
        return envVars;

    }

    public void updateProjectFromCheckOut( File workingDirectory, Project project, BuildDefinition buildDefinition )
        throws ContinuumBuildExecutorException
    {
        File projectXmlFile = null;

        if ( buildDefinition != null )
        {
            String buildFile = StringUtils.clean( buildDefinition.getBuildFile() );

            if ( !StringUtils.isEmpty( buildFile ) )
            {
                projectXmlFile = new File( workingDirectory, buildFile );
            }
        }

        if ( projectXmlFile == null )
        {
            projectXmlFile = new File( workingDirectory, "project.xml" );
        }

        if ( !projectXmlFile.exists() )
        {
            throw new ContinuumBuildExecutorException( "Could not find Maven project descriptor." );
        }

        try
        {
            metadataHelper.mapMetadata( projectXmlFile, project );
        }
        catch ( MavenOneMetadataHelperException e )
        {
            throw new ContinuumBuildExecutorException( "Error while mapping metadata.", e );
        }
    }
}
