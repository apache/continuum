package org.apache.continuum.builder.distributed;

import org.apache.maven.continuum.ContinuumException;

import java.util.Map;

public interface DistributedBuildService
{
    String ROLE = DistributedBuildService.class.getName();

    void prepareBuildFinished( Map<String, Object> context )
        throws ContinuumException;

    boolean shouldBuild( Map<String, Object> context );

    void startPrepareBuild( Map<String, Object> context )
        throws ContinuumException;

    void startProjectBuild( int projectId )
        throws ContinuumException;

    void updateBuildResult( Map<String, Object> context )
        throws ContinuumException;

    void updateProject( Map<String, Object> context )
        throws ContinuumException;

    Map<String, String> getEnvironments( int buildDefinitionId, String installationType )
        throws ContinuumException;
}
