package org.apache.continuum.builder.distributed.manager;

import java.util.List;
import java.util.Map;

import org.apache.continuum.taskqueue.PrepareBuildProjectsTask;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.system.Installation;

public interface DistributedBuildManager
{
    String ROLE = DistributedBuildManager.class.getName();

    void cancelDistributedBuild( String buildAgentUrl, int projectGroupId, String scmRootAddress )
        throws ContinuumException;

    void updateScmResult( Map context )
        throws ContinuumException;

    void updateBuildResult( Map context )
        throws ContinuumException;

    void prepareBuildFinished( Map context )
        throws ContinuumException;

    void startProjectBuild( int projectId )
        throws ContinuumException;

    void startPrepareBuild( Map context )
        throws ContinuumException;

    void reload()
        throws ContinuumException;
    
    void removeAgentFromTaskQueueExecutor( String buildAgentUrl )
        throws ContinuumException;

    boolean isBuildAgentBusy( String buildAgentUrl );

    List<Installation> getAvailableInstallations( String buildAgentUrl )
        throws ContinuumException;

    Map<String, PrepareBuildProjectsTask> getDistributedBuildProjects();

    Map<String, Object> getBuildResult( int projectId )
        throws ContinuumException;
}