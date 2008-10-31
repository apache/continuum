package org.apache.continuum.taskqueue.manager;

public class TaskQueueManagerException
    extends Exception
{
    public TaskQueueManagerException( String message )
    {
        super( message );
    }

    public TaskQueueManagerException( Throwable cause )
    {
        super( cause );
    }

    public TaskQueueManagerException( String message, Throwable cause )
    {
        super( message, cause );
    }
}
