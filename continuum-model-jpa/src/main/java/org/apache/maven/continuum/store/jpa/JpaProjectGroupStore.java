/**
 * 
 */
package org.apache.maven.continuum.store.jpa;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.store.api.EntityNotFoundException;
import org.apache.maven.continuum.store.api.ProjectGroupQuery;
import org.apache.maven.continuum.store.api.Store;
import org.apache.maven.continuum.store.api.StoreException;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 * @since 1.2
 */
public class JpaProjectGroupStore extends StoreSupport implements Store<ProjectGroup, ProjectGroupQuery>
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
        return lookup( null, id );
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.api.Store#lookup(Class, java.lang.Long)
     */
    public ProjectGroup lookup( Class<T> klass, Long id ) throws StoreException, EntityNotFoundException
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
    public List<ProjectGroup> query( ProjectGroupQuery query ) throws StoreException
    {
        Map<String, Object> where = new HashMap<String, Object>();
        StringBuffer sb = new StringBuffer();

        if ( query.hasId() )
        {
            where.put( "id", query.getId() );
            if ( sb.length() > 0 )
                sb.append( "and" );
            sb.append( " projectGroup.id =:id " );
        }
        if ( query.hasDateCreated() )
        {
            where.put( "dateCreated", query.getDateCreated() );
            if ( sb.length() > 0 )
                sb.append( "and" );
            sb.append( " projectGroup.dateCreated =:dateCreated " );
        }
        if ( query.hasDateUpdated() )
        {
            where.put( "dateUpdated", query.getDateUpdated() );
            if ( sb.length() > 0 )
                sb.append( "and" );
            sb.append( " projectGroup.dateUpdated =:dateUpdated " );
        }
        if ( query.hasDescription() )
        {
            where.put( "description", query.getDescription() );
            if ( sb.length() > 0 )
                sb.append( "and" );
            sb.append( " projectGroup.description =:description " );
        }
        if ( query.hasGroupId() )
        {
            where.put( "groupId", query.getGroupId() );
            if ( sb.length() > 0 )
                sb.append( "and" );
            sb.append( " projectGroup.groupId =:groupId " );
        }
        if ( query.hasModelEncoding() )
        {
            where.put( "modelEncoding", query.getModelEncoding() );
            if ( sb.length() > 0 )
                sb.append( "and" );
            sb.append( " projectGroup.modelEncoding =:modelEncoding " );
        }
        if ( query.hasName() )
        {
            where.put( "name", query.getName() );
            if ( sb.length() > 0 )
                sb.append( "and" );
            sb.append( " projectGroup.name =:name " );
        }

        String whereClause = ( sb.length() > 0 ? " where " : "" ) + sb.toString();

        List<ProjectGroup> results =
            find( "select projectGroup from ProjectGroup as projectGroup " + whereClause, where, 0, 0 );

        return results;
    }
}
