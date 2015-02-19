package org.apache.continuum.taskqueue;

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

import org.apache.maven.continuum.model.project.Project;
import org.codehaus.plexus.taskqueue.Task;

import java.io.File;
import java.util.List;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class CheckOutTask
    implements Task
{
    private final int projectId;

    private final File workingDirectory;

    private final String projectName;

    private final String scmUserName;

    private final String scmPassword;

    private final String scmRootUrl;

    private final List<Project> projectsWithCommonScmRoot;

    public CheckOutTask( int projectId, File workingDirectory, String projectName, String scmUserName,
                         String scmPassword, String scmRootUrl, List<Project> projectsWithCommonScmRoot )
    {
        this.projectId = projectId;

        this.workingDirectory = workingDirectory;

        this.projectName = projectName;

        this.scmUserName = scmUserName;

        this.scmPassword = scmPassword;

        this.scmRootUrl = scmRootUrl;

        this.projectsWithCommonScmRoot = projectsWithCommonScmRoot;
    }

    public int getProjectId()
    {
        return projectId;
    }

    public File getWorkingDirectory()
    {
        return workingDirectory;
    }

    public long getMaxExecutionTime()
    {
        return 0;
    }

    public String getProjectName()
    {
        return projectName;
    }


    public String getScmUserName()
    {
        return scmUserName;
    }

    public String getScmPassword()
    {
        return scmPassword;
    }

    public int getHashCode()
    {
        return this.hashCode();
    }

    public String getScmRootUrl()
    {
        return scmRootUrl;
    }

    public List<Project> getProjectsWithCommonScmRoot()
    {
        return projectsWithCommonScmRoot;
    }
}
