package org.apache.continuum.buildagent.utils;

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

import org.apache.continuum.buildagent.buildcontext.BuildContext;
import org.apache.maven.continuum.model.project.Project;

/**
 * @author Jan Stevens Ancajas
 */
public class BuildContextToProject
{
    public static Project getProject( BuildContext buildContext )
    {
        Project project = new Project();

        project.setId( buildContext.getProjectId() );

        project.setName( buildContext.getProjectName() );

        project.setVersion( buildContext.getProjectVersion() );

        project.setScmUrl( buildContext.getScmUrl() );

        project.setScmUsername( buildContext.getScmUsername() );

        project.setScmPassword( buildContext.getScmPassword() );

        project.setScmTag( buildContext.getScmTag() );

        project.setExecutorId( buildContext.getExecutorId() );

        project.setState( buildContext.getProjectState() );

        project.setOldState( buildContext.getProjectState() );

        project.setBuildNumber( buildContext.getBuildNumber() );

        return project;
    }
}
