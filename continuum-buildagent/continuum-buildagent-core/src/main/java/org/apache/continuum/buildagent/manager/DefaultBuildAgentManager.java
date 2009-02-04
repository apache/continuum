package org.apache.continuum.buildagent.manager;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.continuum.buildagent.buildcontext.BuildContext;
import org.apache.continuum.buildagent.configuration.BuildAgentConfigurationService;
import org.apache.continuum.buildagent.taskqueue.manager.BuildAgentTaskQueueManager;
import org.apache.continuum.buildagent.utils.BuildContextToBuildDefinition;
import org.apache.continuum.buildagent.utils.BuildContextToProject;
import org.apache.continuum.buildagent.utils.ContinuumBuildAgentUtil;
import org.apache.continuum.distributed.transport.master.MasterBuildAgentTransportClient;
import org.apache.continuum.taskqueue.BuildProjectTask;
import org.apache.continuum.taskqueue.manager.TaskQueueManagerException;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.scm.ChangeFile;
import org.apache.maven.continuum.model.scm.ChangeSet;
import org.apache.maven.continuum.model.scm.ScmResult;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.codehaus.plexus.action.ActionManager;
import org.codehaus.plexus.action.ActionNotFoundException;
import org.codehaus.plexus.taskqueue.TaskQueueException;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @plexus.component role="org.apache.continuum.buildagent.manager.BuildAgentManager" role-hint="default"
 */
