package org.apache.continuum.web.test.listener;

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

import static org.apache.continuum.web.test.parent.ThreadSafeSeleniumSession.getSession;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.apache.continuum.web.test.parent.SeleniumSession;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

/**
 * Configures Selenium before each test if needed.
 * 
 * @author Carlos Sanchez <a href="mailto:carlos@apache.org">
 */
public class SeleniumListener
    extends TestListenerAdapter
{
    private static final boolean ONE_BROWSER_PER_TEST = false;

    private static final Collection<SeleniumSession> SELENIUM_SESSIONS =
        Collections.synchronizedCollection( new ArrayList<SeleniumSession>() );

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
            System.out.println( "Starting Selenium session: " + "[" + seleniumHost + ", " + seleniumPort + ", "
                + baseUrl + ", " + browser + "]" );
            getSession().start( seleniumHost, Integer.parseInt( seleniumPort ), browser, baseUrl );
            SELENIUM_SESSIONS.add( getSession() );
            System.out.println( "Started Selenium session: " + getSession().configurationString() );
        }
        super.onTestStart( result );
    }

    @Override
    public void onFinish( ITestContext testContext )
    {
        /* ensure all browsers are killed */
        for ( SeleniumSession session : SELENIUM_SESSIONS )
        {
            try
            {
                if ( session.isStarted() )
                {
                    session.stop();
                }
            }
            catch ( RuntimeException e )
            {
                System.err.println( "Error stoping selenium server: " + session.configurationString() );
                e.printStackTrace();
            }
        }
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
        System.out.println( "Test " + tr.getName() + " " + getSession().configurationString() + " -> Failed" );
        super.onTestFailure( tr );
    }

    private String getProperty( String name )
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
