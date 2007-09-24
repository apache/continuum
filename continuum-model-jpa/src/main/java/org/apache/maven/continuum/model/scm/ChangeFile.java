package org.apache.maven.continuum.model.scm;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.maven.continuum.model.CommonUpdatableModelEntity;

/**
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 */
@Entity
@Table( name = "CHANGE_FILE" )
public class ChangeFile extends CommonUpdatableModelEntity
{

    /**
     * Field name
     */
    @Basic
    @Column( name = "NAME" )
    private String name;

    /**
     * Field revision
     */
    @Basic
    @Column( name = "REVISION" )
    private String revision;

    /**
     * Field status
     * <p>
     * TODO: Enum?
     */
    @Basic
    @Column( name = "STATUS" )
    private String status;

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName( String name )
    {
        this.name = name;
    }

    /**
     * @return the revision
     */
    public String getRevision()
    {
        return revision;
    }

    /**
     * @param revision
     *            the revision to set
     */
    public void setRevision( String revision )
    {
        this.revision = revision;
    }

    /**
     * @return the status
     */
    public String getStatus()
    {
        return status;
    }

    /**
     * @param status
     *            the status to set
     */
    public void setStatus( String status )
    {
        this.status = status;
    }

}
