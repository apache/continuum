package org.apache.continuum.buildagent.build.execution;

public class ContinuumAgentBuildExecutorException
    extends Exception
{
    public ContinuumAgentBuildExecutorException( String message )
    {
        super( message );
    }

    public ContinuumAgentBuildExecutorException( Throwable cause )
    {
        super( cause );
    }

    public ContinuumAgentBuildExecutorException( String message, Throwable cause )
    {
        super( message, cause );
    }
}
