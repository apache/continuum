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

import org.apache.continuum.buildagent.NoBuildAgentException;
import org.apache.continuum.buildagent.NoBuildAgentInGroupException;
import org.apache.continuum.utils.build.BuildTrigger;
import org.apache.continuum.web.util.AuditLog;
import org.apache.continuum.web.util.AuditLogConstants;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.web.exception.AuthorizationRequiredException;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 * @plexus.component role="com.opensymphony.xwork2.Action" role-hint="buildProject"
 */
public class BuildProjectAction
    extends ContinuumActionSupport
{
    private int projectId;

    private int buildDefinitionId;

    private int projectGroupId;

    private boolean fromGroupPage = false;

    private boolean fromProjectPage = false;

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

        BuildTrigger buildTrigger = new BuildTrigger( ContinuumProjectState.TRIGGER_FORCED, getPrincipal() );

        try
        {
            if ( projectId > 0 )
            {
                if ( buildDefinitionId > 0 )
                {
                    getContinuum().buildProjectWithBuildDefinition( projectId, buildDefinitionId, buildTrigger );
                }
                else
                {
                    getContinuum().buildProject( projectId, buildTrigger.getTriggeredBy() );
                }
            }
            else
            {
                if ( buildDefinitionId > 0 )
                {
                    getContinuum().buildProjectGroupWithBuildDefinition( projectGroupId, buildDefinitionId,
                                                                         buildTrigger );
                }
                else
                {
                    //TODO: Check if this code is called, I don't think
                    //If it is, it should used the projectId
                    getContinuum().buildProjects( buildTrigger.getTriggeredBy() );
                }
            }
        }
        catch ( NoBuildAgentException e )
        {
            addActionError( getText( "projectGroup.build.error.noBuildAgent" ) );
        }
        catch ( NoBuildAgentInGroupException e )
        {
            addActionError( getText( "projectGroup.build.error.noBuildAgentInGroup" ) );
        }

        AuditLog event = new AuditLog( AuditLogConstants.FORCE_BUILD );
        event.setCurrentUser( getPrincipal() );

        if ( projectId > 0 )
        {
            event.setResource( "Project id=" + projectId );
            event.setCategory( AuditLogConstants.PROJECT );
            event.log();

            if ( fromGroupPage || hasActionErrors() )
            {
                return "to_group_page";
            }
            if ( fromProjectPage )
            {
                return "to_project_page";
            }
        }
        else
        {
            event.setResource( "Project Group id=" + projectGroupId );
            event.setCategory( AuditLogConstants.PROJECT_GROUP );
            event.log();
            if ( fromGroupPage )
            {
                return "to_group_page";
            }
        }

        return SUCCESS;
    }

    public void setProjectId( int projectId )
    {
        this.projectId = projectId;
    }

    public int getProjectId()
    {
        return projectId;
    }

    public void setBuildDefinitionId( int buildDefinitionId )
    {
        this.buildDefinitionId = buildDefinitionId;
    }

    public int getBuildDefinition()
    {
        return buildDefinitionId;
    }

    public int getProjectGroupId()
    {
        return projectGroupId;
    }

    public void setProjectGroupId( int projectGroupId )
    {
        this.projectGroupId = projectGroupId;
    }

    public boolean isFromGroupPage()
    {
        return fromGroupPage;
    }

    public void setFromGroupPage( boolean fromGroupPage )
    {
        this.fromGroupPage = fromGroupPage;
    }

    public boolean isFromProjectPage()
    {
        return fromProjectPage;
    }

    public void setFromProjectPage( boolean fromProjectPage )
    {
        this.fromProjectPage = fromProjectPage;
    }

    public String getProjectGroupName()
        throws ContinuumException
    {
        if ( StringUtils.isEmpty( projectGroupName ) )
        {
            if ( projectGroupId != 0 )
            {
                projectGroupName = getContinuum().getProjectGroup( projectGroupId ).getName();
            }
            else
            {
                ProjectGroup projectGroup = getContinuum().getProjectGroupByProjectId( projectId );

                projectGroupName = projectGroup.getName();
                projectGroupId = projectGroup.getId();
            }
        }

        return projectGroupName;
    }
}
