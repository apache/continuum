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

import org.apache.continuum.configuration.BuildAgentConfigurationException;
import org.apache.continuum.model.repository.LocalRepository;
import org.apache.continuum.release.config.ContinuumReleaseDescriptor;
import org.apache.continuum.release.distributed.DistributedReleaseUtil;
import org.apache.continuum.release.distributed.manager.DistributedReleaseManager;
import org.apache.continuum.utils.release.ReleaseUtil;
import org.apache.continuum.web.action.AbstractReleaseAction;
import org.apache.continuum.web.util.AuditLog;
import org.apache.continuum.web.util.AuditLogConstants;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.system.Profile;
import org.apache.maven.continuum.release.ContinuumReleaseManager;
import org.apache.maven.continuum.release.ContinuumReleaseManagerListener;
import org.apache.maven.continuum.release.DefaultReleaseManagerListener;
import org.apache.maven.continuum.web.exception.AuthorizationRequiredException;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.shared.release.ReleaseResult;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Edwin Punzalan
 */
@Component( role = com.opensymphony.xwork2.Action.class, hint = "releasePerform", instantiationStrategy = "per-lookup" )
public class ReleasePerformAction
    extends AbstractReleaseAction
{
    private int projectId;

    private String releaseId;

    private String scmUrl;

    private String scmUsername;

    private String scmPassword;

    private String scmTag;

    private String scmTagBase;

    private String goals = "clean deploy";

    private String arguments;

    private boolean useReleaseProfile = true;

    private ContinuumReleaseManagerListener listener;

    private ReleaseResult result;

    private String projectGroupName = "";

    private List<Profile> profiles;

    private int profileId;

    private void init()
        throws Exception
    {
        if ( getContinuum().getConfiguration().isDistributedBuildEnabled() )
        {
            DistributedReleaseManager distributedReleaseManager = getContinuum().getDistributedReleaseManager();

            getReleasePluginParameters( distributedReleaseManager.getReleasePluginParameters( projectId, "pom.xml" ) );
        }
        else
        {
            Project project = getContinuum().getProject( projectId );

            String workingDirectory = getContinuum().getWorkingDirectory( project.getId() ).getPath();

            getReleasePluginParameters( workingDirectory, "pom.xml" );
        }
    }

    public String inputFromScm()
        throws Exception
    {
        try
        {
            checkBuildProjectInGroupAuthorization( getProjectGroupName() );
        }
        catch ( AuthorizationRequiredException e )
        {
            return REQUIRES_AUTHORIZATION;
        }

        try
        {
            init();
        }
        catch ( BuildAgentConfigurationException e )
        {
            List<Object> args = new ArrayList<Object>();
            args.add( e.getMessage() );

            addActionError( getText( "distributedBuild.releasePerform.input.error", args ) );
            return RELEASE_ERROR;
        }

        populateFromProject();

        releaseId = "";

        profiles = this.getContinuum().getProfileService().getAllProfiles();

        return SUCCESS;
    }

    public String input()
        throws Exception
    {
        try
        {
            checkBuildProjectInGroupAuthorization( getProjectGroupName() );
        }
        catch ( AuthorizationRequiredException e )
        {
            return REQUIRES_AUTHORIZATION;
        }

        try
        {
            init();
        }
        catch ( BuildAgentConfigurationException e )
        {
            List<Object> args = new ArrayList<Object>();
            args.add( e.getMessage() );

            addActionError( getText( "distributedBuild.releasePerform.input.error", args ) );
            return RELEASE_ERROR;
        }

        return SUCCESS;
    }

    /**
     * FIXME olamy is it really the good place to do that ? should be moved to continuum-release
     * TODO handle remoteTagging
     */
    private void getReleasePluginParameters( String workingDirectory, String pomFilename )
        throws Exception
    {
        Map<String, Object> params = ReleaseUtil.getReleasePluginParameters( workingDirectory, pomFilename );

        if ( params.get( "use-release-profile" ) != null )
        {
            useReleaseProfile = (Boolean) params.get( "use-release-profile" );
        }

        if ( params.get( "perform-goals" ) != null )
        {
            goals = (String) params.get( "perform-goals" );
        }

        if ( params.get( "arguments" ) != null )
        {
            arguments = (String) params.get( "arguments" );
        }
    }

    public String execute()
        throws Exception
    {
        try
        {
            checkBuildProjectInGroupAuthorization( getProjectGroupName() );
        }
        catch ( AuthorizationRequiredException e )
        {
            return REQUIRES_AUTHORIZATION;
        }

        Project project = getContinuum().getProject( projectId );

        LocalRepository repository = project.getProjectGroup().getLocalRepository();

        String username = getPrincipal();

        if ( getContinuum().getConfiguration().isDistributedBuildEnabled() )
        {
            DistributedReleaseManager releaseManager = getContinuum().getDistributedReleaseManager();

            try
            {
                releaseManager.releasePerform( projectId, releaseId, goals, arguments, useReleaseProfile, repository,
                                               username );
            }
            catch ( BuildAgentConfigurationException e )
            {
                List<Object> args = new ArrayList<Object>();
                args.add( e.getMessage() );

                addActionError( getText( "distributedBuild.releasePerform.release.error", args ) );
                return RELEASE_ERROR;
            }
        }
        else
        {
            listener = new DefaultReleaseManagerListener();

            listener.setUsername( username );

            ContinuumReleaseManager releaseManager = getContinuum().getReleaseManager();

            //todo should be configurable
            File performDirectory = new File( getContinuum().getConfiguration().getWorkingDirectory(),
                                              "releases-" + System.currentTimeMillis() );
            performDirectory.mkdirs();

            releaseManager.perform( releaseId, performDirectory, goals, arguments, useReleaseProfile, listener,
                                    repository );
        }

        AuditLog event = new AuditLog( "ReleaseId=" + releaseId, AuditLogConstants.PERFORM_RELEASE );
        event.setCategory( AuditLogConstants.PROJECT );
        event.setCurrentUser( username );
        event.log();

        return SUCCESS;
    }

    public String executeFromScm()
        throws Exception
    {
        if ( getContinuum().getConfiguration().isDistributedBuildEnabled() )
        {
            Project project = getContinuum().getProject( projectId );

            LocalRepository repository = project.getProjectGroup().getLocalRepository();

            DistributedReleaseManager releaseManager = getContinuum().getDistributedReleaseManager();

            Profile profile = null;
            if ( profileId != -1 )
            {
                profile = getContinuum().getProfileService().getProfile( profileId );
            }
            Map<String, String> environments =
                getEnvironments( profile, releaseManager.getDefaultBuildagent( projectId ) );

            try
            {
                releaseId = releaseManager.releasePerformFromScm( projectId, goals, arguments, useReleaseProfile,
                                                                  repository, scmUrl, scmUsername, scmPassword, scmTag,
                                                                  scmTagBase, environments, getPrincipal() );
            }
            catch ( BuildAgentConfigurationException e )
            {
                List<Object> args = new ArrayList<Object>();
                args.add( e.getMessage() );

                addActionError( getText( "distributedBuild.releasePerform.release.error", args ) );
                return RELEASE_ERROR;
            }

            return SUCCESS;
        }
        else
        {
            ContinuumReleaseManager releaseManager = getContinuum().getReleaseManager();

            ContinuumReleaseDescriptor descriptor = new ContinuumReleaseDescriptor();
            descriptor.setScmSourceUrl( scmUrl );
            descriptor.setScmUsername( scmUsername );
            descriptor.setScmPassword( scmPassword );
            descriptor.setScmReleaseLabel( scmTag );
            descriptor.setScmTagBase( scmTagBase );

            if ( profileId != -1 )
            {
                Profile profile = getContinuum().getProfileService().getProfile( profileId );
                descriptor.setEnvironments( getEnvironments( profile, null ) );
            }

            do
            {
                releaseId = String.valueOf( System.currentTimeMillis() );
            }
            while ( releaseManager.getPreparedReleases().containsKey( releaseId ) );

            releaseManager.getPreparedReleases().put( releaseId, descriptor );

            return execute();
        }
    }

    private void populateFromProject()
        throws Exception
    {
        Project project = getContinuum().getProjectWithAllDetails( projectId );

        scmUrl = project.getScmUrl();
        scmUsername = project.getScmUsername();
        scmPassword = project.getScmPassword();

        if ( scmUrl.startsWith( "scm:svn:" ) )
        {
            scmTagBase = new SvnScmProviderRepository( scmUrl, scmUsername, scmPassword ).getTagBase();
        }
        else
        {
            scmTagBase = "";
        }

        releaseId = "";
    }

    private void getReleasePluginParameters( Map context )
    {
        useReleaseProfile = DistributedReleaseUtil.getUseReleaseProfile( context, useReleaseProfile );

        if ( StringUtils.isNotEmpty( DistributedReleaseUtil.getPerformGoals( context, goals ) ) )
        {
            goals = DistributedReleaseUtil.getPerformGoals( context, goals );
        }

        if ( StringUtils.isNotEmpty( DistributedReleaseUtil.getArguments( context, "" ) ) )
        {
            arguments = DistributedReleaseUtil.getArguments( context, "" );
        }
    }

    public String getReleaseId()
    {
        return releaseId;
    }

    public void setReleaseId( String releaseId )
    {
        this.releaseId = releaseId;
    }

    public String getScmUrl()
    {
        return scmUrl;
    }

    public void setScmUrl( String scmUrl )
    {
        this.scmUrl = scmUrl;
    }

    public String getScmUsername()
    {
        return scmUsername;
    }

    public void setScmUsername( String scmUsername )
    {
        this.scmUsername = scmUsername;
    }

    public String getScmPassword()
    {
        return scmPassword;
    }

    public void setScmPassword( String scmPassword )
    {
        this.scmPassword = scmPassword;
    }

    public String getScmTag()
    {
        return scmTag;
    }

    public void setScmTag( String scmTag )
    {
        this.scmTag = scmTag;
    }

    public String getScmTagBase()
    {
        return scmTagBase;
    }

    public void setScmTagBase( String scmTagBase )
    {
        this.scmTagBase = scmTagBase;
    }

    public String getGoals()
    {
        return goals;
    }

    public void setGoals( String goals )
    {
        this.goals = goals;
    }

    public boolean isUseReleaseProfile()
    {
        return useReleaseProfile;
    }

    public void setUseReleaseProfile( boolean useReleaseProfile )
    {
        this.useReleaseProfile = useReleaseProfile;
    }

    public ContinuumReleaseManagerListener getListener()
    {
        return listener;
    }

    public void setListener( ContinuumReleaseManagerListener listener )
    {
        this.listener = listener;
    }

    public ReleaseResult getResult()
    {
        return result;
    }

    public void setResult( ReleaseResult result )
    {
        this.result = result;
    }

    public int getProjectId()
    {
        return projectId;
    }

    public void setProjectId( int projectId )
    {
        this.projectId = projectId;
    }

    public String getProjectGroupName()
        throws ContinuumException
    {
        if ( projectGroupName == null || "".equals( projectGroupName ) )
        {
            projectGroupName = getContinuum().getProjectGroupByProjectId( projectId ).getName();
        }

        return projectGroupName;
    }

    public List<Profile> getProfiles()
    {
        return profiles;
    }

    public void setProfiles( List<Profile> profiles )
    {
        this.profiles = profiles;
    }

    public int getProfileId()
    {
        return profileId;
    }

    public void setProfileId( int profileId )
    {
        this.profileId = profileId;
    }

    public String getArguments()
    {
        return arguments;
    }

    public void setArguments( String arguments )
    {
        this.arguments = arguments;
    }
}
