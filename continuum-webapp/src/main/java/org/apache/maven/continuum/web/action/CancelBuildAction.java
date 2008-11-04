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

import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.continuum.taskqueue.manager.TaskQueueManager;
import org.apache.continuum.taskqueue.manager.TaskQueueManagerException;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.web.action.admin.AbstractBuildQueueAction;
import org.apache.maven.continuum.web.exception.AuthorizationRequiredException;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 * @plexus.component role="com.opensymphony.xwork2.Action" role-hint="cancelBuild"
 */
public class CancelBuildAction
    extends AbstractBuildQueueAction
{

    private int projectId;

    private int projectGroupId;

    private List<String> selectedProjects;

    private String projectGroupName = "";

    public String execute()
        throws ContinuumException
    {
        try
        {
            checkBuildProjectInGroupAuthorization( getProjectGroupName() );
        }
        catch ( AuthorizationRequiredException e )
        {
            return REQUIRES_AUTHORIZATION;
        }

        cancelBuild( projectId );

        return SUCCESS;
    }

    public String cancelBuilds()
        throws ContinuumException
    {
        if ( getSelectedProjects() == null || getSelectedProjects().isEmpty() )
        {
            return SUCCESS;
        }
        int[] projectsId = new int[getSelectedProjects().size()];
        for ( String selectedProjectId : getSelectedProjects() )
        {
            int projectId = Integer.parseInt( selectedProjectId );
            projectsId = ArrayUtils.add( projectsId, projectId );
        }

        TaskQueueManager taskQueueManager = getContinuum().getTaskQueueManager();
        try
        {
            taskQueueManager.removeProjectsFromBuildingQueue( projectsId );
        }
        catch ( TaskQueueManagerException e )
        {
            throw new ContinuumException( "Unable to remove projects from building queue", e );
        }
        // now we must check if the current build is one of this
        int index = ArrayUtils.indexOf( projectsId, getCurrentProjectIdBuilding() );
        if ( index > 0 )
        {
            cancelBuild( projectsId[index] );
        }
        return SUCCESS;
    }


    public void setProjectId( int projectId )
    {
        this.projectId = projectId;
    }

    public String getProjectGroupName()
        throws ContinuumException
    {
        if ( StringUtils.isEmpty( projectGroupName ) )
        {
            projectGroupName = getContinuum().getProjectGroupByProjectId( projectId ).getName();
        }

        return projectGroupName;
    }

    public List<String> getSelectedProjects()
    {
        return selectedProjects;
    }

    public void setSelectedProjects( List<String> selectedProjects )
    {
        this.selectedProjects = selectedProjects;
    }

    public int getProjectGroupId()
    {
        return projectGroupId;
    }

    public void setProjectGroupId( int projectGroupId )
    {
        this.projectGroupId = projectGroupId;
    }
}
