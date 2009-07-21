package org.apache.continuum.web.test.selenium;

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

import static org.apache.continuum.web.test.selenium.ThreadSafeSeleniumSession.getSession;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

/**
 * Configures Selenium before the tests if needed.
 * 
 * @author Carlos Sanchez <a href="mailto:carlos@apache.org">
 */
public class SeleniumListener
    extends TestListenerAdapter
{
    private static final boolean ONE_BROWSER_PER_TEST = false;

    private static final Collection<SeleniumSession> SELENIUM_SESSIONS =
        Collections.synchronizedCollection( new ArrayList<SeleniumSession>() );

    private final Logger logger = LoggerFactory.getLogger( this.getClass() );

    @Override
    public void onTestStart( ITestResult result )
    {
        if ( !getSession().isStarted() && !getSession().isInError() )
        {
            /* start selenium */
            String browser = getProperty( "browser" );
            String seleniumHost = getProperty( "seleniumHost" );
            String seleniumPort = getProperty( "seleniumPort" );
            String baseUrl = getProperty( "baseUrl" );
            Assert.assertNotNull( browser, "browser parameter is not defined" );
            Assert.assertNotNull( seleniumHost, "seleniumHost parameter is not defined" );
            Assert.assertNotNull( seleniumPort, "seleniumPort parameter is not defined" );
            Assert.assertNotNull( baseUrl, "baseUrl parameter is not defined" );
            logger.info( "Starting Selenium session: [" + seleniumHost + ", " + seleniumPort + ", " + baseUrl + ", "
                + browser + "]" );
            getSession().start( seleniumHost, Integer.parseInt( seleniumPort ), browser, baseUrl );
            SELENIUM_SESSIONS.add( getSession() );
            logger.info( "Started Selenium session: {}", getSession().configurationString() );
        }
        super.onTestStart( result );
    }

    private void ensureStopAllSessionsOnExit()
    {
        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            public void run()
            {
                stopAllSessions();
            }
        } );
    }

    private void stopAllSessions()
    {
        /* ensure all browsers are stopped */
        synchronized ( SELENIUM_SESSIONS )
        {
            for ( Iterator<SeleniumSession> it = SELENIUM_SESSIONS.iterator(); it.hasNext(); )
            {
                SeleniumSession session = it.next();

                try
                {
                    if ( session.isStarted() )
                    {
                        logger.info( "Stopping selenium session {}", session.configurationString() );
                        session.stop();
                    }
                }
                catch ( RuntimeException e )
                {
                    /* ignore errors if session has been already stopped */
                    if ( ( e.getMessage() != null )
                        && !e.getMessage().startsWith( "ERROR: Selenium Driver error: session already stopped:" ) )
                    {
                        logger.error( "Error stopping selenium server: " + session.configurationString(), e );
                    }
                }
                finally
                {
                    it.remove();
                }
            }
        }
    }

    @Override
    public void onStart( ITestContext testContext )
    {
        ensureStopAllSessionsOnExit();
        super.onStart( testContext );
    }

    @Override
    public void onFinish( ITestContext testContext )
    {
        stopAllSessions();
        super.onFinish( testContext );
    }

    @Override
    public void onTestSuccess( ITestResult tr )
    {
        if ( ONE_BROWSER_PER_TEST )
        {
            getSession().stop();
        }
        super.onTestSuccess( tr );
    }

    @Override
    public void onTestFailure( ITestResult tr )
    {
        if ( ONE_BROWSER_PER_TEST )
        {
            getSession().stop();
        }
        logger.error( "Test {} {} -> Failed", tr.getName(), getSession().configurationString() );
        SeleniumTestException e = new SeleniumTestException( getSession(), tr.getThrowable() );
        tr.setThrowable( e );
        super.onTestFailure( tr );
    }

    protected String getProperty( String name )
    {
        for ( ITestContext context : getTestContexts() )
        {
            String p = context.getSuite().getParameter( "browser" );
            if ( p != null )
            {
                return p;
            }
        }
        return System.getProperty( name );
    }
}
