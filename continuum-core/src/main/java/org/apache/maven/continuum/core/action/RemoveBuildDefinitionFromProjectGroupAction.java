package org.apache.maven.continuum.core.action;

import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.ContinuumException;

import java.util.Map;
/*
 * Copyright 2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * AddBuildDefinitionToProjectAction:
 *
 * @author: Jesse McConnell <jmcconnell@apache.org>
 * @version: $ID:$
 *
 * @plexus.component
 *   role="org.codehaus.plexus.action.Action"
 *   role-hint="remove-build-definition-from-project-group"
 */
public class RemoveBuildDefinitionFromProjectGroupAction
    extends AbstractBuildDefinitionContinuumAction
{

    public void execute( Map map )
        throws Exception
    {
        BuildDefinition buildDefinition = getBuildDefinition( map );
        int projectGroupId =  getProjectGroupId( map );

        ProjectGroup projectGroup = store.getProjectGroupWithBuildDetails( projectGroupId );

        if ( buildDefinition.isDefaultForProject() )
        {
            throw new ContinuumException( "can't remove default build definition from project group" );
        }

        projectGroup.removeBuildDefinition( buildDefinition );

        store.updateProjectGroup( projectGroup );
    }
}
