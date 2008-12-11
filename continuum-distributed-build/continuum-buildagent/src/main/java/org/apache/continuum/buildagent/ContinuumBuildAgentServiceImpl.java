package org.apache.continuum.buildagent;

/* TODO: 
 * CHeckout the project
 *   
 */

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.continuum.buildagent.buildcontext.manager.BuildContextManager;
import org.apache.continuum.buildagent.configuration.ConfigurationService;
import org.apache.continuum.buildagent.model.BuildContext;
import org.apache.continuum.buildagent.model.Installation;
import org.apache.continuum.buildagent.util.BuildContextToProject;
import org.apache.continuum.dao.BuildResultDao;
import org.apache.continuum.dao.ProjectDao;
import org.apache.continuum.buildagent.continuum.Continuum;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.store.ContinuumObjectNotFoundException;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;

public class ContinuumBuildAgentServiceImpl
    extends AbstractContinuumBuildAgentService
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
    private BuildContextManager buildContextManager;

    public void buildProjects( List<Map> projectsBuildContext )
        throws ContinuumBuildAgentException
    {
        List<BuildContext> buildContextList = initializeBuildContext( projectsBuildContext );
        
        buildContextManager.setBuildContextList( buildContextList );
      
        try
        {

            for ( BuildContext buildContext : buildContextList )
            {
                Project project = BuildContextToProject.getProject( buildContext );
                continuum.buildProject( project.getId(), buildContext.getBuildDefinitionId(),
                                        ContinuumProjectState.TRIGGER_FORCED );
            }
        }
        catch ( Exception e )
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
        return null;
    }

    public boolean isBusy()
        throws ContinuumBuildAgentException
    {
        try
        {
            return continuum.getTaskQueueManager().buildInProgress();
        }
        catch ( Exception e )
        {
            throw new ContinuumBuildAgentException( e.getMessage(), e );
        }
    }

    public int getProjectCurrentlyBuilding()
    {
     
        try
        {
            return continuum.getTaskQueueManager().getCurrentProjectIdBuilding();
        }
        catch ( Exception e )
        {
            return -1;
        }
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

            buildContext.add( context );
        }

        return buildContext;
    }    
}
