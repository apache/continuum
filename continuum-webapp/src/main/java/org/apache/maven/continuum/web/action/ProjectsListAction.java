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

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.store.ContinuumObjectNotFoundException;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.apache.maven.continuum.web.exception.AuthorizationRequiredException;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.dag.CycleDetectedException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 * @plexus.component role="com.opensymphony.xwork.Action" role-hint="projects"
 */
public class ProjectsListAction
    extends ContinuumActionSupport
{
    /**
     * @plexus.requirement role-hint="jdo"
     */
    private ContinuumStore store;

    private Collection selectedProjects;

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
            for ( Iterator i = selectedProjects.iterator(); i.hasNext(); )
            {
                int projectId = Integer.parseInt( (String) i.next() );

                try
                {
                    getLogger().info( "Removing Project with id=" + projectId );

                    getContinuum().removeProject( projectId );
                }
                catch ( ContinuumException e )
                {
                    getLogger().error( "Error removing Project with id=" + projectId );
                    addActionError( "Unable to remove Project with id=" + projectId );
                }
            }
        }

        return SUCCESS;
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
            for ( Iterator i = selectedProjects.iterator(); i.hasNext(); )
            {
                int projectId = Integer.parseInt( (String) i.next() );
                Project p = getContinuum().getProjectWithAllDetails( projectId );
                projectsList.add( p );
            }

            
            List sortedProjects;
            try
            {
                sortedProjects = getContinuum().getProjectsInBuildOrder( projectsList );
            }
            catch ( CycleDetectedException e )
            {
                sortedProjects = projectsList;
            }

            //TODO : Change this part because it's a duplicate of DefaultContinuum.buildProjectGroup*
            List<BuildDefinition> groupDefaultBDs = null;
            if (getBuildDefinitionId() == -1 || getBuildDefinitionId() == 0)
            {
                try
                {
                    groupDefaultBDs = store.getDefaultBuildDefinitionsForProjectGroup( projectGroupId );
                }
                catch ( ContinuumObjectNotFoundException e )
                {
                    throw new ContinuumException( "Project Group (id=" + projectGroupId +
                        " doens't have a default build definition, this should be impossible, it should always have a default definition set." );
                }
                catch ( ContinuumStoreException e )
                {
                    throw new ContinuumException( "Project Group (id=" + projectGroupId +
                        " doens't have a default build definition, this should be impossible, it should always have a default definition set." );
                }
            }
            for ( Iterator i = sortedProjects.iterator(); i.hasNext(); )
            {
                Project project = (Project) i.next();
                if ( this.getBuildDefinitionId() == -1 || getBuildDefinitionId() == 0)
                {
                    int buildDefId = -1;

                    for ( BuildDefinition bd : groupDefaultBDs )
                    {
                        if ( project.getExecutorId().equals( bd.getType() ) )
                        {
                            buildDefId = bd.getId();
                            break;
                        }
                    }

                    BuildDefinition projectDefaultBD = null;
                    if ( this.getBuildDefinitionId() == -1 )
                    {
                        try
                        {
                            projectDefaultBD = store.getDefaultBuildDefinitionForProject( project.getId() );
                        }
                        catch ( ContinuumObjectNotFoundException e )
                        {
                            getLogger().debug( e.getMessage() );
                        }
                        catch ( ContinuumStoreException e )
                        {
                            getLogger().debug( e.getMessage() );
                        }

                        if ( projectDefaultBD != null )
                        {
                            buildDefId = projectDefaultBD.getId();
                            getLogger()
                                .debug(
                                        "Project " + project.getId()
                                            + " has own default build definition, will use it instead of group's." );
                        }
                    }

                    getContinuum().buildProject( project.getId(), buildDefId, ContinuumProjectState.TRIGGER_FORCED );
                }
                else
                {
                    getContinuum().buildProject( project.getId(), this.getBuildDefinitionId(),
                                                 ContinuumProjectState.TRIGGER_FORCED );
                }
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

    public Collection getSelectedProjects()
    {
        return selectedProjects;
    }

    public void setSelectedProjects( Collection selectedProjects )
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
}