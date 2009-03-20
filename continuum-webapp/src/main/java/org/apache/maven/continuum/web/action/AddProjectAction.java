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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.builddefinition.BuildDefinitionServiceException;
import org.apache.maven.continuum.model.project.BuildDefinitionTemplate;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.system.Profile;
import org.apache.maven.continuum.profile.ProfileException;
import org.apache.maven.continuum.profile.ProfileService;
import org.apache.maven.continuum.web.exception.AuthorizationRequiredException;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nick Gonzalez
 * @version $Id$
 * @plexus.component role="com.opensymphony.xwork2.Action" role-hint="addProject"
 */
public class AddProjectAction
    extends ContinuumActionSupport
{
    private Logger logger = LoggerFactory.getLogger( this.getClass() );

    private String projectName;

    private String projectDescription;

    private String projectVersion;

    private String projectScmUrl;

    private String projectScmUsername;

    private String projectScmPassword;

    private String projectScmTag;

    private String projectType;

    private Collection projectGroups;

    private int selectedProjectGroup;

    private String projectGroupName;

    private boolean disableGroupSelection;

    private boolean projectScmUseCache;

    private List<Profile> profiles;

    /**
     * @plexus.requirement role-hint="default"
     */
    private ProfileService profileService;

    private int projectGroupId;

    private int buildDefintionTemplateId;

    private List<BuildDefinitionTemplate> buildDefinitionTemplates;

    public void validate()
    {
        clearErrorsAndMessages();
        try
        {
            if ( !( projectName.trim().length() > 0 ) )
            {
                addActionError( getText( "addProject.name.required" ) );
            }
            if ( !( projectVersion.trim().length() > 0 ) )
            {
                addActionError( getText( "addProject.version.required" ) );
            }
            if ( !( projectScmUrl.trim().length() > 0 ) )
            {
                addActionError( getText( "addProject.scmUrl.required" ) );
            }
            if ( hasActionErrors() )
            {
                input();
            }
        }
        catch ( ContinuumException e )
        {
            logger.error( e.getMessage(), e );
        }
        catch ( BuildDefinitionServiceException e )
        {
            logger.error( e.getMessage(), e );
        }
    }

    public String add()
        throws ContinuumException
    {
        initializeProjectGroupName();

        try
        {
            if ( StringUtils.isEmpty( getProjectGroupName() ) )
            {
                checkAddProjectGroupAuthorization();
            }
            else
            {
                checkAddProjectToGroupAuthorization( getProjectGroupName() );
            }
        }
        catch ( AuthorizationRequiredException authzE )
        {
            addActionError( authzE.getMessage() );
            return REQUIRES_AUTHORIZATION;
        }

        String projectNameTrim = projectName.trim();
        String versionTrim = projectVersion.trim();
        String scmTrim = projectScmUrl.trim();
        for ( Project project : getContinuum().getProjects() )
        {
            // CONTINUUM-1445
            if ( StringUtils.equalsIgnoreCase( project.getName(), projectNameTrim )
                && StringUtils.equalsIgnoreCase( project.getVersion(), versionTrim )
                && StringUtils.equalsIgnoreCase( project.getScmUrl(), scmTrim ) )
            {
                addActionError( getText( "projectName.already.exist.error" ) );
                return INPUT;
            }
        }

        Project project = new Project();

        project.setName( projectNameTrim );

        if ( projectDescription != null )
        {
            project.setDescription( projectDescription.trim() );
        }

        project.setVersion( versionTrim );

        project.setScmUrl( scmTrim );

        project.setScmUsername( projectScmUsername );

        project.setScmPassword( projectScmPassword );

        project.setScmTag( projectScmTag );

        project.setScmUseCache( projectScmUseCache );

        project.setExecutorId( projectType );

        getContinuum().addProject( project, projectType, selectedProjectGroup, this.getBuildDefintionTemplateId() );

        if ( this.getSelectedProjectGroup() > 0 )
        {
            this.setProjectGroupId( this.getSelectedProjectGroup() );
            return "projectGroupSummary";
        }

        return SUCCESS;
    }

    public String input()
        throws ContinuumException, ProfileException, BuildDefinitionServiceException
    {
        try
        {
            if ( StringUtils.isEmpty( getProjectGroupName() ) )
            {
                checkAddProjectGroupAuthorization();
            }
            else
            {
                checkAddProjectToGroupAuthorization( getProjectGroupName() );
            }
        }
        catch ( AuthorizationRequiredException authzE )
        {
            addActionError( authzE.getMessage() );
            return REQUIRES_AUTHORIZATION;
        }

        projectGroups = new ArrayList();

        Collection allProjectGroups = getContinuum().getAllProjectGroups();

        for ( Iterator i = allProjectGroups.iterator(); i.hasNext(); )
        {
            ProjectGroup pg = (ProjectGroup) i.next();

            if ( isAuthorizedToAddProjectToGroup( pg.getName() ) )
            {
                projectGroups.add( pg );
            }
        }

        if ( !disableGroupSelection )
        {
            selectedProjectGroup =
                getContinuum().getProjectGroupByGroupId( Continuum.DEFAULT_PROJECT_GROUP_GROUP_ID ).getId();
        }
        this.profiles = profileService.getAllProfiles();
        buildDefinitionTemplates = getContinuum().getBuildDefinitionService().getAllBuildDefinitionTemplate();
        return INPUT;
    }

    private void initializeProjectGroupName()
    {
        if ( disableGroupSelection == true )
        {
            try
            {
                projectGroupName = getContinuum().getProjectGroup( selectedProjectGroup ).getName();
            }
            catch ( ContinuumException e )
            {
                e.printStackTrace();
            }
        }
    }

    public String getProjectName()
    {
        return projectName;
    }

    public void setProjectName( String projectName )
    {
        this.projectName = projectName;
    }

    public String getProjectScmPassword()
    {
        return projectScmPassword;
    }

    public void setProjectScmPassword( String projectScmPassword )
    {
        this.projectScmPassword = projectScmPassword;
    }

    public String getProjectScmTag()
    {
        return projectScmTag;
    }

    public void setProjectScmTag( String projectScmTag )
    {
        this.projectScmTag = projectScmTag;
    }

    public String getProjectScmUrl()
    {
        return projectScmUrl;
    }

    public void setProjectScmUrl( String projectScmUrl )
    {
        this.projectScmUrl = projectScmUrl;
    }

    public String getProjectScmUsername()
    {
        return projectScmUsername;
    }

    public void setProjectScmUsername( String projectScmUsername )
    {
        this.projectScmUsername = projectScmUsername;
    }

    public String getProjectType()
    {
        return projectType;
    }

    public void setProjectType( String projectType )
    {
        this.projectType = projectType;
    }

    public String getProjectVersion()
    {
        return projectVersion;
    }

    public void setProjectVersion( String projectVersion )
    {
        this.projectVersion = projectVersion;
    }

    public Collection getProjectGroups()
    {
        return projectGroups;
    }

    public void setProjectGroups( Collection projectGroups )
    {
        this.projectGroups = projectGroups;
    }

    public int getSelectedProjectGroup()
    {
        return selectedProjectGroup;
    }

    public void setSelectedProjectGroup( int selectedProjectGroup )
    {
        this.selectedProjectGroup = selectedProjectGroup;
    }

    public boolean isDisableGroupSelection()
    {
        return disableGroupSelection;
    }

    public void setDisableGroupSelection( boolean disableGroupSelection )
    {
        this.disableGroupSelection = disableGroupSelection;
    }

    public String getProjectGroupName()
    {
        return projectGroupName;
    }

    public void setProjectGroupName( String projectGroupName )
    {
        this.projectGroupName = projectGroupName;
    }

    public boolean isProjectScmUseCache()
    {
        return projectScmUseCache;
    }

    public void setProjectScmUseCache( boolean projectScmUseCache )
    {
        this.projectScmUseCache = projectScmUseCache;
    }

    public List<Profile> getProfiles()
    {
        return profiles;
    }

    public void setProfiles( List<Profile> profiles )
    {
        this.profiles = profiles;
    }

    public int getProjectGroupId()
    {
        return projectGroupId;
    }

    public void setProjectGroupId( int projectGroupId )
    {
        this.projectGroupId = projectGroupId;
    }

    public int getBuildDefintionTemplateId()
    {
        return buildDefintionTemplateId;
    }

    public void setBuildDefintionTemplateId( int buildDefintionTemplateId )
    {
        this.buildDefintionTemplateId = buildDefintionTemplateId;
    }

    public List<BuildDefinitionTemplate> getBuildDefinitionTemplates()
    {
        return buildDefinitionTemplates;
    }

    public void setBuildDefinitionTemplates( List<BuildDefinitionTemplate> buildDefinitionTemplates )
    {
        this.buildDefinitionTemplates = buildDefinitionTemplates;
    }

    private boolean isAuthorizedToAddProjectToGroup( String projectGroupName )
    {
        try
        {
            checkAddProjectToGroupAuthorization( projectGroupName );
            return true;
        }
        catch ( AuthorizationRequiredException authzE )
        {
            return false;
        }
    }

    public String getProjectDescription()
    {
        return projectDescription;
    }

    public void setProjectDescription( String projectDescription )
    {
        this.projectDescription = projectDescription;
    }
}
