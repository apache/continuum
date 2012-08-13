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

import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.store.ContinuumObjectNotFoundException;
import org.apache.maven.continuum.store.ContinuumStoreException;

import java.util.Collection;
import java.util.List;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public interface ProjectGroupDao
{
    /**
     * Add the project group.
     *
     * @param group The project group
     * @return The project group added
     */
    ProjectGroup addProjectGroup( ProjectGroup group );

    /**
     * Remove the project group.
     *
     * @param projectGroup the project group to remove
     */
    void removeProjectGroup( ProjectGroup projectGroup );

    /**
     * Return the project group associated to the project group id parameter.
     *
     * @param projectGroupId The project group id
     * @return The project group
     * @throws org.apache.maven.continuum.store.ContinuumStoreException
     *          if the project group can't be obtain
     */
    ProjectGroup getProjectGroup( int projectGroupId )
        throws ContinuumStoreException, ContinuumObjectNotFoundException;

    /**
     * Return the project group associated to the groupId parameter.
     *
     * @param groupId The group id
     * @return The project group
     * @throws ContinuumStoreException if the project group can't be obtain
     */
    ProjectGroup getProjectGroupByGroupId( String groupId )
        throws ContinuumStoreException;

    /**
     * Return the project group associated to the groupId parameter.
     *
     * @param groupId The group id
     * @return The project group
     * @throws ContinuumStoreException if the project group can't be obtain
     */
    ProjectGroup getProjectGroupByGroupIdWithBuildDetails( String groupId )
        throws ContinuumStoreException;

    /**
     * Return the project group associated to the groupId parameter.
     *
     * @param groupId The group id
     * @return The project group
     * @throws ContinuumStoreException if the project group can't be obtain
     */
    ProjectGroup getProjectGroupByGroupIdWithProjects( String groupId )
        throws ContinuumStoreException;

    /**
     * Return the project group of the project.
     *
     * @param projectId The project id
     * @return The project group
     * @throws ContinuumObjectNotFoundException
     *          if the project group can't be obtain
     */
    ProjectGroup getProjectGroupByProjectId( int projectId )
        throws ContinuumObjectNotFoundException;

    /**
     * Return the project group of the project.
     *
     * @param project The project
     * @return The project group
     * @throws ContinuumObjectNotFoundException
     *          if the project group can't be obtain
     */
    ProjectGroup getProjectGroupByProject( Project project )
        throws ContinuumObjectNotFoundException;

    /**
     * Save the modified project group.
     *
     * @param projectGroup The project group
     * @throws ContinuumStoreException if the project group can't be saved
     */
    void updateProjectGroup( ProjectGroup projectGroup )
        throws ContinuumStoreException;

    /**
     * Return the project group with projects populated.
     *
     * @param projectGroupId The project group id
     * @return All project groups
     * @throws ContinuumStoreException if the project group can't be obtain
     */
    ProjectGroup getProjectGroupWithProjects( int projectGroupId )
        throws ContinuumStoreException;

    /**
     * Return all project groups with projects populated.
     *
     * @return All project groups
     */
    Collection<ProjectGroup> getAllProjectGroupsWithProjects();

    /**
     * Return all project groups with build details populated.
     *
     * @return All project groups
     */
    List<ProjectGroup> getAllProjectGroupsWithBuildDetails();

    /**
     * Return all project groups.
     *
     * @return All project groups
     */
    Collection<ProjectGroup> getAllProjectGroups();

    /**
     * Return all project groups with all associated objects populated. This method return the majority of the database.
     *
     * @return all project groups
     */
    Collection<ProjectGroup> getAllProjectGroupsWithTheLot();

    /**
     * Return the project group with associated build details populated.
     *
     * @param projectGroupId the project group id
     * @return the project group
     * @throws ContinuumStoreException if the project group can't be obtain
     */
    ProjectGroup getProjectGroupWithBuildDetailsByProjectGroupId( int projectGroupId )
        throws ContinuumStoreException;

    List<ProjectGroup> getProjectGroupByRepository( int repositoryId );
}
