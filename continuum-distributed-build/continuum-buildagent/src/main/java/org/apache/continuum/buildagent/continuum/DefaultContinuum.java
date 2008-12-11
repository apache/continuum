package org.apache.continuum.buildagent.continuum;

import java.util.HashMap;
import java.util.Map;

import org.apache.continuum.scm.queue.PrepareBuildProjectsTask;
import org.apache.continuum.buildagent.taskqueue.manager.TaskQueueManager;
import org.apache.continuum.taskqueue.manager.TaskQueueManagerException;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.store.ContinuumObjectNotFoundException;
import org.codehaus.plexus.action.ActionManager;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.taskqueue.TaskQueueException;

/**
 * @plexus.component role="org.apache.continuum.buildagent.continuum.Continuum" role-hint="default" 
 */
public class DefaultContinuum
    extends AbstractLogEnabled
    implements Continuum//, Contextualizable, Initializable, Startable
{
    
    /**
     * @plexus.requirement role-hint="task-queue-manager-dist"
     */
    private TaskQueueManager taskQueueManager;

    public void buildProject( int projectId, int buildDefinitionId, int trigger )
        throws ContinuumException
    {
        try
        {
            if ( taskQueueManager.isInBuildingQueue( projectId, buildDefinitionId )
                || taskQueueManager.isInCheckoutQueue( projectId )
                || taskQueueManager.isInPrepareBuildQueue( projectId ) )
            {
                return;
            }
        }
        catch ( TaskQueueManagerException e )
        {
            throw new ContinuumException( e.getMessage(), e );
        }

        Map<Integer, Integer> projectsBuildDefinitionsMap =
            new HashMap<Integer, Integer>( projectId, buildDefinitionId );

        prepareBuildProjects( projectsBuildDefinitionsMap, trigger );
    }
    

    private void prepareBuildProjects( Map<Integer, Integer> projectsBuildDefinitionsMap, int trigger )
        throws ContinuumException
    {

        try
        {
            PrepareBuildProjectsTask task = new PrepareBuildProjectsTask( projectsBuildDefinitionsMap, trigger );
            taskQueueManager.getPrepareBuildQueue().put( task );
            // taskQueueManager.getPrepareBuildQueue().
        }
        catch ( TaskQueueException e )
        {
            throw logAndCreateException( "Error while creating enqueuing object.", e );
        }
        
    }


    public void buildProjectWithBuildDefinition( int projectId, int buildDefinitionId )
        throws ContinuumException
    {
        // TODO Auto-generated method stub
        
    }


    // ----------------------------------------------------------------------
    // Logging
    // ----------------------------------------------------------------------

    private ContinuumException logAndCreateException( String message, Throwable cause )
    {
        if ( cause instanceof ContinuumObjectNotFoundException )
        {
            return new ContinuumException( "No such object.", cause );
        }

        getLogger().error( message, cause );

        return new ContinuumException( message, cause );
    }


    public TaskQueueManager getTaskQueueManager()
    {
        return taskQueueManager;
    }
   

}
