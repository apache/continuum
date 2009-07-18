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

import org.apache.continuum.web.test.parent.SeleniumSession;

/**
 * Exception to encapsulate exceptions in a Selenium Grid environment
 * 
 * @author Carlos Sanchez <a href="mailto:carlos@apache.org">
 */
public class SeleniumTestException
    extends Exception
{
    private static final long serialVersionUID = -7855624601993372434L;

    private SeleniumSession session;

    public SeleniumTestException( SeleniumSession session, Throwable cause )
    {
        super( "Selenium exception " + session.configurationString(), cause );
        this.setSession( session );
    }

    public void setSession( SeleniumSession session )
    {
        this.session = session;
    }

    public SeleniumSession getSession()
    {
        return session;
    }
}
