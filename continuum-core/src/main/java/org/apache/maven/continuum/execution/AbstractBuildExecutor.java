package org.apache.maven.continuum.execution;

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

import org.apache.maven.continuum.installation.InstallationService;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.scm.TestResult;
import org.apache.maven.continuum.model.system.Installation;
import org.apache.maven.continuum.model.system.Profile;
import org.apache.maven.continuum.utils.WorkingDirectoryService;
import org.apache.maven.continuum.utils.shell.ExecutionResult;
import org.apache.maven.continuum.utils.shell.ShellCommandHelper;
import org.codehaus.plexus.commandline.ExecutableResolver;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Collections;
import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AbstractBuildExecutor
    extends AbstractLogEnabled
    implements ContinuumBuildExecutor, Initializable
{
    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

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
    private WorkingDirectoryService workingDirectoryService;

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

    // ----------------------------------------------------------------------
    // Component Lifecycle
    // ----------------------------------------------------------------------

    public void initialize()
        throws InitializationException
    {
        List path = executableResolver.getDefaultPath();

        if ( resolveExecutable )
        {
            if ( StringUtils.isEmpty( defaultExecutable ) )
            {
                getLogger().warn(
                                  "The default executable for build executor '" + id + "' is not set. "
                                      + "This will cause a problem unless the project has a executable configured." );
            }
            else
            {
                File resolvedExecutable = executableResolver.findExecutable( defaultExecutable, path );

                if ( resolvedExecutable == null )
                {
                    getLogger().warn(
                                      "Could not find the executable '" + defaultExecutable + "' in the " + "path '"
                                          + path + "'." );
                }
                else
                {
                    getLogger().info(
                                      "Resolved the executable '" + defaultExecutable + "' to " + "'"
                                          + resolvedExecutable.getAbsolutePath() + "'." );
                }
            }
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    protected ContinuumBuildExecutionResult executeShellCommand( Project project, String executable, String arguments,
                                                                 File output, Map<String, String> environments )
        throws ContinuumBuildExecutorException
    {
        // ----------------------------------------------------------------------
        // If we're not searching the path for the executable, prefix the
        // executable with the working directory to make sure the path is
        // absolute and thus won't be tried resoled by using the PATH
        // ----------------------------------------------------------------------

        String actualExecutable;

        File workingDirectory = getWorkingDirectory( project );

        if ( !resolveExecutable )
        {
            actualExecutable = new File( workingDirectory, executable ).getAbsolutePath();
        }
        else
        {
            List path = executableResolver.getDefaultPath();

            if ( StringUtils.isEmpty( executable ) )
            {
                executable = defaultExecutable;
            }

            File e = executableResolver.findExecutable( executable, path );

            if ( e == null )
            {
                getLogger().warn( "Could not find the executable '" + executable + "' in this path: " );

                for ( Iterator it = path.iterator(); it.hasNext(); )
                {
                    String element = (String) it.next();

                    getLogger().warn( element );
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

        // ----------------------------------------------------------------------
        // Execute the build
        // ----------------------------------------------------------------------

        try
        {
            ExecutionResult result = shellCommandHelper.executeShellCommand( workingDirectory, actualExecutable,
                                                                             arguments, output, project.getId(),
                                                                             environments );

            getLogger().info( "Exit code: " + result.getExitCode() );

            return new ContinuumBuildExecutionResult( FileUtils.fileRead( output ), result.getExitCode() );
        }
        catch ( CommandLineException e )
        {
            if ( e.getCause() instanceof InterruptedException )
            {
                throw new ContinuumBuildCancelledException( "The build was cancelled", e );
            }
            else
            {
                throw new ContinuumBuildExecutorException(
                                                           "Error while executing shell command. The most common error is that '"
                                                               + executable + "' " + "is not in your path.", e );
            }
        }
        catch ( Exception e )
        {
            throw new ContinuumBuildExecutorException( "Error while executing shell command. "
                + "The most common error is that '" + executable + "' " + "is not in your path.", e );
        }
    }

    protected abstract Map<String, String> getEnvironments( BuildDefinition buildDefinition );

    protected String getJavaHomeValue( BuildDefinition buildDefinition )
    {
        Profile profile = buildDefinition.getProfile();
        if ( profile == null )
        {
            return null;
        }
        Installation jdk = profile.getJdk();
        if ( jdk == null )
        {
            return null;
        }
        return jdk.getVarValue();
    }

    protected Map<String, String> getEnvironmentVariable( BuildDefinition buildDefinition )
    {
        Profile profile = buildDefinition.getProfile();
        Map<String, String> envVars = new HashMap<String, String>();
        if ( profile == null )
        {
            return envVars;
        }
        List<Installation> environmentVariables = profile.getEnvironmentVariables();
        if ( environmentVariables.isEmpty() )
        {
            return envVars;
        }
        for ( Iterator<Installation> iterator = environmentVariables.iterator(); iterator.hasNext(); )
        {
            Installation installation = iterator.next();
            envVars.put( installation.getVarName(), installation.getVarValue() );
        }
        return envVars;
    }

    public boolean isBuilding( Project project )
    {
        return shellCommandHelper.isRunning( project.getId() );
    }

    public void killProcess( Project project )
    {
        shellCommandHelper.killProcess( project.getId() );
    }

    public List getDeployableArtifacts( File workingDirectory, BuildDefinition buildDefinition )
        throws ContinuumBuildExecutorException
    {
        // Not supported by this builder
        return Collections.EMPTY_LIST;
    }

    public File getWorkingDirectory( Project project )
    {
        return workingDirectoryService.getWorkingDirectory( project );
    }

    public TestResult getTestResults( Project project )
        throws ContinuumBuildExecutorException
    {
        return null;
    }

    public InstallationService getInstallationService()
    {
        return installationService;
    }

    public void setInstallationService( InstallationService installationService )
    {
        this.installationService = installationService;
    }
}
