package org.apache.continuum.buildagent.taskqueue;

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

import java.util.List;

import org.apache.continuum.buildagent.buildcontext.BuildContext;
import org.codehaus.plexus.taskqueue.Task;

public class PrepareBuildProjectsTask
    implements Task
{
    private final List<BuildContext> buildContexts;

    private final int trigger;

    private final int projectGroupId;

    private final String scmRootAddress;

    private final int scmRootId;

    public PrepareBuildProjectsTask( List<BuildContext> buildContexts, int trigger, int projectGroupId,
                                     String scmRootAddress, int scmRootId )
    {
        this.buildContexts = buildContexts;
        this.trigger = trigger;
        this.projectGroupId = projectGroupId;
        this.scmRootAddress = scmRootAddress;
        this.scmRootId = scmRootId;
    }

    public long getMaxExecutionTime()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public List<BuildContext> getBuildContexts()
    {
        return buildContexts;
    }

    public int getTrigger()
    {
        return trigger;
    }

    public int getProjectGroupId()
    {
        return projectGroupId;
    }

    public String getScmRootAddress()
    {
        return scmRootAddress;
    }

    public int getScmRootId()
    {
        return scmRootId;
    }

    public int getHashCode()
    {
        return projectGroupId + scmRootId + trigger;
    }
}
