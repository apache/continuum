package org.apache.continuum.scm.manager;

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

import org.apache.maven.scm.manager.NoSuchScmProviderException;
import org.codehaus.plexus.spring.PlexusClassPathXmlApplicationContext;
import org.springframework.context.ApplicationContext;

/**
 * @todo replace with a spring integration test
 */
public class ScmManagerTest
    extends TestCase
{
    private ApplicationContext context;

    private ScmManager manager;

    public void setUp()
    {
        context =
            new PlexusClassPathXmlApplicationContext( new String[] { "classpath*:META-INF/spring-context.xml",
                "classpath*:META-INF/plexus/components.xml",
                "classpath*:" + getClass().getName().replace( '.', '/' ) + ".xml" } );
        manager = (ScmManager) context.getBean( "scmManager" );
    }

    public void testScmProviders()
        throws NoSuchScmProviderException
    {
        manager.getScmLogger().info( "Hello, World" );
        assertNotNull( manager.getProviderByType( "svn" ) );
    }
}
