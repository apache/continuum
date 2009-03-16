package org.apache.continuum.buildagent.configuration;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.annotation.Resource;

import org.codehaus.plexus.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultBuildAgentConfigurationService
    implements BuildAgentConfigurationService
{
    private Logger log = LoggerFactory.getLogger( this.getClass() );

    @Resource
    private BuildAgentConfiguration buildAgentConfiguration;

    private GeneralBuildAgentConfiguration generalBuildAgentConfiguration;

    public void initialize()
        throws BuildAgentConfigurationException
    {
        loadData();
    }

    public BuildAgentConfiguration getBuildAgentConfiguration()
    {
        return buildAgentConfiguration;
    }

    public void setBuildAgentConfiguration( BuildAgentConfiguration buildAgentConfiguration )
    {
        this.buildAgentConfiguration = buildAgentConfiguration;
    }

    public File getBuildOutputDirectory()
    {
        return generalBuildAgentConfiguration.getBuildOutputDirectory();
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
        return generalBuildAgentConfiguration.getWorkingDirectory();
    }

    public File getWorkingDirectory( int projectId )
    {
        File dir = new File( generalBuildAgentConfiguration.getWorkingDirectory(), Integer.toString( projectId ) );

        try
        {
            dir = dir.getCanonicalFile();
        }
        catch ( IOException e )
        {
        }

        return dir;
    }

    public String getBuildOutput( int projectId )
        throws BuildAgentConfigurationException
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
        throws BuildAgentConfigurationException
    {
        File dir = getBuildOutputDirectory( projectId );

        if ( !dir.exists() && !dir.mkdirs() )
        {
            throw new BuildAgentConfigurationException( 
                      "Could not make the build output directory: " + "'" + dir.getAbsolutePath() + "'." );
        }

        return new File( dir, "build.log.txt" );
    }

    public String getContinuumServerUrl()
    {
        return generalBuildAgentConfiguration.getContinuumServerUrl();
    }

    public List getAvailableInstallations()
    {
        return generalBuildAgentConfiguration.getInstallations();
    }

    private void loadData()
        throws BuildAgentConfigurationException
    {
        generalBuildAgentConfiguration = buildAgentConfiguration.getContinuumBuildAgentConfiguration();
    }
}