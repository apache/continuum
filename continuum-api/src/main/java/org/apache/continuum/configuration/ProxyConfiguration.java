package org.apache.continuum.configuration;

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


import org.apache.commons.lang.builder.ReflectionToStringBuilder;

/**
 * @author <a href="mailto:olamy@apache.org">olamy</a>
 * @since 24 juin 2008
 */
public class ProxyConfiguration
{
    private String proxyHost;

    private int proxyPort;

    private String proxyUser;

    private String proxyPassword;

    public ProxyConfiguration()
    {
        // nothing here
    }

    public ProxyConfiguration( String proxyHost, String proxyPassword, int proxyPort, String proxyUser )
    {
        super();
        this.proxyHost = proxyHost;
        this.proxyPassword = proxyPassword;
        this.proxyPort = proxyPort;
        this.proxyUser = proxyUser;
    }

    public String getProxyHost()
    {
        return proxyHost;
    }

    public void setProxyHost( String proxyHost )
    {
        this.proxyHost = proxyHost;
    }

    public int getProxyPort()
    {
        return proxyPort;
    }

    public void setProxyPort( int proxyPort )
    {
        this.proxyPort = proxyPort;
    }

    public String getProxyUser()
    {
        return proxyUser;
    }

    public void setProxyUser( String proxyUser )
    {
        this.proxyUser = proxyUser;
    }

    public String getProxyPassword()
    {
        return proxyPassword;
    }

    public void setProxyPassword( String proxyPassword )
    {
        this.proxyPassword = proxyPassword;
    }

    @Override
    public String toString()
    {
        return ReflectionToStringBuilder.toString( this );
    }

}
