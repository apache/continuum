package org.apache.maven.continuum.execution.maven.m2;

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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.continuum.configuration.ConfigurationException;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.execution.AbstractBuildExecutor;
import org.apache.maven.continuum.execution.ContinuumBuildExecutionResult;
import org.apache.maven.continuum.execution.ContinuumBuildExecutor;
import org.apache.maven.continuum.execution.ContinuumBuildExecutorConstants;
import org.apache.maven.continuum.execution.ContinuumBuildExecutorException;
import org.apache.maven.continuum.installation.InstallationService;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.system.Installation;
import org.apache.maven.continuum.model.system.Profile;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.project.artifact.ProjectArtifactMetadata;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class MavenTwoBuildExecutor
    extends AbstractBuildExecutor
    implements ContinuumBuildExecutor
{
    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public static final String CONFIGURATION_GOALS = "goals";

    public static final String ID = ContinuumBuildExecutorConstants.MAVEN_TWO_BUILD_EXECUTOR;

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    /**
     * @plexus.requirement
     */
    private MavenBuilderHelper builderHelper;

    /**
     * @plexus.requirement
     */
    private MavenProjectHelper projectHelper;

    /**
     * @plexus.requirement
     */
    private ConfigurationService configurationService;

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public MavenTwoBuildExecutor()
    {
        super( ID, true );
    }

    public MavenBuilderHelper getBuilderHelper()
    {
        return builderHelper;
    }

    public void setBuilderHelper( MavenBuilderHelper builderHelper )
    {
        this.builderHelper = builderHelper;
    }

    public MavenProjectHelper getProjectHelper()
    {
        return projectHelper;
    }

    public void setProjectHelper( MavenProjectHelper projectHelper )
    {
        this.projectHelper = projectHelper;
    }

    public ConfigurationService getConfigurationService()
    {
        return configurationService;
    }

    public void setConfigurationService( ConfigurationService configurationService )
    {
        this.configurationService = configurationService;
    }

    // ----------------------------------------------------------------------
    // ContinuumBuilder Implementation
    // ----------------------------------------------------------------------

    public ContinuumBuildExecutionResult build( Project project, BuildDefinition buildDefinition, File buildOutput )
        throws ContinuumBuildExecutorException
    {
        String executable = getInstallationService().getExecutorConfigurator( InstallationService.MAVEN2_TYPE )
            .getExecutable();

        StringBuffer arguments = new StringBuffer();

        String buildFile = getBuildFileForProject( project, buildDefinition );

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

        arguments.append( StringUtils.clean( buildDefinition.getGoals() ) );

        Map<String, String> environments = getEnvironments( buildDefinition );
        String m2Home = environments.get( getInstallationService().getEnvVar( InstallationService.MAVEN2_TYPE ) );
        if ( StringUtils.isNotEmpty( m2Home ) )
        {
            executable = m2Home + File.separator + "bin" + File.separator + executable;
            setResolveExecutable( false );
        }

        return executeShellCommand( project, executable, arguments.toString(), buildOutput, environments );
    }

    public void updateProjectFromCheckOut( File workingDirectory, Project project, BuildDefinition buildDefinition )
        throws ContinuumBuildExecutorException
    {
        File f = getPomFile( getBuildFileForProject( project, buildDefinition ), workingDirectory );

        if ( !f.exists() )
        {
            throw new ContinuumBuildExecutorException( "Could not find Maven project descriptor." );
        }

        ContinuumProjectBuildingResult result = new ContinuumProjectBuildingResult();

        builderHelper.mapMetadataToProject( result, f, project );

        if ( result.hasErrors() )
        {
            throw new ContinuumBuildExecutorException( "Error while mapping metadata:" + result.getErrorsAsString() );
        }
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

    public List getDeployableArtifacts( Project continuumProject, File workingDirectory,
                                        BuildDefinition buildDefinition )
        throws ContinuumBuildExecutorException
    {
        File f = getPomFile( getBuildFileForProject( continuumProject, buildDefinition ), workingDirectory );

        if ( !f.exists() )
        {
            throw new ContinuumBuildExecutorException( "Could not find Maven project descriptor '" + f + "'." );
        }

        MavenProject project;

        ContinuumProjectBuildingResult result = new ContinuumProjectBuildingResult();

        project = builderHelper.getMavenProject( result, f );

        if ( result.hasErrors() )
        {
            throw new ContinuumBuildExecutorException(
                "Unable to read the Maven project descriptor '" + f + "': " + result.getErrorsAsString() );
        }

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

        List attachedArtifacts = project.getAttachedArtifacts();

        List artifacts = new ArrayList( attachedArtifacts.size() + 1 );

        if ( artifact.getFile().exists() )
        {
            artifacts.add( artifact );
        }

        for ( Iterator iterator = attachedArtifacts.iterator(); iterator.hasNext(); )
        {
            Artifact attachedArtifact = (Artifact) iterator.next();
            artifacts.add( attachedArtifact );
        }

        return artifacts;
    }

    public void backupTestFiles( Project project, int buildId )
    {
        File backupDirectory = null;
        try
        {
            backupDirectory = configurationService.getTestReportsDirectory( buildId, project.getId() );
            if ( !backupDirectory.exists() )
            {
                backupDirectory.mkdirs();
            }
        }
        catch ( ConfigurationException e )
        {
            getLogger().info( "error on surefire backup directory creation skip backup " + e.getMessage(), e );
        }
        backupTestFiles( getWorkingDirectory( project ), backupDirectory );
    }

    private void backupTestFiles( File workingDir, File backupDirectory )
    {
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir( workingDir );
        scanner.setIncludes(
            new String[]{"**/target/surefire-reports/TEST-*.xml", "**/target/surefire-it-reports/TEST-*.xml"} );
        scanner.scan();

        String[] testResultFiles = scanner.getIncludedFiles();
        if ( testResultFiles.length > 0 )
        {
            getLogger().info( "Backup surefire files." );
        }
        for ( String testResultFile : testResultFiles )
        {
            File xmlFile = new File( workingDir, testResultFile );
            try
            {
                if ( backupDirectory != null )
                {
                    FileUtils.copyFileToDirectory( xmlFile, backupDirectory );
                }
            }
            catch ( IOException e )
            {
                getLogger().info( "failed to backup unit report file " + xmlFile.getPath() );
            }
        }
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
            envVars.put( getInstallationService().getEnvVar( InstallationService.MAVEN2_TYPE ), builder.getVarValue() );
        }
        envVars.putAll( getEnvironmentVariables( buildDefinition ) );
        return envVars;

    }
}
