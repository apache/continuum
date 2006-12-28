/**
 * 
 */
package org.apache.maven.continuum.store;

import org.apache.maven.continuum.model.project.Profile;
import org.apache.maven.continuum.model.project.Schedule;
import org.apache.maven.continuum.model.system.Installation;
import org.apache.maven.continuum.model.system.SystemConfiguration;

/**
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * 
 */
public class JdoSystemStore extends AbstractJdoStore implements SystemStore
{

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.ProjectStore#deleteInstallation(org.apache.maven.continuum.model.system.Installation)
     */
    public void deleteInstallation( Installation installation ) throws ContinuumStoreException
    {
        // TODO: Any checks before installation should be deleted?
        removeObject( installation );
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.ProjectStore#deleteProfile(org.apache.maven.continuum.model.project.Profile)
     */
    public void deleteProfile( Profile profile ) throws ContinuumStoreException
    {
        // TODO: Any checks before profile should be deleted?
        removeObject( profile );
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.ProjectStore#deleteSchedule(org.apache.maven.continuum.model.project.Schedule)
     */
    public void deleteSchedule( Schedule schedule ) throws ContinuumStoreException
    {
        // TODO: Any checks before schedule should be deleted?
        removeObject( schedule );
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.ProjectStore#deleteSystemConfiguration(org.apache.maven.continuum.model.system.SystemConfiguration)
     */
    public void deleteSystemConfiguration( SystemConfiguration systemConfiguration ) throws ContinuumStoreException
    {
        // TODO: Any checks before systemConfiguration should be deleted?
        removeObject( systemConfiguration );
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.ProjectStore#lookupInstallation(long)
     */
    public Installation lookupInstallation( long id ) throws ContinuumObjectNotFoundException, ContinuumStoreException
    {
        return (Installation) getObjectById( Installation.class, id, null );
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.ProjectStore#lookupProfile(long)
     */
    public Profile lookupProfile( long id ) throws ContinuumObjectNotFoundException, ContinuumStoreException
    {
        return (Profile) getObjectById( Profile.class, id, null );
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.ProjectStore#lookupSchedule(long)
     */
    public Schedule lookupSchedule( long id ) throws ContinuumObjectNotFoundException, ContinuumStoreException
    {
        return (Schedule) getObjectById( Schedule.class, id, null );
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.ProjectStore#lookupSystemConfiguration(long)
     */
    public SystemConfiguration lookupSystemConfiguration( long id )
        throws ContinuumObjectNotFoundException, ContinuumStoreException
    {
        return (SystemConfiguration) getObjectById( SystemConfiguration.class, id, null );
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.ProjectStore#saveInstallation(org.apache.maven.continuum.model.system.Installation)
     */
    public Installation saveInstallation( Installation installation ) throws ContinuumStoreException
    {
        updateObject( installation );
        return installation;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.ProjectStore#saveProfile(org.apache.maven.continuum.model.project.Profile)
     */
    public Profile saveProfile( Profile profile ) throws ContinuumStoreException
    {
        updateObject( profile );
        return profile;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.ProjectStore#saveSchedule(org.apache.maven.continuum.model.project.Schedule)
     */
    public Schedule saveSchedule( Schedule schedule ) throws ContinuumStoreException
    {
        updateObject( schedule );
        return schedule;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.ProjectStore#saveSystemConfiguration(org.apache.maven.continuum.model.system.SystemConfiguration)
     */
    public SystemConfiguration saveSystemConfiguration( SystemConfiguration systemConfiguration )
        throws ContinuumStoreException
    {
        updateObject( systemConfiguration );
        return systemConfiguration;
    }

}
