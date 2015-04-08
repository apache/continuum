package org.apache.continuum.taskqueue;

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

import edu.emory.mathcs.backport.java.util.concurrent.CancellationException;
import edu.emory.mathcs.backport.java.util.concurrent.ExecutionException;
import edu.emory.mathcs.backport.java.util.concurrent.ExecutorService;
import edu.emory.mathcs.backport.java.util.concurrent.Executors;
import edu.emory.mathcs.backport.java.util.concurrent.Future;
import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;
import edu.emory.mathcs.backport.java.util.concurrent.TimeoutException;
import org.apache.continuum.utils.ThreadNames;
import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;
import org.codehaus.plexus.taskqueue.Task;
import org.codehaus.plexus.taskqueue.TaskQueue;
import org.codehaus.plexus.taskqueue.execution.TaskExecutionException;
import org.codehaus.plexus.taskqueue.execution.TaskExecutor;
import org.codehaus.plexus.taskqueue.execution.TaskQueueExecutor;
import org.codehaus.plexus.util.StringUtils;

/**
 * Modified plexus ThreadedTaskQueueExecutor
 */
public class ThreadedTaskQueueExecutor
    extends AbstractLogEnabled
    implements TaskQueueExecutor, Initializable, Startable, Disposable
{
    private static final int SHUTDOWN = 1;

    private static final int CANCEL_TASK = 2;

    @Requirement
    private TaskQueue queue;

    @Requirement
    private TaskExecutor executor;

    @Configuration( "" )
    private String name;

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private ExecutorRunnable executorRunnable;

    private ExecutorService executorService;

    private Task currentTask;

    private class ExecutorRunnable
        extends Thread
    {
        private volatile int command;

        private boolean done;

        public ExecutorRunnable()
        {
            super( ThreadNames.formatNext( "%s-executor", name ) );
        }

        public void run()
        {
            while ( command != SHUTDOWN )
            {
                final Task task;

                currentTask = null;

                try
                {
                    task = queue.poll( 100, TimeUnit.MILLISECONDS );
                }
                catch ( InterruptedException e )
                {
                    getLogger().info( "Executor thread interrupted, command: "
                                          + ( command == SHUTDOWN ? "Shutdown" :
                        command == CANCEL_TASK ? "Cancel task" : "Unknown" ) );
                    continue;
                }

                if ( task == null )
                {
                    continue;
                }

                currentTask = task;

                Future future = executorService.submit( new Runnable()
                {
                    public void run()
                    {
                        try
                        {
                            executor.executeTask( task );
                        }
                        catch ( TaskExecutionException e )
                        {
                            getLogger().error( "Error executing task", e );
                        }
                    }
                } );

                try
                {
                    waitForTask( task, future );
                }
                catch ( ExecutionException e )
                {
                    getLogger().error( "Error executing task", e );
                }
            }

            currentTask = null;

            getLogger().info( "Executor thread '" + name + "' exited." );

            done = true;

            synchronized ( this )
            {
                notifyAll();
            }
        }

        private void waitForTask( Task task, Future future )
            throws ExecutionException
        {
            boolean stop = false;

            while ( !stop )
            {
                try
                {
                    if ( task.getMaxExecutionTime() == 0 )
                    {
                        getLogger().debug( "Waiting indefinitely for task to complete" );
                        future.get();
                        return;
                    }
                    else
                    {
                        getLogger().debug( "Waiting at most " + task.getMaxExecutionTime() + "ms for task completion" );
                        future.get( task.getMaxExecutionTime(), TimeUnit.MILLISECONDS );
                        getLogger().debug( "Task completed within " + task.getMaxExecutionTime() + "ms" );
                        return;
                    }
                }
                catch ( InterruptedException e )
                {
                    switch ( command )
                    {
                        case SHUTDOWN:
                        {
                            getLogger().info( "Shutdown command received. Cancelling task." );
                            cancel( future );
                            return;
                        }

                        case CANCEL_TASK:
                        {
                            command = 0;
                            getLogger().info( "Cancelling task" );
                            cancel( future );
                            return;
                        }

                        default:
                            // when can this thread be interrupted, and should we ignore it if shutdown = false?
                            getLogger().warn( "Interrupted while waiting for task to complete; ignoring", e );
                            break;
                    }
                }
                catch ( TimeoutException e )
                {
                    getLogger().warn( "Task " + task + " didn't complete within time, cancelling it." );
                    cancel( future );
                    return;
                }
                catch ( CancellationException e )
                {
                    getLogger().warn( "The task was cancelled", e );
                    return;
                }
            }
        }

        private void cancel( Future future )
        {
            if ( !future.cancel( true ) )
            {
                if ( !future.isDone() && !future.isCancelled() )
                {
                    getLogger().warn( "Unable to cancel task" );
                }
                else
                {
                    getLogger().warn( "Task not cancelled (Flags: done: " + future.isDone() + " cancelled: "
                                          + future.isCancelled() + ")" );
                }
            }
            else
            {
                getLogger().debug( "Task successfully cancelled" );
            }
        }

        public synchronized void shutdown()
        {
            getLogger().debug( "Signalling executor thread to shutdown" );

            command = SHUTDOWN;

            interrupt();
        }

        public synchronized boolean cancelTask( Task task )
        {
            if ( !task.equals( currentTask ) )
            {
                getLogger().debug( "Not cancelling task - it is not running" );
                return false;
            }

            if ( command != SHUTDOWN )
            {
                getLogger().debug( "Signalling executor thread to cancel task" );

                command = CANCEL_TASK;

                interrupt();
            }
            else
            {
                getLogger().debug( "Executor thread already stopping; task will be cancelled automatically" );
            }

            return true;
        }

        public boolean isDone()
        {
            return done;
        }
    }

    // ----------------------------------------------------------------------
    // Component lifecycle
    // ----------------------------------------------------------------------

    public void initialize()
        throws InitializationException
    {
        if ( StringUtils.isEmpty( name ) )
        {
            throw new IllegalArgumentException( "'name' must be set." );
        }
    }

    public void start()
        throws StartingException
    {
        getLogger().info( "Starting task executor, thread name '" + name + "'." );

        this.executorService = Executors.newSingleThreadExecutor();

        executorRunnable = new ExecutorRunnable();

        executorRunnable.setDaemon( true );

        executorRunnable.start();
    }

    public void stop()
        throws StoppingException
    {
        executorRunnable.shutdown();

        int maxSleep = 10 * 1000; // 10 seconds

        int interval = 1000;

        long endTime = System.currentTimeMillis() + maxSleep;

        while ( !executorRunnable.isDone() && executorRunnable.isAlive() )
        {
            if ( System.currentTimeMillis() > endTime )
            {
                getLogger().warn( "Timeout waiting for executor thread '" + name + "' to stop, aborting" );
                break;
            }

            getLogger().info( "Waiting until task executor '" + name + "' is idling..." );

            try
            {
                synchronized ( executorRunnable )
                {
                    executorRunnable.wait( interval );
                }
            }
            catch ( InterruptedException ex )
            {
                // ignore
            }

            // notify again, just in case.
            executorRunnable.shutdown();
        }
    }

    public void dispose()
    {
        executorRunnable.shutdown();
    }

    public Task getCurrentTask()
    {
        return currentTask;
    }

    public synchronized boolean cancelTask( Task task )
    {
        return executorRunnable.cancelTask( task );
    }
}
