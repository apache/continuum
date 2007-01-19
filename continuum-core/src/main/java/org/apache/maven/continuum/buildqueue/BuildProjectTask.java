package org.apache.maven.continuum.buildqueue;

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

import org.codehaus.plexus.taskqueue.Task;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class BuildProjectTask
    implements Task
{
    private long projectId;

    private long buildDefinitionId;

    private long timestamp;

    private int trigger;

    private long maxExecutionTime;

    public BuildProjectTask( long projectId, long buildDefinitionId, int trigger )
    {
        this.projectId = projectId;

        this.buildDefinitionId = buildDefinitionId;

        this.timestamp = System.currentTimeMillis();

        this.trigger = trigger;
    }

    public long getProjectId()
    {
        return projectId;
    }

    public long getBuildDefinitionId()
    {
        return buildDefinitionId;
    }

    public long getTimestamp()
    {
        return timestamp;
    }

    public int getTrigger()
    {
        return trigger;
    }

    public void setMaxExecutionTime( long maxExecutionTime )
    {
        this.maxExecutionTime = maxExecutionTime;
    }

    public long getMaxExecutionTime()
    {
        return maxExecutionTime;
    }
}
