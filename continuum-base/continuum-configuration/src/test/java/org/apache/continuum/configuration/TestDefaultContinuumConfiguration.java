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

import org.codehaus.plexus.spring.PlexusInSpringTestCase;
import org.codehaus.plexus.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:olamy@apache.org">olamy</a>
 * @since 17 juin 2008
 */
public class TestDefaultContinuumConfiguration
    extends PlexusInSpringTestCase
{
    private static final Logger log = LoggerFactory.getLogger( TestDefaultContinuumConfiguration.class );

    private static final String confFile = "target/test-classes/conf/continuum.xml";

    @Override
    protected void setUp()
        throws Exception
    {
        log.info( "appserver.base : " + System.getProperty( "appserver.base" ) );

        File originalConf = new File( getBasedir(), "src/test/resources/conf/continuum.xml" );

        File confUsed = new File( getBasedir(), confFile );
        if ( confUsed.exists() )
        {
            confUsed.delete();
        }
        FileUtils.copyFile( originalConf, confUsed );

        super.setUp();
    }

    public void testLoad()
        throws Exception
    {
        ContinuumConfiguration configuration = (ContinuumConfiguration) lookup( ContinuumConfiguration.class,
                                                                                "default" );
        assertNotNull( configuration );
        GeneralConfiguration generalConfiguration = configuration.getGeneralConfiguration();
        assertNotNull( generalConfiguration );
        assertNotNull( generalConfiguration.getBaseUrl() );
        assertEquals( "http://test", generalConfiguration.getBaseUrl() );
        assertEquals( new File( "myBuildOutputDir" ), generalConfiguration.getBuildOutputDirectory() );
        assertTrue( generalConfiguration.isDistributedBuildEnabled() );
        assertNotNull( generalConfiguration.getBuildAgents() );
        org.apache.continuum.configuration.BuildAgentConfiguration buildAgentConfig =
            generalConfiguration.getBuildAgents().get( 0 );
        assertEquals( "http://buildagent/xmlrpc", buildAgentConfig.getUrl() );
        assertEquals( "linux", buildAgentConfig.getDescription() );
        assertTrue( buildAgentConfig.isEnabled() );

        // agent group tests        
        assertNotNull( "agent group", generalConfiguration.getBuildAgentGroups() );
        BuildAgentGroupConfiguration buildAgentGroupConfig = generalConfiguration.getBuildAgentGroups().get( 0 );
        assertEquals( "group-agent-1", buildAgentGroupConfig.getName() );
        BuildAgentConfiguration agentConfig = buildAgentGroupConfig.getBuildAgents().get( 0 );
        assertEquals( "http://buildagent/xmlrpc", agentConfig.getUrl() );
        assertEquals( "linux", agentConfig.getDescription() );
    }

    public void testDefaultConfiguration()
        throws Exception
    {
        File conf = new File( getBasedir(), confFile );
        if ( conf.exists() )
        {
            conf.delete();
        }
        ContinuumConfiguration configuration = (ContinuumConfiguration) lookup( ContinuumConfiguration.class,
                                                                                "default" );
        assertNotNull( configuration );
        GeneralConfiguration generalConfiguration = new GeneralConfiguration();
        generalConfiguration.setBaseUrl( "http://test/zloug" );
        generalConfiguration.setProxyConfiguration( new ProxyConfiguration() );
        generalConfiguration.getProxyConfiguration().setProxyHost( "localhost" );
        generalConfiguration.getProxyConfiguration().setProxyPort( 8080 );
        File targetDir = new File( getBasedir(), "target" );
        generalConfiguration.setBuildOutputDirectory( targetDir );
        BuildAgentConfiguration buildAgentConfiguration = new BuildAgentConfiguration();
        buildAgentConfiguration.setUrl( "http://buildagent/test" );
        buildAgentConfiguration.setDescription( "windows xp" );
        buildAgentConfiguration.setEnabled( false );

        BuildAgentConfiguration buildAgentConfiguration2 = new BuildAgentConfiguration();
        buildAgentConfiguration2.setUrl( "http://buildagent-node-2/test" );
        buildAgentConfiguration2.setDescription( "linux" );
        buildAgentConfiguration2.setEnabled( true );

        List<BuildAgentConfiguration> buildAgents = new ArrayList<BuildAgentConfiguration>();
        buildAgents.add( buildAgentConfiguration );
        buildAgents.add( buildAgentConfiguration2 );
        BuildAgentGroupConfiguration buildAgentGroupConfiguration = new BuildAgentGroupConfiguration();
        buildAgentGroupConfiguration.setName( "secret-agent" );
        buildAgentGroupConfiguration.setBuildAgents( buildAgents );

        List<BuildAgentGroupConfiguration> buildAgentGroups = new ArrayList<BuildAgentGroupConfiguration>();
        buildAgentGroups.add( buildAgentGroupConfiguration );

        generalConfiguration.setDistributedBuildEnabled( false );
        generalConfiguration.setBuildAgents( buildAgents );
        generalConfiguration.setBuildAgentGroups( buildAgentGroups );
        configuration.setGeneralConfiguration( generalConfiguration );
        configuration.save();

        String contents = FileUtils.fileRead( conf );
        assertTrue( contents.indexOf( "http://test/zloug" ) > 0 );
        assertTrue( contents.indexOf( "localhost" ) > 0 );
        assertTrue( contents.indexOf( "8080" ) > 0 );
        assertTrue( contents.indexOf( "http://buildagent/test" ) > 0 );
        assertTrue( contents.indexOf( "windows xp" ) > 0 );
        assertTrue( contents.indexOf( "http://buildagent-node-2/test" ) > 0 );
        assertTrue( contents.indexOf( "linux" ) > 0 );
        assertTrue( contents.indexOf( "secret-agent" ) > 0 );

        configuration.reload();
        assertEquals( "http://test/zloug", configuration.getGeneralConfiguration().getBaseUrl() );
        assertEquals( "localhost", configuration.getGeneralConfiguration().getProxyConfiguration().getProxyHost() );
        assertEquals( 8080, configuration.getGeneralConfiguration().getProxyConfiguration().getProxyPort() );
        assertEquals( targetDir.getPath(),
                      configuration.getGeneralConfiguration().getBuildOutputDirectory().getPath() );
        assertEquals( "http://buildagent/test", configuration.getGeneralConfiguration().getBuildAgents().get(
            0 ).getUrl() );
        assertFalse( configuration.getGeneralConfiguration().getBuildAgents().get( 0 ).isEnabled() );
        assertEquals( "http://buildagent-node-2/test", configuration.getGeneralConfiguration().getBuildAgents().get(
            1 ).getUrl() );
        assertTrue( configuration.getGeneralConfiguration().getBuildAgents().get( 1 ).isEnabled() );

        assertEquals( "secret-agent", configuration.getGeneralConfiguration().getBuildAgentGroups().get(
            0 ).getName() );
        assertEquals( "http://buildagent/test", configuration.getGeneralConfiguration().getBuildAgentGroups().get(
            0 ).getBuildAgents().get( 0 ).getUrl() );
        assertEquals( "http://buildagent-node-2/test",
                      configuration.getGeneralConfiguration().getBuildAgentGroups().get( 0 ).getBuildAgents().get(
                          1 ).getUrl() );
        assertFalse( configuration.getGeneralConfiguration().isDistributedBuildEnabled() );
        log.info( "generalConfiguration " + configuration.getGeneralConfiguration().toString() );
    }
}
