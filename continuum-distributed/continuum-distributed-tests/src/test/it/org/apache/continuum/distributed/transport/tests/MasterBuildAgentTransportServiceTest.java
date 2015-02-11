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

import junit.framework.TestCase;
import org.apache.continuum.distributed.transport.master.MasterBuildAgentTransportClient;
import org.apache.continuum.distributed.transport.master.MasterBuildAgentTransportService;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * MasterBuildAgentTransportServiceTest
 */
public class MasterBuildAgentTransportServiceTest
{
    private MasterBuildAgentTransportService masterProxy;

    private BeanFactory beanFactory = new XmlBeanFactory( new ClassPathResource( "applicationContext.xml" ) );

    @Before
    public void setUp()
        throws Exception
    {
        masterProxy = new MasterBuildAgentTransportClient( new URL( "http://localhost:9191/master-xmlrpc" ), null,
                                                           null );
    }

    @Test
    public void testReturnBuildResult()
    {
        try
        {
            masterProxy.returnBuildResult( new HashMap<String, Object>(), /* ??? */ null );
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }
    }

    /* this method apparently no longer exists in the interface
    @Test
    public void testReturnScmResult()
    {
        try
        {
            masterProxy.returnScmResult( new HashMap<String, Object>() );
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }
    } */

    @Test
    public void testPing()
    {
        try
        {
            masterProxy.ping();
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }
    }
}
