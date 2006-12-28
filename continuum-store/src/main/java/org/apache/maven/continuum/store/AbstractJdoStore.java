/**
 * 
 */
package org.apache.maven.continuum.store;

import org.codehaus.plexus.jdo.PlexusJdoUtils;
import org.codehaus.plexus.jdo.PlexusObjectNotFoundException;
import org.codehaus.plexus.jdo.PlexusStoreException;

import javax.jdo.FetchPlan;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Transaction;

/**
 * Covenience base class that consolidates some common methods used by
 * extensions.
 * 
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 * @since 1.1
 */
public class AbstractJdoStore
{

    /**
     * Provides hook to obtainig a {@link PersistenceManager} instance for
     * invoking operations on the underlying store.
     */
    private PersistenceManagerFactory continuumPmf;

    /**
     * Lookup and return an Object instance from the underlying store.
     * 
     * @param clazz Expected {@link Class} of the Entity being looked up.
     * @param id object identifier in the underlying store.
     * @param fetchGroup TODO: Document!
     * @return matching Object.
     * @throws ContinuumStoreException
     * @throws ContinuumObjectNotFoundException if not matching Object could be
     *             found.
     */
    protected Object getObjectById( Class clazz, long id, String fetchGroup )
        throws ContinuumStoreException, ContinuumObjectNotFoundException
    {
        try
        {
            return PlexusJdoUtils.getObjectById( getPersistenceManager(), clazz, id, fetchGroup );
        }
        catch ( PlexusObjectNotFoundException e )
        {
            throw new ContinuumObjectNotFoundException( e.getMessage() );
        }
        catch ( PlexusStoreException e )
        {
            throw new ContinuumStoreException( e.getMessage(), e );
        }
    }

    /**
     * Looks up and returns an Entity instance from the underlying store given
     * the String key and the field name to match it against.
     * 
     * @param clazz Expected {@link Class} of the entity being looked up.
     * @param idField Column identifier/name for the field in the underlying
     *            store to match the String identifier against.
     * @param id Identifier value to match in the Id field.
     * @param fetchGroup TODO: Document! What is a fetchGroup?
     * @return Entity instance that matches the lookup criteria as specified by
     *         the passed in Id.
     * @throws ContinuumStoreException
     * @throws ContinuumObjectNotFoundException if there was no instance that
     *             matched the criteria in the underlying store.
     */
    protected Object getObjectFromQuery( Class clazz, String idField, String id, String fetchGroup )
        throws ContinuumStoreException, ContinuumObjectNotFoundException
    {
        try
        {
            return PlexusJdoUtils.getObjectFromQuery( getPersistenceManager(), clazz, idField, id, fetchGroup );
        }
        catch ( PlexusObjectNotFoundException e )
        {
            throw new ContinuumObjectNotFoundException( e.getMessage() );
        }
        catch ( PlexusStoreException e )
        {
            throw new ContinuumStoreException( e.getMessage(), e );
        }
    }

    /**
     * Returns the {@link PersistenceManager} instance that allows interaction
     * with the underlying store.
     * 
     * @return {@link PersistenceManager} instance.
     */
    protected PersistenceManager getPersistenceManager()
    {
        PersistenceManager pm = continuumPmf.getPersistenceManager();

        pm.getFetchPlan().setMaxFetchDepth( -1 );
        pm.getFetchPlan().setDetachmentOptions( FetchPlan.DETACH_LOAD_FIELDS );

        return pm;
    }

    /**
     * Deletes the specified Object instance from the underlying store.
     * 
     * @param o Object instance to be deleted.
     */
    protected void removeObject( Object o )
    {
        PlexusJdoUtils.removeObject( getPersistenceManager(), o );
    }

    /**
     * Rolls back the passed in uncommitted transaction.
     * 
     * @param tx {@link Transaction} to rollback.
     */
    protected void rollback( Transaction tx )
    {
        PlexusJdoUtils.rollbackIfActive( tx );
    }

    /**
     * Updates the specified object's properties in the underlying store.
     * 
     * @param object Object instance to be updated.
     * @throws ContinuumStoreException wraps up any error encountered while
     *             updating the specified object in the store.
     */
    protected void updateObject( Object object ) throws ContinuumStoreException
    {
        try
        {
            PlexusJdoUtils.updateObject( getPersistenceManager(), object );
        }
        catch ( PlexusStoreException e )
        {
            throw new ContinuumStoreException( e.getMessage(), e );
        }
    }

}
