package org.apache.continuum.buildagent;

import java.util.List;
import java.util.Map;

import org.apache.continuum.buildagent.model.Installation;

public interface ContinuumBuildAgentService
{
    void buildProjects( List<Map> projectsBuildContext )
        throws ContinuumBuildAgentException;

    List<Installation> getAvailableInstallations()
        throws ContinuumBuildAgentException;
    
    boolean isBusy()
        throws ContinuumBuildAgentException;

    Map getBuildResult( int projectId )
        throws ContinuumBuildAgentException;

    int getProjectCurrentlyBuilding();
}
