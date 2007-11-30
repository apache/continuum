/**
 * 
 */
package org.apache.maven.continuum.store.jpa;

import java.util.List;

import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.store.api.EntityNotFoundException;
import org.apache.maven.continuum.store.api.Query;
import org.apache.maven.continuum.store.api.Store;
import org.apache.maven.continuum.store.api.StoreException;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 * @since 1.2
 */
public class JpaProjectGroupStore extends StoreSupport implements Store<ProjectGroup>
{

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.api.Store#delete(java.lang.Object)
     */
    @Transactional( readOnly = false )
    public void delete( ProjectGroup entity ) throws StoreException
    {
        getJpaTemplate().remove( entity );
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.api.Store#lookup(java.lang.Long)
     */
    public ProjectGroup lookup( Long id ) throws StoreException, EntityNotFoundException
    {
        return lookup( ProjectGroup.class, id );
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.api.Store#save(java.lang.Object)
     */
    @Transactional( readOnly = false )
    public ProjectGroup save( ProjectGroup entity ) throws StoreException
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

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.api.Store#query(org.apache.maven.continuum.store.api.Query)
     */
    public List<ProjectGroup> query( Query query ) throws StoreException
    {
        // TODO Implement!
        return null;
    }

}
