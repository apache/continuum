package org.apache.maven.continuum.execution;

/*
 * Copyright 2004-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.List;
import java.util.Iterator;
import java.io.File;

import org.apache.maven.continuum.utils.shell.ShellCommandHelper;
import org.apache.maven.continuum.utils.shell.ExecutionResult;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.commandline.ExecutableResolver;

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

    /** @plexus.requirement */
    private ShellCommandHelper shellCommandHelper;

    /** @plexus.requirement */
    private ExecutableResolver executableResolver;

    /** @plexus.configuration */
    private String defaultExecutable;

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private String id;

    private boolean resolveExecutable;

    private File resolvedExecutable;

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
                getLogger().warn( "The default executable for build executor '" + id + "' is not set. " +
                                  "This will cause a problem unless the project has a executable configured." );
            }
            else
            {
                resolvedExecutable = executableResolver.findExecutable( defaultExecutable, path );

                if ( resolvedExecutable == null )
                {
                    getLogger().warn( "Could not find the executable '" + defaultExecutable + "' in the path '" + path + "'" );
                }
                else
                {
                    getLogger().info( "Resolved the executable '" + defaultExecutable + "' to " +
                                      "'" + resolvedExecutable.getAbsolutePath() + "'");
                }
            }
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    protected ContinuumBuildExecutionResult executeShellCommand( File workingDirectory,
                                                                 String executable,
                                                                 String arguments )
        throws ContinuumBuildExecutorException
    {
        File actualExecutable = resolvedExecutable;

        // ----------------------------------------------------------------------
        // If we're not searching the path for the executable, prefix the
        // executable with the working directory to make sure the path is
        // absolute and thus won't be tried resolved by using the PATH
        // ----------------------------------------------------------------------

        if ( !resolveExecutable )
        {
            actualExecutable = new File( workingDirectory, executable );
        }

        // ----------------------------------------------------------------------
        // Find the executable to execute in the path
        // ----------------------------------------------------------------------

        // TODO: actually use the resolved executable. This is disabled now until
        //       I know that this code actually is working properly on all platforms
        List path = executableResolver.getDefaultPath();

        if ( resolveExecutable )
        {
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
            }
            else
            {
                getLogger().warn( "Using executable '" + e.getAbsolutePath() + "'." );
            }

            // TODO: remove comment when this code is working properly
            actualExecutable = e;
        }

        // ----------------------------------------------------------------------
        // Execute the build
        // ----------------------------------------------------------------------

        try
        {
            ExecutionResult result = shellCommandHelper.executeShellCommand( workingDirectory,
                                                                             actualExecutable,
                                                                             arguments );

            return new ContinuumBuildExecutionResult( result.getExitCode() == 0,
                                                      result.getStandardOutput(),
                                                      result.getStandardError(),
                                                      result.getExitCode() );
        }
        catch ( Exception e )
        {
            throw new ContinuumBuildExecutorException( "Error while executing shell command.", e );
        }
    }
}
