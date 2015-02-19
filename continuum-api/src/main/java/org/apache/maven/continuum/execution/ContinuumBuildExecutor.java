package org.apache.maven.continuum.execution;

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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.scm.ChangeSet;
import org.apache.maven.continuum.model.scm.ScmResult;

import java.io.File;
import java.util.List;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public interface ContinuumBuildExecutor
{
    String ROLE = ContinuumBuildExecutor.class.getName();

    // TODO: stream the build output
    ContinuumBuildExecutionResult build( Project project, BuildDefinition buildDefinition, File buildOutput,
                                         List<Project> projectsWithCommonScmRoot, String projectScmRootUrl )
        throws ContinuumBuildExecutorException;

    // TODO: rename to be clearer
    void updateProjectFromCheckOut( File workingDirectory, Project project, BuildDefinition buildDefinition,
                                    ScmResult scmResult )
        throws ContinuumBuildExecutorException;

    boolean isBuilding( Project project );

    void killProcess( Project project );

    // TODO: are these part of the builder interface, or a separate project/build definition interface?
    List<Artifact> getDeployableArtifacts( Project project, File workingDirectory, BuildDefinition buildDefinition )
        throws ContinuumBuildExecutorException;

    //TODO: Move as a plugin
    void backupTestFiles( Project project, int buildId, String projectScmRootUrl,
                          List<Project> projectsWithCommonScmRoot );

    boolean shouldBuild( List<ChangeSet> changes, Project continuumProject, File workingDirectory,
                         BuildDefinition buildDefinition )
        throws ContinuumBuildExecutorException;
}
