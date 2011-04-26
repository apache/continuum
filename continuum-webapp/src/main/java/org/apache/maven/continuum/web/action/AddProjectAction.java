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
import java.util.List;

import org.apache.continuum.web.util.AuditLog;
import org.apache.continuum.web.util.AuditLogConstants;
import org.apache.continuum.web.util.RegexPatternConstants;
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

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.config.ConfigurationManager;
import com.opensymphony.xwork2.config.providers.XWorkConfigurationProvider;
import com.opensymphony.xwork2.inject.Container;
import com.opensymphony.xwork2.util.ValueStack;
import com.opensymphony.xwork2.util.ValueStackFactory;

/**
 * @author Nick Gonzalez
 * @version $Id$
 * @plexus.component role="com.opensymphony.xwork2.Action" role-hint="addProject"
 */
public class AddProjectAction
    extends ContinuumActionSupport
{
    private static final Logger logger = LoggerFactory.getLogger( AddProjectAction.class );

    private String projectName;

    private String projectDescription;

    private String projectVersion;

    private String projectScmUrl;

    private String projectScmUsername;

    private String projectScmPassword;

    private String projectScmTag;

    private String projectType;

    private Collection<ProjectGroup> projectGroups;

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

    private boolean emptyProjectGroups;

    public void validate()
    {
        clearErrorsAndMessages();
        initializeActionContext();
        
        try
        {
            if ( ( projectName != null ) && !( projectName.trim().length() > 0 ) )
            {
                addActionError( getText( "addProject.name.required" ) );
            }
            else if ( ( projectName != null ) && !( projectName.trim().matches( RegexPatternConstants.NAME_REGEX ) ) )
            {
                addActionError( getText( "addProject.name.invalid" ) );
            }
            if (( projectDescription != null ) && !( projectDescription.trim().matches( RegexPatternConstants.DESCRIPTION_REGEX ) ) )
            {
                addActionError( getText( "addProject.description.invalid" ) );
            }
            if ( ( projectVersion != null ) && !( projectVersion.trim().length() > 0 ) )
            {
                addActionError( getText( "addProject.version.required" ) );
            }
            else if ( ( projectVersion != null ) && !(projectVersion.trim().matches( RegexPatternConstants.VERSION_REGEX ) ) )
            {
                addActionError( getText( "addProject.version.invalid" ) );
            }
            if ( ( projectScmUrl != null ) && !( projectScmUrl.trim().length() > 0 ) )
            {
                addActionError( getText( "addProject.scmUrl.required" ) );
            }
            else if ( ( projectScmUrl != null ) && !( projectScmUrl.trim().matches( RegexPatternConstants.SCM_URL_REGEX ) ) )
            {
                addActionError( getText( "addProject.scmUrl.invalid" ) );
            }
            if ( ( projectScmTag != null ) && !( projectScmTag.trim().matches( RegexPatternConstants.SCM_URL_REGEX ) ) )
            {
                addActionError( getText( "addProject.scmTag.invalid" ) );
            }
            if ( isEmptyProjectGroups() )
            {
                addActionError( getText( "addProject.projectGroup.required" ) );
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
        initializeActionContext();
        
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
        //TODO: Instead of get all projects then test them, it would be better to check it directly in the DB
        for ( Project project : getContinuum().getProjects() )
        {
            // CONTINUUM-1445
            if ( StringUtils.equalsIgnoreCase( project.getName(), projectNameTrim ) &&
                StringUtils.equalsIgnoreCase( project.getVersion(), versionTrim ) &&
                StringUtils.equalsIgnoreCase( project.getScmUrl(), scmTrim ) )
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

        AuditLog event = new AuditLog( "Project id=" + project.getId(), AuditLogConstants.ADD_PROJECT );
        event.setCategory( AuditLogConstants.PROJECT );
        event.setCurrentUser( getPrincipal() );
        event.log();

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

        projectGroups = new ArrayList<ProjectGroup>();

        Collection<ProjectGroup> allProjectGroups = getContinuum().getAllProjectGroups();

        for ( ProjectGroup pg : allProjectGroups )
        {
            if ( isAuthorizedToAddProjectToGroup( pg.getName() ) )
            {
                projectGroups.add( pg );
            }
        }

        this.profiles = profileService.getAllProfiles();
        buildDefinitionTemplates = getContinuum().getBuildDefinitionService().getAllBuildDefinitionTemplate();
        return INPUT;
    }

    private void initializeProjectGroupName()
    {
        if ( disableGroupSelection )
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

    private void initializeActionContext()
    {
        // ctan: hack for WW-3161
        if ( ActionContext.getContext() == null )
        {
            // This fix allow initialization of ActionContext.getContext() to avoid NPE

            ConfigurationManager configurationManager = new ConfigurationManager();
            configurationManager.addContainerProvider( new XWorkConfigurationProvider() );
            com.opensymphony.xwork2.config.Configuration config = configurationManager.getConfiguration();
            Container container = config.getContainer();
    
            ValueStack stack = container.getInstance( ValueStackFactory.class ).createValueStack();
            stack.getContext().put( ActionContext.CONTAINER, container );
            ActionContext.setContext( new ActionContext( stack.getContext() ) );
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

    public Collection<ProjectGroup> getProjectGroups()
    {
        return projectGroups;
    }

    public void setProjectGroups( Collection<ProjectGroup> projectGroups )
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

    public boolean isEmptyProjectGroups()
    {
        return emptyProjectGroups;
    }

    public void setEmptyProjectGroups( boolean emptyProjectGroups )
    {
        this.emptyProjectGroups = emptyProjectGroups;
    }
}
