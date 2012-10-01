package org.apache.maven.continuum.release;

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

import org.apache.continuum.model.release.ReleaseListenerSummary;
import org.apache.continuum.model.repository.LocalRepository;
import org.apache.continuum.taskqueue.manager.TaskQueueManagerException;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.shared.release.config.ReleaseDescriptor;
import org.codehaus.plexus.taskqueue.execution.TaskQueueExecutor;

import java.io.File;
import java.util.Map;
import java.util.Properties;

/**
 * The Continuum Release Manager is responsible for performing releases based on a release descriptor
 * that has been received by the Maven Release Plugin.
 *
 * @author Jason van Zyl
 * @version $Id$
 */
public interface ContinuumReleaseManager
{
    String ROLE = ContinuumReleaseManager.class.getName();

    /**
     * Prepare a project for release
     *
     * @param project
     * @param releaseProperties
     * @param releaseVersions
     * @param developmentVersions
     * @param listener
     * @param workingDirectory
     * @return
     * @throws ContinuumReleaseException
     */
    String prepare( Project project, Properties releaseProperties, Map<String, String> releaseVersions,
                    Map<String, String> developmentVersions, ContinuumReleaseManagerListener listener,
                    String workingDirectory )
        throws ContinuumReleaseException;

    /**
     * Prepare a project for release
     *
     * @param project
     * @param releaseProperties
     * @param releaseVersions
     * @param developmentVersions
     * @param listener
     * @param workingDirectory
     * @param environments
     * @param executable
     * @return
     * @throws ContinuumReleaseException
     */
    String prepare( Project project, Properties releaseProperties, Map<String, String> releaseVersions,
                    Map<String, String> developmentVersions, ContinuumReleaseManagerListener listener,
                    String workingDirectory, Map<String, String> environments, String executable )
        throws ContinuumReleaseException;

    /**
     * Perform a release based on a given releaseId
     *
     * @param releaseId
     * @param buildDirectory
     * @param goals
     * @param useReleaseProfile
     * @param listener
     * @throws ContinuumReleaseException
     * @deprecated to remove as not used anymore
     */
    void perform( String releaseId, File buildDirectory, String goals, String arguments, boolean useReleaseProfile,
                  ContinuumReleaseManagerListener listener )
        throws ContinuumReleaseException;

    /**
     * Perform a release based on a release descriptor received by the Maven Release Plugin.
     *
     * @param releaseId
     * @param workingDirectory
     * @param buildDirectory
     * @param goals
     * @param useReleaseProfile
     * @param listener
     * @throws ContinuumReleaseException
     * @deprecated to remove as not used anymore
     */
    void perform( String releaseId, String workingDirectory, File buildDirectory, String goals, String arguments,
                  boolean useReleaseProfile, ContinuumReleaseManagerListener listener )
        throws ContinuumReleaseException;


    /**
     * FIXME use a bean to replace such very huge parameter number (ContinuumReleaseRequest)
     *
     * @param releaseId
     * @param buildDirectory
     * @param goals
     * @param arguments
     * @param useReleaseProfile
     * @param listener
     * @param repository
     * @throws ContinuumReleaseException
     */
    void perform( String releaseId, File buildDirectory, String goals, String arguments, boolean useReleaseProfile,
                  ContinuumReleaseManagerListener listener, LocalRepository repository )
        throws ContinuumReleaseException;

    /**
     * Rollback changes made by a previous release.
     *
     * @param releaseId
     * @param workingDirectory
     * @param listener
     * @throws ContinuumReleaseException
     */
    void rollback( String releaseId, String workingDirectory, ContinuumReleaseManagerListener listener )
        throws ContinuumReleaseException;

    Map<String, ReleaseDescriptor> getPreparedReleases();

    Map<String, String> getPreparedReleasesForProject( String groupId, String artifactId );

    Map getReleaseResults();

    Map getListeners();


    /**
     * Clean up the tagname to respect the scm provider policy.
     *
     * @param scmUrl  The scm url
     * @param tagName The tag name
     * @return The cleaned tag name
     */
    String sanitizeTagName( String scmUrl, String tagName )
        throws Exception;

    /**
     * @param releaseId
     * @return
     */
    ReleaseListenerSummary getListener( String releaseId );

    /**
     * Determines if there is an ongoing release
     *
     * @return true if there is an ongoing release; false otherwise
     * @throws Exception if unable to determine if release is ongoing
     */
    boolean isExecutingRelease()
        throws Exception;

    /**
     * Retrieve the Release TaskQueueExecutor instance
     *
     * @return Release TaskQueueExecutor instance
     * @throws TaskQueueManagerException if unable to retrieve the Release TaskQueueExecutor instance
     */
    TaskQueueExecutor getPerformReleaseTaskQueueExecutor()
        throws TaskQueueManagerException;

    /**
     * Retrieve the PrepareRelease TaskQueueExecutor instance
     *
     * @return PrepareRelease TaskQueueExecutor instance
     * @throws TaskQueueManagerException if unable to retrieve the PrepareRelease TaskQueueExecutor instance
     */
    TaskQueueExecutor getPrepareReleaseTaskQueueExecutor()
        throws TaskQueueManagerException;

    /**
     * Retrieve the RollbackRelease TaskQueueExecutor instance
     *
     * @return RollbackRelease TaskQueueExecutor instance
     * @throws TaskQueueManagerException if unable to retrieve the RollbackRelease TaskQueueExecutor instance
     */
    TaskQueueExecutor getRollbackReleaseTaskQueueExecutor()
        throws TaskQueueManagerException;
}
