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
package org.apache.maven.continuum.reports.surefire;

import java.io.File;

import org.apache.maven.continuum.configuration.ConfigurationException;
import org.apache.maven.continuum.configuration.ConfigurationLoadingException;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.configuration.ConfigurationStoringException;
import org.apache.maven.continuum.model.project.Schedule;
import org.apache.maven.continuum.store.ContinuumStoreException;

/**
 * @author <a href="mailto:olamy@apache.org">olamy</a>
 * @since 12 nov. 07
 * @version $Id$
 */
public class MockConfigurationService
    implements ConfigurationService
{

    /** 
     * @see org.apache.maven.continuum.configuration.ConfigurationService#getApplicationHome()
     */
    public File getApplicationHome()
    {
        return null;
    }

    /** 
     * @see org.apache.maven.continuum.configuration.ConfigurationService#getBuildOutput(int, int)
     */
    public String getBuildOutput( int buildId, int projectId )
        throws ConfigurationException
    {
        return null;
    }

    /** 
     * @see org.apache.maven.continuum.configuration.ConfigurationService#getBuildOutputDirectory()
     */
    public File getBuildOutputDirectory()
    {
        return null;
    }

    /** 
     * @see org.apache.maven.continuum.configuration.ConfigurationService#getBuildOutputDirectory(int)
     */
    public File getBuildOutputDirectory( int projectId )
    {
        return null;
    }

    /** 
     * @see org.apache.maven.continuum.configuration.ConfigurationService#getBuildOutputFile(int, int)
     */
    public File getBuildOutputFile( int buildId, int projectId )
        throws ConfigurationException
    {
        return null;
    }

    /** 
     * @see org.apache.maven.continuum.configuration.ConfigurationService#getDefaultSchedule()
     */
    public Schedule getDefaultSchedule()
        throws ContinuumStoreException, ConfigurationLoadingException
    {
        return null;
    }

    /** 
     * @see org.apache.maven.continuum.configuration.ConfigurationService#getDeploymentRepositoryDirectory()
     */
    public File getDeploymentRepositoryDirectory()
    {
        return null;
    }

    /** 
     * @see org.apache.maven.continuum.configuration.ConfigurationService#getFile(java.lang.String)
     */
    public File getFile( String filename )
    {
        return null;
    }

    /** 
     * @see org.apache.maven.continuum.configuration.ConfigurationService#getTestReportsDirectory(int, int)
     */
    public File getTestReportsDirectory( int buildId, int projectId )
        throws ConfigurationException
    {
        return new File( "src" + File.separatorChar + "test" + File.separatorChar
                         + "resources" + File.separatorChar + "continuum-core" );
    }

    /** 
     * @see org.apache.maven.continuum.configuration.ConfigurationService#getUrl()
     */
    public String getUrl()
    {
        return null;
    }

    /** 
     * @see org.apache.maven.continuum.configuration.ConfigurationService#getWorkingDirectory()
     */
    public File getWorkingDirectory()
    {
        return null;
    }

    /** 
     * @see org.apache.maven.continuum.configuration.ConfigurationService#isInitialized()
     */
    public boolean isInitialized()
    {
        return false;
    }

    /** 
     * @see org.apache.maven.continuum.configuration.ConfigurationService#isLoaded()
     */
    public boolean isLoaded()
    {
        return false;
    }

    /** 
     * @see org.apache.maven.continuum.configuration.ConfigurationService#load()
     */
    public void load()
        throws ConfigurationLoadingException
    {
        // nothing
    }

    /** 
     * @see org.apache.maven.continuum.configuration.ConfigurationService#setBuildOutputDirectory(java.io.File)
     */
    public void setBuildOutputDirectory( File buildOutputDirectory )
    {
        // nothing
    }

    /** 
     * @see org.apache.maven.continuum.configuration.ConfigurationService#setDeploymentRepositoryDirectory(java.io.File)
     */
    public void setDeploymentRepositoryDirectory( File deploymentRepositoryDirectory )
    {
        // nothing
    }

    /** 
     * @see org.apache.maven.continuum.configuration.ConfigurationService#setInitialized(boolean)
     */
    public void setInitialized( boolean initialized )
    {
        // nothing
    }

    /** 
     * @see org.apache.maven.continuum.configuration.ConfigurationService#setUrl(java.lang.String)
     */
    public void setUrl( String url )
    {
        // nothing
    }

    /** 
     * @see org.apache.maven.continuum.configuration.ConfigurationService#setWorkingDirectory(java.io.File)
     */
    public void setWorkingDirectory( File workingDirectory )
    {
        // nothing
    }

    /** 
     * @see org.apache.maven.continuum.configuration.ConfigurationService#store()
     */
    public void store()
        throws ConfigurationStoringException
    {
        // nothing
    }

}
