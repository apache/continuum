package org.apache.maven.continuum.release.tasks;

/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.plugins.release.config.ReleaseDescriptor;
import org.codehaus.plexus.taskqueue.Task;

/**
 * @author Edwin Punzalan
 */
public abstract class AbstractReleaseProjectTask
    implements Task
{
    private String releaseId;

    private ReleaseDescriptor descriptor;

    public AbstractReleaseProjectTask( String releaseId, ReleaseDescriptor descriptor )
    {
        this.releaseId = releaseId;
        this.descriptor = descriptor;
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
}
