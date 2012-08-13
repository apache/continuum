package org.apache.maven.continuum.management;

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

import java.util.Properties;

/**
 * Bean for storing database parameters.
 *
 * @version $Id$
 */
public class DatabaseParams
{
    private final String driverClass;

    private String url;

    private final String groupId;

    private final String artifactId;

    private String version;

    private String username;

    private String password;

    private final Properties properties = new Properties();

    DatabaseParams( String driverClass, String groupId, String artifactId, String version, String username,
                    String password )
    {
        this.driverClass = driverClass;

        this.groupId = groupId;

        this.artifactId = artifactId;

        this.version = version;

        this.username = username;

        this.password = password;
    }

    DatabaseParams( DatabaseParams params )
    {
        this.driverClass = params.driverClass;

        this.groupId = params.groupId;

        this.artifactId = params.artifactId;

        this.version = params.version;

        this.username = params.username;

        this.password = params.password;

        this.url = params.url;
    }

    public String getUrl()
    {
        return url;
    }

    public String getGroupId()
    {
        return groupId;
    }

    public String getArtifactId()
    {
        return artifactId;
    }

    public String getVersion()
    {
        return version;
    }

    public String getUsername()
    {
        return username;
    }

    public String getPassword()
    {
        return password;
    }

    public String getDriverClass()
    {
        return driverClass;
    }

    public void setUrl( String url )
    {
        this.url = url;
    }

    public Properties getProperties()
    {
        return properties;
    }
}
