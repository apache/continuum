/**
 * 
 */
package org.apache.maven.continuum.store.jpa;

import java.util.List;

import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.store.api.EntityNotFoundException;
import org.apache.maven.continuum.store.api.ProjectQuery;
import org.apache.maven.continuum.store.api.Store;
import org.apache.maven.continuum.store.api.StoreException;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 * @since 1.2
 */
public class JpaProjectStore extends StoreSupport implements Store<Project, ProjectQuery>
{

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.api.Store#delete(java.lang.Object)
     */
    @Transactional( readOnly = false )
    public void delete( Project entity ) throws StoreException
    {
        getJpaTemplate().remove( entity );
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.api.Store#lookup(java.lang.Long)
     */
    public Project lookup( Long id ) throws StoreException, EntityNotFoundException
    {
        return lookup( Project.class, id );
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.api.Store#save(java.lang.Object)
     */
    @Transactional( readOnly = false )
    public Project save( Project entity ) throws StoreException
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
    public List<Project> query( ProjectQuery query ) throws StoreException
    {
        // TODO Auto-generated method stub
        return null;
    }

}
