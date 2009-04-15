package org.apache.continuum.builder.distributed.executor.deferred;

import org.codehaus.plexus.taskqueue.Task;
import org.codehaus.plexus.taskqueue.TaskQueue;
import org.codehaus.plexus.taskqueue.execution.TaskExecutionException;
import org.codehaus.plexus.taskqueue.execution.TaskExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DistributedBuildProjectTaskExecutorDeferred
    implements TaskExecutor
{
    private static final Logger log = LoggerFactory.getLogger( DistributedBuildProjectTaskExecutorDeferred.class );

    /**
     * @plexus.requirement
     */
    private TaskQueue distributedTaskQueue;

    public void executeTask( Task task )
        throws TaskExecutionException
    {
        try
        {
            Thread.sleep( 1000 );
            distributedTaskQueue.put( task );
        }
        catch ( Exception e )
        {
            log.error( "error encountered adding the deferred task back to distributed queue", e );
            throw new TaskExecutionException( e.getMessage(), e );
        }
    }
}
