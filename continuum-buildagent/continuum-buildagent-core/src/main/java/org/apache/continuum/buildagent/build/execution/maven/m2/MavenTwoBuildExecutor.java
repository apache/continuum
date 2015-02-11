package org.apache.continuum.buildagent.build.execution.maven.m2;

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
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.continuum.execution.ContinuumBuildExecutorConstants;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.project.artifact.ProjectArtifactMetadata;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class MavenTwoBuildExecutor
    extends AbstractBuildExecutor
    implements ContinuumAgentBuildExecutor
{
    public static final String CONFIGURATION_GOALS = "goals";

    public static final String ID = ContinuumBuildExecutorConstants.MAVEN_TWO_BUILD_EXECUTOR;

    @Requirement
    private MavenProjectHelper projectHelper;

    @Requirement
    private BuildAgentMavenBuilderHelper buildAgentMavenBuilderHelper;

    public MavenTwoBuildExecutor()
    {
        super( ID, true );
    }

    public MavenProjectHelper getProjectHelper()
    {
        return projectHelper;
    }

    public void setProjectHelper( MavenProjectHelper projectHelper )
    {
        this.projectHelper = projectHelper;
    }

    public BuildAgentMavenBuilderHelper getBuildAgentMavenBuilderHelper()
    {
        return buildAgentMavenBuilderHelper;
    }

    public void setBuildAgentMavenBuilderHelper( BuildAgentMavenBuilderHelper builderHelper )
    {
        this.buildAgentMavenBuilderHelper = builderHelper;
    }

    public ContinuumAgentBuildExecutionResult build( Project project, BuildDefinition buildDefinition, File buildOutput,
                                                     Map<String, String> environments, String localRepository )
        throws ContinuumAgentBuildExecutorException, ContinuumAgentBuildCancelledException
    {
        String executable = getBuildAgentInstallationService().getExecutorConfigurator(
            BuildAgentInstallationService.MAVEN2_TYPE ).getExecutable();

        StringBuffer arguments = new StringBuffer();

        String buildFile = getBuildFileForProject( buildDefinition );

        if ( !StringUtils.isEmpty( buildFile ) && !"pom.xml".equals( buildFile ) )
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

        if ( StringUtils.isNotEmpty( localRepository ) )
        {
            arguments.append( "\"-Dmaven.repo.local=" ).append( StringUtils.clean( localRepository ) ).append( "\" " );
        }

        arguments.append( StringUtils.clean( buildDefinition.getGoals() ) );

        String m2Home = null;

        if ( environments != null )
        {
            m2Home = environments.get( getBuildAgentInstallationService().getEnvVar(
                BuildAgentInstallationService.MAVEN2_TYPE ) );
        }

        if ( StringUtils.isNotEmpty( m2Home ) )
        {
            executable = m2Home + File.separator + "bin" + File.separator + executable;
            setResolveExecutable( false );
        }

        return executeShellCommand( project, executable, arguments.toString(), buildOutput, environments );
    }

    @Override
    public List<Artifact> getDeployableArtifacts( Project continuumProject, File workingDirectory,
                                                  BuildDefinition buildDefinition )
        throws ContinuumAgentBuildExecutorException
    {
        MavenProject project = getMavenProject( workingDirectory, buildDefinition );

        // Maven could help us out a lot more here by knowing how to get the deployment artifacts from a project.
        // TODO: this is currently quite lame

        Artifact artifact = project.getArtifact();

        String projectPackaging = project.getPackaging();

        boolean isPomArtifact = "pom".equals( projectPackaging );

        if ( isPomArtifact )
        {
            artifact.setFile( project.getFile() );
        }
        else
        {
            // Attach pom
            ArtifactMetadata metadata = new ProjectArtifactMetadata( artifact, project.getFile() );

            artifact.addMetadata( metadata );

            String finalName = project.getBuild().getFinalName();

            String filename = finalName + "." + artifact.getArtifactHandler().getExtension();

            String buildDirectory = project.getBuild().getDirectory();

            File artifactFile = new File( buildDirectory, filename );

            artifact.setFile( artifactFile );

            // sources jar
            File sourcesFile = new File( buildDirectory, finalName + "-sources.jar" );

            if ( sourcesFile.exists() )
            {
                projectHelper.attachArtifact( project, "java-source", "sources", sourcesFile );
            }

            // tests sources jar
            File testsSourcesFile = new File( buildDirectory, finalName + "-test-sources.jar" );

            if ( testsSourcesFile.exists() )
            {
                projectHelper.attachArtifact( project, "java-source", "test-sources", testsSourcesFile );
            }

            // javadoc jar
            File javadocFile = new File( buildDirectory, finalName + "-javadoc.jar" );

            if ( javadocFile.exists() )
            {
                projectHelper.attachArtifact( project, "javadoc", "javadoc", javadocFile );
            }

            // client jar
            File clientFile = new File( buildDirectory, finalName + "-client.jar" );

            if ( clientFile.exists() )
            {
                projectHelper.attachArtifact( project, projectPackaging + "-client", "client", clientFile );
            }

            // Tests jar
            File testsFile = new File( buildDirectory, finalName + "-tests.jar" );

            if ( testsFile.exists() )
            {
                projectHelper.attachArtifact( project, "jar", "tests", testsFile );
            }
        }

        List<Artifact> attachedArtifacts = project.getAttachedArtifacts();

        List<Artifact> artifacts = new ArrayList<Artifact>( attachedArtifacts.size() + 1 );

        if ( artifact.getFile().exists() )
        {
            artifacts.add( artifact );
        }

        for ( Artifact attachedArtifact : attachedArtifacts )
        {
            artifacts.add( attachedArtifact );
        }

        return artifacts;
    }

    public void updateProjectFromWorkingDirectory( File workingDirectory, Project project,
                                                   BuildDefinition buildDefinition )
        throws ContinuumAgentBuildExecutorException
    {
        File f = getPomFile( getBuildFileForProject( buildDefinition ), workingDirectory );

        if ( !f.exists() )
        {
            throw new ContinuumAgentBuildExecutorException( "Could not find Maven project descriptor." );
        }

        ContinuumProjectBuildingResult result = new ContinuumProjectBuildingResult();

        buildAgentMavenBuilderHelper.mapMetadataToProject( result, f, project );

        if ( result.hasErrors() )
        {
            throw new ContinuumAgentBuildExecutorException(
                "Error while mapping metadata:" + result.getErrorsAsString() );
        }
        else
        {
            updateProject( project );
        }
    }

    @Override
    public MavenProject getMavenProject( File workingDirectory, BuildDefinition buildDefinition )
        throws ContinuumAgentBuildExecutorException
    {
        ContinuumProjectBuildingResult result = new ContinuumProjectBuildingResult();

        File f = getPomFile( getBuildFileForProject( buildDefinition ), workingDirectory );

        if ( !f.exists() )
        {
            throw new ContinuumAgentBuildExecutorException( "Could not find Maven project descriptor '" + f + "'." );
        }

        MavenProject project = buildAgentMavenBuilderHelper.getMavenProject( result, f );

        if ( result.hasErrors() )
        {
            throw new ContinuumAgentBuildExecutorException(
                "Unable to read the Maven project descriptor '" + f + "': " + result.getErrorsAsString() );
        }
        return project;
    }

    private static File getPomFile( String projectBuildFile, File workingDirectory )
    {
        File f = null;

        String buildFile = StringUtils.clean( projectBuildFile );

        if ( !StringUtils.isEmpty( buildFile ) )
        {
            f = new File( workingDirectory, buildFile );
        }

        if ( f == null )
        {
            f = new File( workingDirectory, "pom.xml" );
        }

        return f;
    }

}
