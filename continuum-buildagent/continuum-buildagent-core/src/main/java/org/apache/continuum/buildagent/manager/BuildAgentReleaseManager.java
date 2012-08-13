package org.apache.continuum.buildagent.manager;

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

import org.apache.maven.continuum.release.ContinuumReleaseException;
import org.apache.maven.continuum.release.ContinuumReleaseManager;
import org.apache.maven.shared.release.ReleaseResult;

import java.util.Map;
import java.util.Properties;

public interface BuildAgentReleaseManager
{
    String ROLE = BuildAgentReleaseManager.class.getName();

    String releasePrepare( Map<String, Object> project, Properties properties, Map<String, String> releaseVersion,
                           Map<String, String> developmentVersion, Map<String, String> environments, String username )
        throws ContinuumReleaseException;

    ReleaseResult getReleaseResult( String releaseId );

    Map<String, Object> getListener( String releaseId );

    void removeListener( String releaseId );

    String getPreparedReleaseName( String releaseId );

    void releasePerform( String releaseId, String goals, String arguments, boolean useReleaseProfile, Map repository,
                         String username )
        throws ContinuumReleaseException;

    String releasePerformFromScm( String goals, String arguments, boolean useReleaseProfile, Map repository,
                                  String scmUrl, String scmUsername, String scmPassword, String scmTag,
                                  String scmTagBase, Map<String, String> environments, String username )
        throws ContinuumReleaseException;

    String releaseCleanup( String releaseId );

    void releaseRollback( String releaseId, int projectId )
        throws ContinuumReleaseException;

    ContinuumReleaseManager getReleaseManager();
}
