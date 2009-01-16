package org.apache.continuum.distributed.transport.tests;

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

import java.net.URL;
import java.util.Collections;

import junit.framework.TestCase;

import org.apache.continuum.distributed.transport.slave.SlaveBuildAgentTransportClient;
import org.apache.continuum.distributed.transport.slave.SlaveBuildAgentTransportService;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

/**
 * SlaveBuildAgentTransportServiceTest
 */
public class SlaveBuildAgentTransportServiceTest
    extends TestCase
{
    private SlaveBuildAgentTransportService slaveProxy;
    
    private BeanFactory beanFactory = new XmlBeanFactory( new ClassPathResource( "applicationContext.xml" ) );
    
    protected void setUp()
        throws Exception
    {
        super.setUp();
        
        slaveProxy = new SlaveBuildAgentTransportClient( new URL( "http://localhost:9191/slave-xmlrpc"), null , null );
    }

    public void testBuildProjects()
    {
        try
        {
            slaveProxy.buildProjects( Collections.EMPTY_LIST );
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }
    }

    public void testGetAvailableInstallations()
    {
        try
        {
            slaveProxy.getAvailableInstallations();
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }
    }

    public void testGetBuildResult()
    {
        try
        {
            slaveProxy.getBuildResult( 0 );
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }
    }

    public void testGetProjectCurrentlyBuilding()
    {
        try
        {
            slaveProxy.getProjectCurrentlyBuilding();
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }
    }

    public void testPing()
    {
        try
        {
            slaveProxy.ping();
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }
    }
}
