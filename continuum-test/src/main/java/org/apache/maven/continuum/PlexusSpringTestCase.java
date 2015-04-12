package org.apache.maven.continuum;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.spring.PlexusClassPathXmlApplicationContext;
import org.codehaus.plexus.spring.PlexusToSpringUtils;
import org.junit.After;
import org.junit.Before;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;
import java.io.InputStream;

/**
 * Adapted from {@link org.codehaus.plexus.spring.PlexusInSpringTestCase} for use with JUnit 4 and generics.
 */
public class PlexusSpringTestCase
{
    protected ConfigurableApplicationContext applicationContext;

    @Before
    public void buildContext()
        throws Exception
    {
        preContextStart();
        applicationContext = new PlexusClassPathXmlApplicationContext( getConfigLocations() );
    }

    /**
     * Used for rare cases when subclasses need to do something before context is built.
     */
    protected void preContextStart()
        throws Exception
    {
    }

    @After
    public void teardownContext()
        throws Exception
    {
        if ( applicationContext != null )
        {
            applicationContext.close();
        }
    }

    protected String[] getConfigLocations()
    {
        return new String[] {
            "classpath*:META-INF/spring-context.xml",
            "classpath*:META-INF/plexus/components.xml",
            "classpath*:" + getPlexusConfigLocation(),
            "classpath*:" + getSpringConfigLocation() };
    }

    protected String getSpringConfigLocation()
    {
        return getClass().getName().replace( '.', '/' ) + "-context.xml";
    }

    protected String getPlexusConfigLocation()
    {
        return getClass().getName().replace( '.', '/' ) + ".xml";
    }

    public static String getBasedir()
    {
        return PlexusToSpringUtils.getBasedir();
    }

    public String getTestConfiguration()
    {
        return getTestConfiguration( getClass() );
    }

    public static String getTestConfiguration( Class clazz )
    {
        String s = clazz.getName().replace( '.', '/' );

        return s.substring( 0, s.indexOf( "$" ) ) + ".xml";
    }

    public <T> T lookup( Class<T> role )
    {
        return (T) lookup( role, null );
    }

    public <T> T lookup( Class<T> role, String roleHint )
    {
        return (T) lookup( role.getName(), roleHint );
    }

    public Object lookup( String role )
    {
        return lookup( role, null );
    }

    public Object lookup( String role, String roleHint )
    {
        return applicationContext.getBean( PlexusToSpringUtils.buildSpringId( role, roleHint ) );
    }

    public static File getTestFile( String path )
    {
        return new File( PlexusToSpringUtils.getBasedir(), path );
    }

    public static File getTestFile( String basedir,
                                    String path )
    {
        File basedirFile = new File( basedir );

        if ( !basedirFile.isAbsolute() )
        {
            basedirFile = getTestFile( basedir );
        }

        return new File( basedirFile, path );
    }

    public static String getTestPath( String path )
    {
        return getTestFile( path ).getAbsolutePath();
    }

    public static String getTestPath( String basedir,
                                      String path )
    {
        return getTestFile( basedir, path ).getAbsolutePath();
    }

    protected ConfigurableApplicationContext getApplicationContext()
    {
        return applicationContext;
    }

    protected void release( Object component )
        throws Exception
    {
        // nothing
    }

    protected PlexusContainer getContainer()
    {
        return (PlexusContainer) applicationContext.getBean( "plexusContainer" );
    }

    protected InputStream getResourceAsStream( String resource )
    {
        return getClass().getResourceAsStream( resource );
    }

}
