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

import org.apache.continuum.buildagent.model.Installation;
import org.apache.continuum.buildagent.model.LocalRepository;
import org.codehaus.plexus.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.annotation.Resource;

public class DefaultBuildAgentConfigurationService
    implements BuildAgentConfigurationService
{
    private static final Logger log = LoggerFactory.getLogger( DefaultBuildAgentConfigurationService.class );

    @Resource
    private BuildAgentConfiguration buildAgentConfiguration;

    private GeneralBuildAgentConfiguration generalBuildAgentConfiguration;

    public void initialize()
        throws BuildAgentConfigurationException
    {
        loadData();
    }

    public BuildAgentConfiguration getBuildAgentConfiguration()
    {
        return buildAgentConfiguration;
    }

    public void setBuildAgentConfiguration( BuildAgentConfiguration buildAgentConfiguration )
    {
        this.buildAgentConfiguration = buildAgentConfiguration;
    }

    public File getBuildOutputDirectory()
    {
        return generalBuildAgentConfiguration.getBuildOutputDirectory();
    }

    public File getBuildOutputDirectory( int projectId )
    {
        File dir = new File( getBuildOutputDirectory(), Integer.toString( projectId ) );

        try
        {
            dir = dir.getCanonicalFile();
        }
        catch ( IOException e )
        {
        }

        return dir;
    }

    public File getWorkingDirectory()
    {
        return generalBuildAgentConfiguration.getWorkingDirectory();
    }

    public File getWorkingDirectory( int projectId )
    {
        File dir = new File( generalBuildAgentConfiguration.getWorkingDirectory(), Integer.toString( projectId ) );

        try
        {
            dir = dir.getCanonicalFile();
        }
        catch ( IOException e )
        {
        }

        return dir;
    }

    public String getBuildOutput( int projectId )
        throws BuildAgentConfigurationException
    {
        File file = getBuildOutputFile( projectId );

        try
        {
            if ( file.exists() )
            {
                return FileUtils.fileRead( file.getAbsolutePath() );
            }
            else
            {
                return "There is no output for this build.";
            }
        }
        catch ( IOException e )
        {
            log.warn( "Error reading build output for project '" + projectId + "'.", e );

            return null;
        }
    }

    public File getBuildOutputFile( int projectId )
        throws BuildAgentConfigurationException
    {
        File dir = getBuildOutputDirectory( projectId );

        if ( !dir.exists() && !dir.mkdirs() )
        {
            throw new BuildAgentConfigurationException(
                "Could not make the build output directory: " + "'" + dir.getAbsolutePath() + "'." );
        }

        return new File( dir, "build.log.txt" );
    }

    public String getContinuumServerUrl()
    {
        return generalBuildAgentConfiguration.getContinuumServerUrl();
    }

    public List<Installation> getAvailableInstallations()
    {
        return generalBuildAgentConfiguration.getInstallations();
    }

    public List<LocalRepository> getLocalRepositories()
    {
        return generalBuildAgentConfiguration.getLocalRepositories();
    }

    public LocalRepository getLocalRepositoryByName( String name )
        throws BuildAgentConfigurationException
    {
        for ( LocalRepository repo : generalBuildAgentConfiguration.getLocalRepositories() )
        {
            if ( name.equalsIgnoreCase( repo.getName() ) )
            {
                return repo;
            }
        }
        throw new BuildAgentConfigurationException( String.format( "local repository matching '%s' not found", name ) );
    }

    public String getSharedSecretPassword()
    {
        return generalBuildAgentConfiguration.getSharedSecretPassword();
    }

    public void store()
        throws BuildAgentConfigurationException
    {
        buildAgentConfiguration.setContinuumBuildAgentConfiguration( generalBuildAgentConfiguration );

        buildAgentConfiguration.save();
    }

    private void loadData()
        throws BuildAgentConfigurationException
    {
        generalBuildAgentConfiguration = buildAgentConfiguration.getContinuumBuildAgentConfiguration();
    }
}