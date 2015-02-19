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
import org.apache.maven.continuum.model.project.ProjectNotifier;

import java.util.List;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 */
public class MessageContext
{
    private Project project;

    private BuildDefinition buildDefinition;

    private List<ProjectNotifier> notifiers;

    private BuildResult buildResult;

    private ProjectScmRoot projectScmRoot;

    public Project getProject()
    {
        return project;
    }

    public void setProject( Project project )
    {
        this.project = project;
    }

    public BuildDefinition getBuildDefinition()
    {
        return buildDefinition;
    }

    public void setBuildDefinition( BuildDefinition buildDefinition )
    {
        this.buildDefinition = buildDefinition;
    }

    public List<ProjectNotifier> getNotifiers()
    {
        return notifiers;
    }

    public void setNotifier( List<ProjectNotifier> notifiers )
    {
        this.notifiers = notifiers;
    }

    public BuildResult getBuildResult()
    {
        return buildResult;
    }

    public void setBuildResult( BuildResult buildResult )
    {
        this.buildResult = buildResult;
    }

    public ProjectScmRoot getProjectScmRoot()
    {
        return projectScmRoot;
    }

    public void setProjectScmRoot( ProjectScmRoot projectScmRoot )
    {
        this.projectScmRoot = projectScmRoot;
    }
}
