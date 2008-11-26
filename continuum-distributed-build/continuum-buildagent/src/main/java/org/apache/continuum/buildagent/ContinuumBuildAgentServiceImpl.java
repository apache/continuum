package org.apache.continuum.buildagent;

import java.util.List;
import java.util.Map;

import org.apache.continuum.buildagent.configuration.ContinuumBuildAgentConfiguration;
import org.apache.continuum.buildagent.configuration.ContinuumConfiguration;
import org.apache.continuum.buildagent.configuration.ContinuumConfigurationException;
import org.apache.continuum.buildagent.model.Installation;

public class ContinuumBuildAgentServiceImpl
    implements ContinuumBuildAgentService
{
    /**
     * @plexus.requirement
     */
    private ContinuumConfiguration configuration;

    private ContinuumBuildAgentConfiguration buildAgentConfiguration;

    public void initialize()
        throws ContinuumBuildAgentException
    {
        loadData();
    }

    public void buildProject( List<Map> projectsBuildContext )
        throws ContinuumBuildAgentException
    {
        // TODO Auto-generated method stub
        
    }

    public List<Installation> getAvailableInstallations()
        throws ContinuumBuildAgentException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Map getBuildResult( int projectId )
        throws ContinuumBuildAgentException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isBusy()
        throws ContinuumBuildAgentException
    {
        // TODO Auto-generated method stub
        return false;
    }

    private void loadData()
        throws ContinuumBuildAgentException
    {
        try
        {
            buildAgentConfiguration = configuration.getContinuumBuildAgentConfiguration();
        }
        catch ( ContinuumConfigurationException e )
        {
            throw new ContinuumBuildAgentException( "Unable to load build agent configuration", e );
        }
    }

    public int getProjectCurrentlyBuilding()
    {
        // TODO Auto-generated method stub
        return 0;
    }
}
