package org.apache.continuum.buildagent.build.execution.manager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.continuum.buildagent.build.execution.ContinuumAgentBuildExecutor;
import org.apache.maven.continuum.ContinuumException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @plexus.component role="org.apache.continuum.buildagent.build.execution.manager.BuildExecutorManager"
 * role-hint"default"
 */
public class DefaultBuildExecutorManager
    implements BuildExecutorManager
{
    private Logger log = LoggerFactory.getLogger( this.getClass() );

    /**
     * @plexus.requirement role="org.apache.continuum.buildagent.build.execution.ContinuumAgentBuildExecutor"
     */
    private Map executors;

    // ----------------------------------------------------------------------
    // Component Lifecycle
    // ----------------------------------------------------------------------

    public void initialize()
    {
        if ( executors == null )
        {
            executors = new HashMap();
        }

        if ( executors.size() == 0 )
        {
            log.warn( "No build executors defined." );
        }
        else
        {
            log.info( "Build executors:" );

            for ( Iterator it = executors.keySet().iterator(); it.hasNext(); )
            {
                log.info( "  " + it.next().toString() );
            }
        }
    }

    // ----------------------------------------------------------------------
    // BuildExecutorManager Implementation
    // ----------------------------------------------------------------------

    public ContinuumAgentBuildExecutor getBuildExecutor( String builderType )
        throws ContinuumException
    {
        ContinuumAgentBuildExecutor executor = (ContinuumAgentBuildExecutor) executors.get( builderType );

        if ( executor == null )
        {
            throw new ContinuumException( "No such executor: '" + builderType + "'." );
        }

        return executor;
    }

    public boolean hasBuildExecutor( String executorId )
    {
        return executors.containsKey( executorId );
    }
}
