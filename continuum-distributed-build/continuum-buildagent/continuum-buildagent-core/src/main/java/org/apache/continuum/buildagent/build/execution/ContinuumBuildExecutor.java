package org.apache.continuum.buildagent.build.execution;

import java.io.File;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Project;

public interface ContinuumBuildExecutor
{
    String ROLE = ContinuumBuildExecutor.class.getName();

    ContinuumBuildExecutionResult build( Project project, BuildDefinition buildDefinition, File buildOutput )
        throws ContinuumBuildExecutorException, ContinuumBuildCancelledException;
    
    boolean isBuilding( Project project );

    void killProcess( Project project );

    // TODO: are these part of the builder interface, or a separate project/build definition interface?
    List<Artifact> getDeployableArtifacts( Project project, File workingDirectory, BuildDefinition buildDefinition )
        throws ContinuumBuildExecutorException;
}
