package org.apache.maven.continuum.xmlrpc;

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

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.xmlrpc.project.AddingResult;
import org.apache.maven.continuum.xmlrpc.project.BuildDefinition;
import org.apache.maven.continuum.xmlrpc.project.BuildResult;
import org.apache.maven.continuum.xmlrpc.project.BuildResultSummary;
import org.apache.maven.continuum.xmlrpc.project.Project;
import org.apache.maven.continuum.xmlrpc.project.ProjectGroup;
import org.apache.maven.continuum.xmlrpc.project.ProjectGroupSummary;
import org.apache.maven.continuum.xmlrpc.project.ProjectSummary;
import org.apache.maven.continuum.xmlrpc.project.Schedule;
import org.apache.maven.continuum.xmlrpc.system.Installation;
import org.apache.maven.continuum.xmlrpc.system.Profile;
import org.apache.maven.continuum.xmlrpc.system.SystemConfiguration;
import org.apache.xmlrpc.XmlRpcException;

import java.util.List;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public interface ContinuumService
{
    // ----------------------------------------------------------------------
    // Projects
    // ----------------------------------------------------------------------

    /**
     * Get All projects.
     *
     * @param projectGroupId The project group Id
     * @return List of {@link ProjectSummary}
     * @throws ContinuumException
     * @throws XmlRpcException
     */
    List<ProjectSummary> getProjects( int projectGroupId )
        throws ContinuumException, XmlRpcException;

    /**
     * Get a project.
     *
     * @param projectId the project id
     * @return The project summary
     * @throws ContinuumException
     * @throws XmlRpcException
     */
    ProjectSummary getProjectSummary( int projectId )
        throws ContinuumException, XmlRpcException;

    /**
     * Get a project with all details.
     *
     * @param projectId The project id
     * @return The project
     * @throws ContinuumException
     * @throws XmlRpcException
     */
    Project getProjectWithAllDetails( int projectId )
        throws ContinuumException, XmlRpcException;

    /**
     * Remove a project.
     *
     * @param projectId The project id
     * @throws ContinuumException
     * @throws XmlRpcException
     */
    int removeProject( int projectId )
        throws ContinuumException, XmlRpcException;

    /**
     * Update a project. Useful to change the scm parameters.
     *
     * @param project The project to update
     * @throws ContinuumException
     * @throws XmlRpcException
     */
    ProjectSummary updateProject( ProjectSummary project )
        throws ContinuumException, XmlRpcException;

    // ----------------------------------------------------------------------
    // Projects Groups
    // ----------------------------------------------------------------------

    /**
     * Get all project groups.
     *
     * @return All project groups
     * @throws ContinuumException
     * @throws XmlRpcException
     */
    List<ProjectGroupSummary> getAllProjectGroups()
        throws ContinuumException, XmlRpcException;

    /**
     * Get all project groups with all details (project summaries, notifiers, build definitions).
     *
     * @return All project groups
     * @throws ContinuumException
     * @throws XmlRpcException
     */
    List<ProjectGroup> getAllProjectGroupsWithAllDetails()
        throws ContinuumException, XmlRpcException;

    /**
     * Get all project groups with all details.
     *
     * @return All project groups
     * @throws ContinuumException
     * @throws XmlRpcException
     * @deprecated
     */
    List<ProjectGroup> getAllProjectGroupsWithProjects()
        throws ContinuumException, XmlRpcException;

    /**
     * Get a project group.
     *
     * @param projectGroupId The project group id
     * @return The project group summary
     * @throws ContinuumException
     * @throws XmlRpcException
     */
    ProjectGroupSummary getProjectGroupSummary( int projectGroupId )
        throws ContinuumException, XmlRpcException;

    /**
     * Get a project group with all details.
     *
     * @param projectGroupId The project group id
     * @return The project group
     * @throws ContinuumException
     * @throws XmlRpcException
     */
    ProjectGroup getProjectGroupWithProjects( int projectGroupId )
        throws ContinuumException, XmlRpcException;

    /**
     * Remove a project group.
     *
     * @param projectGroupId The project group id
     * @throws ContinuumException
     * @throws XmlRpcException
     */
    int removeProjectGroup( int projectGroupId )
        throws ContinuumException, XmlRpcException;

    /**
     * Update a project Group.
     *
     * @param projectGroup The project group to update
     * @throws ContinuumException
     * @throws XmlRpcException
     */
    ProjectGroupSummary updateProjectGroup( ProjectGroupSummary projectGroup )
        throws ContinuumException, XmlRpcException;

    // ----------------------------------------------------------------------
    // Build Definitions
    // ----------------------------------------------------------------------

    /**
     * Get the build definitions list of the project.
     *
     * @param projectId The project id
     * @return The build definitions list
     * @throws ContinuumException
     * @throws XmlRpcException
     */
    List<BuildDefinition> getBuildDefinitionsForProject( int projectId )
        throws ContinuumException, XmlRpcException;

    /**
     * Get the build definitions list of the project group.
     *
     * @param projectGroupId The project group id
     * @return The build definitions list
     * @throws ContinuumException
     * @throws XmlRpcException
     */
    List<BuildDefinition> getBuildDefinitionsForProjectGroup( int projectGroupId )
        throws ContinuumException, XmlRpcException;

    /**
     * Update a project build definition.
     *
     * @param projectId The project id
     * @param buildDef  The build defintion to update
     * @return the updated build definition
     * @throws ContinuumException
     * @throws XmlRpcException
     */
    BuildDefinition updateBuildDefinitionForProject( int projectId, BuildDefinition buildDef )
        throws ContinuumException, XmlRpcException;

    /**
     * Update a project group build definition.
     *
     * @param projectGroupId The project group id
     * @param buildDef       The build defintion to update
     * @return the updated build definition
     * @throws ContinuumException
     * @throws XmlRpcException
     */
    BuildDefinition updateBuildDefinitionForProjectGroup( int projectGroupId, BuildDefinition buildDef )
        throws ContinuumException, XmlRpcException;

    /**
     * Add a project build definition.
     *
     * @param projectId The project id
     * @param buildDef  The build defintion to update
     * @return the added build definition
     * @throws ContinuumException
     * @throws XmlRpcException
     */
    BuildDefinition addBuildDefinitionToProject( int projectId, BuildDefinition buildDef )
        throws ContinuumException, XmlRpcException;

    /**
     * Add a project group buildDefinition.
     *
     * @param projectGroupId The project group id
     * @param buildDef       The build defintion to update
     * @return the build definition added
     * @throws ContinuumException
     * @throws XmlRpcException
     */
    BuildDefinition addBuildDefinitionToProjectGroup( int projectGroupId, BuildDefinition buildDef )
        throws ContinuumException, XmlRpcException;

    // ----------------------------------------------------------------------
    // Building
    // ----------------------------------------------------------------------

    /**
     * Add the project to the build queue.
     *
     * @param projectId The project id
     * @throws ContinuumException
     * @throws XmlRpcException
     */
    int addProjectToBuildQueue( int projectId )
        throws ContinuumException, XmlRpcException;

    /**
     * Add the project to the build queue.
     *
     * @param projectId         The project id
     * @param buildDefinitionId The build definition id
     * @throws ContinuumException
     * @throws XmlRpcException
     */
    int addProjectToBuildQueue( int projectId, int buildDefinitionId )
        throws ContinuumException, XmlRpcException;

    /**
     * Build the project
     *
     * @param projectId The project id
     * @throws ContinuumException
     * @throws XmlRpcException
     */
    int buildProject( int projectId )
        throws ContinuumException, XmlRpcException;

    /**
     * Build the project
     *
     * @param projectId         The project id
     * @param buildDefinitionId The build definition id
     * @throws ContinuumException
     * @throws XmlRpcException
     */
    int buildProject( int projectId, int buildDefinitionId )
        throws ContinuumException, XmlRpcException;

    /**
     * Build the project group with the default build definition.
     *
     * @param projectGroupId The project group id
     * @throws ContinuumException
     * @throws XmlRpcException
     */
    int buildGroup( int projectGroupId )
        throws ContinuumException, XmlRpcException;

    /**
     * Build the project group with the specified build definition.
     *
     * @param projectGroupId    The project group id
     * @param buildDefinitionId The build definition id
     * @throws ContinuumException
     * @throws XmlRpcException
     */
    int buildGroup( int projectGroupId, int buildDefinitionId )
        throws ContinuumException, XmlRpcException;
    // ----------------------------------------------------------------------
    // Build Results
    // ----------------------------------------------------------------------

    /**
     * Returns the latest build result for the project.
     *
     * @param projectId The project id
     * @return The build result
     * @throws ContinuumException
     * @throws XmlRpcException
     */
    BuildResult getLatestBuildResult( int projectId )
        throws ContinuumException, XmlRpcException;

    /**
     * Returns the build result.
     *
     * @param projectId The project id
     * @param buildId   The build id
     * @return The build result
     * @throws ContinuumException
     * @throws XmlRpcException
     */
    BuildResult getBuildResult( int projectId, int buildId )
        throws ContinuumException, XmlRpcException;

    /**
     * Returns the project build result summary list.
     *
     * @param projectId The project id
     * @return The build result list
     * @throws ContinuumException
     * @throws XmlRpcException
     */
    List<BuildResultSummary> getBuildResultsForProject( int projectId )
        throws ContinuumException, XmlRpcException;

    /**
     * Remove the project build result.
     *
     * @param br The project build result
     * @return 0
     * @throws ContinuumException
     * @throws XmlRpcException
     */
    int removeBuildResult( BuildResult br )
        throws ContinuumException, XmlRpcException;

    /**
     * Returns the build output.
     *
     * @param projectId The project id
     * @param buildId   The build id
     * @return The build output
     * @throws ContinuumException
     * @throws XmlRpcException
     */
    String getBuildOutput( int projectId, int buildId )
        throws ContinuumException, XmlRpcException;

    // ----------------------------------------------------------------------
    // Maven 2.x projects
    // ----------------------------------------------------------------------

    /**
     * Add a maven 2.x project from an url.
     *
     * @param url The POM url
     * @return The result of the action with the list of projects created
     * @throws ContinuumException
     * @throws XmlRpcException
     */
    AddingResult addMavenTwoProject( String url )
        throws ContinuumException, XmlRpcException;

    /**
     * Add a maven 2.x project from an url.
     *
     * @param url            The POM url
     * @param projectGroupId The id of the group where projects will be stored
     * @return The result of the action with the list of projects created
     * @throws ContinuumException
     * @throws XmlRpcException
     */
    AddingResult addMavenTwoProject( String url, int projectGroupId )
        throws ContinuumException, XmlRpcException;

    // ----------------------------------------------------------------------
    // Maven 1.x projects
    // ----------------------------------------------------------------------

    /**
     * Add a maven 1.x project from an url.
     *
     * @param url The POM url
     * @return The result of the action with the list of projects created
     * @throws ContinuumException
     * @throws XmlRpcException
     */
    AddingResult addMavenOneProject( String url )
        throws ContinuumException, XmlRpcException;

    /**
     * Add a maven 1.x project from an url.
     *
     * @param url            The POM url
     * @param projectGroupId The id of the group where projects will be stored
     * @return The result of the action with the list of projects created
     * @throws ContinuumException
     * @throws XmlRpcException
     */
    AddingResult addMavenOneProject( String url, int projectGroupId )
        throws ContinuumException, XmlRpcException;

    // ----------------------------------------------------------------------
    // Maven ANT projects
    // ----------------------------------------------------------------------

    /**
     * Add an ANT project.
     *
     * @param project The project to add. name, version and scm informations are required
     * @return The project populated with the id.
     * @throws ContinuumException
     * @throws XmlRpcException
     */
    ProjectSummary addAntProject( ProjectSummary project )
        throws ContinuumException, XmlRpcException;

    /**
     * Add an ANT project in the specified group.
     *
     * @param project        The project to add. name, version and scm informations are required
     * @param projectGroupId The id of the group where projects will be stored
     * @return The project populated with the id.
     * @throws ContinuumException
     * @throws XmlRpcException
     */
    ProjectSummary addAntProject( ProjectSummary project, int projectGroupId )
        throws ContinuumException, XmlRpcException;

    // ----------------------------------------------------------------------
    // Maven Shell projects
    // ----------------------------------------------------------------------

    /**
     * Add an shell project.
     *
     * @param project The project to add. name, version and scm informations are required
     * @return The project populated with the id.
     * @throws ContinuumException
     * @throws XmlRpcException
     */
    ProjectSummary addShellProject( ProjectSummary project )
        throws ContinuumException, XmlRpcException;

    /**
     * Add an shell project in the specified group.
     *
     * @param project        The project to add. name, version and scm informations are required
     * @param projectGroupId The id of the group where projects will be stored
     * @return The project populated with the id.
     * @throws ContinuumException
     * @throws XmlRpcException
     */
    ProjectSummary addShellProject( ProjectSummary project, int projectGroupId )
        throws ContinuumException, XmlRpcException;

    // ----------------------------------------------------------------------
    // ADMIN TASKS
    // ----------------------------------------------------------------------

    // ----------------------------------------------------------------------
    // Schedules
    // ----------------------------------------------------------------------

    /**
     * Return the schedules list.
     *
     * @return The schedule list.
     * @throws ContinuumException
     * @throws XmlRpcException
     */
    List<Schedule> getSchedules()
        throws ContinuumException, XmlRpcException;

    /**
     * Return the schedule defined by this id.
     *
     * @param scheduleId The schedule id
     * @return The schedule.
     * @throws ContinuumException
     * @throws XmlRpcException
     */
    Schedule getSchedule( int scheduleId )
        throws ContinuumException, XmlRpcException;

    /**
     * Add the schedule.
     *
     * @param schedule The schedule
     * @return The schedule.
     * @throws ContinuumException
     * @throws XmlRpcException
     */
    Schedule addSchedule( Schedule schedule )
        throws ContinuumException, XmlRpcException;

    /**
     * Update the schedule.
     *
     * @param schedule The schedule
     * @return The schedule.
     * @throws ContinuumException
     * @throws XmlRpcException
     */
    Schedule updateSchedule( Schedule schedule )
        throws ContinuumException, XmlRpcException;

    // ----------------------------------------------------------------------
    // Profiles
    // ----------------------------------------------------------------------

    /**
     * Return the profiles list.
     *
     * @return The profiles list.
     * @throws ContinuumException
     * @throws XmlRpcException
     */
    List<Profile> getProfiles()
        throws ContinuumException, XmlRpcException;

    /**
     * Return the profile defined by this id.
     *
     * @param profileId The profile id
     * @return The profile.
     * @throws ContinuumException
     * @throws XmlRpcException
     */
    Profile getProfile( int profileId )
        throws ContinuumException, XmlRpcException;

    // ----------------------------------------------------------------------
    // Installations
    // ----------------------------------------------------------------------

    /**
     * Return the installations list.
     *
     * @return The installations list.
     * @throws ContinuumException
     * @throws XmlRpcException
     */
    List<Installation> getInstallations()
        throws ContinuumException, XmlRpcException;

    /**
     * Return the installation defined by this id.
     *
     * @param installationId The installation id
     * @return The installation.
     * @throws ContinuumException
     * @throws XmlRpcException
     */
    Installation getInstallation( int installationId )
        throws ContinuumException, XmlRpcException;

    // ----------------------------------------------------------------------
    // SystemConfiguration
    // ----------------------------------------------------------------------

    SystemConfiguration getSystemConfiguration()
        throws ContinuumException, XmlRpcException;

    // ----------------------------------------------------------------------
    // TODO:Users
    // ----------------------------------------------------------------------

    // ----------------------------------------------------------------------
    // Utils
    // ----------------------------------------------------------------------

    boolean ping()
        throws ContinuumException;
}
