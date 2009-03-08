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

import java.util.List;
import java.util.Map;

import org.apache.continuum.buildagent.buildcontext.BuildContext;
import org.apache.continuum.buildagent.taskqueue.manager.BuildAgentTaskQueueManager;
import org.apache.continuum.buildagent.utils.ContinuumBuildAgentUtil;
import org.apache.continuum.taskqueue.BuildProjectTask;
import org.apache.continuum.taskqueue.manager.TaskQueueManagerException;
import org.apache.maven.continuum.ContinuumException;
import org.codehaus.plexus.action.AbstractAction;
import org.codehaus.plexus.taskqueue.TaskQueueException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @plexus.component role="org.codehaus.plexus.action.Action" role-hint="create-agent-build-project-task"
 */
public class CreateBuildProjectTaskAction
    extends AbstractAction
{
    private Logger log = LoggerFactory.getLogger( this.getClass() );

    /**
     * @plexus.requirement
     */
    private BuildAgentTaskQueueManager buildAgentTaskQueueManager;

    public void execute( Map context )
        throws Exception
    {
        List<BuildContext> buildContexts = ContinuumBuildAgentUtil.getBuildContexts( context );

        for ( BuildContext buildContext : buildContexts )
        {
            BuildProjectTask buildProjectTask = new BuildProjectTask( buildContext.getProjectId(),
                                                                      buildContext.getBuildDefinitionId(),
                                                                      buildContext.getTrigger(),
                                                                      buildContext.getProjectName(),
                                                                      "", 
                                                                      buildContext.getScmResult() );
            buildProjectTask.setMaxExecutionTime( buildContext.getMaxExecutionTime() * 1000 );

            try
            {
                if ( !buildAgentTaskQueueManager.isProjectInBuildQueue( buildProjectTask.getProjectId() ) )
                {
                    buildAgentTaskQueueManager.getBuildQueue().put( buildProjectTask );
                }
            }
            catch ( TaskQueueException e )
            {
                log.error( "Error while enqueing build task for project " + buildContext.getProjectId(), e );
                throw new ContinuumException( "Error while enqueuing build task for project " + buildContext.getProjectId(), e );
            }
            catch ( TaskQueueManagerException e )
            {
                log.error( "Error while checking if project " + buildContext.getProjectId() + " is in build queue", e );
                throw new ContinuumException( "Error while checking if project " + buildContext.getProjectId() + " is in build queue", e );
            }
        }

        try
        {
            boolean stop = false;
            while ( !stop )
            {
                if ( buildAgentTaskQueueManager.getCurrentProjectInBuilding() <= 0 && 
                               !buildAgentTaskQueueManager.hasBuildTaskInQueue()  )
                {
                    stop = true;
                }
            }
        }
        catch ( TaskQueueManagerException e )
        {
            throw new ContinuumException( e.getMessage(), e );
        }
    }

}
