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

import java.io.File;

import org.apache.maven.continuum.configuration.ContinuumConfiguration;
import org.apache.maven.continuum.configuration.GeneralConfiguration;
import org.apache.maven.continuum.configuration.ProxyConfiguration;
import org.codehaus.plexus.spring.PlexusInSpringTestCase;
import org.codehaus.plexus.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:olamy@apache.org">olamy</a>
 * @since 17 juin 2008
 * @version $Id$
 */
public class TestDefaultContinuumConfiguration
    extends PlexusInSpringTestCase
{

    private Logger log = LoggerFactory.getLogger( getClass() );
    
    @Override
    protected void setUp()
        throws Exception
    {
        File originalConf = new File( getBasedir(), "src/test/resources/conf/continuum.xml" );

        File confUsed = new File( getBasedir(), "target/test-classes/conf/continuum.xml" );
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

        
        
        
        ContinuumConfiguration configuration = (ContinuumConfiguration) lookup( ContinuumConfiguration.class, "default" );
        assertNotNull( configuration );
        GeneralConfiguration generalConfiguration = configuration.getGeneralConfiguration();
        assertNotNull( generalConfiguration );
        assertNotNull( generalConfiguration.getBaseUrl() );
        assertEquals( "http://test", generalConfiguration.getBaseUrl() );
    }
    
    public void testDefaultConfiguration()
        throws Exception
    {
        File conf = new File( getBasedir(), "target/test-classes/conf/continuum.xml" );
        if ( conf.exists() )
        {
            conf.delete();
        }
        ContinuumConfiguration configuration = (ContinuumConfiguration) lookup( ContinuumConfiguration.class, "default" );
        assertNotNull( configuration );
        GeneralConfiguration generalConfiguration = new GeneralConfiguration();
        generalConfiguration.setBaseUrl( "http://test/zloug" );
        generalConfiguration.setProxyConfiguration( new ProxyConfiguration() );
        generalConfiguration.getProxyConfiguration().setProxyHost( "localhost" );
        generalConfiguration.getProxyConfiguration().setProxyPort( 8080 );
        configuration.setGeneralConfiguration( generalConfiguration );
        configuration.save();

        configuration.reload();
        assertEquals( "http://test/zloug", configuration.getGeneralConfiguration().getBaseUrl() );
        assertEquals( "localhost", configuration.getGeneralConfiguration().getProxyConfiguration().getProxyHost() );
        assertEquals( 8080, configuration.getGeneralConfiguration().getProxyConfiguration().getProxyPort() );
        log.info( "generalConfiguration " + configuration.getGeneralConfiguration().toString() );
    }
   
}
