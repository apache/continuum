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
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.execution.manager.BuildExecutorManager;
import org.apache.maven.continuum.model.project.Project;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
@Component( role = org.codehaus.plexus.action.Action.class, hint = "validate-project" )
public class ValidateProject
    extends AbstractValidationContinuumAction
{

    @Requirement
    private BuildExecutorManager buildExecutorManager;

    @Requirement
    private ProjectDao projectDao;

    public void execute( Map context )
        throws Exception
    {
        Project project = getUnvalidatedProject( context );

        // ----------------------------------------------------------------------
        // Make sure that the builder id is correct before starting to check
        // stuff out
        // ----------------------------------------------------------------------

        if ( !buildExecutorManager.hasBuildExecutor( project.getExecutorId() ) )
        {
            throw new ContinuumException( "No such executor with id '" + project.getExecutorId() + "'." );
        }

        List<Project> projects = projectDao.getAllProjectsByName();

        for ( Project storedProject : projects )
        {
            // CONTINUUM-1445
            if ( StringUtils.equalsIgnoreCase( project.getName(), storedProject.getName() ) &&
                StringUtils.equalsIgnoreCase( project.getVersion(), storedProject.getVersion() ) &&
                StringUtils.equalsIgnoreCase( project.getScmUrl(), storedProject.getScmUrl() ) )
            {
                throw new ContinuumException( "A duplicate project already exist '" + storedProject.getName() + "'." );
            }
        }
        /*
        if ( store.getProjectByName( project.getName() ) != null )
        {
            throw new ContinuumException( "A project with the name '" + project.getName() + "' already exist." );
        }
        */

        //        if ( getProjectByScmUrl( scmUrl ) != null )
        //        {
        //            throw new ContinuumStoreException( "A project with the scm url '" + scmUrl + "' already exist." );
        //        }

        // TODO: Enable
        //        assertStringNotEmpty( project.getPath(), "path" );
        //        assertStringNotEmpty( project.getGroupId(), "group id" );
        //        assertStringNotEmpty( project.getArtifactId(), "artifact id" );

        //        if ( project.getProjectGroup() == null )
        //        {
        //            throw new ContinuumException( "A project has to belong to a project group." );
        //        }

        // TODO: validate that the SCM provider id
    }
}
