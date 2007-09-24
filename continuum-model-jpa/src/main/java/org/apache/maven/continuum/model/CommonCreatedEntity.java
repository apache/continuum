/**
 * 
 */
package org.apache.maven.continuum.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 */
@MappedSuperclass
public abstract class CommonCreatedEntity extends CommonPersistableEntity
{

    /**
     * Date the entity was created.
     */
    @Temporal( TemporalType.TIMESTAMP )
    @Column( name = "DATE_CREATED" )
    private Date dateCreated;

    /**
     * @return the dateCreated
     */
    public Date getDateCreated()
    {
        return dateCreated;
    }

    /**
     * @param dateCreated
     *            the dateCreated to set
     */
    public void setDateCreated( Date dateCreated )
    {
        this.dateCreated = dateCreated;
    }

}
