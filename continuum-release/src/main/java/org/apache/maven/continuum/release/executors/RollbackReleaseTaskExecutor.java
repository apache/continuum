package org.apache.maven.continuum.release.executors;

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

import org.apache.maven.continuum.release.tasks.ReleaseProjectTask;
import org.apache.maven.shared.release.ReleaseExecutionException;
import org.apache.maven.shared.release.ReleaseFailureException;
import org.apache.maven.shared.release.ReleaseManagerListener;
import org.codehaus.plexus.taskqueue.execution.TaskExecutionException;

import java.util.ArrayList;

/**
 * @author Edwin Punzalan
 * @version $Id$
 */
public class RollbackReleaseTaskExecutor
    extends AbstractReleaseTaskExecutor
{
    private static final int ERROR = 99;

    protected void execute( ReleaseProjectTask releaseTask )
        throws TaskExecutionException
    {
        try
        {
            releaseManager.rollback( releaseTask.getDescriptor(), settings, new ArrayList(),
                                     releaseTask.getListener() );
        }
        catch ( ReleaseExecutionException e )
        {
            updateListener( releaseTask.getListener(), e.getMessage(), ERROR );
            throw new TaskExecutionException( "Failed to rollback release", e );
        }
        catch ( ReleaseFailureException e )
        {
            updateListener( releaseTask.getListener(), e.getMessage(), ERROR );
            throw new TaskExecutionException( "Failed to rollback release", e );
        }
    }

    private void updateListener( ReleaseManagerListener listener, String name, int state )
    {
        listener.error( name );
    }
}
