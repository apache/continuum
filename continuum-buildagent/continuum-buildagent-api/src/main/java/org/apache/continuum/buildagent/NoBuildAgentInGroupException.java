package org.apache.continuum.buildagent;

public class NoBuildAgentInGroupException
    extends Exception
{
    public NoBuildAgentInGroupException( String message )
    {
        super( message );
    }

    public NoBuildAgentInGroupException( Throwable cause )
    {
        super( cause );
    }

    public NoBuildAgentInGroupException( String message, Throwable cause )
    {
        super( message, cause );
    }
}
