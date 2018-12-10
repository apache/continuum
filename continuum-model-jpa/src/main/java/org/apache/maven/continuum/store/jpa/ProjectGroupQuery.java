/**
 * 
 */
package org.apache.maven.continuum.store.jpa;

import java.util.Date;
import java.util.Map;

import org.apache.maven.continuum.store.api.Query;

/**
 * Wraps up retrieval criteria for {@link ProjectGroup}s.
 * 
 * @author <a href='mailto:rinku@apache.org'>Rahul Thakur</a>
 * @version $Id$
 * @since 1.2
 */
public class ProjectGroupQuery<ProjectGroup> implements Query<ProjectGroup>
{

    /**
     * ProjectGroup creation date criteria.
     */
    private Date dateCreated;

    /**
     * ProjectGroup update date criteria.
     */
    private Date dateUpdated;

    /**
     * ProjectGroup description criteria.
     */
    private String description;

    /**
     * ProjectGroup groupId criteria.
     */
    private String groupId;

    /**
     * ProjectGroup Id criteria.
     */
    private Long id;

    /**
     * ProjectGroup model encoding criteria.
     */
    private String modelEncoding;

    /**
     * ProjectGroup name criteria.
     */
    private String name;

    /**
     * @return
     * 
     */
    public Date getDateCreated()
    {
        return this.dateCreated;
    }

    /**
     * @return
     * @see org.apache.maven.continuum.model.CommonUpdatableEntity#getDateUpdated()
     */
    public Date getDateUpdated()
    {
        return this.dateUpdated;
    }

    /**
     * @return
     * 
     */
    public String getDescription()
    {
        return this.description;
    }

    /**
     * @return
     * 
     */
    public String getGroupId()
    {
        return this.groupId;
    }

    /**
     * Determine if a date of creation was specified in the query.
     * 
     * @return <code>true</code> if a date of creation was specified, else <code>false</code>.
     */
    public boolean hasDateCreated()
    {
        return ( null != this.dateCreated );
    }

    /**
     * Determine if an update date was specified in the query.
     * 
     * @return <code>true</code> if a date of update was specified, else <code>false</code>.
     */
    public boolean hasDateUpdated()
    {
        return ( null != this.dateUpdated );
    }

    /**
     * Determine if there was a Project Group 'description' specified in the query.
     * 
     * @return <code>true</code> if there was a Project Group 'description' specified , else <code>false</code>.
     */
    public boolean hasDescription()
    {
        return ( null != this.description && this.description.length() > 0 );
    }

    /**
     * Determine if there was a Group Id for the {@link ProjectGroup} specified in the query.
     * 
     * @return <code>true</code> if there was a Group Id for the {@link ProjectGroup} specified, else
     *         <code>false</code>.
     */
    public boolean hasGroupId()
    {
        return ( null != this.groupId && this.groupId.length() > 0 );
    }

    /**
     * 
     * @return
     */
    public boolean hasId()
    {
        return ( null != this.id && this.id.longValue() > 0L );
    }

    /**
     * Determine if there was a model encoding specified in the query.
     * 
     * @return <code>true</code> if there was a model encoding specified, else <code>false</code>.
     */
    public boolean hasModelEncoding()
    {
        return ( null != this.modelEncoding && this.modelEncoding.length() > 0 );
    }

    /**
     * Determine if there is a {@link ProjectGroup} name specified in the query.
     * 
     * @return <code>true</code> if there is a {@link ProjectGroup} name specified, else <code>false</code>.
     */
    public boolean hasName()
    {
        return ( null != this.name && this.name.length() > 0 );
    }

    /**
     * @return
     */
    public Long getId()
    {
        return this.id;
    }

    /**
     * @return
     */
    public String getModelEncoding()
    {
        return this.modelEncoding;
    }

    /**
     * @return
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * @param dateCreated
     */
    public void setDateCreated( Date dateCreated )
    {
        this.dateCreated = dateCreated;
    }

    /**
     * @param dateUpdated
     */
    public void setDateUpdated( Date dateUpdated )
    {
        this.dateUpdated = dateUpdated;
    }

    /**
     * @param description
     */
    public void setDescription( String description )
    {
        this.description = description;
    }

    /**
     * @param groupId
     */
    public void setGroupId( String groupId )
    {
        this.groupId = groupId;
    }

    /**
     * @param id
     */
    public void setId( Long id )
    {
        this.id = id;
    }

    /**
     * @param modelEncoding
     */
    public void setModelEncoding( String modelEncoding )
    {
        this.modelEncoding = modelEncoding;
    }

    /**
     * @param name
     */
    public void setName( String name )
    {
        this.name = name;
    }

    /**
     * @{inheritDoc}
     * 
     * @see org.apache.maven.continuum.store.api.Query#toString(java.util.Map)
     */
    public String toString( Map<String, Object> whereClause )
    {
        StringBuffer sb = new StringBuffer();

        if ( this.hasId() )
        {
            whereClause.put( "id", this.getId() );
            if ( sb.length() > 0 )
                sb.append( "and" );
            sb.append( " projectGroup.id =:id " );
        }
        if ( this.hasDateCreated() )
        {
            whereClause.put( "dateCreated", this.getDateCreated() );
            if ( sb.length() > 0 )
                sb.append( "and" );
            sb.append( " projectGroup.dateCreated =:dateCreated " );
        }
        if ( this.hasDateUpdated() )
        {
            whereClause.put( "dateUpdated", this.getDateUpdated() );
            if ( sb.length() > 0 )
                sb.append( "and" );
            sb.append( " projectGroup.dateUpdated =:dateUpdated " );
        }
        if ( this.hasDescription() )
        {
            whereClause.put( "description", this.getDescription() );
            if ( sb.length() > 0 )
                sb.append( "and" );
            sb.append( " projectGroup.description =:description " );
        }
        if ( this.hasGroupId() )
        {
            whereClause.put( "groupId", this.getGroupId() );
            if ( sb.length() > 0 )
                sb.append( "and" );
            sb.append( " projectGroup.groupId =:groupId " );
        }
        if ( this.hasModelEncoding() )
        {
            whereClause.put( "modelEncoding", this.getModelEncoding() );
            if ( sb.length() > 0 )
                sb.append( "and" );
            sb.append( " projectGroup.modelEncoding =:modelEncoding " );
        }
        if ( this.hasName() )
        {
            whereClause.put( "name", this.getName() );
            if ( sb.length() > 0 )
                sb.append( "and" );
            sb.append( " projectGroup.name =:name " );
        }

        if ( sb.length() > 0 )
            sb.insert( 0, " where " );
        sb.insert( 0, "select projectGroup from ProjectGroup as projectGroup " );

        return sb.toString();
    }

}
