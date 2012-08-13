package org.apache.continuum.release.distributed.manager;

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

import org.apache.continuum.configuration.BuildAgentConfigurationException;
import org.apache.continuum.model.repository.LocalRepository;
import org.apache.continuum.release.model.PreparedRelease;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.release.ContinuumReleaseException;
import org.apache.maven.shared.release.ReleaseResult;

import java.util.List;
import java.util.Map;
import java.util.Properties;


public interface DistributedReleaseManager
{
    Map getReleasePluginParameters( int projectId, String pomFilename )
        throws ContinuumReleaseException, BuildAgentConfigurationException;

    List<Map<String, String>> processProject( int projectId, String pomFilename, boolean autoVersionSubmodules )
        throws ContinuumReleaseException, BuildAgentConfigurationException;

    String releasePrepare( Project project, Properties releaseProperties, Map<String, String> releaseVersion,
                           Map<String, String> developmentVersion, Map<String, String> environments, String username )
        throws ContinuumReleaseException, BuildAgentConfigurationException;

    ReleaseResult getReleaseResult( String releaseId )
        throws ContinuumReleaseException, BuildAgentConfigurationException;

    Map getListener( String releaseId )
        throws ContinuumReleaseException, BuildAgentConfigurationException;

    void removeListener( String releaseId )
        throws ContinuumReleaseException, BuildAgentConfigurationException;

    String getPreparedReleaseName( String releaseId )
        throws ContinuumReleaseException;

    void releasePerform( int projectId, String releaseId, String goals, String arguments, boolean useReleaseProfile,
                         LocalRepository repository, String username )
        throws ContinuumReleaseException, BuildAgentConfigurationException;

    String releasePerformFromScm( int projectId, String goals, String arguments, boolean useReleaseProfile,
                                  LocalRepository repository, String scmUrl, String scmUsername, String scmPassword,
                                  String scmTag, String scmTagBase, Map environments, String username )
        throws ContinuumReleaseException, BuildAgentConfigurationException;

    void releaseRollback( String releaseId, int projectId )
        throws ContinuumReleaseException, BuildAgentConfigurationException;

    String releaseCleanup( String releaseId )
        throws ContinuumReleaseException, BuildAgentConfigurationException;

    List<Map<String, Object>> getAllReleasesInProgress()
        throws ContinuumReleaseException, BuildAgentConfigurationException;

    String getDefaultBuildagent( int projectId );

    PreparedRelease getPreparedRelease( String releaseId, String releaseType )
        throws ContinuumReleaseException;
}