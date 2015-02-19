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

import org.apache.continuum.model.project.ProjectGroupSummary;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.store.ContinuumObjectNotFoundException;
import org.apache.maven.continuum.store.ContinuumStoreException;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 */
public interface ProjectDao
{
    void removeProject( Project project );

    void updateProject( Project project )
        throws ContinuumStoreException;

    Project getProject( int projectId )
        throws ContinuumStoreException;

    Project getProject( String groupId, String artifactId, String version )
        throws ContinuumStoreException;

    Project getProjectByName( String name )
        throws ContinuumStoreException;

    List<Project> getProjectsWithDependenciesByGroupId( int projectGroupId );

    Project getProjectWithBuilds( int projectId )
        throws ContinuumStoreException;

    Project getProjectWithBuildDetails( int projectId )
        throws ContinuumStoreException;

    Project getProjectWithCheckoutResult( int projectId )
        throws ContinuumStoreException;

    List<Project> getProjectsInGroup( int projectGroupId )
        throws ContinuumStoreException;

    List<Project> getProjectsInGroupWithDependencies( int projectGroupId )
        throws ContinuumStoreException;

    Project getProjectWithAllDetails( int projectId )
        throws ContinuumStoreException;

    List<Project> getAllProjectsByName();

    List<Project> getAllProjectsByNameWithDependencies();

    List<Project> getAllProjectsByNameWithBuildDetails();

    ProjectGroup getProjectGroupByProjectId( int projectId )
        throws ContinuumObjectNotFoundException;

    Project getProjectWithDependencies( int projectId )
        throws ContinuumStoreException;

    Map<Integer, ProjectGroupSummary> getProjectsSummary();
}
