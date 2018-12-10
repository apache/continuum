/**
 * 
 */
package org.apache.maven.continuum.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

/**
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 */
@MappedSuperclass
public abstract class CommonUpdatableEntity extends CommonCreatedEntity
{

    /**
     * Date the entity was last updated.
     */
    @Temporal( TemporalType.TIMESTAMP )
    @Column( name = "DATE_UPDATED" )
    private Date dateUpdated;

    /**
     * Version for optimistic locking.
     */
    @Version
    @Column( name = "OBJ_VERSION" )
    private long objectVersion;

    /**
     * @return the dateUpdated
     */
    public Date getDateUpdated()
    {
        return dateUpdated;
    }

    /**
     * @param dateUpdated
     *            the dateUpdated to set
     */
    public void setDateUpdated( Date dateUpdated )
    {
        this.dateUpdated = dateUpdated;
    }

    /**
     * @return the version
     */
    public long getObjectVersion()
    {
        return objectVersion;
    }

    /**
     * @param version
     *            the version to set
     */
    public void setObjectVersion( long version )
    {
        this.objectVersion = version;
    }

}
