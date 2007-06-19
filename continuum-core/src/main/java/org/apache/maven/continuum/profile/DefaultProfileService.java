package org.apache.maven.continuum.profile;

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

import org.apache.maven.continuum.model.system.Installation;
import org.apache.maven.continuum.model.system.Profile;
import org.apache.maven.continuum.store.ContinuumObjectNotFoundException;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;

import java.util.List;

/**
 * @author <a href="mailto:olamy@codehaus.org">olamy</a>
 * @version $Id$
 * @plexus.component role="org.apache.maven.continuum.profile.ProfileService"
 * TODO use some cache mechanism to prevent always reading from store ?
 * @since 15 juin 07
 */
public class DefaultProfileService
    implements ProfileService
{

    /**
     * @plexus.requirement role-hint="jdo"
     */
    private ContinuumStore store;

    /**
     * @see org.apache.maven.continuum.profile.ProfileService#updateProfile(org.apache.maven.continuum.model.system.Profile)
     */
    public void updateProfile( Profile profile )
        throws ProfileException
    {
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
            store.updateProfile( stored );
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
        throws ProfileException
    {
        // TODO check if one with the same name already here
        profile.setBuilder( null );
        profile.setJdk( null );
        profile.setEnvironmentVariables( null );
        return store.addProfile( profile );
    }

    /**
     * @see org.apache.maven.continuum.profile.ProfileService#deletedProfile(int)
     */
    public void deleteProfile( int profileId )
        throws ProfileException
    {
        store.removeProfile( getProfile( profileId ) );
    }

    /**
     * @see org.apache.maven.continuum.profile.ProfileService#getAllProfiles()
     */
    public List<Profile> getAllProfiles()
        throws ProfileException
    {
        return store.getAllProfilesByName();
    }

    /**
     * @see org.apache.maven.continuum.profile.ProfileService#getProfile(int)
     */
    public Profile getProfile( int profileId )
        throws ProfileException
    {
        try
        {
            return store.getProfile( profileId );
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
     * @see org.apache.maven.continuum.profile.ProfileService#setBuilderInProfile(org.apache.maven.continuum.model.system.Profile,org.apache.maven.continuum.model.system.Installation)
     */
    public void setBuilderInProfile( Profile profile, Installation builder )
        throws ProfileException
    {
        Profile stored = getProfile( profile.getId() );
        stored.setBuilder( builder );
        try
        {
            store.updateProfile( stored );
        }
        catch ( ContinuumStoreException e )
        {
            throw new ProfileException( e.getMessage(), e );
        }
    }

    /**
     * @see org.apache.maven.continuum.profile.ProfileService#setJdkInProfile(org.apache.maven.continuum.model.system.Profile,org.apache.maven.continuum.model.system.Installation)
     */
    public void setJdkInProfile( Profile profile, Installation jdk )
        throws ProfileException
    {
        Profile stored = getProfile( profile.getId() );
        stored.setJdk( jdk );
        try
        {
            store.updateProfile( stored );
        }
        catch ( ContinuumStoreException e )
        {
            throw new ProfileException( e.getMessage(), e );
        }
    }

    /**
     * @see org.apache.maven.continuum.profile.ProfileService#addEnvVarInProfile(org.apache.maven.continuum.model.system.Profile,org.apache.maven.continuum.model.system.Installation)
     */
    public void addEnvVarInProfile( Profile profile, Installation envVar )
        throws ProfileException
    {
        Profile stored = getProfile( profile.getId() );
        stored.addEnvironmentVariable( envVar );
        try
        {
            store.updateProfile( stored );
        }
        catch ( ContinuumStoreException e )
        {
            throw new ProfileException( e.getMessage(), e );
        }
    }

}
