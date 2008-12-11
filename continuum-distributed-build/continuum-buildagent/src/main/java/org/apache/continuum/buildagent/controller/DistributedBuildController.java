package org.apache.continuum.buildagent.controller;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.continuum.buildagent.buildcontext.manager.BuildContextManager;
import org.apache.continuum.buildagent.util.BuildContextToProject;
import org.apache.continuum.dao.BuildDefinitionDao;
import org.apache.maven.continuum.buildcontroller.BuildContext;
import org.apache.maven.continuum.buildcontroller.BuildController;
import org.apache.maven.continuum.core.action.AbstractContinuumAction;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.scm.ChangeFile;
import org.apache.maven.continuum.model.scm.ChangeSet;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.apache.maven.continuum.utils.ContinuumUtils;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.codehaus.plexus.action.ActionManager;
import org.codehaus.plexus.action.ActionNotFoundException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.taskqueue.execution.TaskExecutionException;

/**
 * @plexus.component role="org.apache.maven.continuum.buildcontroller.BuildController" role-hint="distributed"
 */
public class DistributedBuildController
    extends AbstractLogEnabled
    implements BuildController
{
    /**
     * @plexus.requirement
     */
    private BuildDefinitionDao buildDefinitionDao;

    /**
     * @plexus.requirement
     */
    private ActionManager actionManager;

    /**
     * @plexus.requirement
     */
    private BuildContextManager buildContextManager;    
    // ----------------------------------------------------------------------
    // BuildController Implementation
    // ----------------------------------------------------------------------

    /**
     * @param projectId
     * @param buildDefinitionId
     * @param trigger
     * @throws TaskExecutionException
     */
    public void build( int projectId, int buildDefinitionId, int trigger )
        throws TaskExecutionException
    {
        getLogger().info( "Initializing build" );
        BuildContext context = initializeBuildContext( projectId, buildDefinitionId, trigger );

        // ignore this if AlwaysBuild ?
        /*if ( !checkScmResult( context ) )
        {
            getLogger().info( "Error updating from SCM, not building" );
            return;
        }*/
        
        getLogger().info( "Starting build of " + context.getProject().getName() );
        startBuild( context );

        try
        {

            Map actionContext = context.getActionContext();

            try
            {
                performAction( "update-project-from-working-directory-dist", context );
            }
            catch ( TaskExecutionException e )
            {
                updateBuildResult( context, ContinuumUtils.throwableToString( e ) );

                //just log the error but don't stop the build from progressing in order not to suppress any build result messages there
                getLogger().error( "Error executing action update-project-from-working-directory '", e );
            }

            performAction( "execute-builder-dist", context );
            // TODO:
            // add the result to a manager that handles the build result
            //
            

            //should we deploy the artifact or only after the build result?
            //performAction( "deploy-artifact-dist", context );

            context.setCancelled( (Boolean) actionContext.get( AbstractContinuumAction.KEY_CANCELLED ) );

            String s = (String) actionContext.get( AbstractContinuumAction.KEY_BUILD_ID );


        }
        finally
        {
            endBuild( context );
        }
    }

    /**
     * Checks if the build should be marked as ERROR and notifies the end of the build.
     *
     * @param context
     * @throws TaskExecutionException
     */
    private void endBuild( BuildContext context )
        throws TaskExecutionException
    {
        
    }

    private void updateBuildResult( BuildContext context, String error )
        throws TaskExecutionException
    {

    }

    private void updateBuildResult( BuildResult build, BuildContext context )
    {

    }

    private void startBuild( BuildContext context )
        throws TaskExecutionException
    {

        Project project = context.getProject();

        project.setOldState( project.getState() );

        project.setState( ContinuumProjectState.BUILDING );

    }

    /**
     * Initializes a BuildContext for the build.
     *
     * @param projectId
     * @param buildDefinitionId
     * @param trigger
     * @return
     * @throws TaskExecutionException
     */
    protected BuildContext initializeBuildContext( int projectId, int buildDefinitionId, int trigger )
        throws TaskExecutionException
    {
        BuildContext context = new BuildContext();

        context.setStartTime( System.currentTimeMillis() );

        context.setTrigger( trigger );

        try
        {
            Project project = BuildContextToProject.getProject( buildContextManager.getBuildContext( projectId ) );

            context.setProject( project );

            BuildDefinition buildDefinition = buildDefinitionDao.getBuildDefinition( buildDefinitionId );

            context.setBuildDefinition( buildDefinition );

            //assume null.
            BuildResult oldBuildResult = null;
                
            context.setOldBuildResult( oldBuildResult );

            context.setScmResult( null );
            
            // CONTINUUM-1871 olamy if continuum is killed during building oldBuildResult will have a endTime 0
            // this means all changes since the project has been loaded in continuum will be in memory
            // now we will load all BuildResult with an Id bigger or equals than the oldBuildResult one
            //if ( oldBuildResult != null )
            //{
            //    context.setOldScmResult(
            //        getOldScmResults( projectId, oldBuildResult.getBuildNumber(), oldBuildResult.getEndTime() ) );
            //}
        }
        catch ( ContinuumStoreException e )
        {
            throw new TaskExecutionException( "Error initializing the build context", e );
        }

        Map actionContext = context.getActionContext();

        actionContext.put( AbstractContinuumAction.KEY_PROJECT_ID, projectId );

        actionContext.put( AbstractContinuumAction.KEY_PROJECT, context.getProject() );

        actionContext.put( AbstractContinuumAction.KEY_BUILD_DEFINITION_ID, buildDefinitionId );

        actionContext.put( AbstractContinuumAction.KEY_BUILD_DEFINITION, context.getBuildDefinition() );

        actionContext.put( AbstractContinuumAction.KEY_TRIGGER, trigger );

        actionContext.put( AbstractContinuumAction.KEY_FIRST_RUN, context.getOldBuildResult() == null );

        if ( context.getOldBuildResult() != null )
        {
            actionContext.put( AbstractContinuumAction.KEY_OLD_BUILD_ID, context.getOldBuildResult().getId() );
        }

        return context;
    }


    private void performAction( String actionName, BuildContext context )
        throws TaskExecutionException
    {
        String error = null;
        TaskExecutionException exception = null;

        try
        {
            getLogger().info( "Performing action " + actionName );
            actionManager.lookup( actionName ).execute( context.getActionContext() );
            return;
        }
        catch ( ActionNotFoundException e )
        {
            error = ContinuumUtils.throwableToString( e );
            exception = new TaskExecutionException( "Error looking up action '" + actionName + "'", e );
        }
        catch ( ScmRepositoryException e )
        {
            error = getValidationMessages( e ) + "\n" + ContinuumUtils.throwableToString( e );

            exception = new TaskExecutionException( "SCM error while executing '" + actionName + "'", e );
        }
        catch ( ScmException e )
        {
            error = ContinuumUtils.throwableToString( e );

            exception = new TaskExecutionException( "SCM error while executing '" + actionName + "'", e );
        }
        catch ( Exception e )
        {
            exception = new TaskExecutionException( "Error executing action '" + actionName + "'", e );
            error = ContinuumUtils.throwableToString( exception );
        }

        // TODO: clean this up. We catch the original exception from the action, and then update the buildresult
        // for it - we need to because of the specialized error message for SCM.
        // If updating the buildresult fails, log the previous error and throw the new one.
        // If updating the buildresult succeeds, throw the original exception. The build result should NOT
        // be updated again - a TaskExecutionException is final, no further action should be taken upon it.

        try
        {
            updateBuildResult( context, error );
        }
        catch ( TaskExecutionException e )
        {
            getLogger().error( "Error updating build result after receiving the following exception: ", exception );
            throw e;
        }

        throw exception;
    }


    private boolean checkAllChangesUnknown( List<ChangeSet> changes )
    {
        for ( ChangeSet changeSet : changes )
        {
            List<ChangeFile> changeFiles = changeSet.getFiles();

            for ( ChangeFile changeFile : changeFiles )
            {
                if ( !"unknown".equalsIgnoreCase( changeFile.getStatus() ) )
                {
                    return false;
                }
            }
        }

        return true;
    }

    private String getValidationMessages( ScmRepositoryException ex )
    {
        List<String> messages = ex.getValidationMessages();

        StringBuffer message = new StringBuffer();

        if ( messages != null && !messages.isEmpty() )
        {
            for ( Iterator<String> i = messages.iterator(); i.hasNext(); )
            {
                message.append( i.next() );

                if ( i.hasNext() )
                {
                    message.append( System.getProperty( "line.separator" ) );
                }
            }
        }
        return message.toString();
    }
/*
    protected void checkProjectDependencies( BuildContext context )
    {
        if ( context.getOldBuildResult() == null )
        {
            return;
        }

        try
        {
            Project project = projectDao.getProjectWithAllDetails( context.getProject().getId() );
            List<ProjectDependency> dependencies = project.getDependencies();

            if ( dependencies == null )
            {
                dependencies = new ArrayList<ProjectDependency>();
            }

            if ( project.getParent() != null )
            {
                dependencies.add( project.getParent() );
            }

            if ( dependencies.isEmpty() )
            {
                return;
            }

            List<ProjectDependency> modifiedDependencies = new ArrayList<ProjectDependency>();

            for ( ProjectDependency dep : dependencies )
            {
                Project dependencyProject =
                    projectDao.getProject( dep.getGroupId(), dep.getArtifactId(), dep.getVersion() );

                if ( dependencyProject != null )
                {
                    List buildResults = buildResultDao.getBuildResultsInSuccessForProject( dependencyProject.getId(),
                                                                                           context.getOldBuildResult().getEndTime() );
                    if ( buildResults != null && !buildResults.isEmpty() )
                    {
                        getLogger().debug( "Dependency changed: " + dep.getGroupId() + ":" + dep.getArtifactId() + ":" +
                            dep.getVersion() );
                        modifiedDependencies.add( dep );
                    }
                    else
                    {
                        getLogger().debug( "Dependency not changed: " + dep.getGroupId() + ":" + dep.getArtifactId() +
                            ":" + dep.getVersion() );
                    }
                }
                else
                {
                    getLogger().debug( "Skip non Continuum project: " + dep.getGroupId() + ":" + dep.getArtifactId() +
                        ":" + dep.getVersion() );
                }
            }

            context.setModifiedDependencies( modifiedDependencies );
            context.getActionContext().put( AbstractContinuumAction.KEY_UPDATE_DEPENDENCIES, modifiedDependencies );
        }
        catch ( ContinuumStoreException e )
        {
            getLogger().warn( "Can't get the project dependencies", e );
        }
    }
*/


}

