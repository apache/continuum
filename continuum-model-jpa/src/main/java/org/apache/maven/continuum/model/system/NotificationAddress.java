package org.apache.maven.continuum.model.system;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.maven.continuum.model.CommonUpdatableModelEntity;

/**
 * 
 * Configures one method for notifying users/developers when a build breaks.
 * 
 * @version $Id$
 */
@Entity
@Table( name = "NOTIFICATION_ADDRESS" )
public class NotificationAddress extends CommonUpdatableModelEntity
{

    /**
     * Field type
     * <p>
     * TODO: Enum?
     */
    @Basic
    @Column( name = "ADDRESS_TYPE" )
    private String type = "mail";

    /**
     * Field address
     */
    @Basic
    @Column( name = "ADDRESS" )
    private String address;

    /**
     * Field configuration
     * <p>
     * TODO: Map!
     */
    @Transient
    private java.util.Map configuration;

    /**
     * @return the type
     */
    public String getType()
    {
        return type;
    }

    /**
     * @param type
     *            the type to set
     */
    public void setType( String type )
    {
        this.type = type;
    }

    /**
     * @return the address
     */
    public String getAddress()
    {
        return address;
    }

    /**
     * @param address
     *            the address to set
     */
    public void setAddress( String address )
    {
        this.address = address;
    }

    /**
     * @return the configuration
     */
    public java.util.Map getConfiguration()
    {
        return configuration;
    }

    /**
     * @param configuration
     *            the configuration to set
     */
    public void setConfiguration( java.util.Map configuration )
    {
        this.configuration = configuration;
    }

}
