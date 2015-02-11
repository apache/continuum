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

import org.apache.continuum.buildagent.buildcontext.BuildContext;
import org.codehaus.plexus.component.annotations.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Jan Steven Ancajas
 */
@Component( role = org.apache.continuum.buildagent.buildcontext.manager.BuildContextManager.class, hint = "default" )
public class DefaultBuildContextManager
    implements BuildContextManager
{
    public Map<Integer, BuildContext> buildContexts;

    public BuildContext getBuildContext( int projectId )
    {
        if ( buildContexts != null )
        {
            return buildContexts.get( projectId );
        }

        return null;
    }

    public List<BuildContext> getBuildContexts()
    {
        List<BuildContext> bContexts = new ArrayList<BuildContext>();

        if ( buildContexts != null )
        {
            bContexts.addAll( buildContexts.values() );
        }

        return bContexts;
    }

    public void addBuildContexts( List<BuildContext> buildContextList )
    {
        if ( buildContexts == null )
        {
            buildContexts = new HashMap<Integer, BuildContext>();
        }

        for ( BuildContext buildContext : buildContextList )
        {
            buildContexts.put( buildContext.getProjectId(), buildContext );
        }
    }

    public void removeBuildContext( int projectId )
    {
        BuildContext buildContext = getBuildContext( projectId );

        if ( buildContext != null )
        {
            buildContexts.remove( buildContext );
        }
    }
}