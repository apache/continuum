/**
 * 
 */
package org.apache.maven.continuum.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 * Common persistable entity.
 * 
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 */
@MappedSuperclass
public abstract class CommonPersistableEntity implements Serializable
{

    /**
     * Unique persistence identifier.
     * <p>
     * This is <code>null</code> if not persisted.
     */
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "ID" )
    private Long id;

    /**
     * @return the id which is the unique persistence identifier.
     */
    public Long getId()
    {
        return id;
    }

    /**
     * @param id
     *            Unique persistence identifier to set.
     */
    public void setId( Long id )
    {
        this.id = id;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( id == null ) ? 0 : id.hashCode() );
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        CommonPersistableEntity other = (CommonPersistableEntity) obj;
        if ( id == null )
        {
            if ( other.id != null )
                return false;
        }
        else if ( !id.equals( other.id ) )
            return false;
        return true;
    }

}
