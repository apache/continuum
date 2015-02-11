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

import org.apache.continuum.buildmanager.BuildsManager;
import org.apache.continuum.dao.ProjectDao;
import org.apache.continuum.utils.build.BuildTrigger;
import org.apache.maven.continuum.execution.ContinuumBuildExecutor;
import org.apache.maven.continuum.execution.manager.BuildExecutorManager;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.scm.ScmResult;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:ctan@apache.org">Maria Catherine Tan</a>
 */
@Component( role = org.codehaus.plexus.action.Action.class, hint = "create-build-project-task" )
public class CreateBuildProjectTaskAction
    extends AbstractContinuumAction
{
    @Requirement
    private BuildExecutorManager executorManager;

    @Requirement
    private ProjectDao projectDao;

    @Requirement( hint = "parallel" )
    private BuildsManager parallelBuildsManager;

    public synchronized void execute( Map context )
        throws Exception
    {
        List<Project> projects = AbstractContinuumAction.getListOfProjects( context );
        Map<Integer, BuildDefinition> projectsBuildDefinitionsMap =
            AbstractContinuumAction.getProjectsBuildDefinitionsMap( context );
        Map<Integer, ScmResult> scmResultMap = AbstractContinuumAction.getScmResultMap( context );
        List<Project> projectsToBeBuilt = new ArrayList<Project>();
        BuildTrigger buildTrigger = AbstractContinuumAction.getBuildTrigger( context );
        int projectGroupId = AbstractContinuumAction.getProjectGroupId( context );

        // update state of each project first
        for ( Project project : projects )
        {
            BuildDefinition buildDefinition = projectsBuildDefinitionsMap.get( project.getId() );

            if ( parallelBuildsManager.isInAnyBuildQueue( project.getId(), buildDefinition.getId() ) )
            {
                getLogger().info( "Project '" + project.getName() + "' is already in build queue." );
                continue;
            }

            if ( parallelBuildsManager.isInAnyCheckoutQueue( project.getId() ) )
            {
                parallelBuildsManager.removeProjectFromCheckoutQueue( project.getId() );
            }

            try
            {
                if ( project.getState() != ContinuumProjectState.NEW &&
                    project.getState() != ContinuumProjectState.CHECKEDOUT &&
                    project.getState() != ContinuumProjectState.OK &&
                    project.getState() != ContinuumProjectState.FAILED &&
                    project.getState() != ContinuumProjectState.ERROR )
                {
                    ContinuumBuildExecutor executor = executorManager.getBuildExecutor( project.getExecutorId() );

                    if ( executor.isBuilding( project ) || project.getState() == ContinuumProjectState.UPDATING )
                    {
                        // project is building
                        getLogger().info( "Project '" + project.getName() + "' already being built." );

                        continue;
                    }
                    else
                    {
                        project.setState( ContinuumProjectState.ERROR );
                    }
                }
                project.setOldState( project.getState() );

                projectDao.updateProject( project );

                project = projectDao.getProject( project.getId() );

                projectsToBeBuilt.add( project );
            }
            catch ( ContinuumStoreException e )
            {
                getLogger().error( "Error while creating build object", e );
            }
        }

        parallelBuildsManager.buildProjects( projectsToBeBuilt, projectsBuildDefinitionsMap, buildTrigger, scmResultMap,
                                             projectGroupId );
    }
}
