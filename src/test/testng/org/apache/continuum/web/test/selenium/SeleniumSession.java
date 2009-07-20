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

import org.apache.continuum.web.test.SetupContinuum;
import org.apache.continuum.web.test.parent.AbstractSeleniumTest;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;

/**
 * Selenium configuration.
 * 
 * @author Carlos Sanchez <a href="mailto:carlos@apache.org">
 */
public class SeleniumSession
{
    private Selenium selenium;

    private Exception error;

    private String host, browser, baseUrl;

    private int port;

    public void start( String seleniumHost, int seleniumPort, String browser, String baseUrl )
    {
        try
        {
            DefaultSelenium s = new DefaultSelenium( seleniumHost, seleniumPort, browser, baseUrl );
            s.start();
            this.selenium = s;
            this.host = seleniumHost;
            this.port = seleniumPort;
            this.browser = browser;
            this.baseUrl = baseUrl;
            afterStart();
        }
        catch ( RuntimeException e )
        {
            error = e;
            throw e;
        }
    }

    private void afterStart()
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

    public void stop()
    {
        if ( getSelenium() != null )
        {
            getSelenium().stop();
        }
        clear();
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

    public boolean isStarted()
    {
        return selenium != null;
    }

    public boolean isInError()
    {
        return error != null;
    }

    public Selenium getSelenium()
    {
        Selenium s = selenium;
        if ( s != null )
        {
            return s;
        }
        else
        {
            throw new RuntimeException( "Selenium session has not been started." );
        }
    }

    public String getHost()
    {
        return host;
    }

    public String getBrowser()
    {
        return browser;
    }

    public String getBaseUrl()
    {
        return baseUrl;
    }

    public int getPort()
    {
        return port;
    }

    public String configurationString()
    {
        return "[" + getHost() + ", " + getPort() + ", " + getBaseUrl() + ", " + getBrowser() + "]";
    }

}
