package org.apache.maven.continuum.web.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.continuum.builder.distributed.manager.DistributedBuildManager;
import org.apache.continuum.configuration.BuildAgentConfiguration;
import org.apache.continuum.configuration.BuildAgentGroupConfiguration;
import org.apache.continuum.web.action.admin.BuildAgentAction;
import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class BuildAgentActionTest
    extends MockObjectTestCase
{
    private BuildAgentAction action;

    private Mock continuumMock;

    private Mock configurationServiceMock;

    private Mock distributedBuildManagerMock;

    private List<BuildAgentConfiguration> buildAgents;

    protected void setUp()
        throws Exception
    {
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
        continuumMock.expects( once() ).method( "getConfiguration" ).will( returnValue( configurationServiceMock.proxy() ) );
        configurationServiceMock.expects( atLeastOnce() ).method( "getBuildAgents" ).will( returnValue( buildAgents ) );
        configurationServiceMock.expects( once() ).method( "addBuildAgent" ).isVoid();
        configurationServiceMock.expects( once() ).method( "store" ).isVoid();
        continuumMock.expects( once() ).method( "getDistributedBuildManager" ).will( returnValue( distributedBuildManagerMock.proxy() ) );
        distributedBuildManagerMock.expects( once() ).method( "reload" ).isVoid();
        
        BuildAgentConfiguration buildAgent = new BuildAgentConfiguration();
        buildAgent.setUrl( "http://sample/agent" );

        action.setBuildAgent( buildAgent );
        action.save();
    }

    public void testDeleteBuildAgent()
        throws Exception
    {
        List<BuildAgentGroupConfiguration> buildAgentGroups = new ArrayList<BuildAgentGroupConfiguration>();

        continuumMock.expects( atLeastOnce() ).method( "getDistributedBuildManager" ).will( returnValue( distributedBuildManagerMock.proxy() ) );
        distributedBuildManagerMock.expects( once() ).method( "isBuildAgentBusy" ).will( returnValue( false ) );
        continuumMock.expects( once() ).method( "getConfiguration" ).will( returnValue( configurationServiceMock.proxy() ) );
        configurationServiceMock.expects( atLeastOnce() ).method( "getBuildAgentGroups" ).will( returnValue( buildAgentGroups ) );
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
