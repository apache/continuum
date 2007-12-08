/**
 * 
 */
package org.apache.maven.continuum.store.jpa;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.continuum.model.CommonUpdatableEntity;
import org.apache.maven.continuum.store.api.EntityNotFoundException;
import org.apache.maven.continuum.store.api.Query;
import org.apache.maven.continuum.store.api.Store;
import org.apache.maven.continuum.store.api.StoreException;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 * @since 1.2
 */
public class JpaStore<T extends CommonUpdatableEntity, Q extends Query<T>> extends StoreSupport implements Store<T, Q>
{

    /**
     * @{inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.api.Store#delete(java.lang.Object)
     */
    @Transactional( readOnly = false )
    public void delete( T entity ) throws StoreException
    {
        getJpaTemplate().remove( entity );
    }

    /**
     * @{inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.api.Store#lookup(Class, java.lang.Long)
     */
    public T lookup( Class<T> klass, Long id ) throws StoreException, EntityNotFoundException
    {
        if ( id == null )
            throw new EntityNotFoundException();
        T entity = null;
        try
        {
            entity = getJpaTemplate().find( klass, id );
        }
        catch ( JpaObjectRetrievalFailureException e )
        {
            throw new EntityNotFoundException();
        }
        if ( entity == null )
            throw new EntityNotFoundException();
        return entity;
    }

    /**
     * @{inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.api.Store#query(org.apache.maven.continuum.store.api.Query)
     */
    public List<T> query( Q query ) throws StoreException
    {
        Map<String, Object> where = new HashMap<String, Object>();
        String q = query.toString( where );

        List<T> results = find( q, where, 0, 0 );

        return results;
    }

    /**
     * @{inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.api.Store#save(java.lang.Object)
     */
    @Transactional( readOnly = true )
    public T save( T entity ) throws StoreException
    {
        if ( null != entity )
        {
            if ( null == entity.getId() )
                getJpaTemplate().persist( entity );
            else
                entity = getJpaTemplate().merge( entity );
        }
        return entity;
    }

}
