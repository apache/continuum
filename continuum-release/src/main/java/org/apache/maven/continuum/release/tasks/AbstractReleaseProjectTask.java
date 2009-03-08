package org.apache.maven.continuum.release.tasks;

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

import org.apache.maven.continuum.model.system.Profile;
import org.apache.maven.shared.release.ReleaseManagerListener;
import org.apache.maven.shared.release.config.ReleaseDescriptor;
import org.codehaus.plexus.taskqueue.Task;

/**
 * @author Edwin Punzalan
 * @version $Id$
 */
public abstract class AbstractReleaseProjectTask
    implements Task, ReleaseProjectTask
{
    private String releaseId;

    private ReleaseDescriptor descriptor;

    private ReleaseManagerListener listener;

    private long maxExecutionTime;

    public AbstractReleaseProjectTask( String releaseId, ReleaseDescriptor descriptor, ReleaseManagerListener listener )
    {
        this.releaseId = releaseId;
        this.descriptor = descriptor;
        this.listener = listener;
    }

    public ReleaseDescriptor getDescriptor()
    {
        return descriptor;
    }

    public void setDescriptor( ReleaseDescriptor descriptor )
    {
        this.descriptor = descriptor;
    }

    public String getReleaseId()
    {
        return releaseId;
    }

    public void setReleaseId( String releaseId )
    {
        this.releaseId = releaseId;
    }

    public ReleaseManagerListener getListener()
    {
        return listener;
    }

    public void setListener( ReleaseManagerListener listener )
    {
        this.listener = listener;
    }

    public long getMaxExecutionTime()
    {
        return maxExecutionTime;
    }

    public void setMaxExecutionTime( long maxTime )
    {
        this.maxExecutionTime = maxTime;
    }
}
