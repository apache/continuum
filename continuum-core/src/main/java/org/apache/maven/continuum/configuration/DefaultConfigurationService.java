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

import org.apache.maven.continuum.execution.ContinuumBuildExecutorConstants;
import org.apache.maven.continuum.initialization.DefaultContinuumInitializer;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Schedule;
import org.apache.maven.continuum.model.system.SystemConfiguration;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id$
 * @plexus.component role="org.apache.maven.continuum.configuration.ConfigurationService"
 */
public class DefaultConfigurationService
    extends AbstractLogEnabled
    implements ConfigurationService
{

    // when adding requirement the template in application.xml must be updated CONTINUUM-1207

    /**
     * @plexus.configuration default-value="${plexus.home}"
     */
    private File applicationHome;

    /**
     * @plexus.configuration default-value=""
     */
    private String defaultAntGoals;

    /**
     * @plexus.configuration default-value=""
     */
    private String defaultAntArguments;

    /**
     * @plexus.configuration default-value="clean:clean jar:install"
     */
    private String defaultM1Goals;

    /**
     * @plexus.configuration default-value=""
     */
    private String defaultM1Arguments;

    /**
     * @plexus.configuration default-value="clean install"
     */
    private String defaultM2Goals;

    /**
     * @plexus.configuration default-value="--batch-mode --non-recursive"
     */
    private String defaultM2Arguments;

    /**
     * @plexus.requirement role-hint="jdo"
     */
    private ContinuumStore store;

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private SystemConfiguration systemConf;

    private boolean loaded = false;

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public File getApplicationHome()
    {
        return applicationHome;
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
        if ( systemConf.getBaseUrl() != null )
        {
            return systemConf.getBaseUrl();
        }
        else
        {
            return "";
        }
    }

    public void setUrl( String url )
    {
        systemConf.setBaseUrl( url );
    }

    public File getBuildOutputDirectory()
    {
        return getFile( systemConf.getBuildOutputDirectory() );
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
        systemConf.setBuildOutputDirectory( f.getAbsolutePath() );
    }

    public File getWorkingDirectory()
    {
        return getFile( systemConf.getWorkingDirectory() );
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

        systemConf.setWorkingDirectory( f.getAbsolutePath() );
    }

    public File getDeploymentRepositoryDirectory()
    {
        return getFile( systemConf.getDeploymentRepositoryDirectory() );
    }

    public void setDeploymentRepositoryDirectory( File deploymentRepositoryDirectory )
    {
        systemConf.setDeploymentRepositoryDirectory(
            deploymentRepositoryDirectory != null ? deploymentRepositoryDirectory.getAbsolutePath() : null );
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
            getLogger().warn( "Error reading build output for build '" + buildId + "'.", e );

            return null;
        }
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

    public void load()
        throws ConfigurationLoadingException
    {
        try
        {
            systemConf = store.getSystemConfiguration();

            if ( systemConf == null )
            {
                systemConf = new SystemConfiguration();

                systemConf = store.addSystemConfiguration( systemConf );
            }

            loaded = true;
        }
        catch ( ContinuumStoreException e )
        {
            throw new ConfigurationLoadingException( "Error reading configuration from database.", e );
        }
    }

    public void store()
        throws ConfigurationStoringException
    {
        try
        {
            store.updateSystemConfiguration( systemConf );
        }
        catch ( ContinuumStoreException e )
        {
            throw new ConfigurationStoringException( "Error writting configuration to database.", e );
        }
    }

    public BuildDefinition getDefaultAntBuildDefinition()
        throws ContinuumStoreException
    {
        BuildDefinition bd = new BuildDefinition();

        bd.setDefaultForProject( true );

        bd.setGoals( getDefaultAntGoals() );

        bd.setArguments( getDefaultAntArguments() );

        bd.setBuildFile( "build.xml" );

        bd.setSchedule( getDefaultSchedule() );

        bd.setType( ContinuumBuildExecutorConstants.ANT_BUILD_EXECUTOR );
        
        return bd;
    }

    public String getDefaultAntGoals()
    {
        return defaultAntGoals;
    }

    public String getDefaultAntArguments()
    {
        return defaultAntArguments;
    }

    public String getDefaultMavenOneGoals()
    {
        return defaultM1Goals;
    }

    public String getDefaultMavenOneArguments()
    {
        return defaultM1Arguments;
    }

    public BuildDefinition getDefaultMavenOneBuildDefinition()
        throws ContinuumStoreException
    {
        BuildDefinition bd = new BuildDefinition();

        bd.setDefaultForProject( true );

        bd.setArguments( getDefaultMavenOneArguments() );

        bd.setGoals( getDefaultMavenOneGoals() );

        bd.setBuildFile( "project.xml" );

        bd.setSchedule( getDefaultSchedule() );

        bd.setType( ContinuumBuildExecutorConstants.MAVEN_ONE_BUILD_EXECUTOR );
        
        return bd;
    }

    public String getDefaultMavenTwoArguments()
    {
        return this.defaultM2Arguments;
    }

    public String getDefaultMavenTwoGoals()
    {
        return this.defaultM2Goals;
    }

    public BuildDefinition getDefaultMavenTwoBuildDefinition()
        throws ContinuumStoreException
    {
        BuildDefinition bd = new BuildDefinition();

        bd.setDefaultForProject( true );

        bd.setGoals( getDefaultMavenTwoGoals() );

        bd.setArguments( getDefaultMavenTwoArguments() );

        bd.setBuildFile( "pom.xml" );

        bd.setSchedule( getDefaultSchedule() );

        bd.setType( ContinuumBuildExecutorConstants.MAVEN_TWO_BUILD_EXECUTOR );
        
        return bd;
    }

    private Schedule getDefaultSchedule()
        throws ContinuumStoreException
    {
        return store.getScheduleByName( DefaultContinuumInitializer.DEFAULT_SCHEDULE_NAME );
    }
}
