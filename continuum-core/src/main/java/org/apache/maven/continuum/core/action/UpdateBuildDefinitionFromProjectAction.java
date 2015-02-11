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
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Project;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import java.util.Map;

/**
 * AddBuildDefinitionToProjectAction:
 *
 * @author Jesse McConnell <jmcconnell@apache.org>
 */
@Component( role = org.codehaus.plexus.action.Action.class, hint = "update-build-definition-from-project" )
public class UpdateBuildDefinitionFromProjectAction
    extends AbstractBuildDefinitionContinuumAction
{

    @Requirement
    private ProjectDao projectDao;

    public void execute( Map context )
        throws Exception
    {
        BuildDefinition buildDefinition = getBuildDefinition( context );
        int projectId = getProjectId( context );

        Project project = projectDao.getProjectWithAllDetails( projectId );

        resolveDefaultBuildDefinitionsForProject( buildDefinition, project );

        updateBuildDefinitionInList( project.getBuildDefinitions(), buildDefinition );

        AbstractContinuumAction.setBuildDefinition( context, buildDefinition );
    }

}
