package org.apache.continuum.builder.distributed.manager;

import java.util.List;
import java.util.Map;

import org.apache.continuum.builder.distributed.BuildAgentListener;
import org.apache.maven.continuum.ContinuumException;

public interface DistributedBuildManager
{
    void updateProjectScmRoot( Map context )
        throws ContinuumException;

    void updateBuildResult( Map context )
        throws ContinuumException;

    List<BuildAgentListener> getBuildAgentListeners();

    void reload();
}
