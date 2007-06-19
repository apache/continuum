package org.apache.maven.continuum.utils.shell;

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

import org.codehaus.plexus.logging.AbstractLogEnabled;
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
 *
 * @plexus.component
 *   role="org.apache.maven.continuum.utils.shell.ShellCommandHelper"
 *   role-hint="default"
 */
public class DefaultShellCommandHelper
    extends AbstractLogEnabled
    implements ShellCommandHelper
{
    // ----------------------------------------------------------------------
    // ShellCommandHelper Implementation
    // ----------------------------------------------------------------------

    public ExecutionResult executeShellCommand( File workingDirectory, String executable, String arguments,
                                                File output, long idCommand, Map<String, String> environments )
        throws Exception
    {
        Commandline cl = new Commandline();

        Commandline.Argument argument = cl.createArgument();

        argument.setLine( arguments );

        return executeShellCommand( workingDirectory, executable, argument.getParts(), output, idCommand, environments );
    }

    public ExecutionResult executeShellCommand( File workingDirectory, String executable, String[] arguments,
                                                File output, long idCommand, Map<String, String> environments )
        throws Exception
    {
        // ----------------------------------------------------------------------
        // Make the command line
        // ----------------------------------------------------------------------

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

        for ( int i = 0; i < arguments.length; i++ )
        {
            String argument = arguments[i];

            cl.createArgument().setValue( argument );
        }

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
}
