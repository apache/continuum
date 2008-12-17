package org.apache.continuum.buildagent.build.execution.maven.m2;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

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
import org.codehaus.plexus.util.StringUtils;

public class MavenTwoBuildExecutor
    extends AbstractBuildExecutor
    implements ContinuumAgentBuildExecutor
{
    public static final String CONFIGURATION_GOALS = "goals";

    public static final String ID = ContinuumBuildExecutorConstants.MAVEN_TWO_BUILD_EXECUTOR;

    /**
     * @plexus.requirement
     */
    private MavenProjectHelper projectHelper;

    /**
     * @plexus.requirement
     */
    private BuildAgentMavenBuilderHelper builderHelper;

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

    public BuildAgentMavenBuilderHelper getBuilderHelper()
    {
        return builderHelper;
    }

    public void setBuilderHelper( BuildAgentMavenBuilderHelper builderHelper )
    {
        this.builderHelper = builderHelper;
    }

    public ContinuumAgentBuildExecutionResult build( Project project, BuildDefinition buildDefinition, File buildOutput )
        throws ContinuumAgentBuildExecutorException, ContinuumAgentBuildCancelledException
    {
        String executable = getBuildAgentInstallationService().getExecutorConfigurator( BuildAgentInstallationService.MAVEN2_TYPE )
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

        return executeShellCommand( project, executable, arguments.toString(), buildOutput, null );
    }

    @Override
    public List<Artifact> getDeployableArtifacts( Project continuumProject, File workingDirectory,
                                        BuildDefinition buildDefinition )
        throws ContinuumAgentBuildExecutorException
    {
        MavenProject project = getMavenProject( continuumProject, workingDirectory, buildDefinition );

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

    private MavenProject getMavenProject( Project continuumProject, File workingDirectory,
                                          BuildDefinition buildDefinition )
        throws ContinuumAgentBuildExecutorException
    {
        ContinuumProjectBuildingResult result = new ContinuumProjectBuildingResult();

        File f = getPomFile( getBuildFileForProject( continuumProject, buildDefinition ), workingDirectory );

        if ( !f.exists() )
        {
            throw new ContinuumAgentBuildExecutorException( "Could not find Maven project descriptor '" + f + "'." );
        }

        MavenProject project = builderHelper.getMavenProject( result, f );

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
