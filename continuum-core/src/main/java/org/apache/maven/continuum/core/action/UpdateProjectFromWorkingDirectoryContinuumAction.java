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

import org.apache.continuum.dao.BuildDefinitionDao;
import org.apache.continuum.dao.ProjectDao;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.execution.ContinuumBuildExecutor;
import org.apache.maven.continuum.execution.ContinuumBuildExecutorException;
import org.apache.maven.continuum.execution.manager.BuildExecutorManager;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.scm.ScmResult;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.apache.maven.continuum.utils.WorkingDirectoryService;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 * @plexus.component role="org.codehaus.plexus.action.Action"
 * role-hint="update-project-from-working-directory"
 */
public class UpdateProjectFromWorkingDirectoryContinuumAction
    extends AbstractContinuumAction
{
    /**
     * @plexus.requirement
     */
    private WorkingDirectoryService workingDirectoryService;

    /**
     * @plexus.requirement
     */
    private BuildExecutorManager buildExecutorManager;

    /**
     * @plexus.requirement
     */
    private BuildDefinitionDao buildDefinitionDao;

    /**
     * @plexus.requirement
     */
    private ProjectDao projectDao;

    public void execute( Map context )
        throws ContinuumStoreException, ContinuumException, ContinuumBuildExecutorException
    {
        Project project = getProject( context );

        project = projectDao.getProjectWithAllDetails( project.getId() );

        getLogger().info( "Updating project '" + project.getName() + "' from checkout." );

        BuildDefinition buildDefinition = buildDefinitionDao.getBuildDefinition( getBuildDefinitionId( context ) );

        // ----------------------------------------------------------------------
        // Make a new descriptor
        // ----------------------------------------------------------------------

        ContinuumBuildExecutor builder = buildExecutorManager.getBuildExecutor( project.getExecutorId() );

        ScmResult scmResult = (ScmResult) context.get( "scmResult" );
        List<Project> projectsWithCommonScmRoot = getListOfProjectsInGroupWithCommonScmRoot( context );
        String projectScmRootUrl = getProjectScmRootUrl( context, project.getScmUrl() );

        builder.updateProjectFromCheckOut( workingDirectoryService.getWorkingDirectory( project, projectScmRootUrl,
                                                                                        projectsWithCommonScmRoot ),
                                           project, buildDefinition, scmResult );
        // ----------------------------------------------------------------------
        // Store the new descriptor
        // ----------------------------------------------------------------------

        projectDao.updateProject( project );
    }
}
