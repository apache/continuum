package org.apache.continuum.buildagent.buildcontext.manager;

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
import org.springframework.stereotype.Service;

/**
 * @author Jan Steven Ancajas
 */
@Service("buildContextManager")
public class DefaultBuildContextManager
    implements BuildContextManager
{
    public List<BuildContext> buildContexts;

    public BuildContext getBuildContext( int projectId )
    {
        BuildContext context = null;

        if (buildContexts!= null)
        {
            for ( BuildContext item : buildContexts )
            {
                if (item.getProjectId() == projectId)
                {
                    context = item;
                    break;
                }
            }
        }

        return context;
    }

    public List<BuildContext> getBuildContextList()
    {
        return buildContexts;
    }

    public void setBuildContextList( List<BuildContext> buildContexts )
    {
        this.buildContexts = buildContexts;
    }
}