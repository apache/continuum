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

import java.util.Map;

import org.apache.continuum.buildmanager.BuildManagerException;
import org.apache.continuum.buildmanager.BuildsManager;
import org.apache.continuum.taskqueue.manager.TaskQueueManager;
import org.apache.continuum.taskqueue.manager.TaskQueueManagerException;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.codehaus.plexus.taskqueue.Task;
import org.codehaus.plexus.taskqueue.TaskQueueException;
import org.codehaus.plexus.taskqueue.execution.TaskExecutionException;
import org.codehaus.plexus.taskqueue.execution.TaskExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executor for tasks in init-prepare-build-project queue. This executor determines where to queue
 * the prepare-build-project task, either in the local build queues or in the distributed build queue.
 * 
 * @author <a href="mailto:oching@apache.org">Maria Odea Ching</a>
 * @version $Id$
 * @plexus.component role="org.codehaus.plexus.taskqueue.execution.TaskExecutor" role-hint="init-prepare-build-project"
 */
public class InitPrepareBuildProjectsTaskExecutor
    implements TaskExecutor
{
    private Logger log = LoggerFactory.getLogger( InitPrepareBuildProjectsTaskExecutor.class );

    /**
     * @plexus.requirement role-hint="parallel"
     */
    private BuildsManager parallelBuildsManager;

    /**
     * @plexus.requirement
     */
    private TaskQueueManager taskQueueManager;

    /**
     * @plexus.requirement
     */
    private ConfigurationService configurationService;

    public void executeTask( Task task )
        throws TaskExecutionException
    {
        PrepareBuildProjectsTask prepareBuildTask = (PrepareBuildProjectsTask) task;

        try
        {
            if ( configurationService.isDistributedBuildEnabled() )
            {
                int allowedParallelBuilds = configurationService.getNumberOfBuildsInParallel();
                Map<String, Task> currentBuilds = parallelBuildsManager.getCurrentBuilds();

                // check the number of local builds executing in parallel
                if ( currentBuilds.size() < allowedParallelBuilds )
                {
                    log.info( "Enqueuing prepare-build-project task '" + prepareBuildTask.getProjectGroupName() +
                        "' to parallel builds queue." );
                    parallelBuildsManager.prepareBuildProjects( task );
                }
                else
                {
                    if ( !taskQueueManager.isInDistributedBuildQueue( prepareBuildTask.getProjectGroupId(),
                                                                      prepareBuildTask.getScmRootAddress() ) )
                    {
                        log.info( "Enqueuing prepare-build-project task '" + prepareBuildTask.getProjectGroupName() +
                            "' to distributed builds queue." );
                        taskQueueManager.getDistributedBuildQueue().put( task );
                    }
                }
            }
            else
            {
                log.info( "Enqueuing prepare-build-project task '" + prepareBuildTask.getProjectGroupName() +
                    "' to parallel builds queue." );
                parallelBuildsManager.prepareBuildProjects( task );
            }
        }
        catch ( TaskQueueManagerException e )
        {
            throw new TaskExecutionException( e.getMessage() );
        }
        catch ( TaskQueueException e )
        {
            throw new TaskExecutionException( e.getMessage() );
        }
        catch ( BuildManagerException e )
        {
            throw new TaskExecutionException( e.getMessage() );
        }
    }
}
