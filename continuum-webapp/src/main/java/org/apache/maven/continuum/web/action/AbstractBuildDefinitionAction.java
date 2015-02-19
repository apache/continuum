package org.apache.maven.continuum.web.action;

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
import org.apache.maven.continuum.web.model.BuildDefinitionSummary;

import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:olamy@apache.org">olamy</a>
 * @since 16 sept. 07
 */
public abstract class AbstractBuildDefinitionAction
    extends ContinuumConfirmAction
{

    protected BuildDefinitionSummary generateBuildDefinitionSummary( BuildDefinition buildDefinition )
    {
        BuildDefinitionSummary bds = new BuildDefinitionSummary();

        bds.setGoals( buildDefinition.getGoals() );
        bds.setId( buildDefinition.getId() );
        bds.setArguments( buildDefinition.getArguments() );
        bds.setBuildFile( buildDefinition.getBuildFile() );
        bds.setScheduleId( buildDefinition.getSchedule().getId() );
        bds.setScheduleName( buildDefinition.getSchedule().getName() );
        bds.setIsDefault( buildDefinition.isDefaultForProject() );
        bds.setIsBuildFresh( buildDefinition.isBuildFresh() );
        if ( buildDefinition.getProfile() != null )
        {
            bds.setProfileName( buildDefinition.getProfile().getName() );
            bds.setProfileId( buildDefinition.getProfile().getId() );
        }
        bds.setDescription( buildDefinition.getDescription() );
        bds.setType( buildDefinition.getType() );
        bds.setAlwaysBuild( buildDefinition.isAlwaysBuild() );
        return bds;
    }

    protected List<BuildDefinitionSummary> generateBuildDefinitionSummaries( List<BuildDefinition> buildDefinitions )
    {
        List<BuildDefinitionSummary> buildDefinitionSummaries = new LinkedList<BuildDefinitionSummary>();
        for ( BuildDefinition buildDefinition : buildDefinitions )
        {
            buildDefinitionSummaries.add( generateBuildDefinitionSummary( buildDefinition ) );
        }
        return buildDefinitionSummaries;
    }
}
