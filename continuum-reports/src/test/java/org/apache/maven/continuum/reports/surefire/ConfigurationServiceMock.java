package org.apache.maven.continuum.reports.surefire;

import org.apache.maven.continuum.configuration.ConfigurationException;

import java.io.File;

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
