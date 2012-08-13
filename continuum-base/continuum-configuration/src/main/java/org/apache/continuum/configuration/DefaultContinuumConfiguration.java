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

import org.apache.commons.lang.StringUtils;
import org.apache.continuum.configuration.model.ContinuumConfigurationModel;
import org.apache.continuum.configuration.model.io.xpp3.ContinuumConfigurationModelXpp3Reader;
import org.apache.continuum.configuration.model.io.xpp3.ContinuumConfigurationModelXpp3Writer;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:olamy@apache.org">olamy</a>
 * @version $Id$
 * @since 17 juin 2008
 */
public class DefaultContinuumConfiguration
    implements ContinuumConfiguration
{
    private static final Logger log = LoggerFactory.getLogger( DefaultContinuumConfiguration.class );

    private File configurationFile;

    private GeneralConfiguration generalConfiguration;

    public static final String CONFIGURATION_FILE = "continuum.xml";

    //----------------------------------------------------
    //  Initialize method configured in the Spring xml 
    //   configuration file
    //----------------------------------------------------
    protected void initialize()
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "configurationFile null " + ( configurationFile.getPath() == null ) );
        }
        if ( configurationFile != null && configurationFile.exists() )
        {
            try
            {
                reload( configurationFile );
            }
            catch ( ContinuumConfigurationException e )
            {
                // skip this and only log a warn
                log.warn( " error on loading configuration from file " + configurationFile.getPath() );
            }
        }
        else
        {
            log.info( "configuration file not exists" );
            this.generalConfiguration = new GeneralConfiguration();
        }
    }

    public void reload()
        throws ContinuumConfigurationException
    {
        this.initialize();
    }

    public void save()
        throws ContinuumConfigurationException
    {
        if ( !configurationFile.exists() )
        {
            configurationFile.getParentFile().mkdirs();
        }
        save( configurationFile );
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
    }

    public void reload( File file )
        throws ContinuumConfigurationException
    {
        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream( file );
            ContinuumConfigurationModelXpp3Reader configurationXpp3Reader = new ContinuumConfigurationModelXpp3Reader();
            ContinuumConfigurationModel configuration = configurationXpp3Reader.read( new InputStreamReader( fis ) );

            this.generalConfiguration = new GeneralConfiguration();

            this.generalConfiguration.setNumberOfBuildsInParallel( configuration.getNumberOfBuildsInParallel() );
            this.generalConfiguration.setBaseUrl( configuration.getBaseUrl() );
            this.generalConfiguration.setSharedSecretPassword( configuration.getSharedSecretPassword() );

            if ( StringUtils.isNotEmpty( configuration.getBuildOutputDirectory() ) )
            {
                // TODO take care if file exists ?
                this.generalConfiguration.setBuildOutputDirectory( new File(
                    configuration.getBuildOutputDirectory() ) );
            }
            if ( StringUtils.isNotEmpty( configuration.getDeploymentRepositoryDirectory() ) )
            {
                // TODO take care if file exists ?
                this.generalConfiguration.setDeploymentRepositoryDirectory( new File(
                    configuration.getDeploymentRepositoryDirectory() ) );
            }
            if ( StringUtils.isNotEmpty( configuration.getWorkingDirectory() ) )
            {
                // TODO take care if file exists ?
                this.generalConfiguration.setWorkingDirectory( new File( configuration.getWorkingDirectory() ) );
            }
            if ( configuration.getProxyConfiguration() != null )
            {
                ProxyConfiguration proxyConfiguration = new ProxyConfiguration(
                    configuration.getProxyConfiguration().getProxyHost(),
                    configuration.getProxyConfiguration().getProxyPassword(),
                    configuration.getProxyConfiguration().getProxyPort(),
                    configuration.getProxyConfiguration().getProxyUser() );
                this.generalConfiguration.setProxyConfiguration( proxyConfiguration );
            }
            if ( StringUtils.isNotEmpty( configuration.getReleaseOutputDirectory() ) )
            {
                // TODO take care if file exists?
                this.generalConfiguration.setReleaseOutputDirectory( new File(
                    configuration.getReleaseOutputDirectory() ) );
            }

            // set the configuration for diistributedBuildEnabled
            this.generalConfiguration.setDistributedBuildEnabled( configuration.isDistributedBuildEnabled() );

            if ( configuration.getBuildAgents() != null )
            {
                List<BuildAgentConfiguration> buildAgents = new ArrayList<BuildAgentConfiguration>();

                List<org.apache.continuum.configuration.model.BuildAgentConfiguration> agents =
                    configuration.getBuildAgents();
                for ( org.apache.continuum.configuration.model.BuildAgentConfiguration agent : agents )
                {
                    BuildAgentConfiguration buildAgent = new BuildAgentConfiguration( agent.getUrl(),
                                                                                      agent.getDescription(),
                                                                                      agent.isEnabled() );
                    buildAgents.add( buildAgent );
                }

                this.generalConfiguration.setBuildAgents( buildAgents );
            }
            if ( configuration.getBuildAgentGroups() != null )
            {
                List<BuildAgentGroupConfiguration> buildAgentGroups = new ArrayList<BuildAgentGroupConfiguration>();
                List<org.apache.continuum.configuration.model.BuildAgentGroupConfiguration> groups =
                    configuration.getBuildAgentGroups();

                for ( org.apache.continuum.configuration.model.BuildAgentGroupConfiguration group : groups )
                {
                    List<BuildAgentConfiguration> agents = new ArrayList<BuildAgentConfiguration>();
                    List<org.apache.continuum.configuration.model.BuildAgentConfiguration> modelAgents =
                        group.getBuildAgents();
                    for ( org.apache.continuum.configuration.model.BuildAgentConfiguration modelAgent : modelAgents )
                    {
                        BuildAgentConfiguration buildAgent = new BuildAgentConfiguration( modelAgent.getUrl(),
                                                                                          modelAgent.getDescription(),
                                                                                          modelAgent.isEnabled() );
                        agents.add( buildAgent );
                    }

                    BuildAgentGroupConfiguration groupAgent = new BuildAgentGroupConfiguration( group.getName(),
                                                                                                agents );
                    buildAgentGroups.add( groupAgent );
                }

                this.generalConfiguration.setBuildAgentGroups( buildAgentGroups );
            }
        }
        catch ( IOException e )
        {
            log.error( e.getMessage(), e );
            throw new RuntimeException( e.getMessage(), e );
        }
        catch ( XmlPullParserException e )
        {
            log.error( e.getMessage(), e );
            throw new RuntimeException( e.getMessage(), e );
        }
        finally
        {
            if ( fis != null )
            {
                IOUtil.close( fis );
            }
        }
    }

    public void save( File file )
        throws ContinuumConfigurationException
    {
        try
        {
            ContinuumConfigurationModel configurationModel = new ContinuumConfigurationModel();
            configurationModel.setBaseUrl( this.generalConfiguration.getBaseUrl() );
            configurationModel.setNumberOfBuildsInParallel( this.generalConfiguration.getNumberOfBuildsInParallel() );
            configurationModel.setSharedSecretPassword( this.generalConfiguration.getSharedSecretPassword() );

            // normally not null but NPE free is better !
            if ( this.generalConfiguration.getBuildOutputDirectory() != null )
            {
                configurationModel.setBuildOutputDirectory(
                    this.generalConfiguration.getBuildOutputDirectory().getPath() );
            }
            if ( this.generalConfiguration.getWorkingDirectory() != null )
            {
                configurationModel.setWorkingDirectory( this.generalConfiguration.getWorkingDirectory().getPath() );
            }
            if ( this.generalConfiguration.getDeploymentRepositoryDirectory() != null )
            {
                configurationModel.setDeploymentRepositoryDirectory(
                    this.generalConfiguration.getDeploymentRepositoryDirectory().getPath() );
            }
            if ( this.generalConfiguration.getProxyConfiguration() != null )
            {
                configurationModel.setProxyConfiguration(
                    new org.apache.continuum.configuration.model.ProxyConfiguration() );
                configurationModel.getProxyConfiguration().setProxyHost(
                    this.generalConfiguration.getProxyConfiguration().getProxyHost() );
                configurationModel.getProxyConfiguration().setProxyPassword(
                    this.generalConfiguration.getProxyConfiguration().getProxyPassword() );
                configurationModel.getProxyConfiguration().setProxyPort(
                    this.generalConfiguration.getProxyConfiguration().getProxyPort() );
                configurationModel.getProxyConfiguration().setProxyHost(
                    this.generalConfiguration.getProxyConfiguration().getProxyHost() );
            }
            if ( this.generalConfiguration.getReleaseOutputDirectory() != null )
            {
                configurationModel.setReleaseOutputDirectory(
                    this.generalConfiguration.getReleaseOutputDirectory().getPath() );
            }

            // set configuration for distributedBuildEnabled.
            configurationModel.setDistributedBuildEnabled( this.generalConfiguration.isDistributedBuildEnabled() );

            if ( this.generalConfiguration.getBuildAgents() != null )
            {
                List<org.apache.continuum.configuration.model.BuildAgentConfiguration> buildAgents =
                    new ArrayList<org.apache.continuum.configuration.model.BuildAgentConfiguration>();

                for ( BuildAgentConfiguration agent : this.generalConfiguration.getBuildAgents() )
                {
                    org.apache.continuum.configuration.model.BuildAgentConfiguration buildAgent =
                        new org.apache.continuum.configuration.model.BuildAgentConfiguration();
                    buildAgent.setUrl( agent.getUrl() );
                    buildAgent.setDescription( agent.getDescription() );
                    buildAgent.setEnabled( agent.isEnabled() );

                    buildAgents.add( buildAgent );
                }
                configurationModel.setBuildAgents( buildAgents );
            }
            if ( this.generalConfiguration.getBuildAgentGroups() != null )
            {
                List<org.apache.continuum.configuration.model.BuildAgentGroupConfiguration> buildAgentGroups =
                    new ArrayList<org.apache.continuum.configuration.model.BuildAgentGroupConfiguration>();

                for ( BuildAgentGroupConfiguration group : this.generalConfiguration.getBuildAgentGroups() )
                {
                    org.apache.continuum.configuration.model.BuildAgentGroupConfiguration buildAgentGroup =
                        new org.apache.continuum.configuration.model.BuildAgentGroupConfiguration();
                    buildAgentGroup.setName( group.getName() );

                    List<org.apache.continuum.configuration.model.BuildAgentConfiguration> buildAgents =
                        new ArrayList<org.apache.continuum.configuration.model.BuildAgentConfiguration>();

                    if ( group.getBuildAgents() != null )
                    {
                        for ( BuildAgentConfiguration agent : group.getBuildAgents() )
                        {
                            org.apache.continuum.configuration.model.BuildAgentConfiguration buildAgent =
                                new org.apache.continuum.configuration.model.BuildAgentConfiguration();
                            buildAgent.setUrl( agent.getUrl() );
                            buildAgent.setDescription( agent.getDescription() );
                            buildAgent.setEnabled( agent.isEnabled() );

                            buildAgents.add( buildAgent );
                        }

                        buildAgentGroup.setBuildAgents( buildAgents );
                    }

                    buildAgentGroups.add( buildAgentGroup );
                }
                configurationModel.setBuildAgentGroups( buildAgentGroups );
            }

            ContinuumConfigurationModelXpp3Writer writer = new ContinuumConfigurationModelXpp3Writer();
            FileWriter fileWriter = new FileWriter( file );
            writer.write( fileWriter, configurationModel );
            fileWriter.flush();
            fileWriter.close();
        }
        catch ( IOException e )
        {
            throw new ContinuumConfigurationException( e.getMessage(), e );
        }

    }

    // ----------------------------------------
    //  Spring injection
    // ----------------------------------------


    public File getConfigurationFile()
    {
        return configurationFile;
    }

    public void setConfigurationFile( File configurationFile )
    {
        this.configurationFile = configurationFile;
    }

}
