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
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.provider.cvslib.cvsexe.CvsExeScmProvider;
import org.apache.maven.scm.provider.cvslib.cvsjava.CvsJavaScmProvider;
import org.codehaus.plexus.spring.PlexusClassPathXmlApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.Properties;

/**
 * @version $Id$
 * @todo replace with a spring integration test
 */
public class ScmManagerTest
    extends TestCase
{
    private static final Logger log = LoggerFactory.getLogger( ScmManagerTest.class );

    private ScmManager manager;

    public void setUp()
    {
        ApplicationContext context = new PlexusClassPathXmlApplicationContext(
            new String[]{"classpath*:META-INF/spring-context.xml", "classpath*:META-INF/plexus/components.xml",
                "classpath*:" + getClass().getName().replace( '.', '/' ) + ".xml"} );
        manager = (ScmManager) context.getBean( "scmManager" );
    }

    public void testScmProviders()
        throws NoSuchScmProviderException
    {
        Properties backupSysProps = System.getProperties();

        try
        {
            manager.getScmLogger().info( "Hello, World" );
            assertNotNull( manager.getProviderByType( "svn" ) );

            ScmProvider cvsProvider = manager.getProviderByType( "cvs" );
            assertNotNull( cvsProvider );

            log.info( "cvs provider class " + cvsProvider.getClass().getName() );

            assertEquals( CvsJavaScmProvider.class, cvsProvider.getClass() );

            System.setProperty( "maven.scm.provider.cvs.implementation", "cvs_native" );

            cvsProvider = manager.getProviderByType( "cvs" );
            assertNotNull( cvsProvider );

            log.info( "cvs provider class " + cvsProvider.getClass().getName() );
            assertEquals( CvsExeScmProvider.class, cvsProvider.getClass() );
            System.setProperty( "maven.scm.provider.cvs.implementation", "cvs" );

            cvsProvider = manager.getProviderByType( "cvs" );
            assertNotNull( cvsProvider );

            log.info( "cvs provider class " + cvsProvider.getClass().getName() );

            assertEquals( CvsJavaScmProvider.class, cvsProvider.getClass() );
        }
        finally
        {
            System.setProperties( backupSysProps );
            System.setProperty( "maven.scm.provider.cvs.implementation", "cvs" );
        }

    }
}
