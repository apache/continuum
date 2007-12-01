/**
 * 
 */
package org.apache.maven.continuum.store.api;

import java.util.Date;

import org.apache.maven.continuum.model.project.ProjectGroup;

/**
 * Wraps up retrieval criteria for {@link ProjectGroup}s.
 * 
 * @author <a href='mailto:rinku@apache.org'>Rahul Thakur</a>
 * @version $Id$
 * @since 1.2
 */
public class ProjectGroupQuery implements Query<ProjectGroup>
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

}
