package org.apache.continuum.buildagent.build.execution;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.continuum.buildagent.configuration.ConfigurationService;
import org.apache.continuum.buildagent.installation.InstallationService;
import org.apache.continuum.buildagent.utils.shell.ExecutionResult;
import org.apache.continuum.buildagent.utils.shell.ShellCommandHelper;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.codehaus.plexus.commandline.ExecutableResolver;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractBuildExecutor
    implements ContinuumAgentBuildExecutor, Initializable
{
    protected Logger log = LoggerFactory.getLogger( getClass() );

    /**
     * @plexus.requirement
     */
    private ShellCommandHelper shellCommandHelper;

    /**
     * @plexus.requirement
     */
    private ExecutableResolver executableResolver;

    /**
     * @plexus.requirement
     */
    private ConfigurationService configurationService;

    /**
     * @plexus.requirement
     */
    private InstallationService installationService;

    /**
     * @plexus.configuration
     */
    private String defaultExecutable;

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private String id;

    private boolean resolveExecutable;

 // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    protected AbstractBuildExecutor( String id, boolean resolveExecutable )
    {
        this.id = id;

        this.resolveExecutable = resolveExecutable;
    }

    public void setShellCommandHelper( ShellCommandHelper shellCommandHelper )
    {
        this.shellCommandHelper = shellCommandHelper;
    }

    public ShellCommandHelper getShellCommandHelper()
    {
        return shellCommandHelper;
    }

    public void setDefaultExecutable( String defaultExecutable )
    {
        this.defaultExecutable = defaultExecutable;
    }

    public ConfigurationService getConfigurationService()
    {
        return configurationService;
    }

    public void setConfigurationService( ConfigurationService configurationService )
    {
        this.configurationService = configurationService;
    }

    public InstallationService getInstallationService()
    {
        return installationService;
    }

    public void setInstallationService( InstallationService installationService )
    {
        this.installationService = installationService;
    }

    // ----------------------------------------------------------------------
    // Component Lifecycle
    // ----------------------------------------------------------------------

    public String getDefaultExecutable()
    {
        return defaultExecutable;
    }

    public void initialize()
        throws InitializationException
    {
        List path = executableResolver.getDefaultPath();

        if ( resolveExecutable )
        {
            if ( StringUtils.isEmpty( defaultExecutable ) )
            {
                log.warn( "The default executable for build executor '" + id + "' is not set. " +
                    "This will cause a problem unless the project has a executable configured." );
            }
            else
            {
                File resolvedExecutable = executableResolver.findExecutable( defaultExecutable, path );

                if ( resolvedExecutable == null )
                {
                    log.warn(
                        "Could not find the executable '" + defaultExecutable + "' in the " + "path '" + path + "'." );
                }
                else
                {
                    log.info( "Resolved the executable '" + defaultExecutable + "' to " + "'" +
                        resolvedExecutable.getAbsolutePath() + "'." );
                }
            }
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    /**
     * Find the actual executable path to be used
     *
     * @param defaultExecutable
     * @return The executable path
     */
    protected String findExecutable( Project project, String executable, String defaultExecutable,
                                     boolean resolveExecutable, File workingDirectory )
    {
        // ----------------------------------------------------------------------
        // If we're not searching the path for the executable, prefix the
        // executable with the working directory to make sure the path is
        // absolute and thus won't be tried resolved by using the PATH
        // ----------------------------------------------------------------------

        String actualExecutable;

        if ( !resolveExecutable )
        {
            actualExecutable = new File( workingDirectory, executable ).getAbsolutePath();
        }
        else
        {
            List<String> path = executableResolver.getDefaultPath();

            if ( StringUtils.isEmpty( executable ) )
            {
                executable = defaultExecutable;
            }

            File e = executableResolver.findExecutable( executable, path );

            if ( e == null )
            {
                log.warn( "Could not find the executable '" + executable + "' in this path: " );

                for ( String element : path )
                {
                    log.warn( element );
                }

                actualExecutable = defaultExecutable;
            }
            else
            {
                actualExecutable = e.getAbsolutePath();
            }
        }

        //sometimes executable isn't found in path but it exit (CONTINUUM-365)
        File actualExecutableFile = new File( actualExecutable );

        if ( !actualExecutableFile.exists() )
        {
            actualExecutable = executable;
        }

        return actualExecutable;
    }

    protected ContinuumAgentBuildExecutionResult executeShellCommand( Project project, String executable, String arguments,
                                                                 File output, Map<String, String> environments )
        throws ContinuumAgentBuildExecutorException, ContinuumAgentBuildCancelledException
    {

        File workingDirectory = getWorkingDirectory( project.getId() );

        String actualExecutable =
            findExecutable( project, executable, defaultExecutable, resolveExecutable, workingDirectory );

        // ----------------------------------------------------------------------
        // Execute the build
        // ----------------------------------------------------------------------

        try
        {
            ExecutionResult result = getShellCommandHelper().executeShellCommand( workingDirectory, actualExecutable,
                                                                                  arguments, output, project.getId(),
                                                                                  environments );

            log.info( "Exit code: " + result.getExitCode() );

            return new ContinuumAgentBuildExecutionResult( output, result.getExitCode() );
        }
        catch ( CommandLineException e )
        {
            if ( e.getCause() instanceof InterruptedException )
            {
                throw new ContinuumAgentBuildCancelledException( "The build was cancelled", e );
            }
            else
            {
                throw new ContinuumAgentBuildExecutorException(
                    "Error while executing shell command. The most common error is that '" + executable + "' " +
                        "is not in your path.", e );
            }
        }
        catch ( Exception e )
        {
            throw new ContinuumAgentBuildExecutorException( "Error while executing shell command. " +
                "The most common error is that '" + executable + "' " + "is not in your path.", e );
        }
    }

    protected Properties getContinuumSystemProperties( Project project )
    {
        Properties properties = new Properties();
        properties.setProperty( "continuum.project.group.name", project.getProjectGroup().getName() );
        properties.setProperty( "continuum.project.lastBuild.state", String.valueOf( project.getOldState() ) );
        properties.setProperty( "continuum.project.lastBuild.number", String.valueOf( project.getBuildNumber() ) );
        properties.setProperty( "continuum.project.nextBuild.number", String.valueOf( project.getBuildNumber() + 1 ) );
        properties.setProperty( "continuum.project.id", String.valueOf( project.getId() ) );
        properties.setProperty( "continuum.project.name", project.getName() );
        properties.setProperty( "continuum.project.version", project.getVersion() );
        return properties;
    }

    protected String getBuildFileForProject( Project project, BuildDefinition buildDefinition )
    {
        String buildFile = StringUtils.clean( buildDefinition.getBuildFile() );
        
        return buildFile;
    }

    public boolean isBuilding( Project project )
    {
        return project.getState() == ContinuumProjectState.BUILDING ||
            getShellCommandHelper().isRunning( project.getId() );
    }

    public void killProcess( Project project )
    {
        getShellCommandHelper().killProcess( project.getId() );
    }

    public List<Artifact> getDeployableArtifacts( Project project, File workingDirectory, BuildDefinition buildDefinition )
        throws ContinuumAgentBuildExecutorException
    {
        // Not supported by this builder
        return Collections.EMPTY_LIST;
    }

    public File getWorkingDirectory( int projectId )
    {
        return getConfigurationService().getWorkingDirectory( projectId );
    }

    public boolean isResolveExecutable()
    {
        return resolveExecutable;
    }

    public void setResolveExecutable( boolean resolveExecutable )
    {
        this.resolveExecutable = resolveExecutable;
    }

    public void setExecutableResolver( ExecutableResolver executableResolver )
    {
        this.executableResolver = executableResolver;
    }

    public ExecutableResolver getExecutableResolver()
    {
        return executableResolver;
    }
}
