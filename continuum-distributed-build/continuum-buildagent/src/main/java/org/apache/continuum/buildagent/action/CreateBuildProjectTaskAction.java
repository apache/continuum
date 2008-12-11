package org.apache.continuum.buildagent.action;

import java.util.Map;

import org.apache.continuum.dao.BuildDefinitionDao;
import org.apache.continuum.dao.ProjectDao;
import org.apache.continuum.taskqueue.manager.TaskQueueManager;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.buildqueue.BuildProjectTask;
import org.apache.maven.continuum.execution.ContinuumBuildExecutor;
import org.apache.maven.continuum.execution.manager.BuildExecutorManager;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.taskqueue.TaskQueueException;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:ctan@apache.org">Maria Catherine Tan</a>
 * @plexus.component role="org.codehaus.plexus.action.Action" role-hint="create-build-project-task"
 */
public class CreateBuildProjectTaskAction
    extends AbstractContinuumAction
{
    /**
     * @plexus.requirement
     */
    private TaskQueueManager taskQueueManager;

    /**
     * @plexus.requirement
     */
    private BuildExecutorManager executorManager;

    /**
     * @plexus.requirement
     */
    private BuildDefinitionDao buildDefinitionDao;
    
    public synchronized void execute( Map context )
        throws Exception
    {
        Project project = AbstractContinuumAction.getProject( context );
        
        int buildDefinitionId = AbstractContinuumAction.getBuildDefinitionId( context );
        int trigger = AbstractContinuumAction.getTrigger( context );
        
        if ( taskQueueManager.isInBuildingQueue( project.getId(), buildDefinitionId ) )
        {
            return;
        }

        if ( taskQueueManager.isInCheckoutQueue( project.getId() ) )
        {
            taskQueueManager.removeProjectFromCheckoutQueue( project.getId() );
        }
        
        try
        {
            /*
            if ( project.getState() != ContinuumProjectState.NEW &&
                project.getState() != ContinuumProjectState.CHECKEDOUT &&
                project.getState() != ContinuumProjectState.OK && project.getState() != ContinuumProjectState.FAILED &&
                project.getState() != ContinuumProjectState.ERROR )
            {
                ContinuumBuildExecutor executor = executorManager.getBuildExecutor( project.getExecutorId() );

                if ( executor.isBuilding( project ) || project.getState() == ContinuumProjectState.UPDATING )
                {
                    // project is building
                    getLogger().info( "Project '" + project.getName() + "' already being built." );

                    return;
                }
                else
                {
                    project.setOldState( project.getState() );

                    project.setState( ContinuumProjectState.ERROR );

                    projectDao.updateProject( project );

                    project = projectDao.getProject( project.getId() );
                }
            }
            else
            {
                project.setOldState( project.getState() );

                projectDao.updateProject( project );

                project = projectDao.getProject( project.getId() );
            }
            */
            
            BuildDefinition buildDefinition = buildDefinitionDao.getBuildDefinition( buildDefinitionId );
            String buildDefinitionLabel = buildDefinition.getDescription();
            if ( StringUtils.isEmpty( buildDefinitionLabel ) )
            {
                buildDefinitionLabel = buildDefinition.getGoals();
            }

            getLogger().info( "Enqueuing '" + project.getName() + "' with build definition '" + buildDefinitionLabel +
                "' - id=" + buildDefinitionId + ")." );

            BuildProjectTask task = new BuildProjectTask( project.getId(), buildDefinitionId, trigger, project
                .getName(), buildDefinitionLabel );

            task.setMaxExecutionTime( buildDefinition.getSchedule()
                .getMaxJobExecutionTime() * 1000 );

            taskQueueManager.getBuildQueue().put( task );
        }
        catch ( ContinuumStoreException e )
        {
            getLogger().error( "Error while creating build object", e );
            throw new ContinuumException( "Error while creating build object.", e );
        }
        catch ( TaskQueueException e )
        {
            getLogger().error( "Error while enqueuing object", e );
            throw new ContinuumException( "Error while enqueuing object.", e );
        }
    }
}

