package org.apache.continuum.scm;

import java.io.File;
import java.util.Date;

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

/**
 * Configuration for a project's source control.
 * @todo JAXB for persistence
 */
public class ContinuumScmConfiguration
{
    /**
     * The SCM URL, in the format specified by Maven SCM.
     */
    private String url;

    /**
     * The SCM username to use in connecting.
     */
    private String username;

    /**
     * The SCM password to use in connecting.
     *
     * @todo using some service to obtain this rather than configuring it would be preferable
     */
    private String password;

    /**
     * The tag, branch, or equivalent to check out from.
     */
    private String tag;

    /**
     * The location of the working directory.
     *
     * @todo is this a File that is absolute, or is it a relative path under the working directories? How will JAXB
     * manage? Don't want to store absolute path in the config unless that's what the user configured, so the base
     * can be relocated.
     */
    private File workingDirectory;

    /**
     * For SCM clients that support it, use cached credentials on the system to avoid needing to pass them in.
     *
     * @todo using some service to obtain them rather than configuring it would be preferable
     */
    private boolean useCredentialsCache;

    /**
     * What was the last time this checkout was updated.
     *
     * @todo we need to improve on the techniques to achieve this
     */
    private Date latestUpdateDate;

    public String getUsername()
    {
        return username;
    }

    public void setUsername( String username )
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword( String password )
    {
        this.password = password;
    }

    public String getTag()
    {
        return tag;
    }

    public void setTag( String tag )
    {
        this.tag = tag;
    }

    public boolean isUseCredentialsCache()
    {
        return useCredentialsCache;
    }

    public void setUseCredentialsCache( boolean useCredentialsCache )
    {
        this.useCredentialsCache = useCredentialsCache;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl( String url )
    {
        this.url = url;
    }

    public File getWorkingDirectory()
    {
        return workingDirectory;
    }

    public void setWorkingDirectory( File workingDirectory )
    {
        this.workingDirectory = workingDirectory;
    }

    public Date getLatestUpdateDate()
    {
        return latestUpdateDate;
    }

    public void setLatestUpdateDate( Date latestUpdateDate )
    {
        this.latestUpdateDate = latestUpdateDate;
    }
}
