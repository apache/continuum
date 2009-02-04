package org.apache.continuum.buildagent.build.execution.maven.m1;

import java.io.File;

import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;

public interface BuildAgentMavenOneMetadataHelper
{
    String ROLE = BuildAgentMavenOneMetadataHelper.class.getName();

    void mapMetadata( ContinuumProjectBuildingResult result, File metadata, Project project )
        throws BuildAgentMavenOneMetadataHelperException;
}
