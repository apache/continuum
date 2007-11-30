/**
 * 
 */
package org.apache.maven.continuum.store.jpa;

import org.apache.maven.continuum.model.CommonPersistableEntity;
import org.apache.maven.continuum.store.api.EntityNotFoundException;
import org.apache.maven.continuum.store.api.Store;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.orm.jpa.support.JpaDaoSupport;

/**
 * Base class for concrete {@link Store} implementations that provides service methods for Entity retrievals.
 * 
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 * @since 1.2
 */
public abstract class StoreSupport extends JpaDaoSupport
{

    /**
     * Service method to lookup matching entities.
     * 
     * @param <E>
     * @param klass
     * @param id
     * @return
     * @throws EntityNotFoundException
     */
    protected <E extends CommonPersistableEntity> E lookup( Class<E> klass, Long id ) throws EntityNotFoundException
    {
        if ( id == null )
            throw new EntityNotFoundException();
        E entity = null;
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

}
