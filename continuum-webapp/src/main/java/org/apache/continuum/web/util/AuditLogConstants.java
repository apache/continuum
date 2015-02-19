package org.apache.continuum.web.util;

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

/**
 * @author Jevica Arianne B. Zurbano
 * @since 09 apr 09
 */
public class AuditLogConstants
{
    public static final String PROJECT = "PROJECT";

    public static final String SCHEDULE = "BUILD_SCHEDULE";

    public static final String TEMPLATE = "BUILD_TEMPLATE";

    public static final String BUILD_DEFINITION = "BUILD_DEFINITION";

    public static final String PROJECT_GROUP = "PROJECT_GROUP";

    public static final String BUILD_RESULT = "BUILD_RESULT";

    public static final String BUILD_QUEUE = "BUILD_QUEUE";

    public static final String BUILD_AGENT = "BUILD_AGENT";

    public static final String LOCAL_REPOSITORY = "LOCAL_REPOSITORY";

    public static final String DIRECTORY = "DIRECTORY";

    // events
    public static final String FORCE_BUILD = "Forced Project Build";

    public static final String CANCEL_BUILD = "Cancelled Project Build";

    public static final String CI_BUILD = "Scheduled Project Build";

    public static final String PREPARE_RELEASE = "Prepare Project Release";

    public static final String PERFORM_RELEASE = "Perform Project Release";

    public static final String ROLLBACK_RELEASE = "Rollback Project Release";

    public static final String ADD_M2_PROJECT = "Added M2 Project";

    public static final String ADD_M2_PROJECT_FAILED = "Failed Adding M2 Project";

    public static final String ADD_M1_PROJECT = "Added M1 Project";

    public static final String ADD_M1_PROJECT_FAILED = "Failed Adding M1 Project";

    public static final String ADD_PROJECT = "Added Project";

    public static final String MODIFY_PROJECT = "Modified Project";

    public static final String REMOVE_PROJECT = "Removed Project";

    public static final String ADD_PROJECT_GROUP = "Added Project Group";

    public static final String MODIFY_PROJECT_GROUP = "Modified Project Group";

    public static final String REMOVE_PROJECT_GROUP = "Removed Project Group";

    public static final String MODIFY_SCHEDULE = "Modified Build Schedule";

    public static final String ADD_SCHEDULE = "Added Build Schedule";

    public static final String REMOVE_SCHEDULE = "Removed Build Schedule";

    public static final String ADD_GOAL = "Added Build Definition";

    public static final String MODIFY_GOAL = "Modified Build Definition";

    public static final String REMOVE_GOAL = "Removed Build Definition";

    public static final String ADD_TEMPLATE = "Added Build Definition Template";

    public static final String MODIFY_TEMPLATE = "Modified Build Definition Template";

    public static final String REMOVE_TEMPLATE = "Removed Build Definition Template";

    public static final String REMOVE_BUILD_RESULT = "Removed Build Result";

    public static final String ADD_BUILD_QUEUE = "Added Build Queue";

    public static final String REMOVE_BUILD_QUEUE = "Removed Build Queue";

    public static final String PURGE_LOCAL_REPOSITORY = "Purged Local Repository";

    public static final String PURGE_DIRECTORY_RELEASES = "Purged Releases Directory";

    public static final String PURGE_DIRECTORY_BUILDOUTPUT = "Purged Build Output Directory";

    public static final String ADD_BUILD_AGENT = "Added Build Agent";

    public static final String ADD_BUILD_AGENT_GROUP = "Added Build Agent Group";

    public static final String MODIFY_BUILD_AGENT = "Modified Build Agent";

    public static final String MODIFY_BUILD_AGENT_GROUP = "Modified Build Agent Group";

    public static final String REMOVE_BUILD_AGENT = "Removed Build Agent";

    public static final String REMOVE_BUILD_AGENT_GROUP = "Removed Build Agent Group";
}

