package org.apache.continuum.buildagent.continuum;

import org.apache.continuum.buildagent.taskqueue.manager.TaskQueueManager;
import org.apache.maven.continuum.ContinuumException;

public interface Continuum
{
    String ROLE = Continuum.class.getName();

    public void buildProject( int projectId, int buildDefinitionId, int trigger )
    throws ContinuumException;
    
    public TaskQueueManager getTaskQueueManager();
}
