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

public class AuditLogConstants
{
    public static final String PROJECT = "PROJECT";

    public static final String SCHEDULE = "BUILD_SCHEDULE";

    public static final String TEMPLATE = "BUILD_TEMPLATE";

    public static final String BUILD_DEFINITION = "BUILD_DEFINITION";
    
    // events
    public static final String FORCE_BUILD = "Forced Project Build";

    public static final String CANCEL_BUILD = "Cancelled Project Build";

    public static final String CI_BUILD = "Scheduled Project Build";

    public static final String PREPARE_RELEASE = "Prepare Project Release";

    public static final String PERFORM_RELEASE = "Perform Project Release";

    public static final String ROLLBACK_RELEASE = "Rollback Project Release";

    public static final String ADD_M2_PROJECT = "Added M2 Project";

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
}

