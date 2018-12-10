package org.apache.maven.continuum.store.api;

import java.util.List;

import org.apache.maven.continuum.model.project.Schedule;
import org.apache.maven.continuum.model.system.Installation;
import org.apache.maven.continuum.model.system.Profile;
import org.apache.maven.continuum.model.system.SystemConfiguration;

/**
 * Defines the contract consisting of operations that can be performed on following entities:
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
public interface DeprecatedSystemStore
{
    public static final String ROLE = DeprecatedSystemStore.class.getName();

    /**
     * Removes the passed {@link Installation} instance from the underlying store.
     * 
     * @param project
     *            {@link Installation} instance to remove.
     * @throws StoreException
     *             if there was an error removing the entity.
     */
    public void deleteInstallation( Installation installation ) throws StoreException;

    /**
     * Removes the passed {@link Profile} instance from the underlying store.
     * 
     * @param project
     *            {@link Profile} instance to remove.
     * @throws StoreException
     *             if there was an error removing the entity.
     */
    public void deleteProfile( Profile profile ) throws StoreException;

    /**
     * Removes the passed {@link Schedule} instance from the underlying store.
     * 
     * @param project
     *            {@link Schedule} instance to remove.
     * @throws StoreException
     *             if there was an error removing the entity.
     */
    public void deleteSchedule( Schedule schedule ) throws StoreException;

    /**
     * Removes the passed {@link SystemConfiguration} instance from the underlying store.
     * 
     * @param project
     *            {@link SystemConfiguration} instance to remove.
     * @throws StoreException
     *             if there was an error removing the entity.
     */
    public void deleteSystemConfiguration( SystemConfiguration systemConfiguration ) throws StoreException;

    /**
     * Looks up the underlying store and returns a {@link Installation} instance that matches the specified id.
     * 
     * @param id
     *            {@link Installation} id to match.
     * @return matching {@link Installation} instance.
     * @throws EntityNotFoundException
     *             if the instance could not be looked up.
     * @throws StoreException
     */
    public Installation lookupInstallation( long id ) throws EntityNotFoundException, StoreException;

    /**
     * Looks up the underlying store and returns a {@link Profile} instance that matches the specified id.
     * 
     * @param id
     *            {@link Profile} id to match.
     * @return matching {@link Profile} instance.
     * @throws EntityNotFoundException
     *             if the instance could not be looked up.
     * @throws StoreException
     */
    public Profile lookupProfile( long id ) throws EntityNotFoundException, StoreException;

    /**
     * Looks up the underlying store and returns a {@link Schedule} instance that matches the specified id.
     * 
     * @param id
     *            {@link Schedule} id to match.
     * @return matching {@link Schedule} instance.
     * @throws EntityNotFoundException
     *             if the instance could not be looked up.
     * @throws StoreException
     */
    public Schedule lookupSchedule( long id ) throws EntityNotFoundException, StoreException;

    /**
     * Looks up the underlying store and returns a {@link SystemConfiguration} instance that matches the specified id.
     * 
     * @param id
     *            {@link SystemConfiguration} id to match.
     * @return matching {@link SystemConfiguration} instance.
     * @throws EntityNotFoundException
     *             if the instance could not be looked up.
     * @throws StoreException
     */
    public SystemConfiguration lookupSystemConfiguration( long id ) throws EntityNotFoundException, StoreException;

    /**
     * Persists the passed in {@link Installation} instance to the underlying store.
     * <p>
     * If the entity instance already exists in the database it is updated, else a new instance is created and an
     * store-generated identifier assigned to it.
     * 
     * @param project
     *            {@link Installation} instance to be created/saved.
     * @return updated {@link Installation} instance.
     * @throws StoreException
     *             if there was an error saving the entity.
     */
    public Installation saveInstallation( Installation installation ) throws StoreException;

    /**
     * Persists the passed in {@link Profile} instance to the underlying store.
     * <p>
     * If the entity instance already exists in the database it is updated, else a new instance is created and an
     * store-generated identifier assigned to it.
     * 
     * @param project
     *            {@link Profile} instance to be created/saved.
     * @return updated {@link Profile} instance.
     * @throws StoreException
     *             if there was an error saving the entity.
     */
    public Profile saveProfile( Profile profile ) throws StoreException;

    /**
     * Persists the passed in {@link Schedule} instance to the underlying store.
     * <p>
     * If the entity instance already exists in the database it is updated, else a new instance is created and an
     * store-generated identifier assigned to it.
     * 
     * @param project
     *            {@link Schedule} instance to be created/saved.
     * @return updated {@link Schedule} instance.
     * @throws StoreException
     *             if there was an error saving the entity.
     */
    public Schedule saveSchedule( Schedule schedule ) throws StoreException;

    /**
     * Persists the passed in {@link SystemConfiguration} instance to the underlying store.
     * <p>
     * If the entity instance already exists in the database it is updated, else a new instance is created and an
     * store-generated identifier assigned to it.
     * 
     * @param project
     *            {@link SystemConfiguration} instance to be created/saved.
     * @return updated {@link SystemConfiguration} instance.
     * @throws StoreException
     *             if there was an error saving the entity.
     */
    public SystemConfiguration saveSystemConfiguration( SystemConfiguration systemConfiguration ) throws StoreException;

    /**
     * Obtains and returns a {@link List} of <b>all</b> {@link Schedule} instances for the system, stored in the
     * underlying store.
     * 
     * @return list of all {@link Schedule} instances stored.
     * @throws StoreException
     */
    public List getAllSchedules() throws StoreException;

    /**
     * Obtains and returns a {@link List} of <b>all</b> {@link Profile} instances for the system, stored in the
     * underlying store.
     * 
     * @return list of all {@link Profile} instances stored.
     * @throws StoreException
     */
    public List getAllProfiles() throws StoreException;

    /**
     * Obtains and returns a {@link List} of <b>all</b> {@link Installation} instances for the system, stored in the
     * underlying store.
     * 
     * @return list of all {@link Installation} instances stored.
     * @throws StoreException
     */
    public List getAllInstallations() throws StoreException;

    /**
     * Obtains and returns a {@link List} of <b>all</b> {@link SystemConfiguration} instances for the system, stored in
     * the underlying store.
     * 
     * @return list of all {@link SystemConfiguration} instances stored.
     * @throws StoreException
     */
    public List getAllSystemConfigurations() throws StoreException;

}
