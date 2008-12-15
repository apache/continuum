package org.apache.continuum.buildagent.utils;

import org.apache.continuum.buildagent.buildcontext.BuildContext;
import org.apache.maven.continuum.model.project.BuildDefinition;

/**
 * @author Maria Catherine Tan
 */
public class BuildContextToBuildDefinition
{
    public static BuildDefinition getBuildDefinition( BuildContext buildContext )
    {
        BuildDefinition buildDefinition = new BuildDefinition();

        buildDefinition.setAlwaysBuild( true );

        buildDefinition.setArguments( buildContext.getArguments() );

        buildDefinition.setBuildFile( buildContext.getBuildFile() );

        buildDefinition.setBuildFresh( buildContext.isBuildFresh() );

        buildDefinition.setGoals( buildContext.getGoals() );

        return buildDefinition;
    }
}
