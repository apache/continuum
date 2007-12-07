/**
 * 
 */
package org.apache.maven.continuum.store.jpa;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.store.api.EntityNotFoundException;
import org.apache.maven.continuum.store.api.ProjectNotifierQuery;
import org.apache.maven.continuum.store.api.Store;
import org.apache.maven.continuum.store.api.StoreException;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 * @since 1.2
 */
public class JpaProjectNotifierStore extends StoreSupport implements Store<ProjectNotifier, ProjectNotifierQuery>
{

    /**
     * @{inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.api.Store#delete(java.lang.Object)
     */
    @Transactional( readOnly = false )
    public void delete( ProjectNotifier entity ) throws StoreException
    {
        getJpaTemplate().remove( entity );
    }

    /**
     * @{inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.api.Store#lookup(java.lang.Long)
     */
    public ProjectNotifier lookup( Long id ) throws StoreException, EntityNotFoundException
    {
        return lookup( null, id );
    }

    /**
     * @{inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.api.Store#lookup(Class, java.lang.Long)
     */
    public ProjectNotifier lookup( Class<T> klass, Long id ) throws StoreException, EntityNotFoundException
    {
        return lookup( ProjectNotifier.class, id );
    }

    /**
     * @{inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.api.Store#query(org.apache.maven.continuum.store.api.Query)
     */
    public List<ProjectNotifier> query( ProjectNotifierQuery query ) throws StoreException
    {
        Map<String, Object> where = new HashMap<String, Object>();
        StringBuffer sb = new StringBuffer();

        if ( query.hasId() )
        {
            where.put( "id", query.getId() );
            if ( sb.length() > 0 )
                sb.append( "and" );
            sb.append( " notifier.id =:id " );
        }
        if ( query.hasDateCreated() )
        {
            where.put( "dateCreated", query.getDateCreated() );
            if ( sb.length() > 0 )
                sb.append( "and" );
            sb.append( " notifier.dateCreated =:dateCreated " );
        }
        if ( query.hasDateUpdated() )
        {
            where.put( "dateUpdated", query.getDateUpdated() );
            if ( sb.length() > 0 )
                sb.append( "and" );
            sb.append( " notifier.dateUpdated =:dateUpdated " );
        }
        if ( query.hasModelEncoding() )
        {
            where.put( "modelEncoding", query.getModelEncoding() );
            if ( sb.length() > 0 )
                sb.append( "and" );
            sb.append( " notifier.modelEncoding =:modelEncoding " );
        }
        if ( query.isDefinedOnProject() )
        {
            // TODO: Implement!
            // Need to check what property is setup on the Notifier.
            // May need to add a property ORM mapping to persist.
        }
        if ( query.isUserDefined() )
        {
            // TODO: Implement!
            // Need to check what property is setup on the Notifier.
            // May need to add a property ORM mapping to persist.
        }

        String whereClause = ( sb.length() > 0 ? " where " : "" ) + sb.toString();

        List<ProjectNotifier> results =
            find( "select notifier from ProjectNotifier as notifier " + whereClause, where, 0, 0 );

        return results;
    }

    /**
     * @{inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.api.Store#save(java.lang.Object)
     */
    @Transactional( readOnly = false )
    public ProjectNotifier save( ProjectNotifier entity ) throws StoreException
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
