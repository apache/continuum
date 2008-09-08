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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.web.exception.AuthorizationRequiredException;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.dag.CycleDetectedException;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 * @plexus.component role="com.opensymphony.xwork.Action" role-hint="projects"
 */
public class ProjectsListAction
    extends ContinuumActionSupport
{
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
        else if ("confirmRemove".equals( methodToCall ))
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
            for ( Iterator i = selectedProjects.iterator(); i.hasNext(); )
            {
                int projectId = Integer.parseInt( (String) i.next() );
                Project p = getContinuum().getProjectWithAllDetails( projectId );
                projectsList.add( p );
            }
            
            List<Project> sortedProjects;
            try
            {
                sortedProjects = getContinuum().getProjectsInBuildOrder( projectsList );
            }
            catch ( CycleDetectedException e )
            {
                sortedProjects = projectsList;
            }

            Map<Integer, Integer> projectsBuildDefsMap = getProjectsBuildDefsMap( sortedProjects );
            
            getContinuum().prepareBuildProjects( projectsBuildDefsMap );
            
            for ( Project project : sortedProjects )
            {
                if ( this.getBuildDefinitionId() <= 0 )
                {
                    getContinuum().buildProject( project.getId(), projectsBuildDefsMap.get( project.getId() ),
                                                 ContinuumProjectState.TRIGGER_FORCED );
                }
                else
                {
                    getContinuum().buildProject( project.getId(), this.getBuildDefinitionId(),
                                                 ContinuumProjectState.TRIGGER_FORCED );
                }
            }
            
            /*
            //TODO : Change this part because it's a duplicate of DefaultContinuum.buildProjectGroup*
            List<BuildDefinition> groupDefaultBDs = null;

            if ( getBuildDefinitionId() <= 0 )
            {
                groupDefaultBDs = getContinuum().getDefaultBuildDefinitionsForProjectGroup( projectGroupId );
            }
          
            
            for ( Iterator i = sortedProjects.iterator(); i.hasNext(); )
            {
                Project project = (Project) i.next();
                if ( this.getBuildDefinitionId() <= 0 )
                {
                    int buildDefId = -1;

                    if ( groupDefaultBDs != null )
                    {
                        for ( BuildDefinition bd : groupDefaultBDs )
                        {
                            if ( project.getExecutorId().equals( bd.getType() ) )
                            {
                                buildDefId = bd.getId();
                                break;
                            }
                        }
                    }

                    BuildDefinition projectDefaultBD = null;
                    if ( this.getBuildDefinitionId() == -1 )
                    {
                        try
                        {
                            projectDefaultBD = getContinuum().getDefaultBuildDefinition( project.getId() );
                        }
                        catch ( ContinuumException e )
                        {
                            // here skip ObjectNotException
                            getLogger().debug( e.getMessage() );
                        }

                        if ( projectDefaultBD != null )
                        {
                            buildDefId = projectDefaultBD.getId();
                            getLogger().debug( "Project " + project.getId() +
                                " has own default build definition, will use it instead of group's." );
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
            */
        }

        return SUCCESS;
    }
    
    private Map<Integer, Integer> getProjectsBuildDefsMap( List<Project> projects )
        throws ContinuumException
    {
        Map<Integer, Integer> projectsBuildDefsMap = new HashMap<Integer, Integer>();
        
        if ( this.getBuildDefinitionId() <= 0 )
        {
            boolean checkDefaultBuildDefinitionForProject = false;
            
            if ( this.getBuildDefinitionId() == -1 )
            {
                checkDefaultBuildDefinitionForProject = true;
            }
            
            List<BuildDefinition> groupDefaultBDs = getContinuum().getDefaultBuildDefinitionsForProjectGroup( projectGroupId );
            
            return getContinuum().getProjectsAndBuildDefinitionsMap( projects, 
                                                                     groupDefaultBDs, 
                                                                     checkDefaultBuildDefinitionForProject );
        }
        else
        {
            return getContinuum().getProjectsAndBuildDefinitionsMap( projects,
                                                                     this.getBuildDefinitionId() );
        }
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
