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

import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildDefinitionTemplate;
import org.apache.maven.continuum.model.project.ProjectGroup;

import java.util.Iterator;
import java.util.Map;

/**
 * AddBuildDefinitionToProjectAction:
 *
 * @author Jesse McConnell <jmcconnell@apache.org>
 * @version $Id$
 * @plexus.component role="org.codehaus.plexus.action.Action"
 * role-hint="add-build-definition-to-project-group"
 */
public class AddBuildDefinitionToProjectGroupAction
    extends AbstractBuildDefinitionContinuumAction
{

    public void execute( Map map )
        throws Exception
    {
        int projectGroupId = getProjectGroupId( map );
        ProjectGroup projectGroup = store.getProjectGroupWithBuildDetailsByProjectGroupId( projectGroupId );
        BuildDefinitionTemplate buildDefinitionTemplate = getBuildDefinitionTemplate( map );
        if ( buildDefinitionTemplate != null )
        {
            for ( Iterator<BuildDefinition> iterator = buildDefinitionTemplate.getBuildDefinitions().iterator(); iterator
                .hasNext(); )
            {
                BuildDefinition buildDefinition = iterator.next();
                resolveDefaultBuildDefinitionsForProjectGroup( buildDefinition, projectGroup );

                projectGroup.addBuildDefinition( buildDefinition );

                store.updateProjectGroup( projectGroup );
            }
        }
        else
        {
            BuildDefinition buildDefinition = getBuildDefinition( map );

            resolveDefaultBuildDefinitionsForProjectGroup( buildDefinition, projectGroup );

            projectGroup.addBuildDefinition( buildDefinition );

            store.updateProjectGroup( projectGroup );
        }
        //map.put( AbstractContinuumAction.KEY_BUILD_DEFINITION, buildDefinition );
    }
}
