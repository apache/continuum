package org.apache.maven.continuum.web.action;

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

import org.apache.continuum.builder.distributed.manager.DistributedBuildManager;
import org.apache.continuum.configuration.BuildAgentConfiguration;
import org.apache.continuum.configuration.BuildAgentGroupConfiguration;
import org.apache.continuum.web.action.AbstractActionTest;
import org.apache.continuum.web.action.admin.BuildAgentAction;
import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class BuildAgentActionTest
    extends AbstractActionTest
{
    private BuildAgentAction action;

    private Continuum continuum;

    private ConfigurationService configurationService;

    private DistributedBuildManager distributedBuildManager;

    private List<BuildAgentConfiguration> buildAgents;

    @Before
    public void setUp()
        throws Exception
    {
        continuum = mock( Continuum.class );
        configurationService = mock( ConfigurationService.class );
        distributedBuildManager = mock( DistributedBuildManager.class );

        action = new BuildAgentAction();
        action.setContinuum( continuum );

        buildAgents = new ArrayList<BuildAgentConfiguration>();
    }

    @Test
    public void testAddBuildAgent()
        throws Exception
    {
        when( continuum.getConfiguration() ).thenReturn( configurationService );
        when( configurationService.getBuildAgents() ).thenReturn( buildAgents );
        when( continuum.getDistributedBuildManager() ).thenReturn( distributedBuildManager );

        BuildAgentConfiguration buildAgent = new BuildAgentConfiguration();
        buildAgent.setUrl( "http://sample/agent" );
        action.setBuildAgent( buildAgent );
        action.save();

        verify( configurationService ).addBuildAgent( any( BuildAgentConfiguration.class ) );
        verify( configurationService ).store();
        verify( distributedBuildManager ).update( any( BuildAgentConfiguration.class ) );
    }

    @Test
    public void testDeleteBuildAgent()
        throws Exception
    {
        List<BuildAgentGroupConfiguration> buildAgentGroups = new ArrayList<BuildAgentGroupConfiguration>();

        when( continuum.getDistributedBuildManager() ).thenReturn( distributedBuildManager );
        when( distributedBuildManager.isBuildAgentBusy( anyString() ) ).thenReturn( false );
        when( continuum.getConfiguration() ).thenReturn( configurationService );
        when( configurationService.getBuildAgentGroups() ).thenReturn( buildAgentGroups );
        when( configurationService.getBuildAgents() ).thenReturn( buildAgents );

        BuildAgentConfiguration buildAgent = new BuildAgentConfiguration();
        buildAgent.setUrl( "http://sample/agent" );

        action.setConfirmed( true );
        action.setBuildAgent( buildAgent );
        action.delete();

        verify( distributedBuildManager, never() ).removeDistributedBuildQueueOfAgent( anyString() );
        verify( distributedBuildManager, never() ).reload();
        verify( configurationService, never() ).removeBuildAgent( any( BuildAgentConfiguration.class ) );
        verify( configurationService, never() ).store();
    }
}
