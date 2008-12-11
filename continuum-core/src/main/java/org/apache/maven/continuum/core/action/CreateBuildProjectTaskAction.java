package org.apache.maven.continuum.core.action;

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

import org.apache.continuum.dao.BuildDefinitionDao;
import org.apache.continuum.dao.ProjectDao;
import org.apache.continuum.taskqueue.manager.TaskQueueManager;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.buildqueue.BuildProjectTask;
import org.apache.maven.continuum.execution.ContinuumBuildExecutor;
import org.apache.maven.continuum.execution.manager.BuildExecutorManager;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.taskqueue.TaskQueueException;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:ctan@apache.org">Maria Catherine Tan</a>
 * @plexus.component role="org.codehaus.plexus.action.Action" role-hint="create-build-project-task"
 */
public class CreateBuildProjectTaskAction
    extends AbstractContinuumAction
{
    /**
     * @plexus.requirement
     */
    private TaskQueueManager taskQueueManager;

    /**
     * @plexus.requirement
     */
    private BuildExecutorManager executorManager;

    /**
     * @plexus.requirement
     */
    private ProjectDao projectDao;

    /**
     * @plexus.requirement
     */
    private BuildDefinitionDao buildDefinitionDao;
    
    public synchronized void execute( Map context )
        throws Exception
    {
    // TODO: deng parallel builds
    // - context now contains a "list" of projects and a "map" of projectId, build definition ket-value pair
    // - update the list of projects
    // - pass this updated list + map of build definitions to builds manager
        
        Project project = AbstractContinuumAction.getProject( context );
        int buildDefinitionId = AbstractContinuumAction.getBuildDefinitionId( context );
        int trigger = AbstractContinuumAction.getTrigger( context );
        
        if ( taskQueueManager.isInBuildingQueue( project.getId(), buildDefinitionId ) )
        {
            return;
        }

        if ( taskQueueManager.isInCheckoutQueue( project.getId() ) )
        {
            taskQueueManager.removeProjectFromCheckoutQueue( project.getId() );
        }
        
        try
        {
            if ( project.getState() != ContinuumProjectState.NEW &&
                project.getState() != ContinuumProjectState.CHECKEDOUT &&
                project.getState() != ContinuumProjectState.OK && project.getState() != ContinuumProjectState.FAILED &&
                project.getState() != ContinuumProjectState.ERROR )
            {
                ContinuumBuildExecutor executor = executorManager.getBuildExecutor( project.getExecutorId() );

                if ( executor.isBuilding( project ) || project.getState() == ContinuumProjectState.UPDATING )
                {
                    // project is building
                    getLogger().info( "Project '" + project.getName() + "' already being built." );

                    return;
                }
                else
                {
                    project.setOldState( project.getState() );

                    project.setState( ContinuumProjectState.ERROR );

                    projectDao.updateProject( project );

                    project = projectDao.getProject( project.getId() );
                }
            }
            else
            {
                project.setOldState( project.getState() );

                projectDao.updateProject( project );

                project = projectDao.getProject( project.getId() );
            }

            BuildDefinition buildDefinition = buildDefinitionDao.getBuildDefinition( buildDefinitionId );
            String buildDefinitionLabel = buildDefinition.getDescription();
            if ( StringUtils.isEmpty( buildDefinitionLabel ) )
            {
                buildDefinitionLabel = buildDefinition.getGoals();
            }

            getLogger().info( "Enqueuing '" + project.getName() + "' with build definition '" + buildDefinitionLabel +
                "' - id=" + buildDefinitionId + ")." );

            BuildProjectTask task = new BuildProjectTask( project.getId(), buildDefinitionId, trigger, project
                .getName(), buildDefinitionLabel );

            task.setMaxExecutionTime( buildDefinition.getSchedule()
                .getMaxJobExecutionTime() * 1000 );

            taskQueueManager.getBuildQueue().put( task );
        }
        catch ( ContinuumStoreException e )
        {
            getLogger().error( "Error while creating build object", e );
            throw new ContinuumException( "Error while creating build object.", e );
        }
        catch ( TaskQueueException e )
        {
            getLogger().error( "Error while enqueuing object", e );
            throw new ContinuumException( "Error while enqueuing object.", e );
        }
    }
}
