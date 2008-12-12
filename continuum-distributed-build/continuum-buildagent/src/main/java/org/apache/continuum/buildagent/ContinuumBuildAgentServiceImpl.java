package org.apache.continuum.buildagent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.continuum.buildagent.configuration.ConfigurationService;
import org.apache.continuum.buildagent.model.BuildContext;
import org.apache.continuum.buildagent.model.Installation;

public class ContinuumBuildAgentServiceImpl
    extends AbstractContinuumBuildAgentService
{
    /**
     * @plexus.requirement
     */
    private ConfigurationService configurationService;

    public void buildProjects( List<Map> projectsBuildContext )
        throws ContinuumBuildAgentException
    {
        List<BuildContext> buildContext = initializeBuildContext( projectsBuildContext );

        prepareBuildProjects( buildContext );
        
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
    {
        
    }

    private List<BuildContext> initializeBuildContext( List<Map> projectsBuildContext )
    {
        List<BuildContext> buildContext = new ArrayList<BuildContext>();
        
        for ( Map map : projectsBuildContext )
        {
            BuildContext context = new BuildContext();
            context.setProjectId( getProjectId( map ) );
            context.setBuildDefinitionId( getBuildDefinitionId( map ) );
            context.setBuildFile( getBuildFile( map ) );
            context.setExecutorId( getExecutorId( map ) );
            context.setGoals( getGoals( map ) );
            context.setArguments( getArguments( map ) );
            context.setScmUrl( getScmUrl( map ) );
            context.setScmUsername( getScmUsername( map ) );
            context.setScmPassword( getScmPassword( map ) );
            context.setBuildFresh( isBuildFresh( map ) );
            context.setProjectGroupId( getProjectGroupId( map ) );
            context.setScmRootAddress( getScmRootAddress( map ) );
            
            buildContext.add( context );
        }

        return buildContext;
    }

    private void prepareBuildProjects( List<BuildContext> context )
    {
        for ( BuildContext buildContext : context )
        {
            if ( buildContext.isBuildFresh() )
            {
                // clean working directory
                cleanWorkingDirectory( buildContext );
            }
        }
    }

    private void cleanWorkingDirectory( BuildContext context )
    {
        
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
