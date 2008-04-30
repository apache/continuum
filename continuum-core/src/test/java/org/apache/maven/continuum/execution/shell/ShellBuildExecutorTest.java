package org.apache.maven.continuum.execution.shell;

import org.apache.maven.continuum.execution.AbstractContinuumBuildExecutorTest;

public class ShellBuildExecutorTest
    extends AbstractContinuumBuildExecutorTest
{
    public ShellBuildExecutorTest()
    {
        executor = new ShellBuildExecutor();
    }
}
