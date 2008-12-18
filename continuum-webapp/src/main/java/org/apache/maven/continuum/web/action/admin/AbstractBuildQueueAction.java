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
package org.apache.maven.continuum.web.action.admin;

import java.util.Map;
import java.util.Set;

import org.apache.continuum.buildmanager.BuildManagerException;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.buildqueue.BuildProjectTask;
import org.apache.maven.continuum.web.action.ContinuumActionSupport;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.taskqueue.Task;

/**
 * @author <a href="mailto:olamy@apache.org">olamy</a>
 * @since 28 sept. 07
 * @version $Id$
 */
public abstract class AbstractBuildQueueAction
    extends ContinuumActionSupport
    implements LogEnabled
{
    protected boolean cancelBuild( int projectId )
        throws ContinuumException
    {
        try
        {
            return getContinuum().getBuildsManager().cancelBuild( projectId );
        }
        catch ( BuildManagerException e )
        {
            getLogger().error( e.getMessage() );
            throw new ContinuumException( e.getMessage(), e );
        }
    }

    /**
     * @return -1 if not project currently building
     * @throws ContinuumException
     */
    protected int getCurrentProjectIdBuilding()
        throws ContinuumException, BuildManagerException
    {
        Map<String, Task> currentBuilds = getContinuum().getBuildsManager().getCurrentBuilds();
        Set<String> keySet = currentBuilds.keySet();
        
        for( String key : keySet )
        {
            Task task = currentBuilds.get( key );
            if ( task != null )
            {
                if ( task instanceof BuildProjectTask )
                {
                    return ( (BuildProjectTask) task ).getProjectId();
                }
            }
        }        
        return -1;
    }
}
