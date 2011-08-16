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
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.continuum.buildagent.model.ContinuumBuildAgentConfigurationModel;
import org.apache.continuum.buildagent.model.io.xpp3.ContinuumBuildAgentConfigurationModelXpp3Reader;
import org.apache.continuum.buildagent.model.io.xpp3.ContinuumBuildAgentConfigurationModelXpp3Writer;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultBuildAgentConfiguration
    implements BuildAgentConfiguration
{
    private static final Logger log = LoggerFactory.getLogger( DefaultBuildAgentConfiguration.class );

    private File configurationFile;

    private GeneralBuildAgentConfiguration generalBuildAgentConfiguration;

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
            catch ( BuildAgentConfigurationException e )
            {
                // skip this and only log a warn
                log.warn( " error on loading configuration from file " + configurationFile.getPath() );
            }
        }
        else
        {
            log.info( "build agent configuration file does not exists" );
            this.generalBuildAgentConfiguration = new GeneralBuildAgentConfiguration();
        }
    }

    public GeneralBuildAgentConfiguration getContinuumBuildAgentConfiguration()
    {
        return generalBuildAgentConfiguration;
    }

    public void reload()
        throws BuildAgentConfigurationException
    {
        this.initialize();
    }

    public void reload( File file )
        throws BuildAgentConfigurationException
    {
        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream( file );
            ContinuumBuildAgentConfigurationModelXpp3Reader configurationXpp3Reader =
                new ContinuumBuildAgentConfigurationModelXpp3Reader();
            ContinuumBuildAgentConfigurationModel configuration =
                configurationXpp3Reader.read( new InputStreamReader( fis ) );

            this.generalBuildAgentConfiguration = new GeneralBuildAgentConfiguration();
            if ( StringUtils.isNotEmpty( configuration.getBuildOutputDirectory() ) )
            {
                this.generalBuildAgentConfiguration.setBuildOutputDirectory(
                    new File( configuration.getBuildOutputDirectory() ) );
            }
            if ( StringUtils.isNotEmpty( configuration.getWorkingDirectory() ) )
            {
                this.generalBuildAgentConfiguration.setWorkingDirectory(
                    new File( configuration.getWorkingDirectory() ) );
            }

            this.generalBuildAgentConfiguration.setContinuumServerUrl( configuration.getContinuumServerUrl() );
            this.generalBuildAgentConfiguration.setInstallations( configuration.getInstallations() );
            this.generalBuildAgentConfiguration.setLocalRepositories( configuration.getLocalRepositories() );
            this.generalBuildAgentConfiguration.setSharedSecretPassword( configuration.getSharedSecretPassword() );
        }
        catch ( IOException e )
        {
            log.error( e.getMessage(), e );
            throw new BuildAgentConfigurationException( e.getMessage(), e );
        }
        catch ( XmlPullParserException e )
        {
            log.error( e.getMessage(), e );
            throw new BuildAgentConfigurationException( e.getMessage(), e );
        }
        finally
        {
            if ( fis != null )
            {
                IOUtil.close( fis );
            }
        }
    }

    public void save()
        throws BuildAgentConfigurationException
    {
        if ( !configurationFile.exists() )
        {
            configurationFile.getParentFile().mkdirs();
        }
        save( configurationFile );
    }

    public void save( File file )
        throws BuildAgentConfigurationException
    {
        try
        {
            ContinuumBuildAgentConfigurationModel configurationModel = new ContinuumBuildAgentConfigurationModel();
            if ( this.generalBuildAgentConfiguration.getBuildOutputDirectory() != null )
            {
                configurationModel.setBuildOutputDirectory(
                    this.generalBuildAgentConfiguration.getBuildOutputDirectory().getPath() );
            }
            if ( this.generalBuildAgentConfiguration.getWorkingDirectory() != null )
            {
                configurationModel.setWorkingDirectory(
                    this.generalBuildAgentConfiguration.getWorkingDirectory().getPath() );
            }
            configurationModel.setContinuumServerUrl( this.generalBuildAgentConfiguration.getContinuumServerUrl() );
            configurationModel.setInstallations( this.generalBuildAgentConfiguration.getInstallations() );
            configurationModel.setLocalRepositories( this.generalBuildAgentConfiguration.getLocalRepositories() );
            configurationModel.setSharedSecretPassword( this.generalBuildAgentConfiguration.getSharedSecretPassword() );

            ContinuumBuildAgentConfigurationModelXpp3Writer writer =
                new ContinuumBuildAgentConfigurationModelXpp3Writer();
            FileWriter fileWriter = new FileWriter( file );
            writer.write( fileWriter, configurationModel );
            fileWriter.flush();
            fileWriter.close();
        }
        catch ( IOException e )
        {
            log.error( e.getMessage(), e );
            throw new BuildAgentConfigurationException( e.getMessage(), e );
        }
    }

    public void setContinuumBuildAgentConfiguration( GeneralBuildAgentConfiguration buildAgentConfiguration )
    {
        this.generalBuildAgentConfiguration = buildAgentConfiguration;
    }

    public File getConfigurationFile()
    {
        return configurationFile;
    }

    public void setConfigurationFile( File configurationFile )
    {
        this.configurationFile = configurationFile;
    }
}