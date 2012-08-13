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

import org.apache.continuum.dao.ProjectDao;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.utils.WorkingDirectoryService;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * @author Jesse McConnell <jmcconnell@apache.org>
 * @version $Id$
 * @plexus.component role="org.codehaus.plexus.action.Action"
 * role-hint="clean-working-directory"
 */
public class CleanWorkingDirectoryAction
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
        List<Project> projectsWithCommonScmRoot = getListOfProjectsInGroupWithCommonScmRoot( context );
        String projectScmRootUrl = getProjectScmRootUrl( context, project.getScmUrl() );

        File workingDirectory = workingDirectoryService.getWorkingDirectory( project, projectScmRootUrl,
                                                                             projectsWithCommonScmRoot );

        if ( workingDirectory.exists() )
        {
            FileSetManager fileSetManager = new FileSetManager();
            FileSet fileSet = new FileSet();
            fileSet.setDirectory( workingDirectory.getPath() );
            fileSet.addInclude( "**/**" );
            // TODO : this with a configuration option somewhere ?
            fileSet.setFollowSymlinks( false );
            fileSetManager.delete( fileSet );
        }
    }
}
