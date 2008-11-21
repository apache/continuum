package org.apache.continuum.buildagent.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.continuum.buildagent.model.ContinuumBuildAgentConfigurationModel;
import org.apache.continuum.buildagent.model.io.xpp3.ContinuumBuildAgentConfigurationModelXpp3Reader;
import org.apache.continuum.buildagent.model.io.xpp3.ContinuumBuildAgentConfigurationModelXpp3Writer;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultContinuumConfiguration
    //implements ContinuumConfiguration
{
    private Logger log = LoggerFactory.getLogger( getClass() );

    private File configurationFile;

    private ContinuumBuildAgentConfiguration buildAgentConfiguration;

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
            log.info( "build agent configuration file does not exists" );
            this.buildAgentConfiguration = new ContinuumBuildAgentConfiguration();
        }
    }

    public ContinuumBuildAgentConfiguration getContinuumBuildAgentConfiguration()
        throws ContinuumConfigurationException
    {
        return buildAgentConfiguration;
    }

    public void reload()
        throws ContinuumConfigurationException
    {
        this.initialize();
    }

    public void reload( File file )
        throws ContinuumConfigurationException
    {
        try
        {
            ContinuumBuildAgentConfigurationModelXpp3Reader configurationXpp3Reader = 
                new ContinuumBuildAgentConfigurationModelXpp3Reader();
            ContinuumBuildAgentConfigurationModel configuration = configurationXpp3Reader
                .read( new InputStreamReader( new FileInputStream( file ) ) );

            this.buildAgentConfiguration = new ContinuumBuildAgentConfiguration();
            if ( StringUtils.isNotEmpty( configuration.getBuildOutputDirectory() ) )
            {
                this.buildAgentConfiguration.setBuildOutputDirectory( new File( configuration.getBuildOutputDirectory() ) );
            }
            if ( StringUtils.isNotEmpty( configuration.getWorkingDirectory() ) )
            {
                this.buildAgentConfiguration.setWorkingDirectory( new File( configuration.getWorkingDirectory() ) );
            }
            this.buildAgentConfiguration.setContinuumServerUrl( configuration.getContinuumServerUrl() );
            this.buildAgentConfiguration.setInstallations( configuration.getInstallations() );
        }
        catch ( IOException e )
        {
            log.error( e.getMessage(), e );
            throw new ContinuumConfigurationException( e.getMessage(), e );
        }
        catch ( XmlPullParserException e )
        {
            log.error( e.getMessage(), e );
            throw new ContinuumConfigurationException( e.getMessage(), e );
        }
    }

    public void save()
        throws ContinuumConfigurationException
    {
        if ( !configurationFile.exists() )
        {
            configurationFile.getParentFile().mkdir();
        }
        save( configurationFile );
    }

    public void save( File file )
        throws ContinuumConfigurationException
    {
        try
        {
            ContinuumBuildAgentConfigurationModel configurationModel = new ContinuumBuildAgentConfigurationModel();
            if ( this.buildAgentConfiguration.getBuildOutputDirectory() != null )
            {
                configurationModel.setBuildOutputDirectory( this.buildAgentConfiguration.getBuildOutputDirectory().getPath() );
            }
            if ( this.buildAgentConfiguration.getWorkingDirectory() != null )
            {
                configurationModel.setWorkingDirectory( this.buildAgentConfiguration.getWorkingDirectory().getPath() );
            }
            configurationModel.setContinuumServerUrl( this.buildAgentConfiguration.getContinuumServerUrl() );
            configurationModel.setInstallations( this.buildAgentConfiguration.getInstallations() );

            ContinuumBuildAgentConfigurationModelXpp3Writer writer = new ContinuumBuildAgentConfigurationModelXpp3Writer();
            FileWriter fileWriter = new FileWriter( file );
            writer.write( fileWriter, configurationModel );
        }
        catch ( IOException e )
        {
            log.error( e.getMessage(), e );
            throw new ContinuumConfigurationException( e.getMessage(), e );
        }
    }

    public void setContinuumBuildAgentConfiguration( ContinuumBuildAgentConfiguration buildAgentConfiguration )
        throws ContinuumConfigurationException
    {
        this.buildAgentConfiguration = buildAgentConfiguration;
    }
}
