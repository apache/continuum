package org.apache.maven.continuum.web.action.admin;

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

import com.opensymphony.xwork2.Preparable;
import org.apache.commons.lang.StringUtils;
import org.apache.continuum.configuration.BuildAgentGroupConfiguration;
import org.apache.maven.continuum.installation.InstallationService;
import org.apache.maven.continuum.model.system.Installation;
import org.apache.maven.continuum.model.system.Profile;
import org.apache.maven.continuum.profile.AlreadyExistsProfileException;
import org.apache.maven.continuum.profile.ProfileException;
import org.apache.maven.continuum.profile.ProfileService;
import org.apache.maven.continuum.security.ContinuumRoleConstants;
import org.apache.maven.continuum.web.action.ContinuumActionSupport;
import org.codehaus.plexus.redback.rbac.Resource;
import org.codehaus.redback.integration.interceptor.SecureAction;
import org.codehaus.redback.integration.interceptor.SecureActionBundle;
import org.codehaus.redback.integration.interceptor.SecureActionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:olamy@codehaus.org">olamy</a>
 * @version $Id$
 * @plexus.component role="com.opensymphony.xwork2.Action" role-hint="profileAdministration"
 * @since 7 juin 07
 */
public class ProfileAction
    extends ContinuumActionSupport
    implements Preparable, SecureAction
{
    private static final Logger logger = LoggerFactory.getLogger( ProfileAction.class );

    /**
     * @plexus.requirement role-hint="default"
     */
    private ProfileService profileService;

    /**
     * @plexus.requirement role-hint="default"
     */
    private InstallationService installationService;

    private List<Profile> profiles;

    private Profile profile;

    private int installationId;

    private List<Installation> allInstallations;

    private List<Installation> profileInstallations;

    private List<BuildAgentGroupConfiguration> buildAgentGroups;

    public void prepare()
        throws Exception
    {
        super.prepare();

        List<BuildAgentGroupConfiguration> agentGroups = getContinuum().getConfiguration().getBuildAgentGroups();
        if ( agentGroups == null )
        {
            agentGroups = Collections.EMPTY_LIST;
        }
        this.setBuildAgentGroups( agentGroups );
    }

    // -------------------------------------------------------
    //  Webwork Methods
    // -------------------------------------------------------

    public String input()
        throws Exception
    {
        this.allInstallations = installationService.getAllInstallations();
        return INPUT;
    }

    public String list()
        throws Exception
    {
        this.profiles = profileService.getAllProfiles();
        return SUCCESS;
    }

    public String edit()
        throws Exception
    {
        if ( logger.isDebugEnabled() )
        {
            logger.debug( "edit profile with id " + profile.getId() );
        }
        this.profile = profileService.getProfile( profile.getId() );
        return SUCCESS;
    }

    public String save()
        throws Exception
    {
        try
        {
            Profile stored = profileService.getProfile( profile.getId() );

            if ( StringUtils.isBlank( profile.getName() ) )
            {
                if ( stored != null )
                {
                    profile = stored;
                }

                this.addFieldError( "profile.name", getResourceBundle().getString( "profile.name.required" ) );
                return INPUT;
            }

            if ( stored == null )
            {
                this.profile = profileService.addProfile( profile );
                this.allInstallations = installationService.getAllInstallations();
                return "editProfile";
            }
            else
            {
                // olamy : the only thing to change here is the profile name
                // but in the UI maybe some installations has been we retrieve it
                // and only set the name related to CONTINUUM-1361
                String name = profile.getName();
                String buildAgentGroup = profile.getBuildAgentGroup();

                profile = profileService.getProfile( profile.getId() );
                // CONTINUUM-1746 we update the profile only if the name has changed 
                // jancajas: added build agent group. updated profile if agent group is changed also.
                if ( !StringUtils.equals( name, profile.getName() ) || !StringUtils.equals( buildAgentGroup,
                                                                                            profile.getBuildAgentGroup() ) )
                {
                    profile.setName( name );
                    profile.setBuildAgentGroup( buildAgentGroup );
                    profileService.updateProfile( profile );
                }
            }
        }
        catch ( AlreadyExistsProfileException e )
        {
            this.addActionError( getResourceBundle().getString( "profile.name.already.exists" ) );
            return INPUT;
        }
        this.profiles = profileService.getAllProfiles();
        return SUCCESS;
    }

    public String delete()
        throws Exception
    {
        try
        {
            profileService.deleteProfile( profile.getId() );
            this.profiles = profileService.getAllProfiles();
        }
        catch ( ProfileException e )
        {
            // display action error in default/success page -- CONTINUUM-2250
            addActionError( getText( "profile.remove.error" ) );
        }
        return SUCCESS;
    }

    public String confirmDelete()
        throws ProfileException
    {
        this.profile = getContinuum().getProfileService().getProfile( profile.getId() );
        return SUCCESS;
    }

    public String addInstallation()
        throws Exception
    {
        Installation installation = installationService.getInstallation( this.getInstallationId() );
        if ( installation != null )
        {
            profileService.addInstallationInProfile( profile, installation );
            // read again
            this.profile = profileService.getProfile( profile.getId() );
        }
        return SUCCESS;
    }

    public String removeInstallation()
        throws Exception
    {

        Installation installation = installationService.getInstallation( this.getInstallationId() );
        profileService.removeInstallationFromProfile( profile, installation );
        this.profile = profileService.getProfile( profile.getId() );
        return SUCCESS;
    }

    // -----------------------------------------------------
    // security
    // -----------------------------------------------------    

    public SecureActionBundle getSecureActionBundle()
        throws SecureActionException
    {
        SecureActionBundle bundle = new SecureActionBundle();
        bundle.setRequiresAuthentication( true );
        bundle.addRequiredAuthorization( ContinuumRoleConstants.CONTINUUM_MANAGE_PROFILES, Resource.GLOBAL );

        return bundle;
    }

    // -------------------------------------------------------
    // Webwork setter/getter
    // -------------------------------------------------------

    public List<Profile> getProfiles()
    {
        return profiles;
    }

    public void setProfiles( List<Profile> profiles )
    {
        this.profiles = profiles;
    }

    public Profile getProfile()
    {
        return profile;
    }

    public void setProfile( Profile profile )
    {
        this.profile = profile;
    }

    public List<Installation> getAllInstallations()
        throws Exception
    {
        if ( this.allInstallations == null )
        {
            this.allInstallations = installationService.getAllInstallations();
        }
        // CONTINUUM-1742 (olamy) don't display already attached en var
        if ( this.profile != null )
        {
            this.allInstallations.removeAll( this.profile.getEnvironmentVariables() );
        }
        return allInstallations;
    }

    public void setAllInstallations( List<Installation> allInstallations )
    {
        this.allInstallations = allInstallations;
    }

    public List<Installation> getProfileInstallations()
    {
        if ( this.profile != null )
        {
            if ( this.profileInstallations == null )
            {
                this.profileInstallations = new ArrayList<Installation>();
                if ( this.profile.getJdk() != null )
                {
                    this.profileInstallations.add( this.profile.getJdk() );
                }
                if ( this.profile.getBuilder() != null )
                {
                    this.profileInstallations.add( this.profile.getBuilder() );
                }
                if ( this.profile.getEnvironmentVariables() != null &&
                    !this.profile.getEnvironmentVariables().isEmpty() )
                {
                    this.profileInstallations.addAll( this.profile.getEnvironmentVariables() );
                }
            }
            return profileInstallations;
        }
        return Collections.EMPTY_LIST;
    }

    public void setProfileInstallations( List<Installation> profileInstallations )
    {
        this.profileInstallations = profileInstallations;
    }

    public int getInstallationId()
    {
        return installationId;
    }

    public void setInstallationId( int installationId )
    {
        this.installationId = installationId;
    }

    public List<BuildAgentGroupConfiguration> getBuildAgentGroups()
    {
        return buildAgentGroups;
    }

    public void setBuildAgentGroups( List<BuildAgentGroupConfiguration> buildAgentGroups )
    {
        this.buildAgentGroups = buildAgentGroups;
    }
}
