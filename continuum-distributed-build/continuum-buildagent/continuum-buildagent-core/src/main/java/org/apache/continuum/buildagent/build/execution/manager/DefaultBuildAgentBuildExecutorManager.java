package org.apache.continuum.buildagent.build.execution.manager;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.continuum.buildagent.build.execution.ContinuumAgentBuildExecutor;
import org.apache.maven.continuum.ContinuumException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @plexus.component role="org.apache.continuum.buildagent.build.execution.manager.BuildAgentBuildExecutorManager"
 * role-hint"default"
 */
public class DefaultBuildAgentBuildExecutorManager
    implements BuildAgentBuildExecutorManager
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
