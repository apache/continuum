package org.apache.continuum.buildagent.utils.shell;

public class ExecutionResult
{
    private int exitCode;

    public ExecutionResult( int exitCode )
    {
        this.exitCode = exitCode;
    }

    public int getExitCode()
    {
        return exitCode;
    }
}
