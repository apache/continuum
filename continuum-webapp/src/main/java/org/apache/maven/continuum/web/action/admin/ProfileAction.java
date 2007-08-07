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

import org.apache.maven.continuum.installation.InstallationService;
import org.apache.maven.continuum.model.system.Installation;
import org.apache.maven.continuum.model.system.Profile;
import org.apache.maven.continuum.profile.ProfileService;
import org.apache.maven.continuum.web.action.ContinuumActionSupport;
import org.codehaus.plexus.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

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
/**
 * @author <a href="mailto:olamy@codehaus.org">olamy</a>
 * @version $Id$
 * @plexus.component role="com.opensymphony.xwork.Action" role-hint="profileAdministration"
 * @since 7 juin 07
 */
public class ProfileAction
    extends ContinuumActionSupport

{
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

    private String installationName;

    private List<Installation> allInstallations;

    private List<Installation> profileInstallations;

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
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "edit profile with id " + profile.getId() );
        }
        this.profile = profileService.getProfile( profile.getId() );
        return SUCCESS;
    }

    public String save()
        throws Exception
    {
        Profile stored = profileService.getProfile( profile.getId() );
        if ( stored == null )
        {
            this.profile = profileService.addProfile( profile );
            this.allInstallations = installationService.getAllInstallations();
            return "editProfile";
        }
        else
        {
            // olamy : the only this to change here is the profile
            // but in the UI maybe some installations has been we retrieve it
            // and only set the name
            String name = profile.getName();
            profile = profileService.getProfile( profile.getId() );
            profile.setName( name );
            profileService.updateProfile( profile );
        }
        this.profiles = profileService.getAllProfiles();
        return SUCCESS;
    }

    public String delete()
        throws Exception
    {
        profileService.deleteProfile( profile.getId() );
        this.profiles = profileService.getAllProfiles();
        return SUCCESS;
    }

    public String addInstallation()
        throws Exception
    {
        Installation installation = installationService.getInstallation( this.installationName );
        if ( InstallationService.JDK_TYPE.equals( installation.getType() ) )
        {
            profileService.setJdkInProfile( profile, installation );
        }
        else if ( InstallationService.MAVEN1_TYPE.equals( installation.getType() ) ||
            InstallationService.MAVEN2_TYPE.equals( installation.getType() ) ||
            InstallationService.ANT_TYPE.equals( installation.getType() ) )
        {
            profileService.setBuilderInProfile( profile, installation );
        }
        else
        {
            profileService.addEnvVarInProfile( profile, installation );
        }
        this.profile = profileService.getProfile( profile.getId() );
        return SUCCESS;
    }

    public String removeInstallation()
        throws Exception
    {

        Installation installation = installationService.getInstallation( this.installationName );
        Profile stored = profileService.getProfile( profile.getId() );
        if ( InstallationService.JDK_TYPE.equals( installation.getType() ) )
        {
            stored.setJdk( null );
        }
        else if ( InstallationService.MAVEN1_TYPE.equals( installation.getType() ) ||
            InstallationService.MAVEN2_TYPE.equals( installation.getType() ) ||
            InstallationService.ANT_TYPE.equals( installation.getType() ) )
        {
            stored.setBuilder( null );
        }
        else
        {
            // remove one
            // TODO move this in ProfileService
            List<Installation> storedEnvVars = stored.getEnvironmentVariables();
            List<Installation> newEnvVars = new ArrayList<Installation>();
            for ( Iterator<Installation> iterator = storedEnvVars.iterator(); iterator.hasNext(); )
            {
                Installation storedInstallation = iterator.next();
                if ( !StringUtils.equals( storedInstallation.getName(), installation.getName() ) )
                {
                    newEnvVars.add( storedInstallation );
                }
            }
            stored.setEnvironmentVariables( newEnvVars );
        }
        profileService.updateProfile( stored );
        this.profile = profileService.getProfile( profile.getId() );
        return SUCCESS;
    }

    // -------------------------------------------------------
    // Webwork setter/getter
    // -------------------------------------------------------

    public List getProfiles()
    {
        return profiles;
    }

    public void setProfiles( List profiles )
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

    public String getInstallationName()
    {
        return installationName;
    }

    public void setInstallationName( String installationName )
    {
        this.installationName = installationName;
    }
}
