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

import org.apache.continuum.model.repository.LocalRepository;
import org.apache.continuum.release.config.ContinuumReleaseDescriptor;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.system.Profile;
import org.apache.maven.continuum.release.ContinuumReleaseManager;
import org.apache.maven.continuum.release.ContinuumReleaseManagerListener;
import org.apache.maven.continuum.release.DefaultReleaseManagerListener;
import org.apache.maven.continuum.web.exception.AuthorizationRequiredException;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.shared.release.ReleaseResult;

import java.io.File;
import java.util.List;

/**
 * @author Edwin Punzalan
 * @plexus.component role="com.opensymphony.xwork.Action" role-hint="releasePerform"
 */
public class ReleasePerformAction
    extends ContinuumActionSupport
{
    private int projectId;

    private String releaseId;

    private String scmUrl;

    private String scmUsername;

    private String scmPassword;

    private String scmTag;

    private String scmTagBase;

    private String goals;

    private boolean useReleaseProfile;

    private ContinuumReleaseManagerListener listener;

    private ReleaseResult result;

    private String projectGroupName = "";

    private List<Profile> profiles;

    private int profileId;

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

        return SUCCESS;
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

        listener = new DefaultReleaseManagerListener();

        ContinuumReleaseManager releaseManager = getContinuum().getReleaseManager();

        Project project = getContinuum().getProject( projectId );

        //todo should be configurable
        File performDirectory = new File( getContinuum().getConfiguration().getWorkingDirectory(),
                                          "releases-" + System.currentTimeMillis() );
        performDirectory.mkdirs();
        
        LocalRepository repository = project.getProjectGroup().getLocalRepository();
        
        releaseManager.perform( releaseId, performDirectory, goals, useReleaseProfile, listener, repository );

        return SUCCESS;
    }

    public String executeFromScm()
        throws Exception
    {
        ContinuumReleaseManager releaseManager = getContinuum().getReleaseManager();

        ContinuumReleaseDescriptor descriptor = new ContinuumReleaseDescriptor();
        descriptor.setScmSourceUrl( scmUrl );
        descriptor.setScmUsername( scmUsername );
        descriptor.setScmPassword( scmPassword );
        descriptor.setScmReleaseLabel( scmTag );
        descriptor.setScmTagBase( scmTagBase );
        
        Profile profile = null;
        
        if ( profileId != -1 )
        {
            profile = getContinuum().getProfileService().getProfile( profileId );
            descriptor.setEnvironments( releaseManager.getEnvironments( profile ) );
        }

        do
        {
            releaseId = String.valueOf( System.currentTimeMillis() );
        }
        while ( releaseManager.getPreparedReleases().containsKey( releaseId ) );

        releaseManager.getPreparedReleases().put( releaseId, descriptor );

        return execute();
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
    
}