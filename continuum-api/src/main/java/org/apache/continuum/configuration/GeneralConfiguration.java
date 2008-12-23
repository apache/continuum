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

import java.io.File;

/**
 * @author <a href="mailto:olamy@apache.org">olamy</a>
 * @version $Id$
 * @since 17 juin 2008
 */
public class GeneralConfiguration
{
    private File workingDirectory;

    private File buildOutputDirectory;

    private File deploymentRepositoryDirectory;

    private String baseUrl;

    private ProxyConfiguration proxyConfiguration;
    
    private File releaseOutputDirectory;

    public GeneralConfiguration()
    {
        // nothing here
    }

    public File getWorkingDirectory()
    {
        return workingDirectory;
    }

    public void setWorkingDirectory( File workingDirectory )
    {
        this.workingDirectory = workingDirectory;
    }

    public File getBuildOutputDirectory()
    {
        return buildOutputDirectory;
    }

    public void setBuildOutputDirectory( File buildOutputDirectory )
    {
        this.buildOutputDirectory = buildOutputDirectory;
    }

    public File getDeploymentRepositoryDirectory()
    {
        return deploymentRepositoryDirectory;
    }

    public void setDeploymentRepositoryDirectory( File deploymentRepositoryDirectory )
    {
        this.deploymentRepositoryDirectory = deploymentRepositoryDirectory;
    }

    public String getBaseUrl()
    {
        return baseUrl;
    }

    public void setBaseUrl( String baseUrl )
    {
        this.baseUrl = baseUrl;
    }

    public ProxyConfiguration getProxyConfiguration()
    {
        return proxyConfiguration;
    }

    public void setProxyConfiguration( ProxyConfiguration proxyConfiguration )
    {
        this.proxyConfiguration = proxyConfiguration;
    }

    @Override
    public String toString()
    {
        return ReflectionToStringBuilder.toString( this );
    }

    public File getReleaseOutputDirectory()
    {
        return releaseOutputDirectory;
    }
    
    public void setReleaseOutputDirectory( File releaseOutputDirectory )
    {
        this.releaseOutputDirectory = releaseOutputDirectory;
    }

}
