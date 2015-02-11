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

import org.apache.commons.lang.ArrayUtils;
import org.apache.continuum.buildmanager.BuildManagerException;
import org.apache.continuum.buildmanager.BuildsManager;
import org.apache.continuum.model.project.ProjectScmRoot;
import org.apache.continuum.taskqueue.BuildProjectTask;
import org.apache.continuum.web.util.AuditLog;
import org.apache.continuum.web.util.AuditLogConstants;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.web.exception.AuthorizationRequiredException;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 */
@Component( role = com.opensymphony.xwork2.Action.class, hint = "cancelBuild", instantiationStrategy = "per-lookup"  )
public class CancelBuildAction
    extends ContinuumActionSupport
{
    private static final Logger logger = LoggerFactory.getLogger( CancelBuildAction.class );

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

            if ( getContinuum().getConfiguration().isDistributedBuildEnabled() )
            {
                getContinuum().getDistributedBuildManager().cancelBuild( projectId );
            }
            else
            {
                BuildsManager buildsManager = getContinuum().getBuildsManager();

                buildsManager.cancelBuild( projectId );
            }

            AuditLog event = new AuditLog( "Project id=" + projectId, AuditLogConstants.CANCEL_BUILD );
            event.setCategory( AuditLogConstants.PROJECT );
            event.setCurrentUser( getPrincipal() );
            event.log();
        }
        catch ( AuthorizationRequiredException e )
        {
            return REQUIRES_AUTHORIZATION;
        }
        catch ( BuildManagerException e )
        {
            throw new ContinuumException( "Error while canceling build", e );
        }

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

        if ( getContinuum().getConfiguration().isDistributedBuildEnabled() )
        {
            for ( int i = 0; i < projectsId.length; i++ )
            {
                getContinuum().getDistributedBuildManager().cancelBuild( projectsId[i] );

                AuditLog event = new AuditLog( "Project id=" + projectsId[i], AuditLogConstants.CANCEL_BUILD );
                event.setCategory( AuditLogConstants.PROJECT );
                event.setCurrentUser( getPrincipal() );
                event.log();
            }
        }
        else
        {
            BuildsManager parallelBuildsManager = getContinuum().getBuildsManager();
            parallelBuildsManager.removeProjectsFromBuildQueue( projectsId );

            try
            {
                // now we must check if the current build is one of this
                int index = ArrayUtils.indexOf( projectsId, getCurrentProjectIdBuilding() );
                if ( index > 0 )
                {
                    int projId = projectsId[index];
                    getContinuum().getBuildsManager().cancelBuild( projId );

                    AuditLog event = new AuditLog( "Project id=" + projId, AuditLogConstants.CANCEL_BUILD );
                    event.setCategory( AuditLogConstants.PROJECT );
                    event.setCurrentUser( getPrincipal() );
                    event.log();
                }

            }
            catch ( BuildManagerException e )
            {
                logger.error( e.getMessage() );
                throw new ContinuumException( e.getMessage(), e );
            }
        }

        return SUCCESS;
    }

    public String cancelGroupBuild()
        throws ContinuumException
    {
        try
        {
            checkBuildProjectInGroupAuthorization( getContinuum().getProjectGroup( projectGroupId ).getName() );
        }
        catch ( AuthorizationRequiredException e )
        {
            return REQUIRES_AUTHORIZATION;
        }

        if ( getContinuum().getConfiguration().isDistributedBuildEnabled() )
        {
            getContinuum().getDistributedBuildManager().cancelGroupBuild( projectGroupId );

            AuditLog event = new AuditLog( "Project Group id=" + projectGroupId, AuditLogConstants.CANCEL_BUILD );
            event.setCategory( AuditLogConstants.PROJECT_GROUP );
            event.setCurrentUser( getPrincipal() );
            event.log();

            return SUCCESS;
        }
        else
        {
            BuildsManager buildsManager = getContinuum().getBuildsManager();

            List<ProjectScmRoot> scmRoots = getContinuum().getProjectScmRootByProjectGroup( projectGroupId );

            if ( scmRoots != null )
            {
                for ( ProjectScmRoot scmRoot : scmRoots )
                {
                    try
                    {
                        buildsManager.removeProjectGroupFromPrepareBuildQueue( projectGroupId,
                                                                               scmRoot.getScmRootAddress() );
                        //taskQueueManager.removeFromPrepareBuildQueue( projectGroupId, scmRoot.getScmRootAddress() );
                    }
                    catch ( BuildManagerException e )
                    {
                        throw new ContinuumException( "Unable to cancel group build", e );
                    }
                }
            }
            Collection<Project> projects = getContinuum().getProjectsInGroup( projectGroupId );

            List<String> projectIds = new ArrayList<String>();

            for ( Project project : projects )
            {
                projectIds.add( Integer.toString( project.getId() ) );
            }

            setSelectedProjects( projectIds );

            return cancelBuilds();
        }
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

    /**
     * @return -1 if not project currently building
     * @throws ContinuumException
     */
    protected int getCurrentProjectIdBuilding()
        throws ContinuumException, BuildManagerException
    {
        Map<String, BuildProjectTask> currentBuilds = getContinuum().getBuildsManager().getCurrentBuilds();
        Set<String> keySet = currentBuilds.keySet();

        for ( String key : keySet )
        {
            BuildProjectTask task = currentBuilds.get( key );
            if ( task != null )
            {
                return task.getProjectId();
            }
        }
        return -1;
    }
}
