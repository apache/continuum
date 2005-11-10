/**
 *
 * Copyright 2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.gbuild.agent;

import org.apache.maven.continuum.execution.ContinuumBuildExecutionResult;
import org.apache.maven.continuum.execution.ContinuumBuildExecutor;
import org.apache.maven.continuum.execution.manager.BuildExecutorManager;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.scm.ScmResult;
import org.apache.maven.continuum.notification.ContinuumNotificationDispatcher;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.utils.ContinuumUtils;

import java.io.File;
import java.util.Date;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public class ExecuteDistributedBuilderContinuumAction extends AbstractDistributedContinuumAction {

    // ----------------------------------------------------------------------
    // Keys for the values that can be in the context
    // ----------------------------------------------------------------------

    public static final String KEY_BUILD_OUTPUT_FILE = "build-output-file";

    /**
     * @plexus.requirement
     */
    private BuildExecutorManager buildExecutorManager;

    /**
     * @plexus.requirement
     */
    private ContinuumStore store;

    /**
     * @plexus.requirement
     */
    private ContinuumNotificationDispatcher notifier;


    public void execute(Map context)
            throws Exception {
        // ----------------------------------------------------------------------
        // Get parameters from the context
        // ----------------------------------------------------------------------

        Project project = getProject(context);

        int trigger = getTrigger(context);

        ScmResult scmResult = getUpdateScmResult(context);

        ContinuumBuildExecutor buildExecutor = buildExecutorManager.getBuildExecutor(project.getExecutorId());

        // ----------------------------------------------------------------------
        // Make the build
        // ----------------------------------------------------------------------

        BuildResult build = new BuildResult();

        build.setStartTime(new Date().getTime());

        build.setState(ContinuumProjectState.BUILDING);

        build.setTrigger(trigger);

        BuildDefinition buildDefinition = getBuildDefinition(context);

        build.setScmResult(scmResult);

        store.addBuildResult(project, build);

        context.put(KEY_BUILD_ID, Integer.toString(build.getId()));

        build = store.getBuildResult(build.getId());

        try {
            notifier.runningGoals(project, build);

            File buildOutputFile = getBuildOutputFile( context );

            ContinuumBuildExecutionResult result = buildExecutor.build(project, buildDefinition, buildOutputFile);

            build.setState(result.getExitCode() == 0 ? ContinuumProjectState.OK : ContinuumProjectState.FAILED);

            build.setExitCode(result.getExitCode());
        }
        catch (Throwable e) {
            getLogger().error("Error running build", e);

            build.setState(ContinuumProjectState.ERROR);

            build.setError(ContinuumUtils.throwableToString(e));
        }
        finally {
            build.setEndTime(new Date().getTime());

            if (build.getState() == ContinuumProjectState.OK) {
                project.setBuildNumber(project.getBuildNumber() + 1);
            }

            project.setLatestBuildId(build.getId());

            build.setBuildNumber(project.getBuildNumber());

            if (build.getState() != ContinuumProjectState.OK && build.getState() != ContinuumProjectState.FAILED && build.getState() != ContinuumProjectState.ERROR) {
                build.setState(ContinuumProjectState.ERROR);
            }

            // ----------------------------------------------------------------------
            // Copy over the build result
            // ----------------------------------------------------------------------

            store.updateBuildResult(build);

            build = store.getBuildResult(build.getId());

            store.updateProject(project);

            notifier.goalsCompleted(project, build);
        }
    }

    private File getBuildOutputFile(Map context) {

        String fileName = getString( context, KEY_BUILD_OUTPUT_FILE );

        return new File( fileName );
    }

}
