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

import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @see org.apache.continuum.utils.shell.DefaultShellCommandHelper
 */
public class DefaultShellCommandHelperTest
    extends TestCase
{
    private static final Logger log = LoggerFactory.getLogger( DefaultShellCommandHelper.class );

    DefaultShellCommandHelper helper;

    public void setUp()
    {
        helper = new DefaultShellCommandHelper();
    }

    public void tearDown()
    {
        helper = null;
    }

    /**
     * To concurrently check the run status of a process.
     */
    private static class RunChecker
        implements Runnable
    {
        ShellCommandHelper shellHelper;

        long pid;

        boolean wasRunning;

        long sleepMillis;

        public RunChecker( ShellCommandHelper shellHelper, long pid, long sleepMillis )
        {
            this.shellHelper = shellHelper;
            this.pid = pid;
            this.sleepMillis = sleepMillis;
        }

        public void run()
        {
            try
            {
                Thread.sleep( sleepMillis );
                this.wasRunning = shellHelper.isRunning( pid );
            }
            catch ( InterruptedException e )
            {
                log.error( "run checker interrupted", e );
            }
        }
    }

    public void testIsRunning()
        throws Exception
    {
        long virtualPid = 1, sleepMillis = 100;
        RunChecker checker = new RunChecker( helper, virtualPid, sleepMillis );
        String[] cmdArgs = { "-cp", System.getProperty( "sleepClasspath" ), Sleep.class.getCanonicalName(), "1" };

        // Verify process isn't running initially
        checker.run();
        assertFalse( "Expected that command was not running", checker.wasRunning );

        // Verify running status is true when running
        Thread checkerThread = new Thread( checker );
        checkerThread.start();

        helper.executeShellCommand( null, "java", cmdArgs, new LogOutputConsumer( log ), virtualPid, null );
        checkerThread.join();
        assertTrue( "Expected that command was running", checker.wasRunning );

        // Verify process isn't running after
        checker.run();
        assertFalse( "Expected that command was not running", checker.wasRunning );
    }

}