public class DefaultBuildAgentManager
    implements BuildAgentManager
{
    private Logger log = LoggerFactory.getLogger( this.getClass() );

    /**
     * @plexus.requirement
     */
    private ActionManager actionManager;

    /**
     * @plexus.requirement
     */
    private BuildAgentConfigurationService buildAgentConfigurationService;

    /**
     * @plexus.requirement
     */
    private BuildAgentTaskQueueManager buildAgentTaskQueueManager;

    public void prepareBuildProjects( List<BuildContext> buildContexts)
        throws ContinuumException
    {
        Map<String, Object> context = null;

        if ( buildContexts != null && buildContexts.size() > 0 )
        {
            try
            {
                for ( BuildContext buildContext : buildContexts )
                {
                    BuildDefinition buildDef = BuildContextToBuildDefinition.getBuildDefinition( buildContext );
        
                    log.info( "Check scm root state" );
                    if ( !checkProjectScmRoot( context ) )
                    {
                        break;
                    }
                    
                    log.info( "Starting prepare build" );
                    startPrepareBuild( buildContext );
                    
                    log.info( "Initializing prepare build" );
                    initializeActionContext( buildContext );
                    
                    try
                    {
                        if ( buildDef.isBuildFresh() )
                        {
                            log.info( "Clean up working directory" );
                            cleanWorkingDirectory( buildContext );
                        }
            
                        log.info( "Updating working directory" );
                        updateWorkingDirectory( buildContext );

                        log.info( "Merging SCM results" );
                        //CONTINUUM-1393
                        if ( !buildDef.isBuildFresh() )
                        {
                            mergeScmResults( buildContext );
                        }    
                    }
                    finally
                    {
                        endProjectPrepareBuild( buildContext );
                        context = buildContext.getActionContext();
                    }
                }
            }
            finally
            {
                endPrepareBuild( context );
            }
        }
        else
        {
            throw new ContinuumException( "No project build context" );
        }
    }

    public void startProjectBuild( int projectId )
        throws ContinuumException
    {
        try
        {
            MasterBuildAgentTransportClient client = new MasterBuildAgentTransportClient(
                new URL( buildAgentConfigurationService.getContinuumServerUrl() ) );
            client.startProjectBuild( projectId );
        }
        catch ( MalformedURLException e )
        {
            log.error( "Invalid continuum server URL '" + buildAgentConfigurationService.getContinuumServerUrl() + "'" );
            throw new ContinuumException( "Invalid continuum server URL '" + buildAgentConfigurationService.getContinuumServerUrl() + "'" );
        }
        catch ( Exception e )
        {
            log.error( "Error starting project build", e );
            throw new ContinuumException( "Error starting project build", e );
        }
    }

    public void returnBuildResult( Map buildResult )
        throws ContinuumException
    {
        try
        {
            MasterBuildAgentTransportClient client = new MasterBuildAgentTransportClient(
                new URL( buildAgentConfigurationService.getContinuumServerUrl() ) );
            client.returnBuildResult( buildResult );
        }
        catch ( MalformedURLException e )
        {
            log.error( "Invalid continuum server URL '" + buildAgentConfigurationService.getContinuumServerUrl() + "'" );
            throw new ContinuumException( "Invalid continuum server URL '" + buildAgentConfigurationService.getContinuumServerUrl() + "'" );
        }
        catch ( Exception e )
        {
            log.error( "Error while returning build result to the continuum server", e );
            throw new ContinuumException( e.getMessage(), e );
        }
    }

    public Map<String, String> getEnvironments( int buildDefinitionId, String installationType )
        throws ContinuumException
    {
        try
        {
            MasterBuildAgentTransportClient client = new MasterBuildAgentTransportClient(
                new URL( buildAgentConfigurationService.getContinuumServerUrl() ) );
            return client.getEnvironments( buildDefinitionId, installationType );
        }
        catch ( MalformedURLException e )
        {
            log.error( "Invalid continuum server URL '" + buildAgentConfigurationService.getContinuumServerUrl() + "'" );
            throw new ContinuumException( "Invalid continuum server URL '" + buildAgentConfigurationService.getContinuumServerUrl() + "'" );
        }
        catch ( Exception e )
        {
            log.error( "Error while retrieving environments for build definition " + buildDefinitionId, e );
            throw new ContinuumException( e.getMessage(), e );
        }
    }

    public void updateProject( Map project )
        throws ContinuumException
    {
        try
        {
            MasterBuildAgentTransportClient client = new MasterBuildAgentTransportClient(
                new URL( buildAgentConfigurationService.getContinuumServerUrl() ) );
            client.updateProject( project );
        }
        catch ( MalformedURLException e )
        {
            log.error( "Invalid continuum server URL '" + buildAgentConfigurationService.getContinuumServerUrl() + "'" );
            throw new ContinuumException( "Invalid continuum server URL '" + buildAgentConfigurationService.getContinuumServerUrl() + "'" );
        }
        catch ( Exception e )
        {
            log.error( "Error while updating project", e );
            throw new ContinuumException( e.getMessage(), e );
        }
    }

    private void startPrepareBuild( BuildContext buildContext )
        throws ContinuumException
    {
        Map<String, Object> actionContext = buildContext.getActionContext();
        
        if ( actionContext == null || !( ContinuumBuildAgentUtil.getScmRootState( actionContext ) == ContinuumProjectState.UPDATING ) ) 
        {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put( ContinuumBuildAgentUtil.KEY_PROJECT_GROUP_ID, new Integer( buildContext.getProjectGroupId() ) );
            map.put( ContinuumBuildAgentUtil.KEY_SCM_ROOT_ADDRESS, buildContext.getScmRootAddress() );
            
            try
            {
                MasterBuildAgentTransportClient client = new MasterBuildAgentTransportClient(
                    new URL( buildAgentConfigurationService.getContinuumServerUrl() ) );
                client.startPrepareBuild( map );
            }
            catch ( MalformedURLException e )
            {
                log.error( "Invalid continuum server URL '" + buildAgentConfigurationService.getContinuumServerUrl() + "'" );
                throw new ContinuumException( "Invalid continuum server URL '" + buildAgentConfigurationService.getContinuumServerUrl() + "'", e );
            }
            catch ( Exception e )
            {
                log.error( "Error starting prepare build", e );
                throw new ContinuumException( "Error starting prepare build", e );
            }
        }
    }

    private void initializeActionContext( BuildContext buildContext )
    {
        Map<String, Object> actionContext = new HashMap<String, Object>();

        actionContext.put( ContinuumBuildAgentUtil.KEY_PROJECT_ID, buildContext.getProjectId() );
        actionContext.put( ContinuumBuildAgentUtil.KEY_PROJECT, BuildContextToProject.getProject( buildContext ) );
        actionContext.put( ContinuumBuildAgentUtil.KEY_BUILD_DEFINITION, BuildContextToBuildDefinition.getBuildDefinition( buildContext ) );
        actionContext.put( ContinuumBuildAgentUtil.KEY_SCM_ROOT_STATE, ContinuumProjectState.UPDATING );
        actionContext.put( ContinuumBuildAgentUtil.KEY_PROJECT_GROUP_ID, buildContext.getProjectGroupId() );
        actionContext.put( ContinuumBuildAgentUtil.KEY_SCM_ROOT_ADDRESS, buildContext.getScmRootAddress() );
        actionContext.put( ContinuumBuildAgentUtil.KEY_OLD_SCM_RESULT, buildContext.getOldScmResult() );

        buildContext.setActionContext( actionContext );
    }

    private boolean checkProjectScmRoot( Map context )
    {
        if ( context != null && ContinuumBuildAgentUtil.getScmRootState( context ) == ContinuumProjectState.ERROR )
        {
            return false;
        }

        return true;
    }

    private void cleanWorkingDirectory( BuildContext buildContext )
        throws ContinuumException
    {
        performAction( "clean-agent-work-directory", buildContext );
    }

    private void updateWorkingDirectory( BuildContext buildContext )
        throws ContinuumException
    {
        Map<String, Object> actionContext = buildContext.getActionContext();

        performAction( "check-agent-working-directory", buildContext );
        
        boolean workingDirectoryExists =
            ContinuumBuildAgentUtil.getBoolean( actionContext, ContinuumBuildAgentUtil.KEY_WORKING_DIRECTORY_EXISTS );
    
        ScmResult scmResult;
    
        if ( workingDirectoryExists )
        {
            performAction( "update-agent-working-directory", buildContext );
    
            scmResult = ContinuumBuildAgentUtil.getUpdateScmResult( actionContext, null );
        }
        else
        {
            Project project = ContinuumBuildAgentUtil.getProject( actionContext );
    
            actionContext.put( ContinuumBuildAgentUtil.KEY_WORKING_DIRECTORY,
                               buildAgentConfigurationService.getWorkingDirectory( project.getId() ).getAbsolutePath() );
    
            performAction( "checkout-agent-project", buildContext );
    
            scmResult = ContinuumBuildAgentUtil.getCheckoutScmResult( actionContext, null );
        }
    
        buildContext.setScmResult( scmResult );
        actionContext.put( ContinuumBuildAgentUtil.KEY_SCM_RESULT, scmResult );
    }

    private void endProjectPrepareBuild( BuildContext buildContext )
        throws ContinuumException
    {
        Map<String, Object> context = buildContext.getActionContext();

        ScmResult scmResult = ContinuumBuildAgentUtil.getScmResult( context, null );
        Project project = ContinuumBuildAgentUtil.getProject( context );

        if ( scmResult == null || !scmResult.isSuccess() )
        {
            context.put( ContinuumBuildAgentUtil.KEY_SCM_ROOT_STATE, ContinuumProjectState.ERROR );
        }
        else
        {
            buildContext.setScmResult( scmResult );
        }

        // connect to continuum server (master)
        try
        {
            MasterBuildAgentTransportClient client = new MasterBuildAgentTransportClient(
                 new URL( buildAgentConfigurationService.getContinuumServerUrl() ) );
            client.returnScmResult( createScmResult( buildContext ) );
        }
        catch ( MalformedURLException e )
        {
            throw new ContinuumException( "Invalid Continuum Server URL '" + buildAgentConfigurationService.getContinuumServerUrl() + "'" );
        }
        catch ( Exception e )
        {
            throw new ContinuumException( "Error while returning scm result to the continuum server", e );
        }
    }

    private void endPrepareBuild( Map context )
        throws ContinuumException
    {
        if ( context != null )
        {
            Map<String, Object> result = new HashMap<String, Object>();
            result.put( ContinuumBuildAgentUtil.KEY_PROJECT_GROUP_ID, new Integer( ContinuumBuildAgentUtil.getProjectGroupId( context ) ) );
            result.put( ContinuumBuildAgentUtil.KEY_SCM_ROOT_ADDRESS, ContinuumBuildAgentUtil.getScmRootAddress( context ) );
            result.put( ContinuumBuildAgentUtil.KEY_SCM_ROOT_STATE, new Integer( ContinuumBuildAgentUtil.getScmRootState( context ) ) );
            
            if ( ContinuumBuildAgentUtil.getScmRootState( context ) == ContinuumProjectState.ERROR )
            {
                String error = convertScmResultToError( ContinuumBuildAgentUtil.getScmResult( context, null ) );
                
                if ( StringUtils.isEmpty( error ) )
                {
                    result.put( ContinuumBuildAgentUtil.KEY_SCM_ERROR, "" );
                }
                else
                {
                    result.put( ContinuumBuildAgentUtil.KEY_SCM_ERROR, error );
                }
            }
            else
            {
                result.put( ContinuumBuildAgentUtil.KEY_SCM_ERROR, "" );
            }

            // connect to continuum server (master)
            try
            {
                MasterBuildAgentTransportClient client = new MasterBuildAgentTransportClient(
                     new URL( buildAgentConfigurationService.getContinuumServerUrl() ) );
                client.prepareBuildFinished( result );
            }
            catch ( MalformedURLException e )
            {
                throw new ContinuumException( "Invalid Continuum Server URL '" + buildAgentConfigurationService.getContinuumServerUrl() + "'" );
            }
            catch ( Exception e )
            {
                throw new ContinuumException( "Error while finishing prepare build", e );
            }
        }
        else
        {
            throw new ContinuumException( "No project build context" );
        }
    }

    private Map<String, Object> createScmResult( BuildContext buildContext )
    {
        Map<String, Object> result = new HashMap<String, Object>();
        ScmResult scmResult = buildContext.getScmResult();

        result.put( ContinuumBuildAgentUtil.KEY_PROJECT_ID, new Integer( buildContext.getProjectId() ) );
        if ( StringUtils.isEmpty( scmResult.getCommandLine() ) )
        {
            result.put( ContinuumBuildAgentUtil.KEY_SCM_COMMAND_LINE, "" );
        }
        else
        {
            result.put( ContinuumBuildAgentUtil.KEY_SCM_COMMAND_LINE, scmResult.getCommandLine() );
        }
        if ( StringUtils.isEmpty( scmResult.getCommandOutput() ) )
        {
            result.put( ContinuumBuildAgentUtil.KEY_SCM_COMMAND_OUTPUT, "" );
        }
        else
        {
            result.put( ContinuumBuildAgentUtil.KEY_SCM_COMMAND_OUTPUT, scmResult.getCommandOutput() );
        }
        if ( StringUtils.isEmpty( scmResult.getProviderMessage() ) )
        {
            result.put( ContinuumBuildAgentUtil.KEY_SCM_PROVIDER_MESSAGE, "" );
        }
        else
        {
            result.put( ContinuumBuildAgentUtil.KEY_SCM_PROVIDER_MESSAGE, scmResult.getProviderMessage() );
        }
        if ( StringUtils.isEmpty( scmResult.getException() ) )
        {
            result.put( ContinuumBuildAgentUtil.KEY_SCM_EXCEPTION, "" );
        }
        else
        {
            result.put( ContinuumBuildAgentUtil.KEY_SCM_EXCEPTION, scmResult.getException() );
        }
        result.put( ContinuumBuildAgentUtil.KEY_SCM_SUCCESS, new Boolean( scmResult.isSuccess() ) );
        result.put( ContinuumBuildAgentUtil.KEY_SCM_CHANGES, getScmChanges( scmResult ) );

        return result;
    }

    private String convertScmResultToError( ScmResult result )
    {
        String error = "";

        if ( result == null )
        {
            error = "Scm result is null.";
        }
        else
        {
            if ( result.getCommandLine() != null )
            {
                error = "Command line: " + StringUtils.clean( result.getCommandLine() ) +
                    System.getProperty( "line.separator" );
            }

            if ( result.getProviderMessage() != null )
            {
                error = "Provider message: " + StringUtils.clean( result.getProviderMessage() ) +
                    System.getProperty( "line.separator" );
            }

            if ( result.getCommandOutput() != null )
            {
                error += "Command output: " + System.getProperty( "line.separator" );
                error += "-------------------------------------------------------------------------------" +
                    System.getProperty( "line.separator" );
                error += StringUtils.clean( result.getCommandOutput() ) + System.getProperty( "line.separator" );
                error += "-------------------------------------------------------------------------------" +
                    System.getProperty( "line.separator" );
            }

            if ( result.getException() != null )
            {
                error += "Exception:" + System.getProperty( "line.separator" );
                error += result.getException();
            }
        }

        return error;
    }

    private void performAction( String actionName, BuildContext buildContext )
        throws ContinuumException
    {
        ContinuumException exception = null;
    
        try
        {
            log.info( "Performing action " + actionName );
            actionManager.lookup( actionName ).execute( buildContext.getActionContext() );
            return;
        }
        catch ( ActionNotFoundException e )
        {
            exception = new ContinuumException( "Error looking up action '" + actionName + "'", e );
        }
        catch ( Exception e )
        {
            exception = new ContinuumException( "Error executing action '" + actionName + "'", e );
        }
        
        ScmResult result = new ScmResult();
        
        result.setSuccess( false );
        
        result.setException( ContinuumBuildAgentUtil.throwableToString( exception ) );

        buildContext.setScmResult( result );
        buildContext.getActionContext().put( ContinuumBuildAgentUtil.KEY_UPDATE_SCM_RESULT, result );
        
        throw exception;
    }
    
    public void buildProjects( List<BuildContext> buildContexts )
        throws ContinuumException
    {
        for ( BuildContext buildContext : buildContexts )
        {
            // only build if it's forced build or project state is not OK
            if ( buildContext.getTrigger() == ContinuumProjectState.TRIGGER_FORCED || 
                 buildContext.getProjectState() != ContinuumProjectState.OK )
            {
                BuildProjectTask buildProjectTask = new BuildProjectTask( buildContext.getProjectId(),
                                                                          buildContext.getBuildDefinitionId(),
                                                                          buildContext.getTrigger(),
                                                                          buildContext.getProjectName(),
                                                                          "" );
                try
                {
                    buildAgentTaskQueueManager.getBuildQueue().put( buildProjectTask );
                }
                catch ( TaskQueueException e )
                {
                    log.error( "Error while enqueing build task for project " + buildContext.getProjectId(), e );
                    throw new ContinuumException( "Error while enqueuing build task for project " + buildContext.getProjectId(), e );
                }
            }
        }

        try
        {
            boolean stop = false;
            while ( !stop )
            {
                if ( buildAgentTaskQueueManager.getCurrentProjectInBuilding() <= 0 && 
                                !buildAgentTaskQueueManager.hasBuildTaskInQueue()  )
                {
                    stop = true;
                }
            }
        }
        catch ( TaskQueueManagerException e )
        {
            throw new ContinuumException( e.getMessage(), e );
        }
    }

    private List<Map> getScmChanges( ScmResult scmResult )
    {
        List<Map> scmChanges = new ArrayList<Map>();

        List<ChangeSet> changes = scmResult.getChanges();

        if ( changes != null )
        {
            for ( ChangeSet cs : changes )
            {
                Map changeSet = new HashMap();

                if ( StringUtils.isNotEmpty( cs.getAuthor() ) )
                {
                    changeSet.put( ContinuumBuildAgentUtil.KEY_CHANGESET_AUTHOR, cs.getAuthor() );
                }
                else
                {
                    changeSet.put( ContinuumBuildAgentUtil.KEY_CHANGESET_AUTHOR, "" );
                }
                if ( StringUtils.isNotEmpty( cs.getComment() ) ) 
                {
                    changeSet.put( ContinuumBuildAgentUtil.KEY_CHANGESET_COMMENT, cs.getComment() );
                }
                else
                {
                    changeSet.put( ContinuumBuildAgentUtil.KEY_CHANGESET_COMMENT, "" );
                }
                changeSet.put( ContinuumBuildAgentUtil.KEY_CHANGESET_DATE, cs.getDateAsDate() );
                changeSet.put( ContinuumBuildAgentUtil.KEY_CHANGESET_FILES, getChangeFiles( cs.getFiles() ) );

                scmChanges.add( changeSet );
            }
        }

        return scmChanges;
    }

    private List getChangeFiles( List<ChangeFile> changeFiles )
    {
        List<Map> files = new ArrayList<Map>();

        if ( changeFiles != null )
        {
            for ( ChangeFile file : changeFiles )
            {
                Map changeFile = new HashMap();
                if ( StringUtils.isNotEmpty( file.getName() ) )
                {
                    changeFile.put( ContinuumBuildAgentUtil.KEY_CHANGEFILE_NAME, file.getName() );
                }
                else
                {
                    changeFile.put( ContinuumBuildAgentUtil.KEY_CHANGEFILE_NAME, "" );
                }
                if ( StringUtils.isNotEmpty( file.getRevision() ) )
                {
                    changeFile.put( ContinuumBuildAgentUtil.KEY_CHANGEFILE_REVISION, file.getRevision() );
                }
                else
                {
                    changeFile.put( ContinuumBuildAgentUtil.KEY_CHANGEFILE_REVISION, "" );
                }
                if ( StringUtils.isNotEmpty( file.getStatus() ) )
                {
                    changeFile.put( ContinuumBuildAgentUtil.KEY_CHANGEFILE_STATUS, file.getStatus() );
                }
                else
                {
                    changeFile.put( ContinuumBuildAgentUtil.KEY_CHANGEFILE_STATUS, "" );
                }

                files.add( changeFile );
            }
        }

        return files;
    }

    private void mergeScmResults( BuildContext buildContext )
    {
        Map context = buildContext.getActionContext();
        ScmResult oldScmResult = ContinuumBuildAgentUtil.getOldScmResult( context, null );
        ScmResult newScmResult = ContinuumBuildAgentUtil.getScmResult( context, null );

        if ( oldScmResult != null )
        {
            if ( newScmResult == null )
            {
                context.put( ContinuumBuildAgentUtil.KEY_SCM_RESULT, oldScmResult );
            }
            else
            {
                List<ChangeSet> oldChanges = oldScmResult.getChanges();

                List<ChangeSet> newChanges = newScmResult.getChanges();

                for ( ChangeSet change : newChanges )
                {
                    if ( !oldChanges.contains( change ) )
                    {
                        oldChanges.add( change );
                    }
                }

                newScmResult.setChanges( oldChanges );
            }
        }
    }
}
