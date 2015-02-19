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

import org.apache.continuum.taskqueue.BuildProjectTask;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.taskqueue.Task;
import org.codehaus.plexus.taskqueue.execution.TaskExecutionException;
import org.codehaus.plexus.taskqueue.execution.TaskExecutor;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class BuildProjectTaskExecutor
    implements TaskExecutor
{

    @Requirement
    private BuildController controller;

    // ----------------------------------------------------------------------
    // TaskExecutor Implementation
    // ----------------------------------------------------------------------

    public void executeTask( Task task )
        throws TaskExecutionException
    {
        BuildProjectTask buildProjectTask = (BuildProjectTask) task;

        controller.build( buildProjectTask.getProjectId(), buildProjectTask.getBuildDefinitionId(),
                          buildProjectTask.getBuildTrigger(), buildProjectTask.getScmResult() );
    }
}
