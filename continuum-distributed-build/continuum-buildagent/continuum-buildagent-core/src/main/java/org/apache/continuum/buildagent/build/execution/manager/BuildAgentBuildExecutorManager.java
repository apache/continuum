package org.apache.continuum.buildagent.build.execution.manager;

import org.apache.continuum.buildagent.build.execution.ContinuumAgentBuildExecutor;
import org.apache.maven.continuum.ContinuumException;

public interface BuildAgentBuildExecutorManager
{
    String ROLE = BuildAgentBuildExecutorManager.class.getName();

    ContinuumAgentBuildExecutor getBuildExecutor( String executorId )
        throws ContinuumException;

    boolean hasBuildExecutor( String executorId );
}
