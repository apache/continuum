package org.apache.continuum.dao.api;

import org.springframework.dao.DataAccessException;
import org.springframework.orm.ObjectRetrievalFailureException;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public interface GenericDao<T>
{
    Class<T> getPersistentClass();

    T findById( long id )
        throws ObjectRetrievalFailureException;

    @SuppressWarnings("unchecked")
    List<T> findAll();

    @SuppressWarnings("unchecked")
    List<T> findByNamedQueryAndNamedParams( Class<T> entityClass, String queryName, Map<String, Object> params )
        throws DataAccessException;

    T findUniqByNamedQueryAndNamedParams( Class<T> entityClass, String queryName, Map<String, Object> params );

    T saveOrUpdate( T entity );

    void delete( long id );

    void delete( T entity );
}
