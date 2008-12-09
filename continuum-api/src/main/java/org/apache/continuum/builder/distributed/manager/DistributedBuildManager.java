package org.apache.continuum.builder.distributed.manager;

import java.util.List;
import java.util.Map;

import org.apache.continuum.builder.distributed.BuildAgentListener;
import org.apache.continuum.scm.queue.PrepareBuildProjectsTask;
import org.apache.maven.continuum.ContinuumException;

public interface DistributedBuildManager
{
    void buildProjectsInQueue()
        throws ContinuumException;

    void cancelDistributedBuild( String buildAgentUrl, int projectId )
        throws ContinuumException;

    void updateProjectScmRoot( Map context )
        throws ContinuumException;

    void updateBuildResult( Map context )
        throws ContinuumException;

    List<BuildAgentListener> getBuildAgentListeners();

    List<PrepareBuildProjectsTask> getDistributedBuildQueue();

    void reload()
        throws ContinuumException;
}
