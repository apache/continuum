package org.apache.continuum.buildagent.configuration;

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

import java.io.File;
import java.util.List;

import org.apache.continuum.buildagent.model.Installation;
import org.apache.continuum.buildagent.model.LocalRepository;

public class GeneralBuildAgentConfiguration
{
    private File workingDirectory;

    private File buildOutputDirectory;

    private String continuumServerUrl;

    private List<Installation> installations;
    
    private List<LocalRepository> localRepositories;

    private String sharedSecretPassword;

    private String buildAgentUrl;

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

    public String getContinuumServerUrl()
    {
        return continuumServerUrl;
    }

    public void setContinuumServerUrl( String continuumServerUrl )
    {
        this.continuumServerUrl = continuumServerUrl;
    }

    public List<Installation> getInstallations()
    {
        return installations;
    }

    public void setInstallations( List<Installation> installations )
    {
        this.installations = installations;
    }
    
    public List<LocalRepository> getLocalRepositories()
    {
        return localRepositories;
    }

    public void setLocalRepositories( List<LocalRepository> localRepositories )
    {
        this.localRepositories = localRepositories;
    }

    public void setSharedSecretPassword( String sharedSecretPassword )
    {
        this.sharedSecretPassword = sharedSecretPassword;
    }

    public String getSharedSecretPassword()
    {
        return sharedSecretPassword;
    }

    public void setBuildAgentUrl( String buildAgentUrl )
    {
        this.buildAgentUrl = buildAgentUrl;
    }

    public String getBuildAgentUrl()
    {
        return buildAgentUrl;
    }
}
