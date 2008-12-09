package org.apache.continuum.builder;

import java.util.List;

import org.apache.continuum.builder.distributed.manager.DistributedBuildManager;
import org.apache.continuum.scm.queue.PrepareBuildProjectsTask;
import org.apache.maven.continuum.ContinuumException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Maria Catherine Tan
 * @plexus.component role="org.apache.continuum.builder.ContinuumBuilder" role-hint="distributedBuild"
 */
public class DistributedContinuumBuilder
    implements ContinuumBuilder
{
    private Logger log = LoggerFactory.getLogger( this.getClass() );

    /**
     * @plexus.requirement
     */
    private DistributedBuildManager distributedBuildManager;
    
    public void buildProjects( PrepareBuildProjectsTask task )
        throws ContinuumException
    {
        List<PrepareBuildProjectsTask> tasks = distributedBuildManager.getDistributedBuildQueue();
        boolean found = false;
        
        for ( PrepareBuildProjectsTask t : tasks )
        {
            if ( t.getProjectGroupId() == task.getProjectGroupId() && t.getScmRootAddress().equals( task.getScmRootAddress() ) )
            {
                found = true;
            }
        }

        if ( found )
        {
            log.info( "build task already in queue, waiting for available build agent..." );
        }
        else
        {
            log.info( "add build task to queue" );
            distributedBuildManager.getDistributedBuildQueue().add( task );
            distributedBuildManager.buildProjectsInQueue();
        }
    }
}
