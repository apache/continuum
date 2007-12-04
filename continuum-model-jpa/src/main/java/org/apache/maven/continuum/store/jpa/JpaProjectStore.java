/**
 * 
 */
package org.apache.maven.continuum.store.jpa;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        Map<String, Object> where = new HashMap<String, Object>();
        StringBuffer sb = new StringBuffer();

        if ( query.hasId() )
        {
            where.put( "id", query.getId() );
            if ( sb.length() > 0 )
                sb.append( "and" );
            sb.append( " project.id =:id " );
        }
        if ( query.hasDateCreated() )
        {
            where.put( "dateCreated", query.getDateCreated() );
            if ( sb.length() > 0 )
                sb.append( "and" );
            sb.append( " project.dateCreated =:dateCreated " );
        }
        if ( query.hasDateUpdated() )
        {
            where.put( "dateUpdated", query.getDateUpdated() );
            if ( sb.length() > 0 )
                sb.append( "and" );
            sb.append( " project.dateUpdated =:dateUpdated " );
        }
        if ( query.hasDescription() )
        {
            where.put( "description", query.getDescription() );
            if ( sb.length() > 0 )
                sb.append( "and" );
            sb.append( " project.description =:description " );
        }
        if ( query.hasGroupId() )
        {
            where.put( "groupId", query.getGroupId() );
            if ( sb.length() > 0 )
                sb.append( "and" );
            sb.append( " project.groupId =:groupId " );
        }
        if ( query.hasModelEncoding() )
        {
            where.put( "modelEncoding", query.getModelEncoding() );
            if ( sb.length() > 0 )
                sb.append( "and" );
            sb.append( " project.modelEncoding =:modelEncoding " );
        }
        if ( query.hasName() )
        {
            where.put( "name", query.getName() );
            if ( sb.length() > 0 )
                sb.append( "and" );
            sb.append( " project.name =:name " );
        }
        if ( query.hasArtifactId() )
        {
            where.put( "artifactId", query.getArtifactId() );
            if ( sb.length() > 0 )
                sb.append( "and" );
            sb.append( " project.artifactId =:artifactId" );
        }
        if ( query.hasBuildNumber() )
        {
            where.put( "buildNumber", query.getBuildNumber() );
            if ( sb.length() > 0 )
                sb.append( "and" );
            sb.append( " project.buildNumber =:buildNumber" );
        }
        if ( query.hasVersion() )
        {
            where.put( "version", query.getVersion() );
            if ( sb.length() > 0 )
                sb.append( "and" );
            sb.append( " project.version =:version" );
        }

        String whereClause = ( sb.length() > 0 ? " where " : "" ) + sb.toString();

        List<Project> results = find( "select project from Project as project " + whereClause, where, 0, 0 );

        return results;
    }

}
