package org.apache.continuum.dao.impl;

import org.apache.continuum.dao.api.GenericDao;
import org.apache.continuum.model.CommonPersistableEntity;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.orm.jpa.JpaCallback;
import org.springframework.orm.jpa.support.JpaDaoSupport;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class GenericDaoJpa<T extends CommonPersistableEntity>
    extends JpaDaoSupport
    implements GenericDao<T>
{
    private Class<T> entityClass;

    public GenericDaoJpa()
    {
    }

    public GenericDaoJpa( Class<T> type )
    {
        entityClass = type;
    }

    public Class<T> getPersistentClass()
    {
        return entityClass;
    }

    public T findById( long id )
        throws ObjectRetrievalFailureException
    {
        return getJpaTemplate().find( getPersistentClass(), id );
    }

    @SuppressWarnings("unchecked")
    public List<T> findAll()
    {
        String q = "SELECT z FROM " + entityClass.getSimpleName() + " z";
        return (List<T>) getJpaTemplate().find( q );
    }

    @SuppressWarnings("unchecked")
    public List<T> findByNamedQueryAndNamedParams( Class<T> entityClass, String queryName, Map<String, Object> params )
        throws DataAccessException
    {
        return (List<T>) getJpaTemplate().findByNamedQueryAndNamedParams( queryName, params );
    }

    @SuppressWarnings("unchecked")
    public T findUniqByNamedQueryAndNamedParams( Class<T> entityClass, final String queryName,
                                                 final Map<String, Object> params )
        throws DataAccessException
    {
        JpaCallback cb = new JpaCallback()
        {
            public Object doInJpa( EntityManager entityManager )
                throws PersistenceException
            {
                Query q = entityManager.createNamedQuery( queryName );
                for ( String key : params.keySet() )
                {
                    q.setParameter( key, params.get( key ) );
                }
                return q.getSingleResult();
            }
        };
        return (T) getJpaTemplate().execute( cb );
    }

    @Transactional
    public T saveOrUpdate( T entity )
    {
        if ( null == entity.getId() )
        {
            getJpaTemplate().persist( entity );
        }
        else
        {
            entity = getJpaTemplate().merge( entity );
        }
        return entity;
    }

    @Transactional
    public void delete( long id )
    {
        delete( findById( id ) );
    }

    public void delete( T entity )
    {
        getJpaTemplate().remove( entity );
    }
}
