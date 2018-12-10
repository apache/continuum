package org.apache.maven.continuum.model.system;

import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apache.maven.continuum.model.CommonUpdatableModelEntity;

/**
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 */
@Entity
@Table( name = "PROFILE" )
public class Profile extends CommonUpdatableModelEntity
{

    /**
     * Field active
     */
    @Basic
    @Column( name = "FLG_ACTIVE", nullable = false )
    private boolean active = false;

    /**
     * Field name
     */
    @Basic
    @Column( name = "NAME", nullable = false )
    private String name;

    /**
     * Field description
     */
    @Basic
    @Column( name = "DESCRIPTION" )
    private String description;

    /**
     * Field scmMode
     * <p>
     * TODO: Enum?
     */
    @Basic
    @Column( name = "SCM_MODE", nullable = false )
    private int scmMode = 0;

    /**
     * Field buildWithoutChanges
     */
    @Basic
    @Column( name = "FLG_BUILD_WITHOUT_CHANGES", nullable = false )
    private boolean buildWithoutChanges = false;

    /**
     * Field jdk
     */
    @OneToOne
    private Installation jdk;

    /**
     * Field builder
     */
    @OneToOne
    private Installation builder;

    /**
     * Field environmentVariables
     */
    @OneToMany
    private List<Installation> environmentVariables;

    /**
     * @return the active
     */
    public boolean isActive()
    {
        return active;
    }

    /**
     * @param active
     *            the active to set
     */
    public void setActive( boolean active )
    {
        this.active = active;
    }

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
     * @return the description
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * @param description
     *            the description to set
     */
    public void setDescription( String description )
    {
        this.description = description;
    }

    /**
     * @return the scmMode
     */
    public int getScmMode()
    {
        return scmMode;
    }

    /**
     * @param scmMode
     *            the scmMode to set
     */
    public void setScmMode( int scmMode )
    {
        this.scmMode = scmMode;
    }

    /**
     * @return the buildWithoutChanges
     */
    public boolean isBuildWithoutChanges()
    {
        return buildWithoutChanges;
    }

    /**
     * @param buildWithoutChanges
     *            the buildWithoutChanges to set
     */
    public void setBuildWithoutChanges( boolean buildWithoutChanges )
    {
        this.buildWithoutChanges = buildWithoutChanges;
    }

    /**
     * @return the jdk
     */
    public Installation getJdk()
    {
        return jdk;
    }

    /**
     * @param jdk
     *            the jdk to set
     */
    public void setJdk( Installation jdk )
    {
        this.jdk = jdk;
    }

    /**
     * @return the builder
     */
    public Installation getBuilder()
    {
        return builder;
    }

    /**
     * @param builder
     *            the builder to set
     */
    public void setBuilder( Installation builder )
    {
        this.builder = builder;
    }

    /**
     * @return the environmentVariables
     */
    public List<Installation> getEnvironmentVariables()
    {
        return environmentVariables;
    }

    /**
     * @param environmentVariables
     *            the environmentVariables to set
     */
    public void setEnvironmentVariables( List<Installation> environmentVariables )
    {
        this.environmentVariables = environmentVariables;
    }

}
