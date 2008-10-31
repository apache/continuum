package org.apache.maven.continuum.notification;

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

import org.apache.continuum.model.project.ProjectScmRoot;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 * @todo use build result for all of these? need project for those that do?
 */
public interface ContinuumNotificationDispatcher
{
    String ROLE = ContinuumNotificationDispatcher.class.getName();

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    String MESSAGE_ID_BUILD_STARTED = "BuildStarted";

    String MESSAGE_ID_CHECKOUT_STARTED = "CheckoutStarted";

    String MESSAGE_ID_CHECKOUT_COMPLETE = "CheckoutComplete";

    String MESSAGE_ID_RUNNING_GOALS = "RunningGoals";

    String MESSAGE_ID_GOALS_COMPLETED = "GoalsCompleted";

    String MESSAGE_ID_BUILD_COMPLETE = "BuildComplete";

    String MESSAGE_ID_PREPARE_BUILD_COMPLETE = "PrepareBuildComplete";

    String CONTEXT_BUILD = "build";

    String CONTEXT_BUILD_OUTPUT = "build-output";

    String CONTEXT_PROJECT = "project";

    String CONTEXT_BUILD_DEFINITION = "buildDefinition";

    String CONTEXT_PROJECT_NOTIFIER = "projectNotifier";

    String CONTEXT_BUILD_RESULT = "result";

    String CONTEXT_UPDATE_SCM_RESULT = "scmResult";

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    void buildStarted( Project project, BuildDefinition buildDefinition );

    void checkoutStarted( Project project, BuildDefinition buildDefinition );

    void checkoutComplete( Project project, BuildDefinition buildDefinition );

    void runningGoals( Project project, BuildDefinition buildDefinition, BuildResult buildResult );

    void goalsCompleted( Project project, BuildDefinition buildDefinition, BuildResult buildResult );

    void buildComplete( Project project, BuildDefinition buildDefinition, BuildResult buildResult );

    void prepareBuildComplete( ProjectScmRoot projectScmRoot );
}
