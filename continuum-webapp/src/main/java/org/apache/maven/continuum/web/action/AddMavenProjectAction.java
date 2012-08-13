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

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.config.ConfigurationManager;
import com.opensymphony.xwork2.config.providers.XWorkConfigurationProvider;
import com.opensymphony.xwork2.inject.Container;
import com.opensymphony.xwork2.util.ValueStack;
import com.opensymphony.xwork2.util.ValueStackFactory;
import org.apache.commons.io.FileUtils;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.builddefinition.BuildDefinitionServiceException;
import org.apache.maven.continuum.model.project.BuildDefinitionTemplate;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.apache.maven.continuum.web.exception.AuthorizationRequiredException;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

/**
 * Action to add a Maven project to Continuum, either Maven 1 or Maven 2.
 *
 * @author <a href="mailto:carlos@apache.org">Carlos Sanchez</a>
 * @version $Id$
 */
public abstract class AddMavenProjectAction
    extends ContinuumActionSupport
    implements ServletRequestAware
{
    private static final long serialVersionUID = -3965565189557706469L;

    private static final int DEFINED_BY_POM_GROUP_ID = -1;

    private String pomUrl;

    private File pomFile;

    private String pom = null;

    private String scmUsername;

    private String scmPassword;

    private Collection<ProjectGroup> projectGroups;

    private String projectGroupName;

    private int selectedProjectGroup = DEFINED_BY_POM_GROUP_ID;

    private boolean disableGroupSelection;

    private boolean scmUseCache;

    private int projectGroupId;

    private List<BuildDefinitionTemplate> buildDefinitionTemplates;

    private int buildDefinitionTemplateId;

    private List<String> errorMessages = new ArrayList<String>();

    private HttpServletRequest httpServletRequest;

    public String execute()
        throws ContinuumException, BuildDefinitionServiceException
    {
        try
        {
            initializeProjectGroupName();

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

        boolean checkProtocol = true;

        if ( !StringUtils.isEmpty( pomUrl ) )
        {
            try
            {
                URL url = new URL( pomUrl );
                if ( pomUrl.startsWith( "http" ) && !StringUtils.isEmpty( scmUsername ) )
                {
                    String encoding = this.httpServletRequest.getCharacterEncoding();
                    if ( StringUtils.isEmpty( encoding ) )
                    {
                        encoding = System.getProperty( "file.encoding" );
                    }

                    String encodedUsername = URLEncoder.encode( scmUsername, encoding );
                    String encodedPassword = URLEncoder.encode( scmPassword, encoding );

                    StringBuffer urlBuffer = new StringBuffer();
                    urlBuffer.append( url.getProtocol() ).append( "://" );
                    urlBuffer.append( encodedUsername ).append( ':' ).append( encodedPassword ).append( '@' ).append(
                        url.getHost() );
                    if ( url.getPort() != -1 )
                    {
                        urlBuffer.append( ":" ).append( url.getPort() );
                    }
                    urlBuffer.append( url.getPath() );

                    pom = urlBuffer.toString();
                }
                else
                {
                    pom = pomUrl;
                }
            }
            catch ( MalformedURLException e )
            {
                addActionError( getText( "add.project.unknown.error" ) );
                return doDefault();
            }
            catch ( UnsupportedEncodingException e )
            {
                addActionError( getText( "add.project.unknown.error" ) );
                return doDefault();
            }

        }
        else
        {
            if ( pomFile != null )
            {
                try
                {
                    //pom = pomFile.toURL().toString();
                    checkProtocol = false;
                    // CONTINUUM-1897
                    // File.c copyFile to tmp one
                    File tmpPom = File.createTempFile( "continuum_tmp", "tmp" );
                    FileUtils.copyFile( pomFile, tmpPom );
                    pom = tmpPom.toURL().toString();
                }
                catch ( MalformedURLException e )
                {
                    // if local file can't be converted to url it's an internal error
                    throw new RuntimeException( e );
                }
                catch ( IOException e )
                {
                    throw new RuntimeException( e );
                }
            }
            else
            {
                // no url or file was filled
                addActionError( getText( "add.project.field.required.error" ) );
                return doDefault();
            }
        }

        ContinuumProjectBuildingResult result = doExecute( pom, selectedProjectGroup, checkProtocol, scmUseCache );

        if ( result.hasErrors() )
        {
            for ( String key : result.getErrors() )
            {
                String cause = result.getErrorsWithCause().get( key );
                String msg = getText( key, new String[]{cause} );

                // olamy : weird getText(key, String[]) must do that something like bla bla {0}
                // here an ugly hack for CONTINUUM-1675
                if ( key.equals( ContinuumProjectBuildingResult.ERROR_MISSING_SCM ) )
                {
                    msg = getResourceBundle().getString( key ) + " " + cause;
                }
                if ( !StringUtils.equals( msg, key ) )
                {
                    errorMessages.add( msg );
                }
                else
                {
                    addActionError( msg );
                }

            }

            return doDefault();
        }

        if ( this.getSelectedProjectGroup() > 0 )
        {
            this.setProjectGroupId( this.getSelectedProjectGroup() );
            return "projectGroupSummary";
        }

        if ( result.getProjectGroups() != null && !result.getProjectGroups().isEmpty() )
        {
            this.setProjectGroupId( ( result.getProjectGroups().get( 0 ) ).getId() );
            return "projectGroupSummary";
        }

        return SUCCESS;
    }

    /**
     * Subclasses must implement this method calling the appropiate operation on the continuum service.
     *
     * @param pomUrl               url of the pom specified by the user
     * @param selectedProjectGroup project group id selected by the user
     * @param checkProtocol        check if the protocol is allowed, use false if the pom is uploaded
     * @return result of adding the pom to continuum
     */
    protected abstract ContinuumProjectBuildingResult doExecute( String pomUrl, int selectedProjectGroup,
                                                                 boolean checkProtocol, boolean scmUseCache )
        throws ContinuumException;

    // TODO: Remove this method because a default method return SUCCESS instead of INPUT
    public String doDefault()
        throws BuildDefinitionServiceException
    {
        return input();
    }

    public String input()
        throws BuildDefinitionServiceException
    {
        try
        {
            initializeProjectGroupName();

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
        Collection<ProjectGroup> allProjectGroups = getContinuum().getAllProjectGroups();
        projectGroups = new ArrayList<ProjectGroup>();

        ProjectGroup defaultGroup = new ProjectGroup();
        defaultGroup.setId( DEFINED_BY_POM_GROUP_ID );
        defaultGroup.setName( "Defined by POM" );
        projectGroups.add( defaultGroup );

        for ( ProjectGroup pg : allProjectGroups )
        {
            if ( isAuthorizedToAddProjectToGroup( pg.getName() ) )
            {
                projectGroups.add( pg );
            }
        }

        initializeProjectGroupName();
        this.setBuildDefinitionTemplates( getContinuum().getBuildDefinitionService().getAllBuildDefinitionTemplate() );
        return INPUT;
    }

    protected String hidePasswordInUrl( String url )
    {
        int indexAt = url.indexOf( "@" );

        if ( indexAt < 0 )
        {
            return url;
        }

        String s = url.substring( 0, indexAt );

        int pos = s.lastIndexOf( ":" );

        return s.substring( 0, pos + 1 ) + "*****" + url.substring( indexAt );
    }

    private void initializeProjectGroupName()
    {
        if ( disableGroupSelection && selectedProjectGroup != DEFINED_BY_POM_GROUP_ID )
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

    public String getPom()
    {
        return pom;
    }

    public void setPom( String pom )
    {
        this.pom = pom;
    }

    public File getPomFile()
    {
        return pomFile;
    }

    public void setPomFile( File pomFile )
    {
        this.pomFile = pomFile;
    }

    public String getPomUrl()
    {
        return pomUrl;
    }

    public void setPomUrl( String pomUrl )
    {
        this.pomUrl = pomUrl;
    }

    public void setScmPassword( String scmPassword )
    {
        this.scmPassword = scmPassword;
    }

    public String getScmUsername()
    {
        return scmUsername;
    }

    public void setScmUsername( String scmUsername )
    {
        this.scmUsername = scmUsername;
    }

    public Collection getProjectGroups()
    {
        return projectGroups;
    }

    public String getProjectGroupName()
    {
        return projectGroupName;
    }

    public void setProjectGroupName( String projectGroupName )
    {
        this.projectGroupName = projectGroupName;
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
        return this.disableGroupSelection;
    }

    public void setDisableGroupSelection( boolean disableGroupSelection )
    {
        this.disableGroupSelection = disableGroupSelection;
    }

    public boolean isScmUseCache()
    {
        return scmUseCache;
    }

    public void setScmUseCache( boolean scmUseCache )
    {
        this.scmUseCache = scmUseCache;
    }

    public int getProjectGroupId()
    {
        return projectGroupId;
    }

    public void setProjectGroupId( int projectGroupId )
    {
        this.projectGroupId = projectGroupId;
    }

    public List<BuildDefinitionTemplate> getBuildDefinitionTemplates()
    {
        return buildDefinitionTemplates;
    }

    public void setBuildDefinitionTemplates( List<BuildDefinitionTemplate> buildDefinitionTemplates )
    {
        this.buildDefinitionTemplates = buildDefinitionTemplates;
    }

    public int getBuildDefinitionTemplateId()
    {
        return buildDefinitionTemplateId;
    }

    public void setBuildDefinitionTemplateId( int buildDefinitionTemplateId )
    {
        this.buildDefinitionTemplateId = buildDefinitionTemplateId;
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

    public List<String> getErrorMessages()
    {
        return errorMessages;
    }

    public void setErrorMessages( List<String> errorMessages )
    {
        this.errorMessages = errorMessages;
    }

    public void setServletRequest( HttpServletRequest httpServletRequest )
    {
        this.httpServletRequest = httpServletRequest;
    }
}
