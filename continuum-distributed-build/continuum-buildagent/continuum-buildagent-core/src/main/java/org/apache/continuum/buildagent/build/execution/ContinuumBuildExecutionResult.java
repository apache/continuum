package org.apache.continuum.buildagent.build.execution;

import java.io.File;

public class ContinuumBuildExecutionResult
{
    private File output;

    private int exitCode;

    public ContinuumBuildExecutionResult( File output, int exitCode )
    {
        this.output = output;

        this.exitCode = exitCode;
    }

    public File getOutput()
    {
        return output;
    }

    public int getExitCode()
    {
        return exitCode;
    }
}
