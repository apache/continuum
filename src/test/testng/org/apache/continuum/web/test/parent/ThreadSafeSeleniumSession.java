package org.apache.continuum.web.test.parent;

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

import org.apache.continuum.web.test.SetupContinuum;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;

/**
 * Storage for selenium related objects needed for each thread when running in parallel.
 * 
 * @author Carlos Sanchez <a href="mailto:carlos@apache.org">
 */
public class ThreadSafeSeleniumSession
{
    private static ThreadLocal<ThreadSafeSeleniumSession> instance = new ThreadLocal<ThreadSafeSeleniumSession>();

    private Selenium selenium;

    private Exception error;

    private String host, browser, baseUrl;

    private int port;

    private static ThreadSafeSeleniumSession getInstance()
    {
        ThreadSafeSeleniumSession session = instance.get();
        if ( session == null )
        {
            session = new ThreadSafeSeleniumSession();
            instance.set( session );
        }
        return session;
    }

    public static void start( String seleniumHost, int seleniumPort, String browser, String baseUrl )
    {
        try
        {
            DefaultSelenium s = new DefaultSelenium( seleniumHost, seleniumPort, browser, baseUrl );
            s.start();
            getInstance().selenium = s;
            getInstance().host = seleniumHost;
            getInstance().port = seleniumPort;
            getInstance().browser = browser;
            getInstance().baseUrl = baseUrl;
            afterStart();
        }
        catch ( RuntimeException e )
        {
            getInstance().error = e;
            throw e;
        }
    }

    private static void afterStart()
    {
        AbstractSeleniumTest.baseUrl = getBaseUrl();
        SetupContinuum setup = new SetupContinuum();
        try
        {
            setup.initializeContinuumIfNeeded();
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
        }
    }

    public static void stop()
    {
        if ( getSelenium() != null )
        {
            getSelenium().stop();
        }
        getInstance().clear();
    }

    private void clear()
    {
        selenium = null;
        error = null;
        host = null;
        port = 0;
        browser = null;
        baseUrl = null;
    }

    public static boolean isStarted()
    {
        return getInstance().selenium != null;
    }

    public static boolean isInError()
    {
        return getInstance().error != null;
    }

    public static Selenium getSelenium()
    {
        Selenium s = getInstance().selenium;
        if ( s != null )
        {
            return s;
        }
        else
        {
            throw new RuntimeException( "Selenium session has not been started." );
        }
    }

    public static String getHost()
    {
        return getInstance().host;
    }

    public static String getBrowser()
    {
        return getInstance().browser;
    }

    public static String getBaseUrl()
    {
        return getInstance().baseUrl;
    }

    public static int getPort()
    {
        return getInstance().port;
    }

    public static String configurationString()
    {
        return "[" + getHost() + ", " + getPort() + ", " + getBaseUrl() + ", " + getBrowser() + "]";
    }

}
