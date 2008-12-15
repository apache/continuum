package org.apache.continuum.buildagent.build.execution.maven.m2;

import java.io.File;

import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.apache.maven.project.MavenProject;

public interface MavenBuilderHelper
{
    String ROLE = MavenBuilderHelper.class.getName();

    MavenProject getMavenProject( ContinuumProjectBuildingResult result, File file );
}
