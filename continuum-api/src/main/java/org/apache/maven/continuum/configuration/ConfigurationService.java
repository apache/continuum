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

import org.apache.continuum.configuration.ContinuumConfigurationException;
import org.apache.maven.continuum.model.project.BuildQueue;
import org.apache.maven.continuum.model.project.Schedule;
import org.apache.maven.continuum.store.ContinuumStoreException;

import java.io.File;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id$
 */
public interface ConfigurationService
{
    String ROLE = ConfigurationService.class.getName();

    public static final String DEFAULT_SCHEDULE_NAME = "DEFAULT_SCHEDULE";
    
    public static final String DEFAULT_BUILD_QUEUE_NAME = "DEFAULT_BUILD_QUEUE";

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    File getApplicationHome();

    boolean isInitialized();

    void setInitialized( boolean initialized );

    String getUrl();

    void setUrl( String url );

    File getBuildOutputDirectory();

    void setBuildOutputDirectory( File buildOutputDirectory );

    File getWorkingDirectory();

    void setWorkingDirectory( File workingDirectory );

    File getDeploymentRepositoryDirectory();

    void setDeploymentRepositoryDirectory( File deploymentRepositoryDirectory );

    String getBuildOutput( int buildId, int projectId )
        throws ConfigurationException;

    File getBuildOutputDirectory( int projectId );

    File getBuildOutputFile( int buildId, int projectId )
        throws ConfigurationException;

    File getTestReportsDirectory( int buildId, int projectId )
        throws ConfigurationException;
    
    File getReleaseOutputDirectory();
    
    void setReleaseOutputDirectory( File releaseOutputDirectory );
    
    File getReleaseOutputDirectory( int projectGroupId );
    
    File getReleaseOutputFile( int projectGroupId, String releaseName )
        throws ConfigurationException;

    String getReleaseOutput( int projectGroupId, String releaseName )
        throws ConfigurationException;
    
    int getNumberOfBuildsInParallel();
    
    void setNumberOfBuildsInParallel( int num );
    
    BuildQueue getDefaultBuildQueue()
        throws ContinuumStoreException;

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    File getFile( String filename );

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    boolean isLoaded();

    void reload()
        throws ConfigurationLoadingException, ContinuumConfigurationException;

    void store()
        throws ConfigurationStoringException, ContinuumConfigurationException;

    Schedule getDefaultSchedule()
        throws ContinuumStoreException, ConfigurationLoadingException, ContinuumConfigurationException;
}
