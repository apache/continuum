package org.apache.maven.continuum.configuration;

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

import java.io.File;

import org.apache.continuum.configuration.BuildAgentConfiguration;
import org.apache.continuum.configuration.BuildAgentGroupConfiguration;
import org.codehaus.plexus.spring.PlexusInSpringTestCase;
import org.codehaus.plexus.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id$
 */
public class ConfigurationServiceTest
    extends PlexusInSpringTestCase
{
    private static final Logger log = LoggerFactory.getLogger( ConfigurationServiceTest.class );

    private static final String confFile = "target/test-classes/conf/continuum.xml";

    @Override
    protected void setUp()
        throws Exception
    {
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
        ConfigurationService service = (ConfigurationService) lookup( "configurationService" );
        assertNotNull( service );

        assertNotNull( service.getUrl() );
        assertEquals( "http://test", service.getUrl() );
        log.info( "myBuildOutputDir " + new File( getBasedir(), "target/myBuildOutputDir" ).getAbsolutePath() );

        log.info( "getBuildOutputDirectory " + service.getBuildOutputDirectory().getAbsolutePath() );
        assertEquals( new File( getBasedir(), "target/myBuildOutputDir" ).getAbsolutePath(),
                      service.getBuildOutputDirectory().getAbsolutePath() );
    }

    public void testConfigurationService()
        throws Exception
    {
        File conf = new File( getBasedir(), confFile );
        if ( conf.exists() )
        {
            conf.delete();
        }

        ConfigurationService service = (ConfigurationService) lookup( "configurationService" );

        assertNotNull( service );

//        service.load();

//        assertEquals( "http://test", service.getUrl() );

//        assertEquals( "build-output-directory", service.getBuildOutputDirectory().getName() );

//        assertEquals( "working-directory", service.getWorkingDirectory().getName() );

        assertEquals( "check # build agents", 1, service.getBuildAgents().size() );

        service.setUrl( "http://test/zloug" );
        service.setBuildOutputDirectory( new File( "testBuildOutputDir" ) );

        BuildAgentConfiguration buildAgent = new BuildAgentConfiguration( "http://test/xmlrpc", "windows", false );
        service.addBuildAgent( buildAgent );

        service.store();

        String contents = FileUtils.fileRead( conf );
        //assertTrue( contents.indexOf( "http://test/zloug" ) > 0 );

        service.reload();

        assertEquals( "http://test/zloug", service.getUrl() );
        assertEquals( "check # build agents", 2, service.getBuildAgents().size() );
        assertEquals( "http://test/xmlrpc", service.getBuildAgents().get( 1 ).getUrl() );
        assertEquals( "windows", service.getBuildAgents().get( 1 ).getDescription() );
        assertFalse( service.getBuildAgents().get( 1 ).isEnabled() );

        assertEquals( "http://test/xmlrpc", buildAgent.getUrl() );
        service.removeBuildAgent( buildAgent );
        service.store();
        service.reload();

        assertEquals( "check # build agents", 1, service.getBuildAgents().size() );
        assertEquals( "http://buildagent/xmlrpc", service.getBuildAgents().get( 0 ).getUrl() );
        assertEquals( "linux", service.getBuildAgents().get( 0 ).getDescription() );
        assertTrue( service.getBuildAgents().get( 0 ).isEnabled() );

        BuildAgentGroupConfiguration buildAgentGroup = new BuildAgentGroupConfiguration();
        buildAgentGroup.setName( "group-1" );
        buildAgentGroup.addBuildAgent( buildAgent );
        service.addBuildAgentGroup( buildAgentGroup );

        service.store();
        service.reload();
        assertEquals( "check # build agent groups", 1, service.getBuildAgentGroups().size() );
        assertEquals( "group-1", service.getBuildAgentGroups().get( 0 ).getName() );
        assertEquals( "windows", service.getBuildAgentGroups().get( 0 ).getBuildAgents().get( 0 ).getDescription() );

        BuildAgentConfiguration buildAgent2 = new BuildAgentConfiguration( "http://machine-1/xmlrpc", "node-1", true );
        //buildAgentGroup.addBuildAgent( buildAgent2 );
        service.addBuildAgent( buildAgentGroup, buildAgent2 );

        service.store();
        service.reload();

        assertEquals( "check # build agent groups", 1, service.getBuildAgentGroups().size() );
        assertEquals( "check # build agent groups", 2, service.getBuildAgentGroups().get( 0 ).getBuildAgents().size() );
        assertEquals( "group-1", service.getBuildAgentGroups().get( 0 ).getName() );
        assertEquals( "windows", service.getBuildAgentGroups().get( 0 ).getBuildAgents().get( 0 ).getDescription() );
        assertEquals( "http://machine-1/xmlrpc",
                      service.getBuildAgentGroups().get( 0 ).getBuildAgents().get( 1 ).getUrl() );
        assertEquals( "node-1", service.getBuildAgentGroups().get( 0 ).getBuildAgents().get( 1 ).getDescription() );
        assertEquals( true, service.getBuildAgentGroups().get( 0 ).getBuildAgents().get( 1 ).isEnabled() );

        service.removeBuildAgent( buildAgentGroup, buildAgent2 );
        service.store();
        service.reload();

        assertEquals( "check # build agent groups", 1, service.getBuildAgentGroups().size() );
        assertEquals( "group-1", service.getBuildAgentGroups().get( 0 ).getName() );
        assertEquals( "windows", service.getBuildAgentGroups().get( 0 ).getBuildAgents().get( 0 ).getDescription() );
        assertNull( service.getSharedSecretPassword() );

        service.setSharedSecretPassword( "password" );
        service.store();
        service.reload();

        assertNotNull( service.getSharedSecretPassword() );
    }
}
