package org.apache.maven.continuum.store;

import org.apache.maven.continuum.model.project.Profile;
import org.apache.maven.continuum.model.project.Schedule;
import org.apache.maven.continuum.model.system.Installation;
import org.apache.maven.continuum.model.system.SystemConfiguration;

import java.util.List;

/**
 * Defines the contract consisting of operations that can be performed on
 * following entities:
 * <ul>
 * <li>{@link Schedule},</li>
 * <li>{@link Profile},</li>
 * <li>{@link Installation},</li>
 * <li>{@link SystemConfiguration}</li>
 * </ul>
 * 
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 * @since 1.1
 */
public interface SystemStore
{
    public static final String ROLE = SystemStore.class.getName();

    /**
     * Removes the passed {@link Installation} instance from the underlying
     * store.
     * 
     * @param project {@link Installation} instance to remove.
     * @throws ContinuumStoreException if there was an error removing the
     *             entity.
     */
    public void deleteInstallation( Installation installation ) throws ContinuumStoreException;

    /**
     * Removes the passed {@link Profile} instance from the underlying store.
     * 
     * @param project {@link Profile} instance to remove.
     * @throws ContinuumStoreException if there was an error removing the
     *             entity.
     */
    public void deleteProfile( Profile profile ) throws ContinuumStoreException;

    /**
     * Removes the passed {@link Schedule} instance from the underlying store.
     * 
     * @param project {@link Schedule} instance to remove.
     * @throws ContinuumStoreException if there was an error removing the
     *             entity.
     */
    public void deleteSchedule( Schedule schedule ) throws ContinuumStoreException;

    /**
     * Removes the passed {@link SystemConfiguration} instance from the
     * underlying store.
     * 
     * @param project {@link SystemConfiguration} instance to remove.
     * @throws ContinuumStoreException if there was an error removing the
     *             entity.
     */
    public void deleteSystemConfiguration( SystemConfiguration systemConfiguration ) throws ContinuumStoreException;

    /**
     * Looks up the underlying store and returns a {@link Installation} instance
     * that matches the specified id.
     * 
     * @param id {@link Installation} id to match.
     * @return matching {@link Installation} instance.
     * @throws ContinuumObjectNotFoundException if the instance could not be
     *             looked up.
     * @throws ContinuumStoreException
     */
    public Installation lookupInstallation( long id ) throws ContinuumObjectNotFoundException, ContinuumStoreException;

    /**
     * Looks up the underlying store and returns a {@link Profile} instance that
     * matches the specified id.
     * 
     * @param id {@link Profile} id to match.
     * @return matching {@link Profile} instance.
     * @throws ContinuumObjectNotFoundException if the instance could not be
     *             looked up.
     * @throws ContinuumStoreException
     */
    public Profile lookupProfile( long id ) throws ContinuumObjectNotFoundException, ContinuumStoreException;

    /**
     * Looks up the underlying store and returns a {@link Schedule} instance
     * that matches the specified id.
     * 
     * @param id {@link Schedule} id to match.
     * @return matching {@link Schedule} instance.
     * @throws ContinuumObjectNotFoundException if the instance could not be
     *             looked up.
     * @throws ContinuumStoreException
     */
    public Schedule lookupSchedule( long id ) throws ContinuumObjectNotFoundException, ContinuumStoreException;

    /**
     * Looks up the underlying store and returns a {@link SystemConfiguration}
     * instance that matches the specified id.
     * 
     * @param id {@link SystemConfiguration} id to match.
     * @return matching {@link SystemConfiguration} instance.
     * @throws ContinuumObjectNotFoundException if the instance could not be
     *             looked up.
     * @throws ContinuumStoreException
     */
    public SystemConfiguration lookupSystemConfiguration( long id )
        throws ContinuumObjectNotFoundException, ContinuumStoreException;

    /**
     * Persists the passed in {@link Installation} instance to the underlying
     * store.
     * <p>
     * If the entity instance already exists in the database it is updated, else
     * a new instance is created and an store-generated identifier assigned to
     * it.
     * 
     * @param project {@link Installation} instance to be created/saved.
     * @return updated {@link Installation} instance.
     * @throws ContinuumStoreException if there was an error saving the entity.
     */
    public Installation saveInstallation( Installation installation ) throws ContinuumStoreException;

    /**
     * Persists the passed in {@link Profile} instance to the underlying store.
     * <p>
     * If the entity instance already exists in the database it is updated, else
     * a new instance is created and an store-generated identifier assigned to
     * it.
     * 
     * @param project {@link Profile} instance to be created/saved.
     * @return updated {@link Profile} instance.
     * @throws ContinuumStoreException if there was an error saving the entity.
     */
    public Profile saveProfile( Profile profile ) throws ContinuumStoreException;

    /**
     * Persists the passed in {@link Schedule} instance to the underlying store.
     * <p>
     * If the entity instance already exists in the database it is updated, else
     * a new instance is created and an store-generated identifier assigned to
     * it.
     * 
     * @param project {@link Schedule} instance to be created/saved.
     * @return updated {@link Schedule} instance.
     * @throws ContinuumStoreException if there was an error saving the entity.
     */
    public Schedule saveSchedule( Schedule schedule ) throws ContinuumStoreException;

    /**
     * Persists the passed in {@link SystemConfiguration} instance to the
     * underlying store.
     * <p>
     * If the entity instance already exists in the database it is updated, else
     * a new instance is created and an store-generated identifier assigned to
     * it.
     * 
     * @param project {@link SystemConfiguration} instance to be created/saved.
     * @return updated {@link SystemConfiguration} instance.
     * @throws ContinuumStoreException if there was an error saving the entity.
     */
    public SystemConfiguration saveSystemConfiguration( SystemConfiguration systemConfiguration )
        throws ContinuumStoreException;

    /**
     * Obtains and returns a {@link List} of <b>all</b> {@link Schedule}
     * instances for the system, stored in the underlying store.
     * 
     * @return list of all {@link Schedule} instances stored.
     * @throws ContinuumStoreException
     */
    public List getAllSchedules() throws ContinuumStoreException;

    /**
     * Obtains and returns a {@link List} of <b>all</b> {@link Profile}
     * instances for the system, stored in the underlying store.
     * 
     * @return list of all {@link Profile} instances stored.
     * @throws ContinuumStoreException
     */
    public List getAllProfiles() throws ContinuumStoreException;

    /**
     * Obtains and returns a {@link List} of <b>all</b> {@link Installation}
     * instances for the system, stored in the underlying store.
     * 
     * @return list of all {@link Installation} instances stored.
     * @throws ContinuumStoreException
     */
    public List getAllInstallations() throws ContinuumStoreException;

    /**
     * Obtains and returns a {@link List} of <b>all</b>
     * {@link SystemConfiguration} instances for the system, stored in the
     * underlying store.
     * 
     * @return list of all {@link SystemConfiguration} instances stored.
     * @throws ContinuumStoreException
     */
    public List getAllSystemConfigurations() throws ContinuumStoreException;

}
