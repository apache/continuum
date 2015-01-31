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
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.Arg;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
@Service( "shellCommandHelper" )
public class DefaultShellCommandHelper
    implements ShellCommandHelper
{
    private static final Logger log = LoggerFactory.getLogger( DefaultShellCommandHelper.class );

    // ----------------------------------------------------------------------
    // ShellCommandHelper Implementation
    // ----------------------------------------------------------------------

    public Properties getSystemEnvVars()
    {
        try
        {
            return CommandLineUtils.getSystemEnvVars( false );
        }
        catch ( IOException e )
        {
            log.warn( "failed to get system environment", e );
        }
        return new Properties();
    }

    public ExecutionResult executeShellCommand( File workingDirectory, String executable, String arguments, File output,
                                                long idCommand, Map<String, String> environments )
        throws Exception
    {
        Commandline cl = new Commandline();

        Arg argument = cl.createArg();

        argument.setLine( arguments );

        return executeShellCommand( workingDirectory, executable, argument.getParts(), output, idCommand,
                                    environments );
    }

    private static class IOConsumerWrapper
        implements StreamConsumer
    {
        private OutputConsumer userConsumer;

        public IOConsumerWrapper( OutputConsumer userConsumer )
        {
            this.userConsumer = userConsumer;
        }

        public void consumeLine( String line )
        {
            if ( userConsumer != null )
            {
                userConsumer.consume( line );
            }
        }
    }

    public ExecutionResult executeShellCommand( File workingDirectory, String executable, String[] arguments,
                                                OutputConsumer io, long idCommand,
                                                Map<String, String> environments )
        throws Exception
    {
        Commandline cl = createCommandline( workingDirectory, executable, arguments, idCommand, environments );

        log.info( "Executing: " + cl );
        File clWorkDir = cl.getWorkingDirectory();
        log.info( "Working directory: " + ( clWorkDir != null ? clWorkDir.getAbsolutePath() : "default" ) );
        log.debug( "EnvironmentVariables " + Arrays.asList( cl.getEnvironmentVariables() ) );

        StreamConsumer consumer = new IOConsumerWrapper( io );

        int exitCode = CommandLineUtils.executeCommandLine( cl, consumer, consumer );

        return new ExecutionResult( exitCode );
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
            for ( String key : environments.keySet() )
            {
                String value = environments.get( key );
                cl.addEnvironment( key, value );
            }
        }

        cl.addSystemEnvironment();

        cl.setExecutable( executable );

        if ( workingDirectory != null )
        {
            cl.setWorkingDirectory( workingDirectory.getAbsolutePath() );
        }

        if ( arguments != null )
        {
            for ( String argument : arguments )
            {
                cl.createArg().setValue( argument );
            }
        }

        return cl;
    }

    public ExecutionResult executeShellCommand( File workingDirectory, String executable, String[] arguments,
                                                File output, long idCommand, Map<String, String> environments )
        throws Exception
    {
        FileOutputConsumer fileConsumer = new FileOutputConsumer( output );
        try
        {
            return executeShellCommand( workingDirectory, executable, arguments, fileConsumer, idCommand,
                                        environments );
        }
        finally
        {
            fileConsumer.flush();
            fileConsumer.close();
        }
    }

    public boolean isRunning( long idCommand )
    {
        return true; //CommandLineUtils.isAlive( idCommand );
    }

    public void killProcess( long idCommand )
    {
        // CommandLineUtils.killProcess( idCommand );
    }

    public void executeGoals( File workingDirectory, String executable, String goals, boolean interactive,
                              String arguments, ReleaseResult relResult, Map<String, String> environments )
        throws Exception
    {
        Commandline cl = new Commandline();

        Arg argument = cl.createArg();

        argument.setLine( arguments );

        executeGoals( workingDirectory, executable, goals, interactive, argument.getParts(), relResult, environments );
    }

    public void executeGoals( File workingDirectory, String executable, String goals, boolean interactive,
                              String[] arguments, ReleaseResult relResult, Map<String, String> environments )
        throws Exception
    {
        if ( executable == null )
        {
            executable = "mvn";
        }

        Commandline cl = createCommandline( workingDirectory, executable, arguments, -1, environments );

        if ( goals != null )
        {
            // accept both space and comma, so the old way still work
            String[] tokens = StringUtils.split( goals, ", " );

            for ( String token : tokens )
            {
                cl.createArg().setValue( token );
            }
        }

        cl.createArg().setValue( "--no-plugin-updates" );

        if ( !interactive )
        {
            cl.createArg().setValue( "--batch-mode" );
        }

        StreamConsumer stdOut = new TeeConsumer( System.out );

        StreamConsumer stdErr = new TeeConsumer( System.err );

        try
        {
            relResult.appendInfo( "Executing: " + cl.toString() );
            log.info( "Executing: " + cl.toString() );

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
