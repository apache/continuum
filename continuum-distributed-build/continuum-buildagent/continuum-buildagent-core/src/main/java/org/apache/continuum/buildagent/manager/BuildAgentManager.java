package org.apache.continuum.buildagent.manager;

import java.util.List;
import java.util.Map;

import org.apache.continuum.buildagent.buildcontext.BuildContext;
import org.apache.maven.continuum.ContinuumException;

public interface BuildAgentManager
{
    String ROLE = BuildAgentManager.class.getName();

    void prepareBuildProjects( List<BuildContext> buildContextList )
        throws ContinuumException;

    void returnBuildResult( Map result )
        throws ContinuumException;
    
    void startProjectBuild( int projectId )
        throws ContinuumException;
}
