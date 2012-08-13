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
import org.jmock.Mock;

import java.util.ArrayList;
import java.util.List;

public class BuildAgentActionTest
    extends AbstractActionTest
{
    private BuildAgentAction action;

    private Mock continuumMock;

    private Mock configurationServiceMock;

    private Mock distributedBuildManagerMock;

    private List<BuildAgentConfiguration> buildAgents;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        action = new BuildAgentAction();
        continuumMock = mock( Continuum.class );
        configurationServiceMock = mock( ConfigurationService.class );
        distributedBuildManagerMock = mock( DistributedBuildManager.class );

        action.setContinuum( (Continuum) continuumMock.proxy() );

        buildAgents = new ArrayList<BuildAgentConfiguration>();
    }

    public void testAddBuildAgent()
        throws Exception
    {
        continuumMock.expects( once() ).method( "getConfiguration" ).will( returnValue(
            configurationServiceMock.proxy() ) );
        configurationServiceMock.expects( atLeastOnce() ).method( "getBuildAgents" ).will( returnValue( buildAgents ) );
        configurationServiceMock.expects( once() ).method( "addBuildAgent" ).isVoid();
        configurationServiceMock.expects( once() ).method( "store" ).isVoid();
        continuumMock.expects( once() ).method( "getDistributedBuildManager" ).will( returnValue(
            distributedBuildManagerMock.proxy() ) );
        distributedBuildManagerMock.expects( once() ).method( "update" ).isVoid();

        BuildAgentConfiguration buildAgent = new BuildAgentConfiguration();
        buildAgent.setUrl( "http://sample/agent" );

        action.setBuildAgent( buildAgent );
        action.save();
    }

    public void testDeleteBuildAgent()
        throws Exception
    {
        List<BuildAgentGroupConfiguration> buildAgentGroups = new ArrayList<BuildAgentGroupConfiguration>();

        continuumMock.expects( atLeastOnce() ).method( "getDistributedBuildManager" ).will( returnValue(
            distributedBuildManagerMock.proxy() ) );
        distributedBuildManagerMock.expects( once() ).method( "isBuildAgentBusy" ).will( returnValue( false ) );
        continuumMock.expects( once() ).method( "getConfiguration" ).will( returnValue(
            configurationServiceMock.proxy() ) );
        configurationServiceMock.expects( atLeastOnce() ).method( "getBuildAgentGroups" ).will( returnValue(
            buildAgentGroups ) );
        configurationServiceMock.expects( atLeastOnce() ).method( "getBuildAgents" ).will( returnValue( buildAgents ) );

        distributedBuildManagerMock.expects( never() ).method( "removeDistributedBuildQueueOfAgent" ).isVoid();
        distributedBuildManagerMock.expects( never() ).method( "reload" ).isVoid();
        configurationServiceMock.expects( never() ).method( "removeBuildAgent" ).isVoid();
        configurationServiceMock.expects( never() ).method( "store" ).isVoid();

        BuildAgentConfiguration buildAgent = new BuildAgentConfiguration();
        buildAgent.setUrl( "http://sample/agent" );

        action.setConfirmed( true );
        action.setBuildAgent( buildAgent );
        action.delete();
    }
}
