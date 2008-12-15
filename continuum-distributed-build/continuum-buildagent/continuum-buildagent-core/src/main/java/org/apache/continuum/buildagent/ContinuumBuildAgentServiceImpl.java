package org.apache.continuum.buildagent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.continuum.buildagent.buildcontext.BuildContext;
import org.apache.continuum.buildagent.configuration.ConfigurationService;
import org.apache.continuum.buildagent.model.Installation;
import org.apache.continuum.buildagent.taskqueue.manager.TaskQueueManager;
import org.apache.continuum.buildagent.utils.ContinuumBuildAgentUtil;
import org.apache.continuum.taskqueue.manager.TaskQueueManagerException;
import org.apache.maven.continuum.ContinuumException;

public class ContinuumBuildAgentServiceImpl
    implements ContinuumBuildAgentService
{
    /**
     * @plexus.requirement
     */
    private ConfigurationService configurationService;

    /**
     * @plexus.requirement
     */
    private Continuum continuum;

    /**
     * @plexus.requirement
     */
    private TaskQueueManager taskQueueManager;

    public void buildProjects( List<Map> projectsBuildContext )
        throws ContinuumBuildAgentException
    {
        List<BuildContext> buildContextList = initializeBuildContext( projectsBuildContext );

        try
        {
            continuum.prepareBuildProjects( buildContextList );
        }
        catch ( ContinuumException e )
        {
            throw new ContinuumBuildAgentException( e.getMessage(), e );
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
    {
        return false;
    }

    public int getProjectCurrentlyBuilding()
        throws ContinuumBuildAgentException
    {
        try
        {
            return taskQueueManager.getCurrentProjectInBuilding();
        }
        catch ( TaskQueueManagerException e )
        {
            throw new ContinuumBuildAgentException( e.getMessage(), e );
        }
    }

    public void cancelBuild()
        throws ContinuumBuildAgentException
    {
        try
        {
            taskQueueManager.cancelBuild();
        }
        catch ( TaskQueueManagerException e )
        {
            throw new ContinuumBuildAgentException( e.getMessage(), e );
        }
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
            context.setProjectName( ContinuumBuildAgentUtil.getProjectName( map ) );
            
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
    
    public Continuum getContinuum()
    {
        return continuum;
    }

    public void setContinuum( Continuum continuum )
    {
        this.continuum = continuum;
    }

    public TaskQueueManager getTaskQueueManager()
    {
        return taskQueueManager;
    }

    public void setTaskQueueManager( TaskQueueManager taskQueueManager )
    {
        this.taskQueueManager = taskQueueManager;
    }
}
