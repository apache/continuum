package org.apache.continuum.buildagent;

public class ContinuumBuildAgentException
    extends Exception
{
    public ContinuumBuildAgentException( String message )
    {
        super( message );
    }

    public ContinuumBuildAgentException( Throwable cause )
    {
        super( cause );
    }

    public ContinuumBuildAgentException( String message, Throwable cause )
    {
        super( message, cause );
    }
}
