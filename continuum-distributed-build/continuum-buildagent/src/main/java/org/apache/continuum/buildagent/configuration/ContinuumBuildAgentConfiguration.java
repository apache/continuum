package org.apache.continuum.buildagent.configuration;

import java.io.File;
import java.util.List;

import org.apache.continuum.buildagent.model.Installation;

public class ContinuumBuildAgentConfiguration
{
    private File workingDirectory;

    private File buildOutputDirectory;

    private String continuumServerUrl;

    private List<Installation> installations;

    public File getWorkingDirectory()
    {
        return workingDirectory;
    }

    public void setWorkingDirectory( File workingDirectory )
    {
        this.workingDirectory = workingDirectory;
    }

    public File getBuildOutputDirectory()
    {
        return buildOutputDirectory;
    }

    public void setBuildOutputDirectory( File buildOutputDirectory )
    {
        this.buildOutputDirectory = buildOutputDirectory;
    }

    public String getContinuumServerUrl()
    {
        return continuumServerUrl;
    }

    public void setContinuumServerUrl( String continuumServerUrl )
    {
        this.continuumServerUrl = continuumServerUrl;
    }

    public List<Installation> getInstallations()
    {
        return installations;
    }

    public void setInstallations( List<Installation> installations )
    {
        this.installations = installations;
    }
}
