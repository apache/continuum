package org.apache.maven.continuum.buildcontroller;

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

import org.apache.continuum.taskqueue.manager.TaskQueueManager;
import org.apache.continuum.taskqueue.manager.TaskQueueManagerException;
import org.apache.maven.continuum.buildqueue.BuildProjectTask;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.taskqueue.Task;
import org.codehaus.plexus.taskqueue.execution.TaskExecutionException;
import org.codehaus.plexus.taskqueue.execution.TaskExecutor;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class BuildProjectTaskExecutor
    extends AbstractLogEnabled
    implements TaskExecutor
{
    /**
     * @plexus.requirement
     */
    private BuildController controller;
    
    /**
     * @plexus.requirement
     */
    private TaskQueueManager taskQueueManager;

    // ----------------------------------------------------------------------
    // TaskExecutor Implementation
    // ----------------------------------------------------------------------

    public void executeTask( Task task )
        throws TaskExecutionException
    {
        BuildProjectTask buildProjectTask = (BuildProjectTask) task;

        try
        {
            while ( taskQueueManager.isInPrepareBuildQueue( buildProjectTask.getProjectId() ) ||
                    taskQueueManager.isInCurrentPrepareBuildTask( buildProjectTask.getProjectId() ) )
            {
                Thread.sleep( 1000 );
            }
        }
        catch ( TaskQueueManagerException e )
        {
            throw new TaskExecutionException( e.getMessage(), e );
        }
        catch ( InterruptedException e )
        {
            throw new TaskExecutionException( e.getMessage(), e );
        }
        
        controller.build( buildProjectTask.getProjectId(), buildProjectTask.getBuildDefinitionId(), buildProjectTask
            .getTrigger() );
    }
}
