/**
 * 
 */
package org.apache.maven.continuum.store.jpa;

import java.util.Date;
import java.util.Map;

import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.store.api.Query;

/**
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 * @since 1.2
 */
public class ProjectNotifierQuery<ProjectNotifier> implements Query<ProjectNotifier>
{

    /**
     * ProjectNotifier creation date criteria.
     */
    private Date dateCreated;

    /**
     * ProjectNotifier update date criteria.
     */
    private Date dateUpdated;

    /**
     * ProjectNotifier Id criteria.
     */
    private Long id;

    /**
     * Determines if a ProjectNotifier is set up on a {@link Project} or a {@link ProjectGroup}.
     */
    private boolean isDefinedOnProject = false;

    /**
     * Determines if a {@link ProjectNotifier} is defined by a user.
     */
    private boolean isUserDefined = false;

    /**
     * ProjectNotifier model encoding criteria.
     */
    private String modelEncoding;

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
     * @return the isDefinedOnProject
     */
    public boolean isDefinedOnProject()
    {
        return isDefinedOnProject;
    }

    /**
     * @return the isUserDefined
     */
    public boolean isUserDefined()
    {
        return isUserDefined;
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
     * @param isDefinedOnProject
     *            the isDefinedOnProject to set
     */
    public void setDefinedOnProject( boolean isDefinedOnProject )
    {
        this.isDefinedOnProject = isDefinedOnProject;
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
     * @param isUserDefined
     *            the isUserDefined to set
     */
    public void setUserDefined( boolean isUserDefined )
    {
        this.isUserDefined = isUserDefined;
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
            sb.append( " notifier.id =:id " );
        }
        if ( this.hasDateCreated() )
        {
            whereClause.put( "dateCreated", this.getDateCreated() );
            if ( sb.length() > 0 )
                sb.append( "and" );
            sb.append( " notifier.dateCreated =:dateCreated " );
        }
        if ( this.hasDateUpdated() )
        {
            whereClause.put( "dateUpdated", this.getDateUpdated() );
            if ( sb.length() > 0 )
                sb.append( "and" );
            sb.append( " notifier.dateUpdated =:dateUpdated " );
        }
        if ( this.hasModelEncoding() )
        {
            whereClause.put( "modelEncoding", this.getModelEncoding() );
            if ( sb.length() > 0 )
                sb.append( "and" );
            sb.append( " notifier.modelEncoding =:modelEncoding " );
        }
        if ( this.isDefinedOnProject() )
        {
            // TODO: Implement!
            // Need to check what property is setup on the Notifier.
            // May need to add a property ORM mapping to persist.
        }
        if ( this.isUserDefined() )
        {
            // TODO: Implement!
            // Need to check what property is setup on the Notifier.
            // May need to add a property ORM mapping to persist.
        }

        if ( sb.length() > 0 )
            sb.insert( 0, " where " );
        sb.insert( 0, "select project from Project as project " );

        return sb.toString();
    }

}
