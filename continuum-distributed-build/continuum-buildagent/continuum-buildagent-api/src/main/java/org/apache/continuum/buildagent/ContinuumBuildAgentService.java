package org.apache.continuum.buildagent;

import java.util.List;
import java.util.Map;

public interface ContinuumBuildAgentService
{
    void buildProjects( List<Map> projectsBuildContext )
        throws ContinuumBuildAgentException;
    
    List<Map> getAvailableInstallations()
        throws ContinuumBuildAgentException;

    Map getBuildResult( int projectId )
        throws ContinuumBuildAgentException;
    
    int getProjectCurrentlyBuilding()
        throws ContinuumBuildAgentException;
    
    void cancelBuild()
        throws ContinuumBuildAgentException;
}
