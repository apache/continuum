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

import org.apache.continuum.taskqueue.manager.TaskQueueManager;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.taskqueue.Task;

/**
 * "Overall" queue which handles 
 * 
 * @author <a href="mailto:oching@apache.org">Maria Odea Ching</a>
 * @plexus.component role="org.apache.continuum.taskqueue.OverallQueue"
 */
public class DefaultOverallQueue
    extends AbstractLogEnabled 
    implements OverallQueue
{
    /**
     * @plexus.requirement
     * 
     * TODO: this should not be a singleton now!
     */
    private TaskQueueManager taskQueueManager;
        
    public void addToPrepareBuildProjectsQueue( Task prepareBuildTask )
        throws Exception
    {
        if( prepareBuildTask != null )
        {
            taskQueueManager.getPrepareBuildQueue().put( prepareBuildTask );
        }
        else
        {
            getLogger().warn( "Cannot add task to prepare-build-project queue." );
        }
    }

    public void addToBuildQueue( Task  buildTask )
        throws Exception
    {
        if( buildTask != null )
        {
            taskQueueManager.getBuildQueue().put( buildTask );
        }
        else
        {
            getLogger().warn( "Cannot add task to build-project queue." );
        }
    }

    public void addToCheckoutQueue( Task checkoutTask )
        throws Exception
    {
        if( checkoutTask != null )
        {
            taskQueueManager.getCheckoutQueue().put( checkoutTask );
        }
        else
        {
            getLogger().warn( "Cannot add task to checkout queue." );
        }
    }

    public TaskQueueManager getTaskQueueManager()
    {
        return taskQueueManager;
    }
}
