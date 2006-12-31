package org.apache.maven.continuum.store.ibatis;

/*
 * Copyright 2004-2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

import org.apache.maven.continuum.model.project.Profile;
import org.apache.maven.continuum.model.project.Schedule;
import org.apache.maven.continuum.model.system.Installation;
import org.apache.maven.continuum.model.system.SystemConfiguration;
import org.apache.maven.continuum.store.ContinuumObjectNotFoundException;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.apache.maven.continuum.store.SystemStore;

import java.util.List;

/**
 * Concrete implementation of {@link SystemStore} that uses Ibatis framework to
 * access the underlying datastore.
 * 
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 * @since 1.1
 * @plexus.component role="org.apache.maven.continuum.store.SystemStore"
 *                   role-hint="ibatis"
 */
public class IbatisSystemStore implements SystemStore
{

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.SystemStore#deleteInstallation(org.apache.maven.continuum.model.system.Installation)
     */
    public void deleteInstallation( Installation installation ) throws ContinuumStoreException
    {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.SystemStore#deleteProfile(org.apache.maven.continuum.model.project.Profile)
     */
    public void deleteProfile( Profile profile ) throws ContinuumStoreException
    {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.SystemStore#deleteSchedule(org.apache.maven.continuum.model.project.Schedule)
     */
    public void deleteSchedule( Schedule schedule ) throws ContinuumStoreException
    {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.SystemStore#deleteSystemConfiguration(org.apache.maven.continuum.model.system.SystemConfiguration)
     */
    public void deleteSystemConfiguration( SystemConfiguration systemConfiguration ) throws ContinuumStoreException
    {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.SystemStore#getAllInstallations()
     */
    public List getAllInstallations() throws ContinuumStoreException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.SystemStore#getAllProfiles()
     */
    public List getAllProfiles() throws ContinuumStoreException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.SystemStore#getAllSchedules()
     */
    public List getAllSchedules() throws ContinuumStoreException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.SystemStore#getAllSystemConfigurations()
     */
    public List getAllSystemConfigurations() throws ContinuumStoreException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.SystemStore#lookupInstallation(long)
     */
    public Installation lookupInstallation( long id ) throws ContinuumObjectNotFoundException, ContinuumStoreException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.SystemStore#lookupProfile(long)
     */
    public Profile lookupProfile( long id ) throws ContinuumObjectNotFoundException, ContinuumStoreException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.SystemStore#lookupSchedule(long)
     */
    public Schedule lookupSchedule( long id ) throws ContinuumObjectNotFoundException, ContinuumStoreException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.SystemStore#lookupSystemConfiguration(long)
     */
    public SystemConfiguration lookupSystemConfiguration( long id )
        throws ContinuumObjectNotFoundException, ContinuumStoreException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.SystemStore#saveInstallation(org.apache.maven.continuum.model.system.Installation)
     */
    public Installation saveInstallation( Installation installation ) throws ContinuumStoreException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.SystemStore#saveProfile(org.apache.maven.continuum.model.project.Profile)
     */
    public Profile saveProfile( Profile profile ) throws ContinuumStoreException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.SystemStore#saveSchedule(org.apache.maven.continuum.model.project.Schedule)
     */
    public Schedule saveSchedule( Schedule schedule ) throws ContinuumStoreException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.SystemStore#saveSystemConfiguration(org.apache.maven.continuum.model.system.SystemConfiguration)
     */
    public SystemConfiguration saveSystemConfiguration( SystemConfiguration systemConfiguration )
        throws ContinuumStoreException
    {
        // TODO Auto-generated method stub
        return null;
    }

}
