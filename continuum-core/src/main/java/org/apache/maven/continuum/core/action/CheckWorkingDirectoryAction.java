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

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.continuum.dao.ProjectDao;
import org.apache.continuum.model.project.ProjectScmRoot;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.utils.WorkingDirectoryService;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 * @plexus.component role="org.codehaus.plexus.action.Action"
 * role-hint="check-working-directory"
 */
public class CheckWorkingDirectoryAction
    extends AbstractContinuumAction
{
    /**
     * @plexus.requirement
     */
    private WorkingDirectoryService workingDirectoryService;

    /**
     * @plexus.requirement
     */
    private ProjectDao projectDao;

    public void execute( Map context )
        throws Exception
    {
        Project project = projectDao.getProject( getProjectId( context ) );
        List<Project> projectsWithSimilarScmRoot = getListOfProjectsInGroupWithSimilarScmRoot( context );
        ProjectScmRoot projectScmRoot = getProjectScmRoot( context );

       // File workingDirectory = workingDirectoryService.getWorkingDirectory( project );
        File workingDirectory =
            workingDirectoryService.getWorkingDirectory( project, projectScmRoot.getScmRootAddress(),
                                                         projectsWithSimilarScmRoot );

        if ( !workingDirectory.exists() )
        {
            context.put( KEY_WORKING_DIRECTORY_EXISTS, Boolean.FALSE );

            return;
        }

        File[] files = workingDirectory.listFiles();

        context.put( KEY_WORKING_DIRECTORY_EXISTS, files.length > 0 );
    }
}
