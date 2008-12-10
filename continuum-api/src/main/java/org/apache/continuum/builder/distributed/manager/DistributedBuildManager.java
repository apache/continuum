package org.apache.continuum.builder.distributed.manager;

import java.util.List;
import java.util.Map;

import org.apache.continuum.scm.queue.PrepareBuildProjectsTask;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.system.Installation;

public interface DistributedBuildManager
{
    String ROLE = DistributedBuildManager.class.getName();

    void cancelDistributedBuild( String buildAgentUrl, int projectGroupId, String scmRootAddress )
        throws ContinuumException;

    void updateProjectScmRoot( Map context )
        throws ContinuumException;

    void updateBuildResult( Map context )
        throws ContinuumException;

    void reload()
        throws ContinuumException;
    
    void removeAgentFromTaskQueueExecutor( String buildAgentUrl );

    boolean isBuildAgentBusy( String buildAgentUrl );

    List<Installation> getAvailableInstallations( String buildAgentUrl )
        throws ContinuumException;

    Map<String, PrepareBuildProjectsTask> getDistributedBuildProjects();
}
