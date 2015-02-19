package org.apache.maven.continuum.web.action;

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

import org.apache.continuum.buildmanager.BuildManagerException;
import org.apache.continuum.buildmanager.BuildsManager;
import org.apache.continuum.taskqueue.BuildProjectTask;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.project.ContinuumProjectState;

import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:olamy@apache.org">olamy</a>
 * @since 5 oct. 07
 */
public abstract class AbstractBuildAction
    extends ContinuumConfirmAction
{
    private int projectId;

    private boolean canDelete = true;

    protected boolean canRemoveBuildResult( BuildResult buildResult )
        throws BuildManagerException
    {
        BuildsManager buildsManager = getContinuum().getBuildsManager();

        Map<String, BuildProjectTask> currentBuilds = buildsManager.getCurrentBuilds();
        Set<String> keySet = currentBuilds.keySet();
        for ( String key : keySet )
        {
            BuildProjectTask buildProjectTask = currentBuilds.get( key );
            if ( buildProjectTask != null && buildResult != null )
            {
                return !( buildResult.getState() == ContinuumProjectState.BUILDING &&
                    ( buildProjectTask.getBuildDefinitionId() == buildResult.getBuildDefinition().getId() &&
                        buildProjectTask.getProjectId() == this.getProjectId() ) );
            }
        }
        return true;
    }

    public int getProjectId()
    {
        return projectId;
    }

    public void setProjectId( int projectId )
    {
        this.projectId = projectId;
    }

    public boolean isCanDelete()
    {
        return canDelete;
    }

    public void setCanDelete( boolean canDelete )
    {
        this.canDelete = canDelete;
    }
}
