/**
 * 
 */
package org.apache.maven.continuum.store.api;

import java.util.List;

import org.apache.maven.continuum.model.CommonUpdatableEntity;

/**
 * Interface that Continuum store extensions/implementations are expected to implement to allow operations on the
 * underlying store.
 * <ul>
 * <li>Entity look ups</li>
 * <li>Entity insert/updates</li>
 * <li>Entity removal</li>
 * <li>Querying one or more entity/entities based on specified criteria</li>
 * </ul>
 * 
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 * @since 1.2
 */
public interface Store<T extends CommonUpdatableEntity, Q extends Query<T>>
{

    /**
     * Looks up the underlying store and returns a {@link T} instance that matches the specified id.
     * 
     * @param klass
     *            {@link Class} for type entity to lookup and return an instance of.
     * @param id
     *            Entity Type {@link T}'s id to match.
     * 
     * @return matching entity type {@link T} instance.
     * @throws StoreException
     * @throws EntityNotFoundException
     *             if the entity specified by the identifier could be located in the system.
     * @throws EntityNotFoundException
     *             if the instance could not be looked up.
     */
    public T lookup( Class<T> klass, Long id ) throws StoreException, EntityNotFoundException;

    /**
     * Persists the passed in entity type {@link T} instance to the underlying store.
     * <p>
     * If the entity instance already exists in the database it is updated, else a new instance is created and an
     * store-generated identifier assigned to it.
     * 
     * @param entity
     *            Type {@link T} instance to be created/saved.
     * @return updated entity type {@link T} instance.
     * @throws StoreException
     *             if there was an error saving the entity.
     */
    public T save( T entity ) throws StoreException;

    /**
     * Removes the passed entity type {@link T} instance from the underlying store.
     * 
     * @param entity
     *            Type {@link T} instance to remove.
     * @throws StoreException
     *             if there was an error removing the entity.
     */
    public void delete( T entity ) throws StoreException;

    /**
     * Obtains a {@link List} of instances of entity type {@link T} which match the criteria specified by the passed in
     * query instance.
     * 
     * @param query
     *            instance that wraps up the criteria for querying matching instances in the system.
     * @return {@link List} of instances of entity type {@link T} which match the specified query.
     * @throws StoreException
     */
    public List<T> query( Q query ) throws StoreException;

}
