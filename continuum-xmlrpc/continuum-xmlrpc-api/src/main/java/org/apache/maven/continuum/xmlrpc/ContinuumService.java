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
import java.util.Map;
import java.util.Properties;

import org.apache.continuum.xmlrpc.release.ContinuumReleaseResult;
import org.apache.continuum.xmlrpc.repository.DirectoryPurgeConfiguration;
import org.apache.continuum.xmlrpc.repository.LocalRepository;
import org.apache.continuum.xmlrpc.repository.RepositoryPurgeConfiguration;
import org.apache.continuum.xmlrpc.utils.BuildTrigger;
import org.apache.maven.continuum.xmlrpc.project.AddingResult;
import org.apache.maven.continuum.xmlrpc.project.BuildAgentConfiguration;
import org.apache.maven.continuum.xmlrpc.project.BuildDefinition;
import org.apache.maven.continuum.xmlrpc.project.BuildDefinitionTemplate;
import org.apache.maven.continuum.xmlrpc.project.BuildProjectTask;
import org.apache.maven.continuum.xmlrpc.project.BuildResult;
import org.apache.maven.continuum.xmlrpc.project.BuildResultSummary;
import org.apache.maven.continuum.xmlrpc.project.Project;
import org.apache.maven.continuum.xmlrpc.project.ProjectGroup;
import org.apache.maven.continuum.xmlrpc.project.ProjectGroupSummary;
import org.apache.maven.continuum.xmlrpc.project.ProjectNotifier;
import org.apache.maven.continuum.xmlrpc.project.ProjectScmRoot;
import org.apache.maven.continuum.xmlrpc.project.ProjectSummary;
import org.apache.maven.continuum.xmlrpc.project.ReleaseListenerSummary;
import org.apache.maven.continuum.xmlrpc.project.Schedule;
import org.apache.maven.continuum.xmlrpc.system.Installation;
import org.apache.maven.continuum.xmlrpc.system.Profile;
import org.apache.maven.continuum.xmlrpc.system.SystemConfiguration;

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
     */
    List<ProjectSummary> getProjects( int projectGroupId )
        throws Exception;

    /**
     * Same method but compatible with standard XMLRPC
     * 
     * @param projectGroupId The project group Id
     * @return List of {@link ProjectSummary} as RPC value
     * @throws Exception
     */
    List<Object> getProjectsRPC( int projectGroupId )
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
     * Same method but compatible with standard XMLRPC
     * 
     * @param projectId the project id
     * @return The project summary as RPC value
     * @throws Exception
     */
    Map<String, Object> getProjectSummaryRPC( int projectId )
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
     * Same method but compatible with standard XMLRPC
     * 
     * @param projectId the project id
     * @return The project as RPC value
     * @throws Exception
     */
    Map<String, Object> getProjectWithAllDetailsRPC( int projectId )
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

    /**
     * Same method but compatible with standard XMLRPC
     * 
     * @param project The project to update
     * @return The project as RPC value
     * @throws Exception
     */
    Map<String, Object> updateProjectRPC( Map<String, Object> project )
        throws Exception;

    // ----------------------------------------------------------------------
    // Projects Groups
    // ----------------------------------------------------------------------

    /**
     * Get a project groups.
     * 
     * @param projectGroupId the id
     * @return project group
     * @throws Exception
     */
    ProjectGroup getProjectGroup( int projectGroupId )
        throws Exception;

    /**
     * Same method but compatible with standard XMLRPC
     * 
     * @param projectGroupId the id
     * @return project group as RPC value
     * @throws Exception
     */
    Map<String, Object> getProjectGroupRPC( int projectGroupId )
        throws Exception;

    /**
     * Get all project groups.
     * 
     * @return All project groups
     * @throws Exception
     */
    List<ProjectGroupSummary> getAllProjectGroups()
        throws Exception;

    /**
     * Same method but compatible with standard XMLRPC
     * 
     * @return List of {@link ProjectGroupSummary} as RPC value
     * @throws Exception
     */
    List<Object> getAllProjectGroupsRPC()
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
     * Same method but compatible with standard XMLRPC
     * 
     * @return List of {@link ProjectGroup} as RPC value
     * @throws Exception
     */
    List<Object> getAllProjectGroupsWithAllDetailsRPC()
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
     * Same method but compatible with standard XMLRPC
     * 
     * @param projectGroupId The project group id
     * @return The project group summary as RPC value
     * @throws Exception
     */
    Map<String, Object> getProjectGroupSummaryRPC( int projectGroupId )
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
     * Same method but compatible with standard XMLRPC
     * 
     * @param projectGroupId The project group id
     * @return The project group as RPC value
     * @throws Exception
     */
    Map<String, Object> getProjectGroupWithProjectsRPC( int projectGroupId )
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
     * Same method but compatible with standard XMLRPC
     * 
     * @param projectGroup The project group to update
     * @return The project group as RPC value
     * @throws Exception
     */
    Map<String, Object> updateProjectGroupRPC( Map<String, Object> projectGroup )
        throws Exception;

    /**
     * Add a project Group.
     * 
     * @param groupName The project group name
     * @param groupId The project group id
     * @param description The project group description
     * @return the project group summary of the created project group
     * @throws Exception
     */
    ProjectGroupSummary addProjectGroup( String groupName, String groupId, String description )
        throws Exception;

    int removeBuildDefinitionFromProjectGroup( int projectGroupId, int buildDefinitionId )
        throws Exception;

    /**
     * Same method but compatible with standard XMLRPC
     * 
     * @param groupName The project group name
     * @param groupId The project group id
     * @param description The project group description
     * @return the project group summary of the created project group as RPC value
     * @throws Exception
     */
    Map<String, Object> addProjectGroupRPC( String groupName, String groupId, String description )
        throws Exception;

    ProjectNotifier getNotifier( int projectid, int notifierId )
        throws Exception;

    Map<String, Object> getNotifierRPC( int projectid, int notifierId )
        throws Exception;

    ProjectNotifier getGroupNotifier( int projectgroupid, int notifierId )
        throws Exception;

    Map<String, Object> getGroupNotifierRPC( int projectgroupid, int notifierId )
        throws Exception;

    ProjectNotifier updateGroupNotifier( int projectgroupid, ProjectNotifier newNotifier )
        throws Exception;

    Map<String, Object> updateGroupNotifierRPC( int projectgroupid, Map<String, Object> newNotifier )
        throws Exception;

    ProjectNotifier updateNotifier( int projectid, ProjectNotifier newNotifier )
        throws Exception;

    Map<String, Object> updateNotifierRPC( int projectid, Map<String, Object> newNotifier )
        throws Exception;

    int removeGroupNotifier( int projectgroupid, int notifierId )
        throws Exception;

    int removeNotifier( int projectid, int notifierId )
        throws Exception;

    ProjectNotifier addNotifier( int projectid, ProjectNotifier newNotifier )
        throws Exception;

    ProjectNotifier addGroupNotifier( int projectgroupid, ProjectNotifier newNotifier )
        throws Exception;

    Map<String, Object> addNotifierRPC( int projectid, Map<String, Object> newNotifier )
        throws Exception;

    Map<String, Object> addGroupNotifierRPC( int projectgroupid, Map<String, Object> newNotifier )
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
     * Same method but compatible with standard XMLRPC
     * 
     * @param projectId The project id
     * @return The build definitions list as RPC value
     * @throws Exception
     */
    List<Object> getBuildDefinitionsForProjectRPC( int projectId )
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
     * Same method but compatible with standard XMLRPC
     * 
     * @param projectGroupId The project group id
     * @return The build definitions list as RPC value
     * @throws Exception
     */
    List<Object> getBuildDefinitionsForProjectGroupRPC( int projectGroupId )
        throws Exception;

    /**
     * Update a project build definition.
     * 
     * @param projectId The project id
     * @param buildDef The build definition to update
     * @return the updated build definition
     * @throws Exception
     */
    BuildDefinition updateBuildDefinitionForProject( int projectId, BuildDefinition buildDef )
        throws Exception;

    /**
     * Same method but compatible with standard XMLRPC
     * 
     * @param projectId The project id
     * @param buildDef The build definition to update
     * @return the updated build definition as RPC value
     * @throws Exception
     */
    Map<String, Object> updateBuildDefinitionForProjectRPC( int projectId, Map<String, Object> buildDef )
        throws Exception;

    /**
     * Update a project group build definition.
     * 
     * @param projectGroupId The project group id
     * @param buildDef The build definition to update
     * @return the updated build definition
     * @throws Exception
     */
    BuildDefinition updateBuildDefinitionForProjectGroup( int projectGroupId, BuildDefinition buildDef )
        throws Exception;

    /**
     * Same method but compatible with standard XMLRPC
     * 
     * @param projectGroupId The project group id
     * @param buildDef The build definition to update
     * @return the updated build definition as RPC value
     * @throws Exception
     */
    Map<String, Object> updateBuildDefinitionForProjectGroupRPC( int projectGroupId, Map<String, Object> buildDef )
        throws Exception;

    /**
     * Add a project build definition.
     * 
     * @param projectId The project id
     * @param buildDef The build definition to update
     * @return the added build definition
     * @throws Exception
     */
    BuildDefinition addBuildDefinitionToProject( int projectId, BuildDefinition buildDef )
        throws Exception;

    /**
     * Same method but compatible with standard XMLRPC
     * 
     * @param projectId The project id
     * @param buildDef The build definition to update
     * @return the added build definition as RPC value
     * @throws Exception
     */
    Map<String, Object> addBuildDefinitionToProjectRPC( int projectId, Map<String, Object> buildDef )
        throws Exception;

    /**
     * Add a project group buildDefinition.
     * 
     * @param projectGroupId The project group id
     * @param buildDef The build definition to update
     * @return the build definition added
     * @throws Exception
     */
    BuildDefinition addBuildDefinitionToProjectGroup( int projectGroupId, BuildDefinition buildDef )
        throws Exception;

    /**
     * Same method but compatible with standard XMLRPC
     * 
     * @param projectGroupId The project group id
     * @param buildDef The build definition to update
     * @return the added build definition as RPC value
     * @throws Exception
     */
    Map<String, Object> addBuildDefinitionToProjectGroupRPC( int projectGroupId, Map<String, Object> buildDef )
        throws Exception;

    /**
     * Get the build definition templates list.
     * 
     * @return The build definitions templates list
     * @throws Exception
     */
    List<BuildDefinitionTemplate> getBuildDefinitionTemplates()
        throws Exception;

    /**
     * Same method but compatible with standard XMLRPC
     * 
     * @return The build definitions templates list as RPC value
     * @throws Exception
     */
    List<Object> getBuildDefinitionTemplatesRPC()
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
     * @param projectId The project id
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
     * @param projectId The project id
     * @param buildDefinitionId The build definition id
     * @throws Exception
     */
    int buildProject( int projectId, int buildDefinitionId )
        throws Exception;

    /**
     * Forced build the project
     * 
     * @param projectId The project id
     * @param buildTrigger The build trigger
     * @return
     * @throws Exception
     */
    int buildProject( int projectId, BuildTrigger buildTrigger )
        throws Exception;
    
    /**
     * Forced build the project
     * 
     * @param projectId The project id
     * @param buildDefinitionId The build definition id
     * @param buildTrigger The build trigger
     * @return
     * @throws Exception
     */
    int buildProject( int projectId, int buildDefinitionId, BuildTrigger buildTrigger )
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
     * @param projectGroupId The project group id
     * @param buildDefinitionId The build definition id
     * @throws Exception
     */
    int buildGroup( int projectGroupId, int buildDefinitionId )
        throws Exception;

    // ----------------------------------------------------------------------
    // SCM roots
    // ----------------------------------------------------------------------

    /**
     * Get the SCM roots for all projects in a project group
     * 
     * @param projectGroupId the project group id
     * @return
     * @throws Exception
     */
    List<ProjectScmRoot> getProjectScmRootByProjectGroup( int projectGroupId )
        throws Exception;

    /**
     * Get the SCM root for a specific project
     * 
     * @param projectId the project id
     * @return
     * @throws Exception
     */
    ProjectScmRoot getProjectScmRootByProject( int projectId )
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
     * Same method but compatible with standard XMLRPC
     * 
     * @param projectId The project id
     * @return The build result as RPC value
     * @throws Exception
     */
    Map<String, Object> getLatestBuildResultRPC( int projectId )
        throws Exception;

    /**
     * Returns the build result.
     * 
     * @param projectId The project id
     * @param buildId The build id
     * @return The build result
     * @throws Exception
     */
    BuildResult getBuildResult( int projectId, int buildId )
        throws Exception;

    /**
     * Same method but compatible with standard XMLRPC
     * 
     * @param projectId The project id
     * @param buildId The build id
     * @return The build result as RPC value
     * @throws Exception
     */
    Map<String, Object> getBuildResultRPC( int projectId, int buildId )
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
     * Same method but compatible with standard XMLRPC
     * 
     * @param projectId The project id
     * @return The build result list as RPC value
     * @throws Exception
     */
    List<Object> getBuildResultsForProjectRPC( int projectId )
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
     * Same method but compatible with standard XMLRPC
     * 
     * @param br The project build result
     * @return 0
     * @throws Exception
     */
    int removeBuildResultRPC( Map<String, Object> br )
        throws Exception;

    /**
     * Returns the build output.
     * 
     * @param projectId The project id
     * @param buildId The build id
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
     * Same method but compatible with standard XMLRPC
     * 
     * @param url The POM url
     * @return The result of the action with the list of projects created as RPC value
     * @throws Exception
     */
    Map<String, Object> addMavenTwoProjectRPC( String url )
        throws Exception;

    /**
     * Add a maven 2.x project from an url.
     * 
     * @param url The POM url
     * @param projectGroupId The id of the group where projects will be stored
     * @return The result of the action with the list of projects created
     * @throws Exception
     */
    AddingResult addMavenTwoProject( String url, int projectGroupId )
        throws Exception;

    /**
     * Same method but compatible with standard XMLRPC
     * 
     * @param url The POM url
     * @param projectGroupId The id of the group where projects will be stored
     * @return The result of the action with the list of projects created as RPC value
     * @throws Exception
     */
    Map<String, Object> addMavenTwoProjectRPC( String url, int projectGroupId )
        throws Exception;

    /**
     * Add a maven 2.x project from an url.
     * 
     * @param url The POM url
     * @param projectGroupId The id of the group where projects will be stored
     * @Param checkoutInSingleDirectory Determines whether the project will be stored on a single directory
     * @return The result of the action with the list of projects created
     * @throws Exception
     */
    AddingResult addMavenTwoProject( String url, int projectGroupId, boolean checkoutInSingleDirectory )
        throws Exception;

    /**
     * Same method but compatible with standard XMLRPC
     * 
     * @param url The POM url
     * @param projectGroupId The id of the group where projects will be stored
     * @Param checkoutInSingleDirectory Determines whether the project will be stored on a single directory
     * @return The result of the action with the list of projects created as RPC value
     * @throws Exception
     */
    Map<String, Object> addMavenTwoProjectRPC( String url, int projectGroupId, boolean checkoutInSingleDirectory )
        throws Exception;

    /**
     * Add a maven 2.x multi-module project from a url and add it to Continuum as a single project instead of as
     * multiple projects (one project per module). To add a multi-module project with its modules as separate Continuum
     * projects, use ContinuumService#addMavenTwoProject( String url, int projectGroupId, boolean
     * checkoutInSingleDirectory) instead.
     * 
     * @param url
     * @param projectGroupId
     * @return
     * @throws Exception
     */
    AddingResult addMavenTwoProjectAsSingleProject( String url, int projectGroupId )
        throws Exception;

    /**
     * Same method but compatible with standard XMLRPC
     * 
     * @param url
     * @param projectGroupId
     * @return 
     * @throws Exception
     */
    Map<String, Object> addMavenTwoProjectAsSingleProjectRPC( String url, int projectGroupId )
        throws Exception;
    
    /**
     * Add a maven 2.x multi-module project from a url
     * 
     * @param url The POM url
     * @param projectGroupId The id of the group where projects will be stored
     * @param checkProtocol Determines whether the protocol will be checked
     * @param useCredentialsCache Determines whether user credentials will be cached
     * @param recursiveProjects Determines whether to load recursive projects
     * @param checkoutInSingleDirectory Determines whether the project will be stored on a single directory
     * @return The result of the action with the list of projects created
     * @throws Exception
     */
    AddingResult addMavenTwoProject( String url, int projectGroupId, boolean checkProtocol,
                                     boolean useCredentialsCache, boolean recursiveProjects,
                                     boolean checkoutInSingleDirectory )
        throws Exception;

    /**
     * Same method but compatible with standard XMLRPC
     * 
     * @param url The POM url
     * @param projectGroupId The id of the group where projects will be stored
     * @param checkProtocol Determines whether the protocol will be checked
     * @param useCredentialsCache Determines whether user credentials will be cached
     * @param recursiveProjects Determines whether to load recursive projects
     * @param checkoutInSingleDirectory Determines whether the project will be stored on a single directory
     * @return The result of the action with the list of projects created as RPC value
     * @throws Exception
     */
    Map<String, Object> addMavenTwoProjectRPC( String url, int projectGroupId, boolean checkProtocol,
                                               boolean useCredentialsCache, boolean recursiveProjects,
                                               boolean checkoutInSingleDirectory )
        throws Exception;
    
    
    // ----------------------------------------------------------------------
    // Maven 1.x projects
    // ----------------------------------------------------------------------

    /**
     * Add a maven 1.x project from an url.
     * 
     * @param url The POM url
     * @param projectGroupId The id of the group where projects will be stored
     * @return The result of the action with the list of projects created
     * @throws Exception
     */
    AddingResult addMavenOneProject( String url, int projectGroupId )
        throws Exception;

    /**
     * Same method but compatible with standard XMLRPC
     * 
     * @param url The POM url
     * @param projectGroupId The id of the group where projects will be stored
     * @return The result of the action with the list of projects created as RPC value
     * @throws Exception
     */
    Map<String, Object> addMavenOneProjectRPC( String url, int projectGroupId )
        throws Exception;

    // ----------------------------------------------------------------------
    // Maven ANT projects
    // ----------------------------------------------------------------------

    /**
     * Add an ANT project in the specified group.
     * 
     * @param project The project to add. name, version and scm informations are required
     * @param projectGroupId The id of the group where projects will be stored
     * @return The project populated with the id.
     * @throws Exception
     */
    ProjectSummary addAntProject( ProjectSummary project, int projectGroupId )
        throws Exception;

    /**
     * Same method but compatible with standard XMLRPC
     * 
     * @param project The project to add. name, version and scm informations are required
     * @param projectGroupId The id of the group where projects will be stored
     * @return The project populated with the id as RPC value
     * @throws Exception
     */
    Map<String, Object> addAntProjectRPC( Map<String, Object> project, int projectGroupId )
        throws Exception;

    // ----------------------------------------------------------------------
    // Maven Shell projects
    // ----------------------------------------------------------------------

    /**
     * Add an shell project in the specified group.
     * 
     * @param project The project to add. name, version and scm informations are required
     * @param projectGroupId The id of the group where projects will be stored
     * @return The project populated with the id.
     * @throws Exception
     */
    ProjectSummary addShellProject( ProjectSummary project, int projectGroupId )
        throws Exception;

    /**
     * Same method but compatible with standard XMLRPC
     * 
     * @param project The project to add. name, version and scm informations are required
     * @param projectGroupId The id of the group where projects will be stored
     * @return The project populated with the id as RPC value
     * @throws Exception
     */
    Map<String, Object> addShellProjectRPC( Map<String, Object> project, int projectGroupId )
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
     * Same method but compatible with standard XMLRPC
     * 
     * @return The schedule list as RPC value.
     * @throws Exception
     */
    List<Object> getSchedulesRPC()
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
     * Same method but compatible with standard XMLRPC
     * 
     * @param scheduleId The schedule id
     * @return The schedule as RPC value.
     * @throws Exception
     */
    Map<String, Object> getScheduleRPC( int scheduleId )
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
     * Same method but compatible with standard XMLRPC
     * 
     * @param schedule The schedule
     * @return The schedule as RPC value.
     * @throws Exception
     */
    Map<String, Object> addScheduleRPC( Map<String, Object> schedule )
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

    /**
     * Same method but compatible with standard XMLRPC
     * 
     * @param schedule The schedule
     * @return The schedule as RPC value.
     * @throws Exception
     */
    Map<String, Object> updateScheduleRPC( Map<String, Object> schedule )
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
     * Same method but compatible with standard XMLRPC
     * 
     * @return The profiles list as RPC value.
     * @throws Exception
     */
    List<Object> getProfilesRPC()
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

    /**
     * Same method but compatible with standard XMLRPC
     * 
     * @param profileId The profile id
     * @return The profile.
     * @throws Exception
     */
    Map<String, Object> getProfileRPC( int profileId )
        throws Exception;

    Profile addProfile( Profile profile )
        throws Exception;

    int updateProfile( Profile profile )
        throws Exception;

    int deleteProfile( int profileId )
        throws Exception;

    Map<String, Object> addProfileRPC( Map<String, Object> profile )
        throws Exception;

    int updateProfileRPC( Map<String, Object> profile )
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
     * Same method but compatible with standard XMLRPC
     * 
     * @return The installations list.
     * @throws Exception
     */
    List<Object> getInstallationsRPC()
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

    /**
     * Same method but compatible with standard XMLRPC
     * 
     * @param installationId The installation id
     * @return The installation.
     * @throws Exception
     */
    Map<String, Object> getInstallationRPC( int installationId )
        throws Exception;

    /**
     * Return the installations list defined by this URL.
     * 
     * @param url The build agent URL
     * @return The installations list.
     * @throws Exception
     */
    List<Installation> getBuildAgentInstallations( String url )
        throws Exception;

    /**
     * Same method but compatible with standard XMLRPC
     * 
     * @param url The build agent URL
     * @return The installations list.
     * @throws Exception
     */
    List<Object> getBuildAgentInstallationsRPC(String url)
        throws Exception;

    Installation addInstallation( Installation installation )
        throws Exception;

    int updateInstallation( Installation installation )
        throws Exception;

    int deleteInstallation( int installationId )
        throws Exception;

    Map<String, Object> addInstallationRPC( Map<String, Object> installation )
        throws Exception;

    int updateInstallationRPC( Map<String, Object> installation )
        throws Exception;

    // ----------------------------------------------------------------------
    // SystemConfiguration
    // ----------------------------------------------------------------------

    SystemConfiguration getSystemConfiguration()
        throws Exception;

    Map<String, Object> getSystemConfigurationRPC()
        throws Exception;

    // ----------------------------------------------------------------------
    // Queue
    // ----------------------------------------------------------------------

    /**
     * Return true if the project is in prepare build queue
     * 
     * @param projectId The project id
     * @throws ContinuumException
     */
    boolean isProjectInPrepareBuildQueue( int projectId )
        throws Exception;

    /**
     * Return true if the project is in building queue.
     * 
     * @param projectId The project id
     * @throws ContinuumException
     */
    boolean isProjectInBuildingQueue( int projectId )
        throws Exception;

    /**
     * Return true if the project is currently preparing build
     * 
     * @param projectId
     * @return
     * @throws Exception
     */
    boolean isProjectCurrentlyPreparingBuild( int projectId )
        throws Exception;

    /**
     * Return true if the project is currently building
     * 
     * @param projectId
     * @return
     * @throws Exception
     */
    boolean isProjectCurrentlyBuilding( int projectId )
        throws Exception;

    /**
     * Return projects building queue.
     * 
     * @throws ContinuumException
     */
    public List<BuildProjectTask> getProjectsInBuildQueue()
        throws Exception;

    /**
     * Remove projects from build queue
     * 
     * @param projectsId project id to be removed from the building queue
     * @return
     * @throws Exception
     */
    int removeProjectsFromBuildingQueue( int[] projectsId )
        throws Exception;

    /**
     * Cancel the current project build
     * 
     * @return
     * @throws Exception
     */
    boolean cancelCurrentBuild()
        throws Exception;

    /**
     * Cancel a project build
     * 
     * @param projectId the project id
     * @return
     * @throws Exception
     */
    boolean cancelBuild( int projectId )
        throws Exception;

    // ----------------------------------------------------------------------
    // TODO:Users
    // ----------------------------------------------------------------------

    // ----------------------------------------------------------------------
    // Utils
    // ----------------------------------------------------------------------

    boolean ping()
        throws Exception;

    // ----------------------------------------------------------------------
    // Local Repository
    // ----------------------------------------------------------------------

    /**
     * Add a local repository
     * 
     * @param repository the local repository to add
     * @return
     * @throws Exception
     */
    LocalRepository addLocalRepository( LocalRepository repository )
        throws Exception;

    /**
     * Same method but compatible with the standard XMLRPC
     * 
     * @param repository the local repository to add
     * @return
     * @throws Exception
     */
    Map<String, Object> addLocalRepositoryRPC( Map<String, Object> repository )
        throws Exception;

    /**
     * Update the local repository
     * 
     * @param repository the local repository to update
     * @return
     * @throws Exception
     */
    int updateLocalRepository( LocalRepository repository )
        throws Exception;

    /**
     * Same method but compatible with the standard XMLRPC
     * 
     * @param repository the local repository to update
     * @return
     * @throws Exception
     */
    int updateLocalRepositoryRPC( Map<String, Object> repository )
        throws Exception;

    /**
     * Remove the local repository
     * 
     * @param repositoryId
     * @return
     * @throws Exception
     */
    int removeLocalRepository( int repositoryId )
        throws Exception;

    /**
     * Returns the local repository
     * 
     * @param repositoryId the local repository id
     * @return
     * @throws Exception
     */
    LocalRepository getLocalRepository( int repositoryId )
        throws Exception;

    /**
     * Same method but compatible with the standard XMLRPC
     * 
     * @param repositoryId
     * @return
     * @throws Exception
     */
    Map<String, Object> getLocalRepositoryRPC( int repositoryId )
        throws Exception;

    /**
     * Returns all local repositories
     * 
     * @return
     * @throws Exception
     */
    List<LocalRepository> getAllLocalRepositories()
        throws Exception;

    /**
     * Same method but compatible with the standard XMLRPC
     * 
     * @return
     * @throws Exception
     */
    List<Object> getAllLocalRepositoriesRPC()
        throws Exception;

    // ----------------------------------------------------------------------
    // Purging
    // ----------------------------------------------------------------------

    /**
     * Add a repository purge configuration
     * 
     * @param repoPurge the repository purge configuration
     * @return
     * @throws Exception
     */
    RepositoryPurgeConfiguration addRepositoryPurgeConfiguration( RepositoryPurgeConfiguration repoPurge )
        throws Exception;

    /**
     * Same method but compatible with the standard XMLRPC
     * 
     * @param repoPurge the repository purge configuration
     * @return
     * @throws Exception
     */
    Map<String, Object> addRepositoryPurgeConfigurationRPC( Map<String, Object> repoPurge )
        throws Exception;

    /**
     * Update the repository purge configuration
     * 
     * @param repoPurge the repository purge configuration
     * @return
     * @throws Exception
     */
    int updateRepositoryPurgeConfiguration( RepositoryPurgeConfiguration repoPurge )
        throws Exception;

    /**
     * Same method but compatible with the standard XMLRPC
     * 
     * @param repoPurge the repository purge configuration
     * @return
     * @throws Exception
     */
    int updateRepositoryPurgeConfigurationRPC( Map<String, Object> repoPurge )
        throws Exception;

    /**
     * Remove repository purge configuration
     * 
     * @param repoPurgeId the repository purge configuration id
     * @return
     * @throws Exception
     */
    int removeRepositoryPurgeConfiguration( int repoPurgeId )
        throws Exception;

    /**
     * Returns the repository purge configuration
     * 
     * @param purgeConfigId the repository purge configuration id
     * @return the repository purge configuration
     * @throws Exception
     */
    RepositoryPurgeConfiguration getRepositoryPurgeConfiguration( int repoPurgeId )
        throws Exception;

    /**
     * Same method but compatible with standard XMLRPC
     * 
     * @param purgeConfigId the repository purge configuration id
     * @return the repository purge configuration
     * @throws Exception
     */
    Map<String, Object> getRepositoryPurgeConfigurationRPC( int purgeConfigId )
        throws Exception;

    /**
     * Returns repository purge configurations list
     * 
     * @return list of repository purge configurations
     * @throws Exception
     */
    List<RepositoryPurgeConfiguration> getAllRepositoryPurgeConfigurations()
        throws Exception;

    /**
     * Same method but compatible with standard XMLRPC
     * 
     * @return list of repository purge configurations
     * @throws Exception
     */
    List<Object> getAllRepositoryPurgeConfigurationsRPC()
        throws Exception;

    /**
     * Add a directory purge configuration
     * 
     * @param dirPurge the directory purge configuration
     * @return
     * @throws Exception
     */
    DirectoryPurgeConfiguration addDirectoryPurgeConfiguration( DirectoryPurgeConfiguration dirPurge )
        throws Exception;

    /**
     * Same method but compatible with the standard XMLRPC
     * 
     * @param dirPurge the directory purge configuration
     * @return
     * @throws Exception
     */
    Map<String, Object> addDirectoryPurgeConfigurationRPC( Map<String, Object> dirPurge )
        throws Exception;

    /**
     * Update the directory purge configuration
     * 
     * @param dirPurge the directory purge configuration
     * @return
     * @throws Exception
     */
    int updateDirectoryPurgeConfiguration( DirectoryPurgeConfiguration dirPurge )
        throws Exception;

    /**
     * Same method but compatible with the standard XMLRPC
     * 
     * @param dirPurge the directory purge configuration
     * @return
     * @throws Exception
     */
    int updateDirectoryPurgeConfigurationRPC( Map<String, Object> dirPurge )
        throws Exception;

    /**
     * Removes the directory purge configuration
     * 
     * @param dirPurgeId the directory purge configuration id
     * @return
     * @throws Exception
     */
    int removeDirectoryPurgeConfiguration( int dirPurgeId )
        throws Exception;

    /**
     * Returns the directory purge configuration
     * 
     * @param purgeConfigId the directory purge configuration id
     * @return the directory purge configuration
     * @throws Exception
     */
    DirectoryPurgeConfiguration getDirectoryPurgeConfiguration( int purgeConfigId )
        throws Exception;

    /**
     * Same method but compatible with standard XMLRPC
     * 
     * @param purgeConfigId the directory purge configuration id
     * @return the directory purge configuration
     * @throws Exception
     */
    Map<String, Object> getDirectoryPurgeConfigurationRPC( int purgeConfigId )
        throws Exception;

    /**
     * Returns directory purge configurations list
     * 
     * @return list of directory purge configurations
     * @throws Exception
     */
    List<DirectoryPurgeConfiguration> getAllDirectoryPurgeConfigurations()
        throws Exception;

    /**
     * Same method but compatible with standard XMLRPC
     * 
     * @return list of directory purge configurations
     * @throws Exception
     */
    List<Object> getAllDirectoryPurgeConfigurationsRPC()
        throws Exception;

    void purgeLocalRepository( int repoPurgeId )
        throws Exception;

    void purgeDirectory( int dirPurgeId )
        throws Exception;

    // ----------------------------------------------------------------------
    // Release Results
    // ----------------------------------------------------------------------

    /**
     * Returns the release result.
     * 
     * @param releaseId The release id
     * @return The release result
     * @throws Exception
     */
    ContinuumReleaseResult getReleaseResult( int releaseId )
        throws Exception;

    /**
     * Same method but compatible with standard XMLRPC
     * 
     * @param releaseId The release id
     * @return The release result as RPC value
     * @throws Exception
     */
    Map<String, Object> getReleaseResultRPC( int releaseId )
        throws Exception;

    /**
     * Returns the project group release result list.
     * 
     * @param projectGroupId The project group id
     * @return The release result list
     * @throws Exception
     */
    List<ContinuumReleaseResult> getReleaseResultsForProjectGroup( int projectGroupId )
        throws Exception;

    /**
     * Same method but compatible with standard XMLRPC
     * 
     * @param projectGroupId The project group id
     * @return The release result list as RPC value
     * @throws Exception
     */
    List<Object> getReleaseResultsForProjectGroupRPC( int projectGroupId )
        throws Exception;

    /**
     * Remove the project release result.
     * 
     * @param releaseResult The project release result
     * @return 0
     * @throws Exception
     */
    int removeReleaseResult( ContinuumReleaseResult releaseResult )
        throws Exception;

    /**
     * Same method but compatible with standard XMLRPC
     * 
     * @param rr The project release result
     * @return 0
     * @throws Exception
     */
    int removeReleaseResultRPC( Map<String, Object> rr )
        throws Exception;

    /**
     * Returns the release output.
     * 
     * @param releaseId The release id
     * @return The release output
     * @throws Exception
     */
    String getReleaseOutput( int releaseId )
        throws Exception;

    /**
     * Release prepare a project
     * 
     * @param projectId
     * @param releaseProperties
     * @param releaseVersions
     * @param developmentVersions
     * @param environments
     * @param username
     * @return The release id
     * @throws Exception
     */
    String releasePrepare( int projectId, Properties releaseProperties, Map<String, String> releaseVersions,
                           Map<String, String> developmentVersions, Map<String, String> environments, String username )
        throws Exception;

    /**
     * Release perform a project
     * 
     * @param projectId
     * @param releaseId
     * @param goals
     * @param arguments
     * @param useReleaseProfile
     * @param repositoryName
     * @param username
     * @return
     * @throws Exception
     */
    int releasePerform( int projectId, String releaseId, String goals, String arguments, boolean useReleaseProfile,
                        String repositoryName, String username )
        throws Exception;

    /**
     * Get release listener
     * 
     * @param projectId
     * @param releaseId
     * @return
     * @throws Exception
     */
    ReleaseListenerSummary getListener( int projectId, String releaseId )
        throws Exception;

    /**
     * Cleanup the release
     * 
     * @param projectId
     * @param releaseId
     * @return
     * @throws Exception
     */
    int releaseCleanup( int projectId, String releaseId )
        throws Exception;

    /**
     * Cleanup the release
     * 
     * @param projectId
     * @param releaseId
     * @param releaseType
     * @return
     * @throws Exception
     */
    int releaseCleanup( int projectId, String releaseId, String releaseType )
        throws Exception;

    /**
     * Rollback a release
     * 
     * @param projectId
     * @param releaseId
     * @return
     * @throws Exception
     */
    int releaseRollback( int projectId, String releaseId )
        throws Exception;

    /**
     * Get release plugin parameters
     * 
     * @param projectId
     * @return
     */
    Map<String, Object> getReleasePluginParameters( int projectId )
        throws Exception;

    List<Map<String, String>> getProjectReleaseAndDevelopmentVersions( int projectId, String pomFilename, boolean autoVersionSubmodules )
        throws Exception;

    /**
     * Add/Register build agent to Continuum Master
     * 
     * @return
     * @throws Exception
     */
    BuildAgentConfiguration addBuildAgent( BuildAgentConfiguration buildAgentConfiguration )
        throws Exception;

    /**
     * Same method but compatible with standard XMLRPC
     * 
     * @return
     * @throws Exception
     */
    Map<String, Object> addBuildAgentRPC( Map<String, Object> buildAgentConfiguration )
        throws Exception;

    /**
     * Get build agent in Continuum Master
     * 
     * @param url - build agent URL
     * @return
     */
    BuildAgentConfiguration getBuildAgent( String url );
    
    /**
     * Get the url of the build agent that is processing the project
     * 
     * @param projectId project Id
     * 
     * @return build agent url
     */
    String getBuildAgentUrl( int projectId ) throws Exception;

    /**
     * Same method but compatible with standard XMLRPC
     * 
     * @param url - build agent URL
     * @return
     */
    Map<String, Object> getBuildAgentRPC( String url );

    /**
     * Update build agent in Continuum Master
     * 
     * @return
     * @throws Exception
     */
    BuildAgentConfiguration updateBuildAgent( BuildAgentConfiguration buildAgentConfiguration )
        throws Exception;

    /**
     * Same method but compatible with standard XMLRPC
     * 
     * @return
     * @throws Exception
     */
    Map<String, Object> updateBuildAgentRPC( Map<String, Object> buildAgentConfiguration )
        throws Exception;

    /**
     * remove build agent in Continuum Master
     * 
     * @param url - build agent URL
     * @return Exception
     */
    boolean removeBuildAgent( String url )
        throws Exception;

    /**
     * List all build agent in Continuum Master
     * 
     * @return
     */
    List<BuildAgentConfiguration> getAllBuildAgents();

    /**
     * Same method but compatible with standard XMLRPC
     * 
     * @return
     */
    List<Object> getAllBuildAgentsRPC();

    boolean pingBuildAgent( String buildAgentUrl )
        throws Exception;
}
