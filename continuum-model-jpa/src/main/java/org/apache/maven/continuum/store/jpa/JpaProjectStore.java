/**
 * 
 */
package org.apache.maven.continuum.store.jpa;

import java.util.List;

import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.store.api.EntityNotFoundException;
import org.apache.maven.continuum.store.api.StoreException;
import org.apache.maven.continuum.store.api.Query;
import org.apache.maven.continuum.store.api.Store;

/**
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 * @since 1.2
 */
public class JpaProjectStore implements Store<Project>
{

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.api.Store#delete(java.lang.Object)
     */
    public void delete( Project entity ) throws StoreException
    {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.api.Store#lookup(java.lang.Long)
     */
    public Project lookup( Long id ) throws StoreException, EntityNotFoundException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.api.Store#save(java.lang.Object)
     */
    public Project save( Project entity ) throws StoreException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.api.Store#query(org.apache.maven.continuum.store.api.Query)
     */
    public List<Project> query( Query query ) throws StoreException
    {
        // TODO Auto-generated method stub
        return null;
    }

}
