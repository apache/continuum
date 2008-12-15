package org.apache.continuum.buildagent.build.execution.manager;

import org.apache.continuum.buildagent.build.execution.ContinuumBuildExecutor;
import org.apache.maven.continuum.ContinuumException;

public interface BuildExecutorManager
{
    String ROLE = BuildExecutorManager.class.getName();

    ContinuumBuildExecutor getBuildExecutor( String executorId )
        throws ContinuumException;

    boolean hasBuildExecutor( String executorId );
}
