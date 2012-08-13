package org.apache.continuum.model.project;

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

public class ProjectGroupSummary
{
    private int projectGroupId;

    private int numberOfSuccesses;

    private int numberOfFailures;

    private int numberOfErrors;

    private int numberOfProjects;

    public ProjectGroupSummary()
    {
    }

    public ProjectGroupSummary( int projectGroupId )
    {
        this.projectGroupId = projectGroupId;
    }

    public int getProjectGroupId()
    {
        return projectGroupId;
    }

    public void setProjectGroupId( int projectGroupId )
    {
        this.projectGroupId = projectGroupId;
    }

    public void setNumberOfSuccesses( int numberOfSuccesses )
    {
        this.numberOfSuccesses = numberOfSuccesses;
    }

    public int getNumberOfSuccesses()
    {
        return numberOfSuccesses;
    }

    public void setNumberOfFailures( int numberOfFailures )
    {
        this.numberOfFailures = numberOfFailures;
    }

    public int getNumberOfFailures()
    {
        return numberOfFailures;
    }

    public void setNumberOfErrors( int numberOfErrors )
    {
        this.numberOfErrors = numberOfErrors;
    }

    public int getNumberOfErrors()
    {
        return numberOfErrors;
    }

    public void setNumberOfProjects( int numberOfProjects )
    {
        this.numberOfProjects = numberOfProjects;
    }

    public int getNumberOfProjects()
    {
        return numberOfProjects;
    }

    public void addProjects( int projects )
    {
        this.numberOfProjects += projects;
    }

    public void addNumberOfSuccesses( int numberOfSuccesses )
    {
        this.numberOfSuccesses += numberOfSuccesses;
    }

    public void addNumberOfErrors( int numberOfErrors )
    {
        this.numberOfErrors += numberOfErrors;
    }

    public void addNumberOfFailures( int numberOfFailures )
    {
        this.numberOfFailures += numberOfFailures;
    }
}
