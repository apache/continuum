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

import org.apache.continuum.utils.build.BuildTrigger;
import org.codehaus.plexus.taskqueue.Task;

import java.util.Map;

public class PrepareBuildProjectsTask
    implements Task
{
    private final Map<Integer, Integer> projectsBuildDefinitionsMap;

    private BuildTrigger buildTrigger;

    private final int projectGroupId;

    private final String projectGroupName;

    private final String scmRootAddress;

    private final int projectScmRootId;

    public PrepareBuildProjectsTask( Map<Integer, Integer> projectsBuildDefinitionsMap, BuildTrigger buildTrigger,
                                     int projectGroupId, String projectGroupName, String scmRootAddress,
                                     int projectScmRootId )
    {
        this.projectsBuildDefinitionsMap = projectsBuildDefinitionsMap;
        this.buildTrigger = buildTrigger;
        this.projectGroupId = projectGroupId;
        this.projectGroupName = projectGroupName;
        this.scmRootAddress = scmRootAddress;
        this.projectScmRootId = projectScmRootId;
    }

    public long getMaxExecutionTime()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public Map<Integer, Integer> getProjectsBuildDefinitionsMap()
    {
        return projectsBuildDefinitionsMap;
    }

    public BuildTrigger getBuildTrigger()
    {
        return buildTrigger;
    }

    public int getHashCode()
    {
        return this.hashCode();
    }

    public int getProjectGroupId()
    {
        return projectGroupId;
    }

    public String getProjectGroupName()
    {
        return projectGroupName;
    }

    public String getScmRootAddress()
    {
        return scmRootAddress;
    }

    public int getProjectScmRootId()
    {
        return projectScmRootId;
    }

    public int hashCode()
    {
        return this.projectGroupId + this.projectScmRootId + this.buildTrigger.getTrigger();
    }
}
