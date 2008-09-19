package org.apache.maven.continuum.reports.surefire;

import java.io.File;

import org.apache.maven.continuum.configuration.ConfigurationException;

public class ConfigurationServiceMock
    extends org.apache.maven.continuum.configuration.ConfigurationServiceMock
{
    public File getTestReportsDirectory( int buildId, int projectId )
        throws ConfigurationException
    {
        return new File( "src" + File.separatorChar + "test" + File.separatorChar + "resources" + File.separatorChar +
            "continuum-core" );
    }
}
