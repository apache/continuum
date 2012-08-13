package org.apache.maven.continuum.profile;

import org.apache.maven.continuum.model.system.Installation;
import org.apache.maven.continuum.model.system.Profile;

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
 * @since 15 juin 07
 */
public interface ProfileService
{
    String ROLE = ProfileService.class.getName();

    public Profile getProfile( int profileId )
        throws ProfileException;

    public void deleteProfile( int profileId )
        throws ProfileException;

    /**
     * @param profile
     * @throws ProfileException
     * @throws AlreadyExistsProfileException if a profile with the same name already exists
     */
    public void updateProfile( Profile profile )
        throws ProfileException, AlreadyExistsProfileException;

    /**
     * <b>Add an empty profile without builder, jdk and envVars</b>
     *
     * @param profile
     * @return
     * @throws ProfileException
     * @throws AlreadyExistsProfileException if a profile with the same name already exists
     */
    public Profile addProfile( Profile profile )
        throws ProfileException, AlreadyExistsProfileException;

    public List<Profile> getAllProfiles()
        throws ProfileException;

    public void setJdkInProfile( Profile profile, Installation jdk )
        throws ProfileException;

    public void setBuilderInProfile( Profile profile, Installation builder )
        throws ProfileException;

    public void addEnvVarInProfile( Profile profile, Installation envVar )
        throws ProfileException;

    public void addInstallationInProfile( Profile profile, Installation installation )
        throws ProfileException;

    /**
     * @param profile
     * @param installation
     * @throws ProfileException
     * @since 1.1-beta-4
     */
    public void removeInstallationFromProfile( Profile profile, Installation installation )
        throws ProfileException;

    public Profile getProfileWithName( String profileName )
        throws ProfileException;

    public boolean alreadyExistsProfileName( Profile profile )
        throws ProfileException;
}
