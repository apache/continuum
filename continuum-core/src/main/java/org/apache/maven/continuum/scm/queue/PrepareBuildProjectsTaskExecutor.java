package org.apache.maven.continuum.scm.queue;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.continuum.dao.BuildDefinitionDao;
import org.apache.continuum.dao.ProjectDao;
import org.apache.continuum.dao.ProjectScmRootDao;
import org.apache.continuum.model.project.ProjectScmRoot;
import org.apache.maven.continuum.core.action.AbstractContinuumAction;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.scm.ChangeSet;
import org.apache.maven.continuum.model.scm.ScmResult;
import org.apache.maven.continuum.notification.ContinuumNotificationDispatcher;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.apache.maven.continuum.utils.ContinuumUtils;
import org.apache.maven.continuum.utils.ProjectSorter;
import org.apache.maven.continuum.utils.WorkingDirectoryService;
import org.codehaus.plexus.action.ActionManager;
import org.codehaus.plexus.action.ActionNotFoundException;
import org.codehaus.plexus.taskqueue.Task;
import org.codehaus.plexus.taskqueue.execution.TaskExecutionException;
import org.codehaus.plexus.taskqueue.execution.TaskExecutor;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.dag.CycleDetectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:ctan@apache.org">Maria Catherine Tan</a>
 * @version $Id$
 * @plexus.component role="org.codehaus.plexus.taskqueue.execution.TaskExecutor"
 * role-hint="prepare-build-project"
 */
