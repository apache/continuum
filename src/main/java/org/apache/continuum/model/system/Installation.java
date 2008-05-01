package org.apache.continuum.model.system;

import org.apache.continuum.model.CommonUpdatableEntity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 */
@Entity
@Table(name = "INSTALLATION")
public class Installation
    extends CommonUpdatableEntity
{

    /**
     * Field type
     * <p/>
     * TODO: Enum?
     */
    @Basic
    @Column(name = "INSTALLATION_TYPE")
    private String type;

    /**
     * Field varValue
     */
    @Basic
    @Column(name = "VAR_VALUE")
    private String varValue;

    /**
     * Field varName
     */
    @Basic
    @Column(name = "VAR_NAME")
    private String varName;

    /**
     * Field name
     */
    @Basic
    @Column(name = "NAME")
    private String name;

    /**
     * @return the type
     */
    public String getType()
    {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType( String type )
    {
        this.type = type;
    }

    /**
     * @return the varValue
     */
    public String getVarValue()
    {
        return varValue;
    }

    /**
     * @param varValue the varValue to set
     */
    public void setVarValue( String varValue )
    {
        this.varValue = varValue;
    }

    /**
     * @return the varName
     */
    public String getVarName()
    {
        return varName;
    }

    /**
     * @param varName the varName to set
     */
    public void setVarName( String varName )
    {
        this.varName = varName;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName( String name )
    {
        this.name = name;
    }

}
