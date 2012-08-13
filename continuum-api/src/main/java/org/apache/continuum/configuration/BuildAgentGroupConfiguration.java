package org.apache.continuum.configuration;

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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BuildAgentGroupConfiguration
{
    private String name;

    private List<BuildAgentConfiguration> buildAgents = new ArrayList<BuildAgentConfiguration>();

    public BuildAgentGroupConfiguration()
    {
        //nil
    }

    public BuildAgentGroupConfiguration( String name, List<BuildAgentConfiguration> buildAgents )
    {
        this.name = name;
        this.buildAgents = buildAgents;
    }

    public void addBuildAgent( BuildAgentConfiguration buildAgent )
    {
        buildAgents.add( buildAgent );
    }

    public void removeBuildAgent( BuildAgentConfiguration buildAgent )
    {
        Iterator<BuildAgentConfiguration> iterator = buildAgents.iterator();
        while ( iterator.hasNext() )
        {
            BuildAgentConfiguration agent = iterator.next();
            if ( agent.getUrl().equals( buildAgent.getUrl() ) )
            {
                iterator.remove();
            }
        }
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public List<BuildAgentConfiguration> getBuildAgents()
    {
        return buildAgents;
    }

    public void setBuildAgents( List<BuildAgentConfiguration> buildAgents )
    {
        this.buildAgents = buildAgents;
    }

}
