package org.apache.continuum.buildagent.build.execution;

public class ContinuumBuildExecutorException
    extends Exception
{
    public ContinuumBuildExecutorException( String message )
    {
        super( message );
    }

    public ContinuumBuildExecutorException( Throwable cause )
    {
        super( cause );
    }

    public ContinuumBuildExecutorException( String message, Throwable cause )
    {
        super( message, cause );
    }
}
