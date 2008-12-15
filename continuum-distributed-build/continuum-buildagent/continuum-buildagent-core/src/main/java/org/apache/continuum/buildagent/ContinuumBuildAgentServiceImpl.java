package org.apache.continuum.buildagent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.continuum.buildagent.buildcontext.BuildContext;
import org.apache.continuum.buildagent.configuration.ConfigurationService;
import org.apache.continuum.buildagent.model.Installation;
import org.apache.continuum.buildagent.utils.ContinuumBuildAgentUtil;

public class ContinuumBuildAgentServiceImpl
    implements ContinuumBuildAgentService
{
    /**
     * @plexus.requirement
     */
    private ConfigurationService configurationService;

    public void buildProjects( List<Map> projectsBuildContext )
        throws ContinuumBuildAgentException
    {
        List<BuildContext> buildContext = initializeBuildContext( projectsBuildContext );

        try
        {
            Thread.sleep( 60000 );
        }
        catch ( InterruptedException e )
        {
        }
    }

    public List<Installation> getAvailableInstallations()
        throws ContinuumBuildAgentException
    {
        return configurationService.getAvailableInstallations();
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

    public int getProjectCurrentlyBuilding()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public void cancelBuild()
        throws ContinuumBuildAgentException
    {
        
    }

    private List<BuildContext> initializeBuildContext( List<Map> projectsBuildContext )
    {
        List<BuildContext> buildContext = new ArrayList<BuildContext>();
        
        for ( Map map : projectsBuildContext )
        {
            BuildContext context = new BuildContext();
            context.setProjectId( ContinuumBuildAgentUtil.getProjectId( map ) );
            context.setBuildDefinitionId( ContinuumBuildAgentUtil.getBuildDefinitionId( map ) );
            context.setBuildFile( ContinuumBuildAgentUtil.getBuildFile( map ) );
            context.setExecutorId( ContinuumBuildAgentUtil.getExecutorId( map ) );
            context.setGoals( ContinuumBuildAgentUtil.getGoals( map ) );
            context.setArguments( ContinuumBuildAgentUtil.getArguments( map ) );
            context.setScmUrl( ContinuumBuildAgentUtil.getScmUrl( map ) );
            context.setScmUsername( ContinuumBuildAgentUtil.getScmUsername( map ) );
            context.setScmPassword( ContinuumBuildAgentUtil.getScmPassword( map ) );
            context.setBuildFresh( ContinuumBuildAgentUtil.isBuildFresh( map ) );
            context.setProjectGroupId( ContinuumBuildAgentUtil.getProjectGroupId( map ) );
            context.setScmRootAddress( ContinuumBuildAgentUtil.getScmRootAddress( map ) );
            
            buildContext.add( context );
        }

        return buildContext;
    }

    public ConfigurationService getConfigurationService()
    {
        return configurationService;
    }

    public void setConfigurationService( ConfigurationService configurationService )
    {
        this.configurationService = configurationService;
    }
}
