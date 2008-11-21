package org.apache.continuum.utils.shell;

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

import org.apache.maven.shared.release.ReleaseResult;
import org.apache.maven.shared.release.exec.MavenExecutorException;
import org.apache.maven.shared.release.exec.TeeConsumer;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;
import org.codehaus.plexus.util.cli.WriterStreamConsumer;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 * @plexus.component role="org.apache.continuum.utils.shell.ShellCommandHelper"
 * role-hint="default"
 */
public class DefaultShellCommandHelper
    extends AbstractLogEnabled
    implements ShellCommandHelper
{
    // ----------------------------------------------------------------------
    // ShellCommandHelper Implementation
    // ----------------------------------------------------------------------

    public ExecutionResult executeShellCommand( File workingDirectory, String executable, String arguments, File output,
                                                long idCommand, Map<String, String> environments )
        throws Exception
    {
        Commandline cl = new Commandline();

        Commandline.Argument argument = cl.createArgument();

        argument.setLine( arguments );

        return executeShellCommand( workingDirectory, executable, argument.getParts(), output, idCommand,
                                    environments );
    }

    /**
     * Make the command line
     * 
     * @param workingDirectory
     * @param executable
     * @param arguments
     * @param idCommand
     * @param environments
     * @return
     * @throws Exception
     */
    protected Commandline createCommandline( File workingDirectory, String executable, String[] arguments,
                                             long idCommand, Map<String, String> environments )
        throws Exception
    {
        Commandline cl = new Commandline();

        cl.setPid( idCommand );

        cl.addEnvironment( "MAVEN_TERMINATE_CMD", "on" );

        if ( environments != null && !environments.isEmpty() )
        {
            for ( Iterator<String> iterator = environments.keySet().iterator(); iterator.hasNext(); )
            {
                String key = iterator.next();
                String value = environments.get( key );
                cl.addEnvironment( key, value );
            }
        }

        cl.addSystemEnvironment();

        cl.setExecutable( executable );

        cl.setWorkingDirectory( workingDirectory.getAbsolutePath() );

        if ( arguments != null )
        {
            for ( int i = 0; i < arguments.length; i++ )
            {
                String argument = arguments[i];

                cl.createArgument().setValue( argument );
            }
        }

        return cl;
    }

    public ExecutionResult executeShellCommand( File workingDirectory, String executable, String[] arguments,
                                                File output, long idCommand, Map<String, String> environments )
        throws Exception
    {

        Commandline cl = createCommandline( workingDirectory, executable, arguments, idCommand, environments );

        getLogger().info( "Executing: " + cl );
        getLogger().info( "Working directory: " + cl.getWorkingDirectory().getAbsolutePath() );
        getLogger().debug( "EnvironmentVariables " + Arrays.asList( cl.getEnvironmentVariables() ) );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        //CommandLineUtils.StringStreamConsumer consumer = new CommandLineUtils.StringStreamConsumer();

        Writer writer = new FileWriter( output );

        StreamConsumer consumer = new WriterStreamConsumer( writer );

        int exitCode = CommandLineUtils.executeCommandLine( cl, consumer, consumer );

        writer.flush();

        writer.close();

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        return new ExecutionResult( exitCode );
    }

    public boolean isRunning( long idCommand )
    {
        return CommandLineUtils.isAlive( idCommand );
    }

    public void killProcess( long idCommand )
    {
        CommandLineUtils.killProcess( idCommand );
    }

    public void executeGoals( File workingDirectory, String goals, boolean interactive, String arguments,
                              ReleaseResult relResult, Map<String, String> environments )
        throws Exception
    {
        Commandline cl = new Commandline();

        Commandline.Argument argument = cl.createArgument();

        argument.setLine( arguments );

        executeGoals( workingDirectory, goals, interactive, argument.getParts(), relResult, environments );
    }

    public void executeGoals( File workingDirectory, String goals, boolean interactive, String[] arguments,
                              ReleaseResult relResult, Map<String, String> environments )
        throws Exception
    {
        Commandline cl = createCommandline( workingDirectory, "mvn", arguments, -1, environments );

        if ( goals != null )
        {
            // accept both space and comma, so the old way still work
            String[] tokens = StringUtils.split( goals, ", " );

            for ( int i = 0; i < tokens.length; ++i )
    		{
                cl.createArgument().setValue( tokens[i] );
    		}
        }

        cl.createArgument().setValue( "--no-plugin-updates" );

        if ( !interactive )
        {
            cl.createArgument().setValue( "--batch-mode" );
        }

        StreamConsumer stdOut = new TeeConsumer( System.out );

        StreamConsumer stdErr = new TeeConsumer( System.err );

        try
        {
    		relResult.appendInfo( "Executing: " + cl.toString() );
    		getLogger().info( "Executing: " + cl.toString() );

    		int result = CommandLineUtils.executeCommandLine( cl, stdOut, stdErr );

    		if ( result != 0 )
    		{
    		    throw new MavenExecutorException( "Maven execution failed, exit code: \'" + result + "\'", result,
    		                                      stdOut.toString(), stdErr.toString() );
    		}
        }
        catch ( CommandLineException e )
        {
            throw new MavenExecutorException( "Can't run goal " + goals, stdOut.toString(), stdErr.toString(), e );
        }
        finally
        {
            relResult.appendOutput( stdOut.toString() );
        }
    }
}
