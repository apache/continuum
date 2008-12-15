package org.apache.continuum.buildagent.build.execution;

import org.apache.maven.continuum.execution.ContinuumBuildExecutorException;

public class ContinuumBuildCancelledException
    extends ContinuumBuildExecutorException
{
    public ContinuumBuildCancelledException( String message )
    {
        super( message );
    }

    public ContinuumBuildCancelledException( String message, Throwable cause )
    {
        super( message, cause );
    }
}
