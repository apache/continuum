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

import org.apache.continuum.configuration.ContinuumConfiguration;
import org.apache.continuum.configuration.ContinuumConfigurationException;
import org.apache.continuum.configuration.GeneralConfiguration;
import org.apache.continuum.dao.BuildQueueDao;
import org.apache.continuum.dao.ScheduleDao;
import org.apache.continuum.dao.SystemConfigurationDao;
import org.apache.maven.continuum.model.project.BuildQueue;
import org.apache.maven.continuum.model.project.Schedule;
import org.apache.maven.continuum.model.system.SystemConfiguration;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id$
 */
public class DefaultConfigurationService
    implements ConfigurationService
{
    private Logger log = LoggerFactory.getLogger( this.getClass() );

    // when adding a requirement, the template in spring-context.xml must be updated CONTINUUM-1207

    /**
     * @plexus.configuration default-value="${plexus.home}"
     */
    private File applicationHome;

    /**
     * @plexus.requirement
     */
    private ScheduleDao scheduleDao;

    /**
     * @plexus.requirement
     */
    private SystemConfigurationDao systemConfigurationDao;
    
    /**
     * @plexus.requirement
     */
    private BuildQueueDao buildQueueDao;

    /**
     * @plexus.requirement
     */
    private ContinuumConfiguration configuration;

    private GeneralConfiguration generalConfiguration;

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private SystemConfiguration systemConf;

    private boolean loaded = false;

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public void initialize()
        throws ConfigurationLoadingException, ContinuumConfigurationException
    {
        loadData();
    }

    public ScheduleDao getScheduleDao()
    {
        return scheduleDao;
    }

    public void setScheduleDao( ScheduleDao scheduleDao )
    {
        this.scheduleDao = scheduleDao;
    }
    
    public BuildQueueDao getBuildQueueDao()
    {
        return buildQueueDao;
    }
    
    public void setBuildQueueDao( BuildQueueDao buildQueueDao )
    {
        this.buildQueueDao = buildQueueDao;
    }

    public SystemConfigurationDao getSystemConfigurationDao()
    {
        return systemConfigurationDao;
    }

    public void setSystemConfigurationDao( SystemConfigurationDao systemConfigurationDao )
    {
        this.systemConfigurationDao = systemConfigurationDao;
    }

    public ContinuumConfiguration getConfiguration()
    {
        return configuration;
    }

    public void setConfiguration( ContinuumConfiguration configuration )
    {
        this.configuration = configuration;
    }

    public File getApplicationHome()
    {
        return applicationHome;
    }

    public void setApplicationHome( File applicationHome )
    {
        this.applicationHome = applicationHome;
    }

    public void setInitialized( boolean initialized )
    {
        systemConf.setInitialized( initialized );
    }

    public boolean isInitialized()
    {
        return systemConf.isInitialized();
    }

    public String getUrl()
    {
        String baseUrl = generalConfiguration.getBaseUrl();
        if ( StringUtils.isEmpty( baseUrl ) )
        {
            baseUrl = systemConf.getBaseUrl();
            setUrl( baseUrl );
        }
        return baseUrl != null ? baseUrl : "";
    }

    public void setUrl( String url )
    {
        generalConfiguration.setBaseUrl( url );
    }

    /** 
     * @see org.apache.maven.continuum.configuration.ConfigurationService#getBuildOutputDirectory()
     */
    public File getBuildOutputDirectory()
    {
        File buildOutputDirectory = generalConfiguration.getBuildOutputDirectory();
        if ( buildOutputDirectory == null )
        {
            buildOutputDirectory = getFile( systemConf.getBuildOutputDirectory() );
            setBuildOutputDirectory( buildOutputDirectory );
        }
        return buildOutputDirectory;
    }

    public void setBuildOutputDirectory( File buildOutputDirectory )
    {
        File f = buildOutputDirectory;
        try
        {
            f = f.getCanonicalFile();
        }
        catch ( IOException e )
        {
        }
        generalConfiguration.setBuildOutputDirectory( f );
    }

    public File getWorkingDirectory()
    {
        File workingDirectory = generalConfiguration.getWorkingDirectory();
        if ( workingDirectory == null )
        {
            workingDirectory = getFile( systemConf.getWorkingDirectory() );
            setWorkingDirectory( workingDirectory );
        }
        return workingDirectory;
    }

    public void setWorkingDirectory( File workingDirectory )
    {
        File f = workingDirectory;
        try
        {
            f = f.getCanonicalFile();
        }
        catch ( IOException e )
        {
        }

        generalConfiguration.setWorkingDirectory( f );
    }

    public File getDeploymentRepositoryDirectory()
    {
        File deploymentDirectory = generalConfiguration.getDeploymentRepositoryDirectory();
        if ( deploymentDirectory == null )
        {
            deploymentDirectory = getFile( systemConf.getDeploymentRepositoryDirectory() );
            setDeploymentRepositoryDirectory( deploymentDirectory );
        }
        return deploymentDirectory;
    }

    public void setDeploymentRepositoryDirectory( File deploymentRepositoryDirectory )
    {
        generalConfiguration.setDeploymentRepositoryDirectory( deploymentRepositoryDirectory );
    }

    public String getBuildOutput( int buildId, int projectId )
        throws ConfigurationException
    {
        File file = getBuildOutputFile( buildId, projectId );

        try
        {
            if ( file.exists() )
            {
                return FileUtils.fileRead( file.getAbsolutePath() );
            }
            else
            {
                return "There are no output for this build.";
            }
        }
        catch ( IOException e )
        {
            log.warn( "Error reading build output for build '" + buildId + "'.", e );

            return null;
        }
    }

    public File getReleaseOutputDirectory()
    {
        File releaseOutputDirectory = generalConfiguration.getReleaseOutputDirectory();

        if ( releaseOutputDirectory == null )
        {
            releaseOutputDirectory = getFile( systemConf.getReleaseOutputDirectory() );
            setReleaseOutputDirectory( releaseOutputDirectory );
        }
        return releaseOutputDirectory;
    }

    public void setReleaseOutputDirectory( File releaseOutputDirectory )
    {
        if ( releaseOutputDirectory == null )
        {
            generalConfiguration.setReleaseOutputDirectory( releaseOutputDirectory );
            return;
        }

        File f = releaseOutputDirectory;
        try
        {
            f = f.getCanonicalFile();
        }
        catch ( IOException e )
        {
        }
        generalConfiguration.setReleaseOutputDirectory( f );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------


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

    public File getTestReportsDirectory( int buildId, int projectId )
        throws ConfigurationException
    {
        File ouputDirectory = getBuildOutputDirectory( projectId );

        return new File(
            ouputDirectory.getPath() + File.separatorChar + buildId + File.separatorChar + "surefire-reports" );

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

    public File getReleaseOutputDirectory( int projectGroupId )
    {
        if ( getReleaseOutputDirectory() == null )
        {
            return null;
        }
        
        File dir = new File( getReleaseOutputDirectory(), Integer.toString( projectGroupId ) );
        
        try
        {
            dir = dir.getCanonicalFile();
        }
        catch ( IOException e )
        {
        }
        
        return dir;
    }

    public File getReleaseOutputFile( int projectGroupId, String name )
        throws ConfigurationException
    {
        File dir = getReleaseOutputDirectory( projectGroupId );

        if ( dir == null )
        {
            return null;
        }
        
        if ( !dir.exists() && !dir.mkdirs() )
        {
            throw new ConfigurationException(
                "Could not make the release output directory: " + "'" + dir.getAbsolutePath() + "'." );
        }

        return new File( dir, name + ".log.txt" );
    }

    public String getReleaseOutput( int projectGroupId, String name )
        throws ConfigurationException
    {
        File file = getReleaseOutputFile( projectGroupId, name );

        try
        {
            if ( file.exists() )
            {
                return FileUtils.fileRead( file.getAbsolutePath() );
            }
            else
            {
                return "There are no output for this release.";
            }
        }
        catch ( IOException e )
        {
            log.warn( "Error reading release output for release '" + name + "'.", e );
            return null;
        }
    }
    
    public int getNumberOfBuildsInParallel()
    {
        return generalConfiguration.getNumberOfBuildsInParallel();
    }
    
    public void setNumberOfBuildsInParallel( int num )
    {
        generalConfiguration.setNumberOfBuildsInParallel( num );
    }
    
    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public File getFile( String filename )
    {
        if ( filename == null )
        {
            return null;
        }

        File f = null;

        if ( filename != null && filename.length() != 0 )
        {
            f = new File( filename );

            if ( !f.isAbsolute() )
            {
                f = new File( applicationHome, filename );
            }
        }

        try
        {
            return f.getCanonicalFile();
        }
        catch ( IOException e )
        {
            return f;
        }
    }

    // ----------------------------------------------------------------------
    // Load and Store
    // ----------------------------------------------------------------------

    public boolean isLoaded()
    {
        return loaded;
    }


    private void loadData()
        throws ConfigurationLoadingException, ContinuumConfigurationException
    {
        generalConfiguration = configuration.getGeneralConfiguration();

        try
        {
            systemConf = getSystemConfigurationDao().getSystemConfiguration();

            if ( systemConf == null )
            {
                systemConf = new SystemConfiguration();

                systemConf = getSystemConfigurationDao().addSystemConfiguration( systemConf );
            }

            loaded = true;
        }
        catch ( ContinuumStoreException e )
        {
            throw new ConfigurationLoadingException( "Error reading configuration from database.", e );
        }
    }

    public void reload()
        throws ConfigurationLoadingException, ContinuumConfigurationException
    {
        configuration.reload();
        loadData();
    }

    public void store()
        throws ConfigurationStoringException, ContinuumConfigurationException
    {
        configuration.setGeneralConfiguration( generalConfiguration );
        configuration.save();
        try
        {
            getSystemConfigurationDao().updateSystemConfiguration( systemConf );
        }
        catch ( ContinuumStoreException e )
        {
            throw new ConfigurationStoringException( "Error writting configuration to database.", e );
        }
    }

    public Schedule getDefaultSchedule()
        throws ContinuumStoreException, ConfigurationLoadingException, ContinuumConfigurationException
    {
        // Schedule
        Schedule defaultSchedule = scheduleDao.getScheduleByName( DEFAULT_SCHEDULE_NAME );

        if ( defaultSchedule == null )
        {
            defaultSchedule = createDefaultSchedule();

            defaultSchedule = scheduleDao.addSchedule( defaultSchedule );
        }

        return defaultSchedule;
    }
    
    public BuildQueue getDefaultBuildQueue()
        throws ContinuumStoreException
    {     
        BuildQueue defaultBuildQueue = buildQueueDao.getBuildQueueByName( DEFAULT_BUILD_QUEUE_NAME );
    
        if ( defaultBuildQueue == null )
        {
            defaultBuildQueue = createDefaultBuildQueue();
    
            defaultBuildQueue = buildQueueDao.addBuildQueue( defaultBuildQueue );
        }
    
        return defaultBuildQueue;
    }
    
    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private Schedule createDefaultSchedule()
        throws ConfigurationLoadingException, ContinuumConfigurationException, ContinuumStoreException
    {

        log.info( "create Default Schedule" );

        Schedule schedule = new Schedule();

        schedule.setName( DEFAULT_SCHEDULE_NAME );

        //It shouldn't be possible
        if ( systemConf == null )
        {
            this.reload();
        }

        schedule.setDescription( systemConf.getDefaultScheduleDescription() );

        schedule.setCronExpression( systemConf.getDefaultScheduleCronExpression() );

        schedule.setActive( true );
        
        BuildQueue buildQueue = getDefaultBuildQueue();
        
        schedule.addBuildQueue( buildQueue );

        return schedule;
    }
    
    private BuildQueue createDefaultBuildQueue()
    {
        log.info( "create Default Build Queue" );
        
        BuildQueue buildQueue = new BuildQueue();
        
        buildQueue.setName( DEFAULT_BUILD_QUEUE_NAME );        
        
        return buildQueue;
    }
}
