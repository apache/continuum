package org.apache.continuum.buildagent;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.continuum.buildagent.buildcontext.BuildContext;
import org.apache.continuum.buildagent.buildcontext.manager.BuildContextManager;
import org.apache.continuum.buildagent.configuration.BuildAgentConfigurationService;
import org.apache.continuum.buildagent.manager.BuildAgentManager;
import org.apache.continuum.buildagent.model.Installation;
import org.apache.continuum.buildagent.taskqueue.manager.BuildAgentTaskQueueManager;
import org.apache.continuum.buildagent.utils.ContinuumBuildAgentUtil;
import org.apache.continuum.taskqueue.manager.TaskQueueManagerException;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @plexus.component role="org.apache.continuum.buildagent.ContinuumBuildAgentService"
 */
public class ContinuumBuildAgentServiceImpl
    implements ContinuumBuildAgentService
{
    private Logger log = LoggerFactory.getLogger( this.getClass() );

    /**
     * @plexus.requirement
     */
    private BuildAgentConfigurationService buildAgentConfigurationService;

    /**
     * @plexus.requirement
     */
    private BuildAgentManager buildAgentManager;

    /**
     * @plexus.requirement
     */
    private BuildAgentTaskQueueManager buildAgentTaskQueueManager;

    /**
     * @plexus.requirement
     */
    private BuildContextManager buildContextManager;

    public void buildProjects( List<Map> projectsBuildContext )
        throws ContinuumBuildAgentException
    {
        List<BuildContext> buildContextList = initializeBuildContext( projectsBuildContext );

        try
        {
            buildAgentManager.prepareBuildProjects( buildContextList );
        }
        catch ( ContinuumException e )
        {
            throw new ContinuumBuildAgentException( e.getMessage(), e );
        }
    }

    public List<Map> getAvailableInstallations()
        throws ContinuumBuildAgentException
    {
        List<Map> installationsList = new ArrayList<Map>();
        
        List<Installation> installations = buildAgentConfigurationService.getAvailableInstallations();

        for ( Installation installation : installations )
        {
            Map map = new HashMap();
            
            if ( StringUtils.isBlank( installation.getName() ) )
            {
                map.put( ContinuumBuildAgentUtil.KEY_INSTALLATION_NAME, "" );
            }
            else
            {
                map.put( ContinuumBuildAgentUtil.KEY_INSTALLATION_NAME, installation.getName() );
            }

            if ( StringUtils.isBlank( installation.getType() ) )
            {
                map.put( ContinuumBuildAgentUtil.KEY_INSTALLATION_TYPE, "" );
            }
            else
            {
                map.put( ContinuumBuildAgentUtil.KEY_INSTALLATION_TYPE, installation.getType() );
            }

            if ( StringUtils.isBlank( installation.getVarName() ) )
            {
                map.put( ContinuumBuildAgentUtil.KEY_INSTALLATION_VAR_NAME, "" );
            }
            else
            {
                map.put( ContinuumBuildAgentUtil.KEY_INSTALLATION_VAR_VALUE, installation.getVarValue() );
            }

            if ( StringUtils.isBlank( installation.getVarValue() ) )
            {
                map.put( ContinuumBuildAgentUtil.KEY_INSTALLATION_VAR_VALUE, "" );
            }
            else
            {
                map.put( ContinuumBuildAgentUtil.KEY_INSTALLATION_VAR_VALUE, installation.getVarValue() );
            }

            installationsList.add( map );
        }

        return installationsList;
    }

    public Map getBuildResult( int projectId )
        throws ContinuumBuildAgentException
    {
        Map result = new HashMap();

        if ( projectId == getProjectCurrentlyBuilding() )
        {
            BuildContext buildContext = buildContextManager.getBuildContext( projectId );
            
            result.put( ContinuumBuildAgentUtil.KEY_PROJECT_ID, new Integer( buildContext.getProjectId() ) );
            result.put( ContinuumBuildAgentUtil.KEY_BUILD_DEFINITION_ID, new Integer( buildContext.getBuildDefinitionId() ) );
            result.put( ContinuumBuildAgentUtil.KEY_TRIGGER, new Integer( buildContext.getTrigger() ) );
            
            BuildResult buildResult = buildContext.getBuildResult();

            if ( buildResult != null )
            {
                if ( buildResult.getStartTime() <= 0 )
                {
                    result.put( ContinuumBuildAgentUtil.KEY_BUILD_START, new Long( buildContext.getBuildStartTime() ).toString() );
                }
                else
                {
                    result.put( ContinuumBuildAgentUtil.KEY_BUILD_START, new Long( buildResult.getStartTime() ).toString() );
                }

                if ( buildResult.getError() == null )
                {
                    result.put( ContinuumBuildAgentUtil.KEY_BUILD_ERROR, "" );
                }
                else
                {
                    result.put( ContinuumBuildAgentUtil.KEY_BUILD_ERROR, buildResult.getError() );
                }

                result.put( ContinuumBuildAgentUtil.KEY_BUILD_STATE, new Integer( buildResult.getState() ) );
                result.put( ContinuumBuildAgentUtil.KEY_BUILD_END, new Long( buildResult.getEndTime() ).toString() );
                result.put( ContinuumBuildAgentUtil.KEY_BUILD_EXIT_CODE, buildResult.getExitCode() );
            }
            else
            {
                result.put( ContinuumBuildAgentUtil.KEY_BUILD_START, new Long( buildContext.getBuildStartTime() ).toString() );
                result.put( ContinuumBuildAgentUtil.KEY_BUILD_END, new Long( 0 ).toString() );
                result.put( ContinuumBuildAgentUtil.KEY_BUILD_STATE, new Integer( ContinuumProjectState.BUILDING ) );
                result.put( ContinuumBuildAgentUtil.KEY_BUILD_ERROR, "" );
                result.put( ContinuumBuildAgentUtil.KEY_BUILD_EXIT_CODE, new Integer( 0 ) );
            }
            
            String buildOutput = getBuildOutputText( projectId );
            if ( buildOutput == null )
            {
                result.put( ContinuumBuildAgentUtil.KEY_BUILD_OUTPUT, "" );
            }
            else
            {
                result.put( ContinuumBuildAgentUtil.KEY_BUILD_OUTPUT, buildOutput );
            }
        }
        return result;
    }

    public int getProjectCurrentlyBuilding()
        throws ContinuumBuildAgentException
    {
        try
        {
            return buildAgentTaskQueueManager.getCurrentProjectInBuilding();
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
            buildAgentTaskQueueManager.cancelBuild();
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
            context.setProjectState( ContinuumBuildAgentUtil.getProjectState( map ) );
            context.setTrigger( ContinuumBuildAgentUtil.getTrigger( map ) );
            context.setLocalRepository( ContinuumBuildAgentUtil.getLocalRepository( map ) );

            buildContext.add( context );
        }

        buildContextManager.setBuildContextList( buildContext );

        return buildContext;
    }

    private String getBuildOutputText( int projectId )
    {
        try
        {
            File buildOutputFile = buildAgentConfigurationService.getBuildOutputFile( projectId );
        
            if ( buildOutputFile.exists() )
            {
                return StringEscapeUtils.escapeHtml( FileUtils.fileRead( buildOutputFile ) );
            }
        }
        catch ( Exception e )
        {
            // do not throw exception, just log it
            log.error( "Error retrieving build output file", e );
        }

        return null;
    }
}
