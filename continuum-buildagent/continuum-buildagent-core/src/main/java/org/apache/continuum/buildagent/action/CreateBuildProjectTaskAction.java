package org.apache.continuum.buildagent.action;

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

import org.apache.continuum.buildagent.buildcontext.BuildContext;
import org.apache.continuum.buildagent.taskqueue.manager.BuildAgentTaskQueueManager;
import org.apache.continuum.buildagent.utils.ContinuumBuildAgentUtil;
import org.apache.continuum.taskqueue.BuildProjectTask;
import org.apache.continuum.taskqueue.manager.TaskQueueManagerException;
import org.apache.continuum.utils.build.BuildTrigger;
import org.apache.maven.continuum.ContinuumException;
import org.codehaus.plexus.action.AbstractAction;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.taskqueue.TaskQueueException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@Component( role = org.codehaus.plexus.action.Action.class, hint = "create-agent-build-project-task" )
public class CreateBuildProjectTaskAction
    extends AbstractAction
{
    private static final Logger log = LoggerFactory.getLogger( CreateBuildProjectTaskAction.class );

    @Requirement
    private BuildAgentTaskQueueManager buildAgentTaskQueueManager;

    public void execute( Map context )
        throws Exception
    {
        List<BuildContext> buildContexts = ContinuumBuildAgentUtil.getBuildContexts( context );

        for ( BuildContext buildContext : buildContexts )
        {
            BuildTrigger buildTrigger = new BuildTrigger( buildContext.getTrigger(), buildContext.getUsername() );

            BuildProjectTask buildProjectTask = new BuildProjectTask( buildContext.getProjectId(),
                                                                      buildContext.getBuildDefinitionId(), buildTrigger,
                                                                      buildContext.getProjectName(),
                                                                      buildContext.getBuildDefinitionLabel(),
                                                                      buildContext.getScmResult(),
                                                                      buildContext.getProjectGroupId() );
            buildProjectTask.setMaxExecutionTime( buildContext.getMaxExecutionTime() * 1000 );

            try
            {
                if ( !buildAgentTaskQueueManager.isProjectInBuildQueue( buildProjectTask.getProjectId() ) )
                {
                    log.info( "Adding project {} to build queue", buildProjectTask.getProjectId() );
                    buildAgentTaskQueueManager.getBuildQueue().put( buildProjectTask );
                }
            }
            catch ( TaskQueueException e )
            {
                log.error( "Error while enqueing build task for project " + buildContext.getProjectId(), e );
                throw new ContinuumException(
                    "Error while enqueuing build task for project " + buildContext.getProjectId(), e );
            }
            catch ( TaskQueueManagerException e )
            {
                log.error( "Error while checking if project " + buildContext.getProjectId() + " is in build queue", e );
                throw new ContinuumException(
                    "Error while checking if project " + buildContext.getProjectId() + " is in build queue", e );
            }
        }
    }

}
