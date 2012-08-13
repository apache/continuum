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
import org.apache.continuum.web.util.AuditLog;
import org.apache.continuum.web.util.AuditLogConstants;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.web.exception.AuthorizationRequiredException;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 * @plexus.component role="com.opensymphony.xwork2.Action" role-hint="projects"
 */
public class ProjectsListAction
    extends ContinuumActionSupport
{
    private static final Logger logger = LoggerFactory.getLogger( ProjectsListAction.class );

    private List<String> selectedProjects;

    private List<String> selectedProjectsNames;

    private String projectGroupName = "";

    private int projectGroupId;

    private String methodToCall;

    private int buildDefinitionId;

    public String execute()
        throws Exception
    {
        if ( StringUtils.isEmpty( methodToCall ) )
        {
            return SUCCESS;
        }

        if ( "build".equals( methodToCall ) )
        {
            return build();
        }
        else if ( "remove".equals( methodToCall ) )
        {
            return remove();
        }
        else if ( "confirmRemove".equals( methodToCall ) )
        {
            return confirmRemove();
        }

        return SUCCESS;
    }

    private String remove()
        throws ContinuumException
    {
        try
        {
            checkModifyProjectGroupAuthorization( getProjectGroupName() );
        }
        catch ( AuthorizationRequiredException e )
        {
            return REQUIRES_AUTHORIZATION;
        }

        if ( selectedProjects != null && !selectedProjects.isEmpty() )
        {
            for ( String selectedProject : selectedProjects )
            {
                int projectId = Integer.parseInt( selectedProject );

                try
                {
                    AuditLog event = new AuditLog( "Project id=" + projectId, AuditLogConstants.REMOVE_PROJECT );
                    event.setCategory( AuditLogConstants.PROJECT );
                    event.setCurrentUser( getPrincipal() );
                    event.log();

                    getContinuum().removeProject( projectId );
                }
                catch ( ContinuumException e )
                {
                    logger.error( "Error removing Project with id=" + projectId );
                    addActionError( getText( "deleteProject.error", "Unable to delete project", new Integer(
                        projectId ).toString() ) );
                }
            }
        }

        return SUCCESS;
    }

    public String confirmRemove()
        throws ContinuumException
    {
        if ( selectedProjects != null && !selectedProjects.isEmpty() )
        {
            this.selectedProjectsNames = new ArrayList<String>();
            for ( String selectedProject : selectedProjects )
            {
                int projectId = Integer.parseInt( selectedProject );
                selectedProjectsNames.add( getContinuum().getProject( projectId ).getName() );
            }
        }
        return "confirmRemove";
    }

    private String build()
        throws ContinuumException
    {
        try
        {
            checkModifyProjectGroupAuthorization( getProjectGroupName() );
        }
        catch ( AuthorizationRequiredException e )
        {
            return REQUIRES_AUTHORIZATION;
        }

        if ( selectedProjects != null && !selectedProjects.isEmpty() )
        {
            ArrayList<Project> projectsList = new ArrayList<Project>();
            for ( String pId : selectedProjects )
            {
                int projectId = Integer.parseInt( pId );
                Project p = getContinuum().getProjectWithAllDetails( projectId );
                projectsList.add( p );

                AuditLog event = new AuditLog( "Project id=" + projectId, AuditLogConstants.FORCE_BUILD );
                event.setCategory( AuditLogConstants.PROJECT );
                event.setCurrentUser( getPrincipal() );
                event.log();
            }

            List<Project> sortedProjects = getContinuum().getProjectsInBuildOrder( projectsList );

            try
            {
                if ( this.getBuildDefinitionId() <= 0 )
                {
                    List<BuildDefinition> groupDefaultBDs = getContinuum().getDefaultBuildDefinitionsForProjectGroup(
                        projectGroupId );
                    getContinuum().buildProjectsWithBuildDefinition( sortedProjects, groupDefaultBDs );
                }
                else
                {
                    getContinuum().buildProjectsWithBuildDefinition( sortedProjects, buildDefinitionId );
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
        }

        return SUCCESS;
    }

    public String getProjectGroupName()
        throws ContinuumException
    {
        if ( StringUtils.isEmpty( projectGroupName ) )
        {
            projectGroupName = getContinuum().getProjectGroup( projectGroupId ).getName();
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

    public void setMethodToCall( String methodToCall )
    {
        this.methodToCall = methodToCall;
    }

    public int getBuildDefinitionId()
    {
        return buildDefinitionId;
    }

    public void setBuildDefinitionId( int buildDefinitionId )
    {
        this.buildDefinitionId = buildDefinitionId;
    }

    public List<String> getSelectedProjectsNames()
    {
        return selectedProjectsNames;
    }

    public void setSelectedProjectsNames( List<String> selectedProjectsNames )
    {
        this.selectedProjectsNames = selectedProjectsNames;
    }
}
