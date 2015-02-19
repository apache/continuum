package org.apache.continuum.profile;

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

import org.apache.commons.lang.StringUtils;
import org.apache.continuum.dao.ProfileDao;
import org.apache.maven.continuum.installation.InstallationService;
import org.apache.maven.continuum.model.system.Installation;
import org.apache.maven.continuum.model.system.Profile;
import org.apache.maven.continuum.profile.AlreadyExistsProfileException;
import org.apache.maven.continuum.profile.ProfileException;
import org.apache.maven.continuum.profile.ProfileService;
import org.apache.maven.continuum.store.ContinuumObjectNotFoundException;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;

/**
 * @author <a href="mailto:olamy@codehaus.org">olamy</a>
 *          TODO use some cache mechanism to prevent always reading from store ?
 * @since 15 juin 07
 */
@Service( "profileService" )
public class DefaultProfileService
    implements ProfileService
{
    @Resource
    private ProfileDao profileDao;

    /**
     * @see org.apache.maven.continuum.profile.ProfileService#updateProfile(org.apache.maven.continuum.model.system.Profile)
     */
    public void updateProfile( Profile profile )
        throws ProfileException, AlreadyExistsProfileException
    {
        // already exists check should be done in the same transaction
        // but we assume we don't have a huge load and a lot of concurrent access ;-)
        if ( alreadyExistsProfileName( profile ) )
        {
            throw new AlreadyExistsProfileException( "profile with name " + profile.getName() + " already exists" );
        }

        try
        {
            Profile stored = getProfile( profile.getId() );
            stored.setActive( profile.isActive() );
            stored.setBuilder( profile.getBuilder() );
            stored.setBuildWithoutChanges( profile.isBuildWithoutChanges() );
            stored.setDescription( profile.getDescription() );
            stored.setJdk( profile.getJdk() );
            stored.setName( profile.getName() );
            stored.setEnvironmentVariables( profile.getEnvironmentVariables() );
            stored.setBuildAgentGroup( profile.getBuildAgentGroup() );
            profileDao.updateProfile( stored );
        }
        catch ( ContinuumStoreException e )
        {
            throw new ProfileException( e.getMessage(), e );
        }
    }

    public void updateProfileCheckDuplicateName( Profile profile, boolean checkDuplicateName )
        throws ProfileException, AlreadyExistsProfileException
    {
        if ( checkDuplicateName )
        {
            // already exists check should be done in the same transaction
            // but we assume we don't have a huge load and a lot of concurrent access ;-)
            if ( alreadyExistsProfileName( profile ) )
            {
                throw new AlreadyExistsProfileException( "profile with name " + profile.getName() + " already exists" );
            }
        }
        try
        {
            Profile stored = getProfile( profile.getId() );
            stored.setActive( profile.isActive() );
            stored.setBuilder( profile.getBuilder() );
            stored.setBuildWithoutChanges( profile.isBuildWithoutChanges() );
            stored.setDescription( profile.getDescription() );
            stored.setJdk( profile.getJdk() );
            stored.setName( profile.getName() );
            stored.setEnvironmentVariables( profile.getEnvironmentVariables() );
            stored.setBuildAgentGroup( profile.getBuildAgentGroup() );
            profileDao.updateProfile( stored );
        }
        catch ( ContinuumStoreException e )
        {
            throw new ProfileException( e.getMessage(), e );
        }
    }

    /**
     * @see org.apache.maven.continuum.profile.ProfileService#addProfile(org.apache.maven.continuum.model.system.Profile)
     */
    public Profile addProfile( Profile profile )
        throws ProfileException, AlreadyExistsProfileException
    {
        // already exists check should be done in the same transaction
        // but we assume we don't have a huge load and a lot of concurrent access ;-)
        if ( alreadyExistsProfileName( profile ) )
        {
            throw new AlreadyExistsProfileException( "profile with name " + profile.getName() + " already exists" );
        }
        profile.setBuilder( null );
        profile.setJdk( null );
        profile.setEnvironmentVariables( null );
        return profileDao.addProfile( profile );
    }

    /**
     * @see org.apache.maven.continuum.profile.ProfileService#deleteProfile(int)
     */
    public void deleteProfile( int profileId )
        throws ProfileException
    {
        try
        {
            profileDao.removeProfile( getProfile( profileId ) );
        }
        catch ( Exception e )
        {
            throw new ProfileException( "Cannot remove the profile", e );
        }
    }

    /**
     * @see org.apache.maven.continuum.profile.ProfileService#getAllProfiles()
     */
    public List<Profile> getAllProfiles()
        throws ProfileException
    {
        return profileDao.getAllProfilesByName();
    }

    /**
     * @see org.apache.maven.continuum.profile.ProfileService#getProfile(int)
     */
    public Profile getProfile( int profileId )
        throws ProfileException
    {
        try
        {
            return profileDao.getProfile( profileId );
        }
        catch ( ContinuumObjectNotFoundException e )
        {
            // really ignore ?
            return null;
        }
        catch ( ContinuumStoreException e )
        {
            throw new ProfileException( e.getMessage(), e );
        }
    }

    /**
     * @see org.apache.maven.continuum.profile.ProfileService#setBuilderInProfile(org.apache.maven.continuum.model.system.Profile, org.apache.maven.continuum.model.system.Installation)
     */
    public void setBuilderInProfile( Profile profile, Installation builder )
        throws ProfileException
    {
        Profile stored = getProfile( profile.getId() );
        stored.setBuilder( builder );
        try
        {
            profileDao.updateProfile( stored );
        }
        catch ( ContinuumStoreException e )
        {
            throw new ProfileException( e.getMessage(), e );
        }
    }

    /**
     * @see org.apache.maven.continuum.profile.ProfileService#setJdkInProfile(org.apache.maven.continuum.model.system.Profile, org.apache.maven.continuum.model.system.Installation)
     */
    public void setJdkInProfile( Profile profile, Installation jdk )
        throws ProfileException
    {
        Profile stored = getProfile( profile.getId() );
        stored.setJdk( jdk );
        try
        {
            profileDao.updateProfile( stored );
        }
        catch ( ContinuumStoreException e )
        {
            throw new ProfileException( e.getMessage(), e );
        }
    }

    /**
     * @see org.apache.maven.continuum.profile.ProfileService#addEnvVarInProfile(org.apache.maven.continuum.model.system.Profile, org.apache.maven.continuum.model.system.Installation)
     */
    public void addEnvVarInProfile( Profile profile, Installation envVar )
        throws ProfileException
    {
        Profile stored = getProfile( profile.getId() );
        stored.addEnvironmentVariable( envVar );
        try
        {
            profileDao.updateProfile( stored );
        }
        catch ( ContinuumStoreException e )
        {
            throw new ProfileException( e.getMessage(), e );
        }
    }

    public void addInstallationInProfile( Profile profile, Installation installation )
        throws ProfileException
    {
        if ( InstallationService.JDK_TYPE.equals( installation.getType() ) )
        {
            setJdkInProfile( profile, installation );
        }
        else if ( InstallationService.MAVEN1_TYPE.equals( installation.getType() ) ||
            InstallationService.MAVEN2_TYPE.equals( installation.getType() ) ||
            InstallationService.ANT_TYPE.equals( installation.getType() ) )
        {
            setBuilderInProfile( profile, installation );
        }
        else
        {
            addEnvVarInProfile( profile, installation );
        }

    }

    public void removeInstallationFromProfile( Profile profile, Installation installation )
        throws ProfileException
    {
        Profile stored = getProfile( profile.getId() );
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
            List<Installation> storedEnvVars = stored.getEnvironmentVariables();
            List<Installation> newEnvVars = new ArrayList<Installation>();
            for ( Installation storedInstallation : storedEnvVars )
            {
                if ( !StringUtils.equals( storedInstallation.getName(), installation.getName() ) )
                {
                    newEnvVars.add( storedInstallation );
                }
            }
            stored.setEnvironmentVariables( newEnvVars );
        }
        try
        {
            updateProfileCheckDuplicateName( stored, false );
        }
        catch ( AlreadyExistsProfileException e )
        {
            // normally cannot happend here but anyway we throw the exception
            throw new ProfileException( e.getMessage(), e );
        }
    }


    public Profile getProfileWithName( String profileName )
        throws ProfileException
    {
        List<Profile> allProfiles = getAllProfiles();
        for ( Profile profile : allProfiles )
        {
            if ( StringUtils.equals( profile.getName(), profileName ) )
            {
                return profile;
            }
        }
        return null;
    }

    /**
     * @param profile
     * @return true if profile with same name (<b>case sensitive</b>) exists
     * @throws ProfileException
     */
    public boolean alreadyExistsProfileName( Profile profile )
        throws ProfileException
    {
        Profile storedProfile = getProfileWithName( profile.getName() );
        return ( storedProfile != null && storedProfile.getId() != profile.getId() );
    }

}
