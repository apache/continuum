package org.apache.continuum.buildagent.build.execution;

import org.apache.maven.continuum.execution.ContinuumBuildExecutorException;

public class ContinuumAgentBuildCancelledException
    extends ContinuumBuildExecutorException
{
    public ContinuumAgentBuildCancelledException( String message )
    {
        super( message );
    }

    public ContinuumAgentBuildCancelledException( String message, Throwable cause )
    {
        super( message, cause );
    }
}
