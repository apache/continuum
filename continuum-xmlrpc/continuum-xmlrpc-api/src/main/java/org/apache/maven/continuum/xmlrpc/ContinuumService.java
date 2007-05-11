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
import org.apache.maven.continuum.xmlrpc.project.BuildResult;
import org.apache.maven.continuum.xmlrpc.project.Project;
import org.apache.maven.continuum.xmlrpc.project.ProjectGroup;
import org.apache.maven.continuum.xmlrpc.project.ProjectGroupSummary;
import org.apache.maven.continuum.xmlrpc.project.ProjectSummary;
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
     * @return List of {@link ProjectSummary}
     * @throws ContinuumException
     * @throws XmlRpcException
     */
    List getProjects()
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

    // ----------------------------------------------------------------------
    // Projects Groups
    // ----------------------------------------------------------------------

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

    // ----------------------------------------------------------------------
    // Build Results
    // ----------------------------------------------------------------------

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
    List getBuildResultsForProject( int projectId )
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
     * @param project The project to add. groupId, artifactId, version and scm informations are required
     * @return The project populated with the id.
     * @throws ContinuumException
     * @throws XmlRpcException
     */
    ProjectSummary addAntProject( ProjectSummary project )
        throws ContinuumException, XmlRpcException;

    // ----------------------------------------------------------------------
    // Maven Shell projects
    // ----------------------------------------------------------------------

    /**
     * Add an shell project.
     *
     * @param project The project to add. groupId, artifactId, version and scm informations are required
     * @return The project populated with the id.
     * @throws ContinuumException
     * @throws XmlRpcException
     */
    ProjectSummary addShellProject( ProjectSummary project )
        throws ContinuumException, XmlRpcException;

    // ----------------------------------------------------------------------
    // ADMIN TASKS
    // ----------------------------------------------------------------------

    // ----------------------------------------------------------------------
    // TODO:Schedule
    // ----------------------------------------------------------------------

    // ----------------------------------------------------------------------
    // TODO:Users
    // ----------------------------------------------------------------------

}
