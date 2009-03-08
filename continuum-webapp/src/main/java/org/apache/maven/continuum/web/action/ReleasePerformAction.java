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
import org.apache.continuum.release.distributed.DistributedReleaseUtil;
import org.apache.continuum.release.distributed.manager.DistributedReleaseManager;
import org.apache.continuum.web.action.AbstractReleaseAction;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.system.Profile;
import org.apache.maven.continuum.release.ContinuumReleaseManager;
import org.apache.maven.continuum.release.ContinuumReleaseManagerListener;
import org.apache.maven.continuum.release.DefaultReleaseManagerListener;
import org.apache.maven.continuum.web.exception.AuthorizationRequiredException;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.shared.release.ReleaseResult;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Edwin Punzalan
 * @version $Id$
 * @plexus.component role="com.opensymphony.xwork2.Action" role-hint="releasePerform"
 */
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

        init();

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

        init();

        return SUCCESS;
    }

    private void getReleasePluginParameters( String workingDirectory, String pomFilename )
        throws Exception
    {
        //TODO: Use the model reader so we'll can get the plugin configuration from parent too
        MavenXpp3Reader pomReader = new MavenXpp3Reader();
        Model model = pomReader.read( new FileReader( new File( workingDirectory, pomFilename ) ) );

        if ( model.getBuild() != null && model.getBuild().getPlugins() != null )
        {
            for ( Plugin plugin : (List<Plugin>) model.getBuild().getPlugins() )
            {
                if ( plugin.getGroupId() != null && plugin.getGroupId().equals( "org.apache.maven.plugins" ) &&
                    plugin.getArtifactId() != null && plugin.getArtifactId().equals( "maven-release-plugin" ) )
                {
                    Xpp3Dom dom = (Xpp3Dom) plugin.getConfiguration();

                    if ( dom != null )
                    {
                        Xpp3Dom configuration = dom.getChild( "useReleaseProfile" );
                        if ( configuration != null )
                        {
                            useReleaseProfile = Boolean.valueOf( configuration.getValue() );
                        }

                        configuration = dom.getChild( "goals" );
                        if ( configuration != null )
                        {
                            goals = configuration.getValue();
                            if ( model.getDistributionManagement() != null &&
                                model.getDistributionManagement().getSite() != null )
                            {
                                goals += " site-deploy";
                            }
                        }

                        configuration = dom.getChild( "arguments" );
                        if ( configuration != null )
                        {
                            arguments = configuration.getValue();
                        }

                    }
                }
            }
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

        if ( getContinuum().getConfiguration().isDistributedBuildEnabled() )
        {
            DistributedReleaseManager releaseManager = getContinuum().getDistributedReleaseManager();

            releaseManager.releasePerform( releaseId, goals, arguments, useReleaseProfile, repository );
        }
        else
        {
            listener = new DefaultReleaseManagerListener();
    
            ContinuumReleaseManager releaseManager = getContinuum().getReleaseManager();

            //todo should be configurable
            File performDirectory = new File( getContinuum().getConfiguration().getWorkingDirectory(),
                                              "releases-" + System.currentTimeMillis() );
            performDirectory.mkdirs();

            releaseManager.perform( releaseId, performDirectory, goals, arguments, useReleaseProfile, listener,
                                    repository );
        }

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
            Map<String, String> environments = new HashMap<String, String>();
            
            if ( profileId != -1 )
            {
                Profile profile = getContinuum().getProfileService().getProfile( profileId );
                environments = getEnvironments( profile );
            }

            releaseManager.releasePerformFromScm( projectId, goals, arguments, useReleaseProfile, repository, scmUrl, 
                                                  scmUsername, scmPassword, scmTag, scmTagBase, environments );

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
                descriptor.setEnvironments( getEnvironments( profile ) );
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

        goals = DistributedReleaseUtil.getGoals( context, goals );

        arguments = DistributedReleaseUtil.getArguments( context, "" );
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
