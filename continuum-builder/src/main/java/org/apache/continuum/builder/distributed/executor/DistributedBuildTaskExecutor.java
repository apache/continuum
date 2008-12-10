package org.apache.continuum.builder.distributed.executor;

import org.codehaus.plexus.taskqueue.Task;
import org.codehaus.plexus.taskqueue.execution.TaskExecutionException;

public interface DistributedBuildTaskExecutor
{
    void executeTask( Task task )
        throws TaskExecutionException;

    String getBuildAgentUrl();

    void setBuildAgentUrl( String buildAgentUrl );
}
