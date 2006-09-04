package org.apache.maven.continuum.release.executors;

/*
 * Copyright 2006 The Apache Software Foundation.
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

import org.apache.maven.continuum.release.tasks.PrepareReleaseProjectTask;
import org.apache.maven.plugins.release.config.ReleaseDescriptor;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.taskqueue.Task;
import org.codehaus.plexus.taskqueue.execution.TaskExecutor;

/**
 * @author Edwin Punzalan
 */
public class PrepareReleaseTaskExecutorTest
    extends PlexusTestCase
{
    private TaskExecutor taskExec;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        taskExec = (TaskExecutor) lookup( ReleaseTaskExecutor.ROLE, "prepare-release" );
    }

    public void testRelease()
        throws Exception
    {
        ReleaseDescriptor descriptor = new ReleaseDescriptor();

        taskExec.executeTask( getPrepareTask( "testRelease", descriptor ) );
    }

    protected Task getPrepareTask( String releaseId, ReleaseDescriptor descriptor )
    {
        Task task = new PrepareReleaseProjectTask( releaseId, descriptor );

        return task;
    }
}