public class PrepareBuildProjectsTaskExecutor
    implements TaskExecutor
{
    private Logger log = LoggerFactory.getLogger( PrepareBuildProjectsTaskExecutor.class );

    /**
     * @plexus.requirement
     */
    private ActionManager actionManager;

    /**
     * @plexus.requirement
     */
    private ProjectDao projectDao;
    
    /**
     * @plexus.requirement
     */
    private BuildDefinitionDao buildDefinitionDao;
    
    /**
     * @plexus.requirement
     */
    private ProjectScmRootDao projectScmRootDao;
    
    /**
     * @plexus.requirement
     */
    private WorkingDirectoryService workingDirectoryService;

    /**
     * @plexus.requirement
     */
    private ContinuumNotificationDispatcher notifierDispatcher;

    public void executeTask( Task task )
        throws TaskExecutionException
    {
        PrepareBuildProjectsTask prepareTask = (PrepareBuildProjectsTask) task;
        
        Map<Integer, Integer> projectsBuildDefinitionsMap = prepareTask.getProjectsBuildDefinitionsMap();
        int trigger = prepareTask.getTrigger();
        Set<Integer> projectsId = projectsBuildDefinitionsMap.keySet();
        Map context = new HashMap();

        try
        {
            for ( Integer projectId : projectsId )
            {
                int buildDefinitionId = projectsBuildDefinitionsMap.get( projectId );
                
                log.info( "Initializing prepare build" );
                context = initializeContext( projectId, buildDefinitionId );

                if ( !checkProjectScmRoot( context ) )
                {
                    break;
                }

                log.info( "Starting prepare build of project: " + AbstractContinuumAction.getProject( context ).getName() );
                startPrepareBuild( context );

                try
                {
                    if ( AbstractContinuumAction.getBuildDefinition( context ).isBuildFresh() )
                    {
                        log.info( "Purging existing working copy" );
                        cleanWorkingDirectory( context );
                    }

                    // ----------------------------------------------------------------------
                    // TODO: Centralize the error handling from the SCM related actions.
                    // ContinuumScmResult should return a ContinuumScmResult from all
                    // methods, even in a case of failure.
                    // ----------------------------------------------------------------------
                    log.info( "Updating working dir" );
                    updateWorkingDirectory( context );

                    log.info( "Merging SCM results" );
                    //CONTINUUM-1393
                    if ( !AbstractContinuumAction.getBuildDefinition( context ).isBuildFresh() )
                    {
                        mergeScmResults( context );
                    }
                }
                finally
                {
                    log.info( "Ending prepare build of project: " + AbstractContinuumAction.getProject( context).getName() );
                    endProjectPrepareBuild( context );
                }
            }
        }
        finally
        {
            log.info( "Ending prepare build" );
            endPrepareBuild( context );
        }

        if ( checkProjectScmRoot( context ) )
        {
            int projectGroupId = AbstractContinuumAction.getProjectGroupId( context );
            buildProjects( projectGroupId, projectsBuildDefinitionsMap, trigger );
        }
    }

    private Map initializeContext( int projectId, int buildDefinitionId )
        throws TaskExecutionException
    {
        Map context = new HashMap();

        try
        {
            Project project = projectDao.getProjectWithScmDetails( projectId );
            ProjectGroup projectGroup = project.getProjectGroup();
            
            List<ProjectScmRoot> scmRoots = projectScmRootDao.getProjectScmRootByProjectGroup( projectGroup.getId() );
            String projectScmUrl = project.getScmUrl();
            
            for ( ProjectScmRoot projectScmRoot : scmRoots )
            {
                if ( projectScmUrl.contains( projectScmRoot.getScmRootAddress() ) )
                {
                    context.put( AbstractContinuumAction.KEY_PROJECT_SCM_ROOT, projectScmRoot );
                    break;
                }
            }

            context.put( AbstractContinuumAction.KEY_PROJECT_GROUP_ID, projectGroup.getId() );
            context.put( AbstractContinuumAction.KEY_PROJECT_ID, projectId );
            context.put( AbstractContinuumAction.KEY_PROJECT, project );
    
            context.put( AbstractContinuumAction.KEY_BUILD_DEFINITION_ID, buildDefinitionId );
            context.put( AbstractContinuumAction.KEY_BUILD_DEFINITION, buildDefinitionDao.getBuildDefinition( buildDefinitionId ) );
            
            context.put( AbstractContinuumAction.KEY_OLD_SCM_RESULT, project.getScmResult() );
        }
        catch ( ContinuumStoreException e )
        {
            throw new TaskExecutionException( "Error initializing pre-build context", e );
        }
        
        return context;
    }
    
    private void cleanWorkingDirectory( Map context )
        throws TaskExecutionException
    {
        performAction( "clean-working-directory", context );
    }
    
    private void updateWorkingDirectory( Map context )
        throws TaskExecutionException
    {
        performAction( "check-working-directory", context );
    
        boolean workingDirectoryExists =
            AbstractContinuumAction.getBoolean( context, AbstractContinuumAction.KEY_WORKING_DIRECTORY_EXISTS );
    
        ScmResult scmResult;
    
        if ( workingDirectoryExists )
        {
            performAction( "update-working-directory-from-scm", context );
    
            scmResult = AbstractContinuumAction.getUpdateScmResult( context, null );
        }
        else
        {
            Project project = AbstractContinuumAction.getProject( context );
    
            context.put( AbstractContinuumAction.KEY_WORKING_DIRECTORY,
                               workingDirectoryService.getWorkingDirectory( project ).getAbsolutePath() );
    
            performAction( "checkout-project", context );
    
            scmResult = AbstractContinuumAction.getCheckoutResult( context, null );
        }
    
        context.put( AbstractContinuumAction.KEY_SCM_RESULT, scmResult );
    }
    
    private boolean checkProjectScmRoot( Map context )
        throws TaskExecutionException
    {
        ProjectScmRoot projectScmRoot = AbstractContinuumAction.getProjectScmRoot( context );
        
        // check state of scm root
        if ( projectScmRoot.getState() == ContinuumProjectState.ERROR )
        {
            return false;
        }
        
        return true;
    }
    
    private void startPrepareBuild( Map context )
        throws TaskExecutionException
    {
        ProjectScmRoot projectScmRoot = AbstractContinuumAction.getProjectScmRoot( context );
        if ( projectScmRoot.getState() != ContinuumProjectState.UPDATING )
        {
            try
            {
                projectScmRoot.setOldState( projectScmRoot.getState() );
                projectScmRoot.setState( ContinuumProjectState.UPDATING );
                projectScmRootDao.updateProjectScmRoot( projectScmRoot );
            }
            catch ( ContinuumStoreException e )
            {
                throw new TaskExecutionException( "Error persisting projectScmRoot", e );
            }
        }
    }
    
    private void endPrepareBuild( Map context )
        throws TaskExecutionException
    {
        ProjectScmRoot projectScmRoot = AbstractContinuumAction.getProjectScmRoot( context );
        
        if ( projectScmRoot.getState() != ContinuumProjectState.ERROR )
        {
            projectScmRoot.setState( ContinuumProjectState.UPDATED );
            projectScmRoot.setError( null );
            
            try
            {
                projectScmRootDao.updateProjectScmRoot( projectScmRoot );
            }
            catch ( ContinuumStoreException e )
            {
                throw new TaskExecutionException( "Error persisting projectScmRoot", e );
            }
        }

        notifierDispatcher.prepareBuildComplete( projectScmRoot );
    }
    
    /**
     *  @param context
     * @throws TaskExecutionException
     */
    private void endProjectPrepareBuild( Map context )
        throws TaskExecutionException
    {
        ScmResult scmResult = AbstractContinuumAction.getScmResult( context, null );
        Project project = AbstractContinuumAction.getProject( context );
        
        if ( scmResult == null || !scmResult.isSuccess() )
        {
            String error = convertScmResultToError( scmResult );
            
            updateProjectScmRoot( context, error );
        }
        
        try
        {
            project.setScmResult( scmResult );

            projectDao.updateProject( project );
        }
        catch ( ContinuumStoreException e )
        {
            throw new TaskExecutionException( "Error storing the project", e );
        }
    }
    
    /**
     * Merges scm results so we'll have all changes since last execution of current build definition
     *
     * @param context The build context
     */
    private void mergeScmResults( Map context )
    {
        ScmResult oldScmResult = AbstractContinuumAction.getOldScmResult( context, null );
        ScmResult newScmResult = AbstractContinuumAction.getScmResult( context, null );

        if ( oldScmResult != null )
        {
            if ( newScmResult == null )
            {
                context.put( AbstractContinuumAction.KEY_SCM_RESULT, oldScmResult );
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
    
    private void performAction( String actionName, Map context )
        throws TaskExecutionException
    {
        TaskExecutionException exception = null;

        try
        {
            log.info( "Performing action " + actionName );
            actionManager.lookup( actionName ).execute( context );
            return;
        }
        catch ( ActionNotFoundException e )
        {
            exception = new TaskExecutionException( "Error looking up action '" + actionName + "'", e );
        }
        catch ( Exception e )
        {
            exception = new TaskExecutionException( "Error executing action '" + actionName + "'", e );
        }
        
        ScmResult result = new ScmResult();
        
        result.setSuccess( false );
        
        result.setException( ContinuumUtils.throwableToString( exception ) );
        
        context.put( AbstractContinuumAction.KEY_SCM_RESULT, result );
        
        throw exception;
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
    
    private void updateProjectScmRoot( Map context, String error )
        throws TaskExecutionException
    {
        ProjectScmRoot projectScmRoot = AbstractContinuumAction.getProjectScmRoot( context );
        
        try
        {
            projectScmRoot.setState( ContinuumProjectState.ERROR );
            projectScmRoot.setError( error );

            projectScmRootDao.updateProjectScmRoot( projectScmRoot );
            
            context.put( AbstractContinuumAction.KEY_PROJECT_SCM_ROOT, projectScmRoot );
        }
        catch ( ContinuumStoreException e )
        {
            throw new TaskExecutionException( "Error storing project scm root", e );
        }
    }

    private void buildProjects( int projectGroupId, Map<Integer, Integer> projectsAndBuildDefinitionsMap, int trigger )
        throws TaskExecutionException
    {
        List<Project> projects = projectDao.getProjectsWithDependenciesByGroupId( projectGroupId );
        List<Project> projectList;
        
        try
        {
            projectList = ProjectSorter.getSortedProjects( projects, log );
        }
        catch ( CycleDetectedException e )
        {
            projectList = projectDao.getAllProjectsByName();
        }

        List<Project> projectsToBeBuilt = new ArrayList<Project>();
        Map<Integer, BuildDefinition> projectsBuildDefinitionsMap = new HashMap<Integer, BuildDefinition>();
        
        for ( Project project : projectList )
        {
            //boolean shouldBuild = false;
            int buildDefinitionId = 0;
            
            if ( projectsAndBuildDefinitionsMap.get( project.getId() ) != null )
            {
                buildDefinitionId = projectsAndBuildDefinitionsMap.get( project.getId() );                
                //shouldBuild = true;         
                try
                {
                    BuildDefinition buildDefinition = buildDefinitionDao.getBuildDefinition( buildDefinitionId );
                    projectsBuildDefinitionsMap.put( project.getId(), buildDefinition );
                    projectsToBeBuilt.add( project );
                }
                catch( ContinuumStoreException e )
                {
                    log.error( "Error while creating build object", e );
                    throw new TaskExecutionException( "Error while creating build object", e );
                }
            }
            else if ( project.getState() == ContinuumProjectState.CHECKEDOUT || project.getState() == ContinuumProjectState.NEW ) //check if no build result yet for project
            {
                try
                {
                    //get default build definition for project
                    //buildDefinitionId = buildDefinitionDao.getDefaultBuildDefinition( project.getId() ).getId();
                    BuildDefinition buildDefinition = buildDefinitionDao.getDefaultBuildDefinition( project.getId() );
                    projectsBuildDefinitionsMap.put( project.getId(), buildDefinition );
                    projectsToBeBuilt.add( project );
                }
                catch ( ContinuumStoreException e )
                {
                    log.error( "Error while creating build object", e );
                    throw new TaskExecutionException( "Error while creating build object", e );
                }
                catch ( Exception e )
                {
                    log.error( e.getMessage(), e );
                    throw new TaskExecutionException( "Error executing action 'build-project'", e );
                }
                //shouldBuild = true;
                projectsToBeBuilt.add( project );
            }
        }
        
        try
        {
            Map context = new HashMap();
            context.put( AbstractContinuumAction.KEY_PROJECTS, projectsToBeBuilt );
            context.put( AbstractContinuumAction.KEY_PROJECTS_BUILD_DEFINITIONS_MAP, projectsBuildDefinitionsMap );
            context.put( AbstractContinuumAction.KEY_TRIGGER, trigger );
            
            log.info( "Performing action create-build-project-task" );
            actionManager.lookup( "create-build-project-task" ).execute( context );
        }
        catch ( ActionNotFoundException e )
        {
           log.error( "Error looking up action 'build-project'" );
           throw new TaskExecutionException( "Error looking up action 'build-project'", e );
        }
        catch ( Exception e )
        {
            log.error( e.getMessage(), e );
            throw new TaskExecutionException( "Error executing action 'build-project'", e );
        }
    }
}
