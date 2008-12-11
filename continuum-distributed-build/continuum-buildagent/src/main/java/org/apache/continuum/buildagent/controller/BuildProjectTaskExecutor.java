package org.apache.continuum.buildagent.controller;

import org.apache.maven.continuum.buildcontroller.BuildController;
import org.apache.maven.continuum.buildqueue.BuildProjectTask;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.taskqueue.Task;
import org.codehaus.plexus.taskqueue.execution.TaskExecutionException;
import org.codehaus.plexus.taskqueue.execution.TaskExecutor;


public class BuildProjectTaskExecutor
    extends AbstractLogEnabled
    implements TaskExecutor
{
    /**
     * @plexus.requirement role-hint="distributed"
     */
    private BuildController controller;

    // ----------------------------------------------------------------------
    // TaskExecutor Implementation
    // ----------------------------------------------------------------------

    public void executeTask( Task task )
        throws TaskExecutionException
    {
        BuildProjectTask buildProjectTask = (BuildProjectTask) task;

        controller.build( buildProjectTask.getProjectId(), buildProjectTask.getBuildDefinitionId(), buildProjectTask
            .getTrigger() );
    }
}
