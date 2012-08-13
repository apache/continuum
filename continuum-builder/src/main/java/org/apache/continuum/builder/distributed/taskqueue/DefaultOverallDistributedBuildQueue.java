package org.apache.continuum.builder.distributed.taskqueue;

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

import org.apache.commons.lang.ArrayUtils;
import org.apache.continuum.builder.distributed.executor.DistributedBuildTaskQueueExecutor;
import org.apache.continuum.builder.distributed.executor.ThreadedDistributedBuildTaskQueueExecutor;
import org.apache.continuum.taskqueue.OverallDistributedBuildQueue;
import org.apache.continuum.taskqueue.PrepareBuildProjectsTask;
import org.codehaus.plexus.taskqueue.Task;
import org.codehaus.plexus.taskqueue.TaskQueue;
import org.codehaus.plexus.taskqueue.TaskQueueException;

import java.util.ArrayList;
import java.util.List;

public class DefaultOverallDistributedBuildQueue
    implements OverallDistributedBuildQueue
{
    private String buildAgentUrl;

    private DistributedBuildTaskQueueExecutor distributedBuildTaskQueueExecutor;

    public void addToDistributedBuildQueue( Task distributedBuildTask )
        throws TaskQueueException
    {
        getDistributedBuildQueue().put( distributedBuildTask );
    }

    public String getBuildAgentUrl()
    {
        return buildAgentUrl;
    }

    public TaskQueue getDistributedBuildQueue()
    {
        return ( (ThreadedDistributedBuildTaskQueueExecutor) distributedBuildTaskQueueExecutor ).getQueue();
    }

    public DistributedBuildTaskQueueExecutor getDistributedBuildTaskQueueExecutor()
    {
        return distributedBuildTaskQueueExecutor;
    }

    public List<PrepareBuildProjectsTask> getProjectsInQueue()
        throws TaskQueueException
    {
        return getDistributedBuildQueue().getQueueSnapshot();
    }

    public boolean isInDistributedBuildQueue( int projectGroupId, int scmRootId )
        throws TaskQueueException
    {
        List<PrepareBuildProjectsTask> tasks = getProjectsInQueue();

        for ( PrepareBuildProjectsTask task : tasks )
        {
            if ( task != null )
            {
                if ( task.getProjectGroupId() == projectGroupId && task.getProjectScmRootId() == scmRootId )
                {
                    return true;
                }
            }
        }

        return false;
    }

    public void removeFromDistributedBuildQueue( int projectGroupId, int scmRootId )
        throws TaskQueueException
    {
        List<PrepareBuildProjectsTask> tasks = getProjectsInQueue();

        for ( PrepareBuildProjectsTask task : tasks )
        {
            if ( task != null )
            {
                if ( task.getProjectGroupId() == projectGroupId && task.getProjectScmRootId() == scmRootId )
                {
                    getDistributedBuildQueue().remove( task );
                    return;
                }
            }
        }
    }

    public void removeFromDistributedBuildQueue( int[] hashCodes )
        throws TaskQueueException
    {
        List<PrepareBuildProjectsTask> tasks = getProjectsInQueue();

        List<PrepareBuildProjectsTask> tasksToRemove = new ArrayList<PrepareBuildProjectsTask>();

        for ( PrepareBuildProjectsTask task : tasks )
        {
            if ( task != null )
            {
                if ( ArrayUtils.contains( hashCodes, task.getHashCode() ) )
                {
                    tasksToRemove.add( task );
                }
            }
        }

        if ( !tasksToRemove.isEmpty() )
        {
            getDistributedBuildQueue().removeAll( tasksToRemove );
        }
    }

    public void removeFromDistributedBuildQueueByHashCode( int hashCode )
        throws TaskQueueException
    {
        List<PrepareBuildProjectsTask> tasks = getProjectsInQueue();

        for ( PrepareBuildProjectsTask task : tasks )
        {
            if ( task != null )
            {
                if ( task.getHashCode() == hashCode )
                {
                    getDistributedBuildQueue().remove( task );
                    return;
                }
            }
        }
    }

    public void setBuildAgentUrl( String buildAgentUrl )
    {
        this.buildAgentUrl = buildAgentUrl;
    }

    public void setDistributedBuildTaskQueueExecutor(
        DistributedBuildTaskQueueExecutor distributedBuildTaskQueueExecutor )
    {
        this.distributedBuildTaskQueueExecutor = distributedBuildTaskQueueExecutor;
    }
}
