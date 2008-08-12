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

import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.DefaultConfigurationBuilder;
import org.apache.commons.configuration.FileConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;

/**
 * @author <a href="mailto:olamy@apache.org">olamy</a>
 * @version $Id$
 * @since 17 juin 2008
 */
public class DefaultContinuumConfiguration
    implements ContinuumConfiguration
{
    private Logger log = LoggerFactory.getLogger( getClass() );

    private ClassPathResource classPathResource;

    private Configuration configuration;

    private GeneralConfiguration generalConfiguration;

    public static final String BASE_URL_KEY = "continuum.baseUrl";

    public static final String BUILDOUTPUT_DIR_KEY = "continuum.buildOutputDirectory";

    public static final String DEPLOYMENT_REPOSITORY_DIR_KEY = "continuum.deploymentRepositoryDirectory";

    public static final String WORKING_DIR_KEY = "continuum.workingDirectory";

    public static final String PROXY_HOST_KEY = "continuum.proxyHost";

    public static final String PROXY_PORT_KEY = "continuum.proxyPort";

    public static final String PROXY_USER_KEY = "continuum.proxyUser";

    public static final String PROXY_PASSWORD_KEY = "continuum.proxyPassword";

    //----------------------------------------------------
    //  Initialize method configured in the Spring xml 
    //   configuration file
    //----------------------------------------------------
    protected void initialize()
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "classPathResource null " + ( classPathResource == null ) );
        }

        try
        {
            DefaultConfigurationBuilder defaultConfigurationBuilder =
                new DefaultConfigurationBuilder( classPathResource.getURL() );
            defaultConfigurationBuilder.load( classPathResource.getInputStream() );

            CombinedConfiguration combinedConfiguration = defaultConfigurationBuilder.getConfiguration( false );
            configuration = combinedConfiguration.getConfiguration( "org.apache.continuum" );

            this.generalConfiguration = new GeneralConfiguration();
            this.generalConfiguration.setBaseUrl( getConfigurationString( BASE_URL_KEY ) );
            log.info( "BaseUrl=" + this.generalConfiguration.getBaseUrl() );
            // TODO check if files exists ?
            String buildOutputDirectory = getConfigurationString( BUILDOUTPUT_DIR_KEY );
            if ( buildOutputDirectory != null )
            {
                this.generalConfiguration.setBuildOutputDirectory( new File( buildOutputDirectory ) );
            }
            String deploymentRepositoryDirectory = getConfigurationString( DEPLOYMENT_REPOSITORY_DIR_KEY );
            if ( deploymentRepositoryDirectory != null )
            {
                this.generalConfiguration.setDeploymentRepositoryDirectory( new File( deploymentRepositoryDirectory ) );
            }
            String workingDirectory = getConfigurationString( WORKING_DIR_KEY );
            if ( workingDirectory != null )
            {
                this.generalConfiguration.setWorkingDirectory( new File( workingDirectory ) );
            }

            this.generalConfiguration.setProxyConfiguration( new ProxyConfiguration() );
            this.generalConfiguration.getProxyConfiguration().setProxyHost( getConfigurationString( PROXY_HOST_KEY ) );
            this.generalConfiguration.getProxyConfiguration().setProxyPort(
                getConfigurationValue( PROXY_PORT_KEY, 0 ) );
            this.generalConfiguration.getProxyConfiguration().setProxyUser( getConfigurationString( PROXY_USER_KEY ) );
            this.generalConfiguration.getProxyConfiguration().setProxyPassword( configuration
                .getString( PROXY_PASSWORD_KEY ) );
        }
        catch ( org.apache.commons.configuration.ConfigurationException e )
        {
            log.error( e.getMessage(), e );
            throw new RuntimeException( e.getMessage(), e );
        }
        catch ( IOException e )
        {
            log.error( e.getMessage(), e );
            throw new RuntimeException( e.getMessage(), e );
        }
    }

    private String getConfigurationString( String key )
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "Configuration=" + configuration );
        }
        return configuration.getString( key );
    }

    private int getConfigurationValue( String key, int defaultValue )
    {
        return configuration.getInt( key, defaultValue );
    }

    public void reload()
        throws ContinuumConfigurationException
    {
        this.initialize();
    }

    public void save()
        throws ContinuumConfigurationException
    {
        FileConfiguration fileConfiguration = (FileConfiguration) configuration;
        try
        {
            fileConfiguration.save();
        }
        catch ( org.apache.commons.configuration.ConfigurationException e )
        {
            throw new ContinuumConfigurationException( e.getMessage(), e );
        }
    }

    /**
     * @see org.apache.continuum.configuration.ContinuumConfiguration#getGeneralConfiguration()
     */
    public GeneralConfiguration getGeneralConfiguration()
        throws ContinuumConfigurationException
    {
        return this.generalConfiguration;
    }

    public void setGeneralConfiguration( GeneralConfiguration generalConfiguration )
        throws ContinuumConfigurationException
    {
        this.generalConfiguration = generalConfiguration;
        this.configuration.setProperty( BASE_URL_KEY, generalConfiguration.getBaseUrl() );
        if ( generalConfiguration.getBuildOutputDirectory() != null )
        {
            this.configuration.setProperty( BUILDOUTPUT_DIR_KEY, generalConfiguration.getBuildOutputDirectory()
                .getPath() );
        }
        if ( generalConfiguration.getDeploymentRepositoryDirectory() != null )
        {
            this.configuration.setProperty( DEPLOYMENT_REPOSITORY_DIR_KEY, generalConfiguration
                .getDeploymentRepositoryDirectory().getPath() );
        }
        if ( generalConfiguration.getWorkingDirectory() != null )
        {
            this.configuration.setProperty( WORKING_DIR_KEY, generalConfiguration.getWorkingDirectory().getPath() );
        }
        ProxyConfiguration proxyConfiguration = this.generalConfiguration.getProxyConfiguration();
        if ( proxyConfiguration != null )
        {
            this.configuration.setProperty( PROXY_HOST_KEY, proxyConfiguration.getProxyHost() );
            this.configuration.setProperty( PROXY_PORT_KEY, proxyConfiguration.getProxyPort() );
            this.configuration.setProperty( PROXY_USER_KEY, proxyConfiguration.getProxyUser() );
            this.configuration.setProperty( PROXY_PASSWORD_KEY, proxyConfiguration.getProxyPassword() );
        }
    }

    // ----------------------------------------
    //  Spring injection
    // ----------------------------------------

    public ClassPathResource getClassPathResource()
    {
        return classPathResource;
    }


    public void setClassPathResource( ClassPathResource classPathResource )
    {
        this.classPathResource = classPathResource;
    }

}
