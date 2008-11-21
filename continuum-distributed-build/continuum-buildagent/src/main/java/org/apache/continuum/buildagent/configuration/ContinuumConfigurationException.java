package org.apache.continuum.buildagent.configuration;

public class ContinuumConfigurationException
    extends Exception
{
    public ContinuumConfigurationException( String message )
    {
        super( message );
    }

    public ContinuumConfigurationException( Throwable cause )
    {
        super( cause );
    }

    public ContinuumConfigurationException( String message, Throwable cause )
    {
        super( message, cause );
    }
}
