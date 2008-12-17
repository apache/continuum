package org.apache.continuum.buildagent.configuration;

public class BuildAgentConfigurationException
    extends Exception
{
    public BuildAgentConfigurationException( String message )
    {
        super( message );
    }

    public BuildAgentConfigurationException( Throwable cause )
    {
        super( cause );
    }

    public BuildAgentConfigurationException( String message, Throwable cause )
    {
        super( message, cause );
    }
}
