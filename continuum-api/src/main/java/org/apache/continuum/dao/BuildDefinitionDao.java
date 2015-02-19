package org.apache.continuum.dao;

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
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.store.ContinuumStoreException;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 */
public interface BuildDefinitionDao
{
    BuildDefinition getBuildDefinition( int buildDefinitionId )
        throws ContinuumStoreException;

    void removeBuildDefinition( BuildDefinition buildDefinition )
        throws ContinuumStoreException;

    BuildDefinition storeBuildDefinition( BuildDefinition buildDefinition )
        throws ContinuumStoreException;

    BuildDefinition addBuildDefinition( BuildDefinition buildDefinition )
        throws ContinuumStoreException;

    List<BuildDefinition> getAllBuildDefinitions()
        throws ContinuumStoreException;

    /**
     * Returns the default build definition of all projects. The key is the project id and the value is the build
     * definition id.
     *
     * @return a map of all default build definitions
     */
    Map<Integer, Integer> getDefaultBuildDefinitions();

    /**
     * returns the default build definitions for the project group and there
     * should always be at least one declared.
     *
     * @param projectGroupId The project group id
     * @return The list of default build definitions
     * @throws ContinuumStoreException if the build definitions list can't be obtain
     */
    List<BuildDefinition> getDefaultBuildDefinitionsForProjectGroup( int projectGroupId )
        throws ContinuumStoreException;

    /**
     * returns the default build definitions for the project group and there
     * should always be at least one declared.
     *
     * @param projectGroup The project group
     * @return The list of default build definitions
     * @throws ContinuumStoreException if the build definitions list can't be obtain
     */
    List<BuildDefinition> getDefaultBuildDefinitionsForProjectGroup( ProjectGroup projectGroup )
        throws ContinuumStoreException;

    /**
     * returns the default build definition of the project, if the project
     * doesn't have on declared the default of the project group will be
     * returned <p/> this should be the most common usage of the default build
     * definition accessing methods
     *
     * @param projectId
     * @return
     * @throws ContinuumStoreException
     * @throws org.apache.maven.continuum.store.ContinuumObjectNotFoundException
     *
     */
    BuildDefinition getDefaultBuildDefinitionForProject( int projectId )
        throws ContinuumStoreException;

    /**
     * returns the default build definition for the project without consulting
     * the project group
     *
     * @param project
     * @return
     * @throws ContinuumStoreException
     * @throws org.apache.maven.continuum.store.ContinuumObjectNotFoundException
     *
     */
    BuildDefinition getDefaultBuildDefinitionForProject( Project project )
        throws ContinuumStoreException;

    /**
     * returns the default build definition of the project, if the project
     * doesn't have on declared the default of the project group will be
     * returned <p/> this should be the most common usage of the default build
     * definition accessing methods
     *
     * @param projectId
     * @return
     * @throws ContinuumStoreException
     * @throws org.apache.maven.continuum.store.ContinuumObjectNotFoundException
     *
     */
    BuildDefinition getDefaultBuildDefinition( int projectId )
        throws ContinuumStoreException;

    List<BuildDefinition> getAllTemplates()
        throws ContinuumStoreException;

    List<BuildDefinition> getBuildDefinitionsBySchedule( int scheduleId );
}
