package org.apache.maven.continuum.model.project;

import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.maven.continuum.model.CommonUpdatableModelEntity;

/**
 * Template which contains some buildDefinitions
 * 
 * @version $Id$
 */
@Entity
@Table( name = "BUILD_DEFINITION_TEMPLATE" )
public class BuildDefinitionTemplate extends CommonUpdatableModelEntity
{

    /**
     * Field name
     */
    @Basic
    @Column( name = "NAME", nullable = false )
    private String name;

    /**
     * Field continuumDefault
     */
    @Basic
    @Column( name = "FLG_CONTINUUM_DEFAULT", nullable = false )
    private boolean continuumDefault = false;

    /**
     * Field type
     * <p>
     * TODO: Enum?
     */
    @Basic
    @Column( name = "TEMPLATE_TYPE" )
    private String type;

    /**
     * Field buildDefinitions
     */
    @OneToMany
    private List<BuildDefinition> buildDefinitions;

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
     * @return the continuumDefault
     */
    public boolean isContinuumDefault()
    {
        return continuumDefault;
    }

    /**
     * @param continuumDefault
     *            the continuumDefault to set
     */
    public void setContinuumDefault( boolean continuumDefault )
    {
        this.continuumDefault = continuumDefault;
    }

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
     * @return the buildDefinitions
     */
    public List<BuildDefinition> getBuildDefinitions()
    {
        return buildDefinitions;
    }

    /**
     * @param buildDefinitions
     *            the buildDefinitions to set
     */
    public void setBuildDefinitions( List<BuildDefinition> buildDefinitions )
    {
        this.buildDefinitions = buildDefinitions;
    }

}
