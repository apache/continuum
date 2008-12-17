package org.apache.continuum.buildagent.configuration;

import java.io.File;
import java.util.List;

import org.apache.continuum.buildagent.model.Installation;

public interface BuildAgentConfigurationService
{
    String ROLE = BuildAgentConfigurationService.class.getName();

    File getBuildOutputDirectory();

    File getBuildOutputDirectory( int projectId );

    File getWorkingDirectory();

    File getWorkingDirectory( int projectId );

    String getContinuumServerUrl();

    String getBuildOutput( int projectId )
        throws BuildAgentConfigurationException;

    File getBuildOutputFile( int projectId )
        throws BuildAgentConfigurationException;

    List<Installation> getAvailableInstallations();
}
