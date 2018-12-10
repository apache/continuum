/**
 * 
 */
package org.apache.maven.continuum.store.jpa;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.apache.maven.continuum.store.api.Store;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.jpa.JpaCallback;
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
     * Prepares and executes a query using the 'where' criteria, a start index and a given range of results to return.
     * 
     * @param queryString
     * @param whereParams
     * @param startIndex
     * @param range
     * @return
     * @throws DataAccessException
     */
    protected List find( final String queryString, final Map<String, Object> whereParams, final int startIndex,
                         final int range ) throws DataAccessException
    {
        return getJpaTemplate().executeFind( new JpaCallback()
        {
            public Object doInJpa( EntityManager em ) throws PersistenceException
            {
                Query query = em.createQuery( queryString );
                if ( whereParams != null )
                {
                    for ( Iterator it = whereParams.entrySet().iterator(); it.hasNext(); )
                    {
                        Map.Entry<String, Object> entry = (Map.Entry<String, Object>) it.next();
                        query.setParameter( entry.getKey(), entry.getValue() );
                    }
                }
                if ( startIndex > 0 )
                {
                    query.setFirstResult( startIndex );
                }
                if ( range > 0 )
                {
                    query.setMaxResults( range );
                }
                return query.getResultList();
            }
        } );
    }

}
