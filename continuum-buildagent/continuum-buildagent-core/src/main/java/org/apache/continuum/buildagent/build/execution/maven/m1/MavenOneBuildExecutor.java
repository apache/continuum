package org.apache.continuum.buildagent.build.execution.maven.m1;

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
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import javax.annotation.Resource;

public class MavenOneBuildExecutor
    extends AbstractBuildExecutor
    implements ContinuumAgentBuildExecutor
{
    public final static String CONFIGURATION_GOALS = "goals";

    public final static String ID = ContinuumBuildExecutorConstants.MAVEN_ONE_BUILD_EXECUTOR;

    @Resource
    private BuildAgentMavenOneMetadataHelper buildAgentMavenOneMetadataHelper;

    public MavenOneBuildExecutor()
    {
        super( ID, true );
    }

    public ContinuumAgentBuildExecutionResult build( Project project, BuildDefinition buildDefinition, File buildOutput,
                                                     Map<String, String> environments, String localRepository )
        throws ContinuumAgentBuildExecutorException, ContinuumAgentBuildCancelledException
    {
        String executable = getBuildAgentInstallationService().getExecutorConfigurator(
            BuildAgentInstallationService.MAVEN1_TYPE ).getExecutable();

        StringBuffer arguments = new StringBuffer();

        String buildFile = getBuildFileForProject( buildDefinition );

        if ( !StringUtils.isEmpty( buildFile ) && !"project.xml".equals( buildFile ) )
        {
            arguments.append( "-p " ).append( buildFile ).append( " " );
        }

        arguments.append( StringUtils.clean( buildDefinition.getArguments() ) ).append( " " );

        Properties props = getContinuumSystemProperties( project );
        for ( Enumeration itr = props.propertyNames(); itr.hasMoreElements(); )
        {
            String name = (String) itr.nextElement();
            String value = props.getProperty( name );
            arguments.append( "\"-D" ).append( name ).append( "=" ).append( value ).append( "\" " );
        }

        if ( StringUtils.isNotEmpty( localRepository ) )
        {
            arguments.append( "\"-Dmaven.repo.local=" ).append( StringUtils.clean( localRepository ) ).append( "\" " );
        }

        arguments.append( StringUtils.clean( buildDefinition.getGoals() ) );

        String m1Home = null;

        if ( environments != null )
        {
            m1Home = environments.get( getBuildAgentInstallationService().getEnvVar(
                BuildAgentInstallationService.MAVEN1_TYPE ) );
        }

        if ( StringUtils.isNotEmpty( m1Home ) )
        {
            executable = m1Home + File.separator + "bin" + File.separator + executable;
            setResolveExecutable( false );
        }

        return executeShellCommand( project, executable, arguments.toString(), buildOutput, environments );
    }

    public void updateProjectFromWorkingDirectory( File workingDirectory, Project project,
                                                   BuildDefinition buildDefinition )
        throws ContinuumAgentBuildExecutorException
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
            throw new ContinuumAgentBuildExecutorException( "Could not find Maven project descriptor." );
        }

        try
        {
            ContinuumProjectBuildingResult result = new ContinuumProjectBuildingResult();

            buildAgentMavenOneMetadataHelper.mapMetadata( result, projectXmlFile, project );

            if ( result.hasErrors() )
            {
                throw new ContinuumAgentBuildExecutorException(
                    "Error while mapping metadata:" + result.getErrorsAsString() );
            }

            updateProject( project );
        }
        catch ( BuildAgentMavenOneMetadataHelperException e )
        {
            throw new ContinuumAgentBuildExecutorException( "Error while mapping metadata.", e );
        }
    }
}
