package org.apache.continuum.buildagent.configuration;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.continuum.buildagent.model.Installation;
import org.codehaus.plexus.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultConfigurationService
    implements ConfigurationService
{
    private Logger log = LoggerFactory.getLogger( this.getClass() );

    /**
     * @plexus.requirement
     */
    private ContinuumConfiguration configuration;

    private ContinuumBuildAgentConfiguration buildAgentConfiguration;

    public void initialize()
        throws ContinuumConfigurationException
    {
        loadData();
    }

    public ContinuumConfiguration getConfiguration()
    {
        return configuration;
    }

    public void setConfiguration( ContinuumConfiguration configuration )
    {
        this.configuration = configuration;
    }

    public File getBuildOutputDirectory()
    {
        return buildAgentConfiguration.getBuildOutputDirectory();
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
        return buildAgentConfiguration.getWorkingDirectory();
    }

    public File getWorkingDirectory( int projectId )
    {
        return new File( buildAgentConfiguration.getWorkingDirectory(), Integer.toString( projectId ) );
    }

    public String getBuildOutput( int projectId )
        throws ContinuumConfigurationException
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
                return "There are no output for this build.";
            }
        }
        catch ( IOException e )
        {
            log.warn( "Error reading build output for project '" + projectId + "'.", e );
    
            return null;
        }
    }

    public File getBuildOutputFile( int projectId )
        throws ContinuumConfigurationException
    {
        File dir = getBuildOutputDirectory( projectId );

        if ( !dir.exists() && !dir.mkdirs() )
        {
            throw new ContinuumConfigurationException( 
                      "Could not make the build output directory: " + "'" + dir.getAbsolutePath() + "'." );
        }

        return new File( dir, "build.log.txt" );
    }

    public String getContinuumServerUrl()
    {
        return buildAgentConfiguration.getContinuumServerUrl();
    }

    public List<Installation> getAvailableInstallations()
    {
        return buildAgentConfiguration.getInstallations();
    }

    private void loadData()
        throws ContinuumConfigurationException
    {
        buildAgentConfiguration = configuration.getContinuumBuildAgentConfiguration();
    }
}