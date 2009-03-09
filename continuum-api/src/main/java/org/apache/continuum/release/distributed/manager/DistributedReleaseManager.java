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

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.continuum.model.repository.LocalRepository;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.release.ContinuumReleaseException;
import org.apache.maven.shared.release.ReleaseResult;

public interface DistributedReleaseManager
{
    Map getReleasePluginParameters( int projectId, String pomFilename )
        throws ContinuumReleaseException;

    List<Map<String, String>> processProject( int projectId, String pomFilename, boolean autoVersionSubmodules )
        throws ContinuumReleaseException;

    String releasePrepare( Project project, Properties releaseProperties, Map<String, String> releaseVersion, 
                           Map<String, String> developmentVersion, Map<String, String> environments )
        throws ContinuumReleaseException;

    ReleaseResult getReleaseResult( String releaseId )
        throws ContinuumReleaseException;

    Map getListener( String releaseId )
        throws ContinuumReleaseException;

    void removeListener( String releaseId )
        throws ContinuumReleaseException;

    String getPreparedReleaseName( String releaseId )
        throws ContinuumReleaseException;

    void releasePerform( int projectId, String releaseId, String goals, String arguments, boolean useReleaseProfile, LocalRepository repository )
        throws ContinuumReleaseException;

    void releasePerformFromScm( int projectId, String goals, String arguments, boolean useReleaseProfile, LocalRepository repository, 
                                String scmUrl, String scmUsername, String scmPassword, String scmTag, String scmTagBase, Map environments )
        throws ContinuumReleaseException;

    void releaseRollback( String releaseId, int projectId )
        throws ContinuumReleaseException;

    String releaseCleanup( String releaseId )
        throws ContinuumReleaseException;

    List<Map> getAllReleasesInProgress()
        throws ContinuumReleaseException;
}