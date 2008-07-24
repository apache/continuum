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

import java.util.List;

import org.apache.maven.continuum.xmlrpc.project.AddingResult;
import org.apache.maven.continuum.xmlrpc.project.BuildDefinition;
import org.apache.maven.continuum.xmlrpc.project.BuildDefinitionTemplate;
import org.apache.maven.continuum.xmlrpc.project.BuildProjectTask;
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
     * @throws Exception
     * @throws XmlRpcException
     */
    List<ProjectSummary> getProjects( int projectGroupId )
        throws Exception;

    /**
     * Get a project.
     *
     * @param projectId the project id
     * @return The project summary
     * @throws Exception
     */
    ProjectSummary getProjectSummary( int projectId )
        throws Exception;

    /**
     * Get a project with all details.
     *
     * @param projectId The project id
     * @return The project
     * @throws Exception
     */
    Project getProjectWithAllDetails( int projectId )
        throws Exception;

    /**
     * Remove a project.
     *
     * @param projectId The project id
     * @throws Exception
     */
    int removeProject( int projectId )
        throws Exception;

    /**
     * Update a project. Useful to change the scm parameters.
     *
     * @param project The project to update
     * @throws Exception
     */
    ProjectSummary updateProject( ProjectSummary project )
        throws Exception;

    // ----------------------------------------------------------------------
    // Projects Groups
    // ----------------------------------------------------------------------

    /**
     * Get all project groups.
     *
     * @return All project groups
     * @throws Exception
     */
    List<ProjectGroupSummary> getAllProjectGroups()
        throws Exception;

    /**
     * Get all project groups with all details (project summaries, notifiers, build definitions).
     *
     * @return All project groups
     * @throws Exception
     */
    List<ProjectGroup> getAllProjectGroupsWithAllDetails()
        throws Exception;

    /**
     * Get all project groups with all details.
     *
     * @return All project groups
     * @throws Exception
     * @deprecated
     */
    List<ProjectGroup> getAllProjectGroupsWithProjects()
        throws Exception;

    /**
     * Get a project group.
     *
     * @param projectGroupId The project group id
     * @return The project group summary
     * @throws Exception
     */
    ProjectGroupSummary getProjectGroupSummary( int projectGroupId )
        throws Exception;

    /**
     * Get a project group with all details.
     *
     * @param projectGroupId The project group id
     * @return The project group
     * @throws Exception
     */
    ProjectGroup getProjectGroupWithProjects( int projectGroupId )
        throws Exception;

    /**
     * Remove a project group.
     *
     * @param projectGroupId The project group id
     * @throws Exception
     */
    int removeProjectGroup( int projectGroupId )
        throws Exception;

    /**
     * Update a project Group.
     *
     * @param projectGroup The project group to update
     * @throws Exception
     */
    ProjectGroupSummary updateProjectGroup( ProjectGroupSummary projectGroup )
        throws Exception;

    /**
     * Add a project Group.
     *
     * @param groupName   The project group name
     * @param groupId     The project group id
     * @param description The project group description
     * @return the project group summary of the created project group
     * @throws Exception
     */
    ProjectGroupSummary addProjectGroup( String groupName, String groupId, String description )
        throws Exception;

    // ----------------------------------------------------------------------
    // Build Definitions
    // ----------------------------------------------------------------------

    /**
     * Get the build definitions list of the project.
     *
     * @param projectId The project id
     * @return The build definitions list
     * @throws Exception
     */
    List<BuildDefinition> getBuildDefinitionsForProject( int projectId )
        throws Exception;

    /**
     * Get the build definitions list of the project group.
     *
     * @param projectGroupId The project group id
     * @return The build definitions list
     * @throws Exception
     */
    List<BuildDefinition> getBuildDefinitionsForProjectGroup( int projectGroupId )
        throws Exception;

    /**
     * Update a project build definition.
     *
     * @param projectId The project id
     * @param buildDef  The build defintion to update
     * @return the updated build definition
     * @throws Exception
     */
    BuildDefinition updateBuildDefinitionForProject( int projectId, BuildDefinition buildDef )
        throws Exception;

    /**
     * Update a project group build definition.
     *
     * @param projectGroupId The project group id
     * @param buildDef       The build defintion to update
     * @return the updated build definition
     * @throws Exception
     */
    BuildDefinition updateBuildDefinitionForProjectGroup( int projectGroupId, BuildDefinition buildDef )
        throws Exception;

    /**
     * Add a project build definition.
     *
     * @param projectId The project id
     * @param buildDef  The build defintion to update
     * @return the added build definition
     * @throws Exception
     */
    BuildDefinition addBuildDefinitionToProject( int projectId, BuildDefinition buildDef )
        throws Exception;

    /**
     * Add a project group buildDefinition.
     *
     * @param projectGroupId The project group id
     * @param buildDef       The build defintion to update
     * @return the build definition added
     * @throws Exception
     */
    BuildDefinition addBuildDefinitionToProjectGroup( int projectGroupId, BuildDefinition buildDef )
        throws Exception;

    /**
     * Get the build definition templates list.
     *
     * @return The build definitions templates list
     * @throws Exception
     */
    List<BuildDefinitionTemplate> getBuildDefinitionTemplates()
        throws Exception;

    // ----------------------------------------------------------------------
    // Building
    // ----------------------------------------------------------------------

    /**
     * Add the project to the build queue.
     *
     * @param projectId The project id
     * @throws Exception
     */
    int addProjectToBuildQueue( int projectId )
        throws Exception;

    /**
     * Add the project to the build queue.
     *
     * @param projectId         The project id
     * @param buildDefinitionId The build definition id
     * @throws Exception
     */
    int addProjectToBuildQueue( int projectId, int buildDefinitionId )
        throws Exception;

    /**
     * Build the project
     *
     * @param projectId The project id
     * @throws Exception
     */
    int buildProject( int projectId )
        throws Exception;

    /**
     * Build the project
     *
     * @param projectId         The project id
     * @param buildDefinitionId The build definition id
     * @throws Exception
     */
    int buildProject( int projectId, int buildDefinitionId )
        throws Exception;

    /**
     * Build the project group with the default build definition.
     *
     * @param projectGroupId The project group id
     * @throws Exception
     */
    int buildGroup( int projectGroupId )
        throws Exception;

    /**
     * Build the project group with the specified build definition.
     *
     * @param projectGroupId    The project group id
     * @param buildDefinitionId The build definition id
     * @throws Exception
     */
    int buildGroup( int projectGroupId, int buildDefinitionId )
        throws Exception;
    // ----------------------------------------------------------------------
    // Build Results
    // ----------------------------------------------------------------------

    /**
     * Returns the latest build result for the project.
     *
     * @param projectId The project id
     * @return The build result
     * @throws Exception
     */
    BuildResult getLatestBuildResult( int projectId )
        throws Exception;

    /**
     * Returns the build result.
     *
     * @param projectId The project id
     * @param buildId   The build id
     * @return The build result
     * @throws Exception
     */
    BuildResult getBuildResult( int projectId, int buildId )
        throws Exception;

    /**
     * Returns the project build result summary list.
     *
     * @param projectId The project id
     * @return The build result list
     * @throws Exception
     */
    List<BuildResultSummary> getBuildResultsForProject( int projectId )
        throws Exception;

    /**
     * Remove the project build result.
     *
     * @param br The project build result
     * @return 0
     * @throws Exception
     */
    int removeBuildResult( BuildResult br )
        throws Exception;

    /**
     * Returns the build output.
     *
     * @param projectId The project id
     * @param buildId   The build id
     * @return The build output
     * @throws Exception
     */
    String getBuildOutput( int projectId, int buildId )
        throws Exception;

    // ----------------------------------------------------------------------
    // Maven 2.x projects
    // ----------------------------------------------------------------------

    /**
     * Add a maven 2.x project from an url.
     *
     * @param url The POM url
     * @return The result of the action with the list of projects created
     * @throws Exception
     */
    AddingResult addMavenTwoProject( String url )
        throws Exception;

    /**
     * Add a maven 2.x project from an url.
     *
     * @param url            The POM url
     * @param projectGroupId The id of the group where projects will be stored
     * @return The result of the action with the list of projects created
     * @throws Exception
     */
    AddingResult addMavenTwoProject( String url, int projectGroupId )
        throws Exception;

    // ----------------------------------------------------------------------
    // Maven 1.x projects
    // ----------------------------------------------------------------------

    /**
     * Add a maven 1.x project from an url.
     *
     * @param url The POM url
     * @return The result of the action with the list of projects created
     * @throws Exception
     */
    AddingResult addMavenOneProject( String url )
        throws Exception;

    /**
     * Add a maven 1.x project from an url.
     *
     * @param url            The POM url
     * @param projectGroupId The id of the group where projects will be stored
     * @return The result of the action with the list of projects created
     * @throws Exception
     */
    AddingResult addMavenOneProject( String url, int projectGroupId )
        throws Exception;

    // ----------------------------------------------------------------------
    // Maven ANT projects
    // ----------------------------------------------------------------------

    /**
     * Add an ANT project.
     *
     * @param project The project to add. name, version and scm informations are required
     * @return The project populated with the id.
     * @throws Exception
     */
    ProjectSummary addAntProject( ProjectSummary project )
        throws Exception;

    /**
     * Add an ANT project in the specified group.
     *
     * @param project        The project to add. name, version and scm informations are required
     * @param projectGroupId The id of the group where projects will be stored
     * @return The project populated with the id.
     * @throws Exception
     */
    ProjectSummary addAntProject( ProjectSummary project, int projectGroupId )
        throws Exception;

    // ----------------------------------------------------------------------
    // Maven Shell projects
    // ----------------------------------------------------------------------

    /**
     * Add an shell project.
     *
     * @param project The project to add. name, version and scm informations are required
     * @return The project populated with the id.
     * @throws Exception
     */
    ProjectSummary addShellProject( ProjectSummary project )
        throws Exception;

    /**
     * Add an shell project in the specified group.
     *
     * @param project        The project to add. name, version and scm informations are required
     * @param projectGroupId The id of the group where projects will be stored
     * @return The project populated with the id.
     * @throws Exception
     */
    ProjectSummary addShellProject( ProjectSummary project, int projectGroupId )
        throws Exception;

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
     * @throws Exception
     */
    List<Schedule> getSchedules()
        throws Exception;

    /**
     * Return the schedule defined by this id.
     *
     * @param scheduleId The schedule id
     * @return The schedule.
     * @throws Exception
     */
    Schedule getSchedule( int scheduleId )
        throws Exception;

    /**
     * Add the schedule.
     *
     * @param schedule The schedule
     * @return The schedule.
     * @throws Exception
     */
    Schedule addSchedule( Schedule schedule )
        throws Exception;

    /**
     * Update the schedule.
     *
     * @param schedule The schedule
     * @return The schedule.
     * @throws Exception
     */
    Schedule updateSchedule( Schedule schedule )
        throws Exception;

    // ----------------------------------------------------------------------
    // Profiles
    // ----------------------------------------------------------------------

    /**
     * Return the profiles list.
     *
     * @return The profiles list.
     * @throws Exception
     */
    List<Profile> getProfiles()
        throws Exception;

    /**
     * Return the profile defined by this id.
     *
     * @param profileId The profile id
     * @return The profile.
     * @throws Exception
     */
    Profile getProfile( int profileId )
        throws Exception;

    // ----------------------------------------------------------------------
    // Installations
    // ----------------------------------------------------------------------

    /**
     * Return the installations list.
     *
     * @return The installations list.
     * @throws Exception
     */
    List<Installation> getInstallations()
        throws Exception;

    /**
     * Return the installation defined by this id.
     *
     * @param installationId The installation id
     * @return The installation.
     * @throws Exception
     */
    Installation getInstallation( int installationId )
        throws Exception;

    // ----------------------------------------------------------------------
    // SystemConfiguration
    // ----------------------------------------------------------------------

    SystemConfiguration getSystemConfiguration()
        throws Exception;
    
    // ----------------------------------------------------------------------
    // Queue
    // ----------------------------------------------------------------------
    
        
        /**
     * Return true is the project is in building queue.
     *
     * @param projectGroupId    The project group id
     * @throws ContinuumException
     */
    boolean isProjectInBuildingQueue( int projectId )
        throws Exception;

    /**
     * Return projects building queue.
     *
     * @throws ContinuumException
     */
    public List<BuildProjectTask> getProjectsInBuildQueue()
        throws Exception;    

    // ----------------------------------------------------------------------
    // TODO:Users
    // ----------------------------------------------------------------------

    // ----------------------------------------------------------------------
    // Utils
    // ----------------------------------------------------------------------

    boolean ping()
        throws Exception;
}
