package org.apache.maven.continuum.configuration;

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
import java.util.Map;

import org.apache.continuum.buildqueue.BuildQueueServiceException;
import org.apache.continuum.configuration.BuildAgentConfiguration;
import org.apache.continuum.configuration.BuildAgentGroupConfiguration;
import org.apache.maven.continuum.model.project.BuildQueue;
import org.apache.maven.continuum.model.project.Schedule;
import org.apache.maven.continuum.store.ContinuumStoreException;

/**
 * Mock class for testing WagonContinuumNotifier's call to ConfigurationService.getBuildOutputFile()
 *
 * @author <a href="mailto:nramirez@exist">Napoleon Esmundo C. Ramirez</a>
 */
public class ConfigurationServiceMock
    implements ConfigurationService
{
    private final String basedir;

    public ConfigurationServiceMock()
    {
        basedir = System.getProperty( "basedir" );
    }

    public File getBuildOutputDirectory()
    {
        return new File( basedir, "src/test/resources" + "/" + "build-output-directory" );
    }

    public File getBuildOutputDirectory( int projectId )
    {
        return new File( getBuildOutputDirectory(), Integer.toString( projectId ) );
    }

    public File getBuildOutputFile( int buildId, int projectId )
        throws ConfigurationException
    {
        File dir = getBuildOutputDirectory( projectId );

        if ( !dir.exists() && !dir.mkdirs() )
        {
            throw new ConfigurationException(
                "Could not make the build output directory: " + "'" + dir.getAbsolutePath() + "'." );
        }

        return new File( dir, buildId + ".log.txt" );
    }

    public File getWorkingDirectory()
    {
        return new File( basedir, "src/test/resources" + "/" + "working-directory" );
    }

    public File getTestReportsDirectory( int buildId, int projectId )
        throws ConfigurationException
    {
        File dir = getBuildOutputDirectory( projectId );

        if ( !dir.exists() && !dir.mkdirs() )
        {
            throw new ConfigurationException(
                "Could not make the build output directory: " + "'" + dir.getAbsolutePath() + "'." );
        }
        return new File( dir.getPath() + File.separatorChar + buildId + File.separatorChar + "surefire-reports " );
    }

    public File getApplicationHome()
    {
        return null;
    }

    public boolean isInitialized()
    {
        return false;
    }

    public void setInitialized( boolean initialized )
    {
    }

    public String getUrl()
    {
        return null;
    }

    public void setUrl( String url )
    {
    }

    public void setBuildOutputDirectory( File buildOutputDirectory )
    {
    }

    public void setWorkingDirectory( File workingDirectory )
    {
    }

    public File getDeploymentRepositoryDirectory()
    {
        return null;
    }

    public void setDeploymentRepositoryDirectory( File deploymentRepositoryDirectory )
    {
    }

    public void setJdks( Map jdks )
    {
    }

    public String getCompanyLogo()
    {
        return null;
    }

    public void setCompanyLogo( String companyLogoUrl )
    {
    }

    public String getCompanyName()
    {
        return null;
    }

    public void setCompanyName( String companyName )
    {
    }

    public String getCompanyUrl()
    {
        return null;
    }

    public void setCompanyUrl( String companyUrl )
    {
    }

    public boolean isGuestAccountEnabled()
    {
        return false;
    }

    public void setGuestAccountEnabled( boolean enabled )
    {
    }

    public String getBuildOutput( int buildId, int projectId )
        throws ConfigurationException
    {
        return null;
    }

    public File getFile( String filename )
    {
        return null;
    }

    public String getSharedSecretPassword()
    {
        return null;
    }

    public void setSharedSecretPassword( String sharedSecretPassword )
    {        
    }

    public boolean isLoaded()
    {
        return false;
    }

    public void reload()
        throws ConfigurationLoadingException
    {
    }

    public void store()
        throws ConfigurationStoringException
    {
    }

    public BuildQueue getDefaultBuildQueue()
        throws BuildQueueServiceException
    {
        return null;
    }

    public Schedule getDefaultSchedule()
        throws ContinuumStoreException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public File getChrootJailDirectory()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void setChrootJailDirectory( File chrootJailDirectory )
    {
        // TODO Auto-generated method stub

    }

    public File getReleaseOutputDirectory()
    {
        return new File( basedir, "src/test/resources" + "/" + "release-output-directory" );
    }

    public File getReleaseOutputDirectory( int projectGroupId )
    {
        return new File( getReleaseOutputDirectory(), Integer.toString( projectGroupId ) );
    }

    public File getReleaseOutputFile( int projectGroupId, String releaseName )
        throws ConfigurationException
    {
        File dir = getReleaseOutputDirectory( projectGroupId );

        if ( !dir.exists() && !dir.mkdirs() )
        {
            throw new ConfigurationException(
                "Could not make the release output directory: " + "'" + dir.getAbsolutePath() + "'." );
        }

        return new File( dir, releaseName + ".log.txt" );
    }

    public void setReleaseOutputDirectory( File releaseOutputDirectory )
    {
    }

    public String getReleaseOutput( int projectGroupId, String name )
    {
        return null;
    }

    public int getNumberOfBuildsInParallel()
    {
        return 1;
    }

    public void setNumberOfBuildsInParallel( int num )
    {

    }

    public void addBuildAgent( BuildAgentConfiguration buildAgent )
        throws ConfigurationException
    {
    }

    public List<BuildAgentConfiguration> getBuildAgents()
    {
        return null;
    }

    public boolean isDistributedBuildEnabled()
    {
        return false;
    }

    public void removeBuildAgent( BuildAgentConfiguration buildAgent )
    {
    }

    public void setDistributedBuildEnabled( boolean distributedBuildEnabled )
    {
    }

    public void updateBuildAgent( BuildAgentConfiguration buildAgent )
    {
    }

    public void addBuildAgentGroup( BuildAgentGroupConfiguration buildAgentGroup )
        throws ConfigurationException
    {
    }

    public void removeBuildAgentGroup( BuildAgentGroupConfiguration buildAgentGroup )
        throws ConfigurationException
    {
    }

    public void updateBuildAgentGroup( BuildAgentGroupConfiguration buildAgentGroup )
        throws ConfigurationException
    {
    }

    public List<BuildAgentGroupConfiguration> getBuildAgentGroups()
    {
        return null;
    }

    public void addBuildAgent( BuildAgentGroupConfiguration buildAgentGroup, BuildAgentConfiguration buildAgent )
        throws ConfigurationException
    {
    }

    public void removeBuildAgent( BuildAgentGroupConfiguration buildAgentGroup, BuildAgentConfiguration buildAgent )
        throws ConfigurationException
    {
    }

    public BuildAgentGroupConfiguration getBuildAgentGroup( String name )
    {
        return null;
    }

    public BuildAgentConfiguration getBuildAgent( String url )
    {
        return null;
    }

    public boolean containsBuildAgentUrl( String buildAgentUrl, BuildAgentGroupConfiguration buildAgentGroup )
    {
        return false;
    }
}
