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
import org.apache.continuum.dao.ScheduleDao;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.project.Schedule;
import org.apache.maven.continuum.store.ContinuumObjectNotFoundException;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.component.annotations.Requirement;

import java.util.List;

/**
 * AbstractBuildDefinitionContinuumAction:
 *
 * @author Jesse McConnell <jmcconnell@apache.org>
 */
public abstract class AbstractBuildDefinitionContinuumAction
    extends AbstractContinuumAction
{
    @Requirement
    private BuildDefinitionDao buildDefinitionDao;

    @Requirement
    private ScheduleDao scheduleDao;

    protected void resolveDefaultBuildDefinitionsForProject( BuildDefinition buildDefinition, Project project )
        throws ContinuumException
    {
        try
        {
            // if buildDefinition passed in is not default then we are done
            if ( buildDefinition.isDefaultForProject() )
            {
                BuildDefinition storedDefinition = buildDefinitionDao.getDefaultBuildDefinitionForProject(
                    project.getId() );

                if ( storedDefinition != null )
                {
                    storedDefinition.setDefaultForProject( false );

                    buildDefinitionDao.storeBuildDefinition( storedDefinition );
                }
            }
        }
        catch ( ContinuumObjectNotFoundException nfe )
        {
            getLogger().debug( getClass().getName() +
                                   ": safely ignoring the resetting of old build definition becuase it didn't exist" );
        }
        catch ( ContinuumStoreException cse )
        {
            throw new ContinuumException( "error updating old default build definition", cse );
        }
    }

    /**
     * resolves build definition defaults between project groups and projects
     * <p/>
     * 1) project groups have default build definitions
     * 2) if project has default build definition, that overrides project group definition
     * 3) changing parent default build definition does not effect project if it has a default declared
     * 4) project groups must have a default build definition
     *
     * @param buildDefinition
     * @param projectGroup
     * @throws ContinuumException
     */
    protected void resolveDefaultBuildDefinitionsForProjectGroup( BuildDefinition buildDefinition,
                                                                  ProjectGroup projectGroup )
        throws ContinuumException
    {
        try
        {
            List<BuildDefinition> storedDefinitions = buildDefinitionDao.getDefaultBuildDefinitionsForProjectGroup(
                projectGroup.getId() );

            for ( BuildDefinition storedDefinition : storedDefinitions )
            {
                // if buildDefinition passed in is not default then we are done
                if ( buildDefinition.isDefaultForProject() )
                {
                    if ( storedDefinition != null && storedDefinition.getId() != buildDefinition.getId() )
                    {
                        if ( buildDefinition.getType() != null && buildDefinition.getType().equals(
                            storedDefinition.getType() ) )
                        {
                            //Required to get build def from store because storedDefinition is readonly
                            BuildDefinition def = buildDefinitionDao.getBuildDefinition( storedDefinition.getId() );
                            def.setDefaultForProject( false );

                            buildDefinitionDao.storeBuildDefinition( def );
                        }
                    }
                }
                else
                {
                    //make sure we are not wacking out default build definition, that would be bad
                    if ( buildDefinition.getId() == storedDefinition.getId() )
                    {
                        getLogger().info(
                            "processing this build definition would result in no default build definition for project group" );
                        throw new ContinuumException(
                            "processing this build definition would result in no default build definition for project group" );
                    }
                }
            }
        }
        catch ( ContinuumStoreException cse )
        {
            getLogger().info( "error updating old default build definition", cse );
            throw new ContinuumException( "error updating old default build definition", cse );
        }
    }

    /**
     * attempts to walk through the list of build definitions and upon finding a match update it with the
     * information in the BuildDefinition object passed in.
     *
     * @param buildDefinitions
     * @param buildDefinition
     * @throws ContinuumException
     */
    protected void updateBuildDefinitionInList( List<BuildDefinition> buildDefinitions,
                                                BuildDefinition buildDefinition )
        throws ContinuumException
    {
        try
        {
            BuildDefinition storedDefinition = null;

            for ( BuildDefinition bd : buildDefinitions )
            {
                if ( bd.getId() == buildDefinition.getId() )
                {
                    storedDefinition = bd;
                }
            }

            if ( storedDefinition != null )
            {
                storedDefinition.setGoals( buildDefinition.getGoals() );
                storedDefinition.setArguments( buildDefinition.getArguments() );
                storedDefinition.setBuildFile( buildDefinition.getBuildFile() );
                storedDefinition.setBuildFresh( buildDefinition.isBuildFresh() );
                storedDefinition.setUpdatePolicy( buildDefinition.getUpdatePolicy() );

                // special case of this is resolved in the resolveDefaultBuildDefinitionsForProjectGroup method
                storedDefinition.setDefaultForProject( buildDefinition.isDefaultForProject() );

                Schedule schedule;
                if ( buildDefinition.getSchedule() == null )
                {
                    try
                    {
                        schedule = scheduleDao.getScheduleByName( ConfigurationService.DEFAULT_SCHEDULE_NAME );
                    }
                    catch ( ContinuumStoreException e )
                    {
                        throw new ContinuumException( "Can't get default schedule.", e );
                    }
                }
                else
                {
                    schedule = scheduleDao.getSchedule( buildDefinition.getSchedule().getId() );
                }

                storedDefinition.setSchedule( schedule );

                storedDefinition.setProfile( buildDefinition.getProfile() );

                storedDefinition.setDescription( buildDefinition.getDescription() );

                storedDefinition.setType( buildDefinition.getType() );

                storedDefinition.setAlwaysBuild( buildDefinition.isAlwaysBuild() );

                buildDefinitionDao.storeBuildDefinition( storedDefinition );
            }
            else
            {
                throw new ContinuumException( "failed update, build definition didn't exist in project group" );
            }
        }
        catch ( ContinuumStoreException cse )
        {
            throw new ContinuumException( "error in accessing or storing build definition" );
        }
    }
}
